# Ignite Robotics — Coding Standards

**Applies to:** All Java in `Robot-2026` and every season repository after it
**Status:** **Draft.** Not ratified
**Owner:** ⚠️ UNASSIGNED — TODO(P2): a season lead must be named here. This line must not merge as a placeholder.
**Ratified by:** —
**Effective:** on ratification
**Review date:** end of the 2027 build season

---

## What this document is

This is Ignite's **constitution**: the engineering decisions that hold across *every* change,
in every subsystem, in every session. It is the counterpart to a spec. A spec is the decisions
for one change — what this feature must do, and how you will know it works. A constitution is
the decisions you do not re-make each time you open the editor.

Both exist for the same reason: they are the things you write down **so that a tool cannot
decide them for you.** An agent handed a spec and this document is working inside your
engineering judgment. An agent handed neither is supplying its own.

Two companions, and the boundary between them:

- [AI Use Standard](ai-use-standard.md) answers *what may I use a tool for, at my tier?* It
  contains no Java conventions.
- This document answers *what does good Ignite code look like?* It contains no AI policy.

The team's AI code reviewer reviews against this document. When the two disagree, this
document is the source and the reviewer is what needs fixing — see **Provenance** at the
bottom.

---

## Architecture

### Command-Based Architecture

- **Framework**: Build on WPILib's command-based architecture with `LoggedRobot` or
  `TimedRobot` base classes.
- **Subsystem Organization**: Organize code into focused subsystems representing physical
  mechanisms (drive, intake, arm, vision, LEDs, etc.).
- **Superstructure Pattern**: Implement high-level Superstructure/StateMachine classes to
  coordinate multi-subsystem behaviors and prevent conflicts.
- **State Management**: Use explicit state machines (enums with switch statements or
  graph-based transitions) rather than implicit boolean flags.

*Why:* a subsystem that maps onto a physical mechanism is a subsystem you can reason about
while looking at the robot, and WPILib's requirement system can only stop two commands from
fighting over a mechanism if the mechanism is a subsystem. Implicit boolean flags are a state
machine you did not admit to writing — with `2^n` states, no names, and no way to see which
one you are in.

### Design Patterns

- **Singleton Subsystems**: Prevent multiple instances of a subsystem.
- **State Machine Pattern**: Implement explicit state machines for coordinating complex
  behaviors using enumerated states.
- **Graph-Based State Machines**: For complex multi-step sequences, use JGraphT with states as
  nodes and transitions as edges.
- **Command Composition Pattern**: Build complex autonomous routines through composition.

*Why:* there is exactly one physical elevator, so two `Elevator` objects means two sets of
configuration racing each other onto the same CAN device. Composition is what lets an
autonomous routine be assembled from pieces you have already tested individually, instead of
one long method you can only test by running the whole auto.

### Core Libraries

- **WPILib 2025 or 2026**: Core FRC framework with command-based architecture, typesafe units,
  NetworkTables. Use the current published version.
- **CTRE Phoenix 6+**: Industry standard for TalonFX/Kraken motors, Pigeon 2 IMU, CANcoder
  encoders.
- **AdvantageKit 4+**: Logging framework with deterministic replay, `@AutoLog` annotation
  processing.
- **ChoreoLib 2025+** or **PathPlannerLib 2025+**: Trajectory planning for autonomous.
- **Photon Vision**: Vision processing.
- **Logging**: Epilogue for subsystems, commands, and telemetry. CTRE Hoot logging for devices.

*Why:* a fixed library set is what makes code from one season readable in the next, and it is
what makes a question answerable by anyone else on the team. Picking a different trajectory
library for one subsystem costs everyone else the ability to help you debug it.

---

## Team conventions

### Type-safe units

Always use WPILib's Units system to prevent conversion errors:

```java
import edu.wpi.first.units.*;
import static edu.wpi.first.units.Units.*;

// Good
Measure<Distance> height = Meters.of(1.5);
Measure<Velocity<Distance>> velocity = MetersPerSecond.of(2.0);
Measure<Voltage> voltage = Volts.of(12.0);

// Bad
double height = 1.5; // What units? Meters? Inches?
```

*Why:* a `double` that means radians and a `double` that means degrees are the same type, so
the compiler cannot help you. A `Measure<Angle>` and a raw `double` are not the same type, so
it can.

### Command factory methods

