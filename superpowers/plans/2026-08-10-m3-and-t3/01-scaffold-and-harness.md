# Task 1: M3 scaffold, the given robot, and the deliverables harness

This task builds everything the student is *given* and nothing they are supposed to write. Two things here are new to this repository and both can fail silently: the WPILib **HAL** must start inside `./gradlew test` (M0–M2 tests were plain Java and never touched native code), and the **Epilogue annotation processor** must run without turning a clean `assemble` red. Prove both before writing anything that depends on them. The `DeliverablesFixture` is M3's `ContractProbe` — if its messages are wrong, the milestone is unusable, so this task ends by reading every message it can emit rather than assuming.

**Files:**
- Create: `projects/m3-command-based/settings.gradle`, `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`, `gradle/wrapper/gradle-wrapper.properties`, `.gitignore`, `WPILib-License.md`, `.wpilib/wpilib_preferences.json` (copies)
- Create: `projects/m3-command-based/build.gradle`
- Create: `projects/m3-command-based/vendordeps/WPILibNewCommands.json` (copy)
- Create: `projects/m3-command-based/src/main/java/frc/robot/Main.java`
- Create: `projects/m3-command-based/src/main/java/frc/robot/Robot.java`
- Create: `projects/m3-command-based/src/main/java/frc/robot/Constants.java`
- Create: `projects/m3-command-based/src/main/java/frc/robot/RobotContainer.java`
- Create: `projects/m3-command-based/src/main/java/frc/robot/harness/M3Deliverables.java`
- Test: `projects/m3-command-based/src/test/java/frc/robot/harness/DeliverablesFixture.java`
- Test: `projects/m3-command-based/src/test/java/frc/robot/CommandContractTest.java` (one smoke case only; Task 2 fills it out)

**Consumes:** nothing.

**Produces:** the project directory Tasks 2 and 3 add to, and two APIs they are written against.

`frc.robot.harness.M3Deliverables` (main source, given, locked):
- `Subsystem drivetrain()`
- `Subsystem mechanism()`
- `Command subclassCommand()`
- `Command factoryCommand()`
- `Command composition()`

`frc.robot.harness.DeliverablesFixture` (test source, given, locked):
- `static void setUp()` — HAL, DriverStation enabled, timing paused, scheduler reset, container constructed once
- `static void tearDown()` — scheduler drained, timing resumed
- `static Subsystem drivetrain()` / `mechanism()`
- `static Command subclassCommand()` / `factoryCommand()` / `composition()` — each proves freshness before returning
- `static void schedule(String member, Command command)`
- `static void runScheduler(int loops)` — one 20ms simulated tick per loop
- `static HoldCommand holdCommand(Subsystem... requirements)` — a given command that never finishes and records whether its `end(true)` ran
- `HoldCommand.wasInterrupted() -> boolean`

**Lane:** `milestone`. Work in `.worktrees/m3`.

---

- [ ] **Step 1: Copy the scaffold from M0**

```bash
cd /Users/y0shi/workspace/ignite/docs
mkdir -p projects/m3-command-based
cd projects/m0-onboarding
for f in settings.gradle gradlew gradlew.bat gradle/wrapper/gradle-wrapper.jar \
         gradle/wrapper/gradle-wrapper.properties .gitignore WPILib-License.md \
         .wpilib/wpilib_preferences.json; do
  mkdir -p "../m3-command-based/$(dirname "$f")"
  cp "$f" "../m3-command-based/$f"
done
chmod +x ../m3-command-based/gradlew

mkdir -p ../m3-command-based/vendordeps
cp ~/wpilib/2026/vendordeps/WPILibNewCommands.json ../m3-command-based/vendordeps/
```

Copy byte-for-byte. `settings.gradle` sets no `rootProject.name` — Gradle derives it from the directory, and M0/M1/M2 do the same. Do not add one.

Confirm the vendordep is the 2026 one:

```bash
cd /Users/y0shi/workspace/ignite/docs
grep -E '"(name|version|frcYear)"' projects/m3-command-based/vendordeps/WPILibNewCommands.json
```

Expected: `"name": "WPILib-New-Commands"`, `"version": "1.0.0"`, `"frcYear": "2026"`.

- [ ] **Step 2: Write `build.gradle`**

This is `projects/m2-oop/build.gradle` with three differences, all marked: the Epilogue annotation processor, `ROBOT_MAIN_CLASS`, and the comment block explaining that this one *is* a robot.

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

