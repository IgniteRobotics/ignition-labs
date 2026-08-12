# Task 9: Integration

The serialized tail. One owner, on the merged branch, after every lane has landed. Everything here is either a **contended file** — `tests.lock`, `docs/programming/README.md`, `docs/programming/teaching-repo-spec.md`, which every milestone plan rewrites — or a wiring change that only makes sense once all the pieces exist.

This is also where M6's central mechanism actually becomes enforced. Up to now the regression gate has been proven to work in Task 3 and unit-tested in Task 5; nothing has run it in CI. If this task is done wrong, M6 ships with a gate that never fires and nobody finds out.

**Files:**
- Modify: `.github/workflows/skeleton-integrity.yml`
- Modify: `.github/workflows/student-check.yml`
- Modify: `tools/tests_lock.py`, `tests/test_tests_lock.py`
- Modify: `.github/CODEOWNERS`
- Modify: `tests.lock` (regenerated)
- Modify: `docs/programming/milestones/m6-simulation-telemetry-testing.md`
- Modify: `docs/programming/README.md`
- Modify: `docs/programming/teaching-repo-spec.md`
- Modify: `mkdocs.yml` (nav entry for `UNTESTED-TARGETS.md`)

**Interfaces:**
- Consumes: everything. Tasks 2 (the worked example's fully-qualified test name), 3 (the three patches and their tiers), 5 (`python3 -m tools.regression_gate`), 6 (`UNTESTED-TARGETS.md`), 7 (the cross-check).
- Produces: build-order step 8, closed.

**Lane:** `integration`. Runs on the merged branch, not in any lane worktree.

---

- [ ] **Step 1: Rewrite the inverted check as a three-way branch**

In `.github/workflows/skeleton-integrity.yml`, replace the body of the step named `Inverted check — given tests MUST fail, and for the right reason` with the following. Keep the step's `working-directory: ${{ matrix.project }}` — the new branch changes directory explicitly where it needs to.

```bash
          # Three branches, all keyed on what the project CONTAINS rather than on a list of
          # project names. An exemption list is the smaller diff and is deliberately not
          # what this is: a name-keyed skip is the exact shape of "a check that passes while
          # the thing it protects is broken," which this repository keeps producing.
          #
          #   regressions/ present  -> M6: the suite is green, and must go provably red
          #                            under a planted patch. Spec §12.3's guarantee is that
          #                            a skeleton is never SILENTLY SATISFIABLE; for M1-M5
          #                            that means the given tests fail, for M6 it means the
          #                            given tests catch a regression.
          #   no src/test           -> M0: no given tests, nothing to invert.
          #   otherwise             -> M1-M5: the given tests must be red, for a real reason.
          if [ -d regressions ]; then
            echo "$(basename "$PWD") ships regressions/ — running the regression gate."
            cd "$GITHUB_WORKSPACE"
            # Example tier: the ONE given test must catch this, which is the only way this
            # repository can prove the mechanism works at all -- there are no student tests
            # here to catch anything.
            python3 -m tools.regression_gate "${{ matrix.project }}" \
              "${{ matrix.project }}/regressions/drop-gear-ratio.patch" \
              --expect "caught:frc.robot.WorkedExampleTest#ticksToMechanismRotations_appliesBothTicksPerRevolutionAndGearRatio"
            # Student tier: the given test must NOT catch these. If it does, a student
            # passes the gate in their own fork having written no tests at all -- and
            # nothing else in this pipeline would ever say so.
            for patch in "${{ matrix.project }}"/regressions/*.patch; do
              case "$(basename "$patch")" in
                drop-gear-ratio.patch) continue ;;
              esac
              python3 -m tools.regression_gate "${{ matrix.project }}" "$patch" \
                --expect no-failure
            done
            exit 0
          fi

          # A skeleton with no src/test has no given tests, so there is nothing to invert --
          # M0 is a complete working project by design (spec §9.1, fade N/A).
          if [ ! -d src/test ]; then
            echo "No src/test in ${{ matrix.project }} — no given tests to invert. Skipping."
            exit 0
          fi
```

Leave the existing M1–M5 body — the `set +e` / `./gradlew test` / `given_tests_red.py` block — below this, unchanged.

**The `no-failure` loop is the load-bearing half.** The `caught:` call proves the mechanism; the `no-failure` calls prove the student's gate is still real. Deleting the loop leaves a workflow that looks correct and enforces nothing.

`python3 -m` and `cd "$GITHUB_WORKSPACE"`, not `python3 tools/regression_gate.py`: the module imports `TOOLCHAIN_FAILURES` from `tools.given_tests_red`, which needs the repository root on `sys.path`, and the patches carry repository-relative paths.

- [ ] **Step 2: Add the student-tier step to `student-check.yml`**

In the `build` job, after `Build with Gradle` and before the formatting-comment steps, insert:

```yaml
      - name: Your tests must catch a real regression
        working-directory: ${{ github.workspace }}
        run: |
          # M6's definition-of-done item -- "you demonstrated a test failing on an
          # introduced regression" -- is the one most likely to be technically met and
          # substantively missed. A student can satisfy a test-count check with three
          # assertions on `true`; they cannot satisfy this one.
          #
          # Only the student-tier patches run here. drop-gear-ratio is caught by the given
          # worked example, so including it would pass for every student regardless of what
          # they wrote -- which is exactly the hole this check exists to close.
          if [ ! -d "${{ matrix.project }}/regressions" ]; then
            echo "${{ matrix.project }} ships no regressions/ — nothing to check."
            exit 0
          fi
          for patch in "${{ matrix.project }}"/regressions/*.patch; do
            case "$(basename "$patch")" in
              drop-gear-ratio.patch) continue ;;
            esac
            python3 -m tools.regression_gate "${{ matrix.project }}" "$patch" \
              --expect any-failure
          done
```

`student-check.yml`'s `build` job has no `setup-python` step. It does not need one — `regression_gate.py` is stdlib-only and the runner's preinstalled `python3` is enough, the same reasoning `given_tests_red.py` already relies on.

- [ ] **Step 3: Lock the patches**

In `tools/tests_lock.py`, add to `PATTERNS`:

```python
    "projects/*/regressions/*.patch",
```

A patch edited into a no-op defeats the entire gate, and it is the single most attractive file in the tree to edit. `LaunchProfile.java` is already covered — `projects/*/src/main/java/**/plant/*.java` was pre-registered for M4 and M6 is its first consumer.

Add coverage to `tests/test_tests_lock.py` mirroring the existing per-pattern tests: a `.patch` under `projects/x/regressions/` appears in the manifest, and modifying it makes `verify_manifest` report it.

```bash
poetry run pytest tests/test_tests_lock.py -v
```

- [ ] **Step 4: Add CODEOWNERS coverage**

In `.github/CODEOWNERS`, add alongside the existing `harness/` entry:

```
/projects/*/regressions/
```

- [ ] **Step 5: Regenerate `tests.lock` and prove it catches tampering**

```bash
cd /Users/y0shi/workspace/ignite/docs
poetry run python tools/tests_lock.py write
poetry run python tools/tests_lock.py verify      # expect rc=0
grep -c 'm6-testing' tests.lock                   # expect 7
```

**Seven entries:** `LaunchProfile.java` and `ShooterFlywheel.java` (both under `plant/`), `WorkedExampleTest.java` and `support/PhoenixTestSupport.java` (both under `src/test/`), and the three `.patch` files. A count other than seven means either a pattern is not matching or something that should not be locked is. Then prove the lock actually fires:

```bash
printf '\n' >> projects/m6-testing/regressions/flip-sign.patch
poetry run python tools/tests_lock.py verify; echo "rc=$?"    # expect rc=1, names the patch
git checkout -- projects/m6-testing/regressions/flip-sign.patch
poetry run python tools/tests_lock.py verify                  # expect rc=0
```

Paste the tamper message. A student who edits a patch must be told what happened in a sentence that invites disagreement rather than makes an accusation.

**Student-owned files must not appear.** M6 students add files under `src/test/`, and `verify_manifest` only checks paths listed in the manifest, so their new files are fine. What must never happen is a student's own test getting locked — confirm `tests.lock` names `WorkedExampleTest.java` and no other test file in `m6-testing`.

- [ ] **Step 6: Correct the two false log claims**

`Robot-2026/.gitignore:177` ignores `logs/`; zero logs are tracked. Both of these tell a student otherwise.

In `docs/programming/milestones/m6-simulation-telemetry-testing.md`, line ~74, replace:

> Practice on real logs from last season — `Robot-2026/logs/` has hundreds.

with:

> Practice on the real log shipped in `projects/m6-testing/logs/`. `Robot-2026` writes hundreds of logs a season, but they are gitignored and never committed — cloning it gets you none, which is worth knowing before you go looking.

And in the resources list, line ~215, replace the `Robot-2026/logs/` entry with:

> - **Ignite:** `projects/m6-testing/logs/` — a real match log and two diagnostic questions. Logs are not committed to `Robot-2026`; this one was copied out of a practice session

- [ ] **Step 7: Point the milestone doc at the project**

M3's file states its project in the first line under `## Project` (`m3-wpilib-command-based.md:253` — *"The project is `projects/m3-command-based/`. You are given …"*). Match that shape at the top of M6's `## Project` section, naming both artifacts, since M6 is the only milestone with two:

> The warm-up project is `projects/m6-testing/`. The deliverable is a pull request against `Robot-2026`, adding tests to a class from [`UNTESTED-TARGETS.md`](../UNTESTED-TARGETS.md) — pure logic only, per the hard rule stated there.

Also apply Task 2 Step 6's outcome: **if the Phoenix spike failed**, state in the "Project" section that the simulated-device test runs locally and is excluded from CI, so a student is not confused by a test that passes at home and does not appear in their pull request's checks.

- [ ] **Step 8: Update the two status documents**

`docs/programming/README.md` — the status row currently reads *"**M0–M3 built** … M4–M8 skeleton projects are not yet built."* Update it to name M6 as built, with `projects/m6-testing/` and `docs/programming/UNTESTED-TARGETS.md`, and to say M4, M5, M7, and M8 remain. Keep the Kickoff Gate sentence: M6 is deliberately out of numeric order and a reader needs to know the gate block is still M0–M3.

`docs/programming/teaching-repo-spec.md` §13 — set step 8's Status cell to `Done`. Update the solutions row's note to `M0–M3, M6, T1–T3 done; cross-check live`.

- [ ] **Step 9: Add `UNTESTED-TARGETS.md` to the site navigation**

`mkdocs.yml` uses an explicit `nav:`, so a file not listed there is built but unreachable. Add it to the `Programming Curriculum` block after `Real-Repo Ladder` — it is the same kind of document, a process reference read on the site rather than in a clone:

```yaml
      - Real-Repo Ladder: programming/robot-repo-ladder.md
      - Untested Targets: programming/UNTESTED-TARGETS.md
```

- [ ] **Step 10: Run every gate, and report actual output**

```bash
cd /Users/y0shi/workspace/ignite/docs
export JAVA_HOME=~/wpilib/2026/jdk

poetry run pytest                                          # whole suite
poetry run mkdocs build
poetry run python tools/site_guard.py _site                # zero files from projects/
poetry run python tools/tests_lock.py verify

for p in projects/m0-onboarding projects/m1-java-fundamentals projects/m2-oop \
         projects/m3-command-based projects/m6-testing \
         projects/testouts/t1-find-the-bug projects/testouts/t2-supplier-swap \
         projects/testouts/t3-scheduler-conflict; do
  echo "=== $p ==="
  (cd "$p" && ./gradlew assemble --console=plain >/dev/null 2>&1); echo "  assemble rc=$?"
  (cd "$p" && ./gradlew test --console=plain >/dev/null 2>&1);     echo "  test     rc=$?"
done
```

Expected: `assemble` rc=0 everywhere. `test` rc=1 for M1, M2, M3, T1, T2, T3 (red by design), **rc=0 for M0 and M6** (green by design, for different reasons).

`site_guard.py` matters more than usual this time: M6 ships a `.wpilog` and three `.patch` files, and none of them may reach the built site. They sit under `projects/`, outside `docs_dir`, so MkDocs structurally cannot read them — this step confirms that rather than assuming it.

- [ ] **Step 11: Prove the new CI branch fires — on a real pull request**

Everything above tests the pieces. This tests the wiring, and it is the only step that does.

Open the pull request for this branch and confirm in the Actions output:

1. `skeleton-integrity / integrity (projects/m6-testing)` ran the **regression gate**, not the inverted check, and logged the `caught:` result plus two `no-failure` results.
2. The same job for `projects/m3-command-based` still ran the **inverted check** — the new branch must not have swallowed M1–M5.
3. The same job for `projects/m0-onboarding` still **skipped** on the absent `src/test`.
4. `student-check` ran its regression step for `projects/m6-testing` and skipped it for every other changed project.

Paste the four log excerpts. A green check mark is not evidence of which branch executed, and this repository's recurring failure is precisely a check that goes green without doing its job.

- [ ] **Step 12: Confirm the gate can still fail, in CI, on this branch**

Push one throwaway commit that changes the worked example's fixture from `new LaunchProfile(2048.0, 4.0)` / expected `0.5` to `new LaunchProfile(2048.0, 1.0)` / expected `2.0`.

That combination is the interesting one: the test still **passes** on the unmodified tree, so the baseline stays green — but at a gear ratio of 1.0, dropping the `/ gearRatio` term changes nothing, so `drop-gear-ratio.patch` slips past it. Confirm `skeleton-integrity` goes **red** with the gate's *"the patch was applied and all test(s) still PASS"* message. That is precisely the failure mode a future edit to the worked example would introduce, and the only way to know the gate notices is to make it happen once. Then revert it.

```bash
git revert --no-edit HEAD
git push
```

A gate that has only ever been observed green is a gate nobody knows works. This is the CI-level counterpart of Task 3 Step 4, and it is the last chance to find out that the wiring is inert before a cohort depends on it.

- [ ] **Step 13: Final commit**

```bash
git status --short   # no reference implementation, no reference tests, no leftover throwaway commits
git add -A
git commit -m "Wire M6's regression gate into CI; close build-order step 8

skeleton-integrity's inverted check becomes a three-way branch keyed on
project contents: regressions/ runs the gate, no src/test skips, everything
else keeps the M1-M5 inverted check. student-check gains the student-tier
patches. tests.lock now covers regressions/*.patch, because a patch edited
into a no-op defeats the whole mechanism.

Corrects two false claims in m6-simulation-telemetry-testing.md: Robot-2026
gitignores logs/ and tracks none, so 'practice on real logs from
Robot-2026/logs' is untrue for anyone who clones it. M6 ships its own log.

Verified on this pull request that the gate branch actually fires, that M1-M5
still take the inverted check, and that the gate goes red when the worked
example stops catching its patch."
```
