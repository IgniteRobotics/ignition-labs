# M5 Auto + Vision, and T5 `drifting-auto` Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans. Steps use checkbox (`- [ ]`) syntax for tracking.
>
> This is a **split plan** per [`superpowers/plan-conventions.md`](../../plan-conventions.md). Read this file plus the one task file you were handed. Do not read sibling task files — each is self-sufficient, and reading them all is the cost this layout exists to remove.

**Goal:** Ship `projects/m5-auto-vision/` (an L1 milestone where the student composes an autonomous routine and fuses AprilTag vision into a pose estimator, against a given swerve drivetrain and vision harness) and `projects/testouts/t5-drifting-auto/` (three recorded log sets, one per root cause, for the Track D log-diagnosis fixture). This is build-order step 10 (spec §13) — and §10 calls T5 "the most expensive artifact in this spec."

**Architecture:** Two standalone GradleRIO projects. Unlike M4, **M5 uses its vendor simulation for real**, and that difference was measured rather than assumed. Three findings from the design record drive every task:

1. **PhotonVision's sim honours WPILib's simulated clock; CTRE's does not.** Paused timing plus `stepTiming(0.020)` yields 39 distinct camera frames identical to full precision across three runs. So the student's vision code runs against the genuine `VisionSystemSim`. A test that forgets `SimHooks.pauseTiming()` reads one frozen frame forever and passes or fails for reasons unrelated to the student.
2. **PathPlanner is deterministic headless** — 62 ticks, `endX=2.396930148`, three runs identical — including loading `.path` files and `RobotConfig` from `src/main/deploy/pathplanner/`.
3. **The drivetrain is a kinematic sim with no Phoenix in the loop**, for the reason step 9 measured. Building a swerve drivetrain is explicitly not M5's lesson.

**Tech Stack:** Java 17, GradleRIO 2026.2.1, Gradle 8.11, Spotless 8.2.1 (`googleJavaFormat 1.19.2`), JUnit Jupiter 5.10.1, PathplannerLib `2026.1.2`, photonlib `v2026.2.1`, Phoenix6 `26.1.1`, WPILibNewCommands `1.0.0`.

**Design record:** [`superpowers/specs/2026-08-16-m5-auto-vision-design.md`](../../specs/2026-08-16-m5-auto-vision-design.md). **Read it before Task 2, 4, or 6.** It carries all five spikes, the discriminating-signal table T5 depends on, and why an apparent vision nondeterminism was neither noise nor warm-up.

---

## Constraints delta

`CLAUDE.md` at the repository root loads automatically and already carries the toolchain, pinned versions, Spotless wiring, package naming, the fade curve, the authoring rules, the clean-clone invariant, and the verification discipline. **Read it. It is not repeated here.** Only what is specific to M5 and T5 appears below.

- **Fade level L1 — harness and fill** (spec §8), same as M3 and M4. Given: the swerve drivetrain and its kinematic sim, `AutoBuilder` already configured, the AprilTag layout, the `VisionSim` harness with configurable noise and latency, a wired `SendableChooser`, one example path file, `Main`/`Robot` wiring, and given tests. Student writes: paths, named commands, the composition with timeouts, chooser entries, pose-estimator fusion, timestamp handling, distance-based filtering.
- **`SimHooks.pauseTiming()` is mandatory in every given test.** Not a style preference — spike G does not hold without it. `PhotonCameraSim` is FPS-limited, and against a running wall-clock in a tight loop it returns the same cached frame while the robot drives away from it.
- **Vision noise is unseeded. No given test may assert a frame-exact vision value.** All three required assertions are aggregate or camera-free by design; see the table below. If a task finds itself wanting a per-frame vision assertion, that is a signal the test is wrong, not that the noise needs seeding.
- **Field layout: `AprilTagFields.k2026RebuiltWelded`, loaded as `AprilTagFieldLayout.loadField(...)`** — exactly what `Robot-2026/src/main/java/frc/robot/Constants.java:38` does. The design record's spike used the 2025 field because that is what it happened to load; **do not copy that**.
- **Phoenix6 ships as a vendordep and stays out of the tested control path.** Spec §9.6 requires the vendordep set to match `Robot-2026`; nothing requires a CTRE signal in the loop, and step 9 proved one cannot be stepped.
- **`RobotConfig.fromGUISettings()` reads the shipped `src/main/deploy/pathplanner/settings.json`.** Ship it. Building the config in code teaches a path the student will not take in their own project.
- **Package `frc.robot`** for M5 (M3 onward). **`frc.testout.t5`** for T5.
- **`plant/` and `harness/` are already globbed by `tools/tests_lock.py:PATTERNS`**, including the test-out variants added in step 9. Put given, locked code there and tamper-evidence is free.
- **Pinned SHA.** `Robot-2026` → `28ce5397d88275001d656dbb717d22cf9ac0150d`. Every student-facing reference to `vision/` or `LaunchCalculator` is pinned to it. **Real repo is read-only for M5** (spec §9.6) — no pull request in this milestone.
- **`tests.lock`, `docs/programming/README.md`, and `docs/programming/teaching-repo-spec.md` belong to Task 9 only.** Two lanes finishing together clobber each other there, every time.

