# M8 — Spec-anchored development: the second AI milestone — design record

**Date:** 2026-08-22
**Build-order step:** new, **inserted** at position 8. In-Season Deliverables & Ownership moves from M8 to **M9** — see §3, which is the largest mechanical consequence of this record
**Governing sections:** §8 (fade curve), §9 (milestone inventory), §13 (build order), §15 (unmitigated risks)
**Source material:** [`2026-08-21-m7-spec-driven-development-design.md`](2026-08-21-m7-spec-driven-development-design.md) §2.1, which named three levels of SDD and deferred this one, and [`2026-08-21-m7-openspec-required-design.md`](2026-08-21-m7-openspec-required-design.md), which made the tooling required and the format exact

M7 teaches a student to write a spec, build against it, and ship. Then the change is archived and the student never touches it again. **M7's spec is write-once**, and that is the whole of what M7 can teach: there was nothing in `openspec/specs/` to write *against*, so every requirement a student has ever written has been an `ADDED` one against an empty file.

The thing M7 structurally cannot teach is what happens when a spec meets its second change — when behaviour someone already wrote down has to become different behaviour, and there is now a document on disk asserting otherwise. That is the difference between having written a spec and running a codebase that has specs, and it is the only part of spec-driven development that compounds.

This milestone sits **directly after M7 and anchors to M7's own output.** That placement is the central decision and it changes what the milestone is; see §2.3.

---

## 1. What ships

| Deliverable | Location | New? |
|---|---|---|
| Milestone page | `docs/programming/milestones/m8-spec-anchored-development.md` | new file |
| Project directory | `projects/m8-spec-anchored/` | new |
| Worked `MODIFIED` delta | `projects/m8-spec-anchored/MODIFIED-EXAMPLE.md` | new |
| The second-reader protocol | `projects/m8-spec-anchored/SPEC-REVIEW.md` | new |
| Drafted-spec allowance | `docs/programming/ai-use-standard.md` §4 — takes it to v1.3 | edit |
| **In-season milestone renumbered M8 → M9** | `docs/programming/milestones/`, `projects/`, and ~19 files | **rename** |
| Ladder, gate row, nav, registration | `docs/programming/README.md`, `docs/programming/curriculum-spec.md`, `mkdocs.yml`, `tools/site_guard.py`, `tools/changed_projects.py`, `tests/test_site_guard.py`, `tests/test_changed_projects.py` | edit |
| §1.3, §8, **new §9.9**, §9.10, §13, §15 | `docs/programming/teaching-repo-spec.md` | edit |

**No Gradle project, and no capability backlog.** Like M7, M8 ships no skeleton and no tests. §2.3 removes the need for a backlog file entirely — the student's subject is already determined by what they shipped at M7. `tests.lock` is untouched.

---

## 2. Decisions

### 2.1 "Full SDD" means spec-anchored. Spec-as-source stays rejected, and M8 is where the rejection gets argued rather than asserted

The M7 record named three levels: **spec-first** (the spec guides the build, then you are done with it), **spec-anchored** (it survives into maintenance), **spec-as-source** (the spec is the artifact, code is generated, the tooling marks it `DO NOT EDIT`).

M7 takes spec-first. This milestone takes **spec-anchored**, which that record explicitly deferred rather than rejected.

**Spec-as-source remains rejected on principle**, and the principle is unchanged: a student who never edits code did not learn to write code, which is the failure `ai-use-standard.md` §1 exists to prevent. But M8 is the right place to stop *asserting* that and start *arguing* it, because it is the first point at which a student has enough experience to disagree competently. They will have written a spec by hand, watched an agent misread it, and then maintained it through a change. Handing them the spec-as-source position at that point — and asking them to make the strongest case for it before hearing ours — is a better defence of the line than repeating it.

**A student who argues Ignite out of this position has done something valuable.** The line is a judgement about what a training team should optimise for, not a law of nature.

### 2.2 The content is `MODIFIED` and `REMOVED`

Everything a student has written by the end of M7 is an `## ADDED Requirements` block. OpenSpec has two others, and they are where the difficulty lives:

```markdown
## MODIFIED Requirements

### Requirement: Vision exception occurrence count
The system SHALL <the full new behaviour, restated>.

## REMOVED Requirements

### Requirement: <name>
<why it is going>
```

Two things make this harder than M7:

