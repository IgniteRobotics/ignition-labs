# Working with AI

FRC 6829 · Programming subteam

How to use AI coding tools on real Ignite work — the robot repo, the scouting app, anything the subteam ships.

---

## Start here

These pages are **practice**, not policy. The policy is [the AI Use Standard](../ai-use-standard.md): what you are allowed to use, at which tier, and what you have to disclose. These pages are what to actually *do* once you are allowed.

They are also **not a milestone.** Nothing here has a test-out and nothing here gates anything. The milestone ladder is self-guided and stays that way. Read these when the thing they describe is in front of you.

| If you are | Read this first |
|---|---|
| Brand new, nowhere near using an agent | [The Rules](the-rules.md) — so the restrictions make sense before they annoy you |
| About to configure a tool for the first time | [The Harness](the-harness.md) |
| Writing a spec, or about to | [The SDD Loop](the-loop.md) |
| Getting worse answers than you did last week | [Context & Prompts](context-and-prompts.md) |
| Trying to make knowledge survive graduation | [Research & Team Memory](research-and-memory.md) |
| Running the 45-minute session | [Session Guide](session-guide.md) |

---

## The one thing to understand first

**FIRST has no rule about this.** We checked the 2026 Game Manual directly — searching the full manual for *artificial intelligence*, *AI*, *generative*, *machine learning*, *LLM*, *ChatGPT*, and *Copilot* returns exactly one hit, and it is a stray image caption reading "AI-generated content may be incorrect."

There is no rule. Nobody is coming to enforce anything.

So everything in these pages, and everything in the standard, is **Ignite deciding on purpose** — before the pressure of a build season decides for us. When somebody tells you a rule here is stricter than it needs to be, they are not wrong about the rule. They are wrong about who wrote it and why.

---

## What these pages cover

**[The Harness](the-harness.md)** — An AI coding tool is not a chatbot. It is a model running in a loop with tools, permissions, and a feedback path, and *that configuration* is what decides output quality — measurably more than which model you picked. Covers the agent loop, the FRC feedback ladder, hooks as enforcement, and the security posture that keeps a session from doing something expensive.

**[The SDD Loop](the-loop.md)** — Spec-driven development is not "write a document, then code." It is a loop with a verification ladder inside it: explore, optionally research, propose, red, apply, green, simulate, bench, field, replay, review, archive. Covers where tests and simulation actually sit, and the measured reason a vague spec makes an agent cheat.

**[The Rules](the-rules.md)** — Twelve standing rules, each with the evidence under it, plus a copyable twelve-article constitution for any Ignite repository. Includes the one most people get wrong: an agent that never tells you you are wrong is not being helpful.

**[Context & Prompts](context-and-prompts.md)** — Context is a budget and you spend it whether you notice or not. Covers why long context makes answers *worse*, the five-component prompt, and when to clear context.

**[Research & Team Memory](research-and-memory.md)** — Skills, subagents, and the knowledge base. This is the page about the problem no software company has as badly as we do: we lose 100% of our people every four years.

---

## The short version

If you read nothing else on these pages, read this.

> 1. There is always a constitution.
> 2. Spec first. No spec, no code.
> 3. One format, and it is OpenSpec.
> 4. Tests before implementation. Nothing edits them once implementation starts.
> 5. The agent's job is to disagree with you. It flips about half the time you push — so stop pushing.
> 6. Never let it grade its own homework.
> 7. Green is a floor, not a ceiling.
> 8. Context is a budget — and it gets cleared at every boundary.
> 9. Simulate before you deploy. Replay after you compete.
> 10. Enforce with machines. Request with prose.
> 11. Disclose, and defend.
> 12. Nothing leaves undocumented.

Each one is explained, with the evidence, in [The Rules](the-rules.md).

---

## Where the numbers come from

Every claim on these pages that has a number attached is sourced. The research behind them — 40 sources, with an audit of what could and could not be verified — is a design record kept with the team's engineering notes rather than on this site. Ask a mentor if you want to read it.

Two things it is honest about, and so are we:

- **Nobody has run a controlled trial on this whole workflow.** Each rule is individually evidenced. The assembled thing is a synthesis.
- **No other FRC team has published anything like this** that we could find. If you find one, that is genuinely useful information — tell a mentor.
