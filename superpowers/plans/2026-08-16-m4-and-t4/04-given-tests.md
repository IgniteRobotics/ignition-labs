# Task 4: Given tests, `SimDriver`, and the six required verification passes

The gate. Everything before this task built the mechanism; this task proves the gate that
checks a student's work actually gates on the right thing — reaches and holds for the
flywheel, holds against gravity for the arm, physically reaches the limit switch for
homing, and only that. **Every verification step below is required by `00-plan.md`'s
self-review and by `CLAUDE.md`'s verification discipline. None are optional, and a task
report that omits their actual command output is not a completed task.**

**Files:**
- Create: `projects/m4-control/src/test/java/frc/robot/harness/SimDriver.java`
- Create: `projects/m4-control/src/test/java/frc/robot/FlywheelVelocityTest.java`
- Create: `projects/m4-control/src/test/java/frc/robot/ArmGravityTest.java`
- Create: `projects/m4-control/src/test/java/frc/robot/ArmHomingTest.java`
- Modify (temporarily, reverted before commit): `projects/m4-control/src/main/java/frc/robot/RobotContainer.java`

**Consumes:** Task 3's `M4Deliverables`, `Robot`, `Constants`, and the `RobotContainer`
stub.
**Produces:** `frc.robot.harness.SimDriver` (named in this task and in Task 8) and the
three given test files that gate the milestone.

**Lane:** `milestone`. Worktree `.worktrees/m4`.

---

- [ ] **Step 1: Write `SimDriver`**

The stepped loop, in one place — the M3 equivalent is `DeliverablesFixture`. Builds
exactly one `RobotContainer`, turns a stub's `UnsupportedOperationException` into a
sentence, and pairs "run your update method" with "step the plant" in the same order
`Robot.java` does.

Create `projects/m4-control/src/test/java/frc/robot/harness/SimDriver.java`:

