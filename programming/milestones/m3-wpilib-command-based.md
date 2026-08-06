# M3 — WPILib Command-Based Programming

## At a glance

The framework every line of Ignite's robot code is written in. This is the center of gravity of the whole curriculum and the last milestone in the "everyone must have this before kickoff" block.

By the end you can read `RobotContainer.java` and understand what it does — which is the actual bar for contributing during a season.

**Estimated time:** 5–7 sessions. The largest milestone.
**Prerequisites:** M0, M1, M2
**AI posture:** 🟡 Yellow (first Yellow milestone)

## Who enters here

**Track C** enters here, after testing out of M1 and M2.

**Tracks A and B** arrive here having worked M0–M2.

**Track D** tests out.

## Test-out challenge

Given a working `RobotContainer` with three subsystems:

1. Add a new command that requires two of them
2. Bind it to a controller trigger
3. Explain what happens if it is scheduled while another command already requires one of those subsystems

**Pass criteria:** the code works, and part 3 is answered correctly — the running command is interrupted and its `end(true)` runs. A student who says "they both run" or "it queues" has not got the scheduler model.

**Time:** 60 minutes.

## Concepts

### The 20ms loop

Robot code runs `TimedRobot`, which calls your periodic methods every 20 milliseconds — 50 times a second. Everything the robot does is a consequence of what happens in those slices.

**This is why you never block.** A `Thread.sleep()`, a `while` loop that waits for a sensor, or anything slow stops *everything* — driver control, safety systems, the works. Recall from M1: **check, do not wait.**

Command-based programming exists to make "check, do not wait" the natural way to write code instead of a discipline you have to remember.

### Why command-based exists

Imagine writing an intake sequence without a framework. Extend the intake, wait until it is out, spin the rollers, wait for a game piece, stop, retract. You cannot wait for any of it. So you write a state variable, a switch statement, and a lot of `if (extensionDone && !rollerStarted)`. Then you add a second mechanism that needs to run at the same time, and it gets much worse.

Team 8744's lessons frame the contrast well: manual task micromanagement versus a system where "tasks are treated separately" and a scheduler handles concurrency.

Command-based gives you two things: a way to describe *what a mechanism can do* (subsystems and commands) and a scheduler that runs many of them at once without you writing the bookkeeping.

### Subsystems

A subsystem owns hardware. One subsystem per mechanism.

```java
public class IntakeSubsystem extends SubsystemBase {
  private final TalonFX rollerLeader;

  public IntakeSubsystem() {
    rollerLeader = new TalonFX(IntakeConstants.ROLLER_ID);
  }

  @Override
  public void periodic() {
    // runs every 20ms, always
  }
}
```

**The rule that makes the framework work:** only the subsystem touches its own hardware. Nothing else gets a reference to that motor. This is why a mechanism can never be commanded by two things at once — the framework can enforce it, because all access goes through one object.

Ignite has one subsystem per mechanism: drivetrain, intake, indexer, shooter, vision, UI, PDH.

### Commands

A command is something a subsystem can *do*. It has four parts:

| Method | When it runs |
|---|---|
| `initialize()` | Once, when scheduled |
| `execute()` | Every 20ms while running |
| `isFinished()` | Every 20ms; return `true` to stop |
| `end(boolean interrupted)` | Once, when finished or interrupted |

`end` receives whether it was interrupted rather than finishing naturally. That matters — an interrupted shooter should stop its flywheel; a completed one might not need to.

### Two ways to write a command — and you need both

**Idiom 1: an explicit `Command` subclass.**

```java
public class DriveDistance extends Command {
  private final Drivetrain drive;
  private final double target;

  public DriveDistance(Drivetrain drive, double target) {
    this.drive = drive;
    this.target = target;
    addRequirements(drive);
  }

  @Override public void initialize() { drive.resetEncoders(); }
  @Override public void execute()    { drive.arcadeDrive(0.5, 0); }
  @Override public boolean isFinished() { return drive.getDistance() >= target; }
  @Override public void end(boolean interrupted) { drive.stop(); }
}
```

**Idiom 2: a factory method on the subsystem.**

```java
public Command stopRollerNoPID() {
  return runOnce(() -> rollerLeader.set(0)).withName("Stop Roller No PID");
}

public Command extendCommand() {
  return runOnce(() -> setExtension(IntakeConstants.EXTENDED))
      .andThen(Commands.waitUntil(() -> aboveRollerSetpoint()));
}
```

