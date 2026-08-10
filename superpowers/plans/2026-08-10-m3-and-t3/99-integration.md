# Task 9: Integration — locking, ownership, documentation, and the CI gates

The serialized tail. Every file here is edited by *every* milestone plan, so it belongs to one owner running after both lanes have landed — two agents finishing concurrently would clobber each other on `tests.lock` and the status tables every single time. This task also carries one change no earlier milestone needed: `tools/tests_lock.py` must learn to lock given code that lives in `src/main/`, because `M3Deliverables` does.

**Files:**
- Modify: `tools/tests_lock.py` — `PATTERNS`
- Modify: `tests/test_tests_lock.py`
- Modify: `CODEOWNERS`
- Modify: `tests.lock` (regenerated)
- Modify: `docs/programming/milestones/m3-wpilib-command-based.md`
- Modify: `docs/programming/README.md`
- Modify: `docs/programming/teaching-repo-spec.md`

**Consumes:** every file Tasks 1–4 committed, on the merged feature branch. Specifically: `projects/m3-command-based/src/main/java/frc/robot/harness/M3Deliverables.java`, four test classes plus `DeliverablesFixture.java` under `projects/m3-command-based/src/test/java/frc/robot/`, and two files under `projects/testouts/t3-scheduler-conflict/src/test/java/frc/robot/`.

**Produces:** nothing. This is the end of the plan.

**Lane:** integration. Single owner, on the merged branch — not in either lane's worktree.

---

- [ ] **Step 1: Teach `tests_lock.py` about `harness/`**

`M3Deliverables` is given, is load-bearing, and lives in `src/main/` because `RobotContainer` has to implement it. A student who edits it can make the contract mean whatever they like. The existing manifest covers `src/test/` and M4's `plant/`; add the third case.

In `tools/tests_lock.py`:

```diff
 PATTERNS = [
     "projects/*/src/test/**/*.java",
     "projects/*/src/main/java/**/plant/*.java",
+    "projects/*/src/main/java/**/harness/*.java",
     "projects/testouts/*/src/test/**/*.java",
 ]
```

Then in `tests/test_tests_lock.py`, stop keeping a second copy of the patterns — a duplicate that drifts is how a pattern gets added to the tool and silently goes untested. Replace the module-level constant with the real one and extend the fixture:

```diff
-from tools.tests_lock import compute_manifest, verify_manifest, write_manifest
-
-PATTERNS = ["projects/*/src/test/**/*.java", "projects/*/src/main/java/**/plant/*.java"]
+from tools.tests_lock import PATTERNS, compute_manifest, verify_manifest, write_manifest
```

```diff
     plant = tmp_path / "projects" / "m4" / "src" / "main" / "java" / "plant"
     plant.mkdir(parents=True)
     (plant / "FlywheelPlant.java").write_text("class FlywheelPlant {}", encoding="utf-8")
+    harness = tmp_path / "projects" / "m3" / "src" / "main" / "java" / "frc" / "robot" / "harness"
+    harness.mkdir(parents=True)
+    (harness / "M3Deliverables.java").write_text("interface M3Deliverables {}", encoding="utf-8")
     return tmp_path
```

```diff
 def test_manifest_covers_every_matched_file(project):
     manifest = compute_manifest(project, PATTERNS)
     assert set(manifest) == {
         "projects/m1/src/test/java/DeadbandTest.java",
         "projects/m4/src/main/java/plant/FlywheelPlant.java",
+        "projects/m3/src/main/java/frc/robot/harness/M3Deliverables.java",
     }
```

Then add one case, because "the tool locks it" and "editing it is caught" are different claims:

```python
def test_modified_harness_interface_is_reported(project, tmp_path):
    lock = tmp_path / "tests.lock"
    write_manifest(project, PATTERNS, lock)
    target = project / "projects/m3/src/main/java/frc/robot/harness/M3Deliverables.java"
    target.write_text("interface M3Deliverables { /* removed a method */ }", encoding="utf-8")
    assert verify_manifest(project, lock) == [
        "projects/m3/src/main/java/frc/robot/harness/M3Deliverables.java"
    ]
```

```bash
cd /Users/y0shi/workspace/ignite/docs
poetry run pytest tests/test_tests_lock.py -v
```

Expected: green, seven cases. Paste the output.

- [ ] **Step 2: Give `harness/` an owner**

In `CODEOWNERS`, immediately after the `plant/` line so the two given-main-source rules sit together:

```diff
 /projects/*/src/main/java/**/plant/   @IgniteRobotics/mentors
+/projects/*/src/main/java/**/harness/ @IgniteRobotics/mentors
```

The file's existing header already says the `@IgniteRobotics/mentors` team must exist for any of this to bind — do not repeat that, and do not add a second note.

- [ ] **Step 3: Regenerate and prove `tests.lock`**

```bash
cd /Users/y0shi/workspace/ignite/docs
poetry run python tools/tests_lock.py write
git diff --stat tests.lock
cat tests.lock
```

