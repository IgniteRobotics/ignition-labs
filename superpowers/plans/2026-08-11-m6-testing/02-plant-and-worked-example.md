# Task 2: The plant, the Phoenix support class, and the one worked example

Everything M6 gives the student. `LaunchProfile` is the pure-logic practice target, shaped after `Robot-2026`'s `LaunchCalculator` — the real one is one repository away, and this is the rehearsal. `ShooterFlywheel` is the `AutoCloseable` demonstration. `PhoenixTestSupport` is where the CTRE timing quirks are taught once instead of costing three students an afternoon each.

**The risky part is the partition of `LaunchProfile`'s public surface.** Exactly one method is worked-example territory; the other three are student territory, and Task 3's patches depend on that line holding. If the worked example touches a student-territory method — even incidentally, even in a setup assertion — the student gate becomes fake and nobody finds out until a cohort passes it having written nothing.

**Files:**
- Create: `projects/m6-testing/src/main/java/frc/robot/plant/LaunchProfile.java`
- Create: `projects/m6-testing/src/main/java/frc/robot/plant/ShooterFlywheel.java`
- Create: `projects/m6-testing/src/test/java/frc/robot/support/PhoenixTestSupport.java`
- Create: `projects/m6-testing/src/test/java/frc/robot/WorkedExampleTest.java`

**Interfaces:**
- Consumes: Task 1's `projects/m6-testing/` scaffold with both vendordeps resolving, and **Task 1's spike verdict** (needed at Step 6).
- Produces:
  - `LaunchProfile(double ticksPerRevolution, double gearRatio)`, `ticksToMechanismRotations(double) -> double`, `timeOfFlightSeconds(double) -> double`, `lookAheadDistanceMeters(double, double) -> double`, `atSetpoint(double, double) -> boolean`
  - `ShooterFlywheel(int canId)`, `setVelocity(double)`, `velocityRotationsPerSecond() -> double`, `motor() -> TalonFX`, `close()`
  - `PhoenixTestSupport.enableRobot()`, `settleAfterConstruction(TalonFX)`, `settleAfterControlRequest(TalonFX)`
  - **The territory partition:** worked example = `ticksToMechanismRotations` only. Student = `timeOfFlightSeconds`, `lookAheadDistanceMeters`, `atSetpoint`.

**Lane:** `milestone`. Worktree `.worktrees/m6`.

---

- [ ] **Step 1: Write `LaunchProfile`**

Given, implemented, correct, and locked. Unlike every earlier milestone's stubs, the student **reads this file** — they cannot write a test against a contract they cannot see. It is documented like production code, not like a stub.

Create `projects/m6-testing/src/main/java/frc/robot/plant/LaunchProfile.java`:

