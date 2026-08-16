# Task 4: `SimDriver` and the given tests, plus the seven required verification passes

The gate. Everything before this task built the mechanism; this task proves the gate
checks the right thing — a routine that actually terminates, a fused estimate that
actually beats odometry alone, and a filter that actually looks at the timestamp. **Every
verification step below is required by `00-plan.md`'s self-review and by `CLAUDE.md`'s
verification discipline. None are optional, and a task report that omits their actual
command output is not a completed task.**

**Step 7 is the one to not shortcut.** Confirming `FusedPoseTest` **fails** when vision is
never actually fused is, per `00-plan.md`, "the single easiest assertion in M5 to get
backwards" — a test that passes whether or not vision is wired proves nothing.

**Files:**
- Create: `projects/m5-auto-vision/src/test/java/frc/robot/harness/SimDriver.java`
- Create: `projects/m5-auto-vision/src/test/java/frc/robot/AutoTerminationTest.java`
- Create: `projects/m5-auto-vision/src/test/java/frc/robot/FusedPoseTest.java`
- Create: `projects/m5-auto-vision/src/test/java/frc/robot/BadTimestampTest.java`
- Modify (temporarily, reverted before commit):
  `projects/m5-auto-vision/src/main/java/frc/robot/RobotContainer.java`

**Consumes:** Task 3's `M5Deliverables`, `Robot`, `Constants`, and the `RobotContainer`
stub.
**Produces:** `frc.robot.harness.SimDriver` (named in this task and in Task 8) and the
three given test files that gate the milestone.

**Lane:** `milestone`. Worktree `.worktrees/m5`.

---

- [ ] **Step 1: Write `SimDriver`**

The stepped loop, in one place — M4's equivalent, extended with a second loop shape for
the drivetrain/vision half and a generic command-completion loop for the auto routine. Every
method `SimDriver` itself calls on your `RobotContainer` more than once, or that a test does
not call directly, gets a stub's `UnsupportedOperationException` turned into a sentence —
same rule M4's `SimDriver` established for `flywheelPlant()`/`armPlant()`.

The camera-frame-to-candidate-pose conversion here is the **same given code** `Robot.java`
uses (Task 3): a `PhotonPoseEstimator` built from `VisionSim.fieldLayout()` and
`vision().robotToCamera()`, calling the non-deprecated
`estimateLowestAmbiguityPose(PhotonPipelineResult)`. It is duplicated rather than shared
because `Robot.java` lives in `src/main/` and this file lives in `src/test/harness/` — main
cannot depend on test.

`runOdometryLoop` resets both the drivetrain's true pose and your pose estimator to the
same fixed starting pose **on every call**, so two calls in the same test method (one
`feedVision=false`, one `feedVision=true`, as `FusedPoseTest` makes) start from identical
conditions. The commanded drive pattern is fixed and deterministic — 1 m/s forward, 0.3
rad/s rotation — chosen to arc near the `k2026RebuiltWelded` tag cluster the example path
also targets, so the camera has something to see for most of a ten-second run without
requiring `setMaxSightRange` or a hand-tuned trajectory.

Create `projects/m5-auto-vision/src/test/java/frc/robot/harness/SimDriver.java`:

