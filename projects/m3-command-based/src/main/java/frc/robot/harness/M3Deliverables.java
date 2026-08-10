package frc.robot.harness;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Subsystem;

/**
 * Exposes your M3 deliverables to the test harness. {@code RobotContainer} implements it.
 *
 * <p>This interface exists because the given tests cannot name classes you have not written yet.
 * They ask for your work through these five methods instead, so you can call your subsystems and
 * commands whatever you like.
 *
 * <p><b>Every command method must return a new instance each time it is called.</b> WPILib refuses
 * to schedule a command that is part of a composition, and {@link #composition()} is built out of
 * the other two — so if you store one command object in a field and hand the same one back every
 * time, it cannot be scheduled on its own and neither the tests nor your robot will work. Build the
 * command inside the method and return it.
 */
public interface M3Deliverables {
  /** Your drivetrain subsystem. */
  Subsystem drivetrain();

  /** Your other subsystem — whatever mechanism you chose. Must not be the drivetrain. */
  Subsystem mechanism();

  /** A new instance of the command you wrote as an explicit {@code Command} subclass. */
  Command subclassCommand();

  /** A new instance of the command you wrote as a factory method on a subsystem. */
  Command factoryCommand();

  /**
   * A new instance of your composition — a sequence, or a parallel with a timeout. It has to
   * require both subsystems, which means it has to be built out of commands that between them use
   * both.
   */
  Command composition();
}
