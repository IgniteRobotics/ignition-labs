# Worked example — a spec's second change

`SPEC-TEMPLATE.md` at M7 walked one change written from nothing — an `## ADDED Requirements`
block against an empty `openspec/specs/`. This picks the same capability up six weeks later,
after that change archived, and changes it again.

**Copy the shape, not the subject.** The vision-exception capability is spent twice over now.
Yours is the capability your own M7 change archived into.

---

## 1. The capability spec, as it stands

`openspec/specs/vision-telemetry/spec.md` — settled behaviour, put there by
`openspec archive`. This is the file you read before you touch anything.

```markdown
## Purpose

Vision telemetry exists so that a mentor reading a match log afterwards can tell whether
the vision path failed, and how often, without reading raw console output line by line.

## Requirements

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

**One line in that file is somebody's work, not the archive's.** `## Purpose` did not arrive
written. Archiving a brand-new capability creates its spec with that heading reading
`TBD - created by archiving change <name>. Update Purpose after archive.` The sentence above
replaced the TBD, and writing it is the first thing M8 asks of you, because you are reading
the whole file anyway.

Everything under `## Requirements` is exactly what the M7 change put there. **Read all of
it before you touch any of it** — the next section is about to restate one of those
requirements in full, and *in full* means every scenario hanging off it.

---

## 2. The change

`openspec new change rescope-vision-exception-count` scaffolds the folder and a
`.openspec.yaml`, and stops there — no `proposal.md`, no `tasks.md`, no `specs/`.
All three files are yours: `proposal.md` and `tasks.md` exactly as at M7, and the delta at
`openspec/changes/rescope-vision-exception-count/specs/vision-telemetry/spec.md`. The count
is rescoped from *since boot* to *since the current enable cycle*, and the exception-type
field goes.

```markdown
## MODIFIED Requirements

### Requirement: Vision exception occurrence count
The system SHALL record the number of times the vision-estimate handler has caught an
exception since the current enable cycle began, as a telemetry field published alongside
the vision data.

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
- GIVEN the handler has already caught an exception during this enable cycle
- WHEN it catches a second one
- THEN the recorded count reads two, having not reset between loops

#### Scenario: Running in simulation
- GIVEN the code is running in simulation, where console capture is disabled
- WHEN the handler catches an exception
- THEN the recorded count appears in the log anyway

#### Scenario: The robot is enabled again
- GIVEN the recorded count reads two from a previous enable cycle
- WHEN the robot transitions from disabled to enabled
- THEN the recorded count reads zero

## REMOVED Requirements

### Requirement: Most recent vision exception type
The field is one slot with no lifetime of its own. Once the count resets each enable cycle
the two fields can disagree — the count reads zero while the class name still names an
exception from a cycle that ended, and nothing in the log distinguishes those. Nor does one
slot answer the question the field was added for when more than one type fires: it names the
last, which is rarely the one that mattered. The honest replacements are a count per
exception type, or nothing, and no mentor has a question today that the first would answer.
Removed rather than left quietly inconsistent with the requirement above it.
```

---

## 3. The reasoning, edit by edit

### Why the requirement was rescoped rather than joined by a new one

The test is one question: **read the old sentence and ask whether it is still true after the
change.** "Since robot boot" is not. The field no longer does that.

Adding a second requirement would have left the spec with two counters and a reader deciding
which of them is the number in the log. There is one field and it behaves differently now:
one requirement, modified. `ADDED` is for behaviour that did not exist, and reaching for it
because it is the block you know how to write is how a spec ends up describing a system
alongside the one that replaced it.

### Why *Repeated failures accumulate* was kept and reworded, not dropped

The first attempt dropped it. The scenario said "this session", which is precisely the
wording the change makes wrong, and deleting a scenario that has gone false reads like
tidying up. Validation refused, by name:

> `✗ [ERROR] MODIFIED "Vision exception occurrence count" omits scenario(s) the current spec still has: "Repeated failures accumulate". Copy them into the MODIFIED block (a MODIFIED requirement replaces the whole block, so archive refuses to drop them).`

A `MODIFIED` requirement replaces the whole block rather than patching part of it, so a
scenario left out of the block is a scenario deleted from the settled spec. **The scenario
was not obsolete; its wording was.** "This session" became "during this enable cycle" and
the outcome held unchanged.

**Count what actually changed in that block.** Four scenarios came across from the settled
spec, three of them word for word; one word moved in the fourth; and a fifth was added for
the behaviour that is genuinely new. That ratio is normal, and it is what *restated in full*
buys you — the three you left alone and the one you reworded are all still there, still
true, and still the record of what this requirement guarantees. A diff-shaped delta would
have shown one line and quietly orphaned the rest.

**That refusal is the milestone.** You cannot change one clause of a requirement without
accounting for every scenario hanging off it, so *read the whole thing before you change it*
stops being advice and becomes a precondition — held by something other than a mentor's
attention.

What validation does **not** do is check that any of it is true. It enforces completeness,
with or without `--strict`: it will not let you drop a scenario, and it will happily accept
one you reworded into a falsehood and exit zero. That gap is the entire reason
`SPEC-REVIEW.md` exists.

### Why removing a requirement takes an argument

Once this change archives, the requirement is gone. Archive reports `Totals: + 0, ~ 1, - 1`
and the block is simply not in the capability spec afterwards. The paragraph under the
`REMOVED` heading is the only surviving record of why, and it lives in `changes/archive/`,
where nobody browses.

So write it for the person who notices the field missing next season and cannot tell whether
it went on purpose. "No longer needed" does not survive that reading. Name what the field
could not answer, name what you considered putting in its place, and say what would have to
be true to bring it back. Unlike a wrong requirement, a bad deletion leaves nothing behind
for anyone to read and disagree with.

---

**One requirement modified, one removed, one scenario genuinely new.** Same target as M7,
and for the same reason: the spec somebody actually reads is the short one. A `MODIFIED`
block looks longer than the change it carries, and that is the format working as intended,
not a spec getting out of hand.