```java
package frc.robot.harness;

import static org.junit.jupiter.api.Assertions.fail;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.simulation.DriverStationSim;
import edu.wpi.first.wpilibj.simulation.SimHooks;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.Constants;
import frc.robot.RobotContainer;
import frc.robot.plant.SwerveDriveSim;
import frc.robot.plant.VisionSim;
import java.util.Optional;
import org.photonvision.EstimatedRobotPose;
import org.photonvision.PhotonPoseEstimator;

/**
 * Given test support. Part of the milestone -- do not edit.
 *
 * <p>Builds exactly one {@code RobotContainer} for the whole run, turns a stub's {@code
 * UnsupportedOperationException} into a sentence you can act on, and owns the two stepped loops
 * every given test drives your work through: {@link #runOdometryLoop} for pose fusion, {@link
 * #runCommandToCompletion} for the auto routine.
 */
public final class SimDriver {
  private static final Pose2d ODOMETRY_LOOP_START = new Pose2d(3.0, 4.0, Rotation2d.kZero);
  private static final ChassisSpeeds ODOMETRY_LOOP_SPEEDS = new ChassisSpeeds(1.0, 0.0, 0.3);

  private static RobotContainer container;
  private static AssertionError containerFailure;
  private static PhotonPoseEstimator visionEstimator;

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

  private static SwerveDriveSim drivetrain() {
    SwerveDriveSim drivetrain;
    try {
      drivetrain = container().drivetrain();
    } catch (UnsupportedOperationException e) {
      return fail(
          "drivetrain() in RobotContainer still throws UnsupportedOperationException. "
              + "Construct a SwerveDriveSim in your constructor and return it.");
    }
    if (drivetrain == null) {
      return fail("drivetrain() returned null. The harness needs the real drivetrain back.");
    }
    return drivetrain;
  }

  private static VisionSim vision() {
    VisionSim vision;
    try {
      vision = container().vision();
    } catch (UnsupportedOperationException e) {
      return fail(
          "vision() in RobotContainer still throws UnsupportedOperationException. Construct a "
              + "VisionSim in your constructor and return it.");
    }
    if (vision == null) {
      return fail("vision() returned null. The harness needs the real VisionSim back.");
    }
    return vision;
  }

  private static PhotonPoseEstimator visionEstimator() {
    if (visionEstimator == null) {
      visionEstimator =
          new PhotonPoseEstimator(VisionSim.fieldLayout(), vision().robotToCamera());
    }
    return visionEstimator;
  }

  /**
   * Runs {@code iterations} ticks of: drive a fixed pattern, step the drivetrain, update vision,
   * call {@code updateOdometry()}, and -- only if {@code feedVision} is true -- offer the camera's
   * latest frame to {@code acceptsMeasurement()}/{@code addVisionMeasurement()}, the same given
   * conversion {@code Robot.java} uses. Resets the drivetrain's true pose and your estimator to the
   * same fixed starting pose every call. Returns the distance between true pose and {@code
   * estimatedPose()} after each tick.
   */
  public static double[] runOdometryLoop(int iterations, boolean feedVision) {
    drivetrain().resetTo(ODOMETRY_LOOP_START);
    try {
      container().resetEstimator(ODOMETRY_LOOP_START);
    } catch (UnsupportedOperationException e) {
      fail(
          "resetEstimator() in RobotContainer still throws UnsupportedOperationException. Seed "
              + "your pose estimator with the pose it's given.");
    }

    double[] error = new double[iterations];
    for (int i = 0; i < iterations; i++) {
      drivetrain().drive(ODOMETRY_LOOP_SPEEDS);
      drivetrain().step(Constants.SIM_STEP_SECONDS);
      vision().update(drivetrain().truePose());

      try {
        container().updateOdometry();
      } catch (UnsupportedOperationException e) {
        fail(
            "updateOdometry() in RobotContainer still throws UnsupportedOperationException. "
                + "Update your pose estimator from drivetrain().gyroAngle() and "
                + "drivetrain().modulePositions() -- wheels and gyro only, no vision.");
      }

      if (feedVision) {
        Optional<EstimatedRobotPose> candidate =
            visionEstimator().estimateLowestAmbiguityPose(vision().latestResult());
        if (candidate.isPresent()) {
          Pose2d visionPose = candidate.get().estimatedPose.toPose2d();
          double timestamp = candidate.get().timestampSeconds;
          boolean accepted;
          try {
            accepted = container().acceptsMeasurement(visionPose, timestamp);
          } catch (UnsupportedOperationException e) {
            return fail(
                "acceptsMeasurement() in RobotContainer still throws "
                    + "UnsupportedOperationException. Decide whether to trust a vision pose "
                    + "here.");
          }
          if (accepted) {
            try {
              container().addVisionMeasurement(visionPose, timestamp);
            } catch (UnsupportedOperationException e) {
              return fail(
                  "addVisionMeasurement() in RobotContainer still throws "
                      + "UnsupportedOperationException. Fuse it into your pose estimator here.");
            }
          }
        }
      }

      Pose2d estimated;
      try {
        estimated = container().estimatedPose();
      } catch (UnsupportedOperationException e) {
        return fail(
            "estimatedPose() in RobotContainer still throws UnsupportedOperationException. "
                + "Return your pose estimator's current estimate.");
      }
      error[i] = drivetrain().truePose().getTranslation().getDistance(estimated.getTranslation());

      SimHooks.stepTiming(Constants.SIM_STEP_SECONDS);
    }
    return error;
  }

  /**
   * Schedules {@code command} and runs the scheduler for up to {@code maxIterations} loops,
   * stepping the drivetrain and vision once per loop -- the same pairing {@code Robot} does.
   * Returns whether the scheduler reported the command finished within that budget.
   */
  public static boolean runCommandToCompletion(Command command, int maxIterations) {
    try {
      CommandScheduler.getInstance().schedule(command);
    } catch (IllegalArgumentException e) {
      return fail("Could not schedule that command -- " + e.getMessage());
    }
    for (int i = 0; i < maxIterations; i++) {
      boolean wasScheduled = CommandScheduler.getInstance().isScheduled(command);
      CommandScheduler.getInstance().run();
      drivetrain().step(Constants.SIM_STEP_SECONDS);
      vision().update(drivetrain().truePose());
      SimHooks.stepTiming(Constants.SIM_STEP_SECONDS);
      boolean isScheduledNow = CommandScheduler.getInstance().isScheduled(command);
      if (wasScheduled && !isScheduledNow) {
        return true;
      }
    }
    return false;
  }
}
```

