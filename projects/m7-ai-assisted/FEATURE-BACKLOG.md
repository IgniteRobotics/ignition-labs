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

**One entry, and that is deliberate rather than an oversight — see the note under it.**

### 1. Log the exception `grabVisionEstimateList()` currently swallows

**Where:** `src/main/java/frc/robot/statemachines/DriveState.java:52`

A bare `// TODO add to log` sits directly above a `catch (Exception e)` whose entire
handling is two `System.out.println` calls. On a roboRIO, `System.out` reaches the
console — it does not reach the `.wpilog`. So if that queue-draining loop ever throws
during a match, the vision measurements for that cycle vanish and there is **no record
anywhere a mentor can find afterwards.** The symptom is "vision looked flaky in match 4"
and the evidence does not exist.

Work out what should be recorded, in what form, and justify it. The TODO says that
something is missing, not what — deciding what is the interesting half, and it is the
half you will be asked to defend.

**Why it is a good M7 feature:** genuinely small; the failure it makes visible is real
rather than hypothetical; the design questions have no single right answer (what does a
mentor reading this after a match actually need? does an exception here mean one bad
cycle or a broken camera? what does the handler cost if it fires every loop?); and it
sits in the vision path, which is where this team's hardest debugging happens.

**Where you will be pushed in defense:** why this shape of record and not another, what
question it lets someone answer six weeks later, whether swallowing the exception at all
is the right behaviour once it is visible, and what happens if the same exception fires
fifty times in one match.

---

**This backlog is under-filled on purpose rather than by accident.** It shipped with one
verified entry because a candidate that does not survive checking is worse than no
candidate — the first draft of this file led with a feature that turned out to already
be implemented, and a student would have lost a session discovering that.

**A mentor must add entries before assigning M7 to a cohort.** The selection criteria
above are the tool for that, and they matter more than any entry under them. Verify every
claim against the repository at the SHA you are pinning before you write it down —
specifically, check every way a value could already be reaching telemetry, including
WPILib Epilogue's `@Logged` annotations, which do not look like logging calls.

## Done

*(Move entries here with the pull request number and the student's name as they ship.
Keeping them is not bookkeeping — it is how the next student sees what M7-sized actually
looked like.)*