```java
package frc.robot.plant;

import edu.wpi.first.math.interpolation.InterpolatingDoubleTreeMap;

/**
 * The launch math for a flywheel shooter, reduced to pure logic.
 *
 * <p>This class is a deliberately simplified stand-in for {@code
 * Robot-2026/src/main/java/frc/robot/statemachines/LaunchCalculator.java}. The real one does
 * the same four jobs — convert encoder ticks to mechanism rotations, look up a time of flight
 * by distance, predict where the robot will be when the game piece actually leaves, and decide
 * whether the flywheel is up to speed — but it reaches them through a singleton, a pose
 * estimator, and a swerve drive's chassis speeds. None of that is testable without a robot.
 * This one takes doubles and returns doubles, which is the whole point: <b>pure logic is what
 * is worth testing.</b>
 *
 * <p>Nothing here is yours to change. Your job is to find out whether it is right.
 */
public final class LaunchProfile {

  /**
   * How close the measured flywheel velocity must be to the target, in rotations per second,
   * before a launch is allowed.
   */
  private static final double VELOCITY_TOLERANCE_RPS = 0.5;

  /**
   * Seconds between the decision to launch and the game piece actually leaving the launcher.
   * The robot keeps moving during that window, so the distance the math should use is not the
   * distance right now.
   */
  private static final double PHASE_DELAY_SECONDS = 0.03;

  /** Distance in meters to time of flight in seconds, measured on the practice field. */
  private static final InterpolatingDoubleTreeMap TIME_OF_FLIGHT = new InterpolatingDoubleTreeMap();

  static {
    TIME_OF_FLIGHT.put(1.38, 0.90);
    TIME_OF_FLIGHT.put(1.88, 1.09);
    TIME_OF_FLIGHT.put(3.15, 1.11);
    TIME_OF_FLIGHT.put(4.55, 1.12);
    TIME_OF_FLIGHT.put(5.68, 1.16);
  }

  private final double ticksPerRevolution;
  private final double gearRatio;

  /**
   * @param ticksPerRevolution encoder ticks in one full revolution of the motor shaft; must be
   *     positive
   * @param gearRatio motor revolutions per one revolution of the mechanism; must be positive
   * @throws IllegalArgumentException if either argument is zero, negative, or not a number
   */
  public LaunchProfile(double ticksPerRevolution, double gearRatio) {
    if (!(ticksPerRevolution > 0.0)) {
      throw new IllegalArgumentException(
          "ticksPerRevolution must be positive, got " + ticksPerRevolution);
    }
    if (!(gearRatio > 0.0)) {
      throw new IllegalArgumentException("gearRatio must be positive, got " + gearRatio);
    }
    this.ticksPerRevolution = ticksPerRevolution;
    this.gearRatio = gearRatio;
  }

  /**
   * Converts a raw encoder reading to rotations of the mechanism the motor drives.
   *
   * @param ticks raw encoder ticks; may be negative, meaning the mechanism turned backwards
   * @return mechanism rotations
   */
  public double ticksToMechanismRotations(double ticks) {
    return ticks / ticksPerRevolution / gearRatio;
  }

  /**
   * Looks up how long a game piece stays in the air for a given launch distance.
   *
   * <p>Distances between measured points are interpolated. Distances outside the measured range
   * clamp to the nearest endpoint rather than extrapolating — the practice-field data says
   * nothing about what happens at nine meters, and a straight-line guess out there would be
   * confidently wrong.
   *
   * @param distanceMeters distance from launcher to target
   * @return time of flight in seconds
   */
  public double timeOfFlightSeconds(double distanceMeters) {
    return TIME_OF_FLIGHT.get(distanceMeters);
  }

  /**
   * Corrects a launch distance for the robot's own motion during the launch delay.
   *
   * @param distanceMeters distance to the target right now
   * @param closingSpeedMetersPerSecond how fast the robot is closing on the target; positive
   *     means approaching, negative means retreating
   * @return the distance the launch math should actually use
   */
  public double lookAheadDistanceMeters(
      double distanceMeters, double closingSpeedMetersPerSecond) {
    return distanceMeters - closingSpeedMetersPerSecond * PHASE_DELAY_SECONDS;
  }

  /**
   * Whether the flywheel is close enough to its commanded velocity to launch.
   *
   * @param measuredRps measured flywheel velocity, rotations per second
   * @param targetRps commanded flywheel velocity, rotations per second
   * @return true when the difference is within tolerance, inclusive of the boundary
   */
  public boolean atSetpoint(double measuredRps, double targetRps) {
    return Math.abs(measuredRps - targetRps) <= VELOCITY_TOLERANCE_RPS;
  }
}
```

- [ ] **Step 2: Write `ShooterFlywheel`**

The `AutoCloseable` demonstration. WPILib recommends subsystems implement `AutoCloseable` so tests can tear down simulated hardware between cases; without it the second test in a file fails because the first one's device still holds its CAN ID.

Create `projects/m6-testing/src/main/java/frc/robot/plant/ShooterFlywheel.java`:

```java
package frc.robot.plant;

import com.ctre.phoenix6.controls.VelocityVoltage;
import com.ctre.phoenix6.hardware.TalonFX;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

/**
 * A one-motor flywheel, and the reason this project depends on Phoenix at all.
 *
 * <p>It implements {@link AutoCloseable} because WPILib recommends subsystems do, and the
 * recommendation exists for a concrete reason: a simulated device holds its CAN ID until it is
 * closed. Two tests in one file that each construct a {@code ShooterFlywheel} on the same ID
 * will fail on the second one unless the first was torn down. That failure looks like a bug in
 * your code and is not.
 */
public class ShooterFlywheel extends SubsystemBase implements AutoCloseable {

  private final TalonFX motor;
  private final VelocityVoltage request = new VelocityVoltage(0.0);

  public ShooterFlywheel(int canId) {
    motor = new TalonFX(canId);
  }

  /**
   * Commands the flywheel to a velocity.
   *
   * @param rotationsPerSecond target velocity
   */
  public void setVelocity(double rotationsPerSecond) {
    motor.setControl(request.withVelocity(rotationsPerSecond));
  }

  /**
   * @return the flywheel's most recently reported velocity, rotations per second
   */
  public double velocityRotationsPerSecond() {
    return motor.getVelocity().getValueAsDouble();
  }

  /**
   * The underlying motor, exposed so a test can reach its simulation state and its status
   * signals. Production code has no business calling this.
   *
   * @return the TalonFX this flywheel drives
   */
  public TalonFX motor() {
    return motor;
  }

  @Override
  public void close() {
    motor.close();
  }
}
```

