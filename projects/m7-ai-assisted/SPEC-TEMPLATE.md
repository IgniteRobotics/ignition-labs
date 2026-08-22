# Spec template — one worked OpenSpec change

**Ignite uses OpenSpec.** Not "a spec format." OpenSpec, with its folders, its file
names, and its requirement syntax — because a spec everyone writes differently is a spec
nobody can review quickly, and because `openspec validate` can check a real one and cannot
check a paragraph in a pull request.

This file walks one complete change, end to end, in the exact shape yours takes. **Copy the
shape, not the subject.** The feature below is backlog entry 1, and writing it out here
spends it — it is not available to pick. Yours will be one of the others.

---

## What you are making

One folder on your branch in `Robot-2026`:

```
openspec/
├── specs/                                  settled behaviour. You read this, rarely write it
└── changes/
    └── add-vision-exception-signal/        ← your change. One folder, one feature
        ├── proposal.md                     why, what, and the approach you chose
        ├── tasks.md                        the checklist you work through
        └── specs/
            └── vision-telemetry/
                └── spec.md                 the delta: what this change adds to that capability
```

**The word "delta" is the whole idea.** `Robot-2026` is forty-odd files you did not write.
You are not describing the system — you are describing the difference your change makes to
it. `openspec/specs/` holds what is already settled; your `specs/` folder holds only what
you are adding, changing, or removing. That is why the folder is called `changes` and why
your spec fits on a screen instead of running to forty pages.

`design.md` is a fourth file OpenSpec supports and you almost certainly do not need. Add it
only when *how* you are building something needs an argument that would swamp the proposal.

---

## The commands, in order

```bash
# once per machine
npm install -g @fission-ai/openspec@latest

# on your branch in Robot-2026
openspec new change add-vision-exception-signal   # creates the folder. You write the files
# ... you write proposal.md, tasks.md, and the delta spec ...
openspec validate add-vision-exception-signal     # must pass before you commit
git add openspec/changes/add-vision-exception-signal
git commit -m "Spec: structured signal for the swallowed vision exception"
# ... now, and only now, you start writing Java ...
```

Useful while you work: `openspec list` shows every change in flight, `openspec show
add-vision-exception-signal` prints yours, and `openspec view` opens a dashboard.
After your pull request merges, a mentor runs `openspec archive
add-vision-exception-signal`, which folds your delta into `openspec/specs/` and moves the
folder to `changes/archive/`.

### The one command you do not run

OpenSpec ships a skill called `openspec-propose`, and it works: ask your agent to propose a
change and it reads the code and drafts your proposal, your delta spec, and your tasks in one
step. **Do not use it, and do not use `openspec-apply-change` to write the spec for you.**

This is not us being precious about tooling. `ai-use-standard.md` §4 is explicit that the
spec is Yellow-tier work even at Green, and the reason is the whole point of this
milestone: the spec is where *your* decisions live. An agent that writes your spec has
made the decisions and left you the typing, and then the defense review is you defending
a document you did not think through. You already lived this at M6, where you wrote the
tests that unlocked Green rather than generating them.

What you **should** do, once your draft is written: hand it to the agent and ask *"here is
my spec — what did I miss?"* That is the single best prompt in this milestone. Critique,
not authorship.

---

# Worked example

## `proposal.md`

```markdown
# Structured signal for the swallowed vision exception

## Why

`DriveState.grabVisionEstimateList()` catches every exception from draining the vision
queue and handles it with two `System.out.println` calls under a `// TODO add to log`.
That output does reach the `.wpilog` on a real robot — `DataLogManager`'s console capture
is on by default — but it lands as raw text interleaved with all other stdout under one
`console` key, and console capture is skipped entirely in simulation.

"Did this throw during match 4, and how often?" cannot be answered today without reading
console text line by line, six weeks after the match.

## What changes

A mentor opening the log sees two new fields beside the vision telemetry: how many times
this handler has caught an exception since boot, and the class name of the most recent
one. Nothing about when the exception is caught, what the method returns, or what the
drivetrain does with the result changes.

## Approach

Publish both values as Epilogue-logged fields on `DriveState`. `RobotContainer` already
holds it, though `DriveState` carries no `@Logged` of its own yet, so annotating it is
part of this change. Epilogue is the mechanism `Robot-2026` already uses for telemetry and
`coding-standards.md` names it for subsystems and telemetry, so this adds no new logging
path.

