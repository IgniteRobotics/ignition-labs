# Ignite Robotics — Programming Curriculum Spec

**Status:** Approved spec, not yet executed
**Owner:** Josh Reddick
**Created:** 2026-08-05
**Team:** FRC 6829 Ignite Robotics
**Backlog item:** "Build programming fall curriculum — include how/when/where/why to use AI. Cover what docs and deliverables students own in-season and how they build them."

---

## 1. Purpose

This document specifies **what to build**, not the curriculum itself. It defines the structure, the milestone ladder, the entry points, the deliverable files, and the acceptance criteria for each. Subsequent work executes against this spec.

The curriculum it describes must serve four distinct populations without being written four times, must work year-round rather than only in a fall block, must be verifiable by a thin mentor bench, and must take an explicit position on AI-assisted development that no other FRC team has yet published.

### 1.1 What already exists

This spec is built on three audited research reports in the team wiki, not from scratch:

| Report | What it supplies |
|---|---|
| `frc-programming-curriculum-research` | Topic map, pacing skeleton from 8 peer curricula across 7 teams, WPILib's official three-tier structure |
| `frc-coding-best-practices-research` | WPILib/CTRE best practices plus a gap analysis against Ignite's actual repos |
| `frc-ai-coding-standards-research` | FIRST's rules posture, Chief Delphi sentiment, CS-education literature on novices and AI |
| `frc-fall-curriculum-research-audit` | Reconciliation pass; found and corrected 2 errors across the three |

**Three further corrections found while writing this spec and `resources.md`.** None of them undermine the research's conclusions, but all three need fixing in the wiki — tracked as step 7 of the execution plan.

**1. Epilogue is deployed, not aspirational.** `frc-coding-best-practices-research` states that `Robot-2026` has "zero hits for `@AutoLog`, `LoggedRobot`, `Epilogue`" and that the team's structured-logging aspiration "is not yet implemented in the actual codebase." This is **incorrect**. Epilogue is deployed across 13 source files: `Robot.java` imports `edu.wpi.first.epilogue.Epilogue`, calls `Epilogue.configure(...)` at line 44 and `Epilogue.bind(this)` at line 61, and every subsystem plus `RobotContainer`, `LaunchState`, `ShiftState`, and `LaunchRequest` carry `@Logged` annotations with `Importance` levels. The original grep was faulty. The rest of that row stands — `Robot` still extends `TimedRobot`, there is no AdvantageKit vendordep, and there is no IO-interface abstraction layer — but Ignite is using WPILib's native telemetry framework properly and the curriculum should teach Epilogue as an existing team convention rather than as an aspiration.

**2. Two WPILib doc URLs are stale.** WPILib restructured the command-based documentation under a `commands-v2/` subdirectory. The paths the research cites for "Structuring a Command-Based Robot Project" and "Organizing Command-Based Projects" now 404. Current paths are in `resources.md` §1.

**3. CTRE's simulation-intro page is not a 404.** The research recorded it as one. The page exists at the cited path; the failure was ReadTheDocs bot-blocking, which returns spurious 429s to automated clients and was misread as a missing page.

### 1.2 Design constraints (decided, not open)

| Constraint | Decision |
|---|---|
| Track structure | One spine, four documented entry points, all tracks converge |
| Calendar | Year-round / evergreen, with an explicit pre-season completion gate |
| Hardware floor | **Desktop simulation is mandatory for every project.** Last season's competition robot is available as optional hardware validation |
| Scope | Curriculum documents + AI-use standard + in-season student deliverables spec |
| Out of scope | Rewriting `CommandTemplate`, building an LMS or tracking site |
| Reference codebases | **2026 only.** `Phoenix-2026` is the reading-scale repo; `Robot-2026` is the full-complexity reference. No milestone cites pre-2026 Ignite code |
| Format | Markdown, importable to both Notion and GitHub without rework |

**On the reference-codebase constraint.** Curriculum content ages through its code references faster than through its prose. The rule is that every Ignite file a student is sent to read must be on the current WPILib and vendordep generation, so that `./gradlew build` on a reference repo works with the toolchain M0 just installed. Re-checking this is part of the per-season re-verification pass (see §9.1 and `README.md`), alongside the external link check.

---

## 2. Architecture: one spine, four entry points

Four separate curricula would mean four documents to maintain and four places for content to drift. Peer team 1099 (DiscoTechs) solved the identical mixed-audience problem by branching the *track* while sharing the *content*, and that is the model adopted here.

```
M0  Tooling, Git & the Build             <-- Track A enters
M1  Java Fundamentals                    <-- Track B enters (test-out of M1)
M2  OOP, Interfaces & Lambdas
M3  WPILib Command-Based Programming     <-- Track C enters (test-out of M1-M2)
M4  Sensors, Actuators & Control
M5  Autonomous, Odometry & Vision
M6  Simulation, Telemetry & Testing      <-- Track D enters (test-out of M1-M5)
M7  AI-Assisted Development
M8  In-Season Deliverables & Ownership
        |
        v
   All tracks converge
```

### 2.1 The four entry points

