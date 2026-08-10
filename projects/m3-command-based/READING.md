# Reading — `Phoenix-2026`, end to end

Twenty-two Java files. WPILib 2026, the same Phoenix 6 swerve stack the
competition robot runs, and small enough that you can actually finish it in a
sitting.

Do this **before** you start building. You are about to write both command
idioms; this repository contains one of each, written by people on your team,
running on real hardware.

---

## Get it

This is a read. You are not forking anything and you are not changing anything —
M3 is a **clone** on the [real-repo ladder](../../docs/programming/robot-repo-ladder.md).

```bash
cd ~/workspace/ignite            # or wherever you keep repositories
git clone https://github.com/IgniteRobotics/Phoenix-2026.git
cd Phoenix-2026
git checkout a4273829a8f64fda90c559e263b0f79164d94bb9
```

That pins you to the exact commit this assignment was written against, so the
file list matches. Git will say you are in "detached HEAD" state. That is fine —
you are looking at a photograph of the repository, not standing on a branch.

In a browser instead:
<https://github.com/IgniteRobotics/Phoenix-2026/tree/a4273829a8f64fda90c559e263b0f79164d94bb9>

---

## Read in this order

1. **`Main.java`**, then **`Robot.java`** — start where the program starts. Find
   the line that runs the scheduler.
2. **`RobotContainer.java`** — the subsystems, the bindings, the default
   command. This is the file you are about to write your own version of.
3. **`subsystems/drive/DrivetrainSubsystem.java`** — the factory idiom. Find a
   method that returns a `Command`.
4. **`commands/DriveToPose.java`** — the subclass idiom. All four methods, in
   one place.
5. **Everything else**, quickly. `generated/` is machine-written by CTRE's Tuner
   and is not worth reading closely. `preferences/` you already met in M2.

---

## Questions to answer

Write your answers down. A mentor will ask, and "I read it" is not an answer to
any of them.

1. `Robot.robotPeriodic()` calls one method that makes the entire command
   framework work. Name it, and say what happens to every command you write if
   that line is deleted.

2. `DriveToPose` grabs its PID controllers in `initialize()` rather than in the
   constructor. Work out why. The sharp version: a command object is built once
   at startup but scheduled hundreds of times — what breaks if per-run setup
   happens in the constructor?

3. **The one that matters.** Find a factory method on `DrivetrainSubsystem` and
   find `DriveToPose`. Both produce a `Command` that ends up requiring the
   drivetrain, but each declares that requirement in a different place in the
   file. Find the exact line in each — file name and line number for both.

4. Pick one binding in `RobotContainer` and say, in one sentence each: what
   condition triggers it, what command runs, and what happens if the driver lets
   go.

5. `DriveToPose.end(boolean interrupted)` stops the drivetrain whether it was
   interrupted or not. Say what would happen on the real robot if it only did
   that when `interrupted` was false.

---

## Then, and only then

Open `Robot-2026`'s container — the full-complexity one, twice the size, and
the repository you will eventually contribute to:

<https://github.com/IgniteRobotics/Robot-2026/blob/28ce5397d88275001d656dbb717d22cf9ac0150d/src/main/java/frc/robot/RobotContainer.java>

You are not expected to follow all of it yet. You **are** expected to be able to
pick any single binding out of it and say what it does. That is the actual bar
for contributing during a season, and it is what M3 is getting you to.

One thing to notice while you are in there: there is not one `extends Command`
in the whole repository — `Robot-2026` uses the factory idiom exclusively, and
`Phoenix-2026` does not. That is why you are writing both.
