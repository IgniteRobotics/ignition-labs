# M6 Testing Sandbox + `UNTESTED-TARGETS.md` Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans. Steps use checkbox (`- [ ]`) syntax for tracking.
>
> This is a **split plan** per [`superpowers/plan-conventions.md`](../../plan-conventions.md). Read this file plus the one task file you were handed. Do not read sibling task files — each is self-sufficient, and reading them all is the cost this layout exists to remove.

**Goal:** Ship `projects/m6-testing/` (an L0 warm-up sandbox where the student writes every test against given, working code), `docs/programming/UNTESTED-TARGETS.md` (the published list a student picks their real `Robot-2026` pull-request target from), and the two-tier regression gate that makes "you have written a test that caught a real regression" a thing CI can actually verify. This is build-order step 8 (spec §13) — the first step after the Kickoff Gate block, and the gate to M7.

**Architecture:** One standalone GradleRIO project, the first in this repository with a CTRE vendordep. M6 inverts the model every earlier milestone used: the student writes **tests, not code**, so `LaunchProfile` ships implemented and the given worked-example test **passes**. Three mechanisms carry the design:

1. **The regression gate replaces the inverted check**, keyed on the presence of `regressions/`. §12.3's real guarantee — *a skeleton must never be silently satisfiable* — means "given tests fail" for M1–M5 and "given tests catch a planted regression" for M6. The branch is derived from what the project contains, never from a list of project names.
2. **Two tiers of regression patch.** One patch the given worked example catches, so CI can prove the mechanism *in this repository* where no student tests exist. Two patches the worked example deliberately does **not** catch, so a student cannot pass the gate in their own fork having written nothing. The split is a deliberate partition of `LaunchProfile`'s public surface.
3. **`tools/regression_gate.py`.** A non-zero exit status is not evidence: `git apply` exits non-zero when a patch does not apply, and `./gradlew test` exits non-zero on a link error, a compile error, and an empty suite. The gate reads the JUnit XML and confirms a *named* test went from passing to failing, then verifies the revert byte-for-byte.

**Tech Stack:** Java 17, GradleRIO 2026.2.1, Gradle 8.11, Spotless 8.2.1 (`googleJavaFormat 1.19.2`), JUnit Jupiter 5.10.1, WPILibNewCommands 1.0.0 (frcYear 2026), Phoenix 6 `26.1.1`, Python 3.11 stdlib for the gate.

**Design record:** [`superpowers/specs/2026-08-11-m6-testing-design.md`](../../specs/2026-08-11-m6-testing-design.md).

---

## Constraints delta

`CLAUDE.md` at the repository root loads automatically and already carries the toolchain, pinned versions, Spotless wiring, package naming, the fade curve, the authoring rules, the clean-clone invariant, and the verification discipline. **Read it. It is not repeated here.** Only what is specific to M6 appears below.

- **Fade level L0 — worked example only** (spec §8). Exactly **one** given test. The student writes every other test in the sandbox and every test in their `Robot-2026` pull request. A student given as much help at M6 as at M1 has been failed by the scaffolding: do not add a second given test, a test template, or a "fill in the assertion" stub.
- **M6 is green on a clean clone, and that is correct.** `assemble` green, `compileTestJava` green, `test` **green**. This is the one milestone where §12.3's red does not apply, because the student's deliverable *is* the red. Do not "fix" this by adding a throwing stub. The regression gate is what replaces the guarantee.
- **`LaunchProfile` ships implemented, correct, and locked.** It lives in `frc.robot.plant`, which `tools/tests_lock.py:PATTERNS` already globs — the pattern was pre-registered for M4 and M6 is its first consumer. The regression patches apply to this file; a student who edits it silently disables the gate.
- **No `Robot.java`, no `Main.java`, no simulator GUI, no `simgui*.json`.** M6 is a testing sandbox, not a robot. Nothing in it is launched with `./gradlew simulateJava`, and the milestone's own first pitfall is *do not instantiate the whole robot*. `projects/m2-oop/build.gradle:35` is the precedent: GradleRIO applied for the toolchain and vendordep machinery, `ROBOT_MAIN_CLASS` naming an ordinary class purely for the jar manifest, no `wpi.sim.addGui()`.
- **Vendordeps: `WPILibNewCommands` + `Phoenix6-26.1.1` only** (spec §9). No PathPlanner, no photonlib. Copy `Phoenix6-26.1.1.json` verbatim from `Robot-2026/vendordeps/` @ `28ce5397d88275001d656dbb717d22cf9ac0150d`.
- **Phoenix waits are signal waits, not sleeps.** `PhoenixTestSupport` uses `BaseStatusSignal.waitForAll(timeout, ...)`. The documented ~100 ms and ~20 ms figures appear as comments explaining *why each wait exists* — teaching the number without the mechanism is how a student learns to sprinkle `Thread.sleep`. Task 1's spike decides whether this holds; if it does not, the fallback is stated there.
- **`regressions/*.patch` never states the fix.** A patch name says what was broken, never what the correct value is. `regressions/README.md` explains the two tiers and why they exist; it does not explain how to catch either patch.
- **Pinned SHA.** `Robot-2026` → `28ce5397d88275001d656dbb717d22cf9ac0150d` (2026-06-09), the same SHA M2's and M3's `READING.md` already use. Every student-facing reference to it is pinned.
- **`Robot-2026` is not modified by this plan.** Its `.gitignore:177` ignores `logs/`, which is why M6 ships its own log — see Task 4. It is a separate repository, mid-season, and changing it is outside this plan's business.
- **`tools/`, `tests/`, `.github/workflows/`, `.github/CODEOWNERS`, and `tests.lock` belong to Tasks 5 and 9 only.** No other task may edit them.

