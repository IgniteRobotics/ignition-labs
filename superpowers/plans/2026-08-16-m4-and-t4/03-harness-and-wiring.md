# Task 3: `M4Deliverables`, `PlotHarness`, `SysIdWiring`, `Robot`, `Constants`, and the
`RobotContainer` stub

Everything a student needs and nothing they don't, at L1 density (harness given, control
logic theirs — match M3, do not exceed it). This task wires the whole project together
without writing a single line of the feedforward/feedback loop that is the actual lesson.

**The risky part is where physics stepping lives.** `Robot.simulationPeriodic()` steps
both plants; `Robot.robotPeriodic()` calls both `update*()` methods every 20ms. Given
tests never construct a `Robot` at all — they call `updateFlywheel()`/`updateArm()` and
`plant.step()` directly, in that order, through `SimDriver` (Task 4) — but the pairing has
to be the same one `Robot.java` uses, or a test passing would prove nothing about how the
robot actually runs.

**Files:**
- Create: `projects/m4-control/src/main/java/frc/robot/harness/M4Deliverables.java`
- Create: `projects/m4-control/src/main/java/frc/robot/harness/PlotHarness.java`
- Create: `projects/m4-control/src/main/java/frc/robot/harness/SysIdWiring.java`
- Create: `projects/m4-control/src/main/java/frc/robot/Main.java`
- Create: `projects/m4-control/src/main/java/frc/robot/Robot.java`
- Create: `projects/m4-control/src/main/java/frc/robot/Constants.java`
- Create: `projects/m4-control/src/main/java/frc/robot/RobotContainer.java`

