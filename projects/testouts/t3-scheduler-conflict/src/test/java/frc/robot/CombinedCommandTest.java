package frc.robot;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.Subsystem;
import frc.robot.harness.SchedulerFixture;
import frc.robot.harness.SchedulerFixture.HoldCommand;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Given tests for T3 part 1. Part of the fixture — do not edit. */
class CombinedCommandTest {

  @BeforeEach
  void setUp() {
    SchedulerFixture.setUp();
  }

  @AfterEach
  void tearDown() {
    SchedulerFixture.tearDown();
  }

  @Test
  void combinedCommandIsANewInstanceEveryTime() {
    assertNotSame(
        SchedulerFixture.combinedCommand(),
        SchedulerFixture.combinedCommand(),
        "combinedCommand() returned the same object twice.");
  }

  @Test
  void combinedCommandHasBeenGivenAName() {
    Command command = SchedulerFixture.combinedCommand();
    assertNotEquals(
        command.getClass().getSimpleName(),
        command.getName(),
        "combinedCommand() is still called \""
            + command.getName()
            + "\" — that is its class name, which is what WPILib uses when nobody has set one. "
            + "Chain .withName(\"...\") onto it.");
  }

  @Test
  void combinedCommandRequiresTheElevatorAndTheGripper() {
    Command command = SchedulerFixture.combinedCommand();

    assertTrue(
        command.getRequirements().contains(SchedulerFixture.elevator()),
        "Your command does not require the elevator. A command requires a subsystem either "
            + "because you called addRequirements(...) or because it was built with run()/"
            + "runOnce() on that subsystem.");
    assertTrue(
        command.getRequirements().contains(SchedulerFixture.gripper()),
        "Your command does not require the gripper. The task asks for a command that requires "
            + "two of the three subsystems.");
    assertFalse(
        command.getRequirements().contains(SchedulerFixture.drivetrain()),
        "Your command requires the drivetrain as well. Requiring a subsystem you do not use "
            + "blocks it for no reason — on a real robot that means the driver loses steering "
            + "while your command runs.");
  }

  @Test
  void schedulingItInterruptsWhateverWasUsingTheElevator() {
    assertInterrupts(SchedulerFixture.elevator(), "elevator");
  }

  @Test
  void schedulingItInterruptsWhateverWasUsingTheGripper() {
    assertInterrupts(SchedulerFixture.gripper(), "gripper");
  }

  private static void assertInterrupts(Subsystem contested, String name) {
    HoldCommand occupant = SchedulerFixture.holdCommand(contested);
    SchedulerFixture.schedule("the fixture's hold command", occupant);
    SchedulerFixture.runScheduler(2);
    assertTrue(
        CommandScheduler.getInstance().isScheduled(occupant),
        "The fixture could not get its own command running — show a mentor.");

    SchedulerFixture.schedule("combinedCommand()", SchedulerFixture.combinedCommand());
    SchedulerFixture.runScheduler(1);

    assertFalse(
        CommandScheduler.getInstance().isScheduled(occupant),
        "Something was already using the "
            + name
            + " and it is still running after your command was scheduled. Both cannot run. The "
            + "scheduler interrupts the first one — unless your command never actually claimed "
            + "the "
            + name
            + ".");
    assertTrue(
        occupant.wasInterrupted(),
        "The command using the "
            + name
            + " stopped, but its end(true) never ran, so it never got to stop what it started. "
            + "That is the half of interruption that matters on a real robot.");
  }
}
