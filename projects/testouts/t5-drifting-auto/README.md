# T5 — Test-Out

**Time: 60 minutes.** Track D.

This project has no code for you to edit. Your mentor will tell you which file
under `logs/` to open — `a-run.wpilog`, `b-run.wpilog`, or `c-run.wpilog`. It is
a recording of one autonomous routine, driven by one real (simulated) run, that
drifts off its intended path partway through. Your job is to work out why.

Open your assigned log in **AdvantageScope** or **DataLogTool** (both ship with
WPILib — `~/wpilib/2026/tools/`). Every log carries the same five signal
streams:

| Entry | What it is |
|---|---|
| `/t5/truePose` | The robot's actual field position and heading |
| `/t5/estimatedPose` | What the robot's own pose estimator believed |
| `/t5/pathTargetPose` | Where the path-following controller was aiming, over time |
| `/t5/moduleStates` | Each swerve module's commanded velocity and angle |
| `/t5/events` | Timestamped markers: when the path phase and each of the two named commands (`step2`, `step3`) started and ended |

---

## The three candidates

Exactly one of these is what happened in your log:

1. **Odometry drift** — the robot's pose estimator lost track of where the
   robot actually was.
2. **A mistuned path-following controller** — the controller steering the
   robot toward the path was not aggressive enough to keep up.
3. **A named command that blocked longer than expected** — one of the two
   named commands (`step2` or `step3`) did not finish when it should have.

## What you're doing for the next 60 minutes

1. Open your assigned log. Look at all five signal categories above, not just
   the ones that seem obviously relevant — a cause you can rule out is as much
   evidence as a cause you can confirm.
2. Work out **which of the three candidates actually happened.**
3. Work out **what specifically in the log rules out the other two.** Not "I
   don't think it's that one" — point at a signal and say what it does or
   doesn't show.

## How you pass

Say both of the following out loud, to a mentor:

1. **Which of the three candidates it was**, and the specific evidence that
   supports it.
2. **What rules out each of the other two.** A correct diagnosis without this
   half has not passed — guessing right is not the same as showing why the
   other two don't fit.

Nothing here is graded by a test; there is nothing to run and nothing to turn
green. The three questions above, answered correctly and with evidence, are
the entire pass criterion.

If 60 minutes runs out without a confident answer, that is not a failure of
anything — it means log-reading under time pressure is where you start, which
is exactly what this fixture exists to find out.
