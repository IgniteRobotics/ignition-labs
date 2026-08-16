# Task 5: `README.md` and `READING.md`

The last milestone-lane task. Everything the student needs to hear in prose rather than
in a stub's Javadoc: the order to work in, why the control loop is written in Java instead
of on the `TalonFX` (the thing a student who has seen `Robot-2026` will otherwise ask
about immediately), how to run SysId and get a plot, and the pointer into the two real
files this milestone was designed against.

**No formula and no gain value goes in either file** — same rule as every stub's Javadoc.
Step 4 greps for it.

**Files:**
- Create: `projects/m4-control/README.md`
- Create: `projects/m4-control/READING.md`

**Consumes:** Task 4's given tests (README references what they check), Task 2's
`ArmPlant`/`FlywheelPlant` Javadoc (README repeats the modelling-limitation language where
a student will actually hit it, per design record §5).
**Produces:** the two files a student opens first.

**Lane:** `milestone`. Worktree `.worktrees/m4`.

---

- [ ] **Step 1: Write `README.md`**

Structure follows M3's `README.md`: what's given vs. theirs, the order, then "done when."
Folds in design record §5's docs-alignment paragraph — stating plainly that this project's
loop runs in Java while `ShooterSubsystem` closes the same kind of loop on the device, and
why — because a student who has skimmed `READING.md` before writing code will otherwise
open `RobotContainer.java` expecting `VelocityVoltage` and `Slot0`, and it isn't there.

Create `projects/m4-control/README.md`:

