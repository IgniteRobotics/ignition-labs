# M7 spec-driven development and the coding-standards constitution — design record

**Date:** 2026-08-21
**Build-order step:** none — content addition to a milestone already shipped (spec §13 step 11), plus one prerequisite commit in `Robot-2026`
**Governing sections:** §9.8 (M7 inventory), §5.7 (documentation ownership), §7 (Pages), §13 (build order), §15 (unmitigated risks)
**Source material:** [`../notes/2026-08-21-spec-driven-development-for-m7.md`](../notes/2026-08-21-spec-driven-development-for-m7.md) — the research report, framework survey, and the four decisions this record implements

M7 teaches everything downstream of code existing. Read its project steps: *pick a real,
useful feature*, then *use AI assistance genuinely and substantially.* There is nothing
between them. Every quality control the milestone teaches — the seven-point reading
checklist, the named failure modes, the defense review — fires after an agent has already
made the engineering decisions.

That is also why the assessment is softer than it looks. The defense review is
retrospective, and a student can reconstruct a defense for a diff they did not author.
`ai-use-standard.md` §7 is honest that the whole standard runs on this one mechanism. A
mechanism that load-bearing should have a second, tamper-evident leg.

This adds the front half of the loop — **a spec the student writes before the agent
starts** — and the standing half — **a constitution the team writes once.** It does not
disturb what M7 already gets right, and it removes nothing.

---

## 1. What ships

| Deliverable | Location | New? |
|---|---|---|
| Coding-standards constitution | `docs/programming/coding-standards.md` | new file |
| M7 Concepts section — *Writing it down first* | `docs/programming/milestones/m7-ai-assisted-development.md` | edit |
| M7 project step, DoD items, failure modes, resources | same file | edit |
| Spec as a fourth Green condition | `docs/programming/ai-use-standard.md` §4 | edit |
| `Spec:` disclosure line | same file §6 | edit |
| Divergence question | same file §7 | edit |
| Two worked-case rows | same file §9 | edit |
| Forward reference from the `Constants.java` note | `docs/programming/milestones/m1-java-fundamentals.md` | edit |
| Constitution reference in place of the restated gap | `docs/programming/milestones/m8-in-season-deliverables.md` | edit |
| Worked spec example | `projects/m7-ai-assisted/SPEC-TEMPLATE.md` | new file |
| *Questions about the spec* question group | `projects/m7-ai-assisted/DEFENSE-REVIEW.md` | edit |
| `Spec:` line in the disclosure block | `projects/m7-ai-assisted/pull-request-template.md` | edit |
| Both setup paths; file table, order list, DoD | `projects/m7-ai-assisted/README.md` | edit |
| Nav entry | `mkdocs.yml` | edit |
| Page registration | `tools/site_guard.py`, `tests/test_site_guard.py` | edit |
| §1.3, §9.8 updates | `docs/programming/teaching-repo-spec.md` | edit |

**Prerequisite, outside this repository:** one commit on `Robot-2026` seeding
`openspec/` — see §2.4. Nothing in this repository depends on it at build time; M7's text
does at teaching time.

**No Gradle project changes.** No `build.gradle`, no source set, no given test. `tests.lock`
is untouched — M7 ships no tests, by design (spec §9.8), and nothing here changes that.

---

## 2. Decisions

### 2.1 M7 adopts spec-first, and says out loud where it stops

Böckeler's three levels, in the *Exploring Gen AI* series: **spec-first** (the spec guides
initial development, then is discarded), **spec-anchored** (it persists through
maintenance), **spec-as-source** (the spec is the artifact; humans never edit generated
code).

M7 takes spec-first. It is the only one that fits a 2–3 session milestone, and the other
two are not neutral about what a student learns.

**Spec-as-source is disqualified on principle, not on weight.** Tessl marks generated files
`DO NOT EDIT`. A student who never edits code is a student who did not learn to write code,
which is the failure `ai-use-standard.md` §1 exists to prevent — *"A season where the robot
works and nobody learned anything is a failed season that happens to have a trophy."* The
milestone should name spec-as-source and say why Ignite stops short of it, because students
will meet that tooling within a season and the line should be theirs before a vendor draws
it for them.

Spec-anchored is not rejected, only deferred. It is a plausible future for `Robot-2026`
once there are enough archived specs to anchor to. It is not a thing to teach first.

### 2.2 OpenSpec, format-first — the CLI is optional and both paths are documented

Six frameworks surveyed in the research report. OpenSpec wins on the four constraints that
actually bind here:

| Constraint | Why OpenSpec |
|---|---|
| Brownfield — one small feature in a 46-file repo the student did not write | Delta model: captures only what is changing. Spec Kit's constitution-then-specify flow is greenfield-shaped |
| No required vendor (`ai-use-standard.md` §2, §11) | Plain markdown, 30+ tools, no API key, no MCP. A student on Copilot and a student on Claude Code write the same file |
| 2–3 sessions | No phase gates. One change folder, a proposal, a task list |
| A toolchain that is Java and nothing else | The CLI is convenience, not substrate |

**The CLI is optional, and the README documents both paths — manual first.** `CLAUDE.md`
pins this repository's toolchain deliberately. Making Node ≥20.19 a prerequisite for
*completing a milestone* means a student blocked on an npm install has lost a session to
something that was never their problem, which is the same failure the clean-clone invariant
(§12.3) exists to prevent. The manual path is four folders and a markdown file and always
works; `openspec init` plus `validate` / `list` / `view` is the accelerant for students who
want it.

**Rejected: GitHub Spec Kit**, despite being the largest and GitHub-native — greenfield-shaped,
needs Python 3.11 + `uv` on top of the Java toolchain, and produces enough markdown to draw
*"I'd rather review code than all these markdown files"* from a practitioner far more
patient than a sophomore.

**Rejected: BMAD, GSD, Kiro** — phase-gated multi-agent methodologies aimed at multi-week
enterprise features. Kiro is additionally IDE-locked.

**Rejected: Superpowers**, and this one needs saying because it is the incumbent. This
repository is authored with a Superpowers-derived workflow — `superpowers/specs/`,
`superpowers/plans/`, `plan-conventions.md`, this very document. It is still the wrong thing
to hand students: it is a skills layer rather than a spec format, so there is no portable
artifact to walk a mentor through in a defense review; it installs per harness; and seven
mandatory workflow skills including git worktrees is more process than a one-feature
milestone can carry. **Superpowers stays the mentors' authoring methodology. OpenSpec becomes
the students' shipping methodology.** Different jobs, different users.

### 2.3 The spec is Yellow-tier work, even at Green — and it is Green's fourth condition

Everything above fails if the student has an agent write the spec. Then it is not evidence
of authorship, it is a second artifact to defend.

The rule, stated in `ai-use-standard.md` rather than only in M7:

> **The spec is Yellow-tier work, even at Green.** You may use AI to explain API surface, to
> critique a spec you wrote, and to point at cases you missed. You may not ask it to write
> the spec, or to decide what the feature should do.

This is not new policy. It is the *existing* Green construction moved one step earlier. §4
already says: *"Yellow still applies during M6. The tests that unlock Green are written by
you, not generated. You do not get to use the agent to build the safety net that makes the
agent safe."* Same sentence, one milestone later: **you do not get to use the agent to write
the constraints that constrain the agent.**

**Green gains a fourth condition rather than folding this into the M6 note.** Green now
reads *spec → tests → disclosure → defense*, and the spec sorts first because it is the only
one that happens before the code exists.

**Rejected: folding it into the existing M6 note.** That keeps the tier structure stable and
was this record's original lean. It loses because a folded rule is a rule people skim past,
and §9's worked-case table — which is how the document actually gets used — needs a
condition to point at.

Note what the rule does not forbid. *"Here is my spec, what did I miss?"* is allowed and
should be encouraged: it is the same shape as the highest-value prompt M7 already blesses.

### 2.4 Specs ship into `Robot-2026` — and a mentor seeds the scaffolding, not the first student

Every M7 spec commits as `openspec/changes/<feature>/proposal.md` and archives on merge.
The archive becomes a standing record of how Ignite students reason about features, which
the team has no version of today.

**The scaffolding must not arrive with the first student's pull request.** `openspec init`
does not only create `openspec/`. It writes outside it, into vendor-specific hidden
directories chosen by whichever agent the student selected: `.claude/skills/`,
`.cursor/skills/`, `.cursor/commands/`, `.agents/skills/`.

Three consequences if the first PR carries that:

1. **The first student's tool choice gets baked into the season repo**, and the next student
   on a different tool layers theirs on top. Over a season `Robot-2026` accumulates shims for
   tools most of the team does not use — the opposite of the vendor-neutrality that decided
   §2.2.
2. **The first student does setup work nobody else does**, on a milestone where session
   budget is scarce, and their pull request mixes scaffolding with feature work — making the
   one assessment that matters noisier for the student with the least to go on.
3. **It is a layout decision about a shared repository made by whoever went first.** That is
   how a repository ends up with three conventions and no rule, which is exactly the
   complaint M1 already makes about `Constants.java`.