```java
package frc.robot.harness;

import static org.junit.jupiter.api.Assertions.fail;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.simulation.DriverStationSim;
import edu.wpi.first.wpilibj.simulation.SimHooks;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.Constants;
import frc.robot.RobotContainer;
import frc.robot.plant.ArmPlant;
import frc.robot.plant.FlywheelPlant;

/**
 * Given test support. Part of the milestone -- do not edit.
 *
 * <p>Builds exactly one {@code RobotContainer} for the whole run, turns a stub's {@code
 * UnsupportedOperationException} into a sentence you can act on, and owns the stepped loop every
 * given test drives your control code through: call your update method, then advance the plant's
 * physics by exactly one {@link Constants#SIM_STEP_SECONDS} tick. That pairing is exactly what
 * {@code Robot.robotPeriodic()} and {@code Robot.simulationPeriodic()} do, one call apart -- a
 * given test looks like one lap of the real robot loop.
 */
public final class SimDriver {
  private static RobotContainer container;
  private static AssertionError containerFailure;

  private SimDriver() {}

  /** Call from {@code @BeforeEach}. */
  public static void setUp() {
    if (!HAL.initialize(500, 0)) {
      fail(
          "The WPILib hardware layer would not start. This is a toolchain problem, not "
              + "something you did -- show a mentor.");
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
          "The simulated driver station never reported the robot as enabled. This is a "
              + "toolchain problem -- show a mentor.");
    }
  }

  /** Your {@code RobotContainer}, or a readable failure explaining why it could not be had. */
  public static RobotContainer container() {
    if (containerFailure != null) {
      throw containerFailure;
    }
    if (container == null) {
      try {
        container = new RobotContainer();
      } catch (RuntimeException | Error e) {
        try {
          fail(
              "Building your RobotContainer threw "
                  + describe(e)
                  + "\nEvery test in this milestone has to build one first, so nothing can pass "
                  + "until that constructor runs cleanly. Start there.");
        } catch (AssertionError failure) {
          containerFailure = failure;
          throw failure;
        }
      }
    }
    return container;
  }

  private static String describe(Throwable e) {
    return e.getClass().getSimpleName() + (e.getMessage() == null ? "" : ": " + e.getMessage());
  }

  private static FlywheelPlant flywheelPlant() {
    FlywheelPlant plant;
    try {
      plant = container().flywheelPlant();
    } catch (UnsupportedOperationException e) {
      return fail(
          "flywheelPlant() in RobotContainer still throws UnsupportedOperationException. "
              + "Construct a FlywheelPlant in your constructor and return it.");
    }
    if (plant == null) {
      return fail("flywheelPlant() returned null. The harness needs the real plant back.");
    }
    return plant;
  }

  private static ArmPlant armPlant() {
    ArmPlant plant;
    try {
      plant = container().armPlant();
    } catch (UnsupportedOperationException e) {
      return fail(
          "armPlant() in RobotContainer still throws UnsupportedOperationException. Construct "
              + "an ArmPlant in your constructor and return it.");
    }
    if (plant == null) {
      return fail("armPlant() returned null. The harness needs the real plant back.");
    }
    return plant;
  }

  /**
   * Runs {@code iterations} loops of {@code updateFlywheel()} followed by one physics step, and
   * returns the measured velocity, rotations per second, after each iteration.
   */
  public static double[] runFlywheelLoop(int iterations) {
    double[] history = new double[iterations];
    for (int i = 0; i < iterations; i++) {
      try {
        container().updateFlywheel();
      } catch (UnsupportedOperationException e) {
        fail(
            "updateFlywheel() in RobotContainer still throws UnsupportedOperationException. "
                + "Implement one 20ms iteration of your feedforward + feedback loop.");
      }
      flywheelPlant().step(Constants.SIM_STEP_SECONDS);
      history[i] = flywheelPlant().velocityRotationsPerSecond();
      SimHooks.stepTiming(Constants.SIM_STEP_SECONDS);
    }
    return history;
  }

  /**
   * Runs {@code iterations} loops of {@code updateArm()} followed by one physics step, and
   * returns the measured position, radians, after each iteration.
   */
  public static double[] runArmLoop(int iterations) {
    double[] history = new double[iterations];
    for (int i = 0; i < iterations; i++) {
      try {
        container().updateArm();
      } catch (UnsupportedOperationException e) {
        fail(
            "updateArm() in RobotContainer still throws UnsupportedOperationException. "
                + "Implement one 20ms iteration of your feedforward + feedback loop.");
      }
      armPlant().step(Constants.SIM_STEP_SECONDS);
      history[i] = armPlant().measuredPositionRadians();
      SimHooks.stepTiming(Constants.SIM_STEP_SECONDS);
    }
    return history;
  }

  /**
   * The outcome of {@link #runHomingLoop(int)}: whether {@code homingCommand()} finished within
   * budget, and whether the arm was actually at its reverse limit at the exact moment it did.
   *
   * <p>Checking at the finish instant, rather than at the end of the loop's full iteration budget,
   * catches a homing command that finishes <em>immediately</em> without ever driving anywhere -- at
   * t=0 the arm is nowhere near the limit, so {@code atHomeLimitWhenFinished} is false. It does
   * <b>not</b> catch a homing command that never drives at all but simply waits before zeroing:
   * this arm's gravity carries an un-driven arm down to a stable rest exactly at the limit switch
   * and it stays there, so any wait past the fall time reads as "arrived" by this check too.
   * Distinguishing "actively driven here" from "gravity did it" would require instrumenting whether
   * {@link ArmPlant#setVoltage} was ever called with a meaningfully nonzero value, which is outside
   * what the pinned plant contract exposes to a test. Documented here rather than silently left for
   * a future maintainer to rediscover.
   */
  public static final class HomingResult {
    public final boolean finishedWithinBudget;
    public final boolean atHomeLimitWhenFinished;
    public final double measuredPositionRadiansWhenFinished;

    private HomingResult(
        boolean finishedWithinBudget,
        boolean atHomeLimitWhenFinished,
        double measuredPositionRadiansWhenFinished) {
      this.finishedWithinBudget = finishedWithinBudget;
      this.atHomeLimitWhenFinished = atHomeLimitWhenFinished;
      this.measuredPositionRadiansWhenFinished = measuredPositionRadiansWhenFinished;
    }
  }

  /**
   * Schedules a fresh {@code homingCommand()} and runs the scheduler for up to {@code iterations}
   * loops, stepping the arm's physics once per loop -- the same pairing {@code Robot} does. Stops
   * the instant the scheduler reports the command finished, and reports the arm's state at that
   * exact tick rather than after however many more ticks the loop budget happens to allow.
   */
  public static HomingResult runHomingLoop(int iterations) {
    Command homing;
    try {
      homing = container().homingCommand();
    } catch (UnsupportedOperationException e) {
      return fail(
          "homingCommand() in RobotContainer still throws UnsupportedOperationException. Return "
              + "a new command that drives to the reverse limit, then zeroes.");
    }
    if (homing == null) {
      return fail("homingCommand() returned null. The harness needs a real command back.");
    }
    try {
      CommandScheduler.getInstance().schedule(homing);
    } catch (IllegalArgumentException e) {
      return fail("homingCommand() could not be scheduled -- " + e.getMessage());
    }
    for (int i = 0; i < iterations; i++) {
      boolean wasScheduled = CommandScheduler.getInstance().isScheduled(homing);
      CommandScheduler.getInstance().run();
      armPlant().step(Constants.SIM_STEP_SECONDS);
      SimHooks.stepTiming(Constants.SIM_STEP_SECONDS);
      boolean isScheduledNow = CommandScheduler.getInstance().isScheduled(homing);
      if (wasScheduled && !isScheduledNow) {
        return new HomingResult(
            true, armPlant().atHomeLimit(), armPlant().measuredPositionRadians());
      }
    }
    return new HomingResult(false, armPlant().atHomeLimit(), armPlant().measuredPositionRadians());
  }
}
```

