# Ignite Programming Curriculum — Resource Library

**Last verified:** 2026-08-05
**Companion to:** `curriculum-spec.md`

Every external link used anywhere in the curriculum lives here. Milestone files link *to this file* rather than carrying their own copies, so a moved or dead URL gets fixed in one place instead of nine.

**Rule:** if a link appears in a milestone file, it must appear here. If it does not appear here, it does not go in a milestone file.

The **Used by** column maps each resource to the milestones that cite it, so you can see at a glance what a student at a given stage actually needs to open.

---

## 1. WPILib official documentation

The spine of the curriculum. When WPILib's docs and a peer team's tutorial disagree, WPILib wins.

| Resource | What it's good for | Used by |
|---|---|---|
| [WPILib Docs home](https://docs.wpilib.org/en/stable/) | Top-level table of contents; the four-tier structure the curriculum's spine is modeled on | All |
| [Zero to Robot](https://docs.wpilib.org/en/stable/docs/zero-to-robot/introduction.html) | The official four-step setup path: wiring, software install, roboRIO prep, first program | M0 |
| [Git Version Control Introduction](https://docs.wpilib.org/en/stable/docs/software/basic-programming/git-getting-started.html) | Deliberately introductory git walkthrough. Also warns not to hand-edit the generated `.gitignore` | M0 |
| [Robot Code CI with GitHub Actions](https://docs.wpilib.org/en/stable/docs/software/advanced-gradlerio/robot-code-ci.html) | Why the build runs on every PR and what it checks | M0 |
| [Command-Based Programming index](https://docs.wpilib.org/en/stable/docs/software/commandbased/index.html) | Landing page for the whole command-based section | M3 |
| [What Is Command-Based Programming?](https://docs.wpilib.org/en/stable/docs/software/commandbased/what-is-command-based.html) | The conceptual "why" — read before any of the mechanics | M3 |
| [Commands](https://docs.wpilib.org/en/stable/docs/software/commandbased/commands.html) | `initialize` / `execute` / `end` / `isFinished`, and both authoring idioms | M3 |
| [Subsystems](https://docs.wpilib.org/en/stable/docs/software/commandbased/subsystems.html) | Why a subsystem owns hardware and what "requirements" mean | M3 |
| [Command Compositions](https://docs.wpilib.org/en/stable/docs/software/commandbased/command-compositions.html) | Sequential, parallel, race, and the decorators like `.withTimeout()` | M3 |
| [Binding Commands to Triggers](https://docs.wpilib.org/en/stable/docs/software/commandbased/binding-commands-to-triggers.html) | `CommandXboxController` and the trigger API Ignite's real code uses | M3 |
| [The Command Scheduler](https://docs.wpilib.org/en/stable/docs/software/commandbased/command-scheduler.html) | What actually runs your commands, and how interruption works | M3 |
| [Structuring a Command-Based Robot Project](https://docs.wpilib.org/en/stable/docs/software/commandbased/structuring-command-based-project.html) | Thin `Robot.java`, declarative `RobotContainer`, subsystems as private fields | M3 |
| [Organizing Command-Based Projects](https://docs.wpilib.org/en/stable/docs/software/commandbased/organizing-command-based.html) | WPILib's own take on factory methods vs. subclasses, and the named anti-patterns | M3 |
| [Introduction to PID](https://docs.wpilib.org/en/stable/docs/software/advanced-controls/introduction/introduction-to-pid.html) | P as a software spring, D as a damper, and why WPILib says the I term is "generally not recommended for FRC use" | M4 |
| [Feedforward Control](https://docs.wpilib.org/en/stable/docs/software/advanced-controls/controllers/feedforward.html) | kS / kV / kA / kG and the three feedforward classes. States plainly that accurate feedforward matters more than feedback | M4 |
| [System Identification (SysId)](https://docs.wpilib.org/en/stable/docs/software/advanced-controls/system-identification/index.html) | Characterizing a mechanism instead of guessing gains | M4 |
| [CAN Bus Addressing](https://docs.wpilib.org/en/stable/docs/software/can-devices/can-addressing.html) | Device addressing, the 20ms heartbeat, the 100ms self-disable timeout | M4 |
| [Kinematics and Odometry](https://docs.wpilib.org/en/stable/docs/software/kinematics-and-odometry/index.html) | How wheel and gyro readings become a field-relative pose | M5 |
| [Robot Simulation Introduction](https://docs.wpilib.org/en/stable/docs/software/wpilib-tools/robot-simulation/introduction.html) | Running robot code with no hardware — the backbone of every project in this curriculum | M3, M4, M5, M6 |
| [Unit Testing](https://docs.wpilib.org/en/stable/docs/software/wpilib-tools/robot-simulation/unit-testing.html) | JUnit 5 is already available in every WPILib Java project. Also covers the `AutoCloseable` teardown pattern | M6 |
| [Robot Telemetry with Annotations (Epilogue)](https://docs.wpilib.org/en/stable/docs/software/telemetry/robot-telemetry-with-annotations.html) | `@Logged` and `Importance` — the telemetry framework Ignite already runs in production | M6 |

> **Path note.** WPILib has since removed the `commands-v2/` subdirectory it previously used for the command-based pages. Links that still include it — including the ones previously listed here — now 404. The URLs above omit `commands-v2/` and are the current ones, confirmed live against `docs.wpilib.org` on 2026-08-07.

---

## 2. Vendor documentation

What Ignite's robot actually runs on. Every one of these corresponds to a live vendordep in `Robot-2026`.

| Resource | What it's good for | Used by |
|---|---|---|
| [Phoenix 6: Status Signals](https://v6.docs.ctr-electronics.com/en/stable/docs/api-reference/api-usage/status-signals.html) | Caching signals, `BaseStatusSignal.refreshAll()`, `waitForAll()`, and why bus utilization over 90% causes indeterminate behavior | M4 |
| [Phoenix 6: Configuration](https://v6.docs.ctr-electronics.com/en/stable/docs/api-reference/api-usage/configuration.html) | Config objects, method chaining, `.clone()` for near-identical devices, and why `apply()` is init-only | M4 |
| [Phoenix 6: Simulation Introduction](https://v6.docs.ctr-electronics.com/en/stable/docs/api-reference/simulation/simulation-intro.html) | `SimState`, driving a physics model from `getMotorVoltage()`, and keeping sim code out of robot code | M4, M6 |
| [Phoenix 6: Unit Testing](https://v6.docs.ctr-electronics.com/en/stable/docs/api-reference/wpilib-integration/unit-testing.html) | The documented timing quirks — `DriverStationSim.setEnabled(true)`, the ~100ms and ~20ms delays. Read this before writing your first Phoenix test or you will chase a ghost | M6 |
| [PathPlanner](https://pathplanner.dev/) | Ignite's actual autonomous tooling. `AutoBuilder` and `NamedCommands` are already wired in `RobotContainer` | M5 |
| [Choreo](https://choreo.autos/) | The alternative. Taught as a comparison, not a competitor | M5 |
| [PhotonVision](https://docs.photonvision.org/) | Ignite's actual vision stack (`photonlib` vendordep, `VisionSubsystem.java`) | M5 |
| [Limelight](https://docs.limelightvision.io/) | The alternative. Same "use what you know" logic as PathPlanner vs. Choreo | M5 |

---

## 3. Tooling

| Resource | What it's good for | Used by |
|---|---|---|
| [AdvantageScope](https://docs.advantagescope.org/) | Log visualization and diagnostics. The M6 project requires answering a real question from a real match log | M4, M5, M6 |
| [AdvantageKit](https://docs.advantagekit.org/) | Deterministic log replay and the IO-abstraction pattern. **Ignite does not use this** — it is a "what's next" pointer for veterans, not required by any milestone | M6 (optional) |

---

## 4. Peer team curricula

Useful for structure and sequencing. **Treat specific technical claims with suspicion** and verify against WPILib — see the caution below.

| Resource | What it's good for | Used by |
|---|---|---|
| [FRC Java Koans (Team 3550)](https://github.com/jletroui/FrcJavaKoans) | Interactive Java course requiring zero prior programming experience, scoped deliberately to what robot programming needs | M1 |
| [Team 8744 — 2026 Software Lessons](https://github.com/FRC-Team8744/FRC_2026_SoftwareLessons) | A live, in-progress 2026 fall curriculum — the closest analog to what Ignite is building. Lesson01 teaches Java through an immediate robotics hook rather than in the abstract | M1, M3 |
| [Team 3255 — FRC Java Tutorial](https://frcteam3255.github.io/FRC-Java-Tutorial/) | Full public tutorial site; a good template for what a from-scratch curriculum's table of contents looks like | M0, M1, M3 |
| [Team 5712 — Gray Matter Coding Workshop](https://www.frc5712.com/) | Closed-loop control taught on a CTRE benchtop rig, then whole-robot autonomy. Vendor-matched to Ignite's stack | M4, M5 |
| [Team 937 — Build Season Schedule](https://frc937.gitbook.io/frc-team-937/programming/build-season-schedule) | Concrete week-by-week programming deliverables. The direct source for M9's cadence | M9 |
| [Team 2554 — Documentation site](https://docs.jpsrobotics2554.org/) | Example of a beginner/advanced split within one team's docs | Reference |
| [LearnFRC](https://learnfrc.com/) | Useful for **sequencing and pacing only** — see caution | Reference |

> **Caution on LearnFRC.** It is an individual project by one team's software lead, not an official resource, and it discloses that its lessons are AI-assisted. The community found real errors — one widely-cited example is an intro unit listing kelvin, moles, and candelas as relevant units of measurement in FRC. Use it for structure; do not copy its content. The sharper critique worth carrying into Ignite's own writing came from the same thread: most FRC documentation "only makes sense if you already know what you're looking for." Write so that is not true of ours.

---

## 5. Reference codebases

Real competition code worth reading. None of these are Ignite's conventions — they are examples of what mature teams do.

| Resource | What it's good for | Used by |
|---|---|---|
| [Team 254 — FRC-2025-Public](https://github.com/Team254/FRC-2025-Public) | The README is the model: setup instructions, a prose "code highlights" walkthrough, an explicit naming-conventions section | M9 |
| [Team 3061 — 3061-lib](https://github.com/HuskieRobotics/3061-lib) | The closest external analog to what `CommandTemplate` should be: a maintained scaffold with a documented architecture | Reference |
| [Team 2910 — 2025 Competition Robot](https://github.com/FRCTeam2910/2025CompetitionRobot-Public) | The Superstructure pattern for coordinating mechanisms — a pattern Ignite's own code-review prompt already aspires to | M3, Reference |
| [AdvantageKit](https://github.com/Mechanical-Advantage/AdvantageKit) | Source and template projects for the IO-abstraction pattern | M6 (optional) |

---

## 6. AI use — policy and research

Everything backing `ai-use-standard.md`. The tiers are not arbitrary; each one traces to something here.

| Resource | What it's good for | Used by |
|---|---|---|
| [FIRST — Expanding the FIRST Toolbox with AI](https://community.firstinspires.org/expanding-the-first-toolbox-with-artificial-intelligence) | FIRST's only official AI position (Nov 2023). Permissive-with-attribution, aimed more at awards than robot code, and explicitly subject to change | M7 |
| [The Widening Gap (Prather et al., 2024)](https://arxiv.org/abs/2405.17739) | **The load-bearing citation for the Red tier.** Struggling students accepted AI suggestions at 34.1% vs 24.5% for non-struggling students, and "most of these ten who struggled thought they understood more than they actually did." Introduces the idea that novices must develop "negative expertise" — the ability to ignore bad suggestions | M7 |
| ["It's Weird That it Knows What I Want" (Prather et al., 2023)](https://arxiv.org/abs/2304.02491) | Origin of **shepherding** and **drifting**, the two over-reliance failure modes M7 requires students to be able to name | M7 |
| [AI Code Generators and Novice Learners (Kazemitabaar et al., CHI 2023)](https://arxiv.org/abs/2302.07427) | **The counterpoint, and it must be taught alongside the Widening Gap.** 69 novices aged 10–17 — an FRC-comparable population — showed improved performance with no retention penalty. Critically, the design paired every AI-assisted task with an unaided follow-up. Scaffolded AI use is not the same as unrestricted AI use | M7 |
| [Beyond the Benefits: harms review (2025)](https://arxiv.org/abs/2510.04443) | Reviews 224 papers into six harm categories. Its honest caveat matters: many claimed harms "remain insufficiently supported by empirical evidence." Do not overclaim what the research shows | M7 |
| [Harvard CS50 — Academic Honesty Policy](https://cs50.harvard.edu/x/2026/honesty/) | A strict tool-specific exception model. CS50 built its own Socratic "duck" rather than policing general-purpose tools | M7 |
| [UMD — Sample Syllabus Language for AI Policies](https://tltc.umd.edu/sample-syllabus-language-ai-course-policies) | Four reusable policy templates spanning the permissiveness spectrum. Useful when wording the standard | M7 |

---

## 7. Chief Delphi threads

Practitioner discussion. Higher signal than blog posts, lower authority than official docs. All verified live on 2026-08-05.

**On AI:**

| Thread | Why it matters |
|---|---|
| [Software training for new students in the AI age](https://www.chiefdelphi.com/t/sofware-training-for-new-students-in-the-ai-age/522478) | A mentor asks Ignite's exact question. Every substantive reply converges on teaching without AI first — the direct source for the Red tier |
| [How is your team using AI/LLMs for Robot Code in 2026?](https://www.chiefdelphi.com/t/how-is-your-team-using-ai-llms-for-robot-code-in-2026/513448) | Real teams at both extremes, including one whose vibe-coded control system failed on day one of competition and had to be rewritten |
| [Student written code](https://www.chiefdelphi.com/t/student-written-code/520752) | The vendor-library reframe the AI standard has to answer: is calling a 50,000-line closed-source library meaningfully different from AI-generated boilerplate? |
| [The Next Revolution: AI in FRC (Team 254, 2026 Champs)](https://www.chiefdelphi.com/t/2026-championship-conference-presentation-the-next-revolution-ai-in-frc-by-254/519529) | The most credentialed programming team in FRC going public with heavy AI use — read by the community as a mid-season reversal |

**On testing and code quality:**

| Thread | Why it matters |
|---|---|
| [JUnit tests hanging in GitHub Action](https://www.chiefdelphi.com/t/junit-tests-hanging-in-github-action-but-not-when-run-in-vscode/449746) | **Read before writing your first test.** A WPILib maintainer explains why you must not instantiate `TimedRobot` in a test — it is not designed to ever exit, and CI will hang |
| [No, You Don't Need A Robot To Test Your Code](https://www.chiefdelphi.com/t/no-you-dont-need-a-robot-to-test-your-code/167375) | Source of the framing M6 adopts: perfect TDD is a trap for a team with zero tests. "Just do what you can" |
| [Programming style resources](https://www.chiefdelphi.com/t/programming-style-resources/425512) | Why formatting consistency is not bikeshedding — most code cost is maintenance, not authorship |
| [How does your team use git and GitHub?](https://www.chiefdelphi.com/t/how-does-your-team-use-git-and-github/338720) | Source of M0's curated command allow-list, and the practice of tagging before and after every competition |

**On tooling choices and season planning:**

| Thread | Why it matters |
|---|---|
| [Limelight vs PhotonVision](https://www.chiefdelphi.com/t/limelight-vs-photonvision/452856) | The real consensus is not technical: "use the software you have the most experience with." Ramp-up time is the limiting factor |
| [Choreo vs PathPlanner](https://www.chiefdelphi.com/t/choreo-vs-pathplanner/467373) | Same conclusion. Switching costs are low; familiarity decides. Settle this in the fall, not in Week 1 |
| [Biding Time for Programmers](https://www.chiefdelphi.com/t/biding-time-for-programmers/509817) | What programmers do in Week 0–1 while mechanical builds: simulation work and last year's robot. Directly informs M9 |

**On other teams' training programs:**

| Thread | Why it matters |
|---|---|
| [Team 3255 Retrobots rookie program](https://www.chiefdelphi.com/t/frc-2020-in-2025-team-3255s-rookie-training-program-retrobots/508492) | Rebuilding a prior season's robot as the teaching vehicle — breadth-first exploration before specialization |
| [FRC Java Koans launch thread](https://www.chiefdelphi.com/t/team-3550-presents-frc-java-koans-for-teaching-java-to-rookie-students/438711) | The author's reasoning for scoping Java down, plus a pacing data point: students were driving a Romi after minutes of orientation, not hours |
| [FRC 5712 Coding Workshop Resources](https://www.chiefdelphi.com/t/frc-5712-coding-workshop-resources/508952) | Benchtop mechanisms that run on a CANivore with no roboRIO — the pattern Ignite's `Phoenix-2026` rig already matches |
| [LearnFRC launch thread](https://www.chiefdelphi.com/t/learnfrc-resource/521987) | The critique thread. Read it for the failure mode, not the resource |

---

## 8. Ignite's own code

Internal references. Paths are relative to `~/workspace/ignite/`. These are not links — open them locally.

| Path | What it demonstrates | Used by |
|---|---|---|
| `Robot-2026/src/main/java/frc/robot/RobotContainer.java` | The idiom students will actually meet in January: `CommandXboxController` triggers, `@Logged`, `AutoBuilder`, `NamedCommands` | M3 |
| `Robot-2026/src/main/java/frc/robot/Robot.java` | Epilogue configuration and binding — `Epilogue.configure()` and `Epilogue.bind(this)` | M6 |
| `Robot-2026/src/main/java/frc/robot/statemachines/LaunchState.java` | The singleton pattern students hit on day one | M2 |
| `Robot-2026/src/main/java/frc/robot/statemachines/LaunchCalculator.java` | Real applied math: interpolating tree maps and parabolic ballistics | M4, M5 |
| `Robot-2026/src/main/java/frc/robot/subsystems/shooter/ShooterSubsystem.java` | Closed-loop velocity control with CTRE, typed units, `@Logged` importance levels | M4 |
| `Robot-2026/src/main/java/frc/robot/subsystems/vision/VisionSubsystem.java` | PhotonVision integration and pose fusion. Also one of only two well-documented files in the repo | M5 |
| `Robot-2026/src/main/java/frc/robot/subsystems/drive/DriveMotorConfigs.java` | **A deliberate teaching target.** Four near-identical `create*Slot0Configs()` methods where CTRE's `.clone()` pattern would cut real duplication. Safe, small, real refactor | M4 |
| `Robot-2026/build.gradle` | JUnit 5 is configured and entirely unused. The M6 starting point | M6 |
| `Robot-2026/.github/workflows/pr-build-check.yml` | The CI your Robot-2026 pull requests must pass — build plus `spotlessCheck`. Not this repository's CI: `ignition-labs` runs its own `student-check.yml`, which is what your M0 pull request is checked by | M4 |
| `Robot-2026/.github/prompts/code-review-prompt.md` | How Ignite's coding standards get enforced. The standards themselves are written down in `coding-standards.md`, which is the source; this prompt is downstream of it | M3–M7 |
| [OpenSpec](https://github.com/Fission-AI/OpenSpec) ([docs](https://github.com/Fission-AI/OpenSpec/tree/main/docs)) | The spec format M7 writes in. Delta-based: `openspec/specs/` holds settled behaviour, `openspec/changes/` holds one folder per in-flight change, `openspec/changes/archive/` holds merged ones. **Required at M7** — the CLI needs Node 20.19+, and Ignite writes OpenSpec's real format, not a simplification of it | M7 |
| `Robot-2026/src/main/java/frc/robot/preferences/` | `RobotPreference<T>` — an abstract generic class implementing `Supplier<T>`, with four concrete subclasses. Generics, abstraction, inheritance, and the supplier idea in five short files | M2 |
| `Robot-2026/src/main/java/frc/robot/Constants.java` and `.../intake/IntakeConstants.java` | `static final` in production — and three different naming conventions across two files, none of them written down | M1 |
| `Phoenix-2026/` (whole repo) | **The reading-scale 2026 codebase.** 22 Java files on WPILib 2026.2.1 + Phoenix 6 + PathPlanner + photonlib, with both CI workflows. Same stack as the competition robot, half the size. Read end to end in M3 | M3 |
| `Phoenix-2026/src/main/java/frc/robot/commands/DriveToPose.java` | The explicit `Command` subclass idiom on the 2026 toolchain. `Robot-2026` contains no `extends Command` at all, so this is the only place in current Ignite code to see it | M3 |
| `Phoenix-2026/build.gradle` | Spotless pinned to 6.25.0 while `Robot-2026` is on 8.2.1. A real, small, safe version-drift fix on team code | M0 (stretch) |
| `docs/VISION_GUIDE.md` | PhotonVision setup for the team's actual camera hardware | M5 |
| `docs/vision-subsystem.md` | The multi-camera pose fusion pipeline in prose | M5 |
| `docs/field-boundary-speed-limiting.md` | An already-written, already-assignable student design project | M4 (stretch) |

---

## 9. Verification log

| Group | Count | Method | Result |
|---|---|---|---|
| WPILib docs | 22 | Path-verified against the `wpilibsuite/frc-docs` source tree | All confirmed. Found the `commands-v2/` restructure that invalidated 2 previously-cited URLs |
| CTRE Phoenix 6 | 4 | Path-verified against the `CrossTheRoadElec/Phoenix6-Documentation` source tree | All confirmed, including `simulation-intro` which prior research had recorded as a 404 |
| PathPlanner, Choreo, Limelight, AdvantageScope, AdvantageKit | 5 | HTTP | 200 |
| Peer curricula and reference repos | 11 | HTTP | 200 |
| AI research and policy sources | 7 | HTTP | 200 |
| Chief Delphi threads | 15 | HTTP via Discourse JSON API | 200 |

**Why two groups were path-verified instead of fetched.** ReadTheDocs-hosted sites (`docs.wpilib.org`, `v6.docs.ctr-electronics.com`, `docs.photonvision.org`) return HTTP 429 to automated clients from this network regardless of pacing or user agent. Checking the documentation source repositories is a stronger check anyway — it confirms the page exists at that exact path, and it is what surfaced the `commands-v2` move that a simple 200-check would have missed on the old URLs.

**Re-verify this file at the start of each season.** WPILib restructures its docs between seasons; this pass found two stale paths inside a research file written five months ago.
