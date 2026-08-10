package frc.robot.subsystems;

import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.GripperConstants;

/** Given, and working. */
public class GripperSubsystem extends SubsystemBase {
  private final PWMSparkMax motor = new PWMSparkMax(GripperConstants.MOTOR_CHANNEL);
  private boolean closed;

  /** Closes the gripper and holds it closed. */
  public Command closeCommand() {
    return runOnce(
            () -> {
              closed = true;
              motor.set(GripperConstants.GRIP_SPEED);
            })
        .withName("Close Gripper");
  }

  /** Opens the gripper. */
  public Command openCommand() {
    return runOnce(
            () -> {
              closed = false;
              motor.set(0.0);
            })
        .withName("Open Gripper");
  }

  /** Whether the gripper is currently holding. */
  public boolean isClosed() {
    return closed;
  }
}
