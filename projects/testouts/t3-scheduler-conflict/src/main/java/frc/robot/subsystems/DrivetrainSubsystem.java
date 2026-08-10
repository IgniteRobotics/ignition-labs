package frc.robot.subsystems;

import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.DrivetrainConstants;
import java.util.function.DoubleSupplier;

/** Given, and working. Two motors and a differential drive, all simulated. */
public class DrivetrainSubsystem extends SubsystemBase {
  private final PWMSparkMax left = new PWMSparkMax(DrivetrainConstants.LEFT_MOTOR_CHANNEL);
  private final PWMSparkMax right = new PWMSparkMax(DrivetrainConstants.RIGHT_MOTOR_CHANNEL);
  private final DifferentialDrive drive = new DifferentialDrive(left::set, right::set);

  public DrivetrainSubsystem() {
    drive.setSafetyEnabled(false);
  }

  /** Drives from two live inputs. This is the drivetrain's default command. */
  public Command driveWithSticks(DoubleSupplier forward, DoubleSupplier rotation) {
    return run(() -> drive.arcadeDrive(forward.getAsDouble(), rotation.getAsDouble()))
        .withName("Drive With Sticks");
  }

  /** Stops both motors. */
  public Command stopCommand() {
    return runOnce(() -> drive.arcadeDrive(0.0, 0.0)).withName("Stop Drivetrain");
  }
}
