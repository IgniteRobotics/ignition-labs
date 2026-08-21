# M7 — AI-Assisted Development

## At a glance

Using AI coding tools well, now that you can tell when they are wrong.

Required before you use AI tooling on competition robot code.

**Estimated time:** 2–3 sessions
**Prerequisites:** **M6 complete. No exceptions, including for veterans.**
**AI posture:** 🟢 Green — this milestone is where Green is earned and exercised

## Who enters here

Everyone, after M6. All four tracks have converged by this point.

**Read `ai-use-standard.md` in full before starting.** This milestone teaches the practice; that document is the policy, and you sign it at the end.

## Test-out challenge

**There is none.** You cannot test out of M7 by asserting you already use these tools well. The defense review in the project *is* the assessment.

## Concepts

### What these tools actually are

A language model predicts likely text. Trained on enormous amounts of code, its predictions are often correct code. That is a genuinely useful thing and it is not the same as understanding.

**What follows from that, concretely:**

- It is **fluent about things that do not exist.** A hallucinated WPILib method looks exactly like a real one
- It is **anchored in the past.** Its training has a cutoff, WPILib changes every season, and it will confidently suggest deprecated APIs. This is the most common concrete failure in FRC specifically — a mentor reported AI routinely recommending "deprecated classes from former versions of wpilib"
- It **cannot know your robot.** Not your gear ratio, your moment of inertia, your battery, your wiring. Anything requiring that knowledge — gains, standard deviations, thresholds — it will produce plausibly and wrongly
- It is **not deterministic.** This is where the "it's just like a compiler" analogy breaks. A compiler produces the same output every time. The sharpest version of this rebuttal, from a Chief Delphi thread: *"If your java compiler is hallucinating or deleting code then I think you got bigger problems."*

### Where the line actually is

This is `ai-use-standard.md` §3 and it is worth restating because M7 is where you apply it constantly.

**Interface-level trust.** You trust that CTRE's `VelocityVoltage` does what its docs say. You do not defend its internals. You defend that you chose the right control request, with the right units, for the right reason.

**Decision-level trust.** Control strategy, subsystem decomposition, gains, autonomous structure — these are engineering decisions. If an AI made one and you cannot explain why it is right, you cannot fix it when it breaks.

**A vendor library is a dependency. AI-generated control logic is a decision wearing a dependency's clothes.**

The practical test, every time: *could I defend this in review?*

### Prompting, as an actual skill

This gets modeled live by a mentor rather than described. The differences that matter:

**Give context.** "Fix this" versus "This intake command should stop when the beam break triggers, but it runs forever. Here is the subsystem and the command. WPILib 2026, Phoenix 6."

**Ask for reasoning, not just output.** "Explain what this does and why" produces something you can evaluate. "Write me X" produces something you can only accept or reject.

**Ask it to critique instead of produce.** "Here is my subsystem. What is wrong with it?" is the single highest-value prompt shape in this whole milestone, because you keep authorship and gain a reviewer.

**Say what you already tried.** Otherwise you get the obvious suggestion you already ruled out.

**Give it the docs.** These models are stale on WPILib. Pasting the current API surface changes the output quality substantially.

### Writing it down first

Everything above fires *after* the agent has already decided. In a think-aloud study of nineteen students, the introductory ones issued low-context prompts at roughly three times the rate of the experienced ones, spent most of their prompts debugging rather than building, and four of them restarted the whole project outright once the accumulated complexity got away from them. That last one is drifting, measured. The fix is not a better prompt — it is writing the decisions down before you prompt at all, at two scales:

> **A spec is the decisions for this change. A constitution is the decisions that hold across every change.** Both are things you write down *so the agent cannot make them for you.*

Ignite's constitution is [`coding-standards.md`](../coding-standards.md): read it once, hand it to the agent every time. Your spec is one change folder in `Robot-2026`, written and committed before you start.

**Where Ignite stops.** Three levels exist. *Spec-first:* the spec guides the build, then you are done with it. *Spec-anchored:* it survives into maintenance. *Spec-as-source:* the spec is the artifact, the code is generated, and the tooling marks it `DO NOT EDIT`. Ignite does spec-first, and stops there on principle — a student who never edits code is a student who did not learn to write code, which is exactly the failure `ai-use-standard.md` §1 exists to prevent. You will meet spec-as-source tooling within a season, and the line should be yours before a vendor draws it for you.