1. **`MODIFIED` restates the requirement in full, not as a diff** — and the tool enforces it. Verified on 2026-08-22 against OpenSpec 1.10.0: a `MODIFIED` block that omits a scenario the current spec still has is rejected, by name:

   > `✗ [ERROR] MODIFIED "Vision exception occurrence count" omits scenario(s) the current spec still has: "Repeated failures accumulate". Copy them into the MODIFIED block (a MODIFIED requirement replaces the whole block, so archive refuses to drop them).`

   **This is better for the milestone than a lint rule would be.** You cannot change one clause of a requirement without accounting for every scenario attached to it, so the tool makes *read the whole thing before you change it* a precondition rather than advice — which is M8's lesson, enforced by something other than a mentor's attention.

   **An earlier draft of this record claimed `openspec validate` would not catch this. That was wrong, and it was wrong because it had never been run.** The correct line is narrower and more useful: **validation enforces completeness, not truth.** It will not let you silently drop a scenario; it will happily accept one you reworded into a falsehood. That gap is exactly the space §2.4's second-reader test occupies.
2. **`REMOVED` requires justifying a deletion.** Removing a requirement claims that behaviour the team once decided on is no longer wanted. That is the hardest thing here to do honestly and the easiest to do by accident. Verified: a `REMOVED` block merges cleanly and the requirement disappears from the capability spec — `Totals: + 0, ~ 1, - 1`.

### 2.3 The student extends their own M7 change — which is what "directly after M7" buys

**This reverses an earlier draft of this record**, which had the student modify a capability specified by somebody else, and which therefore could not run until several M7 changes had been archived across a cohort. Placing the milestone directly after M7 dissolves that dependency: **the spec you anchor to is the one you wrote and archived at M7.**

What that costs and what it buys:

**Lost:** reading a stranger's spec cold, and the argument-for-specs that lands hardest when the author has graduated. That lesson is not gone, only deferred — it arrives for real during the season, when a student modifies a capability the cohort ahead of them specified.

**Gained, and it is worth more than it looks:**

- **The milestone can run in the curriculum's first year.** The prerequisite drops from *a populated cohort archive* to *your own M7 change is archived*, which is one mentor command on one pull request.
- **The loop closes.** M7 ends at "shipped and archived" and M8 begins there. A student sees, in one continuous arc, that a spec is not a deliverable you hand in — it is a thing that stays and that you will meet again.
- **You are the future person your own spec failed.** Six weeks is long enough to have forgotten your own reasoning. Discovering that a requirement *you* wrote is ambiguous, or that a scenario you were proud of does not say what you meant, is a sharper lesson than finding the same fault in a stranger's work — because there is nobody else to blame and no way to read it as the other person being careless.

**The extension must be a real second change, not a touch-up.** A student who adds a scenario they forgot has not modified a requirement. `CAPABILITY-BACKLOG.md` is therefore not needed as a file, but the *mentor* must have a second change in mind for each capability before assigning M8 — which belongs in the milestone's mentor notes, not in a depleting list.

### 2.4 The assessment is the second-reader test, not another defense review

M7 already assesses by defense review, and repeating it would produce a milestone that feels like M7 with extra folders. The thing M8 must actually check is different: **is the updated spec true, and can somebody else act on it?**

> **The second-reader test.** A second student — who has not seen the diff — reads only the updated capability spec and states what the system now does, including at least one edge case. Then they read the diff.
>
> If their description does not match the code, **the spec is wrong**, regardless of whether the code is right.

Why this and not a defense review:

- It tests the property spec-anchored exists to provide. A spec's value is that a future person can trust it without reading the code; the test is simply that person, early.
- **The author cannot pass it by being articulate.** M7's defense review is a conversation the student drives. Here the student is silent while somebody else tries to use their work.
- It costs one peer and fifteen minutes, and needs no mentor with deep context. Any M7-completed student can be the reader, which the new placement makes plentiful.
- It teaches both students. The reader practises consuming a spec, which is the more common activity in a real team.

Failure is normal and is not a retry: the student fixes the spec and the reader re-reads. **The number of round trips is the interesting signal** and should be reported rather than hidden.

### 2.5 Spec rot is the named enemy

Spec-anchored development has one catastrophic failure mode: **a spec that describes what the code used to do.** A stale spec is worse than no spec, for the same reason `coding-standards.md` argues a constitution that silently omits a standard is worse than no constitution — it tells a reader the rules while the system runs on different ones, and it is trusted *because* it exists.

M8 teaches the detection and the discipline:

- `openspec validate --specs` and `--strict` — and what they do **not** catch. They enforce **completeness, not truth**: they will refuse a `MODIFIED` block that drops a scenario (§2.2), and they will cheerfully accept one whose GIVEN/WHEN/THEN describes behaviour the code has never had. That boundary is the lesson, and it is why §2.4's assessment is a human reading rather than a command.

**Archive leaves a `## Purpose` stub, and somebody has to write it.** Verified: `openspec archive` creates the capability spec with `## Purpose` reading `TBD - created by archiving change <name>. Update Purpose after archive.` Left alone, every capability in `Robot-2026` ends up with a TBD purpose and the settled specs are a list of requirements with no statement of what the capability is *for*.

**M8 is where that gets fixed, and it is a good first task.** The student is already reading the whole capability spec in order to modify it; writing its Purpose costs a sentence and forces them to say what the capability is, not just what it does. Add it to M7's archive step as the mentor's responsibility only if M8 is not being run.
- The rule: **if you change behaviour a spec describes, the spec change ships in the same pull request.** Not a follow-up. A spec updated later was wrong in the interim, and the interim is where somebody trusted it.

Under the new placement, the student's own M7 spec is the most likely thing to have drifted — their M7 implementation diverged from it, and M7's done list only required them to *name* the divergence, not to fix the document. **M8 opens by making them repair the drift they created**, which is both a real task and the cheapest possible demonstration that drift happens to careful people.

### 2.6 M8 permits the agent to draft a spec, under red-line review — the next tier boundary

This is the decision most worth arguing with, and it is the natural continuation of how Ignite tiers everything else.

The pattern is established twice. **Yellow → Green at M6:** you may lean on the agent once you have written the tests that catch it. **The spec rule at M7:** you write the spec yourself, because you have never written one and cannot yet review one. In both cases the tool is *earned by demonstrating you do not need it.*

By M8 a student has written a conforming spec by hand, watched an agent misread it, and defended the divergence. They can now review a drafted spec competently, which is exactly the qualification M7 said they lacked. So:

> **At M8, `openspec-propose` is permitted as a first draft that you must red-line.** You disclose that you used it, and you show what you changed and why — at minimum one requirement you rewrote and one scenario you added or deleted. A draft accepted unchanged is not a pass; it is evidence you did not review it.

This mirrors M7's strongest prompt shape, inverted: at M7 you write and the agent critiques; at M8 the agent drafts and you critique. The skill assessed is the same one — **holding the decisions** — and the red-line is the artifact that proves it.

**The risk is named in §7:** a student who rubber-stamps has done less work than an M7 student on a harder milestone. The second-reader test is the backstop, because a rubber-stamped spec fails it.

**Rejected: keeping the M7 ban.** It is the safe answer and it teaches that the rule was about the tool rather than about competence. The tiers exist to be climbed. **Back-to-back placement makes this more defensible, not less** — the ban and its lifting are six weeks apart with the same student, so the contrast is something they experience rather than read about.

### 2.7 Fade level L0, and the scaffolding is one worked `MODIFIED` delta

`MODIFIED-EXAMPLE.md`: a capability spec, and the same spec after a `MODIFIED` change, side by side, with the reasoning for each edit. Per the authoring rules it works the same feature `SPEC-TEMPLATE.md` already spends — the vision-exception signal — which is deliberate: it is the one capability guaranteed not to be any student's subject, and continuing it lets the example show a *second* change to a spec the reader already knows.

### 2.8 Placement, gating, and what moves

**M8 is pre-season and sits directly after M7.** In-Season Deliverables & Ownership becomes **M9**, and keeps every property it has today — it runs *during* the season, takes a full season, and is not gated by the Kickoff Readiness Gate. The README line *"M8 is not gated — it runs during the season"* becomes **M9**.

**M8 is added to the Kickoff Readiness Gate.** The AI-tooling row becomes **`+ M7 and M8`, and a signed AI-use standard**, in `README.md` and in `curriculum-spec.md` §3 — both gate tables, because two gate tables disagreeing about whether M8 is required is worse than either answer alone.

**This reverses the position this section originally took**, which was that M7 plus a signed standard remained sufficient. The original argument is preserved in §9 with its reasoning intact; what beat it is short. §2.6 moves the drafted-spec boundary to M8, and that boundary lives inside the Green tier of `ai-use-standard.md` §4. A student cleared for agentic use at M7 is cleared under a version of Green whose current text they have not reached — the gate would be admitting people to a tier under the previous edition of the document that defines it. The gate is not *M7 was enough*; the gate is *the standard you signed is the standard you are working under.*

