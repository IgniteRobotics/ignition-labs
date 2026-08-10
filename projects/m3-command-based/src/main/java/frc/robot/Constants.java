package frc.robot;

/**
 * Numbers that describe the robot, in one place with names on them.
 *
 * <p>Ignite splits these per mechanism — {@code ShooterConstants}, {@code IntakeConstants} — rather
 * than keeping one flat list, and this file follows that. When you add a subsystem, add a nested
 * class here for whatever it has to be told: channels, ratios, limits, speeds. A number typed
 * inline where it is used is a number nobody can find again.
 */
public final class Constants {
  private Constants() {}

  /** Given. The controller port that the simulator's saved config already maps a joystick to. */
  public static final class OperatorConstants {
    private OperatorConstants() {}

    public static final int DRIVER_CONTROLLER_PORT = 0;
  }

  // Your subsystems' constants go here, one nested class each.
}
