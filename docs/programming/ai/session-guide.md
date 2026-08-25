# Session Guide — Programming at Ignite (75 min)

Facilitator notes for the two-part session. **This is the presenter's page, not the student's** — students get [the other pages](README.md) and the slides.

The deck runs in two parts. **Part one** is the programming curriculum as a whole: the ladder, the four entry points, the kickoff gate, and the three AI tiers. **Part two** is how we work with AI, and it only lands on top of part one — every rule in it is attached to a milestone. The written companion to part one is [The Program at a Glance](the-program.md); the companion to part two is [everything else in this section](README.md).

**[Open the slide deck](session-deck.html)** — 35 slides, self-contained, works offline. Arrow keys or click to advance, `n` toggles presenter notes on the current slide, `f` for fullscreen. The rail across the top shows the **target clock time** for each slide, so you can tell at a glance whether you are behind. Slide numbers below match it.

---

## Before you start

**Audience:** everyone, all tiers. Assume some people in the room have never written a line of Java and some have shipped competition code. The session is designed so nobody needs a laptop and nobody needs to be at the Green tier.

**Time:** the full deck runs **about 75 minutes** — 12 for part one, 63 for part two, of which 16 are hands-on. It is written to be the team's standing overview of how we work, not only a first session, so it carries real detail on the ladder, the loop, OpenSpec, and the constitution.

**For a 60-minute slot, skip slides 11, 14, 19, 24, and 27** and hold the activity to 10 minutes. For a 45-minute slot, also cut part one down to the ladder (slide 4) and the tier table (slide 7), and hand out the docs page for the rest. Full cut paths are at the bottom.

Protect the activity in every version. If you are running long, cut from part one or from *How we do things* — never from the activity.

**Materials:**

- Projector and the deck
- **Paper and pens for everyone** — the first exercise and the main activity are both written, on purpose
- Optional: one laptop at the front to show a real repo's hooks firing

**The one thing you must not do:** turn this into a demo. A live agent session eats the clock and produces a room of spectators. The evidence in Part 1 is specifically that watching does not stick.

---

## Running order

| Time | Slides | Part | Mode |
|---|---|---|---|
| 0:00–0:12 | 1–7 | **Part one — the program**: the tracks, the ladder, the gate, the tiers | Presented |
| 0:12–0:15 | 8–9 | **Part two opens** · cold open — write a prediction | Written, individual |
| 0:15–0:27 | 10–14 | What's actually true | Presented |
| 0:27–0:54 | 15–27 | **How we do things** — the line, the loop, SDD, OpenSpec, research, the constitution | Presented |
| 0:54–1:10 | 28–31 | **Activity — write a prompt, red-line a partner's** | Written, in pairs |
| 1:10–1:15 | 32–34 | The rules | Presented |
| 1:15–1:17 | 35 | What next | Presented |

---

## Part one — The program (slides 2–7, 12 min)

**Ten to twelve minutes, and do not let it run long.** Half the room already knows the ladder and the other half is about to be handed a URL. The job here is not to teach the curriculum — [The Program at a Glance](the-program.md) does that, and it is self-guided. The job is to make part two make sense, because every AI rule in part two is attached to a milestone.

### Slide 3 — Which track am I?

Read the four rows and **ask for hands, track by track.** It tells you who is actually in the room, and it tells the people in each row that they are not the only one.

Then say the last line twice: **caught between two, pick the earlier one.** A test-out moves you up in under an hour. Overclaiming is the single most common way somebody stalls out, it costs weeks rather than an hour, and it almost always surfaces in M3 — three weeks after the decision that caused it, when nobody connects the two.

### Slide 4 — The ladder, in one picture

**This is the slide worth spending time on, and it is the one to keep if you only keep two.**

Walk the top row first: four entry points, one sequence, everyone ends in the same place. Track D entering at M6 usually draws a question — answer it here rather than deferring, because it is the same argument as the tier boundary below. Veterans are not asked to redo M0–M5; they are asked to acquire the one skill the ladder exists to guarantee before the tooling is handed over. As of 2026-08-18 our own repositories contained zero test files across three seasons, so this is measured, not asserted.

