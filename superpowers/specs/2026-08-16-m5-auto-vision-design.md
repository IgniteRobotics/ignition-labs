# M5 + T5 design record — build-order step 10

**Date:** 2026-08-16
**Spec sections implemented:** §9.6 (M5), §10 (`t5-drifting-auto`), §13 step 10.
**Status:** spiked, decided, ready to plan.

Step 9 established that this repository's hard questions are answered by experiment, not by
documentation — M4's whole architecture turned on a spike that contradicted the obvious design.
M5 depends on two vendor simulations and a third-party path follower, so the same discipline was
applied first, before a line of the plan was written.

---

## 1. The spikes

Run 2026-08-16 against a throwaway project carrying all four of M5's vendordeps at the versions
`Robot-2026` pins: `PathplannerLib 2026.1.2`, `photonlib v2026.2.1`, `Phoenix6 26.1.1`,
`WPILibNewCommands 1.0.0`.

| # | Question | Result |
|---|---|---|
| **A** | Does `VisionSystemSim` produce results headless — no coprocessor, no NT server? | **Yes.** 100/100 frames with targets |
| **B** | Is it deterministic across runs in one JVM? | **No**, in a free-running loop: `yawSum` `1091.84` / `−207.13` / `−207.13` |
| **C** | Does the AprilTag field layout load offline? | **Yes.** 22 tags |
| **D** | Is `SwerveDrivePoseEstimator` deterministic when stepped? | **Yes.** `1.980000000` × 3 |
| **E** | Is B's variance bounded warm-up or randomness? | Neither — **the frame never changed at all**. Constant `−4.1425` while the robot moved |
| **F** | Spread with noise on | mean yaw `−4.31 … −4.12` across five runs |
| **G** | **Does the sim honour WPILib's *simulated* clock?** | **Yes.** Paused timing + `stepTiming(0.020)` → **39 distinct frames, identical to full precision, three runs** |
| **H/I/J** | Does PathPlanner load and follow a path headless, deterministically? | **Yes.** `RobotConfig.fromGUISettings()` OK, path loaded, **62 ticks and `endX=2.396930148` identical × 3** |
| **K** | Can a real `.wpilog` be authored headless? | **Yes.** `DataLogWriter`, 3668 bytes for 250 samples |

### What B and E actually meant

`PhotonCameraSim` is **FPS-limited**. In a tight loop almost no time passes, so it never produces a
new frame and `getLatestResult()` returns the same cached one — which is why E saw a frozen yaw
while the robot drove 0.02 m per iteration. B's variance was not sensor noise; it was **JIT warm-up
changing how much wall-clock elapsed**, and therefore how many frames got produced.

### G is the finding that shapes the milestone

**PhotonVision's simulation reads WPILib's timebase; CTRE Phoenix's does not.** That single
difference is why M4 and M5 have opposite architectures:

| | Clock | Consequence |
|---|---|---|
| CTRE Phoenix 6 sim | wall-clock | M4 had to keep the device out of the tested control path entirely |
| PhotonVision sim | WPILib simulated time | **M5 can use the real `VisionSystemSim` directly** |
| PathPlanner | caller-driven | deterministic under a stepped `CommandScheduler` |

So M5's given harness pauses timing and steps it, exactly as M4's `SimDriver` already does, and the
student's vision code runs against the genuine PhotonVision simulation rather than a stand-in. This
is a materially better outcome than M4's and it should be stated in the milestone document, because
"why does M4 bypass the vendor sim and M5 not?" is otherwise a fair and unanswerable question.

---

## 2. Decisions

**The drivetrain is a deterministic kinematic swerve sim. Phoenix stays out of the loop.**
Spec §9.6 requires the `Phoenix6` vendordep "matching `Robot-2026` exactly" and it ships — but
nothing in the tested path reads a CTRE signal, for the reason step 9 measured. Building a swerve
drivetrain is explicitly *not* M5's lesson; composing autos and fusing vision is. A kinematic sim
that integrates commanded `ChassisSpeeds` is given, locked, and enough.

**PathPlanner is real, including the deploy directory.** `RobotConfig.fromGUISettings()` reads
`src/main/deploy/pathplanner/settings.json`, which the fixture ships — that is how a student's own
project works, and building the config in code instead would teach a path they will not take.

**Vision noise and latency are configurable and default to on.** §9.6 asks for a harness the
student "can make worse on purpose." Noise is unseeded, so **no given test may assert a
frame-exact vision value.** The three required tests are all robust to that, which is not a
coincidence — it is why these three:

| Spec test | Why it survives unseeded noise |
|---|---|
| Routine terminates when a step is forced to fail | No vision involved |
| Fused pose error < odometry-only error over a run | Aggregate over a whole run, not a frame |
| A deliberately wrong timestamp is rejected by the student's filter | Calls the student's filter directly with a synthetic measurement — no camera in the loop |

That third one is the milestone's real gate. §9.6 says it is "the only DoD item in M5 a student can
otherwise fake by getting a good result for the wrong reason."

---

## 3. T5 — `t5-drifting-auto`

§10 calls this "the most expensive artifact in this spec": three log sets, one per root cause —
odometry drift, mistuned path controller, blocking named command — each convincing enough that the
other two causes can be **ruled out by evidence**. The pass criterion is the correct diagnosis *and*
the ruling-out, so a log that only supports the right answer fails the fixture, not the student.

**They are recordings of real runs, not synthesized traces.** Spike K confirms `.wpilog` can be
written headless, so the fixture ships a recorder that runs the *same* auto three times against the
*same* drivetrain with one fault injected each time. This matters more than convenience:

- A synthesized trace is only as consistent as the author's imagination, and the ruling-out
  evidence is exactly the part an author forgets to fake.
- A recorded run is internally consistent by construction — module states, pose, path target, and
  event-marker timing all agree because they came from one execution.
- §16 asks a future maintainer to **re-generate T5's logs if the sim stack changes**. A recorder is
  regenerable; three hand-built binaries are not.

Each log must carry enough signal to discriminate, which is the actual design work:

| Fault | Its signature | What rules the other two out |
|---|---|---|
| Odometry drift | Estimated pose diverges from true pose; path *target* tracks fine against the estimate | Controller error stays small; every step completes on time |
| Mistuned path controller | Large, persistent error between path target and estimated pose | Estimated pose matches true pose; steps still complete |
| Blocking named command | One step's duration runs long or never ends; later markers never fire | Pose is accurate and controller error is small right up to the stall |

So every log records, at minimum: true pose, estimated pose, path target pose, per-module state,
event-marker timestamps, and each command's start/end. **A log missing any one of these makes at
least one ruling-out impossible**, and nobody discovers that until a mentor is mid-fixture.

---

## 4. Open items

- Whether M5's given tests can share `SimDriver`'s shape from M4 or need their own is a Task 1
  question, not decided here. They will need `SimHooks.pauseTiming()` either way — spike G does not
  hold without it, and a test that forgets it will read frozen vision frames and pass or fail for
  reasons unrelated to the student's code.
- `AprilTagFields.k2025ReefscapeWelded` was used in the spike because it is what loaded. **The
  planner must confirm the correct 2026 field enum** before anything student-facing cites a season.
- Spec §9.6 lists `photonlib` without a version; the vendordep in `Robot-2026` is `v2026.2.1`.
  Unlike §9.5's stale `26.1.0`, this is an omission rather than a contradiction — worth pinning in
  the step 12 pass.