- [ ] **Step 2: Write `AutoTerminationTest`**

Black-box by necessity: a given test cannot reach into the student's own named commands to
literally inject a failure, so it verifies the necessary condition instead — the whole
composed routine finishes within a generous, but bounded, budget. A composition where every
step either finishes on its own (a path reaching its end) or is wrapped in
`.withTimeout(...)` always finishes inside this budget; a composition with one unbounded
step never does, regardless of whether that step happens to be "the one that fails" on any
given run.

`autoRoutine()` is called directly, unwrapped — same precedent as M4's tests calling
`setFlywheelTarget(...)` directly: the stub's own `UnsupportedOperationException` message
is already informative enough that a test-local wrapper adds nothing.

Create `projects/m5-auto-vision/src/test/java/frc/robot/AutoTerminationTest.java`:

```java
package frc.robot;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.harness.SimDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Given. Part of the milestone -- do not edit. */
class AutoTerminationTest {

  private static final int BUDGET_ITERATIONS = 1000; // 20s at Constants.SIM_STEP_SECONDS

  @BeforeEach
  void setUp() {
    SimDriver.setUp();
  }

  @AfterEach
  void tearDown() {
    SimDriver.tearDown();
  }

  @Test
  void autoRoutineTerminatesWithinABoundedBudget() {
    Command routine = SimDriver.container().autoRoutine();
    assertNotNull(routine, "autoRoutine() returned null. The harness needs a real command back.");

    boolean finished = SimDriver.runCommandToCompletion(routine, BUDGET_ITERATIONS);

    assertTrue(
        finished,
        "autoRoutine() never finished within "
            + (BUDGET_ITERATIONS * Constants.SIM_STEP_SECONDS)
            + " simulated seconds. Every step in your composition needs a bound: a path finishes "
            + "on its own once it reaches the end, but any other step -- a named command "
            + "standing in for an intake or a shot -- can hang forever unless you wrap it with "
            + ".withTimeout(...). A step that can hang forever hangs your whole autonomous "
            + "period on a real robot, not just this test.");
  }
}
```

- [ ] **Step 3: Write `FusedPoseTest`**

Aggregate over a whole ten-second run, not a single frame — the design record's table is
explicit about why this survives unseeded vision noise where a frame-exact assertion would
not.

Create `projects/m5-auto-vision/src/test/java/frc/robot/FusedPoseTest.java`:

