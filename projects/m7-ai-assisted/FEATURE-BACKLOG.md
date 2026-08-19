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

`Robot-2026`'s `build.gradle` is already wired for tests (`org.junit.jupiter:junit-jupiter:5.10.1`
and `useJUnitPlatform()`, the same versions the training projects pin), and M6 pull requests
are adding coverage a class at a time — but most of the codebase still has none. If
`src/test/` does not exist yet on your branch, create it. Either way: prefer a feature
whose logic can be exercised without the HAL.

---

## Open

**One entry, and that is deliberate rather than an oversight — see the note under it.**

### 1. Give `grabVisionEstimateList()`'s swallowed exception a structured signal

**Where:** `src/main/java/frc/robot/statemachines/DriveState.java:52`

A bare `// TODO add to log` sits directly above a `catch (Exception e)` whose entire
handling is two `System.out.println` calls.

Be precise about what is and is not wrong here, because the obvious version of this
feature is already done. `Robot.java` calls `DataLogManager.start()`, and console capture
is on by default — so that `println` output **does** reach the `.wpilog`, as raw console
text, on a roboRIO. (`DataLogManager.startConsoleLog()` is guarded by
`if (RobotBase.isReal())`, so in simulation it never reaches the log at all.) The failure
is not invisible on the real robot.

What it is not is *findable*. It lands interleaved with every other line of stdout under
a single `console` key: no structured field, no count — no field sitting alongside the
vision telemetry you would be looking at when you went hunting in AdvantageScope's field
list. "Did this throw during match 4, and how often?" is a question the current
handler cannot answer without reading raw console text line by line.

Work out what should be recorded, in what form, and justify it. The TODO says that
something is missing, not what — deciding what is the interesting half, and it is the
half you will be asked to defend.

**Why it is a good M7 feature:** genuinely small; the gap is real and you can demonstrate
it; the design questions have no single right answer (a counter, a boolean, a timestamped
message, or the exception class? what does a mentor reading this after a match actually
need? what does it cost if it fires every loop?); and it sits in the vision path, where
this team's hardest debugging happens.

**Where you will be pushed in defense:** why this shape of record rather than another,
what question it lets someone answer six weeks later that they cannot answer today,
whether swallowing the exception at all is the right behaviour once it is visible, and
what happens if the same exception fires fifty times in one match.

---

**This backlog is under-filled on purpose rather than by accident, and it still is as of
2026-08-18.** It shipped with one verified entry because a candidate that does not survive
checking is worse than no candidate — the first draft of this file led with a feature that
turned out to already be implemented, and a student would have lost a session discovering
that. D-4 (`docs/programming/UNTESTED-TARGETS.md` can be a shortlist that does not deplete)
does not solve this half of the problem: a feature, unlike a test target, cannot be shipped
by two students at once, so a short backlog here is a real scarcity, not a cosmetic one.

**A mentor must add entries before assigning M7 to a cohort.** The selection criteria
above are the tool for that, and they matter more than any entry under them. Verify every
claim against the repository at the SHA you are pinning before you write it down — and
note that "this value is not being logged" is the single easiest claim to get wrong here,
because WPILib publishes through at least two mechanisms that do not look like logging
calls: Epilogue's `@Logged` annotations, which turn a plain private field into a
NetworkTables entry, and `DataLogManager`'s console capture, which is enabled by default
and tails stdout into the `.wpilog`. Both drafts of this file's first entry were wrong for
exactly that reason, one for each mechanism.

## Done

*(Move entries here with the pull request number and the student's name as they ship.
Keeping them is not bookkeeping — it is how the next student sees what M7-sized actually
looked like.)*