The cost is real and is stated where students read it: two more sessions before kickoff, and a student who finishes M7 in December is not cleared until M8 is done too. That belongs in the fall announcement, not in January.

---

## 3. The renumber — the largest mechanical consequence

Inserting at position 8 renames a **shipped, published milestone**. Scope, measured:

| What | Detail |
|---|---|
| File renames | `docs/programming/milestones/m8-in-season-deliverables.md` → `m9-in-season-deliverables.md`; `projects/m8-ownership/` → `projects/m9-ownership/` |
| Files carrying `M8` references | ~19, led by `teaching-repo-spec.md` (18 hits), `programming/README.md` (10), `curriculum-spec.md` (8), `resources.md` (5) |
| Tooling | `tools/site_guard.py` `REQUIRED_PAGES`, `tools/templates_sync.py`, `tools/changed_projects.py`, `tests/test_site_guard.py`, `tests/test_changed_projects.py`, `.github/workflows/skeleton-integrity.yml` |
| Nav | `mkdocs.yml` — two entries |

**The published URL `programming/milestones/m8-in-season-deliverables/` breaks, and that is the decision.** `mkdocs.yml` declares no plugins and still declares none: **`mkdocs-redirects` was considered and is not being added.** A plugin is a permanent dependency — one more thing in the §16 seasonal bump, one more thing that can break a `mkdocs build --strict` that currently has nothing to break — bought to preserve exactly one URL, on a site one season old, whose entire audience is a team that can be told in a Slack message. The break is announced, not softened.

The safety net is that nothing *inside* the repository is allowed to break with it: `mkdocs build --strict` fails on a dead internal link, `tools/site_guard.py`'s hand-maintained `REQUIRED_PAGES` fails if either page stops rendering, and `docs-links.yml` catches external rot. An outside bookmark is the only thing that dies, and no student's workflow depends on one.

**The renumber ships atomically with M8's content, in one pull request.** Renumbering first would publish a ladder that runs M0–M7 and then M9, which is a visibly broken curriculum for however long the gap lasts.

---

## 4. Definition of done — draft

Mentor-verified, like M7's. Every item is a minute of someone's review.

- [ ] You repaired the drift between your M7 spec and what you actually shipped
- [ ] Your change folder contains a `MODIFIED` or `REMOVED` requirement, restated in full
- [ ] `openspec validate` passes, and the spec change shipped in the same pull request as the code
- [ ] A second reader described the new behaviour correctly from your spec alone
- [ ] You can say how many round trips that took, and what you fixed each time
- [ ] You can name something `openspec validate` cannot check about a requirement
- [ ] If you used `openspec-propose`, you can show one requirement you rewrote and one scenario you added or deleted
- [ ] You can make the strongest case *for* spec-as-source, and then say why Ignite stops short of it

The last item is §2.1's argument made assessable. A student who can only recite the Ignite position has not met it.

**Item 1 — corrected during implementation, 2026-08-22.** The drafted item covers the drift repair only, and the shipped list extends it to *"…and wrote the capability's `## Purpose` line."* The `## Purpose` write is a task §2.5 calls a good first task, the milestone page calls "the first thing M8 asks of you", and `curriculum-spec.md`'s DoD summary already stated as part of item 1 — while no authoritative checklist contained it, so nothing a mentor marks covered a named failure mode of the milestone. Extending item 1 rather than adding a ninth item keeps the eight-item list the summary and the spec both describe, and keeps the two shipped copies of the list byte-identical.

---

## 5. Spec amendments

`docs/programming/teaching-repo-spec.md`:

