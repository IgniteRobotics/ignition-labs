# Spec-driven development, and what it would do to M7 — 2026-08-21

**Status: research report, decisions recorded (§9). Nothing in `docs/` or `projects/` has
been changed.** The four questions this report opened were answered on 2026-08-21 and §7
is written to match; the next step is a design record under `superpowers/specs/`, then
implementation.

**Recommendation up front: adopt OpenSpec's *format and loop*, make its *CLI optional*,
and add roughly one page to M7 plus one new given file.** Not Spec Kit, not BMAD, and —
this is the surprising one — not Superpowers, even though this repository is itself built
with a Superpowers-derived workflow. Reasoning in §5. Confidence is moderate-to-high on
the framework choice and lower on the session-cost estimate, which is the number most
likely to be wrong.

---

## 1. What M7 currently teaches, and the hole in it

M7 is in good shape. It teaches, in order: what a language model actually is, the
interface-vs-decision trust line, prompting as a skill, AI-as-reviewer, the named failure
modes (shepherding, drifting, false confidence), and a seven-point checklist for reading
generated code. Then the project: pick a real feature, use AI substantially, test it,
disclose it, **defend the diff**. Then the miscalibration exercise.

Every one of those is downstream of code existing. Read the milestone's own project steps:

> 1. Pick a real, useful feature. Not a toy
> 2. Use AI assistance genuinely and substantially.

**There is nothing between step 1 and step 2.** A student goes from a `FEATURE-BACKLOG.md`
entry — one or two sentences — straight to a prompt. Everything M7 teaches about quality
control fires *after* the agent has already made the engineering decisions.

That gap is exactly where the observational evidence says novices lose. In a 2026
think-aloud study of 19 students (9 CS1, 10 upper-division SWE) building an app with an
agentic tool, the introductory students issued **low-context prompts at 28.89% versus
8.39%** for the advanced students; **61.01% of all prompts were debugging** rather than
construction; **2.23% of testing touched edge cases**; **90.37% of code interactions were
interpreting rather than editing**; and **4 of 19 students restarted their entire project
mid-session** under accumulated agent-generated complexity. That last statistic is
`drifting`, measured. M7 names drifting and tells the student to notice it. It does not
give them a structure that prevents it.

The other hole is in the assessment. M7's assessment is the defense review, and the
defense review is **retrospective**. The student is asked "where does this number come
from?" about a decision that may have been made by an agent forty minutes earlier. A
sufficiently articulate student — or one who steps out and asks a chatbot to justify the
diff — can reconstruct a defense after the fact. `ai-use-standard.md` §7 is honest that
the whole standard runs on this mechanism:

> That is the actual mechanism this entire standard runs on. Everything else is scaffolding
> around it.

A mechanism that load-bearing should have a second, tamper-evident leg.

---

## 2. What spec-driven development actually is

Birgitta Böckeler's definition, in Fowler's *Exploring Gen AI* series, is the one worth
using because it is the least breathless: SDD is "writing a 'spec' before writing code
with AI ('documentation first')," where a spec is "a structured, behavior-oriented
artifact… written in natural language that expresses software functionality and serves as
guidance to AI coding agents."

She splits it into three levels, and the split matters more than any tool choice:

| Level | What it means | Fits Ignite? |
|---|---|---|
| **Spec-first** | Spec guides initial development, then is discarded | **Yes — this is the one** |
| **Spec-anchored** | Spec persists through maintenance and feature evolution | Maybe, later, for `Robot-2026` |
| **Spec-as-source** | Spec is the artifact; humans never edit generated code | **No.** Directly contradicts §3 |

Spec-as-source is where the vendors are pushing (Tessl literally marks generated files
`DO NOT EDIT`). It is incompatible with this program: a student who never edits code is a
student who did not learn to write code, which is the failure `ai-use-standard.md` §1
exists to prevent. **If M7 adopts SDD, it adopts spec-first and says out loud why it stops
there.** That paragraph is worth writing regardless of which tool wins, because students
will meet spec-as-source tooling within a season and should know where Ignite's line is.

SDD emerged in 2025 as the industry's answer to vibe coding — agents producing plausible
code that drifts from intent and hallucinates APIs. That is the same failure list M7
already teaches, arrived at from the professional side rather than the education side.
The convergence is the reason this is worth doing at all.