---

## The pinned API contract

**Every signature below is fixed here and must match byte-for-byte in every task that names it.** No worker reads a sibling task file. If a task needs a signature that is not here, that is a gap in this plan — fix it here, not locally.

### `frc.robot.plant.SwerveDriveSim` — given, implemented, locked

Kinematic. Integrates commanded `ChassisSpeeds`; no motors, no Phoenix, deterministic.

```java
public final class SwerveDriveSim {
  public SwerveDriveSim();
  public void drive(ChassisSpeeds speeds);          // robot-relative
  public void step(double dtSeconds);               // advances true pose and module positions
  public Pose2d truePose();                         // ground truth -- tests use it, students should not
  public Rotation2d gyroAngle();                    // carries the drift the student must correct
  public SwerveModulePosition[] modulePositions();
  public ChassisSpeeds currentSpeeds();
  public static SwerveDriveKinematics kinematics();
  public void resetTo(Pose2d pose);
}
```

### `frc.robot.plant.VisionSim` — given, implemented, locked

Wraps PhotonVision's `VisionSystemSim`. Noise and latency configurable so the student can make it worse on purpose (§9.6).

```java
public final class VisionSim {
  public VisionSim();
  public void update(Pose2d trueRobotPose);         // call AFTER SwerveDriveSim.step
  public PhotonPipelineResult latestResult();
  public PhotonCamera camera();
  public Transform3d robotToCamera();
  public void setNoise(double avgErrPixels, double errStdDevPixels);
  public void setLatency(double avgMs, double stdDevMs);
  public static AprilTagFieldLayout fieldLayout();  // k2026RebuiltWelded
}
```

### `frc.robot.plant.ScoringMechanism` — given, implemented, locked

**Added 2026-08-16 during Phase A review. This was a gap in the first draft of the contract.**
Without it, `AutoTerminationTest` degrades into "the routine finished within a budget," which
proves nothing: PathPlanner's follow command is time-based and terminates on its own whether or
not the robot went anywhere, so nothing in a path-only routine can actually hang. Spec §9.6 and
the milestone's project step 7 both require *forcing a step to fail*, and this is the thing that
can be made to fail.

```java
public final class ScoringMechanism {
  public ScoringMechanism();
  public void startDeploy();
  public boolean isDeployed();          // true a fixed number of ticks after startDeploy()
  public void step(double dtSeconds);
  public void setFaulted(boolean faulted);  // faulted -> isDeployed() never becomes true
  public void reset();
}
```

The student's named command starts the deploy and waits for `isDeployed()`. `setFaulted(true)` is
how a given test forces that step to fail, and a routine without a timeout will hang on it — which
is exactly the lesson project step 4 ("add timeouts so no step can hang") teaches.

### `frc.robot.harness.VisionPoseHelper` — given, implemented, locked

