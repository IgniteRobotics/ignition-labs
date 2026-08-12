# M6 — `projects/m6-testing/` + `UNTESTED-TARGETS.md` — design

**Date:** 2026-08-11
**Build-order step:** 8 (`teaching-repo-spec.md` §13)
**Governing sections:** spec §9.7 (M6), §6.5 (pure-logic hard rule), §12.2 (`tests.lock`),
§12.3 (clean-clone invariant), §16 (maintenance ownership)

M6 is the first milestone where the student writes **tests, not code**. That single
inversion breaks two mechanisms that have held since M0, and most of this document is
about how they are rebuilt rather than exempted.

---

## 1. What ships

```
projects/m6-testing/
  build.gradle                          GradleRIO 2026.2.1, Phoenix 6 — first CTRE project
  settings.gradle  gradlew  gradlew.bat  gradle/wrapper/
  vendordeps/
    WPILibNewCommands.json
    Phoenix6-26.1.1.json                copied from Robot-2026 @ 28ce539
  src/main/java/frc/robot/
    plant/LaunchProfile.java            GIVEN, IMPLEMENTED — the practice target
    plant/ShooterFlywheel.java          GIVEN, IMPLEMENTED, AutoCloseable, TalonFX
  src/test/java/frc/robot/
    LaunchProfileExampleTest.java       GIVEN worked example — passes, fully commented
    support/PhoenixTestSupport.java     GIVEN — the CTRE timing quirks
  regressions/
    README.md
    example-path.patch
    flip-sign.patch
    drop-gear-ratio.patch
  logs/
    <one real .wpilog, 0.6–3.2 MB>
    QUESTIONS.md
  README.md  READING.md

docs/programming/UNTESTED-TARGETS.md    published; named owner in the file
tools/regression_gate.py                stdlib-only, mirrors given_tests_red.py
tests/test_regression_gate.py           pytest coverage for the above
```

Scope is spec §9.7 exactly. No Epilogue exercise is added to the sandbox: M6 is fade **L0**,
and the `@Logged` DoD item is satisfied against the student's own work, mentor-verified.

**No `Robot.java`, no `Main.java`, no simulator GUI, no `simgui*.json`.** M6 is a testing
sandbox, not a robot — nothing in it is launched with `./gradlew simulateJava`, and its own
milestone document's first pitfall is *do not instantiate the whole robot*. Shipping a
`TimedRobot` in the project whose lesson is "don't construct one in a test" invites exactly
the mistake. `projects/m2-oop/build.gradle:35` is the precedent: GradleRIO applied for the
toolchain and the vendordep machinery, with `ROBOT_MAIN_CLASS` naming an ordinary class
purely to satisfy the jar manifest, and no `wpi.sim.addGui()`. M6 follows it.

**Package:** `frc.robot` (spec: M3 onward). **Vendordeps:** WPILibNewCommands + Phoenix 6,
minimum set per spec §9. `ShooterFlywheel` extends `SubsystemBase` — the DoD item says
*subsystem*, and WPILib's `AutoCloseable` teardown guidance is written about subsystems, so
matching the shape the student will meet in `Robot-2026` is worth the one vendordep.

---

## 2. The clean-clone inversion

Every project M1–M5 ships stubs that throw, so §12.3 holds: `assemble` green,
`compileTestJava` green, `test` **red**. `skeleton-integrity.yml`'s inverted check verifies
that red is real by reading the JUnit reports (`tools/given_tests_red.py`).

M6 cannot satisfy it. `LaunchProfile` ships **implemented** — a student writing tests needs
something correct to write them against — and the given worked-example test **passes**. On a
clean clone M6 is green, and the inverted check fires the moment it sees `src/test`.

**Resolution: the regression gate replaces the inverted check, keyed on the presence of
`regressions/`.** The workflow becomes a three-way branch:

```
if [ -d regressions/ ]; then      # M6: green, then provably red under a patch
  ./gradlew test                          # must PASS
  git apply regressions/example-path.patch
  ./gradlew test                          # must FAIL — and for the named test
  git apply -R regressions/example-path.patch
elif [ ! -d src/test ]; then      # M0: no given tests, nothing to invert
  skip
else                              # M1–M5: given tests must be red
  inverted check
fi
```

This keeps the property both existing branches were written for: **the skip is derived from
what the project contains, not from a list of project names.** An exemption list would have
been the smaller diff and is rejected for that reason — it is the failure mode this
repository keeps producing, where a check passes while the thing it protects is broken.

§12.3's guarantee is not weakened, it is restated for a milestone whose stubs are the
student's tests: *a skeleton must never be silently satisfiable.* For M1–M5 that means the
given tests fail. For M6 it means the given tests catch a planted regression.

---

## 3. Two-tier regressions

The regression check has two different jobs and they cannot share a patch.

- In **this repository**, CI must prove the mechanism works — which requires a patch the
  *given* worked-example test catches, because no student tests exist here.
- In the **student's fork**, CI must prove the student's tests catch something — which
  requires a patch the given worked example does **not** catch. Otherwise a student passes
  the gate having written nothing.

