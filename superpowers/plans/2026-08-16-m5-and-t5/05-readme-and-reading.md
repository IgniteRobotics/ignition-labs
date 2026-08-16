# Task 5: `README.md` and `READING.md`

The last milestone-lane task. Everything the student needs to hear in prose rather than in
a stub's Javadoc: the order to work in, why the drivetrain is a kinematic sim instead of a
real swerve (the thing a student who has driven the field with `simulateJava` will
otherwise ask about immediately), and the pointer into the real vision pipeline this
milestone was designed against.

**`docs/programming/milestones/m5-autonomous-odometry-vision.md` already exists** — it is
curriculum prose, not part of this plan, and this task does not touch it. It carries the
canonical Definition of Done and the mentor-verified list; `README.md` below mirrors that
list rather than inventing a second one, so the two never drift apart.

**No formula and no filtering threshold goes in either file** — same rule as every stub's
Javadoc. Step 4 greps for it.

**Files:**
- Create: `projects/m5-auto-vision/README.md`
- Create: `projects/m5-auto-vision/READING.md`

**Consumes:** Task 4's given tests (README references what they check), Task 3's
`M5Deliverables` Javadoc.
**Produces:** the two files a student opens first.

**Lane:** `milestone`. Worktree `.worktrees/m5`. This is the last task in the `milestone`
lane — stop and report back after Step 5.

---

- [ ] **Step 1: Write `README.md`**

Structure follows M4's: what's given vs. theirs, why the plant is shaped the way it is,
the order, "what the given tests check and don't," a modelling limit, then "done when" —
mirrored from the milestone doc's own DoD list rather than reinvented.

Create `projects/m5-auto-vision/README.md`:

```markdown
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
and `AutoBuilder.buildAutoChooser()` are ready to call the moment you construct your
`SwerveDriveSim`.

`VisionSim` is the opposite kind of given: it is the **real** `VisionSystemSim`, not a
stand-in. PhotonVision's simulation honours WPILib's simulated clock — unlike the CTRE
Phoenix simulation M4 had to work around — so the vision code you write runs against the
genuine vendor simulator.

---

## Read first, build second

Do [`READING.md`](READING.md) before you write anything. Two real files from `Robot-2026`,
at the exact commit this milestone was written against.

---

## Part 1 — an autonomous routine

1. In `RobotContainer`'s constructor, construct your `SwerveDriveSim` and `VisionSim`.
2. Build `autoRoutine()`: `AutoBuilder.followPath(PathPlannerPath.fromPathFile("Example Path"))`
   composed with at least one named command standing in for a real robot action (a
   `Commands.print(...)` is enough), via `Commands.sequence(...)`.
3. Add timeouts. Anything in your composition that is not a path — which finishes on its
   own — needs `.withTimeout(...)` or it can hang your whole autonomous period.
4. Build `autoChooser()`: a `SendableChooser<Command>` you populate yourself, with at least
   `autoRoutine()` as an option.
5. Run `./gradlew simulateJava` and watch your routine execute in AdvantageScope.
6. Deliberately make one step fail — replace a named command with something that never
   finishes and remove its timeout on purpose — and confirm the routine still terminates
   once you put the timeout back. This is what `AutoTerminationTest` checks automatically;
   watching it fail and then pass yourself is the point, not just the test going green.
7. Confirm `./gradlew test --tests AutoTerminationTest` passes.

## Part 2 — vision-corrected odometry

1. Build a pose estimator field (`SwerveDrivePoseEstimator`), seeded from
   `drivetrain().gyroAngle()` and `drivetrain().modulePositions()`. Write `updateOdometry()`
   against it — **wheels and gyro only, never vision** — and `estimatedPose()`/
   `resetEstimator()` as thin wrappers.
2. Run `./gradlew simulateJava`, drive around, and watch your estimate drift from truth in
   AdvantageScope. This drift is real — `SwerveDriveSim`'s gyro carries a fixed-rate bias on
   purpose.
3. Write `addVisionMeasurement()` to fuse a pose into the same estimator. Confirm
   `./gradlew test --tests FusedPoseTest` passes — mean pose error with vision fused should
   be visibly lower than odometry alone over a ten-second run.
4. Write `acceptsMeasurement()` — your filter. At minimum: reject an implausible timestamp,
   and reject a candidate pose far from what you currently believe. Confirm
   `./gradlew test --tests BadTimestampTest` passes.
5. **Submit a vision measurement with a deliberately wrong timestamp yourself** — call
   `addVisionMeasurement()` directly with a timestamp you know is wrong, bypassing your own
   filter — and watch the estimate get *worse* in AdvantageScope. Understand why: a pose is
   only correct at the instant it was captured, and telling the estimator it was captured
   at the wrong instant corrupts the fusion math, not just the number.

**Step 5 is the important one.** Everyone learns that vision helps. Far fewer learn that
vision applied wrongly is worse than no vision — and this milestone has exactly one test
that can catch a filter that got this backwards: `BadTimestampTest`.

---

## What the given tests check, and what they do not

`AutoTerminationTest` is a **black-box bound, not a fault-injection test**: it cannot reach
into your own named commands to force one to fail, so it checks the necessary condition
instead — your whole routine finishes within a generous but bounded budget. A composition
missing a timeout anywhere fails this, regardless of which step it was.

`FusedPoseTest` checks **mean error over a whole run**, not any single frame — vision noise
is unseeded on purpose, so no given test can assert on a frame-exact value. Passing this
test only says fusion helped on average; it does not grade your standard deviations or
your filter's exact threshold.

`BadTimestampTest` calls `acceptsMeasurement()` directly, with no camera involved at all,
and a pose identical to your current estimate — the only thing wrong is the timestamp.
This is deliberately the one test a distance-only filter cannot fake its way through.

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
```

