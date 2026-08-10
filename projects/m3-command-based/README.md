# M3 — Command-Based Programming

The framework every line of Ignite's robot code is written in. By the end of this
you will have built a two-subsystem robot that runs in the simulator, and you
will be able to read `Robot-2026/RobotContainer.java` and say what it does.

This is the biggest milestone in the curriculum. Five to seven sessions is
normal. Nothing here is a race.

---

## What you are given, and what is yours

Everything that is not the lesson is already here:

| Given | Why |
|---|---|
| `Main.java` | Never edit it. Every WPILib project has one and they are all identical |
| `Robot.java` | The scheduler call and the logging setup. Read `robotPeriodic()` — you owe a mentor a walkthrough of it |
| `Constants.java` | The shape. You add a nested class per subsystem |
| `simgui.json`, `simgui-ds.json` | The simulator opens with a keyboard joystick already mapped to port 0 |
| `harness/M3Deliverables.java` | How the tests reach your work. Read it before you start |
| `src/test/` | The given tests. Do not edit them |

Yours: **the subsystems, the commands, the composition, the bindings, and the
default command.** That is the whole milestone, and it is all inside
`RobotContainer.java` and the files you create alongside it.

---

## Read first, build second

Do [`READING.md`](READING.md) before you write anything. It is `Phoenix-2026`,
end to end — 22 files, the same toolchain you are about to use, and both of the
command idioms you are about to write. Reading a small working robot before
building one is the fastest route through this milestone, and skipping it is the
slowest.

---

## The order

**1. Run the tests, before you change anything.**

```bash
./gradlew test
```

Everything fails. That is correct and expected — see "What red means here"
below.

**2. Write your two subsystems.**

A drivetrain and one other mechanism, in a `subsystems/` package. Keep them
plain: `PWMSparkMax` motors and, for the drivetrain, `DifferentialDrive`. There
is no real hardware and there does not need to be.

They go in `RobotContainer` as **private final fields** — not static, not
public, not reachable from anywhere else. Then return them from `drivetrain()`
and `mechanism()`.

**3. Write one command as an explicit `Command` subclass.**

`initialize`, `execute`, `isFinished`, `end(boolean)`. Call
`addRequirements(...)` in the constructor — if you forget, two commands will
quietly fight over the same motor, and one of the given tests exists purely to
catch that.

Give it a name with `.withName("Something Readable")` and return a **new one**
from `subclassCommand()` each time it is called.

**4. Write one command as a factory method on a subsystem.**

A method on the subsystem that returns a `Command`, built with `run(...)` or
`runOnce(...)`. Those give you the requirement for free. Name it, and return a
new one from `factoryCommand()`.

**5. Compose them, and bind all three.**

A sequence, or a parallel with a timeout. It has to end up requiring both
subsystems — which it will, if it is built out of commands that use both.
Return a new one from `composition()`.

Then bind them in `configureBindings()`: one with `onTrue`, one with
`whileTrue`, and the composition on a third button.

**6. Give the drivetrain a default command.**

Whatever runs when no button is held — on a real robot, the one reading the
driver's sticks. `drivetrain.setDefaultCommand(...)` in
`configureDefaultCommands()`.

**7. Run it.**

```bash
./gradlew simulateJava
```

The joystick is already mapped. Press your buttons and watch the command names
appear in the scheduler widget. If the names say `InstantCommand`, you skipped
`.withName()`.

**8. Demonstrate interruption.**

Bind two commands that require the *same* subsystem to two different buttons.
Press one, then press the other while the first is still running. Watch the
first one's `end(true)` fire.

Then be able to explain what the scheduler did. This is the thing M3 is
actually for, and a mentor will ask.

---

## Why there is an `M3Deliverables` interface

The given tests have to reach classes you have not written yet, and they cannot
guess what you called them. So `RobotContainer` implements a small interface
with five methods, and the tests go through those.

One rule, and it is the one that trips people:

> **Every command method returns a new instance each time it is called.**

Not a stored field. WPILib refuses to schedule a command that is part of a
composition, and your `composition()` is built out of the other two — so if you
hand back the same object every time, it cannot be scheduled on its own, and
neither the tests nor your robot will work. Build the command inside the method.

---

## What red means here

`./gradlew test` failing is the normal state of this project until you finish
it. The failures are the specification: each one names the method it wanted and
what was wrong with what it got.

What is *not* normal is a failure that mentions `UnsatisfiedLinkError`, or a
compile error before any test runs. That is a toolchain problem, not your code.
Show a mentor rather than trying to fix it.

**Do not edit anything under `src/test/` or in `harness/`.** They are part of
the milestone and CI checks that they have not changed. If you think one is
wrong, say so in your pull request — you might be right, and a mentor will look.

---

## Done when

- [ ] `./gradlew test` is green
- [ ] The robot runs in the simulator and both commands execute from button presses
- [ ] Both command names show up in the simulator's scheduler widget
- [ ] You wrote one command each way and can say why you would pick each
- [ ] Your subsystems are private fields in `RobotContainer`
- [ ] The drivetrain has a default command
- [ ] You demonstrated interruption and can explain what the scheduler did
- [ ] You can trace one full 20ms loop out loud, start to finish
- [ ] You have read `Phoenix-2026` end to end and can point at where each idiom appears
- [ ] You can open `Robot-2026/RobotContainer.java` and explain any binding a mentor points at

The last four are mentor-verified. No test can check them, and they are half the
milestone.
