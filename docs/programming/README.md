# Ignite Robotics — Programming Curriculum

FRC 6829 · Programming subteam

Everything you need to go from wherever you are to owning a subsystem during a competition season.

---

## Which track am I?

Answer the first question that describes you and stop.

| If this is you | You are | Start here |
|---|---|---|
| I have never really written code | **Track A** | [Track A — New to Programming](tracks/track-a-new-to-programming.md) |
| I have written code, but never a class — Python, Arduino, MATLAB, Scratch, JS | **Track B** | [Track B — Programmed, Not Object-Oriented](tracks/track-b-non-oo-programmer.md) |
| I am comfortable in Java, C++, or C#, but have never touched WPILib | **Track C** | [Track C — OO Experience, No FRC](tracks/track-c-oo-no-frc.md) |
| I have programmed for an FRC team for a season or more | **Track D** | [Track D — FRC Veteran, Leveling Up With AI](tracks/track-d-frc-veteran-ai.md) |

Not sure between two? **Pick the earlier one.** The test-outs will move you up in under an hour if you were being modest. Starting too high costs you weeks of confusion in M3.

---

## How this works

**One ladder, four entry points.** There is a single sequence of nine milestones. Your track determines where you get on and which test-outs you attempt — not what you eventually learn. Everyone converges.

```
M0  Tooling, Git & the Build          <-- Track A, B, C, D all start here
M1  Java Fundamentals                 <-- Track B tests out
M2  OOP, Interfaces & Lambdas
M3  WPILib Command-Based              <-- Track C's real starting point
M4  Sensors, Actuators & Control
M5  Autonomous, Odometry & Vision
M6  Simulation, Telemetry & Testing   <-- Track D's real starting point
M7  AI-Assisted Development
M8  In-Season Deliverables & Ownership
```

**Test-outs, not self-assessment.** Skipping a milestone means passing a 30–60 minute challenge a mentor verifies. Failing one is routing information, not a judgment — you work that milestone, no penalty and no record.

**Simulation is mandatory, hardware is optional.** Every project is completable on your own laptop. You never wait for the robot, and you cannot break anything.

**Definitions of done are objective.** Every milestone ends in a checklist where each item is a yes or no. A mentor who never taught the milestone can verify it. Nobody is comparing your pace to anyone else's.

---

## The milestones

| # | Milestone | What you can do afterward |
|---|---|---|
| **M0** | [Tooling, Git & the Build](milestones/m0-tooling-and-git.md) | Get a pull request merged past CI |
| **M1** | [Java Fundamentals](milestones/m1-java-fundamentals.md) | Read and write the Java robot code uses |
| **M2** | [OOP, Interfaces & Lambdas](milestones/m2-oop-interfaces-lambdas.md) | Understand what WPILib is built on |
| **M3** | [WPILib Command-Based](milestones/m3-wpilib-command-based.md) | Read our actual robot code |
| **M4** | [Sensors, Actuators & Control](milestones/m4-sensors-actuators-control.md) | Make a mechanism go where you told it |
| **M5** | [Autonomous, Odometry & Vision](milestones/m5-autonomous-odometry-vision.md) | Fifteen seconds with no driver |
| **M6** | [Simulation, Telemetry & Testing](milestones/m6-simulation-telemetry-testing.md) | Know your code is wrong before the robot does |
| **M7** | [AI-Assisted Development](milestones/m7-ai-assisted-development.md) | Use AI tools without shipping what you cannot explain |
| **M8** | [In-Season Deliverables](milestones/m8-in-season-deliverables.md) | Own a subsystem through a season |

---

## Kickoff Readiness Gate

The curriculum runs year-round. This is the one hard deadline.

| Your role at kickoff | You need |
|---|---|
| Any programming subteam member | **M0, M1, M2, M3** |
| Committing to competition robot code | **+ M4, M6** |
| Autonomous or vision owner | **+ M5** |
| Using AI tooling on competition code | **+ M7**, and a signed AI-use standard |

**This has a real consequence.** If you have not cleared M0–M3 by kickoff, you can still participate, but you do not get commit access to the season repository. That is stated here in advance rather than discovered in January.

M8 is not gated — it runs during the season.

---

## AI tooling

**Read [the AI use standard](ai-use-standard.md) before using any AI coding tool on team code.** You sign it at M7.

The short version: your tier is set by the milestones you have completed.