- [ ] **Step 2: Write `READING.md`**

Two files from `Robot-2026`, at the pinned commit this plan already cites everywhere else:
`28ce5397d88275001d656dbb717d22cf9ac0150d`. Confirmed against a local checkout of
`Robot-2026` at that exact SHA before writing this task — `VisionSubsystem.java` selects
the lowest-ambiguity target per camera and scales its trust by tag distance, exactly the
two concepts `README.md`'s Part 2 asks a student to build a simplified version of;
`VisionConstants.java` names the real ambiguity threshold (`MAXIMUM_ALLOWED_AMBIGUITY =
0.25`) this milestone deliberately does not hand the student an equivalent of.

Create `projects/m5-auto-vision/READING.md`:

```markdown
# Reading — two files from `Robot-2026`

Not a whole-repository read — two files, plus one constants file, that this milestone was
designed against, at the exact commit this project cites everywhere else.

---

## Get it

\`\`\`bash
cd ~/workspace/ignite            # or wherever you keep repositories
git clone https://github.com/IgniteRobotics/Robot-2026.git
cd Robot-2026
git checkout 28ce5397d88275001d656dbb717d22cf9ac0150d
\`\`\`

Detached HEAD is expected — you are looking at a photograph of the repository, not
standing on a branch.

In a browser instead:
<https://github.com/IgniteRobotics/Robot-2026/tree/28ce5397d88275001d656dbb717d22cf9ac0150d>

---

## `src/main/java/frc/robot/subsystems/vision/VisionSubsystem.java`

Read this first. It is the real version of what your `acceptsMeasurement()`/
`addVisionMeasurement()` are a simplified model of — this file runs three cameras, not
one, and does considerably more than this milestone asks you to build, but the two ideas
underneath are the same ones.

Find:

1. The ambiguity-based target selection — search for `lowestAmbiguityScore` and
   `MAXIMUM_ALLOWED_AMBIGUITY`. A single AprilTag viewed near head-on has two
   mathematically valid pose solutions; this is the real robot's version of "reject an
   untrustworthy candidate" that your `acceptsMeasurement()` builds a much smaller version
   of.
2. The distance-scaled standard deviations — search for `averageRobotToTagDistance` and
   the comment describing the formula as `coefficient × distance^1.2 / tag_count`. Say, in
   one sentence, why trust should fall off with distance rather than staying constant.
3. `frontCameraResultsRejectedDueToJumping` and its neighbors — three separate rejection
   counters, one per camera, one per reason. This is a real team's answer to "how do I know
   my filter is actually filtering anything," logged rather than silent.

## `src/main/java/frc/robot/subsystems/vision/VisionConstants.java`

Find `MAXIMUM_ALLOWED_AMBIGUITY = 0.25` and `XY_STD_DEV_COEFFICIENT` /
`THETA_STD_DEV_COEFFICIENT`. These are real, tuned numbers for a real set of cameras at a
real mounting position — and they are exactly the shape of number this project's
`Constants.java` deliberately does not give you an equivalent of. Choosing your own
distance threshold in `RobotContainer` and being able to defend it is the milestone; this
file is what "defend it" looks like when a team has actually done the tuning.

## `src/main/java/frc/robot/statemachines/LaunchCalculator.java`

A real robot's use of a fused pose estimate for something with a right answer: computing a
launcher's target angle and rotational velocity from `DriveState.getInstance().getCurrentDriveStats().Pose`.
Find the `phaseDelay` field and the loop that iterates a lookahead pose against a
time-of-flight table. You do not need to reproduce this — read it as one concrete answer
to "why does it matter that the pose estimate I built in Part 2 is accurate," rather than
an abstract claim.

---

## Questions to answer

A mentor will ask. "I read it" is not an answer to any of them.

1. `VisionSubsystem` tracks a `frontCameraResultsRejectedDueToAmbiguity` counter separately
   from `frontCameraResultsRejectedDueToJumping`. Why might a team want to know *which*
   reason a measurement was rejected for, rather than just a single rejected/accepted
   count?
2. The distance-scaling formula raises distance to the power `1.2`, not `1.0` or `2.0`.
   You don't need to derive why `1.2` specifically — say what raising *any* distance term
   to a power greater than 1 does to trust at long range compared to scaling it linearly.
3. `LaunchCalculator` computes a `lookaheadPose` from the robot's current field-relative
   velocity before aiming. If the pose estimate feeding it were built from odometry alone,
   with no vision correction, what would that do to a shot taken after the robot had been
   driving for thirty seconds?

---

## Then, and only then

Start `RobotContainer.java`. `README.md` has the order.
```

