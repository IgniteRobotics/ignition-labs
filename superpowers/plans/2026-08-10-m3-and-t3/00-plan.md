# M3 Command-Based + T3 Scheduler-Conflict Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans. Steps use checkbox (`- [ ]`) syntax for tracking.
>
> This is a **split plan** per [`superpowers/plan-conventions.md`](../../plan-conventions.md). Read this file plus the one task file you were handed. Do not read sibling task files — each is self-sufficient, and reading them all is the cost this layout exists to remove.

**Goal:** Ship `projects/m3-command-based/` (an L1 harness-and-fill exercise: the whole robot except the lesson, plus a given `M3Deliverables` seam and given tests that prove scheduler semantics) and `projects/testouts/t3-scheduler-conflict/` (a working three-subsystem robot where the student adds one two-subsystem command), so a Track A/B/C student can work M3 end to end and a Track C/D student can test out of it. This is build-order step 7 — the last step in the Kickoff Gate block (spec §13).

**Architecture:** Two standalone GradleRIO projects (spec §4.2), the first two in this repository that are *robots*: `frc.robot`, a `TimedRobot`, a vendordep, a running `CommandScheduler`, and tests that need the WPILib HAL. Three mechanisms carry the whole design:

1. **`M3Deliverables`** — a given interface in `frc.robot.harness` that `RobotContainer` implements. It is how a given test names classes the student has not written. M2 solved the same problem reflectively because L2 forbids giving signatures; L1 does not, so M3 gets the honest version: a real interface, a real `implements`, and compile errors the student can read.
2. **`DeliverablesFixture`** — a given test-side fixture that owns HAL startup, DriverStation enable, simulated timing, and one shared `RobotContainer`. It also translates every way a deliverable can be missing (still throwing the stub's exception, `null`, already composed) into a sentence a fifteen-year-old can act on. This is M3's `ContractProbe`, and it is the risky part of the plan.
3. **Interruption is asserted against a *given* command, not between two student commands.** The scheduler test schedules a given long-running command on the student's drivetrain, then schedules the student's command, and asserts the given one was interrupted and its `end(true)` ran. Deterministic, and it catches the milestone's named failure mode — a subclass command that forgot `addRequirements()`.

**Tech Stack:** Java 17, GradleRIO 2026.2.1, Gradle 8.11, Spotless 8.2.1 (`googleJavaFormat 1.19.2`), JUnit Jupiter 5.10.1, WPILibNewCommands 1.0.0 (frcYear 2026), Epilogue via `annotationProcessor wpi.java.deps.wpilibAnnotations()`.

---

## Constraints delta

`CLAUDE.md` at the repository root loads automatically and already carries the toolchain, pinned versions, Spotless wiring, package naming, the fade curve, the authoring rules, the clean-clone invariant, and the verification discipline. **Read it. It is not repeated here.** Only what is specific to M3 and T3 appears below.

- **Fade level L1 — harness and fill** (spec §8). The student is given everything that is not the lesson: `Main`, `Robot`, the scheduler wiring, Epilogue, `Constants` shape, the controller, the sim config, and the test harness. The student writes **subsystems, commands, the composition, the bindings, and the default command** — nothing in either project may contain a compilable subsystem or command that the student is supposed to write.
- **Package is `frc.robot`** in both projects. This is the first milestone where the spec's "M3 onward" rule applies (§4.4), and it applies to T3 too: T3 *is* a robot project, with `Main`, `Robot`, and `RobotContainer`, and the reason `frc.testout.t1`/`t2` exist is that those fixtures contain no robot. Do not name T3's package `frc.testout.t3`.
- **Vendordeps: `WPILibNewCommands` only** (spec §9.4). Copy `~/wpilib/2026/vendordeps/WPILibNewCommands.json` verbatim into each project's `vendordeps/`. No Phoenix, no PathPlanner, no photonlib — M3's lesson is the command-based model, and a swerve stack triples the surface for zero pedagogical gain.
- **Deliberately plain WPILib**: simulated `PWMSparkMax` and `DifferentialDrive`. Phoenix arrives in M4 where it is the lesson.
- **Given main-source harness code lives in a `harness/` package** — `src/main/java/frc/robot/harness/`. This mirrors the `plant/` convention M4 will use, and it is what lets `tools/tests_lock.py` and `CODEOWNERS` protect a given file that is not under `src/test/`. Task 9 adds both patterns; **no other task may edit `tools/`, `tests/`, or `CODEOWNERS`.**
- **Pinned SHAs.** `Phoenix-2026` → `a4273829a8f64fda90c559e263b0f79164d94bb9` (2026-02-10). `Robot-2026` → `28ce5397d88275001d656dbb717d22cf9ac0150d` (2026-06-09), the same SHA M2's `READING.md` already uses. Every student-facing reference to either repository is pinned.
- **HAL is required to run these tests, and that is new.** M0–M2 tests were plain Java. From M3 a test that constructs a subsystem touches PWM, which is native. `wpi.java.configureTestTasks(test)` supplies the desktop natives; `DeliverablesFixture` calls `HAL.initialize(500, 0)`. Task 1 proves this before anything depends on it.
- **One shared `RobotContainer` for the whole test run.** PWM channels are a global HAL resource: constructing a second `RobotContainer` allocates the same channel twice and throws. The fixture caches one instance statically and resets scheduler state between tests. Do not "fix" this with `forkEvery`.
- **Simulated time, not wall time.** Compositions use `withTimeout`, which reads the WPILib timer. The fixture pauses timing and steps it 20ms per scheduler loop via `SimHooks`. A test that sleeps is wrong.
- **One step in this plan needs a human at a screen.** Seeding `simgui.json` (Task 3, Step 4) means launching the simulator, mapping a keyboard joystick to port 0, and quitting so the sim writes the file itself. An agent must stop there and hand back rather than hand-write JSON whose schema it is guessing at — a seeded config that the sim silently ignores is exactly the "check that passes while the thing it protects is broken" failure this repository keeps producing.

---

## File structure

**Created — `projects/m3-command-based/`**

| Path | Responsibility |
|---|---|
| `settings.gradle`, `gradlew`, `gradlew.bat`, `gradle/wrapper/`, `.gitignore`, `WPILib-License.md`, `.wpilib/wpilib_preferences.json` | Copied from M0 unchanged |
| `build.gradle` | M2's, plus the Epilogue annotation processor and `ROBOT_MAIN_CLASS = "frc.robot.Main"` |
| `vendordeps/WPILibNewCommands.json` | Copied from the WPILib install. The only vendordep |
| `simgui.json` | Seeded by launching the sim once and quitting. Joystick mapped to port 0 |
| `src/main/java/frc/robot/Main.java` | Entry point. Given, never edited |
| `src/main/java/frc/robot/Robot.java` | Thin `TimedRobot`: scheduler run + Epilogue configure/bind |
| `src/main/java/frc/robot/Constants.java` | Given shape — `OperatorConstants` filled, per-subsystem classes the student adds |
| `src/main/java/frc/robot/RobotContainer.java` | Three marked TODO regions, the controller constructed, `implements M3Deliverables` with throwing stubs |
| `src/main/java/frc/robot/harness/M3Deliverables.java` | **Given, locked.** The five-method seam the given tests run against |
| `src/test/java/frc/robot/harness/DeliverablesFixture.java` | **Given, locked.** HAL, DriverStation, simulated time, the shared container, and every failure message |
| `src/test/java/frc/robot/CommandContractTest.java` | Given. Both commands exist, are named, are fresh per call, and declare requirements |
| `src/test/java/frc/robot/CompositionTest.java` | Given. The composition requires both subsystems and runs |
| `src/test/java/frc/robot/SchedulerInterruptionTest.java` | Given. The one that matters — interruption and `end(true)` |
| `src/test/java/frc/robot/DefaultCommandTest.java` | Given. The drivetrain has a default command and the scheduler runs it |
| `README.md` | Student-facing: order of work, what red means here, how to run the sim |
| `READING.md` | `Phoenix-2026` end to end, then `Robot-2026/RobotContainer.java`. Both pinned |

**Created — `projects/testouts/t3-scheduler-conflict/`**

| Path | Responsibility |
|---|---|
| Scaffold files, `build.gradle`, `vendordeps/` | Same shape as M3's, no Epilogue processor |
| `src/main/java/frc/robot/Main.java`, `Robot.java`, `Constants.java` | Given, working |
| `src/main/java/frc/robot/subsystems/DrivetrainSubsystem.java` | Given, working. Factory commands, a default command |
| `src/main/java/frc/robot/subsystems/ElevatorSubsystem.java` | Given, working |
| `src/main/java/frc/robot/subsystems/GripperSubsystem.java` | Given, working |
| `src/main/java/frc/robot/RobotContainer.java` | Given and working except one seam: `combinedCommand()` throws until the student writes it |
| `src/test/java/frc/robot/harness/SchedulerFixture.java` | **Given, locked.** T3's own copy of the HAL/scheduler support — the projects are standalone and must not share source |
| `src/test/java/frc/robot/CombinedCommandTest.java` | Given. Requirements, naming, freshness, interruption |
| `README.md` | The task, the 60-minute box, the pass criteria |

**Created — `../ignite-training-solutions/`** (separate repository, mentor-only)

| Path | Responsibility |
|---|---|
| `solutions/m3-command-based/src/main/java/frc/robot/**` | Reference `RobotContainer`, two subsystems, one `Command` subclass |
| `solutions/testouts/t3-scheduler-conflict/src/main/java/frc/robot/**` | Reference `RobotContainer` with the combined command |
| `authoring-notes/m3-command-based.md` | Which trap each given test aims at |
| `answer-keys/t3-scheduler-conflict.md` | The interruption explanation, and what a failing answer sounds like |

**Modified** (Task 9 only)

| Path | Change |
|---|---|
| `tools/tests_lock.py` | `PATTERNS` gains `projects/*/src/main/java/**/harness/*.java` |
| `tests/test_tests_lock.py` | Coverage for the new pattern |
| `CODEOWNERS` | Gains `/projects/*/src/main/java/**/harness/` |
| `tests.lock` | Regenerated |
| `docs/programming/milestones/m3-wpilib-command-based.md` | Project and test-out pointers |
| `docs/programming/README.md` | Status row: M3 built |
| `docs/programming/teaching-repo-spec.md` | §13 step 7 → Done |

---

## Task graph

| Task | File | Depends on | Touches | Lane |
|---|---|---|---|---|
| 1 | `01-scaffold-and-harness.md` | — | `projects/m3-command-based/` | milestone |
| 2 | `02-given-tests.md` | 1 | `projects/m3-command-based/src/test/` | milestone |
| 3 | `03-docs-and-sim.md` | 1 | `projects/m3-command-based/` docs + `simgui.json` | milestone |
| 4 | `04-t3-fixture.md` | — | `projects/testouts/t3-scheduler-conflict/` | testout |
| 5 | `05-solutions.md` | 2, 4 | `../ignite-training-solutions/` | tail |
| 9 | `99-integration.md` | all | `tests.lock`, `tools/`, `tests/`, `CODEOWNERS`, `docs/` | integration |

**Lanes:** `milestone` (tasks 1 → 2 → 3, strictly sequential — one Gradle project, one `build/`), `testout` (task 4).
**Maximum useful concurrency: 2.**
**Serialized tail:** task 5, then task 9. Single owner, on the merged branch. Task 5 runs `cross-check.sh`, which archives the labs repository at `HEAD` — it needs both lanes committed, not just present in a worktree.

**Worktrees:** `.worktrees/m3` for the milestone lane, `.worktrees/t3` for the testout lane. Never two agents in one worktree, and never two agents running Gradle against the same project directory.

**Task 3 hands back to a human** at its Step 4. Schedule it accordingly: an agent can complete Steps 1–3 and 5–7, but the `simgui.json` seeding needs a person.

---

## Self-review

**Spec coverage.**

| Spec requirement | Task |
|---|---|
| §9.4 `Main.java`; thin `Robot.java` with the scheduler running and Epilogue configured, matching `Robot-2026` | 1 |
| §9.4 `RobotContainer.java` with marked TODO regions and the controller already constructed | 1 |
| §9.4 `Constants.java` stub | 1 |
| §9.4 seeded `simgui.json` so the simulator opens with a joystick already mapped | 3 |
| §9.4 deliberately plain WPILib — simulated `PWMSparkMax` and `DifferentialDrive`, no Phoenix | Constraints delta; 1 (README/READING name the plant), 4 (T3's given subsystems) |
| §9.4 the `M3Deliverables` given interface, five members exactly as specified | 1 |
| §9.4 given tests: both commands have a `.withName()` | 2 (`CommandContractTest`) |
| §9.4 given tests: the composition requires both subsystems | 2 (`CompositionTest`) |
| §9.4 `SchedulerInterruptionTest` — two commands requiring the same subsystem, first is interrupted, `end(true)` fires | 2 |
| §9.4 tests cover: both commands exist, are named, and run | 2 |
| §9.4 vendordeps: `WPILibNewCommands` only | 1, 4 |
| §9.4 real repo: clone `Phoenix-2026` end to end, then `Robot-2026/RobotContainer.java`, read-only | 3 (`READING.md`, both pinned) |
| §8 L1 — the student writes subsystems, commands, control logic | 1 (nothing given is a subsystem or a command the student owes) |
| §8 a stub never contains the answer in a comment | 1 Step 8 grep; 4 Step 7 grep |
| §8 a given test never asserts on implementation | 2 — every assertion goes through `Command`, `Subsystem`, and `CommandScheduler` public API. No reflection anywhere in M3 |
| §10 T3 — working `RobotContainer` with three subsystems; add a command requiring two; bind it; explain interruption; code half test-covered, explanation mentor-verified | 4 |
| §11.1 reference implementation + authoring notes + answer key | 5 |
| §11.2 solutions cross-check green | 5 |
| §12.2 tamper evidence for given files, including the main-source harness | 9 |
| §12.3 clean clone: `assemble` green, `compileTestJava` green, `test` red from assertions | 1 Step 9, 2 Step 8, 4 Step 8, 9 Step 6 |
| §13 step 7 status | 9 |
| §14 zero files from `projects/` in the built site | 9 Step 5 |
| Milestone DoD: default command on the drivetrain | 2 (`DefaultCommandTest`) |

**Not covered, deliberately.** The mentor-verified half of M3 stays mentor-verified per D3: articulating the subclass-vs-factory tradeoff, tracing one 20ms loop out loud, the button bindings actually being bound (a test can see a `Trigger`'s command only by driving simulated HID state, which is brittle and asserts on the student's choice of button), the names appearing in the simulator dashboard, and having read `Phoenix-2026`. T3's part 3 — the interruption explanation — is the fixture's pass criterion and is explicitly mentor-verified by §10. All of it appears in the project READMEs' "Done when" lists and in the milestone file.

**One test is allowed to abort rather than fail.** `SchedulerInterruptionTest`'s second case — the student's command being the one interrupted — is inapplicable if their command finishes in its first loop. It is guarded by a JUnit `assumeTrue` with an explanatory message, so it reports as skipped rather than passing vacuously. The first case, which schedules a *given* long-running command and asserts the student's command interrupts it, has no such hole and is the one that carries the coverage.

**Type consistency.** `M3Deliverables`'s five members — `drivetrain() -> Subsystem`, `mechanism() -> Subsystem`, `subclassCommand() -> Command`, `factoryCommand() -> Command`, `composition() -> Command` — are named identically in the interface (Task 1), in `RobotContainer`'s stubs (Task 1), in every `DeliverablesFixture` accessor (Task 1), in all four test classes (Task 2), in the reference solution (Task 5), and in both READMEs (Tasks 1, 3). `DeliverablesFixture`'s API — `setUp()`, `tearDown()`, `drivetrain()`, `mechanism()`, `subclassCommand()`, `factoryCommand()`, `composition()`, `runScheduler(int)`, `holdCommand(Subsystem...)` — is fixed in Task 1 and Task 2 is written entirely against it. T3's `SchedulerFixture` mirrors it with `combinedCommand()`, `drivetrain()`, `elevator()`, `gripper()`.

**The failure class this plan is built against.** Eight times across five plans this repository has produced a check that passes while the thing it protects is broken. Six steps exist only to attack that, and none are optional: Task 1 Step 2 (a HAL spike, run and deleted, before anything depends on HAL working), Task 1 Step 10 (read every failure message the fixture can emit — a fixture whose messages have never been read is a fixture whose messages are wrong), Task 2 Step 5 (a control implementation — a suite no correct robot passes is as broken as one every wrong robot passes), Task 2 Step 6 (the distractor matrix — five plausible-but-wrong robots, each must fail the case aimed at it), Task 4 Step 6 (complete the T3 fixture end to end within its own time box), and Task 5 (the cross-check, which is the only mechanism that keeps any of this true next month). A task report that omits its command output is not a completed task.

**Known limitation.** `M3Deliverables` requires a fresh command instance per call, and that is a real constraint on the student's design — a student who stores `private final Command driveForward = ...` and returns the field will fail `CommandContractTest` even though their robot works in the simulator. The constraint is not arbitrary: WPILib forbids scheduling a command that belongs to a composition, so a stored instance that `composition()` also uses cannot be scheduled on its own, and the given tests must schedule both. The interface Javadoc states it, the README states it, and the fixture's failure message states it with the fix. It is still the most likely thing to confuse a student who has otherwise done the milestone correctly, and a mentor should expect it once per cohort.
