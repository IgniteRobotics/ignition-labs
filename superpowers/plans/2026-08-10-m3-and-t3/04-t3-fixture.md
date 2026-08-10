# Task 4: The T3 scheduler-conflict test-out fixture

A complete, working, three-subsystem robot with exactly one thing missing: a command that requires two of the subsystems, and a binding for it. Sixty minutes, Tracks C and D. The code half is test-covered; the interruption explanation is the pass criterion and is mentor-verified (spec §10). The risk in a fixture like this is shipping one nobody has finished — Step 6 finishes it, inside its own time box, before it ships.

**Files:**
- Create: `projects/testouts/t3-scheduler-conflict/settings.gradle`, `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`, `gradle/wrapper/gradle-wrapper.properties`, `.gitignore`, `WPILib-License.md`, `.wpilib/wpilib_preferences.json` (copies)
- Create: `projects/testouts/t3-scheduler-conflict/build.gradle`
- Create: `projects/testouts/t3-scheduler-conflict/vendordeps/WPILibNewCommands.json` (copy)
- Create: `projects/testouts/t3-scheduler-conflict/src/main/java/frc/robot/Main.java`, `Robot.java`, `Constants.java`, `RobotContainer.java`
- Create: `projects/testouts/t3-scheduler-conflict/src/main/java/frc/robot/subsystems/DrivetrainSubsystem.java`, `ElevatorSubsystem.java`, `GripperSubsystem.java`
- Test: `projects/testouts/t3-scheduler-conflict/src/test/java/frc/robot/harness/SchedulerFixture.java`
- Test: `projects/testouts/t3-scheduler-conflict/src/test/java/frc/robot/CombinedCommandTest.java`
- Create: `projects/testouts/t3-scheduler-conflict/README.md`

**Consumes:** nothing. This lane is independent of the M3 lane and shares no file with it. The two projects deliberately do **not** share source: each `projects/*/` is standalone (spec §4.2), so T3 gets its own `SchedulerFixture` rather than importing M3's.

**Produces:** the fixture, and — left **uncommitted** in `../ignite-training-solutions/solutions/testouts/t3-scheduler-conflict/` — the reference answer Task 5 finishes and commits.

**Lane:** `testout`. Work in `.worktrees/t3`.

---

- [ ] **Step 1: Copy the scaffold and write `build.gradle`**

```bash
cd /Users/y0shi/workspace/ignite/docs
mkdir -p projects/testouts/t3-scheduler-conflict
cd projects/m0-onboarding
for f in settings.gradle gradlew gradlew.bat gradle/wrapper/gradle-wrapper.jar \
         gradle/wrapper/gradle-wrapper.properties .gitignore WPILib-License.md \
         .wpilib/wpilib_preferences.json; do
  mkdir -p "../testouts/t3-scheduler-conflict/$(dirname "$f")"
  cp "$f" "../testouts/t3-scheduler-conflict/$f"
done
chmod +x ../testouts/t3-scheduler-conflict/gradlew

mkdir -p ../testouts/t3-scheduler-conflict/vendordeps
cp ~/wpilib/2026/vendordeps/WPILibNewCommands.json ../testouts/t3-scheduler-conflict/vendordeps/
```

`projects/testouts/t3-scheduler-conflict/build.gradle` — this is `projects/m2-oop/build.gradle` with one change, `ROBOT_MAIN_CLASS`. **No Epilogue annotation processor:** nothing here is `@Logged`, T3 is a 60-minute fixture with no telemetry requirement, and an unused processor is one more thing that can fail on a machine nobody has tested.

