package frc.robot;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.Subsystem;
import frc.robot.harness.DeliverablesFixture;
import frc.robot.harness.DeliverablesFixture.HoldCommand;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Given tests for what the scheduler does when two commands want the same subsystem. Part of the
 * milestone — do not edit.
 *
 * <p>The answer, which you should be able to say out loud before a mentor asks: the one already
 * running is interrupted, its end(true) runs, and the new one starts. It does not queue. They do
 * not both run.
 */
class SchedulerInterruptionTest {

  @BeforeEach
  void setUp() {
    DeliverablesFixture.setUp();
  }

  @AfterEach
  void tearDown() {
    DeliverablesFixture.tearDown();
  }

  @Test
  void schedulingYourCommandInterruptsWhateverWasAlreadyUsingItsSubsystem() {
    Command yours = DeliverablesFixture.subclassCommand();
    Subsystem contested = oneRequirementOf(yours);

    HoldCommand occupant = DeliverablesFixture.holdCommand(contested);
    DeliverablesFixture.schedule("the harness's hold command", occupant);
    DeliverablesFixture.runScheduler(2);
    assertTrue(
        CommandScheduler.getInstance().isScheduled(occupant),
        "The harness could not get its own command running, which means something is wrong with "
            + "the harness rather than with your robot. Show a mentor.");

    DeliverablesFixture.schedule("subclassCommand()", yours);
    DeliverablesFixture.runScheduler(1);

    assertFalse(
        CommandScheduler.getInstance().isScheduled(occupant),
        "Your command was scheduled while another command was already using the same subsystem, "
            + "and that other command is still running. Both cannot run — the scheduler is "
            + "supposed to interrupt the first one. It did not, which means your command is not "
            + "actually claiming the subsystem: check that it calls addRequirements(...), or that "
            + "the factory method is built with run()/runOnce() on the subsystem itself.");
    assertTrue(
        occupant.wasInterrupted(),
        "The command that was running stopped, but its end(true) never ran — so it never got the "
            + "chance to stop its motors. This is the half of interruption that matters on a real "
            + "robot.");
  }

  @Test
  void yourCommandIsInterruptedWhenSomethingElseNeedsItsSubsystem() {
    Command yours = DeliverablesFixture.subclassCommand();
    Subsystem contested = oneRequirementOf(yours);

    AtomicBoolean endedInterrupted = new AtomicBoolean(false);
    CommandScheduler.getInstance()
        .onCommandInterrupt(
            interrupted -> {
              if (interrupted == yours) {
                endedInterrupted.set(true);
              }
            });

    DeliverablesFixture.schedule("subclassCommand()", yours);
    DeliverablesFixture.runScheduler(2);
    assumeTrue(
        CommandScheduler.getInstance().isScheduled(yours),
        "subclassCommand() finished on its own within two scheduler loops, so there was nothing "
            + "left to interrupt. That is a legitimate command — this case simply does not apply "
            + "to it, and the other test in this class covers the semantics either way.");

    DeliverablesFixture.schedule(
        "the harness's hold command", DeliverablesFixture.holdCommand(contested));

    assertFalse(
        CommandScheduler.getInstance().isScheduled(yours),
        "Something else claimed the subsystem your command was using and your command kept "
            + "running. Two commands cannot own one mechanism at once.");
    assertTrue(
        endedInterrupted.get(),
        "The scheduler never reported interrupting your command. onCommandInterrupt fires right "
            + "after the scheduler calls end(true) on the command it is interrupting, so this "
            + "confirms the scheduler ran the interruption path on your command — it cannot see "
            + "what your end(boolean) body actually did with it.");
  }

  private static Subsystem oneRequirementOf(Command command) {
    Set<Subsystem> requirements = command.getRequirements();
    assertFalse(
        requirements.isEmpty(),
        "subclassCommand() requires no subsystem at all, so nothing can conflict with it and "
            + "interruption cannot be demonstrated. Call addRequirements(...) in its constructor.");
    return requirements.iterator().next();
  }
}
