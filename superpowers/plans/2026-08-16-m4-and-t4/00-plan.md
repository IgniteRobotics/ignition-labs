# M4 Control + T4 `bad-kv` Implementation Plan

> **Renamed 2026-08-18.** The fixture this plan builds as `projects/testouts/t4-bad-kv/`
> now ships as `projects/testouts/t4-ringing-flywheel/`. The old name announced the fault
> before a student opened anything — the same leak class T5's log file sizes carried. This
> plan is left in its original wording as the record of what was built and when; only the
> directory moved. Three published documents that also stated the fault outright were
> corrected at the same time.

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans. Steps use checkbox (`- [ ]`) syntax for tracking.
>
> This is a **split plan** per [`superpowers/plan-conventions.md`](../../plan-conventions.md). Read this file plus the one task file you were handed. Do not read sibling task files — each is self-sufficient, and reading them all is the cost this layout exists to remove.

**Goal:** Ship `projects/m4-control/` (an L1 harness-and-fill milestone where the student characterizes two simulated mechanisms they cannot look up, writes feedforward and feedback for both, motion-profiles the arm and homes it against a limit switch) and `projects/testouts/t4-bad-kv/` (the Track D fixture: a mechanism whose kV is a third of the truth with a large kP hiding it). This is build-order step 9 (spec §13).

**Architecture:** One standalone GradleRIO project plus one standalone test-out fixture. The novel mechanism is the **plant seam**, and it was decided by spike rather than by documentation — see the design record. CTRE's simulation advances its onboard control loop and its status-signal pipeline on wall-clock time, so neither can be driven by a stepped test: a `Slot0` + `VelocityVoltage` loop stepped 250 × 20 ms returned `35.64`, `0.00`, `0.00` on three identical runs, and `getVelocity()` never reflects `setRotorVelocity()` at all. Three consequences shape every task:

1. **The student's control loop is Java, and the plant integrates the commanded voltage directly.** The `TalonFX` is real and really receives a `VoltageOut`; the physics uses the same number rather than reading it back through CTRE. One code path, identical in `simulateJava` and under test, bit-identical across runs.
2. **The plants are the measurement surface.** `FlywheelPlant.velocityRotationsPerSecond()` and `ArmPlant.measuredPositionRadians()` come off the WPILib sim, not off a status signal. The limit switch does too — experiment C proved the device path works, but only through a real-time `waitForAll`, which does not belong inside a 250-iteration loop.
3. **Given tests drive the student's loop through `M4Deliverables`.** Same seam M3 used, for the same reason: a given test cannot name a class the student has not written.

**Tech Stack:** Java 17, GradleRIO 2026.2.1, Gradle 8.11, Spotless 8.2.1 (`googleJavaFormat 1.19.2`), JUnit Jupiter 5.10.1, WPILibNewCommands 1.0.0 (frcYear 2026), Phoenix 6 `26.1.1`, Python 3.11 + matplotlib for T4's plot generator.

**Design record:** [`superpowers/specs/2026-08-16-m4-control-design.md`](../../specs/2026-08-16-m4-control-design.md). **Read it before Task 2 or Task 6.** It carries the spike numbers, the rejected alternatives, and the one accepted modelling limitation.

---

## Constraints delta

`CLAUDE.md` at the repository root loads automatically and already carries the toolchain, pinned versions, Spotless wiring, package naming, the fade curve, the authoring rules, the clean-clone invariant, and the verification discipline. **Read it. It is not repeated here.** Only what is specific to M4 and T4 appears below.