- **§1.3** — dated revision entry naming M8's insertion, the M8 → M9 renumber, the accepted URL break, the gate row, the new §13 step, and `ai-use-standard.md` to v1.3.
- **§9 — corrected during implementation, 2026-08-22.** An earlier draft of this bullet said §9.8 held in-season content and had to move. **It does not.** The measured headings were §9.7 = M6, **§9.8 = M7**, §9.9 = in-season. So: **§9.8 does not move**, M8 becomes a **new §9.9**, and the in-season milestone moves **§9.9 → §9.10**. The only in-repo `§9.8` cross-references are `teaching-repo-spec.md:42` and `:44`, two shipped §1.3 revision entries that point at M7 and correctly stay put — renumbering §9.8 would have silently repointed them at the wrong milestone. The `§9.9` references are all under `superpowers/`, which is history and is not renumbered.
- **§13** gains numbered step 13. This is the first genuinely new milestone since the original build order, and unlike the M7 spec work it is not a content addition to a shipped step. Shipped step 11's label changes from *M8 templates* to *M9 templates* — the artifact is unchanged, only its milestone's number moved.
- **§8** — the fade curve's L0 row gains M9; the table read *M6, M7, M8* and becomes *M6, M7, M8, M9*, with a sentence recording that the curve ends at L0 and both new milestones sit there.
- **§15** — no new unowned *list*: §2.3 removed the capability backlog, which is a small win against the risk register rather than an addition to it, and §15 now says so. **It does gain one row.** The *file* count and the *risk* count move in opposite directions and the section separates them — §7 of this record names repository-wide spec health as unsolved and §8 puts assigning it out of scope, so the register of record has to carry it rather than leave it stated only here. Severity Medium, mitigation **none**.
- **§5.4, §6.1, §4.1, D1, §14** — mechanical: the commit-access heading becomes M7–M9, the ladder matrix gains an M8 rung, the layout tree gains `projects/m8-spec-anchored/` and renames `m8-ownership/`, and the §14 acceptance line about `site_guard.py` goes from nine milestones to ten.

`docs/programming/curriculum-spec.md` — **added to this list during implementation.** Beyond the §3 gate row (§2.8), its §4 ran `### 4.9 M7` → `### 4.10 M9` with no subsection for M8, while §4.1 declares every milestone file follows an identical structure. **A `### 4.10` for M8 was authored and in-season renumbered to `### 4.11`.** There are zero cross-references to `§4.9`/`§4.10`/`§4.11` anywhere in the repository, so the renumber dangles no citation. Its DoD summary is explicitly marked as a summary — the milestone file carries the authoritative eight-item list, and a verbatim third copy would only create a drift surface nothing checks.

`docs/programming/ai-use-standard.md` → **v1.3**, for §2.6. Green's four conditions are unchanged; what changes is the spec rule's boundary, which currently forbids agent-drafted specs unconditionally and must say *"before M8."* Anyone who acknowledged v1.2 signs again.

---

## 6. Prerequisites — not tasks

| # | Gate | Owner | Blocks |
|---|---|---|---|
| **Q1** | The student's own M7 change is merged **and archived** (`openspec archive`) | The mentor who sat their M7 defense review | That student starting M8 |
| **Q2** | A mentor runs M8 end to end, including sitting on both sides of the second-reader test | Any M7-completed mentor | Teaching, not merge |

**Q1 is per-student, not per-cohort** — which is the entire practical gain from the new placement. It is one command on one pull request, already assigned in M7's `DEFENSE-REVIEW.md`. It does mean **an M7 archive that never happens silently blocks that student's M8**, so the mentor note in M7 should say the archive is a downstream dependency rather than tidying.

---

## 7. Risks

| Risk | Mitigation |
|---|---|
| **Two spec milestones back to back** — four to six consecutive sessions of specification before a student writes another feature | Real, and new with this placement. M8's change is small by construction (a second change to a capability that already exists), and the §2.5 drift repair is short. Measure on the first student; if it lands past two sessions, cut the `REMOVED` requirement and teach `MODIFIED` only |
| **The extension is a touch-up, not a change** | §2.3. The mentor identifies a genuine second change before assigning; a forgotten scenario is not a `MODIFIED` requirement |
| **The §2.6 relaxation gets rubber-stamped** | The second-reader test fails a spec its author did not really write. The red-line is a DoD item and is shown, not asserted |
| **The renumber breaks links and cross-references** | §3. Atomic with the content; the URL break is accepted deliberately; `--strict` and `site_guard.py` catch internal breakage, and lychee catches external |
| **Spec rot outruns the curriculum** | Unsolved. §2.5 teaches detection; nobody owns repository-wide spec health, and this record does not pretend otherwise |
| **Losing the stranger's-spec lesson** | §2.3 accepts this. It returns during the season, on real capabilities the cohort ahead specified — arguably where it belongs |
| **OpenSpec churn** in `MODIFIED`/`REMOVED` semantics | Same posture as M7: requirement syntax is the stable core; commands are named once per document |

---

## 8. Out of scope

