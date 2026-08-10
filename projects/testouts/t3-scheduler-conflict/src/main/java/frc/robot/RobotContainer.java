package frc.robot;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import frc.robot.subsystems.DrivetrainSubsystem;
import frc.robot.subsystems.ElevatorSubsystem;
import frc.robot.subsystems.GripperSubsystem;

/**
 * A working three-subsystem robot. Everything here runs today except {@link #combinedCommand()},
 * which is your job — see README.md.
 */
public class RobotContainer {
  private final CommandXboxController driverController =
      new CommandXboxController(Constants.OperatorConstants.DRIVER_CONTROLLER_PORT);

  private final DrivetrainSubsystem drivetrain = new DrivetrainSubsystem();
  private final ElevatorSubsystem elevator = new ElevatorSubsystem();
  private final GripperSubsystem gripper = new GripperSubsystem();

  public RobotContainer() {
    configureDefaultCommands();
    configureBindings();
  }

  private void configureDefaultCommands() {
    drivetrain.setDefaultCommand(
        drivetrain.driveWithSticks(
            () -> -driverController.getLeftY(), () -> -driverController.getRightX()));
  }

  private void configureBindings() {
    driverController.a().whileTrue(elevator.raiseCommand());
    driverController.b().whileTrue(elevator.lowerCommand());
    driverController.x().onTrue(gripper.closeCommand());
    driverController.y().onTrue(gripper.openCommand());

    // Your binding goes here. README.md, part 2.
  }

  /**
   * Part 1 — return a new instance of the command you wrote.
   *
   * <p>A new instance every call: WPILib will not schedule a command that already belongs to a
   * composition, so a stored field handed back repeatedly cannot be scheduled.
   */
  public Command combinedCommand() {
    throw new UnsupportedOperationException("TODO(T3): return your combined command");
  }

  /** Given, for the tests. */
  public DrivetrainSubsystem drivetrain() {
    return drivetrain;
  }

  /** Given, for the tests. */
  public ElevatorSubsystem elevator() {
    return elevator;
  }

  /** Given, for the tests. */
  public GripperSubsystem gripper() {
    return gripper;
  }
}
