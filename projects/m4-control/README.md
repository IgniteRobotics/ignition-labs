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
| `Robot.java` | Runs the command scheduler, then your two control loops, every 20ms, and steps both plants' physics while simulating. Read it before you start |
| `Constants.java` | CAN IDs, the two setpoints the given tests use, and the driver controller's USB port. No gain lives here — that is yours to add, and it is the milestone |
| `plant/FlywheelPlant.java`, `plant/ArmPlant.java` | The simulated mechanisms. Read their Javadoc — it states what each method measures and what the simulation does and does not model. It does not state a gain |
| `harness/M4Deliverables.java` | How the tests, and `Robot`, reach your work. Read it before you start |
| `harness/MechanismSubsystem.java` | One given instance per mechanism, already constructed in `RobotContainer`. Pass it as the `requirement` when you build `SysIdWiring.forMotor(...)` — that's what keeps a sweep and your own control loop from driving the same motor at once |
| `harness/PlotHarness.java` | Records a setpoint-vs-measurement history and writes it to `build/plots/<name>.csv`. Use it yourself — see Part 1 step 4 |
| `harness/SysIdWiring.java` | Wires a `SysIdRoutine` to a `TalonFX`, logging through CTRE's `SignalLogger`. Bind its four commands to buttons on the given `driver` controller in your own `RobotContainer` |
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
2. Characterize it. In your constructor, build `SysIdWiring.forMotor(<plant>.motor(), "<name>",
   flywheelMechanism)` — the given `flywheelMechanism` is the requirement, and passing it is
   what keeps a sweep and your own control loop from driving the motor at once — then bind all
   four of its commands to buttons on the given `driver` controller. Run `./gradlew
   simulateJava`, and run each of quasistatic-forward, quasistatic-reverse, dynamic-forward,
   dynamic-reverse in turn. Every one of those requests reaches the plant exactly the way your
   own `setVoltage` calls do, so the sweep really moves the flywheel. WPILib's SysId analyzer
   tool turns the log into kS, kV, and kA.
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
2. Characterize it the same way, using the given `armMechanism` as the requirement.
   `ArmPlant`'s gravity term means the SysId run matters more here — a flywheel has no
   preferred direction, an arm does.
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

Both plants feed the physics from the same request your control loop sends —
`motor.getAppliedControl()`, the voltage the `TalonFX` is currently holding. There is
exactly one path in, and it's why the SysId workflow above actually moves the mechanism:
a sweep reaches the plant the same way your own `plant.setVoltage(...)` calls do.

What that path does not simulate is the *device's response* to the request. Supply-current
limits, voltage compensation, and ramp rates you configure through `motor()`, along with any
inversion or neutral-mode setting, would change how a real `TalonFX` answers a voltage
request — none of that changes what the plant simulates here, because the physics reads the
request itself, not the device's reaction to it. If you configure a current limit and see no
change in your plot, you have not found a bug — you have found the edge of what this model
represents. `FlywheelPlant`'s and `ArmPlant`'s Javadoc say the same thing, in the file you'll
actually be reading.

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
- [ ] You opened at least one pull request against a real robot repository — the refactor
      above is the recommended vehicle, not a requirement; see the milestone page for what
      counts

The kS/kV/kA/kG explanations, the feedforward-before-kP and kI-stays-at-zero
explanations, and reading your own plots are mentor-verified. No test can check them, and
they are half the milestone.