```java
package frc.robot;

import static org.junit.jupiter.api.Assertions.assertTrue;

import frc.robot.harness.SimDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Given. Part of the milestone -- do not edit. */
class FusedPoseTest {

  private static final int ITERATIONS = 500; // 10s at Constants.SIM_STEP_SECONDS

  @BeforeEach
  void setUp() {
    SimDriver.setUp();
  }

  @AfterEach
  void tearDown() {
    SimDriver.tearDown();
  }

  @Test
  void fusingVisionReducesPoseErrorComparedToOdometryAlone() {
    double[] odometryOnlyError = SimDriver.runOdometryLoop(ITERATIONS, false);
    double[] fusedError = SimDriver.runOdometryLoop(ITERATIONS, true);

    double odometryOnlyMean = mean(odometryOnlyError);
    double fusedMean = mean(fusedError);

    assertTrue(
        fusedMean < odometryOnlyMean,
        "Over a "
            + (ITERATIONS * Constants.SIM_STEP_SECONDS)
            + "s run, mean pose error with vision fused ("
            + fusedMean
            + "m) was not lower than mean pose error from odometry alone ("
            + odometryOnlyMean
            + "m). The drivetrain's gyro carries a real drift your odometry-only estimate has "
            + "nothing to correct it with -- vision is supposed to be the correction. Check that "
            + "acceptsMeasurement() is actually accepting plausible measurements, and that "
            + "addVisionMeasurement() actually reaches your pose estimator.");
  }

  private static double mean(double[] values) {
    double sum = 0.0;
    for (double value : values) {
      sum += value;
    }
    return sum / values.length;
  }
}
```

- [ ] **Step 4: Write `BadTimestampTest`**

No camera in the loop at all — the synthetic pose is set to exactly the estimator's own
current estimate, so a filter that only checks "is this pose close to where I already think
I am" would wave it straight through. Only the timestamp (`-1.0`, which is never a valid
FPGA timestamp under any reasonable definition of "sane") is wrong. This is deliberately
the same trap the design record calls out: a distance-only filter can look correct on
`FusedPoseTest` while never having handled timestamps at all.

Create `projects/m5-auto-vision/src/test/java/frc/robot/BadTimestampTest.java`:

```java
package frc.robot;

import static org.junit.jupiter.api.Assertions.assertFalse;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import frc.robot.harness.SimDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Given. Part of the milestone -- do not edit. */
class BadTimestampTest {

  @BeforeEach
  void setUp() {
    SimDriver.setUp();
  }

  @AfterEach
  void tearDown() {
    SimDriver.tearDown();
  }

  @Test
  void rejectsAMeasurementWithAnImpossibleTimestamp() {
    Pose2d startingPose = new Pose2d(2.0, 4.0, Rotation2d.kZero);
    SimDriver.container().resetEstimator(startingPose);

    // Exactly where the estimator already believes it is -- a filter that only checks
    // distance from the current estimate would accept this. Only the timestamp is wrong:
    // -1.0 is never a valid FPGA timestamp.
    boolean accepted = SimDriver.container().acceptsMeasurement(startingPose, -1.0);

    assertFalse(
        accepted,
        "acceptsMeasurement() accepted a measurement at a pose identical to the current "
            + "estimate, timestamped -1.0 seconds -- a timestamp that can never be valid. A "
            + "filter that only checks how far a vision pose is from your current estimate will "
            + "accept this one, because the position looks perfect. Check the timestamp too.");
  }
}
```

- [ ] **Step 5: Compile, then run a CORRECT implementation against the suite**

Compile first:

```bash
export JAVA_HOME=~/wpilib/2026/jdk
cd /Users/y0shi/workspace/ignite/docs/.worktrees/m5/projects/m5-auto-vision
./gradlew compileTestJava --console=plain   # expect rc=0
./gradlew test --console=plain              # expect rc=1 -- every test RED, from
                                             # UnsupportedOperationException-derived failures
                                             # or a real assertion, not a compile error
```

Paste the actual failure summary. Then temporarily fill in `RobotContainer.java` with a
correct implementation. **This file is never committed** — confirm with `git status --short`
before every commit in this task.