| Patch | Regresses | Caught by | Enforced in |
|---|---|---|---|
| `example-path.patch` | the one `LaunchProfile` method the worked example covers | the given test | `skeleton-integrity.yml` — proves the mechanism |
| `flip-sign.patch` | a method the worked example does **not** touch | student tests only | `student-check.yml` — the gate |
| `drop-gear-ratio.patch` | ditto | student tests only | `student-check.yml` |

`LaunchProfile`'s public surface is therefore partitioned deliberately: one method is
worked-example territory, the rest is student territory. `regressions/README.md` states
which tier each patch belongs to and why the split exists. **It never states the fix** — the
patch names say *what* was broken, never *what the correct value is*, per the authoring rule
that a stub never contains the answer.

This is the mechanism spec §9.7 calls "the best idea in this spec." A student can satisfy a
test-count check with three assertions on `true`. They cannot satisfy this one.

---

## 4. `tools/regression_gate.py`

The gate is a Python tool, not inline shell, for the reason `given_tests_red.py` already
exists: **a non-zero exit status is not evidence.** `git apply` exits non-zero when a patch
does not apply at all. `./gradlew test` exits non-zero on `UnsatisfiedLinkError`, on a
compile error, and on a build that never ran a test. Every one of those would read as
"the regression was caught" to a naive `if ! ./gradlew test`.

The gate:

1. Confirms the pre-patch run **passed**, with at least one test actually executed.
2. Applies the patch, failing loudly if `git apply` itself fails.
3. Confirms the post-patch run failed **from an assertion in a named test**, not from a
   link error, a compile error, or an empty suite.
4. Reverts, and confirms the tree is byte-identical to where it started.

Step 4 is not ceremony. A gate that leaves a patch applied poisons every later step in the
job, and the failure would surface somewhere unrelated.

Stdlib-only so the GitHub runner's preinstalled `python3` suffices — no `setup-python` step,
matching `given_tests_red.py`. Covered by `tests/test_regression_gate.py` in the existing
pytest suite.

---

## 5. `tests.lock`

`PATTERNS` already globs `src/test/**/*.java`, so the worked example and `PhoenixTestSupport`
are locked automatically. Two additions are required:

- **`plant/LaunchProfile.java` is given implementation.** It is already covered —
  `projects/*/src/main/java/**/plant/*.java` was pre-registered in `PATTERNS` for M4 and is
  unused until now. M6 is its first consumer. Without the lock a student edits the class the
  patches apply to, and the gate silently stops meaning anything.
- **`projects/*/regressions/*.patch` is new.** A patch edited into a no-op defeats the entire
  mechanism, and it is the single most attractive thing in the tree to edit.

`verify_manifest` only checks paths listed in the manifest, so **student-added test files do
not trip the lock** — which M6 depends on, since every student adds files under `src/test/`.
This is existing behaviour and is being relied on deliberately; it must not be "fixed" into
a whole-tree comparison.

---

## 6. Phoenix in CI

`student-check.yml` runs `./gradlew build`, and in the Gradle Java plugin `build` → `check` →
`test`. Every test an M6 student writes therefore executes on `ubuntu-latest`, including the
Phoenix one the DoD requires. This is not a choice the design made; it is a consequence of
existing CI, and it means a Phoenix problem lands on the student as a red PR they cannot
diagnose.

**Native resolution is not the risk.** `Phoenix6-26.1.1.json` ships `simProTalonFX`,
`api-cpp-sim`, and `tools-sim` for `linuxx86-64`. Verified against
`Robot-2026/vendordeps/Phoenix6-26.1.1.json` @ 28ce539.

**Flake is the risk.** CTRE's documented ~100 ms post-construction and ~20 ms post-request
figures are wall-clock, and a shared CI runner under contention is exactly where wall-clock
waits become intermittent. `m6-simulation-telemetry-testing.md` line 178 already names this
as a failure mode: intermittent failure is worse than consistent failure, because the
student assumes their code is flaky when their test is.

**Therefore `PhoenixTestSupport` implements the waits as `BaseStatusSignal.waitForAll(...)`
with a timeout, not `Thread.sleep`.** CTRE's own guide prefers signal waits, and they are
deterministic under contention where sleeps are not. The ~100 ms / ~20 ms figures still
appear — as comments explaining *why each wait exists*, which is the teaching content §9.7
asked for. Teaching the number without the mechanism is how a student learns to sprinkle
sleeps.

**Spike, first task:** run the Phoenix test 20× on `ubuntu-latest` and count flakes. If any
run flakes, the Phoenix test carries a JUnit tag excluded from the CI test task and the
simulated-device DoD item becomes mentor-verified locally. That fallback is stated now so
the spike has a defined outcome either way rather than becoming an open-ended investigation.

---

## 7. The log, and a correction owed to two documents

**`Robot-2026/.gitignore:177` ignores `logs/`. Zero logs are tracked.** The 274 log
directories are local to one machine. A student who forks and clones `Robot-2026` gets none.

Two documents assert otherwise and are wrong:

- `docs/programming/milestones/m6-simulation-telemetry-testing.md:74` — "Practice on real
  logs from last season — `Robot-2026/logs/` has hundreds."