Both examples above are real — the second pair is Ignite's actual `IntakeSubsystem`.

**Why you must learn both.** Ignite's onboarding repo (`2025-Robotics-Intro`) uses idiom 1 throughout. Ignite's actual competition code (`Robot-2026`) uses idiom 2 almost exclusively. Learn one and half the codebase is unreadable.

**Which to use when.** WPILib's own guidance: prefer factory methods on the subsystem for single-subsystem commands, use factory methods in a separate class for multi-subsystem commands, and reserve full subclasses for commands with genuinely complex internal state. WPILib is explicit that "the best organization system is a perennial topic of debate" — this is a convention, not a law.

**Note `.withName()`.** Ignite names its commands. That name shows up in Glass, Shuffleboard, and AdvantageScope when you are trying to work out what the robot thought it was doing. It costs nothing and it saves real debugging time. Do it.

### Command composition

Commands combine into bigger commands.

```java
Commands.sequence(a, b, c)      // one after another
Commands.parallel(a, b)         // together, ends when all end
Commands.race(a, b)             // together, ends when the first ends
Commands.waitUntil(condition)   // waits for a condition
Commands.waitSeconds(2.0)
```

And decorators, which read more naturally chained:

```java
intake.collectCommand()
    .withTimeout(3.0)                    // give up after 3 seconds
    .andThen(intake.stowCommand())       // then stow
    .until(() -> hasGamePiece())         // or stop early if loaded
```

Real example from Ignite's `RobotContainer`:

```java
.onTrue(intake.stopRollerNoPID().andThen(intake.stowCommand()).withName("Stow Intake"));
```

**Composition is where the framework pays off.** That intake sequence from earlier — extend, wait, spin, wait, stop, retract — is a `sequence` of small commands, and the scheduler handles all the timing.

### Triggers and bindings

A `Trigger` is a condition. Bind commands to it:

```java
driverJoystick.a().onTrue(intake.agitateCommand());
driverJoystick.x().onTrue(intake.stopRollerNoPID());
operatorJoystick.leftBumper().whileTrue(drivetrain.setXCommand());
```

| Binding | Behavior |
|---|---|
| `.onTrue(cmd)` | Schedule once when the condition becomes true |
| `.whileTrue(cmd)` | Run while true, interrupt when it goes false |
| `.onFalse(cmd)` | Schedule when the condition becomes false |
| `.toggleOnTrue(cmd)` | Start on first press, cancel on second |

**Triggers are not only buttons.** Any `BooleanSupplier` works — this is where M2's lambdas pay off:

```java
new Trigger(() -> indexer.hasGamePiece()).onTrue(rumbleController());
```

Ignite pairs `.whileTrue` with `.onFalse` frequently, to run something while held and clean up on release:

```java
.whileTrue(shooter.spinFlywheelPostCommand())
.onFalse(shooter.stopFlywheelCommand().andThen(shooter.stowHood()));
```

### The scheduler and requirements

Every 20ms the `CommandScheduler` runs every scheduled command's `execute()`, checks `isFinished()`, and cleans up the ones that are done.

**Requirements are the safety mechanism.** A command declares which subsystems it needs — via `addRequirements()` in a subclass, or automatically when you use `runOnce`/`run` on the subsystem itself.

**If a new command is scheduled that requires a subsystem already in use, the running command is interrupted** — its `end(true)` is called — and the new one starts. It does not queue. Both do not run.

This is what prevents two commands from fighting over one motor, and it is the thing the test-out challenge checks.

**Default commands.** A subsystem can have one, which runs whenever nothing else requires it. The drivetrain's default command is almost always the one reading the driver's sticks.

### Project structure

| File | Role |
|---|---|
| `Main.java` | Entry point. Never edit it |
| `Robot.java` | Thin. Must call `CommandScheduler.getInstance().run()` |
| `RobotContainer.java` | Declarative setup: subsystems, bindings, auto chooser |
| `subsystems/` | One package per mechanism |
| `commands/` | Multi-subsystem commands that do not belong to one subsystem |

**Subsystems are private fields in `RobotContainer`, not globals.** Commands get the subsystems they need passed to them explicitly. WPILib is firm about this: global subsystems make control flow untraceable.

