package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;

/**
 * The smallest robot program that runs. It does nothing except tick, which is the point — M0 is
 * about the toolchain, git, and the build, not about making anything move.
 *
 * <p>Each of these methods is called by WPILib at a specific moment. robotPeriodic() runs every 20
 * milliseconds, always. The others run when the robot enters that mode. You will meet all of them
 * properly in M3.
 */
public class Robot extends TimedRobot {
  @Override
  public void robotInit() {}

  @Override
  public void robotPeriodic() {}

  @Override
  public void autonomousInit() {}

  @Override
  public void autonomousPeriodic() {}

  @Override
  public void teleopInit() {}

  @Override
  public void teleopPeriodic() {}

  @Override
  public void disabledInit() {}

  @Override
  public void disabledPeriodic() {}

  @Override
  public void testInit() {}

  @Override
  public void testPeriodic() {}

  @Override
  public void simulationPeriodic() {}
}