### The critical case, stated fairly

The skeptics are not wrong and M7 should carry their argument, not hide it.

- **Overhead scales badly downward.** Böckeler on Kiro: "like using a sledgehammer to
  crack a nut" — a small bug fix inflated into four user stories and sixteen acceptance
  criteria. M7 features are *deliberately* small (`FEATURE-BACKLOG.md` §"What makes a
  feature M7-sized"). This is the single biggest risk to the idea.
- **Reviewing specs can be worse than reviewing code.** Böckeler again, on Spec Kit: "I'd
  rather review code than all these markdown files."
- **Agents ignore the spec anyway.** Despite elaborate prompts and checklists, agents
  "frequently ignored instructions or over-interpreted them."
- **The MDD parallel.** Model-driven development failed on inflexibility; SDD may inherit
  MDD's rigidity *plus* LLM non-determinism. Böckeler's word for the risk is
  *Verschlimmbesserung* — making it worse by trying to improve it.
- **Spec maintenance is a real tax**, and specs rarely land as written: constraints emerge
  within an hour of building that the spec never anticipated.

Every one of those is an argument for **the lightest possible framework**, and several are
arguments for **no framework at all, just a template**. Both readings are live; see §5.

---

## 3. Why it fits *this* program specifically

Three reasons, in descending strength.

**A spec is where the decisions live.** `ai-use-standard.md` §3 draws the line at
dependency-versus-decision, and then enforces it socially ("can you defend it"). A spec
makes that line *physical*. Control mode, subsystem placement, boundary behaviour,
interruption semantics, where every number comes from — those are spec contents. The
implementation is dependency-shaped. **SDD gives §3 a file.** Right now §3 is a
principle with no artifact, which is why it has to be re-argued in every defense review.

**A spec written before the code is tamper-evident authorship.** A commit that lands a
`proposal.md` before any Java changes is timestamped evidence that the student made the
decisions, not a post-hoc reconstruction. It converts the defense review from "can you
explain this?" into "you wrote this down before you built it — walk me through where the
build diverged from it, and why." That second question is strictly harder to fake and
strictly more useful, because **the divergence is the interesting part.** Every real
project diverges from its spec; a student who can say *where* and *why* has demonstrated
exactly the engineering judgment M7 claims to assess.

**It converts the best prompt shape into a habit.** M7 already identifies the highest-value
prompt: "Here is my subsystem. What is wrong with it?" — because "you keep authorship and
gain a reviewer." A spec generalises that to the whole feature. You author the contract;
the agent implements against it; you review implementation against a standard *you* set.
That is AI-as-reviewer-and-implementer with the human in the decision seat, which is the
posture the whole document argues for.

There is also a fourth, weaker reason worth naming honestly: **spec-writing is the skill
the field is converging on.** Computing-education commentary in 2026 is broadly agreed
that the bottleneck moved from producing code to "prompting, orchestrating, and
validating," and that students must learn to "supervise, constrain, and critically and
ethically audit semi-autonomous computational agents." Weak because it is fashion-adjacent
and M7's existing content already covers validating. It is the *constraining* half that is
missing.

---

## 4. The framework landscape, as of today

Verified against the GitHub API on 2026-08-21.

| Framework | Stars | License | Install cost | Artifacts | Weight | Agent lock-in |
|---|---:|---|---|---|---|---|
| **OpenSpec** | 65.8k | MIT | `npm i -g @fission-ai/openspec`, Node ≥20.19 | `openspec/specs/`, `openspec/changes/<name>/{proposal,design,tasks}.md`, `archive/` | **Light** — "no phase gates" | None; 30+ tools, plain markdown |
| **GitHub Spec Kit** | 130.6k | MIT | `uv tool install specify-cli`, Python ≥3.11 + `uv` | constitution, spec, plan, `tasks.md`, `.github/skills/*/SKILL.md` | Moderate — 5 ordered commands | Low; Copilot-first, 30+ agents |
| **Superpowers** | 275.4k | MIT | plugin install per harness | `SKILL.md` skills; design + plan docs on disk | Moderate — TDD + review gates | **Harness-coupled** (14 harnesses, each a separate install) |
| **BMAD-METHOD** | 52.1k | non-standard | `npm i bmad-method` | `SPEC.md`, `prd.md`, `ARCHITECTURE-SPINE.md`, epics, `stories.yaml` | **High** — phase-locked, 12+ persona agents | Installs its own agent fleet |
| **GSD Core** | — | — | repo install | `CONTEXT/RESEARCH/PLAN(XML)/VERIFICATION/UAT/STATE.md` | **Highest** — blocking research gates | Multi-runtime |
| **Kiro** (AWS) | — | commercial | IDE | Requirements → Design → Tasks | Moderate, but mandatory | **IDE-locked** |

Positioning, from the clearest of the comparison writeups: "Specification frameworks (a
portable spec format): OpenSpec, GitHub Spec Kit. Full project methodologies (idea → code,
phase-gated): BMAD Method, GSD Core. Skills / workflow layer (not a spec schema):
Superpowers."

