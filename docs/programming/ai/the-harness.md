# The Harness

The tool is not a chatbot. Understanding what it actually is changes which questions are worth asking about it.

---

## What a harness is

A **coding agent** is a language model running in a loop with tool access, aimed at a goal:

```
1. Send the model the conversation so far
2. Read the response; find any tool calls in it
3. Ask permission if the tool is dangerous
4. Run the tools; put the results back in the conversation
5. Not done? Go to 1.
```

The **harness** is everything in that loop that is not the model: the tool definitions, the system prompt, how context gets built, the permission gates, and the path by which the agent finds out it was wrong. Claude Code, Codex CLI, Cursor Agent, and Gemini CLI are all harnesses in this sense.

The harness is the part you control. You cannot change the model's weights. You can change every other thing in that list.

---

## Why this matters more than model choice

A 2026 study compared plain text search (`grep`) against semantic vector search across four different harnesses, on the same 116 questions, with the same underlying data. The only thing that varied was the plumbing: whether tool output was placed directly into the context, or written to a file the agent had to go read.

The result: `grep` consistently beat vector search **and** overall scores depended so strongly on the harness and tool-calling style that file-based delivery could *invert or erase* the advantage, with no change to the data at all.

Read that twice. Same data, same model, same question, and the plumbing decided the winner.

**What this means for you.** "Which model should I use?" is a worse question than:

> What can this agent see? What can it run? How does it find out it was wrong?

The third question is the one that decides output quality, and in FRC we are unusually well set up to answer it.

---

## The FRC feedback ladder

An agent loop is only as good as the signal that comes back. In most software, that signal is a flaky test suite. Ours is better, and you already have every piece of it.

| Signal | Where it comes from | Latency | What it catches |
|---|---|---|---|
| `./gradlew compileTestJava` | the build | seconds | API drift, deprecated WPILib |
| `./gradlew test` | the JUnit suite | seconds | logic, edge cases, contract violations |
| `spotlessCheck` | the formatter | seconds | style, so review time goes to substance |
| WPILib simulation | the sim harness | seconds–minutes | integration, scheduler conflicts |
| Deterministic log replay | AdvantageKit | faster than real time | **behaviour against a real match** |

Nobody outside FRC has the last row. AdvantageKit records every input flowing into the robot code each loop cycle (sensor values, joystick state, NetworkTables updates) so the full state of the code can be replayed in simulation from a log file, deterministically, faster than real time.

That is a verification oracle an agent can drive. "Change this, replay Quals 42, show me the flywheel velocity trace" is a closed loop with ground truth in it. Very few professional software teams have anything that good.

See [The SDD Loop](the-loop.md) for the standing replay procedure.

---

## Guardrails that do not depend on the model behaving

Most of our guardrails are *prompt-level* or *human-level*: tiers, disclosure blocks, defending your diff in review. Those are necessary. They are not sufficient, because a prompt is a request and a person under deadline pressure is a poor enforcement layer.

Harnesses expose **deterministic gates**. In Claude Code these are hooks: shell commands that fire at fixed points in the lifecycle, independent of what the model decides. A `PreToolUse` hook fires before a tool call and can block it; an exit code of 2 blocks unconditionally, and even a JSON response saying `"allow"` cannot override it.

The distinction is the part worth memorizing:

> **A prompt asks. A hook enforces.**
>
> If a rule matters, it goes in a hook, in CI, or in a test — not in a paragraph the agent is asked to remember.

### A starting set for any Ignite repository

| Hook | Blocks | Why it exists |
|---|---|---|
| `PreToolUse` on `Edit`/`Write` | `src/test/` once implementation starts | You don't let the agent rewrite the net that catches it |
| `PreToolUse` on `Bash` | `git push`, `git commit --no-verify` | Disclosure and review are not optional |
| `PostToolUse` on `Edit` | runs the formatter on the touched file only | Keeps format noise out of the diff |
| `SessionStart` | prints your current tier | A standard is invisible if it lives only in a document |

That last one is the smallest and probably the most valuable. A tier you see every session is a tier you follow.

---

## Security: the part people skip

A 2026 systematic analysis of prompt-injection attacks against agentic coding assistants examined skills, tools, and the Model Context Protocol ecosystem. It found that **85%+ of identified attacks successfully compromise at least one major platform**, and that adaptive attacks achieved **78–93% success against every published defense** it evaluated.

Named attack shapes include:

- **Tool poisoning** — malicious instructions hidden in a tool's *description*
- **Rug pulls** — a tool's behaviour changed after you approved it
- **Tool squatting** — a name-alike entry in a registry
- **Context contamination** — corrupted metadata shadowing a legitimate tool

Their platform ranking put Cursor at Critical (auto-approve on, unsandboxed), GitHub Copilot at High, and Claude Code at Low (mandatory tool confirmation, sandboxed by default).

### The Rule of Two

The most useful single mitigation, from Meta, is this: an agent should not simultaneously do all three of the following. Pick at most two.

1. **Process untrusted input** — a Chief Delphi thread, a vendor changelog, another team's repo
2. **Access sensitive data** — scouting data on other teams, sponsor material, credentials
3. **Communicate externally** — `git push`, posting to Slack, opening a PR

An agent session doing all three at once is the shape of every incident in that paper. "Don't do all three at once" is a rule you can hold in your head, which is more than can be said for most security guidance.

This also upgrades a rule you already have. "Never paste anything you would not want public" is correct, and it is framed as a discipline problem. It is also an architecture problem, and the architecture version is enforceable.

---

## What to take away

1. The harness is a system you configure, not a chatbox you type into.
2. Configuration beats model choice. Spend your attention accordingly.
3. FRC has an unusually good feedback signal. Use it. It is what makes agent output checkable.
4. If a rule matters, a machine enforces it.
5. Never let one session touch untrusted input, sensitive data, and the outside world at the same time.
