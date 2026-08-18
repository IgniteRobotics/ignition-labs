# Task 9: Integration

The serialized tail. Everything contended across lanes, plus the docs-alignment edit, plus the CI
gates run across the whole repository rather than one project.

**Single owner. Runs after both lanes have merged and after Task 8.** `plan-conventions.md` names
the three contended files and the reason: two agents finishing concurrently clobber each other on
them every time.

Step 10 has one shape of risk step 9 did not: **T5 is a Gradle project with no tests.** Every prior
test-out fixture had a red suite, and both the clean-clone invariant and `skeleton-integrity.yml`'s
inverted check are written around that. T5 takes a different branch of each, and getting that wrong
produces a green CI badge over a broken gate — the exact failure class this repository keeps
producing.

**Files:**
- Modify: `tests.lock`
- Modify: `docs/programming/milestones/m5-autonomous-odometry-vision.md`
- Modify: `docs/programming/README.md`, `docs/programming/teaching-repo-spec.md`
- Test: the full pytest suite, `mkdocs build --strict`, `site_guard.py`

**Consumes:** both lanes' committed output; Task 8's cross-check result.
**Produces:** a mergeable `feature/m5-and-t5`.
**Lane:** integration.

---

- [x] **Step 1: Merge both lanes**

```bash
cd /Users/y0shi/workspace/ignite/docs
git checkout feature/m5-and-t5
git merge lane/m5-milestone
git merge lane/t5-testout
git status --short          # expect empty
```

Conflicts here mean a lane touched a contended file. If one did, that is the bug — resolve toward
this task owning the file, and note which lane violated it.

- [x] **Step 2: Confirm the three logs came across as binary, and check what they cost**

```bash
git ls-files -s projects/testouts/t5-drifting-auto/logs/
du -h projects/testouts/t5-drifting-auto/logs/*.wpilog
python3 -c "
import pathlib
for p in sorted(pathlib.Path('projects/testouts/t5-drifting-auto/logs').glob('*.wpilog')):
    print(p.name, p.stat().st_size, p.read_bytes()[:6])
"
```

Every `.wpilog` must start with `WPILOG` and be non-trivial in size — a few hundred bytes means the
recording captured headers and nothing else, which no amount of downstream verification catches
because every check in Task 7 was a byte-comparison against an equally empty file. This is the first
step that looks at the logs as *data* rather than as bytes to compare.

Note the total added to the repository. If the three together exceed a few MB, say so in the report;
they are committed binaries in a repository every student clones, and §16 says they get regenerated
when the sim stack moves.

- [x] **Step 3: Regenerate `tests.lock`**

```bash
poetry run python tools/tests_lock.py write
poetry run python tools/tests_lock.py verify        # expect rc=0, "tests.lock OK"
git diff --stat tests.lock
```

Confirm the new entries are exactly:

| Expected | Path |
|---|---|
| M5's three given tests | `AutoTerminationTest`, `FusedPoseTest`, `BadTimestampTest` |
| M5's test support | `src/test/java/frc/robot/harness/SimDriver.java` |
| M5's plants | `plant/SwerveDriveSim.java`, `plant/VisionSim.java`, `plant/ScoringMechanism.java` |
| M5's harness | `harness/M5Deliverables.java`, `harness/VisionPoseHelper.java` |
| T5's plant | `frc/testout/t5/plant/T5DriveSim.java` |

**`RobotContainer.java` must not appear** — it is student territory, and locking it makes M5
unsolvable.

**`LogRecorder.java` must not appear either, and that is correct, not a miss.** It sits at the
package root (`frc/testout/t5/`), and `tools/tests_lock.py:PATTERNS` reaches `**/plant/*.java` and
`**/harness/*.java` only. `00-plan.md`'s file-structure table records the decision to leave it
there: T5 ships no tests, so there is no gate a tampered recorder could subvert. **Do not "fix"
this by adding a pattern** — a new glob at the package root would sweep in every future test-out's
main sources.

- [x] **Step 4: Prove the lock actually fails on tamper**

```bash
echo "// tamper" >> projects/m5-auto-vision/src/main/java/frc/robot/plant/VisionSim.java
poetry run python tools/tests_lock.py verify        # expect rc=1, naming VisionSim.java
git checkout projects/m5-auto-vision/src/main/java/frc/robot/plant/VisionSim.java
poetry run python tools/tests_lock.py verify        # expect rc=0 again
```

A lock nobody has seen fail is a lock nobody knows works. Paste both outcomes.

- [x] **Step 5: `CODEOWNERS` — confirm, expect no edit**

```bash
grep -nE "plant/|harness/|testouts/" .github/CODEOWNERS
```

M5's `plant/` and `harness/` are covered by the existing `/projects/*/src/main/java/**/` lines, and
everything under `projects/testouts/` is covered wholesale. Expect **no change**. If you find
yourself adding a line, say why in the report — step 9 already generalized these patterns, and a
new one means M5 put a given file somewhere no previous milestone did.

- [x] **Step 6: The docs-alignment edit** — `m5-autonomous-odometry-vision.md`

Four edits. The first two are the load-bearing ones.

