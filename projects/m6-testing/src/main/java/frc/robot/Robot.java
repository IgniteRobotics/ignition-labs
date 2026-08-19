package frc.robot;

import edu.wpi.first.epilogue.Epilogue;
import edu.wpi.first.epilogue.Logged;
import edu.wpi.first.epilogue.NotLogged;
import edu.wpi.first.wpilibj.DataLogManager;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.plant.ShooterFlywheel;

/**
 * Given. Exists so this project has something to run under {@code ./gradlew simulateJava} for the
 * Definition-of-Done item that asks you to add {@code @Logged} to something and see it appear in
 * the log — everything else in this milestone is tested directly, without ever constructing a
 * {@code Robot} (see "do not instantiate the whole robot in a test" on the milestone page).
 *
 * <p>{@link #robotPeriodic()} runs the command scheduler (there is nothing scheduled in this
 * sandbox, but every WPILib robot calls this every tick regardless) and commands {@link
 * #shooterFlywheel} to a fixed demo velocity, so there is a real signal moving while you simulate.
 *
 * <p><b>The one thing you touch here:</b> {@link #shooterVelocityRotationsPerSecond} is marked
 * {@code @NotLogged} on purpose — nothing in this project logs anything yet. Swap that annotation
 * for {@code @Logged}, run {@code ./gradlew simulateJava}, enable the robot from the Sim GUI, let
 * it run a few seconds, and open the log it writes (next to the ones already in {@code logs/}) in
 * AdvantageScope. Look for a signal named after the field, under the {@code Robot} root the
 * constructor configures below. Put the annotation back when you are done; nothing else in this
 * file needs to change.
 */
@Logged
public class Robot extends TimedRobot {
  private static final int SHOOTER_CAN_ID = 1;
  private static final double DEMO_VELOCITY_ROTATIONS_PER_SECOND = 20.0;

  @NotLogged private final ShooterFlywheel shooterFlywheel = new ShooterFlywheel(SHOOTER_CAN_ID);

  @NotLogged private double shooterVelocityRotationsPerSecond;

  public Robot() {
    DataLogManager.start();

    Epilogue.configure(
        config -> {
          config.root = "Robot";
        });
    Epilogue.bind(this);
  }

  @Override
  public void robotPeriodic() {
    CommandScheduler.getInstance().run();
    shooterFlywheel.setVelocity(DEMO_VELOCITY_ROTATIONS_PER_SECOND);
    shooterVelocityRotationsPerSecond = shooterFlywheel.velocityRotationsPerSecond();
  }
}
