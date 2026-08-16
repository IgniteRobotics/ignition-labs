# Task 2: `FlywheelPlant` and `ArmPlant`

The measurement surface for the whole milestone, and the reason M4 exists as a spike-first
design rather than a documentation-first one. **Read
[`superpowers/specs/2026-08-16-m4-control-design.md`](../../specs/2026-08-16-m4-control-design.md)
before writing a line here** — §2's four experiments are why these classes read the
physics `sim` object directly instead of reading back through CTRE's status-signal
pipeline, and §4 is why they are built from `LinearSystemId.create*System(DCMotor, J,
gearing)` rather than from gains.

**The determinism proof in Step 6 is not optional.** The entire architecture — given
tests that step a plant 250 times and assert on where it ended up — rests on that step
loop being bit-identical across runs in one JVM. If it is not, stop and report rather than
loosening a tolerance to paper over it.

**Inversion and neutral mode are inert, same as current limits, voltage compensation, and
ramp rates.** Design record §3 was corrected on review to say this plainly (its earlier
draft claimed the opposite, and the code snippet directly above that claim already
contradicted it — physics is fed from `commandedVolts`, pre-inversion, never read back off
the `TalonFX`). Both plants' Javadoc below states the limitation as corrected.

**Arm parameters are pinned by design record §3.1, not chosen here.** `SingleJointedArmSim`
takes no mass — its gravity term assumes `J = ⅓mL²`, so `J` and the arm length `L` are not
independent, and picking them without deriving one from the other risks an unphysical arm.
An earlier `J = 0.62`, `G = 100` pairing held horizontal on `0.257 V`, cheap enough that
omitting `kG` entirely still lands within about 3° of target — the exact distractor Task
4's `ArmGravityTest` exists to catch would have silently passed. The parameters below —
`m = 8` kg, `L = 0.9` m, `G = 30`, `J = mL²/3 = 2.16` kg·m² — were checked against that
failure mode and hold horizontal on `1.99 V`; a kG-omitted arm misses by roughly `12°`,
comfortably past the test's tolerance. Keep `m = 8 kg` documented in the Javadoc even
though the constructor never takes it, so a future change to `L` gets `J` re-derived
rather than picked independently.

**The flywheel has a kP ceiling, and it belongs in this task file, not in student-facing
text.** With `J = 0.0021`, `G = 1.5`, design record §3.2 derives a discrete kP stability
ceiling of `1.496` — kP `1.0` peaks at `41.42` rot/s and settles clean, kP `2.0` diverges.
This number is a system-identification result (it falls straight out of kV/kA), so it must
never appear in `README.md`, a Javadoc, or a test — Task 5's README already only says
"too high oscillates," which is enough for a student tuning by feel to stay well clear of
it without ever being told the boundary. It matters here because Task 4 Step 5's reference
implementation and Task 8's solutions gains must both sit visibly inside the band, or
whoever is verifying will misread a diverging loop as a broken plant.

**Clamp in Java, call `setInput`, not `setInputVoltage`.** `FlywheelSim`/`SingleJointedArmSim`
inherit `setInputVoltage(double)` from `LinearSystemSim`, which is `setInput(volts)` followed
by `clampInput(RobotController.getBatteryVoltage())` — a HAL read. M4 does initialize the HAL
(`SimDriver`/`Robot` both bring it up), so this would probably work here, but it would make the
plant's behavior depend on simulated battery state the model otherwise ignores everywhere else —
exactly the kind of hidden coupling that makes Step 6's determinism proof stop reproducing later
if that state ever isn't a plain constant. `setVoltage` already clamps to ±12 into
`commandedVolts` before `step` ever runs, so `step` calls the HAL-free `sim.setInput(commandedVolts)`
directly. T4's plant uses the identical two-line pattern (clamp in Java, then `setInput`), pinned
in `00-plan.md`'s T4 section — both projects' plants read the same way.

**Files:**
- Create: `projects/m4-control/src/main/java/frc/robot/plant/FlywheelPlant.java`
- Create: `projects/m4-control/src/main/java/frc/robot/plant/ArmPlant.java`
- Create (temporary, deleted in Step 6): `projects/m4-control/src/test/java/frc/robot/plant/DeterminismSpikeTest.java`