```groovy
plugins {
    id "java"
    id "com.diffplug.spotless" version "8.2.1"
    id "edu.wpi.first.GradleRIO" version "2026.2.1"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

spotless {
    // Not enforced during build. Formatting is checked by CI (spotlessCheck) as a
    // separate, deliberately failable step -- see teaching-repo-spec.md §4.3. Do not
    // wire compileJava to spotlessApply: that reformats the tree on every build, which
    // makes the formatting check structurally unable to fail.
    enforceCheck false

    java {
        target 'src/**/*.java'
        removeUnusedImports()
        googleJavaFormat('1.19.2').reflowLongStrings()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

// T3 is a working robot, unlike the M1 and M2 fixtures. ./gradlew simulateJava launches it.
def ROBOT_MAIN_CLASS = "frc.robot.Main"

deploy {
    targets {
        roborio(getTargetTypeClass('RoboRIO')) {
            team = project.frc.getTeamOrDefault(6829)
            debug = project.frc.getDebugOrDefault(false)

            artifacts {
                frcJava(getArtifactTypeClass('FRCJavaArtifact')) {
                }
            }
        }
    }
}

def deployArtifact = deploy.targets.roborio.artifacts.frcJava

wpi.java.debugJni = false
def includeDesktopSupport = true

repositories {
    mavenCentral()
}

dependencies {
    implementation wpi.java.deps.wpilib()
    implementation wpi.java.vendor.java()

    roborioDebug wpi.java.deps.wpilibJniDebug(wpi.platforms.roborio)
    roborioDebug wpi.java.vendor.jniDebug(wpi.platforms.roborio)

    roborioRelease wpi.java.deps.wpilibJniRelease(wpi.platforms.roborio)
    roborioRelease wpi.java.vendor.jniRelease(wpi.platforms.roborio)

    nativeDebug wpi.java.deps.wpilibJniDebug(wpi.platforms.desktop)
    nativeDebug wpi.java.vendor.jniDebug(wpi.platforms.desktop)
    simulationDebug wpi.sim.enableDebug()

    nativeRelease wpi.java.deps.wpilibJniRelease(wpi.platforms.desktop)
    nativeRelease wpi.java.vendor.jniRelease(wpi.platforms.desktop)
    simulationRelease wpi.sim.enableRelease()

    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.1'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}

test {
    useJUnitPlatform()
    systemProperty 'junit.jupiter.extensions.autodetection.enabled', 'true'
}

jar {
    from { configurations.runtimeClasspath.collect { it.isDirectory() ? it : zipTree(it) } }
    from sourceSets.main.allSource
    manifest edu.wpi.first.gradlerio.GradleRIOPlugin.javaManifest(ROBOT_MAIN_CLASS)
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

deployArtifact.jarTask = jar
wpi.java.configureExecutableTasks(jar)
wpi.java.configureTestTasks(test)

tasks.withType(JavaCompile) {
    options.compilerArgs.add '-XDstringConcat=inline'
}
```

- [ ] **Step 2: Write the three subsystems**

All three are **given and working**. Each has named factory commands, which is also a worked example of the idiom the student is about to use.

`src/main/java/frc/robot/subsystems/DrivetrainSubsystem.java`:

```java
package frc.robot.subsystems;

import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.DrivetrainConstants;
import java.util.function.DoubleSupplier;

/** Given, and working. Two motors and a differential drive, all simulated. */
public class DrivetrainSubsystem extends SubsystemBase {
  private final PWMSparkMax left = new PWMSparkMax(DrivetrainConstants.LEFT_MOTOR_CHANNEL);
  private final PWMSparkMax right = new PWMSparkMax(DrivetrainConstants.RIGHT_MOTOR_CHANNEL);
  private final DifferentialDrive drive = new DifferentialDrive(left::set, right::set);

  public DrivetrainSubsystem() {
    drive.setSafetyEnabled(false);
  }

  /** Drives from two live inputs. This is the drivetrain's default command. */
  public Command driveWithSticks(DoubleSupplier forward, DoubleSupplier rotation) {
    return run(() -> drive.arcadeDrive(forward.getAsDouble(), rotation.getAsDouble()))
        .withName("Drive With Sticks");
  }

  /** Stops both motors. */
  public Command stopCommand() {
    return runOnce(() -> drive.arcadeDrive(0.0, 0.0)).withName("Stop Drivetrain");
  }
}
```

`src/main/java/frc/robot/subsystems/ElevatorSubsystem.java`:

```java
package frc.robot.subsystems;

import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.ElevatorConstants;

/** Given, and working. */
public class ElevatorSubsystem extends SubsystemBase {
  private final PWMSparkMax motor = new PWMSparkMax(ElevatorConstants.MOTOR_CHANNEL);
  private double commandedSpeed;

  /** Runs the elevator up until it is interrupted. */
  public Command raiseCommand() {
    return run(() -> setSpeed(ElevatorConstants.RAISE_SPEED))
        .finallyDo(interrupted -> setSpeed(0.0))
        .withName("Raise Elevator");
  }

  /** Runs the elevator down until it is interrupted. */
  public Command lowerCommand() {
    return run(() -> setSpeed(-ElevatorConstants.RAISE_SPEED))
        .finallyDo(interrupted -> setSpeed(0.0))
        .withName("Lower Elevator");
  }

  /** The speed most recently commanded. Useful when you want to see what happened. */
  public double commandedSpeed() {
    return commandedSpeed;
  }

  private void setSpeed(double speed) {
    commandedSpeed = speed;
    motor.set(speed);
  }
}
```

`src/main/java/frc/robot/subsystems/GripperSubsystem.java`:

```java
package frc.robot.subsystems;

import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.GripperConstants;

/** Given, and working. */
public class GripperSubsystem extends SubsystemBase {
  private final PWMSparkMax motor = new PWMSparkMax(GripperConstants.MOTOR_CHANNEL);
  private boolean closed;

  /** Closes the gripper and holds it closed. */
  public Command closeCommand() {
    return runOnce(
            () -> {
              closed = true;
              motor.set(GripperConstants.GRIP_SPEED);
            })
        .withName("Close Gripper");
  }

  /** Opens the gripper. */
  public Command openCommand() {
    return runOnce(
            () -> {
              closed = false;
              motor.set(0.0);
            })
        .withName("Open Gripper");
  }

  /** Whether the gripper is currently holding. */
  public boolean isClosed() {
    return closed;
  }
}
```

- [ ] **Step 3: Write `Main`, `Robot`, `Constants`, and `RobotContainer`**

`src/main/java/frc/robot/Main.java`:

```java
package frc.robot;

import edu.wpi.first.wpilibj.RobotBase;

/** Do not edit. Hands control to {@link Robot}. */
public final class Main {
  private Main() {}

  public static void main(String... args) {
    RobotBase.startRobot(Robot::new);
  }
}
```

`src/main/java/frc/robot/Robot.java`:

```java
package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj2.command.CommandScheduler;

/** Given, and working. Runs the scheduler every 20ms and nothing else. */
public class Robot extends TimedRobot {
  @SuppressWarnings("unused")
  private final RobotContainer robotContainer = new RobotContainer();

  @Override
  public void robotPeriodic() {
    CommandScheduler.getInstance().run();
  }

  @Override
  public void testInit() {
    CommandScheduler.getInstance().cancelAll();
  }
}
```

`src/main/java/frc/robot/Constants.java`:

```java
package frc.robot;

/** Given. Channels and speeds for a robot that does not exist outside the simulator. */
public final class Constants {
  private Constants() {}

  /** The driver's controller. */
  public static final class OperatorConstants {
    private OperatorConstants() {}

    public static final int DRIVER_CONTROLLER_PORT = 0;
  }

  /** The drivetrain. */
  public static final class DrivetrainConstants {
    private DrivetrainConstants() {}

    public static final int LEFT_MOTOR_CHANNEL = 0;
    public static final int RIGHT_MOTOR_CHANNEL = 1;
  }

  /** The elevator. */
  public static final class ElevatorConstants {
    private ElevatorConstants() {}

    public static final int MOTOR_CHANNEL = 2;
    public static final double RAISE_SPEED = 0.4;
  }

  /** The gripper. */
  public static final class GripperConstants {
    private GripperConstants() {}

    public static final int MOTOR_CHANNEL = 3;
    public static final double GRIP_SPEED = 0.3;
  }
}
```

`src/main/java/frc/robot/RobotContainer.java` — working, except one method:

```java
package frc.robot;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import frc.robot.subsystems.DrivetrainSubsystem;
import frc.robot.subsystems.ElevatorSubsystem;
import frc.robot.subsystems.GripperSubsystem;

/**
 * A working three-subsystem robot. Everything here runs today except {@link #combinedCommand()},
 * which is your job — see README.md.
 */
public class RobotContainer {
  private final CommandXboxController driverController =
      new CommandXboxController(Constants.OperatorConstants.DRIVER_CONTROLLER_PORT);

  private final DrivetrainSubsystem drivetrain = new DrivetrainSubsystem();
  private final ElevatorSubsystem elevator = new ElevatorSubsystem();
  private final GripperSubsystem gripper = new GripperSubsystem();

  public RobotContainer() {
    configureDefaultCommands();
    configureBindings();
  }

  private void configureDefaultCommands() {
    drivetrain.setDefaultCommand(
        drivetrain.driveWithSticks(
            () -> -driverController.getLeftY(), () -> -driverController.getRightX()));
  }

  private void configureBindings() {
    driverController.a().whileTrue(elevator.raiseCommand());
    driverController.b().whileTrue(elevator.lowerCommand());
    driverController.x().onTrue(gripper.closeCommand());
    driverController.y().onTrue(gripper.openCommand());

    // Your binding goes here. README.md, part 2.
  }

  /**
   * Part 1 — return a new instance of the command you wrote.
   *
   * <p>A new instance every call: WPILib will not schedule a command that already belongs to a
   * composition, so a stored field handed back repeatedly cannot be scheduled.
   */
  public Command combinedCommand() {
    throw new UnsupportedOperationException("TODO(T3): return your combined command");
  }

  /** Given, for the tests. */
  public DrivetrainSubsystem drivetrain() {
    return drivetrain;
  }

  /** Given, for the tests. */
  public ElevatorSubsystem elevator() {
    return elevator;
  }

  /** Given, for the tests. */
  public GripperSubsystem gripper() {
    return gripper;
  }
}
```

- [ ] **Step 4: Write the given fixture and tests**

`src/test/java/frc/robot/harness/SchedulerFixture.java` — T3's own copy of the HAL and scheduler support. Given, locked:

```java
package frc.robot.harness;

import static org.junit.jupiter.api.Assertions.fail;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.simulation.DriverStationSim;
import edu.wpi.first.wpilibj.simulation.SimHooks;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.Subsystem;
import frc.robot.RobotContainer;

/** Given test support for T3. Part of the fixture — do not edit. */
public final class SchedulerFixture {
  private static RobotContainer container;

  private SchedulerFixture() {}

  /** Call from {@code @BeforeEach}. */
  public static void setUp() {
    if (!HAL.initialize(500, 0)) {
      fail("The WPILib hardware layer would not start. Show a mentor — this is not your code.");
    }
    DriverStationSim.setDsAttached(true);
    DriverStationSim.setEnabled(true);
    for (int i = 0; i < 100 && !DriverStation.isEnabled(); i++) {
      DriverStationSim.notifyNewData();
    }
    if (!DriverStation.isEnabled()) {
      fail("The simulated driver station never enabled. Show a mentor — this is not your code.");
    }
    SimHooks.pauseTiming();
    CommandScheduler.getInstance().cancelAll();
    container();
  }

  /** Call from {@code @AfterEach}. */
  public static void tearDown() {
    CommandScheduler.getInstance().cancelAll();
    CommandScheduler.getInstance().run();
    SimHooks.resumeTiming();
  }

  private static RobotContainer container() {
    if (container == null) {
      try {
        container = new RobotContainer();
      } catch (RuntimeException | Error e) {
        return fail("Building RobotContainer threw " + describe(e));
      }
    }
    return container;
  }

  private static String describe(Throwable e) {
    return e.getClass().getSimpleName() + (e.getMessage() == null ? "" : ": " + e.getMessage());
  }

  /** A fresh instance of the student's combined command, or a readable failure. */
  public static Command combinedCommand() {
    Command first = call();
    Command second = call();
    if (first == second) {
      fail(
          "combinedCommand() handed back the same Command object twice. Build a new one inside "
              + "the method — WPILib will not schedule a command that belongs to a composition.");
    }
    return second;
  }

  private static Command call() {
    Command command;
    try {
      command = container().combinedCommand();
    } catch (UnsupportedOperationException e) {
      return fail(
          "combinedCommand() still throws UnsupportedOperationException — that is the stub this "
              + "fixture ships with. Part 1 of the test-out is to replace it. See README.md.");
    } catch (RuntimeException e) {
      return fail("combinedCommand() threw " + describe(e) + " — that came out of your code.");
    }
    if (command == null) {
      return fail("combinedCommand() returned null. The tests have no other way to reach it.");
    }
    return command;
  }

  /** The elevator. */
  public static Subsystem elevator() {
    return container().elevator();
  }

  /** The gripper. */
  public static Subsystem gripper() {
    return container().gripper();
  }

  /** The drivetrain. */
  public static Subsystem drivetrain() {
    return container().drivetrain();
  }

  /** Schedules a command, turning WPILib's rejection into something readable. */
  public static void schedule(String what, Command command) {
    try {
      CommandScheduler.getInstance().schedule(command);
    } catch (IllegalArgumentException e) {
      fail(
          what
              + " could not be scheduled — "
              + e.getMessage()
              + "\nReturn a new command from combinedCommand() rather than a stored one.");
    }
  }

  /** Runs the scheduler {@code loops} times, advancing simulated time 20ms per loop. */
  public static void runScheduler(int loops) {
    for (int i = 0; i < loops; i++) {
      CommandScheduler.getInstance().run();
      SimHooks.stepTiming(0.02);
    }
  }

  /** A given command that requires whatever it is handed and never finishes on its own. */
  public static HoldCommand holdCommand(Subsystem... requirements) {
    return new HoldCommand(requirements);
  }

  /** Given. Occupies a subsystem so the tests can watch what interrupting it does. */
  public static final class HoldCommand extends Command {
    private boolean interruptedEndRan;

    private HoldCommand(Subsystem... requirements) {
      addRequirements(requirements);
      setName("Given Hold");
    }

    @Override
    public boolean isFinished() {
      return false;
    }

    @Override
    public void end(boolean interrupted) {
      if (interrupted) {
        interruptedEndRan = true;
      }
    }

    /** True once this command's {@code end(true)} has run. */
    public boolean wasInterrupted() {
      return interruptedEndRan;
    }
  }
}
```

`src/test/java/frc/robot/CombinedCommandTest.java`:

```java
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
```

- [ ] **Step 5: Prove the clean-clone invariant and read the messages**

```bash
cd /Users/y0shi/workspace/ignite/docs/projects/testouts/t3-scheduler-conflict
export JAVA_HOME=~/wpilib/2026/jdk
./gradlew clean --console=plain > /dev/null
./gradlew assemble --console=plain;        echo "assemble rc=$? (expect 0)"
./gradlew compileTestJava --console=plain; echo "compileTestJava rc=$? (expect 0)"
./gradlew test --console=plain;            echo "test rc=$? (expect 1)"
```

All five cases must fail, every one of them on `combinedCommand() still throws UnsupportedOperationException`. Paste the output. A failure mentioning a compile error or `UnsatisfiedLinkError` means the fixture is broken, not incomplete.

Then provoke the other two messages and read them: make `combinedCommand()` `return null;`, run, quote; then return a stored field (`private final Command stored = elevator.raiseCommand();`) so the freshness check fires, run, quote. `git checkout` after each.

- [ ] **Step 6: Complete the fixture end to end, inside its own time box**

A fixture nobody has finished is a fixture nobody knows is completable. Set a timer for 60 minutes — the fixture's stated box — and do the whole test-out as a student would, from the README only.

Part 1, in `RobotContainer`:

```java
  public Command combinedCommand() {
    return elevator
        .raiseCommand()
        .alongWith(gripper.closeCommand())
        .withTimeout(2.0)
        .withName("Raise And Grip");
  }
```

Part 2, in `configureBindings()`:

```java
    driverController.rightBumper().onTrue(combinedCommand());
```

```bash
cd /Users/y0shi/workspace/ignite/docs/projects/testouts/t3-scheduler-conflict
export JAVA_HOME=~/wpilib/2026/jdk
./gradlew test --console=plain; echo "test rc=$? (expect 0)"
```