Expected: the fourteen existing lines plus **eight** new ones —

```
projects/m3-command-based/src/main/java/frc/robot/harness/M3Deliverables.java
projects/m3-command-based/src/test/java/frc/robot/CommandContractTest.java
projects/m3-command-based/src/test/java/frc/robot/CompositionTest.java
projects/m3-command-based/src/test/java/frc/robot/DefaultCommandTest.java
projects/m3-command-based/src/test/java/frc/robot/SchedulerInterruptionTest.java
projects/m3-command-based/src/test/java/frc/robot/harness/DeliverablesFixture.java
projects/testouts/t3-scheduler-conflict/src/test/java/frc/robot/CombinedCommandTest.java
projects/testouts/t3-scheduler-conflict/src/test/java/frc/robot/harness/SchedulerFixture.java
```

Twenty-two lines total. Read the file rather than trusting the count: both fixtures and the interface must be present — a student who edits `DeliverablesFixture` can make every contract test pass against anything — and **no** `Main.java`, `Robot.java`, `RobotContainer.java`, or `Constants.java` may appear. `RobotContainer.java` in particular is the student's file in M3 and the student's file in T3; if the manifest lists it, the pattern is over-matching and every student's first commit fails CI.

Then prove tampering is caught in the new files specifically:

```bash
poetry run python tools/tests_lock.py verify   # expect: tests.lock OK, rc 0

sed -i '' 's/schedulingYourCommandInterruptsWhateverWasAlreadyUsingItsSubsystem/disabled/' \
  projects/m3-command-based/src/test/java/frc/robot/SchedulerInterruptionTest.java
poetry run python tools/tests_lock.py verify; echo "rc=$?"

git checkout projects/m3-command-based/src/test/java/frc/robot/SchedulerInterruptionTest.java

sed -i '' 's/Subsystem mechanism();/Subsystem mechanism(); \/\/ tampered/' \
  projects/m3-command-based/src/main/java/frc/robot/harness/M3Deliverables.java
poetry run python tools/tests_lock.py verify; echo "rc=$?"

git checkout projects/m3-command-based/src/main/java/frc/robot/harness/M3Deliverables.java
poetry run python tools/tests_lock.py verify   # expect: tests.lock OK again
```

Both tampered runs must exit 1, name the file, and print the tamper message. The second is the one that matters — it is the first time this repository has locked a file outside `src/test/`, and if the new pattern is wrong it will pass here silently. Record both outputs.

- [ ] **Step 4: Point the milestone document at the skeleton**

In `docs/programming/milestones/m3-wpilib-command-based.md`, in the `## Project` section, insert immediately after the `**Then build a two-subsystem robot in simulation.**` line:

```markdown
The project is `projects/m3-command-based/`. You are given the whole robot except
the lesson — `Main`, a thin `Robot` with the scheduler running, a `Constants`
shape, the controller, a seeded simulator config, and a test harness. The
subsystems, the commands, the composition, the bindings, and the default command
are yours. Its README walks the order, and its `READING.md` is the `Phoenix-2026`
assignment above. The git commands are in [the git workflow](../git-workflow.md);
this milestone does not repeat them.
```

In the `## Test-out challenge` section, append after the `**Time:** 60 minutes.` line:

```markdown
The fixture is `projects/testouts/t3-scheduler-conflict/`.
```

Project paths stay in backticks rather than becoming links: `projects/` is outside `docs_dir` and a link to it would be a dead link in the published site. This matches what M1's and M2's milestone documents already do.

Change no other line. Spec §14 requires every DoD item to stay covered and the milestone files to stay otherwise unchanged.

- [ ] **Step 5: Update the two status tables**

`docs/programming/README.md`, the "Teaching repository for M0–M3 projects" row. It currently reads **M0, M1, and M2 built** and ends "M3–M8 skeleton projects are not yet built; they arrive in later plans". Rewrite that cell to name M3 and its fixture as built and change the tail to M4–M8, keeping the row's existing sentence shape:

```markdown
| Teaching repository for M0–M3 projects | **M0–M3 built** — `projects/m0-onboarding/` builds from a clean clone and its simulator starts, and `projects/m1-java-fundamentals/`, `projects/m2-oop/`, and `projects/m3-command-based/` with their test-out fixtures `projects/testouts/t1-find-the-bug/`, `t2-supplier-swap/`, and `t3-scheduler-conflict/` assemble green and test red, alongside the process docs, verification tooling, four CI workflows, and the Pages pipeline already in this repository. **The Kickoff Gate block (M0–M3) is complete.** M4–M8 skeleton projects are not yet built; they arrive in later plans |
```

`docs/programming/teaching-repo-spec.md` §13:

```diff
-| **7** | **M3** + T3 | Largest milestone. The `M3Deliverables` harness is the risky part — build it first and validate against a throwaway solution | — |
+| **7** | **M3** + T3 | Largest milestone. The `M3Deliverables` harness is the risky part — build it first and validate against a throwaway solution | Done |
```

