package frc.robot.plant;

import com.ctre.phoenix6.controls.ControlRequest;
import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.sim.TalonFXSimState;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.system.plant.LinearSystemId;
import edu.wpi.first.wpilibj.simulation.SingleJointedArmSim;

/**
 * A simulated single-jointed arm with gravity, a reverse limit switch, and an encoder that boots at
 * an arbitrary offset. Part 2 of this milestone.
 *
 * <p>Built from a moment of inertia, an arm length, and a gear reduction — {@link
 * LinearSystemId#createSingleJointedArmSystem} — never from a gravity-feedforward or PID gain.
 *
 * <p>{@code SingleJointedArmSim} takes no mass; its gravity term assumes {@code J = m*L^2/3}, so
 * {@link #J_KG_M2} is not independent of {@link #ARM_LENGTH_METERS} even though the constructor
 * takes them as separate arguments. This plant models an 8kg arm at this length — that mass never
 * appears in code because the simulator has no parameter for it, but it is the number {@link
 * #J_KG_M2} was derived from, and it is the number to start from again if {@link
 * #ARM_LENGTH_METERS} ever changes.
 *
 * <p><b>How voltage reaches the physics.</b> Same path as {@link FlywheelPlant}: {@link #step}
 * feeds the simulation from the voltage request the {@code TalonFX} is currently holding — {@code
 * motor.getAppliedControl()} — so every {@link VoltageOut} that reaches this motor reaches the arm,
 * whether it came from your control loop or from a SysId sweep. The same modelling limit applies
 * too: it is the request that is simulated, not the device's response to it, so current limits,
 * voltage compensation, ramp rates, and any inversion or neutral-mode configuration applied through
 * {@link #motor()} are inert.
 *
 * <p><b>Homing exists because {@link #measuredPositionRadians()} boots wrong.</b> A relative
 * encoder reads zero — or here, an arbitrary offset — wherever the mechanism happens to be at
 * power-on. {@link #atHomeLimit()} reads the physical simulated angle directly, independent of that
 * offset; {@link #zeroHere()} is the only thing that corrects the offset, and it does so
 * unconditionally, wherever you call it from. Calling it without having reached the limit still
 * makes {@link #measuredPositionRadians()} read {@link #HOME_ANGLE_RADIANS} — it does not know the
 * difference between "I am home" and "I gave up." A given test in this milestone checks {@link
 * #atHomeLimit()} for exactly that reason.
 */
public final class ArmPlant implements AutoCloseable {

  /** Radians from horizontal that a homed arm reads when it is sitting on the reverse limit. */
  public static final double HOME_ANGLE_RADIANS = -0.5236;

  // This plant models an 8kg arm -- SingleJointedArmSim has no mass parameter, so that
  // number never appears below, but J_KG_M2 is derived from it (J = m*L^2/3, design record
  // §3.1) and is not a free choice independent of ARM_LENGTH_METERS.
  private static final double ARM_LENGTH_METERS = 0.9;
  private static final double J_KG_M2 = 2.16; // = 8 kg * 0.9 m^2 / 3
  private static final double GEARING = 30.0;
  private static final double MIN_ANGLE_RADIANS = HOME_ANGLE_RADIANS; // -30 degrees
  private static final double MAX_ANGLE_RADIANS = 1.5708; // 90 degrees
  private static final double STARTING_ANGLE_RADIANS = 0.2618; // 15 degrees; arbitrary
  private static final double ENCODER_BOOT_OFFSET_RADIANS = 1.0472; // 60 degrees; arbitrary
  private static final double LIMIT_SWITCH_TOLERANCE_RADIANS = 1.0e-3;

  private final TalonFX motor;
  private final TalonFXSimState simState;
  private final SingleJointedArmSim sim;
  private final VoltageOut voltageRequest = new VoltageOut(0.0);
  private double encoderOffsetRadians = ENCODER_BOOT_OFFSET_RADIANS;

  public ArmPlant(int canId) {
    motor = new TalonFX(canId);
    simState = motor.getSimState();
    simState.setSupplyVoltage(12.0);
    sim =
        new SingleJointedArmSim(
            LinearSystemId.createSingleJointedArmSystem(DCMotor.getKrakenX60(1), J_KG_M2, GEARING),
            DCMotor.getKrakenX60(1),
            GEARING,
            ARM_LENGTH_METERS,
            MIN_ANGLE_RADIANS,
            MAX_ANGLE_RADIANS,
            true,
            STARTING_ANGLE_RADIANS);
  }

  /**
   * Commands a voltage. Clamped to the battery rail, and sent to the real {@code TalonFX} as a
   * {@link VoltageOut} — which is also how it reaches the physics, since {@link #step} reads the
   * device's current request back.
   *
   * @param volts commanded voltage; positive drives the arm toward {@link #MAX_ANGLE_RADIANS}
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
   * @return the raw encoder reading, radians. Boots at an arbitrary offset from the arm's true
   *     angle — see the class Javadoc — until {@link #zeroHere()} is called.
   */
  public double measuredPositionRadians() {
    return sim.getAngleRads() + encoderOffsetRadians;
  }

  /**
   * @return the arm's angular velocity, radians per second
   */
  public double velocityRadiansPerSecond() {
    return sim.getVelocityRadPerSec();
  }

  /**
   * @return whether the reverse limit switch is tripped. Reads the true simulated angle, not the
   *     encoder — this is true if and only if the arm is physically at its minimum travel,
   *     regardless of what {@link #measuredPositionRadians()} currently reads.
   */
  public boolean atHomeLimit() {
    return sim.getAngleRads() <= MIN_ANGLE_RADIANS + LIMIT_SWITCH_TOLERANCE_RADIANS;
  }

  /**
   * Rebases the encoder offset so {@link #measuredPositionRadians()} immediately reads {@link
   * #HOME_ANGLE_RADIANS}, whatever the arm's current true angle is. Call this once the arm is
   * actually at the limit — this method has no way to check that for you.
   */
  public void zeroHere() {
    encoderOffsetRadians = HOME_ANGLE_RADIANS - sim.getAngleRads();
  }

  /**
   * Advances the physics by one timestep and republishes telemetry, including the reverse limit
   * switch. {@code Robot.simulationPeriodic()} calls this once per 20ms tick; given tests call it
   * directly in a stepped loop.
   *
   * @param dtSeconds timestep, seconds
   */
  public void step(double dtSeconds) {
    sim.setInput(appliedVolts());
    sim.update(dtSeconds);
    double rotorRotations = sim.getAngleRads() / (2.0 * Math.PI) * GEARING;
    simState.setRawRotorPosition(rotorRotations);
    simState.setRotorVelocity(sim.getVelocityRadPerSec() / (2.0 * Math.PI) * GEARING);
    simState.setReverseLimit(atHomeLimit());
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
