# M4 — Sensors, Actuators & Control

## At a glance

Making a mechanism go where you told it and stay there. This is where robot programming stops being software and starts being engineering.

Required before you commit to competition robot code.

**Estimated time:** 5–7 sessions. Tied with M3 as the largest milestone.
**Prerequisites:** M3
**AI posture:** 🟡 Yellow

## Who enters here

**Tracks A, B, C** arrive here having worked M3.

**Track D** tests out.

## Test-out challenge

You are given a simulated flywheel velocity controller with deliberately wrong gains. It reaches its target, overshoots, oscillates — and then settles a little *short* of the setpoint rather than on it. That last part is the most useful thing in the plot.

1. Diagnose which term is wrong, from the response plot alone
2. Fix it
3. Explain why the original tuning was fragile even though it "worked"

**Pass criteria:** correct diagnosis and a real explanation of why feedback compensating for bad feedforward is brittle — it works at one setpoint and one battery voltage, and stops working at others.

**Time:** 60 minutes.

## Concepts

### Motors and the CAN bus

Ignite runs CTRE Phoenix 6 with TalonFX controllers. They are not wired individually to the roboRIO — they share a **CAN bus**, a daisy-chained two-wire network.

**What you must know:**

- Every device has a **unique numeric ID**. Duplicate IDs cause chaos that presents as random mechanism behavior
- The roboRIO sends a **heartbeat every 20ms**. Devices that lose it **self-disable after 100ms** — this is a safety feature, and it is why a loose CAN wire makes a mechanism go limp rather than run away
- **Bus utilization above 90% causes indeterminate behavior**, per CTRE. Every status signal you leave enabled costs bandwidth

CAN problems look like software problems. When a mechanism behaves impossibly, check wiring and IDs before you debug code.

### Sensors

| Sensor | Gives you | Watch out for |
|---|---|---|
| **Integrated encoder** (inside the TalonFX) | Motor rotations | Measures the *motor*, not the mechanism — divide by gear ratio |
| **Absolute encoder** (CANcoder) | True position, survives power cycle | Needs an offset configured |
| **Gyro / IMU** (Pigeon 2) | Heading | Drifts slowly. Vision corrects it in M5 |
| **Limit switch** | At-the-end boolean | Check polarity. Normally-open vs normally-closed inverts your logic |

**Absolute versus relative is the distinction that matters.** A relative encoder reads zero at boot wherever the mechanism happens to be. If your arm boots at 30 degrees, it thinks it is at zero and drives itself into the frame. Either use an absolute encoder or **home** the mechanism at startup — drive it slowly to a limit switch, then zero.

### Units

Use WPILib's type-safe units. Ignite does:

```java
private AngularVelocity velocityTarget;   // not "double velocityTargetRadsPerSec"
private Angle hoodTarget;
```

The compiler then stops you adding radians to degrees. Unit confusion is a top source of FRC control bugs and it is entirely preventable.

If you use raw doubles, **put the unit in the name** — `speedRPS`, not `speed`.

### Feedforward: teach this first, and use it first

**Feedforward is a model of what your mechanism needs.** Feedback corrects what is left over. WPILib states directly that "an accurate feedforward is more important than feedback to effective control of a mechanism."

```java
voltage = kS * signum(velocity) + kV * velocity + kA * acceleration
```

| Term | Compensates for | Intuition |
|---|---|---|
| **kS** | Static friction | Voltage to just barely start moving |
| **kV** | Velocity | Voltage to hold a given speed. **The big one** |
| **kA** | Acceleration | Extra voltage to speed up. Often negligible |
| **kG** | Gravity | Arms and elevators only. Holds position against weight |

Three variants: `SimpleMotorFeedforward` (flywheels, drivetrains), `ArmFeedforward` (kG scales with the cosine of the angle), `ElevatorFeedforward` (kG constant).

**Why order matters.** Tune feedforward first, and the mechanism nearly goes where you want with no feedback at all. Then a small amount of feedback cleans up the rest. Do it backwards and you get a large kP fighting a bad model — which works at exactly one setpoint and one battery voltage, and is precisely what the test-out challenge is about.

### PID: feedback

```java
output = kP * error + kI * integral(error) + kD * derivative(error)
```

