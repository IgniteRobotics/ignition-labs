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
