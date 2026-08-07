# Ignite Robotics — Teaching Repository Spec

> ## ⚠️ Partially superseded — read this first
>
> [`superpowers/specs/2026-08-07-skeleton-projects-design.md`](../superpowers/specs/2026-08-07-skeleton-projects-design.md) **overrides decisions D1, D2, and §6.7 of this document, and obviates the §4.2 spike.** Where the two disagree, that design wins.
>
> | Here | Now |
> |---|---|
> | **D1** — skeletons live in a new `ignite-training` repo | They live in **this repository**, at `projects/` |
> | **D2** — "template repository + fork/PR", venue unstated | Fork `docs` once at M0; branch per milestone; **PR into your own fork**. Upstream sees exactly one student PR, ever |
> | **§4.2** — spike the multi-project GradleRIO build | **Deleted.** No Gradle root exists, so standalone-per-milestone (the stated fallback) is the only option |
> | **§6.7** — M6 ships a `Robot-2026` lookalike to stay self-contained | That is the warm-up. The **M6 deliverable is a real PR** adding `src/test` to `Robot-2026` |
>
> Still in force: D3, D4, D5, D6, the §5 fade curve, the §6 inventories, the §7 fixtures, and the §9 anti-goals.
>
> **This banner is removed when the design is folded in — step 12 of its build order.**

**Status:** Proposed spec, partially superseded — see banner above. Not yet executed
**Owner:** Josh Reddick
**Created:** 2026-08-06
**Team:** FRC 6829 Ignite Robotics
**Parent:** [`curriculum-spec.md`](curriculum-spec.md) — this document executes the "Teaching repository for M0–M3 projects: **Not built**" line in that spec's status table, and extends it to all nine milestones.

---

## 1. Purpose

The curriculum is written. Every milestone specifies a project. **None of those projects has code to start from.**

Today a student who reaches M1 is told "write six robot-flavored exercises, each ships with assertions that must pass" — and no such exercises exist. A student at M4 is told "characterize a flywheel with SysId in simulation" and must first build a simulated flywheel. A mentor running an M5 test-out is told "give the student an autonomous routine that drifts" and must author one on the spot.

This spec defines the **skeleton source code** that closes that gap: what exists, how much of it is given versus written, how it is distributed, and how completion is verified.

### 1.1 Scope

**In scope.** A single teaching repository containing per-milestone skeleton projects, per-track test-out fixtures, the CI that verifies them, and a private companion repository holding reference solutions.

**Out of scope.** Rewriting `CommandTemplate` (still excluded per `curriculum-spec.md` §8, still the highest-leverage separate fix). Any tracking site, roster system, or LMS. Changes to the curriculum prose — this spec adds code the existing documents already assume.

### 1.2 What this is not

It is not a course-in-a-box that replaces mentors. Every milestone retains explanation-level Definition-of-Done items that only a human can verify, and this spec deliberately does not try to automate them. The skeletons remove setup friction and make the mechanical half of each DoD objectively checkable. That is all.

---

## 2. Research basis

Six searches across peer FRC practice, CS-education literature, and the GitHub/WPILib tooling surface. The finding that shapes everything below: **no FRC team has published a milestone-ladder skeleton set.** Building one puts Ignite past the state of the art, which is the same position the curriculum documents already occupy.

### 2.1 What peer teams actually do

