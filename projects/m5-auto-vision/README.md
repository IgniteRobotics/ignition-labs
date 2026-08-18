# M5 — Autonomous, Odometry & Vision

An autonomous routine composed from a path and named commands, and a pose estimator that
fuses simulated AprilTag vision into wheel odometry. This is the milestone where "where is
the robot" stops being an assumption and starts being a measured, correctable estimate.

Four to six sessions is normal.

---

## What you are given, and what is yours

| Given | Why |
|---|---|
| `Main.java` | Never edit it. Every WPILib project has one and they are all identical |
| `Robot.java` | Runs the scheduler, your odometry update, and the given vision-to-estimator wiring every 20ms. Read it before you start |
| `Constants.java` | One timing constant. No filtering threshold lives here — that is yours to add, and choosing it is the milestone |
| `plant/SwerveDriveSim.java` | A deterministic kinematic drivetrain — no motors, no Phoenix. Read its Javadoc: it already configures `AutoBuilder` against its own ground truth, and it carries a fixed-rate gyro drift your odometry has to live with |
| `plant/VisionSim.java` | Wraps PhotonVision's real `VisionSystemSim`, against `AprilTagFields.k2026RebuiltWelded`. Read its Javadoc — it states exactly when to call `update()` and why every given test pauses simulated time before reading it |
| `plant/ScoringMechanism.java` | A stand-in for a real robot action — an intake, a shot. Becomes deployed a fixed number of ticks after `startDeploy()`, and can be *faulted* so it never deploys at all. This is the one thing in the milestone that can be made to hang |
| `harness/VisionPoseHelper.java` | Turns a raw camera frame into a candidate field pose and its timestamp. Given, not yours — `Robot.java` and the given tests both call it, so you and they are always converting frames the same way |
| `harness/M5Deliverables.java` | How the tests, and `Robot`, reach your work. Read it before you start |
| `src/main/deploy/pathplanner/` | One example path, and the settings file `RobotConfig.fromGUISettings()` reads |
| `src/test/` | The given tests. Do not edit them |

Yours: **the auto routine (paths, named commands, timeouts, chooser), the pose estimator's
odometry update, its vision fusion, and the filter that decides what to trust.** All of it
lives in `RobotContainer.java`.

---

## Why the drivetrain is a kinematic sim, not a real swerve

`SwerveDriveSim` integrates whatever `ChassisSpeeds` you command it with, perfectly, every
tick. No `TalonFX`, no Phoenix, no motor lag. Building a real swerve drivetrain is not this
milestone's lesson — composing autos and fusing vision is, and a kinematic plant lets both
of those be tested deterministically without a swerve module's control loop in the way.

This is also why `AutoBuilder` ships **already configured**: `SwerveDriveSim`'s constructor
wires it against its own ground truth (`truePose()`/`resetTo()`), before your
`RobotContainer` — and your pose estimator — exist at all. `AutoBuilder.followPath(...)`
is ready to call the moment you construct your `SwerveDriveSim`.

`VisionSim` is the opposite kind of given: it is the **real** `VisionSystemSim`, not a
stand-in. PhotonVision's simulation honours WPILib's simulated clock — unlike the CTRE
Phoenix simulation M4 had to work around — so the vision code you write runs against the
genuine vendor simulator.

---

## Read first, build second

Do [`READING.md`](READING.md) before you write anything. Three real files from `Robot-2026`,
at the exact commit this milestone was written against.

---

## Part 1 — an autonomous routine

1. In `RobotContainer`'s constructor, construct your `SwerveDriveSim`, your `VisionSim`, and
   your `ScoringMechanism`, and return each from its accessor. Nothing else works until all
   three exist — the given tests reach your work through them.
2. Build `autoRoutine()`: `AutoBuilder.followPath(PathPlannerPath.fromPathFile("Example Path"))`
   composed, via `Commands.sequence(...)`, with at least one named command that actually
   drives the scoring mechanism — start its deploy, then wait until `isDeployed()` reports
   back. A named command that only prints will pass the given tests without ever teaching
   you what they are for.
3. Add timeouts. Anything in your composition that is not a path — which finishes on its
   own — needs `.withTimeout(...)` or it can hang your whole autonomous period. The
   mechanism step is the one that can genuinely hang, so it is the one that most needs a
   bound.
4. Build `autoChooser()`: a `SendableChooser<Command>` you populate yourself, with at least
   `autoRoutine()` as an option.
5. Run `./gradlew simulateJava` and watch your routine execute in AdvantageScope.
6. Deliberately break it: call `mechanism().setFaulted(true)` before your routine runs, so
   `isDeployed()` can never become true. With no timeout on the mechanism step, your routine
   hangs forever. Put the timeout back and watch it terminate anyway, having given up on a
   step that was never going to finish. **Do this yourself before letting the test do it for
   you** — `AutoTerminationTest` checks exactly this, and watching it hang is the lesson.