**Consumes:** Task 1's buildable scaffold.
**Produces:** `frc.robot.plant.FlywheelPlant` and `frc.robot.plant.ArmPlant`, exactly as
pinned in `00-plan.md`'s API contract — every task after this one names them and takes
the signatures verbatim.

**Lane:** `milestone`. Worktree `.worktrees/m4`.

---

- [ ] **Step 1: Write `FlywheelPlant`**

The physical parameters below — `0.0021` kg·m² and a `1.5` gear reduction — are the exact
numbers the design record's §4 uses as its worked example of "a student who opens this
file finds a moment of inertia, not a kV." Keep them; they are cited by name in this
plan's self-review.

**Why the gearing matters, concretely, and is not just flavor:** `velocityRotationsPerSecond()`
reports *mechanism* rotations per second — what the student's control loop reads and
targets. The `TalonFX`'s own `getVelocity()` status signal, which is what a SysId run
logs, reports *rotor* rotations per second — `1.5×` faster, because the reduction sits
between the motor and the mechanism. A characterization run that logs rotor velocity and
a control loop that targets mechanism velocity are in different units, and the plant does
nothing to paper over that. That gap is real and is exactly what a "gear ratio applied
twice, or not at all" bug looks like in Task 4's distractor matrix.

Create `projects/m4-control/src/main/java/frc/robot/plant/FlywheelPlant.java`:

```java
package frc.robot.plant;

import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.sim.TalonFXSimState;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.system.plant.LinearSystemId;
import edu.wpi.first.wpilibj.simulation.FlywheelSim;

/**
 * A simulated single-motor flywheel, and the mechanism Part 1 of this milestone is about.
 *
 * <p>Built from a moment of inertia and a gear reduction — {@link LinearSystemId#createFlywheelSystem}
 * — never from a velocity-versus-voltage gain. Reading this file hands you a physical quantity to
 * reason about, not a kV to copy into your controller. You still have to characterize it.
 *
 * <p><b>What this simulates, and what it does not.</b> {@link #step} feeds the physics from the
 * voltage {@link #setVoltage} was last called with — not from anything read back off the {@code
 * TalonFX}. One consequence: current limits, voltage compensation, ramp rates, and any inversion
 * or neutral-mode configuration you apply through {@link #motor()} are all inert here. A student
 * who configures a 40A supply current limit and sees no change in the plot has not found a bug;
 * they have found the edge of what this model represents.
 *
 * <p>{@link #velocityRotationsPerSecond()} reads the mechanism's own velocity — not the {@code
 * TalonFX}'s rotor velocity, which runs faster by the gear reduction. Characterizing the motor
 * (through {@link #motor()} and SysId) and controlling the mechanism (through this method) are
 * two different units, on purpose.
 */
public final class FlywheelPlant implements AutoCloseable {

  private static final double J_KG_M2 = 0.0021;
  private static final double GEARING = 1.5;

  private final TalonFX motor;
  private final TalonFXSimState simState;
  private final FlywheelSim sim;
  private final VoltageOut voltageRequest = new VoltageOut(0.0);
  private double commandedVolts;

  public FlywheelPlant(int canId) {
    motor = new TalonFX(canId);
    simState = motor.getSimState();
    simState.setSupplyVoltage(12.0);
    sim =
        new FlywheelSim(
            LinearSystemId.createFlywheelSystem(DCMotor.getKrakenX60(1), J_KG_M2, GEARING),
            DCMotor.getKrakenX60(1));
  }

  /**
   * Commands a voltage. Clamped to the battery rail, and sent to the real {@code TalonFX} as a
   * {@link VoltageOut} — the device really receives it, even though {@link #step} does not read
   * it back.
   *
   * @param volts commanded voltage; positive spins the mechanism forward
   */
  public void setVoltage(double volts) {
    commandedVolts = MathUtil.clamp(volts, -12.0, 12.0);
    motor.setControl(voltageRequest.withOutput(commandedVolts));
  }

  /**
   * @return the mechanism's current velocity, rotations per second. This is the measurement your
   *     control loop reads and the measurement the given tests assert on.
   */
  public double velocityRotationsPerSecond() {
    return sim.getAngularVelocityRadPerSec() / (2.0 * Math.PI);
  }

  /**
   * Advances the physics by one timestep and republishes telemetry. {@code Robot.simulationPeriodic()}
   * calls this once per 20ms tick; given tests call it directly in a stepped loop.
   *
   * @param dtSeconds timestep, seconds
   */
  public void step(double dtSeconds) {
    sim.setInput(commandedVolts);
    sim.update(dtSeconds);
    simState.setRotorVelocity(velocityRotationsPerSecond() * GEARING);
  }

  /**
   * @return the underlying {@code TalonFX}, for configuration practice and for wiring SysId. No
   *     configuration you apply through it changes what {@link #step} simulates — see the class
   *     Javadoc.
   */
  public TalonFX motor() {
    return motor;
  }

  @Override
  public void close() {
    motor.close();
  }
}
```

