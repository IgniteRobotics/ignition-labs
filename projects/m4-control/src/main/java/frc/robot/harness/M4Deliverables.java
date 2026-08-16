package frc.robot.harness;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.plant.ArmPlant;
import frc.robot.plant.FlywheelPlant;

/**
 * Exposes your M4 deliverables to the test harness and to {@code Robot}. {@code RobotContainer}
 * implements it.
 *
 * <p>The given tests cannot name your subsystem classes — they don't know what you called them — so
 * they reach your control loop through these seven methods instead. {@code Robot} uses the same
 * seam: {@code robotPeriodic()} calls {@link #updateFlywheel()} and {@link #updateArm()} every
 * 20ms, and {@code simulationPeriodic()} steps the plants {@link #flywheelPlant()} and {@link
 * #armPlant()} return.
 *
 * <p><b>{@link #homingCommand()} must return a new instance each time it is called</b>, the same
 * rule M3's deliverables carried: a command object that has already run once cannot be scheduled
 * again.
 */
public interface M4Deliverables {

  /**
   * Sets the flywheel's target velocity. Takes effect on the next {@link #updateFlywheel()}.
   *
   * @param rotationsPerSecond target velocity, mechanism rotations per second
   */
  void setFlywheelTarget(double rotationsPerSecond);

  /**
   * Runs exactly one 20ms iteration of your flywheel control loop: read {@link
   * FlywheelPlant#velocityRotationsPerSecond()}, compute a voltage from your feedforward and
   * feedback, and call {@link FlywheelPlant#setVoltage(double)}.
   */
  void updateFlywheel();

  /**
   * Sets the arm's target position. Takes effect on the next {@link #updateArm()}.
   *
   * @param radians target position, radians from horizontal
   */
  void setArmTarget(double radians);

  /**
   * Runs exactly one 20ms iteration of your arm control loop: read {@link
   * ArmPlant#measuredPositionRadians()}, compute a voltage from your feedforward and feedback, and
   * call {@link ArmPlant#setVoltage(double)}.
   */
  void updateArm();

  /**
   * @return a NEW command that drives the arm toward {@link ArmPlant#atHomeLimit()} and, once
   *     there, calls {@link ArmPlant#zeroHere()} and finishes. A stored, already-run instance
   *     cannot be rescheduled — build it inside this method.
   */
  Command homingCommand();

  /**
   * @return your flywheel's plant. The same instance every call — unlike {@link #homingCommand()},
   *     this is not required to be fresh.
   */
  FlywheelPlant flywheelPlant();

  /**
   * @return your arm's plant. The same instance every call.
   */
  ArmPlant armPlant();
}