- **Plants.** `private final SwerveDriveSim drivetrain = new SwerveDriveSim();` and
  `private final VisionSim vision = new VisionSim();`.
- **Pose estimator.** A `SwerveDrivePoseEstimator` field, constructed from
  `SwerveDriveSim.kinematics()`, `drivetrain.gyroAngle()`, `drivetrain.modulePositions()`,
  and `Pose2d.kZero` (or any starting pose — `resetEstimator` reseeds it before every given
  test that matters uses it). `updateOdometry()` calls
  `poseEstimator.update(drivetrain.gyroAngle(), drivetrain.modulePositions())`.
  `estimatedPose()` returns `poseEstimator.getEstimatedPosition()`. `resetEstimator(pose)`
  calls `poseEstimator.resetPosition(drivetrain.gyroAngle(), drivetrain.modulePositions(),
  pose)`.
- **Fusion.** `addVisionMeasurement(pose, timestamp)` calls
  `poseEstimator.addVisionMeasurement(pose, timestamp)` — a straight pass-through; WPILib's
  estimator does the latency-compensated fusion.
- **The filter.** `acceptsMeasurement(pose, timestamp)` needs at minimum a timestamp sanity
  check (reject non-positive or otherwise implausible timestamps — `BadTimestampTest`'s
  `-1.0` must fail whatever check you write) and a distance-from-current-estimate check
  (reject a candidate wildly far from `estimatedPose()` — this is the "distance-based
  filtering" the constraints delta names, and it is deliberately not backed by any given
  constant; pick a number in meters that rejects an obviously-wrong jump without rejecting
  normal correction, and be ready to defend it).
- **Auto routine.** `AutoBuilder.followPath(PathPlannerPath.fromPathFile("Example Path"))`
  composed with one or two placeholder named commands (e.g. `Commands.print("intake")`, or
  anything that finishes on its own quickly) via `Commands.sequence(...)`, each wrapped with
  a short `.withTimeout(...)` if it is not already guaranteed to finish. `autoChooser()`
  returns a `new SendableChooser<Command>()` with at least `autoRoutine()` added as an
  option.

Run the suite:

```bash
./gradlew test --console=plain
```

**Iterate until all three given tests are green — paste the actual `BUILD SUCCESSFUL`
output with the test count.** If `FusedPoseTest` does not go green with a filter that
accepts plausible measurements, check the distance threshold you picked is not so tight it
rejects everything, and check `acceptsMeasurement`'s timestamp check is not so strict it
rejects a real (if latent) camera frame — a timestamp a few hundred milliseconds old is
normal; `-1.0` is not.

Then delete every trace of the reference implementation:

```bash
git diff --stat -- src/main/java/frc/robot/RobotContainer.java   # confirm it changed
git checkout -- src/main/java/frc/robot/RobotContainer.java
git status --short   # confirm RobotContainer.java is no longer modified
```

- [ ] **Step 6: Run the DISTRACTORS — each must fail the test aimed at it**

For each row: apply the change to `RobotContainer.java` (starting from the correct
implementation you just deleted), run the named test, **read the actual failure message**,
then revert with `git checkout -- src/main/java/frc/robot/RobotContainer.java` before
moving to the next row.

| # | Distractor | Test it must fail | Why it fails |
|---|---|---|---|
| 1 | `autoRoutine()` composes an unbounded step with no `.withTimeout(...)` (e.g. `Commands.idle()` appended plain to the sequence) | `AutoTerminationTest` | The scheduler never reports the routine finished, so `runCommandToCompletion` exhausts its 1000-iteration budget and returns `false` |
| 2 | `acceptsMeasurement(...)` always returns `false` | `FusedPoseTest` | No vision measurement is ever fused regardless of quality, so the "fused" run is really a second odometry-only run — its mean error is not lower than the first, only approximately equal to it |
| 3 | `acceptsMeasurement(...)` checks only `visionPose.getTranslation().getDistance(estimatedPose().getTranslation()) < 1.0`, ignoring `timestampSeconds` entirely | `BadTimestampTest` | `BadTimestampTest`'s synthetic pose is exactly the current estimate — distance `0.0` — so this filter accepts it, and the timestamp of `-1.0` is never examined |

