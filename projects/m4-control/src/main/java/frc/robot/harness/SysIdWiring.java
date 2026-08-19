package frc.robot.harness;

import com.ctre.phoenix6.SignalLogger;
import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.TalonFX;
import edu.wpi.first.units.Units;
import edu.wpi.first.wpilibj2.command.Subsystem;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine;

/**
 * Wires a {@link SysIdRoutine} to a {@code TalonFX}, logging through CTRE's {@code SignalLogger}.
 *
 * <p>Given boilerplate. Bind {@code routine.quasistatic(...)} and {@code routine.dynamic(...)} to
 * controller buttons in your own {@code RobotContainer}, in each of {@link
 * SysIdRoutine.Direction#kForward} and {@link SysIdRoutine.Direction#kReverse}, then run {@code
 * SignalLogger.start()} before and {@code SignalLogger.stop()} after — WPILib's SysId documentation
 * covers the rest of the workflow, which is the same for every mechanism.
 *
 * <p>Pass the mechanism's given {@link MechanismSubsystem} as the requirement. The sweep drives the
 * motor through the same simulated device your control loop does, so the two must not run at once —
 * the requirement is what guarantees they don't. Nothing here logs a mechanism measurement itself:
 * {@code SignalLogger} captures the device's own voltage, position, and velocity signals, which is
 * what the analyser reads.
 */
public final class SysIdWiring {

  private SysIdWiring() {}

  /**
   * @param motor the device to drive and log
   * @param logName a short name distinguishing this routine's log entries from any other
   * @param requirement the mechanism's {@link MechanismSubsystem}, so this routine cannot run
   *     concurrently with your own control loop
   * @return a configured {@link SysIdRoutine} ready to bind to buttons
   */
  public static SysIdRoutine forMotor(TalonFX motor, String logName, Subsystem requirement) {
    VoltageOut request = new VoltageOut(0.0);
    return new SysIdRoutine(
        new SysIdRoutine.Config(
            null,
            null,
            null,
            state -> SignalLogger.writeString(logName + "-state", state.toString())),
        new SysIdRoutine.Mechanism(
            volts -> motor.setControl(request.withOutput(volts.in(Units.Volts))),
            null,
            requirement));
  }
}
