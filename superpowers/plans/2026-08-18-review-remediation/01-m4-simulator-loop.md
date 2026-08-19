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

**Consumes:** decision **D-1** from `00-plan.md`. Do not start without it.
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

- [ ] **Step 2: Implement the chosen D-1 option.**
      Under **option A**, the shape is:
      - `FlywheelPlant.step()` / `ArmPlant.step()` take their input from the device's applied
        output (`simState.getMotorVoltage()` or equivalent) rather than a cached field, so
        *any* control request — the student's loop or a SysId sweep — reaches the physics
        through one path.
      - A given seam that suspends the student's loop while a characterization routine owns
        the mechanism. A `Subsystem` per mechanism with the SysId routine declaring it as a
        requirement is the idiomatic form and is what the real robot does.
      - `Robot.robotPeriodic()` runs `CommandScheduler.getInstance().run()` **before** the
        student's update calls, matching M5's `Robot.java`.
      - A `CommandXboxController` constructed in `RobotContainer`, with the SysId bindings
        left as a marked TODO region — the binding is the student's, the controller is not.
      - `FlywheelPlant.step()` publishes rotor position as well as velocity.

      Under **option B** or **C**, follow `00-plan.md`'s description instead and skip the
      scheduler/controller work only if the chosen option genuinely does not need it —
      `homingCommand()` still has to be runnable in the simulator either way.

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
      Report the actual numbers and the actual observed behaviour. "It works" is not a
      completed step.

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
