# Task 1: Make M4's SysId and homing runnable

M4's README tells a student to bind SysId routines to buttons, run `./gradlew simulateJava`,
and read kS/kV/kA out of the log. None of that can happen today. This task makes it true.
The risk is over-correcting: M4 is fade level L1, and everything added here has to be wiring
the student was never going to write.

**Files:**
- Modify: `projects/m4-control/src/main/java/frc/robot/Robot.java`
- Modify: `projects/m4-control/src/main/java/frc/robot/RobotContainer.java`
- Modify: `projects/m4-control/src/main/java/frc/robot/plant/FlywheelPlant.java`
- Modify: `projects/m4-control/src/main/java/frc/robot/plant/ArmPlant.java`
- Modify: `projects/m4-control/src/main/java/frc/robot/harness/SysIdWiring.java`
- Modify (if the seam needs it): `projects/m4-control/src/main/java/frc/robot/harness/M4Deliverables.java`
- Modify (if a CAN ID or step constant is needed): `projects/m4-control/src/main/java/frc/robot/Constants.java`
- Test: `projects/m4-control/src/test/**` — read, do not edit

**Consumes:** decision **D-1**, resolved 2026-08-18 as **option A** — route the device's
applied output into the plant. Read D-1 in `00-plan.md` for the reasoning and the risk it
accepts before starting.
**Produces:** a `projects/m4-control/` in which a student can run a SysId sweep in
`simulateJava` and watch a homing command execute. Task 2 documents whatever this produces.
**Lane:** `m4`

---

## The three defects, stated precisely

Verify each yourself before fixing it — the point is that these were believed working.

1. **The plants never see SysId's voltage.** `FlywheelPlant.step()` and `ArmPlant.step()`
   both call `sim.setInput(commandedVolts)`. `commandedVolts` is written only by
   `setVoltage()`. `SysIdWiring.forMotor()` drives through
   `motor.setControl(request.withOutput(volts))` on the `TalonFX`. Nothing connects them.

2. **The student's own loop overwrites the sweep.** `Robot.robotPeriodic()` calls
   `updateFlywheel()` and `updateArm()` unconditionally, every tick, with no way to suspend
   them.

3. **SysId routines cannot be scheduled.** `Robot.java` never calls
   `CommandScheduler.getInstance().run()` (M3's and M5's both do), and the project contains
   no `Subsystem` and no controller. `SysIdWiring.forMotor()` requires a `Subsystem` and
   returns Commands. The same gap means `homingCommand()` can never run in the simulator
   either.

Plus one asymmetry: `FlywheelPlant.step()` publishes `simState.setRotorVelocity(...)` but
never a rotor position; `ArmPlant.step()` publishes both. The flywheel `TalonFX`'s Position
signal is permanently zero.

---

- [ ] **Step 1: Reproduce all three, and record the output.**
      Do not take the list above on trust.
      ```bash
      export JAVA_HOME=~/wpilib/2026/jdk
      cd projects/m4-control
      grep -n "commandedVolts" src/main/java/frc/robot/plant/*.java
      grep -rn "CommandScheduler\|CommandXboxController\|Subsystem" src/main/java/
      ```
      Expected: `commandedVolts` written only in `setVoltage`; zero `CommandScheduler` and
      zero controller references under `src/main/`. Paste both outputs into the task report.

- [ ] **Step 2: Implement option A.** Five pieces:

      1. **One input path.** `FlywheelPlant.step()` and `ArmPlant.step()` take their input
         from `simState.getMotorVoltage()` instead of the cached `commandedVolts` field.
         Verified present on the pinned Phoenix 26.1.1:
         ```
         public final double getMotorVoltage();
         ```
         `setVoltage()` already calls `motor.setControl(voltageRequest.withOutput(...))`, so
         the device genuinely receives the student's command — the cached field is pure
         redundancy today and should go, not be kept in parallel. Two paths into the physics
         is how this broke in the first place.

      2. **Rotor position on the flywheel.** `FlywheelPlant.step()` publishes
         `setRotorVelocity(...)` but no position; `ArmPlant` publishes both. Add
         `setRawRotorPosition(...)` so the flywheel `TalonFX`'s Position signal is real —
         SysId's analysis and any student inspecting the device both need it.

      3. **Scheduler.** `Robot.robotPeriodic()` calls `CommandScheduler.getInstance().run()`
         **before** the student's `updateFlywheel()`/`updateArm()` calls, matching M5's
         `Robot.java`. Mandatory independently of SysId — `homingCommand()` is unreachable
         without it (finding C2).

      4. **A `Subsystem` per mechanism**, given, minimal. `SysIdRoutine.Mechanism` requires
         one, and requirements are what let a sweep take the mechanism away from the
         student's loop — which is the correct mechanism for suspending it, not a boolean
         flag. This is the M3 interruption lesson doing real work one milestone later; say so
         in a comment.

      5. **A `CommandXboxController`** constructed in `RobotContainer`, with the SysId
         bindings left as a marked TODO region. The controller is given; the binding is the
         student's.

      **Update the plants' class Javadoc in the same commit.** Both currently document the
      old behaviour explicitly — *"`step` feeds the physics from the voltage `setVoltage` was
      last called with — not from anything read back off the `TalonFX`… current limits,
      voltage compensation, ramp rates… are all inert here."* That paragraph becomes false the
      moment step 2.1 lands. Leaving it is worse than the original bug, because a student who
      reads it will disbelieve their own correct plot. Task 2 owns the matching README
      section.

