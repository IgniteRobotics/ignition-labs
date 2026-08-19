package frc.robot.harness;

import edu.wpi.first.wpilibj2.command.SubsystemBase;

/**
 * Given. One of these exists per mechanism, and it holds no state and does no work — its whole job
 * is to be a <em>requirement</em>.
 *
 * <p>M3 taught that scheduling a command which requires a subsystem interrupts whatever else
 * required it. That is the mechanism M4 uses to get your control loop out of the way: {@code
 * SysIdRoutine.Mechanism} declares this subsystem as a requirement, so while a sweep is running the
 * scheduler reports it as taken and {@code Robot.robotPeriodic()} skips that mechanism's update
 * call. Nothing has to remember to set a flag, and two things can never drive the same motor at
 * once. A {@code homingCommand()} that requires the arm's instance gets the same protection for
 * free.
 */
public final class MechanismSubsystem extends SubsystemBase {

  /**
   * @param name a short name for this mechanism, used in scheduler telemetry and in SysId's log
   *     entry titles
   */
  public MechanismSubsystem(String name) {
    setName(name);
  }
}