**Consumes:** Task 2's `FlywheelPlant` and `ArmPlant`.
**Produces:** `frc.robot.harness.M4Deliverables` (the seam Task 4's given tests drive),
`PlotHarness`, `Constants.FlywheelConstants.TARGET_ROTATIONS_PER_SECOND`,
`Constants.ArmConstants.SCORING_ANGLE_RADIANS`, `Constants.SIM_STEP_SECONDS` — every name
exactly as pinned in `00-plan.md`.

**Lane:** `milestone`. Worktree `.worktrees/m4`.

---

- [ ] **Step 1: Write `M4Deliverables`**

The seam. Given tests cannot name `RobotContainer`'s own subsystem types because the
student hasn't written them yet, so they reach the student's work through this interface
instead — same reason M3 has `M3Deliverables`.

Create `projects/m4-control/src/main/java/frc/robot/harness/M4Deliverables.java`:

```java
package frc.robot.harness;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.plant.ArmPlant;
import frc.robot.plant.FlywheelPlant;

/**
 * Exposes your M4 deliverables to the test harness and to {@code Robot}. {@code RobotContainer}
 * implements it.
 *
 * <p>The given tests cannot name your subsystem classes — they don't know what you called them —
 * so they reach your control loop through these seven methods instead. {@code Robot} uses the
 * same seam: {@code robotPeriodic()} calls {@link #updateFlywheel()} and {@link #updateArm()}
 * every 20ms, and {@code simulationPeriodic()} steps the plants {@link #flywheelPlant()} and
 * {@link #armPlant()} return.
 *
 * <p><b>{@link #homingCommand()} must return a new instance each time it is called</b>, the same
 * rule M3's deliverables carried: a command object that has already run once cannot be scheduled
 * again.
 */
public interface M4Deliverables {

  /**
   * Sets the flywheel's target velocity. Takes effect on the next {@link #updateFlywheel()}.
   *
   * @param rotationsPerSecond target velocity, mechanism rotations per second
   */
  void setFlywheelTarget(double rotationsPerSecond);

  /**
   * Runs exactly one 20ms iteration of your flywheel control loop: read {@link
   * FlywheelPlant#velocityRotationsPerSecond()}, compute a voltage from your feedforward and
   * feedback, and call {@link FlywheelPlant#setVoltage(double)}.
   */
  void updateFlywheel();

  /**
   * Sets the arm's target position. Takes effect on the next {@link #updateArm()}.
   *
   * @param radians target position, radians from horizontal
   */
  void setArmTarget(double radians);

  /**
   * Runs exactly one 20ms iteration of your arm control loop: read {@link
   * ArmPlant#measuredPositionRadians()}, compute a voltage from your feedforward and feedback,
   * and call {@link ArmPlant#setVoltage(double)}.
   */
  void updateArm();

  /**
   * @return a NEW command that drives the arm toward {@link ArmPlant#atHomeLimit()} and, once
   *     there, calls {@link ArmPlant#zeroHere()} and finishes. A stored, already-run instance
   *     cannot be rescheduled — build it inside this method.
   */
  Command homingCommand();

  /**
   * @return your flywheel's plant. The same instance every call — unlike {@link
   *     #homingCommand()}, this is not required to be fresh.
   */
  FlywheelPlant flywheelPlant();

  /**
   * @return your arm's plant. The same instance every call.
   */
  ArmPlant armPlant();
}
```

- [ ] **Step 2: Write `PlotHarness`**

Given, locked. CSV to `build/plots/`, for the plots the README asks the student to produce
and read — spec §9.5's mentor-verified item. Not used by any given test; Task 5's
`README.md` is where a student is told to reach for it.

Create `projects/m4-control/src/main/java/frc/robot/harness/PlotHarness.java`:

```java
package frc.robot.harness;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Records a setpoint-versus-measurement history and writes it to a CSV under {@code build/plots/}.
 *
 * <p>Given, locked. Use this from your own code — a command that runs during {@code
 * simulateJava}, or a manual test you write yourself — to produce the plots this milestone asks
 * you to read. No given test uses it; it exists for you.
 *
 * <pre>{@code
 * PlotHarness harness = new PlotHarness("flywheel-step", "target_rps", "measured_rps");
 * double t = 0.0;
 * for (int i = 0; i < 250; i++) {
 *   // ... run one iteration of your loop ...
 *   harness.record(t, target, plant.velocityRotationsPerSecond());
 *   t += Constants.SIM_STEP_SECONDS;
 * }
 * Path written = harness.write();
 * }</pre>
 */
public final class PlotHarness {

  private final String name;
  private final String setpointLabel;
  private final String measurementLabel;
  private final List<double[]> rows = new ArrayList<>();

  /**
   * @param name file name, without extension — written to {@code build/plots/<name>.csv}
   * @param setpointLabel CSV column header for the setpoint
   * @param measurementLabel CSV column header for the measurement
   */
  public PlotHarness(String name, String setpointLabel, String measurementLabel) {
    this.name = name;
    this.setpointLabel = setpointLabel;
    this.measurementLabel = measurementLabel;
  }

  /**
   * Records one sample.
   *
   * @param timeSeconds elapsed time, seconds
   * @param setpoint the commanded value at this instant
   * @param measurement the measured value at this instant
   */
  public void record(double timeSeconds, double setpoint, double measurement) {
    rows.add(new double[] {timeSeconds, setpoint, measurement});
  }

  /**
   * Writes every recorded sample to {@code build/plots/<name>.csv}, creating the directory if it
   * does not exist.
   *
   * @return the path written
   * @throws IOException if the file could not be written
   */
  public Path write() throws IOException {
    Path dir = Path.of("build", "plots");
    Files.createDirectories(dir);
    Path file = dir.resolve(name + ".csv");
    StringBuilder csv = new StringBuilder();
    csv.append("time_seconds,").append(setpointLabel).append(',').append(measurementLabel).append('\n');
    for (double[] row : rows) {
      csv.append(row[0]).append(',').append(row[1]).append(',').append(row[2]).append('\n');
    }
    Files.writeString(file, csv.toString(), StandardCharsets.UTF_8);
    return file;
  }
}
```

- [ ] **Step 3: Write `SysIdWiring`**

Given boilerplate, Yellow-tier permitted — the CTRE `SysIdRoutine` + `SignalLogger` wiring
spec §9.5 asks for, so a student spends their effort on running and reading
characterization, not assembling it from the API docs.

**This class is not part of the pinned API contract and no given test exercises it.** If
`SysIdRoutine.Config` or `SysIdRoutine.Mechanism`'s exact constructor overload differs on
the installed WPILib version from what is shown below, adjust the call to match — the
shape that matters is a voltage consumer that reaches the motor through {@link
com.ctre.phoenix6.controls.VoltageOut} and a state consumer that reaches {@code
SignalLogger}, not this exact overload.

Create `projects/m4-control/src/main/java/frc/robot/harness/SysIdWiring.java`:

```java
package frc.robot.harness;

import com.ctre.phoenix6.SignalLogger;
import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.TalonFX;
import edu.wpi.first.units.Units;
import edu.wpi.first.wpilibj2.command.Subsystem;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine;

/**
 * Wires a {@link SysIdRoutine} to a {@code TalonFX}, logging through CTRE's {@code SignalLogger}.
 *
 * <p>Given boilerplate. Bind {@code routine.quasistatic(...)} and {@code routine.dynamic(...)}
 * to controller buttons in your own {@code RobotContainer}, in each of {@link
 * SysIdRoutine.Direction#kForward} and {@link SysIdRoutine.Direction#kReverse}, then run {@code
 * SignalLogger.start()} before and {@code SignalLogger.stop()} after — WPILib's SysId
 * documentation covers the rest of the workflow, which is the same for every mechanism.
 */
public final class SysIdWiring {

  private SysIdWiring() {}

  /**
   * @param motor the device to drive and log
   * @param logName a short name distinguishing this routine's log entries from any other
   * @param requirement the subsystem this routine requires, so it cannot run concurrently with
   *     your own control loop
   * @return a configured {@link SysIdRoutine} ready to bind to buttons
   */
  public static SysIdRoutine forMotor(TalonFX motor, String logName, Subsystem requirement) {
    VoltageOut request = new VoltageOut(0.0);
    return new SysIdRoutine(
        new SysIdRoutine.Config(
            null,
            null,
            null,
            state -> SignalLogger.writeString(logName + "-state", state.toString())),
        new SysIdRoutine.Mechanism(
            volts -> motor.setControl(request.withOutput(volts.in(Units.Volts))),
            null,
            requirement));
  }
}
```

- [ ] **Step 4: Write `Main.java`**

Byte-for-byte M3's — every WPILib project has one identical to this.

Create `projects/m4-control/src/main/java/frc/robot/Main.java`:

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

- [ ] **Step 5: Write `Robot.java`**

Given. `robotPeriodic()` calls both `update*()` methods every 20ms regardless of DS state
— the plants integrate whatever voltage they were last commanded with unconditionally
(design record §2-3), so there is no separate "only when enabled" branch to add here
without contradicting that model. `simulationPeriodic()` steps both plants; `TimedRobot`
calls it once per 20ms tick, immediately after `robotPeriodic()`, only when running under
simulation — which is exactly the pairing Task 4's `SimDriver` reproduces by hand.

Create `projects/m4-control/src/main/java/frc/robot/Robot.java`:

```java
package frc.robot;

import edu.wpi.first.epilogue.Epilogue;
import edu.wpi.first.epilogue.Logged;
import edu.wpi.first.epilogue.NotLogged;
import edu.wpi.first.wpilibj.TimedRobot;

/**
 * Given. You should not need to change anything in this file to finish M4.
 *
 * <p>Two things happen every 20ms: {@link #robotPeriodic()} runs your flywheel and arm loops
 * once each, and {@link #simulationPeriodic()} — called immediately afterward, only while
 * simulating — advances both plants' physics by the same tick. Given tests reproduce that same
 * pairing by hand, one call apart, without constructing a {@code Robot} at all.
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
```

- [ ] **Step 6: Write `Constants.java`**

Exactly the three names the pinned contract fixes, plus the two CAN IDs the plants need to
be constructed with — wiring, not a gain. **No gain constant of any kind goes in this
file; that is the milestone.**

Create `projects/m4-control/src/main/java/frc/robot/Constants.java`:

```java
package frc.robot;

/**
 * Numbers that describe the robot, in one place with names on them.
 *
 * <p>No gain lives here — not a kS, kV, kA, kG, kP, or kD, and not a number that happens to equal
 * one. Characterizing and tuning both mechanisms is the milestone; a gain in this file would hand
 * over the answer. Your own feedforward and PID gains belong in {@code RobotContainer}, as your
 * own fields.
 */
public final class Constants {
  private Constants() {}

  /** Seconds per simulated physics step. Given tests and {@code Robot.simulationPeriodic()} both use this. */
  public static final double SIM_STEP_SECONDS = 0.020;

  public static final class FlywheelConstants {
    private FlywheelConstants() {}

    /** CAN ID of the flywheel's TalonFX. */
    public static final int CAN_ID = 20;

    /** The velocity Part 1's given tests command and check against. */
    public static final double TARGET_ROTATIONS_PER_SECOND = 40.0;
  }

  public static final class ArmConstants {
    private ArmConstants() {}

    /** CAN ID of the arm's TalonFX. */
    public static final int CAN_ID = 21;

    /** The position Part 2's gravity-holding given test commands and checks against. 45 degrees. */
    public static final double SCORING_ANGLE_RADIANS = 0.7854;
  }
}
```

- [ ] **Step 7: Write the `RobotContainer` stub**

Student territory. Every method a throwing stub, same as M3's `RobotContainer` — including
`flywheelPlant()`/`armPlant()`, even though returning a constructed plant is not itself the
lesson; M3's `drivetrain()`/`mechanism()` were the same shape.

Create `projects/m4-control/src/main/java/frc/robot/RobotContainer.java`:

```java
package frc.robot;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.harness.M4Deliverables;
import frc.robot.plant.ArmPlant;
import frc.robot.plant.FlywheelPlant;

/**
 * Where M4 is really built. Everything else in this project is given.
 *
 * <p>Work through README.md in order: Part 1 is the flywheel, Part 2 is the arm. The three TODO
 * regions below are the whole job, and the seven methods at the bottom are how the tests and
 * {@code Robot} reach what you built — implement each to do or return the thing its Javadoc in
 * {@link M4Deliverables} names.
 */
public class RobotContainer implements M4Deliverables {

  // ===================== TODO 1 — construct your plants ======================
  // One FlywheelPlant and one ArmPlant, private final fields, using
  // Constants.FlywheelConstants.CAN_ID and Constants.ArmConstants.CAN_ID. Both
  // classes are given -- see plant/. README.md Part 1 step 1 / Part 2 step 1.
  // =============================================================================

  // ===================== TODO 2 — flywheel control ============================
  // Your feedforward, your feedback, and a field to hold the current target. One
  // iteration of the whole loop -- read the plant's velocity, compute a voltage,
  // command it -- is what updateFlywheel() below has to do. README.md Part 1.
  // =============================================================================

  // ===================== TODO 3 — arm control ==================================
  // Your feedforward (gravity matters here), your feedback, a motion profile, a
  // field to hold the current target, and the homing sequence homingCommand()
  // returns. README.md Part 2.
  // ==============================================================================

  public RobotContainer() {}

  @Override
  public void setFlywheelTarget(double rotationsPerSecond) {
    throw new UnsupportedOperationException("TODO(M4): store the flywheel target");
  }

  @Override
  public void updateFlywheel() {
    throw new UnsupportedOperationException(
        "TODO(M4): one 20ms iteration of your flywheel feedforward + feedback loop");
  }

  @Override
  public void setArmTarget(double radians) {
    throw new UnsupportedOperationException("TODO(M4): store the arm target");
  }

  @Override
  public void updateArm() {
    throw new UnsupportedOperationException(
        "TODO(M4): one 20ms iteration of your arm feedforward + feedback loop");
  }

  @Override
  public Command homingCommand() {
    throw new UnsupportedOperationException(
        "TODO(M4): return a NEW command that drives to the reverse limit, then zeroes");
  }

  @Override
  public FlywheelPlant flywheelPlant() {
    throw new UnsupportedOperationException("TODO(M4): return your FlywheelPlant");
  }

  @Override
  public ArmPlant armPlant() {
    throw new UnsupportedOperationException("TODO(M4): return your ArmPlant");
  }
}
```

- [ ] **Step 8: Compile**

```bash
export JAVA_HOME=~/wpilib/2026/jdk
cd projects/m4-control
./gradlew compileJava --console=plain    # expect rc=0
```

The unmodified stub must compile clean — this is half of the clean-clone invariant, and
Task 4 proves the other half (`test` red from assertions, not from a compile error).

- [ ] **Step 9: Confirm no given file hands over an answer**

```bash
cd projects/m4-control
grep -rniE 'you should assert|the answer is|expected value is|hint:' src/main/
grep -rniE '\bk[sSvVaAgGpPdD]\b|kSTATIC|kVELOCITY|kFF' src/main/
```

Expected: no output from either. `TODO(M4)` strings in `RobotContainer.java` are expected
and are not a hit for the first grep — they name what to do, not the answer.

- [ ] **Step 10: Format and commit**

```bash
cd projects/m4-control
./gradlew spotlessApply --console=plain
./gradlew spotlessCheck --console=plain   # expect rc=0
./gradlew compileJava --console=plain     # expect rc=0
cd /Users/y0shi/workspace/ignite/docs
git status --short
git add projects/m4-control/src/main/java/frc/robot
git commit -m "Wire M4: harness, Robot, Constants, and the RobotContainer stub

M4Deliverables is the seam both the given tests and Robot.java reach the
student's control loop through -- robotPeriodic() calls updateFlywheel()/
updateArm() every 20ms, simulationPeriodic() steps both plants immediately
after. No gain anywhere: Constants carries CAN IDs and the two given
setpoints only."
```