- **Fade level L1 — harness and fill** (spec §8). Given: `Main`/`Robot` wiring, both simulated plants, vendordeps, sim config, the `PlotHarness`, the `M4Deliverables` seam, and given tests that assert observable behaviour. Student writes: subsystems, commands, feedforward application, gains, PID, the motion profile, the homing sequence. M3 is the same level — match its density, do not exceed it.
- **The plants' true parameters are physical, never gains.** `LinearSystemId.createFlywheelSystem(DCMotor, J, gearing)`, never `identifyVelocitySystem(kV, kA)`. The second form prints the answer key into the skeleton. Same for the arm: `createSingleJointedArmSystem(DCMotor, J, gearing)`.
- **No kS, kV, kA, kG, kP, kD or any numeric equivalent appears anywhere a student can read it** — not in `Constants.java`, not in a Javadoc, not in a test's expected value, not in `README.md`, not in a variable name. Task 4 and Task 9 both grep for this. A test asserting `assertEquals(0.12, ...)` on a gain would hand over kV.
- **Given tests use a loose behavioural gate, not a numeric tuning gate.** Reaches setpoint, holds it, does not run away, homes to the limit. Overshoot and settling-time *quality* is mentor-verified from the student's own plot, which is already a DoD item. A bound tight enough to catch kP-first tuning also fails legitimately good gains, and the student cannot tell the two apart.
- **Vendordeps: `Phoenix6-26.1.1` + `WPILibNewCommands` only** (spec §9) for M4. Copy `Phoenix6-26.1.1.json` verbatim from `projects/m6-testing/vendordeps/`. **T4 has no vendordeps at all** — `FlywheelSim` extends `LinearSystemSim`, which is pure math and needs neither the HAL nor CAN.
- **Spec §9.5 says Phoenix `26.1.0`; this plan ships `26.1.1`.** That matches `Robot-2026` @ `28ce539` and `projects/m6-testing/`. The spec line is stale. Task 9 does **not** silently edit it — it is logged for step 12's doc-correction pass.
- **Package `frc.robot`** for M4 (M3 onward, per `CLAUDE.md`). **`frc.testout.t4`** for T4, matching `t1` and `t2`.
- **`plant/` and `harness/` are already locked by `tools/tests_lock.py:PATTERNS`** — `projects/*/src/main/java/**/plant/*.java` and `.../harness/*.java` are globbed today. Putting the plants in `frc.robot.plant` and the seam in `frc.robot.harness` gets tamper-evidence for free. Do not invent a new directory.
- **Pinned SHA.** `Robot-2026` → `28ce5397d88275001d656dbb717d22cf9ac0150d` (2026-06-09), the same SHA M2, M3 and M6 already cite. Every student-facing reference to `ShooterSubsystem` or `DriveMotorConfigs` is pinned to it.
- **`Robot-2026` is not modified by this plan.** The stretch pull request is a student's to open.
- **`tests.lock`, `docs/programming/README.md`, and `docs/programming/teaching-repo-spec.md` belong to Task 9 only.** No other task may touch them — `plan-conventions.md` is explicit and two lanes finishing together will clobber each other otherwise.

---

## The pinned API contract

**Every signature below is fixed here and must match byte-for-byte in every task that names it.** This section exists because five task files reference these types and no worker reads a sibling task file. If a task needs a signature that is not here, that is a gap in this plan — fix it here, not locally.

### `frc.robot.plant.FlywheelPlant` — given, implemented, locked

```java
public final class FlywheelPlant implements AutoCloseable {
  public FlywheelPlant(int canId);
  public void setVoltage(double volts);                 // clamped to ±12; also sent as VoltageOut
  public double velocityRotationsPerSecond();           // the measurement the student reads
  public void step(double dtSeconds);                   // advances physics; harness + Robot call it
  public TalonFX motor();                               // for configuration practice and SysId
  public void close();
}
```

### `frc.robot.plant.ArmPlant` — given, implemented, locked

```java
public final class ArmPlant implements AutoCloseable {
  /** Radians from horizontal that a homed arm reads when it is sitting on the reverse limit. */
  public static final double HOME_ANGLE_RADIANS = -0.5236;   // -30°

  public ArmPlant(int canId);
  public void setVoltage(double volts);                 // clamped to ±12; also sent as VoltageOut
  public double measuredPositionRadians();              // raw encoder: boots at an arbitrary offset
  public double velocityRadiansPerSecond();
  public boolean atHomeLimit();                         // reverse limit switch
  public void zeroHere();                               // offsets the encoder so measured == HOME_ANGLE_RADIANS
  public void step(double dtSeconds);
  public TalonFX motor();
  public void close();
}
```