| Source | What it is | What we take |
|---|---|---|
| [FRC Java Koans (Team 3550)](https://github.com/jletroui/FrcJavaKoans) | The only mature model found. A single Java project where a "master" runner walks sequential exercises, printing expected-vs-actual until each passes. Progression is gated — completing a koan reveals the next | The **stub-plus-given-assertion** shape for M1, and the principle that feedback is expected-vs-actual, not pass/fail |
| [Team 8744 — FRC_2026_SoftwareLessons](https://github.com/FRC-Team8744/FRC_2026_SoftwareLessons) | Cited in `curriculum-spec.md` §4.3 as an M1 resource. Inspecting it: markdown lesson outlines and visuals folders, **no starter code, no tests, no Gradle project.** Self-described as "just an outline of the season lessons for now" | A caution. Our own M1 resource link points at a repo that has the same gap this spec exists to close — worth knowing before recommending it to a student |
| [SciGuides (Team 1155)](https://github.com/SciBorgs/SciGuides) | "Public projects and lessons for teaching members." Has a `projects/` directory alongside `reference-sheets/` and `troubleshooting-logs/` | The idea of shipping **troubleshooting logs** as a first-class artifact next to the code |
| [FRC 1099 DiscoTechs training](https://sites.google.com/student.brookfieldps.org/discotechs/frc-java-programming-training) | Already the model for the curriculum's track architecture (`curriculum-spec.md` §2) | Nothing new — their published material is lesson-level, not code-level |
| [2026 KitBot Java Software Guide](https://firstfrc.blob.core.windows.net/frc2026/KitBot/2026-kitbot-java-guide.pdf) | Official FIRST walkthrough of a complete, working, minimal robot project | The **read-a-whole-small-thing-first** pattern, which M3 already uses with `Phoenix-2026` |

### 2.2 Tooling

- **GitHub Classroom has a working Java/Gradle/JUnit autograding path** ([classroom-resources/autograding-example-java](https://github.com/classroom-resources/autograding-example-java)), with tests re-running on every push. **Not adopted** — see §3, decision D2.
- **Zero-install is technically viable.** `wpilib/roborio-cross-ubuntu:2026-22.04-py314` is current ([wpilibsuite/docker-images](https://github.com/wpilibsuite/docker-images)), and Codespaces-for-FRC is a documented community pattern ([Chief Delphi](https://www.chiefdelphi.com/t/github-codespaces-programming-in-your-browser-for-frc-2023-update/423814), [Evan Pratten](https://ewpratten.com/blog/codespaces-for-frc/)). **Not adopted** — see decision D4.
- **Simulation runs headless from Gradle**: `./gradlew simulateJava` ([WPILib robot simulation docs](https://docs.wpilib.org/en/stable/docs/software/wpilib-tools/robot-simulation/introduction.html)), which is what makes CI-verified simulation projects possible at all.

### 2.3 The CS-education finding that sets the scaffold curve

Faded worked examples come out of cognitive load theory and are established in mathematics, engineering, and psychology. In programming specifically, the strongest result is that **faded worked examples combined with metacognitive scaffolding beat both constant scaffolding and none** ([Shin, Jung, Zumbach & Yi, 2023](https://journals.sagepub.com/doi/abs/10.1177/07356331231174454)). Separately, novices "welcome additional scaffolds at the outset" and that need "tended to fade as they gained familiarity with programming terminology and constructs" ([fade-in vs. fade-out scaffolding, *J. Computing in Higher Education*](https://link.springer.com/article/10.1007/s12528-021-09307-w)).

**Consequence for this spec:** skeleton density is not uniform. It is a deliberate decreasing curve across the ladder — §5. A student who gets the same amount of help in M6 as in M1 has been failed by the scaffolding, not served by it.

The metacognitive half of that finding is already satisfied by the curriculum's existing **Common failure modes** sections. This spec adds nothing there and should not.

---

## 3. Decisions

Six decisions, all made, recorded here with reasoning so they can be argued with rather than rediscovered.

| # | Decision | Reasoning |
|---|---|---|
| **D1** | **One repository, per-milestone Gradle projects.** `ignite-training`, with `m0-onboarding/` … `m8-ownership/` and `testouts/` as sibling project directories | One clone, one toolchain verification, one place to keep on the current WPILib generation. Nine separate repos is nine seasonal re-verification passes and nine chances for one to rot unnoticed — the exact failure mode `curriculum-spec.md` §1.2 was written to prevent. Accepted cost: a student can see later milestones early, which is harmless — reading ahead is not the risk, and the solutions are elsewhere |
| **D2** | **Template repository + fork/PR. CI runs the tests. No GitHub Classroom** | Classroom's autograding is genuinely better verification, but it is a roster-linked tracking system, and `curriculum-spec.md` §8 excluded tracking systems after a peer resource drew real criticism for collecting student names and emails with no deletion path. Reversing that needs a privacy review, not a convenience argument. The fork/PR flow also *is* M0's learning objective — routing students through Classroom would bypass the ritual the first milestone exists to teach |
| **D3** | **Tests enforce the mechanical Definition-of-Done items only** | A JUnit assertion can prove a deadband function clamps correctly. It cannot prove a student can explain why `1/2` is `0`. Wiring CI to "check" the explanation items would produce a green check that means less than the mentor conversation it replaced. The test suite is the floor; the mentor conversation remains the ceiling. Every milestone's DoD is split explicitly in §6 |
| **D4** | **Local WPILib toolchain only. No devcontainer, no Codespaces** | Installing the toolchain and getting `./gradlew build` green **is** M0's stated learning objective. A devcontainer lets a student skip the thing M0 exists to teach. Revisit only if a real student is genuinely blocked by a locked-down machine — at which point it is a one-off accommodation, not a shipped artifact |
| **D5** | **Shared milestone skeletons + per-track test-out fixtures** | The curriculum is "one spine, four entry points" — tracks differ by *where you get on*, not what you learn. Per-track skeleton variants would be up to 36 artifacts duplicating the same content four ways, which is exactly the drift `curriculum-spec.md` §2 rejected. The track axis surfaces where it actually exists: the test-out fixtures, which are the load-bearing mechanism for Tracks B, C, and D |
| **D6** | **Reference solutions live in a separate private repository** | `ignite-training-solutions`, mentor-access only. A `solutions/` directory in the student repo relies on honor, and Red-tier students under time pressure are precisely the population that will look. Accepted cost: two repos to keep in sync, mitigated by the cross-check CI in §8.2 |

---

## 4. Repository architecture

### 4.1 Layout

```
ignite-training/                        (public, template repository)
├── README.md                           routing: which directory for which milestone
├── CONTRIBUTING.md                     the fork → branch → PR → review → merge ritual
├── SETUP.md                            toolchain install, verified per OS
├── tests.lock                          SHA-256 manifest of every given test file
├── settings.gradle                     see §4.2 — pending the D1 spike
├── build.gradle                        shared conventions (Spotless, Java 17, JUnit)
├── gradle/wrapper/                     Gradle 8.11, matching Robot-2026 and Phoenix-2026
├── .github/
│   ├── workflows/
│   │   ├── skeleton-integrity.yml      runs on this repo: compiles, formats, verifies tests.lock
│   │   └── student-check.yml           runs on student forks: build + test + spotlessCheck
│   ├── PULL_REQUEST_TEMPLATE.md        includes the AI-use disclosure block
│   └── CODEOWNERS                      given test files require mentor review to change
├── templates/                          M8 fill-in-the-blank deliverables, as real files
│   ├── SUBSYSTEM-README.md
│   ├── TUNING-LOG.md
│   └── PIT-DOC.md
├── m0-onboarding/
├── m1-java-fundamentals/
├── m2-oop/
├── m3-command-based/
├── m4-control/
├── m5-auto-vision/
├── m6-testing/
├── m7-ai-assisted/
├── m8-ownership/
└── testouts/
    ├── t1-find-the-bug/
    ├── t2-supplier-swap/
    ├── t3-scheduler-conflict/
    ├── t4-bad-kv/
    └── t5-drifting-auto/
```

### 4.2 The one load-bearing technical unknown

**GradleRIO is designed for single-project builds.** Every Ignite repo, every WPILib example, and every peer team repo surveyed is one Gradle project at the repository root. A multi-project build where each subproject applies `edu.wpi.first.GradleRIO`, carries its own `vendordeps/`, and declares its own `ROBOT_MAIN_CLASS` is not a configuration WPILib documents or tests.

Two specific risks:

1. **Vendordep resolution.** GradleRIO reads `vendordeps/*.json` relative to the project directory. Per-subproject vendordeps may or may not resolve correctly in a multi-project build.
2. **VS Code extension targeting.** The WPILib extension reads `.wpilib/wpilib_preferences.json` at the workspace root and its "Simulate Robot Code" command assumes a root project. Students may be pushed to `./gradlew :m3:simulateJava` from a terminal, which is a worse first experience than the button.

**This is spiked before anything else is built** — step 0 of §10.

**Defined fallback if the spike fails.** Each milestone directory becomes a **standalone Gradle project** — its own `gradlew`, its own `settings.gradle`, no root aggregation — inside the same repository. This preserves every benefit of D1 (one clone, one re-verification surface, one CI config) and costs only a matrix strategy in the CI workflow. Students open the milestone directory as their VS Code workspace and everything behaves exactly as the WPILib extension expects.

The fallback is close enough to free that the spike should be time-boxed to one session. **Do not spend a week making the multi-project build work.**

### 4.3 Build conventions

Every skeleton project matches the current in-house stack, verified against `Robot-2026` and `Phoenix-2026` on 2026-08-06:

| Setting | Value | Source of truth |
|---|---|---|
| GradleRIO | `2026.2.1` | Both repos |
| Gradle wrapper | `8.11` | Both repos |
| Java | `sourceCompatibility` / `targetCompatibility` 17 | Both repos |
| Spotless plugin | `8.2.1` | `Robot-2026` (newer than `Phoenix-2026`'s 6.25.0) |
| Formatter | `googleJavaFormat('1.19.2').reflowLongStrings()`, `removeUnusedImports()`, `trimTrailingWhitespace()`, `endWithNewline()` | Both repos, identical |
| Spotless enforcement | `enforceCheck false` plus `compileJava.dependsOn 'spotlessApply'` | Both repos |
| JUnit | `org.junit.jupiter:junit-jupiter:5.10.1`, `useJUnitPlatform()`, `wpi.java.configureTestTasks(test)` | Both repos |
| Vendordeps | Minimum set per milestone — see §6. Never "all of them by default" | — |

**Not carried over:** the `com.peterabeles.gversion` plugin and generated `BuildConstants` from `Robot-2026`. Useful in a season repo, noise in a teaching one.

### 4.4 Package naming

| Milestones | Root package | Why |
|---|---|---|
| M1, M2 | `frc.training.m1`, `frc.training.m2` | Pure exercises with no robot. Naming them `frc.robot` would be a lie about what they are, and invites pasting training code into a season repo out of muscle memory |
| M3 onward | `frc.robot` | From M3 the entire point is that the structure matches the real repositories. A student who reads `Phoenix-2026` and then opens `m3-command-based/` should see the same shape, same package, same file names |

### 4.5 The two CI workflows do different jobs

This distinction matters and is easy to get wrong.

**`skeleton-integrity.yml`** runs on `ignite-training` itself. A freshly cloned skeleton is *supposed* to fail its tests — that is the student's target. So this workflow must **not** run `./gradlew build`. It runs:

- `./gradlew assemble` — every skeleton compiles on a clean clone
- `./gradlew spotlessCheck` — every skeleton is already formatted
- `tests.lock` verification — see §9.2
- An inverted check per milestone: the given tests **fail** on the unmodified skeleton. A skeleton whose tests pass out of the box has a stub that accidentally implements itself, which is a real and silent authoring bug

**`student-check.yml`** ships in the template and runs on the student's fork. It runs `./gradlew build` (which includes `test`), plus `spotlessCheck`, and posts the PR comment. Model it on `Robot-2026/.github/workflows/pr-build-check.yml`, which already does the build-plus-format-plus-comment dance well.

> **Correction candidate for the curriculum docs.** `curriculum-spec.md` §2.2, `tracks/track-d-frc-veteran-ai.md`, and `milestones/m6-simulation-telemetry-testing.md` all state "Neither CI workflow runs `./gradlew test`." Literally true — neither invokes `test` by name — but `pr-build-check.yml` runs `./gradlew build`, and in the Gradle Java plugin `build` → `check` → `test`. **Ignite's CI already gates on tests; there simply are none to run.** That is a materially better starting position than the docs claim, and the M6 argument gets stronger rather than weaker: the harness is wired, the tests are the only missing piece. Verify empirically during the §10 step-0 spike (GradleRIO's `wpi.java.configureTestTasks` may alter this), then correct all three documents.

---

## 5. The scaffold-fade curve

Four density levels, decreasing across the ladder, per §2.3.

| Level | Milestones | What the student is given | What the student writes |
|---|---|---|---|
| **L3 — Stub and fill** | M1 | Class, method signature, and a Javadoc contract stating exact expected behavior. Body is `throw new UnsupportedOperationException("TODO")`. Tests given and complete | Method bodies only |
| **L2 — Contract and build** | M2 | A Javadoc-only specification of the required public API, and a read-only contract test that enforces it. No interface file — writing the signatures is part of the exercise | Class, signatures, bodies, plus their own tests |
| **L1 — Harness and fill** | M3, M4, M5 | Everything that is not the lesson: `Main`/`Robot` wiring, simulated plants, vendordeps, sim config, logging, a test harness. Plus given tests that assert observable behavior | Subsystems, commands, control logic, gains, auto routines — the lesson |
| **L0 — Worked example only** | M6, M7, M8 | One fully-commented worked example, the awkward setup encapsulated (CTRE sim timing, `AutoCloseable` teardown), and the raw material (a log, a regression patch, a feature backlog) | Everything else |

Two rules that hold at every level:

- **A stub never contains the answer in a comment.** `// TODO: divide by ticksPerRotation` is not scaffolding, it is the solution with extra steps. The Javadoc states the *contract* — inputs, outputs, edge cases — never the implementation.
- **A given test never asserts on implementation.** It asserts on behavior through the public API. A test that requires a specific private field name teaches compliance, not correctness.

---

## 6. Per-milestone skeleton inventory

Each entry lists what ships, which DoD items the tests cover, and which stay mentor-verified per D3.

### 6.1 M0 — `m0-onboarding/` · fade: N/A

M0 is not about code. The skeleton exists so the PR has something real to change.

**Given:** a complete, working, minimal `TimedRobot` project that builds and whose simulator launches. `CONTRIBUTING.md` with the curated command set from `curriculum-spec.md` §4.2 — `clone`, `checkout -b`, `add -u`, `commit -m`, `fetch --all`, `merge`, `push` — and nothing else. `student-check.yml`. A deliberately mis-formatted `FormatMe.java`.

**Student authors:** `contributors/<github-handle>.md` from a template, and runs `./gradlew spotlessApply` on `FormatMe.java`, then opens one PR containing both.

> **Privacy note.** GitHub handle only — no legal names, no emails, no school. The file is self-created and removable by the student's own PR at any time. This is deliberately *not* a roster: nothing reads it, nothing aggregates it, and the Kickoff Gate checklist stays where `curriculum-spec.md` §3 put it, with a mentor.

**Tests cover:** contributor file exists and matches the schema; `spotlessCheck` passes.
**Mentor verifies:** simulator launches and the student can point at the Driver Station panel; can explain a branch without using the word "branch".

### 6.2 M1 — `m1-java-fundamentals/` · fade: L3

**Given:** six stub classes in `frc.training.m1`, one per exercise in `milestones/m1-java-fundamentals.md` — `Deadband`, `TickConversion`, `GearRatio`, `AtSetpoint`, `Clamp`, `Interpolate`. Each: one static method, a Javadoc contract, an `UnsupportedOperationException` body. Six complete, read-only test classes.

Every test suite includes the trap cases the milestone's failure-mode section names:
- `TickConversion` — an input that returns `0` under integer division and the correct non-zero value otherwise
- `AtSetpoint` — values that are equal to within `1e-9` but not `==`
- `Clamp` — both boundaries inclusive, plus min-greater-than-max
- `Interpolate` — a query exactly at each known point, and one outside the range

Plus two non-test artifacts:
- `CompilerErrorGarden.java` — three commented-out broken snippets, uncommented one at a time, for the "read a compiler error unaided" DoD item
- `BlockingWaitDemo.java` — a runnable `main` showing a `while` loop starving the 20ms loop

**Vendordeps:** none. This is plain Java.

**Tests cover:** all six exercises pass their assertions.
**Mentor verifies:** reads a compiler error unaided; explains `1/2 == 0`; explains why `==` is wrong for doubles; explains the blocking-`while` danger.

### 6.3 M2 — `m2-oop/` · fade: L2

**Given:** `SensorProfileSpec.java` — a Javadoc-only specification class describing the required public API (constructor taking ticks-per-rotation and gear ratio; ticks↔rotations; ticks-per-100ms↔RPM; one method taking a `DoubleSupplier`) with no signatures to copy. A read-only `SensorProfileContractTest` that enforces the API by behavior. A student test file with one worked example and three empty `@Test` methods carrying TODOs.

`SupplierDemo.java` — a working class with the milestone's headline bug already planted: a constructor that captures `supplier.getAsDouble()` once instead of storing the supplier, so the value appears frozen. A given test fails until the student fixes it. This is the single most common M2 misunderstanding and the failure-modes section already calls it out; giving it to them broken is cheaper than waiting for them to write it broken.

`READING.md` — the `Robot-2026/src/main/java/frc/robot/preferences/` reading assignment, with **a pinned commit SHA**, so the read is reproducible and the seasonal re-verification pass has something concrete to check.

**Vendordeps:** none.

**Tests cover:** contract test passes; four student tests exist and pass; `SupplierDemo` fixed.
**Mentor verifies:** value-vs-supplier explanation; `LaunchState.getInstance()` and what a singleton guarantees; why both launch-request builders implement one interface; why `RobotPreference<T>` implements `Supplier<T>`.

### 6.4 M3 — `m3-command-based/` · fade: L1

**Given:** `Main.java`; a thin `Robot.java` with the scheduler running and Epilogue configured (`Epilogue.configure(...)` and `Epilogue.bind(this)`, matching `Robot-2026`); a `RobotContainer.java` with marked TODO regions and the controller already constructed; a `Constants.java` stub; a seeded `simgui.json` so the simulator opens with a joystick already mapped.

**Deliberately plain WPILib.** Simulated `PWMSparkMax` and `DifferentialDrive`, no Phoenix. M3's lesson is the command-based model, and adding a swerve stack triples the surface for zero pedagogical gain. Phoenix arrives in M4 where it is the lesson.

**The test-harness design.** Given tests must assert on classes the student has not written yet. Solving this by reflection over guessed class names is brittle and teaches students to name things to please a grader. Instead the skeleton ships a small given interface:

```java
/** Exposes your M3 deliverables to the test harness. Implement it in RobotContainer. */
public interface M3Deliverables {
  Subsystem drivetrain();
  Subsystem mechanism();
  Command subclassCommand();   // your explicit Command subclass
  Command factoryCommand();    // your subsystem factory method
  Command composition();       // your sequence or parallel-with-timeout
}
```

Given tests then run against the interface: both commands have a `.withName()`, the composition requires both subsystems, and — the important one — `SchedulerInterruptionTest` schedules two commands requiring the same subsystem and asserts the first is interrupted and its `end(true)` fires. That is the exact model the M3 test-out and DoD both turn on, and a student can watch it pass.

**Vendordeps:** `WPILibNewCommands` only.

**Tests cover:** both commands exist, are named, and run; composition requires both subsystems; interruption semantics hold.
**Mentor verifies:** articulates the subclass-vs-factory tradeoff; traces one full loop iteration out loud; subsystems are private fields rather than globals.

### 6.5 M4 — `m4-control/` · fade: L1

**Given:** two simulated plants — `FlywheelPlant.java` (`FlywheelSim`) and `ArmPlant.java` (`SingleJointedArmSim`) — with **true parameters the student must not read.** They live in a `plant/` package listed in `CODEOWNERS` and covered by `tests.lock`. The point of the milestone is characterizing a system you cannot look up, and a student who reads kV out of a constants file has skipped it.

Also given: `SysIdRoutine` wiring (boilerplate, explicitly permitted under Yellow tier); Epilogue `@Logged` on setpoint and measurement together, per the M6 convention; a `PlotHarness` that dumps setpoint-vs-measurement CSV for the "produce a plot and read it" DoD item; a limit-switch sim for homing.

**Student writes:** feedforward application, gains, PID, motion profile, homing sequence.

**Vendordeps:** `Phoenix6` (26.1.0), `WPILibNewCommands`.

**Tests cover:** flywheel reaches and holds commanded velocity within tolerance and settling time; arm holds position against gravity; homing zeroes on limit-switch contact.
**Mentor verifies:** what each of kS, kV, kA, kG does; why feedforward is tuned before P; reads their own plot.

**Stretch, unchanged:** the `DriveMotorConfigs.java` `.clone()` refactor happens as a PR against `Robot-2026`, not here. Real code, real PR — that is the point of it.

### 6.6 M5 — `m5-auto-vision/` · fade: L1

**Given:** a working simulated swerve drivetrain with `AutoBuilder` already configured; the AprilTag field layout; a `VisionSim` harness built on PhotonVision's `VisionSystemSim` with **configurable noise and latency** so the student can make it worse on purpose; a `SendableChooser` already wired; one example path file.

Building a swerve drivetrain is not M5's lesson. Composing autos and fusing vision is.

**Student writes:** paths, named commands, the composition with timeouts, the chooser entries; pose-estimator fusion, timestamp handling, distance-based filtering.

**Vendordeps:** `PathplannerLib` (2026.1.2), `photonlib`, `Phoenix6`, `WPILibNewCommands` — matching `Robot-2026` exactly.

**Tests cover:** the routine terminates even when a step is forced to fail (the milestone's step 7); fused pose error is lower than odometry-only error over a run; a measurement submitted with a deliberately wrong timestamp is rejected by the student's filter.

That last test is the one that matters. The milestone says "everyone learns that vision helps; far fewer learn that vision applied wrongly is worse than no vision" — and it is the only DoD item in M5 that a student can otherwise fake by getting a good result for the wrong reason.

**Mentor verifies:** explains odometry drift and why vision corrects it; articulates one real PathPlanner-vs-Choreo tradeoff.

### 6.7 M6 — `m6-testing/` · fade: L0

The gate to M7. The bar is deliberately low and the skeleton must not raise it.

**Given, and nothing more:**

- **One** fully-commented worked-example test, showing the `AutoCloseable` teardown pattern
- `PhoenixTestSupport.java` — the CTRE simulation timing quirks encapsulated: `DriverStationSim.setEnabled(true)` plus `DriverStation.notifyNewData()`, the ~100ms delay after constructing devices, the ~20ms delay after a control request before asserting. `milestones/m6-simulation-telemetry-testing.md` says these must be taught explicitly rather than discovered; a helper class with a comment explaining each delay is how you teach it once instead of losing three students to intermittent failures
- `LaunchProfile.java` — a class with real logic worth testing, shaped after `Robot-2026`'s `LaunchCalculator`, so the exercise does not depend on a second repo building
- `regressions/*.patch` — `git apply`-able one-line regressions (flip a sign, drop a gear ratio). Makes "introduce a deliberate regression" one reproducible command instead of an improvisation
- `logs/` — one real `.wpilog` copied from `Robot-2026/logs/`, plus `QUESTIONS.md` with the diagnostic questions. Answer key lives in the solutions repo

**Student writes:** every test.

**The verification mechanism, which is the best idea in this spec.** CI does not count tests. It runs:

```
./gradlew :m6:test                                  → must pass
git apply regressions/flip-sign.patch
./gradlew :m6:test                                  → must FAIL
git apply -R regressions/flip-sign.patch
```

A student can satisfy a test-count check with three assertions on `true`. They cannot satisfy this one without a test that actually catches the regression — which is the entire stated point of M6, and the one DoD item most likely to be technically met and substantively missed.

**Tests cover:** three-plus tests pass; the regression check fails then passes.
**Mentor verifies:** the AdvantageScope diagnostic answer *with evidence*; explains why not to instantiate `TimedRobot` in a test.

### 6.8 M7 — `m7-ai-assisted/` · fade: L0

Almost no code. M7 ships a feature into `m3`/`m4`/`m5` or a real repo — building a sandbox for it would defeat the "pick a real, useful feature, not a toy" requirement.

**Given:** `ai-code-review.yml`, copied from `Robot-2026`, so students meet AI-as-reviewer as a running thing rather than a description. A PR template with the disclosure block from `ai-use-standard.md` §6. `FEATURE-BACKLOG.md` — a mentor-maintained list of real, small, shippable features, so nobody spends their M7 session choosing one. `DEFENSE-REVIEW.md` — the mentor-side question bank, published openly because a defense review students can prepare for is still a defense review. `MISCALIBRATION.md` — the deliberate-wrong-answer exercise, with a place to paste what the tool confidently said.

**Tests cover:** nothing. M7's assessment is the defense review, by design.
**Mentor verifies:** all five DoD items.

### 6.9 M8 — `m8-ownership/` · fade: L0

No skeleton code. The deliverables in `student-deliverables.md` become **real files** in `templates/` so a student copies rather than retypes — which is the difference between a template and a description of a template, and is already the stated acceptance criterion in `curriculum-spec.md` §7.

`m8-ownership/README.md` points at `templates/` and at the week-by-week cadence.

---

## 7. Test-out fixtures

The track axis, per D5. Each fixture is a standalone project with a student-facing `README.md` stating the task and the time box, and an answer key in the solutions repo.

| Fixture | Milestone | Attempted by | What ships |
|---|---|---|---|
| — | M0 | B, C, D | No fixture. Pass-on-sight: a merged PR that passed CI, plus `./gradlew build` on the student's own machine while a mentor watches |
| **`t1-find-the-bug/`** | M1 | B, C, D | **Three rotating variants** — off-by-one, inverted comparison, integer truncation. Each is one short method plus a failing test. Mentor picks one |
| **`t2-supplier-swap/`** | M2 | C, D | An interface with two implementations. Add a third and wire it in; then convert one method from `double` to `DoubleSupplier`. Given tests cover the third implementation; the supplier explanation is mentor-verified |
| **`t3-scheduler-conflict/`** | M3 | C, D | A working `RobotContainer` with three subsystems. Add a command requiring two, bind it, explain interruption. The code half is test-covered; the explanation is the pass criterion and is mentor-verified |
| **`t4-bad-kv/`** | M4 | D | A simulated mechanism with kV far too low and kP compensating, shipping with a pre-generated response plot. Reaches target, overshoots, oscillates |
| **`t5-drifting-auto/`** | M5 | D | **Three pre-recorded log sets**, one per root cause: odometry drift, mistuned path controller, blocking named command. Mentor picks one |

**Why the rotating variants.** A single fixed T1 leaks: the second student to attempt it hears the answer from the first. Three variants and a mentor's choice cost little to author and make the fixture reusable across a cohort. The same logic drives T5's three log sets.

**T5 is the most expensive artifact in this spec.** Three *convincing* logs — each showing a genuinely different failure signature, each with enough surrounding data that the other two causes can be ruled out by evidence — is real work. The pass criterion is explicitly "correct diagnosis *and* the ruling-out," so a log that only supports the right answer without excluding the wrong ones fails the fixture, not the student. Budget accordingly; it is scheduled last in §10 for that reason.

---

## 8. Solutions repository

### 8.1 Contents

`ignite-training-solutions`, private, mentor access only:

- A complete reference implementation of every milestone project
- An answer key per test-out fixture, including *why* each distractor is wrong — a mentor running T5 needs to know what evidence rules out the other two causes, not just which cause it is
- The M6 AdvantageScope diagnostic answers with the supporting log evidence
- Authoring notes: which trap each given test is aimed at, so a future maintainer does not "simplify" a test case that exists on purpose

### 8.2 Keeping it in sync

The obvious failure mode of D6 is a skeleton test changing and the solution silently no longer passing it. A scheduled CI job in the solutions repo:

1. Checks out `ignite-training` at `main`
2. Overlays the solution sources
3. Runs the full test suite
4. Fails loudly if anything is red

Run it weekly and on every push to either repository. Without this, D6's accepted cost is not actually mitigated — it is just deferred to whoever discovers the drift during a session.

---

## 9. Requirements the skeletons must satisfy

### 9.1 Anti-goals

| Must not | Why |
|---|---|
| Contain the answer in a comment | `// TODO: divide by ticksPerRotation` is the solution with extra steps |
| Be seeded from `CommandTemplate` | Stock WPILib sample code, still on 2025.2.1, no README, no CI, no Ignite conventions. `curriculum-spec.md` §8 is explicit: seed from `Robot-2026` and `Phoenix-2026` |
| Reference pre-2026 Ignite code | `curriculum-spec.md` §1.2. No student-facing path may point at `2025-Robotics-Intro`, `Robot-2025`, or earlier |
| Require hardware for any step | Simulation is mandatory for every project. Hardware variants stay optional enrichment |
| Require an AI tool | M0–M2 are Red tier. A skeleton whose setup assumes Copilot is unusable by the students it is aimed at |
| Assert on private implementation | Teaches naming-to-please-a-grader instead of correctness |
| Ship a test the student can trivially delete | See §9.2 |

### 9.2 Given tests must be tamper-evident

A student who cannot make a test pass can delete the test. This is not hypothetical and it is not malice — it is what a frustrated fifteen-year-old at minute forty does.

Three layers, in order of strength:

1. **`tests.lock`** — a SHA-256 manifest of every given test file and every read-only source file (M4's `plant/` package especially). `student-check.yml` verifies it and fails the PR on any mismatch, naming the modified file.
2. **`CODEOWNERS`** — given test paths require mentor review, so a change is at least visible.
3. **The failure message says so.** When the lock check fails, the CI comment reads *"this test is part of the milestone — if you think it is wrong, say so in the PR and a mentor will look."* A student who has found a genuinely bad test should be able to say so and be right. That happens, and the response should not be an accusation.

Layer 3 matters more than the other two. The check exists to make tampering visible, not to be uncircumventable — anyone determined can rewrite history. Visible is enough.

### 9.3 Every skeleton compiles on a clean clone

Stubs throw `UnsupportedOperationException`; they do not fail to compile. A student's first experience must be a green `./gradlew assemble` and a red `./gradlew test`, in that order. Compile errors on clone are indistinguishable from a broken toolchain, and a Track A student cannot tell the difference — which loses them a session to a problem that was never theirs.

---

## 10. Execution plan

Ordered by dependency, then by value. Each step is independently reviewable and independently useful.

| # | Step | Notes |
|---|---|---|
| **0** | **Spike the multi-project GradleRIO build.** Two subprojects, each with vendordeps, each running `simulateJava` and `test`. **Time-box to one session.** Fall back to standalone-projects-in-one-repo per §4.2 without argument. Also verify empirically whether `./gradlew build` runs tests under GradleRIO, for the §4.5 correction | Blocks everything |
| **1** | Repository scaffold: root build conventions, both CI workflows, `tests.lock` tooling, `CODEOWNERS`, PR template, `CONTRIBUTING.md`, `SETUP.md` | — |
| **2** | **M0** | Smallest, and the one every single student hits |
| **3** | **M1** + **T1** | Highest volume of students. L3 is the most authoring-heavy fade level per unit of content |
| **4** | **M2** + **T2** | — |
| **5** | **M3** + **T3** | Largest milestone. The `M3Deliverables` harness is the risky part — build it first and validate against a throwaway solution |
| — | — | **Kickoff Gate block complete (M0–M3). Ship it and stop if time runs out.** Everything below can land after the season starts |
| **6** | **M6** | Out of numeric order deliberately: it is the M7 gate, and the regression-patch verification in §6.7 is novel enough to want proving early |
| **7** | **M4** + **T4** | — |
| **8** | **M5** + **T5** | T5's three log sets are the single largest authoring cost in the spec |
| **9** | **M7**, **M8 templates** | Mostly documents |
| **10** | Solutions repository, built continuously alongside steps 2–9, with the §8.2 cross-check job live from step 2 | Not a final step. A milestone shipped without its solution is not shipped |
| **11** | **Doc updates.** `curriculum-spec.md` §8 status table, `README.md` status table, the §4.5 CI correction in three files, and the `resources.md` seasonal re-verification list | — |

**If only one thing gets built, build steps 0–5.** That is the Kickoff Readiness block, it is what gates commit access, and it is where the students who most need scaffolding actually are.

---

## 11. Acceptance criteria

- [ ] A student clones the repository, runs `./gradlew assemble`, and it is green before they write a line
- [ ] Every milestone's mechanical DoD items are covered by a given test; every explanation item is documented as mentor-verified, in the milestone file, unchanged
- [ ] `skeleton-integrity.yml` passes on `main`, including the inverted check that each skeleton's tests fail unmodified
- [ ] A student fork's `student-check.yml` produces a single green check a mentor can read without opening the code
- [ ] No skeleton references pre-2026 Ignite code, and every referenced path is pinned to a commit SHA
- [ ] Every project completes in simulation with no hardware
- [ ] Every test-out fixture has an answer key that explains the distractors, not just the answer
- [ ] The solutions cross-check job is green
- [ ] `tests.lock` verification correctly fails a PR that modifies a given test, with a message that invites disagreement
- [ ] The M6 regression check demonstrably fails for a student whose tests pass but catch nothing

---

## 12. Maintenance

The teaching repository joins the per-season re-verification pass alongside `resources.md`'s two halves. `README.md` currently names two things to re-verify each season; this adds a third.

Each season, before the fall:

1. Bump GradleRIO, vendordeps, and the Gradle wrapper to match `Robot-2026` and `Phoenix-2026`. **A skeleton that does not build with the toolchain M0 just installed is worse than no skeleton at all** — that is the exact failure mode `curriculum-spec.md` §1.2 was written to prevent, and it applies with more force to code than to prose
2. Re-pin every commit SHA in the `READING.md` files
3. Re-run the solutions cross-check
4. Re-generate T4's plot and T5's logs if the sim stack changed

Order matters: bump first, then re-pin, then cross-check. A cross-check against an un-bumped skeleton proves nothing useful.

---

## 13. Open items

| Item | Status |
|---|---|
| Does the multi-project GradleRIO build work? | **Unknown — spiked at step 0.** Fallback defined and cheap |
| Does `./gradlew build` run tests under GradleRIO's `configureTestTasks`? | **Unverified.** Determines whether three curriculum documents need a correction. Checked during the same spike |
| Who maintains `FEATURE-BACKLOG.md` in-season? | Unassigned. It goes stale fast and M7 depends on it |
| Should `templates/` live here or stay inline in `student-deliverables.md`? | Proposed: **both** — the prose stays for reading, the files exist for copying. Duplication risk is real; a CI check that the two match would resolve it |
| Is one worked-example test enough scaffolding for M6? | **Genuinely uncertain.** L0 is the sharpest drop on the fade curve and M6 has no test-out, so every student hits it at full density. Watch the first cohort; if they stall, the fix is a second worked example, not a lower bar |

---

## 14. Sources

**Peer FRC practice**
[FRC Java Koans — Team 3550](https://github.com/jletroui/FrcJavaKoans) · [FRC_2026_SoftwareLessons — Team 8744](https://github.com/FRC-Team8744/FRC_2026_SoftwareLessons) · [SciGuides — Team 1155](https://github.com/SciBorgs/SciGuides) · [FRC 1099 DiscoTechs Java Training](https://sites.google.com/student.brookfieldps.org/discotechs/frc-java-programming-training) · [Chief Delphi — How do you do programming lessons to new students?](https://www.chiefdelphi.com/t/how-do-you-do-programming-lessons-to-new-students/516927) · [Chief Delphi — FRC Programming Course: Share Your Methods](https://www.chiefdelphi.com/t/frc-programming-course-share-your-methods/475292)

**Tooling**
[classroom-resources/autograding-example-java](https://github.com/classroom-resources/autograding-example-java) · [GitHub Classroom — adding tests for autograding](https://classroom.github.com/help/adding-tests-for-auto-grading) · [wpilibsuite/docker-images](https://github.com/wpilibsuite/docker-images) · [Chief Delphi — Codespaces for FRC](https://www.chiefdelphi.com/t/github-codespaces-programming-in-your-browser-for-frc-2023-update/423814) · [Evan Pratten — Integrating GitHub Codespaces with FRC](https://ewpratten.com/blog/codespaces-for-frc/) · [WPILib Robot Simulation Introduction](https://docs.wpilib.org/en/stable/docs/software/wpilib-tools/robot-simulation/introduction.html) · [2026 KitBot Java Software Guide](https://firstfrc.blob.core.windows.net/frc2026/KitBot/2026-kitbot-java-guide.pdf)

**CS-education literature**
[Shin, Jung, Zumbach & Yi (2023) — Worked-Out Example and Metacognitive Scaffolding on Problem-Solving Programming](https://journals.sagepub.com/doi/abs/10.1177/07356331231174454) · [Fade-in vs. fade-out scaffolding for novice programmers, *J. Computing in Higher Education*](https://link.springer.com/article/10.1007/s12528-021-09307-w) · [Suggestions for graduated exposure to programming concepts using fading worked examples (ICER)](https://dl.acm.org/doi/abs/10.1145/1288580.1288594) · [Scaffolding Novices: When and How Parsons Problems Impact Novice Programming (ICER 2024)](https://dl.acm.org/doi/10.1145/3632620.3671110)

**Codebase claims** verified directly against `~/workspace/ignite/Robot-2026` and `~/workspace/ignite/Phoenix-2026` on 2026-08-06: build tooling versions, vendordep set, `pr-build-check.yml` contents, absence of `src/test` in both repos.

---

## 15. Audit

```
Queries sent: 6 | Sources received: 41 | Sources cited: 17
Failures: 1 (Chief Delphi returned HTTP 403 to automated fetch — same bot-blocking
             class as the ReadTheDocs issue in curriculum-spec.md §1.1; the thread
             is cited from search-result metadata only, not fetched content)
Routing decision: fallback (no research specialist matched)
Sub-questions: (1) how peers distribute starter code; (2) what "skeleton" means per
level; (3) how completion is verified automatically; (4) what environment floor
removes install friction
```