Subsystems should expose command factory methods rather than public setters:

```java
public class Elevator extends SubsystemBase {
    // Bad: Exposes internal state
    public void setPosition(double meters) { ... }

    // Good: Returns command encapsulating behavior
    public Command setPositionCommand(double meters) {
        return runOnce(() -> setpoint = meters)
            .andThen(Commands.waitUntil(() -> atSetpoint()))
            .withName("ElevatorTo" + meters);
    }

    // Named preset commands
    public Command stowCommand() {
        return setPositionCommand(Constants.STOW_HEIGHT);
    }
}
```

*Why:* a command carries the subsystem requirement with it, so the scheduler can interrupt it
and stop two callers from driving the same mechanism at once. A public setter carries none of
that, and any caller anywhere can move the mechanism with nothing arbitrating.

### Use position or velocity control

Always use the CTRE position and velocity control if possible:

```java
    public void setTargetAngle(Rotation2d target) {
        shoulderOne.setControl(
                motionMagicVoltage.withPosition(target.getRadians() / ArmConstants.SHOULDER_POSITION_COEFFICIENT));
    }

    public ControlRequest getMotionMagicRequest(Angle mechanismPosition) {
        return new MotionMagicExpoVoltage(mechanismPosition).withSlot(0).withEnableFOC(true);
    }

    public ControlRequest getVelocityRequest(AngularVelocity mechanismVelocity) {
        return new VelocityTorqueCurrentFOC(mechanismVelocity).withSlot(1);
    }

    public ControlRequest getPositionRequest(Angle mechanismPosition) {
        return new PositionTorqueCurrentFOC(mechanismPosition).withSlot(2);
    }
```

*Why:* the closed loop runs on the motor controller at a far higher rate than the roboRIO's
20 ms periodic, and it keeps running through a loop overrun. A control loop written in
`periodic()` is a control loop whose timing is at the mercy of everything else in the robot
program.

### Automatic homing

Implement automatic homing for mechanisms without absolute encoders:

```java
private enum HomingState { NEEDS_HOMING, HOMING, HOMED }
private HomingState homingState = HomingState.NEEDS_HOMING;

@Override
public void periodic() {
    if (homingState == HomingState.HOMING) {
        if (Math.abs(inputs.velocityRadPerSec) < HOMING_VELOCITY_THRESHOLD) {
            homingDebounceCount++;
            if (homingDebounceCount > HOMING_DEBOUNCE_SAMPLES) {
                motor.setPosition(0.0);
                homingState = HomingState.HOMED;
            }
        }
    }
}
```

*Why:* a relative encoder reads zero wherever the mechanism happened to be at power-on, so
every position setpoint and every soft limit is measured from a lie until something establishes
the real zero. Homing in code means that happens the same way every match, instead of depending
on someone remembering to park the arm before the robot boots.

### Thread priority management

Elevate critical threads for consistent control loop timing:

```java
// In Robot.java
Thread.currentThread().setPriority(4); // Elevated priority
```

*Why:* the main robot thread competes with logging, vision, and NetworkTables for the roboRIO's
CPU. Left at default priority, a heavy vision frame can delay the control loop, and delayed
control is indistinguishable from badly tuned control when you go looking for the cause.

### Cached sensor reads

Prevent redundant CAN bus calls with cached values:

```java
public class CachedDouble {
    private double value;
    private boolean updated = false;

    public void set(double value) {
        this.value = value;
        this.updated = true;
    }

    public double get() {
        if (!updated) {
            // Refresh from hardware
        }
        return value;
    }

    public void reset() {
        updated = false;
    }
}

// Call reset() once per periodic cycle
```

*Why:* the CAN bus has a fixed bandwidth shared by every device on the robot, and calling a
getter five times in one loop asks for the same value five times. Reading once per cycle also
means every calculation in that cycle uses the *same* sensor value, rather than several values
sampled microseconds apart.

### Javadoc documentation

Document public APIs, especially base classes and interfaces:

```java
/**
 * Base class for servo motor subsystems with automatic homing.
 *
 * @param <IO> The IO interface type for hardware abstraction
 */
public abstract class ServoMotorSubsystem<IO extends MotorIO> extends SubsystemBase {
    /**
     * Creates a command to move to the specified position.
     *
     * @param position Target position in mechanism units
     * @return Command that completes when position is reached
     */
    public Command setPositionCommand(double position) { ... }
}
```

