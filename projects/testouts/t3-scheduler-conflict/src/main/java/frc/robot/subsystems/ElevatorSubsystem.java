package frc.robot.subsystems;

import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.ElevatorConstants;

/** Given, and working. */
public class ElevatorSubsystem extends SubsystemBase {
  private final PWMSparkMax motor = new PWMSparkMax(ElevatorConstants.MOTOR_CHANNEL);
  private double commandedSpeed;

  /** Runs the elevator up until it is interrupted. */
  public Command raiseCommand() {
    return run(() -> setSpeed(ElevatorConstants.RAISE_SPEED))
        .finallyDo(interrupted -> setSpeed(0.0))
        .withName("Raise Elevator");
  }

  /** Runs the elevator down until it is interrupted. */
  public Command lowerCommand() {
    return run(() -> setSpeed(-ElevatorConstants.RAISE_SPEED))
        .finallyDo(interrupted -> setSpeed(0.0))
        .withName("Lower Elevator");
  }

  /** The speed most recently commanded. Useful when you want to see what happened. */
  public double commandedSpeed() {
    return commandedSpeed;
  }

  private void setSpeed(double speed) {
    commandedSpeed = speed;
    motor.set(speed);
  }
}