Paste each distractor's actual failing assertion message — the read-every-failure-message
discipline `CLAUDE.md` requires.

- [ ] **Step 7: Confirm `FusedPoseTest` FAILS for odometry-only — not optional**

The single easiest assertion in this milestone to get backwards, per `00-plan.md`'s
self-review: a test that passes whether or not vision is wired proves nothing. This step is
distractor #2 from Step 6, isolated and re-run on its own so the report shows this specific
check happened, not folded into a table row.

Starting from the stub, apply **only** the change: `acceptsMeasurement(...)` always returns
`false` (vision correctly computed and offered, but never trusted). Run:

```bash
export JAVA_HOME=~/wpilib/2026/jdk
cd /Users/y0shi/workspace/ignite/docs/.worktrees/m5/projects/m5-auto-vision
./gradlew test --tests "frc.robot.FusedPoseTest" --console=plain
```

**Paste the actual failure output — `fusedMean` must not be meaningfully lower than
`odometryOnlyMean`.** If this test passes with vision never fused, `FusedPoseTest` is
measuring noise, not fusion, and Task 4 is not done — the aggregate window, the drive
pattern, or the gyro drift rate needs to be revisited before this task can be committed.

Revert:

```bash
git checkout -- src/main/java/frc/robot/RobotContainer.java
git status --short
```

- [ ] **Step 8: The clean-clone invariant, from an actual fresh clone**

```bash
cd /Users/y0shi/workspace/ignite/docs/.worktrees/m5
git status --short    # must be clean -- confirms Steps 5-7 left no residue
rm -rf /tmp/m5-clean-clone && git worktree add --detach /tmp/m5-clean-clone HEAD
cd /tmp/m5-clean-clone/projects/m5-auto-vision
export JAVA_HOME=~/wpilib/2026/jdk
chmod +x gradlew
./gradlew assemble --console=plain          # expect rc=0 -- GREEN
./gradlew compileTestJava --console=plain   # expect rc=0 -- GREEN
./gradlew test --console=plain              # expect rc=1 -- RED, from assertions
cd /Users/y0shi/workspace/ignite/docs/.worktrees/m5
git worktree remove /tmp/m5-clean-clone
```

Paste all three command outputs in full. The red in the third command must come from
assertion failures inside the three given tests — never from a compile error, a missing
class, or an `UnsupportedOperationException` propagating past a `fail()` call uncaught. If
it does, that is a bug in `SimDriver`, not in the milestone.

- [ ] **Step 9: Confirm no filtering threshold leaked into the given tests**

```bash
cd /Users/y0shi/workspace/ignite/docs/.worktrees/m5/projects/m5-auto-vision
grep -rniE 'MAX_TRUSTED_DISTANCE|max.?trusted|trust.?distance' src/test/
```

Expected: no output.

- [ ] **Step 10: Format and commit**

```bash
cd /Users/y0shi/workspace/ignite/docs/.worktrees/m5/projects/m5-auto-vision
./gradlew spotlessApply --console=plain
./gradlew spotlessCheck --console=plain   # expect rc=0
./gradlew test --console=plain            # expect rc=1, unmodified stub, still RED from
                                           # assertions after formatting
cd /Users/y0shi/workspace/ignite/docs/.worktrees/m5
git status --short   # confirm RobotContainer.java shows NO diff -- Steps 5-7's edits must
                      # not have leaked back in
git add projects/m5-auto-vision/src/test
git commit -m "Add M5's given tests: auto termination, fused pose, bad timestamp

Verified against a correct reference implementation (deleted, never
committed) and three distractors -- an unbounded auto step, a filter that
rejects everything, and a filter that checks distance but ignores the
timestamp. FusedPoseTest confirmed to fail on its own when vision is
computed but never trusted (Step 7), so a passing run means fusion
actually happened. Clean-clone invariant confirmed from an actual git
worktree: assemble green, compileTestJava green, test red from
assertions."
```
