# M2 — OOP, Interfaces & Lambdas

## At a glance

Objects, interfaces, and lambdas — the three things you must understand before WPILib's API makes any sense at all.

The highest-leverage topic here is lambdas. WPILib's entire command and trigger system is built on them. A student who does not understand `() -> value` cannot read `RobotContainer.java`, and everything in M3 will feel like magic instead of code.

**Estimated time:** 3–4 sessions
**Prerequisites:** M1
**AI posture:** 🔴 Red

## Who enters here

**Tracks A and B** work this milestone.

**Tracks C and D** test out. If you are comfortable in Java, C++, or C#, the only genuinely new content is the specific way WPILib leans on functional interfaces — skim that section even if you test out.

## Test-out challenge

Two parts:

1. Given an interface and two implementing classes, add a third implementation and wire it into existing code
2. Rewrite one method to take a `DoubleSupplier` instead of a `double`, and explain the difference in *when the value is read*

**Pass criteria:** the code works, and the explanation of part 2 is correct. Part 2 is the real test — a student who describes a supplier as "a fancy double" has not got it.

**Time:** 45 minutes.

## Concepts

### Classes and objects

A class bundles state with the behavior that acts on it.

```java
public class Flywheel {
  private double targetRPM;          // state

  public void setTarget(double rpm) { // behavior
    targetRPM = rpm;
  }

  public double getTarget() {
    return targetRPM;
  }
}
```

A class is the blueprint. An object is one built from it. `new Flywheel()` builds one.

**Why this matters on a robot.** A robot has two flywheels, four swerve modules, three cameras. Each needs its own state — its own target, its own encoder, its own current position. Writing one class and making four objects is the difference between manageable code and sixteen copy-pasted variables named `flPosition`, `frPosition`, and so on.

### Fields, constructors, and encapsulation

```java
public class Flywheel {
  private final TalonFX motor;
  private double targetRPM = 0;

  public Flywheel(int canId) {
    motor = new TalonFX(canId);
  }
}
```

The constructor runs once when the object is created. It is where hardware gets wired up.

**`private` versus `public`.** `private` means only this class can touch it. Make fields private by default and expose behavior through methods.

This feels like pointless ceremony when you are working alone. It stops feeling that way the first time someone sets a motor from three different files and you have to find out which one is fighting the others. Private fields mean there is exactly one place a value can change, which means there is exactly one place to look.

### Inheritance, and when not to use it

```java
public class ShooterSubsystem extends SubsystemBase { ... }
```

`extends` means "is a kind of" and inherits everything from the parent. You will use this constantly for one specific purpose — every WPILib subsystem extends `SubsystemBase` — and rarely for anything else.

**When not to use it.** Inheritance is often the wrong tool. If you are reaching for it to share a couple of methods between unrelated classes, use composition instead: give each class a field holding an object that does the shared work. Deep inheritance hierarchies are hard to follow and hard to change.

The realistic guidance for FRC: extend WPILib base classes when the framework tells you to, and be suspicious of any inheritance you invent yourself.

### Interfaces

An interface is a contract — a list of methods, with no implementation.

```java
public interface LaunchRequestBuilder {
  LaunchRequest build(double distance);
}
```

Any class can implement it, and calling code does not care which one it gets. Ignite's real shooter code does exactly this: `ParabolicLaunchRequestBuilder` computes a launch from physics, `MappedLaunchRequestBuilder` looks it up from a tuned table, and both satisfy the same interface. The shooter can use either without knowing the difference.

**That is the point of interfaces.** They let you swap implementations — a physics model for a lookup table, real hardware for a simulator — without touching the code that uses them.

### The singleton pattern

You will hit this on day one of reading Ignite's code, so it is taught here rather than left as a surprise.

```java
public class LaunchState {
  private static LaunchState instance = null;

  private LaunchState() {}

  public static synchronized LaunchState getInstance() {
    if (instance == null) instance = new LaunchState();
    return instance;
  }
}
```

The constructor is private, so nobody can make one with `new`. Everyone calls `getInstance()` and gets the same shared object.

**What it guarantees:** exactly one instance exists, and everyone sees the same state.

**Why Ignite uses it for state machines:** the drivetrain, the shooter, and the UI all need to agree on what the robot is currently trying to do. One shared object makes that automatic.

**The honest caveat:** singletons are shared global state, and shared global state makes code harder to test and harder to reason about. WPILib deliberately pushes the opposite pattern for subsystems — private fields in `RobotContainer`, explicitly passed to the commands that need them. Ignite uses singletons for state machines and dependency injection for subsystems. Know both, and know that the subsystem pattern is the one WPILib recommends.

### Lambdas and functional interfaces

**The most important section in this milestone.**

A lambda is a small anonymous function you can pass around like a value.

```java
() -> 5.0                          // takes nothing, returns 5.0
() -> joystick.getLeftY()          // takes nothing, returns the current stick value
(x) -> x * 2                       // takes x, returns double it
() -> motor.stop()                 // takes nothing, returns nothing, does something
```