```markdown
# M4 — Sensors, Actuators & Control

Two simulated mechanisms, characterized and controlled in closed loop: a flywheel
(velocity) and an arm (position, against gravity, homed to a limit switch). This is
the milestone where robot programming stops being software and starts being engineering.

Five to seven sessions is normal. Nothing here is a race.

---

## What you are given, and what is yours

| Given | Why |
|---|---|
| `Main.java` | Never edit it. Every WPILib project has one and they are all identical |
| `Robot.java` | Runs your two control loops every 20ms, and steps both plants' physics while simulating. Read it before you start |
| `Constants.java` | CAN IDs and the two setpoints the given tests use. No gain lives here — that is yours to add, and it is the milestone |
| `plant/FlywheelPlant.java`, `plant/ArmPlant.java` | The simulated mechanisms. Read their Javadoc — it states what each method measures and what the simulation does and does not model. It does not state a gain |
| `harness/M4Deliverables.java` | How the tests, and `Robot`, reach your work. Read it before you start |
| `harness/PlotHarness.java` | Records a setpoint-vs-measurement history and writes it to `build/plots/<name>.csv`. Use it yourself — see Part 1 step 4 |
| `harness/SysIdWiring.java` | Wires a `SysIdRoutine` to a `TalonFX`, logging through CTRE's `SignalLogger`. Bind it to buttons in your own `RobotContainer` |
| `src/test/` | The given tests. Do not edit them |

Yours: **two plants constructed and wired, a feedforward and a PID loop for each
mechanism, a motion profile for the arm, and a homing sequence.** All of it lives in
`RobotContainer.java`.

---

## Why the control loop is written in Java, not on the `TalonFX`

If you have looked at `Robot-2026`'s `ShooterSubsystem`, you have seen a CTRE `Slot0` +
`VelocityVoltage` closed loop — gains configured once, commanded with a target, and the
`TalonFX` itself does the rest. This project does not do that. Your feedforward and PID
math run in `updateFlywheel()`/`updateArm()`, computing a plain voltage that gets sent
with `plant.setVoltage(...)`.

Both are real, both ship on real robots, and `READING.md` asks you to read the onboard
version. This project uses the Java version for a concrete reason: CTRE's simulated
onboard control loop advances on wall-clock time, not on a test's own stepped loop, so a
JUnit test cannot reliably drive it and assert on where it converged. Writing the loop
yourself means the exact code that runs in the simulator is the exact code a given test
exercises — no difference between what you watched work and what got graded.

---

## Read first, build second

Do [`READING.md`](READING.md) before you write anything. Twenty minutes with two real
files from `Robot-2026`, at the exact commit this milestone was written against.

---

## Part 1 — velocity control (flywheel)

1. Construct a `FlywheelPlant` in your constructor, using `Constants.FlywheelConstants.CAN_ID`.
   Return it from `flywheelPlant()`.
2. Characterize it. Bind `SysIdWiring.forMotor(...)`'s routines to controller buttons,
   run `./gradlew simulateJava`, and run each of quasistatic-forward, quasistatic-reverse,
   dynamic-forward, dynamic-reverse. WPILib's SysId analyzer tool turns the log into kS,
   kV, and kA.
3. Write `setFlywheelTarget`/`updateFlywheel`. Apply feedforward alone first — with your
   PID's P at zero, confirm the flywheel roughly holds a commanded speed with `plant.setVoltage`
   fed only by the feedforward term.
4. Add feedback until it tracks crisply. Use `PlotHarness` to record a step response —
   command a target, run the loop, record `(time, target, plant.velocityRotationsPerSecond())`
   each iteration, call `.write()`. Open the CSV and read it: does it reach the target,
   overshoot, oscillate?
5. Confirm `./gradlew test --tests FlywheelVelocityTest` passes.

## Part 2 — position control with gravity (arm)

1. Construct an `ArmPlant` in your constructor, using `Constants.ArmConstants.CAN_ID`.
   Return it from `armPlant()`.
2. Characterize it the same way. `ArmPlant`'s gravity term means the SysId run matters
   more here — a flywheel has no preferred direction, an arm does.
3. Write `setArmTarget`/`updateArm`. Confirm feedforward alone — gravity compensation
   included — holds the arm roughly level with P at zero. Add kP, then kD only if it
   overshoots.
4. Add a motion profile (`TrapezoidProfile` or `ProfiledPIDController`) so the arm
   accelerates, cruises, and decelerates into a large setpoint change instead of chasing a
   step. Plot before and after with `PlotHarness` and compare.
5. Write `homingCommand()`: drive slowly toward the reverse limit — read
   `armPlant().atHomeLimit()` every iteration — and once it is true, stop and call
   `armPlant().zeroHere()`. Return a **new** command instance each call; the interruption
   lesson from M3 still applies.
6. Confirm `./gradlew test --tests ArmGravityTest` and
   `./gradlew test --tests ArmHomingTest` both pass.

## Stretch — real code

Refactor `Robot-2026`'s `DriveMotorConfigs.java` to use CTRE's `.clone()` pattern instead
of four near-identical `create*Slot0Configs()` methods. Small, safe, real, and it makes
the file substantially shorter. **Open a pull request** — spec §6.7 asks for one real PR
before you finish M4, and this is it.

## Optional hardware variant

Same exercises on last season's robot or a benchtop mechanism. Simulation first, always —
you cannot break a simulated gearbox.

---

## What the given tests check, and what they do not

`FlywheelVelocityTest` and `ArmGravityTest` are a **behavioral gate, not a tuning gate**:
they check that you reach and hold a target within a loose tolerance, and that the
flywheel does not run away. They do not check overshoot or settling time — a bound tight
enough to catch a P-only hack also rejects legitimately good gains, and you cannot always
tell the two apart from a passing or failing test alone. Reading your own plot is how you
actually judge tuning quality, and it is a mentor-verified item below, not a test.

`ArmHomingTest` checks two things, not one: that the arm's reverse limit switch actually
tripped, and that your encoder reads the home angle afterward. Zeroing rebases whatever
position you happen to be at, so a routine that calls `zeroHere()` without ever confirming
`atHomeLimit()` first can still end up with an encoder that reads correctly by coincidence,
or one that doesn't — either check alone can be fooled depending on how and when it's
wrong. Passing both is what actually says the arm got there and then zeroed, not just one
or the other.

---

## A modelling limit, on purpose

Both plants integrate the voltage you command them with directly — they do not read it
back through the `TalonFX`'s own simulation. One consequence: supply-current limits,
voltage compensation, and ramp rates you configure through `motor()` are inert in this
simulation. If you configure a current limit and see no change in your plot, you have not
found a bug — you have found the edge of what this model represents. `FlywheelPlant`'s and
`ArmPlant`'s Javadoc say the same thing, in the file you'll actually be reading.

---

## Definition of done

- [ ] `./gradlew test` is green
- [ ] Flywheel reaches and holds commanded velocity in simulation
- [ ] Arm holds position against simulated gravity
- [ ] Arm homes correctly at startup
- [ ] You can explain what each of kS, kV, kA, kG does
- [ ] You can explain why feedforward is tuned before kP
- [ ] You can explain why kI stays at zero
- [ ] You produced plots for both mechanisms with `PlotHarness` and can read them
- [ ] You ran SysId yourself and used the numbers it produced
- [ ] Motion profile demonstrated, with a before/after plot
- [ ] You can explain what happens when a CAN device stops hearing the roboRIO
- [ ] You opened the stretch pull request against `Robot-2026`

The kS/kV/kA/kG explanations, the feedforward-before-kP and kI-stays-at-zero
explanations, and reading your own plots are mentor-verified. No test can check them, and
they are half the milestone.
```

- [ ] **Step 2: Write `READING.md`**

M3's `READING.md` sent students to clone `Phoenix-2026` whole. M4 is narrower — two files,
the ones the design record and the milestone document both name, at the pinned commit this
plan already cites everywhere else: `28ce5397d88275001d656dbb717d22cf9ac0150d`. Confirmed
against a local checkout of `Robot-2026` at that exact SHA before writing this task:
`ShooterSubsystem.java` closes its flywheel loop with `VelocityVoltage` and a `Slot0`
config, and wires a real `SysIdRoutine` logging through `SignalLogger`; `DriveMotorConfigs.java`
has exactly the four near-identical `create*Slot0Configs()` methods the stretch project
asks you to collapse.

