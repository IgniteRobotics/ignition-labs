# Context & Prompts

Everything you put in front of the model competes for the same finite resource. Most people find this out by accident, when answers get worse and nothing announces why.

---

## Prompting is half the skill

**Prompt engineering** is writing and organizing instructions well. It is discrete. You write one, you send it.

**Context engineering** is curating the whole set of information the model sees during a session: the system prompt, the tools, the files, the conversation history, the tool output. It is iterative, because the curation decision happens *every single turn*.

If you only think about the prompt, you are managing about half of what determines the answer.

---

## Why long context makes answers worse

The mechanism is architectural. Attention requires every token to relate to every other token, giving n² pairwise relationships for n tokens. Models therefore have an **attention budget**, and every new token spends some of it.

> Context is a finite resource with diminishing marginal returns.

The empirical version is called **context rot**. A study of 18 frontier models (GPT-4.1, Claude 4, Gemini 2.5, Qwen3) across needle-in-a-haystack tasks, long conversational question-answering, and a task as trivial as repeating words back found that performance varies significantly as input length changes, **even on simple tasks**.

Specifics worth knowing:

- Degradation is **worse when your question and the answer share little vocabulary**, which is exactly the case where you are asking about something you cannot already name.
- **A single distractor** measurably degrades performance versus a clean baseline. Multiple distractors compound it, unevenly, and the effect amplifies at longer contexts.
- Focused prompts of about 300 tokens beat full prompts of about 113,000 tokens, substantially, on the same underlying task.
- **Shuffling the material to destroy its logical flow consistently *improved* performance.** Nobody fully knows why. These are empirical objects, not understood ones.

**The FRC translation:** pasting the entire `RobotContainer.java`, three subsystems, and a Chief Delphi thread into the window spends the attention budget on distractors, and then you wonder why the answer is worse than it was yesterday. Volume is not thoroughness.

---

## Four techniques for work that outlasts one window

1. **Compaction** — summarize the conversation and start fresh with the summary. Watch out: aggressive compaction loses subtle but critical context.
2. **Structured note-taking** — write notes to a file outside the window, pull them back later. We already do this in season; it is called the tuning log. Naming it as note-taking makes the idea transfer in both directions.
3. **Sub-agents** — a specialized agent works in a clean window and returns only a condensed summary, often 1,000–2,000 tokens. See [Research & Team Memory](research-and-memory.md).
4. **Just-in-time retrieval** — hold lightweight identifiers (file paths, queries, links) and load the actual content at runtime, instead of pre-loading everything. This is how you already work. You do not memorize the codebase; you remember where things are.

---

## Clear context at the boundaries

Two hard boundaries, every time, from Article VIII of the constitution:

- Between **research** and **spec writing**
- Between **spec writing** and **implementation**

Each transition changes what counts as signal. The dead ends from a research session are distractors while you are writing requirements. The deliberation behind a requirement is a distractor while you are implementing it.

> **What crosses a boundary is the written artifact, never the conversation.**
>
> If a fact did not make it into the document, it does not survive the boundary. That is the forcing function, not a side effect.

---

## The prompt is an artifact, not a chat message

The teaching evidence behind this section is the strongest on any of these pages.

A semester-long randomized controlled trial in an introductory CS course put 979 students across four conditions of increasing engagement, all teaching the same five-component framework.

| Condition | Engagement | Time | Immediate gain | Gain at 7 weeks |
|---|---|---|---|---|
| Text reminder (a policy) | passive | <1 min | 0.08 | 0.06 |
| Scenario-based reading | passive | ~3 min | 0.39 | 0.15 |
| Select-only | active | ~11 min | 0.45 | 0.17 |
| **Select-then-write** | **constructive** | **~37 min** | **0.51** | **0.24** |

Three things fall out of that table:

1. **Publishing a policy is worth almost nothing.** A text reminder, which is structurally what a written standard *is*, produced a gain of 0.08. Reading these pages is not the same as practising this.
2. **Writing a prompt and having it critiqued retained 0.24 at seven weeks; reading about prompting retained 0.15.** The best condition cost 37 minutes, which is one meeting.
3. **Everything decays.** The best condition lost more than half its gain in seven weeks, and seven weeks is about a third of a build season. Practise it in ordinary code review, or it goes away.

---

## The five components

Every good prompt has these. The one people skip is the last one, and it is the one that encodes your tier.

| Component | What it answers |
|---|---|
| **Problem** | What is actually wrong, stated as a symptom |
| **Context** | Where in the code, which versions, what reproduces it |
| **Method** | How you want it to work — explain first? diagnose only? |
| **Level** | What you already know and what you do not |
| **Guardrails** | What it must not touch, and what it must cite |

### A worked example

```
Problem:    Indexer jams when two game pieces enter within one loop cycle.

Context:    frc.robot.subsystems.indexer, WPILib 2026, command-based,
            TalonFX + beam break. Reproduces in sim: IndexerJamTest,
            currently red.

Method:     Explain the failure first. Do not write code until I say so.

Level:      I have written the tests for this and I can read the sim output.
            I have not built a state machine before.

Guardrails: Do not modify anything under src/test/. Do not touch gains.
            Cite the current WPILib docs for any API you use.
```

That is not a chat message. That is a document, and it belongs next to the spec: committed, red-lined in review, versioned like anything else.

### What a bad version looks like

```
my indexer keeps jamming, can you fix it? I think the kP is too high
```

Four problems, and every one of them is covered above:

1. No context: the model does not know the class, the hardware, or the version.
2. No method: "fix it" means it will write code immediately.
3. No level: it cannot calibrate the explanation.
4. **It states a belief.** "I think the kP is too high" is exactly what sycophancy latches onto, and now you are likely to get agreement instead of diagnosis. See Rule 5.

---

## What to take away

1. Context is finite, and long context makes answers *worse*, not just slower.
2. Give the smallest sufficient context, not the largest available one.
3. Clear context at the research → spec and spec → implementation boundaries.
4. Write prompts with all five components. Never leak the answer you expect.
5. Reading this page retains about 0.15. Writing one prompt and having a partner red-line it retains about 0.24. Go do that.