**That taxonomy decides most of this.** M7 needs a portable spec format. It does not need
a project methodology, and it does not need a skills layer.

---

## 5. Recommendation: OpenSpec, format-first, CLI optional

### Why OpenSpec

1. **It is a delta framework, and M7 is a delta.** OpenSpec's change model "captures only
   what's changing, rather than trying to document the entire system up front." M7 adds
   one small feature to `Robot-2026` — a 46-Java-file brownfield repository a student did
   not write. Spec Kit's constitution-then-specify flow is greenfield-shaped; asking a
   student to write a constitution for someone else's season repo is theatre. Every
   head-to-head comparison found lands the same way: "OpenSpec wins on brownfield. Spec
   Kit is the safest bet for a scaling team." Ignite is not a scaling team.

2. **It is tool-agnostic, and Ignite's tooling is not stable.** The team runs a
   Gemini-based reviewer, `ai-use-standard.md` §11 notes OpenAI began offering Codex seats
   to FRC teams in May 2026, and students will show up with Copilot, Claude, and whatever
   ships next. OpenSpec is plain markdown with slash-command shims for 30+ tools and needs
   no API key and no MCP. **A student on a school Chromebook with Copilot and a student on
   Claude Code write the same file.** Spec Kit is workable here but Copilot-first;
   Superpowers is a per-harness plugin install, which is a support burden in a shop with
   fifteen laptops and one mentor.

3. **It is the lightest thing that is still a format.** No phase gates, fluid updates any
   time, a change folder with a proposal and a task list. That is about the maximum
   ceremony a 2–3 session milestone can absorb, and it is well under Böckeler's
   sledgehammer threshold.

4. **The artifacts map onto files M7 already has.** `proposal.md`'s "why this matters /
   what's changing / technical approach" is the PR template's *What this changes / Why*
   plus the parts `DEFENSE-REVIEW.md` asks about. `tasks.md` is the checklist a student
   currently keeps in their head. This is not a new layer; it is the existing layer
   written down before instead of after.

5. **MIT, actively developed** (last push 2026-08-21), and the format survives the tool
   dying — which matters for a document that already knows it expires (§11).

### Why the CLI should be optional, not required

`CLAUDE.md` pins this repository's toolchain deliberately and warns against bumping it as
part of a feature. Adding **Node ≥20.19 as a prerequisite for completing a curriculum
milestone** is a real cost: WPILib lab machines carry a Java toolchain and nothing else,
and a student blocked on an npm install has lost a session to something that was never
their problem — the same failure mode the clean-clone invariant exists to prevent.

The good news is that OpenSpec's CLI is **convenience, not substrate**. `openspec init`
scaffolds folders and slash commands; `validate`, `list`, and `view` are inspection tools.
The workflow — write the spec, hand it to the agent, implement, archive — is a folder of
markdown. **M7 should teach the format, ship a filled-in worked example, and mention the
CLI as an optional accelerant for students who want it.** That also happens to be the
right fade level: M7 is L0, worked-example-only.

### Why not the others

