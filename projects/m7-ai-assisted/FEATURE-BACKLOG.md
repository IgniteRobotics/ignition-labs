# M7 feature backlog

**Mentor-maintained.** Entries get used up — a student ships one and it leaves this
list. The selection criteria below matter more than any entry under them, because they
are what a mentor uses to add the next one.

Pinned to `Robot-2026` at commit `28ce5397d88275001d656dbb717d22cf9ac0150d`. Line
numbers and details were verified at that commit; the repository has moved since, so
confirm before assigning.

---

## What makes a feature M7-sized

A good M7 feature is **small, real, useful, and defensible**. All four.

- **Small** — one or two sessions. If it needs a design discussion, it is not this.
- **Real** — someone actually wants it. Not an exercise invented to be an exercise.
- **Useful** — the robot or the team is better with it. "Refactor X for clarity" is
  rarely useful enough to defend under questioning.
- **Defensible** — the student must be able to answer *why this way?* for every decision
  in the diff. A feature whose correctness rests on a number nobody can justify is a bad
  M7 feature no matter how small it is.

And one hard filter:

- **Testable without hardware.** M7's done list requires tests that pass and cover the
  new behaviour. A feature that can only be verified on a real robot cannot satisfy that
  in a session.

**Tuning tasks are not M7 features.** `Robot-2026` carries several `TODO: tune` markers —
`IndexerConstants.java`, `ShooterConstants.java`, `VisionConstants.java`. Tuning is M4's
lesson, it needs hardware, and there is no diff to defend at the end of it. Do not put
them here.

## A note before you start

`Robot-2026` has **no tests at all** — there is no `src/test/` directory. Its
`build.gradle` is already wired for them (`org.junit.jupiter:junit-jupiter:5.10.1` and
`useJUnitPlatform()`, the same versions the training projects pin), so nothing is
blocking you, but your feature's test may well be the first test in the repository. Plan
for that: prefer a feature whose logic can be exercised without the HAL, and expect to
create `src/test/java/frc/robot/...` yourself.

---

## Open

### 1. Publish `VisionSubsystem`'s rejection counters to telemetry

**Where:** `src/main/java/frc/robot/subsystems/vision/VisionSubsystem.java`

`VisionSubsystem` keeps twelve counters — accepted, rejected-total,
rejected-due-to-ambiguity, and rejected-due-to-jumping, for each of the front, left, and
right cameras. Their Javadoc says they are *"Used for telemetry"* and *"Tracks telemetry
for monitoring."*

**Nothing publishes them.** They are `private`, they have no accessors, and no other file
in the repository reads them. They are incremented every loop and thrown away.

That means nobody can answer "is the vision filter rejecting anything, and why" without
attaching a debugger. Which camera is dropping frames, and for which reason, is exactly
the question you want answered from a log after a match rather than guessed at in the
pit.

**Why it is a good M7 feature:** small and self-contained; obviously useful; the design
questions are real but answerable (raw counts or rates? per-camera or aggregate? what
happens to the numbers when the robot is disabled?); and it is testable without hardware,
because you can drive the counters through the subsystem's own accept and reject paths.

**Where you will be pushed in defense:** why these names, why this grouping, what a
reviewer reading the log six weeks from now would need, and what the numbers mean if
nobody ever resets them.

*If you are doing M5 or have just finished it, you have already read this file — it is
what `READING.md` sends you to. You are improving code you have studied.*

### 2. Log the drive state that is currently marked unlogged

**Where:** `src/main/java/frc/robot/statemachines/DriveState.java:52`

A bare `// TODO add to log` sits on state that never reaches the log. Work out what
should be logged there, in what form, and justify it — the TODO says *that* something is
missing, not *what*, and deciding what is the interesting half.

**Where you will be pushed in defense:** why this field and not its neighbours, what
question the log entry lets someone answer later, and what it costs every loop.

---

## Done

*(Move entries here with the pull request number and the student's name as they ship.
Keeping them is not bookkeeping — it is how the next student sees what M7-sized actually
looked like.)*
