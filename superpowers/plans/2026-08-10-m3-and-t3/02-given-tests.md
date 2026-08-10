# Task 2: The given test suite, a control robot, and the distractor matrix

Four given test classes that assert M3's behaviour entirely through WPILib's public API — `Command`, `Subsystem`, `CommandScheduler` — and no reflection anywhere. The risk here is not writing the tests; it is shipping a suite that no correct robot passes, or one that every wrong robot passes. Steps 5 and 6 exist to catch each of those, and neither is optional.

**Files:**
- Modify: `projects/m3-command-based/src/test/java/frc/robot/CommandContractTest.java`
- Create: `projects/m3-command-based/src/test/java/frc/robot/CompositionTest.java`
- Create: `projects/m3-command-based/src/test/java/frc/robot/SchedulerInterruptionTest.java`
- Create: `projects/m3-command-based/src/test/java/frc/robot/DefaultCommandTest.java`
- Temporary, never committed to this repository: a control implementation under `projects/m3-command-based/src/main/java/frc/robot/subsystems/` and `commands/`

**Consumes:** Task 1's `frc.robot.harness.DeliverablesFixture` —
`setUp()`, `tearDown()`, `drivetrain()`, `mechanism()`, `subclassCommand()`, `factoryCommand()`, `composition()`, `schedule(String member, Command command)`, `runScheduler(int loops)`, `holdCommand(Subsystem... requirements)` returning `HoldCommand` with `wasInterrupted()`. Every accessor either returns a non-null value or fails the test with a readable message, so no test needs a null check.

**Produces:** the four given test classes Task 9 locks, and — left **uncommitted** in the solutions repository working tree at `../ignite-training-solutions/solutions/m3-command-based/src/main/java/frc/robot/` — the control implementation Task 5 finishes and commits. Do not delete it from there; Task 5 depends on it existing.

**Lane:** `milestone`. Work in `.worktrees/m3`, after Task 1 is committed.

---

- [ ] **Step 1: Fill out `CommandContractTest`**

Replace `projects/m3-command-based/src/test/java/frc/robot/CommandContractTest.java` entirely:

```java
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
```

- [ ] **Step 2: Write `CompositionTest`**

`projects/m3-command-based/src/test/java/frc/robot/CompositionTest.java`:

```java
package frc.robot;

import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.harness.DeliverablesFixture;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Given tests for M3's composition. Part of the milestone — do not edit. */
class CompositionTest {

  @BeforeEach
  void setUp() {
    DeliverablesFixture.setUp();
  }

  @AfterEach
  void tearDown() {
    DeliverablesFixture.tearDown();
  }

  @Test
  void theCompositionNeedsBothSubsystems() {
    Command composition = DeliverablesFixture.composition();

    assertTrue(
        composition.getRequirements().contains(DeliverablesFixture.drivetrain()),
        "Your composition does not require the drivetrain. A composition requires everything its "
            + "parts require, so if the drivetrain is missing then nothing inside it uses the "
            + "drivetrain. M3 asks for a composition that combines both of your commands.");
    assertTrue(
        composition.getRequirements().contains(DeliverablesFixture.mechanism()),
        "Your composition does not require the subsystem mechanism() returns. A composition "
            + "requires everything its parts require, so nothing inside it is using that "
            + "mechanism.");
  }

  @Test
  void theCompositionRunsWhenScheduled() {
    Command composition = DeliverablesFixture.composition();
    AtomicBoolean started = new AtomicBoolean(false);
    CommandScheduler.getInstance()
        .onCommandInitialize(
            scheduled -> {
              if (scheduled == composition) {
                started.set(true);
              }
            });

    DeliverablesFixture.schedule("composition()", composition);
    DeliverablesFixture.runScheduler(5);

    assertTrue(
        started.get(),
        "Your composition never started, even though the scheduler ran five times after it was "
            + "scheduled.");
  }
}
```

**This class deliberately does not assert that the composition terminates.** A sequence whose last step is a hold-until-released command is a legitimate answer to "a sequence or a parallel with a timeout", and a test demanding termination would reject it. Requirements and running are what §9.4 asks for; do not add a termination assertion later without changing the interface Javadoc first.

- [ ] **Step 3: Write `SchedulerInterruptionTest`**

