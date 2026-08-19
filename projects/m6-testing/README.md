# M6 — Simulation, Telemetry & Testing

How you find out your code is wrong before the robot tells you.

This project is the **warm-up**. The deliverable for this milestone is a pull request
against `Robot-2026` that adds tests to a class from
[`UNTESTED-TARGETS.md`](../../docs/programming/UNTESTED-TARGETS.md). This sandbox is where
you learn the moves first, on code where a mistake costs nothing.

---

## This one is different: green is the starting line

Every project before this one arrived red, and your job was to make it pass.

**This one arrives green.** `LaunchProfile` and `ShooterFlywheel` are written, correct, and
not yours to change. There is exactly one test in `src/test/`, and it already passes.

Your job is the tests. That is the entire milestone, and it is why the bar is set where it
is: **you have written a test that caught a real regression.** That is it. Three good tests
beat an abandoned attempt at full coverage.

---

## What you are given

| Given | Why |
|---|---|
| `plant/LaunchProfile.java` | The practice target. Real launch math, no hardware. Read it — you cannot test a contract you have not read |
| `plant/ShooterFlywheel.java` | One motor, and the reason `AutoCloseable` matters |
| `Robot.java`, `Main.java` | Runs `ShooterFlywheel` under simulation so you have something to log. Given, complete — the one exception is described in `Robot.java`'s Javadoc and in Part 4 below |
| `src/test/.../WorkedExampleTest.java` | The one worked example. Read it, then write your own. **Do not edit it** |
| `src/test/.../support/PhoenixTestSupport.java` | The CTRE simulation timing setup. It stays here — see below |
| `regressions/` | Deliberate one-line breaks. [`regressions/README.md`](regressions/README.md) |
| `logs/` | Two real diagnostic logs and two questions — see below |

Yours: **every other test in `src/test/`, and every test in your `Robot-2026` pull
request.**

---

## The three parts

**Part 1 — test the logic.** Write at least three tests against `LaunchProfile`. Then apply
a regression patch, watch your test fail, revert it, watch it pass. Step 4 there is the
point of the whole exercise: a test you have never seen fail is not yet known to work.

**Part 2 — test against simulated hardware.** One test against `ShooterFlywheel` using
`PhoenixTestSupport`. Then write a second test in the same file and confirm both pass —
that is what `AutoCloseable` buys you.

**Part 3 — diagnose from a log.** [`logs/QUESTIONS.md`](logs/QUESTIONS.md).

`logs/` ships two files, deliberately different in kind:

- **`FRC_20260305_233032.wpilog`** is an ordinary NetworkTables log from a practice
  session, opened in AdvantageScope the way you'd open any match log.
- **`PHX_20260304_085129_flywheel-signals.wpilog`** is **not** a match log. It started as a
  Phoenix signal log — CTRE calls the raw format a "hoot" — captured directly off the CAN
  bus by `SignalLogger`, which is a different recorder than the one that writes the
  NetworkTables log above. It was converted to `.wpilog` with CTRE's `owlet` tool (keeping
  only the signals the question needs, not the whole capture) so it opens in AdvantageScope
  with nothing extra to install. Source capture:
  `Robot-2026/logs/2026-03-04_08-51-29/sim_2026-03-04_08-51-29.hoot`. The entries live
  under `Phoenix6/`, not `NT:/`, because they never passed through NetworkTables — they are
  the motor controllers' own CAN telemetry.

Two different recorders, two different failure pictures. Knowing which one you're looking
at is part of the exercise.

**Part 4 — produce a log yourself.** `Robot.java` is given: it runs `ShooterFlywheel` under
simulation, logging nothing yet — its class Javadoc says exactly which field to change.

```bash
./gradlew simulateJava     # enable the robot from the Sim GUI, let it run a few seconds
```

Open the log it just wrote in AdvantageScope — nothing to find yet. Swap the field's
`@NotLogged` for `@Logged` in `Robot.java`, run it again, and find the signal this time. Put
the annotation back afterward; nothing else in the file changes.

---

## Running it

```bash
./gradlew test                              # your tests
git apply regressions/flip-sign.patch       # break it
./gradlew test                              # your tests should now fail
git apply -R regressions/flip-sign.patch    # put it back
```

If your tests stay green with a patch applied, they are not testing the thing that broke.
That is a finding, not a failure.

---

## `PhoenixTestSupport` stays here

It never goes into `Robot-2026`. Tests you add to the season repository are subject to the
pure-logic hard rule: nothing that depends on timing, simulation, or hardware. A flaky test
here costs you an afternoon. A flaky test there blocks the team's pull requests during
competition season.

---

## Done when

Mechanically checked:

- [ ] At least three of your own passing tests against `LaunchProfile`
- [ ] `./gradlew test` green
- [ ] Your tests fail under a regression patch and pass again when it is reverted
- [ ] One test against `ShooterFlywheel` with correct Phoenix timing
- [ ] Two tests in one file both pass
- [ ] A merged pull request on `Robot-2026`, tests pure-logic, target from `UNTESTED-TARGETS.md`

Mentor verifies:

- [ ] Both log questions answered **with evidence**, and the alternative ruled out
- [ ] You added `@Logged` to a field in `Robot.java`, ran `./gradlew simulateJava`, and saw the signal appear in the log
- [ ] You can explain why not to instantiate `TimedRobot` in a test
- [ ] You can explain the difference between what simulation proves and what it does not

Completing this unlocks 🟢 Green and M7.