1. **Project pointer**, at the head of the `## Project` section, matching M4's form exactly
   (`m4-sensors-actuators-control.md:154` — absolute GitHub URL, not a relative path, because
   `projects/` is outside `docs_dir` and a relative link fails `mkdocs build --strict`):

   > **The skeleton is [`projects/m5-auto-vision/`](https://github.com/IgniteRobotics/ignition-labs/tree/main/projects/m5-auto-vision).** A simulated swerve drivetrain, a PhotonVision `VisionSystemSim` with configurable noise and latency, and a scoring mechanism you can make fail are given. Composing the routine and fusing vision into the estimate is the milestone.

2. **The ground-truth divergence.** `00-plan.md`'s self-review requires this in three places —
   `SwerveDriveSim`'s class Javadoc (Task 2), the project `README.md` (Task 5), and here. One short
   paragraph, in these words or better, immediately after the pointer:

   > Path following in the training project runs against the drivetrain's **true** pose, not the pose estimator you write in Part 2. On a real robot — including `Robot-2026` — the follower runs against the estimate. The training project differs so that Part 1 works before Part 2 exists, and so a routine does not fail because your estimator is still a stub. If you notice the difference and it bothers you, that is the right instinct; do not copy this wiring into a season repository.

   A student who copies the wiring into a season repo has been misled by us, and this paragraph is
   the only thing standing between them and that. Precedent: M4's inert-configuration limitation
   was handled the same way, in the same place.

3. **Part 1 step 7** currently reads "Deliberately make one step fail. Confirm the routine still
   terminates." Name the seam so a student is not guessing at how: the given `ScoringMechanism` has
   `setFaulted(true)`, and that is the thing to make fail. Do **not** state what a correct timeout
   looks like.

4. **Part 2's `Constants` note.** If Part 2's prose implies a threshold constant exists, remove the
   implication. There is deliberately no `MAX_TRUSTED_DISTANCE_METERS` — the value is the student's
   to choose and defend, and the DoD is an explanation item, not a number.

**Leave the `## Test-out challenge` section alone.** It already describes T5 correctly, including
the ruling-out pass criterion. Re-read it to confirm, and change nothing if it holds.

- [x] **Step 7: Status rows**

- `docs/programming/README.md:133` — the built-milestones paragraph. Add M5 and T5; the sentence
  currently ends "M5, M7, and M8 skeleton projects are not yet built." That becomes M7 and M8.
- `docs/programming/teaching-repo-spec.md` §13 — step 10 → `Done`; update the solutions row
  (`M0–M4, M6, T1–T4 done`) to include M5 and T5.

**Do not** fix §9.5's stale `26.1.0`, **do not** pin §9.6's missing photonlib version, and **do
not** rename `t4-bad-kv`. All three are logged for the step 12 doc-correction pass — the first two
in design record §4. This task ships what step 10 owns; it does not quietly edit the governing
document's other claims.

- [x] **Step 8: Full gates**

```bash
export JAVA_HOME=~/wpilib/2026/jdk
poetry run pytest -q
poetry run mkdocs build --strict
poetry run python tools/site_guard.py _site
```

`site_guard` must report zero files from `projects/` — §14. In particular **no `.wpilog` may reach
`_site`**; they are the largest binaries this repository has ever published-adjacent, and the
structural guarantee is that `docs_dir` cannot see them. Paste all three.

- [x] **Step 9: Clean-clone invariant, from a real clone — and note that the two projects differ**

```bash
tmp=$(mktemp -d)
git clone -q --branch feature/m5-and-t5 . "$tmp/labs"
for p in projects/m5-auto-vision projects/testouts/t5-drifting-auto; do
  echo "=== $p ==="
  ( cd "$tmp/labs/$p"
    ./gradlew assemble -q        >/dev/null 2>&1; echo "assemble        rc=$?"
    ./gradlew compileTestJava -q >/dev/null 2>&1; echo "compileTestJava rc=$?"
    ./gradlew test -q            >/dev/null 2>&1; echo "test            rc=$?" )
done
rm -rf "$tmp"
```

| Project | Expected | Why |
|---|---|---|
| `m5-auto-vision` | `0 / 0 / 1` | the normal invariant — red from failing assertions in the three given tests |
| `t5-drifting-auto` | `0 / 0 / 0` | **it has no tests.** A green `test` here is correct, and the fixture is graded by conversation, not by a suite |

**The M5 red must come from failing assertions, not compile errors** — if `compileTestJava` is
anything but 0, stop. A Track A student cannot tell a compile error from a broken toolchain and it
costs them a session that was never their problem.

For T5, confirm the `0` on `test` is "no tests ran," not "tests ran and passed." A green suite in a
skeleton would mean the stubs are already satisfied.

```bash
grep -rn "src/test" projects/testouts/t5-drifting-auto/ 2>/dev/null; ls projects/testouts/t5-drifting-auto/src/
```

- [x] **Step 10: `skeleton-integrity.yml` reachability — the step T5 makes non-obvious**

M5 has `src/test` and no `regressions/`, so it takes the **inverted-check** branch: given tests must
fail unmodified. T5 has no `src/test`, so it takes the **skip** branch — the same branch
`m0-onboarding` takes, for the same stated reason.

Confirm both by reading the workflow's project discovery, not by assuming:

```bash
grep -nE "src/test|regressions|FormatMe|find .*projects" .github/workflows/skeleton-integrity.yml
```

Report which branch each of the two new projects lands in, and confirm neither lands in the
regression-gate branch by accident. **If T5 lands in the inverted-check branch it will fail the
workflow** — a project whose tests cannot fail because it has none — and that failure would arrive
on `main`, not here.

- [x] **Step 11: Commit**

```bash
git add -A
git status --short
git commit -m "Integrate M5 + T5: tests.lock, docs alignment, status rows"
```

- [x] **Step 12: Report, then stop**

Report actual output for steps 2, 3, 4, 8, 9, and 10. **Do not merge to `main` and do not push** —
that is Josh's call, and `finishing-a-development-branch` covers it.