**Not Spec Kit** — despite being the biggest and GitHub-native. Greenfield-shaped,
requires Python 3.11 + `uv` on top of the Java toolchain, and produces enough markdown to
draw the "I'd rather review code than all these markdown files" complaint from a
practitioner far more patient than a sophomore. Its **constitution** concept is genuinely
good and Ignite half-has one already: `code-review-prompt.md` is described in M7's own text
as "the closest thing the team has to a style guide." That is a separate, worthwhile
project — promoting those standards out of a bot prompt into a real document — and it does
not require adopting Spec Kit to do it.

**Not BMAD, GSD, or Kiro.** Phase-gated multi-agent methodologies aimed at multi-week
enterprise features. BMAD reportedly runs $800–2,000/developer/month in frontier-model API
cost, which settles it. Kiro is IDE-locked, which violates no-required-tool.

**Not Superpowers — and this needs explaining, because it is the incumbent.** This
repository is *already* authored with a Superpowers-derived workflow: design records in
`superpowers/specs/`, split plans in `superpowers/plans/`, `plan-conventions.md`,
brainstorm → spec → plan → parallel-agent execution. It works, it is why the curriculum got
built, and dog-fooding it would be a decent argument.

It is still the wrong thing to hand students, for three reasons:

- **It is not a spec format.** It is "a skills library and software development
  methodology — not a spec format." There is no portable artifact to put in a PR and walk
  a mentor through. The defense review needs a document, and Superpowers' output is
  behaviour.
- **It is harness-coupled.** Fourteen harnesses, fourteen install paths. `ai-use-standard.md`
  makes a point of not requiring a specific vendor.
- **It is much bigger than M7.** Seven mandatory workflow skills including git worktrees
  and subagent-driven development. A student shipping one small feature does not need
  worktree discipline, and teaching it here would blow the session budget on process.

**The honest split: Superpowers stays the mentors' authoring methodology for this
repository. OpenSpec becomes the students' shipping methodology in `Robot-2026`.** Those
are different jobs with different users. Nothing about picking OpenSpec for M7 argues
against the way this repo is built.

---

## 6. The one rule that makes it work

Everything above fails if the student asks the agent to write the spec. Then the spec is
not evidence of authorship, it is a second artifact to defend and a longer session for
nothing. Böckeler's warning about agents that "over-interpret" applies doubly when the
agent authored the instructions it is over-interpreting.

The rule, and it should be stated in `ai-use-standard.md`, not just in M7:

> **The spec is Yellow-tier work, even at Green.** You may use AI to explain API surface,
> to critique a spec you wrote, and to point at cases you missed. You may not ask it to
> write the spec, or to decide what the feature should do.

This is not a new idea — it is the *existing* Green-tier construction applied one step
earlier. §4 already says:

> Yellow still applies *during* M6. The tests that unlock Green are written by you, not
> generated. You do not get to use the agent to build the safety net that makes the agent
> safe.

Same sentence shape, one milestone later: **you do not get to use the agent to write the
constraints that constrain the agent.** The policy architecture already supports this;
it just has not been extended forward. I think this is the strongest single idea in this
report and it is worth adopting even if the framework recommendation is rejected.

Note what it does *not* forbid: "here is my spec, what did I miss?" is allowed and should
be actively encouraged, since it is the same shape as M7's already-blessed highest-value
prompt.

---

## 7. What would change, file by file

Scoped deliberately small. Nothing here has been done.

### `docs/programming/milestones/m7-ai-assisted-development.md`

- **New Concepts subsection**, after "Prompting, as an actual skill" and before "AI as
  reviewer, not author" — call it *"Writing it down first"*. Contents: the spec-first /
  spec-anchored / spec-as-source ladder, why Ignite stops at spec-first, the Yellow-tier
  rule from §6, and the honest overhead argument (a two-line fix does not get a spec).
  Roughly one page. This is where the critical case lives — M7's voice already argues with
  itself and should here too.
- **Project step 2 splits.** New step: *write the spec, commit it, then start.* The
  existing "use AI assistance genuinely and substantially" becomes step 3 and gains "…
  against the spec you wrote."
- **Defense review step gains its best question:** "where did the build diverge from your
  spec, and why?"
- **New done items:** spec committed before the first implementation commit; can name one
  place the implementation diverged from the spec and say why.