// M3 is the first project in this repository that is actually a robot: frc.robot, a
// TimedRobot, a running CommandScheduler, and a simulator you launch with
// ./gradlew simulateJava. ROBOT_MAIN_CLASS is load-bearing here, unlike in M1 and M2.
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
    // Epilogue's annotation processor. Robot.java is annotated @Logged and calls
    // Epilogue.bind(this); without this line the generated Epilogue class does not
    // exist and Robot.java does not compile. Matches Robot-2026's build.gradle.
    annotationProcessor wpi.java.deps.wpilibAnnotations()

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

`wpi.java.configureTestTasks(test)` is the line that puts the desktop HAL natives on the test JVM's library path. It was already present in M0–M2 and did nothing there. From here it is the reason the tests can run at all.

- [ ] **Step 3: Spike the HAL, read the result, delete the spike**

Before a single line of harness code exists, prove that a JUnit test in this project can start the HAL and construct a PWM device. Everything in Tasks 1 and 2 assumes this; nothing so far has demonstrated it.

Write `projects/m3-command-based/src/test/java/frc/robot/HalSpikeTest.java`:

```java
package frc.robot;

import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;
import edu.wpi.first.wpilibj.simulation.DriverStationSim;
import edu.wpi.first.wpilibj.simulation.SimHooks;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import org.junit.jupiter.api.Test;

class HalSpikeTest {
  @Test
  void theHalStartsAndAPwmDeviceCanBeConstructed() {
    assertTrue(HAL.initialize(500, 0), "HAL.initialize returned false");

    DriverStationSim.setDsAttached(true);
    DriverStationSim.setEnabled(true);
    DriverStationSim.notifyNewData();
    System.out.println("SPIKE enabled=" + DriverStation.isEnabled());

    PWMSparkMax motor = new PWMSparkMax(0);
    motor.set(0.5);
    System.out.println("SPIKE motor=" + motor.get());

    SimHooks.pauseTiming();
    CommandScheduler.getInstance().run();
    SimHooks.stepTiming(0.02);
    SimHooks.resumeTiming();
    System.out.println("SPIKE scheduler ran");

    motor.close();
  }
}
```

```bash
cd /Users/y0shi/workspace/ignite/docs/projects/m3-command-based
export JAVA_HOME=~/wpilib/2026/jdk
./gradlew test --console=plain --info 2>&1 | grep -E 'SPIKE|UnsatisfiedLink|BUILD|FAILED'
```

Expected: `SPIKE enabled=true`, a non-zero `SPIKE motor=`, `SPIKE scheduler ran`, `BUILD SUCCESSFUL`.

**Paste the real output into the task report.** If it fails with `UnsatisfiedLinkError`, the desktop natives are not reaching the test JVM and every later step is built on sand — stop and report rather than working around it. If `enabled=false`, the DriverStation needs a bounded retry loop around `notifyNewData()`; note that and carry it into Step 6's `waitUntilEnabled`.

Then delete it. A passing test in `src/test/` inverts `skeleton-integrity.yml`'s gate:

```bash
rm projects/m3-command-based/src/test/java/frc/robot/HalSpikeTest.java
```

Nothing from this step is committed.

- [ ] **Step 4: Write `Main.java`**

`projects/m3-command-based/src/main/java/frc/robot/Main.java`:

```java
package frc.robot;

import edu.wpi.first.wpilibj.RobotBase;

/**
 * Do not edit this file. It exists to hand control to {@link Robot}, and every WPILib project has
 * one that looks exactly like this.
 */
public final class Main {
  private Main() {}

  public static void main(String... args) {
    RobotBase.startRobot(Robot::new);
  }
}
```

- [ ] **Step 5: Write `Robot.java` and `Constants.java`**

`projects/m3-command-based/src/main/java/frc/robot/Robot.java`. Thin on purpose: the scheduler call and Epilogue, and nothing else. The shape matches `Robot-2026` so the student recognises it when they read the real one.