---

## File structure

**Created — `projects/m6-testing/`**

| Path | Responsibility |
|---|---|
| `settings.gradle`, `gradlew`, `gradlew.bat`, `gradle/wrapper/`, `.gitignore`, `WPILib-License.md`, `.wpilib/wpilib_preferences.json` | Copied from M3 unchanged |
| `build.gradle` | M2's shape — GradleRIO without a robot — plus the Phoenix vendordep and `ROBOT_MAIN_CLASS = "frc.robot.plant.LaunchProfile"` |
| `vendordeps/WPILibNewCommands.json` | From the WPILib install |
| `vendordeps/Phoenix6-26.1.1.json` | From `Robot-2026` @ `28ce539`. The first CTRE dep in this repository |
| `src/main/java/frc/robot/plant/LaunchProfile.java` | **Given, implemented, locked.** Four public methods, partitioned into worked-example and student territory |
| `src/main/java/frc/robot/plant/ShooterFlywheel.java` | **Given, implemented, locked.** `SubsystemBase implements AutoCloseable`, one `TalonFX` |
| `src/test/java/frc/robot/WorkedExampleTest.java` | **Given, locked.** The one worked example. Two `@Test` methods — `ticksToMechanismRotations` and the `AutoCloseable` demonstration — and nothing else on `LaunchProfile` |
| `src/test/java/frc/robot/support/PhoenixTestSupport.java` | **Given, locked.** The CTRE timing quirks, one comment per wait |
| `regressions/README.md` | The two tiers, and why. Never the fix |
| `regressions/drop-gear-ratio.patch` | Example tier — caught by the worked example |
| `regressions/flip-sign.patch` | Student tier |
| `regressions/widen-tolerance.patch` | Student tier |
| `logs/<name>.wpilog` | One real log, 0.6–3.2 MB, carrying the flywheel signals |
| `logs/QUESTIONS.md` | The diagnostic questions, answerable and provable from the shipped log |
| `README.md` | Student-facing: what green means here, the three parts, how to run the regression check locally |
| `READING.md` | `Robot-2026`'s `LaunchCalculator` and the two `LaunchRequestBuilder`s, pinned |

**Created — elsewhere**

| Path | Responsibility |
|---|---|
| `docs/programming/UNTESTED-TARGETS.md` | Published. Seed targets from `Robot-2026` @ `28ce539`, the §6.5 hard rule inline, a named owner, a claimed-by column |
| `tools/regression_gate.py` | Stdlib-only. Mirrors `given_tests_red.py`'s shape: pure functions plus a `verdict` |
| `tests/test_regression_gate.py` | Pytest coverage, against synthetic JUnit XML |

**Created — `../ignite-training-solutions/`** (separate repository, mentor-only)

| Path | Responsibility |
|---|---|
| `solutions/m6-testing/src/test/java/frc/robot/**` | Reference student tests: three-plus on `LaunchProfile`, one Phoenix test, both catching their student-tier patch |
| `answer-keys/m6-log-diagnosis.md` | The answer **and the evidence trail**, per §9.7's pass criterion |
| `authoring-notes/m6.md` | The two-tier partition, and which trap each patch aims at |

**Modified** (Tasks 5 and 9 only)

