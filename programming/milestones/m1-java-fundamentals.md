# M1 — Java Fundamentals

## At a glance

Enough Java to program a robot. Not the Java language — the parts of it robot code actually uses.

This distinction is the whole design of this milestone. Java is enormous and most of it never appears in an FRC codebase. We teach the subset, get you to running robot code fast, and let the rest arrive when you need it.

**Estimated time:** 3–5 sessions. This should be measured in **hours, not weeks.**
**Prerequisites:** M0
**AI posture:** 🔴 Red

## Who enters here

**Track A** works this milestone in full.

**Track B** (has programmed, but not in Java or another OO language) attempts the test-out first. If you have written Python, Arduino C, or MATLAB, most of this will be familiar with different syntax — take the test-out, and if you pass, go straight to M2.

**Tracks C and D** test out.

## Test-out challenge

You are given a short Java method containing a deliberate bug — an off-by-one, an inverted comparison, or an integer-division truncation.

**Pass criteria:** find it, fix it, and explain what was wrong and why it produced the behavior it did. The explanation matters more than the fix; guessing correctly is not passing.

**Time:** 30 minutes.

## Concepts

### Scope, stated plainly

We teach: variables, the types robot code uses, operators, control flow, methods, arrays and lists, constants, and reading compiler errors.

We do not teach here: generics beyond using them, exception design, threading, streams, or the collections framework in depth. They come later or never.

Other teams make the same cut. Team 3550's Java Koans author described the goal as "not to teach the entirety of the Java language, but rather the fundamentals required to start learning how to program a FRC robot," designed so students "do not stay too long in pure, abstract, programming territories." Team 8744's 2026 lessons cover Java's eight primitive types and then narrow immediately to `int` and `double`, saying outright that the lesson skips around "to match our needs for robot programming."

**If this milestone takes more than about a week of sessions, something is wrong.** One instructor reported roughly four hours of logic and syntax before students were driving a Romi. Long fundamentals blocks lose the room, and the room is the point.

### Types

Java requires you to declare what kind of thing every variable holds.

```java
int count = 5;              // whole numbers
double speed = 0.75;        // decimals — most robot values
boolean isLoaded = true;    // true or false
String name = "shooter";    // text
```

There are eight primitive types. You will use `double` constantly, `int` regularly, `boolean` regularly, and the other five almost never.

**The trap that catches everyone.** Integer division truncates:

```java
int a = 1 / 2;        // 0, not 0.5
double b = 1 / 2;     // still 0.0 — the division happened before the assignment
double c = 1.0 / 2;   // 0.5 — correct
```

This causes real, hard-to-see robot bugs. A gear ratio computed as `int` silently becomes zero and your mechanism does not move.

### Operators

Arithmetic `+ - * / %`, comparison `== != < > <= >=`, logical `&& || !`.

**`=` assigns. `==` compares.** `if (x = 5)` does not compile in Java, which is one of the few places Java's strictness saves you outright.

**Never use `==` on `String`.** Use `.equals()`. `==` compares whether they are the same object, not whether they contain the same text.

**Never compare `double` with `==`.** Floating-point arithmetic is inexact — `0.1 + 0.2` is not exactly `0.3`. For robot code, check whether the difference is small enough:

```java
if (Math.abs(current - target) < TOLERANCE) { ... }
```

This is exactly how "is the shooter at speed?" gets answered in real code.

### Control flow

```java
if (condition) { ... } else if (other) { ... } else { ... }

switch (state) {
  case IDLE: ...; break;
  case SHOOTING: ...; break;
  default: ...;
}

for (int i = 0; i < 4; i++) { ... }
for (String name : names) { ... }
while (condition) { ... }
```

**A warning specific to robot code.** Robot code runs in a loop that must complete every 20 milliseconds. A `while` loop that waits for something to happen will hang the entire robot — no driver control, no safety systems, nothing. In robot code you almost never wait. You check whether the thing has happened, and if not, you return and check again next cycle.

That idea — *check, do not wait* — is the single most important mental shift between console programming and robot programming, and it is why command-based programming in M3 is shaped the way it is.

### Methods

```java
public double convertTicksToRotations(double ticks, double ticksPerRotation) {
  return ticks / ticksPerRotation;
}
```

`public` is visibility, `double` is what comes back, then the name, then parameters. `void` means it returns nothing.

