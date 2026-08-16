# M4 + T4 design record — build-order step 9

**Date:** 2026-08-16
**Spec sections implemented:** §9.5 (M4), §10 (`t4-bad-kv`), §6.7 (the required pull request), §13 step 9.
**Status:** decided, spiked, ready to build.

This record exists because M4's central mechanism was decided by experiment, not by reading
documentation, and the experiment overturned the obvious design. Anyone who later asks "why
doesn't M4 use `VelocityVoltage` when `Robot-2026` does?" should find the answer here rather
than rediscovering it.

---

## 1. The question

Spec §9.5 gives M4 the `Phoenix6` vendordep and M3's section says "Phoenix arrives in M4 where
it is the lesson." `Robot-2026`'s `ShooterSubsystem` closes its velocity loop **on the device**:
`Slot0` gains applied through `TalonFXConfiguration`, commanded with `VelocityVoltage`.

M4's given tests must assert that a flywheel "reaches and holds commanded velocity within
tolerance and settling time" and that an arm "holds position against gravity." Both require
running a closed loop for seconds of simulated time and asserting on where it ended up.

So: can a CTRE onboard closed loop be advanced by a stepped simulation loop — no wall-clock
sleeps — deterministically enough to assert on?

## 2. The spike

Run 2026-08-16 against a throwaway copy of `projects/m6-testing/` (Phoenix 6 `26.1.1`, WPILib
`2026.2.1`, JDK 17), on an otherwise idle machine. Four experiments.

| # | What it did | Result |
|---|---|---|
| **A** | `Slot0` + `VelocityVoltage`, 250 × 20 ms steps, plant fed from `simState.getMotorVoltage()` | **`35.64`, `0.00`, `0.00`** rot/s across three identical runs in one JVM |
| **B′** | Student-side `SimpleMotorFeedforward` + `PIDController`, volts fed straight to `FlywheelSim` | **`40.154889`** rot/s, five runs, bit-identical |
| **C** | `TalonFXSimState.setReverseLimit(bool)` → `motor.getReverseLimit()`, with `BaseStatusSignal.waitForAll` | `Open` / `ClosedToGround` — correct both ways |
| **D** | `setRotorVelocity(x)` then `motor.getVelocity()` in a stepped loop, with and without `.refresh()` | **`0.0000`** every iteration, both ways |

**A and D are the findings that matter.** CTRE's simulation advances its control loop and its
status-signal pipeline on wall-clock time, not on the caller's step loop. A stepped test neither
gets a converging onboard loop (A) nor can read a measurement back off the device (D). Experiment
A returning zero twice in three tries *on an idle machine* is not a tolerance problem to be tuned
around; it is the wrong mechanism for the job.

C is the useful positive: the simulated limit-switch input round-trips correctly, because that
path goes through a real-time `waitForAll` — fine for a handful of assertions, not for 250 of them.

## 3. What was decided

**M4's control loop is student-written Java, and the plant integrates the commanded voltage
directly.** The `TalonFX` is real and really receives the command; the physics does not read the
voltage back through CTRE's signal pipeline.

```java
// Student writes, once. Same code in the simulator and under test.
double volts = feedforward.calculate(setpoint) + pid.calculate(measurement, setpoint);
plant.setVoltage(volts);
```

```java
// Given, inside FlywheelPlant. The honest seam.
public void setVoltage(double volts) {
  commandedVolts = MathUtil.clamp(volts, -12.0, 12.0);
  motor.setControl(new VoltageOut(commandedVolts));   // the device really gets it
}

public void step(double dtSeconds) {
  sim.setInputVoltage(commandedVolts);                // physics uses the same number
  sim.update(dtSeconds);
  simState.setRotorVelocity(rotorRotationsPerSecond());  // telemetry stays truthful
}
```

**Rejected alternatives, and why.**

- *Keep onboard `Slot0` and loosen the tests.* Experiment A's spread is not narrow. Any bound
  loose enough to admit `0.00` admits everything, which is a gate that cannot fail — the exact
  failure class `CLAUDE.md`'s verification-discipline section exists to prevent.
- *Drop Phoenix from M4 entirely.* Loses the CTRE-configuration lesson, the SysId wiring, and
  the continuity with `ShooterSubsystem` that §9.5 asks for, in exchange for deleting six lines.
- *Two modes on the plant — real-time in the simulator, stepped under test.* Two code paths that
  must be proven equivalent, forever, to buy authenticity the single path already mostly has.

**The accepted cost, stated plainly.** Because physics integrates the commanded voltage rather
than the device's applied output, configuration that would shape that output on real hardware —
supply-current limits, voltage compensation, ramp rates — is inert in this simulation. The
`FlywheelPlant` Javadoc says so in those words. A student who configures a 40 A limit and sees no
change in the plot has not found a bug; they have found the edge of the model, and the comment
tells them where it is. Motor inversion and neutral mode *are* honoured, because the plant reads
them from the configuration it applied.

## 4. Keeping the true parameters unreadable

§9.5 requires plants "with true parameters the student must not read." The mitigation is
`CODEOWNERS` plus `tests.lock`, per §12.2 — the file is in the tree and a determined student can
open it, and that is accepted.

One thing makes it meaningfully harder for free: **construct the plants from physical quantities,
never from gains.**

```java
// Given. Reading this hands you a moment of inertia, not a kV.
LinearSystemId.createFlywheelSystem(DCMotor.getKrakenX60(1), J_KG_M2, GEARING)
```

not

```java
LinearSystemId.identifyVelocitySystem(TRUE_KV, TRUE_KA)   // this would print the answer
```

Both produce the same plant. Only the second one is the answer key. A student who opens
`FlywheelPlant.java` finds `0.0021` and `1.5` and still has to characterize the system — which
is the milestone.

## 5. Docs alignment

The milestone document currently teaches WPILib feedforward/PID math in one section and CTRE
`Slot0` configuration in another, without saying which one the project uses. That ambiguity is
what sent this design to a spike, so the plan closes it rather than leaving it for step 12:

- The **Project** section says the control loop is written in Java against the given plants.
- The **CTRE Phoenix 6 specifics** section keeps every word about config objects, `apply()`
  blocking, status-signal batching and `.clone()` — that knowledge is load-bearing for the
  stretch pull request and for M5 — and gains one short paragraph stating that M4's loop runs
  in Java, that `ShooterSubsystem` closes the same loop on the device, and why the training
  project does it the other way.
- The **Definition of done** gains the §6.7 required-pull-request item, which §13 had parked in
  step 12.

## 6. T4 — `t4-bad-kv`

No Phoenix, no vendordeps, no HAL. `FlywheelSim` and `SingleJointedArmSim` extend
`LinearSystemSim`, which is pure math and needs no simulated hardware, so the fixture is a plain
GradleRIO project shaped like `t2-supplier-swap`.

Ships a mechanism whose kV is roughly a third of the truth with a large kP papering over it: it
reaches the setpoint, overshoots, and rings. The response plot is generated, committed, and
regenerable — §16 lists re-generating it as a seasonal task, so the generator is part of the
deliverable, not a one-off.

The 60-minute pass criterion is diagnosis plus the explanation of *why* the original tuning was
fragile. The given test gates the fix; the explanation stays mentor-verified, as §10 requires.

## 7. Open items for later steps

- The `Phoenix6` vendordep is pinned at `26.1.1` here, matching `Robot-2026` @ `28ce539` and
  `projects/m6-testing/`. Spec §9.5 says `26.1.0`. The spec line is stale; step 12's doc-correction
  pass should fix it rather than this plan silently disagreeing with it.
- Nothing in this design requires changing `Robot-2026`.