```java
package frc.robot;

import edu.wpi.first.epilogue.Epilogue;
import edu.wpi.first.epilogue.Logged;
import edu.wpi.first.epilogue.NotLogged;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;

/**
 * Given. You should not need to change anything in this file to finish M3.
 *
 * <p>Two things happen here and they are the whole reason command-based works. {@code
 * robotPeriodic} runs the {@link CommandScheduler} every 20 milliseconds — that single call is what
 * makes every command you write actually execute. And Epilogue is bound to this object, which is
 * what puts the scheduler's state on the dashboard so you can watch which command is running.
 *
 * <p>Read {@code robotPeriodic} before you start. Tracing one pass through it, out loud, is a
 * definition-of-done item for this milestone.
 */
@Logged
public class Robot extends TimedRobot {
  @NotLogged private final RobotContainer robotContainer;

  @Logged(name = "Scheduler")
  CommandScheduler scheduler = CommandScheduler.getInstance();

  private Command autonomousCommand;

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
    // Polls the buttons, starts newly-scheduled commands, runs every running command's
    // execute(), ends the ones that are finished or interrupted, and runs every
    // subsystem's periodic(). Nothing in the command-based framework happens without it.
    CommandScheduler.getInstance().run();
  }

  @Override
  public void autonomousInit() {
    autonomousCommand = robotContainer.getAutonomousCommand();
    if (autonomousCommand != null) {
      autonomousCommand.schedule();
    }
  }

  @Override
  public void teleopInit() {
    if (autonomousCommand != null) {
      autonomousCommand.cancel();
    }
  }

  @Override
  public void testInit() {
    CommandScheduler.getInstance().cancelAll();
  }
}
```

`@NotLogged` on `robotContainer` is deliberate: Epilogue only generates loggers for `@Logged` types, and leaving the container out means the annotation processor never has to look at the classes the *student* writes. If it did, a student's subsystem field of some type Epilogue cannot log would turn their clean build red for a reason that has nothing to do with M3.

`projects/m3-command-based/src/main/java/frc/robot/Constants.java`:

```java
package frc.robot;

/**
 * Numbers that describe the robot, in one place with names on them.
 *
 * <p>Ignite splits these per mechanism — {@code ShooterConstants}, {@code IntakeConstants} — rather
 * than keeping one flat list, and this file follows that. When you add a subsystem, add a nested
 * class here for whatever it has to be told: channels, ratios, limits, speeds. A number typed
 * inline where it is used is a number nobody can find again.
 */
public final class Constants {
  private Constants() {}

  /** Given. The controller port that {@code simgui.json} already maps a joystick to. */
  public static final class OperatorConstants {
    private OperatorConstants() {}

    public static final int DRIVER_CONTROLLER_PORT = 0;
  }

  // Your subsystems' constants go here, one nested class each.
}
```

- [ ] **Step 6: Write the given interface and the fixture**

`projects/m3-command-based/src/main/java/frc/robot/harness/M3Deliverables.java`. This file is **given and locked** — it is in `harness/`, which `CODEOWNERS` and `tests.lock` cover (Task 9 adds both patterns).

```java
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
```

`projects/m3-command-based/src/test/java/frc/robot/harness/DeliverablesFixture.java`. Given, locked, and the piece everything else rests on:

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
import java.util.function.Function;

/**
 * Given test support. Part of the milestone — do not edit.
 *
 * <p>It owns four things the given tests would otherwise each have to get right: starting the
 * WPILib hardware layer, telling the simulated driver station the robot is enabled, stepping
 * simulated time so timeouts work, and building exactly one {@code RobotContainer} for the whole
 * run. It also turns every way a deliverable can be missing into a sentence you can act on.
 */
public final class DeliverablesFixture {
  private static final String COMPOSED_HINT =
      "WPILib will not schedule a command that belongs to a composition. Return a NEW command "
          + "from each M3Deliverables method rather than storing one in a field and handing the "
          + "same object back every time.";

  private static RobotContainer container;

  private DeliverablesFixture() {}

