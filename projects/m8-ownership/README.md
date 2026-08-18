# M8 — In-Season Ownership

**No skeleton, no code, no tests.** M8 is what happens after the curriculum ends and the
season starts: you own a subsystem, on the real robot, for real matches.

The whole milestone is deliverables. This directory points at them.

---

## The templates

Real files, in [`templates/`](../../templates/) at the repository root. Copy them — do
not retype them, and do not write your own from memory.

| Template | When |
|---|---|
| `subsystem-readme.md` | Week 1, then updated whenever the subsystem changes |
| `tuning-log.md` | Every tuning session, while you are tuning |
| `pr-description.md` | Every pull request |
| `competition-week-checklist.md` | Before, during, and after each event |
| `pit-documentation.md` | Before the first event |
| `post-season-retro.md` | End of season |

Each one is explained, with the reasoning behind it, in
[`student-deliverables.md`](../../docs/programming/student-deliverables.md). Read the
explanation once; copy the file every time.

## The cadence

Adapted from Team 937's published schedule. A reference, not a mandate — but the
**ordering** is the part that matters.

| Week | Programming deliverable |
|---|---|
| 1 | Subsystems and simple commands for every mechanism and the drivetrain. Minimum functional bot |
| 2 | Basic autonomous |
| 3 | Tune game-piece mechanisms |
| 4 | Advanced autonomous |
| 5 | Tune endgame mechanism |
| 6 | Stress testing, driver practice, pit documentation |
| After | Iterate |

**Make it work, then make it good.** Control code and basic auto come before tuning. A
subsystem tuned before it is finished gets tuned twice.

## Week 0 and 1, when there is no robot yet

There is no such thing as a blocked programmer in Week 1.

1. **Start simulation-based subsystem logic as soon as the design is settled.** You do
   not need hardware to write and test a subsystem's logic — that is what M6 was for
2. **Work against last year's robot.** It drives, it has mechanisms, and it is sitting
   right there

## Definition of done

M8's done list is in
[the milestone document](../../docs/programming/milestones/m8-in-season-deliverables.md).
Everything on it is mentor-verified across a season; there is nothing here to run.
