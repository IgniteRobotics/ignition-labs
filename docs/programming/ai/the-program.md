# The Program at a Glance

FRC 6829 · Programming subteam

**Part one of the session.** Ten minutes on the shape of the programming curriculum: where you get on, how you move, and what each rung unlocks. Everything after this page is [part two](README.md): how we actually work with AI tools.

This is an orientation, not the curriculum itself. The curriculum lives at
**[Programming Curriculum → Overview](../README.md)**, and every milestone has its own page.
If you are looking for what to *do next*, go there. If you are looking for why the ladder is
shaped like this, you are in the right place.

---

## One ladder, four ways on

There is a single sequence of ten milestones. Your track does not change *what* you eventually
learn, only where you get on and which test-outs you attempt. Everyone converges.

| Track | If this is you | Where you really start |
|---|---|---|
| **A** | You have never really written code | M0 — and you do all ten |
| **B** | You have written code, but never a class — Python, Arduino, MATLAB, Scratch, JS | M0, then test out of M1 |
| **C** | You are comfortable in Java, C++ or C#, but have never touched WPILib | M3 |
| **D** | You have programmed for an FRC team for a season or more | M6 |

Full entry-point guides: [Track A](../tracks/track-a-new-to-programming.md) ·
[Track B](../tracks/track-b-non-oo-programmer.md) ·
[Track C](../tracks/track-c-oo-no-frc.md) ·
[Track D](../tracks/track-d-frc-veteran-ai.md)

Caught between two? Pick the earlier one. A test-out moves you up in under an hour if you
were being modest. Starting too high costs you weeks rather than an hour, and it
almost always surfaces in M3, three weeks after the decision that caused it.

---

## The ten milestones

| # | Milestone | What you can do afterward |
|---|---|---|
| **M0** | [Tooling, Git & the Build](../milestones/m0-tooling-and-git.md) | Get a pull request merged past CI |
| **M1** | [Java Fundamentals](../milestones/m1-java-fundamentals.md) | Read and write the Java robot code uses |
| **M2** | [OOP, Interfaces & Lambdas](../milestones/m2-oop-interfaces-lambdas.md) | Understand what WPILib is built on |
| **M3** | [WPILib Command-Based](../milestones/m3-wpilib-command-based.md) | Read our actual robot code |
| **M4** | [Sensors, Actuators & Control](../milestones/m4-sensors-actuators-control.md) | Make a mechanism go where you told it |
| **M5** | [Autonomous, Odometry & Vision](../milestones/m5-autonomous-odometry-vision.md) | Fifteen seconds with no driver |
| **M6** | [Simulation, Telemetry & Testing](../milestones/m6-simulation-telemetry-testing.md) | Know your code is wrong before the robot does |
| **M7** | [AI-Assisted Development](../milestones/m7-ai-assisted-development.md) | Use AI tools without shipping what you cannot explain |
| **M8** | [Spec-Anchored Development](../milestones/m8-spec-anchored-development.md) | Change a spec somebody else has to act on |
| **M9** | [In-Season Deliverables](../milestones/m9-in-season-deliverables.md) | Own a subsystem through a season |

---

## How you move

**Test-outs, not self-assessment.** Skipping a milestone means passing a 30–60 minute challenge
that a mentor verifies. Failing one is routing information, not a judgment. You work that
milestone, no penalty and no record of the attempt.

**Definitions of done are objective.** Every milestone ends in a checklist where each item is a
yes or a no, and a mentor who never taught that milestone can verify it. That property is doing
more work than it looks like it is. Nobody grades your vibe, nobody can gatekeep you on taste,
and, the part that matters for part two, nobody can talk their way past one either.

Simulation is mandatory. Hardware is optional. Every project finishes on your own laptop.
You never wait for the robot, and you cannot break anything.

**The ladder is self-guided.** Nobody schedules it for you. This session is not a milestone,
has no test-out, and gates nothing.

---

## The Kickoff Readiness Gate

The curriculum runs year-round. This is the only hard deadline in it.

| Your role at kickoff | You need |
|---|---|
| Any programming subteam member | **M0, M1, M2, M3** |
| Committing to competition robot code | **+ M4, M6** |
| Autonomous or vision owner | **+ M5** |
| Using AI tooling on competition code | **+ M7 *and* M8**, and a signed [AI Use Standard](../ai-use-standard.md) |

If you have not cleared M0–M3 by kickoff you can still participate, but you do not get commit
access to the season repository. That is published in August rather than discovered in January,
which is why it is written down at all.

The last row is the one people get wrong. Finishing M7 in December does not clear you for
agentic use on competition code. M8 has to be done too, and it is two more sessions. Plan for
both before kickoff, or plan on not using the tools in January.

M9 is not gated. It runs during the season.

---

## What the ladder unlocks

Where you are on the ladder sets your AI tier. Nobody negotiates it, and preference does not
enter into it.

| Tier | Where you are | What you may do |
|---|---|---|
| 🔴 **Red** | M0–M2 | Explanation only. No code generation — turn Copilot off |
| 🟡 **Yellow** | M3–M5 | Boilerplate, Javadoc, stack-trace triage. **Not** control logic, subsystem design, or gains. Disclosure required |
| 🟢 **Green** | After M6 | Full agentic use — gated on a spec committed first, tests passing, disclosure, and defending the diff in review. **You write that spec yourself until M8**; from M8 an agent may draft one you red-line |

The binding version of this table, and the document you sign at M7, is
[the AI Use Standard](../ai-use-standard.md). What is on this page is the shape; that page is
the commitment.

---

## Why the boundary is M6

This is the decision people argue with, so here is the reasoning rather than the assertion.

M6 is what makes Green tier possible, rather than something Green tier is handed out for. Everything that
makes agentic development safe is a verification skill: writing a test before the thing it
tests, running a mechanism in simulation, reading telemetry, telling *green* from *correct*.
Those are M6's content. A student cleared for full agentic use without them cannot do the one
thing the clearance requires, which is check the work.

That is also why Track D enters at M6, not M7. An FRC veteran arriving with three seasons of
shipped code has, in our experience, never written a test. As of 2026-08-18 our own
repositories contained zero test files across three seasons. Veterans are not asked to redo
M0–M5. They are asked to acquire the one skill the ladder exists to guarantee before the tooling
is handed over.

The same logic explains the M8 clause in the Green row. M7 teaches you to use the tools on a
spec **you** wrote. M8 teaches you to write and change a spec somebody else has to act on, which
is the skill you need before an agent is allowed to draft one for you. Reviewing a generated spec
is harder than writing one, not easier.

If you think a boundary is wrong, bring the argument. The reasoning is written down precisely so
it can be argued with; see [the curriculum spec](../curriculum-spec.md) for the full record of
what was decided and why.

---

## Where this goes next

Part one told you what you are permitted to do. Part two is method rather than permission, and it
applies from the day you are cleared for any of it.

| Next | What it is |
|---|---|
| [Working with AI — Overview](README.md) | Part two in full, and the twelve rules in short form |
| [The Rules](the-rules.md) | The twelve, each with the evidence under it, plus a copyable constitution |
| [The SDD Loop](the-loop.md) | What spec-driven development actually is, and where tests and simulation sit |
| [Session Guide](session-guide.md) | Facilitator notes for running both parts |