*Why:* a public method on a base class is a contract with everyone who extends it, including
next season's students. The units a parameter expects and the condition under which a command
finishes are not recoverable from the signature, and guessing wrong about either one moves a
mechanism somewhere you did not intend.

---

## Safety and error handling

### Mechanism limits

Use device built in limits if available:

```java
public static SoftwareLimitSwitchConfigs createSoftLimitConigs(){
            SoftwareLimitSwitchConfigs newConfigs = new SoftwareLimitSwitchConfigs();
            newConfigs.ForwardSoftLimitEnable = false;
            newConfigs.ReverseSoftLimitEnable = false;
            newConfigs.ForwardSoftLimitThreshold = CLIMBER_FORWARD_SOFT_LIMIT;
             newConfigs.ReverseSoftLimitThreshold = CLIMBER_REVERSE_SOFT_LIMIT;
                return newConfigs;
        }
```

If no built in limits, then enforce software limits:

```java
public void setPosition(double position) {
    // Clamp to safe range
    position = MathUtil.clamp(position, MIN_POSITION, MAX_POSITION);
    setpoint = position;
}
```

*Why:* a limit enforced on the motor controller still holds when the roboRIO code is the thing
that is broken, which is exactly the case where you need it. A limit enforced only in a
`periodic()` that stopped running is not a limit. Mechanisms driven past their travel break
themselves, and they break during a match, not during testing.

### Current limiting

Protect motors from overcurrent:

```java
TalonFXConfiguration config = new TalonFXConfiguration();
config.CurrentLimits.SupplyCurrentLimit = 40.0;
config.CurrentLimits.SupplyCurrentThreshold = 60.0;
config.CurrentLimits.SupplyTimeThreshold = 0.1;
config.CurrentLimits.SupplyCurrentLimitEnable = true;
motor.getConfigurator().apply(config);
```

*Why:* a stalled motor draws its full stall current until something stops it — which is either
a current limit you configured or a breaker trip that takes the whole mechanism offline
mid-match. The limit is also what keeps one stalled mechanism from browning out the rest of the
robot.

### Fault detection

Monitor and log hardware faults:

```java
@Override
public void periodic() {
    if (inputs.motorFaults != 0 || inputs.motorStickyFaults != 0) {
        Logger.recordOutput("Elevator/Faults", true);
        DriverStation.reportWarning(
            "Elevator motor fault detected: " + inputs.motorFaults,
            false
        );
    }
}
```

*Why:* hardware failures are silent by default — a motor controller that browned out or lost
its encoder simply stops behaving, and from the driver station it looks like a code bug. A
logged fault turns twenty minutes of debugging the wrong thing into one line of the log.

### Match logging

Explicitly log match events:

```java
@Override
public void autonomousInit() {
    Logger.recordOutput("Match/AutonomousStart", Timer.getFPGATimestamp());
    Logger.recordOutput("Match/SelectedAuto", autoChooser.getSelected().getName());
}

@Override
public void teleopInit() {
    Logger.recordOutput("Match/TeleopStart", Timer.getFPGATimestamp());
}
```

*Why:* between matches there is no time to reproduce anything, so the log is the only evidence
you have. Knowing which auto was actually selected and when each period began is what turns
"it did something weird in auto" into a specific window of a specific log to look at.

---

## Provenance

**Source:** `projects/m7-ai-assisted/reference/code-review-prompt.md`, lines 53–300 — the
reference copy of `Robot-2026`'s `.github/prompts/code-review-prompt.md`.

**Pinned at:** `Robot-2026` commit `28ce5397d88275001d656dbb717d22cf9ac0150d`.

**Direction of truth.** This document is the source; the review prompt is downstream of it. A
standard is written down in exactly one place, and that place is here. A new standard is added
here first, and the review prompt is what changes to match — never the other way round.

**What was dropped from lines 53–300:** nothing. Every standard in that range has a home above,
in the source's own order and grouping.

**What was not promoted, and why:** lines 1–52 (tone, review scope, and what a reviewer should
not focus on) and lines 301–311 (the review's output format) are instructions for conducting a
review, not statements about what good code looks like. They stay with the prompt.

**Status.** This document is a **draft** until a season lead ratifies it. Draft means the
standards in it are Ignite's current practice as written down for the first time, not that they
are optional.
