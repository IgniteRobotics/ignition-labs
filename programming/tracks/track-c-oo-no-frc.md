# Track C — Object-Oriented Experience, No FRC

**This track is for you if:** you are comfortable in Java, C++, or C#. You have written classes, used interfaces, and probably shipped something real. You have never touched WPILib, a roboRIO, or a CAN bus.

You are not learning to program. You are learning a domain: a framework with unusual constraints, hardware that lies to you, and control theory.

---

## Your path

Test out fast, land in M3, and go.

| Milestone | Your move | Sessions |
|---|---|---|
| **[M0 — Tooling, Git & the Build](../milestones/m0-tooling-and-git.md)** | **Attempt the test-out.** 15 minutes if your environment works | 0–2 |
| **[M1 — Java Fundamentals](../milestones/m1-java-fundamentals.md)** | **Test out.** 30 minutes | 0 |
| **[M2 — OOP, Interfaces & Lambdas](../milestones/m2-oop-interfaces-lambdas.md)** | **Test out.** 45 minutes | 0 |
| **[M3 — WPILib Command-Based](../milestones/m3-wpilib-command-based.md)** | **Work it. Your real starting point** | 5–7 |
| — | **Kickoff readiness minimum reached** | — |
| **[M4 — Sensors, Actuators & Control](../milestones/m4-sensors-actuators-control.md)** | Work it. Probably the hardest thing here for you | 5–7 |
| **[M5](../milestones/m5-autonomous-odometry-vision.md)** through **[M8](../milestones/m8-in-season-deliverables.md)** | Same as everyone | See each |

**Expected total to kickoff readiness:** 5 to 9 sessions.

---

## Do not skip M0

It is genuinely tempting and it is the most common way Track C stalls.

The FRC toolchain is not a normal Java toolchain. WPILib ships its own VS Code build with a specific JDK and extension. Projects are Gradle, but with a vendordep system and a deploy target that is a roboRIO over a network. Version mismatches between seasons produce build errors that look exactly like code errors.

**If you have used git and Gradle professionally**, take the test-out: show a merged PR that passed CI, and run `./gradlew build` on an FRC project on your machine. Fifteen minutes and you are through.

**If `./gradlew build` does not work on your machine yet**, that is the entire point of M0. Do it now, calmly, rather than at kickoff.

---

## Take the M1 and M2 test-outs, but read two sections anyway

You will pass both. Before you move on, skim these:

**From M1 — nothing.** Genuinely. Skip it.

**From M2 — two sections:**

**The singleton section.** Not because it is hard, but because Ignite uses singletons for state machines (`LaunchState.getInstance()`) and dependency injection for subsystems, and you will hit both in the first hour of reading real code. Know that the split is intentional.

**The lambdas and functional interfaces section.** You know what a lambda is. What is worth two minutes is *how completely* WPILib is built on `DoubleSupplier` and `BooleanSupplier`. Nearly every API takes a supplier rather than a value, because everything is re-evaluated fifty times a second. Once you have that, `RobotContainer` reads normally instead of looking over-abstracted.

---

## What will actually be new

**M3 — the framework.** Command-based is a scheduler with a requirement system that prevents two commands from driving one mechanism. You will recognize it as a resource-locking cooperative scheduler, which makes it easy — but read the "why it exists" section anyway, because the constraint driving it is the 20ms loop, and that constraint is what makes robot code different from anything you have written.

**The thing that catches experienced developers:** you cannot block. No `sleep`, no waiting loops, no long synchronous work. Everything is "check, and return." Straightforward once you internalize it, and easy to violate by habit.

**M4 — control theory. This is your real gap.**

If you are a strong software developer with no controls background, M4 will be the hardest milestone in this curriculum for you, and it will be hard in an unfamiliar way. It is not complexity — it is that the answers come from physics and measurement rather than from reasoning about code. You cannot think your way to a kV. You have to characterize the mechanism.

Budget real time. Do not skim it because the code looks simple. The code *is* simple; the engineering is not.

**M5 — sensor fusion.** Odometry drifts, vision is noisy and late, and a Kalman filter reconciles them. The concepts will be familiar if you have done anything with state estimation. The FRC-specific trap is timestamps: a vision measurement describes where the robot *was* at capture, and submitting it without that timestamp actively makes your estimate worse.

**M6 — testing, with real constraints.** You know how to test. What is new: `TimedRobot` is not designed to ever exit, so instantiating the robot in a test hangs CI forever. And CTRE's simulation has documented timing requirements — specific delays after construction and after sending a control request — that will waste an afternoon if you do not read the guide first.

---

## The honest framing

Your instinct will be that this is straightforward, because the code is not complicated. Compared to production software, an FRC codebase is small and simple.

What makes it hard is different: the hardware lies, the constraints are physical, and you get exactly one chance in a match with no ability to reproduce the failure afterward except from a log. The skills that transfer are your instincts about structure and testing — and those are genuinely valuable here, because they are the thing this team is thinnest on.

The skills that do not transfer are anything that assumes you can reproduce a bug on demand.

---

## AI posture

You start at 🟡 Yellow, because your tier is set by milestone completion and you will have cleared M2 immediately.

Yellow means: boilerplate, Javadoc, stack-trace triage, and API explanation are fine. Control logic, subsystem decomposition, and gain values are not. See `../ai-use-standard.md` §4.

**One specific warning for you.** These tools are stale on WPILib — the API changes every season, and they will confidently suggest patterns from 2022. You have enough Java fluency for wrong-but-plausible WPILib code to look completely fine. Check against current docs, not against whether it reads well.

Green opens after M6, same as everyone.

---

## Start here

**[M0 test-out](../milestones/m0-tooling-and-git.md)** — fifteen minutes, then M1 and M2 test-outs back to back, then **[M3](../milestones/m3-wpilib-command-based.md)** for real.

Before M3, open `Robot-2026/src/main/java/frc/robot/RobotContainer.java`. You will follow the structure and not the vocabulary. That gap is exactly what M3 closes.
