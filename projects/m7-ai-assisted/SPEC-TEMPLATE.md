# Spec template — one worked example

This is a complete spec for a real feature, at the size a spec should be. **Copy the
shape, not the subject.** The feature below is backlog entry 1, and writing it out here
spends it — it is not available to pick. Yours will be one of the others.

Note the length. Six scenarios, one screen. That is the target, not the floor.

---

# Worked example: structured signal for `grabVisionEstimateList()`'s swallowed exception

## Why

`DriveState.grabVisionEstimateList()` catches every exception from draining the vision
queue and handles it with two `System.out.println` calls under a `// TODO add to log`.
That output does reach the `.wpilog` on a real robot — `DataLogManager`'s console capture
is on by default — but it lands as raw text interleaved with all other stdout under one
`console` key, and console capture is skipped entirely in simulation. "Did this throw
during match 4, and how often?" cannot be answered without reading console text line by
line, six weeks after the match.

## What changes

A mentor opening the log sees two new fields next to the vision telemetry: a count of how
many times this handler has fired since the robot booted, and the class name of the most
recent exception. Nothing about when the exception is caught, what the method returns, or
what the drivetrain does with the result changes.

## Approach

Publish both values as Epilogue-logged fields on `DriveState`, which `RobotContainer`
already holds — though `DriveState` carries no `@Logged` of its own yet, so annotating it is
part of this change. Epilogue is the mechanism `Robot-2026` uses for telemetry, and
`coding-standards.md` (**Draft, not ratified**) names Epilogue for subsystems and telemetry
under Core Libraries, so this adds no new logging path.

**Rejected: `DriverStation.reportWarning` on each catch.** It is the obvious answer, it is
what the coding standards' Fault detection example reaches for, and it is wrong here. That
example guards a hardware fault that is checked once per `periodic()`; this handler sits in
the vision path and can fire on every loop. Fifty identical warnings in one match buries the
Driver Station in exactly the noise this feature exists to remove, and DS messages are not a
field you can plot or filter later. A count is queryable and costs one number no matter how
often it fires.

Both fields are `camelCase`, per `coding-standards.md`'s naming conventions.

## Scenarios

- **WHEN** the queue drains without throwing **THEN** the count is unchanged and the method
  returns the same list it returns today
- **WHEN** the catch block runs once **THEN** the count increases by exactly one for that
  call, regardless of how many entries were still pending
- **WHEN** it throws a second time in the same robot session **THEN** the count reads 2 —
  it accumulates for the session and is not reset per loop
- **WHEN** an exception is caught **THEN** the recorded class name is that exception's
  simple name, published as its own field rather than inside the console stream
- **WHEN** it throws on every loop for ten seconds **THEN** the count is the only thing that
  grows; no per-occurrence output is added beyond what already prints
- **WHEN** the code runs in simulation **THEN** both fields appear in the log, which the
  current `println` output does not

## Out of scope

- **Whether the exception should be swallowed at all.** Making the failure visible and
  changing what happens on failure are two decisions. This is the first one
- Removing or rewording the existing `println` calls
- The vision pipeline, the queue's threading, and `addVisionEstimate()`
- Any AdvantageScope layout or dashboard configuration

## Tasks

- [ ] Add the two logged fields to `DriveState` and update them in the catch block
- [ ] Confirm both appear in the log in simulation
- [ ] Test: one forced failure increments the count by one
- [ ] Test: repeated failures accumulate rather than reset
- [ ] Test: the recorded class name matches the exception thrown
- [ ] Commit the spec before the first implementation commit

---

## How to use this

Your spec goes in `Robot-2026`, one folder per change:
`openspec/changes/<your-feature>/proposal.md`. The `openspec/` folder is already there —
a mentor set it up — so you are adding a folder, not installing anything. Nothing in this
training repository runs OpenSpec, and the CLI is optional.

**Commit the spec before your first implementation commit.** The commit timestamps are how
that item on the done list is checked, which is why it is about ordering and not existence.

**You write this, not the agent.** The spec is Yellow-tier work even though M7 is Green:
you may hand your finished spec to an agent and ask *"here is my spec — what did I miss?"*,
and that is the best prompt in this milestone. You may not ask it to write the spec. The
spec is the part that constrains the tool; a spec the tool wrote constrains nothing.

Hand the agent both this and `coding-standards.md`. One is the decisions for this change;
the other is the decisions that hold across every change.

## How big a spec should be

**A two-line fix does not get a spec.** Fix it, test it, open the PR. A feature that does
need one gets a spec this size — not three times it. Sixteen acceptance criteria for a
two-file change is a session spent on a document nobody will read.

The cheapest way to keep a spec short is the **Out of scope** heading. Most
over-specification is a spec that never said what it was *not* doing, so every reviewer
question got answered by adding another criterion. Answer them once, in four bullets.