- **kP** — proportional to how far off you are. **A software spring**: pulls harder the further away you get
- **kI** — accumulates error over time. **WPILib says the I term is "generally not recommended for FRC use."** It causes windup and oscillation, and usually means your feedforward is wrong. Start at zero and leave it there
- **kD** — responds to how fast error is changing. **A software damper**: resists rapid change, reduces overshoot

**Tuning procedure:**

1. Get feedforward right first. Confirm the mechanism roughly tracks with kP = 0
2. Raise kP until it responds crisply. Too high oscillates
3. Add kD only if it overshoots
4. Leave kI at zero

**Position versus velocity control.** A flywheel controls velocity. An arm controls position. Same math, very different feel — a velocity loop tolerates a much higher kP than a position loop.

### SysId

Stop guessing gains. WPILib's SysId tool runs your mechanism through a characterization routine, records the response, and computes kS, kV, and kA.

Ignite has SysId routines wired already — `ShooterSubsystem` uses `SysIdRoutine` and CTRE's `SignalLogger`.

**Do it in simulation first.** The workflow is identical and you cannot break anything.

### Motion profiling

Commanding an arm to jump from 0 to 90 degrees asks for infinite acceleration. A motion profile generates a smooth path — accelerate, cruise, decelerate — respecting limits you set. The controller follows the profile instead of chasing a step.

The result is mechanisms that move fast without slamming, and far less wear.

### CTRE Phoenix 6 specifics

Everything in this section is about the **competition** robot. Your M4 project computes its control loop in Java and commands a voltage; `Robot-2026` hands the gains to the motor controller and lets it close the loop on-device at 1 kHz. Both are correct, and on real hardware the on-device loop is usually the better answer — it runs faster than your 20 ms robot loop and keeps running if your code stutters. The training project does it the other way because CTRE's *simulation* runs its control loop on wall-clock time, which a stepped test cannot drive deterministically. The gains are the same numbers either way; only who multiplies them changes.

**Configuration objects** are passive data structures, chainable, and applied once:

```java
var config = new TalonFXConfiguration();
config.Slot0.kP = 0.1;
config.Slot0.kV = 0.12;
motor.getConfigurator().apply(config);
```

- `apply()` **blocks**. Call it at init, never in `periodic()`
- You do **not** need to factory-default first — a new config object already starts from defaults
- **`.clone()` a base config** to share settings across near-identical devices

**Status signals** are cached. Get them once, refresh them deliberately:

```java
BaseStatusSignal.refreshAll(pos1, vel1, pos2, vel2);   // batch, not one at a time
BaseStatusSignal.waitForAll(0.02, ...);                // synchronized multi-device read
```

Batching matters for CAN bandwidth, and `waitForAll` matters for odometry, where you want all four swerve modules and the gyro read at the same instant.

**Two gaps in Ignite's own code, worth knowing about:**

1. `Robot-2026` contains **zero** uses of `refreshAll`, `waitForAll`, or `optimizeBusUtilization`. Signal handling relies entirely on CTRE's generated swerve defaults. Not broken, but not deliberate either.
2. `DriveMotorConfigs.java` has **four near-identical `create*Slot0Configs()` methods**, one per swerve corner, each repeating the same five-field assignment. CTRE's `.clone()` pattern would collapse them. **This is your stretch project.**

## Project

