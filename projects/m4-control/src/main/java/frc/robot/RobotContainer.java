package frc.robot;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import frc.robot.harness.M4Deliverables;
import frc.robot.harness.MechanismSubsystem;
import frc.robot.plant.ArmPlant;
import frc.robot.plant.FlywheelPlant;

/**
 * Where M4 is really built. Everything else in this project is given.
 *
 * <p>Work through README.md in order: Part 1 is the flywheel, Part 2 is the arm. The four TODO
 * regions below are the whole job, and the seven methods at the bottom are how the tests and {@code
 * Robot} reach what you built — implement each to do or return the thing its Javadoc in {@link
 * M4Deliverables} names.
 *
 * <p>Three things above the TODO regions are given and already wired: a {@link
 * CommandXboxController} on port {@link Constants.OperatorConstants#DRIVER_CONTROLLER_PORT}, and
 * one {@link MechanismSubsystem} per mechanism. You do not have to build any of them; you do have
 * to use them. A SysId routine requires a subsystem, and requiring one is what suspends {@code
 * Robot}'s call to your update method while the sweep runs — read {@link MechanismSubsystem}'s
 * Javadoc, it is six lines and it explains the whole interlock.
 */
public class RobotContainer implements M4Deliverables {

  // ============================= given, already wired ==========================
  // The driver controller, and one requirement token per mechanism. Bind to the
  // first; hand the second to SysIdWiring.forMotor(...) and to any command of
  // your own that drives a mechanism -- homingCommand() included.
  // =============================================================================
  private final CommandXboxController driver =
      new CommandXboxController(Constants.OperatorConstants.DRIVER_CONTROLLER_PORT);
  private final MechanismSubsystem flywheelMechanism = new MechanismSubsystem("Flywheel");
  private final MechanismSubsystem armMechanism = new MechanismSubsystem("Arm");

  // ===================== TODO 1 — construct your plants ======================
  // One FlywheelPlant and one ArmPlant, private final fields, using
  // Constants.FlywheelConstants.CAN_ID and Constants.ArmConstants.CAN_ID. Both
  // classes are given -- see plant/. README.md Part 1 step 1 / Part 2 step 1.
  // =============================================================================

  // ===================== TODO 2 — flywheel control ============================
  // Your feedforward, your feedback, and a field to hold the current target. One
  // iteration of the whole loop -- read the plant's velocity, compute a voltage,
  // command it -- is what updateFlywheel() below has to do. README.md Part 1.
  // =============================================================================

  // ===================== TODO 3 — arm control ==================================
  // Your feedforward (gravity matters here), your feedback, a motion profile, a
  // field to hold the current target, and the homing sequence homingCommand()
  // returns. README.md Part 2.
  // ==============================================================================

  public RobotContainer() {
    // =================== TODO 4 — bind your SysId routines ====================
    // Build a routine per mechanism with SysIdWiring.forMotor(<plant>.motor(),
    // "<name>", <mechanism>), then bind all four of its commands to buttons on
    // `driver` -- quasistatic and dynamic, forward and reverse. Four buttons, or
    // two plus a modifier; that part is up to you. README.md Part 1 step 2.
    // Nothing else in this project touches `driver`, so until you bind something
    // the controller does nothing.
    // ===========================================================================
  }

  /**
   * @return the flywheel's requirement token. Given — see {@link MechanismSubsystem}.
   */
  public MechanismSubsystem flywheelMechanism() {
    return flywheelMechanism;
  }

  /**
   * @return the arm's requirement token. Given — see {@link MechanismSubsystem}.
   */
  public MechanismSubsystem armMechanism() {
    return armMechanism;
  }

  @Override
  public void setFlywheelTarget(double rotationsPerSecond) {
    throw new UnsupportedOperationException("TODO(M4): store the flywheel target");
  }

  @Override
  public void updateFlywheel() {
    throw new UnsupportedOperationException(
        "TODO(M4): one 20ms iteration of your flywheel feedforward + feedback loop");
  }

  @Override
  public void setArmTarget(double radians) {
    throw new UnsupportedOperationException("TODO(M4): store the arm target");
  }

  @Override
  public void updateArm() {
    throw new UnsupportedOperationException(
        "TODO(M4): one 20ms iteration of your arm feedforward + feedback loop");
  }

  @Override
  public Command homingCommand() {
    throw new UnsupportedOperationException(
        "TODO(M4): return a NEW command that drives to the reverse limit, then zeroes");
  }

  @Override
  public FlywheelPlant flywheelPlant() {
    throw new UnsupportedOperationException("TODO(M4): return your FlywheelPlant");
  }

  @Override
  public ArmPlant armPlant() {
    throw new UnsupportedOperationException("TODO(M4): return your ArmPlant");
  }
}