- **Practising spec-as-source** (§2.1). Argued, not adopted.
- **Retrofitting specs onto M3–M6.** Those milestones are Yellow; a spec-anchored loop presumes an agent the student may not use that way yet.
- **Full spec coverage of `Robot-2026`.** The delta model exists so nobody has to, and attempting it produces exactly the stale documentation §2.5 warns about, at scale.
- **Wiring OpenSpec into CI**, here or in `Robot-2026`. Nothing executes.
- **Changing what M9 (in-season) teaches.** It is renumbered and otherwise untouched.
- ~~**Adding M8 to the Kickoff Readiness Gate.**~~ **Reversed.** M8 *is* on the gate — §2.8, with the rejected position at §9.4. What is out of scope is the roster mechanics: the gate is a checklist a mentor maintains, and this record adds no tooling for it.
- **Assigning repository-wide spec-health ownership.** Named in §7 as unmitigated.

---

## 9. Questions answered, and the position that lost

The three questions this section posed were decided by the programming lead on 2026-08-22, before implementation. They are recorded as decisions rather than left posed, because a record that still asks a question somebody answered is a record that will be read as undecided.

### 9.1 Does §2.6 go too far? — **No. It ships as written.**

The question was whether permitting `openspec-propose` as a red-lined first draft relaxes, one day later, the rule M7 had just tightened.

**Decision: it ships.** The rule M7 wrote was never *the tool is dangerous*; it was *you have not written one yourself yet*. That condition expires by construction — an M8 student has written a conforming spec by hand, watched an agent misread it, and defended where the build diverged. A boundary that never moves once its precondition is met was a ban wearing a tier's clothes, and it would teach that the rule was about the tool. **What makes it safe is not the argument, it is §2.4:** a rubber-stamped spec fails the second-reader test, because you cannot answer for edge cases in a document you did not really write. The relaxation is backed by an assessment that detects the abuse, which is the only kind of relaxation worth making.

### 9.2 Two spec milestones back to back? — **Yes, and it stays pre-season.**

The alternative was M8 as offseason work a returning student does after M9, which the old numbering implied.

**Decision: directly after M7, pre-season.** §2.3 is the reason and it is decisive: the spec a student anchors to is the one *they* archived at M7, so the prerequisite is one mentor command on one pull request rather than a populated cohort archive. Deferring M8 past a season breaks that — the student is now modifying a stranger's capability after all, which is the dependency the placement exists to dissolve, and the arc from *shipped and archived* to *and now it changes* stops being one continuous thing. The four-to-six-session concern is real and stays in §7 as a risk with a measurement and a cut: if the first student lands past two sessions, drop `REMOVED` and teach `MODIFIED` only.

### 9.3 Accept the broken URL, or add `mkdocs-redirects`? — **Accept the break.**

**Decision: accept it, announce it, add no plugin.** Reasoning in §3, which is now written as the decision rather than a recommendation.

### 9.4 Rejected: leaving M8 out of the Kickoff Readiness Gate

This was §2.8's original position and it is preserved here because the argument for it is good and someone will make it again. As written:

> **M8 itself is not added to the Kickoff Readiness Gate.** M7 plus a signed standard remains the requirement for using AI tooling on competition code. Adding M8 would raise that bar substantially in the same season it is introduced, and M8's value is depth rather than safety — M7 is what makes agentic use safe. Flagged as reviewable after one cohort.

**Why it lost.** The *depth rather than safety* split does not survive §2.6. M8 is where the drafted-spec allowance opens, and that allowance is a clause of Green in `ai-use-standard.md` §4 — so M8 is not decoration on top of a settled tier, it is part of the tier's definition. Gating at M7 would admit students to Green under v1.2 while the published standard is v1.3, and the gate would be enforcing a document that no longer exists.

The *same season it is introduced* objection is the strongest part of the losing case and it is not answered by argument, only by scheduling: the gate is a roster checklist a mentor maintains and it is announced in the fall, so the two extra sessions are a planning cost rather than a surprise. `README.md` states the consequence in advance, next to the M0–M3 one, which is what that section already does with every other row.

**What would reopen this.** One cohort's evidence that the M7→M8 pair does not fit before kickoff. That is a scheduling finding, not an argument, and it would move the gate back — it would not move §2.6.

---

## 10. Next

An implementation plan under `superpowers/plans/2026-08-XX-m8-spec-anchored/`, per `plan-conventions.md`. Two things gate it, and neither is optional:

- **M7 has still never been run end to end by a mentor.** Its command sequences are transcribed from upstream documentation, and `openspec validate` has not been run against `SPEC-TEMPLATE.md`. M8 is built entirely on M7's output; building it first means building on a format nobody has executed.
- **The renumber and M8's content ship together** (§3), so the plan carries both and the integration task owns every renamed reference.