**One mentor commit on `Robot-2026` before any student starts M7:** `openspec init` against
the generic **`agents`** target — which writes `.agents/skills/` and an `AGENTS.md` rather
than anything vendor-specific — plus empty `openspec/specs/` and `openspec/changes/`. After
that, a student pull request contains only `openspec/changes/<feature>/` and their Java.
Students wanting their own tool's shims run `openspec init --tools <tool>` locally and leave
those files uncommitted.

This is a prerequisite task in the plan, not a step in the milestone, and it needs the
season lead's agreement because it is someone else's repository.

### 2.5 The constitution is the source; code review is downstream of it

Ignite's coding standards live in exactly one place — `Robot-2026/.github/prompts/code-review-prompt.md`
— and its only reader is a bot. **Three milestones name this and none closes it.** M1, on
`Constants.java`: *"Three conventions, one codebase. Nobody wrote the rule down. This is what
inconsistency costs, and you will meet more of it."* M7: *"the closest thing the team has to a
style guide."* M8, again: *"the team's actual coding standards live in exactly one place …
a file whose only reader is a bot."*

`docs/programming/coding-standards.md` closes it, drafted from the review prompt:
Superstructure pattern, type-safe units, command factory methods, homing state machines,
thread priority, current limiting, fault detection. Marked **draft** until a season lead
ratifies it.

**Direction of truth: the document is the source, the review prompt is downstream.** The
cheapest honest implementation is a prompt that names the constitution as the standard it
reviews against, so a standard is written in exactly one place. Generating the prompt from
the document also satisfies this and costs more to build.

**Rejected: two independent copies.** That is the failure §5.7 already had to fix once for
the four git documents, and there is no reason to believe it goes differently the second
time.

**Adopting the constitution concept is not adopting Spec Kit.** Spec Kit popularised the
name — *"high-level immutable principles that apply to every change across every session"* —
not the idea. §2.2 stands unchanged.

### 2.6 The constitution concept is taught, beside the spec

The word appears zero times in `docs/` today. It belongs in the same new M7 Concepts section
as the spec, because they are one idea at two scales:

> **A spec is the decisions for this change. A constitution is the decisions that hold across
> every change.** Both are things you write down *so the agent cannot make them for you.*

That framing costs a paragraph, makes the spec easier to explain rather than harder, and
outlives whatever tooling exists in 2028. §2.3's Yellow-tier rule extends to it: a student
does not get to have an agent write the team's standards either.

Downstream: M7's DoD item *"read `reference/code-review-prompt.md` and can name two standards
in it"* retargets at the constitution; M7 gains the standing-context practice of handing the
agent the constitution alongside the spec — which is the actual payoff and the reason this
ships in the same change as the spec work; M1's `Constants.java` note gains a forward
reference; M8 references the constitution instead of restating the gap as open.

### 2.7 `SPEC-TEMPLATE.md` is a worked example of a feature the student will not ship

M7 is fade level **L0 — worked example only** (spec §8). One fully worked spec, in OpenSpec's
WHEN/THEN scenario style, with the headings a student copies.

Per `CLAUDE.md`'s authoring rules it must be a **different** feature from the one the student
ships. `FEATURE-BACKLOG.md` currently has one Open entry — the `grabVisionEstimateList()`
exception-signal feature — which makes it the obvious worked example and *therefore*
disqualifies it as a student's pick unless the backlog is restocked first. That interaction
is a real constraint on ordering, not a detail; see §8.

The template is deliberately short. Böckeler's *"sledgehammer to crack a nut"* — a small bug
inflated into four user stories and sixteen acceptance criteria — is the failure mode a
generous template produces, and it should be quoted at the student in the prose.

### 2.8 Nothing in this repository runs OpenSpec

Same pattern step 11 already set for `ai-code-review.yml`: M7 ships reference material for a
pipeline that runs somewhere else. `projects/m7-ai-assisted/` gains a template and
instructions; the live `openspec/` lives in `Robot-2026`. No `openspec/` directory in
`ignition-labs`, no CI wiring, nothing that executes.

This also keeps §2.2's Superpowers/OpenSpec split clean: adopting OpenSpec for students does
not argue for changing how this repository is authored, and nothing here does.

---

## 3. Documentation ownership

Four documents can now plausibly contain "our conventions." Without a boundary they converge,
which is what §5.7 was written to prevent for the git set.

