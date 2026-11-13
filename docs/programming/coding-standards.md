# Ignite Robotics — Coding Standards

**Applies to:** All Java in `Robot-2026` and every season repository after it; Python and
JavaScript in Flashpoint (its own section below)
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

### No blocking loops in commands or subsystems

Avoid the use of `while` loops within commands or subsystems.

*Why:* every subsystem's `periodic()` and every command's `execute()` runs on the same thread,
once every 20 ms, one after another. A loop that spins until something becomes true does not
wait — it stops the entire robot program, including the code that would have made the condition
true. Waiting is what `Commands.waitUntil()`, a command's `isFinished()`, and a state machine
are for.

---

## Team conventions

### Naming conventions

- `camelCase` for variables and methods
- `PascalCase` for classes
- lowercase for package names
- `UPPER_SNAKE_CASE` for constants

*Why:* these carry information no other part of the syntax does — seeing `MAX_HEIGHT` tells you
the value never changes, and seeing `Elevator` tells you it is a type before you have looked
anything up. They are worth having mostly because they are the *same* everywhere: a codebase
with three conventions in it costs every reader a moment on every identifier, forever, and no
one convention was ever worth that.

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

> **[Editorial note — not part of the standard.]** The three "Good" lines above do not compile
> against WPILib 2026, the version this document names. On 2026, `Distance`, `LinearVelocity` and
> `Voltage` are *measures* living in `edu.wpi.first.units.measure`, and only the *units* —
> `DistanceUnit`, `VoltageUnit` — live in `edu.wpi.first.units`. `Measure<U>` is declared
> `Measure<U extends Unit>`, so `Measure<Distance>` is rejected twice over: the name does not
> resolve under the import shown, and a measure is not a unit. `Meters.of(1.5)` already returns
> `Distance`, so the type parameter has nothing left to do. The current form is:
>
> ```java
> import edu.wpi.first.units.measure.*;
> import static edu.wpi.first.units.Units.*;
>
> Distance height = Meters.of(1.5);
> LinearVelocity velocity = MetersPerSecond.of(2.0);
> Voltage voltage = Volts.of(12.0);
> ```
>
> The *Why* below inherits the same staleness: read `Measure<Angle>` there as `Angle`.
> `Robot-2026` already writes the modern idiom in its own source — `IntakeConstants.java:130` is
> `Measure<CurrentUnit>`, parameterized on a unit, not on a measure. The example is reproduced
> verbatim because the pinned source is the source of truth for this document; the fix belongs
> upstream in `Robot-2026`'s prompt, followed by a re-copy of the reference file — not a
> hand-patch here.

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

> **[Editorial note — not part of the standard.]** In the source this example is copied from,
> both enable flags read `false`, as reproduced above. As written the example configures two
> thresholds and then leaves them switched off, which is the opposite of what the standard above
> it says to do. This looks like a defect in `Robot-2026`, not an intended standard: **enable
> the limits you configure.** The example is reproduced verbatim because the pinned source is
> the source of truth for this document; the fix belongs upstream in `Robot-2026`, followed by
> a re-copy of the reference file — not a hand-patch here.

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

> **[Editorial note — not part of the standard.]** `SupplyCurrentThreshold` and
> `SupplyTimeThreshold` no longer exist on Phoenix 6's `CurrentLimitsConfigs`; the example above
> does not compile against the pinned vendor version. Their replacements are
> `SupplyCurrentLowerLimit` and `SupplyCurrentLowerTime` — **and the rename inverted which field
> holds which number.** `SupplyCurrentLimit` is now the *peak* current allowed; after
> `SupplyCurrentLowerTime` at that peak, the limit drops to `SupplyCurrentLowerLimit`. Renaming
> the fields in place would silently swap the sustained and peak values. The form that preserves
> what the example above intends — 40 A sustained, 60 A tolerated for 0.1 s — is:
>
> ```java
> TalonFXConfiguration config = new TalonFXConfiguration();
> config.CurrentLimits.SupplyCurrentLimit = 60.0;      // peak
> config.CurrentLimits.SupplyCurrentLowerLimit = 40.0; // sustained, after the time below
> config.CurrentLimits.SupplyCurrentLowerTime = 0.1;
> config.CurrentLimits.SupplyCurrentLimitEnable = true;
> motor.getConfigurator().apply(config);
> ```
>
> Reproduced verbatim for the same reason as the note above: the fix belongs upstream in
> `Robot-2026`'s prompt, followed by a re-copy of the reference file.

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

## Flashpoint (Python and JavaScript)