- [ ] **Step 2: Write `FlywheelVelocityTest`**

Create `projects/m4-control/src/test/java/frc/robot/FlywheelVelocityTest.java`:

```java
package frc.robot;

import static org.junit.jupiter.api.Assertions.assertTrue;

import frc.robot.Constants.FlywheelConstants;
import frc.robot.harness.SimDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Given. Part of the milestone -- do not edit. */
class FlywheelVelocityTest {

  private static final int ITERATIONS = 250; // 5s at Constants.SIM_STEP_SECONDS
  private static final int HOLD_WINDOW = 50; // the last 1s must stay settled
  private static final double VELOCITY_TOLERANCE_RPS = 2.0;
  private static final double RUNAWAY_LIMIT_RPS =
      FlywheelConstants.TARGET_ROTATIONS_PER_SECOND * 2.5;

  @BeforeEach
  void setUp() {
    SimDriver.setUp();
  }

  @AfterEach
  void tearDown() {
    SimDriver.tearDown();
  }

  @Test
  void reachesAndHoldsCommandedVelocity() {
    SimDriver.container().setFlywheelTarget(FlywheelConstants.TARGET_ROTATIONS_PER_SECOND);

    double[] measured = SimDriver.runFlywheelLoop(ITERATIONS);

    for (int i = 0; i < measured.length; i++) {
      assertTrue(
          Math.abs(measured[i]) <= RUNAWAY_LIMIT_RPS,
          "Flywheel velocity hit "
              + measured[i]
              + " rot/s at iteration "
              + i
              + ", more than 2.5x the "
              + FlywheelConstants.TARGET_ROTATIONS_PER_SECOND
              + " rot/s target. That is a runaway, not a fast response -- a loop with no "
              + "feedback term, or a feedback term with the wrong sign, looks like this.");
    }

    for (int i = ITERATIONS - HOLD_WINDOW; i < ITERATIONS; i++) {
      assertTrue(
          Math.abs(measured[i] - FlywheelConstants.TARGET_ROTATIONS_PER_SECOND)
              <= VELOCITY_TOLERANCE_RPS,
          "At iteration "
              + i
              + " (t="
              + (i * Constants.SIM_STEP_SECONDS)
              + "s) the flywheel measured "
              + measured[i]
              + " rot/s, more than "
              + VELOCITY_TOLERANCE_RPS
              + " rot/s away from the "
              + FlywheelConstants.TARGET_ROTATIONS_PER_SECOND
              + " rot/s target. Checked over the last second of a five-second run, so a response "
              + "that is merely slow to arrive is not enough -- it has to still be there.");
    }
  }
}
```