This is the one that matters — it is the model the test-out and the DoD both turn on.

`projects/m3-command-based/src/test/java/frc/robot/SchedulerInterruptionTest.java`:

```java
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
        "Your command was interrupted but its end(true) never ran. Whatever your command was "
            + "driving is still being driven. Give it an end(boolean) that stops what it started.");
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
```

The second case can abort rather than fail, and that is deliberate: a command that finishes in its first loop cannot be interrupted, and the alternative — demanding a long-running command — would constrain a design decision M3 leaves to the student. An aborted test reports as skipped and is visible in the output. The first case has no such hole, needs nothing from the student except correct requirements, and is where the coverage actually lives.

- [ ] **Step 4: Write `DefaultCommandTest`**

`projects/m3-command-based/src/test/java/frc/robot/DefaultCommandTest.java`:

```java
package frc.robot;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.Subsystem;
import frc.robot.harness.DeliverablesFixture;
import frc.robot.harness.DeliverablesFixture.HoldCommand;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Given tests for the drivetrain's default command. Part of the milestone — do not edit. */
class DefaultCommandTest {

  @BeforeEach
  void setUp() {
    DeliverablesFixture.setUp();
  }

  @AfterEach
  void tearDown() {
    DeliverablesFixture.tearDown();
  }

  @Test
  void theDrivetrainHasADefaultCommand() {
    assertNotNull(
        CommandScheduler.getInstance().getDefaultCommand(DeliverablesFixture.drivetrain()),
        "The drivetrain has no default command, so the robot does nothing at all when no button "
            + "is held. Set one in RobotContainer with drivetrain.setDefaultCommand(...) — on a "
            + "real robot this is the command that reads the driver's sticks.");
  }

  @Test
  void theDefaultCommandRunsWhenNothingElseWantsTheDrivetrain() {
    Command byDefault =
        CommandScheduler.getInstance().getDefaultCommand(DeliverablesFixture.drivetrain());
    assertNotNull(byDefault, "The drivetrain has no default command.");

    DeliverablesFixture.runScheduler(2);

    assertTrue(
        CommandScheduler.getInstance().isScheduled(byDefault),
        "The drivetrain has a default command but the scheduler is not running it. A default "
            + "command must require the subsystem it is the default for.");
  }

  @Test
  void theDefaultCommandStandsAsideAndThenComesBack() {
    Subsystem drivetrain = DeliverablesFixture.drivetrain();
    Command byDefault = CommandScheduler.getInstance().getDefaultCommand(drivetrain);
    assertNotNull(byDefault, "The drivetrain has no default command.");
    DeliverablesFixture.runScheduler(2);

    HoldCommand occupant = DeliverablesFixture.holdCommand(drivetrain);
    DeliverablesFixture.schedule("the harness's hold command", occupant);
    DeliverablesFixture.runScheduler(1);
    assertFalse(
        CommandScheduler.getInstance().isScheduled(byDefault),
        "Something else took the drivetrain and the default command kept running anyway.");

    occupant.cancel();
    DeliverablesFixture.runScheduler(2);
    assertTrue(
        CommandScheduler.getInstance().isScheduled(byDefault),
        "The command that took the drivetrain ended and the default command did not come back. "
            + "That is what a default command is for — it is what runs whenever nothing else "
            + "needs the subsystem.");
  }
}
```

- [ ] **Step 5: Run a correct robot against the suite**

A suite no right answer passes is as broken as one every wrong answer passes. Write a complete, correct M3 into the project, run the tests, and require green.

`projects/m3-command-based/src/main/java/frc/robot/subsystems/DrivetrainSubsystem.java`:

```java
package frc.robot.subsystems;

import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import java.util.function.DoubleSupplier;

public class DrivetrainSubsystem extends SubsystemBase {
  private final PWMSparkMax left = new PWMSparkMax(0);
  private final PWMSparkMax right = new PWMSparkMax(1);
  private final DifferentialDrive drive = new DifferentialDrive(left::set, right::set);

  public DrivetrainSubsystem() {
    drive.setSafetyEnabled(false);
  }

  public void arcade(double forward, double rotation) {
    drive.arcadeDrive(forward, rotation);
  }

  public Command driveWithSticks(DoubleSupplier forward, DoubleSupplier rotation) {
    return run(() -> arcade(forward.getAsDouble(), rotation.getAsDouble()))
        .withName("Drive With Sticks");
  }
}
```