**Rejected: `DriverStation.reportWarning` on each catch.** It is the obvious answer and it
is wrong here. The coding standards' fault-detection example guards a hardware fault
checked once per `periodic()`; this handler sits in the vision path and can fire every
loop. Fifty identical warnings in one match buries the Driver Station in exactly the noise
this change exists to remove, and DS messages are not a field you can plot or filter
later. A count is queryable and costs one number no matter how often it fires.

## Out of scope

- **Whether the exception should be swallowed at all.** Making a failure visible and
  changing what happens on failure are two decisions. This is the first one
- Removing or rewording the existing `println` calls
- The vision pipeline, the queue's threading, and `addVisionEstimate()`
- Any AdvantageScope layout or dashboard configuration
```

## `specs/vision-telemetry/spec.md` — the delta

This is the file `openspec validate` actually checks, and the heading levels are not
decorative. `## ADDED Requirements`, then `### Requirement:`, then `#### Scenario:`.

```markdown
## ADDED Requirements

### Requirement: Vision exception occurrence count
The system SHALL record the number of times the vision-estimate handler has caught an
exception since robot boot, as a telemetry field published alongside the vision data.

#### Scenario: Queue drains without throwing
- GIVEN the vision estimate queue drains cleanly
- WHEN the handler completes
- THEN the recorded count is unchanged
- AND the method returns the same list it returns today

#### Scenario: A single caught exception
- GIVEN the recorded count reads N
- WHEN the handler catches one exception
- THEN the recorded count reads N plus one, regardless of how many entries were pending

#### Scenario: Repeated failures accumulate
- GIVEN the handler has already caught an exception this session
- WHEN it catches a second one
- THEN the recorded count reads two, having not reset between loops

#### Scenario: Running in simulation
- GIVEN the code is running in simulation, where console capture is disabled
- WHEN the handler catches an exception
- THEN the recorded count appears in the log anyway

### Requirement: Most recent vision exception type
The system SHALL record the simple class name of the most recently caught vision-estimate
exception, as its own telemetry field.

#### Scenario: An exception is caught
- GIVEN the handler catches an exception
- WHEN the log is read afterwards
- THEN the recorded class name is that exception's simple name, in its own field rather
  than inside the console stream
```

**Note there are two requirements, not one.** "Record a count and the class name" has an
*and also* in it, and OpenSpec's rule is one behaviour per requirement — if you can split
it, it was two. This is the rule people break most often, and splitting is what makes each
one independently testable.

**Note also what is not here.** No mention of Epilogue, `@Logged`, or field names. A
requirement states observable behaviour; *how* you achieve it belongs in `## Approach`, or
in `design.md` if it needs a real argument. If your requirement cannot be checked from
outside the code, it is a design note wearing a requirement's heading.

## `tasks.md`

```markdown
## 1. Implementation
- [ ] 1.1 Add the two logged fields to `DriveState`
- [ ] 1.2 Update both in the catch block

## 2. Verification
- [ ] 2.1 One forced failure increments the count by exactly one
- [ ] 2.2 Repeated failures accumulate rather than reset
- [ ] 2.3 The recorded class name matches the exception thrown
- [ ] 2.4 Both fields appear in the log in simulation
```

---

## How big a spec should be

Look at the length above. **Two requirements, five scenarios, one screen.** That is the
target, not the floor.

**A two-line fix does not get a spec.** Fix it, test it, open the pull request. A feature
that does need one gets a spec this size — not three times it. Sixteen acceptance criteria
for a two-file change is a session spent on a document nobody will read, and it is the
most common way this goes wrong.

The cheapest way to keep a spec short is the **Out of scope** heading. Most
over-specification is a spec that never said what it was *not* doing, so every reviewer
question got answered by adding another criterion. Answer them once, in four bullets.

## Before you write Java

1. `openspec validate <your-change>` passes.
2. The change folder is **committed**, before your first implementation commit. The commit
   timestamps are how that done-list item gets checked — it is about ordering, not
   existence.
3. You have handed the agent your spec *and* `coding-standards.md`. One is the decisions
   for this change; the other is the decisions that hold across every change.

Then build it. Expect to diverge from your own spec — every real project does, and *where
you diverged and why* is the question the defense review is built around.