| Path | Change | Task |
|---|---|---|
| `tools/regression_gate.py`, `tests/test_regression_gate.py` | Created | 5 |
| `tools/tests_lock.py` | `PATTERNS` gains `projects/*/regressions/*.patch` | 9 |
| `tests/test_tests_lock.py` | Coverage for the new pattern | 9 |
| `.github/workflows/skeleton-integrity.yml` | Inverted check becomes the three-way branch | 9 |
| `.github/workflows/student-check.yml` | Gains the student-tier regression step | 9 |
| `.github/CODEOWNERS` | Gains `/projects/*/regressions/` | 9 |
| `tests.lock` | Regenerated | 9 |
| `docs/programming/milestones/m6-simulation-telemetry-testing.md` | Project pointer; **two corrected log claims** (lines 74, 215) | 9 |
| `docs/programming/README.md` | Status row: M6 built | 9 |
| `docs/programming/teaching-repo-spec.md` | §13 step 8 → Done | 9 |

---

## Task graph

| Task | File | Depends on | Touches | Lane |
|---|---|---|---|---|
| 1 | `01-spike-and-scaffold.md` | — | `projects/m6-testing/` | milestone |
| 2 | `02-plant-and-worked-example.md` | 1 | `projects/m6-testing/src/` | milestone |
| 3 | `03-regressions.md` | 2 | `projects/m6-testing/regressions/` | milestone |
| 4 | `04-log-and-questions.md` | 1 | `projects/m6-testing/logs/`, `README.md`, `READING.md` | milestone |
| 5 | `05-regression-gate.md` | — | `tools/`, `tests/` | ci-tooling |
| 6 | `06-untested-targets.md` | — | `docs/programming/UNTESTED-TARGETS.md` | docs |
| 7 | `07-solutions.md` | 2, 3 | `../ignite-training-solutions/` | tail |
| 9 | `99-integration.md` | all | workflows, `tools/`, `tests/`, `CODEOWNERS`, `tests.lock`, `docs/` | integration |

**Lanes:** `milestone` (tasks 1 → 2 → 3 → 4, strictly sequential — one Gradle project, one `build/`), `ci-tooling` (task 5), `docs` (task 6).
**Maximum useful concurrency: 3.**
**No test-out lane.** M6 has no test-out, by design — spec §10 and the milestone document's most opinionated decision.
**Serialized tail:** task 7, then task 9. Single owner, on the merged branch. Task 7 runs `cross-check.sh`, which archives the labs repository at `HEAD` — it needs the milestone lane committed, not just present in a worktree.

**Worktrees:** `.worktrees/m6` for the milestone lane, `.worktrees/m6-tools` for `ci-tooling`, `.worktrees/m6-docs` for `docs`. Never two agents in one worktree, and never two agents running Gradle against the same project directory.

**Task 1 contains a spike whose outcome can change Task 2.** The agent running Task 1 must report the spike's actual flake count before Task 2 starts. If the spike fails its bar, Task 2's Step 6 takes its stated fallback rather than improvising one.

---

## Self-review

**Spec coverage.**

| Spec requirement | Task |
|---|---|
| §9.7 one fully-commented worked-example test showing `AutoCloseable` teardown | 2 |
| §9.7 `PhoenixTestSupport.java` — `setEnabled` + `notifyNewData`, the ~100 ms and ~20 ms waits, each explained | 2 |
| §9.7 `PhoenixTestSupport` stays in the sandbox and never ships to `Robot-2026` (§6.5) | 2 (class Javadoc), 6 (`UNTESTED-TARGETS.md` states the rule), 9 (`README.md`) |
| §9.7 `LaunchProfile.java` — real logic worth testing, shaped after `Robot-2026`'s `LaunchCalculator` | 2 |
| §9.7 `regressions/*.patch` — `git apply`-able one-line regressions | 3 |
| §9.7 `logs/` — one real `.wpilog` plus `QUESTIONS.md`; answer key in the solutions repository | 4, 7 |
| §9.7 the verification mechanism: `test` passes → apply → `test` FAILS → revert | 3 (proves it), 5 (implements it), 9 (wires it) |
| §9.7 student writes every test, in both places | Constraints delta; 2 (exactly one given test) |
| §9.7 tests cover: three-plus sandbox tests pass; the regression check fails then passes | 7 (the reference tests), 5 + 9 (the check) |
| §8 L0 — worked example only | Constraints delta; 2 |
| §8 a stub never contains the answer in a comment | 2 Step 8 grep; 3 Step 5 grep |
| §8 a given test never asserts on implementation | 2 — every assertion goes through `LaunchProfile`'s public API |
| §6.5 pure-logic hard rule on the real-repo pull request | 6 (stated inline in `UNTESTED-TARGETS.md`) |
| §11.1 reference implementation + authoring notes + answer key | 7 |
| §11.2 solutions cross-check green | 7 |
| §12.2 tamper evidence for given files **and the patches** | 9 |
| §12.3 restated for M6: green, then provably red under a patch | 3, 5, 9 |
| §13 step 8 status; `UNTESTED-TARGETS.md` exists and has an owner | 6, 9 |
| §14 zero files from `projects/` in the built site | 9 |
| §16 `UNTESTED-TARGETS.md` has a named owner | 6 |
| Milestone DoD: a subsystem implements `AutoCloseable`, two tests in one file both pass | 2 (`ShooterFlywheel`), 7 (the reference tests prove it) |
| Milestone DoD: diagnostic question answered from a real log, with evidence | 4, 7 |

