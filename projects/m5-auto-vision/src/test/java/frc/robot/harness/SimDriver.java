package frc.robot.harness;

import static org.junit.jupiter.api.Assertions.fail;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.simulation.DriverStationSim;
import edu.wpi.first.wpilibj.simulation.SimHooks;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.Constants;
import frc.robot.RobotContainer;
import frc.robot.plant.ScoringMechanism;
import frc.robot.plant.SwerveDriveSim;
import frc.robot.plant.VisionSim;
import java.util.Optional;
import org.photonvision.targeting.PhotonPipelineResult;

/**
 * Given test support. Part of the milestone -- do not edit.
 *
 * <p>Builds exactly one {@code RobotContainer} for the whole run, turns a stub's {@code
 * UnsupportedOperationException} into a sentence you can act on, and owns the two stepped loops
 * every given test drives your work through: {@link #runOdometryLoop} for pose fusion, {@link
 * #runCommandToCompletion} for the auto routine.
 */
public final class SimDriver {
  // A tight circle (radius 1.5m, period ~9.4s) centered on the k2026RebuiltWelded tag cluster
  // at (4.6, 4.0) -- not the wide arc an earlier draft used. FusedPoseTest's 30s window needs
  // the camera to hold targets for the WHOLE run, not just "most of" a shorter one; a wide loop
  // spends most of a 30s window far outside sight range (measured: 39% visibility, max 7.0m from
  // the cluster) where vision cannot correct anything. This pattern measured 95.9% visibility,
  // max 1.5m from the cluster, over 1500 ticks.
  private static final Pose2d ODOMETRY_LOOP_START = new Pose2d(4.6, 2.5, Rotation2d.kZero);
  private static final ChassisSpeeds ODOMETRY_LOOP_SPEEDS = new ChassisSpeeds(1.0, 0.0, 0.667);

  private static RobotContainer container;
  private static AssertionError containerFailure;

  private SimDriver() {}

  /** Call from {@code @BeforeEach}. */
  public static void setUp() {
    if (!HAL.initialize(500, 0)) {
      fail(
          "The WPILib hardware layer would not start. This is a toolchain problem, not "
              + "something you did -- show a mentor.");
    }
    DriverStationSim.setDsAttached(true);
    DriverStationSim.setEnabled(true);
    waitUntilEnabled();
    SimHooks.pauseTiming();
    CommandScheduler.getInstance().cancelAll();
    container();
  }

  /** Call from {@code @AfterEach}. */
  public static void tearDown() {
    CommandScheduler.getInstance().cancelAll();
    CommandScheduler.getInstance().run();
    SimHooks.resumeTiming();
  }

  private static void waitUntilEnabled() {
    for (int i = 0; i < 100 && !DriverStation.isEnabled(); i++) {
      DriverStationSim.notifyNewData();
    }
    if (!DriverStation.isEnabled()) {
      fail(
          "The simulated driver station never reported the robot as enabled. This is a "
              + "toolchain problem -- show a mentor.");
    }
  }

  /** Your {@code RobotContainer}, or a readable failure explaining why it could not be had. */
  public static RobotContainer container() {
    if (containerFailure != null) {
      throw containerFailure;
    }
    if (container == null) {
      try {
        container = new RobotContainer();
      } catch (RuntimeException | Error e) {
        try {
          fail(
              "Building your RobotContainer threw "
                  + describe(e)
                  + "\nEvery test in this milestone has to build one first, so nothing can pass "
                  + "until that constructor runs cleanly. Start there.");
        } catch (AssertionError failure) {
          containerFailure = failure;
          throw failure;
        }
      }
    }
    return container;
  }

  private static String describe(Throwable e) {
    return e.getClass().getSimpleName() + (e.getMessage() == null ? "" : ": " + e.getMessage());
  }

  private static SwerveDriveSim drivetrain() {
    SwerveDriveSim drivetrain;
    try {
      drivetrain = container().drivetrain();
    } catch (UnsupportedOperationException e) {
      return fail(
          "drivetrain() in RobotContainer still throws UnsupportedOperationException. "
              + "Construct a SwerveDriveSim in your constructor and return it.");
    }
    if (drivetrain == null) {
      return fail("drivetrain() returned null. The harness needs the real drivetrain back.");
    }
    return drivetrain;
  }

  private static VisionSim vision() {
    VisionSim vision;
    try {
      vision = container().vision();
    } catch (UnsupportedOperationException e) {
      return fail(
          "vision() in RobotContainer still throws UnsupportedOperationException. Construct a "
              + "VisionSim in your constructor and return it.");
    }
    if (vision == null) {
      return fail("vision() returned null. The harness needs the real VisionSim back.");
    }
    return vision;
  }

  /**
   * The student's {@link ScoringMechanism}, or a readable failure if it is still a stub. Public
   * because {@code AutoTerminationTest} faults the mechanism directly; going through {@code
   * container().mechanism()} instead would let a raw {@code UnsupportedOperationException} reach
   * the student as a bare stack trace, which is the one thing this class exists to prevent.
   */
  public static ScoringMechanism mechanism() {
    ScoringMechanism mechanism;
    try {
      mechanism = container().mechanism();
    } catch (UnsupportedOperationException e) {
      return fail(
          "mechanism() in RobotContainer still throws UnsupportedOperationException. "
              + "Construct a ScoringMechanism in your constructor and return it.");
    }
    if (mechanism == null) {
      return fail("mechanism() returned null. The harness needs the real ScoringMechanism back.");
    }
    return mechanism;
  }

