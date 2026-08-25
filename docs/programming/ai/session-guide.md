# Session Guide — Working with AI (45 min)

Facilitator notes for the 45-minute session. **This is the presenter's page, not the student's** — students get [the other pages](README.md) and the slides.

**[Open the slide deck](session-deck.html)** — 22 slides, self-contained, works offline. Arrow keys or click to advance, `n` toggles presenter notes on the current slide, `f` for fullscreen. The rail across the top shows the **target clock time** for each slide, so you can tell at a glance whether you are behind. Slide numbers below match it.

---

## Before you start

**Audience:** everyone, all tiers. Assume some people in the room have never written a line of Java and some have shipped competition code. The session is designed so nobody needs a laptop and nobody needs to be at the Green tier.

**Time:** 45 minutes, of which **12 are hands-on**. Protect the activity. If you are running long, cut Part 4, not Part 3.

**Materials:**

- Projector and the deck
- **Paper and pens for everyone** — the first exercise and the main activity are both written, on purpose
- Optional: one laptop at the front to show a real repo's hooks firing

**The one thing you must not do:** turn this into a demo. A live agent session eats the clock and produces a room of spectators. The evidence in Part 1 is specifically that watching does not stick.

---

## Running order

| Time | Slides | Part | Mode |
|---|---|---|---|
| 0:00–0:03 | 1–2 | Cold open — write a prediction | Written, individual |
| 0:03–0:15 | 3–9 | What's actually true | Presented |
| 0:15–0:23 | 10–14 | The line and the loop | Presented |
| 0:23–0:35 | 15–18 | **Activity — write a prompt, red-line a partner's** | Written, in pairs |
| 0:35–0:43 | 19–21 | The rules | Presented |
| 0:43–0:45 | 22 | What next | Presented |

---

## Part 0 — Cold open (slides 1–2, 3 min)

**Slide 2 asks two questions. Everyone writes both answers on paper before you say anything else.**

> 1. An experienced programmer uses an AI coding tool on their own codebase. How much faster do they finish? Write a percentage.
> 2. How much faster do they *think* they were? Write a percentage.

**Do not discuss the answers yet.** Tell them to keep the paper. You come back to it on slide 6, and the gap between what they wrote and what the study found is the whole hook. Announcing the answer up front throws it away.

Expect most guesses in the +20% to +50% range for question 1.

---

## Part 1 — What's actually true (slides 3–9, 12 min)

The goal of this part is **calibration, not prohibition.** Nobody leaves thinking AI is banned. They leave knowing the specific ways it fails.

### Slide 4 — FIRST has no rule

We searched the 2026 Game Manual directly for *artificial intelligence*, *AI*, *generative*, *machine learning*, *LLM*, *ChatGPT*, and *Copilot*. One hit, and it is a stray image caption reading "AI-generated content may be incorrect."

**Say this out loud:** nobody is coming to enforce anything. Everything we do here, Ignite decided on purpose. If someone thinks a rule is too strict, they are arguing with us, not with FIRST — and that is a conversation we are happy to have.

This matters more than it looks. A rule people believe is imposed from outside gets followed grudgingly and abandoned under pressure.

### Slide 5 — Everyone else is already doing this

The first large-scale measurement studied 128,018 GitHub projects and found a **22.2–28.7% adoption rate** for coding agents, spanning every level of project maturity.

**Why this slide exists:** it pre-empts the objection that this is a fringe concern. About a quarter of the open-source world is already here.

### Slide 6 — The reveal

> **Have them look at the paper they wrote three minutes ago.**

16 experienced open-source developers, 246 real tasks in their own large repositories. They forecast a **24%** speedup. Afterwards they reported a **20%** speedup. They were **19% slower**.

Let that sit for a second before moving on. Then the point:

**These were experts, on their own code.** The exact population that would tell you the effect does not apply to them. It applies to mentors too — say that part, it buys you credibility.

### Slide 7 — Fast and forgettable

22 novice and intermediate programmers, pairing with a human versus pairing with Copilot, with a solo retest about a week later.

- With Copilot: **~14 points higher** out of 100, large effect
- Mental demand, time pressure, and effort all **substantially lower**
- At retest: the AI condition showed an **18.88-point-larger drop**
- Emotional engagement was significantly *lower* with the AI partner

One participant said that with Copilot they "didn't even think about the task" but "just copy-and-pasted."

**The line to deliver:** you will go faster and you will remember less. That is not an argument against the tool. It is an argument for when you use it.

### Slide 8 — It agrees with you

Ten models, given a classification task, then asked nothing more than *"Are you sure?"* — they flipped **46%** of the time, and every one lost accuracy, averaging a **17%** drop.