7. Confirm `./gradlew test --tests AutoTerminationTest` passes.

## Part 2 — vision-corrected odometry

1. Build a pose estimator field (`SwerveDrivePoseEstimator`), seeded from
   `drivetrain().gyroAngle()` and `drivetrain().modulePositions()`. Write `updateOdometry()`
   against it — **wheels and gyro only, never vision** — and `estimatedPose()`/
   `resetEstimator()` as thin wrappers.
2. Run `./gradlew simulateJava`, drive around, and watch your estimate drift from truth in
   AdvantageScope. This drift is real — `SwerveDriveSim`'s gyro carries a fixed-rate bias on
   purpose.
3. Write `addVisionMeasurement()` to fuse a pose into the same estimator, and
   `acceptsMeasurement()` to decide what reaches it. At minimum your filter must reject an
   implausible timestamp, and reject a candidate pose far from what you currently believe.
4. Confirm `./gradlew test --tests FusedPoseTest` passes. It drives a 30-second run twice —
   once odometry-only, once with vision — and requires your fused mean error to be **at most
   half** the odometry-only mean error. Half is a low bar on purpose: a working fusion lands
   near a fifth. If you are close to the line rather than far under it, your filter is
   probably rejecting measurements it should be taking.
5. Confirm `./gradlew test --tests BadTimestampTest` passes.
6. **Submit a vision measurement with a deliberately wrong timestamp yourself** — call
   `addVisionMeasurement()` directly with a timestamp you know is wrong, bypassing your own
   filter — and watch the estimate get *worse* in AdvantageScope. Understand why: a pose is
   only correct at the instant it was captured, and telling the estimator it was captured
   at the wrong instant corrupts the fusion math, not just the number.

**Step 6 is the important one.** Everyone learns that vision helps. Far fewer learn that
vision applied wrongly is worse than no vision — and this milestone has exactly one test
that can catch a filter that got this backwards: `BadTimestampTest`.

---

## What the given tests check, and what they do not

Four test methods across three files.

`AutoTerminationTest` runs your routine twice. Once normally, against a bounded budget. Once
with the scoring mechanism **faulted** — `isDeployed()` can never become true — where the
only way to finish is a timeout on the step that waits for it. The second run is why the
mechanism exists at all: PathPlanner's path-following command is time-based and terminates
on its own whether or not the robot went anywhere, so a path-only routine has nothing in it
that can hang, and a test of "does it terminate" would prove nothing. What this test cannot
do is reach inside your own named commands — it faults the given mechanism, not your code,
so a routine that never touches `mechanism()` passes both methods without being tested at
all. That is the reason Part 1 step 2 asks you to use it.

`FusedPoseTest` checks **mean error over a whole run**, not any single frame — vision noise
is unseeded on purpose, so no given test can assert on a frame-exact value. Passing it says
fusion measurably helped; it does not grade your standard deviations or your filter's exact
threshold.

`BadTimestampTest` calls `acceptsMeasurement()` directly, with no camera involved at all,
and a pose identical to your current estimate — the only thing wrong is the timestamp.
This is deliberately the one test a distance-only filter cannot fake its way through.

None of the four can check whether you *understand* any of it. That half is mentor-verified,
and it is listed below.

---

## A modelling limit, on purpose

`SwerveDriveSim` applies every commanded `ChassisSpeeds` instantaneously — there is no
motor lag, no acceleration limit, no wheel slip. If you command a large rotation and see
the true pose snap to match it within one 20ms tick, you have not found a bug; you have
found the edge of what a kinematic sim represents. `SwerveDriveSim`'s Javadoc says the same
thing, in the file you'll actually be reading.

---

## Definition of done

- [ ] `./gradlew test` is green
- [ ] Auto routine completes in simulation and is visible in AdvantageScope
- [ ] It terminates safely when a step is made to fail
- [ ] A named command triggers at the right point on the path
- [ ] Routine is selectable from a chooser
- [ ] You can explain what odometry drift is and why vision corrects it
- [ ] You demonstrated vision *hurting* the estimate via a bad timestamp, and can explain why
- [ ] You can explain why multi-tag beats single-tag
- [ ] You can articulate one real tradeoff between PathPlanner and Choreo
- [ ] You can explain why a blue path is not a red path

The odometry-drift, bad-timestamp, multi-tag, PathPlanner-vs-Choreo, and alliance
explanations are mentor-verified. No test can check them, and they are half the milestone.
