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
tells them where it is.

**Corrected 2026-08-16.** An earlier draft of this section claimed motor inversion and neutral mode
*are* honoured "because the plant reads them from the configuration it applied." That is wrong, and
the code snippet above contradicts it: physics is fed from `commandedVolts`, the pre-inversion
value, and nothing is read back off the device. Honouring inversion would mean reading the applied
configuration back through CTRE, which is the wall-clock dependency §2 spent four experiments
ruling out. **Inversion and neutral mode are inert too**, alongside current limits, voltage
compensation and ramp rates. Both plants' Javadoc says so plainly. Caught during plan review by the
agent implementing Task 2 — the review working as intended.

## 3.1 The arm's gravity model, and what it forces

`SingleJointedArmSim` does not take a mass. Its gravity term is

```
α = −3/2 · g · cos(θ) / L
```

derived on the assumption that `J = ⅓mL²` — so **mass cancels and gravity depends only on arm
length.** Two consequences that are easy to get wrong and were:

1. **`J` and `L` are independent constructor arguments, but physically they are not.** A plant
   built with a `J` that is not `⅓mL²` for the `L` it was given is unphysical. Pick `m` and `L`,
   then derive `J`.
2. **The voltage needed to hold the arm horizontal is what decides whether the milestone's arm
   is a real exercise**, and it falls out as

   ```
   V_hold = (3/2 · g / L) / (G·Kt / (R·J))     which for a consistent J reduces to  ≈ 8.3 · m·L / G
   ```

An early draft used `J = 0.62`, `G = 100`, giving `V_hold = 0.257 V`. At that scale a student who
omits kG entirely lands within about 3° of target — so the "arm holds position against gravity"
test would pass the exact distractor it exists to catch, and the milestone's kG lesson would be
unassessed. The failure is silent: the arm looks fine.

**Verified parameters** (Kraken X60 ×1, `m = 8 kg`, `L = 0.9 m`, `G = 30`, `J = mL²/3 = 2.16`):
`V_hold = 1.99 V`, maximum speed `1.75 rad/s` (≈100 °/s), and the kG-omitted distractor settles
**11.96° short** — a gate that fails with margin. The stiffness is sane too: the earlier `G = 100`
arm had a velocity pole near `−181`, four times faster than the 20 ms control period, which is a
badly scaled mechanism even where the integrator copes.

## 3.2 The flywheel's kP ceiling

With `J = 0.0021`, `G = 1.5` and a Kraken, true kV is `0.179 V/(rot/s)` — `7.16 V` at the 40 rot/s
setpoint, comfortable against the 12 V rail. The discrete open-loop pole is `0.786`, which puts the
**kP stability ceiling at 1.496**: kP `1.0` peaks at `41.42` and settles clean, kP `2.0` diverges.

That band is narrow but it is honest, and "too high oscillates" is a lesson the milestone already
teaches. What must not happen is a reference implementation or a README tuning hint that walks a
student past `1.4` and leaves them thinking they broke something. Whatever gains the solutions
repository ships must sit visibly inside the band.

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

### 6.1 The symptom the physics actually produces

§10 and the milestone document both describe T4 as "reaches target, overshoots, oscillates." A
parameter sweep run 2026-08-16 across four plants (NEO and Kraken, `J` from `0.0006` to `0.05`,
gearings 1 and 3) and kP from `1.0` to `64.0` found **no configuration that overshoots strongly
and also settles on the setpoint.** The best overshoot available anywhere in the space is `+2.9%`,
and it comes with `-5.1%` steady-state error.

That is not a tuning failure, it is the system order. **A flywheel velocity loop is first-order.**
With the input voltage-saturated for the whole rise there is no stored momentum to carry the
output past the setpoint, so the only overshoot mechanism is discrete-time gain-induced ringing —
a negative closed-loop pole. Reaching that pole requires a kP close to the discrete stability
edge, and the same kP is what bounds how small the feedforward-deficit offset can get. The two
requirements pull in opposite directions.

The shipped fixture (NEO, `J = 0.0006`, direct drive, kV at a third of truth, kP `1.5`, pole
`-0.740`) therefore produces: a saturated rise, a peak of `41.15` rot/s, several visibly decaying
rings, and a settle at `37.95` rot/s against a `40.0` setpoint.

**The steady-state offset is the best clue in the plot, and the documents do not currently mention
it.** It is the direct signature of feedback covering for feedforward: at equilibrium the P term
must supply exactly the voltage the feedforward failed to, so the residual error is
`(kV_true − kV_set) · setpoint / kP`. Step 9's docs-alignment edit adds "and settles a little short
of the setpoint" to the symptom description in both places. Nothing about the diagnosis moves into
student-facing text.

### 6.2 Why the gate cannot be passed by the wrong diagnosis

Verified numerically before the fixture was written:

| Student's change | Overshoot | Steady state | Verdict |
|---|---|---|---|
| *(shipped)* kV ⅓, kP 1.5 | `+1.15` | `−2.05` | **FAIL** |
| Fix kV, leave kP at 1.5 | `+1.99` | `0.00` | **FAIL** — overshoot |
| Fix kV, retune kP to 0.5 | `0.00` | `0.00` | **PASS** |
| Cheat: leave kV, raise kP to 1.7 | `+1.99` | `−1.82` | **FAIL** |
| Cheat: leave kV, raise kP to 3.0 | `+4.49` | `−3.35` | **FAIL** |

The second row is the pedagogically important one and it is deliberate: correcting the
feedforward without re-tuning the proportional term still fails, because a kP chosen to paper
over a bad model is the wrong kP for a good one. That is the milestone's stated lesson —
"tune feedforward first, then a small amount of feedback" — enforced by the gate rather than
asserted in prose. The fixture README describes the symptom; it never says which term is wrong.

## 7. Open items for later steps

- The `Phoenix6` vendordep is pinned at `26.1.1` here, matching `Robot-2026` @ `28ce539` and
  `projects/m6-testing/`. Spec §9.5 says `26.1.0`. The spec line is stale; step 12's doc-correction
  pass should fix it rather than this plan silently disagreeing with it.
- Nothing in this design requires changing `Robot-2026`.
