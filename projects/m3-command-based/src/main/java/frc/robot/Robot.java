package frc.robot;

import edu.wpi.first.epilogue.Epilogue;
import edu.wpi.first.epilogue.Logged;
import edu.wpi.first.epilogue.NotLogged;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;

/**
 * Given. You should not need to change anything in this file to finish M3.
 *
 * <p>Two things happen here and they are the whole reason command-based works. {@code
 * robotPeriodic} runs the {@link CommandScheduler} every 20 milliseconds — that single call is what
 * makes every command you write actually execute. And Epilogue is bound to this object, which is
 * what puts the scheduler's state on the dashboard so you can watch which command is running.
 *
 * <p>Read {@code robotPeriodic} before you start. Tracing one pass through it, out loud, is a
 * definition-of-done item for this milestone.
 */
@Logged
public class Robot extends TimedRobot {
  @NotLogged private final RobotContainer robotContainer;

  @Logged(name = "Scheduler")
  CommandScheduler scheduler = CommandScheduler.getInstance();

  private Command autonomousCommand;

  public Robot() {
    robotContainer = new RobotContainer();

    Epilogue.configure(
        config -> {
          config.root = "Robot";
        });
    Epilogue.bind(this);
  }

  @Override
  public void robotPeriodic() {
    // Polls the buttons, starts newly-scheduled commands, runs every running command's
    // execute(), ends the ones that are finished or interrupted, and runs every
    // subsystem's periodic(). Nothing in the command-based framework happens without it.
    CommandScheduler.getInstance().run();
  }

  @Override
  public void autonomousInit() {
    autonomousCommand = robotContainer.getAutonomousCommand();
    if (autonomousCommand != null) {
      CommandScheduler.getInstance().schedule(autonomousCommand);
    }
  }

  @Override
  public void teleopInit() {
    if (autonomousCommand != null) {
      autonomousCommand.cancel();
    }
  }

  @Override
  public void testInit() {
    CommandScheduler.getInstance().cancelAll();
  }
}