`projects/m3-command-based/src/main/java/frc/robot/subsystems/ElevatorSubsystem.java`:

```java
package frc.robot.subsystems;

import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class ElevatorSubsystem extends SubsystemBase {
  private final PWMSparkMax motor = new PWMSparkMax(2);

  public Command raiseCommand() {
    return runOnce(() -> motor.set(0.5)).withName("Raise Elevator");
  }

  public Command stopCommand() {
    return runOnce(() -> motor.set(0.0)).withName("Stop Elevator");
  }
}
```

`projects/m3-command-based/src/main/java/frc/robot/commands/DriveForSeconds.java`:

```java
package frc.robot.commands;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.DrivetrainSubsystem;

public class DriveForSeconds extends Command {
  private final DrivetrainSubsystem drivetrain;
  private final double seconds;
  private final Timer timer = new Timer();

  public DriveForSeconds(DrivetrainSubsystem drivetrain, double seconds) {
    this.drivetrain = drivetrain;
    this.seconds = seconds;
    addRequirements(drivetrain);
  }

  @Override
  public void initialize() {
    timer.restart();
  }

  @Override
  public void execute() {
    drivetrain.arcade(0.5, 0.0);
  }

  @Override
  public boolean isFinished() {
    return timer.hasElapsed(seconds);
  }

  @Override
  public void end(boolean interrupted) {
    drivetrain.arcade(0.0, 0.0);
  }
}
```

Then replace the three TODO regions and five stubs in `RobotContainer.java` with:

```java
  private final DrivetrainSubsystem drivetrain = new DrivetrainSubsystem();
  private final ElevatorSubsystem elevator = new ElevatorSubsystem();

  private void configureBindings() {
    driverController.a().onTrue(subclassCommand());
    driverController.b().whileTrue(factoryCommand());
    driverController.x().onTrue(composition());
  }

  private void configureDefaultCommands() {
    drivetrain.setDefaultCommand(
        drivetrain.driveWithSticks(
            () -> -driverController.getLeftY(), () -> -driverController.getRightX()));
  }

  @Override
  public Subsystem drivetrain() {
    return drivetrain;
  }

  @Override
  public Subsystem mechanism() {
    return elevator;
  }

  @Override
  public Command subclassCommand() {
    return new DriveForSeconds(drivetrain, 2.0).withName("Drive Two Seconds");
  }

  @Override
  public Command factoryCommand() {
    return elevator.raiseCommand();
  }

  @Override
  public Command composition() {
    return new DriveForSeconds(drivetrain, 2.0)
        .withName("Drive Two Seconds")
        .alongWith(elevator.raiseCommand())
        .withTimeout(3.0)
        .withName("Drive And Raise");
  }
```

with `import frc.robot.commands.DriveForSeconds;`, `import frc.robot.subsystems.DrivetrainSubsystem;` and `import frc.robot.subsystems.ElevatorSubsystem;` added.

```bash
cd /Users/y0shi/workspace/ignite/docs/projects/m3-command-based
export JAVA_HOME=~/wpilib/2026/jdk
./gradlew test --console=plain
```

Expected: **rc=0, every test green**, with at most `yourCommandIsInterruptedWhenSomethingElseNeedsItsSubsystem` reported as skipped. Paste the summary line and the skip reason if there is one. If anything is red, the suite is wrong — fix the test, not the control implementation, unless the control implementation is genuinely incorrect M3.

Also confirm the composition's requirements really are both, since `withTimeout` wraps twice:

```bash
./gradlew test --tests '*CompositionTest*' --console=plain; echo "rc=$?"
```

- [ ] **Step 6: Run the distractor matrix**

Six plausible-but-wrong robots. For each: apply the change on top of the control implementation, run `./gradlew test --console=plain`, record **which tests failed and the message a student would read**, then revert. A distractor that fails the wrong test, or fails everything, means a test is over-broad.

