# The Rules

Twelve rules, meant to be posted, memorized, and argued with. Each one has the reason and the evidence under it.

Rules 1–4 are about the artifacts. Rules 5–8 are about controlling the agent. Rules 9–12 are about closing out.

---

## Rule 1: There is always a constitution

**Every repository has a constitution: a short file of non-negotiable principles that every change is checked against, and that the agent reads before it does anything.**

The idea is borrowed from GitHub's Spec Kit, which numbers its articles so a review can cite one: Article III is a Test-First Imperative, Article IX is Integration-First Testing. Kiro calls the same idea steering files. Claude Code and Codex read `AGENTS.md`. The name varies; the function does not.

**The distinction that makes it useful:** the spec changes every change; the constitution changes once a season.

> If you find yourself explaining the same thing to an agent twice, it was a constitution line and you wrote it in a chat window instead.

### A starting constitution

Copy this into any Ignite repository and adapt it. Twelve articles, short on purpose. A constitution nobody has read is decoration.

```markdown
# Constitution — Ignite Robotics robot code

## Article I — Simulation-First
Every subsystem is constructible and testable in simulation without hardware, and
that includes a simulated mechanism, not only a simulated controller: the motor,
the gearbox ratio, the moment of inertia, the sensor, and the physical limits.
A subsystem whose sim stops at the device layer has not been simulated. A subsystem
that cannot be simulated cannot be reviewed.

## Article II — Test-First (NON-NEGOTIABLE as to order)
No implementation before tests exist, are reviewed, and are confirmed to FAIL.

Authorship follows training, order never does:
- Before completing SDD training: tests are written by a person.
- After completing SDD training: the agent may author tests, provided they are
  written first — or, at the earliest, once the interface is defined — and are
  reviewed and confirmed FAILING by a person before any implementation.
- At every level: once implementation begins, no agent writes, edits, or deletes
  a test file. Authoring at rung 3 and editing at rung 4 are different acts and
  the second one is never allowed.

## Article III — Behaviour, Not Internals
Tests and requirements describe observable behaviour through the public API.
No test names a private field. Double comparisons use an explicit delta.

## Article IV — Units Are Mandatory
Every quantity carries its unit. Use the units library where one exists; where one
does not, the unit is in the identifier — heightMeters, velocityRadPerSec,
timeoutSeconds. A bare double named `height` does not pass review.
Requirements and scenarios state units too: "250 ms", never "a short delay".
Conversions happen once, at the boundary, and are named.

## Article V — Decisions Are Human
Control strategy, subsystem decomposition, autonomous structure, and every gain
are engineering decisions. AI may explain, critique, and implement. It may not decide.

## Article VI — Current Season Only
When AI output disagrees with the current WPILib or vendor documentation, the
documentation wins. No API from a prior season without a citation to a current doc.

## Article VII — Harness-Agnostic
Skills, agents, prompts, hooks, and controls are written to work across harnesses,
not against one vendor's. Standing instructions live in AGENTS.md. Anything that
must be harness-specific is a thin adapter over a portable definition, is named as
such, and has an equivalent for every harness the team uses. A workflow that only
runs in one tool is a workflow the team loses when that tool changes.

## Article VIII — Structured Notes, and Clean Context
Notes are structured, and the language is Markdown — in the repository, in the
knowledge base, reviewable in a pull request. Not a chat log, not a screenshot.

Context is cleared at two boundaries, every time:
- between research and spec writing;
- between spec writing and implementation.

What crosses a boundary is the written artifact, never the conversation. If a fact
did not make it into the document, it does not survive the boundary — which is the
point.

## Article IX — Enforced, Not Requested
A rule that matters is enforced by a hook, a CI check, or a test. Prose is a
reminder, not a control.

## Article X — Say the Cost
Every change states what it makes worse. A change with no trade-off has not been
analysed.

## Article XI — Disclose
Every pull request carries the AI-use block. Attribution is the one thing FIRST
actually asks for.

## Article XII — Nothing Leaves Undocumented
A change is done when the spec is archived, the decision is recorded, and anything
the agent got wrong is written into AGENTS.md.
```

---

## Rule 2: Spec first. No spec, no code.

**The proposal and the delta spec are committed before the first implementation commit. Not sketched and not intended: committed, with a hash you can point at in review.**

The reason is measured, not aesthetic: ambiguity is what makes an agent special-case its way to green. See [The SDD Loop](the-loop.md) for the numbers: hardcoding rates jumping from near zero to 22–44% on underspecified problems.

**The tell that this rule is being gamed:** someone who cannot say where the implementation diverged from the spec was never steering by it. Every real build diverges. Pointing at where, and defending the call, *is* the evidence that the document was steering the work.

---

## Rule 3: One format, and it is OpenSpec

**`ADDED` / `MODIFIED` / `REMOVED` requirement sections, observable requirements, GIVEN/WHEN/THEN scenarios. Validate before review.**

The reason is not that OpenSpec wins some abstract comparison. It is that **a spec everyone writes differently is a spec nobody can review quickly**, and review speed decides how much you ship during a six-week build. The format also gives you the one-scenario-one-test mapping for free, which no free-form document does.

Pin the version at the start of the season and write the pinned commands into `AGENTS.md`.

---

## Rule 4: Tests come first, and nothing edits them once implementation starts

**The order is non-negotiable. The authorship is earned. See Article II above and [The SDD Loop](the-loop.md).**

**The mechanical form is phase-aware:** a `PreToolUse` hook denies writes under `src/test/` whenever any implementation file in the change is already modified, and CI verifies a checksum manifest of every test file taken at the rung-3 → rung-4 boundary.