- [ ] **Step 3: Write `PhoenixTestSupport`**

Spec §9.7's stated purpose: teach the timing quirks once, in a helper with a comment explaining each delay, instead of losing three students to intermittent failures. The waits are `BaseStatusSignal.waitForAll`, **not** `Thread.sleep` — see the constraints delta.

Create `projects/m6-testing/src/test/java/frc/robot/support/PhoenixTestSupport.java`:

```java
package frc.robot.support;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.StatusCode;
import com.ctre.phoenix6.hardware.TalonFX;
import edu.wpi.first.hal.HAL;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.simulation.DriverStationSim;

/**
 * The CTRE simulation setup that is not obvious and will waste an afternoon if you do not know
 * it.
 *
 * <p><b>This class stays here.</b> It is scaffolding for a sandbox, and it must never be copied
 * into {@code Robot-2026}. The pull request you open for this milestone is subject to the
 * pure-logic hard rule: no test you add to the season repository may depend on timing,
 * simulation, or hardware, because a flaky test there blocks the whole team's pull requests
 * during competition season. A flake here costs one student an afternoon. There is the
 * difference.
 *
 * <p>Note what these methods do <i>not</i> do: sleep. {@code Thread.sleep(100)} on your laptop
 * and {@code Thread.sleep(100)} on a loaded CI runner are not the same wait, which is how a
 * test becomes intermittent — and an intermittent failure is worse than a consistent one,
 * because you will assume your code is flaky when your test is. Waiting on the signal itself
 * asks the question you actually mean: <i>has the value arrived yet?</i>
 */
public final class PhoenixTestSupport {

  /**
   * CTRE documents roughly 100 ms after constructing devices and enabling before commanding an
   * actuator. That is how long the simulated device takes to come up and start publishing. The
   * timeout here is generous on purpose: it is a ceiling on how long we are willing to wait,
   * not a duration we intend to spend.
   */
  private static final double CONSTRUCTION_TIMEOUT_SECONDS = 0.5;

  /**
   * CTRE documents roughly 20 ms after a control request before the result is worth asserting
   * on. Same reasoning as above — this is the ceiling, and the wait returns as soon as the
   * signal updates.
   */
  private static final double CONTROL_REQUEST_TIMEOUT_SECONDS = 0.1;

  private PhoenixTestSupport() {}

  /**
   * Brings up the HAL and puts the simulated robot in enabled state.
   *
   * <p>A disabled robot ignores actuator commands, so a test that skips this asserts on a motor
   * that was never allowed to move — and the failure says nothing about why.
   */
  public static void enableRobot() {
    assertTrue(
        HAL.initialize(500, 0),
        "The WPILib HAL failed to initialize. This is an environment problem, not a problem "
            + "with your test: the desktop native libraries are not reaching the test JVM. "
            + "Check that JAVA_HOME points at ~/wpilib/2026/jdk and re-run.");
    DriverStationSim.setEnabled(true);
    DriverStationSim.notifyNewData();
    DriverStation.refreshData();
    assertTrue(
        DriverStation.isEnabled(),
        "The simulated Driver Station did not report enabled after setEnabled(true) and "
            + "notifyNewData(). Anything you command a motor to do from here will be ignored.");
  }

  /**
   * Waits for a freshly constructed device to start publishing.
   *
   * @param motor the device that was just constructed
   */
  public static void settleAfterConstruction(TalonFX motor) {
    StatusCode code = BaseStatusSignal.waitForAll(CONSTRUCTION_TIMEOUT_SECONDS, motor.getVelocity());
    assertTrue(
        code.isOK(),
        "A TalonFX did not start publishing within "
            + CONSTRUCTION_TIMEOUT_SECONDS
            + "s of being constructed (status: "
            + code
            + "). Every assertion after this point would be reading a value that was never "
            + "sent. If you see this, the device is not simulating — check that the robot was "
            + "enabled first.");
  }

  /**
   * Waits for a device to report the result of a control request.
   *
   * @param motor the device that was just commanded
   */
  public static void settleAfterControlRequest(TalonFX motor) {
    StatusCode code =
        BaseStatusSignal.waitForAll(CONTROL_REQUEST_TIMEOUT_SECONDS, motor.getVelocity());
    assertTrue(
        code.isOK(),
        "A TalonFX did not report a new velocity within "
            + CONTROL_REQUEST_TIMEOUT_SECONDS
            + "s of a control request (status: "
            + code
            + "). Asserting now would test the value from before the request, which passes or "
            + "fails at random.");
  }
}
```