- [ ] **Step 3: Confirm the pinned SHA and all three paths, once more, before shipping**

```bash
cd /Users/y0shi/workspace/ignite/Robot-2026
git rev-parse HEAD   # expect 28ce5397d88275001d656dbb717d22cf9ac0150d
test -f src/main/java/frc/robot/subsystems/vision/VisionSubsystem.java && echo OK
test -f src/main/java/frc/robot/subsystems/vision/VisionConstants.java && echo OK
test -f src/main/java/frc/robot/statemachines/LaunchCalculator.java && echo OK
```

All three `OK`s and the matching SHA are required before committing — a pinned reference
that does not resolve is worse than no reference at all.

- [ ] **Step 4: Confirm no filtering threshold or gain leaked into student-facing prose**

```bash
cd /Users/y0shi/workspace/ignite/docs/.worktrees/m5/projects/m5-auto-vision
grep -niE 'MAX_TRUSTED_DISTANCE|max.?trusted|trust.?distance' README.md READING.md
```

Expected: no output. Mentions of "distance-based filtering," "a threshold," or "the number
you picked" as concepts are fine and expected — the grep is tuned to catch a proposed
concrete constant name or value, not the concept.

- [ ] **Step 5: Commit**

```bash
cd /Users/y0shi/workspace/ignite/docs/.worktrees/m5
git status --short
git add projects/m5-auto-vision/README.md projects/m5-auto-vision/READING.md
git commit -m "Add M5's README and READING

READING.md points at VisionSubsystem.java, VisionConstants.java, and
LaunchCalculator.java from Robot-2026 at the pinned SHA -- the real
ambiguity-based filtering and distance-scaled trust this milestone's
acceptsMeasurement() builds a simplified version of. README.md states why
the drivetrain is a kinematic sim and why AutoBuilder ships already
configured against its own ground truth, and mirrors the milestone doc's
existing Definition of Done rather than inventing a second one."
```

This is the last task in the `milestone` lane. Tasks 6-7 (`testout` lane, a different
worktree) and tasks 8-9 (the serialized tail) are not yours — stop here and report back.
