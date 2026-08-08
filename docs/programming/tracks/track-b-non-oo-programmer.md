# Track B — Programmed Before, But Not Object-Oriented

**This track is for you if:** you have written real code, but not in Java, C++, or C#. Python scripts, Arduino sketches, MATLAB, JavaScript you picked up for a web project, a lot of Scratch — anything where you got things working but never wrote a class.

You already have the hard part: you can think in code. Loops, conditionals, and variables are not new. What is new is the object-oriented model everything in WPILib is built on, and Java's insistence that you declare what everything is.

---

## Your path

You start at M0, attempt one test-out, and go from there.

| Milestone | Your move | Sessions |
|---|---|---|
| **[M0 — Tooling, Git & the Build](../milestones/m0-tooling-and-git.md)** | **Work it.** Try the test-out if you already use git | 1–3 |
| **[M1 — Java Fundamentals](../milestones/m1-java-fundamentals.md)** | **Attempt the test-out.** Most of Track B passes | 0–3 |
| **[M2 — OOP, Interfaces & Lambdas](../milestones/m2-oop-interfaces-lambdas.md)** | **Work it.** This is your real starting point | 3–4 |
| **[M3 — WPILib Command-Based](../milestones/m3-wpilib-command-based.md)** | Work it | 5–7 |
| — | **Kickoff readiness minimum reached** | — |
| **[M4](../milestones/m4-sensors-actuators-control.md)** through **[M8](../milestones/m8-in-season-deliverables.md)** | Same as everyone | See each |

**Expected total to kickoff readiness:** 9 to 17 sessions, depending on how the M1 test-out goes.

---

## About the M1 test-out

**Take it.** You are the population it was designed for.

You are given a short Java method with a deliberate bug — an off-by-one, an inverted comparison, or an integer-division truncation — and you find it, fix it, and explain what was wrong. 30 minutes.

**If you pass:** skip to M2. Skim M1's "Types" section anyway for the integer-division trap, because that one catches people from Python especially hard.

**If you do not pass:** work M1. It is three to five sessions and it is mostly syntax you will recognize with different punctuation. No penalty, and nobody is told.

---

## What is actually different about Java

Depending on where you are coming from, these are the adjustments that take real effort.

**Static typing.** Every variable declares its type and cannot change it. `double speed = 0.75;` not `speed = 0.75`. This feels like bureaucracy for about a week and then starts catching your bugs at compile time instead of on the robot.

**Integer division truncates.** `1 / 2` is `0`, not `0.5`. Python 3 protects you from this. Java does not, and it produces silent, real robot bugs — a gear ratio that quietly becomes zero and a mechanism that does not move. **This is the single most common thing that bites Track B students.**

**Everything lives in a class.** There are no free-floating functions. This is the biggest structural adjustment coming from scripting, and M2 is where it clicks.

**No REPL.** You cannot poke at things interactively. You compile and run. The simulator partly makes up for this.

**Explicit visibility.** `private` and `public` on everything. It feels like ceremony when you are working alone and stops feeling that way the first time six people share one repository.

**If you are coming from Arduino C specifically:** you are closer than most of Track B. You already know static types and a compile step. Your gap is genuinely just objects — go straight at the M1 test-out.

---

## M2 is your real starting point

Do not rush it. Three things in it matter more than they look:

**Classes and objects.** Not just syntax — the shift from "a script that does things" to "objects that own state and behavior." Everything in WPILib is shaped this way.

**Interfaces.** Contracts that let you swap implementations. Ignite's shooter uses this for real: one interface, two ways of computing a launch, and the shooter does not care which it gets.

**Lambdas.** The highest-leverage topic in the milestone. If you have used Python's `lambda` or JavaScript arrow functions you have most of it — the new part is *why* WPILib uses them so heavily. The distinction between passing a value and passing a way-to-get-a-value is what makes `RobotContainer` make sense, and everything in M3 depends on it.

---

## One thing to unlearn

**In robot code, you never wait.**

Whatever you have written before, you have probably written `sleep()`, or a `while` loop that spins until something is true. In robot code that freezes the entire robot — driver control, safety systems, all of it — because everything runs inside a 20ms loop that must finish.

The pattern is **check, do not wait.** Ask whether the thing has happened; if not, return and ask again next cycle.

This is the single largest mental shift between scripting and robot programming, and it is why command-based programming in M3 is shaped the way it is.

---

## AI posture

You are at 🔴 Red until you finish M2 — the same as someone starting from zero, and this is worth explaining rather than just asserting.

The reason is not that you cannot code. It is that AI-generated Java looks *right* to someone with programming experience in another language, which makes it harder to catch when it is wrong. You have enough context to be fooled and not yet enough to verify.

Yellow opens at M3. See `../ai-use-standard.md` §4.

---

## Start here

**[M0 — Tooling, Git & the Build](../milestones/m0-tooling-and-git.md)** — then take the M1 test-out.

If you already use git daily, try M0's test-out too: show a merged PR that passed CI, and run `./gradlew build` on an FRC project. Fifteen minutes.