- [ ] **Step 2: Write `ArmPlant`**

`MIN_ANGLE_RADIANS` is set to the exact same constant as `HOME_ANGLE_RADIANS` — that is
not a coincidence, it is the geometry: the reverse limit switch is physically mounted at
the arm's minimum travel, so "at the limit" and "at the home angle" are the same position.
What makes the raw encoder read something else at boot is `ENCODER_BOOT_OFFSET_RADIANS`,
an arbitrary miscalibration applied only to the *reading*, never to the physical
simulation. `zeroHere()` collapses that offset to zero the instant it is called at the
limit, which is also why a homing routine that calls it *without* ever reaching the limit
still makes `measuredPositionRadians()` read `HOME_ANGLE_RADIANS` — it rebases whatever
position you happen to be at. `atHomeLimit()` is what actually catches that: it reads the
physical simulated angle, never the offset encoder reading. Task 4's homing distractor
depends on this distinction; do not collapse the two.

`WPILib`'s `SingleJointedArmSim` clamps the simulated angle at `minAngleRads` /
`maxAngleRads` and zeroes velocity on contact — confirmed against the WPILib source this
plan was written against. That is what makes `atHomeLimit()`'s direct angle comparison
reliable: a student driving into the limit cannot punch through it in simulation.

**`J_KG_M2` is not a free choice — it is derived from `m` and `L`.** `SingleJointedArmSim`'s
gravity term is `α = -3/2 · g · cos(θ) / L`, which assumes `J = ⅓mL²`; mass cancels out of
the sim's own math, but a `J` that doesn't correspond to *some* physical mass at this length
is not modelling a real arm. Design record §3.1 picked `m = 8` kg and `L = 0.9` m for the
gravity-holding voltage they produce — `1.99 V`, large enough that a kG-omitted feedforward
misses the target by roughly 12°, comfortably past `ArmGravityTest`'s tolerance — and
`J = mL²/3 = 2.16` kg·m² follows from that. If `ARM_LENGTH_METERS` ever changes,
`J_KG_M2` has to be re-derived the same way, not picked independently.

Create `projects/m4-control/src/main/java/frc/robot/plant/ArmPlant.java`:

```java
package frc.robot.plant;

import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.sim.TalonFXSimState;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.system.plant.LinearSystemId;
import edu.wpi.first.wpilibj.simulation.SingleJointedArmSim;

/**
 * A simulated single-jointed arm with gravity, a reverse limit switch, and an encoder that boots
 * at an arbitrary offset. Part 2 of this milestone.
 *
 * <p>Built from a moment of inertia, an arm length, and a gear reduction — {@link
 * LinearSystemId#createSingleJointedArmSystem} — never from a gravity-feedforward or PID gain.
 *
 * <p>{@code SingleJointedArmSim} takes no mass; its gravity term assumes {@code J = m*L^2/3}, so
 * {@link #J_KG_M2} is not independent of {@link #ARM_LENGTH_METERS} even though the constructor
 * takes them as separate arguments. This plant models an 8kg arm at this length — that mass never
 * appears in code because the simulator has no parameter for it, but it is the number {@link
 * #J_KG_M2} was derived from, and it is the number to start from again if {@link
 * #ARM_LENGTH_METERS} ever changes.
 *
 * <p><b>What this simulates, and what it does not.</b> Same limitation as {@link FlywheelPlant}:
 * {@link #step} feeds physics from the voltage {@link #setVoltage} was last called with, not from
 * anything read back off the {@code TalonFX}. Current limits, voltage compensation, ramp rates,
 * and any inversion or neutral-mode configuration applied through {@link #motor()} are inert.
 *
 * <p><b>Homing exists because {@link #measuredPositionRadians()} boots wrong.</b> A relative
 * encoder reads zero — or here, an arbitrary offset — wherever the mechanism happens to be at
 * power-on. {@link #atHomeLimit()} reads the physical simulated angle directly, independent of
 * that offset; {@link #zeroHere()} is the only thing that corrects the offset, and it does so
 * unconditionally, wherever you call it from. Calling it without having reached the limit still
 * makes {@link #measuredPositionRadians()} read {@link #HOME_ANGLE_RADIANS} — it does not know
 * the difference between "I am home" and "I gave up." A given test in this milestone checks
 * {@link #atHomeLimit()} for exactly that reason.
 */
public final class ArmPlant implements AutoCloseable {

  /** Radians from horizontal that a homed arm reads when it is sitting on the reverse limit. */
  public static final double HOME_ANGLE_RADIANS = -0.5236;

  // This plant models an 8kg arm -- SingleJointedArmSim has no mass parameter, so that
  // number never appears below, but J_KG_M2 is derived from it (J = m*L^2/3, design record
  // §3.1) and is not a free choice independent of ARM_LENGTH_METERS.
  private static final double ARM_LENGTH_METERS = 0.9;
  private static final double J_KG_M2 = 2.16; // = 8 kg * 0.9 m^2 / 3
  private static final double GEARING = 30.0;
  private static final double MIN_ANGLE_RADIANS = HOME_ANGLE_RADIANS; // -30 degrees
  private static final double MAX_ANGLE_RADIANS = 1.5708; // 90 degrees
  private static final double STARTING_ANGLE_RADIANS = 0.2618; // 15 degrees; arbitrary
  private static final double ENCODER_BOOT_OFFSET_RADIANS = 1.0472; // 60 degrees; arbitrary
  private static final double LIMIT_SWITCH_TOLERANCE_RADIANS = 1.0e-3;

  private final TalonFX motor;
  private final TalonFXSimState simState;
  private final SingleJointedArmSim sim;
  private final VoltageOut voltageRequest = new VoltageOut(0.0);
  private double commandedVolts;
  private double encoderOffsetRadians = ENCODER_BOOT_OFFSET_RADIANS;

  public ArmPlant(int canId) {
    motor = new TalonFX(canId);
    simState = motor.getSimState();
    simState.setSupplyVoltage(12.0);
    sim =
        new SingleJointedArmSim(
            LinearSystemId.createSingleJointedArmSystem(DCMotor.getKrakenX60(1), J_KG_M2, GEARING),
            DCMotor.getKrakenX60(1),
            GEARING,
            ARM_LENGTH_METERS,
            MIN_ANGLE_RADIANS,
            MAX_ANGLE_RADIANS,
            true,
            STARTING_ANGLE_RADIANS);
  }

  /**
   * Commands a voltage. Clamped to the battery rail, and sent to the real {@code TalonFX} as a
   * {@link VoltageOut}.
   *
   * @param volts commanded voltage; positive drives the arm toward {@link #MAX_ANGLE_RADIANS}
   */
  public void setVoltage(double volts) {
    commandedVolts = MathUtil.clamp(volts, -12.0, 12.0);
    motor.setControl(voltageRequest.withOutput(commandedVolts));
  }

  /**
   * @return the raw encoder reading, radians. Boots at an arbitrary offset from the arm's true
   *     angle — see the class Javadoc — until {@link #zeroHere()} is called.
   */
  public double measuredPositionRadians() {
    return sim.getAngleRads() + encoderOffsetRadians;
  }

  /**
   * @return the arm's angular velocity, radians per second
   */
  public double velocityRadiansPerSecond() {
    return sim.getVelocityRadPerSec();
  }

  /**
   * @return whether the reverse limit switch is tripped. Reads the true simulated angle, not the
   *     encoder — this is true if and only if the arm is physically at its minimum travel,
   *     regardless of what {@link #measuredPositionRadians()} currently reads.
   */
  public boolean atHomeLimit() {
    return sim.getAngleRads() <= MIN_ANGLE_RADIANS + LIMIT_SWITCH_TOLERANCE_RADIANS;
  }

  /**
   * Rebases the encoder offset so {@link #measuredPositionRadians()} immediately reads {@link
   * #HOME_ANGLE_RADIANS}, whatever the arm's current true angle is. Call this once the arm is
   * actually at the limit — this method has no way to check that for you.
   */
  public void zeroHere() {
    encoderOffsetRadians = HOME_ANGLE_RADIANS - sim.getAngleRads();
  }

  /**
   * Advances the physics by one timestep and republishes telemetry, including the reverse limit
   * switch. {@code Robot.simulationPeriodic()} calls this once per 20ms tick; given tests call it
   * directly in a stepped loop.
   *
   * @param dtSeconds timestep, seconds
   */
  public void step(double dtSeconds) {
    sim.setInput(commandedVolts);
    sim.update(dtSeconds);
    double rotorRotations = sim.getAngleRads() / (2.0 * Math.PI) * GEARING;
    simState.setRawRotorPosition(rotorRotations);
    simState.setRotorVelocity(sim.getVelocityRadPerSec() / (2.0 * Math.PI) * GEARING);
    simState.setReverseLimit(atHomeLimit());
  }

  /**
   * @return the underlying {@code TalonFX}, for configuration practice. No configuration you
   *     apply through it changes what {@link #step} simulates — see the class Javadoc.
   */
  public TalonFX motor() {
    return motor;
  }

  @Override
  public void close() {
    motor.close();
  }
}
```