| Track | Audience | First milestone worked | Test-outs attempted |
|---|---|---|---|
| **A** | New to programming entirely | M0 | — |
| **B** | Has programmed, but not in an OO language (Python scripts, Scratch, Arduino C, MATLAB) | M0 | M1 |
| **C** | Comfortable with OO (Java, C++, C#), no FRC or WPILib experience | M0 | M0, M1, M2 |
| **D** | FRC-experienced, wants to level up with AI | M0 | M0 through M5 |

**M0 is universal.** Every track starts there, including veterans — but M0's test-out is trivial for anyone who already has the toolchain: show a merged PR that passed CI. A Track C student who has used git professionally clears it in minutes. A Track C student who has never touched Gradle or the WPILib extension does not, and that is exactly the routing this catches.

Track A students walk the whole ladder. **No track skips M6 onward** — the convergence point is deliberate and non-negotiable.

### 2.2 Why Track D enters at M6 and not M7

Ignite's veteran programmers are the target audience for AI-assisted development, and they are also the population with the weakest verification habits. `Robot-2026` has JUnit 5 configured in `build.gradle` with a `test { useJUnitPlatform() }` block and **no `src/test` directory at all**. `Robot-2025` has zero tests. `Robot-2024` had exactly one. Neither CI workflow runs `./gradlew test`. The `Phoenix-2026` benchtop rig — which exists precisely for simulated hardware work — has no test directory either.

Handing agentic AI tooling to programmers with no automated verification layer produces velocity without a safety net. Every substantive reply in the Chief Delphi thread ["Software training for new students in the AI age"](https://www.chiefdelphi.com/t/sofware-training-for-new-students-in-the-ai-age/522478) converged on teaching fundamentals before introducing AI, and the CSTA/TeachAI joint guidance states plainly that "people with a greater grasp of underlying computing concepts are able to use AI tools more effectively than those without."

M6 is therefore the price of admission to M7. Veterans test out of M1–M5 quickly — most will clear it in one or two sessions — but they build the test harness before they get the agent.

### 2.3 Test-out mechanism

Skipping a milestone requires passing its **test-out challenge**, not self-assessment. Each challenge is:

- Time-boxed to 30–60 minutes
- Verifiable by a mentor without reading the student's code line by line
- Focused on the milestone's load-bearing concept, not trivia

A student who fails a test-out enters at that milestone. No penalty, no discussion — the challenge exists to route people correctly, not to rank them.

---

## 3. Kickoff Readiness Gate

Delivery is evergreen, so calendar pressure concentrates in a single gate rather than a weekly schedule. This is what "must be completed prior to season start" means operationally.

| Role at kickoff | Required milestones |
|---|---|
| Any programming subteam member | **M0, M1, M2, M3** |
| Anyone committing to competition robot code | **+ M4, M6** |
| Autonomous or vision owner | **+ M5** |
| Anyone using AI tooling on competition code | **+ M7** and a signed AI-use standard |

M8 is explicitly *not* gated — it is the in-season milestone and runs during the season itself.

**Enforcement:** the gate is a roster checklist a mentor maintains, not an honor system. A student who has not cleared M0–M3 by kickoff can still participate but does not get commit access to the season repo. This is a real consequence and it must be stated to students in advance, in the fall, not discovered in January.

---

## 4. Milestone specifications

Every milestone file follows an identical section structure. This is a hard requirement — consistency is what makes the ladder navigable and what lets a mentor verify a milestone they did not personally teach.

### 4.1 Required section structure for every `milestones/mN-*.md`

```
# MN — Title

## At a glance
(one-paragraph summary, estimated time, prerequisite milestones)

## Who enters here
(which tracks start at this milestone, if any)

## Test-out challenge
(the 30-60 min challenge to skip this milestone; explicit pass criteria)

## Concepts
(the teachable content, broken into subsections)

## Project
(the hands-on deliverable; simulation-mandatory variant first, optional
 hardware variant second)

## Definition of done
(observable, mentor-verifiable checklist; every item is a yes/no)

## Common failure modes
(what goes wrong and what it looks like when it does)

## Resources
(primary docs first, peer curricula second, Ignite codebase references third)

## AI posture
(Red / Yellow / Green for this milestone; what is and is not allowed)
```

### 4.2 M0 — Tooling, Git & the Build

**Enters here:** all tracks. Tracks C and D will normally clear the test-out immediately; Tracks A and B work the milestone.

**Concepts.** WPILib VS Code installation and the FRC toolchain. What Gradle does and why `./gradlew build` matters. Reading a stack trace. Git as a concept before git as commands: commits as save points, branches as parallel universes, merges as reconciliation. The curated command set — `clone`, `checkout -b`, `add -u`, `commit -m`, `fetch --all`, `merge`, `push` — deliberately narrower than git's full surface. This mirrors a pattern reported on Chief Delphi by a team that hands rookies an explicit allow-list rather than all of git, keeping them productive without exposing `rebase` and `reset` before they can recover from either. Pull requests and code review as a team ritual. What Spotless and Google Java Format do and why formatting consistency is not bikeshedding.

**Project (simulation-mandatory).** Fork the Ignite teaching repo. Create a branch. Make a trivial but real change. Open a pull request. Get it through `spotlessCheck` in CI. Respond to at least one review comment. Merge it.

The project is deliberately not about code quality — it is about surviving the mechanics of contribution. A student who cannot open a passing PR in October will not be able to contribute in January.

**Test-out challenge.** Produce a merged PR on any repo that passed a CI check, and run `./gradlew build` successfully on an FRC project on your own machine while a mentor watches. 15 minutes for anyone who already has the toolchain. This is the only test-out that is pass-on-sight rather than assessed.

**Definition of done.**
- [ ] WPILib VS Code installed; `./gradlew build` succeeds locally
- [ ] Simulator launches and the student can identify the Driver Station panel
- [ ] A merged PR exists under the student's GitHub account
- [ ] `spotlessCheck` passed without a mentor fixing it for them
- [ ] Student can explain what a branch is without using the word "branch"

**AI posture:** 🔴 Red — AI as explainer only. Students may ask an AI what a stack trace means. They may not have it generate git commands to paste blindly.

**Resources.** [WPILib Zero to Robot](https://docs.wpilib.org/en/stable/docs/zero-to-robot/introduction.html) · [WPILib Git Version Control Introduction](https://docs.wpilib.org/en/stable/docs/software/basic-programming/git-getting-started.html) · [Robot Code CI with GitHub Actions](https://docs.wpilib.org/en/stable/docs/software/advanced-gradlerio/robot-code-ci.html) · Ignite reference: `Robot-2026/.github/workflows/pr-build-check.yml`

### 4.3 M1 — Java Fundamentals

**Enters here:** Track B (via test-out attempt)

**Scope discipline.** This milestone teaches the Java required to program a robot, not the Java language. Team 3550's FRC Java Koans author framed the cut precisely: "the goal is not to teach the entirety of the Java language, but rather the fundamentals required to start learning how to program a FRC robot," designed so students "do not stay too long in pure, abstract, programming territories." Team 8744's 2026 lesson repo makes the same cut and says so out loud — it covers Java's eight primitive types, then narrows immediately to `int` and `double` because that is what robot code uses.

Reported pacing from teams running this: **hours, not weeks.** One instructor reports roughly four hours of logic and syntax before students were driving a robot. Anything longer loses the room.

**Concepts.** Variables and the types that matter (`int`, `double`, `boolean`, and why `double` dominates in robot code). Operators. Control flow: `if`/`else`, `switch`, `for`, `while`. Methods, parameters, return values. Arrays and `List`. Reading a compiler error. Constants and why they are `static final` and SCREAMING_SNAKE_CASE.

**Project (simulation-mandatory).** A set of robot-flavored console exercises: convert encoder ticks to rotations, clamp a joystick value to a deadband, compute a gear ratio, decide whether a sensor reading is within tolerance. Every exercise is a real computation that appears somewhere in Ignite's actual codebase.

**Test-out challenge.** Given a short Java method with a deliberate bug — an off-by-one, an inverted comparison, an integer-division truncation — find and fix it, and explain what was wrong. 30 minutes.

**Definition of done.**
- [ ] All exercises compile and pass their provided assertions
- [ ] Student can read a compiler error and locate the offending line unaided
- [ ] Student can explain why `1/2` is `0` in Java
- [ ] Student wrote at least one method that takes parameters and returns a value

**AI posture:** 🔴 Red — this is the milestone where AI use is most damaging and least detectable. See §6.

**Resources.** [FRC Java Koans (Team 3550)](https://github.com/jletroui/FrcJavaKoans) · [Team 8744 Lesson01_BasicJava](https://github.com/FRC-Team8744/FRC_2026_SoftwareLessons) · [Team 3255 FRC Java Tutorial](https://frcteam3255.github.io/FRC-Java-Tutorial/)

### 4.4 M2 — OOP, Interfaces & Lambdas

**Concepts.** Classes and objects as a way to bundle state with behavior. Fields, constructors, methods. `public`/`private` and why encapsulation matters when four people share a repo. Inheritance and when *not* to use it. Interfaces as contracts. The singleton pattern — taught here because Ignite's `statemachines/` package uses it and students will encounter `LaunchState.getInstance()` on day one. Lambdas and functional interfaces: `Supplier<Double>`, `DoubleSupplier`, `BooleanSupplier`, `Runnable` — this is the highest-leverage topic in the milestone, because WPILib's entire command and trigger API is built on them and a student who does not understand `() -> value` cannot read `RobotContainer`.

**Project (simulation-mandatory).** Build a unit-conversion class with tests — ticks to rotations to RPM, with a configurable gear ratio and encoder resolution. The class is written from scratch; there is deliberately no Ignite file to copy from, which is what makes it safe under Red tier. Students then read `Robot-2026/src/main/java/frc/robot/preferences/` — `RobotPreference<T>` is an abstract generic class implementing `Supplier<T>` with four concrete subclasses, and it carries generics, abstraction, inheritance, and the supplier concept in five short files. It also connects the milestone's lambda material directly to M3, which a unit converter cannot do.

**Test-out challenge.** Given an interface and two implementing classes, add a third implementation and wire it in. Then rewrite one method to take a `DoubleSupplier` instead of a `double` and explain the difference in when the value is read. 45 minutes.

**Definition of done.**
- [ ] Conversion class has at least four passing JUnit tests
- [ ] Student can explain the difference between passing a value and passing a supplier
- [ ] Student can read `Robot-2026`'s `LaunchState.getInstance()` and explain what a singleton guarantees
- [ ] Student wrote at least one lambda

**AI posture:** 🔴 Red — same reasoning as M1. Lambdas in particular are a concept students must struggle with; AI-generated lambdas are trivially producible and teach nothing.

**Resources.** Ignite reference: `Robot-2026/src/main/java/frc/robot/preferences/` (abstract generic class implementing `Supplier<T>` — the highest-value read in the milestone), `Robot-2026/src/main/java/frc/robot/statemachines/LaunchState.java`

### 4.5 M3 — WPILib Command-Based Programming

**Enters here:** Track C

This is the spine's center of gravity and the largest milestone. It is the last milestone in the "everyone must have this before kickoff" block.

**Concepts.** The `TimedRobot` lifecycle and the 20ms loop. What a subsystem is and why it owns hardware. What a command is and why it has `initialize`/`execute`/`end`/`isFinished`. **Both command idioms** — explicit `Command` subclasses *and* subsystem-hosted factory methods returning `Command` — with an explicit explanation of why WPILib moved toward factories and when a full subclass is still correct. Command compositions: sequential, parallel, race, and decorators like `.withTimeout()`. Triggers and `CommandXboxController` bindings. The command scheduler and requirement-based interruption. Project structure: `Robot.java` thin, `RobotContainer.java` declarative, subsystems as private fields injected into commands rather than reached globally.

**Why both idioms are mandatory.** Ignite's competition code (`Robot-2026`) uses `CommandXboxController` triggers, subsystem factory methods, and static `Commands.parallel(...)` composition — and contains **zero** `extends Command` subclasses. The benchtop rig (`Phoenix-2026`) carries the subclass idiom in `commands/DriveToPose.java`. Both repos are on WPILib 2026.2.1 and Phoenix 6, so a student sees both idioms without leaving the current toolchain. A student taught only one idiom cannot read the other. Teaching both, with the reason for the shift, closes a gap the research explicitly flagged.

**Project (simulation-mandatory).** Read `Phoenix-2026` end to end first — 22 files on the current toolchain, containing both idioms, and small enough to finish in a sitting. Then build a two-subsystem robot in simulation: a drivetrain and one mechanism. Write one command as an explicit subclass and one as a factory method. Compose them into a sequence. Bind both to controller buttons. Run it in the simulator and demonstrate it.

**Optional hardware variant.** Deploy the same code to last season's competition robot.

**Test-out challenge.** Given a working `RobotContainer` with three subsystems, add a new command that requires two of them, bind it to a trigger, and explain what happens if it is scheduled while another command already requires one of those subsystems. 60 minutes.

**Definition of done.**
- [ ] Robot runs in simulation; both commands execute from button presses
- [ ] Student wrote one command each way and can articulate the tradeoff
- [ ] Student can explain what happens when two commands require the same subsystem
- [ ] Subsystems are private fields in `RobotContainer`, not globals or statics
- [ ] Student can trace one full loop iteration out loud

**AI posture:** 🟡 Yellow begins here — see §6. AI may generate boilerplate and Javadoc. AI may not design the subsystem decomposition or write the command logic.

**Resources.** [Command-Based Programming index](https://docs.wpilib.org/en/stable/docs/software/commandbased/index.html) · [Structuring a Command-Based Robot Project](https://docs.wpilib.org/en/stable/docs/software/commandbased/commands-v2/structuring-command-based-project.html) · [Organizing Command-Based Projects](https://docs.wpilib.org/en/stable/docs/software/commandbased/commands-v2/organizing-command-based.html) · Ignite reference: `Phoenix-2026/` (read end to end), `Phoenix-2026/src/main/java/frc/robot/commands/DriveToPose.java` (subclass idiom), `Robot-2026/src/main/java/frc/robot/RobotContainer.java` (full complexity)

### 4.6 M4 — Sensors, Actuators & Control

**Concepts.** Motor controllers and the CAN bus: device addressing, the roboRIO's 20ms heartbeat, the 100ms self-disable timeout, and why bus utilization above 90% causes indeterminate behavior. Encoders — quadrature, absolute, and integrated CAN encoders. Gyros and IMUs. Limit switches and homing.

Control theory at FRC depth: P as a software spring, D as a software damper, and WPILib's own position that the I term is "generally not recommended for FRC use." Feedforward before feedback — WPILib states directly that "an accurate feedforward is more important than feedback to effective control of a mechanism," and the curriculum teaches it in that order deliberately. `SimpleMotorFeedforward`, `ArmFeedforward`, `ElevatorFeedforward` and the kS/kV/kA/kG terms. Motion profiling. Combining feedforward and PID. SysId for characterization.

CTRE Phoenix 6 specifics, because that is what Ignite runs: `TalonFXConfiguration` and config objects as passive data structures, method chaining, `.clone()` to share a base config across near-identical devices, `apply()` being blocking and init-only, and status signals — caching them, batch-refreshing with `BaseStatusSignal.refreshAll()`, and `waitForAll()` for synchronized multi-device reads.

**Note on an Ignite-specific teaching opportunity.** `Robot-2026`'s `DriveMotorConfigs.java` contains four near-identical `create*Slot0Configs()` methods, one per swerve corner, each repeating the same five-field assignment with different constants. CTRE's own guidance recommends `.clone()`-and-override for exactly this case. This is a genuine, small, safe refactor that makes an excellent M4 stretch project on real team code.

**Project (simulation-mandatory).** Closed-loop control of a flywheel in simulation. Characterize it with SysId, apply feedforward, add PID, tune it, and produce a plot showing the response. Then do the same for a position-controlled arm with gravity feedforward.

**Optional hardware variant.** Same exercise on last season's robot or a benchtop mechanism.

**Test-out challenge.** Given a simulated mechanism with deliberately wrong gains — a kV that is far too low and a P that is compensating for it — diagnose which term is wrong from the response plot alone, fix it, and explain why the original tuning was fragile. 60 minutes.

**Definition of done.**
- [ ] Flywheel reaches and holds a commanded velocity in simulation
- [ ] Arm holds position against simulated gravity
- [ ] Student can explain what each of kS, kV, kA, kG does
- [ ] Student can explain why feedforward is tuned before P
- [ ] Student produced a plot and can read it

**AI posture:** 🟡 Yellow. AI may not choose gains or design the control strategy.

**Resources.** [Introduction to PID](https://docs.wpilib.org/en/stable/docs/software/advanced-controls/introduction/introduction-to-pid.html) · [Feedforward Control](https://docs.wpilib.org/en/stable/docs/software/advanced-controls/controllers/feedforward.html) · [CAN Addressing](https://docs.wpilib.org/en/stable/docs/software/can-devices/can-addressing.html) · [Phoenix 6 Status Signals](https://v6.docs.ctr-electronics.com/en/stable/docs/api-reference/api-usage/status-signals.html) · [Phoenix 6 Configuration](https://v6.docs.ctr-electronics.com/en/stable/docs/api-reference/api-usage/configuration.html) · [Team 5712 Gray Matter Coding Workshop](https://www.frc5712.com/)

### 4.7 M5 — Autonomous, Odometry & Vision

**Concepts.** Kinematics and odometry — how wheel and gyro readings become a field-relative pose. Swerve specifics. Pose estimation and why odometry drifts. Trajectory following. PathPlanner, which Ignite already uses (`PathplannerLib` is a live vendordep and `RobotContainer` builds autos through `AutoBuilder` and `NamedCommands`). Choreo as the alternative, taught as a comparison rather than a competitor — the community consensus is explicitly "use whichever you already know," with ramp-up time being the limiting factor rather than capability.

Vision: AprilTags, camera calibration, and fusing vision measurements into pose estimation. PhotonVision, which Ignite runs (`photonlib` vendordep, `VisionSubsystem.java`). Limelight as the alternative. The same "use what you know" logic applies.

Autonomous routines as command compositions, and named commands as the bridge between path files and robot behavior.

**Project (simulation-mandatory).** Build a two-piece autonomous routine in simulation: follow a PathPlanner path, trigger a mechanism via a named command, follow a second path. Separately, demonstrate AprilTag pose estimation correcting simulated odometry drift.

**Test-out challenge.** Given an autonomous routine that drifts off its path partway through, determine from the logs whether the cause is odometry drift, a mistuned path-following controller, or a named command blocking longer than expected — and say what evidence rules out the other two. 60 minutes.

**Definition of done.**
- [ ] Auto routine completes in simulation and is visible in AdvantageScope
- [ ] Student can explain what odometry drift is and why vision corrects it
- [ ] Student can add a named command and wire it into a path
- [ ] Student can articulate one real tradeoff between PathPlanner and Choreo

**AI posture:** 🟡 Yellow.

**Resources.** [WPILib Kinematics and Odometry](https://docs.wpilib.org/en/stable/docs/software/kinematics-and-odometry/index.html) · [PathPlanner docs](https://pathplanner.dev/) · [Choreo docs](https://choreo.autos/) · [PhotonVision docs](https://docs.photonvision.org/) · Ignite reference: `Robot-2026/src/main/java/frc/robot/subsystems/vision/VisionSubsystem.java`

### 4.8 M6 — Simulation, Telemetry & Testing

**Enters here:** Track D

This milestone exists because Ignite does not do it, and because it is the gate to M7.

**Concepts.** WPILib's desktop simulator and how to run robot code without hardware. Glass, Shuffleboard, and AdvantageScope. Reading a log after the fact instead of guessing during a match. **Epilogue** — Ignite's actual telemetry framework, already deployed across 13 files with `@Logged` and `Importance` levels; students learn the convention that exists rather than inventing one.

JUnit 5, which is already configured in `Robot-2026/build.gradle` and entirely unused. Writing a test against a simulated subsystem. The `AutoCloseable` teardown pattern WPILib recommends. CTRE's `SimState` and the documented timing quirks — `DriverStationSim.setEnabled(true)` plus `DriverStation.notifyNewData()`, a ~100ms delay after constructing devices, a ~20ms delay after sending a control request before asserting.

**Two cautions that must be taught explicitly, not discovered:**

1. **Do not instantiate the whole robot in a test.** A WPILib maintainer's guidance on this is direct: `RobotBase` (and therefore `TimedRobot`) "is not designed to ever exit," and full-robot tests hang CI on background threads (`robot main`, `DataLogDS`, `NTListener`, `MotorSafety`). Test scoped pieces of logic, not the robot.
2. **Do not write tests that re-assert the simulator's own model.** A test that verifies CTRE's simulated motor behaves like CTRE says it behaves tests CTRE, not Ignite.

**The bar is deliberately low.** Ignite is starting from zero tests, not improving an existing suite. The framing that fits, borrowed from a practitioner in the community: perfect TDD is a trap for teams with no tests, because people "see the hype, hate the process, and end up avoiding writing good tests (or any)." The goal of M6 is a student who has written a real test that catches a real regression — not a convert to test-first development.

**Project (simulation-mandatory).** Write a JUnit test suite for one subsystem's logic against simulated hardware. Deliberately introduce a regression and demonstrate the test catching it. Then take a real log from last season, open it in AdvantageScope, and answer a specific diagnostic question about a match.

**Test-out challenge.** There is none that skips this milestone. M6 is the convergence point; every track completes it.

**Definition of done.**
- [ ] At least three passing JUnit tests exist against a simulated subsystem
- [ ] Student demonstrated a test failing on an introduced regression, then passing after the fix
- [ ] `./gradlew test` runs green locally
- [ ] Student answered a diagnostic question from a real AdvantageScope log
- [ ] Student can explain why not to instantiate `TimedRobot` in a test

**AI posture:** 🟢 Green unlocks on completion of this milestone, not during it. Tests in M6 are written by the student.

**Resources.** [WPILib Unit Testing](https://docs.wpilib.org/en/stable/docs/software/wpilib-tools/robot-simulation/unit-testing.html) · [Robot Simulation Introduction](https://docs.wpilib.org/en/stable/docs/software/wpilib-tools/robot-simulation/introduction.html) · [Phoenix 6 Unit Testing](https://v6.docs.ctr-electronics.com/en/stable/docs/api-reference/wpilib-integration/unit-testing.html) · [AdvantageScope docs](https://docs.advantagescope.org/) · Ignite reference: `Robot-2026/src/main/java/frc/robot/Robot.java` (Epilogue configuration), `Robot-2026/build.gradle` (JUnit, already wired)

### 4.9 M7 — AI-Assisted Development

**Prerequisite:** M6 complete. No exceptions, including for veterans.

**Concepts.** What LLM coding tools actually do and where they fail. The distinction the whole standard rests on: **interface-level trust versus decision-level trust.** You do not defend CTRE Phoenix's internals — you defend its contract. You *do* defend AI-generated control logic, because that is an engineering decision, not a dependency. This reframe is what survives the strongest counter-argument in the community, that calling a `.set()` into a 50,000-line closed-source vendor library and calling it student-written is already a compromise, so AI is a difference of degree rather than kind.

Prompting as a modeled skill, demonstrated live rather than described. AI as reviewer and explainer rather than author — the CodeAid pattern, where the tool answers conceptual questions, produces annotated pseudocode, and points at mistakes without handing over the answer. Reading and critiquing AI-generated code. Recognizing the two documented over-reliance failure modes by name: **shepherding** (spending your time coercing the tool into producing what you already knew you wanted) and **drifting** (hesitantly accepting a suggestion, half-understanding it, backtracking, and getting progressively more lost).

Ignite's existing Gemini PR-review bot (`.github/workflows/ai-code-review.yml`, `.github/prompts/code-review-prompt.md`) is a live, in-house example of AI-as-reviewer and should be taught as such. Notably, that prompt file is currently the *only* place Ignite's coding standards are written down, and only a bot reads it.

**Project (simulation-mandatory).** Ship a real feature using AI assistance under the Green tier of the standard: disclosed in the PR, covered by tests the student wrote or verified, and then **defended in review** — the student walks a mentor through every decision in the diff and explains why it is correct. Any line the student cannot defend gets removed and rewritten.

**Definition of done.**
- [ ] Feature merged with an AI-use disclosure in the PR description
- [ ] Tests pass and cover the new behavior
- [ ] Student defended every decision in the diff, unaided, in review
- [ ] Student can name shepherding and drifting and describe a time they did one
- [ ] Student has read and signed the Ignite AI-use standard

**Resources.** `ai-use-standard.md` (this curriculum) · [The Widening Gap (Prather et al., 2024)](https://arxiv.org/html/2405.17739v1) · [FIRST: Expanding the FIRST Toolbox with AI](https://community.firstinspires.org/expanding-the-first-toolbox-with-artificial-intelligence) · Ignite reference: `Robot-2026/.github/prompts/code-review-prompt.md`

### 4.10 M8 — In-Season Deliverables & Ownership

**Runs during the season, not before it.** This milestone is the answer to the backlog item's second half: what students own in-season and how they build it. Full detail lives in `student-deliverables.md`; this milestone is the teaching wrapper around it.

**Concepts.** Owning a subsystem end to end. Writing the subsystem README a future student will read. Keeping a tuning log so that gains have a recorded derivation instead of being magic numbers. Competition-week git discipline — tagging before and after every event so there is a known-good state to fall back to, and never editing code on the field without committing it. Pit documentation. The build-season cadence and what "done" means in each week.

**Week-by-week reference cadence**, adapted from team 937's published programming schedule: subsystems and simple commands for every mechanism by end of Week 1; basic autonomous Week 2; game-piece mechanism tuning Week 3; advanced autonomous Week 4; endgame mechanism tuning Week 5; stress testing and driver practice Week 6; iterate after.

**What programmers do in Week 0 and 1 while mechanical builds.** This is a real and recurring problem, and the community answer is concrete: start simulation-based subsystem logic as soon as the design is finalized, and work on code and pathing against last year's robot in the meantime.

**Project.** Own one real subsystem for the season, including all of its documentation deliverables.

**Definition of done.**
- [ ] Subsystem README exists and a student who did not write it can follow it
- [ ] Tuning log exists with derivations, not just final values
- [ ] Repo was tagged before and after each competition
- [ ] Pit documentation exists for the subsystem
- [ ] Post-season retro written

**AI posture:** 🟢 Green, under the standard.

**Resources.** `student-deliverables.md` · [Team 937 Build Season Schedule](https://frc937.gitbook.io/frc-team-937/programming/build-season-schedule)

---

## 5. Deliverable files

```
docs/programming/
├── README.md                    # index, how to use, track selector
├── curriculum-spec.md           # this document
├── ai-use-standard.md           # the signed policy
├── student-deliverables.md      # in-season ownership + doc templates
├── resources.md                 # curated link library
├── tracks/
│   ├── track-a-new-to-programming.md
│   ├── track-b-non-oo-programmer.md
│   ├── track-c-oo-no-frc.md
│   └── track-d-frc-veteran-ai.md
└── milestones/
    ├── m0-tooling-and-git.md
    ├── m1-java-fundamentals.md
    ├── m2-oop-interfaces-lambdas.md
    ├── m3-wpilib-command-based.md
    ├── m4-sensors-actuators-control.md
    ├── m5-autonomous-odometry-vision.md
    ├── m6-simulation-telemetry-testing.md
    ├── m7-ai-assisted-development.md
    └── m8-in-season-deliverables.md
```

### 5.1 Per-file acceptance criteria

| File | Must contain | Done when |
|---|---|---|
| `README.md` | Curriculum overview, the spine diagram, a track-selector decision tree, the Kickoff Readiness Gate table, links to everything else | A new student can self-route to the correct track in under 5 minutes without asking a mentor |
| `tracks/*.md` | Who this track is for, entry milestone, which test-outs to attempt, expected time, what "done" looks like, links into the milestone files | Reads as a standalone onboarding page; no content duplicated from milestone files, only linked |
| `milestones/*.md` | Exactly the section structure in §4.1 | Every DoD item is objectively verifiable; every resource link resolves |
| `ai-use-standard.md` | The three tiers, the interface-vs-decision-trust principle, disclosure format, the defense-review process, a signature block | A mentor can adjudicate a disputed case using only this document |
| `student-deliverables.md` | Subsystem README template, tuning log template, pit doc template, competition-week git checklist, week-by-week cadence | Templates are fill-in-the-blank, not prose descriptions of what a template would contain |
| `resources.md` | Every external link used anywhere in the curriculum, grouped by topic, each with a one-line note on what it is good for | No link appears in a milestone file that is absent here; **every URL returns 200 at time of writing** |

**Link verification status: complete.** All 64 external URLs are verified as of 2026-08-05 — see the verification log in `resources.md` §9. ReadTheDocs-hosted sites block automated clients from this network, so WPILib and CTRE paths were confirmed against their documentation source repositories instead, which is a stronger check. That pass surfaced two stale paths (see §1.1).

### 5.2 Markdown constraints

Content must render correctly in **both** Notion (via import) and GitHub. That means:

- No raw HTML
- No list nesting deeper than three levels
- Standard pipe tables only; no merged cells
- Full absolute URLs for external links
- Repo-relative paths for internal links, with the caveat that Notion will not resolve them — internal links must therefore also be understandable as plain text
- Fenced code blocks with an explicit language tag
- No footnote syntax; use inline parenthetical attribution
- Emoji are acceptable and used deliberately for the AI tier markers (🔴🟡🟢)

---

## 6. AI-Use Standard — design requirements

Full text goes in `ai-use-standard.md`. This section specifies what that document must decide.

### 6.1 Grounding

FIRST has **no rule** governing AI-assisted robot code. The 2026 Game Manual contains no mention of AI, ChatGPT, Copilot, LLMs, or generative AI anywhere in the robot construction rules, inspection rules, or elsewhere. The only official FIRST position is a November 2023 community blog post from the EVP/COO, which is permissive-with-attribution, aimed more at award submissions than competition code, predates the current generation of coding agents, and explicitly says FIRST may revisit it.

No FRC team was found to have published a formal AI-use policy. Ignite would be among the first. That is an opportunity and a risk: there is no field-tested language to adapt, but the practitioner community has already converged informally on three norms — **disclose AI use, the human retains responsibility, teach fundamentals before introducing AI**. The standard formalizes those rather than inventing from nothing.

### 6.2 The three tiers

Modeled on MIT Course 6, where AI policy varies by what a specific course is trying to build in a student rather than following one department-wide rule.

**🔴 Red — M0 through M2.** No AI code generation. AI permitted only as an explainer: what does this error mean, what does this keyword do, explain this concept differently. Never "write me a method that does X."

*Rationale:* this is where the harm is best documented. Prather et al. found that students who struggled accepted AI suggestions at 34.1% versus 24.5% for students who did not — the students least equipped to evaluate output were the most likely to accept it, and "most of these ten who struggled thought they understood more than they actually did." A rookie-programming instructor's firsthand account matches: students who lean on AI early "create LOTS of really bad code really quickly" and require re-teaching of "fundamental programming concepts like declaring/initializing variables, constructors, what a parameter is."

**🟡 Yellow — M3 through M5.** AI permitted for boilerplate, Javadoc, repetitive patterned code, and stack-trace triage. **Not permitted** for control logic, subsystem decomposition, or autonomous structure. Disclosure required on any PR containing AI-assisted code.

*Rationale:* the excluded categories are precisely the engineering decisions these milestones exist to teach. The included categories are the ones where community practice already sits — inline autocomplete on three near-identical `stop*()` methods is a convenience, not a substitute for understanding.

**🟢 Green — on completion of M6, and thereafter.** Yellow still applies *during* M6 itself; the tests that unlock Green are written by the student, not generated. Once M6 is signed off, full agentic use is permitted, gated on three conditions, all of which must hold:

1. Tests exist, cover the new behavior, and pass
2. AI use is disclosed in the PR description, naming the tool and what it did
3. The author defends every decision in the diff during review

*Rationale:* M6 supplies the verification layer that makes velocity safe. The defense review is the actual enforcement mechanism — it is the one check that cannot be gamed by a tool.

### 6.3 The load-bearing principle

The standard must state this explicitly, because it is what makes the policy defensible against the strongest student objection:

> The question is not who typed the characters. The question is whether the person submitting the code can explain and defend every decision in it.
>
> You do not defend a vendor library's internals — you defend its interface contract. You *do* defend AI-generated control logic, because that is an engineering decision the assignment exists to teach, not a dependency you consume.

This is **interface-level trust versus decision-level trust**, and it draws a cleaner line than "AI versus not-AI."

### 6.4 Disclosure format

A required PR-description block, short enough that people actually fill it in:

```
AI use: [none | assisted | generated]
Tool:
What it did:
How I verified it:
```

The community norm this codifies is real and already operating — but the standard must also take the sharper position that appeared in response to it: disclosure does not transfer responsibility. "The person who created code remains responsible for whatever it does... responsibility must always lie with a human." Disclosing that AI wrote something is not a defense for it being wrong.

### 6.5 Required sections in `ai-use-standard.md`

- Scope and why this document exists
- What FIRST does and does not require
- The three tiers, with the milestone mapping
- The interface-vs-decision-trust principle
- Disclosure format and where it goes
- The defense review: what it is, who runs it, what failing looks like
- Named failure modes: shepherding, drifting, false confidence
- Mentor guidance: this closes the same loophole as the covert-takeover problem
- Review date — this document expires and must be revisited each season
- Signature block

### 6.6 A note the standard must include

The mentor-takeover problem and the AI-takeover problem are the same failure wearing two faces. Prior team research identified programming as FRC's number-one covert mentor-takeover zone: a broken autonomous routine visibly loses matches, so a mentor under deadline pressure is tempted to quietly take the keyboard. A rookie handing a broken subsystem to an AI produces an identical outcome — working code, no learning — with a different actor. A policy that addresses only one closes half the loophole.

---

## 7. Student deliverables spec — design requirements

Full content in `student-deliverables.md`. Required artifacts:

| Artifact | Owner | Cadence | Template required |
|---|---|---|---|
| Subsystem README | Subsystem owner | Created Week 1, updated on change | Yes |
| Tuning log | Subsystem owner | Every tuning session | Yes |
| PR description with AI disclosure | Author | Every PR | Yes |
| Competition-week git tags | Programming lead | Before and after each event | Checklist |
| Pit documentation | Subsystem owner | Before first event | Yes |
| Post-season retro | Whole subteam | End of season | Prompts |

**Templates must be fill-in-the-blank.** A description of what a good README contains is not a template. The acceptance test is that a student can produce a compliant document by filling in blanks without writing prose from scratch.

**Why the tuning log matters specifically.** `Robot-2026`'s constants are undocumented magic numbers with no recorded derivation. A tuning log converts "these gains work" into "these gains work because," which is the difference between a value that survives a mechanism change and one that gets re-derived from zero every season.

---

## 8. Known gaps and deliberate exclusions

| Item | Status | Note |
|---|---|---|
| `CommandTemplate` is stock WPILib sample code, still on WPILib 2025.2.1 | **Excluded from scope, still the highest-leverage fix** | Every season inherits generic sample code with no README, no CI, no Ignite conventions. **It must be bumped to 2026 before it seeds a 2027 season repo.** The curriculum will produce a teaching repo as a byproduct; seed that from `Robot-2026` and `Phoenix-2026` conventions, not from `CommandTemplate` |
| `2025-Robotics-Intro` is retired from the curriculum | **Fixed** | It is a Romi project on WPILib 2025.2.1 with one vendordep, last touched 2025-02-11 — and §1.2 excludes Romi as a hardware floor, so three milestones sending students into it was a self-contradiction. All nine references were retargeted to 2026 repos: M1 and M2 to `Robot-2026` (`Constants`, `IntakeConstants`, `preferences/`), M3's subclass idiom to `Phoenix-2026/commands/DriveToPose.java`. **No student-facing path now references pre-2026 Ignite code** |
| No README in any Ignite repo | **Partially addressed** | M8's subsystem README deliverable creates the habit; the repo-level READMEs remain unwritten |
| Ignite's standards live only in a bot prompt | **Addressed indirectly** | `.github/prompts/code-review-prompt.md` is the best existing draft of Ignite's conventions and should be mined when writing M3–M6 content. Converting it to a human-facing doc is out of scope here |
| Romi / XRP practice robots | **Optional enrichment only** | Not selected as a hardware floor; no milestone depends on them |
| `Phoenix-2026` benchtop rig | **Promoted: M3 depends on it as reading material** | Reclassified from optional enrichment. It is on WPILib 2026.2.1, Phoenix 6 26.1.0, PathPlanner 2026.1.2 and photonlib — the same stack as `Robot-2026` — with both CI workflows, Spotless, and `useJUnitPlatform()` configured, in **22 Java files versus `Robot-2026`'s 48**. It is the only current Ignite repo containing an explicit `Command` subclass. M3 now assigns it as a read-end-to-end target with `Robot-2026` as the full-complexity reference. Its *hardware* use remains optional — the M4 hardware variant is still enrichment |
| Tracking site / LMS | **Explicitly excluded** | A peer resource drew real criticism for collecting student names and emails without a deletion path. If Ignite ever builds one, that is a separate project with its own privacy review |

---

## 9. Execution plan

Work proceeds in this order. Each step is independently reviewable.

1. **`resources.md`** — build the link library first. Everything else cites it, and building it first prevents link drift across nine milestone files.
2. **`ai-use-standard.md`** — write the policy before the milestone that teaches it, because M7's content depends on the tier definitions being settled.
3. **`milestones/m0` through `m8`** — in order. M3 and M4 are the largest; budget accordingly.
4. **`student-deliverables.md`** — templates, written after M8 so the milestone and the artifacts stay consistent.
5. **`tracks/*.md`** — written last, because a track guide is a routing document over content that must already exist.
6. **`README.md`** — written last for the same reason.
7. **Wiki correction** — fix the Epilogue claim in `frc-coding-best-practices-research` §Gap Analysis and note it in `frc-fall-curriculum-research-audit`.

### 9.1 Acceptance criteria for the curriculum as a whole

- [ ] A student with no programming experience can self-route to Track A and start unaided
- [ ] A returning veteran can test out through M5 in one session and know exactly what they still owe
- [ ] A mentor can verify any milestone's definition of done without having taught it
- [ ] Every project is completable with simulation alone
- [ ] Every external link resolves
- [ ] Every file imports cleanly into Notion with no manual fixup
- [ ] The AI standard can adjudicate a real disputed case without further interpretation

---

## 10. Sources

Primary research lives in the team wiki: `frc-programming-curriculum-research`, `frc-coding-best-practices-research`, `frc-ai-coding-standards-research`, reconciled in `frc-fall-curriculum-research-audit`. Those files carry full citation ledgers and verification tiers.

Codebase claims in this spec were verified directly against `~/workspace/ignite/Robot-2026` on 2026-08-05, including the Epilogue correction in §1.1.