The types WPILib uses constantly:

| Type | Shape | Means |
|---|---|---|
| `DoubleSupplier` | `() -> someDouble` | "ask me for a double whenever you want" |
| `BooleanSupplier` | `() -> someBoolean` | "ask me for a boolean whenever you want" |
| `Runnable` | `() -> doSomething()` | "here is an action to run later" |

**Here is the idea that unlocks WPILib.** Consider the difference:

```java
double speed = joystick.getLeftY();        // reads the stick ONCE, right now
DoubleSupplier speed = () -> joystick.getLeftY();  // a way to read it, ANY time
```

The first captures a number at one instant. It will be stale forever after.

The second captures *the ability to get the number*. Whoever holds it can ask again every 20ms and get a fresh value.

Robot code is built almost entirely on the second thing. When you write a drive command, you are not handing it the joystick's current position — you are handing it a way to read the joystick, which it will use fifty times a second for as long as the command runs.

Once that clicks, `RobotContainer` stops looking like magic. Look at Ignite's real code and you will see `DoubleSupplier` parameters everywhere for exactly this reason.

## Project

**Build a unit-conversion class with tests.**

Write a `SensorProfile` class that converts between encoder ticks, mechanism rotations, and RPM, given a configurable ticks-per-rotation and gear ratio.

Requirements:
- Constructor takes ticks-per-rotation and gear ratio
- Fields are `private final`
- Methods: ticks to rotations, rotations to ticks, ticks-per-100ms to RPM, and back
- At least four JUnit tests, including one that would catch an integer-division bug
- One method takes a `DoubleSupplier` rather than a `double`, and you can explain why that would be useful

**Then read real Ignite code.** Open `Robot-2026/src/main/java/frc/robot/preferences/`. Five files, and between them they use every concept in this milestone:

```java
public abstract class RobotPreference<T> implements Supplier<T> {
```

Work through it in this order. Find the four concrete subclasses. Work out what the `<T>` buys you — why is this one generic class and not four unrelated ones? Then answer the question that matters: because `RobotPreference<T>` implements `Supplier<T>`, a tunable preference can be handed to a command **the same way a joystick axis can**. That is the supplier idea from the section above, in production code, and it is why M3 will make sense.

Compare it to what you just built. Your conversion class holds a value; a preference supplies one on demand. Being able to say why that difference matters is the exercise.

## Definition of done

- [ ] Conversion class has at least four passing JUnit tests
- [ ] All fields are private
- [ ] You can explain the difference between passing a value and passing a supplier
- [ ] You can read `Robot-2026`'s `LaunchState.getInstance()` and explain what a singleton guarantees
- [ ] You can explain why the shooter's two launch-request builders both implement one interface
- [ ] You wrote at least one lambda and can say what its type is
- [ ] You read `Robot-2026/.../preferences/` and can explain why `RobotPreference<T>` implements `Supplier<T>`

## Common failure modes

**Treating a supplier as a value.** The single most common misunderstanding at this stage, and it produces robot code where the joystick appears frozen at whatever it read at startup. If you find yourself writing `.getAsDouble()` immediately after receiving a supplier, stop and ask why you were given a supplier.

**Making everything public.** Feels easier. Makes it impossible later to find where a value is being changed.

**Reaching for inheritance.** If you invented a base class to share two methods, consider whether a field holding a helper object would be simpler.

**Singleton overuse.** They are convenient and they are global state. Ignite uses them for state machines specifically. Do not make your subsystems singletons — WPILib's pattern is private fields in `RobotContainer`, and M3 depends on that.

**Skipping the tests.** The tests are where you find out your conversion is off by a gear ratio. Four tests is not a formality here; it is the fastest path to knowing your class is right.

## AI posture — 🔴 Red

**Allowed:** "Explain what an interface is with a different example" · "What does `synchronized` do?" · "Why would a constructor be private?"

**Not allowed:** generating the `SensorProfile` class, generating the tests, or inline-completing lambdas.

**Lambdas specifically.** They are terse enough that an AI produces them instantly and correctly, and they are conceptually the hardest thing in this milestone. That combination is the worst case: you get working code and no understanding, and you find out in M3 when nothing makes sense.

This is the last Red milestone. M3 moves to Yellow.

## Resources

See `resources.md` for the full library. For this milestone:

- **Ignite:** `Robot-2026/.../preferences/` — an abstract generic class, four subclasses, and `Supplier<T>` doing real work. The single best file set in the codebase for this milestone
- **Ignite:** `Robot-2026/.../statemachines/LaunchState.java` — the singleton you will meet on day one
- **Ignite:** `Robot-2026/.../subsystems/shooter/LaunchRequestBuilder.java` and its two implementations — interfaces doing real work
- **Ignite:** `Robot-2026/.../RobotContainer.java` — read it once now. Most of it will not make sense yet. Find the lambdas