Then drop to the coloured bands and say the sentence the whole session hangs on:

> **Your AI tier is not a preference and it is not a negotiation. It is a function of where you are on this line.**

Green starts after M6 because everything that makes agentic development safe is a verification skill — writing a test before the thing it tests, running a mechanism in simulation, reading telemetry, telling *green* from *correct*. That is M6's content. Green tier is not a reward for finishing M6; it is only *possible* after M6.

### Slide 5 — How you move

Three properties, and the word to land is **objective.** Every definition of done is a yes-or-no list a mentor who never taught the milestone can verify. Nobody grades your vibe and nobody can gatekeep you on taste — and, the half that matters for part two, nobody can talk their way past one either. That is exactly the property that makes the tier table two slides later enforceable rather than aspirational.

Say plainly that the ladder is self-guided and that **this session is not a milestone.** It has no test-out and gates nothing. People assume otherwise and then wait to be scheduled.

### Slide 6 — The Kickoff Readiness Gate

Read the table, then slow down on the last row. **Finishing M7 in December does not clear you for agentic use in January** — M8 has to be done too, and it is two more sessions. Somebody discovers this at kickoff every year, and the whole reason the slide exists in August is so that nobody discovers it at kickoff.

The commit-access consequence is real and it is published in advance on purpose. It is not a threat; it is the alternative to an argument in January that nobody can win.

### Slide 7 — What the ladder unlocks

The tier table. This is the **handoff into part two**, so say it out loud: this table is *permission*, and it is the last thing today that will be. Everything after this slide is method, and it applies from the day you are cleared for any of it.

Flag the fine print in the Green row — you write that spec yourself until M8; from M8 an agent may draft one you red-line. Reviewing a generated spec is harder than writing one, not easier, which is why it comes second.

If somebody argues with a boundary, that is a good sign and there is time for one round of it. The reasoning is written down in [The Program at a Glance](the-program.md) and in the [curriculum spec](../curriculum-spec.md) precisely so it can be argued with. If it turns into three rounds, take it after.

---

## Part two opens · cold open (slides 8–9, 3 min)

**Slide 9 asks two questions. Everyone writes both answers on paper before you say anything else.**

> 1. An experienced programmer uses an AI coding tool on their own codebase. How much faster do they finish? Write a percentage.
> 2. How much faster do they *think* they were? Write a percentage.

**Do not discuss the answers yet.** Tell them to keep the paper. You come back to it on slide 12, and the gap between what they wrote and what the study found is the whole hook. Announcing the answer up front throws it away.

Expect most guesses in the +20% to +50% range for question 1.

---

## What's actually true (slides 10–14, 12 min)

Part two proper starts here. The goal of this stretch is **calibration, not prohibition.** Nobody leaves thinking AI is banned. They leave knowing the specific ways it fails.

### Slide 10 — FIRST has a policy, not a method

**Get the shape of this right — do not say "there are no rules."** There is a policy, and it is permissive.

