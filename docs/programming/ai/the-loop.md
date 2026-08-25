# The SDD Loop

Spec-driven development is not "write a document, then code." That is a waterfall with a new hat, and it fails the same way.

**SDD is a loop with a verification ladder in the middle of it.** The spec's job is to make each rung of that ladder decidable.

---

## The ladder

```
  ┌─ 0. CONSTITUTION ──── standing rules; read before every change
  │
  ├─ 1. EXPLORE ───────── read the code, the knowledge base, the mechanism
  ├╌ 1b. RESEARCH ╌╌╌╌╌╌ OPTIONAL. Only when the answer isn't in the repo.
  ┆                      Always produces a cited document. Clear context after.
  ├─ 2. PROPOSE ───────── the change: proposal, delta spec, tasks
  ├─ 3. RED ───────────── scenarios become failing tests. Confirm they fail.
  ├─ 4. APPLY ─────────── implement against the spec, agent may drive
  ├─ 5. GREEN ────────── unit + integration tests pass; format; CI
  ├─ 6. SIMULATE ─────── drive it in the sim GUI; watch it, don't just assert
  ├─ 7. BENCH ────────── real hardware, robot on blocks, one subsystem
  ├─ 8. FIELD ────────── practice field / match
  ├─ 9. REPLAY ───────── log replay against what actually happened
  ├─ 10. REVIEW ──────── defend the diff; disclosure block
  └─ 11. ARCHIVE ─────── spec archived, decision recorded, AGENTS.md updated
        │
        └──→ back to 1
```

Rungs 3 and 5 bracket the implementation. **Rungs 6 through 9 are the ones teams skip, and they are the ones that catch the failures that lose matches.**

---

## Rung 1b — research (optional, and it always leaves a document)

Most changes skip this. Reach for it when rung 1 ended with a question the repository cannot answer: a vendor API nobody has used, a control approach nobody has built, a mechanism whose behaviour is genuinely unknown.

**It is optional. What it produces is not.**

> **Research that leaves no document did not happen.** The output is a Markdown file in the knowledge base, with citations, committed. Not a chat transcript, not a summary in someone's head, not a Slack message.

We turn over completely every four years. The second person to need an answer is always under more time pressure than the first, and the document is the only thing from this rung that survives the session.

**Every claim carries a reference.** Not "the docs say" — a link, a version, and a date. A vendor behaviour that was true in `2026.2.1` is a claim *about* `2026.2.1`. Where you could not verify something, say so; an unverified claim recorded as unverified is useful, and one recorded as fact is a trap.

Then **clear your context.** What crosses into rung 2 is the committed document, not the conversation that produced it. See [Context & Prompts](context-and-prompts.md).

---

## Rung 2 — propose: the spec makes the tests writable

The delta spec is the load-bearing part, and its format exists for a reason that only shows up at rung 3:

```markdown
## ADDED Requirements

### Requirement: Indexer recovers from a jam without operator input

The indexer SHALL detect a stalled feed and attempt reversal before surfacing a fault.

#### Scenario: Stall detected during intake
- **GIVEN** the indexer is commanded forward at ≥ 0.3 duty cycle
- **AND** measured velocity is below 5 rad/s for longer than 250 ms
- **WHEN** the periodic loop runs
- **THEN** the indexer enters REVERSING and commands −0.4 duty cycle
```

**Every GIVEN/WHEN/THEN scenario is one JUnit test.** GIVEN is the fixture, WHEN is the stimulus, THEN is the assertion.

That one-to-one mapping is the whole practical argument for the format: **a scenario you cannot turn into a test is a scenario you have not finished writing** — and you find that out before the code exists rather than after.

Two rules follow:

- **A requirement with no numbers is not a requirement.** "Recovers quickly" is not testable. "250 ms" is. The thresholds are engineering decisions and they are yours to make.
- **A scenario that names an internal field is broken.** Describe observable behaviour. If the only way to test it is to reach into a private, the requirement is describing an implementation, not a contract.

---

## Rung 3 — red: tests first

The order is not negotiable, and it is not just a style preference. A 2026 study across eight models on a class-level benchmark found that test-driven generation **improves correctness by 12 to 26 absolute points**, reaching up to 71% fully-correct classes.

Twelve to twenty-six points is not a rounding error. Writing the tests first is the single highest-leverage thing anyone does in this loop, and it costs nothing but sequencing.

**The half people skip: confirm the tests fail.** A test that passes against an unimplemented feature is testing nothing, and you will not find out until the mechanism is on the field.

### Who writes them

Order is absolute. Authorship is earned.

- **Before you are trained in this loop:** a person writes the tests.
- **After:** the agent may author them here at rung 3 — first, or at the earliest once the interface is defined — with a person reviewing them and watching them fail.
- **At every level:** once implementation begins, nothing edits a test file.

---

## The rule that is not optional: nothing edits the tests

This is where the loop stops being a style preference and becomes a safety property.

