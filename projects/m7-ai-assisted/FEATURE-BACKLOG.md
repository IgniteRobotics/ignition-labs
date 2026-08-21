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

**Four entries, after a second mining pass on 2026-08-21 — see the note at the end of
this section for why it shipped with one.**

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

### 2. Give the lemon-cluster centroid marker a name that actually clears it

**Where:** `src/main/java/frc/robot/subsystems/lemon_hunter/LemonHunterSubsystem.java:99`
and `:104`

`periodic()` has two branches that publish to the `Field2d` shown on the dashboard. When
no lemons are detected, it resets `lemonField.getObject("Cluser Centroid Position")` —
misspelled — to the origin. When lemons are found, it writes the real reading to
`lemonField.getObject("Cluster Centroid Position")` — spelled correctly. `Field2d.getObject`
keys each call to a distinct entry by that string, so these are two different objects, not
one being reset and then set. The result: once the robot has found and then lost a
cluster, the dashboard's centroid marker keeps showing wherever it last saw one,
indefinitely, because nothing ever writes into the object the widget is actually bound to.

Be precise about what is and is not affected. `lemonList`, `bestClusterSize`, and the
"Lemon Positions" `Field2d` object all reset correctly on the empty branch — none of them
depend on a string key matching across two call sites the way the centroid marker does.
This is a single, narrow key mismatch, not a general problem with the reset logic.

Work out what the empty case should actually publish, and why. Matching the spelling is
necessary but is not, on its own, the interesting decision: today's "reset" writes
`(0, 0)`, which — once the key mismatch stops hiding it — reads on the dashboard as a
phantom lemon sitting at the field origin, not as "no target." Whatever you land on,
structure the decision so it can be exercised as a unit test without a connected camera —
`LemonHunterConstants.lemonHunterCamera` and `periodic()`'s frame-reading are not what is
broken here, and pulling the fix into something callable and testable keeps the two apart.

**Why it is a good M7 feature:** genuinely small; the bug is real and reproducible by
inspection, not invented; it is dashboard-facing, so a driver or mentor is the one who
loses signal; and "what should this render as instead" has no single obviously-right
answer, which is exactly the kind of decision this milestone asks a student to defend.

**Where you will be pushed in defense:** why the origin, or whatever you replace it with,
is the right placeholder for "nothing detected"; why the fix is structured to be testable
without a camera rather than only provable by watching the dashboard; and what a driver
would actually infer from each version of the marker mid-match.

---

### 3. Decide what `LemonHunterSubsystem` returns when there is no cluster

**Where:** `src/main/java/frc/robot/subsystems/lemon_hunter/LemonHunterSubsystem.java:209`
(`getNearestLemonInCluster`) and `:225` (`getClusterCentroid`); the unguarded call site is
`src/main/java/frc/robot/RobotContainer.java:345`.

Both methods return `null` when `bestCluster` is empty — which is the routine state every
cycle the robot has not clustered a lemon, not a rare edge case. `RobotContainer`'s private
`getDriveToLemonRequest()` calls `hunter.getClusterCentroid(...)` and calls
`.getTranslation()` on the result on the very next line, with no null check anywhere in
between.

Be exact about what is and is not already firing. `getDriveToLemonRequest()` is not bound
to any trigger on this commit — `configureBindings()` never calls it, so this is not a bug
a driver hits today. What is real: `DriveConstants.LEMON_HUNTING_REQUEST` and
`DriveConstants.HUNT_SPEED` exist already, used nowhere except inside this same method,
which reads as work left mid-wire rather than a hypothetical. The contract gap is sitting
in checked-in, main-branch code, and it becomes a live crash the moment anyone finishes
wiring it to a control. `getNearestLemonInCluster()` has the identical return-null-on-empty
shape and no caller at all yet — the same question applies to it before it gets one.

Work out what "no cluster" should mean to a caller of each method — and pick one contract
that both methods and `getDriveToLemonRequest()`'s eventual caller can rely on
consistently, rather than patching the one call site that happens to exist today.

