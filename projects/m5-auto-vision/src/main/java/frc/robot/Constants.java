package frc.robot;

/**
 * Numbers that describe the robot, in one place with names on them.
 *
 * <p>No filtering threshold lives here either — see {@link VisionConstants}.
 */
public final class Constants {
  private Constants() {}

  /**
   * Seconds per simulated physics step. Given tests and {@code Robot.simulationPeriodic()} both use
   * this.
   */
  public static final double SIM_STEP_SECONDS = 0.020;

  public static final class VisionConstants {
    private VisionConstants() {}

    // No MAX_TRUSTED_DISTANCE_METERS here. Choosing how far you still trust a vision pose --
    // and defending that number -- is the milestone. Put your own threshold in
    // RobotContainer, as your own field.
  }
}
