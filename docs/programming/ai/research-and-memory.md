# Research & Team Memory

This is the page about the problem no software company has as badly as we do.

A company loses maybe 15% of its engineers a year. We lose 100% of our seniors every year, and 100% of everybody within four. There is no five-year veteran. The mentor is the only continuous memory, and mentors turn over too.

---

## What we rediscover every season

Why the 2024 shooter oscillated. Which CAN ID convention we use and why. That a specific vendor library needed a specific workaround. What the swerve offsets mean. Why we abandoned an approach in Week 3 that looks appealing in Week 1.

We already produce the raw material and mostly throw it away:

| Artifact | Exists at | Currently lives | Survives graduation? |
|---|---|---|---|
| OpenSpec changes + archive | every change | repo | **yes** |
| Tuning logs | in season | repo | **yes** |
| Pit documentation | competition | repo | yes |
| PR disclosure blocks | every PR | GitHub | partially |
| Design decisions, rejected options | — | somebody's head | **no** |
| Match observations, failure modes | — | Slack scrollback | **no** |
| "Why we did it that way" | — | the senior who left | **no** |

The bottom three rows are the ones that hurt.

---

## Do not build a graph database

You will read about knowledge graphs and be tempted. Here is why the answer is no.

The expensive version costs what its name suggests. Microsoft's GraphRAG builds an entity graph over a corpus using a language model, then pre-summarizes it. It measurably improves answer comprehensiveness and diversity over plain search. It also costs so much to index that Microsoft published a follow-up, LazyGraphRAG, specifically to fix that: LazyGraphRAG matches plain search's indexing cost, which is **0.1% of the cost of full GraphRAG**, while reaching comparable answer quality at **more than 700 times lower query cost**.

A thousand-fold cost reduction is not a tuning improvement. It is an admission the original approach was not viable for most corpora, and our corpus is a few thousand Markdown files.

**And plain search wins anyway.** The harness study in [The Harness](the-harness.md) found `grep` beating vector search across every harness tested, with delivery mechanics mattering more than the retrieval strategy either way.

> **The knowledge graph a high school robotics team should build is a directory of structured Markdown files with explicit links, in the repo, searchable with `grep`.**

The "graph" is the link structure. The "index" is the filesystem. The "retrieval engine" ships with the operating system. It costs nothing, it works offline in a pit with no wifi, it survives any vendor going away, and a fifteen-year-old can review it in a pull request.

It is also the better *learning* answer, which is what decides it. You can read the whole knowledge base. Nobody can read an embedding.

---

## What to build instead

```
knowledge/
  decisions/     one file per engineering decision: context, options, choice, why
  mechanisms/    one per subsystem, across seasons: geometry, gains, failure modes
  incidents/     one per competition failure: symptom, diagnosis, fix, prevention
  research/      one per investigation, with citations
  vendors/       CTRE/REV/PhotonVision workarounds, pinned to versions
  seasons/       retrospectives
  INDEX.md       the hand-maintained entry point
```

Frontmatter carries the links and the time information:

```yaml
---
id: dec-2026-swerve-offset-convention
type: decision
season: 2026
status: active          # active | superseded | abandoned
supersedes: [dec-2025-swerve-offset-convention]
valid_from: 2026-01-10
valid_until: null       # set when superseded
relates_to: [mech-swerve-module, inc-2026-week3-drift]
---
```

`valid_until` is the field that matters most and gets filled in least. A gain that was right in Week 4 and superseded in Week 5 is a *fact with an expiry date*, and recording it as timeless is how next season inherits a wrong number.

### Two rules make it self-maintaining

1. **Definition of done gains a row.** A change that makes an engineering decision writes or updates a `decisions/` file. The spec says *what*; the decision file says *why this and not that*. They are different documents and the second is the one that is missing.
2. **Competition procedure gains an incident rule.** Anything that goes wrong at an event gets an `incidents/` file **before the team leaves the venue.** Week 6 memory is unreliable and Week 40 memory is fiction.

**If the corpus ever outgrows `grep`**, realistically several seasons out if ever, LazyGraphRAG over the same Markdown is an additive change, because the Markdown was the source of truth all along. That is the actual design win: the cheap answer is also the migration path.

