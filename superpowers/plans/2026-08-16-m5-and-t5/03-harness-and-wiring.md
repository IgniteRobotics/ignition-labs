# Task 3: `M5Deliverables`, `Robot`, `Constants`, and the `RobotContainer` stub

Everything a student needs and nothing they don't, at L1 density (harness given, control
logic theirs — match M3/M4, do not exceed it). This task wires the whole project together
without composing a single named command or fusing a single vision measurement, which are
the actual lessons.

**`updateOdometry()` is wheels-and-gyro only, by contract, and `Robot.java` is why that
separation has to hold.** `Robot.robotPeriodic()` below calls `updateOdometry()` every
tick, unconditionally, and separately reads whatever `VisionSim.latestResult()` last
produced and offers it to the fusion seam (`acceptsMeasurement`/`addVisionMeasurement`).
Task 4's `SimDriver.runOdometryLoop(iterations, feedVision)` reproduces exactly this
pairing by hand, with `feedVision` toggling only the second half — which only works as a
true odometry-only baseline if `updateOdometry()` is guaranteed never to touch vision
itself. That guarantee is the whole reason `FusedPoseTest` can mean anything.

**The camera-frame-to-candidate-pose conversion is given, not student-written, and it lives
in exactly one place: `frc.robot.harness.VisionPoseHelper` (pinned in `00-plan.md`), which
both `Robot.java` and Task 4's `SimDriver` call.** Both need to turn `VisionSim.latestResult()`
into a candidate `Pose2d` + timestamp before either can call
`acceptsMeasurement`/`addVisionMeasurement`. `src/test` compiles against `src/main`'s output,
so `SimDriver` depending on a `src/main` helper is the normal case, not an obstacle — an
earlier draft of this task wrongly assumed the two source sets could not depend on each other
and duplicated the conversion between them, which is exactly the drift `VisionPoseHelper` was
added to the pinned contract to prevent (see `00-plan.md`'s Phase A review note). Both callers
route through `VisionPoseHelper`, which wraps the same non-deprecated
`PhotonPoseEstimator.estimateLowestAmbiguityPose(PhotonPipelineResult)` — confirmed against
`photonlib-java-v2026.2.1-sources.jar` that `PhotonPoseEstimator.update(PhotonPipelineResult)`
is `@Deprecated(forRemoval = true, since = "2026")` in favor of exactly this method. What
the student writes is what happens to that candidate once it reaches `acceptsMeasurement`/
`addVisionMeasurement` — the filter, and the fusion into their own pose estimator.

**`ScoringMechanism` (pinned in `00-plan.md`, added during Phase A review) is also built in
this task, even though it lives under `plant/` and Task 2 is nominally that directory's task.**
Task 2 was authored and committed before the Phase A review added `ScoringMechanism` and
`VisionPoseHelper` to the contract; this lane owns the whole project sequentially, so both land
here rather than reopening Task 2. Without it, `AutoTerminationTest` (Task 4) degrades into "the
routine finished within a budget," which proves nothing — PathPlanner's follow command is
time-based and terminates on its own whether or not the robot went anywhere, so nothing in a
path-only routine can actually hang. `ScoringMechanism.setFaulted(true)` is the thing a given
test can force to fail, and a routine without a timeout on that step will hang on it — exactly
the lesson project step 4 ("add timeouts so no step can hang") teaches.

**Unlike M4's `Robot.java`, this one calls `CommandScheduler.getInstance().run()`.** M4 had
no command that needed to make progress on a real robot outside of a test harness that
drives the scheduler directly. M5's entire autonomous period is a scheduled `Command` — the
routine the student's `autoRoutine()` returns literally cannot execute on a real robot
without the scheduler being ticked every period. This is a genuine difference from M4's
precedent, not an oversight of it.

**Files:**
- Create: `projects/m5-auto-vision/src/main/java/frc/robot/plant/ScoringMechanism.java`
- Create: `projects/m5-auto-vision/src/main/java/frc/robot/harness/VisionPoseHelper.java`
- Create: `projects/m5-auto-vision/src/main/java/frc/robot/harness/M5Deliverables.java`
- Create: `projects/m5-auto-vision/src/main/java/frc/robot/Main.java`
- Create: `projects/m5-auto-vision/src/main/java/frc/robot/Robot.java`
- Create: `projects/m5-auto-vision/src/main/java/frc/robot/Constants.java`
- Create: `projects/m5-auto-vision/src/main/java/frc/robot/RobotContainer.java`
- Modify: `projects/m5-auto-vision/src/main/java/frc/robot/plant/VisionSim.java` — widen
  `ROBOT_TO_CAMERA` from `private` to `public` so `VisionPoseHelper` can build its
  `PhotonPoseEstimator` from the same mount transform `VisionSim` itself uses, rather than a
  second copy of the numbers.

**Consumes:** Task 2's `SwerveDriveSim` and `VisionSim`.
**Produces:** `frc.robot.plant.ScoringMechanism`, `frc.robot.harness.VisionPoseHelper`, and
`frc.robot.harness.M5Deliverables` (the seam Task 4's given tests drive), plus
`Constants.SIM_STEP_SECONDS` — every name exactly as pinned in `00-plan.md`.

**Lane:** `milestone`. Worktree `.worktrees/m5`.

---

- [ ] **Step 1: Write `ScoringMechanism`**

Given, locked, pinned in `00-plan.md`. A fixed number of ticks after `startDeploy()`,
`isDeployed()` becomes `true` — unless `setFaulted(true)` was called, in which case it never
does. This is the one step in the milestone that can actually be made to hang; PathPlanner's
follow command is time-based and always terminates on its own.

Create `projects/m5-auto-vision/src/main/java/frc/robot/plant/ScoringMechanism.java`:

```java
package frc.robot.plant;

/**
 * A scoring mechanism that becomes deployed a fixed number of ticks after {@link #startDeploy()}
 * -- unless {@link #setFaulted(boolean)} is {@code true}, in which case {@link #isDeployed()} never
 * becomes {@code true}.
 *
 * <p>This is the one step in the milestone {@code AutoTerminationTest} (Task 4) can force to fail.
 * PathPlanner's path-following command is time-based and terminates on its own whether or not the
 * robot went anywhere, so nothing in a path-only routine can actually hang. A named command that
 * starts this mechanism's deploy and waits on {@link #isDeployed()}, with no timeout, will hang when
 * this is faulted -- which is exactly the lesson project step 4 ("add timeouts so no step can hang")
 * teaches.
 */
public final class ScoringMechanism {

  private static final int DEPLOY_TICKS = 25;

  private boolean deploying = false;
  private boolean deployed = false;
  private boolean faulted = false;
  private int ticksSinceStart = 0;

  public ScoringMechanism() {}

  /** Starts (or restarts) the deploy sequence. */
  public void startDeploy() {
    deploying = true;
    deployed = false;
    ticksSinceStart = 0;
  }

  /**
   * @return {@code true} once {@link #step} has advanced enough ticks since {@link #startDeploy()}
   *     was called -- unless {@link #setFaulted} is {@code true}, in which case this never becomes
   *     {@code true}.
   */
  public boolean isDeployed() {
    return deployed;
  }

  /**
   * Advances the deploy sequence by one tick, unless faulted or not currently deploying.
   *
   * @param dtSeconds unused by the tick count; accepted for consistency with the other given
   *     plants' {@code step(dt)} signature
   */
  public void step(double dtSeconds) {
    if (!deploying || faulted) {
      return;
    }
    ticksSinceStart++;
    if (ticksSinceStart >= DEPLOY_TICKS) {
      deployed = true;
      deploying = false;
    }
  }

  /**
   * @param faulted when {@code true}, {@link #isDeployed()} can never become {@code true} while a
   *     deploy is in progress -- simulating a mechanism that physically cannot finish. Does not
   *     un-deploy a mechanism that already finished before the fault was set.
   */
  public void setFaulted(boolean faulted) {
    this.faulted = faulted;
  }

  /**
   * Resets to the not-deploying, not-deployed state. Does not clear a fault -- a fault models a
   * hardware condition that a software reset does not repair on a real robot either.
   */
  public void reset() {
    deploying = false;
    deployed = false;
    ticksSinceStart = 0;
  }
}
```

- [ ] **Step 2: Write `VisionPoseHelper`**

Given, locked, pinned in `00-plan.md`. The single place the camera-frame-to-candidate-pose
conversion lives — both `Robot.java` (Step 5, below) and Task 4's `SimDriver` call it, so a
test-source copy and a main-source copy of the same conversion can never drift apart. Wraps
`PhotonPoseEstimator.estimateLowestAmbiguityPose(PhotonPipelineResult)` — the non-deprecated
replacement for `PhotonPoseEstimator.update(PhotonPipelineResult)`, confirmed against
`photonlib-java-v2026.2.1-sources.jar`.

Built from `VisionSim.fieldLayout()` and `VisionSim.ROBOT_TO_CAMERA` — widen that field from
`private` to `public` in `projects/m5-auto-vision/src/main/java/frc/robot/plant/VisionSim.java`
(this class lives in `frc.robot.harness`, a different package, so package-private visibility is
not enough) so this class uses the exact same mount transform `VisionSim` itself simulates
against, rather than a second, driftable copy of the same numbers:

```java
  public static final Transform3d ROBOT_TO_CAMERA =
      new Transform3d(
          new Translation3d(0.28, 0.0, 0.22), new Rotation3d(0.0, Math.toRadians(-15.0), 0.0));
```

(Only the modifier changes — `private static final` becomes `public static final`; the value and
every other line of `VisionSim.java` from Task 2 is untouched.)

Create `projects/m5-auto-vision/src/main/java/frc/robot/harness/VisionPoseHelper.java`:

```java
package frc.robot.harness;

import edu.wpi.first.math.geometry.Pose2d;
import frc.robot.plant.VisionSim;
import java.util.Optional;
import org.photonvision.PhotonPoseEstimator;
import org.photonvision.targeting.PhotonPipelineResult;

/**
 * Converts a raw {@link PhotonPipelineResult} into a candidate field pose. Given, not student work
 * -- but it lives in exactly one place in {@code src/main} that both {@code Robot} and the given
 * test harness (Task 4's {@code SimDriver}) call. A test-source copy and a main-source copy of the
 * same conversion would drift, and the day they disagree the given test measures something the
 * robot does not do.
 *
 * <p>Wraps {@link PhotonPoseEstimator#estimateLowestAmbiguityPose(PhotonPipelineResult)} --
 * <b>not</b> {@code PhotonPoseEstimator.update(PhotonPipelineResult)}, which is {@code
 * Deprecated(forRemoval = true, since = "2026")} in the pinned {@code v2026.2.1}.
 */
public final class VisionPoseHelper {

  private static final PhotonPoseEstimator ESTIMATOR =
      new PhotonPoseEstimator(VisionSim.fieldLayout(), VisionSim.ROBOT_TO_CAMERA);

  private VisionPoseHelper() {}

  /**
   * @param result the camera's latest frame
   * @return the lowest-ambiguity candidate field pose; empty when the frame has no usable target
   */
  public static Optional<Pose2d> candidatePose(PhotonPipelineResult result) {
    return ESTIMATOR.estimateLowestAmbiguityPose(result).map(est -> est.estimatedPose.toPose2d());
  }

  /**
   * @param result the camera's latest frame
   * @return when the frame that produced {@code result} was actually captured -- not when this
   *     method is called
   */
  public static double timestampSeconds(PhotonPipelineResult result) {
    return result.getTimestampSeconds();
  }
}
```

- [ ] **Step 3: Write `M5Deliverables`**

The seam. Given tests cannot name `RobotContainer`'s own pose-estimator field or filter
logic because the student hasn't written it yet, so they reach it through these nine
methods instead — same reason M3 has `M3Deliverables` and M4 has `M4Deliverables`.

Create `projects/m5-auto-vision/src/main/java/frc/robot/harness/M5Deliverables.java`:

```java
package frc.robot.harness;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.plant.ScoringMechanism;
import frc.robot.plant.SwerveDriveSim;
import frc.robot.plant.VisionSim;

/**
 * Exposes your M5 deliverables to the test harness and to {@code Robot}. {@code RobotContainer}
 * implements it.
 *
 * <p>The given tests cannot name your pose estimator or your filter logic — they don't know what
 * you called them — so they reach your work through these eight methods instead. {@code Robot}
 * uses the same seam: {@code robotPeriodic()} calls {@link #updateOdometry()} every 20ms, then
 * offers whatever the camera last produced to {@link #acceptsMeasurement} and, if accepted, {@link
 * #addVisionMeasurement}.
 *
 * <p><b>{@link #autoRoutine()} must return a new instance each time it is called</b>, the same rule
 * every milestone's command-returning deliverable has carried since M3: a command object that has
 * already run once cannot be scheduled again.
 *
 * <p><b>{@link #acceptsMeasurement} is the single most important method here.</b> It is the only
 * seam a given test drives with a synthetic measurement and no camera at all (`BadTimestampTest`) —
 * per the design record, it is "the only DoD item in M5 a student can otherwise fake by getting a
 * good result for the wrong reason."
 */
public interface M5Deliverables {

  /**
   * @return a NEW command composing your paths, named commands, and timeouts into one autonomous
   *     routine. A stored, already-run instance cannot be rescheduled — build it inside this
   *     method.
   */
  Command autoRoutine();

  /**
   * @return the chooser you built and populated yourself, ready to put on the dashboard
   */
  SendableChooser<Command> autoChooser();

  /**
   * Runs exactly one 20ms iteration of your pose estimator's odometry update: read {@link
   * #drivetrain()}'s {@link SwerveDriveSim#gyroAngle()} and {@link
   * SwerveDriveSim#modulePositions()}, and update your estimator from them. <b>Wheels and gyro
   * only</b> — never touch vision in here. {@code Robot.robotPeriodic()} calls this every tick
   * unconditionally; the given tests rely on it never fusing a vision measurement on its own so
   * that an odometry-only run means what it says.
   */
  void updateOdometry();

  /**
   * Fuses a vision-derived pose into your estimator, at the timestamp it was actually captured.
   * Only ever called after {@link #acceptsMeasurement} returned {@code true} for the same
   * arguments — this method does not re-check that itself.
   *
   * @param visionPose the candidate pose a vision frame produced
   * @param timestampSeconds when the frame that produced it was captured, not when this method is
   *     called
   */
  void addVisionMeasurement(Pose2d visionPose, double timestampSeconds);

  /**
   * Your filter. Decide whether {@code visionPose}/{@code timestampSeconds} is trustworthy enough
   * to fuse — {@link #estimatedPose()} is available to compare against. There is no {@code
   * MAX_TRUSTED_DISTANCE_METERS} constant anywhere in this project; choosing and defending that
   * number is the milestone.
   *
   * @param visionPose the candidate pose a vision frame produced
   * @param timestampSeconds when the frame that produced it was captured
   * @return whether {@link #addVisionMeasurement} should be called with these same arguments
   */
  boolean acceptsMeasurement(Pose2d visionPose, double timestampSeconds);

  /**
   * @return your pose estimator's current best estimate
   */
  Pose2d estimatedPose();

  /**
   * Seeds your pose estimator at a known pose — call this whenever you know for certain where the
   * robot is, e.g. at the start of autonomous.
   *
   * @param pose the known pose
   */
  void resetEstimator(Pose2d pose);

  /**
   * @return your drivetrain. The same instance every call.
   */
  SwerveDriveSim drivetrain();

  /**
   * @return your vision sim. The same instance every call.
   */
  VisionSim vision();

  /**
   * @return your scoring mechanism. The same instance every call. Your auto routine's deploy named
   *     command starts it with {@link ScoringMechanism#startDeploy()} and waits on {@link
   *     ScoringMechanism#isDeployed()}.
   */
  ScoringMechanism mechanism();
}
```

- [ ] **Step 4: Write `Main.java`**

Byte-for-byte every prior milestone's — every WPILib project has one identical to this.

Create `projects/m5-auto-vision/src/main/java/frc/robot/Main.java`:

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

Given. Three things happen every 20ms: the scheduler runs (so `autoRoutine()` and any named
command actually progress); `updateOdometry()` runs, unconditionally, for the
wheels-and-gyro half of the estimate; and whatever the camera last produced gets offered to
the fusion seam through `VisionPoseHelper`, gated by the student's own filter.
`simulationPeriodic()` — only while simulating — steps the drivetrain, then updates vision
from its true pose, then steps the scoring mechanism, in that order — the drivetrain/vision
pairing is exactly what `VisionSim`'s Javadoc requires and exactly what Task 4's `SimDriver`
reproduces by hand.

Create `projects/m5-auto-vision/src/main/java/frc/robot/Robot.java`:

```java
package frc.robot;

import edu.wpi.first.epilogue.Epilogue;
import edu.wpi.first.epilogue.Logged;
import edu.wpi.first.epilogue.NotLogged;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.harness.VisionPoseHelper;
import java.util.Optional;
import org.photonvision.targeting.PhotonPipelineResult;

/**
 * Given. You should not need to change anything in this file to finish M5.
 *
 * <p>{@link #robotPeriodic()} runs the {@code CommandScheduler} first -- your auto routine and any
 * named command only progress if this runs every tick -- then calls {@link
 * frc.robot.harness.M5Deliverables#updateOdometry()} for the wheels-and-gyro half of your pose
 * estimate, then reads whatever the camera last produced and offers it to your fusion seam through
 * {@link VisionPoseHelper}: {@link frc.robot.harness.M5Deliverables#acceptsMeasurement} decides
 * whether it is trusted, {@link frc.robot.harness.M5Deliverables#addVisionMeasurement} is where it
 * lands if so. {@link VisionPoseHelper} is given, not something you write -- it always resolves to
 * the single lowest-ambiguity tag in view. What you write is what happens to that candidate once it
 * reaches your seam.
 *
 * <p>{@link #simulationPeriodic()} -- only while simulating -- advances the drivetrain's physics,
 * updates the vision sim from the drivetrain's true pose, and steps the scoring mechanism, in that
 * order. Given tests reproduce the drivetrain/vision pairing by hand, without constructing a {@code
 * Robot} at all.
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
    CommandScheduler.getInstance().run();
    robotContainer.updateOdometry();

    PhotonPipelineResult result = robotContainer.vision().latestResult();
    Optional<Pose2d> candidate = VisionPoseHelper.candidatePose(result);
    if (candidate.isPresent()) {
      double timestamp = VisionPoseHelper.timestampSeconds(result);
      if (robotContainer.acceptsMeasurement(candidate.get(), timestamp)) {
        robotContainer.addVisionMeasurement(candidate.get(), timestamp);
      }
    }
  }

  @Override
  public void simulationPeriodic() {
    robotContainer.drivetrain().step(Constants.SIM_STEP_SECONDS);
    robotContainer.vision().update(robotContainer.drivetrain().truePose());
    robotContainer.mechanism().step(Constants.SIM_STEP_SECONDS);
  }
}
```

- [ ] **Step 6: Write `Constants.java`**

Exactly the one pinned constant, plus a documented, deliberately empty `VisionConstants` —
present so a student who goes looking for a distance threshold finds a note explaining why
there isn't one, not silence. **No filtering threshold, and no gain of any kind, goes in
this file.**

Create `projects/m5-auto-vision/src/main/java/frc/robot/Constants.java`:

```java
package frc.robot;

/**
 * Numbers that describe the robot, in one place with names on them.
 *
 * <p>No filtering threshold lives here either — see {@link VisionConstants}.
 */
public final class Constants {
  private Constants() {}

  /**
   * Seconds per simulated physics step. Given tests and {@code Robot.simulationPeriodic()} both use
   * this.
   */
  public static final double SIM_STEP_SECONDS = 0.020;

  public static final class VisionConstants {
    private VisionConstants() {}

    // No MAX_TRUSTED_DISTANCE_METERS here. Choosing how far you still trust a vision pose --
    // and defending that number -- is the milestone. Put your own threshold in
    // RobotContainer, as your own field.
  }
}
```

- [ ] **Step 7: Write the `RobotContainer` stub**

Student territory. Every method a throwing stub, same as every prior milestone's
`RobotContainer`. Four TODO regions instead of M4's three: plants (now including the given
`ScoringMechanism`, not just the two Task 2 plants), the pose estimator's odometry half, the
fusion/filter half, and the auto routine/chooser.

Create `projects/m5-auto-vision/src/main/java/frc/robot/RobotContainer.java`:

```java
package frc.robot;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.harness.M5Deliverables;
import frc.robot.plant.ScoringMechanism;
import frc.robot.plant.SwerveDriveSim;
import frc.robot.plant.VisionSim;

/**
 * Where M5 is really built. Everything else in this project is given.
 *
 * <p>Work through README.md in order: Part 1 is the auto routine, Part 2 is vision fusion. The
 * four TODO regions below are the whole job, and the nine methods at the bottom are how the tests
 * and {@code Robot} reach what you built — implement each to do or return the thing its Javadoc in
 * {@link M5Deliverables} names.
 */
public class RobotContainer implements M5Deliverables {

  // ===================== TODO 1 — construct your plants =======================
  // One SwerveDriveSim, one VisionSim, and one ScoringMechanism, private final
  // fields. All three classes are given -- see plant/. README.md Part 1 step 1.
  // ==============================================================================

  // ===================== TODO 2 — pose estimator, odometry half ================
  // A SwerveDrivePoseEstimator field, seeded in your constructor from
  // drivetrain().gyroAngle() and drivetrain().modulePositions(). updateOdometry(),
  // estimatedPose(), and resetEstimator() are built on it. README.md Part 2 step 1.
  // ==============================================================================

  // ===================== TODO 3 — vision fusion and the filter =================
  // acceptsMeasurement() is your filter -- estimatedPose() is available to compare
  // a candidate against. addVisionMeasurement() feeds an accepted candidate, and
  // its exact timestamp, into the same pose estimator. README.md Part 2 steps 2-3.
  // ==============================================================================

  // ===================== TODO 4 — the auto routine and chooser =================
  // Paths (AutoBuilder.followPath), named commands, timeouts, composed with
  // Commands.sequence(...) or similar. A SendableChooser<Command> you build and
  // populate yourself. README.md Part 1 steps 2-4.
  // ==============================================================================

  public RobotContainer() {}

  @Override
  public Command autoRoutine() {
    throw new UnsupportedOperationException(
        "TODO(M5): return a NEW command composing your paths, named commands, and timeouts");
  }

  @Override
  public SendableChooser<Command> autoChooser() {
    throw new UnsupportedOperationException(
        "TODO(M5): build and populate your own SendableChooser<Command>");
  }

  @Override
  public void updateOdometry() {
    throw new UnsupportedOperationException(
        "TODO(M5): one 20ms wheels-and-gyro-only update of your pose estimator");
  }

  @Override
  public void addVisionMeasurement(Pose2d visionPose, double timestampSeconds) {
    throw new UnsupportedOperationException(
        "TODO(M5): fuse an accepted vision pose into your pose estimator, at its own timestamp");
  }

  @Override
  public boolean acceptsMeasurement(Pose2d visionPose, double timestampSeconds) {
    throw new UnsupportedOperationException(
        "TODO(M5): decide whether to trust this vision pose enough to fuse it");
  }

  @Override
  public Pose2d estimatedPose() {
    throw new UnsupportedOperationException("TODO(M5): return your pose estimator's estimate");
  }

  @Override
  public void resetEstimator(Pose2d pose) {
    throw new UnsupportedOperationException("TODO(M5): seed your pose estimator at this pose");
  }

  @Override
  public SwerveDriveSim drivetrain() {
    throw new UnsupportedOperationException("TODO(M5): return your SwerveDriveSim");
  }

  @Override
  public VisionSim vision() {
    throw new UnsupportedOperationException("TODO(M5): return your VisionSim");
  }

  @Override
  public ScoringMechanism mechanism() {
    throw new UnsupportedOperationException("TODO(M5): return your ScoringMechanism");
  }
}
```

- [ ] **Step 8: Compile**

```bash
export JAVA_HOME=~/wpilib/2026/jdk
cd /Users/y0shi/workspace/ignite/docs/.worktrees/m5/projects/m5-auto-vision
./gradlew compileJava --console=plain    # expect rc=0
```

The unmodified stub must compile clean — this is half of the clean-clone invariant, and
Task 4 proves the other half (`test` red from assertions, not from a compile error).

- [ ] **Step 9: Confirm no given file hands over an answer or a filtering threshold**

```bash
cd /Users/y0shi/workspace/ignite/docs/.worktrees/m5/projects/m5-auto-vision
grep -rniE 'you should assert|the answer is|expected value is|hint:' src/main/
grep -rniE 'MAX_TRUSTED_DISTANCE|max.?trusted|trust.?distance' src/main/
```

Expected: no output from either. `TODO(M5)` strings in `RobotContainer.java` are expected
and are not a hit for the first grep — they name what to do, not the answer.

- [ ] **Step 10: Format and commit**

```bash
cd /Users/y0shi/workspace/ignite/docs/.worktrees/m5/projects/m5-auto-vision
./gradlew spotlessApply --console=plain
./gradlew spotlessCheck --console=plain   # expect rc=0
./gradlew compileJava --console=plain     # expect rc=0
cd /Users/y0shi/workspace/ignite/docs/.worktrees/m5
git status --short
git add projects/m5-auto-vision/src/main/java/frc/robot
git commit -m "Wire M5: harness, Robot, Constants, and the RobotContainer stub

Also adds ScoringMechanism (plant/) and VisionPoseHelper (harness/), both
pinned in 00-plan.md's contract during the Phase A review after Task 2 was
already committed -- this lane owns the whole project sequentially, so
both land here rather than reopening Task 2.

M5Deliverables is the seam both the given tests and Robot.java reach the
student's auto routine and pose fusion through. Robot.robotPeriodic() runs
the CommandScheduler (unlike M4 -- this milestone's auto routine needs it
to progress at all), then updateOdometry() unconditionally, then offers
the camera's latest frame -- converted through VisionPoseHelper, the one
place the given, non-deprecated
PhotonPoseEstimator.estimateLowestAmbiguityPose() call lives -- to
acceptsMeasurement()/addVisionMeasurement(). No filtering threshold
anywhere: Constants.VisionConstants is deliberately empty."
```