- the same file's resources list, line 215 — "`Robot-2026/logs/` — hundreds of real logs for
  Part 3."

Spec §9.7's "one real `.wpilog` copied from `Robot-2026/logs/`" is a *sourcing* instruction
and stays correct — the copy is exactly what makes the log available.

**Resolution:** M6 ships its own log, and both claims are rewritten to point at it. Scanning
`Robot-2026/logs/` for files carrying `NT:/SmartDashboard/Launch/Flywheel Velocity (Rotations
Per Second)` finds usable candidates between 0.6 MB and 3.2 MB — small enough to commit
without bloating the repository. `Robot-2026` itself is not modified: it is a separate repo,
mid-season, and changing its `.gitignore` is outside this plan's business. The gap is
recorded here rather than silently absorbed.

`logs/` sits under `projects/`, so it is structurally unpublishable — MkDocs cannot read past
`docs_dir`.

### `QUESTIONS.md` is the real authoring cost

The diagnostic question must be answerable **and provable from the trace**. A question whose
answer requires a signal the log does not carry is unanswerable, and nobody discovers that
until a student is stuck in front of AdvantageScope. The candidate log must therefore be
parsed and the answer established *before* the question is written, not after. Spec §9.7's
pass criterion is the answer *with evidence*; the answer key in
`ignite-training-solutions/answer-keys/m6-log-diagnosis.md` records the evidence trail, not
just the conclusion.

---

## 8. `UNTESTED-TARGETS.md`

Lives at `docs/programming/UNTESTED-TARGETS.md` — published prose, alongside
`git-workflow.md` and `robot-repo-ladder.md`, which are likewise process documents students
read on the site. Students pick their M6 pull-request target from it.

Seed entries come from `Robot-2026` @ 28ce539, which has no `src/test` directory at all:
`statemachines/LaunchCalculator.java`, `subsystems/shooter/MappedLaunchRequestBuilder.java`,
`subsystems/shooter/ParabolicLaunchRequestBuilder.java`. Each entry names the class, the
pure-logic seam worth testing, and a claimed-by column.

Spec §16 flags this file and `FEATURE-BACKLOG.md` as having the same unassigned-owner
staleness risk. **The named owner goes in the file itself**, not in a side document, and the
file states the §6.5 pure-logic hard rule inline so a student choosing a target reads the
constraint at the moment of choosing rather than at review.

---

## 9. Lanes and sequencing

| Lane | Work | Concurrent with |
|---|---|---|
| `milestone` | spike → scaffold → plant + worked example → regressions → logs/QUESTIONS | — (sequential; one Gradle project) |
| `ci-tooling` | `regression_gate.py` + pytest, against synthetic fixtures | `milestone`, `docs` |
| `docs` | `UNTESTED-TARGETS.md` | `milestone`, `ci-tooling` |

**Maximum useful concurrency: 3.** There is **no test-out lane** — M6 has no test-out, by
design and by the most opinionated decision in the curriculum.

`ci-tooling` builds against synthetic fixtures precisely so it does not wait on the
milestone lane; the two are wired together in the tail.

**Serialized tail (`99-integration.md`), single owner, after all lanes land:** the
`skeleton-integrity.yml` three-way branch, the `student-check.yml` regression step,
`tests.lock` regeneration, the `docs/programming/README.md` status row, spec §13's status
column, and the two corrected log claims in
`m6-simulation-telemetry-testing.md`. `tests.lock`, that README, and that spec are the three
contended files named in `CLAUDE.md`; no other task touches them.

Step 12's remaining doc corrections — the §7.3 CI claim across three files, M6's "first
tests" wording, `curriculum-spec.md` §8 — stay in step 12. Only the two claims M6's own file
makes about logs are corrected here, because this plan owns that file.

**Solutions.** Per spec §13, from step 7 the solution ships with the milestone.
`ignite-training-solutions` receives `solutions/m6-testing/` (the three-plus sandbox tests,
including ones that catch the student-tier patches), `answer-keys/m6-log-diagnosis.md`, and
`authoring-notes/m6.md`. A milestone shipped without its solution is not shipped.

---

## 10. Verification

Beyond the standard clean-clone and cross-check discipline in `CLAUDE.md`:

- **The two-tier split is proven, not asserted.** Run the worked-example test against
  `flip-sign.patch` and `drop-gear-ratio.patch` and confirm it **passes** — if it catches
  either, the student gate is fake and the method partition is wrong.
- **The student tier is satisfiable.** The solutions-repo tests catch both student-tier
  patches. A gate no correct answer passes is as broken as one every wrong answer passes.
- **Every failure message the gate can emit is read.** `regression_gate.py` has more failure
  modes than any tool in `tools/` — patch-did-not-apply, empty suite, link error,
  tree-not-restored. A message that has never been read is a message that is wrong.
- **The revert is verified byte-for-byte**, not by exit code.
- **`QUESTIONS.md` is answered end to end from the shipped log before it ships.**
- **The Phoenix spike reports its actual flake count over 20 runs**, not a summary of what
  should have happened.
