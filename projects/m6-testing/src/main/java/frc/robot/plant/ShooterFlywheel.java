package frc.robot.plant;

import com.ctre.phoenix6.controls.VelocityVoltage;
import com.ctre.phoenix6.hardware.TalonFX;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

/**
 * A one-motor flywheel, and the reason this project depends on Phoenix at all.
 *
 * <p>It implements {@link AutoCloseable} because WPILib recommends subsystems do, and the
 * recommendation exists for a concrete reason: a simulated device holds its CAN ID until it is
 * closed. Two tests in one file that each construct a {@code ShooterFlywheel} on the same ID will
 * fail on the second one unless the first was torn down. That failure looks like a bug in your code
 * and is not.
 */
public class ShooterFlywheel extends SubsystemBase implements AutoCloseable {

  private final TalonFX motor;
  private final VelocityVoltage request = new VelocityVoltage(0.0);

  public ShooterFlywheel(int canId) {
    motor = new TalonFX(canId);
  }

  /**
   * Commands the flywheel to a velocity.
   *
   * @param rotationsPerSecond target velocity
   */
  public void setVelocity(double rotationsPerSecond) {
    motor.setControl(request.withVelocity(rotationsPerSecond));
  }

  /**
   * @return the flywheel's most recently reported velocity, rotations per second
   */
  public double velocityRotationsPerSecond() {
    return motor.getVelocity().getValueAsDouble();
  }

  /**
   * The underlying motor, exposed so a test can reach its simulation state and its status signals.
   * Production code has no business calling this.
   *
   * @return the TalonFX this flywheel drives
   */
  public TalonFX motor() {
    return motor;
  }

  @Override
  public void close() {
    motor.close();
  }
}
