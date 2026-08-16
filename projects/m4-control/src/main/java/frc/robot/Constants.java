package frc.robot;

/**
 * Numbers that describe the robot, in one place with names on them.
 *
 * <p>No gain lives here — not a kS, kV, kA, kG, kP, or kD, and not a number that happens to equal
 * one. Characterizing and tuning both mechanisms is the milestone; a gain in this file would hand
 * over the answer. Your own feedforward and PID gains belong in {@code RobotContainer}, as your own
 * fields.
 */
public final class Constants {
  private Constants() {}

  /**
   * Seconds per simulated physics step. Given tests and {@code Robot.simulationPeriodic()} both use
   * this.
   */
  public static final double SIM_STEP_SECONDS = 0.020;

  public static final class FlywheelConstants {
    private FlywheelConstants() {}

    /** CAN ID of the flywheel's TalonFX. */
    public static final int CAN_ID = 20;

    /** The velocity Part 1's given tests command and check against. */
    public static final double TARGET_ROTATIONS_PER_SECOND = 40.0;
  }

  public static final class ArmConstants {
    private ArmConstants() {}

    /** CAN ID of the arm's TalonFX. */
    public static final int CAN_ID = 21;

    /** The position Part 2's gravity-holding given test commands and checks against. 45 degrees. */
    public static final double SCORING_ANGLE_RADIANS = 0.7854;
  }
}