Create `projects/m4-control/READING.md`:

```markdown
# Reading — two files from `Robot-2026`

Not a whole-repository read this time — two files, the ones this milestone was designed
against, at the exact commit this project cites everywhere else.

---

## Get it

```bash
cd ~/workspace/ignite            # or wherever you keep repositories
git clone https://github.com/IgniteRobotics/Robot-2026.git
cd Robot-2026
git checkout 28ce5397d88275001d656dbb717d22cf9ac0150d
```

Detached HEAD is expected — you are looking at a photograph of the repository, not
standing on a branch.

In a browser instead:
<https://github.com/IgniteRobotics/Robot-2026/tree/28ce5397d88275001d656dbb717d22cf9ac0150d>

---

## `src/main/java/frc/robot/subsystems/shooter/ShooterSubsystem.java`

Read this first. It is the file `README.md`'s "why the control loop is written in Java,
not on the `TalonFX`" section is contrasting your project against.

Find:

1. The `VelocityVoltage` field and the `Slot0Configs` applied in the constructor. This is
   the onboard closed loop — gains configured once, a target commanded, the `TalonFX`
   itself closes the loop.
2. `m_sysIdRoutineFlywheel` — a real `SysIdRoutine`, wired the same shape as this
   project's `harness/SysIdWiring.java`, with a real ramp rate and a reduced step voltage.
   Say, in one sentence, why the step voltage was reduced from the default.
3. `periodic()` — where the velocity target actually gets sent to the motor. This runs
   every 20ms, the same cadence your own `updateFlywheel()` will run at, driven by a
   different mechanism (`CommandScheduler`'s subsystem periodic, versus `Robot.robotPeriodic()`
   calling your method directly).

## `src/main/java/frc/robot/subsystems/drive/DriveMotorConfigs.java`

Your stretch project, if you take it. Four methods — one per swerve corner — each
building a `Slot0Configs` by assigning the same five fields from four different sets of
named constants.

Find the exact lines that repeat across all four methods, and work out what a `.clone()`
of one base config, then per-corner field overrides, would look like. You do not have to
write it here — just be able to describe the refactor before you open `RobotContainer.java`
and start your own project.

---

## Questions to answer

A mentor will ask. "I read it" is not an answer to any of them.

1. `ShooterSubsystem.periodic()` commands `velocityControl.withVelocity(...)` every cycle,
   not just when the target changes. Why is that the right choice for a closed loop, and
   what would go wrong if it only ran once per `set` call?
2. The `SysIdRoutine.Config` reduces dynamic step voltage from its default. Say what a
   dynamic SysId test actually does to a mechanism, and why a full step voltage risked a
   brownout on this one.
3. Count the fields `DriveMotorConfigs` assigns per corner. Are any of them a term this
   milestone taught you the name of? Naming one is not answering the stretch project's
   question of *how* to collapse the duplication, but it tells you what you are looking at.

---

## Then, and only then

Start `RobotContainer.java`. `README.md` has the order.
```

- [ ] **Step 3: Confirm the pinned SHA and both paths, once more, before shipping**

```bash
cd /Users/y0shi/workspace/ignite/Robot-2026
git rev-parse HEAD   # expect 28ce5397d88275001d656dbb717d22cf9ac0150d
test -f src/main/java/frc/robot/subsystems/shooter/ShooterSubsystem.java && echo OK
test -f src/main/java/frc/robot/subsystems/drive/DriveMotorConfigs.java && echo OK
```

Both `OK`s and the matching SHA are required before committing — a pinned reference that
does not resolve is worse than no reference at all (spec's no-pre-2026-Ignite-references
rule exists for exactly this failure mode).

- [ ] **Step 4: Confirm no gain leaked into student-facing prose**

```bash
cd projects/m4-control   # relative to the worktree root -- NOT the main repo checkout
grep -niE '\bk[sSvVaAgGpPdD]\s*=|\bk[sSvVaAgGpPdD]\b.{0,20}[0-9]' README.md READING.md
```

Expected: no output. Mentions of the letters `kS`, `kV`, `kA`, `kG`, `kP`, `kD` as terms
(as in "explain what each of kS, kV, kA, kG does") are fine and expected — the grep is
tuned to catch an assigned or nearby numeric value, not the term itself.

- [ ] **Step 5: Commit**

```bash
cd /Users/y0shi/workspace/ignite/docs
git status --short
git add projects/m4-control/README.md projects/m4-control/READING.md
git commit -m "Add M4's README and READING

READING.md points at two files from Robot-2026 at the pinned SHA instead of
a whole-repository clone -- ShooterSubsystem for the onboard closed loop
this project deliberately does not use, DriveMotorConfigs for the stretch
project. README.md states why the control loop runs in Java rather than on
the TalonFX (design record §5) where a student will actually ask the
question."
```

This is the last task in the `milestone` lane. Tasks 6-7 (`testout` lane, a different
worktree) and tasks 8-9 (the serialized tail) are not yours — stop here and report back.
