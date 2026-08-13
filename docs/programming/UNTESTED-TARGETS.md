# Untested targets

Classes in `Robot-2026` with real logic and no test coverage. **This is where you pick your
M6 pull-request target from.**

**Owner:** the programming mentor who owns `FEATURE-BACKLOG.md`. Both lists have the same
failure mode — nobody's job, so nobody updates them, so the milestone quietly loses its
targets. One person owns both. If the name in this line is out of date, that is the bug.

**Last verified:** 2026-08-11, against
[`Robot-2026` @ `28ce539`](https://github.com/IgniteRobotics/Robot-2026/tree/28ce5397d88275001d656dbb717d22cf9ac0150d).

---

## The hard rule, before you pick

Every test you add to `Robot-2026` must be **pure logic**. No timing, no simulation, no
hardware.

This is not style advice. A flaky test in the season repository blocks the whole team's
pull requests, and it will do it during competition week, at 11pm, when the person who
wrote it is asleep. A flaky test in `projects/m6-testing/` costs one student an afternoon.
That is the entire difference, and it is why `PhoenixTestSupport` stays in the sandbox and
never comes here.

If the class you want to test cannot be tested without constructing a device, waiting on a
signal, or stepping a simulation — **pick a different class.** That is not a failure of your
skills. It is a real property of the code, and noticing it is part of the milestone.

---

## Targets

| Class | The seam worth testing | Claimed by |
|---|---|---|
| [`subsystems/shooter/MappedLaunchRequestBuilder.java`](https://github.com/IgniteRobotics/Robot-2026/blob/28ce5397d88275001d656dbb717d22cf9ac0150d/src/main/java/frc/robot/subsystems/shooter/MappedLaunchRequestBuilder.java) | Interpolated lookup by distance. Test what it returns between measured points, and what it does outside the measured range | |
| [`subsystems/shooter/ParabolicLaunchRequestBuilder.java`](https://github.com/IgniteRobotics/Robot-2026/blob/28ce5397d88275001d656dbb717d22cf9ac0150d/src/main/java/frc/robot/subsystems/shooter/ParabolicLaunchRequestBuilder.java) | The same interface, different math. Two implementations of one contract — the M2 lesson, in production | |
| [`statemachines/LaunchCalculator.java`](https://github.com/IgniteRobotics/Robot-2026/blob/28ce5397d88275001d656dbb717d22cf9ac0150d/src/main/java/frc/robot/statemachines/LaunchCalculator.java) | The time-of-flight maps and the phase-delay prediction. **Harder than it looks** — see below | |

**Put your GitHub handle in the claimed-by column in the same pull request that adds your
tests.** Two students testing the same class is wasted effort, and the column is how you
find out before you start rather than in review.

## A note on `LaunchCalculator`

It is the best-looking target and the most awkward one. The math is genuinely worth testing.
The way you reach it is not: it is a singleton with a private constructor, and it pulls the
robot's pose and speeds from `DriveState.getInstance()` in the middle of the calculation.

You cannot test that without either a robot or a substantial refactor, and **a refactor of a
competition-season state machine is not an M6-sized task.** If you take this one, expect to
spend your session arguing with a mentor about where the seam should go — which is a real
and useful argument, and a different one from the milestone's.

The two builders are the better first pull request. Take `LaunchCalculator` second, if at
all.

## Adding a target

Anyone can. A class belongs here if it has logic worth being wrong about and no test
covering it. Add the row, name the seam in one sentence, and leave claimed-by empty.