- **New failure mode: spec theatre.** Writing the spec after the code to satisfy the
  checklist. Catch: the commit timestamps. Also worth naming the inverse — over-specifying
  a small feature until the spec costs more than the code.
- **Resources:** Böckeler's *Understanding Spec-Driven Development* (the critical case),
  the OpenSpec repository.

### `docs/programming/ai-use-standard.md`

- **§4 Green tier: the spec becomes a fourth condition** (decision 1). Green now reads
  *spec → tests → disclosure → defense*, and the spec condition comes first because it is
  the only one that happens before the code exists. The Yellow-tier rule from §6 of this
  report attaches to it rather than to the M6 note.
- **§6 disclosure block:** add a `Spec:` line pointing at the committed spec path. One
  line. It makes the tamper-evidence checkable at review time without a new process.
- **§7 defense review:** add the divergence question and note that the spec is the
  review's spine.
- **§9 worked cases:** two rows. *"M7 student asks an agent to draft the spec for their
  feature"* → **Not allowed.** *"M7 student writes the spec, then asks an agent what cases
  it missed"* → **Allowed, disclose as assisted.** The table is how this document actually
  gets used; a rule not in it will not be applied consistently.

### `projects/m7-ai-assisted/`

- **New `SPEC-TEMPLATE.md`** — the L0 worked example. One real backlog feature (the
  `grabVisionEstimateList()` exception-signal entry is the obvious candidate, being the
  only Open item) specified end to end, in OpenSpec's WHEN/THEN scenario style, with the
  section headings a student copies. Per `CLAUDE.md`'s authoring rules this is a worked
  example of a *different* feature than the one the student ships — it must not contain the
  answer to their own.
- **`DEFENSE-REVIEW.md`** — new question group, *"Questions about the spec"*, sitting
  before "Questions about the code." Divergence, what you left out, what you would specify
  differently now.
- **`pull-request-template.md`** — `Spec:` line in the AI-use block, matching §6.
- **`README.md`** — the order list grows a step; the file table grows a row. **Both setup
  paths documented side by side** (decision 3): `npm install -g @fission-ai/openspec` +
  `openspec init` for students who want the CLI's `validate` / `list` / `view`, and the
  four-line "make the folder yourself" path for students on a machine with no Node. The
  manual path is written first, because it is the one that always works and the one the
  worked example assumes.

### `Robot-2026` (separate repository — needs its own change)

**Specs ship** (decision 2). Every M7 spec commits into the season repo as
`openspec/changes/<feature>/proposal.md`, archived to `openspec/changes/archive/` on merge.
The archive becomes a growing record of how Ignite students reason about features, which is
a mentoring and recruiting asset the team currently has no version of.

**But the scaffolding must not arrive with the first student's pull request**, and this
corrects the first draft of this report, which assumed it would. `openspec init` does not
only create `openspec/`. It also writes **outside** it, into tool-specific hidden
directories chosen by whichever agent the student selected:

```
openspec/{specs,changes}/, openspec/config.yaml
.claude/skills/        if claude selected
.cursor/skills/        if cursor selected
.cursor/commands/      if delivery includes commands
.agents/skills/        shared, for AGENTS.md-compatible tools
```

Three problems follow if the first PR carries that:

1. **The first student's tool choice gets baked into the season repo**, and the second
   student on a different tool adds theirs on top. Over a season `Robot-2026` accumulates
   skills and command shims for tools most of the team does not use. That is the opposite
   of the vendor-neutrality that made OpenSpec the pick in §5.
2. **The first student does setup work nobody else does.** On a milestone where session
   budget is the scarce resource, that is an uneven milestone — and their pull request
   mixes scaffolding with feature work, which makes the one assessment that matters
   noisier for exactly the student who had the least to go on.
3. **It is a layout decision about a shared repository made by whoever happened to go
   first.** That is how repositories end up with three conventions and no rule, which is
   the complaint M1 already makes about `Constants.java`.

**Fix: a mentor seeds it once, before any student starts M7.** One commit on
`Robot-2026`: `openspec init` against the generic **`agents`** target, which writes
`.agents/skills/` and an `AGENTS.md` rather than anything vendor-specific, plus the empty
`openspec/specs/` and `openspec/changes/` folders. After that, decision 2 behaves exactly
as assumed — **every student pull request contains only `openspec/changes/<feature>/` and
their Java.** Students who want their own tool's shims run `openspec init --tools <tool>`
locally and leave those files uncommitted.

