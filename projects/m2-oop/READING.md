# Reading — `Robot-2026/src/main/java/frc/robot/preferences/`

Five files. Between them they use every concept in M2, and one of them is the
answer to why any of this matters.

Do this **after** `SensorProfile` passes. The reading lands differently once you
have built something that does nearly the same job a worse way.

---

## Get it

This is a read. You are not forking anything and you are not changing anything —
M2 is a **clone** on the [real-repo ladder](../../docs/programming/robot-repo-ladder.md).

```bash
cd ~/workspace/ignite            # or wherever you keep repositories
git clone https://github.com/IgniteRobotics/Robot-2026.git
cd Robot-2026
git checkout 28ce5397d88275001d656dbb717d22cf9ac0150d
```

That last command pins you to the exact commit this assignment was written
against, so the line numbers and the file list match. Git will tell you that you
are in "detached HEAD" state. That is fine and expected — you are looking at a
photograph of the repository, not standing on a branch.

In a browser instead:
<https://github.com/IgniteRobotics/Robot-2026/tree/28ce5397d88275001d656dbb717d22cf9ac0150d/src/main/java/frc/robot/preferences>

---

## Read in this order

1. **`RobotPreference.java`** — the abstract base. Twenty-odd lines.
2. **`DoublePreference.java`**, **`IntegerPreference.java`**,
   **`BooleanPreference.java`**, **`StringPreference.java`** — the four concrete
   subclasses.

---

## Questions to answer

Write your answers down. A mentor will ask you these, and "I read it" is not an
answer to any of them.

1. `RobotPreference` is declared `abstract`. What stops you writing
   `new RobotPreference("shooterRpm")`, and why would you not want to?

2. The class is `RobotPreference<T>`, not four unrelated classes. Work out what
   the `<T>` buys. Then answer the sharper version: what would you have had to
   write four times if it were not there?

3. **The one that matters.** The declaration is:

   ```java
   public abstract class RobotPreference<T> implements Supplier<T>
   ```

   You wrote `rotationsFrom` in this milestone — a method that hands back a way
   of getting a value rather than a value. `RobotPreference` is the same idea
   made into a class.

   So: a command needs a number that a driver can tune from the dashboard
   mid-competition. Explain how `implements Supplier<T>` means that command does
   not need to know the number came from a dashboard at all, and could just as
   easily have come from a joystick axis.

   That answer is why M3 will make sense.

4. Compare it to what you built. Your `SensorProfile` **holds** settings and
   answers questions about them. A `RobotPreference` **supplies** a value on
   demand. Say what changes about the code that uses each one.

---

## Two more files, while you are in there

- `src/main/java/frc/robot/statemachines/LaunchState.java` — the singleton. Find
  `getInstance()` and work out what stops anyone calling `new LaunchState()`.
- `src/main/java/frc/robot/subsystems/shooter/LaunchRequestBuilder.java` and its
  two implementations, `ParabolicLaunchRequestBuilder` and
  `MappedLaunchRequestBuilder`. One computes a launch from physics; the other
  looks it up from a tuned table. Answer: what can the shooter do because both
  satisfy the same interface, that it could not do otherwise?
