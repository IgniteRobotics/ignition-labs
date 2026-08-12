# Task 3: The two-tier regression patches

Spec §9.7 calls this mechanism the best idea in the spec, and it is the one thing that makes M6's definition-of-done item — *you demonstrated a test failing on an introduced regression* — verifiable rather than self-reported. A student can satisfy a test-count check with three assertions on `true`. They cannot satisfy this one.

**Two tiers, because one patch cannot do both jobs.** In this repository CI must prove the mechanism works, and the only test here is the given worked example — so it needs a patch the worked example catches. In the student's fork CI must prove the *student's* tests catch something — so it needs patches the worked example does **not** catch, or a student passes having written nothing.

**The risk is silent overlap.** If a student-tier patch happens to be caught by the worked example, the gate goes green for everyone forever and nothing announces it. Step 4 is the step that exists to catch that, and it is not optional.

**Files:**
- Create: `projects/m6-testing/regressions/README.md`
- Create: `projects/m6-testing/regressions/drop-gear-ratio.patch` — example tier
- Create: `projects/m6-testing/regressions/flip-sign.patch` — student tier
- Create: `projects/m6-testing/regressions/widen-tolerance.patch` — student tier

**Interfaces:**
- Consumes: Task 2's `LaunchProfile` at its committed content, and the territory partition — worked example covers `ticksToMechanismRotations`; student covers `timeOfFlightSeconds`, `lookAheadDistanceMeters`, `atSetpoint`.
- Produces: three patches that apply cleanly to `projects/m6-testing/` from the project directory, and the tier mapping Task 5's gate and Task 9's workflows are wired against:

  | Patch | Tier | Must be caught by |
  |---|---|---|
  | `drop-gear-ratio.patch` | example | `WorkedExampleTest#ticksToMechanismRotations_appliesBothTicksPerRevolutionAndGearRatio` |
  | `flip-sign.patch` | student | a student test on `lookAheadDistanceMeters` |
  | `widen-tolerance.patch` | student | a student test on `atSetpoint` |

**Lane:** `milestone`. Worktree `.worktrees/m6`.

---

- [ ] **Step 1: Generate `drop-gear-ratio.patch` (example tier)**

**Generate the patches with `git diff`; do not hand-write them.** A hand-written unified diff with a miscounted hunk header applies on your machine and fails in CI, and the failure surfaces as "the regression was caught" if anything downstream is reading exit statuses instead of test results.

```bash
cd /Users/y0shi/workspace/ignite/docs/projects/m6-testing
mkdir -p regressions
```

Edit `src/main/java/frc/robot/plant/LaunchProfile.java`, changing exactly one line in `ticksToMechanismRotations`:

```java
    return ticks / ticksPerRevolution / gearRatio;
```

to:

```java
    return ticks / ticksPerRevolution;
```

Then capture and revert:

```bash
git diff -- src/main/java/frc/robot/plant/LaunchProfile.java > regressions/drop-gear-ratio.patch
git checkout -- src/main/java/frc/robot/plant/LaunchProfile.java
git diff --quiet -- src/main/java/frc/robot/plant/LaunchProfile.java && echo "reverted clean"
```

The paths in the patch are repository-relative (`a/projects/m6-testing/src/...`), so it applies with `git apply -p3` from inside the project or plain `git apply` from the repository root. Task 5's gate runs from the repository root; Step 3 below verifies both.

- [ ] **Step 2: Generate the two student-tier patches**

`flip-sign.patch` — in `lookAheadDistanceMeters`, change:

```java
    return distanceMeters - closingSpeedMetersPerSecond * PHASE_DELAY_SECONDS;
```

to:

```java
    return distanceMeters + closingSpeedMetersPerSecond * PHASE_DELAY_SECONDS;
```

```bash
git diff -- src/main/java/frc/robot/plant/LaunchProfile.java > regressions/flip-sign.patch
git checkout -- src/main/java/frc/robot/plant/LaunchProfile.java
```

`widen-tolerance.patch` — change the constant:

```java
  private static final double VELOCITY_TOLERANCE_RPS = 0.5;
```

to:

```java
  private static final double VELOCITY_TOLERANCE_RPS = 50.0;
```

```bash
git diff -- src/main/java/frc/robot/plant/LaunchProfile.java > regressions/widen-tolerance.patch
git checkout -- src/main/java/frc/robot/plant/LaunchProfile.java
git status --short   # expect only the three untracked .patch files
```

`widen-tolerance` is the one that catches the student who wrote only the obvious case. `assertTrue(profile.atSetpoint(100.0, 100.0))` passes at a tolerance of 50 rps just as happily as at 0.5. Catching it requires a **negative** case — a measured velocity that is genuinely too far off — which is the "writing tests that never fail" failure mode the milestone document names, made concrete.

- [ ] **Step 3: Verify each patch applies and reverts, from both directories**

```bash
cd /Users/y0shi/workspace/ignite/docs
for p in drop-gear-ratio flip-sign widen-tolerance; do
  echo "=== $p ==="
  git apply --check projects/m6-testing/regressions/$p.patch && echo "  applies from repo root"
  git apply projects/m6-testing/regressions/$p.patch
  git apply -R projects/m6-testing/regressions/$p.patch
  git diff --quiet && echo "  reverts clean" || echo "  DIRTY AFTER REVERT — STOP"
done
```