### `frc.robot.harness.M4Deliverables` — given interface, `RobotContainer` implements it

```java
public interface M4Deliverables {
  void setFlywheelTarget(double rotationsPerSecond);
  void updateFlywheel();                                // exactly one 20 ms iteration of your loop
  void setArmTarget(double radians);
  void updateArm();                                     // exactly one 20 ms iteration of your loop
  Command homingCommand();                              // a NEW instance each call
  FlywheelPlant flywheelPlant();
  ArmPlant armPlant();
}
```

### `frc.robot.harness.PlotHarness` — given, implemented, locked

```java
public final class PlotHarness {
  public PlotHarness(String name, String setpointLabel, String measurementLabel);
  public void record(double timeSeconds, double setpoint, double measurement);
  /** Writes build/plots/<name>.csv and returns the path written. */
  public Path write() throws IOException;
}
```

### Plant physical parameters — pinned

Both plants are Kraken X60 ×1 (`DCMotor.getKrakenX60(1)`; it lives in `wpimath` and needs no
vendordep).

| Plant | Parameters | Consequences |
|---|---|---|
| `FlywheelPlant` | `J = 0.0021` kg·m², `G = 1.5` | true kV `0.179 V/(rot/s)` → `7.16 V` at the 40 rot/s setpoint; **kP stability ceiling `1.496`** |
| `ArmPlant` | `m = 8` kg, `L = 0.9` m, `G = 30`, `J = mL²/3 = 2.16` kg·m² | `V_hold = 1.99 V` at horizontal; max speed `1.75 rad/s`; kG-omitted distractor misses by `11.96°` |

`ArmPlant`'s angle limits: minimum `HOME_ANGLE_RADIANS` (−30°, where the reverse limit switch
sits), maximum `1.5708` (+90°).

**`SingleJointedArmSim` takes no mass.** Its gravity term is `α = −3/2·g·cos(θ)/L`, which assumes
`J = ⅓mL²`, so mass cancels and gravity depends only on arm length. `J` and `L` are separate
constructor arguments but are not physically independent — if you change one, re-derive the other,
and keep `m = 8` kg documented in the Javadoc so the next maintainer can. Design record §3.1 has
the derivation and the numbers that were rejected.

### Constants the given tests and the student both use

`frc.robot.Constants.FlywheelConstants.TARGET_ROTATIONS_PER_SECOND = 40.0`
`frc.robot.Constants.ArmConstants.SCORING_ANGLE_RADIANS = 0.7854` (45°)
`frc.robot.Constants.SIM_STEP_SECONDS = 0.020`

**No gain constant of any kind lives in `Constants.java`.** The student adds their own, and that is the milestone.

### T4 — `frc.testout.t4`

```java
public final class BadlyTunedFlywheel {
  public BadlyTunedFlywheel();
  public void setTarget(double rotationsPerSecond);
  public void update();                                 // one 20 ms iteration, gains live in here
  public double velocityRotationsPerSecond();
  public double appliedVolts();
}
```

Gains are fields on `BadlyTunedFlywheel` — that is the whole point of the fixture, the student edits them. The plant it drives is `frc.testout.t4.plant.T4FlywheelPlant`, physically parameterised, and locked:

```java
public final class T4FlywheelPlant {
  public T4FlywheelPlant();
  public void setVoltage(double volts);                 // clamped to ±12
  public double velocityRotationsPerSecond();
  public void step(double dtSeconds);
}
```

### Never call `FlywheelSim.setInputVoltage()` in T4

`FlywheelSim.setInputVoltage(double)` is `setInput(volts)` followed by
`clampInput(RobotController.getBatteryVoltage())`, and `getBatteryVoltage()` goes through the
HAL. T4 initialises no HAL by design, and an uninitialised `getBatteryVoltage()` returning `0.0`
would clamp every input to zero — a plant that silently never moves. Both plants clamp in Java
and call the HAL-free `setInput(double)` instead:

```java
sim.setInput(MathUtil.clamp(volts, -12.0, 12.0));
sim.update(dtSeconds);
```

M4 does initialise the HAL, so this is not strictly forced there — but M4 uses the same two lines
anyway, so that the two plants read identically and neither depends on battery state that the
model ignores.

---

## File structure

**Created — `projects/m4-control/`**

| Path | Responsibility |
|---|---|
| `settings.gradle`, `gradlew`, `gradlew.bat`, `gradle/wrapper/`, `.gitignore`, `WPILib-License.md`, `.wpilib/`, `simgui*.json`, `networktables.json` | Copied from M3 unchanged |
| `build.gradle` | M3's shape verbatim — it *is* a robot — plus the Phoenix vendordep |
| `vendordeps/WPILibNewCommands.json`, `vendordeps/Phoenix6-26.1.1.json` | From `projects/m6-testing/vendordeps/`, verbatim |
| `src/main/java/frc/robot/plant/FlywheelPlant.java` | **Given, implemented, locked.** Physical parameters only |
| `src/main/java/frc/robot/plant/ArmPlant.java` | **Given, implemented, locked.** Gravity, limit switch, encoder offset |
| `src/main/java/frc/robot/harness/M4Deliverables.java` | **Given, locked.** The seam the given tests drive |
| `src/main/java/frc/robot/harness/PlotHarness.java` | **Given, locked.** CSV to `build/plots/` |
| `src/main/java/frc/robot/harness/SysIdWiring.java` | **Given, locked.** `SysIdRoutine` + `SignalLogger`, boilerplate, Yellow-tier permitted |
| `src/main/java/frc/robot/Main.java`, `Robot.java`, `Constants.java` | Given wiring. `Robot.simulationPeriodic()` steps both plants |
| `src/main/java/frc/robot/RobotContainer.java` | **Student territory.** Implements `M4Deliverables`; every method a throwing stub |
| `src/test/java/frc/robot/FlywheelVelocityTest.java` | Given. Reaches, holds, does not run away |
| `src/test/java/frc/robot/ArmGravityTest.java` | Given. Holds position against gravity |
| `src/test/java/frc/robot/ArmHomingTest.java` | Given. Homes from an unknown start to the limit |
| `src/test/java/frc/robot/harness/SimDriver.java` | Given helper. The stepped loop, in one place |
| `README.md` | Student-facing: the two parts, how to run SysId, how to get a plot |
| `READING.md` | `ShooterSubsystem` and `DriveMotorConfigs`, pinned to `28ce539` |

**Created — `projects/testouts/t4-bad-kv/`**

| Path | Responsibility |
|---|---|
| Gradle scaffold | Copied from `t2-supplier-swap`. **No vendordeps** |
| `src/main/java/frc/testout/t4/plant/T4FlywheelPlant.java` | **Given, locked.** Physical parameters |
| `src/main/java/frc/testout/t4/BadlyTunedFlywheel.java` | **Student edits this.** kV ≈ ⅓ of truth, kP large |
| `src/test/java/frc/testout/t4/FlywheelTuningTest.java` | Given. Fails against the shipped gains, passes against a correct fix |
| `plots/response.csv`, `plots/response.png` | Pre-generated, committed, regenerable |
| `plots/README.md` | How to regenerate, and why §16 says you will have to |
| `README.md` | The task, the three questions, the 60-minute box |

**Created — elsewhere**

| Path | Responsibility |
|---|---|
| `tools/gen_t4_plot.py` | CSV → PNG. Stdlib + matplotlib. Run from the repo root |
| `tests/test_gen_t4_plot.py` | Pytest coverage against a synthetic CSV |

**Created — `../ignite-training-solutions/`** (separate repository, mentor-only)

| Path | Responsibility |
|---|---|
| `solutions/m4-control/src/main/java/frc/robot/RobotContainer.java` | The reference implementation, with real characterized gains |
| `answer-keys/t4-bad-kv.md` | The diagnosis, the fix, and **why each distractor is wrong** (§10) |
| `authoring-notes/m4.md` | Which trap each given test aims at; the spike result restated |

