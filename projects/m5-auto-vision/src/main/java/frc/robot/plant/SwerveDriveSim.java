package frc.robot.plant;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.config.PIDConstants;
import com.pathplanner.lib.config.RobotConfig;
import com.pathplanner.lib.controllers.PPHolonomicDriveController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.geometry.Twist2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import java.io.IOException;
import org.json.simple.parser.ParseException;

/**
 * A deterministic kinematic swerve drivetrain: it integrates whatever {@link ChassisSpeeds} {@link
 * #drive} was last called with, perfectly, every {@link #step}. No motors, no Phoenix, no motor lag
 * -- building a real swerve drivetrain is not this milestone's lesson, composing autos and fusing
 * vision is.
 *
 * <p><b>{@link #gyroAngle()} carries a fixed-rate drift that {@link #truePose()} never sees.</b> A
 * real gyro drifts over a match; this plant models that as a constant bias rather than sampled
 * noise, specifically so the drift -- and therefore the gap between an odometry-only pose estimate
 * and the truth -- is bit-identical across runs. {@link #truePose()} is ground truth; given tests
 * use it and you should not build your own pose estimate from it.
 *
 * <p><b>{@code AutoBuilder} is already configured, against this plant's own ground truth,</b> not
 * against any pose estimate you build in {@code RobotContainer} -- this class is constructed and
 * tested standalone, before your code exists. {@link
 * com.pathplanner.lib.auto.AutoBuilder#followPath} and {@code AutoBuilder.buildAutoChooser()} are
 * ready to call the moment this constructor returns.
 */
public final class SwerveDriveSim {

  private static final double TRACK_WIDTH_METERS = 0.6;
  private static final double WHEEL_BASE_METERS = 0.6;

  // A real gyro's drift is not zero-mean over a match; modeling it as a fixed rate rather
  // than sampled noise is what keeps this whole plant bit-identical across runs (Task 2
  // Step 6 proves it) while still giving odometry-only pose estimation something real to
  // fail at correcting.
  private static final double GYRO_DRIFT_RADIANS_PER_SECOND = 0.010;

  private static final Translation2d FRONT_LEFT =
      new Translation2d(WHEEL_BASE_METERS / 2.0, TRACK_WIDTH_METERS / 2.0);
  private static final Translation2d FRONT_RIGHT =
      new Translation2d(WHEEL_BASE_METERS / 2.0, -TRACK_WIDTH_METERS / 2.0);
  private static final Translation2d BACK_LEFT =
      new Translation2d(-WHEEL_BASE_METERS / 2.0, TRACK_WIDTH_METERS / 2.0);
  private static final Translation2d BACK_RIGHT =
      new Translation2d(-WHEEL_BASE_METERS / 2.0, -TRACK_WIDTH_METERS / 2.0);

  private static final SwerveDriveKinematics KINEMATICS =
      new SwerveDriveKinematics(FRONT_LEFT, FRONT_RIGHT, BACK_LEFT, BACK_RIGHT);

  private final double[] moduleDistanceMeters = new double[4];
  private final Rotation2d[] moduleAngles = {
    Rotation2d.kZero, Rotation2d.kZero, Rotation2d.kZero, Rotation2d.kZero
  };

  private Pose2d truePose = Pose2d.kZero;
  private ChassisSpeeds commandedSpeeds = new ChassisSpeeds();
  private double gyroDriftRadians = 0.0;

  public SwerveDriveSim() {
    try {
      RobotConfig config = RobotConfig.fromGUISettings();
      AutoBuilder.configure(
          this::truePose,
          this::resetTo,
          this::currentSpeeds,
          this::drive,
          new PPHolonomicDriveController(new PIDConstants(5.0, 0.0), new PIDConstants(3.0, 0.0)),
          config,
          () -> DriverStation.getAlliance().orElse(Alliance.Blue) == Alliance.Red);
    } catch (IOException | ParseException e) {
      throw new IllegalStateException(
          "Could not load src/main/deploy/pathplanner/settings.json -- it ships with the "
              + "milestone and should never be missing or malformed. If you see this on a "
              + "fresh clone, show a mentor.",
          e);
    }
  }

  /**
   * Commands robot-relative chassis speeds. Takes effect on the next {@link #step}.
   *
   * @param speeds robot-relative
   */
  public void drive(ChassisSpeeds speeds) {
    commandedSpeeds = speeds;
  }

  /**
   * Advances the true pose and every module's odometer by one timestep, from whatever {@link
   * #drive} was last called with. {@code Robot.simulationPeriodic()} calls this once per 20ms tick;
   * given tests call it directly in a stepped loop.
   *
   * @param dtSeconds timestep, seconds
   */
  public void step(double dtSeconds) {
    truePose =
        truePose.exp(
            new Twist2d(
                commandedSpeeds.vxMetersPerSecond * dtSeconds,
                commandedSpeeds.vyMetersPerSecond * dtSeconds,
                commandedSpeeds.omegaRadiansPerSecond * dtSeconds));

    SwerveModuleState[] states = KINEMATICS.toSwerveModuleStates(commandedSpeeds);
    for (int i = 0; i < states.length; i++) {
      moduleDistanceMeters[i] += states[i].speedMetersPerSecond * dtSeconds;
      moduleAngles[i] = states[i].angle;
    }

    gyroDriftRadians += GYRO_DRIFT_RADIANS_PER_SECOND * dtSeconds;
  }

  /**
   * @return ground truth. Given tests use this to score your pose estimate; your own {@code
   *     RobotContainer} should not read it.
   */
  public Pose2d truePose() {
    return truePose;
  }

  /**
   * @return the simulated gyro heading -- {@link #truePose()}'s rotation plus this plant's
   *     fixed-rate drift. This is what a real gyro reading looks like; build your odometry from it,
   *     not from {@link #truePose()}.
   */
  public Rotation2d gyroAngle() {
    return truePose.getRotation().plus(Rotation2d.fromRadians(gyroDriftRadians));
  }

  /**
   * @return each module's position, front-left/front-right/back-left/back-right, matching {@link
   *     #kinematics()}'s construction order
   */
  public SwerveModulePosition[] modulePositions() {
    SwerveModulePosition[] positions = new SwerveModulePosition[4];
    for (int i = 0; i < 4; i++) {
      positions[i] = new SwerveModulePosition(moduleDistanceMeters[i], moduleAngles[i]);
    }
    return positions;
  }

  /**
   * @return the robot-relative speeds last commanded through {@link #drive}
   */
  public ChassisSpeeds currentSpeeds() {
    return commandedSpeeds;
  }

  /**
   * @return the kinematics every module position/state above is defined against: front-left,
   *     front-right, back-left, back-right, each ±0.3m from center
   */
  public static SwerveDriveKinematics kinematics() {
    return KINEMATICS;
  }

  /**
   * Teleports the true pose. Does not touch module odometers or the gyro's accumulated drift --
   * those are hardware state that a pose reset does not rezero on a real robot either.
   *
   * @param pose the new true pose
   */
  public void resetTo(Pose2d pose) {
    truePose = pose;
  }
}