That seeding commit is a prerequisite task in the implementation plan, not a step in the
milestone.

### The constitution (decision 4 — new scope, own deliverable)

Promote `Robot-2026/.github/prompts/code-review-prompt.md` out of a bot prompt into a real
standards document, **make it the source of truth, and have code review enforce it.**

**The gap is named in three separate milestones and closed in none of them.** M1, teaching
naming conventions, points at `Robot-2026/.../Constants.java` and says it uses three
conventions at once — "Nobody wrote the rule down. This is what inconsistency costs, and
you will meet more of it." M7 says the standards file "is the closest thing the team has to
a style guide" and that only a bot reads it. M8 says the same thing again, verbatim: "the
team's actual coding standards live in exactly one place: `.github/prompts/code-review-prompt.md`,
a file whose only reader is a bot." Three milestones complain; nothing fixes it. The
constitution fixes it, and it turns M1's line from an unanswered grievance into a setup
that pays off six milestones later.

- **New `docs/programming/coding-standards.md`**, drafted *from* the review prompt:
  Superstructure pattern, type-safe units, command factory methods, homing state machines,
  thread priority, current limiting, fault detection. Marked **draft** until a season lead
  ratifies it.
- **Direction of truth: resolved.** "Code review should enforce it" settles the question
  the first draft of this report left open. **The document is the source; the review prompt
  is downstream of it** — either generated from it or reduced to a thin wrapper that points
  at it. Anything else recreates the two-copies-no-owner failure that
  `teaching-repo-spec.md` §5.7 already had to fix once for the four git documents.
  Mechanically the cheapest honest version is a prompt that names the constitution as the
  standard it reviews against, so there is exactly one place a standard is written.
- **Adopting the constitution concept does not mean adopting Spec Kit.** Spec Kit
  popularised the name — "high-level immutable principles that apply to every change across
  every session" — not the idea. Nothing here changes the §5 recommendation.
- **This does not overlap M8.** M8's documentation deliverable is per-subsystem READMEs and
  tuning logs — it maps the codebase. The constitution states the rules. They are adjacent
  and M8 should reference it, but neither replaces the other.
- **Still needs an owner and a ratifier.** Same shape as the `FEATURE-BACKLOG.md` problem,
  and it must not get the same answer of "unassigned."

### The constitution in the curriculum (decision 4, second half)

**Not covered anywhere today** — the word appears zero times in `docs/`. It should, and it
belongs in the same new M7 Concepts section as the spec, because the two are the same idea
at two scales:

> **A spec is the decisions for this change. A constitution is the decisions that hold
> across every change.** Both are things you write down *so the agent cannot make them for
> you.*

That framing costs one paragraph, makes the spec easier to explain rather than harder, and
generalises past whatever tooling exists in 2028. The Yellow-tier rule from §6 extends to
it cleanly and should be stated: a student does not get to have an agent write the team's
standards either.

Concrete milestone effects:

- **M7**'s done item "read `reference/code-review-prompt.md` and can name two standards in
  it" retargets at the constitution.
- **M7** gains the standing-context practice: hand the agent the constitution alongside
  your spec. This is the actual payoff of decision 4 and the reason it belongs in the same
  change as the spec work rather than after it.
- **M1**'s `Constants.java` note gains a forward reference — the rule is written down now,
  and here is where.
- **M8** references it rather than restating the gap as open.

### Repo-level (contended files — integration task only, per `CLAUDE.md`)

- `docs/programming/teaching-repo-spec.md` §9.8 given-inventory gains `SPEC-TEMPLATE.md`;
  the change-log block at the top gains an entry; §13's status column may need a revisit
  row for step 11. If the constitution lands in `docs/`, `mkdocs.yml`'s `nav:` and
  `tools/site_guard.py`'s required-page list both need it — a page not in `nav:` renders
  but stays unreachable.
- A design record under `superpowers/specs/2026-08-XX-m7-spec-driven-development.md`,
  carrying the four decisions below, per the pattern every other decision here follows.
