package frc.robot;

import edu.wpi.first.epilogue.Epilogue;
import edu.wpi.first.epilogue.Logged;
import edu.wpi.first.epilogue.NotLogged;
import edu.wpi.first.wpilibj.TimedRobot;

/**
 * Given. You should not need to change anything in this file to finish M4.
 *
 * <p>Two things happen every 20ms: {@link #robotPeriodic()} runs your flywheel and arm loops once
 * each, and {@link #simulationPeriodic()} — called immediately afterward, only while simulating —
 * advances both plants' physics by the same tick. Given tests reproduce that same pairing by hand,
 * one call apart, without constructing a {@code Robot} at all.
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
    robotContainer.updateFlywheel();
    robotContainer.updateArm();
  }

  @Override
  public void simulationPeriodic() {
    robotContainer.flywheelPlant().step(Constants.SIM_STEP_SECONDS);
    robotContainer.armPlant().step(Constants.SIM_STEP_SECONDS);
  }
}
