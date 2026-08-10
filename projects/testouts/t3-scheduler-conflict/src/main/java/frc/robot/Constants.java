package frc.robot;

/** Given. Channels and speeds for a robot that does not exist outside the simulator. */
public final class Constants {
  private Constants() {}

  /** The driver's controller. */
  public static final class OperatorConstants {
    private OperatorConstants() {}

    public static final int DRIVER_CONTROLLER_PORT = 0;
  }

  /** The drivetrain. */
  public static final class DrivetrainConstants {
    private DrivetrainConstants() {}

    public static final int LEFT_MOTOR_CHANNEL = 0;
    public static final int RIGHT_MOTOR_CHANNEL = 1;
  }

  /** The elevator. */
  public static final class ElevatorConstants {
    private ElevatorConstants() {}

    public static final int MOTOR_CHANNEL = 2;
    public static final double RAISE_SPEED = 0.4;
  }

  /** The gripper. */
  public static final class GripperConstants {
    private GripperConstants() {}

    public static final int MOTOR_CHANNEL = 3;
    public static final double GRIP_SPEED = 0.3;
  }
}
