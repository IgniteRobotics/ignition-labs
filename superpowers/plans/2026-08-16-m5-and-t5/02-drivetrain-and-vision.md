# Task 2: `SwerveDriveSim` and `VisionSim`

The measurement surface for the whole milestone, and the reason M5 exists as a spike-first
design rather than a documentation-first one, same as M4. **Read
[`superpowers/specs/2026-08-16-m5-auto-vision-design.md`](../../specs/2026-08-16-m5-auto-vision-design.md)
before writing a line here** — §1's spikes are why `VisionSim` can wrap the real
`VisionSystemSim` instead of a stand-in (spike G: PhotonVision's sim honours WPILib's
simulated clock, unlike Phoenix's), and why every given test that touches it must call
`SimHooks.pauseTiming()` (spikes B/E: `PhotonCameraSim` is FPS-limited against wall-clock,
and a tight free-running loop reads one frozen frame forever).

**Two given, locked plants, and one of them owns `AutoBuilder.configure(...)`.** Spec §9.6
says `AutoBuilder` ships "already configured" — that has to happen in given code, not in
`RobotContainer` (which is entirely student stub in Task 3), and `SwerveDriveSim` is the
only given class that has its own pose/reset/speeds/drive methods to wire it with. It
configures itself against its **own** ground truth (`truePose()`/`resetTo()`), not against
whatever pose estimate the student eventually builds — `SwerveDriveSim` is constructed and
determinism-tested in this task, standalone, before `RobotContainer` exists at all, so it
cannot depend on a type that doesn't exist yet. This also keeps `AutoTerminationTest`
(Task 4) meaningful independent of whether Part 2's fusion code is even written: composing
an auto routine and fusing vision are separately gradable, the same way M4's flywheel and
arm parts were.

**Gyro drift is a fixed rate, not noise.** `gyroAngle()` is where the milestone's odometry
error comes from — real swerve gyros drift, and `FusedPoseTest` (Task 4) needs an
odometry-only run to measurably diverge from truth over ten seconds so a vision-corrected
run has something real to beat. A **fixed-rate** bias, rather than sampled noise, is what
keeps `SwerveDriveSim` itself bit-identical across runs (Step 6 proves this); the
noise-bearing half of the milestone's nondeterminism lives entirely in `VisionSim`, which
is exactly where spike B/E/G already characterized it.

**Files:**
- Create: `projects/m5-auto-vision/src/main/java/frc/robot/plant/SwerveDriveSim.java`
- Create: `projects/m5-auto-vision/src/main/java/frc/robot/plant/VisionSim.java`
- Create (temporary, deleted in Step 6): `projects/m5-auto-vision/src/test/java/frc/robot/plant/DeterminismSpikeTest.java`

**Consumes:** Task 1's buildable scaffold, including
`src/main/deploy/pathplanner/settings.json`.
**Produces:** `frc.robot.plant.SwerveDriveSim` and `frc.robot.plant.VisionSim`, exactly as
pinned in `00-plan.md`'s API contract — every task after this one names them and takes the
signatures verbatim.

**Lane:** `milestone`. Worktree `.worktrees/m5`.

---

- [ ] **Step 1: Write `SwerveDriveSim`**

Module geometry is `±0.3m` in both axes (0.6m track width, 0.6m wheelbase) — this is the
exact number Task 1's `settings.json` was sized to match (`flModuleX`/`flModuleY`/etc.).
If this ever changes, that file has to change with it.

`step(dt)` does three things, in order: integrates `truePose` from the last commanded
`ChassisSpeeds` via `Pose2d.exp(Twist2d)` (the same Lie-group integration WPILib's own
odometry classes use — a curved-arc step, not a linear one); advances each module's
odometer by `speedMetersPerSecond * dt` from `SwerveDriveKinematics.toSwerveModuleStates`;
and accumulates the gyro's fixed-rate drift. `gyroAngle()` reads `truePose`'s rotation
*plus* that drift — `truePose()` itself never sees it, which is exactly what lets given
tests use `truePose()` as ground truth while a student's odometry, built from
`gyroAngle()`, drifts.

`AutoBuilder.configure(...)` runs once, in the constructor, wired to `this::truePose`,
`this::resetTo`, `this::currentSpeeds`, and `this::drive` — the four callbacks a holonomic
`AutoBuilder` needs, all satisfied by this same class. Zero `Subsystem` requirements are
passed (the parameter is varargs); `SwerveDriveSim` is a plant, not a `Subsystem`, matching
`FlywheelPlant`/`ArmPlant`'s M4 precedent. `RobotConfig.fromGUISettings()` throwing is not
a condition a real run should ever encounter — Task 1 ships a valid `settings.json` — so a
failure here is wrapped and rethrown loudly rather than swallowed; a swallowed exception
would leave `AutoBuilder` silently unconfigured and turn every later `AutoBuilder.followPath`
call into a confusing, seemingly-unrelated failure far from its actual cause.

Create `projects/m5-auto-vision/src/main/java/frc/robot/plant/SwerveDriveSim.java`:

```java
package frc.robot.plant;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.config.PIDConstants;
import com.pathplanner.lib.config.RobotConfig;
import com.pathplanner.lib.controllers.PPHolonomicDriveController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.geometry.Twist2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import java.io.IOException;
import org.json.simple.parser.ParseException;

/**
 * A deterministic kinematic swerve drivetrain: it integrates whatever {@link ChassisSpeeds} {@link
 * #drive} was last called with, perfectly, every {@link #step}. No motors, no Phoenix, no motor lag
 * -- building a real swerve drivetrain is not this milestone's lesson, composing autos and fusing
 * vision is.
 *
 * <p><b>{@link #gyroAngle()} carries a fixed-rate drift that {@link #truePose()} never sees.</b> A
 * real gyro drifts over a match; this plant models that as a constant bias rather than sampled
 * noise, specifically so the drift -- and therefore the gap between an odometry-only pose estimate
 * and the truth -- is bit-identical across runs. {@link #truePose()} is ground truth; given tests
 * use it and you should not build your own pose estimate from it.
 *
 * <p><b>{@code AutoBuilder} is already configured, against this plant's own ground truth,</b> not
 * against any pose estimate you build in {@code RobotContainer} -- this class is constructed and
 * tested standalone, before your code exists. {@link com.pathplanner.lib.auto.AutoBuilder#followPath}
 * and {@code AutoBuilder.buildAutoChooser()} are ready to call the moment this constructor returns.
 */
public final class SwerveDriveSim {

  private static final double TRACK_WIDTH_METERS = 0.6;
  private static final double WHEEL_BASE_METERS = 0.6;

  // A real gyro's drift is not zero-mean over a match; modeling it as a fixed rate rather
  // than sampled noise is what keeps this whole plant bit-identical across runs (Task 2
  // Step 6 proves it) while still giving odometry-only pose estimation something real to
  // fail at correcting.
  private static final double GYRO_DRIFT_RADIANS_PER_SECOND = 0.010;

  private static final Translation2d FRONT_LEFT =
      new Translation2d(WHEEL_BASE_METERS / 2.0, TRACK_WIDTH_METERS / 2.0);
  private static final Translation2d FRONT_RIGHT =
      new Translation2d(WHEEL_BASE_METERS / 2.0, -TRACK_WIDTH_METERS / 2.0);
  private static final Translation2d BACK_LEFT =
      new Translation2d(-WHEEL_BASE_METERS / 2.0, TRACK_WIDTH_METERS / 2.0);
  private static final Translation2d BACK_RIGHT =
      new Translation2d(-WHEEL_BASE_METERS / 2.0, -TRACK_WIDTH_METERS / 2.0);

  private static final SwerveDriveKinematics KINEMATICS =
      new SwerveDriveKinematics(FRONT_LEFT, FRONT_RIGHT, BACK_LEFT, BACK_RIGHT);

  private final double[] moduleDistanceMeters = new double[4];
  private final Rotation2d[] moduleAngles = {
    Rotation2d.kZero, Rotation2d.kZero, Rotation2d.kZero, Rotation2d.kZero
  };

  private Pose2d truePose = Pose2d.kZero;
  private ChassisSpeeds commandedSpeeds = new ChassisSpeeds();
  private double gyroDriftRadians = 0.0;

  public SwerveDriveSim() {
    try {
      RobotConfig config = RobotConfig.fromGUISettings();
      AutoBuilder.configure(
          this::truePose,
          this::resetTo,
          this::currentSpeeds,
          this::drive,
          new PPHolonomicDriveController(new PIDConstants(5.0, 0.0), new PIDConstants(3.0, 0.0)),
          config,
          () -> DriverStation.getAlliance().orElse(Alliance.Blue) == Alliance.Red);
    } catch (IOException | ParseException e) {
      throw new IllegalStateException(
          "Could not load src/main/deploy/pathplanner/settings.json -- it ships with the "
              + "milestone and should never be missing or malformed. If you see this on a "
              + "fresh clone, show a mentor.",
          e);
    }
  }

  /**
   * Commands robot-relative chassis speeds. Takes effect on the next {@link #step}.
   *
   * @param speeds robot-relative
   */
  public void drive(ChassisSpeeds speeds) {
    commandedSpeeds = speeds;
  }

  /**
   * Advances the true pose and every module's odometer by one timestep, from whatever {@link
   * #drive} was last called with. {@code Robot.simulationPeriodic()} calls this once per 20ms tick;
   * given tests call it directly in a stepped loop.
   *
   * @param dtSeconds timestep, seconds
   */
  public void step(double dtSeconds) {
    truePose =
        truePose.exp(
            new Twist2d(
                commandedSpeeds.vxMetersPerSecond * dtSeconds,
                commandedSpeeds.vyMetersPerSecond * dtSeconds,
                commandedSpeeds.omegaRadiansPerSecond * dtSeconds));

    SwerveModuleState[] states = KINEMATICS.toSwerveModuleStates(commandedSpeeds);
    for (int i = 0; i < states.length; i++) {
      moduleDistanceMeters[i] += states[i].speedMetersPerSecond * dtSeconds;
      moduleAngles[i] = states[i].angle;
    }

    gyroDriftRadians += GYRO_DRIFT_RADIANS_PER_SECOND * dtSeconds;
  }

  /**
   * @return ground truth. Given tests use this to score your pose estimate; your own
   *     {@code RobotContainer} should not read it.
   */
  public Pose2d truePose() {
    return truePose;
  }

  /**
   * @return the simulated gyro heading -- {@link #truePose()}'s rotation plus this plant's
   *     fixed-rate drift. This is what a real gyro reading looks like; build your odometry from it,
   *     not from {@link #truePose()}.
   */
  public Rotation2d gyroAngle() {
    return truePose.getRotation().plus(Rotation2d.fromRadians(gyroDriftRadians));
  }

  /**
   * @return each module's position, front-left/front-right/back-left/back-right, matching {@link
   *     #kinematics()}'s construction order
   */
  public SwerveModulePosition[] modulePositions() {
    SwerveModulePosition[] positions = new SwerveModulePosition[4];
    for (int i = 0; i < 4; i++) {
      positions[i] = new SwerveModulePosition(moduleDistanceMeters[i], moduleAngles[i]);
    }
    return positions;
  }

  /**
   * @return the robot-relative speeds last commanded through {@link #drive}
   */
  public ChassisSpeeds currentSpeeds() {
    return commandedSpeeds;
  }

  /**
   * @return the kinematics every module position/state above is defined against: front-left,
   *     front-right, back-left, back-right, each ±0.3m from center
   */
  public static SwerveDriveKinematics kinematics() {
    return KINEMATICS;
  }

  /**
   * Teleports the true pose. Does not touch module odometers or the gyro's accumulated drift --
   * those are hardware state that a pose reset does not rezero on a real robot either.
   *
   * @param pose the new true pose
   */
  public void resetTo(Pose2d pose) {
    truePose = pose;
  }
}
```

- [ ] **Step 2: Write `VisionSim`**

Wraps PhotonVision's real `VisionSystemSim` — spike A confirmed it produces results
headless (100/100 frames with targets, no coprocessor, no NT server needed beyond the
in-process instance HAL initialization already brings up), and spike G confirmed it reads
WPILib's simulated clock rather than wall-clock, which is the entire reason this milestone
can use the vendor sim directly instead of a stand-in the way M4 had to.

`PhotonCameraSim`'s `prop` field is `public final` and stored by reference, not copied — so
`setNoise`/`setLatency` below mutate the exact `SimCameraProperties` object the camera sim
already holds, and take effect on the next frame it produces. Confirmed against
`photonlib-java-v2026.2.1-sources.jar`.

Field layout is `AprilTagFields.k2026RebuiltWelded` via `AprilTagFieldLayout.loadField(...)`
— matching `Robot-2026/src/main/java/frc/robot/Constants.java:38` exactly. The design
record's spike used the 2025 field because that is what it happened to load; this is not
that.

Create `projects/m5-auto-vision/src/main/java/frc/robot/plant/VisionSim.java`:

```java
package frc.robot.plant;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.apriltag.AprilTagFields;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation3d;
import org.photonvision.PhotonCamera;
import org.photonvision.simulation.PhotonCameraSim;
import org.photonvision.simulation.SimCameraProperties;
import org.photonvision.simulation.VisionSystemSim;
import org.photonvision.targeting.PhotonPipelineResult;

/**
 * Wraps PhotonVision's real {@link VisionSystemSim} -- not a stand-in. Spike G (design record §1)
 * found that PhotonVision's simulation honours WPILib's simulated clock, unlike CTRE Phoenix's,
 * which is why this milestone can hand you the genuine vendor simulation instead of a substitute
 * the way M4 had to.
 *
 * <p><b>{@link #update} must be called AFTER {@link SwerveDriveSim#step}, every tick, or not at
 * all.</b> Passing a stale pose produces a stale frame.
 *
 * <p><b>Every given test that reads {@link #latestResult()} calls {@code SimHooks.pauseTiming()}
 * first.</b> {@link PhotonCameraSim} is FPS-limited against the same simulated clock {@link #update}
 * advances -- spike B/E found that in a free-running loop it returns one cached frame while the
 * robot drives away from it, because almost no simulated time passes between iterations without
 * {@code pauseTiming()} plus an explicit {@code SimHooks.stepTiming(...)} per tick.
 *
 * <p><b>Noise and latency are unseeded and configurable</b> -- {@link #setNoise} and {@link
 * #setLatency} let you make the vision worse on purpose. No given test may assert a frame-exact
 * vision value because of this; the three that exist are aggregate-over-a-run or bypass the camera
 * entirely (design record §2).
 */
public final class VisionSim {

  private static final String CAMERA_NAME = "M5_SimCamera";

  private static final Transform3d ROBOT_TO_CAMERA =
      new Transform3d(
          new Translation3d(0.28, 0.0, 0.22), new Rotation3d(0.0, Math.toRadians(-15.0), 0.0));

  private final VisionSystemSim visionSim = new VisionSystemSim("m5");
  private final PhotonCamera camera = new PhotonCamera(CAMERA_NAME);
  private final SimCameraProperties cameraProperties = new SimCameraProperties();
  private final PhotonCameraSim cameraSim;

  public VisionSim() {
    cameraProperties.setCalibration(960, 720, Rotation2d.fromDegrees(90.0));
    cameraProperties.setFPS(20.0);
    cameraProperties.setAvgLatencyMs(20.0);
    cameraProperties.setLatencyStdDevMs(5.0);
    cameraProperties.setCalibError(0.20, 0.05);

    cameraSim = new PhotonCameraSim(camera, cameraProperties);
    visionSim.addAprilTags(fieldLayout());
    visionSim.addCamera(cameraSim, ROBOT_TO_CAMERA);
  }

  /**
   * Advances the simulated camera from the robot's true pose. Call this once per tick, immediately
   * after {@link SwerveDriveSim#step} -- see the class Javadoc.
   *
   * @param trueRobotPose ground truth, e.g. {@link SwerveDriveSim#truePose()}
   */
  public void update(Pose2d trueRobotPose) {
    visionSim.update(trueRobotPose);
  }

  /**
   * @return the most recent frame the simulated camera published. May have no targets, and may be
   *     the same frame as last call -- see the class Javadoc on FPS limiting.
   */
  public PhotonPipelineResult latestResult() {
    return camera.getLatestResult();
  }

  /**
   * @return the underlying {@link PhotonCamera}
   */
  public PhotonCamera camera() {
    return camera;
  }

  /**
   * @return the fixed camera-to-robot mount transform this simulated camera was built with
   */
  public Transform3d robotToCamera() {
    return ROBOT_TO_CAMERA;
  }

  /**
   * Configures pixel-level noise on target detections. Unseeded -- repeated calls with the same
   * arguments do not reproduce the same frames.
   *
   * @param avgErrPixels average pixel error
   * @param errStdDevPixels pixel error standard deviation
   */
  public void setNoise(double avgErrPixels, double errStdDevPixels) {
    cameraProperties.setCalibError(avgErrPixels, errStdDevPixels);
  }

  /**
   * Configures simulated processing latency.
   *
   * @param avgMs average latency, milliseconds
   * @param stdDevMs latency standard deviation, milliseconds
   */
  public void setLatency(double avgMs, double stdDevMs) {
    cameraProperties.setAvgLatencyMs(avgMs);
    cameraProperties.setLatencyStdDevMs(stdDevMs);
  }

  /**
   * @return the 2026 field's AprilTag layout, {@code k2026RebuiltWelded} -- matching
   *     {@code Robot-2026/src/main/java/frc/robot/Constants.java:38} exactly
   */
  public static AprilTagFieldLayout fieldLayout() {
    return AprilTagFieldLayout.loadField(AprilTagFields.k2026RebuiltWelded);
  }
}
```

- [ ] **Step 3: Compile**

```bash
export JAVA_HOME=~/wpilib/2026/jdk
cd /Users/y0shi/workspace/ignite/docs/.worktrees/m5/projects/m5-auto-vision
./gradlew compileJava --console=plain
```

If any overload above (`SimCameraProperties.setCalibration`, `PhotonCameraSim`'s
constructor, `AutoBuilder.configure`, `PPHolonomicDriveController`'s constructor, or
anything else) resolves differently on the installed vendordep versions than shown here,
fix the call site — the physical shape (kinematic integration, given fixed-rate drift,
real `VisionSystemSim`, `AutoBuilder` wired to this plant's own ground truth) is what must
not change, not the exact overload.

- [ ] **Step 4: Confirm no vision filtering threshold leaked in**

```bash
cd /Users/y0shi/workspace/ignite/docs/.worktrees/m5/projects/m5-auto-vision
grep -rniE 'MAX_TRUSTED_DISTANCE|max.?trusted|trust.?distance' src/main/java/frc/robot/plant/
```

Expected: no output. `SwerveDriveSim` and `VisionSim` are the plant, not the filter — the
filter, and any distance threshold it uses, is Task 3's `RobotContainer` stub, and it is
the student's to choose in Part 2. Neither plant should reference or hint at one.

- [ ] **Step 5: Confirm the field layout and module geometry**

```bash
cd /Users/y0shi/workspace/ignite/docs/.worktrees/m5/projects/m5-auto-vision
grep -n "k2026RebuiltWelded\|k2025" src/main/java/frc/robot/plant/VisionSim.java
grep -n "TRACK_WIDTH_METERS\|WHEEL_BASE_METERS" src/main/java/frc/robot/plant/SwerveDriveSim.java
```

Confirm `k2026RebuiltWelded` appears and `k2025...` does not, and that both module-geometry
constants read `0.6` — matching Task 1's `settings.json` `robotTrackwidth`/`flModuleX`/etc.
exactly.

- [ ] **Step 6: Determinism proof — not optional, and prove it fails without `pauseTiming` too**

This is the property the milestone's given tests rest on, and it has two halves that must
both be shown, not just asserted:

1. `SwerveDriveSim`, stepped in a loop, is bit-identical across runs in one JVM — same
   property M4's plants proved, same reason.
2. `VisionSim`, read in a loop **with** `SimHooks.pauseTiming()` and an explicit
   `SimHooks.stepTiming(...)` per tick, produces the same sequence of distinct frames
   across runs. **Without** `pauseTiming()`, in a free-running loop, it must visibly fail
   to (spike B/E's finding) — this half is what makes the mandatory-`pauseTiming()` rule in
   every given test something the executor has seen fail, not something copied from a rule
   list.

Create the throwaway `projects/m5-auto-vision/src/test/java/frc/robot/plant/DeterminismSpikeTest.java`:

```java
package frc.robot.plant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.simulation.DriverStationSim;
import edu.wpi.first.wpilibj.simulation.SimHooks;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DeterminismSpikeTest {

  private double runDrivetrain() {
    HAL.initialize(500, 0);
    DriverStationSim.setEnabled(true);
    SwerveDriveSim drivetrain = new SwerveDriveSim();
    drivetrain.drive(new ChassisSpeeds(1.0, 0.0, 0.3));
    for (int i = 0; i < 250; i++) {
      drivetrain.step(0.020);
    }
    return drivetrain.truePose().getX()
        + drivetrain.truePose().getY()
        + drivetrain.gyroAngle().getRadians();
  }

  private int distinctFramesWithPausedTiming() {
    HAL.initialize(500, 0);
    DriverStationSim.setEnabled(true);
    SwerveDriveSim drivetrain = new SwerveDriveSim();
    VisionSim vision = new VisionSim();
    drivetrain.drive(new ChassisSpeeds(1.0, 0.0, 0.0));
    SimHooks.pauseTiming();
    Set<Double> seenTimestamps = new HashSet<>();
    for (int i = 0; i < 250; i++) {
      drivetrain.step(0.020);
      vision.update(drivetrain.truePose());
      seenTimestamps.add(vision.latestResult().getTimestampSeconds());
      SimHooks.stepTiming(0.020);
    }
    SimHooks.resumeTiming();
    return seenTimestamps.size();
  }

  private int distinctFramesWithoutPausedTiming() {
    HAL.initialize(500, 0);
    DriverStationSim.setEnabled(true);
    SwerveDriveSim drivetrain = new SwerveDriveSim();
    VisionSim vision = new VisionSim();
    drivetrain.drive(new ChassisSpeeds(1.0, 0.0, 0.0));
    Set<Double> seenTimestamps = new HashSet<>();
    for (int i = 0; i < 250; i++) {
      drivetrain.step(0.020);
      vision.update(drivetrain.truePose());
      seenTimestamps.add(vision.latestResult().getTimestampSeconds());
    }
    return seenTimestamps.size();
  }

  @Test
  void drivetrainStepLoopIsBitIdenticalAcrossFiveRuns() {
    double first = runDrivetrain();
    for (int run = 1; run < 5; run++) {
      assertEquals(first, runDrivetrain(), 0.0, "run " + run + " diverged from run 0");
    }
  }

  @Test
  void visionFrameSequenceIsIdenticalAcrossThreeRunsWithPausedTiming() {
    int first = distinctFramesWithPausedTiming();
    for (int run = 1; run < 3; run++) {
      assertEquals(
          first, distinctFramesWithPausedTiming(), "run " + run + " saw a different frame count");
    }
  }

  @Test
  void withoutPausedTimingTheLoopSeesFarFewerDistinctFrames() {
    int paused = distinctFramesWithPausedTiming();
    int freeRunning = distinctFramesWithoutPausedTiming();
    assertTrue(
        freeRunning < paused,
        "Expected the free-running loop (no pauseTiming) to see fewer distinct frames than the "
            + "paused-and-stepped loop -- paused saw "
            + paused
            + ", free-running saw "
            + freeRunning
            + ". If this does not hold, spike B/E's finding does not reproduce here and the "
            + "mandatory-pauseTiming() rule in every given test needs to be re-examined before "
            + "Task 4 is written.");
  }
}
```

```bash
export JAVA_HOME=~/wpilib/2026/jdk
cd /Users/y0shi/workspace/ignite/docs/.worktrees/m5/projects/m5-auto-vision
./gradlew test --tests "frc.robot.plant.DeterminismSpikeTest" --console=plain --rerun-tasks
```

**Expect `DriverStation.reportError` noise about `AutoBuilder` "already configured"** in the
console output — `runDrivetrain()` constructs a fresh `SwerveDriveSim` five times in one
JVM, and each construction calls `AutoBuilder.configure(...)` again. This is benign here:
the spike never calls `AutoBuilder.followPath`/`buildAutoChooser`, so which instance
`AutoBuilder`'s global state happens to point at does not affect what this test checks. The
real test suite (Task 4) never hits this — `SimDriver` builds exactly one `RobotContainer`,
and therefore exactly one `SwerveDriveSim`, for its entire run.

**Record the actual values in your task report**: the drivetrain's final combined value,
the paused-timing distinct-frame count, and the free-running distinct-frame count. If
either determinism test fails, stop — that means one of the plants carries hidden
non-constructor state, and Task 4's test design needs to be rethought before it is
written, not patched with a tolerance. If the third test fails (free-running is *not*
visibly worse), that means spike G/B/E's characterization does not hold against this exact
vendordep pin and the `pauseTiming()` requirement needs to be re-justified, not assumed.

Then delete the spike — it is a proof, not a shipped test:

```bash
rm projects/m5-auto-vision/src/test/java/frc/robot/plant/DeterminismSpikeTest.java
```

- [ ] **Step 7: Format and commit**

```bash
cd /Users/y0shi/workspace/ignite/docs/.worktrees/m5/projects/m5-auto-vision
./gradlew spotlessApply --console=plain
./gradlew spotlessCheck --console=plain    # expect rc=0
./gradlew compileJava --console=plain      # expect rc=0
cd /Users/y0shi/workspace/ignite/docs/.worktrees/m5
git status --short    # only the two plant files -- DeterminismSpikeTest.java must be gone
git add projects/m5-auto-vision/src/main/java/frc/robot/plant
git commit -m "Add M5's SwerveDriveSim and VisionSim

SwerveDriveSim is a deterministic kinematic integrator carrying a
fixed-rate gyro drift (not noise) and its own AutoBuilder.configure(...)
wired against its own ground truth. VisionSim wraps the real
VisionSystemSim -- spike G found it honours WPILib's simulated clock,
unlike Phoenix's -- against AprilTagFields.k2026RebuiltWelded. Determinism
confirmed: drivetrain bit-identical across 5 runs (<value>); vision frame
sequence identical across 3 runs with pauseTiming (<paused> frames);
free-running without pauseTiming saw <free-running> frames, confirming
spike B/E reproduces here."
```

Replace `<value>`, `<paused>`, and `<free-running>` with Step 6's actual recorded numbers.