- [ ] **Step 4: Write the one worked example**

L0 means **one** given test file. It carries two `@Test` methods because spec §9.7 asks it to demonstrate the `AutoCloseable` teardown pattern, and that cannot be shown on a pure-logic class.

**The partition is load-bearing.** This file may assert on `ticksToMechanismRotations` and nothing else on `LaunchProfile`. Not in a setup line, not in a sanity check, not "while we're here."

Create `projects/m6-testing/src/test/java/frc/robot/WorkedExampleTest.java`:

```java
package frc.robot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import frc.robot.plant.LaunchProfile;
import frc.robot.plant.ShooterFlywheel;
import frc.robot.support.PhoenixTestSupport;
import org.junit.jupiter.api.Test;

/**
 * The worked example. Read it, then write your own.
 *
 * <p>This is the only test in this project that was written for you. Everything else in {@code
 * src/test/} is yours, and three good tests beat an abandoned attempt at full coverage.
 *
 * <p><b>Do not edit this file.</b> It is covered by {@code tests.lock}; a pull request that
 * changes it fails CI with a message inviting you to disagree in review instead.
 */
class WorkedExampleTest {

  /**
   * A pure-logic test. No hardware, no simulation, no setup — construct the thing, call the
   * method, assert the answer.
   *
   * <p>Three things here are worth copying into your own tests:
   *
   * <ol>
   *   <li><b>The name says what is being claimed.</b> Not {@code test1}. When this fails in CI
   *       six weeks from now, the name is the first thing anyone reads.
   *   <li><b>The expected value was worked out by hand, not by running the code.</b> 4096 ticks
   *       at 2048 ticks per revolution is two motor revolutions; at a 4:1 reduction that is
   *       half a mechanism revolution. If you compute the expectation the same way the code
   *       does, you have written a test that agrees with any bug the code contains.
   *   <li><b>The tolerance argument is not optional.</b> {@code assertEquals(0.5, actual)} on
   *       doubles compares exact bit patterns and fails on floating-point noise. That is M1's
   *       lesson arriving in test form, and it is the single most common way a correct test is
   *       written wrong.
   * </ol>
   */
  @Test
  void ticksToMechanismRotations_appliesBothTicksPerRevolutionAndGearRatio() {
    LaunchProfile profile = new LaunchProfile(2048.0, 4.0);

    assertEquals(0.5, profile.ticksToMechanismRotations(4096.0), 1e-9);
  }

  /**
   * A simulated-hardware test, and the reason {@link ShooterFlywheel} implements {@link
   * AutoCloseable}.
   *
   * <p>The try-with-resources block is the pattern to copy. A simulated device holds its CAN ID
   * until it is closed, so a second test that constructs a flywheel on ID 1 fails unless this
   * one tore its own down — and it tears down even if an assertion throws, which a {@code
   * close()} call at the end of the method would not.
   *
   * <p>Note what is asserted and what is not. This checks that <b>a command reached the
   * device</b>. It does not assert that the simulated motor spins up to exactly the velocity
   * requested, because that would be testing whether CTRE's physics model behaves the way CTRE
   * says it does — which tests CTRE, not you. If your assertion would pass regardless of what
   * you wrote, you have not tested your code.
   */
  @Test
  void shooterFlywheel_acceptsAControlRequest_andClosesCleanly() {
    PhoenixTestSupport.enableRobot();

    try (ShooterFlywheel flywheel = new ShooterFlywheel(1)) {
      PhoenixTestSupport.settleAfterConstruction(flywheel.motor());

      flywheel.motor().getSimState().setSupplyVoltage(12.0);
      flywheel.setVelocity(20.0);
      PhoenixTestSupport.settleAfterControlRequest(flywheel.motor());

      assertTrue(
          flywheel.motor().getVelocity().getStatus().isOK(),
          "The flywheel's velocity signal is not reporting OK after a control request, so "
              + "there is nothing meaningful to assert about the value it holds.");
    }
  }
}
```

- [ ] **Step 5: Run the suite and confirm it is GREEN**

M6 is the one milestone that is green on a clean clone. This is the step that proves it.