- [ ] **Step 3: Write `ArmGravityTest`**

Create `projects/m4-control/src/test/java/frc/robot/ArmGravityTest.java`:

```java
package frc.robot;

import static org.junit.jupiter.api.Assertions.assertTrue;

import frc.robot.Constants.ArmConstants;
import frc.robot.harness.SimDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Given. Part of the milestone -- do not edit. */
class ArmGravityTest {

  private static final int ITERATIONS = 250; // 5s at Constants.SIM_STEP_SECONDS
  private static final int HOLD_WINDOW = 50; // the last 1s must stay settled
  private static final double POSITION_TOLERANCE_RADIANS = 0.05; // ~3 degrees

  @BeforeEach
  void setUp() {
    SimDriver.setUp();
  }

  @AfterEach
  void tearDown() {
    SimDriver.tearDown();
  }

  @Test
  void holdsPositionAgainstGravity() {
    SimDriver.container().setArmTarget(ArmConstants.SCORING_ANGLE_RADIANS);

    double[] measured = SimDriver.runArmLoop(ITERATIONS);

    for (int i = ITERATIONS - HOLD_WINDOW; i < ITERATIONS; i++) {
      assertTrue(
          Math.abs(measured[i] - ArmConstants.SCORING_ANGLE_RADIANS)
              <= POSITION_TOLERANCE_RADIANS,
          "At iteration "
              + i
              + " (t="
              + (i * Constants.SIM_STEP_SECONDS)
              + "s) the arm measured "
              + measured[i]
              + " rad, more than "
              + POSITION_TOLERANCE_RADIANS
              + " rad away from the "
              + ArmConstants.SCORING_ANGLE_RADIANS
              + " rad target. An arm feedforward with no gravity term sags below its target and "
              + "settles there -- that is what this window is checking for, over the last second "
              + "of a five-second run.");
    }
  }
}
```

- [ ] **Step 4: Write `ArmHomingTest`**

`atHomeLimit()` reads the arm's true physical position; `measuredPositionRadians()` reads
the encoder, which `zeroHere()` rebases unconditionally, at whatever position it is called
from. Checking both at the *end of the test window* is not enough on its own — see the
box below, found and fixed during this task's own review. Check both **at the exact instant
`homingCommand()` reports finished**, via `SimDriver.HomingResult`.

**Why end-of-window checking is not sufficient, verified empirically.** This arm starts at
15°, above `HOME_ANGLE_RADIANS` (−30°), and gravity pulls it toward the limit with no
voltage commanded at all. Over a full 5-second (250-iteration) window, an *un-driven* arm
falls into its own reverse limit switch on gravity alone — so a homing command whose
`isFinished()` never once depends on `atHomeLimit()` can still have the physical limit
switch tripped by the time the test's fixed iteration budget runs out, purely by
coincidence of timing. Checking `atHomeLimit()` at the moment the command itself reports
done removes that coincidence for the most likely real bug (an immediate no-op zero), but
**does not** remove it entirely — see the residual documented in `SimDriver.HomingResult`'s
Javadoc and repeated in this task's self-review below. A test that only checked the final
state after 250 iterations would have let a homing command that does nothing but wait pass
outright; this repository's own review process is what caught it, not automated tooling.

Create `projects/m4-control/src/test/java/frc/robot/ArmHomingTest.java`:

```java
package frc.robot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import frc.robot.harness.SimDriver;
import frc.robot.harness.SimDriver.HomingResult;
import frc.robot.plant.ArmPlant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Given. Part of the milestone -- do not edit. */
class ArmHomingTest {

  private static final int ITERATIONS = 250; // 5s at Constants.SIM_STEP_SECONDS
  private static final double POSITION_TOLERANCE_RADIANS = 0.01;

  @BeforeEach
  void setUp() {
    SimDriver.setUp();
  }

  @AfterEach
  void tearDown() {
    SimDriver.tearDown();
  }

  @Test
  void homesToTheLimitSwitchFromAnUnknownStart() {
    HomingResult result = SimDriver.runHomingLoop(ITERATIONS);

    assertTrue(
        result.finishedWithinBudget,
        "homingCommand() never finished within five simulated seconds. It has to end on its "
            + "own once the arm is home -- check isFinished() actually returns true once "
            + "armPlant().atHomeLimit() does.");

    assertTrue(
        result.atHomeLimitWhenFinished,
        "homingCommand() reported finished, but the arm's reverse limit switch was not tripped "
            + "at that exact instant. Checking at the moment of finish, not after however many "
            + "seconds the test happens to run for, is deliberate: a gravity-loaded arm can "
            + "drift into its own limit switch given enough uncontrolled time, so a command that "
            + "finishes for the wrong reason could otherwise still look right by the end of a "
            + "long window. Your homingCommand() must not finish until armPlant().atHomeLimit() "
            + "is actually true.");

    assertEquals(
        ArmPlant.HOME_ANGLE_RADIANS,
        result.measuredPositionRadiansWhenFinished,
        POSITION_TOLERANCE_RADIANS,
        "homingCommand() finished at the reverse limit, but measuredPositionRadians() does not "
            + "read "
            + ArmPlant.HOME_ANGLE_RADIANS
            + " at that moment. Call zeroHere() once atHomeLimit() is true, not before.");
  }
}
```

- [ ] **Step 5: Compile, then run a CORRECT implementation against the suite**

Compile first:

```bash
export JAVA_HOME=~/wpilib/2026/jdk
cd projects/m4-control
./gradlew compileTestJava --console=plain   # expect rc=0
./gradlew test --console=plain              # expect rc=1 -- every test RED, from
                                             # UnsupportedOperationException-derived failures,
                                             # not a compile error
```

Paste the actual failure summary. Then temporarily fill in `RobotContainer.java` with a
correct implementation -- feedforward first, then a modest kP, kD only if it overshoots,
exactly the procedure the milestone teaches. **This file is never committed.** Design
record §3.1/§3.2 already derived and verified the numbers below by hand against a
discrete-ZOH model of the real plants (Task 2 pinned the constants that produce them) --
use them as a starting point, and confirm them against the real integrator rather than
re-deriving from scratch:

- **Flywheel feedforward.** True `kV` (volts per mechanism rotation/second) is `0.179` --
  `7.16 V` at the 40 rot/s target, comfortable rail headroom. `kS` and `kA` can start at
  zero.
- **Flywheel feedback.** **Stay under kP `1.4`.** Design record §3.2's discrete stability
  ceiling for this plant is `1.496` -- kP `1.0` peaks at `41.42` rot/s and settles clean;
  kP `2.0` diverges outright. If your loop diverges, that is very likely this ceiling, not
  a broken plant -- lower kP before suspecting anything else. **This number is a
  characterization result and must never appear in `README.md`, a Javadoc, a test, or a
  commit message that ships** -- it belongs only in this task file, the design record, and
  your own scratch notes while tuning the throwaway implementation.
- **Arm feedforward.** True `kG` (volts to hold the arm level) is `1.99 V`.
  `ArmFeedforward` scales it by `cos(angle)` for you. `kV`/`kA` matter far less here than
  `kG` does -- max arm speed at full voltage is only `1.75 rad/s`.
- **Arm feedback.** `kP` large enough to close the gap `kG` alone leaves, `kD` only if it
  overshoots into oscillation. A `TrapezoidProfile` or `ProfiledPIDController` for the
  motion-profile requirement is real M4 content but is not exercised by
  `ArmGravityTest`/`ArmHomingTest` -- a plain `PIDController` is enough to make this step's
  suite pass; do not skip writing the profile in the actual student-facing README, only in
  this throwaway verification pass if time is short.

If what you observe running against the real `FlywheelSim`/`SingleJointedArmSim` disagrees
meaningfully with these numbers -- not "tuning took a few tries," an actual mismatch like
the arm holding on a voltage nowhere near `1.99 V` -- **stop and report the discrepancy**
rather than quietly retuning until the suite goes green. These parameters were pinned
specifically because an earlier, unverified pairing would have shipped a test that passed
even with `kG` omitted entirely; silently drifting away from the verified numbers now risks
reintroducing exactly that.