**Modified** (Task 9 only)

| Path | Change |
|---|---|
| `tests.lock` | Regenerated |
| `.github/CODEOWNERS` | `/projects/*/src/main/java/**/plant/` if not already covered |
| `docs/programming/milestones/m4-sensors-actuators-control.md` | Project pointer; the docs-alignment paragraph (design record §5); the §6.7 required-PR DoD item |
| `docs/programming/README.md` | Status row: M4 and T4 built |
| `docs/programming/teaching-repo-spec.md` | §13 step 9 → Done |

---

## Task graph

| Task | File | Depends on | Touches | Lane |
|---|---|---|---|---|
| 1 | `01-scaffold.md` | — | `projects/m4-control/` build files | milestone |
| 2 | `02-plants.md` | 1 | `.../frc/robot/plant/` | milestone |
| 3 | `03-harness-and-wiring.md` | 2 | `.../frc/robot/harness/`, `Robot`, `Constants`, `RobotContainer` | milestone |
| 4 | `04-given-tests.md` | 3 | `projects/m4-control/src/test/` | milestone |
| 5 | `05-readme-and-reading.md` | 4 | `projects/m4-control/README.md`, `READING.md` | milestone |
| 6 | `06-t4-fixture.md` | — | `projects/testouts/t4-bad-kv/` | testout |
| 7 | `07-t4-plot.md` | 6 | `t4-bad-kv/plots/`, `tools/`, `tests/` | testout |
| 8 | `08-solutions.md` | 4, 7 | `../ignite-training-solutions/` | tail |
| 9 | `99-integration.md` | all | `tests.lock`, `CODEOWNERS`, `docs/` | integration |

**Lanes:** `milestone` (tasks 1 → 2 → 3 → 4 → 5, strictly sequential — one Gradle project, one `build/`), `testout` (tasks 6 → 7, sequential).
**Maximum useful concurrency: 2.** Do not fan out further. Everything inside `projects/m4-control/` shares a `build/` directory and a Gradle daemon; a second agent in there collides rather than helps.
**Serialized tail:** task 8, then task 9. Single owner, on the merged branch. Task 8 runs `cross-check.sh`, which archives this repository at `HEAD` — it needs both lanes committed, not merely present in a worktree.

**Worktrees:** `.worktrees/m4` for the milestone lane, `.worktrees/t4` for the testout lane. Never two agents in one worktree, and never two agents running Gradle against the same project directory.

---

## Self-review

**Spec coverage.**

| Spec requirement | Task |
|---|---|
| §9.5 two simulated plants, `FlywheelSim` and `SingleJointedArmSim` | 2 |
| §9.5 true parameters the student must not read; `plant/` package, `CODEOWNERS`, `tests.lock` | 2 (physical params), 9 (lock + owners) |
| §9.5 `SysIdRoutine` wiring given as permitted boilerplate | 3 |
| §9.5 Epilogue `@Logged` on setpoint and measurement together | 3 |
| §9.5 `PlotHarness` dumping setpoint-vs-measurement CSV | 3 |
| §9.5 limit-switch sim for homing | 2 (`ArmPlant.atHomeLimit`) |
| §9.5 student writes feedforward, gains, PID, motion profile, homing | 3 (the stubs), 8 (the reference implementation) |
| §9.5 vendordeps `Phoenix6` + `WPILibNewCommands` | 1 |
| §9.5 tests: flywheel reaches and holds; arm holds against gravity; homing zeroes on contact | 4 |
| §9.5 mentor-verified half (kS/kV/kA/kG, why FF before P, reads own plot) | 5 (`README.md` states it), 9 (milestone doc) |
| §6.7 / §9.5 the required real pull request | 5 (`README.md`), 9 (DoD item in the milestone doc) |
| §8 L1 density — harness given, lesson withheld | Constraints delta; 3 |
| §8 a stub never contains the answer in a comment | 3, and the gain grep in 4 and 9 |
| §8 a given test never asserts on implementation | 4 — every assertion goes through `M4Deliverables` and the plants |
| §8 no test asserts that a stub throws | 4 |
| §10 `t4-bad-kv`: kV far too low, kP compensating, pre-generated response plot | 6, 7 |
| §10 T4 answer key explains the distractors, not just the answer | 8 |
| §11.1 reference implementation + authoring notes + answer key | 8 |
| §11.2 solutions cross-check green | 8 |
| §12.3 clean-clone invariant: `assemble` green, `compileTestJava` green, `test` red | 4 Step 7 proves it |
| §13 step 9 status | 9 |
| §14 zero files from `projects/` in the built site | 9 |
| §16 T4's plot is regenerable | 7 |