Ignite splits constants per subsystem (`ShooterConstants`, `IntakeConstants`) rather than one global `Constants.java`, and adds `preferences/` for tunable values and `statemachines/` for shared robot state. That is a deliberate divergence from WPILib's default and it is reasonable — but it exists only in the code, written down nowhere. Now you know.

## Project

**Build a two-subsystem robot in simulation.**

Requirements:

1. Two subsystems — a simple drivetrain and one mechanism
2. **One command written as an explicit `Command` subclass**
3. **One command written as a subsystem factory method**
4. Both named with `.withName()`
5. A composition combining them — a sequence or a parallel with a timeout
6. All three bound to controller buttons: one `onTrue`, one `whileTrue`
7. A default command on the drivetrain that reads the sticks
8. Everything runs in the simulator

**Then demonstrate interruption.** Bind two commands that require the same subsystem to different buttons. Press one, then the other while the first runs. Watch the first one's `end(true)` fire. Be able to explain it.

**Optional hardware variant.** Deploy to last season's robot.

## Definition of done

- [ ] Robot runs in simulation; both commands execute from button presses
- [ ] You wrote one command each way and can articulate the tradeoff
- [ ] Both commands are named and the names appear in the simulator's dashboard
- [ ] You demonstrated interruption and can explain what the scheduler did
- [ ] Subsystems are private fields in `RobotContainer`, not globals or statics
- [ ] The drivetrain has a default command
- [ ] You can trace one full 20ms loop iteration out loud, start to finish
- [ ] You can open `Robot-2026/RobotContainer.java` and explain any binding a mentor points at

## Common failure modes

**Blocking inside a command.** `Thread.sleep()`, or a `while` loop waiting for a sensor. Symptom: the entire robot freezes. Use `isFinished()` or `Commands.waitUntil()`.

**Forgetting `addRequirements()`.** Two commands quietly fight over one motor and the mechanism jitters. If you use `runOnce`/`run` on the subsystem, requirements are automatic — this bites mainly in the subclass idiom.

**Doing work in the constructor instead of `initialize()`.** A command object is constructed once, at startup, but may be scheduled hundreds of times. Anything that must happen per-run goes in `initialize()`. This produces baffling "it only works the first time" bugs.

**Scheduling a command from inside `execute()`.** Almost always wrong. Use composition instead.

**Reading a joystick value once.** Storing `joystick.getLeftY()` in a field at construction. This is M2's supplier lesson arriving in real form — the value freezes at startup. Pass a `DoubleSupplier`.

**One giant subsystem.** Putting the intake and indexer in one class because they work together. Then nothing can use them independently. Split by hardware ownership, coordinate with compositions.

**`RobotContainer` sprawl.** It grows fast. Split into `configureBindings()`, `configureDefaultCommands()`, `configureAutos()`.

## AI posture — 🟡 Yellow

First Yellow milestone. Read `ai-use-standard.md` §4 before using anything.

**Allowed:** Javadoc on code you wrote · explaining unfamiliar WPILib API · stack trace triage · repetitive boilerplate (the third near-identical `stopX()` method) · asking it to review code you already wrote

**Not allowed:** deciding what your subsystems are · writing your command logic · structuring your compositions

**Why the line is there.** Subsystem decomposition is the engineering decision this milestone teaches. An AI will happily produce a plausible five-subsystem design, and you will have no idea whether it is right, because working out whether it is right is the skill.

Disclosure is now required on PRs. See `ai-use-standard.md` §6.

## Resources

See `resources.md` for the full library. For this milestone:

- **WPILib: What Is Command-Based Programming?** — read the "why" before the mechanics
- **WPILib: Commands, Subsystems, Command Compositions, Binding Commands to Triggers, The Command Scheduler** — the core sequence
- **WPILib: Structuring a Command-Based Robot Project** — file layout and the private-fields rule
- **WPILib: Organizing Command-Based Projects** — factory methods vs. subclasses, and the named anti-patterns
- **Team 8744 Lesson02** — the clearest rookie-facing explanation of *why* command-based is shaped this way
- **Ignite:** `Robot-2026/.../RobotContainer.java` — the real thing. Read it before and after this milestone
- **Ignite:** `Robot-2026/.../subsystems/intake/IntakeSubsystem.java` — factory methods in production
- **Ignite:** `2025-Robotics-Intro/.../commands/DriveDistance.java` — the subclass idiom
