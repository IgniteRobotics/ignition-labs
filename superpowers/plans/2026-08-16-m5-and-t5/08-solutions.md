# Task 8: Reference solution, T5 answer key, authoring notes

Everything mentor-only that step 10 owes the companion repository. Spec §11.2 is blunt about the
stakes — *a milestone shipped without its solution is not shipped* — and §11.1 requires the T5
answer key to explain the **distractors**, which for a log-diagnosis fixture means the ruling-out
evidence itself, not merely which letter is which fault.

Two things make this task riskier than M4's equivalent.

**First, this is the only place anybody proves M5's given tests are satisfiable.** Every
verification up to here has proved they can *fail* — the distractor matrix in Task 4 is entirely
negative evidence. A suite no correct answer passes is exactly as broken as one every wrong answer
passes, and until Step 3 runs, nobody knows which kind M5 is.

**Second, `FusedPoseTest` is a comparative assertion against an unseeded noise source.** M4's
assertions were deterministic scalars. This one says *fused error beats odometry-only error over a
run*, and the reference implementation is the first honest sample of the margin. If it passes by a
hair, the test is a coin flip in CI and that has to surface here rather than in a student's first
session.

**Files:**
- Create: `../ignite-training-solutions/solutions/m5-auto-vision/src/main/java/frc/robot/RobotContainer.java`
- Create: `../ignite-training-solutions/solutions/m5-auto-vision/src/main/deploy/pathplanner/paths/*.path` (the routine's own paths, if the reference routine needs more than the shipped example)
- Create: `../ignite-training-solutions/answer-keys/t5-drifting-auto.md`
- Create: `../ignite-training-solutions/authoring-notes/m5.md`
- Test: `../ignite-training-solutions/tools/cross-check.sh`

**Consumes:** `M5Deliverables`, `SwerveDriveSim`, `VisionSim`, `ScoringMechanism`,
`VisionPoseHelper`, `SimDriver`, `Constants.SIM_STEP_SECONDS` — every signature pinned in
`00-plan.md`. `LogRecorder.Fault` and `T5DriveSim` for the answer key.

**Produces:** nothing this repository consumes. The cross-check job is the only consumer.

**Lane:** tail. Runs on the merged branch, after both lanes have committed — `cross-check.sh`
archives the labs repo at a ref, so uncommitted work in a worktree is invisible to it.

---

- [ ] **Step 1: Confirm both lanes are merged and committed**

```bash
cd /Users/y0shi/workspace/ignite/docs
git log --oneline -1                       # expect the merge of both lanes
git status --short                         # expect empty
```

`cross-check.sh` extracts `HEAD` with `git archive`. A dirty tree or an unmerged lane means it
silently tests something other than what you think.

- [ ] **Step 2: Write `RobotContainer.java` implementing `M5Deliverables`**

Ten methods, in two groups. The whole point of the interface split is that Part 1 works before
Part 2 exists, so write it in that order and run the suite between the two.

**Part 1 — composition (`autoRoutine`, `autoChooser`, `mechanism`, `drivetrain`).**

`autoRoutine()` returns a **new instance each call** — the contract says so, and a cached `Command`
that has already run is finished forever, which turns `AutoTerminationTest` into a test of nothing.
Compose: follow the example path, run a named command that calls `mechanism().startDeploy()` and
waits on `isDeployed()`, follow a second path. **Every step that can block carries a timeout**, and
the deploy step is the one that can — `ScoringMechanism.setFaulted(true)` is precisely how Task 4
forces it to. Register the named command with `NamedCommands.registerCommand` before building the
routine; PathPlanner resolves markers at build time, and registering afterwards yields a routine
whose marker fires nothing with no error at all.

`autoChooser()` returns the wired `SendableChooser<Command>` with at least the one routine as an
entry. Add a `Commands.none()` "do nothing" default — that is what a real drive team wants and it
costs one line.

**Part 2 — fusion (`updateOdometry`, `addVisionMeasurement`, `acceptsMeasurement`, `estimatedPose`,
`resetEstimator`, `vision`).**

Back the estimator with `SwerveDrivePoseEstimator` over `SwerveDriveSim.kinematics()`.
`updateOdometry()` is one 20ms iteration of **wheels and gyro only** — `gyroAngle()` and
`modulePositions()`, nothing from the camera. That separation is what lets `FusedPoseTest` run the
same loop twice with vision on and off.

`acceptsMeasurement(Pose2d, double)` is the filter, and it is the method §9.6 calls the only DoD
item a student can otherwise fake. The reference implementation rejects on all three of:

| Reject when | Because |
|---|---|
| the timestamp is not plausibly recent — outside a small window around the current sim time | this is the wrong-timestamp case, and it must be caught **without a camera in the loop** |
| the candidate pose is outside the field bounds | `VisionSim.fieldLayout()` gives the field dimensions; a pose off the carpet is a bad solve |
| the tag distance exceeds the student's chosen threshold | §9.6's distance-based filtering |

**There is no `MAX_TRUSTED_DISTANCE_METERS` constant and you must not add one to the skeleton.**
The threshold is the student's to choose and defend. Pick one here, in the solution, and write down
in `authoring-notes/m5.md` how it was chosen — a mentor needs to know what a defensible answer
sounds like, and "4 meters because the answer key says 4 meters" is not one.

`addVisionMeasurement` calls `acceptsMeasurement` first and drops the measurement when it is false.
A filter the fusion path does not consult is a filter that passes review and does nothing.

- [ ] **Step 3: Prove the suite is satisfiable — the step this task exists for**

```bash
cd ../ignite-training-solutions && ./tools/cross-check.sh 2>&1 | tail -40
```

Expected: every overlaid milestone green, **including M5's three given tests**. Paste the output.

If M5 is red here, **do not adjust the tests to make the solution pass.** Report which assertion
failed and by how much. A tolerance moved to accommodate a solution is a tolerance that no longer
measures anything, and it is the precise failure mode `CLAUDE.md`'s verification section names.

- [ ] **Step 4: Measure `FusedPoseTest`'s margin, not just its colour**

Green once is not evidence for an unseeded comparative assertion. Run the fused-versus-odometry-only
comparison **ten times in one JVM** against this implementation and record the distribution of the
margin — the gap between odometry-only error and fused error.

```bash
cd ../ignite-training-solutions/solutions/m5-auto-vision   # or wherever cross-check overlays it
export JAVA_HOME=~/wpilib/2026/jdk
./gradlew test --tests '*FusedPoseTest*' -i --console=plain 2>&1 | tail -40
```

Report the ten margins. What you are looking for:

- **A margin comfortably larger than its own spread** — fused wins every run, not eight of ten.
- If the smallest margin is within noise of the assertion threshold, say so plainly and stop. The
  fix is Task 4's threshold or run length, not this task's implementation, and the finding belongs
  in the report either way.

This is the M5-specific analogue of step 9's "an arm too light for its own kG test to fail." A
comparative test whose margin nobody measured is a test nobody knows the strength of.

- [ ] **Step 5: Write `answer-keys/t5-drifting-auto.md`**

**This file is the only place in either repository that records which letter carries which fault.**
Task 7 generated the assignment and deliberately did not write it down. Recover it the way
`logs/README.md` documents:

```bash
cd /Users/y0shi/workspace/ignite/docs/projects/testouts/t5-drifting-auto
export JAVA_HOME=~/wpilib/2026/jdk
for f in ODOMETRY_DRIFT MISTUNED_CONTROLLER BLOCKING_COMMAND; do
  ./gradlew generateLog -Pfault=$f -Pout=/tmp/t5-key/$f.wpilog --console=plain > /dev/null
  for letter in a b c; do
    cmp -s /tmp/t5-key/$f.wpilog logs/$letter-run.wpilog && echo "$f -> $letter-run.wpilog"
  done
done
rm -rf /tmp/t5-key
```

**That output goes in the answer key and nowhere else.** Not in this task's report, not in a commit
message in the labs repository, not in a terminal transcript you paste back to Josh.

The key must carry, per §11.1 and §10:

1. **The mapping**, from the command above.
2. **Per log: the signature that identifies the fault**, and — the part that matters —
   **the specific signals that rule out each other cause.** From design record §3:

| Fault | Its signature | What rules the other two out |
|---|---|---|
| Odometry drift | `estimatedPose` diverges from `truePose`; `pathTargetPose` tracks the *estimate* fine | controller error stays small → not a mistune; every marker in `/t5/events` fires on time → not a block |
| Mistuned path controller | large, persistent error between `pathTargetPose` and `estimatedPose` | `estimatedPose` matches `truePose` → not drift; steps still complete → not a block |
| Blocking named command | one step's duration runs long or never ends; later markers never fire | pose is accurate and controller error is small right up to the stall → neither of the other two |

Write this as prose a mentor can grade against, not as a table to hand over. **What a mentor is
listening for is the second column of that third column** — a student pointing at
`/t5/estimatedPose` versus `/t5/truePose` and saying what it does *not* show.

3. **What a weak pass sounds like.** Correct diagnosis, no ruling-out, is a fail per §10 and the
   milestone document. Give the mentor one sentence per fault of what a parroted answer looks like
   next to a reasoned one.

4. **The regeneration shortcut, flagged for mentors.** `00-plan.md`'s second known-limitation note
   records that a student with the fixture tree can recover the mapping in about three commands.
   That was accepted deliberately — the pass criterion is the ruling-out, and the shortcut does not
   supply it. The key must say so, and must tell a mentor what to do about it: if a student's
   diagnosis is confident but their ruling-out is thin, **ask them which signal excludes the other
   two before assuming they read the log.** Do not treat the shortcut as cheating; treat it as
   having answered a different question than the one asked.

- [ ] **Step 6: Write `authoring-notes/m5.md`**

Follow `m4.md`'s shape. It must carry, so a future maintainer does not "simplify" something
load-bearing:

- **`SimHooks.pauseTiming()` is not a style preference.** Spike G: paused timing plus
  `stepTiming(0.020)` yields 39 distinct camera frames identical across three runs; against a
  running wall clock `PhotonCameraSim` is FPS-limited and returns the same cached frame while the
  robot drives away from it. A test that drops the pause reads a frozen frame and passes or fails
  for reasons unrelated to the student. Record Task 2 Step 6's both-directions proof.
- **Why the drivetrain is a kinematic sim with no Phoenix in the loop** — step 9's measurement, and
  the fact that building a swerve drivetrain is explicitly not M5's lesson.
- **Why `AutoBuilder` follows `truePose()` and not the student's estimate**, and that this is a real
  divergence from `Robot-2026`, documented in three student-visible places on purpose. A maintainer
  who "corrects" it to follow the estimator breaks Part 1 for every student whose Part 2 is still a
  stub, and `AutoTerminationTest` starts failing for a reason that has nothing to do with autos.
- **Why `ScoringMechanism` exists at all.** It was missing from the first contract draft. Without
  it `AutoTerminationTest` degrades into "the routine finished within a budget," which proves
  nothing — PathPlanner's follow command is time-based and terminates on its own whether or not the
  robot moved. `setFaulted(true)` is the only thing in M5 that can be made to hang.
- **Why `VisionPoseHelper` is one class in `src/main` and not a copy in the test sources.** The day
  the two copies disagree, the given test measures something the robot does not do.
- **The distance threshold chosen in Step 2 and the reasoning**, explicitly labelled as *one*
  defensible answer rather than *the* answer.
- **`FusedPoseTest`'s measured margin** from Step 4, with the ten numbers. This is the single most
  useful thing a future maintainer can have when the sim stack moves under them.
- **Field layout is `k2026RebuiltWelded`.** The design record's spike used the 2025 field because
  that is what happened to load; anyone reading the spike must not copy it.

- [ ] **Step 7: Commit in the solutions repository**

```bash
cd ../ignite-training-solutions
git status --short
git add solutions/m5-auto-vision answer-keys/t5-drifting-auto.md authoring-notes/m5.md
git commit -m "Add M5 reference implementation, T5 answer key, and M5 authoring notes"
```

- [ ] **Step 8: Confirm nothing leaked into the labs repository**

```bash
cd /Users/y0shi/workspace/ignite/docs
git status --short          # MUST be empty
```

Then the two M5-specific leaks, both of which would ship the milestone already solved:

```bash
# 1. No implementation in the student's RobotContainer -- stubs only.
grep -c "UnsupportedOperationException" projects/m5-auto-vision/src/main/java/frc/robot/RobotContainer.java

# 2. No filtering threshold anywhere in the skeleton. This is the student's decision.
grep -rniE "trusted.?distance|max.?distance|MAX_TRUSTED" projects/m5-auto-vision/src/main/java/
```

The first must equal the number of `M5Deliverables` methods; the second must return nothing.
`CLAUDE.md`: *never commit a reference implementation to this repo.* This step is the last chance
to catch it.

Finally, confirm the answer key did not escape:

```bash
grep -rniE "a-run|b-run|c-run" projects/testouts/t5-drifting-auto/ --include=*.md
```

Every hit must be a neutral mention of the filename. **No line may pair a letter with a fault
name** — that is the one invariant Tasks 7 and 8 exist on either side of.
