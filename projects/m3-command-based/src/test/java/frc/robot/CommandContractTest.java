package frc.robot;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.Subsystem;
import frc.robot.harness.DeliverablesFixture;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Given tests for the commands M3 asks you to write. Part of the milestone — do not edit. */
class CommandContractTest {

  @BeforeEach
  void setUp() {
    DeliverablesFixture.setUp();
  }

  @AfterEach
  void tearDown() {
    DeliverablesFixture.tearDown();
  }

  @Test
  void theRobotContainerExposesADrivetrain() {
    assertNotNull(DeliverablesFixture.drivetrain());
  }

  @Test
  void theRobotContainerExposesASecondSubsystemThatIsNotTheDrivetrain() {
    assertNotSame(
        DeliverablesFixture.drivetrain(),
        DeliverablesFixture.mechanism(),
        "drivetrain() and mechanism() handed back the same object. M3 needs two subsystems — a "
            + "drivetrain and one other mechanism — because one subsystem cannot demonstrate "
            + "anything about requirements or interruption.");
  }

  @Test
  void subclassCommandIsANewInstanceEveryTime() {
    assertNotSame(
        DeliverablesFixture.subclassCommand(),
        DeliverablesFixture.subclassCommand(),
        "subclassCommand() returned the same object twice.");
  }

  @Test
  void factoryCommandIsANewInstanceEveryTime() {
    assertNotSame(
        DeliverablesFixture.factoryCommand(),
        DeliverablesFixture.factoryCommand(),
        "factoryCommand() returned the same object twice.");
  }

  @Test
  void subclassCommandHasBeenGivenAName() {
    assertHasBeenNamed("subclassCommand()", DeliverablesFixture.subclassCommand());
  }

  @Test
  void factoryCommandHasBeenGivenAName() {
    assertHasBeenNamed("factoryCommand()", DeliverablesFixture.factoryCommand());
  }

  @Test
  void subclassCommandDeclaresWhichSubsystemItNeeds() {
    assertDeclaresRequirements("subclassCommand()", DeliverablesFixture.subclassCommand());
  }

  @Test
  void factoryCommandDeclaresWhichSubsystemItNeeds() {
    assertDeclaresRequirements("factoryCommand()", DeliverablesFixture.factoryCommand());
  }

  @Test
  void subclassCommandActuallyRunsWhenScheduled() {
    assertRunsWhenScheduled("subclassCommand()", DeliverablesFixture.subclassCommand());
  }

  @Test
  void factoryCommandActuallyRunsWhenScheduled() {
    assertRunsWhenScheduled("factoryCommand()", DeliverablesFixture.factoryCommand());
  }

  /**
   * A command WPILib has not been told a name for reports its own class name. That is the tell:
   * "InstantCommand" or "DriveForward" is the default, not a name.
   */
  private static void assertHasBeenNamed(String member, Command command) {
    String name = command.getName();
    assertNotNull(name, member + " has a null name.");
    assertFalse(name.isBlank(), member + " has a blank name.");
    assertNotEquals(
        command.getClass().getSimpleName(),
        name,
        member
            + " is still called \""
            + name
            + "\", which is just its class name — the name WPILib uses when nobody has set one. "
            + "Chain .withName(\"Something Readable\") onto it. That name is what shows up in "
            + "Glass and AdvantageScope when you are trying to work out what the robot thought it "
            + "was doing.");
  }

  private static void assertDeclaresRequirements(String member, Command command) {
    Set<Subsystem> requirements = command.getRequirements();
    assertFalse(
        requirements.isEmpty(),
        member
            + " does not require any subsystem. A command that requires nothing can run at the "
            + "same time as anything else, including something else driving the same motor. In a "
            + "Command subclass you declare this by calling addRequirements(...) in the "
            + "constructor; a factory method built with run()/runOnce() on the subsystem gets it "
            + "for free.");

    Set<Subsystem> yours = new HashSet<>();
    yours.add(DeliverablesFixture.drivetrain());
    yours.add(DeliverablesFixture.mechanism());
    assertTrue(
        yours.containsAll(requirements),
        member
            + " requires a subsystem that is neither the one drivetrain() returns nor the one "
            + "mechanism() returns. The harness can only reason about the two subsystems you "
            + "expose.");
  }

  private static void assertRunsWhenScheduled(String member, Command command) {
    AtomicBoolean started = new AtomicBoolean(false);
    CommandScheduler.getInstance()
        .onCommandInitialize(
            scheduled -> {
              if (scheduled == command) {
                started.set(true);
              }
            });

    DeliverablesFixture.schedule(member, command);
    DeliverablesFixture.runScheduler(2);

    assertTrue(
        started.get(),
        member
            + " never started. It was handed to the scheduler and the scheduler ran twice, and "
            + "its initialize() was never called. The usual cause is a command whose requirements "
            + "were already taken by something else at the moment it was scheduled.");
  }
}