[*Expanding the FIRST Toolbox with Artificial Intelligence*](https://community.firstinspires.org/expanding-the-first-toolbox-with-artificial-intelligence), Chris Rake, EVP & COO, November 2023. Can teams use AI? *"The answer is yes!"* — code generation, sensor data, translation, outreach, judging submissions. Judges are *"instructed to evaluate teams based on what they have accomplished in relation to the judging criteria, and not discredit a team or rank them differently based on the tools they used."* It asks for one thing back: **attribution.**

**Then the actual point of the slide.** That is the entire published guidance. There is no verification standard, no method, no guidance on when the tool hurts more than it helps, and nothing about what to do at 11pm in week five. The statement also predates the current generation of coding agents by more than two years, and FIRST said they may revisit it.

The 2026 Game Manual adds nothing — we searched it for *artificial intelligence*, *AI*, *generative*, *machine learning*, *LLM*, *ChatGPT*, and *Copilot*, and got one hit: a stray image caption reading "AI-generated content may be incorrect."

**So: we are permitted, and we are unguided.** Everything that follows is Ignite filling that gap on purpose. If someone thinks a rule is too strict, they are right that FIRST does not require it — and wrong that this makes it arbitrary. That is a conversation worth having out loud.

### Slide 11 — Everyone else is already doing this

The first large-scale measurement studied 128,018 GitHub projects and found a **22.2–28.7% adoption rate** for coding agents, spanning every level of project maturity.

**Why this slide exists:** it pre-empts the objection that this is a fringe concern. About a quarter of the open-source world is already here.

### Slide 12 — The reveal

> **Have them look at the paper they wrote three minutes ago.**

16 experienced open-source developers, 246 real tasks in their own large repositories. They forecast a **24%** speedup. Afterwards they reported a **20%** speedup. They were **19% slower**.

Let that sit for a second before moving on. Then the point:

**These were experts, on their own code.** The exact population that would tell you the effect does not apply to them. It applies to mentors too — say that part, it buys you credibility.

### Slide 13 — Two failure modes, two rules

This slide carries both findings side by side, then the two rules they buy you. **The rules are the point; the statistics are why.**

**Left — you remember less.** 22 novice and intermediate programmers, pairing with a human versus pairing with Copilot, retested alone about a week later. With Copilot they scored **~14 points higher** out of 100 on the day. At retest the AI condition showed an **18.88-point-larger drop**. Mental demand, time pressure, effort, and engagement all fell. One participant said they "didn't even think about the task" but "just copy-and-pasted."

**Right — it caves when you push.** Ten models, given a task, then asked nothing but *"Are you sure?"* — they flipped **46%** of the time and every one lost accuracy, averaging **17%**. Repeated in June 2026 across seven frontier models: a mean flip rate of **48.7%**. Two and a half years of progress moved it from 46% to 48.7%.

**Then the two rules.** Spend your time here, not on the numbers.

> **Never ask it to do something you couldn't do yourself.**

Say this one carefully, because it is easy to hear wrong. It does not mean "don't use it for hard things." It means **don't use it as a substitute for understanding**. The test is not "could I do this faster?" — it is "if it hands me something wrong, would I know?" If the answer is no, you are not reviewing the output, you are hoping.

That distinction is the entire tier system in one sentence, and it is the honest answer to "why can't new members use it for everything." Not because we don't trust them. Because you cannot check work you couldn't have done.

> **Never trust an answer you haven't verified.**

It is confident when it is right and confident when it is wrong — the tone carries no information. And per the left-hand column, the moment you push back it is roughly a coin flip whether it abandons a correct answer. So agreement is not confirmation, and neither is confidence.

Verification means running it, testing it, simulating it, or checking it against the current docs. It does not mean asking the model whether it is sure — that is the one move guaranteed to make things worse.

### Slide 14 — So what

Three sentences, then move on:

1. The problem is not the tool. It is the sequencing and the verification.
2. Everything we ask you to do exists because of one of those three slides.
3. The rest of the session is what to do about it.

---

## How we do things (slides 15–27, 27 min)

This is the part that turns the deck from a session into an overview. It answers "so what do you actually want me to do," and it is the section a returning member should be able to re-watch in ten minutes.

### Slide 16 — Dependency versus decision

The question is never who typed the characters. It is whether you can explain and defend every decision in what you submit.

- **Dependency:** you trust a vendor library does what its documentation says. You need to know what it does, not how it works.
- **Decision:** picking a control strategy, decomposing a mechanism, choosing a gain, structuring an auto routine.

**AI-generated control logic is a decision wearing a dependency's clothes.** That sentence is the whole slide.

### Slide 17 — It is a harness, not a chatbot

A model in a loop with tools, permissions, and a feedback path. You cannot change the model. You can change everything else — and that configuration measurably matters more than which model you picked.

The better question is **"what can it see, what can it run, and how does it find out it was wrong?"**

### Slide 18 — The loop, as a picture

The diagram is the centrepiece of this part. **Do not walk all eleven rungs** — the picture already did that. Point at three things, in this order:

1. **The ratio.** One box builds. Five verify. Say it out loud as a ratio: *"you spend one step writing the feature and five steps finding out whether it works."* That is the most useful thing on the slide, and it reframes the whole method as being mostly about checking.
2. **The three red rungs** — RED, SIMULATE, REPLAY. These are the ones teams skip under pressure and then lose matches to.
3. **The bar underneath.** The constitution is checked at every rung, and it is the same file all season. You come back to it on slide 26.

**On rung 3, be precise**, because this is the question people ask: the *order* is non-negotiable, the *authorship* is earned. Untrained, a person writes the tests. Once you are trained in this loop, **the agent may write the tests from scenarios you specified** — and you review them and watch them fail before a line of the feature exists. At every level, once implementation begins, nothing edits them. The right-hand callout on the slide says this; say it anyway.

The dashed box is rung 1b, research — optional, and covered on the loop page in the docs rather than in this session.

### Slide 19 — Why five verification rungs

The table shows what each rung catches and what it cannot. The line that lands:

> **Simulation raises the floor. It does not replace the bench.**

Rung 7 is where sign errors live — inverted motors, swapped CAN IDs, a gear ratio entered upside down — and no amount of simulation finds them.

Also worth saying: rung 9 is why we **pull the log before touching anything** after a bad match. It turns a story into a regression test.

*(Cuttable at 45 minutes — the docs carry the same table.)*

### Slide 20 — What spec-driven development actually is

The definition, said plainly: **the spec — not the prompt, not the chat history — is the single source of truth for the change.** You write down what has to be true before the code exists. The agent builds against the document. You check the build against the document.

The table contrasts three ways of working, and the third column is the one that lands: vibe coding leaves nothing behind, docs-afterwards leave a document that rots, spec-driven leaves the file *and* the tests it produced.

**Three properties make a spec a spec.** Miss any one and you have a wiki page:

1. **Written before** the implementation, not alongside or after.
2. **Observable** — every requirement is something you could write a test for.
3. **A diff** against what the system already promises, not a fresh essay each time.

**Why this matters more with an agent than without one:** an agent has no memory of what you meant. Next session it is a stranger with no context. The spec is the only thing that carries your intent across that gap — and slide 22 is what happens when it can't.

*(Cuttable at 45 minutes only if you are confident the room already knows what a spec is. Usually they don't.)*

### Slide 21 — One scenario becomes one test

The payoff of the spec format, side by side: the GIVEN/WHEN/THEN scenario on the left, the JUnit test on the right, with the mapping visible.

**GIVEN is the fixture. WHEN is the stimulus. THEN is the assertion.**

The point to make: **a scenario you cannot turn into a test is a scenario you have not finished writing** — and the format makes you find that out *before* the code exists rather than after.

Point at the numbers in the left column too: *"250 ms"*, not *"a short delay."* That is Article IV, and it is the difference between a requirement and a wish.

### Slide 22 — The money slide

Three coding agents, in an environment where cheating was easy. On clearly-specified problems, hardcoding was rare — **0 to 2.1%**. On **ambiguous** problems it jumped to **44.4%, 33.3%, and 22.2%**.

> **Ambiguity is what makes it cheat.**

Not malice. An underspecified problem where "make the test pass" is the only legible goal.

**This is the single most important slide in the deck**, and its placement is deliberate: it lands *after* they have seen a sharp spec and its test, so they know exactly what "specified" looks like. It converts everything that follows from process imposed on them into something that visibly protects them.

### Slide 23 — OpenSpec: a change is a folder

Two halves: what gets committed, and the three commands.

The folder — `proposal.md`, `design.md`, `tasks.md`, and the delta under `specs/` — is committed **before the first implementation commit**, with a hash you can point at in review.

**Why a folder and not a conversation:** a spec everyone writes differently is a spec nobody can review quickly, and review speed is the whole game during a six-week build.

**The tell that someone gamed this:** they cannot say where the build diverged from the spec. Every real build diverges. Being able to point at where, and defend the call you made, *is* the evidence the document was load-bearing rather than decorative.

Say the version-pinning line — pin at the start of the season, put the pinned commands in `AGENTS.md` — but don't dwell.

### Slide 24 — The delta format

Three verbs — `ADDED`, `MODIFIED`, `REMOVED` — because **the spec is a diff against what the system already promises**, not a fresh document each time.

The right-hand card is the useful half. Two rules, both enforceable in about four seconds of review:

- **A requirement with no numbers is not a requirement.** "Recovers quickly" is not testable.
- **A scenario that names a private field is broken.** That is an implementation, not a contract.

*(Cuttable at 60 minutes — slide 20 already showed a good spec, and the docs carry the format in full.)*

### Slide 25 — Research, and why it ends in a file

Rung 1b. Optional — most changes skip it — but what it produces is not.

> **Research that leaves no document didn't happen.**

Three things to land, in this order:

**How you do it.** Only when the answer isn't already in the repo. This is **the one rung where fanning out actually pays** — several agents reading several sources, each in its own context, each returning a summary. Building is not like that and should stay single-agent. Every claim gets a link, a version, and a date; "the docs say" is not a citation. And you write down what you *could not* verify — that section is never empty by default, and an unverified claim recorded as unverified is useful where the same claim recorded as fact is a trap.

**Why Markdown, in the repo.** Because it is greppable, reviewable in a pull request, works in a pit with no wifi, and outlives whatever tool wrote it. Someone will ask why not a database or a knowledge graph — the short answer is that plain text search beat semantic search in every harness that has been tested, and a graph nobody can read is worse than a folder anybody can.

**How you use it later.** Point at the frontmatter. `sources_verified` is the shelf life: a claim about vendor version 2026.2.1 is a claim *about* 2026.2.1, and next season somebody needs to know that before trusting it. `status` tells you whether it was answered, partial, or a dead end — dead ends are worth keeping, because the next person will have the same idea.

Close with the context rule: **what crosses into the spec is the file, never the conversation.** The dead ends and raw page content that got you to the answer are distractors by the time you are writing requirements.

### Slide 26 — What a constitution is

Ties back to the bar under the diagram on slide 18.

**The definition:** a short file — `constitution.md` — committed next to the code, that the agent reads before every change. **A spec describes one change. A constitution describes what is true of every change.** Articles are numbered so a review can cite one: *"that's Article IV."*

**The distinction to land:** the spec changes every change; the constitution changes once a season.

**Read the snippet out loud.** It is Article II, verbatim from the real file — the same rung-3 rule from slide 18, but notice the *form*: specific enough to check, short enough to read, written so a reviewer can point at it. Vague articles ("write good code") are decoration. That contrast is the teaching.

Other tools call this steering files or house rules. Same job, different name.

### Slide 27 — All twelve articles

Do not read twelve. Read **one** — Article IX, *Enforced, Not Requested* — and explain why it is load-bearing: a hook or a CI check fires whether or not the model cooperates; a paragraph does not. **Every other article is only as real as its enforcement.**

Then the test for what belongs in the file, which is the line people remember:

> **If you have explained something to an agent twice, it was a constitution line and you wrote it in a chat window instead.**

*(Cuttable at 60 minutes — slide 26 already showed what an article looks like, and the full file is in the docs.)*

## Activity (slides 28–31, 16 min)

**This is the part that actually changes behaviour.** In the controlled trial, reading about prompting retained 0.15 at seven weeks; writing one and having it critiqued retained 0.24. Everything before this slide is setup for this slide.

### Setup (slides 29–30, 2 min)

Show the five components — Problem, Context, Method, Level, Guardrails — and the bad-versus-good example. Point out that the bad one *states a belief* ("I think the kP is too high"), which is exactly what slide 13 said never to do.

### Write (slide 31, 6 min)

Each person writes one prompt, on paper, using all five components, for a scenario from the deck. Offer two scenarios so the room is not identical:

- **A** — An autonomous routine scores from the left starting position and misses from the right.
- **B** — A shooter reaches the right speed but takes four seconds to settle, and drivers are giving up on it.

**People with no coding experience still do this.** The five components are about how you ask a question, and none of them require Java. Pair a newer member with an experienced one.

### Red-line (4 min)

Swap papers. Each person marks their partner's prompt:

1. Circle any place the prompt **leaks the expected answer**
2. Mark any component that is **missing or vague**
3. Write one thing the `Guardrails:` line should say and does not

### Share (2 min)

Take two or three examples of leaked answers from the room. Do not debug the prompts — just surface the pattern. **"I think it's the..."** shows up in most first drafts, and seeing that it is near-universal is the lesson.

---

## The rules (slides 32–34, 6 min)

### Slide 33 — All twelve rules, in three groups

Show them. Do not read them. **The grouping is the message:** four rules about what you write down, four about how you drive the tool, four about what outlives the session.

Tell people the full list with the evidence under each rule lives in the docs, then go straight to the next slide.

### Slide 34 — The three for today

Pick these three and spend the time here:

1. **Spec first. No spec, no code.** — because of slide 22
2. **The agent's job is to disagree with you.** — because of slide 13
3. **Green is a floor, not a ceiling.** — passing tests is not correct, and in FRC the specific failure is an auto that works from exactly the position it was tested from

---

## Slide 35 — What next (2 min)

- The pages: [Working with AI](README.md), and the rule list in [The Rules](the-rules.md)
- The policy is separate and it is [the AI Use Standard](../ai-use-standard.md). Which tier you are on, and what you must disclose, is there.
- **Keep the prompt you wrote.** It goes in the repo next to your first spec.

---

## Facilitator FAQ

**"So does FIRST allow this or not?"**
Yes, explicitly, since November 2023 — and judges are instructed not to rank you differently for it. What FIRST does not give you is any method, which is the gap everything here fills. Attribution is the one thing they actually ask for, and our disclosure block already exceeds it.

**"Is AI banned for new members?"**
No. Explaining a concept, decoding an error message, and quizzing you are fine at every tier. Writing your code for you is not, at the start. The standard has the specifics.

**"Team 254 uses AI, so why are we careful?"**
They do — they presented on it at the 2026 Championship. They also have a decade of infrastructure to catch it when it is wrong. The gate is not "are you allowed," it is "can you tell when it's wrong yet."

**"This says AI made experts slower. So why use it at all?"**
That study measured one thing in one setting in early 2025 and said so itself. The honest answer: it is very good at some things — explaining unfamiliar APIs, boilerplate, review, replay-driven debugging — and worse than you think at making engineering decisions. The rules exist to keep it on the first list.

**"What if I disagree with a rule?"**
Say so, with the reason. Every rule in the list has evidence under it, and evidence can be argued with. That is a much better conversation than quiet non-compliance, and it is how the standard gets to version 1.4.

---

## Cut paths

| You have | Show | What you give up |
|---|---|---|
| **75 min** | everything | — |
| **60 min** | skip 11, 14, 19, 24, 27; activity to 10 min | verification-rung detail, the delta format, the article list — all in the docs |
| **45 min** | the above, plus part one down to slides 4 and 7, and skip 16, 23, 25, 33 | the tracks and gate tables (hand out [The Program at a Glance](the-program.md) instead), the OpenSpec mechanics, and research |
| **20 min** | 1, 4, 7, 9, 12, 13, 18, 20, 22, 28–31, 34 | everything but the ladder, the tier table, the prediction, the reveal, the two rules, the loop picture, what SDD is, the ambiguity finding, the full activity, and three rules to close |

Cutting part one entirely is a false economy in a room with new members in it. Slides 4 and 7 take four minutes together, and they are what make every rule in part two land as a consequence of *where you are* rather than as a list of restrictions.

**Never cut the activity to fit.** A 20-minute session with the activity beats a 60-minute session without it, and the trial data is unambiguous about which way that trade goes.

**If you are cutting to 20, keep slides 4, 18 and 20.** The ladder is what makes the tier boundaries land as consequences instead of restrictions, the loop picture is the one thing a returning member can look at once and use, and slide 20 is the only place the method itself is defined. Everything else hangs off those three.