  /**
   * A fresh instance of the student's auto routine, or a readable failure if it is still a stub.
   */
  public static Command autoRoutine() {
    Command routine;
    try {
      routine = container().autoRoutine();
    } catch (UnsupportedOperationException e) {
      return fail(
          "autoRoutine() in RobotContainer still throws UnsupportedOperationException. Compose "
              + "your paths and named commands into a single command and return a NEW one each "
              + "call.");
    }
    if (routine == null) {
      return fail("autoRoutine() returned null. The harness needs a real command back.");
    }
    return routine;
  }

  /** Seeds the student's pose estimator, or fails readably if it is still a stub. */
  public static void resetEstimator(Pose2d pose) {
    try {
      container().resetEstimator(pose);
    } catch (UnsupportedOperationException e) {
      fail(
          "resetEstimator() in RobotContainer still throws UnsupportedOperationException. Seed "
              + "your pose estimator with the pose it's given.");
    }
  }

  /** The student's filter decision, or a readable failure if it is still a stub. */
  public static boolean acceptsMeasurement(Pose2d visionPose, double timestampSeconds) {
    try {
      return container().acceptsMeasurement(visionPose, timestampSeconds);
    } catch (UnsupportedOperationException e) {
      return fail(
          "acceptsMeasurement() in RobotContainer still throws UnsupportedOperationException. "
              + "Decide whether to trust a vision pose enough to fuse it.");
    }
  }

  /**
   * Runs {@code iterations} ticks of: drive a fixed pattern, step the drivetrain, update vision,
   * call {@code updateOdometry()}, and -- only if {@code feedVision} is true -- offer the camera's
   * latest frame to {@code acceptsMeasurement()}/{@code addVisionMeasurement()}, the same given
   * conversion {@code Robot.java} uses. Resets the drivetrain's true pose and your estimator to the
   * same fixed starting pose every call. Returns the distance between true pose and {@code
   * estimatedPose()} after each tick.
   */
  public static double[] runOdometryLoop(int iterations, boolean feedVision) {
    drivetrain().resetTo(ODOMETRY_LOOP_START);
    resetEstimator(ODOMETRY_LOOP_START);

    double[] error = new double[iterations];
    for (int i = 0; i < iterations; i++) {
      drivetrain().drive(ODOMETRY_LOOP_SPEEDS);
      drivetrain().step(Constants.SIM_STEP_SECONDS);
      vision().update(drivetrain().truePose());

      try {
        container().updateOdometry();
      } catch (UnsupportedOperationException e) {
        fail(
            "updateOdometry() in RobotContainer still throws UnsupportedOperationException. "
                + "Update your pose estimator from drivetrain().gyroAngle() and "
                + "drivetrain().modulePositions() -- wheels and gyro only, no vision.");
      }

      if (feedVision) {
        PhotonPipelineResult result = vision().latestResult();
        Optional<Pose2d> candidate = VisionPoseHelper.candidatePose(result);
        if (candidate.isPresent()) {
          Pose2d visionPose = candidate.get();
          double timestamp = VisionPoseHelper.timestampSeconds(result);
          boolean accepted;
          try {
            accepted = container().acceptsMeasurement(visionPose, timestamp);
          } catch (UnsupportedOperationException e) {
            return fail(
                "acceptsMeasurement() in RobotContainer still throws "
                    + "UnsupportedOperationException. Decide whether to trust a vision pose "
                    + "here.");
          }
          if (accepted) {
            try {
              container().addVisionMeasurement(visionPose, timestamp);
            } catch (UnsupportedOperationException e) {
              return fail(
                  "addVisionMeasurement() in RobotContainer still throws "
                      + "UnsupportedOperationException. Fuse it into your pose estimator here.");
            }
          }
        }
      }

      Pose2d estimated;
      try {
        estimated = container().estimatedPose();
      } catch (UnsupportedOperationException e) {
        return fail(
            "estimatedPose() in RobotContainer still throws UnsupportedOperationException. "
                + "Return your pose estimator's current estimate.");
      }
      error[i] = drivetrain().truePose().getTranslation().getDistance(estimated.getTranslation());

      SimHooks.stepTiming(Constants.SIM_STEP_SECONDS);
    }
    return error;
  }

  /**
   * Schedules {@code command} and runs the scheduler for up to {@code maxIterations} loops,
   * stepping the drivetrain, vision, and scoring mechanism once per loop -- the same pairing {@code
   * Robot.simulationPeriodic()} does. Returns whether the scheduler reported the command finished
   * within that budget.
   */
  public static boolean runCommandToCompletion(Command command, int maxIterations) {
    try {
      CommandScheduler.getInstance().schedule(command);
    } catch (IllegalArgumentException e) {
      return fail("Could not schedule that command -- " + e.getMessage());
    }
    for (int i = 0; i < maxIterations; i++) {
      boolean wasScheduled = CommandScheduler.getInstance().isScheduled(command);
      CommandScheduler.getInstance().run();
      drivetrain().step(Constants.SIM_STEP_SECONDS);
      vision().update(drivetrain().truePose());
      mechanism().step(Constants.SIM_STEP_SECONDS);
      SimHooks.stepTiming(Constants.SIM_STEP_SECONDS);
      boolean isScheduledNow = CommandScheduler.getInstance().isScheduled(command);
      if (wasScheduled && !isScheduledNow) {
        return true;
      }
    }
    return false;
  }
}
