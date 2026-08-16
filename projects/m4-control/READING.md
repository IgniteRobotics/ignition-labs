# Reading — two files from `Robot-2026`

Not a whole-repository read this time — two files, the ones this milestone was designed
against, at the exact commit this project cites everywhere else.

---

## Get it

```bash
cd ~/workspace/ignite            # or wherever you keep repositories
git clone https://github.com/IgniteRobotics/Robot-2026.git
cd Robot-2026
git checkout 28ce5397d88275001d656dbb717d22cf9ac0150d
```

Detached HEAD is expected — you are looking at a photograph of the repository, not
standing on a branch.

In a browser instead:
<https://github.com/IgniteRobotics/Robot-2026/tree/28ce5397d88275001d656dbb717d22cf9ac0150d>

---

## `src/main/java/frc/robot/subsystems/shooter/ShooterSubsystem.java`

Read this first. It is the file `README.md`'s "why the control loop is written in Java,
not on the `TalonFX`" section is contrasting your project against.

Find:

1. The `VelocityVoltage` field and the `Slot0Configs` applied in the constructor. This is
   the onboard closed loop — gains configured once, a target commanded, the `TalonFX`
   itself closes the loop.
2. `m_sysIdRoutineFlywheel` — a real `SysIdRoutine`, wired the same shape as this
   project's `harness/SysIdWiring.java`, with a real ramp rate and a reduced step voltage.
   Say, in one sentence, why the step voltage was reduced from the default.
3. `periodic()` — where the velocity target actually gets sent to the motor. This runs
   every 20ms, the same cadence your own `updateFlywheel()` will run at, driven by a
   different mechanism (`CommandScheduler`'s subsystem periodic, versus `Robot.robotPeriodic()`
   calling your method directly).

## `src/main/java/frc/robot/subsystems/drive/DriveMotorConfigs.java`

Your stretch project, if you take it. Four methods — one per swerve corner — each
building a `Slot0Configs` by assigning the same five fields from four different sets of
named constants.

Find the exact lines that repeat across all four methods, and work out what a `.clone()`
of one base config, then per-corner field overrides, would look like. You do not have to
write it here — just be able to describe the refactor before you open `RobotContainer.java`
and start your own project.

---

## Questions to answer

A mentor will ask. "I read it" is not an answer to any of them.

1. `ShooterSubsystem.periodic()` commands `velocityControl.withVelocity(...)` every cycle,
   not just when the target changes. Why is that the right choice for a closed loop, and
   what would go wrong if it only ran once per `set` call?
2. The `SysIdRoutine.Config` reduces dynamic step voltage from its default. Say what a
   dynamic SysId test actually does to a mechanism, and why a full step voltage risked a
   brownout on this one.
3. Count the fields `DriveMotorConfigs` assigns per corner. Are any of them a term this
   milestone taught you the name of? Naming one is not answering the stretch project's
   question of *how* to collapse the duplication, but it tells you what you are looking at.

---

## Then, and only then

Start `RobotContainer.java`. `README.md` has the order.