Run the suite:

```bash
./gradlew test --console=plain
```

**Iterate the gains until all three given tests are green — paste the actual `BUILD
SUCCESSFUL` output with the test count.** If a mechanism cannot reach its tolerance window
within 250 iterations no matter what you try, that is Task 2 Step 5's sanity check having
been wrong in practice -- go back and adjust `J_KG_M2`/`GEARING`/`ARM_LENGTH_METERS` in the
plant, not the test's tolerance, and note the change when you report back.

Then delete every trace of the reference implementation:

```bash
git diff --stat -- src/main/java/frc/robot/RobotContainer.java   # confirm it changed
git checkout -- src/main/java/frc/robot/RobotContainer.java
git status --short   # confirm RobotContainer.java is no longer modified
```

- [ ] **Step 6: Run the DISTRACTORS — each must fail the test aimed at it**

For each row: apply the change to `RobotContainer.java` (starting from the correct
implementation you just deleted, or from the stub with a minimal implementation that
demonstrates the bug), run the named test, **read the actual failure message**, then
revert with `git checkout -- src/main/java/frc/robot/RobotContainer.java` before moving to
the next row.

| # | Distractor | Test it must fail | Why it fails |
|---|---|---|---|
| 1 | `updateFlywheel()` uses a `kP`-only loop (`voltage = kP * (target - measured)`), no feedforward, with the same modest `kP` the correct implementation used alongside its feedforward | `FlywheelVelocityTest` | Without `kV`, a small `kP` alone cannot produce enough steady-state voltage to reach a nonzero velocity target — it settles well short, failing the hold-window assertion |
| 2 | `updateFlywheel()`'s feedforward is computed against `flywheelPlant().velocityRotationsPerSecond() * FlywheelPlant`'s gearing (i.e. applies the 1.5x gear ratio a second time on top of a correctly-converted `kV`) | `FlywheelVelocityTest` | Systematically over- or under-commands voltage by the gearing factor; settles at the wrong velocity, failing the hold-window assertion. This is the trap `FlywheelPlant`'s Javadoc (Task 2) names directly: SysId characterizes rotor velocity, control targets mechanism velocity, and they are not the same number |
| 3 | `updateArm()`'s feedforward omits the gravity term entirely (`kG = 0`, or a plain `SimpleMotorFeedforward` instead of `ArmFeedforward`) | `ArmGravityTest` | Design record §3.1 verified this arm's kG-omitted miss at roughly 12° -- the arm sags below `SCORING_ANGLE_RADIANS` and settles there, well outside `POSITION_TOLERANCE_RADIANS` (~3°). This margin is the reason Task 2's arm parameters were pinned rather than picked freely: an earlier, lighter arm held within 3° even with `kG = 0`, which would have made this exact row a false negative |
| 4 | `homingCommand()` returns `Commands.runOnce(armPlant::zeroHere)` — zeroes immediately, never drives toward the limit at all | `ArmHomingTest` | **Two findings from two verification passes.** Against the *original* end-of-window design, this distractor's failure was NOT the one it looks like on paper: `zeroHere()` runs at t=0 from `STARTING_ANGLE_RADIANS` (15°), and gravity alone carries the un-driven arm down to the limit over the remaining 5-second window regardless — so the end-of-window `atHomeLimit()` check actually passed, and the distractor was only caught by `measuredPositionRadians()` reading `-1.309` rad instead of `-0.5236`. After `ArmHomingTest` was strengthened to check at the exact instant `homingCommand()` reports finished (see Step 1's `HomingResult`), this distractor is now caught the way it was always meant to be: `homing­Command() reported finished, but the arm's reverse limit switch was not tripped at that exact instant` — because at t=0.02s (one tick), the arm is still at ~15°, nowhere near the limit |

Paste each distractor's actual failing assertion message — this is the read-every-failure-message
discipline `CLAUDE.md` requires, and distractor 4 specifically is only meaningful if you
can see that it fails on the limit-switch assertion and not the position one.

