# M5 — Autonomous, Odometry & Vision

## At a glance

Where the robot is on the field, and what it does for fifteen seconds without a driver.

Required if you own autonomous or vision during a season.

**Estimated time:** 4–6 sessions
**Prerequisites:** M4
**AI posture:** 🟡 Yellow

## Who enters here

**Tracks A, B, C** arrive here having worked M4.

**Track D** tests out. This is the last milestone Track D tests out of.

## Test-out challenge

You are given an autonomous routine that drifts off its path partway through. Determine from the logs whether the cause is odometry drift, a mistuned path-following controller, or a named command blocking longer than expected — and say what evidence rules out the other two.

**Pass criteria:** correct diagnosis *and* the ruling-out. A student who guesses right without evidence has not passed. The skill being tested is log-driven diagnosis, not pattern matching.

**Time:** 60 minutes.

## Concepts

### Odometry

Odometry answers "where am I?" by accumulating small movements. Every 20ms, read how far each wheel turned and which way the gyro says you are facing, work out how much the robot moved, and add it to your estimate.

**Kinematics** is the geometry converting wheel states to chassis motion and back. For swerve: four modules each with a speed and an angle, combined into a chassis velocity (forward, sideways, rotational) — and, run backwards, a chassis velocity split into four module commands.

**Odometry drifts, always.** Wheels slip. Encoders quantize. Small errors accumulate and never self-correct. After a match's worth of driving, pure wheel odometry can be off by a meter. That is not a bug to fix; it is the nature of dead reckoning, and it is the entire reason vision exists in M5.

**Pose** is position plus heading — `Pose2d`. Field-relative, measured from a fixed field origin.

**Alliance matters.** The field origin convention means a blue-alliance path is not a red-alliance path. Getting this wrong produces an autonomous that works perfectly in practice and drives into a wall at competition. Ignite has an `AllianceState` for exactly this reason.

### Pose estimation

A `PoseEstimator` fuses wheel odometry with vision, using a Kalman filter. You tell it how much to trust each source via standard deviations.

**The intuition:** wheel odometry is smooth but drifts. Vision is drift-free but noisy and intermittent. The filter uses odometry for continuity between vision updates, and vision to pull the estimate back to truth whenever a tag is visible.

**Timestamps are critical.** A vision measurement describes where the robot was when the frame was captured, not when the code received it — and that gap is tens of milliseconds during which the robot moved. Vision measurements must be submitted with their capture timestamp. Getting this wrong makes vision actively harmful: the estimate jitters and fights itself.

Ignite's `vision-subsystem.md` in this repo documents the real pipeline: three PhotonVision cameras feeding pose estimates through clustering and fusion every loop cycle.

### AprilTags

Printed fiducial markers at known field locations. A camera that sees one can compute where the camera is relative to the tag; since the tag's field position is known, that yields the robot's field position.

**What degrades them:** distance (error grows fast), viewing angle (sharp angles are unreliable), motion blur, and ambiguity — a single tag viewed head-on has two mathematically valid solutions. Two or more tags resolve it.

**Practical filtering.** Reject measurements that are too far, too ambiguous, or that place the robot outside the field. Trust multi-tag results far more than single-tag. Scale your standard deviations with distance rather than trusting everything equally.

### PhotonVision

Ignite's vision stack — `photonlib` vendordep, `VisionSubsystem.java`, running on Orange Pi 5 coprocessors with Arducam OV9281 cameras. `docs/VISION_GUIDE.md` in this repo covers the actual hardware setup.

Vision runs on a **coprocessor**, not the roboRIO. It does the image processing and sends results over NetworkTables. Your robot code consumes results; it does not process images.

**Camera calibration is not optional.** An uncalibrated camera produces confidently wrong pose estimates. Calibration is tedious and it is the difference between vision helping and hurting.

**Limelight** is the main alternative — paid hardware, simpler out of the box, less configurable.

### PathPlanner

Ignite's autonomous tooling. You draw paths in a GUI, the robot follows them.

```java
AutoBuilder.buildAuto("Left Double Half Sweep");
NamedCommands.registerCommand("shoot", shooter.launchCommand());
```

**Named commands** are the bridge: you place a marker on a path in the GUI, register a command under that name in code, and the routine triggers it at that point.

**Choreo** is the alternative — optimizes trajectories against your robot's real physical constraints, producing more aggressive paths.

**Do not agonize over this choice.** The community consensus is unambiguous and it is not about capability: "use the software you have the most experience with," because "ramp-up time is going to be the limiting factor, not the software itself." One team that switched to Choreo reported it was easy but went back to PathPlanner because it was easier to develop on.

**What matters is that you understand the concepts underneath** — trajectory following, holonomic drive, feedforward on a path — because those transfer. The tool does not.

This is also exactly the kind of decision to settle in the fall. Deciding it cold in Week 1 costs real days.

### Building an autonomous routine

An autonomous routine is a command composition. That is all it is — M3's compositions applied to paths and mechanisms.