Expected: all five green. Report how long the whole thing took from a cold read of the README — if it ran past 60 minutes, the time box in the README is wrong and one of the two has to change.

Then copy the answer to the solutions repository and revert here:

```bash
cd /Users/y0shi/workspace/ignite/docs
SOL=../ignite-training-solutions/solutions/testouts/t3-scheduler-conflict/src/main/java/frc/robot
mkdir -p "$SOL"
cp projects/testouts/t3-scheduler-conflict/src/main/java/frc/robot/RobotContainer.java "$SOL/"
git checkout projects/testouts/t3-scheduler-conflict/src/main/java/frc/robot/RobotContainer.java
./projects/testouts/t3-scheduler-conflict/gradlew -p projects/testouts/t3-scheduler-conflict \
    test --console=plain; echo "test rc=$? (expect 1 — red again)"
```

- [ ] **Step 7: Write `README.md`**

`projects/testouts/t3-scheduler-conflict/README.md`:

````markdown
# T3 — Scheduler Conflict

**Time: 60 minutes.** Tracks C and D.

This is a working robot. Three subsystems — a drivetrain, an elevator, and a
gripper — all wired up, all bound to buttons, all running in the simulator
today:

```bash
./gradlew simulateJava
```

There is exactly one thing missing.

---

## Part 1 — a command that needs two subsystems

Write a command that requires **the elevator and the gripper**, and return a new
instance of it from `RobotContainer.combinedCommand()`.

Either idiom is fine — a factory-style composition or an explicit `Command`
subclass. Name it. Do not make it require the drivetrain.

```bash
./gradlew test
```

Five tests, all green.

**Do not edit anything under `src/test/`.** Those tests are part of the fixture
and CI checks that they have not changed. If you think one is wrong, say so —
you might be right, and a mentor will look.

---

## Part 2 — bind it

Bind your command to a controller button in `configureBindings()`. No test
covers this. Run the simulator and press the button.

---

## Part 3 — the one that actually decides this

While your command is running, press the button bound to the elevator. Then
answer, out loud, to the mentor:

> What happens to your command, and when?

Be specific about three things: which command is still running afterwards, what
the scheduler did to the other one, and which of your command's four methods ran
as a result.

That is the whole test-out. Parts 1 and 2 check you can write and wire a
command. Part 3 checks you have the scheduler's model in your head, and the
difference between passing and failing is entirely in this answer.

"They both run" is a failing answer. So is "it queues".

---

## How you pass

1. Part 1's tests are green.
2. Your command is bound and you can demonstrate it.
3. Your part-3 explanation is correct.

If 60 minutes runs out, that is not a failure of anything. It means M3 is where
you start, which is exactly what this fixture exists to find out.
````

- [ ] **Step 8: Check nothing gives the answer away, then format and commit**

```bash
cd /Users/y0shi/workspace/ignite/docs/projects/testouts/t3-scheduler-conflict
grep -niE 'alongWith|Commands\.(sequence|parallel|race)|withTimeout|rightBumper' README.md src/main/java/frc/robot/RobotContainer.java
```

Expected: nothing. The README says *what* the command must require and that it must be named; it must not show how to build it, and `RobotContainer`'s remaining comment must not either. The three given subsystems do show named factory commands — that is the worked example the fixture is allowed to have, and it is why the grep is scoped away from `subsystems/`.

```bash
export JAVA_HOME=~/wpilib/2026/jdk
./gradlew spotlessApply --console=plain && ./gradlew spotlessCheck --console=plain
./gradlew test --console=plain; echo "test rc=$? (expect 1)"

cd /Users/y0shi/workspace/ignite/docs
git status --short   # no build/, no .gradle/, and RobotContainer.java back to the stub
git add projects/testouts/t3-scheduler-conflict
git commit -m "T3 scheduler-conflict test-out fixture"
```

`git diff --cached projects/testouts/t3-scheduler-conflict/src/main/java/frc/robot/RobotContainer.java` must show `combinedCommand()` throwing and the binding region empty. If it shows the answer, Step 6's revert did not happen.