- [ ] **Step 3: Compile**

```bash
export JAVA_HOME=~/wpilib/2026/jdk
cd projects/m4-control
./gradlew compileJava --console=plain
```

If `LinearSystemId.createFlywheelSystem` / `createSingleJointedArmSystem`, `FlywheelSim`,
`SingleJointedArmSim`, `TalonFXSimState`, or `VoltageOut` resolve to a different overload
on the installed WPILib/Phoenix version than shown above, fix the call site here — the
physical quantities and the method names are the part that must not change; the exact
overload is not pinned by the contract.

- [ ] **Step 4: Confirm no gain leaked in**

```bash
cd projects/m4-control
grep -rniE '\bk[sSvVaAgGpPdD]\b|kSTATIC|kVELOCITY|kFF' \
  src/main/java/frc/robot/plant/
```

Expected: no output. `J_KG_M2` and `GEARING` are physical quantities, not gains — this
grep is confirming that a `kV`-, `kP`-, `kG`-shaped name never entered the file, not that
the file contains no numbers.

- [ ] **Step 5: Confirm the physical parameters actually reach a plausible bandwidth**

This step used to be a by-hand approximation; it is now confirming numbers design record
§3.1/§3.2 already derived and verified, not deriving them fresh. Record does not mean
re-run — reading the two sections and checking the plant's constants match is enough:

- **Flywheel** (`J = 0.0021`, `G = 1.5`): true kV `0.179 V/(rot/s)`, so the 40 rot/s target
  needs `7.16 V` — comfortable rail headroom. Discrete kP stability ceiling `1.496`.
- **Arm** (`m = 8` kg, `L = 0.9` m, `G = 30`, `J = 2.16`): holds horizontal on `1.99 V`; max
  speed `1.75 rad/s` (≈100°/s); a kG-omitted feedforward misses by about 12°.