  /** Call from {@code @BeforeEach}. */
  public static void setUp() {
    if (!HAL.initialize(500, 0)) {
      fail(
          "The WPILib hardware layer would not start. This is a toolchain problem, not something "
              + "you did — show a mentor.");
    }
    DriverStationSim.setDsAttached(true);
    DriverStationSim.setEnabled(true);
    waitUntilEnabled();
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

  private static void waitUntilEnabled() {
    for (int i = 0; i < 100 && !DriverStation.isEnabled(); i++) {
      DriverStationSim.notifyNewData();
    }
    if (!DriverStation.isEnabled()) {
      fail(
          "The simulated driver station never reported the robot as enabled, so the scheduler "
              + "would refuse to run anything. This is a toolchain problem — show a mentor.");
    }
  }

  private static RobotContainer container() {
    if (container == null) {
      try {
        container = new RobotContainer();
      } catch (RuntimeException | Error e) {
        return fail(
            "Building your RobotContainer threw "
                + describe(e)
                + "\nEvery test in this milestone has to build one first, so nothing can pass "
                + "until that constructor runs cleanly. Start there.");
      }
    }
    return container;
  }

  private static String describe(Throwable e) {
    return e.getClass().getSimpleName() + (e.getMessage() == null ? "" : ": " + e.getMessage());
  }

  /** Your drivetrain, or a readable failure explaining why it could not be had. */
  public static Subsystem drivetrain() {
    return required("drivetrain()", M3Deliverables::drivetrain);
  }

  /** Your other subsystem, or a readable failure. */
  public static Subsystem mechanism() {
    return required("mechanism()", M3Deliverables::mechanism);
  }

  /** A fresh instance of your subclass command. */
  public static Command subclassCommand() {
    return fresh("subclassCommand()", M3Deliverables::subclassCommand);
  }

  /** A fresh instance of your factory command. */
  public static Command factoryCommand() {
    return fresh("factoryCommand()", M3Deliverables::factoryCommand);
  }

  /** A fresh instance of your composition. */
  public static Command composition() {
    return fresh("composition()", M3Deliverables::composition);
  }

  private static <T> T required(String member, Function<M3Deliverables, T> accessor) {
    T value;
    try {
      value = accessor.apply(container());
    } catch (UnsupportedOperationException e) {
      return fail(
          member
              + " in RobotContainer still throws UnsupportedOperationException.\nThat is the stub "
              + "this project ships with. Implement it and return the thing its Javadoc names. "
              + "README.md walks the order.");
    } catch (RuntimeException e) {
      return fail(
          member
              + " in RobotContainer threw "
              + describe(e)
              + "\nThat came out of your code, not out of the test. Fix that first.");
    }
    if (value == null) {
      return fail(
          member
              + " in RobotContainer returned null. The harness needs the real object back — the "
              + "tests have no other way to reach your work.");
    }
    return value;
  }

  private static Command fresh(String member, Function<M3Deliverables, Command> accessor) {
    Command first = required(member, accessor);
    Command second = required(member, accessor);
    if (first == second) {
      fail(member + " handed back the same Command object twice.\n" + COMPOSED_HINT);
    }
    return second;
  }

  /** Schedules a command, turning WPILib's rejection into something readable. */
  public static void schedule(String member, Command command) {
    try {
      CommandScheduler.getInstance().schedule(command);
    } catch (IllegalArgumentException e) {
      fail(member + " could not be scheduled — " + e.getMessage() + "\n" + COMPOSED_HINT);
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

If Step 3 reported `enabled=false`, `waitUntilEnabled` above is already the fix; confirm it converges and say how many iterations it took.

- [ ] **Step 7: Write `RobotContainer.java`**

Three marked TODO regions, the controller constructed, five throwing stubs. Nothing here names a subsystem, a command, or a mechanism — deciding those is the milestone.

```java
package frc.robot;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.Subsystem;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import frc.robot.harness.M3Deliverables;

/**
 * Where the robot is wired together: which subsystems exist, which commands are bound to which
 * buttons, and what runs when nothing else does.
 *
 * <p>This is the file M3 is really about. Everything else in this project is given.
 *
 * <p>Work through README.md in order. The three TODO regions below are the whole job, and the five
 * methods at the bottom are how the tests reach what you built — implement each one to return the
 * thing it names.
 */
public class RobotContainer implements M3Deliverables {
  /** Given. Port 0, which is the port {@code simgui.json} already maps a joystick to. */
  private final CommandXboxController driverController =
      new CommandXboxController(Constants.OperatorConstants.DRIVER_CONTROLLER_PORT);

  // ===================== TODO 1 — your subsystems ==========================
  // Two of them: a drivetrain, and one other mechanism. Private final fields on
  // this class — not static, not public, and not reachable from anywhere else.
  // README.md step 2.
  // =========================================================================

  public RobotContainer() {
    configureDefaultCommands();
    configureBindings();
  }

  /**
   * TODO 2 — bind your commands to the controller. Three bindings: one {@code onTrue}, one {@code
   * whileTrue}, and your composition. README.md step 5.
   */
  private void configureBindings() {}

  /**
   * TODO 3 — give the drivetrain a default command, so that something sensible happens when no
   * other command wants it. README.md step 6.
   */
  private void configureDefaultCommands() {}

  /** M3 has no autonomous. M5 is where that starts. */
  public Command getAutonomousCommand() {
    return Commands.none();
  }

  @Override
  public Subsystem drivetrain() {
    throw new UnsupportedOperationException("TODO(M3): return your drivetrain subsystem");
  }

  @Override
  public Subsystem mechanism() {
    throw new UnsupportedOperationException("TODO(M3): return your other subsystem");
  }

  @Override
  public Command subclassCommand() {
    throw new UnsupportedOperationException(
        "TODO(M3): return a new instance of your Command subclass");
  }

  @Override
  public Command factoryCommand() {
    throw new UnsupportedOperationException(
        "TODO(M3): return a new instance of your subsystem's factory command");
  }

  @Override
  public Command composition() {
    throw new UnsupportedOperationException("TODO(M3): return a new instance of your composition");
  }
}
```

`driverController` is unused until the student writes TODO 2. That is expected and produces no error.

- [ ] **Step 8: Write the one smoke test**

`projects/m3-command-based/src/test/java/frc/robot/CommandContractTest.java`. Task 2 fills this class out; right now it needs exactly one case, so that the project is red for the right reason from this commit onward.

```java
package frc.robot;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import frc.robot.harness.DeliverablesFixture;
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
}
```

- [ ] **Step 9: Check nothing here contains the answer**

```bash
cd /Users/y0shi/workspace/ignite/docs/projects/m3-command-based
grep -rniE 'PWMSparkMax|DifferentialDrive|SubsystemBase|addRequirements|withName|runOnce|arcadeDrive|extends Command' src/main/ && echo "FOUND — read each hit" || echo "clean"
```

Expected: `clean`. Every one of those is something the student writes. `HoldCommand` in the fixture does use `addRequirements` and `extends Command`, but that is under `src/test/` and is the harness — the grep is deliberately scoped to `src/main/`.

`Robot.java`'s Javadoc mentions the scheduler and Epilogue, which is description of given code, not the answer to anything. Read the hits rather than trusting the exit status.

- [ ] **Step 10: Prove the clean-clone invariant**

```bash
cd /Users/y0shi/workspace/ignite/docs/projects/m3-command-based
export JAVA_HOME=~/wpilib/2026/jdk
./gradlew clean --console=plain > /dev/null
./gradlew assemble --console=plain;        echo "assemble rc=$? (expect 0)"
./gradlew compileTestJava --console=plain; echo "compileTestJava rc=$? (expect 0)"
./gradlew test --console=plain;            echo "test rc=$? (expect 1)"
```

Three things must hold, and each needs its own line in the task report:

1. `assemble` rc=0 — and its output contains **no** Epilogue processor error. Grep it: `./gradlew clean assemble --console=plain 2>&1 | grep -iE 'epilogue|error|warning'`. Warnings are acceptable and should be quoted in the report; an error is not. If Epilogue errors on the `scheduler` field, the fallback is `@NotLogged` on it — take that and say so, do not remove `@Logged` from `Robot` (spec §9.4 requires Epilogue configured).
2. `compileTestJava` rc=0.
3. `test` rc=1, and the failure is `theRobotContainerExposesADrivetrain` failing an assertion — **not** a compile error, not an `UnsatisfiedLinkError`, not an `ExceptionInInitializerError`. Paste the failure block.

- [ ] **Step 11: Read every message the fixture can produce**

A fixture whose messages have never been read is a fixture whose messages are wrong. Provoke each one, read it as a fifteen-year-old would, and paste it into the report. Revert after each.

```bash
cd /Users/y0shi/workspace/ignite/docs/projects/m3-command-based
export JAVA_HOME=~/wpilib/2026/jdk
```

1. **Still a stub** — already covered by Step 10's run. Quote the message.
2. **Returns null** — edit `RobotContainer.drivetrain()` to `return null;`, run `./gradlew test`, quote the message, then `git checkout src/main/java/frc/robot/RobotContainer.java`.
3. **Throws something else** — edit it to `throw new IllegalStateException("not wired yet");`, run, quote, revert.
4. **Constructor blows up** — add `private final Object boom = broken();` plus a `private static Object broken() { throw new IllegalStateException("bad channel"); }` to `RobotContainer`, run, confirm the message names the constructor rather than the accessor, revert.

Each message must name the method, say what is wrong, and say what to do. If any of them reads like a stack trace with a sentence attached, rewrite it now — Task 2 and every student after depend on these.

- [ ] **Step 12: Format and commit**

```bash
cd /Users/y0shi/workspace/ignite/docs/projects/m3-command-based
export JAVA_HOME=~/wpilib/2026/jdk
./gradlew spotlessApply --console=plain && ./gradlew spotlessCheck --console=plain
./gradlew test --console=plain; echo "test rc=$? (expect 1 — still red after formatting)"

cd /Users/y0shi/workspace/ignite/docs
git status --short   # no build/, no .gradle/, no HalSpikeTest.java
git add projects/m3-command-based
git commit -m "M3 scaffold, given robot, and the deliverables harness"
```

Re-running the tests after `spotlessApply` is one command and it proves formatting did not accidentally change behaviour. `git status --short` must not list `HalSpikeTest.java` — if it does, Step 3's deletion did not happen and a passing test is about to be committed into a project whose CI gate requires red.