```java
Commands.sequence(
    shooter.launchCommand(),
    AutoBuilder.followPath(pathToPiece),
    intake.collectCommand().withTimeout(2.0),
    AutoBuilder.followPath(pathBack),
    shooter.launchCommand());
```

**Design for failure.** Autonomous runs with no human to notice something went wrong. If the intake misses, the routine should still do something reasonable rather than hanging forever. `withTimeout()` on anything that could block is not optional.

**A chooser** on the dashboard lets the drive team pick a routine before a match. Ignite uses `SendableChooser`.

## Project

**The skeleton is [`projects/m5-auto-vision/`](https://github.com/IgniteRobotics/ignition-labs/tree/main/projects/m5-auto-vision).** A simulated swerve drivetrain, a PhotonVision `VisionSystemSim` with configurable noise and latency, and a scoring mechanism you can make fail are given. Composing the routine and fusing vision into the estimate is the milestone.

Path following in the training project runs against the drivetrain's **true** pose, not the pose estimator you write in Part 2. On a real robot — including `Robot-2026` — the follower runs against the estimate. The training project differs so that Part 1 works before Part 2 exists, and so a routine does not fail because your estimator is still a stub. If you notice the difference and it bothers you, that is the right instinct; do not copy this wiring into a season repository.

**Two parts, both in simulation.**

**Part 1 — an autonomous routine.**
1. Build a path in PathPlanner
2. Register at least one named command
3. Compose: follow a path, trigger the mechanism, follow a second path
4. Add timeouts so no step can hang
5. Add it to a `SendableChooser`
6. Run in simulation and watch it in AdvantageScope
7. Deliberately make one step fail — the given `ScoringMechanism` has `setFaulted(true)`, which stops it ever reporting deployed. Confirm the routine still terminates

**Part 2 — vision-corrected odometry.**
1. Start with wheel odometry only. Drive around in sim and watch the estimate drift from truth
2. Add simulated AprilTag measurements to a pose estimator
3. Show the estimate being pulled back
4. Submit a vision measurement with a deliberately wrong timestamp. Watch it get worse. Understand why
5. Add distance-based filtering and show the improvement

**Part 2 step 4 is the important one.** Everyone learns that vision helps. Far fewer learn that vision applied wrongly is worse than no vision.

## Definition of done

- [ ] Auto routine completes in simulation and is visible in AdvantageScope
- [ ] It terminates safely when a step is made to fail
- [ ] A named command triggers at the right point on the path
- [ ] Routine is selectable from a chooser
- [ ] You can explain what odometry drift is and why vision corrects it
- [ ] You demonstrated vision *hurting* the estimate via a bad timestamp, and can explain why
- [ ] You can explain why multi-tag beats single-tag
- [ ] You can articulate one real tradeoff between PathPlanner and Choreo
- [ ] You can explain why a blue path is not a red path

## Common failure modes

**Untimestamped vision measurements.** Or timestamped on arrival rather than capture. Produces jitter that looks like a tuning problem and is not.

**Trusting vision equally at all distances.** A tag at six meters is far worse than one at two. Scale your standard deviations.

**Not handling alliance flipping.** Works all through build season, fails at the event.

**No timeouts.** One mechanism misses, one step never finishes, the robot sits still for the rest of autonomous.

**Tuning path following before drivetrain control.** M4 is a prerequisite for a reason. If velocity control is bad, path following is hopeless and you will tune the wrong layer for hours.

**Uncalibrated cameras.** Confidently wrong pose estimates, which are worse than none.

**Paths built for a robot that does not exist yet.** In-season, mechanical changes invalidate assumptions. Keep paths easy to regenerate.

**Only testing on a full battery on a clean floor.** Autonomous is where the robot is least able to compensate for surprises.

## AI posture — 🟡 Yellow

**Allowed:** explaining the PhotonVision or PathPlanner API · Javadoc · triaging a stack trace from a failing path command · boilerplate

**Not allowed:** structuring your autonomous routine, choosing standard deviations, designing your filtering strategy. Routine structure is a decision, and it is what M5 teaches.

Standard deviations in particular are a tuning problem specific to your cameras, your mounting, and your field — the same category as gains in M4. An AI will produce plausible numbers that mean nothing.

## Resources

See `resources.md` for the full library. For this milestone:

- **WPILib: Kinematics and Odometry** — the geometry underneath everything here
- **PathPlanner** — Ignite's actual autonomous tooling
- **Choreo** — the alternative, for comparison
- **PhotonVision** — Ignite's actual vision stack
- **Limelight** — the alternative
- **AdvantageScope** — how you will actually see what your routine did
- **Chief Delphi: Limelight vs PhotonVision** and **Choreo vs PathPlanner** — read these before arguing about tooling
- **Ignite:** `docs/VISION_GUIDE.md` — real setup for our actual cameras and coprocessors
- **Ignite:** `docs/vision-subsystem.md` — the multi-camera fusion pipeline in prose
- **Ignite:** `Robot-2026/.../vision/VisionSubsystem.java` — the implementation
- **Ignite:** `Robot-2026/.../statemachines/LaunchCalculator.java` — pose feeding a real shooting solution