Confirm the constants in the file you just wrote match this table exactly. If Task 4's
Step 5 (running a correct implementation against the real `FlywheelSim`/`SingleJointedArmSim`
integrator, not the discrete-ZOH hand model these numbers come from) finds a real
disagreement — not "tuning is fiddly," an actual mismatch like the arm holding on a
noticeably different voltage than `1.99 V` — **report the discrepancy rather than quietly
retuning to make a test pass.** These parameters were pinned specifically because an earlier,
untested pairing would have shipped a gate that couldn't fail (the original `J = 0.62`,
`G = 100` arm let a kG-omitted feedforward pass within 3° of target); silently drifting
away from the verified numbers risks the same failure mode a second time.

- [ ] **Step 6: Determinism proof — not optional**

This is the property the whole architecture rests on: a stepped loop against these plants,
run five times in one JVM with the same inputs, must produce bit-identical results. If it
does not, no given test in Task 4 can be trusted to fail for the reason it claims to.

Create the throwaway `projects/m4-control/src/test/java/frc/robot/plant/DeterminismSpikeTest.java`:

```java
package frc.robot.plant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.wpilibj.simulation.DriverStationSim;
import org.junit.jupiter.api.Test;

class DeterminismSpikeTest {

  private double runFlywheel() {
    HAL.initialize(500, 0);
    DriverStationSim.setEnabled(true);
    try (FlywheelPlant plant = new FlywheelPlant(1)) {
      plant.setVoltage(9.0);
      for (int i = 0; i < 250; i++) {
        plant.step(0.020);
      }
      return plant.velocityRotationsPerSecond();
    }
  }

  private double runArm() {
    HAL.initialize(500, 0);
    DriverStationSim.setEnabled(true);
    try (ArmPlant plant = new ArmPlant(2)) {
      plant.setVoltage(3.0);
      for (int i = 0; i < 250; i++) {
        plant.step(0.020);
      }
      return plant.measuredPositionRadians();
    }
  }

  @Test
  void flywheelStepLoopIsBitIdenticalAcrossFiveRuns() {
    double first = runFlywheel();
    for (int run = 1; run < 5; run++) {
      assertEquals(first, runFlywheel(), 0.0, "run " + run + " diverged from run 0");
    }
  }

  @Test
  void armStepLoopIsBitIdenticalAcrossFiveRuns() {
    double first = runArm();
    for (int run = 1; run < 5; run++) {
      assertEquals(first, runArm(), 0.0, "run " + run + " diverged from run 0");
    }
  }
}
```

```bash
export JAVA_HOME=~/wpilib/2026/jdk
cd projects/m4-control
./gradlew test --tests "frc.robot.plant.DeterminismSpikeTest" --console=plain --rerun-tasks
```

**Record the actual final values for both mechanisms in your task report** — not "it
passed," the numbers `assertEquals(first, ..., 0.0, ...)` compared. If either test fails,
stop: that means `FlywheelSim`/`SingleJointedArmSim` carry hidden state that is not a pure
function of the constructor arguments and the `step()` call sequence (a static field, a
random seed, anything time-based), and Task 4's whole test design needs to be rethought
before it is written, not patched with a tolerance.

Then delete the spike — it is a proof, not a shipped test:

```bash
rm projects/m4-control/src/test/java/frc/robot/plant/DeterminismSpikeTest.java
```

- [ ] **Step 7: Format and commit**

```bash
cd projects/m4-control
./gradlew spotlessApply --console=plain
./gradlew spotlessCheck --console=plain    # expect rc=0
./gradlew compileJava --console=plain      # expect rc=0
cd /Users/y0shi/workspace/ignite/docs
git status --short    # only the two plant files -- DeterminismSpikeTest.java must be gone
git add projects/m4-control/src/main/java/frc/robot/plant
git commit -m "Add M4's FlywheelPlant and ArmPlant

Built from moment of inertia and gear reduction, never from gains --
LinearSystemId.createFlywheelSystem / createSingleJointedArmSystem. Physics
integrates the commanded voltage directly rather than reading it back
through CTRE (design record §2-3); step-loop determinism confirmed
bit-identical across 5 runs in one JVM (values: <flywheel>, <arm>)."
```

Replace `<flywheel>` and `<arm>` with Step 6's actual recorded values.
