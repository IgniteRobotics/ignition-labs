package frc.testout.t5.plant;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;

/**
 * A kinematic swerve drivetrain -- integrates commanded {@link ChassisSpeeds}, no motors, no
 * Phoenix, deterministic. This is the drivetrain {@code LogRecorder}'s recordings ran against.
 * Given, locked -- part of the fixture. {@code tests.lock} and {@code CODEOWNERS} both cover this
 * file (spec §12.2).
 *
 * <p>Shaped after M5's {@code frc.robot.plant.SwerveDriveSim} (same reason M4's plants and T4's
 * plant are built from physical quantities in code: nothing here needs a vendor simulator, and a
 * plain kinematic integrator is enough to make three recordings that disagree with each other only
 * in the one way each fault is supposed to make them disagree).
 */
public final class T5DriveSim {
  private static final double TRACK_WIDTH_METERS = 0.60;
  private static final double WHEEL_BASE_METERS = 0.60;

  private static final SwerveDriveKinematics KINEMATICS =
      new SwerveDriveKinematics(
          new Translation2d(WHEEL_BASE_METERS / 2, TRACK_WIDTH_METERS / 2),
          new Translation2d(WHEEL_BASE_METERS / 2, -TRACK_WIDTH_METERS / 2),
          new Translation2d(-WHEEL_BASE_METERS / 2, TRACK_WIDTH_METERS / 2),
          new Translation2d(-WHEEL_BASE_METERS / 2, -TRACK_WIDTH_METERS / 2));

  private Pose2d pose = new Pose2d();
  private ChassisSpeeds commanded = new ChassisSpeeds();
  private final double[] wheelDistanceMeters = new double[4];
  private SwerveModuleState[] moduleStates =
      new SwerveModuleState[] {
        new SwerveModuleState(), new SwerveModuleState(),
        new SwerveModuleState(), new SwerveModuleState()
      };

  /** Commands robot-relative chassis speeds; takes effect on the next {@link #step}. */
  public void drive(ChassisSpeeds robotRelativeSpeeds) {
    this.commanded = robotRelativeSpeeds;
  }

  /** Advances true pose, wheel distances, and module states by {@code dtSeconds}. */
  public void step(double dtSeconds) {
    moduleStates = KINEMATICS.toSwerveModuleStates(commanded);
    for (int i = 0; i < 4; i++) {
      wheelDistanceMeters[i] += moduleStates[i].speedMetersPerSecond * dtSeconds;
    }
    Translation2d fieldDelta =
        new Translation2d(
                commanded.vxMetersPerSecond * dtSeconds, commanded.vyMetersPerSecond * dtSeconds)
            .rotateBy(pose.getRotation());
    Rotation2d newRotation =
        pose.getRotation().plus(new Rotation2d(commanded.omegaRadiansPerSecond * dtSeconds));
    pose = new Pose2d(pose.getTranslation().plus(fieldDelta), newRotation);
  }

  /** Ground truth pose. Never available to a real robot's own code -- exists here for logging. */
  public Pose2d truePose() {
    return pose;
  }

  /** Ground truth heading. A recording's odometry estimator may choose to corrupt this. */
  public Rotation2d gyroAngle() {
    return pose.getRotation();
  }

  /** Ground truth per-module velocity and angle, as of the most recent {@link #step}. */
  public SwerveModuleState[] moduleStates() {
    return moduleStates;
  }

  /** Ground truth per-module distance and angle, as of the most recent {@link #step}. */
  public SwerveModulePosition[] modulePositions() {
    SwerveModulePosition[] out = new SwerveModulePosition[4];
    for (int i = 0; i < 4; i++) {
      out[i] = new SwerveModulePosition(wheelDistanceMeters[i], moduleStates[i].angle);
    }
    return out;
  }

  /** The kinematics every module position and state above is defined against. */
  public static SwerveDriveKinematics kinematics() {
    return KINEMATICS;
  }

  /** Teleports the true pose. Does not touch wheel distances -- only used before a run starts. */
  public void resetTo(Pose2d newPose) {
    pose = newPose;
  }
}
