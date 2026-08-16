# Task 9: Integration

The serialized tail. Everything contended across lanes, plus the docs-alignment edit step 9 pulled
forward from step 12, plus the CI gates run across the whole repository rather than one project.

**Single owner. Runs after both lanes have merged and after Task 8.** `plan-conventions.md` names
the three contended files and the reason: two agents finishing concurrently clobber each other on
them every time.

**Files:**
- Modify: `tests.lock`, `.github/CODEOWNERS`
- Modify: `docs/programming/milestones/m4-sensors-actuators-control.md`
- Modify: `docs/programming/README.md`, `docs/programming/teaching-repo-spec.md`
- Test: the full pytest suite, `mkdocs build`, `site_guard.py`

**Consumes:** both lanes' committed output; Task 8's cross-check result.
**Produces:** a mergeable `feature/m4-and-t4`.
**Lane:** integration.

---

- [ ] **Step 1: Merge both lanes**

```bash
cd /Users/y0shi/workspace/ignite/docs
git checkout feature/m4-and-t4
git merge lane/m4-milestone
git merge lane/t4-testout
git status --short          # expect empty
```

Conflicts here mean a lane touched a contended file. If one did, that is the bug — resolve toward
this task owning the file, and note which lane violated it.

- [ ] **Step 2: Regenerate `tests.lock`**

```bash
poetry run python tools/tests_lock.py write
poetry run python tools/tests_lock.py verify        # expect rc=0, "tests.lock OK"
git diff --stat tests.lock
```

Confirm the new entries are exactly: M4's three given tests plus `SimDriver`, both M4 plants, both
M4 harness files, T4's `FlywheelTuningTest`, and `T4FlywheelPlant`. **`RobotContainer.java` must
not appear** — it is student territory. `BadlyTunedFlywheel.java` must not appear either; the
student edits it, and locking it would make the fixture unsolvable.

- [ ] **Step 3: Prove the lock actually fails on tamper**

```bash
echo "// tamper" >> projects/m4-control/src/main/java/frc/robot/plant/ArmPlant.java
poetry run python tools/tests_lock.py verify        # expect rc=1, naming ArmPlant.java
git checkout projects/m4-control/src/main/java/frc/robot/plant/ArmPlant.java
poetry run python tools/tests_lock.py verify        # expect rc=0 again
```

A lock nobody has seen fail is a lock nobody knows works. Paste both outcomes.

- [ ] **Step 4: `CODEOWNERS`**

Confirm `/projects/*/src/main/java/**/plant/` is covered. Add it if not — §12.2 names M4's `plant/`
package specifically as the thing that must require mentor review.

- [ ] **Step 5: The docs-alignment edit** — `m4-sensors-actuators-control.md`

Per design record §5, and Josh's explicit call that the docs be made to agree with the skeleton
rather than left contradicting it:

1. **Project section** — state that the control loop is written in Java against the given plants.
2. **CTRE Phoenix 6 specifics** — keep every word about config objects, `apply()` blocking, status
   signal batching and `.clone()`; that knowledge carries the stretch PR and M5. Add one short
   paragraph: M4's loop runs in Java, `ShooterSubsystem` closes the same loop on the device, and
   the training project differs because CTRE's simulated control loop and signal pipeline run on
   wall-clock time and cannot be stepped deterministically by a test.
3. **Test-out challenge** — add "and settles a little short of the target" to the symptom
   description (design record §6.1). Do **not** name the faulty term; the fixture is blind.
4. **Definition of done** — add the §6.7 required-pull-request item.
5. Add the project pointer to `projects/m4-control/`.

- [ ] **Step 6: Status rows**

- `docs/programming/README.md` — M4 and T4 built.
- `docs/programming/teaching-repo-spec.md` §13 — step 9 → Done; update the solutions row to include M4/T4.

**Do not** change §9.5's `26.1.0` → `26.1.1`, and **do not** rename `t4-bad-kv`. Both are logged in
design record §7 for the step 12 doc-correction pass. This task ships what step 9 owns; it does not
quietly edit the governing document's other claims.

- [ ] **Step 7: Full gates**

```bash
export JAVA_HOME=~/wpilib/2026/jdk
poetry run pytest -q
poetry run mkdocs build --strict
poetry run python tools/site_guard.py _site
```

`site_guard` must report zero files from `projects/` — §14. Paste all three.

- [ ] **Step 8: Clean-clone invariant on both new projects, from a real clone**

```bash
tmp=$(mktemp -d)
git clone -q --branch feature/m4-and-t4 . "$tmp/labs"
for p in projects/m4-control projects/testouts/t4-bad-kv; do
  echo "=== $p ==="
  ( cd "$tmp/labs/$p"
    ./gradlew assemble -q        >/dev/null 2>&1; echo "assemble        rc=$?"
    ./gradlew compileTestJava -q >/dev/null 2>&1; echo "compileTestJava rc=$?"
    ./gradlew test -q            >/dev/null 2>&1; echo "test            rc=$?" )
done
rm -rf "$tmp"
```

Expect `0 / 0 / 1` for both. **The red must come from failing assertions, not compile errors** —
if `compileTestJava` is anything but 0, stop; a Track A student cannot tell a compile error from a
broken toolchain and it costs them a session that was never their problem.

- [ ] **Step 9: `skeleton-integrity.yml` reachability**

M4 has `src/test` and no `regressions/`, so it takes the inverted-check branch — given tests must
fail unmodified. Confirm the workflow's project discovery picks up both new projects and that
neither lands in the regression-gate branch by accident.

- [ ] **Step 10: Commit**

```bash
git add -A
git status --short
git commit -m "Integrate M4 + T4: tests.lock, CODEOWNERS, docs alignment, status rows"
```

- [ ] **Step 11: Report, then stop**

Report actual output for steps 2, 3, 7 and 8. **Do not merge to `main` and do not push** — that is
Josh's call, and `finishing-a-development-branch` covers it.
