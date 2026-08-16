package frc.robot;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.harness.M4Deliverables;
import frc.robot.plant.ArmPlant;
import frc.robot.plant.FlywheelPlant;

/**
 * Where M4 is really built. Everything else in this project is given.
 *
 * <p>Work through README.md in order: Part 1 is the flywheel, Part 2 is the arm. The three TODO
 * regions below are the whole job, and the seven methods at the bottom are how the tests and {@code
 * Robot} reach what you built — implement each to do or return the thing its Javadoc in {@link
 * M4Deliverables} names.
 */
public class RobotContainer implements M4Deliverables {

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

  public RobotContainer() {}

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