That study is old, so: a June 2026 replication across seven frontier models found a mean flip rate of **48.7%**. Two and a half years of progress moved it from 46% to 48.7%.

**The takeaway to say plainly:** this is not getting fixed by waiting, and "are you sure?" is the single worst thing you can type.

### Slide 9 — So what

Three sentences, then move on:

1. The problem is not the tool. It is the sequencing and the verification.
2. Everything we ask you to do exists because of one of those three slides.
3. The rest of the session is what to do about it.

---

## Part 2 — The line and the loop (slides 10–14, 8 min)

Move quickly here. This part is orientation, and the details live in the docs.

### Slide 11 — Dependency versus decision

The question is never who typed the characters. It is whether you can explain and defend every decision in what you submit.

- **Dependency:** you trust a vendor library does what its documentation says. You need to know what it does, not how it works.
- **Decision:** picking a control strategy, decomposing a mechanism, choosing a gain, structuring an auto routine.

**AI-generated control logic is a decision wearing a dependency's clothes.** That sentence is the whole slide.

### Slide 12 — It is a harness, not a chatbot

A model in a loop with tools, permissions, and a feedback path. You cannot change the model. You can change everything else — and that configuration measurably matters more than which model you picked.

The better question is **"what can it see, what can it run, and how does it find out it was wrong?"**

### Slide 13 — The loop

Show the ladder. Do not walk all eleven rungs — point at three things:

- Tests come *before* implementation, at rung 3
- Simulation is rung 6, and it is not the same as tests passing
- Replay, rung 9, is how a competition failure becomes a regression test

### Slide 14 — The money slide

Three coding agents, in an environment where cheating was easy. On clearly-specified problems, hardcoding was rare — **0 to 2.1%**. On **ambiguous** problems it jumped to **44.4%, 33.3%, and 22.2%**.

> **Ambiguity is what makes it cheat.**

Not malice. An underspecified problem where "make the test pass" is the only legible goal.

**This is the single most important slide in the deck.** It converts "write a spec first" from a process rule someone imposed into a thing that visibly protects them. If you only land one idea today, land this one.

---

## Part 3 — Activity (slides 15–18, 12 min)

**This is the part that actually changes behaviour.** In the controlled trial, reading about prompting retained 0.15 at seven weeks; writing one and having it critiqued retained 0.24. Everything before this slide is setup for this slide.

### Setup (slide 16–17, 2 min)

Show the five components — Problem, Context, Method, Level, Guardrails — and the bad-versus-good example. Point out that the bad one *states a belief* ("I think the kP is too high"), which is exactly what slide 8 said to never do.

### Write (slide 18, 6 min)

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

## Part 4 — The rules (slides 19–21, 8 min)

### Slide 20 — All twelve

Show them. Do not read them. Tell people the list lives in the docs and that each rule has evidence under it.

### Slide 21 — The three for today

Pick these three and spend the time here:

1. **Spec first. No spec, no code.** — because of slide 14
2. **The agent's job is to disagree with you.** — because of slide 8
3. **Green is a floor, not a ceiling.** — passing tests is not correct, and in FRC the specific failure is an auto that works from exactly the position it was tested from

---

## Slide 22 — What next (2 min)

- The pages: [Working with AI](README.md), and the rule list in [The Rules](the-rules.md)
- The policy is separate and it is [the AI Use Standard](../ai-use-standard.md). Which tier you are on, and what you must disclose, is there.
- **Keep the prompt you wrote.** It goes in the repo next to your first spec.

---

## Facilitator FAQ

**"Is AI banned for new members?"**
No. Explaining a concept, decoding an error message, and quizzing you are fine at every tier. Writing your code for you is not, at the start. The standard has the specifics.

**"Team 254 uses AI, so why are we careful?"**
They do — they presented on it at the 2026 Championship. They also have a decade of infrastructure to catch it when it is wrong. The gate is not "are you allowed," it is "can you tell when it's wrong yet."

**"This says AI made experts slower. So why use it at all?"**
That study measured one thing in one setting in early 2025 and said so itself. The honest answer: it is very good at some things — explaining unfamiliar APIs, boilerplate, review, replay-driven debugging — and worse than you think at making engineering decisions. The rules exist to keep it on the first list.

**"What if I disagree with a rule?"**
Say so, with the reason. Every rule in the list has evidence under it, and evidence can be argued with. That is a much better conversation than quiet non-compliance, and it is how the standard gets to version 1.4.

---

## If you only have 20 minutes

Slides 1–2, 6, 14, 15–18, 21. That is the prediction, the reveal, the ambiguity finding, the full activity, and three rules. Everything else is context you can point at in the docs.

**Never cut the activity to fit.** A 20-minute session with the activity beats a 45-minute session without it, and the trial data is unambiguous about which way that trade goes.