**A two-line fix does not get a spec.** Over-specifying is its own failure: a small bug inflated into four user stories and sixteen acceptance criteria, *"like using a sledgehammer to crack a nut."* You are writing one spec, for one real feature, at the scale of the feature.

**From `ai-use-standard.md`: the spec is Yellow-tier work, even at Green.** You may ask an agent what your spec missed. You may not ask it to write your spec. You have lived the parallel already — at M6 you wrote the tests that unlocked Green. Here you write the constraints that constrain the agent.

### AI as reviewer, not author

The pattern with the best support in the education research: the tool answers conceptual questions, explains code, and points at mistakes without handing over the answer. Systems built this way shifted students "away from using it to generate code for them and toward using it as a learning assistant."

**Ignite already runs one of these.** `.github/workflows/ai-code-review.yml` runs a Gemini-based reviewer against every PR diff, driven by `.github/prompts/code-review-prompt.md`.

Two things worth noticing about that setup:

1. It is AI in the reviewer seat, which is the safe seat
2. **That prompt file is where Ignite's standards get enforced. It is not where they live.** The standards it checks — the Superstructure pattern, type-safe units, command factory methods, homing state machines, thread priority, current limiting, fault detection — are written down for humans in [`coding-standards.md`](../coding-standards.md), and that document is the source. Read the constitution first, then read the prompt to see what enforcing a written standard actually looks like

The bot is frequently wrong. Push back on it. Learning to disagree with an automated reviewer is part of this milestone.

### Failure modes, by name

Knowing the names makes these dramatically easier to catch in yourself.

**Shepherding.** You spend your time coercing the tool into producing what you already knew you wanted. Twenty minutes of prompt-rewriting for four minutes of typing. Correlates with worse outcomes in observational studies.

*Catch it:* three prompt rewrites means close the panel and write the code.

**Drifting.** You accept a suggestion you half understand, then another on top of it. Something breaks, you backtrack, and you no longer know what your own code does. Each step felt fine; the destination is a codebase you are lost in.

*Catch it:* if you cannot explain the last thing you accepted, stop and return to the last point you understood.

**False confidence.** Your code works, so you conclude you understand it. You find out otherwise when asked to change it, or at a competition when you are the person who is supposed to know. This is the one the research found most consistently, and it does not feel like anything from the inside.

*Catch it:* you cannot. That is what the defense review is for.

### Reading AI-generated code critically

A checklist that works:

1. **Does it use APIs that exist?** Check against current docs, not memory
2. **Are the units right?** The most common subtle error. Radians versus degrees, RPS versus RPM
3. **What happens at the boundaries?** Zero, negative, sensor disconnected, mechanism already at target
4. **Does it block?** `Thread.sleep`, a waiting `while` — instant robot freeze
5. **Where did every number come from?** Any constant you cannot source is a problem
6. **Does it match our conventions?** Factory methods, per-subsystem constants, `.withName()`, `@Logged`
7. **Is it more complicated than it needs to be?** These tools over-engineer. Three straightforward lines beat a premature abstraction

## Project

**Ship a real feature, AI-assisted, under the Green tier — then defend it.**

1. Pick a real, useful feature. Not a toy
2. **Write the spec, and commit it before you start.** One change folder in `Robot-2026`: `openspec/changes/<your-feature>/proposal.md`. What is changing, why, the technical approach, and the cases that must hold. `SPEC-TEMPLATE.md` is a worked example. The folder is already there — a mentor set it up. If you get there first and it is not, ask a mentor rather than creating it yourself; which tool the season repo standardizes on is not a decision the first student to arrive should make
3. **Use AI assistance genuinely and substantially, against the spec you wrote.** This is the one milestone where that is the assignment rather than a concession
4. **Write or verify tests covering the new behavior.** They must pass
5. Open a PR with the disclosure block filled in — see `ai-use-standard.md` §6. The "how I verified it" field must be specific
6. **Defend the diff in review.** Walk a mentor or an M7-completed peer through every decision. Expect: why this control mode? what if this sensor reads zero? where does this number come from? what happens if this is interrupted halfway? And: **where did the build diverge from your spec, and why?** Divergence is expected and is the interesting part — if you report none, you either picked something too small or are not looking
7. **Anything you cannot defend comes out and gets rewritten by you.** Not the whole PR — just that part. No penalty. Finding the gap is the point