Every patch must report both lines. A patch that does not revert clean poisons every later step in a CI job and fails somewhere unrelated to the real cause.

- [ ] **Step 4: The two-tier proof — the most important verification in this plan**

Run the **given worked example** against each patch. The example-tier patch must be caught; both student-tier patches must **not** be.

```bash
export JAVA_HOME=~/wpilib/2026/jdk
cd /Users/y0shi/workspace/ignite/docs

# Baseline: green.
(cd projects/m6-testing && ./gradlew test --console=plain); echo "baseline rc=$?"   # expect 0

for p in drop-gear-ratio flip-sign widen-tolerance; do
  git apply projects/m6-testing/regressions/$p.patch
  (cd projects/m6-testing && ./gradlew test --console=plain --rerun-tasks); echo "$p rc=$?"
  git apply -R projects/m6-testing/regressions/$p.patch
done
```

**Required results:**

| Patch | Expected `rc` | Meaning |
|---|---|---|
| `drop-gear-ratio` | **1** | The worked example caught it. CI can prove the mechanism here |
| `flip-sign` | **0** | The worked example did not catch it. The student's own test has to |
| `widen-tolerance` | **0** | Same |

**If `flip-sign` or `widen-tolerance` returns 1, stop.** The worked example is reaching into student territory and the fork gate is fake — a student would pass it having written nothing. Go back to Task 2 Step 7, find the leak, and re-run this step. Do not proceed by loosening the patch.

**If `drop-gear-ratio` returns 0, stop.** Nothing here proves the gate works, and CI would be enforcing an assertion no test can fail.

Paste the four `rc=` lines into your task report verbatim.

- [ ] **Step 5: Confirm no patch states the correct value**

A patch name says *what was broken*. It must never say what the right answer is — a student who reads the fix repairs the plant instead of testing it, and the exercise evaporates.

```bash
cd /Users/y0shi/workspace/ignite/docs/projects/m6-testing
grep -nE '^\+.*(should be|correct|actually|the real value|fix:)' regressions/*.patch
grep -rniE 'the answer|correct value|should equal|expected result' regressions/README.md
```

Expected: no output from either. The `+` lines in each patch are the *broken* code, and that is all they are.

- [ ] **Step 6: Write `regressions/README.md`**

Student-facing. It explains the mechanism and the two tiers honestly — hiding the tiers would not make the gate stronger, and a student who understands why the check exists is likelier to write a real test than one who thinks it is arbitrary.

Create `projects/m6-testing/regressions/README.md`:

```markdown
# Deliberate regressions

Part 1 step 4 of this milestone asks you to introduce a deliberate regression, watch your
test fail, fix it, and watch it pass. **That step is the point of the whole exercise.** A
test you have never seen fail is not yet known to work.

These patches make that one reproducible command instead of an improvisation.

## Running one

From this project's directory:

```bash
git apply regressions/flip-sign.patch
./gradlew test          # your tests should now FAIL
git apply -R regressions/flip-sign.patch
./gradlew test          # and pass again
```

If `./gradlew test` stays green with a patch applied, your tests are not testing the thing
that broke. That is the finding. It is not a failure — it is the exercise working.

## What each one breaks

| Patch | Breaks |
|---|---|
| `drop-gear-ratio.patch` | The tick-to-rotation conversion stops accounting for the gearbox |
| `flip-sign.patch` | The look-ahead correction moves the aim point the wrong way |
| `widen-tolerance.patch` | "At setpoint" starts accepting a flywheel that is nowhere near speed |

None of them tells you the correct value. Read `LaunchProfile.java` for that — it is given,
it is correct, and it is yours to test, not to change.

## Which ones CI checks

`drop-gear-ratio.patch` is caught by the worked example in `src/test/`, which is how this
repository's own CI proves the mechanism works at all.

**`flip-sign.patch` and `widen-tolerance.patch` are not caught by anything you were given.**
When you open your pull request, CI applies them and requires your tests to fail. That is
the gate, and the only way through it is a test that genuinely catches the break.

`widen-tolerance.patch` is the one that catches a test which can never fail. A tolerance of
50 rotations per second accepts `atSetpoint(100.0, 100.0)` exactly as happily as a tolerance
of 0.5 does. Catching it needs a case where the flywheel is genuinely too slow.
```

- [ ] **Step 7: Confirm the tree is clean and the suite is still green**

```bash
cd /Users/y0shi/workspace/ignite/docs
git status --short          # only the four new files under regressions/
git diff --quiet && echo "no stray plant edits"
export JAVA_HOME=~/wpilib/2026/jdk
(cd projects/m6-testing && ./gradlew test --console=plain); echo "rc=$?"   # expect 0
```

A leftover applied patch here would ship M6 with a broken plant and a red skeleton — the opposite of what this milestone is.

- [ ] **Step 8: Commit**

```bash
git add projects/m6-testing/regressions
git commit -m "Add M6's two-tier regression patches

drop-gear-ratio is caught by the given worked example, so this repository's CI
can prove the mechanism works where no student tests exist. flip-sign and
widen-tolerance are deliberately NOT caught by anything given, so a student
cannot pass the gate in their own fork having written nothing.

Verified: worked example rc=1 under drop-gear-ratio, rc=0 under both
student-tier patches."
```
