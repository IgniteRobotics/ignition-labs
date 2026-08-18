package frc.testout.t4.plant;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.system.plant.LinearSystemId;
import edu.wpi.first.wpilibj.simulation.FlywheelSim;

/**
 * A simulated flywheel built from physical quantities, never from gains. Given, locked — part of
 * the fixture. {@code tests.lock} and {@code CODEOWNERS} both cover this file (spec §12.2).
 */
public final class T4FlywheelPlant {
  private static final DCMotor GEARBOX = DCMotor.getNEO(1);
  private static final double GEARING = 1.0;
  private static final double MOMENT_OF_INERTIA_KG_METERS_SQUARED = 0.0006;

  private final FlywheelSim sim =
      new FlywheelSim(
          LinearSystemId.createFlywheelSystem(
              GEARBOX, MOMENT_OF_INERTIA_KG_METERS_SQUARED, GEARING),
          GEARBOX);

  /**
   * Commands a voltage to the flywheel, clamped to what a battery can deliver.
   *
   * <p>Deliberately {@code setInput}, never {@code setInputVoltage}: the latter re-clamps through
   * {@code RobotController.getBatteryVoltage()}, which goes through the HAL. This fixture never
   * initializes the HAL, so an uninitialized battery-voltage read would silently clamp every
   * command to zero.
   */
  public void setVoltage(double volts) {
    sim.setInput(MathUtil.clamp(volts, -12.0, 12.0));
  }

  /** Advances the simulated physics by {@code dtSeconds}. */
  public void step(double dtSeconds) {
    sim.update(dtSeconds);
  }

  /** The flywheel's current angular velocity. */
  public double velocityRotationsPerSecond() {
    return sim.getAngularVelocityRPM() / 60.0;
  }
}