**Not covered, deliberately.** M6's mentor-verified half stays mentor-verified per §9.7: the `Robot-2026` pull request being merged and its tests being pure-logic, the AdvantageScope answer *with evidence*, the `@Logged` DoD item, explaining why not to instantiate `TimedRobot` in a test, and explaining what simulation proves versus what it does not. None of these get a sandbox artifact — M6 is L0, and the deliverable is the pull request. All appear in the project `README.md`'s "Done when" list and in the milestone file.

**The Epilogue DoD item has no sandbox artifact and that is intentional.** "You added `@Logged` to something and saw it appear in the log" is satisfied against the student's own `Robot-2026` work, where Epilogue is already configured and thirteen files already carry the annotation. Adding a logged subsystem to the sandbox would be scaffolding at the fade level where scaffolding is supposed to be gone.

**Type consistency.** `LaunchProfile`'s public surface — `LaunchProfile(double ticksPerRevolution, double gearRatio)`, `ticksToMechanismRotations(double) -> double`, `timeOfFlightSeconds(double) -> double`, `lookAheadDistanceMeters(double, double) -> double`, `atSetpoint(double, double) -> boolean` — is fixed in Task 2 and named identically in the worked example (Task 2), all three patches (Task 3), the reference tests (Task 7), and `README.md` (Task 4). `PhoenixTestSupport`'s API — `enableRobot()`, `settleAfterConstruction(TalonFX)`, `settleAfterControlRequest(TalonFX)` — is fixed in Task 2 and used in Tasks 2 and 7. `ShooterFlywheel`'s — `ShooterFlywheel(int canId)`, `setVelocity(double)`, `velocityRotationsPerSecond() -> double`, `motor() -> TalonFX`, `close()` — likewise. `regression_gate.py`'s CLI — `regression_gate.py PROJECT_DIR PATCH --expect {caught:CLASS#METHOD | any-failure | no-failure}` — is fixed in Task 5 and called from both workflows in Task 9: `caught:` for the example tier in `skeleton-integrity.yml`, `no-failure` for the two-tier self-check, `any-failure` for the student tier in `student-check.yml`.

**The territory partition, stated once so three tasks agree on it.** Worked example: `ticksToMechanismRotations`. Student: `timeOfFlightSeconds`, `lookAheadDistanceMeters`, `atSetpoint`. Task 2 Step 7 greps to prove the worked example stays inside its half; Task 3 Step 4 proves it from the other direction by running the worked example against both student-tier patches.

**The failure class this plan is built against.** This repository has repeatedly produced checks that pass while the thing they protect is broken, and M6's whole deliverable *is* a check. Six steps exist only to attack that, and none are optional:

- **Task 1 Step 3** — the Phoenix spike, run 20× and reported as a count, before anything depends on Phoenix working in CI.
- **Task 2 Step 9** — read every failure message `PhoenixTestSupport` can emit.
- **Task 3 Step 4** — the two-tier proof: run the worked example against **both student-tier patches** and confirm it **passes**. If it catches either, the student gate is fake and the partition of `LaunchProfile` is wrong. This is the single most important verification in the plan.
- **Task 3 Step 6** — apply and revert each patch, confirming `git status --porcelain` is empty afterward. A gate that leaves a patch applied poisons every later step in the job and fails somewhere unrelated.
- **Task 4 Step 4** — answer `QUESTIONS.md` end to end from the shipped log *before* it ships. A question whose answer needs a signal the log does not carry is unanswerable, and nobody finds out until a student is stuck in front of AdvantageScope.
- **Task 5 Step 8** — read every failure message `regression_gate.py` can emit. It has more failure modes than any tool in `tools/`.

A task report that omits its command output is not a completed task.

**Known limitation.** The student-tier patches are visible in the tree, so a student who reads `flip-sign.patch` learns which method is regressed and can write a test aimed at exactly that line. This is accepted, not overlooked: the patch is *in the repository they cloned*, and hiding it would mean either encrypting it or moving the gate server-side, both of which cost more than the exercise is worth. A student who reads the patch and writes a test that catches it has done the thing M6 asks for — the milestone's bar is deliberately low, and "wrote a test that catches a real regression" is met either way. What the patches must never do is state the **correct** value, because that would let the student fix the plant instead of testing it. Task 3 Step 5 greps for exactly that.