---

## Research notes

Rung 1b of [the loop](the-loop.md) is optional. Its output is not.

> **Research that leaves no document did not happen.**

```markdown
---
id: res-2026-swerve-setpoint-optimization
type: research
question: Does module setpoint optimization change behaviour when the drivebase
          is commanded through a discontinuity in heading?
status: answered          # answered | partial | dead-end
sources_verified: 2026-01-14
---

## Question
[one paragraph — what we did not know, and why it blocked the change]

## Answer
[the short version, up front, with confidence stated]

## What we verified, and how
[commands run, output observed, sim results — evidence, not recollection]

## What we could not verify
[stated plainly; this section is never empty by default]

## References
[links, with version and access date for anything version-dependent]
```

`sources_verified` exists because a research note's shelf life is set by the things it cites, not by when it was written.

The "what we could not verify" section is not optional and is rarely empty. An unverified claim recorded as unverified is useful. The same claim recorded as fact is a trap someone walks into in Week 5.

---

## Skills: packaging what we already wrote

A **skill** is a directory with a `SKILL.md` at its root that an agent loads only when it is relevant. The design property that matters is progressive disclosure:

| Level | Loaded | Token cost | Content |
|---|---|---|---|
| Metadata | always, at startup | ~100 per skill | name + description |
| Instructions | when triggered | under 5k | the `SKILL.md` body |
| Resources | as needed | none until read | bundled files; scripts return only their output |

You can install many skills without paying for the ones you do not use. And when a skill runs a script, the script's code never enters the context window. Only its output does.

That is the attention-budget argument from [Context & Prompts](context-and-prompts.md), turned into a file format.

### Skills worth writing

| Skill | What it bundles | Why a skill and not a prompt |
|---|---|---|
| `wpilib-2026` | current API reference, deprecations by season | Attacks our most-reported real failure: confidently wrong old APIs |
| `ignite-spec` | the spec template, the delta format, a validate script | The team format, enforced deterministically |
| `ignite-review` | our checklist for reading generated code | The checklist is currently prose nobody re-reads |
| `tuning-log` | the tuning-log schema + an append script | Article VIII, made mechanical |
| `scouting-query` | match-data schema + query scripts | The strategy subteam gets a tool, not a chatbot |

Notice the pattern: every one of these is something we already wrote as prose. A skill is mostly a packaging decision about documentation you already own.

### Security, non-negotiable

The vendor documentation is blunt about this: use skills only from trusted sources, because a malicious skill can direct an agent to invoke tools or run code in ways that do not match its stated purpose. Skills that fetch external URLs are called out as particularly risky.

> **Ignite skills live in an Ignite repository and are reviewed like code. We do not install skills from the internet.**

---

## Subagents, and when to fan out

A subagent is worth reaching for when a side task would flood your main conversation with search results or file contents you will never reference again. It works in its own context window, with its own tool list, and returns only a summary.

The underrated field is the tool list. A subagent restricted to read-and-search tools *cannot edit anything*, which is a far stronger statement than any instruction in a prompt. A read-only reviewer is a review that structurally cannot become a rewrite. That is Rule 6, implemented in a config file instead of a culture.

### When not to fan out

Multi-agent setups get oversold, so here are the numbers.

Anthropic's own multi-agent research system outperformed its single-agent equivalent by **90.2%** on research tasks. It also used about **15 times more tokens than a chat**, and token usage alone explained **80% of the performance variance**. Their follow-up guidance puts typical multi-agent overhead at **3–10× single-agent** for equivalent tasks, and says outright that most coding tasks involve fewer independent pieces than research does.

The decomposition rule is: split by context boundaries, not by problem type. Sequential phases (plan, implement, test) create coordination overhead and should stay in one agent.

> **Fan out for search. Stay single-agent for building.**
>
> Three agents reading three vendor changelogs in parallel: good. That is independent work with a context boundary between each.
>
> Three agents building three subsystems that share `RobotContainer`: bad, and for the same reason we do not put three people on it.

Rung 1b, research, is the one rung where fanning out actually pays. The building rungs are not.

The 90.2% is the number someone will quote at you. The 15× belongs on the same slide.