**Not covered, deliberately.** The M4 mentor-verified items stay mentor-verified per §9.5: explaining what each of kS, kV, kA and kG does, why feedforward is tuned before P, reading their own plot, having run SysId themselves, and what happens when a CAN device stops hearing the roboRIO. None of these get a test. They appear in the project `README.md`'s "Done when" list and in the milestone document.

**The one modelling limitation, stated once so three tasks agree on it.** Physics integrates the *commanded* voltage, not the device's applied output, so supply-current limits, voltage compensation and ramp rates are inert in this simulation. Inversion and neutral mode are honoured. `FlywheelPlant`'s Javadoc says this in those words (Task 2); `README.md` repeats it where a student will hit it (Task 5). It is a consequence of experiment D, not an oversight — the design record has the numbers.

**Type consistency.** Every cross-task signature is pinned in *The pinned API contract* above and nowhere else. `FlywheelPlant` is named in tasks 2, 3, 4, 5, 8. `ArmPlant` and `HOME_ANGLE_RADIANS` in 2, 3, 4, 8. `M4Deliverables` in 3, 4, 8. `PlotHarness` in 3, 5, 8. `SimDriver` in 4 and 8. `T4FlywheelPlant` and `BadlyTunedFlywheel` in 6, 7, 8. `Constants.SIM_STEP_SECONDS` in 3 and 4.

**The failure class this plan is built against.** This repository has repeatedly produced checks that pass while the thing they protect is broken, and M4's given tests are a gate on tuning quality — the easiest kind to get wrong in both directions at once. Six steps exist only to attack that, and none are optional:

- **Task 2 Step 6** — determinism proof. Run each plant's step loop five times in one JVM and confirm the final state is bit-identical. This is the property experiment B′ demonstrated and the entire architecture rests on; if it does not hold, stop and report rather than loosening a tolerance.
- **Task 4 Step 5** — run a **correct** implementation against the suite and confirm green. A suite no right answer passes is as broken as one every wrong answer passes.
- **Task 4 Step 6** — run the **distractors**: kP-only with no feedforward, feedforward with the gear ratio applied twice, an arm with kG omitted, and a homing routine that zeroes without ever touching the limit. Each must fail the test aimed at it, and each failure message must be read.
- **Task 4 Step 7** — the clean-clone invariant, from an actual fresh clone, all three commands, output pasted.
- **Task 6 Step 5** — confirm the shipped T4 gains actually produce the described symptom: reaches target, overshoots, oscillates. A fixture whose plot shows a clean first-order rise teaches nothing and the student cannot diagnose it.
- **Task 7 Step 4** — regenerate the committed plot from scratch and confirm it is byte-identical to what shipped. §16 asks a future maintainer to do exactly this; if it does not reproduce today it will not reproduce in a year.

A task report that omits its command output is not a completed task.

**Known limitation.** `FlywheelPlant.java` and `ArmPlant.java` are in the tree, so a student can open them and read `J_KG_M2` and `GEARING`. This is accepted, not overlooked — §12.2's mitigation is `tests.lock` plus `CODEOWNERS` plus a failure message that invites disagreement, and hiding the file would mean shipping a binary blob into a teaching repository. What the plants must never contain is a **gain**, because a gain is the answer and a moment of inertia is a starting point. Task 2 constructs both plants from physical quantities for exactly this reason, and Task 4 and Task 9 grep to keep it that way.