**Added 2026-08-16 during Phase A review.** Converting a raw `PhotonPipelineResult` into a
candidate field pose is given, not student work — but it must live in **one** place in `src/main`
that both `Robot` and `SimDriver` call. A test-source copy and a main-source copy of the same
conversion will drift, and the day they disagree the given test is measuring something the robot
does not do.

```java
public final class VisionPoseHelper {
  /** Empty when the frame has no usable target. */
  public static Optional<Pose2d> candidatePose(PhotonPipelineResult result);
  public static double timestampSeconds(PhotonPipelineResult result);
}
```

Uses photonlib's strategy-specific estimation. **Do not call `PhotonPoseEstimator.update(PhotonPipelineResult)`** — it is `@Deprecated(forRemoval = true, since = "2026")` in the pinned `v2026.2.1`.

### `frc.robot.harness.M5Deliverables` — given interface, `RobotContainer` implements it

```java
public interface M5Deliverables {
  Command autoRoutine();                            // a NEW instance each call
  SendableChooser<Command> autoChooser();
  void updateOdometry();                            // one 20ms iteration: wheels + gyro only
  void addVisionMeasurement(Pose2d visionPose, double timestampSeconds);
  boolean acceptsMeasurement(Pose2d visionPose, double timestampSeconds);  // the student's filter
  Pose2d estimatedPose();
  void resetEstimator(Pose2d pose);
  SwerveDriveSim drivetrain();
  VisionSim vision();
  ScoringMechanism mechanism();
}
```

`acceptsMeasurement` is the seam the timestamp test drives directly, with no camera in the loop. It is the single most important method in the interface — §9.6 calls the wrong-timestamp DoD item "the only DoD item in M5 a student can otherwise fake by getting a good result for the wrong reason."

### `frc.robot.harness.SimDriver` — given test support, locked

Shaped after M4's. **Must call `SimHooks.pauseTiming()` in `setUp()`.**

```java
public final class SimDriver {
  public static void setUp();
  public static void tearDown();
  public static RobotContainer container();
  /** Steps drivetrain + vision + the student's odometry for N ticks; returns per-tick pose error. */
  public static double[] runOdometryLoop(int iterations, boolean feedVision);
  /** Schedules a command, runs the scheduler stepped, returns whether it finished within budget. */
  public static boolean runCommandToCompletion(Command command, int maxIterations);

  // Guarded accessors -- added 2026-08-17 during Task 4 verification. Every one of these wraps
  // a RobotContainer method a given test reaches DIRECTLY, turning the stub's
  // UnsupportedOperationException into a sentence. They exist because the first pass had the
  // tests call container().autoRoutine(), container().mechanism(), container().resetEstimator()
  // and container().acceptsMeasurement() straight through: three of the four given tests then
  // failed against an untouched skeleton with a raw stack trace instead of guidance, which is
  // precisely what §12.3 forbids and what SimDriver exists to prevent.
  public static Command autoRoutine();
  public static ScoringMechanism mechanism();
  public static void resetEstimator(Pose2d pose);
  public static boolean acceptsMeasurement(Pose2d visionPose, double timestampSeconds);
}
```

**A given test must never call `container().<student method>()` directly.** `container()` itself
is public because a test needs the instance, but every route from there into student code belongs
behind one of the guarded accessors above. Adding a given test that reaches a student method not
in this list means adding a guarded accessor for it first.

### Constants

`frc.robot.Constants.SIM_STEP_SECONDS = 0.020`
`frc.robot.Constants.VisionConstants.MAX_TRUSTED_DISTANCE_METERS` — **does not exist.** The filtering threshold is the student's to choose and defend; a constant here would hand it over.

### T5 — `frc.testout.t5`

```java
public final class LogRecorder {
  public enum Fault { ODOMETRY_DRIFT, MISTUNED_CONTROLLER, BLOCKING_COMMAND }
  public static Path record(Fault fault, Path outputDirectory) throws IOException;
}
```

One auto, one drivetrain, three runs, one fault injected each. Recorded, not synthesized — design record §3 explains why that is not a convenience choice.

---

## File structure

**Created — `projects/m5-auto-vision/`**