**The skeleton is [`projects/m4-control/`](https://github.com/IgniteRobotics/ignition-labs/tree/main/projects/m4-control).** Two simulated plants are given — a flywheel and a gravity-loaded arm — with their true parameters deliberately withheld. Characterizing a system you cannot look up is the milestone.

**Closed-loop control in simulation, two mechanisms.**

**You write the control loop in Java**, against the given plants: read the measurement, compute feedforward plus feedback, command a voltage. `ShooterSubsystem` closes the same loop *on the device*, with `Slot0` gains and `VelocityVoltage`, and that is how the competition robot does it — read it, because the stretch project and M5 both assume you have. The training project runs the loop in Java for a reason worth knowing: CTRE's simulation advances its onboard control loop and its status-signal pipeline on wall-clock time, so a test that steps the simulation cannot drive an on-device loop or read a measurement back from it. The same gains you characterize here are the numbers you would put in `Slot0`.

**Part 1 — velocity control (flywheel).**
1. Characterize it with SysId in sim; record kS, kV, kA
2. Apply feedforward alone. Confirm it roughly holds speed with kP = 0
3. Add kP until it tracks crisply
4. Produce a plot of commanded versus actual velocity through a step change
5. Be able to read your own plot

**Part 2 — position control with gravity (arm).**
1. Characterize it; get kG
2. Confirm feedforward alone holds position against gravity
3. Add kP, then kD if it overshoots
4. Add a motion profile. Compare the plots
5. Implement homing: drive slowly to a limit switch at startup, then zero

**Stretch — real code.** Refactor `DriveMotorConfigs.java` to use CTRE's `.clone()` pattern. Small, safe, real, and it makes the file substantially shorter. Open a PR.

**Optional hardware variant.** Same exercises on last season's robot or a benchtop mechanism. Simulation first, always — you cannot break a simulated gearbox.

## Definition of done

- [ ] `./gradlew test` is green
- [ ] Flywheel reaches and holds commanded velocity in simulation
- [ ] Arm holds position against simulated gravity
- [ ] Arm homes correctly at startup
- [ ] You can explain what each of kS, kV, kA, kG does
- [ ] You can explain why feedforward is tuned before kP
- [ ] You produced plots for both and can read them
- [ ] You ran SysId yourself and used the numbers it produced
- [ ] Motion profile demonstrated, with a before/after plot
- [ ] You can explain why kI stays at zero
- [ ] You can explain what happens when a CAN device stops hearing the roboRIO
- [ ] **You have opened at least one pull request against a real robot repository.** Required, and the refactor above is only the recommended vehicle — a documentation fix or a typo correction counts. The skill being rehearsed is the ritual, not the refactor, and M6's pull request is assessed. That is a bad place to attempt one for the first time

## Common failure modes

**Tuning kP first.** The most common mistake. Produces something that works at one setpoint and falls apart everywhere else.

**Using kI to fix steady-state error.** The error is almost always bad feedforward or unmodeled friction. kI hides it and adds windup.

**Gear ratio applied twice, or not at all.** Motor rotations are not mechanism rotations. Symptom: everything off by exactly the gear ratio.

**Forgetting the mechanism starts wherever it was left.** Relative encoders read zero at boot. Home, or use absolute.

**Calling `apply()` in `periodic()`.** It blocks. Your loop overruns and the robot stutters. Init only.

**Duplicate CAN IDs.** Impossible-looking behavior. Check IDs before debugging code.

**Tuning on a low battery.** Gains that work at 12.6V behave differently at 11.5V. This is also *why* feedforward matters — a good model degrades gracefully; a big kP does not.

**Not writing down what you tried.** You will land on numbers and have no idea how. M9 makes the tuning log a real deliverable; start the habit now.

## AI posture — 🟡 Yellow

**Allowed:** explaining what a CTRE API does · Javadoc · stack trace triage · boilerplate config code · asking it to review a subsystem you wrote

**Not allowed — and this one is specific and important: never ask an AI for a gain value.**

It does not know your mechanism. It has never seen your gearbox, your moment of inertia, or your battery. It will produce a confident, plausible number, and that number is worthless. Characterize with SysId. That is what M4 is for.

Also expect **deprecated API suggestions**. These models routinely recommend Phoenix 5 or old WPILib patterns. When AI output disagrees with the current CTRE or WPILib docs, the docs win.

## Resources

See `resources.md` for the full library. For this milestone:

- **WPILib: Introduction to PID** — P as a spring, D as a damper, and the position on kI
- **WPILib: Feedforward Control** — the four terms and three variants
- **WPILib: System Identification (SysId)** — characterizing instead of guessing
- **WPILib: CAN Bus Addressing** — IDs, heartbeat, the 100ms timeout
- **CTRE: Configuration** — config objects, chaining, `.clone()`, why `apply()` is init-only
- **CTRE: Status Signals** — caching, `refreshAll`, `waitForAll`, bus utilization
- **CTRE: Simulation Introduction** — driving a physics model from motor voltage
- **Team 5712 Gray Matter Coding Workshop** — closed-loop control on a CTRE rig, vendor-matched to ours
- **Ignite:** `Robot-2026/.../shooter/ShooterSubsystem.java` — velocity control, typed units, SysId in production
- **Ignite:** `Robot-2026/.../drive/DriveMotorConfigs.java` — your stretch project
- **Ignite:** `docs/field-boundary-speed-limiting.md` — an already-written design project, if you want a bigger one