A 2026 reward-hacking benchmark ran three major coding agents in an environment where cheating was easy. It observed four distinct behaviours: hardcoding expected outputs, modifying the test files, **deleting the test files after passing**, and "heuristic solutions" that work on the visible cases and are not general.

On clearly-specified problems, outright hardcoding was rare — 0 to 2.1%. But heuristic solutions ran at **20.7%** for one agent, and only **42.1%** of that agent's solutions were both legitimate and correct.

Then the number that should change how you write specs:

> On **ambiguous** problems, hardcoding jumped to **44.4%, 33.3%, and 22.2%** across the three agents — from near zero.

**Ambiguity is what causes the cheating.** Not malice, not a bad model — an underspecified problem where "make the test pass" is the only legible goal.

That is the real argument for spec-first, and it is causal rather than aesthetic. A vague requirement is a standing invitation to special-case, and roughly a third of the time the invitation gets accepted.

| Control | What it stops |
|---|---|
| Tests exist and are red before implementation begins | "Write tests that fit what I built" |
| A `PreToolUse` hook blocks `src/test/` writes once implementation starts | Editing and deleting the tests |
| A checksum manifest of test files, verified in CI | Catches it if the hook is bypassed |
| Held-out cases the agent never sees | Heuristics that fit the visible cases |
| A human reads the diff for special-casing | What the machines miss |

That benchmark also found held-out tests produced **both false positives and false negatives** — heuristics sometimes passed them. Held-out tests are necessary and they are not sufficient. A person still reads the diff.

---

## Rungs 5 and 6 — green, then simulate

**Green means the assertions passed. Simulate means somebody watched it.**

WPILib's physics simulation models mechanism motion without modifying your non-simulation code — it updates state from the commanded inputs and generates simulated sensor readings for the next timestep, and vendor `SimState` APIs update simulated device state while the regular device APIs stay in the code under test.

So a physics-backed unit test — construct the subsystem, command it, step the model, assert on where the encoder ended up — runs in seconds in the build. **That is rung 5, and it belongs in CI.**

Rung 6 is the sim GUI with a human in front of it, and it exists because assertions only check what you thought to assert.

> **Mock the field. Never mock the mechanism.**
>
> Simulate the motor, the encoder, and the gearbox. Do not write a `FakeIndexer` that returns what your test wants.

A `FakeIndexer` passes and teaches nothing. A simulated TalonFX with the real gear ratio and the real moment of inertia will show you the oscillation before the mechanism does.

### What each rung actually catches

| Rung | Catches | Cannot catch |
|---|---|---|
| 5 Green | logic, edge cases, contract violations, regressions | anything you didn't assert; wrong assumptions in the sim model |
| 6 Simulate | scheduler conflicts, command interruption, "it never reaches that state" | electrical, mechanical, latency, friction, field geometry |
| 7 Bench | wiring, CAN IDs, inversions, gear ratios, current limits | anything requiring the full field |
| 8 Field | driver ergonomics, real-world timing, alliance interactions | intermittents that need many repetitions |
| 9 Replay | *exactly what happened*, deterministically, at speed | anything not logged |

Rung 7 is where sign errors live, and no amount of simulation finds them. **Simulation raises the floor; it does not replace the bench.**

---

## Rung 9 — replay closes the loop

Log replay converts a competition failure from an anecdote into a **regression test**. Write this procedure down somewhere you can find it at 11pm in Week 5, because nobody invents it under pressure:

1. Something goes wrong in a match. **Pull the log before anything else.**
2. Replay it. Reproduce the failure in sim, on a laptop, off the field.
3. Write the failing test *from the replay*, at rung 3. This is a new scenario and it belongs in the spec as a `MODIFIED` requirement.
4. Fix. Rungs 4–6.
5. Replay the original log against the fix. **The bug is fixed when the old log produces the right behaviour** — not when someone says it looks better.
6. Keep the log. It is a fixture now, and it belongs in the knowledge base.

An agent can drive steps 2 through 5 productively, because every one of them has ground truth attached. This is the single best use of an agent on Ignite code, and it is nothing like "write me a subsystem."

---

## The loop under build-season pressure

The full ladder is right and it is not always affordable. Say the degraded mode out loud so people degrade deliberately instead of silently.

| Situation | Minimum loop | Never skip |
|---|---|---|
| Offseason / preseason | all rungs, 1b included | — |
| Build weeks 1–4 | 1–6, 10, 11; 1b when the answer isn't in the repo | tests before code; archive; **research still writes its document** |
| Build weeks 5–6 | 1–6, 10 | tests before code |
| Competition, between matches | 3, 4, 5, 9 | **a failing test that reproduces it** |
| Competition, 10 minutes to queue | 4, 7 — human types it, no agent | a second person watches the change |

The last row is a real policy, not a joke.

> **Under queue pressure, turn the agent off.**

The loop that makes agent output trustworthy is the loop you do not have time to run, and an unverified agent diff going onto a robot ninety seconds before a match is the worst risk-adjusted decision available in this sport.