- **No `tests.lock` change** — M7 ships no tests. **No version pins change** — the CLI is
  optional by design, so nothing enters the pinned-versions table.

---

## 8. Costs, risks, and what this does not fix

**Session cost: +0.5 to +1 session on a 2–3 session milestone.** This is the number I am
least confident in and it should be measured on the first student through, not assumed.
The constitution (decision 4) is *authoring* cost, not student session cost — it is a
mentor writing a document once, and it should not be charged against M7's budget. It does
add reading to the milestone, but it replaces reading a bot prompt rather than adding to
it.
If it lands at +1 on a 2-session milestone that is a 50% increase and the scope should be
cut to the template plus the done items, dropping the Concepts page.

**Risk: spec theatre.** Mitigated by commit ordering, which is checkable, and by the
divergence question, which is not fakeable by someone who did not write the spec.

**Risk: over-specification.** A student who writes a sixteen-acceptance-criterion spec for
a two-file change has learned the wrong lesson. The template must be short, and the
milestone text must say out loud that a small change gets a small spec — Böckeler's
sledgehammer line is worth quoting directly at the student.

**Risk: the framework is fashion.** SDD is roughly one year old as a named practice and at
least one prominent 2026 piece argues it is already dying. The mitigation is structural
rather than editorial: **teach the practice, cite the tool.** If OpenSpec is abandoned in
2027, a markdown template and a habit survive it. This is the same reasoning §11 applies
to the standard as a whole.

**What this does not fix.** `FEATURE-BACKLOG.md` still has one open entry and no owner —
§15 calls that the largest unmitigated risk in the spec and it is untouched by anything
here. **If anything, specs make it slightly worse**, since a spec-first M7 has a longer
runway per feature and therefore benefits more from a stocked backlog. Two students still
cannot ship the same feature. That problem needs an owner, not a framework.

---

## 9. Decisions — 2026-08-21, Joshua Reddick

All four open questions answered. §7 above is written to match; a design record under
`superpowers/specs/` should carry these forward verbatim.

| # | Question | Decision | Effect |
|---|---|---|---|
| 1 | Green's conditions: three or four? | **Four.** The spec is its own condition, not folded into the M6 note | `ai-use-standard.md` §4 gains a condition; it sorts first, being the only one that precedes the code |
| 2 | Spec in the PR description, or committed? | **Ship all specs.** Committed to `Robot-2026`, archived on merge | A mentor seeds the scaffolding once; student PRs then carry only `openspec/changes/<feature>/` |
| 3 | CLI optional, required, or forbidden? | **Optional, and document both paths** | Manual folder path written first; CLI path second. No new pinned version |
| 4 | Pick up the constitution project? | **Yes**, and code review enforces it | The constitution is the source, the review prompt is downstream; the concept enters M7 alongside the spec |

**Decisions 1 and 2 reinforce each other**, which is worth noticing: a condition that says
*ship a spec* is only checkable because decision 2 puts the spec in the repository with a
timestamp. Either alone is weaker than the pair. Decision 1 also overrides the lean this report
argued for — folding kept the tier structure stable, but a folded rule is a rule people
skim past, and this one is load-bearing.

**Decision 4 is the largest scope change here**, and the one with the most value outside
M7. "Code review should enforce it" also answers the direction-of-truth question this
report left open: the constitution is the source and the review prompt derives from it.
What decision 4 still lacks is an **owner and a ratifier**, which belongs in the design
record.

**Decision 2 needed a correction rather than an answer.** The assumption that `openspec/`
arrives with the first student pull request is right about *specs* and wrong about
*scaffolding* — `openspec init` writes vendor-specific skill and command directories
outside `openspec/`, so "whoever goes first picks the team's tooling." A one-commit mentor
seeding task against the generic `agents` target removes the problem and makes the
assumption true. Details in §7.

### Still open, and not created by these decisions

- **`FEATURE-BACKLOG.md` has one entry and no owner** (`teaching-repo-spec.md` §15).
  Untouched by everything above, and slightly worsened by a longer per-feature runway.
- **Who ratifies the constitution, and who owns it in-season.** Same shape as the backlog
  problem, and it should not get the same answer of "unassigned."