- [ ] **Step 3: Hold the line on fade level.**
      Re-read `docs/programming/teaching-repo-spec.md` §8's L1 row, then read your own diff
      against it. Every added line must be wiring, not lesson. Specifically confirm the diff
      contains **no** feedforward term, **no** PID controller, **no** profile, **no** homing
      implementation, and **no** number that is or could be read as a gain — including in
      comments, Javadoc, and field names.
      ```bash
      git diff -- projects/m4-control/src/main | grep -inE "kS|kV|kA|kG|kP|kD|feedforward|PIDController|TrapezoidProfile"
      ```
      Expected: nothing but the words appearing in prose that *describes* what the student
      writes. Any match that is code is a failure of this step.

- [ ] **Step 4: Complete the fixture end to end, as a student would.**
      This is the step that would have caught the original defect and it is not optional.
      Write a throwaway reference implementation in a scratch directory outside the repo
      (**never commit it** — `CLAUDE.md`), then:
      1. Run a SysId sweep in `./gradlew simulateJava` and confirm the mechanism actually
         moves and the log contains a usable voltage/velocity trace.
      2. Feed that log to the WPILib SysId analyser and confirm it produces kS/kV/kA that
         are *plausible for the given plant constants* (`FlywheelPlant`: `J = 0.0021`,
         `GEARING = 1.5`, Kraken X60).
      3. Schedule `homingCommand()` in the simulator and watch it drive to the limit and
         zero.
      4. **Run the given suite against that reference implementation and confirm it is
         green.** This is the check D-1 explicitly accepts risk on: option A changes the
         plants' input path, and therefore the dynamics `FlywheelVelocityTest` and
         `ArmGravityTest` were tuned against. A suite no right answer passes is as broken as
         one every wrong answer passes.

      Report the actual numbers and the actual observed behaviour. "It works" is not a
      completed step.

      **If the given tests fail against a correct implementation**, re-tune the *test*
      thresholds to the new dynamics and say exactly what moved and why. One hard limit: if
      the fix requires **loosening** a tolerance, stop and escalate rather than committing.
      A looser gate means the new plant behaves worse than the old one, which would be
      evidence against option A itself — and quietly widening a bound until it passes is the
      precise failure this repository keeps producing.

- [ ] **Step 5: Confirm the clean-clone invariant still holds.**
      ```bash
      cd projects/m4-control
      ./gradlew assemble          # expect rc=0
      ./gradlew compileTestJava   # expect rc=0
      ./gradlew test              # expect rc=1, from assertions only
      ./gradlew spotlessCheck     # expect rc=0
      ```
      Then read the `test` output and confirm every failure is an assertion or a harness
      `fail(...)` message, not a compile error, a link error, or an NPE from new wiring.
      Paste the failure list.

- [ ] **Step 6: Read every failure message the new wiring can produce.**
      If step 2 added a seam a student can leave unimplemented, it must fail with a sentence
      that says what to do — matching the existing `SimDriver` messages
      (*"Construct a FlywheelPlant in your constructor and return it"*). Quote each new
      message in the report.

- [ ] **Step 7: Format and commit.**
      ```bash
      ./gradlew spotlessApply && ./gradlew spotlessCheck
      cd ../.. && git status --short
      ```
      `git status --short` must show no reference implementation and nothing outside
      `projects/m4-control/src/`. **Do not run `tools/tests_lock.py write`** — task 9 owns
      the manifest.
