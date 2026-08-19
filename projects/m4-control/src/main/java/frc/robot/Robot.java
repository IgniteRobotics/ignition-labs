package frc.robot;

import edu.wpi.first.epilogue.Epilogue;
import edu.wpi.first.epilogue.Logged;
import edu.wpi.first.epilogue.NotLogged;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj2.command.CommandScheduler;

/**
 * Given. You should not need to change anything in this file to finish M4.
 *
 * <p>Two things happen every 20ms: {@link #robotPeriodic()} runs the command scheduler and then
 * your flywheel and arm loops once each, and {@link #simulationPeriodic()} — called immediately
 * afterward, only while simulating — advances both plants' physics by the same tick. Given tests
 * reproduce that same pairing by hand, one call apart, without constructing a {@code Robot} at all.
 *
 * <p><b>Why the two {@code requiring(...)} checks.</b> The scheduler runs first, so anything you
 * scheduled — a SysId sweep, {@code homingCommand()} — has already commanded its voltage by the
 * time your update method would be called. Calling it anyway would immediately overwrite that
 * command with your loop's output, once every 20ms, and the sweep you are watching would be your
 * control loop instead. {@code requiring(subsystem)} returns the command that currently owns a
 * mechanism, or null if nothing does, so a mechanism under command is left alone until that command
 * ends. This is M3's interruption lesson doing real work: the requirement is the interlock, not a
 * flag anybody has to remember to set.
 */
@Logged
public class Robot extends TimedRobot {
  @NotLogged private final RobotContainer robotContainer;

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
    CommandScheduler scheduler = CommandScheduler.getInstance();
    scheduler.run();

    if (scheduler.requiring(robotContainer.flywheelMechanism()) == null) {
      robotContainer.updateFlywheel();
    }
    if (scheduler.requiring(robotContainer.armMechanism()) == null) {
      robotContainer.updateArm();
    }
  }

  @Override
  public void simulationPeriodic() {
    robotContainer.flywheelPlant().step(Constants.SIM_STEP_SECONDS);
    robotContainer.armPlant().step(Constants.SIM_STEP_SECONDS);
  }
}