- **Whether the review prompt is generated from the constitution or merely points at it.**
  Both satisfy "code review enforces it"; the second is cheaper and harder to desynchronise.
  An implementation detail, but one the design record should state rather than discover.

---

## Sources

**Primary — repository and API, verified 2026-08-21**

- GitHub API `/repos` for `Fission-AI/OpenSpec` (65,784★, MIT, TypeScript, pushed
  2026-08-21), `github/spec-kit` (130,638★, MIT, Python), `obra/superpowers` (275,424★,
  MIT), `bmad-code-org/BMAD-METHOD` (52,134★) — primary
- [Fission-AI/OpenSpec](https://github.com/Fission-AI/OpenSpec) — README, workflow,
  directory layout, prerequisites — primary
- [github/spec-kit](https://github.com/github/spec-kit) — CLI, command sequence,
  constitution, prerequisites — primary
- [obra/superpowers](https://github.com/obra/superpowers) — skills list, workflow,
  harness support — primary
- [OpenSpec CLI reference](https://github.com/Fission-AI/OpenSpec/blob/main/docs/cli.md) —
  `init` / `list` / `validate` / `view` — primary
- Ignite: `docs/programming/milestones/m7-ai-assisted-development.md`,
  `docs/programming/ai-use-standard.md`, `docs/programming/teaching-repo-spec.md` §9.8 and
  §15, `projects/m7-ai-assisted/*`, `superpowers/plan-conventions.md` — primary
- Ignite: `docs/programming/milestones/m1-java-fundamentals.md` (the `Constants.java`
  three-conventions note) and `m8-in-season-deliverables.md` ("a file whose only reader is
  a bot") — the two other places the curriculum names the standards gap — primary

**Secondary — analysis and evidence**

- [Böckeler, *Understanding Spec-Driven Development: Kiro, spec-kit, and Tessl*, martinfowler.com](https://martinfowler.com/articles/exploring-gen-ai/sdd-3-tools.html)
  — the definition, the three-level ladder, and the critical case — secondary, high
  reliability
- [*Exploring Student-AI Interactions in Vibe Coding*, arXiv:2507.22614](https://arxiv.org/html/2507.22614v2)
  — 19 participants; the low-context-prompt, edge-case, and restart numbers — secondary,
  peer-review status unconfirmed
- [*Planning on Paper: Problem Decomposition with Diagrams in Introductory Computing*, arXiv:2606.12427](https://arxiv.org/abs/2606.12427)
  — 55 diagrams; motivates decomposition-first CS1 under GenAI, but reports no AI-use
  findings — secondary, weak support, cited only for the framing
- [*2026 AI Specification Frameworks Compared*, bswen](https://docs.bswen.com/blog/2026-08-07-ai-spec-frameworks-compared/)
  — the artifact/weight/agent table and the three-category positioning — tertiary
- [*OpenSpec vs Spec Kit: Brownfield vs Greenfield*](https://codemyspec.com/blog/openspec-vs-spec-kit)
  and [*BMAD vs Spec Kit vs OpenSpec*, DEV](https://dev.to/willtorber/spec-kit-vs-bmad-vs-openspec-choosing-an-sdd-framework-in-2026-d3j)
  — brownfield fit, delta-spec model, BMAD cost figures — tertiary, the $800–2,000 figure
  is uncorroborated
- [*The Limits of Spec-Driven Development*, Isoform](https://isoform.ai/blog/the-limits-of-spec-driven-development)
  and [*Why Spec-Driven Development Tools Fail in the Enterprise*, Martinelli](https://martinelli.ch/why-spec-driven-development-tools-fail-in-the-enterprise/)
  — overhead-scaling and maintenance-tax arguments — tertiary
- [*Computing Education When Writing Code Is No Longer the Challenge*, CACM](https://cacm.acm.org/opinion/computing-education-when-writing-code-is-no-longer-the-challenge/)
  — the competency-shift framing — secondary, opinion column

**Audit.** Queries sent: 6. Sources received: ~45. Sources fetched and read: 7. Sources
cited: 19. Tool failures: 0. Routing: general-research fallback — no specialist matched.
Not verified independently: the BMAD monthly-cost figure, and Superpowers' harness count
(taken from its own README).
