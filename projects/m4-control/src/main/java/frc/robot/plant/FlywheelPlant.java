package frc.robot.plant;

import com.ctre.phoenix6.controls.ControlRequest;
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
 * <p><b>How voltage reaches the physics.</b> {@link #step} feeds the simulation from the voltage
 * request the {@code TalonFX} is currently holding — {@code motor.getAppliedControl()} — not from a
 * copy of the last {@link #setVoltage} argument kept on the side. There is exactly one way in and
 * it is the device, so anything that sends this motor a {@link VoltageOut} reaches the mechanism:
 * your control loop through {@link #setVoltage}, or a SysId sweep bound to a button. Send it some
 * other kind of request and this plant reads zero volts — it models a motor driven by voltage, and
 * that is the only request it understands.
 *
 * <p><b>What this still does not simulate.</b> The physics is fed the request, not the simulated
 * device's response to it, so current limits, voltage compensation, ramp rates, and any inversion
 * or neutral-mode configuration you apply through {@link #motor()} remain inert here. A student who
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
  private double rotorPositionRotations;

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
   * {@link VoltageOut} — which is also how it reaches the physics, since {@link #step} reads the
   * device's current request back.
   *
   * @param volts commanded voltage; positive spins the mechanism forward
   */
  public void setVoltage(double volts) {
    motor.setControl(voltageRequest.withOutput(MathUtil.clamp(volts, -12.0, 12.0)));
  }

  /** The voltage the device has been asked for, clamped to the battery rail. */
  private double appliedVolts() {
    ControlRequest request = motor.getAppliedControl();
    return request instanceof VoltageOut voltageOut
        ? MathUtil.clamp(voltageOut.Output, -12.0, 12.0)
        : 0.0;
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
    sim.setInput(appliedVolts());
    sim.update(dtSeconds);
    double rotorRotationsPerSecond = velocityRotationsPerSecond() * GEARING;
    rotorPositionRotations += rotorRotationsPerSecond * dtSeconds;
    simState.setRawRotorPosition(rotorPositionRotations);
    simState.setRotorVelocity(rotorRotationsPerSecond);
  }

  /**
   * @return the underlying {@code TalonFX}, for configuration practice and for wiring SysId. A
   *     voltage request sent through it drives this mechanism; configuration applied through it
   *     does not change what {@link #step} simulates — see the class Javadoc.
   */
  public TalonFX motor() {
    return motor;
  }

  @Override
  public void close() {
    motor.close();
  }
}