| Path | Responsibility |
|---|---|
| Gradle scaffold, `.wpilib/`, `simgui*.json`, `WPILib-License.md` | Copied from `projects/m4-control/` |
| `vendordeps/{PathplannerLib-2026.1.2,photonlib,Phoenix6-26.1.1,WPILibNewCommands}.json` | Verbatim from `Robot-2026` @ `28ce539` |
| `src/main/deploy/pathplanner/settings.json` | Ships; `RobotConfig.fromGUISettings()` reads it |
| `src/main/deploy/pathplanner/paths/Example Path.path` | The one given example path (§9.6) |
| `src/main/java/frc/robot/plant/SwerveDriveSim.java` | **Given, locked.** Kinematic, deterministic |
| `src/main/java/frc/robot/plant/VisionSim.java` | **Given, locked.** Real `VisionSystemSim` |
| `src/main/java/frc/robot/plant/ScoringMechanism.java` | **Given, locked.** The one step `AutoTerminationTest` can force to fail |
| `src/main/java/frc/robot/harness/M5Deliverables.java` | **Given, locked.** The seam |
| `src/main/java/frc/robot/harness/VisionPoseHelper.java` | **Given, locked.** The one place the camera-frame-to-candidate-pose conversion lives |
| `src/main/java/frc/robot/Main.java`, `Robot.java`, `Constants.java` | Given wiring |
| `src/main/java/frc/robot/RobotContainer.java` | **Student territory.** Throwing stubs |
| `src/test/java/frc/robot/AutoTerminationTest.java` | Given. Routine terminates when a step is forced to fail |
| `src/test/java/frc/robot/FusedPoseTest.java` | Given. Fused error < odometry-only error over a run |
| `src/test/java/frc/robot/BadTimestampTest.java` | Given. The student's filter rejects a wrong-timestamp measurement |
| `src/test/java/frc/robot/harness/SimDriver.java` | Given. Owns `pauseTiming` and the stepped loop |
| `README.md`, `READING.md` | Student-facing; `Robot-2026`'s `vision/` and `LaunchCalculator`, pinned |

**Created — `projects/testouts/t5-drifting-auto/`**

| Path | Responsibility |
|---|---|
| Gradle scaffold | From `m5-auto-vision`; needs PathPlanner + WPILib, no photonlib unless a log needs vision signals |
| `src/main/java/frc/testout/t5/plant/T5DriveSim.java` | **Given, locked.** The drivetrain the recordings ran against |
| `src/main/java/frc/testout/t5/LogRecorder.java` | **Given, not locked.** Regenerates all three logs. `tools/tests_lock.py:PATTERNS` reaches `**/plant/*.java` and `**/harness/*.java` only, so a file at the package root is outside the manifest by construction. Left there deliberately rather than moved: T5 ships no tests, so there is no gate a tampered recorder could subvert, and the second known-limitation note below makes locking it pointless anyway. **Task 9 must not expect it in `tests.lock`.** |
| `logs/a-*.wpilog`, `logs/b-*.wpilog`, `logs/c-*.wpilog` | The three recorded sets. Neutral filenames — see below |
| `logs/README.md` | How to regenerate (§16). Never which log is which fault |
| `README.md` | The task, the three candidate causes, the 60-minute box |

**Created — elsewhere:** `../ignite-training-solutions/solutions/m5-auto-vision/…`, `answer-keys/t5-drifting-auto.md`, `authoring-notes/m5.md`.

**Modified (Task 9 only):** `tests.lock`, `docs/programming/milestones/m5-autonomous-odometry-vision.md`, `docs/programming/README.md`, `docs/programming/teaching-repo-spec.md`.

---

## Task graph

