# Task 3: `FEATURE-BACKLOG.md`, `DEFENSE-REVIEW.md`, `MISCALIBRATION.md`

The three documents that carry M7's actual teaching. The risk in all three is the same and it is not technical: each one is easy to write as a plausible-sounding generic document, and a generic version of any of them is worse than not shipping it. The backlog must name features that really exist in `Robot-2026`; the question bank must ask questions a student can actually be wrong about; the miscalibration worksheet must produce a wrong answer rather than describe the possibility of one.

**Read spec §4 before starting.** It carries the mining findings these entries come from, verified at the pinned SHA.

**Files:**
- Create: `projects/m7-ai-assisted/FEATURE-BACKLOG.md`
- Create: `projects/m7-ai-assisted/DEFENSE-REVIEW.md`
- Create: `projects/m7-ai-assisted/MISCALIBRATION.md`

**Consumes:** Task 2's `projects/m7-ai-assisted/` directory and `reference/code-review-prompt.md`, which `DEFENSE-REVIEW.md` links to.
**Produces:** the three documents. Task 6 writes authoring notes referencing the backlog's selection criteria.

**Lane:** `m7`. Work in `.worktrees/m7`, after Task 2's commit lands.

---

- [ ] **Step 1: Re-verify every claim the backlog is about to make**

The backlog names real code. Confirm each claim at the pinned SHA before writing it down — a backlog entry pointing at something that is not there costs a student their session.

```bash
R=/Users/y0shi/workspace/ignite/Robot-2026
S=28ce5397d88275001d656dbb717d22cf9ac0150d
VS=src/main/java/frc/robot/subsystems/vision/VisionSubsystem.java

echo "--- counters declared (expect 12) ---"
git -C $R show $S:$VS | grep -cE "private long .*(Accepted|Rejected)"

echo "--- public accessors for them (expect 0) ---"
git -C $R show $S:$VS | grep -cE "public .*(Accepted|Rejected)"

echo "--- other files referencing them (expect only VisionSubsystem) ---"
git -C $R grep -lE "ResultsRejectedDue|ResultsAccepted" $S -- '*.java'

echo "--- the unlogged DriveState TODO ---"
git -C $R show $S:src/main/java/frc/robot/statemachines/DriveState.java | sed -n '52p'

echo "--- test files in the repo (expect 0) ---"
git -C $R ls-tree -r --name-only $S -- src/test | wc -l

echo "--- but the harness is wired ---"
git -C $R show $S:build.gradle | grep -E "junit-jupiter|useJUnitPlatform"
```

Expected: 12, 0, one path (`VisionSubsystem.java` only), the `// TODO add to log` line, 0, and two matching build lines. If any differs, the entry it supports must be rewritten or dropped rather than shipped as-is.

- [ ] **Step 2: Write `FEATURE-BACKLOG.md`**

Selection criteria come first and entries second, deliberately: entries get consumed as students ship them, and a mentor refilling the file needs the rule rather than the examples.

````markdown
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
````

- [ ] **Step 3: Write `DEFENSE-REVIEW.md`**

````markdown
# Defense review — the question bank

**Published openly, on purpose.** A defense review you can prepare for is still a
defense review. If reading this list first means you write a diff you can explain, the
list has done its job — that is the outcome it exists to produce, not a leak.

**For the mentor:** this is a bank, not a script. Pick the questions the diff actually
raises. The pass condition is not "answered all of them," it is that the student can
account for their own code without help.

---

## How this works

The student walks a mentor, or a peer who has finished M7, through **every decision in
the diff**, unaided — no AI open, no notes. Expect it to take twenty minutes.

The rule that makes it work: **anything the student cannot defend comes out and gets
rewritten by them.** Not the whole pull request, just that part. There is no penalty for
this, and a review that removes something is a successful review, not a failed one.
Finding the gap is the entire point.

## Questions about the code

Asked against specific lines in the diff. The four the milestone names:

- Why this control mode?
- What happens if this sensor reads zero?
- Where does this number come from?
- What happens if this is interrupted halfway?

And more that apply to most diffs:

- Walk me through what happens on the first loop after this is enabled.
- What did you consider and reject? Why?
- Which line here are you least sure about?
- What happens the first time this runs at a competition, on a cold robot?
- If this breaks in two months, what will the symptom look like?
- What in this diff did you write, and what did the tool write?
- Point at a line you changed after the tool produced it, and say why.

## Questions about the tests

- What would have to break for this test to fail?
- Does this test pass against code that does nothing? Show me.
- What does this test *not* cover?
- Why did you test at this level rather than the one below it?

## Questions from the done list

Each maps to a Definition-of-done item and can be asked directly:

- **Shepherding and drifting.** Name both. Describe a time in this feature where you did
  one of them. (A student who has genuinely used AI substantially has done both; an
  answer of "neither happened" usually means the session was short or the memory is
  generous.)
- **Interface-level versus decision-level trust.** What is the difference, and which one
  did you extend to the tool in this diff?
- **The review prompt.** You read `reference/code-review-prompt.md`. Name two standards
  it holds this repository to. (Two is a low bar on purpose — the point is that the
  student opened it.)
- **Miscalibration.** Show me the wrong answer you elicited. What made it convincing?

## What a pass sounds like

| Weak | Strong |
|---|---|
| "That's what the AI suggested." | "It suggested that; I checked it against how the subsystem is initialised and it holds because…" |
| "I think it's the standard way." | "I picked it over the alternative because of X. The tradeoff is Y." |
| "It works, the tests pass." | "It works, and here is the case I expected to break it, and why it doesn't." |
| "I'd have to look." | "I'd have to look" — *about one line, having explained the rest.* |