Everything above is about robot Java. This section is for
[Flashpoint](https://github.com/IgniteRobotics/flashpoint), the team's telemetry platform: a
Python backend and a small browser app. It has two kinds of rule. **Check** rules fail the
required pull-request check, so you'll know before review. **Review** rules depend on judgment
and are enforced by a reviewer. Flashpoint's developer guide carries this same text. If the two
ever differ, this page is right and the guide gets fixed.

### Python: formatting, naming, and types

- Formatting and import order come from `ruff format` and `ruff check`. Don't argue with the
  formatter. *(Check)*
- `snake_case` for functions and variables, `PascalCase` for classes, `UPPER_SNAKE_CASE` for
  constants, `kebab-case` for file and directory names outside the Python package. This is
  different from the Java naming above. Each language follows its own community's convention,
  and nobody should have to translate. *(Check, except filenames: Review)*
- Type hints on everything. `mypy --strict` passes. *(Check)*

*Why:* the formatter makes every file look the same, so a diff shows only what changed. Strict
types catch a whole class of bug before the code runs, and they tell the next reader what a
function expects without them having to trace every caller.

### Python: suppressions say which rule and why

Turning off a rule names the rule and gives a reason on the same line:
`# noqa: S608 - table name from the internal registry`, or for the type checker
`# type: ignore[arg-type]  # fixture dict is untyped`. A bare `# noqa`, a `# type: ignore`
with no error code or no reason, or a suppression for a rule that isn't firing all fail the
check. *(Check)*

*Why:* a suppression is a claim that you looked at this line and it's fine. Without a rule name
it hides every future problem on that line too. Without a reason, the next person can't tell
whether you were right.

### Python: processes, shells, and SQL

- Library code never exits the process. Only the command-line entry point calls `sys.exit`.
  *(Check)*
- Subprocesses take an argument list, never a shell string. `shell=True` and `os.system` are
  never allowed, even with a suppression. *(Check)*
- SQL is built only from internal identifiers, never from user or log data. Values go in as
  bound parameters. A line that builds SQL from strings says which identifiers it fills in.
  *(Check)*

*Why:* one bad log calling `sys.exit` in the middle of a batch import stops every log after
it. A shell string with a filename in it breaks on the first space and can run commands the
filename smuggled in. SQL built from data is the same bug.

### Python: errors, output, and time

- Don't catch every exception, except at a boundary that records the error and carries on: a
  per-file ingest loop, a request handler, a service loop. The suppression names the boundary.
  *(Check)*
- Library code doesn't `print`. It logs, or returns data for the CLI to show. *(Check)*
- Date-times are timezone-aware. *(Check)*
- Functions stay under cyclomatic complexity 12. The few that were over when this standard was
  adopted are marked grandfathered and must not get worse. *(Check for new code, Review for
  grandfathered functions)*

*Why:* a catch-all in the wrong place turns a crash you would have seen into wrong numbers you
won't see. Robot logs come from different clocks and time zones, and a naive date-time is
off by hours without saying so. A function too complex to hold in your head is too complex to
review.

### Python: tests

- Every change comes with tests. Unit tests use synthetic fixtures; tests that need real logs
  are marked `corpus`. *(Review)*
- Coverage never drops below the floors in `pyproject.toml`: an overall floor and 85% on the
  reader and lake packages. Floors go up, not down. Lowering one needs a mentor. *(Check)*
- Once implementation of a change starts, nobody edits its tests to make them pass. *(Review)*

*Why:* the readers and the lake turn a log into every number Flashpoint shows. If they're
wrong, every chart is wrong, and nothing on the screen will tell you.

### Browser app: scripts, escaping, and offline

- App scripts are classic scripts: no `import`/`export`, no build step. Each file runs in
  strict mode and touches only the `FP` namespace, the mode object, and the vendored chart
  library. *(Check)*
- Views build the page only through the app's `h()` DOM builder. No `innerHTML`, inline
  handlers, `eval`, or `new Function`. *(Check)*
- No remote hosts. Everything the app loads ships with it. *(Check)*
- Colours come from the role-named theme tokens, never a literal. *(Check)*
- Vendored libraries are never edited. *(Check)*

*Why:* the match report has to open by double-clicking a file on a pit laptop with no network,
and browsers refuse ES modules from `file://`. Log data includes names that came from robot
code, so anything that turns strings into HTML is an injection waiting for the wrong match
name. Tokens let another team re-theme the app without touching code.

### Browser app: lint and format

JavaScript and CSS pass Biome's recommended rules, with warnings treated as errors, and match
Biome's formatter. A `biome-ignore` comment carries a reason. *(Check)*

*Why:* the same reasons as ruff: one style, so a diff shows only what changed, and a linter
that catches unused variables and loose comparisons before a reviewer has to.

### Running the checks

`poetry run pre-commit run --all-files` runs every formatter and linter, at the same versions
CI uses. Run it before you push.

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

**Two standards promoted from *outside* lines 53–300** — so that anyone diffing the two
documents by eye does not read them as drift:

| Promoted | Source line | Landed at |
|---|---|---|
| Naming conventions — camelCase / PascalCase / lowercase packages / `UPPER_SNAKE_CASE` | 19 | Team conventions → Naming conventions |
| No `while` loops in commands or subsystems | 45 | Architecture → No blocking loops in commands or subsystems |

Both sit inside the review prompt's *What to Review* section, which is otherwise review scope
rather than standards. They were promoted anyway, by ruling, for two reasons. The design
record's ownership boundary states that this document owns "patterns, units, **naming**,
safety" — naming is explicitly ours, and its landing in a scope section of the prompt is an
accident of where a heading fell. And the `while`-loop rule passes the same test everything
else here passes: it describes what good code looks like, not how a review is conducted. The
two do not rest equally well: naming is named in the ownership boundary, while the
`while`-loop rule is promoted on the weaker argument that it reads as a standard — the
asymmetry was seen, not missed, and a future reader narrowing this document should reopen the
second before the first.

M1 raises the naming question directly — three conventions in one codebase, and nobody wrote
the rule down. This is where it is written down.

**Cosmetic corrections were made while promoting.** Typos were fixed ("Subsytems" →
"Subsystems"), a marketing adjective dropped ("Revolutionary logging framework" → "Logging
framework"), title-cased fragments sentence-cased ("Vision Processing" → "Vision processing"),
doubled spaces collapsed, terminal punctuation made consistent, and code identifiers put in
backticks. These are the deltas a reader diffing the two documents by eye will find that no
table above explains; none of them changes what any standard requires.

**What was not promoted, and why:** the rest of lines 1–52 (tone, review scope, and what a
reviewer should not focus on) and lines 301–311 (the review's output format) are instructions
for conducting a review, not statements about what good code looks like. They stay with the
prompt.

**Three examples carry editorial notes.** Each is reproduced verbatim with a marked note beside
it. The notes are commentary, not standards, and every correction belongs upstream in
`Robot-2026` followed by a re-copy of the reference file — never a hand-patch here.

| Example | What is wrong with it |
|---|---|
| `createSoftLimitConigs()`, under *Mechanism limits* | Sets both enable flags to `false` in the pinned source, contradicting the standard it illustrates. A semantic defect; the API is current |
| Type-safe units, under *Team conventions* | Does not compile on WPILib 2026. `Measure<Distance>`, `Measure<Velocity<Distance>>` and `Measure<Voltage>` all name measure types where `Measure<U extends Unit>` requires unit types, and none of the three resolve under the import shown. The 2026 form is the bare measure type |
| Current limiting, under *Safety and error handling* | Does not compile on the pinned Phoenix 6. `SupplyCurrentThreshold` and `SupplyTimeThreshold` were replaced by `SupplyCurrentLowerLimit` and `SupplyCurrentLowerTime`, and the replacement inverted which field carries the peak and which the sustained value |

The last two are the same class of error and worth naming as one: the promotion audit checked
that every standard in the source has a home here, which is a structural check and cannot tell
whether the code inside a standard still compiles. Every code example in this document was
subsequently checked against the pinned toolchain — WPILib `2026.2.1` and Phoenix 6 `26.1.x` —
by inspecting the shipped jars. The remaining examples are either plain Java or use API that is
still current. A reader who finds a fourth should add a note in this same form rather than
editing the example.

**The Flashpoint section has a different source.** It was not promoted from the review prompt.
It was written for Flashpoint's `code-quality-gates` OpenSpec change
(`openspec/changes/code-quality-gates/specs/coding-standards/spec.md` in the Flashpoint repo),
which is also what wires each *(Check)* rule into Flashpoint's CI. Direction of truth is the
same as for the rest of this document: this page is the source, and Flashpoint's developer
guide follows it. A new Flashpoint rule is added here first. If it's a *(Check)* rule, the
Flashpoint change that enforces it lands with it.

**Status.** This document is a **draft** until a season lead ratifies it. Draft means the
standards in it are Ignite's current practice as written down for the first time, not that they
are optional.