| Document | Owns | Never contains |
|---|---|---|
| `ai-use-standard.md` | *What may I use a tool for, at my tier?* — policy, tiers, disclosure, defense review | Java conventions. Anything specific to one mechanism |
| `coding-standards.md` | *What does good Ignite code look like?* — patterns, units, naming, safety | AI policy. Per-subsystem detail. Anything true of one season only |
| `Robot-2026/.github/prompts/code-review-prompt.md` | *Enforcement.* Points at the constitution and adds nothing normative | Any standard not in the constitution (§2.5) |
| `templates/subsystem-readme.md` (M8) | *What is this subsystem, and how do I not break it?* | Team-wide rules. Links to the constitution instead |

`coding-standards.md` carries an owner, a ratifier, and a review date in its own header,
mirroring `ai-use-standard.md` §11. **An unowned constitution is `FEATURE-BACKLOG.md` again,**
and §15 already names that as the largest unmitigated risk in the spec. This one does not
ship unassigned.

---

## 4. Spec amendments

`docs/programming/teaching-repo-spec.md`:

- **§1.3** gains a dated revision entry naming: spec-first SDD added to M7 (§9.8); the spec
  made Green's fourth condition; `coding-standards.md` added as a governing document with an
  ownership boundary (§5.7); `SPEC-TEMPLATE.md` added to M7's given inventory; M7's
  mentor-verified count raised from nine to eleven. Originating document preserved at
  `../superpowers/specs/2026-08-21-m7-spec-driven-development-design.md`.
- **§9.8 Given** gains `SPEC-TEMPLATE.md` and a pointer to `coding-standards.md`.
- **§9.8 Mentor verifies** changes `all nine DoD items` → `all eleven DoD items`.
- **§9.8** gains one sentence recording that M7's specs commit to `Robot-2026`, and that the
  `openspec/` scaffolding there is mentor-seeded (§2.4).
- **§5.7** gains the ownership table from §3, or a pointer to it, so the boundary is governing
  rather than advisory — the same promotion the 2026-08-19 record made for the git documents.
- **§13** gains no new numbered step. This is a content addition to shipped step 11, matching
  the precedent set by the 2026-08-19 M0 record.
- **§15** — the `FEATURE-BACKLOG.md` risk is **unchanged and slightly worse**; see §8. This
  record does not claim to mitigate it.

`docs/programming/ai-use-standard.md` is edited rather than amended-by-reference, per §1.1's
scope note that this spec adds process the existing documents already assume. Its §11 expiry
(end of 2027 build season) is unchanged and now covers the spec rule too.

---

## 5. Definition of done additions

M7's DoD goes from nine items to eleven, **in both places it appears** —
`docs/programming/milestones/m7-ai-assisted-development.md` and
`projects/m7-ai-assisted/README.md`, which carry the same list verbatim and drift the
moment one is edited alone. Two added:

- [ ] Your spec was committed before your first implementation commit
- [ ] You can name one place the implementation diverged from your spec, and say why

And one retargeted:

- [ ] ~~You read `reference/code-review-prompt.md`~~ **You read `coding-standards.md`** and
  can name two standards in it

**Two, not five.** The list already gates a milestone whose entire assessment is
mentor-verified, and every item added is a minute of someone's defense review.

The second item is the one that matters. *"Where did the build diverge from your spec, and
why?"* is strictly harder to fake than *"why this control mode?"*, because it requires having
written the spec — and every real project diverges, so a student answering "it didn't" has
told you something too.

---

## 6. Registration — the two hand-maintained lists

`coding-standards.md` is a new page and both lists are deliberately static:

1. **`mkdocs.yml` `nav:`** — under *Programming Curriculum*, adjacent to `ai-use-standard`,
   since the two are the pair a student is handed at M7.
2. **`tools/site_guard.py` `REQUIRED_PAGES`** — add `"programming/coding-standards"`. The
   module comment explains why this list is hardcoded (finding C11b): a nav-derived list
   shrinks in lockstep with a bad nav edit and can never observe the failure it exists to
   catch. A page in nav but not in `REQUIRED_PAGES` renders today and silently stops being
   guarded.

`tests/test_site_guard.py` gains an assertion that `"programming/coding-standards"` is in
`REQUIRED_PAGES`, mirroring the existing `team-gitflow` test.

`REQUIRED_ASSETS` is untouched — this change ships no images.

---

## 7. Verification

Per the repository's verification discipline, each command is run and its **actual output**
reported, not summarized.

| Check | Command | Expected |
|---|---|---|
| Suite | `poetry run pytest` | green, including the new `test_site_guard.py` assertion |
| Build | `poetry run mkdocs build` | clean; `coding-standards.md` renders |
| Guard | `poetry run python tools/site_guard.py _site` | `site guard OK` |
| Lock unchanged | `poetry run python tools/tests_lock.py verify` | rc=0 — no test files touched |
| No stray commits | `git status --short` | only intended files |

