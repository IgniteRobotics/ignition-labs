# Task 8: Reference solutions, answer key, authoring notes

Everything mentor-only that step 9 owes the companion repository. Spec §11.2 is blunt about the
stakes — *a milestone shipped without its solution is not shipped* — and §11.1 requires the T4
answer key to explain the **distractors**, not merely the answer, because a mentor running the
fixture needs to know what rules the wrong diagnoses out.

The risky part is not writing the implementation. It is that this is the first time anybody proves
M4's given tests are **satisfiable at all**. Every verification so far has proved the tests can
*fail*. A suite no correct answer passes is exactly as broken as one every wrong answer passes, and
until this task runs, nobody knows which kind M4 is.

**Files:**
- Create: `../ignite-training-solutions/solutions/m4-control/src/main/java/frc/robot/RobotContainer.java`
- Create: `../ignite-training-solutions/answer-keys/t4-bad-kv.md`
- Create: `../ignite-training-solutions/authoring-notes/m4.md`
- Test: `../ignite-training-solutions/tools/cross-check.sh`

**Consumes:** `M4Deliverables`, `FlywheelPlant`, `ArmPlant` (incl. `HOME_ANGLE_RADIANS`), `PlotHarness`, `Constants.SIM_STEP_SECONDS` — all pinned in `00-plan.md`. `BadlyTunedFlywheel` and `T4FlywheelPlant` for the answer key.
**Produces:** nothing this repository consumes. The cross-check job is the only consumer.
**Lane:** tail. Runs on the merged branch, after both lanes have committed — `cross-check.sh` archives the labs repo at a ref, so uncommitted work in a worktree is invisible to it.

---

- [ ] **Step 1: Confirm both lanes are merged and committed**

```bash
cd /Users/y0shi/workspace/ignite/docs
git log --oneline -1                       # expect the merge of both lanes
git status --short                         # expect empty
```

`cross-check.sh` extracts `HEAD` with `git archive`. A dirty tree or an unmerged lane means it
silently tests something other than what you think.

- [ ] **Step 2: Write `RobotContainer.java` implementing `M4Deliverables`**

The gains are **characterized, not guessed** — that is the milestone's own rule and the solution
must not violate it. Derive them from the plants' physical parameters (design record §3.1, §3.2):

| Gain | Source | Constraint |
|---|---|---|
| Flywheel kV | `2π / (Kv/G)` → `0.179 V/(rot/s)` | — |
| Flywheel kP | tuned | **must sit visibly below the `1.496` ceiling** — use `0.5`, which settles clean with no overshoot |
| Arm kG | `(3/2·g/L) / (G·Kt/(R·J))` → `1.99 V` | must be present; omitting it costs `11.96°` |
| Arm kP, kD | tuned | position loop, profiled |

Write the flywheel loop with `SimpleMotorFeedforward` + `PIDController`, the arm with
`ArmFeedforward` + `ProfiledPIDController`, and `homingCommand()` driving slowly negative until
`armPlant().atHomeLimit()` then calling `zeroHere()`.

- [ ] **Step 3: Prove the suite is satisfiable — the step this task exists for**

```bash
cd ../ignite-training-solutions && ./tools/cross-check.sh 2>&1 | tail -30
```

Expected: every overlaid milestone green, **including M4's three given tests**. Paste the output.

If M4 is red here, **do not adjust the tests to make the solution pass.** Report which assertion
failed and by how much. A tolerance moved to accommodate a solution is a tolerance that no longer
measures anything, and it is the precise failure mode `CLAUDE.md`'s verification section names.

- [ ] **Step 4: Write `answer-keys/t4-bad-kv.md`**

Must contain, per §11.1: the diagnosis; the characterization that produces it; the corrected
values; and **why each wrong path is wrong**, with the numbers from design record §6.2 —

| Path | Result | Why a mentor rules it out |
|---|---|---|
| Raise kP, leave kV | settles further short, `−1.82` at kP 1.7 | more proportional gain cannot close a gap feedforward left open; it only shrinks it while making the ring worse |
| Fix kV, leave kP at 1.5 | overshoot `+1.99`, settles on target | a kP sized to cover a bad model is oversized for a good one |
| Fix kV, retune kP to ~0.5 | clean | correct |

Also record the answers to the fixture's three Part 2 questions, since those are the actual pass
criterion and a mentor needs to know what a *good* answer sounds like versus a parroted one.

- [ ] **Step 5: Write `authoring-notes/m4.md`**

Follow `m6.md`'s shape. It must carry, so a future maintainer does not "simplify" something load-bearing:

- Why the control loop is Java and not on-device `Slot0` — the four spike experiments, with numbers.
- Why the plants are built from physical quantities and never `identifyVelocitySystem`.
- **Why the arm is `m=8, L=0.9, G=30`** and what the rejected `J=0.62, G=100` arm did: held horizontal on `0.257 V`, so a kG-omitted student passed the test that exists to catch them.
- The flywheel's `1.496` kP ceiling.
- Which trap each given test aims at.

- [ ] **Step 6: Commit in the solutions repository**

```bash
cd ../ignite-training-solutions
git status --short
git add solutions/m4-control answer-keys/t4-bad-kv.md authoring-notes/m4.md
git commit -m "Add M4 reference implementation, T4 answer key, and M4 authoring notes"
```

- [ ] **Step 7: Confirm nothing leaked into the labs repository**

```bash
cd /Users/y0shi/workspace/ignite/docs
git status --short          # MUST be empty
grep -rnE "kS|kV|kA|kG|kP|kD" projects/m4-control/src/main/java/frc/robot/Constants.java
```

The second command must return nothing. `CLAUDE.md`: *never commit a reference implementation to
this repo.* This step is the last chance to catch it.
