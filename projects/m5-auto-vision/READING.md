# Reading — three files from `Robot-2026`

Not a whole-repository read — three files that this milestone was designed against, at the
exact commit this project cites everywhere else.

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

## `src/main/java/frc/robot/subsystems/vision/VisionSubsystem.java`

Read this first. It is the real version of what your `acceptsMeasurement()`/
`addVisionMeasurement()` are a simplified model of — this file runs three cameras, not
one, and does considerably more than this milestone asks you to build, but the two ideas
underneath are the same ones.

Find:

1. The ambiguity-based target selection — search for `lowestAmbiguityScore` and
   `MAXIMUM_ALLOWED_AMBIGUITY`. A single AprilTag viewed near head-on has two
   mathematically valid pose solutions; this is the real robot's version of "reject an
   untrustworthy candidate" that your `acceptsMeasurement()` builds a much smaller version
   of.
2. The distance-scaled standard deviations — search for `averageRobotToTagDistance` and
   the comment describing the formula as `coefficient × distance^1.2 / tag_count`. Say, in
   one sentence, why trust should fall off with distance rather than staying constant.
3. `frontCameraResultsRejectedDueToJumping` and its neighbours — separate rejection
   counters, one per camera, one per reason. This is a real team's answer to "how do I know
   my filter is actually filtering anything," logged rather than silent.

Note what the formula divides by: `tag_count`. Seeing two tags does not merely give you
another opinion, it makes the estimate itself tighter — which is the whole of the
"why multi-tag beats single-tag" question your mentor will ask.

## `src/main/java/frc/robot/subsystems/vision/VisionConstants.java`

Find `MAXIMUM_ALLOWED_AMBIGUITY = 0.25`, `XY_STD_DEV_COEFFICIENT = 0.01`, and
`THETA_STD_DEV_COEFFICIENT = 0.03`. These are real, tuned numbers for a real set of cameras
at a real mounting position — and they are exactly the shape of number this project's
`Constants.java` deliberately does not give you an equivalent of. Choosing your own
distance threshold in `RobotContainer` and being able to defend it is the milestone; this
file is what "defend it" looks like when a team has actually done the tuning.

Notice that the heading coefficient is three times the translation one. That is a
statement about which the cameras measure worse, and it was arrived at by tuning, not by
derivation.

## `src/main/java/frc/robot/statemachines/LaunchCalculator.java`

A real robot's use of a fused pose estimate for something with a right answer: computing a
launcher's target angle and rotational velocity from the current drive state's pose. Find
the `phaseDelay` field and the loop that iterates a `lookaheadPose` against a
time-of-flight table. You do not need to reproduce this — read it as one concrete answer
to "why does it matter that the pose estimate I built in Part 2 is accurate," rather than
an abstract claim.

---

## Questions to answer

A mentor will ask. "I read it" is not an answer to any of them.

1. `VisionSubsystem` tracks a `frontCameraResultsRejectedDueToAmbiguity` counter separately
   from `frontCameraResultsRejectedDueToJumping`. Why might a team want to know *which*
   reason a measurement was rejected for, rather than just a single rejected/accepted
   count?
2. The distance-scaling formula raises distance to the power `1.2`, not `1.0` or `2.0`.
   You don't need to derive why `1.2` specifically — say what raising *any* distance term
   to a power greater than 1 does to trust at long range compared to scaling it linearly.
3. `LaunchCalculator` computes a `lookaheadPose` from the robot's current field-relative
   velocity before aiming. If the pose estimate feeding it were built from odometry alone,
   with no vision correction, what would that do to a shot taken after the robot had been
   driving for thirty seconds?

---

## Then, and only then

Start `RobotContainer.java`. `README.md` has the order.