```bash
export JAVA_HOME=~/wpilib/2026/jdk
cd projects/m6-testing
./gradlew assemble --console=plain          # expect rc=0
./gradlew compileTestJava --console=plain   # expect rc=0
./gradlew test --console=plain              # expect rc=0 — GREEN, 2 tests
```

If `test` is red, M6 is broken. Do not proceed and do not "fix" it by relaxing an assertion — read the failure. An `UnsatisfiedLinkError` means `wpi.java.configureTestTasks(test)` is missing from `build.gradle`, not that the test is wrong.

- [ ] **Step 6: Apply Task 1's spike verdict**

Look up the verdict the Task 1 agent reported.

**If the spike passed (20/20):** nothing to do. Continue to Step 7.

**If the spike failed:** the Phoenix worked-example test cannot run in CI. Add the tag to `shooterFlywheel_acceptsAControlRequest_andClosesCleanly`:

```java
  @Tag("phoenix-sim")
  @Test
  void shooterFlywheel_acceptsAControlRequest_andClosesCleanly() {
```

with `import org.junit.jupiter.api.Tag;`, and add the exclusion to `build.gradle`'s existing `test` block:

```groovy
test {
    useJUnitPlatform {
        // Phoenix simulation was measured as unreliable on shared CI runners (Task 1's
        // spike). It runs locally, where a student can see it work and a mentor can watch;
        // it is excluded in CI so that nobody's pull request goes red for a reason that was
        // never theirs. If a future toolchain makes this stable, delete the exclusion --
        // do not delete the test.
        if (System.getenv("CI") != null) {
            excludeTags 'phoenix-sim'
        }
    }
    systemProperty 'junit.jupiter.extensions.autodetection.enabled', 'true'
}
```

Then re-run Step 5 with `CI=true ./gradlew test --console=plain --rerun-tasks` and confirm one test runs, green. Report which branch you took — Task 9 needs it for `README.md`.

- [ ] **Step 7: Confirm the territory partition holds — by grep, before Task 3 depends on it**

```bash
cd projects/m6-testing
grep -nE 'timeOfFlightSeconds|lookAheadDistanceMeters|atSetpoint' \
  src/test/java/frc/robot/WorkedExampleTest.java
```

**Expected: no output, exit status 1.** Any hit means the worked example touches student territory and Task 3's student-tier patches will be caught by the given test — which makes the student gate fake. Fix it here; it is far cheaper than finding out in Task 3.

- [ ] **Step 8: Confirm no given file hands over an answer**

The plant is given implemented, so the usual "a stub never contains the answer" grep does not apply to it. What must not appear is a *test* the student owes, or a comment telling them what to assert:

```bash
cd projects/m6-testing
grep -rniE 'you should assert|the answer is|expected value is|hint:|TODO' src/
```

Expected: no output. A `TODO` in a given file at L0 is scaffolding that fade level removed on purpose.

- [ ] **Step 9: Read every failure message `PhoenixTestSupport` can emit**

Not "check they exist" — trigger them and read what a student sees. A probe whose messages have never been read is a probe whose messages are wrong.

Temporarily, one at a time, then revert each:

1. Comment out `DriverStationSim.setEnabled(true)` in `enableRobot()`, run `./gradlew test`, read the enabled-state failure.
2. In `WorkedExampleTest`, call `settleAfterConstruction` on a device constructed **before** `enableRobot()`, run, read the construction-timeout failure.
3. Pass `0.0` as `CONSTRUCTION_TIMEOUT_SECONDS` and run, to see the timeout message with a real `StatusCode` interpolated rather than a placeholder.

For each: paste the actual message into your task report and confirm it tells a fifteen-year-old what to do next. Revert every edit before Step 10 — `git diff` must be clean of them.

- [ ] **Step 10: Format and commit**

```bash
cd projects/m6-testing
./gradlew spotlessApply --console=plain
./gradlew spotlessCheck --console=plain     # expect rc=0
./gradlew test --console=plain              # expect rc=0, still green after formatting
cd /Users/y0shi/workspace/ignite/docs
git status --short    # no reference tests, no leftover debug edits
git add projects/m6-testing
git commit -m "Add M6's plant, Phoenix support, and the one worked example

LaunchProfile ships implemented -- at L0 the student writes tests, not code,
and cannot write a test against a contract they cannot read. Its public
surface is partitioned: ticksToMechanismRotations is worked-example
territory, the other three methods are the student's, and Task 3's
two-tier patches depend on that line holding."
```
