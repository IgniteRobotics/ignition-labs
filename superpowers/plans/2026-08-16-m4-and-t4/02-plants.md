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

**A documentation note, not a contract change:** the design record's §3 says motor
inversion and neutral mode are "honoured, because the plant reads them from the
configuration it applied." The concrete code in that same section feeds physics from
`commandedVolts` — the pre-invert value passed to `setVoltage` — not from anything read
back off the `TalonFX`, and reading configuration back safely (without reintroducing the
wall-clock dependency §2 spent four experiments ruling out) is not a small addition. This
task implements the code as written in §3 and, for that reason, does **not** honour
inversion or neutral mode — physics always uses the raw `commandedVolts`. Both plants'
Javadoc says so, alongside the current-limit / voltage-compensation / ramp-rate
limitation the design record already documents. This is a documentation-accuracy
correction, not a contract change: the pinned API contract in `00-plan.md` says nothing
about inversion, so nothing pinned is affected.

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

  private static final double J_KG_M2 = 0.62;
  private static final double GEARING = 100.0;
  private static final double ARM_LENGTH_METERS = 0.75;
  private static final double MIN_ANGLE_RADIANS = HOME_ANGLE_RADIANS;
  private static final double MAX_ANGLE_RADIANS = 2.0944; // 120 degrees
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

Sanity-check by hand, not by running a controller (none exists yet): a `DCMotor.getKrakenX60(1)`
free speed is roughly 608 rad/s; at `GEARING = 1.5` the mechanism free speed caps near 405
rad/s (≈64 rot/s), comfortably above the 40 rot/s flywheel target Task 4 will assert
against. For the arm, `GEARING = 100.0` gives enormous torque authority relative to
gravity's load at this length and mass — expect the arm to move quickly and to need real
feedback to avoid overshoot, which is the lesson, not a defect. If Task 4's Step 5 (running
a correct implementation) finds either mechanism cannot reasonably reach its target within
the loop budget it plans to use, that is this step's approximation being wrong in practice
— come back and adjust `J_KG_M2`, `GEARING`, or `ARM_LENGTH_METERS` here, and record the
change in this task's commit message rather than papering over it with a looser test
tolerance in Task 4.

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