**Then, deliberately, break something.** Ask an AI for something you know it will get wrong — a kP for your shooter, or a WPILib API from three seasons ago. Look closely at how confident and plausible the wrong answer is. That calibration is worth more than any amount of being told it happens.

## Definition of done

- [ ] Feature merged with a filled-in AI-use disclosure
- [ ] Tests pass and cover the new behavior
- [ ] You defended every decision in the diff, unaided, in review
- [ ] Anything you could not defend was removed and rewritten by you
- [ ] You can name shepherding and drifting and describe a time you did one
- [ ] You deliberately elicited a confidently wrong answer and can describe what made it convincing
- [ ] You read the coding standards (`coding-standards.md`) and can name two standards in it
- [ ] You have read and signed the AI-use standard
- [ ] You can explain the difference between interface-level and decision-level trust
- [ ] Your spec was committed before your first implementation commit
- [ ] You can name one place the implementation diverged from your spec, and say why

## Common failure modes

**Skipping straight here.** M6 is the prerequisite because tests are what make Green safe. There is no version of this that works without them.

**Spec theatre.** Writing the spec after the code, to satisfy the checklist. It is checkable — the commit timestamps say so, which is why the item is about ordering and not about existence. The same mistake points the other way too: sixteen acceptance criteria for a two-file change is a session spent on a document nobody will read.

**Treating disclosure as absolution.** It is not. "The AI wrote it" explains what happened; it does not transfer responsibility. Responsibility always sits with a human.

**Accepting code that works without understanding it.** The defense review exists to catch this and it will.

**Using it for gains.** It cannot know your mechanism. See M4.

**Not checking for deprecated APIs.** The single most frequent concrete failure in FRC.

**Vague verification.** "I read it and it looked right" is not verification. "Tests pass," "checked against the CTRE docs," "ran it in sim and watched it reach setpoint" are.

**Over-trusting the review bot.** It is a reviewer, not an authority. It is often wrong. Disagree with it.

**Stopping asking humans.** The highest-rated community advice on this is blunt: experienced humans "will obliterate anything any LLM will throw your way." We have mentors.

## AI posture — 🟢 Green

Full agentic use, gated on four conditions that must all hold:

1. Your spec is written and committed before your first implementation commit
2. Tests exist, cover the new behavior, and pass
3. AI use is disclosed in the PR
4. You defend the diff in review

See `ai-use-standard.md` §4 for the full tier and §7 for how a defense review runs.

## Resources

See `resources.md` for the full library. For this milestone:

- **`ai-use-standard.md`** — the policy. Read it fully before starting
- **The Widening Gap (Prather et al., 2024)** — why the tiers exist, and the false-confidence finding
- **"It's Weird That it Knows What I Want" (Prather et al., 2023)** — the origin of shepherding and drifting
- **AI Code Generators and Novice Learners (Kazemitabaar et al., 2023)** — the counterpoint. Scaffolded AI use measurably helped learners aged 10–17. Read this alongside the Widening Gap, not instead of it
- **FIRST: Expanding the FIRST Toolbox with AI** — FIRST's only official position
- **Chief Delphi: Student written code** — the vendor-library argument you should be able to answer
- **Chief Delphi: The Next Revolution: AI in FRC (254)** — the most credentialed programming team in FRC, going public with heavy AI use
- **Böckeler, *Understanding Spec-Driven Development*** — the source of the spec-first / spec-anchored / spec-as-source ladder, and the sledgehammer warning. On this list *because* it is skeptical, not despite it
- **OpenSpec** — the spec format M7 uses. Four folders and a markdown file. The CLI is optional and you can complete the milestone without installing anything
- **Ignite:** [`coding-standards.md`](../coding-standards.md) — the constitution. Our standards, in one place, written to be read by a person
- **Ignite:** `Robot-2026/.github/workflows/ai-code-review.yml` — AI in the reviewer seat
