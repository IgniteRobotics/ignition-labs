package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.CommandScheduler;

/** Given, and working. Runs the scheduler every 20ms and nothing else. */
public class Robot extends TimedRobot {
  @SuppressWarnings("unused")
  private final RobotContainer robotContainer = new RobotContainer();

  public Robot() {
    // Part 3 asks you to watch what the scheduler did to your command — this is what
    // gives you somewhere to watch it happen.
    SmartDashboard.putData("Scheduler", CommandScheduler.getInstance());
  }

  @Override
  public void robotPeriodic() {
    CommandScheduler.getInstance().run();
  }

  @Override
  public void testInit() {
    CommandScheduler.getInstance().cancelAll();
  }
}