The last row is not a joke. Nobody holds a whole diff in their head perfectly. A student
who explains the diff and flags one line they want to re-check is doing better than one
who has an answer for everything.

## Failing well

A student who cannot defend three-quarters of their diff has not failed the milestone —
they have discovered that they shipped code they do not understand, which is the exact
thing M7 exists to surface. The response is: cut what they cannot defend, rewrite it
themselves, and come back. That is a completed M7, not a retry.
````

- [ ] **Step 4: Write `MISCALIBRATION.md`**

````markdown
# Miscalibration — eliciting a confidently wrong answer

One done item on this milestone cannot be satisfied by reading: **you have to make an AI
tool be confidently, specifically wrong, and then look closely at how convincing it
was.**

Everyone is told that these tools hallucinate. Almost nobody has watched one do it about
something they personally know the answer to. That gap is the whole exercise. Being told
costs you nothing and teaches you nothing; being fooled for ten seconds by an answer in
your own domain recalibrates you permanently.

---

## How to do it

**Ask for something you already know the answer to, where being wrong is checkable.**

Two reliable prompts:

- **A tuning constant for your robot.** "What kP should I use for a shooter flywheel
  running two Kraken X60s at 6000 RPM?" There is no way to know this without the
  mechanism. You will very likely get a specific number anyway.
- **A WPILib API from a few seasons ago.** Ask how to do something using a class or
  method signature that changed. The answer will often be fluent, well-structured, and
  describe an API that no longer exists.

Then check it. The number against your own mechanism, the API against the current
WPILib documentation.

**What to look for is not that it was wrong — you knew that going in. It is what the
wrongness looked like.** Was it hedged, or confident? Did it show its work? Was the
format identical to the format it uses when it is right? That last one is the finding
that matters, and it is why "I'll be able to tell" does not survive contact.

---

## Write it up

Fill this in and bring it to your defense review.

### The prompt I used

```
[paste exactly what you asked, including any context you gave it]
```

### What it told me

```
[paste the answer verbatim. Do not clean it up or trim it -- the shape of the
wrong answer is the data. Include the confident bits.]
```

### The tool and version

```
[which tool, and roughly when -- these change fast, and an answer that was wrong
last month may not be this month]
```

### How I knew it was wrong

[What did you check it against? Be specific -- "the docs" is not an answer, which
page is.]

### What made it convincing

[The important one. What about the answer would have made you accept it if you had
not already known better? Confidence? Specificity? Correct-looking format? A
plausible explanation attached to a wrong number?]

### Would I have caught this in code review?

[Honestly. If this had arrived in a pull request from a teammate rather than from a
prompt you wrote to trap it, would you have flagged it?]

---

## The part that carries forward

The reason this is a done item and not a demo: **the failure mode is not that the tool is
wrong, it is that wrong output looks exactly like right output.** There is no tell. Which
is why every other rule in `ai-use-standard.md` is about verification rather than about
detection — you cannot detect it, so you have to check it.
````

- [ ] **Step 5: Verify nothing in these documents points at something that does not exist**

```bash
cd /Users/y0shi/workspace/ignite/docs/.worktrees/m7
echo "--- reference/code-review-prompt.md exists (DEFENSE-REVIEW links it) ---"
test -f projects/m7-ai-assisted/reference/code-review-prompt.md && echo OK || echo MISSING

echo "--- ai-use-standard.md exists and has a §6 ---"
grep -q "^## 6. Disclosure" docs/programming/ai-use-standard.md && echo OK || echo MISSING

echo "--- every internal link resolves ---"
grep -ohE '\]\([^)h][^)]*\)' projects/m7-ai-assisted/*.md | tr -d '()]' | sort -u
```

Check each printed path by eye against the directory. `docs-links.yml` will catch broken links in `docs/`, but **this directory is outside `docs_dir`**, so nothing checks these automatically. That is why this step is manual and required.

- [ ] **Step 6: Confirm no answer leaked into a student-facing document**

`DEFENSE-REVIEW.md` is published openly by design, so there is no answer key to protect here. What there *is* to protect: the backlog must not tell a student what the answer to their own design questions is.

```bash
grep -niE "you should (use|publish|log|pick)|the answer is|the correct (value|approach)" \
  projects/m7-ai-assisted/FEATURE-BACKLOG.md
```

Expected: no output. The backlog names *where* the work is and *what will be asked about it*, never what to do. If a line tells the student which design to pick, cut it — choosing and defending is the milestone.

- [ ] **Step 7: Commit**

```bash
git add projects/m7-ai-assisted
git commit -m "Add M7's feature backlog, defense question bank, and miscalibration worksheet

The backlog is seeded from features mined out of Robot-2026 at the pinned SHA
rather than invented, because 'pick a real feature, not a toy' fails the moment
the list is plausible-sounding fiction. Its lead entry is VisionSubsystem's
twelve rejection counters, which are incremented every loop and never published:
private, no accessors, referenced by no other file. Verified at the pinned
commit. It is also the code M5's READING.md already sends students to read, so
an M7 student improves something they have studied.

Selection criteria come before entries deliberately -- entries get consumed as
students ship them, and the mentor refilling the file needs the rule, not the
examples. Tuning TODOs are explicitly excluded: tuning is M4's lesson, needs
hardware, and leaves no diff to defend.

The backlog also records that Robot-2026 has no tests at all while its
build.gradle is already wired for them, so a student plans for their feature's
test being the repository's first."
```