Methods are how you avoid writing the same thing four times. If you have copy-pasted a block and changed one number, that block should be a method taking that number as a parameter.

### Constants

```java
public static final double MAX_SPEED = 4.5;
```

`final` means it cannot change. `static` means there is one shared copy. SCREAMING_SNAKE_CASE by convention.

**Why this matters more in robot code than anywhere else.** Magic numbers scattered through a subsystem are the single most common source of tuning pain in FRC. When the shooter gets a new gear ratio, you want to change one constant, not hunt for `6.75` in eleven places. Ignite splits constants per subsystem — `ShooterConstants.java`, `IntakeConstants.java` — rather than one global file.

### Reading compiler errors

Java's errors are wordy but honest. Read the first one only, fix it, recompile. Errors cascade; the fifteenth error is usually a consequence of the first.

The four you will meet constantly:

| Error | Usually means |
|---|---|
| `cannot find symbol` | Typo in a name, or you forgot an import |
| `incompatible types` | Assigning a `double` to an `int`, or similar |
| `missing return statement` | A path through your method returns nothing |
| `; expected` | Missing semicolon, usually on the line *above* the one reported |

## Project

**Robot-flavored exercises.** Each one is a computation that appears somewhere in real FRC code.

1. **Deadband.** Given a joystick value from -1.0 to 1.0, return 0 if its magnitude is below a threshold, otherwise return it unchanged. Real joysticks never rest at exactly zero.
2. **Tick conversion.** Convert encoder ticks to rotations given ticks-per-rotation. Then to RPM given a time interval. Watch for integer division.
3. **Gear ratio.** Given motor rotations and a gear ratio, compute mechanism rotations.
4. **At-setpoint check.** Given current and target values and a tolerance, return whether you are close enough.
5. **Clamp.** Constrain a value between a minimum and maximum. Used everywhere to keep a mechanism inside its safe range.
6. **Interpolate.** Given two known distance-to-speed pairs and a distance between them, estimate the speed. This is a simplified version of what `LaunchCalculator` does in Ignite's real shooter code.

Each exercise ships with assertions that must pass.

## Definition of done

- [ ] All six exercises compile and pass their assertions
- [ ] You can read a compiler error and find the offending line unaided
- [ ] You can explain why `1/2` is `0` in Java and how to fix it
- [ ] You can explain why `==` is wrong for comparing two `double` values
- [ ] You wrote at least one method that takes parameters and returns a value
- [ ] You can explain why a `while` loop that waits is dangerous in robot code

## Common failure modes

**Integer division.** The most common real bug at this stage, and it is silent — no error, just a wrong answer, usually zero.

**Comparing doubles with `==`.** Works in testing with clean numbers, fails on the robot with real sensor values. Always use a tolerance.

**Copy-paste instead of a method.** Four near-identical blocks with one number different. This scales into genuinely painful code by M4.

**Trying to learn all of Java.** If you are reading about interfaces, generics, or streams, you have gone past this milestone. Come back to M1's list.

**Getting stuck silently.** Fifteen minutes on the same error means ask someone. The Red AI tier is not asking you to suffer alone; it is asking you to ask a *human*.

**Off-by-one in loops.** `<` versus `<=`. Universal, permanent, affects everyone forever.

## AI posture — 🔴 Red

**Allowed:** "What does `static final` mean?" · "Explain what a return type is, differently" · "What does this error message mean?" · "Quiz me on control flow"

**Not allowed:** "Write me a method that clamps a value" · "Fix this code" · Inline autocomplete of anything beyond a variable name

**Why this milestone specifically.** Every exercise above is trivially generatable. That is exactly the problem — you would get six passing exercises and no ability to write the seventh. Research tracking novice programmers found that struggling students accepted AI suggestions at a substantially higher rate than students who were doing well, and that most of them believed they understood more than they did.

The struggle here is short. It is measured in hours. Take the hours.

## Resources

See `resources.md` for the full library. For this milestone:

- **FRC Java Koans (Team 3550)** — interactive Java scoped to exactly this subset, needs no prior experience
- **Team 8744 Lesson01_BasicJava** — the same cut, taught through an immediate robotics hook
- **Team 3255 FRC Java Tutorial** — a second path through the same material
- **Ignite:** `2025-Robotics-Intro/.../robotbase/calc/SensorProfile.java` — the real version of exercises 2 and 3, once you have written yours