| # | The wrong robot | Must fail |
|---|---|---|
| 1 | Drop `.withName("Raise Elevator")` from `ElevatorSubsystem.raiseCommand()` | `factoryCommandHasBeenGivenAName` only |
| 2 | Delete `addRequirements(drivetrain)` from `DriveForSeconds` | `subclassCommandDeclaresWhichSubsystemItNeeds` and both `SchedulerInterruptionTest` cases |
| 3 | `composition()` returns `new DriveForSeconds(drivetrain, 2.0).withName("X").withTimeout(3.0).withName("Y")` — drivetrain only | `theCompositionNeedsBothSubsystems`, on the mechanism assertion |
| 4 | Store the subclass command: add `private final Command stored = new DriveForSeconds(drivetrain, 2.0).withName("Drive");` and return `stored` from `subclassCommand()` | `subclassCommandIsANewInstanceEveryTime`, plus the freshness message from every other case that asks for it |
| 5 | Delete the body of `configureDefaultCommands()` | all three `DefaultCommandTest` cases, nothing else |
| 6 | `mechanism()` returns `drivetrain` | `theRobotContainerExposesASecondSubsystemThatIsNotTheDrivetrain`, and `theCompositionNeedsBothSubsystems` |

Distractor 2 is the important one: forgetting `addRequirements()` is a named failure mode in the milestone, it produces a robot that looks fine in the simulator, and if the suite does not catch it then `SchedulerInterruptionTest` is decorative. Read its failure message in full and confirm it says what to do.

- [ ] **Step 7: Move the control implementation to the solutions repository**

It must not be committed here — committing it ships M3 already solved.

```bash
cd /Users/y0shi/workspace/ignite/docs
SOL=../ignite-training-solutions/solutions/m3-command-based/src/main/java/frc/robot
mkdir -p "$SOL/subsystems" "$SOL/commands"
cp projects/m3-command-based/src/main/java/frc/robot/RobotContainer.java "$SOL/"
cp projects/m3-command-based/src/main/java/frc/robot/subsystems/*.java "$SOL/subsystems/"
cp projects/m3-command-based/src/main/java/frc/robot/commands/*.java "$SOL/commands/"

git checkout projects/m3-command-based/src/main/java/frc/robot/RobotContainer.java
rm -r projects/m3-command-based/src/main/java/frc/robot/subsystems \
      projects/m3-command-based/src/main/java/frc/robot/commands

git status --short projects/m3-command-based
ls -R ../ignite-training-solutions/solutions/m3-command-based
```

`git status --short projects/m3-command-based` must show only the four test files. `ls -R` must list `RobotContainer.java`, two subsystems, and `DriveForSeconds.java`. Leave them uncommitted in the solutions repository — Task 5 finishes and commits that side.

- [ ] **Step 8: Prove the skeleton is red again, for the right reason**

```bash
cd /Users/y0shi/workspace/ignite/docs/projects/m3-command-based
export JAVA_HOME=~/wpilib/2026/jdk
./gradlew clean --console=plain > /dev/null
./gradlew assemble --console=plain;        echo "assemble rc=$? (expect 0)"
./gradlew compileTestJava --console=plain; echo "compileTestJava rc=$? (expect 0)"
./gradlew test --console=plain;            echo "test rc=$? (expect 1)"
```

Then read the report and confirm every failure is an assertion carrying one of the fixture's messages — not a compile error, not `UnsatisfiedLinkError`:

```bash
grep -c 'UnsupportedOperationException' build/reports/tests/test/index.html || true
open build/reports/tests/test/index.html   # or read the text output above
```

Paste the list of failing test names. Expected: every case in all four classes, each failing on "still throws UnsupportedOperationException".

- [ ] **Step 9: Format and commit**

```bash
cd /Users/y0shi/workspace/ignite/docs/projects/m3-command-based
export JAVA_HOME=~/wpilib/2026/jdk
./gradlew spotlessApply --console=plain && ./gradlew spotlessCheck --console=plain
./gradlew test --console=plain; echo "test rc=$? (expect 1)"

cd /Users/y0shi/workspace/ignite/docs
git status --short
git add projects/m3-command-based/src/test
git commit -m "M3 given tests: contract, composition, interruption, default command"
```

`git status --short` must not list anything under `projects/m3-command-based/src/main/java/frc/robot/subsystems` or `commands`. If it does, Step 7 did not finish and the milestone is about to ship solved.