And the solutions row at the bottom of the same table, whose status cell reads `M0–M2, T1–T2 done; cross-check live`:

```diff
-| **—** | Solutions repository, built continuously alongside steps 4–11, with the §11.2 cross-check job live from step 4 | Not a final step. A milestone shipped without its solution is not shipped. Backfilled late: steps 4–6 shipped without solutions, and each plan's verification implementation was written and then deleted rather than kept | M0–M2, T1–T2 done; cross-check live |
+| **—** | Solutions repository, built continuously alongside steps 4–11, with the §11.2 cross-check job live from step 4 | Not a final step. A milestone shipped without its solution is not shipped. Backfilled late: steps 4–6 shipped without solutions, and each plan's verification implementation was written and then deleted rather than kept — from step 7 the solution ships with the milestone | M0–M3, T1–T3 done; cross-check live |
```

Only make that second edit if Task 5 actually committed and its cross-check was green. If it was not, leave the row alone and say so in the task report — a status table claiming a solution that does not exist is worse than one that is behind.

- [ ] **Step 6: Run every gate `skeleton-integrity.yml` runs, locally**

```bash
cd /Users/y0shi/workspace/ignite/docs
export JAVA_HOME=~/wpilib/2026/jdk

# The matrix step: both new projects must be discovered by the workflow's own find.
find projects -mindepth 2 -maxdepth 3 -name settings.gradle | sort

for p in projects/m0-onboarding projects/m1-java-fundamentals projects/m2-oop \
         projects/m3-command-based projects/testouts/t1-find-the-bug \
         projects/testouts/t2-supplier-swap projects/testouts/t3-scheduler-conflict; do
  echo "=== $p ==="
  ( cd "$p"
    ./gradlew spotlessCheck   --console=plain > /dev/null 2>&1; echo "  spotlessCheck   rc=$?"
    ./gradlew assemble        --console=plain > /dev/null 2>&1; echo "  assemble        rc=$?"
    ./gradlew compileTestJava --console=plain > /dev/null 2>&1; echo "  compileTestJava rc=$?"
    ./gradlew test            --console=plain > /dev/null 2>&1; echo "  test            rc=$?"
  )
done
```

Expected, and check each line rather than skimming:

- `find` lists seven projects. `projects/m3-command-based` is at depth 2 and `projects/testouts/t3-scheduler-conflict` at depth 3 — both inside the workflow's `-mindepth 2 -maxdepth 3` window.
- `spotlessCheck` rc=0 everywhere except `m0-onboarding`, which is rc=1 by design (`FormatMe.java`, spec §9.1) and is exempted by filename in the workflow.
- `assemble` and `compileTestJava` rc=0 everywhere.
- `test` rc=1 everywhere except `m0-onboarding`, which has no `src/test` and is skipped by the inverted check.

**CI runs this on `ubuntu-latest` with a stock Temurin JDK and no WPILib install**, and M3 is the first project whose tests need desktop HAL natives. That path has not been exercised until now. Watch the first CI run on the pull request specifically for `UnsatisfiedLinkError` in `m3-command-based` and `t3-scheduler-conflict`; a green local run on macOS is evidence, not proof, and this is the single most likely thing in this plan to behave differently there.

- [ ] **Step 7: Build the site and check nothing leaked**

```bash
cd /Users/y0shi/workspace/ignite/docs
poetry run mkdocs build 2>&1 | tail -5
poetry run python tools/site_guard.py _site
```

The guard must report zero leaked files — nothing from `projects/` may appear in `_site/` (spec §14). M3 is the first milestone whose project directory contains student-facing prose (`README.md`, `READING.md`) that looks publishable, so this check is doing more work than it was for M1 and M2. Read its output.

- [ ] **Step 8: Run the Python suite**

```bash
cd /Users/y0shi/workspace/ignite/docs
poetry run pytest
```

Expected: green. `tools/tests_lock.py` changed in this task, so this is not a formality.

- [ ] **Step 9: Commit**

```bash
cd /Users/y0shi/workspace/ignite/docs
git status --short
git add tools/tests_lock.py tests/test_tests_lock.py CODEOWNERS tests.lock \
        docs/programming/milestones/m3-wpilib-command-based.md \
        docs/programming/README.md docs/programming/teaching-repo-spec.md
git commit -m "Lock M3 and T3 given files, cross-link docs, close build-order step 7"
```

`git status --short` must be clean afterwards apart from untracked build output. Confirm one last time that neither project ships solved:

```bash
git ls-files projects/m3-command-based | grep -E 'subsystems/|commands/' || echo "clean — no student sources committed"
git show HEAD:projects/testouts/t3-scheduler-conflict/src/main/java/frc/robot/RobotContainer.java \
  | grep -A2 'combinedCommand'
```

The first must print `clean`. The second must show the method throwing `UnsupportedOperationException`.
