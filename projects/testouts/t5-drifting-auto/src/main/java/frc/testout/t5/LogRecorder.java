package frc.testout.t5;

import com.pathplanner.lib.config.ModuleConfig;
import com.pathplanner.lib.config.RobotConfig;
import com.pathplanner.lib.path.GoalEndState;
import com.pathplanner.lib.path.IdealStartingState;
import com.pathplanner.lib.path.PathConstraints;
import com.pathplanner.lib.path.PathPlannerPath;
import com.pathplanner.lib.path.Waypoint;
import com.pathplanner.lib.trajectory.PathPlannerTrajectory;
import com.pathplanner.lib.trajectory.PathPlannerTrajectoryState;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveOdometry;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.util.datalog.DataLogWriter;
import edu.wpi.first.util.datalog.StringLogEntry;
import edu.wpi.first.util.datalog.StructArrayLogEntry;
import edu.wpi.first.util.datalog.StructLogEntry;
import frc.testout.t5.plant.T5DriveSim;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Runs one auto routine against {@link T5DriveSim} with exactly one fault injected, and writes
 * every tick to a {@code .wpilog}. Given, locked -- part of the fixture. {@code tests.lock} and
 * {@code CODEOWNERS} both cover this file (spec §12.2).
 *
 * <p>This is a recording, not a synthesized trace: the same auto runs against the same drivetrain
 * three times, once per {@link Fault}, and every signal in the resulting log came from that one
 * execution. See {@code superpowers/specs/2026-08-16-m5-auto-vision-design.md} §3 for why that
 * matters.
 *
 * <p>The routine is three steps: follow a path, run a named command ({@code step2}), run a second
 * named command ({@code step3}). {@link Fault#ODOMETRY_DRIFT} biases the gyro angle fed to the
 * odometry estimator (never the true pose) so the path-following controller -- which only ever sees
 * the estimate, exactly like a real robot -- drives the *true* robot off the intended path while
 * believing it is on target. {@link Fault#MISTUNED_CONTROLLER} swaps in a proportional gain too low
 * to track the path's speed, producing a large, persistent gap between the path's target pose and
 * the estimate while the estimate itself stays accurate. {@link Fault#BLOCKING_COMMAND} makes
 * {@code step2} never report finished, so {@code step2:end}, {@code step3:start} and {@code
 * step3:end} never appear -- the recording window closes with the routine still waiting on it.
 *
 * <p><b>Every recording is exactly {@link #TOTAL_RECORD_SECONDS} long, whichever fault it
 * carries.</b> A run whose routine finishes early keeps recording the robot sitting idle until the
 * window closes. That is deliberate and it is not padding: those trailing ticks are real ticks of
 * the same simulation, stepped and logged like every other tick, and a real robot's log does not
 * stop the microsecond auto ends either. The reason it matters is that file length would otherwise
 * identify the blocking run from {@code ls -l} alone, without opening the log -- a free giveaway,
 * where the regeneration shortcut this fixture knowingly accepts (see {@code logs/README.md}) at
 * least costs three Gradle runs and knowing the trick. With the window fixed, what separates a
 * blocked run from a healthy one is the event markers, which is the signal the fixture means to
 * test.
 *
 * <p>A fault stays injected for the whole window, not just the routine: {@link
 * Fault#ODOMETRY_DRIFT} is a property of the gyro, and a biased gyro does not stop being biased
 * because the routine ended. So the drift keeps accumulating through the idle tail, and the
 * estimated heading keeps rotating while the true heading sits still.
 *
 * <p>The output file is always named {@code recording.wpilog} inside {@code outputDirectory} --
 * deliberately independent of which fault was recorded. Renaming it into {@code logs/} is the
 * caller's job (see {@link GenerateLog}), specifically so nothing in this class ever has to know or
 * reveal the letter a run will end up as.
 */
public final class LogRecorder {
  /** One root cause per recorded run. */
  public enum Fault {
    ODOMETRY_DRIFT,
    MISTUNED_CONTROLLER,
    BLOCKING_COMMAND
  }

  /**
   * Length of every recording, regardless of fault. See the class Javadoc for why this is fixed
   * rather than "however long the routine took." Long enough that the blocking run reads as
   * unmistakably stuck -- it spends 3.7 s parked after {@code step2:start}, longer than the entire
   * 3.2 s routine -- and short enough to keep {@link #GYRO_DRIFT_RAD_PER_TICK} below the wrap
   * ceiling described there. Those two pull in opposite directions, so neither constant can be
   * changed without re-checking the other against the discrimination matrix.
   */
  private static final double TOTAL_RECORD_SECONDS = 6.0;

  private static final double DT_SECONDS = 0.02;
  private static final long DT_MICROS = (long) (DT_SECONDS * 1_000_000);
  private static final double STEP2_NOMINAL_SECONDS = 0.5;
  private static final double STEP3_NOMINAL_SECONDS = 0.4;

  /**
   * Gyro bias accumulated per tick, applied for the whole window. <b>The product of this and {@link
   * #TOTAL_TICKS} must stay under pi radians.</b> {@link edu.wpi.first.math.geometry.Rotation2d}
   * normalizes to (-180, 180], so a run that accumulates more than half a turn plots the estimated
   * heading as a sawtooth that snaps at the wrap point and lands back near the true heading -- a
   * log whose final heading error reads as roughly zero for the one fault that is entirely about
   * heading. At 0.0080 over 300 ticks the ramp ends at 137.5 degrees: monotonic, unmissable, and 42
   * degrees clear of the ceiling.
   */
  private static final double GYRO_DRIFT_RAD_PER_TICK = 0.0080;

  private static final double GOOD_KP = 12.0;
  private static final double GOOD_KP_THETA = 10.0;
  private static final double BAD_KP = 2.5;
  private static final double BAD_KP_THETA = 2.0;

  /**
   * Total ticks in one recording. Every phase length below is counted in ticks rather than
   * accumulated in seconds, so all three faults produce exactly this many samples per signal --
   * summing 0.02 three hundred times would not.
   */
  private static final int TOTAL_TICKS = (int) Math.round(TOTAL_RECORD_SECONDS / DT_SECONDS);

  private LogRecorder() {}

  /**
   * Runs the fixture's one auto routine once, with {@code fault} injected, and writes the result to
   * {@code outputDirectory/recording.wpilog}.
   *
   * @param fault which root cause to inject into this run
   * @param outputDirectory directory to write into; created if it does not exist
   * @return the path actually written
   */
  public static Path record(Fault fault, Path outputDirectory) throws IOException {
    Files.createDirectories(outputDirectory);
    Path out = outputDirectory.resolve("recording.wpilog");

    RobotConfig config = robotConfig();
    PathPlannerTrajectory trajectory = trajectory(config);

    double kP = fault == Fault.MISTUNED_CONTROLLER ? BAD_KP : GOOD_KP;
    double kPTheta = fault == Fault.MISTUNED_CONTROLLER ? BAD_KP_THETA : GOOD_KP_THETA;

    T5DriveSim drive = new T5DriveSim();
    SwerveDriveOdometry odometry =
        new SwerveDriveOdometry(
            T5DriveSim.kinematics(), drive.gyroAngle(), drive.modulePositions(), new Pose2d());

    // Timestamp 0 means "use wall-clock now" to every WPILib DataLog call that takes one --
    // entry creation as well as data appends. Passing 0 anywhere here would make two
    // regenerations of the same fault differ byte-for-byte even though every value in them is
    // identical. Start one simulated tick in so every timestamp this method ever passes,
    // including each entry's own creation time, is the deterministic simulated clock.
    long startMicros = DT_MICROS;

    try (DataLogWriter log = new DataLogWriter(out.toString())) {
      StructLogEntry<Pose2d> truePoseLog =
          StructLogEntry.create(log, "/t5/truePose", Pose2d.struct, startMicros);
      StructLogEntry<Pose2d> estimatedPoseLog =
          StructLogEntry.create(log, "/t5/estimatedPose", Pose2d.struct, startMicros);
      StructLogEntry<Pose2d> pathTargetPoseLog =
          StructLogEntry.create(log, "/t5/pathTargetPose", Pose2d.struct, startMicros);
      StructArrayLogEntry<SwerveModuleState> moduleStatesLog =
          StructArrayLogEntry.create(
              log, "/t5/moduleStates", SwerveModuleState.struct, startMicros);
      StringLogEntry eventsLog = new StringLogEntry(log, "/t5/events", startMicros);

      // The tick counter is the clock. micros is always derived from it, never accumulated, so
      // every fault's samples land on exactly the same timestamps.
      int tick = 0;
      double gyroDriftRad = 0.0;

      eventsLog.append("path:start", startMicros);
      double pathEndSeconds = trajectory.getTotalTimeSeconds();
      double tSeconds = 0.0;
      while (tSeconds < pathEndSeconds) {
        long micros = startMicros + (long) tick * DT_MICROS;
        PathPlannerTrajectoryState target = trajectory.sample(tSeconds);
        Pose2d estimated = odometry.getPoseMeters();

        Translation2d posError = target.pose.getTranslation().minus(estimated.getTranslation());
        double rotErrorRad = target.pose.getRotation().minus(estimated.getRotation()).getRadians();
        ChassisSpeeds fieldSpeeds =
            new ChassisSpeeds(kP * posError.getX(), kP * posError.getY(), kPTheta * rotErrorRad);
        drive.drive(ChassisSpeeds.fromFieldRelativeSpeeds(fieldSpeeds, estimated.getRotation()));
        drive.step(DT_SECONDS);

        Rotation2d gyroForOdometry = drive.gyroAngle();
        if (fault == Fault.ODOMETRY_DRIFT) {
          gyroDriftRad += GYRO_DRIFT_RAD_PER_TICK;
          gyroForOdometry = gyroForOdometry.plus(new Rotation2d(gyroDriftRad));
        }
        odometry.update(gyroForOdometry, drive.modulePositions());

        truePoseLog.append(drive.truePose(), micros);
        estimatedPoseLog.append(odometry.getPoseMeters(), micros);
        pathTargetPoseLog.append(target.pose, micros);
        moduleStatesLog.append(drive.moduleStates(), micros);

        tSeconds += DT_SECONDS;
        tick++;
      }
      eventsLog.append("path:end", startMicros + (long) tick * DT_MICROS);

      Pose2d endPose = trajectory.getEndState().pose;
      eventsLog.append("step2:start", startMicros + (long) tick * DT_MICROS);

      if (fault == Fault.BLOCKING_COMMAND) {
        // step2 never reports finished, so nothing after step2:start ever happened: no step2:end,
        // no step3 markers. The robot holds where the path left it until the recording window
        // closes, which is what the remaining ticks are.
        runHoldingStep(
            drive,
            odometry,
            fault,
            gyroDriftRad,
            endPose,
            startMicros + (long) tick * DT_MICROS,
            truePoseLog,
            estimatedPoseLog,
            pathTargetPoseLog,
            moduleStatesLog,
            TOTAL_TICKS - tick);
        log.flush();
        return out;
      }

      int step2Ticks = ticksFor(STEP2_NOMINAL_SECONDS);
      gyroDriftRad =
          runHoldingStep(
              drive,
              odometry,
              fault,
              gyroDriftRad,
              endPose,
              startMicros + (long) tick * DT_MICROS,
              truePoseLog,
              estimatedPoseLog,
              pathTargetPoseLog,
              moduleStatesLog,
              step2Ticks);
      tick += step2Ticks;
      eventsLog.append("step2:end", startMicros + (long) tick * DT_MICROS);

      eventsLog.append("step3:start", startMicros + (long) tick * DT_MICROS);
      int step3Ticks = ticksFor(STEP3_NOMINAL_SECONDS);
      gyroDriftRad =
          runHoldingStep(
              drive,
              odometry,
              fault,
              gyroDriftRad,
              endPose,
              startMicros + (long) tick * DT_MICROS,
              truePoseLog,
              estimatedPoseLog,
              pathTargetPoseLog,
              moduleStatesLog,
              step3Ticks);
      tick += step3Ticks;
      eventsLog.append("step3:end", startMicros + (long) tick * DT_MICROS);

      // Trailing idle telemetry. The routine is over; the log is not, because every recording is
      // the same length whichever fault it carries. No further markers -- nothing further happens.
      runHoldingStep(
          drive,
          odometry,
          fault,
          gyroDriftRad,
          endPose,
          startMicros + (long) tick * DT_MICROS,
          truePoseLog,
          estimatedPoseLog,
          pathTargetPoseLog,
          moduleStatesLog,
          TOTAL_TICKS - tick);

      log.flush();
    }
    return out;
  }

  /** Ticks in {@code seconds} of simulated time, rounded rather than accumulated. */
  private static int ticksFor(double seconds) {
    return (int) Math.round(seconds / DT_SECONDS);
  }

  /**
   * Holds the robot at rest (a stand-in for a named command that does not move the drivetrain, such
   * as an intake or shooter action) for {@code ticks} ticks of simulated time, logging every tick
   * the same way the path-following phase does.
   *
   * @return the accumulated gyro drift at the end of the step, so the caller can carry it into the
   *     next one. A gyro bias is cumulative across a whole run; a drift that restarted from the
   *     same value at every phase boundary would put a step in the estimated heading that no real
   *     sensor produces, and this fixture is read by mentors looking for exactly that kind of
   *     artifact.
   */
  private static double runHoldingStep(
      T5DriveSim drive,
      SwerveDriveOdometry odometry,
      Fault fault,
      double gyroDriftRadAtStart,
      Pose2d holdTargetPose,
      long startMicros,
      StructLogEntry<Pose2d> truePoseLog,
      StructLogEntry<Pose2d> estimatedPoseLog,
      StructLogEntry<Pose2d> pathTargetPoseLog,
      StructArrayLogEntry<SwerveModuleState> moduleStatesLog,
      int ticks) {
    double gyroDriftRad = gyroDriftRadAtStart;
    for (int i = 0; i < ticks; i++) {
      long micros = startMicros + (long) i * DT_MICROS;
      drive.drive(new ChassisSpeeds());
      drive.step(DT_SECONDS);

      Rotation2d gyroForOdometry = drive.gyroAngle();
      if (fault == Fault.ODOMETRY_DRIFT) {
        gyroDriftRad += GYRO_DRIFT_RAD_PER_TICK;
        gyroForOdometry = gyroForOdometry.plus(new Rotation2d(gyroDriftRad));
      }
      odometry.update(gyroForOdometry, drive.modulePositions());

      truePoseLog.append(drive.truePose(), micros);
      estimatedPoseLog.append(odometry.getPoseMeters(), micros);
      pathTargetPoseLog.append(holdTargetPose, micros);
      moduleStatesLog.append(drive.moduleStates(), micros);
    }
    return gyroDriftRad;
  }

  private static RobotConfig robotConfig() {
    double halfTrackMeters = 0.30;
    // .withReduction(6.75) matters: PathPlannerLib derives each module's available accel torque
    // from the motor's current draw AT the module's max wheel speed, with no separate gear-ratio
    // parameter of its own -- it expects the DCMotor passed in to already represent the module's
    // output shaft. Passing the bare motor (no reduction) makes that "max speed" correspond to a
    // motor shaft speed close to stall, so the torque budget for actually accelerating collapses
    // to near zero and every generated trajectory silently comes back with getTotalTimeSeconds()
    // == 0 -- no exception, no warning, just a path that "takes no time" to drive.
    return new RobotConfig(
        55.0,
        6.0,
        new ModuleConfig(0.048, 4.5, 1.2, DCMotor.getKrakenX60(1).withReduction(6.75), 60.0, 1),
        new Translation2d(halfTrackMeters, halfTrackMeters),
        new Translation2d(halfTrackMeters, -halfTrackMeters),
        new Translation2d(-halfTrackMeters, halfTrackMeters),
        new Translation2d(-halfTrackMeters, -halfTrackMeters));
  }

  private static PathPlannerTrajectory trajectory(RobotConfig config) {
    List<Waypoint> waypoints =
        PathPlannerPath.waypointsFromPoses(
            new Pose2d(0, 0, Rotation2d.kZero), new Pose2d(3.0, 1.0, Rotation2d.fromDegrees(90)));
    PathPlannerPath path =
        new PathPlannerPath(
            waypoints,
            new PathConstraints(3.0, 3.0, 2 * Math.PI, 4 * Math.PI),
            new IdealStartingState(0.0, Rotation2d.kZero),
            new GoalEndState(0.0, Rotation2d.fromDegrees(90)));
    return path.generateTrajectory(new ChassisSpeeds(), Rotation2d.kZero, config);
  }
}