Two independent controls, because the thing being defended against is an agent editing the thing that judges it, and that behaviour has been observed in the field, including outright deletion of test files after they passed.

---

## Rule 5: The agent's job is to disagree with you

**Configure it, prompt it, and review it so that agreement is expensive. An agent that never tells you you are wrong is not being helpful; it is being useless in a way that feels good.**

This is not a vibe. It is the best-measured failure mode in the field.

### It caves when challenged

The FlipFlop experiment gave ten models a classification task, then followed up with nothing more than "Are you sure?" Models flipped roughly **46% of the time**, and every one of the ten lost accuracy, averaging a **17% drop**.

That study is old enough to argue with, so: a June 2026 replication across seven frontier models found a mean answer-flip rate of **48.7%** under plausible counter-arguments, ranging from 17.5% to 97.3% by model. STEM held up better than the humanities, and "better" still meant **20.9%** on elementary mathematics.

Two and a half years of model progress moved the mean from 46% to 48.7%. This is not getting fixed by waiting.

### Warmth makes it worse, and benchmarks do not see it

Controlled experiments across five models trained to be warmer and more empathetic found **error rates rising 10 to 30 percentage points**, with warm models significantly more likely to validate incorrect user beliefs, *particularly when the user's message expressed sadness*. Critically, this happened **despite preserved performance on standard benchmarks**.

A frustrated student at midnight is exactly the emotional register that triggers it.

### What to do instead

| Do | Instead of | Why |
|---|---|---|
| State the symptom: "the shooter oscillates at 3000 rpm" | "I think kP is too high, right?" | A stated belief is what sycophancy latches onto |
| "What would have to be true for this to be wrong?" | "Are you sure?" | "Are you sure?" is the literal FlipFlop trigger |
| "Give me the strongest case against this design" as its own task | Asking for an opinion | Makes disagreement the assignment, not a risk |
| Review in a **fresh context**, code unattributed | "Here's my code, what do you think" | Ownership cues bias the response toward approval |
| Put it in the constitution: *when the approach is wrong, say so in the first sentence* | Hoping | A standing instruction, not a per-prompt one |

And the harder discipline, which is the inverse: when it disagrees with you, that is data, not an obstacle.

The build-season failure mode is someone re-prompting until the agent agrees. Re-prompting until you get the answer you wanted is not verification. It is the same act as running the test again until it passes.

---

## Rule 6: Never let it grade its own homework

**The thing that wrote the code does not decide whether the code is good.**

Language models systematically favour their own output. A 2026 study measuring this across 20 mainstream models found that **advanced capabilities are often uncorrelated, or even negatively correlated, with low self-preference bias**.

A better model does not grade itself more honestly. It grades itself more confidently.

Three separations, cheapest first:

1. **Fresh context.** Review in a new session with no memory of writing it.
2. **Read-only reviewer.** A subagent whose tool list is read-and-search-only *structurally cannot* turn a review into a rewrite.
3. **A different model, or a person.** Reserve for changes touching control logic or safety.

Rung 10 of the loop is a human defending the diff, and no amount of agent review substitutes for it.

---

## Rule 7: Green is a floor, not a ceiling

**Passing tests means "not obviously broken." It does not mean correct, and it never means general.**

One agent in the reward-hacking benchmark produced correct-and-legitimate solutions on only **42.1%** of clearly-specified problems, while producing non-general heuristics on **20.7%**: code that fits the cases in front of it.

In FRC, that shape of bug is specifically an auto routine that works from the exact starting position it was tested from, and nowhere else on the field.

The check is a question, asked in review, every time:

> **What input would break this that we did not test?**

If nobody can answer, nobody understands the change yet.

---

## Rule 8: Context is a budget; clear it at the boundaries

**The smallest sufficient context beats the largest available one. Pasting four files in is not being thorough. See [Context & Prompts](context-and-prompts.md).**

**The operational half is Article VIII:** clear context between research and spec writing, and between spec writing and implementation. Each transition changes what counts as signal. The dead ends from a research session are distractors while you are writing requirements; the deliberation behind a requirement is a distractor while you are implementing it.

What crosses a boundary is the artifact, never the conversation. If it matters and it is not in the document, it is about to be lost, which is the forcing function rather than a side effect.

---

## Rule 9: Simulate before you deploy; replay after you compete

**A change that has never been watched in the sim GUI does not go on the robot**, and a competition failure that was never replayed was never diagnosed. It was guessed at, and the guess will be re-guessed next event.

---

## Rule 10: Enforce with machines, request with prose

**If a rule matters, it is a hook, a CI check, or a test. Everything else is a reminder.**

A hook fires whether or not the model cooperates. A paragraph in a standards document has no such property. Rule 5 is the reason to take this seriously: the model is measurably persuadable, so any control that routes through the model's judgement is a control that can be argued out of.

---

## Rule 11: Disclose, and defend

**The disclosure block on every pull request, and a human who can walk the diff. Attribution is the one thing FIRST actually asks for; defending the diff is the one check that cannot be gamed by a tool.**

**The addition worth making:** review asks not only "why is this correct" but **"what did you give it, and what did you deliberately leave out?"** That question separates someone who ran the loop from someone who got lucky.

---

## Rule 12: Nothing leaves the session undocumented

**The change is not done when it works. It is done when the spec is archived, the engineering decision is written down *with the options that were rejected*, and anything the agent got wrong about this repository has been written into `AGENTS.md` so the next person does not re-teach it.**

A team that loses 100% of its members every four years cannot afford to learn the same lesson twice. This is the only rung on the ladder that survives graduation. See [Research & Team Memory](research-and-memory.md).