**Four things must be verified by hand, because no command can — and this repository has a
documented history of checks that pass while the thing they protect is broken:**

1. **The constitution is checked line by line against `code-review-prompt.md`.** Every
   standard in the prompt appears in the document, or is deliberately dropped with a reason
   recorded. A constitution that silently omits a standard the bot still enforces is worse
   than no constitution: it tells a student the rules while a reviewer applies different ones.
2. **The M7 loop is run end to end, once, by a mentor, before a student meets it.** Write a
   spec from `SPEC-TEMPLATE.md` for a real backlog feature, hand it to an agent, ship it into
   `Robot-2026`, and sit the defense review from the other side. `CLAUDE.md`: *a fixture
   nobody has finished is a fixture nobody knows is completable.*
3. **The divergence question is asked against that real PR.** If the honest answer is "it
   didn't diverge," the feature was too small to need a spec and the template is teaching the
   wrong scale — that is a finding, and it is cheaper to have it now than in front of a
   student.
4. **`SPEC-TEMPLATE.md` is read for leaked answers.** It must not contain the implementation
   of the feature a student will pick, in prose, in a scenario, or in a heading (`CLAUDE.md`
   authoring rules).

**Also verified by hand, in `Robot-2026`:** that a fresh `openspec init --tools agents` writes
nothing vendor-specific, and that a second contributor's local `openspec init --tools claude`
produces only untracked files. §2.4 is an assertion about a tool's behaviour, and an assertion
nobody has run is a guess.

---

## 8. Risks

| Risk | Mitigation |
|---|---|
| **Spec theatre** — the spec written after the code to satisfy the checklist | Commit ordering is checkable and is a DoD item; the divergence question is not answerable by someone who did not write it |
| **Over-specification** — sixteen acceptance criteria for a two-file change | Template kept short (§2.7); the sledgehammer failure named in the prose, not just avoided |
| **Session cost** — estimated +0.5 to +1 on a 2–3 session milestone | Measured on the first student through, not assumed. If it lands at +1, cut to the template and DoD items and drop the Concepts section |
| **The constitution desynchronises from enforcement** | §2.5 makes the document the source and the prompt a pointer; §3 makes the boundary governing |
| **The constitution ships unowned** | §3 requires an owner and ratifier in its header. This is the `FEATURE-BACKLOG.md` failure and it does not get the same answer |
| **OpenSpec is abandoned** — SDD is roughly one year old and already has obituaries | Format-first, CLI optional (§2.2). A markdown template and a habit survive the tool. Same reasoning `ai-use-standard.md` §11 applies to itself |
| **`Robot-2026` layout change lands without the season lead** | §2.4 is a prerequisite with a named approver, sequenced before any M7 text depends on it |
| **`FEATURE-BACKLOG.md` scarcity** — one Open entry, no owner (§15) | **Unmitigated, and made slightly worse.** A longer per-feature runway raises the cost of an empty backlog, and §2.7 consumes the one remaining entry as the worked example. The backlog must be restocked before this ships, which is a scheduling dependency this record cannot satisfy on its own |

---

## 9. Out of scope

- **Spec-anchored and spec-as-source** (§2.1). Deferred and rejected respectively.
- **`ignition-labs` adopting OpenSpec for its own authoring.** Superpowers stays (§2.2, §2.8).
- **Wiring OpenSpec into any CI**, here or in `Robot-2026`. Nothing in this change executes.
- **Rewriting the Gemini review pipeline.** Only the prompt's *content* moves; the workflow,
  the script, and `requirements.txt` are untouched.
- **Assigning `FEATURE-BACKLOG.md` an owner.** Named in §8 as a blocking dependency, not
  solved here.
- **M8 template changes** beyond replacing the restated standards gap with a reference (§2.6).
- **Retrofitting specs into M3–M6.** Those milestones are Yellow; a spec-first loop presumes
  an agent the student is not permitted to use that way yet.

---

## 10. Next

An implementation plan under `superpowers/plans/2026-08-XX-m7-spec-driven/`, per
`plan-conventions.md`. The real lane width is narrow — the constitution and the M7 prose are
two genuinely independent files, the `projects/` edits are one sequential lane, and
`teaching-repo-spec.md`, `mkdocs.yml`, and `site_guard.py` are contended and belong to the
integration task. Two lanes plus a serialized tail, with the `Robot-2026` seeding commit and
the backlog restock as prerequisites ahead of both.
