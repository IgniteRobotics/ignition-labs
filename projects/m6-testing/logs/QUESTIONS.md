# Diagnose it from the log

Two real captures from the 2026 season. Open them in AdvantageScope.

You get one match. It lasts two and a half minutes. You cannot re-run it, and nobody
filmed the part you needed. The log is all you have — which is why this is a skill and
not a formality.

## The rules

**Show the evidence, not just the answer.** "The shooter was below setpoint" is not an
answer. "Between t=<x> and t=<y> the measured flywheel velocity sat <n> rps under the
commanded value, and here is the plot" is an answer.

**Rule out the alternative.** For each question there is more than one plausible
explanation. Say what each one is and name the signal that rules it out. An answer that
happens to be right without ruling anything out does not pass — a mentor cannot tell it
apart from a guess.

## Question 1

Open `PHX_20260304_085129_flywheel-signals.wpilog`. This one did not come from a match —
it is a Phoenix signal log (CTRE calls the raw format a "hoot"), converted to `.wpilog` so
it opens the same way in AdvantageScope. It carries CAN-level telemetry for the two
flywheel `TalonFX` controllers (device IDs 5 and 6) instead of NetworkTables entries: look
under `Phoenix6/`.

Somewhere in the first 15 seconds, the flywheel leader is commanded to a new velocity, and
the command holds steady for roughly half a minute before the robot disables.

**Does the flywheel's measured velocity ever get anywhere near the commanded value? If it
doesn't, what's actually going on?**

More than one explanation is plausible here — do not stop at the first one that occurs to
you:

- Is the velocity controller badly tuned?
- Is the motor dead or disconnected?
- Are you reading telemetry from the wrong device?
- Does the log simply end before the flywheel would have caught up?

For every one of those you rule out, name the specific signal (and, where useful, a second
device's matching signal) that rules it out, and give the timestamp and value. Whatever
you conclude, say what it would take to find out for certain on the real robot.

## Question 2

Open `FRC_20260305_233032.wpilog`. Somewhere in this log the launch distance the robot was
aiming for and the distance it was actually at are not the same number.

**Find that, and explain why the two differ.**

This one is not a bug. Something in the code is doing it on purpose, and the sandbox class
you have been writing tests against does the same thing. Say what and why.

## How this is graded

A mentor asks you to walk through both on screen. Bring the plots. Expect to be asked "how
do you know it wasn't <the other thing>" — that question is the whole exercise.