**What `ArmHomingTest` does not prove — documented, not fixed.** Checking `atHomeLimit()`
at the instant `homingCommand()` reports finished (rather than at the end of the test's
fixed window) closes the gap for the most likely real student bug: a homing routine that
zeroes immediately without ever checking the limit. It does **not** close a more general
one. This arm's gravity carries an un-driven arm to a *stable rest exactly at the limit
switch* — it is not a transient the arm passes through and drifts away from, it is where
the arm stays, indefinitely, under zero voltage. That means a command shaped like

```java
Commands.waitSeconds(3).andThen(Commands.runOnce(armPlant::zeroHere))
```

— which drives nothing at all, ever — reliably **passes** `ArmHomingTest`, for any wait
constant longer than the arm's fall time. This was verified during this task's own review,
not merely reasoned about: `waitSeconds(3)` was run against the strengthened test and it
passed all three assertions. Closing this fully would require the test being able to tell
"the student actively drove voltage toward the limit" apart from "gravity did it alone" —
which means instrumenting whether `ArmPlant.setVoltage` was ever called with a
meaningfully nonzero value, something the pinned plant contract does not expose to a test
and that this task is not the right place to add (`ArmPlant` is Task 2's locked file).
**On a real robot this bug is essentially impossible** — homing a real arm with the motor
never commanded requires gravity to do the whole job at exactly the rate the test expects,
on hardware with real friction and stiction working against it — which is presumably why
spec §9.5 asks for a homed-to-a-limit-switch mechanism in the first place rather than a
more elaborate one; but the simulated test does not enforce this the way a real mechanism
would. Task 8's authoring notes should carry this forward so a future maintainer does not
"simplify" `HomingResult`'s finish-instant check back down to an end-of-window one without
knowing what problem it was solving, and does not conclude from a passing `ArmHomingTest`
alone that a submitted `homingCommand()` necessarily drives anything.

- [ ] **Step 7: The clean-clone invariant, from an actual fresh clone**

```bash
cd /Users/y0shi/workspace/ignite/docs
git status --short    # must be clean -- confirms Steps 5-6 left no residue
rm -rf /tmp/m4-clean-clone && git worktree add --detach /tmp/m4-clean-clone HEAD
cd /tmp/m4-clean-clone/projects/m4-control
export JAVA_HOME=~/wpilib/2026/jdk
chmod +x gradlew
./gradlew assemble --console=plain          # expect rc=0 -- GREEN
./gradlew compileTestJava --console=plain   # expect rc=0 -- GREEN
./gradlew test --console=plain              # expect rc=1 -- RED, from assertions
cd /Users/y0shi/workspace/ignite/docs
git worktree remove /tmp/m4-clean-clone
```

Paste all three command outputs in full. The red in the third command must come from
`assertTrue`/`assertEquals` failure messages inside the three given tests — never from a
compile error, a missing class, or an `UnsupportedOperationException` propagating past a
`fail()` call uncaught. If it does, that is a bug in `SimDriver`, not in the milestone.

- [ ] **Step 8: Confirm no gain leaked into the given tests**

```bash
cd projects/m4-control
grep -rniE '\bk[sSvVaAgGpPdD]\b|kSTATIC|kVELOCITY|kFF' src/test/
```

Expected: no output.

- [ ] **Step 9: Format and commit**

```bash
cd projects/m4-control
./gradlew spotlessApply --console=plain
./gradlew spotlessCheck --console=plain   # expect rc=0
./gradlew test --console=plain            # expect rc=1, unmodified stub, still RED from
                                           # assertions after formatting
cd /Users/y0shi/workspace/ignite/docs
git status --short   # confirm RobotContainer.java shows NO diff -- Step 5/6's edits must
                      # not have leaked back in
git add projects/m4-control/src/test
git commit -m "Add M4's given tests: flywheel velocity, arm gravity, arm homing

Verified against a correct reference implementation (deleted, never
committed) and four distractors -- P-only with no feedforward, gear ratio
applied twice on the flywheel feedforward, arm feedforward missing kG, and
a homing routine that zeroes without touching the limit switch. Clean-clone
invariant confirmed from an actual git worktree: assemble green,
compileTestJava green, test red from assertions."
```
