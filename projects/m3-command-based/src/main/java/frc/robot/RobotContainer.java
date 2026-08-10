package frc.robot;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.Subsystem;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import frc.robot.harness.M3Deliverables;

/**
 * Where the robot is wired together: which subsystems exist, which commands are bound to which
 * buttons, and what runs when nothing else does.
 *
 * <p>This is the file M3 is really about. Everything else in this project is given.
 *
 * <p>Work through README.md in order. The three TODO regions below are the whole job, and the five
 * methods at the bottom are how the tests reach what you built — implement each one to return the
 * thing it names.
 */
public class RobotContainer implements M3Deliverables {
  /** Given. Port 0, which is the port {@code simgui.json} already maps a joystick to. */
  private final CommandXboxController driverController =
      new CommandXboxController(Constants.OperatorConstants.DRIVER_CONTROLLER_PORT);

  // ===================== TODO 1 — your subsystems ==========================
  // Two of them: a drivetrain, and one other mechanism. Private final fields on
  // this class — not static, not public, and not reachable from anywhere else.
  // README.md step 2.
  // =========================================================================

  public RobotContainer() {
    configureDefaultCommands();
    configureBindings();
  }

  /**
   * TODO 2 — bind your commands to the controller. Three bindings: one {@code onTrue}, one {@code
   * whileTrue}, and your composition. README.md step 5.
   */
  private void configureBindings() {}

  /**
   * TODO 3 — give the drivetrain a default command, so that something sensible happens when no
   * other command wants it. README.md step 6.
   */
  private void configureDefaultCommands() {}

  /** M3 has no autonomous. M5 is where that starts. */
  public Command getAutonomousCommand() {
    return Commands.none();
  }

  @Override
  public Subsystem drivetrain() {
    throw new UnsupportedOperationException("TODO(M3): return your drivetrain subsystem");
  }

  @Override
  public Subsystem mechanism() {
    throw new UnsupportedOperationException("TODO(M3): return your other subsystem");
  }

  @Override
  public Command subclassCommand() {
    throw new UnsupportedOperationException(
        "TODO(M3): return a new instance of your Command subclass");
  }

  @Override
  public Command factoryCommand() {
    throw new UnsupportedOperationException(
        "TODO(M3): return a new instance of your subsystem's factory command");
  }

  @Override
  public Command composition() {
    throw new UnsupportedOperationException("TODO(M3): return a new instance of your composition");
  }
}