**Why it is a good M7 feature:** real — this is present-tense code in `main`, not a
scenario built for the exercise; small — two methods, one existing call site; useful — it
closes a crash before it ships rather than after; defensible — `Optional`, a documented
null contract enforced at the call site, a sentinel value, and "guard the call site instead
of the method" are all legitimate and not equivalent; and testable without hardware —
`bestCluster` is a public field, so a test sets it directly and calls the accessor, no
camera and no HAL involved.

**Where you will be pushed in defense:** why this contract beats the alternatives; what
still breaks if the chosen contract is "return null, caller must check" and a future caller
does not; why the fix covers both methods instead of only the one with a caller today; and
what your test asserts, and why it does not need a camera to do it.

---

### 4. Name the commands that already fire on driver input

**Where:** `src/main/java/frc/robot/RobotContainer.java:258`, `:265`, `:275`

Three `.onFalse(...)` bindings schedule a command with no `.withName()` anywhere in the
chain. Two are identical `shooter.stopFlywheelCommand().andThen(shooter.stowHood())`
sequences — one bound to `leftTrigger()` release, the other to `rightBumper()` release. The
third is `vision.stopResetPose()` alone, bound to `start()` release. The two commands
composed inside each sequence are individually named ("Stop Spinning Flywheel", "Stow
Hood"), but the composite scheduled at each binding is not, and an unnamed
`SequentialCommandGroup` reports its class name rather than anything binding-specific — so
both triggers' releases schedule something that looks identical in the command scheduler,
Shuffleboard, or a match log. There is no way to tell which binding fired from that name
alone. `vision.stopResetPose()` is a single `runOnce` with no name at the method or the
binding.

This is the same "already handled, not yet findable" problem entry 1 is built around, on a
different seam: nothing here is scheduling incorrectly. The gap is entirely in what a
mentor sees when reconstructing what happened during a match after the fact.

Work out where the name belongs and what it should say. Naming each `.andThen()` chain at
its binding site is not the same decision as naming it to distinguish the two triggers from
each other, and both are different from pushing a name onto `vision.stopResetPose()` inside
`VisionSubsystem` so it carries one everywhere it is ever bound. Any of the three closes the
gap described above; they are not interchangeable once you ask what a mentor actually needs
to be able to tell apart.

**Why it is a good M7 feature:** small; real — these bindings already run in every match;
useful — this is exactly the kind of gap that costs someone a match-log investigation later;
defensible — the naming options above are genuinely different answers, not stylistic
variants; and testable without hardware — `Command.getName()` is a plain assertion against
the object you built, no HAL and no simulation involved.

**Where you will be pushed in defense:** why your naming choice over the others; what a
mentor could tell apart from a log with the fix that they could not before; and why the fix
does or does not also touch `VisionSubsystem.stopResetPose()`'s own definition rather than
only the binding site.

---

**This backlog was under-filled on purpose rather than by accident, and it was still that
way as of 2026-08-18.** It shipped with one verified entry because a candidate that does
not survive checking is worse than no candidate — the first draft of this file led with a
feature that turned out to already be implemented, and a student would have lost a session
discovering that. D-4 (`docs/programming/UNTESTED-TARGETS.md` can be a shortlist that does
not deplete) does not solve this half of the problem: a feature, unlike a test target,
cannot be shipped by two students at once, so a short backlog here is a real scarcity, not
a cosmetic one. **That changed 2026-08-21** — a second mining pass verified three more
candidates against the same pinned commit and added them above, so the backlog is no
longer running at zero spare capacity. Restocking stays real work each time: see the
selection criteria and the caution in the next paragraph, both of which applied just as
much to this pass as to the first.

**Last mining sweep: 2026-08-21, against `Robot-2026` @
`28ce5397d88275001d656dbb717d22cf9ac0150d`** — the same commit already pinned at the top of
this file. Entries 2–4 above came from this sweep. The next person restocking should
re-clone or re-fetch at a newer commit, pin it explicitly, and note the new SHA and date
here rather than assuming this one still reflects `main`.

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
