# T3 — Scheduler Conflict

**Time: 60 minutes.** Tracks C and D.

This is a working robot. Three subsystems — a drivetrain, an elevator, and a
gripper — all wired up, all bound to buttons, all running in the simulator
today:

```bash
./gradlew simulateJava
```

There is exactly one thing missing.

---

## Part 1 — a command that needs two subsystems

Write a command that requires **the elevator and the gripper**, and return a new
instance of it from `RobotContainer.combinedCommand()`.

Either idiom is fine — a factory-style composition or an explicit `Command`
subclass. Name it. Do not make it require the drivetrain.

```bash
./gradlew test
```

Five tests, all green.

**Do not edit anything under `src/test/`.** Those tests are part of the fixture
and CI checks that they have not changed. If you think one is wrong, say so —
you might be right, and a mentor will look.

---

## Part 2 — bind it

Bind your command to a controller button in `configureBindings()`. No test
covers this. Run the simulator and press the button.

---

## Part 3 — the one that actually decides this

While your command is running, press the button bound to the elevator. Then
answer, out loud, to the mentor:

> What happens to your command, and when?

Be specific about three things: which command is still running afterwards, what
the scheduler did to the other one, and which of your command's four methods ran
as a result.

That is the whole test-out. Parts 1 and 2 check you can write and wire a
command. Part 3 checks you have the scheduler's model in your head, and the
difference between passing and failing is entirely in this answer.

"They both run" is a failing answer. So is "it queues".

---

## How you pass

1. Part 1's tests are green.
2. Your command is bound and you can demonstrate it.
3. Your part-3 explanation is correct.

If 60 minutes runs out, that is not a failure of anything. It means M3 is where
you start, which is exactly what this fixture exists to find out.