| Tier | Milestones | What you can do |
|---|---|---|
| 🔴 **Red** | M0–M2 | Explanation only. **No code generation. Turn Copilot off** |
| 🟡 **Yellow** | M3–M5 | Boilerplate, Javadoc, stack-trace triage. **Not** control logic, subsystem design, or gains. Disclosure required |
| 🟢 **Green** | After M6 | Full agentic use — gated on tests passing, disclosure, and defending the diff in review |

FIRST has no rule about this and no other FRC team has published a policy. This is ours, we own it, and it is written to be argued with. If you think a tier boundary is wrong, bring the argument.

---

## Everything in this folder

| File | What it is |
|---|---|
| [`README.md`](README.md) | This page |
| [`git-workflow.md`](git-workflow.md) | How you use git at every milestone — clone, fork, and branch, and when each applies |
| [`robot-repo-ladder.md`](robot-repo-ladder.md) | The four levels of access to the real robot repositories, and which milestone unlocks each |
| [`curriculum-spec.md`](curriculum-spec.md) | The design document. Why the curriculum is shaped this way. For mentors and anyone editing it |
| [`teaching-repo-spec.md`](teaching-repo-spec.md) | Spec for the skeleton source code students start each project from. For mentors and anyone building it |
| [`ai-use-standard.md`](ai-use-standard.md) | The AI policy. Required reading, signed at M7 |
| [`student-deliverables.md`](student-deliverables.md) | Fill-in-the-blank templates for everything you produce in-season |
| [`resources.md`](resources.md) | Every external link in the curriculum, with notes on what each is good for |
| [`tracks/`](tracks/track-a-new-to-programming.md) | The four entry-point guides, starting with Track A |
| [`milestones/`](milestones/m0-tooling-and-git.md) | M0 through M8, starting with M0 |

---

## For mentors

Start with [`curriculum-spec.md`](curriculum-spec.md) — it records every design decision and the reasoning, including the ones students will push back on.

The three most likely to generate friction:

1. **Track D enters at M6, not M7** (spec §2.2). Veterans write tests before they get agentic tooling. This is the most contested decision in the curriculum and the reasoning is measured, not asserted — our repos contain zero test files across three seasons
2. **Red tier forbids all code generation through M2** (`ai-use-standard.md` §4). Students who know these tools work will find this arbitrary. The reasoning is in the standard and it is worth explaining rather than enforcing
3. **The Kickoff Gate gates commit access.** It only works if someone maintains the roster checklist and it is announced in the fall

`ai-use-standard.md` §10 is written for you specifically. The short version: the mentor-takeover problem and the AI-takeover problem are the same failure with different actors, and this standard binds mentors harder than students.

---

## Status

| Piece | State |
|---|---|
| Spec, resources, AI standard, all nine milestones, deliverables, tracks | **Written** |
| Teaching repository for M0–M3 + M6 projects | **M0–M3, M6 built** — `projects/m0-onboarding/` builds from a clean clone and its simulator starts, and `projects/m1-java-fundamentals/`, `projects/m2-oop/`, and `projects/m3-command-based/` with their test-out fixtures `projects/testouts/t1-find-the-bug/`, `t2-supplier-swap/`, and `t3-scheduler-conflict/` assemble green and test red, alongside the process docs, verification tooling, four CI workflows, and the Pages pipeline already in this repository. `projects/m6-testing/` and `docs/programming/UNTESTED-TARGETS.md` are also built — M6 is deliberately out of numeric order (it is the M7 gate), and the Kickoff Gate block below is still M0–M3, not M0–M3+M6. **The Kickoff Gate block (M0–M3) is complete.** M4, M5, M7, and M8 skeleton projects are not yet built; they arrive in later plans |
| `CommandTemplate` rewrite | **Out of scope** — see spec §8. Still the highest-leverage single fix, and it is still on WPILib 2025.2.1 |
| Repo-level READMEs | **Not written** — M8 creates the habit per subsystem |
| Code references | **All 2026.** `Phoenix-2026` is the reading-scale repo, `Robot-2026` the full-complexity one. No milestone cites pre-2026 Ignite code |

**Re-verify [`resources.md`](resources.md) at the start of each season** — both halves of it.

1. **External links.** WPILib restructures its documentation between seasons; building this curriculum found two already-stale URLs inside research written five months earlier.
2. **Ignite code references.** Every repo path a student is sent to read must be on the current WPILib and vendordep generation. A reference repo that no longer builds with the toolchain M0 installs is worse than no reference at all — this is how the curriculum first drifted, and §8 of the spec records the fix.
