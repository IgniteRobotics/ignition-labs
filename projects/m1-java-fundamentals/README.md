# M1 — Java Fundamentals

Six small exercises. Each one is a calculation that appears somewhere in real
FRC code, stripped of the robot around it.

Read [the milestone](../../docs/programming/milestones/m1-java-fundamentals.md)
first. The git commands — fork, branch, commit, push, pull request — are in
[the git workflow](../../docs/programming/git-workflow.md), and this milestone
does not repeat them.

---

## Before you start

**This project ships broken, on purpose.** Every exercise is an empty method
that throws `UnsupportedOperationException`. Your job is to fill them in.

Prove that to yourself before you write anything:

```bash
./gradlew assemble
```

Green. The project compiles.

```bash
./gradlew test
```

Red, with every test failing on `UnsupportedOperationException`. That is not a
broken laptop and it is not a broken clone. That is the starting line.

When all six exercises are done, the second command turns green too. That is
the whole milestone.

---

## The exercises

Each lives in `src/main/java/frc/training/m1/`, and each has a Javadoc comment
above it describing exactly what it must do — what comes in, what goes out, and
what the awkward cases are. Read it before you write. It is the specification,
and the tests enforce it literally.

| # | File | What it does |
|---|---|---|
| 1 | `Deadband.java` | Ignores joystick noise near zero |
| 2 | `TickConversion.java` | Encoder ticks to rotations, and to RPM |
| 3 | `GearRatio.java` | Motor rotations to mechanism rotations |
| 4 | `AtSetpoint.java` | Is the mechanism close enough yet? |
| 5 | `Clamp.java` | Keep a value inside a safe range |
| 6 | `Interpolate.java` | Estimate between two measurements |

Work one at a time. Run just the one you are on:

```bash
./gradlew test --tests '*DeadbandTest'
```

**Do not edit anything under `src/test/`.** Those tests are part of the
milestone, and CI checks that they have not changed. If you think one of them
is wrong, say so in your pull request — that happens, and you might be right.

---

## Two other things in here

**`CompilerErrorGarden.java`** holds three broken snippets, commented out.
Uncomment one, run `./gradlew compileJava`, and read the error. Read only the
first one — errors cascade, and the rest are usually its children. Fix it,
recompile, comment it back out, move to the next.

**`BlockingWaitDemo.java`** shows what happens when robot code waits instead of
checking:

```bash
./gradlew blockingWaitDemo
```

Watch the timestamps. One loop takes two seconds. On a real robot, those two
seconds are two seconds with no driver control and no way to stop it.

---

## There is no simulator here

M1 has no robot in it — these are plain Java exercises, and the package is
`frc.training.m1`, not `frc.robot`, for that reason. **WPILib: Simulate Robot
Code** has nothing to run and will not work. Robot code starts at M3.

---

## Done when

- `./gradlew test` is green
- You can read a compiler error and find the line it means, without help
- You can explain why `1/2` is `0` in Java, and how to fix it
- You can explain why `==` is the wrong way to compare two `double` values
- You can explain why a `while` loop that waits is dangerous in robot code