| Task | File | Depends on | Touches | Lane |
|---|---|---|---|---|
| 1 | `01-scaffold.md` | — | `projects/m5-auto-vision/` build files, vendordeps, deploy | milestone |
| 2 | `02-drivetrain-and-vision.md` | 1 | `.../frc/robot/plant/` | milestone |
| 3 | `03-harness-and-wiring.md` | 2 | `.../frc/robot/harness/`, `Robot`, `Constants`, `RobotContainer` | milestone |
| 4 | `04-given-tests.md` | 3 | `projects/m5-auto-vision/src/test/` | milestone |
| 5 | `05-readme-and-reading.md` | 4 | `README.md`, `READING.md` | milestone |
| 6 | `06-t5-recorder.md` | — | `projects/testouts/t5-drifting-auto/` src | testout |
| 7 | `07-t5-logs-and-readme.md` | 6 | `t5-drifting-auto/logs/`, `README.md` | testout |
| 8 | `08-solutions.md` | 4, 7 | `../ignite-training-solutions/` | tail |
| 9 | `99-integration.md` | all | `tests.lock`, `docs/` | integration |

**Lanes:** `milestone` (1 → 2 → 3 → 4 → 5, strictly sequential — one Gradle project, one `build/`), `testout` (6 → 7).
**Maximum useful concurrency: 2.** Do not fan out further.
**Serialized tail:** task 8, then task 9, on the merged branch.
**Worktrees:** `.worktrees/m5` and `.worktrees/t5`. Never two agents in one worktree.

**Task 6 is the long pole, not Task 2.** §10 budgets T5 as the most expensive artifact in the spec, and the cost is not writing the recorder — it is making three logs each of which *rules out* the other two causes. Start the testout lane at the same time as the milestone lane, not after.

---

## Self-review

**Spec coverage.**

| Spec requirement | Task |
|---|---|
| §9.6 simulated swerve drivetrain with `AutoBuilder` configured | 2, 3 |
| §9.6 AprilTag field layout | 2 (`k2026RebuiltWelded`) |
| §9.6 `VisionSim` on `VisionSystemSim` with configurable noise and latency | 2 |
| §9.6 `SendableChooser` already wired | 3 |
| §9.6 one example path file | 1 |
| §9.6 student writes paths, named commands, composition with timeouts, chooser entries | 3 (stubs), 8 (reference) |
| §9.6 student writes fusion, timestamp handling, distance filtering | 3 (stubs), 8 (reference) |
| §9.6 vendordeps match `Robot-2026` exactly | 1 |
| §9.6 real repo read-only: `vision/`, `LaunchCalculator` | 5 (`READING.md`) |
| §9.6 test: routine terminates when a step is forced to fail | 4 |
| §9.6 test: fused pose error < odometry-only error | 4 |
| §9.6 test: wrong-timestamp measurement rejected by the student's filter | 4 |
| §9.6 mentor-verified: odometry drift, PathPlanner-vs-Choreo tradeoff | 5, 9 |
| §10 T5: three log sets, one per root cause, mentor picks one | 6, 7 |
| §10 pass criterion is diagnosis **and** the ruling-out | 7 (logs carry the discriminating signals), 8 (answer key) |
| §11.1 reference implementation, answer key explaining distractors, authoring notes | 8 |
| §11.2 cross-check green | 8 |
| §12.3 clean-clone invariant | 4, 9 |
| §16 T5's logs regenerable | 6 (`LogRecorder`), 7 (`logs/README.md`) |

**Not covered, deliberately.** M5's mentor-verified half stays mentor-verified per §9.6: explaining odometry drift and why vision corrects it, why multi-tag beats single-tag, one real PathPlanner-vs-Choreo tradeoff, and why a blue path is not a red path. None gets a test. The AdvantageScope DoD item ("visible in AdvantageScope") is satisfied against the student's own sim run, not a fixture.

**The failure class this plan is built against.** Step 9 produced six defects, none of which showed up as a red test: a HAL clamp that silently zeroed a plant, an arm too light for its own kG test to fail, rejected constants surviving in an executed code block under corrected prose, a README that answered its own pass-criterion question, a limit switch gravity trips for you, and a `tests.lock` glob that missed test-out plants. M5 has the same shape of risk in new places. These steps are not optional:

- **Task 2 Step 6** — determinism proof for **both** plants: five runs in one JVM, bit-identical. For `VisionSim` this only holds with `pauseTiming`; prove it holds, and prove it *fails* without, so the requirement is understood rather than copied.
- **Task 4 Step 5** — run a correct implementation. The suite must be satisfiable.
- **Task 4 Step 6** — run the distractors. At minimum: a filter that accepts everything (must fail `BadTimestampTest`); fusion with no timestamp handling at all (must fail `FusedPoseTest`); a routine with no timeouts whose failing step hangs (must fail `AutoTerminationTest`). Read every message.
- **Task 4 Step 7** — confirm `FusedPoseTest` **fails** for odometry-only. A test that passes whether or not vision is wired proves nothing, and this is the single easiest assertion in M5 to get backwards.
- **Task 6 Step 5** — the discrimination matrix. For each of the three logs, confirm **the other two causes can be ruled out from signals actually present in that file.** Design record §3 lists the minimum signal set. A log missing one makes a ruling-out impossible and nobody finds out until a mentor is mid-fixture. This is the most important verification in the plan.
- **Task 7 Step 4** — regenerate all three logs and confirm they reproduce.

A task report that omits its command output is not a completed task.

**Type consistency.** Every cross-task signature is pinned above and nowhere else. `SwerveDriveSim` appears in tasks 2, 3, 4, 8; `VisionSim` in 2, 3, 4, 8; `M5Deliverables` in 3, 4, 8; `SimDriver` in 4, 8; `LogRecorder` and `T5DriveSim` in 6, 7, 8; `Constants.SIM_STEP_SECONDS` in 3 and 4.

**AutoBuilder follows ground truth, not the student's estimate — and that must be stated in the skeleton.** `AutoBuilder.configure(...)` is owned by `SwerveDriveSim` (the only given class that can own it, since `RobotContainer` is entirely student stub) and is wired to `truePose()`. On a real robot — including `Robot-2026` — the path follower runs against the pose *estimator*. The training project differs so that Part 1 (composition, timeouts, chooser, termination) works before Part 2 (fusion) is written, and so `AutoTerminationTest` does not fail because the student's estimator is still a stub. None of Part 1's DoD items depend on closed-loop path accuracy.

That is a real divergence from the pattern the student will use next season, so it is documented in three places, in these words or better: `SwerveDriveSim`'s class Javadoc, the project `README.md`, and the milestone document. Precedent: M4's inert-configuration limitation was handled the same way. A student who notices the difference and asks is doing exactly the right thing; a student who copies the wiring into a season repo has been misled by us.

**Known limitation, decided not deferred.** T5's three logs ship as files a student can open in a hex editor, and their filenames would leak the answer if named for their faults. They are named `a-`, `b-`, `c-` for that reason, and `logs/README.md` never says which is which — the mapping lives only in the solutions repository's answer key. This is the same class of leak as step 9's `t4-bad-kv` directory name, caught this time before it shipped rather than after.

**Second known limitation, accepted with eyes open (Josh's call, 2026-08-16).** The measure above hides the mapping; it does not make it unrecoverable. `LogRecorder` and `T5DriveSim` ship inside the student-facing fixture, generation is deterministic by design (§16 requires it), and `logs/README.md` documents the recovery recipe for maintainers:

```bash
./gradlew generateLog -Pfault=ODOMETRY_DRIFT -Pout=/tmp/candidate.wpilog
cmp /tmp/candidate.wpilog logs/a-run.wpilog
```

Three runs and a `cmp` yield the diagnosis without opening AdvantageScope, and deleting that section from `logs/README.md` would not close it — a `Fault` enum next to a `generateLog` task is a signpost on its own. The alternative considered and rejected was moving both classes into `../ignite-training-solutions/` and shipping `logs/` + `README.md` alone.

**Rejected because the diagnosis is not the pass criterion.** §10 and the milestone document both require *the ruling-out* — a student who recovers the letter still has to point at signals and say what excludes the other two, in front of a mentor, and that is the half this fixture exists to measure. Treat a student who finds the shortcut as having demonstrated something worth noting rather than as having cheated. **Do not "fix" this in a later maintenance pass without re-reading this paragraph** — it is a decision, not an oversight, and the deterministic regeneration it depends on is a §16 requirement that outranks the leak.

Mentors should know the shortcut exists; that belongs in the answer key (Task 8), not in anything student-facing.
