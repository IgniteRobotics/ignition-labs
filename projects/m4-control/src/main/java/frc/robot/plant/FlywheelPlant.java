package frc.robot.plant;

import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.sim.TalonFXSimState;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.system.plant.LinearSystemId;
import edu.wpi.first.wpilibj.simulation.FlywheelSim;

/**
 * A simulated single-motor flywheel, and the mechanism Part 1 of this milestone is about.
 *
 * <p>Built from a moment of inertia and a gear reduction — {@link
 * LinearSystemId#createFlywheelSystem} — never from a velocity-versus-voltage gain. Reading this
 * file hands you a physical quantity to reason about, not a kV to copy into your controller. You
 * still have to characterize it.
 *
 * <p><b>What this simulates, and what it does not.</b> {@link #step} feeds the physics from the
 * voltage {@link #setVoltage} was last called with — not from anything read back off the {@code
 * TalonFX}. One consequence: current limits, voltage compensation, ramp rates, and any inversion or
 * neutral-mode configuration you apply through {@link #motor()} are all inert here. A student who
 * configures a 40A supply current limit and sees no change in the plot has not found a bug; they
 * have found the edge of what this model represents.
 *
 * <p>{@link #velocityRotationsPerSecond()} reads the mechanism's own velocity — not the {@code
 * TalonFX}'s rotor velocity, which runs faster by the gear reduction. Characterizing the motor
 * (through {@link #motor()} and SysId) and controlling the mechanism (through this method) are two
 * different units, on purpose.
 */
public final class FlywheelPlant implements AutoCloseable {

  private static final double J_KG_M2 = 0.0021;
  private static final double GEARING = 1.5;

  private final TalonFX motor;
  private final TalonFXSimState simState;
  private final FlywheelSim sim;
  private final VoltageOut voltageRequest = new VoltageOut(0.0);
  private double commandedVolts;

  public FlywheelPlant(int canId) {
    motor = new TalonFX(canId);
    simState = motor.getSimState();
    simState.setSupplyVoltage(12.0);
    sim =
        new FlywheelSim(
            LinearSystemId.createFlywheelSystem(DCMotor.getKrakenX60(1), J_KG_M2, GEARING),
            DCMotor.getKrakenX60(1));
  }

  /**
   * Commands a voltage. Clamped to the battery rail, and sent to the real {@code TalonFX} as a
   * {@link VoltageOut} — the device really receives it, even though {@link #step} does not read it
   * back.
   *
   * @param volts commanded voltage; positive spins the mechanism forward
   */
  public void setVoltage(double volts) {
    commandedVolts = MathUtil.clamp(volts, -12.0, 12.0);
    motor.setControl(voltageRequest.withOutput(commandedVolts));
  }

  /**
   * @return the mechanism's current velocity, rotations per second. This is the measurement your
   *     control loop reads and the measurement the given tests assert on.
   */
  public double velocityRotationsPerSecond() {
    return sim.getAngularVelocityRadPerSec() / (2.0 * Math.PI);
  }

  /**
   * Advances the physics by one timestep and republishes telemetry. {@code
   * Robot.simulationPeriodic()} calls this once per 20ms tick; given tests call it directly in a
   * stepped loop.
   *
   * @param dtSeconds timestep, seconds
   */
  public void step(double dtSeconds) {
    sim.setInput(commandedVolts);
    sim.update(dtSeconds);
    simState.setRotorVelocity(velocityRotationsPerSecond() * GEARING);
  }

  /**
   * @return the underlying {@code TalonFX}, for configuration practice and for wiring SysId. No
   *     configuration you apply through it changes what {@link #step} simulates — see the class
   *     Javadoc.
   */
  public TalonFX motor() {
    return motor;
  }

  @Override
  public void close() {
    motor.close();
  }
}
