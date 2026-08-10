# Task 3: The student-facing documents and the seeded simulator config

The project README is what a student reads first and the only place the harness gets explained; `READING.md` is the `Phoenix-2026` assignment, which the milestone puts *before* the build. The simulator config is the one artifact in this plan that cannot be authored by hand — Step 4 requires a person at a screen, and an agent must stop there rather than guess at a JSON schema whose only failure mode is being silently ignored.

**Files:**
- Create: `projects/m3-command-based/README.md`
- Create: `projects/m3-command-based/READING.md`
- Create: `projects/m3-command-based/simgui.json` (produced by the simulator, not typed)
- Modify: `projects/m3-command-based/.gitignore` (only if it ignores `simgui.json`)

**Consumes:** Task 1's project layout and the exact member names of `frc.robot.harness.M3Deliverables` — `drivetrain()`, `mechanism()`, `subclassCommand()`, `factoryCommand()`, `composition()` — and the three TODO regions in `RobotContainer` (subsystems, bindings, default commands). The README's numbered steps must match those names exactly; the fixture's failure messages point at "README.md step N".

**Produces:** nothing other tasks consume.

**Lane:** `milestone`. Work in `.worktrees/m3`, after Task 1 is committed. **Step 4 needs a human** — an agent that reaches it should complete Steps 5–7 for the documents, then report that `simgui.json` is outstanding and hand back.

---

- [ ] **Step 1: Check whether the wrapper's `.gitignore` excludes the sim config**

```bash
cd /Users/y0shi/workspace/ignite/docs
grep -n 'simgui\|networktables' projects/m3-command-based/.gitignore
```

Expected at the time of writing: two hits, `networktables.json` and `simgui-window.json` — and **not** `simgui.json`, so there is nothing to change and this step ends here. Check anyway rather than skipping: the file is copied from M0, M0's came from WPILib, and a version bump that starts ignoring `simgui.json` would silently drop the artifact §9.4 requires.

If `simgui.json` *is* matched, add an un-ignore line immediately below the matching rule and nothing else:

```gitignore
# M3 ships a seeded simgui.json so the simulator opens with a joystick already
# mapped (teaching-repo-spec.md §9.4). The per-window layout stays ignored.
!simgui.json
```

Do not delete the original rule and do not touch any other line — the file is copied byte-for-byte from M0 and any other divergence between the projects should be deliberate.

- [ ] **Step 2: Write `README.md`**

`projects/m3-command-based/README.md`:

````markdown
# M3 — Command-Based Programming

The framework every line of Ignite's robot code is written in. By the end of this
you will have built a two-subsystem robot that runs in the simulator, and you
will be able to read `Robot-2026/RobotContainer.java` and say what it does.

This is the biggest milestone in the curriculum. Five to seven sessions is
normal. Nothing here is a race.

---

## What you are given, and what is yours

Everything that is not the lesson is already here:

| Given | Why |
|---|---|
| `Main.java` | Never edit it. Every WPILib project has one and they are all identical |
| `Robot.java` | The scheduler call and the logging setup. Read `robotPeriodic()` — you owe a mentor a walkthrough of it |
| `Constants.java` | The shape. You add a nested class per subsystem |
| `simgui.json` | The simulator opens with a joystick already mapped to port 0 |
| `harness/M3Deliverables.java` | How the tests reach your work. Read it before you start |
| `src/test/` | The given tests. Do not edit them |

Yours: **the subsystems, the commands, the composition, the bindings, and the
default command.** That is the whole milestone, and it is all inside
`RobotContainer.java` and the files you create alongside it.

---

## Read first, build second

Do [`READING.md`](READING.md) before you write anything. It is `Phoenix-2026`,
end to end — 22 files, the same toolchain you are about to use, and both of the
command idioms you are about to write. Reading a small working robot before
building one is the fastest route through this milestone, and skipping it is the
slowest.

---

## The order

**1. Run the tests, before you change anything.**

```bash
./gradlew test
```

Everything fails. That is correct and expected — see "What red means here"
below.

**2. Write your two subsystems.**

A drivetrain and one other mechanism, in a `subsystems/` package. Keep them
plain: `PWMSparkMax` motors and, for the drivetrain, `DifferentialDrive`. There
is no real hardware and there does not need to be.

They go in `RobotContainer` as **private final fields** — not static, not
public, not reachable from anywhere else. Then return them from `drivetrain()`
and `mechanism()`.

**3. Write one command as an explicit `Command` subclass.**

`initialize`, `execute`, `isFinished`, `end(boolean)`. Call
`addRequirements(...)` in the constructor — if you forget, two commands will
quietly fight over the same motor, and one of the given tests exists purely to
catch that.

Give it a name with `.withName("Something Readable")` and return a **new one**
from `subclassCommand()` each time it is called.

**4. Write one command as a factory method on a subsystem.**

A method on the subsystem that returns a `Command`, built with `run(...)` or
`runOnce(...)`. Those give you the requirement for free. Name it, and return a
new one from `factoryCommand()`.

**5. Compose them, and bind all three.**

A sequence, or a parallel with a timeout. It has to end up requiring both
subsystems — which it will, if it is built out of commands that use both.
Return a new one from `composition()`.

Then bind them in `configureBindings()`: one with `onTrue`, one with
`whileTrue`, and the composition on a third button.

**6. Give the drivetrain a default command.**

Whatever runs when no button is held — on a real robot, the one reading the
driver's sticks. `drivetrain.setDefaultCommand(...)` in
`configureDefaultCommands()`.

**7. Run it.**

```bash
./gradlew simulateJava
```

The joystick is already mapped. Press your buttons and watch the command names
appear in the scheduler widget. If the names say `InstantCommand`, you skipped
`.withName()`.

**8. Demonstrate interruption.**

Bind two commands that require the *same* subsystem to two different buttons.
Press one, then press the other while the first is still running. Watch the
first one's `end(true)` fire.

Then be able to explain what the scheduler did. This is the thing M3 is
actually for, and a mentor will ask.

---

## Why there is an `M3Deliverables` interface

The given tests have to reach classes you have not written yet, and they cannot
guess what you called them. So `RobotContainer` implements a small interface
with five methods, and the tests go through those.

One rule, and it is the one that trips people:

> **Every command method returns a new instance each time it is called.**

Not a stored field. WPILib refuses to schedule a command that is part of a
composition, and your `composition()` is built out of the other two — so if you
hand back the same object every time, it cannot be scheduled on its own, and
neither the tests nor your robot will work. Build the command inside the method.

---

## What red means here

`./gradlew test` failing is the normal state of this project until you finish
it. The failures are the specification: each one names the method it wanted and
what was wrong with what it got.

What is *not* normal is a failure that mentions `UnsatisfiedLinkError`, or a
compile error before any test runs. That is a toolchain problem, not your code.
Show a mentor rather than trying to fix it.

**Do not edit anything under `src/test/` or in `harness/`.** They are part of
the milestone and CI checks that they have not changed. If you think one is
wrong, say so in your pull request — you might be right, and a mentor will look.

---

## Done when

- [ ] `./gradlew test` is green
- [ ] The robot runs in the simulator and both commands execute from button presses
- [ ] Both command names show up in the simulator's scheduler widget
- [ ] You wrote one command each way and can say why you would pick each
- [ ] Your subsystems are private fields in `RobotContainer`
- [ ] The drivetrain has a default command
- [ ] You demonstrated interruption and can explain what the scheduler did
- [ ] You can trace one full 20ms loop out loud, start to finish
- [ ] You have read `Phoenix-2026` end to end and can point at where each idiom appears
- [ ] You can open `Robot-2026/RobotContainer.java` and explain any binding a mentor points at

The last four are mentor-verified. No test can check them, and they are half the
milestone.
````

- [ ] **Step 3: Write `READING.md`**

`projects/m3-command-based/READING.md`:

````markdown
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
   find `DriveToPose`. Both produce a `Command`. Both end up requiring the
   drivetrain — but they say so in different ways. Find both ways.

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
````

- [ ] **Step 4: Seed `simgui.json` — human required**

The simulator writes this file itself on exit. Hand-writing it means guessing at a schema whose failure mode is being silently ignored, which would ship a "seeded" config that seeds nothing.

At a machine with a display:

```bash
cd /Users/y0shi/workspace/ignite/docs/projects/m3-command-based
export JAVA_HOME=~/wpilib/2026/jdk
./gradlew simulateJava
```

In the sim GUI:

1. **DS → Keyboard 0 Settings** — leave the defaults; this is the keyboard-backed joystick.
2. Drag **Keyboard 0** from the *System Joysticks* list onto **Joystick[0]** in the *Joysticks* window. Port 0 is what `Constants.OperatorConstants.DRIVER_CONTROLLER_PORT` uses.
3. Make sure the **Joysticks** and **Robot State** windows are visible.
4. Quit the simulator from its window — not with `Ctrl-C`, which can exit before the config is written.

Then confirm the file exists, is valid JSON, and actually records the mapping:

```bash
cd /Users/y0shi/workspace/ignite/docs/projects/m3-command-based
python3 -m json.tool simgui.json | head -40
git status --short simgui.json
```

The output must contain a joystick section naming port 0 — read it rather than assuming. Relaunch `./gradlew simulateJava` once more and confirm Joystick[0] comes up already populated; that round trip is the only real proof the seeding worked. Quit again and check `git diff simgui.json` is empty or trivially reordered.

`simgui-window.json` stays ignored — it is per-machine window geometry and pinning it would fight every student's monitor.

- [ ] **Step 5: Check the documents against the code they describe**

The README's steps are referenced by name in the fixture's failure messages, and a wrong path in `READING.md` costs a session.

```bash
cd /Users/y0shi/workspace/ignite/docs/projects/m3-command-based
grep -o 'drivetrain()\|mechanism()\|subclassCommand()\|factoryCommand()\|composition()' README.md | sort | uniq -c
grep -n 'setDefaultCommand\|configureBindings\|configureDefaultCommands' README.md
```

All five interface members must appear in the README, spelled exactly as in `src/main/java/frc/robot/harness/M3Deliverables.java`. Diff the two lists by eye rather than trusting the counts.

Then check every path and link:

```bash
cd /Users/y0shi/workspace/ignite/docs
ls docs/programming/robot-repo-ladder.md          # READING.md's relative link target
git -C ~/workspace/ignite/Phoenix-2026 cat-file -t a4273829a8f64fda90c559e263b0f79164d94bb9
git -C ~/workspace/ignite/Robot-2026  cat-file -t 28ce5397d88275001d656dbb717d22cf9ac0150d
git -C ~/workspace/ignite/Phoenix-2026 ls-tree --name-only \
    a4273829a8f64fda90c559e263b0f79164d94bb9 -r -- src/main/java/frc/robot
```

Both `cat-file -t` must print `commit`. The `ls-tree` output must contain `commands/DriveToPose.java`, `subsystems/drive/DrivetrainSubsystem.java`, `RobotContainer.java`, `Robot.java`, and `Main.java` — every file `READING.md` sends a student to. If any is missing or renamed at that SHA, fix the reading list; do not move the pin.

`READING.md`'s relative link resolves from `projects/m3-command-based/` → `../../docs/programming/robot-repo-ladder.md`, matching what M2's `READING.md` already does. These files are outside `docs_dir` and are never published, so MkDocs never resolves them — a broken link here fails a student, not a build.

- [ ] **Step 6: Check nothing in the documents gives the answer**

```bash
cd /Users/y0shi/workspace/ignite/docs/projects/m3-command-based
grep -niE 'arcadeDrive|new PWMSparkMax|extends SubsystemBase|Commands\.(sequence|parallel)\(' README.md READING.md
```

Naming the *types* a student will use (`PWMSparkMax`, `DifferentialDrive`) is scaffolding — L1 gives them the plant and keeps the lesson. Handing them a line of working code is not. Read every hit and delete any that is a copyable implementation rather than a pointer.

- [ ] **Step 7: Commit**

```bash
cd /Users/y0shi/workspace/ignite/docs
git status --short projects/m3-command-based
git add projects/m3-command-based/README.md projects/m3-command-based/READING.md \
        projects/m3-command-based/simgui.json projects/m3-command-based/.gitignore
git commit -m "M3 student README, Phoenix-2026 reading assignment, seeded sim config"
```

There is no Java in this task, so no `spotlessApply` — but run `./gradlew spotlessCheck` once anyway to confirm the previous tasks' tree is still clean before the lane merges. If `simgui.json` is outstanding because Step 4 has not been done by a human, commit the two documents, say so explicitly in the task report, and leave the task open. Do not commit a hand-written `simgui.json`.
