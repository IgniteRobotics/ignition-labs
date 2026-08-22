# M9 — Spec-anchored development: the second AI milestone — design record

**Date:** 2026-08-22
**Build-order step:** new. §13 gains a numbered step — unlike the M7 spec work, this is a new milestone rather than content added to a shipped one
**Governing sections:** §8 (fade curve), §9 (milestone inventory — gains §9.9), §13 (build order), §15 (unmitigated risks)
**Source material:** [`2026-08-21-m7-spec-driven-development-design.md`](2026-08-21-m7-spec-driven-development-design.md) §2.1, which named three levels of SDD and deferred this one, and [`2026-08-21-m7-openspec-required-design.md`](2026-08-21-m7-openspec-required-design.md), which made the tooling required and the format exact

M7 teaches a student to write a spec, build against it, and ship. Then the change is archived and the student never touches it again. **M7's spec is write-once**, and that is the whole of what M7 can teach: there was nothing in `openspec/specs/` to write *against*, so every requirement a student has ever written has been an `ADDED` one against an empty file.

The thing M7 structurally cannot teach is what happens when a spec meets its second change — when behaviour someone already wrote down needs to become different behaviour, and there is now a document on disk asserting otherwise. That is the difference between having written a spec and running a codebase that has specs, and it is the only part of spec-driven development that compounds.

This record specifies the milestone that teaches it.

---

## 1. What ships

| Deliverable | Location | New? |
|---|---|---|
| Milestone page | `docs/programming/milestones/m9-spec-anchored-development.md` | new file |
| Project directory | `projects/m9-spec-anchored/` | new |
| Worked `MODIFIED` delta | `projects/m9-spec-anchored/MODIFIED-EXAMPLE.md` | new |
| The second-reader protocol | `projects/m9-spec-anchored/SPEC-REVIEW.md` | new |
| Capability backlog | `projects/m9-spec-anchored/CAPABILITY-BACKLOG.md` | new |
| Drafted-spec allowance | `docs/programming/ai-use-standard.md` §4 — takes it to v1.3 | edit |
| Ladder, nav, registration | `docs/programming/README.md`, `mkdocs.yml`, `tools/site_guard.py`, `tests/test_site_guard.py` | edit |
| §1.3, §9.9, §13 | `docs/programming/teaching-repo-spec.md` | edit |

**No Gradle project.** Like M7, M9 ships no skeleton and no tests — the work happens on `Robot-2026`. `tests.lock` is untouched.

---

## 2. Decisions

### 2.1 "Full SDD" means spec-anchored. Spec-as-source stays rejected, and M9 is where the rejection gets argued rather than asserted

The M7 record named three levels: **spec-first** (the spec guides the build, then you are done with it), **spec-anchored** (it survives into maintenance), **spec-as-source** (the spec is the artifact, code is generated, the tooling marks it `DO NOT EDIT`).

M7 takes spec-first. This milestone takes **spec-anchored**, which that record explicitly deferred rather than rejected: *"a plausible future for `Robot-2026` once there are enough archived specs to anchor to. It is not a thing to teach first."* That condition is now reachable — see §2.3.

**Spec-as-source remains rejected on principle**, and the principle is unchanged: a student who never edits code did not learn to write code, which is the failure `ai-use-standard.md` §1 exists to prevent. But M9 is the right place to stop *asserting* that and start *arguing* it, because it is the first point at which a student has enough experience to disagree competently. They will have written specs by hand, watched an agent misread one, and maintained one through a change. Handing them the spec-as-source position at that point — and asking them to make the strongest case for it before hearing ours — is a better defence of the line than repeating it.

**A student who argues Ignite out of this position has done something valuable.** The line is a judgement about what a training team should optimise for, not a law of nature, and it should be re-examined by people who have earned the standing to examine it.

### 2.2 The content is `MODIFIED` and `REMOVED` — changing behaviour someone else specified

Everything a student has written by the end of M7 is an `## ADDED Requirements` block. OpenSpec has two others, and they are where the difficulty lives:

```markdown
## MODIFIED Requirements

### Requirement: Vision exception occurrence count
The system SHALL <the full new behaviour, restated>.

## REMOVED Requirements

### Requirement: <name>
<why it is going>
```

Three things make this genuinely harder than M7, and they are the milestone:

1. **`MODIFIED` restates the requirement in full, not as a diff.** You must first read and understand the existing requirement well enough to rewrite it whole. A student who patches a phrase without reading the scenarios ships a requirement that contradicts its own examples.
2. **The prior author is usually gone.** In a team with a season's turnover, the person whose requirement you are changing has graduated. The spec is the only thing left of their reasoning, which is either the argument for keeping specs or the argument against — and the student finds out which by trying to use one.
3. **`REMOVED` requires justifying a deletion.** Removing a requirement is a claim that behaviour the team once decided on is no longer wanted. That is the hardest thing on this list to do honestly, and the easiest to do by accident.

**The student changes a capability they did not originally specify.** Assigning your own M7 requirement back to you halves the milestone.

### 2.3 M9 cannot run in the curriculum's first year, by construction — and that is a feature

`openspec/specs/` starts empty. It gains content only when M7 changes are archived (`openspec archive`, which the mentor runs post-merge). **Until several M7 students have shipped and archived, there is nothing to anchor to and this milestone has no content.**

That is a hard sequencing constraint and it should be written down rather than discovered:

- **Minimum viable input:** three or four archived changes across at least two distinct capabilities. Fewer, and every student modifies the same requirement.
- **Realistically:** M9 runs for **returning students in their second season**, on specs written by the cohort ahead of them.

This is the first milestone in the curriculum that is *inherently* a second-year milestone, and the curriculum currently has nothing for a returning student who finished M7 last season except M8 and the season itself. **M9 fills a real hole rather than extending a ladder for its own sake.** It also means the archive discipline in M7 stops being bookkeeping and becomes load-bearing: skip the archive step for a season and M9 has no inputs.

### 2.4 The assessment is the second-reader test, not another defense review

M7 already assesses by defense review, and repeating it would produce a milestone that feels like M7 with extra folders. The thing M9 must actually check is different: **is the updated spec true, and can somebody else act on it?**

> **The second-reader test.** A second student — who has not seen the diff — reads only the updated capability spec and states what the system now does, including at least one edge case. Then they read the diff.
>
> If their description does not match the code, **the spec is wrong**, regardless of whether the code is right.

Why this and not a defense review:

- It tests the property spec-anchored exists to provide. A spec's whole value is that a future person can trust it without reading the code; the test is simply that person, early.
- **The author cannot pass it by being articulate.** M7's defense review is a conversation the student drives. Here the student is silent while someone else tries to use their work, which is a much harder thing to talk your way through.
- It costs one peer and fifteen minutes, and it needs no mentor with deep context.
- It teaches both students. The reader is practising the skill of consuming a spec, which is the more common activity in a real team.

Failure here is normal and is not a retry: the student fixes the spec and the reader re-reads. **The number of round trips is itself the interesting signal** and should be reported rather than hidden.

### 2.5 Spec rot is the named enemy, and the milestone confronts it directly

Spec-anchored development has exactly one catastrophic failure mode: **a spec that describes what the code used to do.** A stale spec is worse than no spec, for the same reason `coding-standards.md` argues that a constitution silently omitting a standard is worse than no constitution — it tells a reader the rules while the system runs on different ones, and it is trusted precisely because it exists.

M9 teaches the detection and the discipline:

- `openspec validate --specs` and `openspec validate --strict` — structural checks, and what they do and do not catch. **They cannot tell you a requirement is false**, only that it is malformed. That limitation is the lesson.
- Reading a capability spec against the code it claims to describe, and finding the drift.
- The rule: **if you change behaviour a spec describes, the spec change ships in the same pull request.** Not a follow-up. A spec updated later is a spec that was wrong in the interim, and the interim is where somebody trusted it.

One of M9's project steps should be **finding an existing drift** — a place where an archived spec no longer matches `Robot-2026` — because by the time this milestone runs there will be some, and finding one is how a student learns the failure is real rather than theoretical.

### 2.6 M9 permits the agent to draft a spec, under red-line review — the next tier boundary

This is the decision most worth arguing with, and it is the natural continuation of how Ignite tiers everything else.

The pattern is already established twice. **Yellow → Green at M6:** you may lean on the agent once you have written the tests that catch it. **The spec rule at M7:** you write the spec yourself, because you have never written one and cannot yet review one. In both cases the tool is *earned by demonstrating you do not need it.*

By M9 a student has written conforming specs by hand, watched an agent misread one, and defended the divergence. They can now review a drafted spec competently, which is exactly the qualification M7 said they lacked. So:

> **At M9, `/opsx:propose` is permitted as a first draft that you must red-line.** You disclose that you used it, and you show what you changed and why — at minimum one requirement you rewrote and one scenario you added or deleted. A draft accepted unchanged is not a pass; it is evidence you did not review it.

This mirrors M7's own strongest prompt shape, inverted: at M7 you write and the agent critiques, at M9 the agent drafts and you critique. The skill being assessed is the same one — **holding the decisions** — and the red-line is the artifact that proves it.

**The risk is obvious and named in §8:** a student who rubber-stamps the draft has done less work than an M7 student and gets a harder milestone signed off. The second-reader test is the backstop, because a rubber-stamped spec fails it — the reader's description will not match code the student never really specified.

**Rejected: keeping the M7 ban.** It is the safe answer and it teaches that the rule was about the tool rather than about competence. The tiers exist to be climbed.

### 2.7 Fade level L0, and the scaffolding is one worked `MODIFIED` delta

M9 is past the end of the fade curve (§8 stops at M6–M8 = L0). Its one piece of scaffolding is `MODIFIED-EXAMPLE.md`: an existing capability spec, and the same spec after a `MODIFIED` change, side by side, with the reasoning for each edit.

Per the authoring rules it works a capability **no student will be assigned**, and `CAPABILITY-BACKLOG.md` — the M9 analogue of M7's feature backlog — must be restocked before it ships, for exactly the reason M7's had to be.

### 2.8 Numbering and placement

**M9**, after M8 in number but not in sequence. M8 runs *during* a season and takes a full season; M9 is offseason work for a student who has already done M7 and, typically, a season. The ladder in `docs/programming/README.md` needs a line saying so, because "M9 comes after M8" will otherwise read as "finish the season first," which is not the requirement.

**Prerequisite:** M7 complete, and `openspec/specs/` populated (§2.3).

---

## 3. Definition of done — draft

Mentor-verified, like M7's. Eight is the target; every item is a minute of someone's review.

- [ ] You changed a capability you did not originally specify
- [ ] Your change folder contains a `MODIFIED` or `REMOVED` requirement, restated in full
- [ ] `openspec validate` passes, and the spec change shipped in the same pull request as the code
- [ ] A second reader described the new behaviour correctly from your spec alone
- [ ] You can say how many round trips that took, and what you fixed each time
- [ ] You found one place where an existing spec had drifted from the code, and said what it would have cost someone
- [ ] If you used `/opsx:propose`, you can show one requirement you rewrote and one scenario you added or deleted
- [ ] You can make the strongest case *for* spec-as-source, and then say why Ignite stops short of it

The last item is §2.1's argument made assessable. A student who can only recite the Ignite position has not met it.

---

## 4. Spec amendments

`docs/programming/teaching-repo-spec.md`:

- **§1.3** — dated revision entry naming M9's addition, the new §13 step, `ai-use-standard.md` to v1.3, and this record as the originating document.
- **§9** gains **§9.9** — M9's inventory, given files, mentor-verified count, and the `openspec/specs/`-populated prerequisite.
- **§13** gains a numbered step. **This is the first genuinely new milestone since the original build order**, so unlike the M7 spec work it is not a content addition to a shipped step.
- **§8** — a sentence noting the fade curve ends at L0 and M9 sits past it.
- **§15** — `CAPABILITY-BACKLOG.md` joins `FEATURE-BACKLOG.md` as an unowned, depleting list. **It does not ship unowned**; see §7.

`docs/programming/ai-use-standard.md` → **v1.3**, for §2.6's drafted-spec allowance. Green's conditions are unchanged; what changes is the boundary in the spec rule, which currently forbids agent-drafted specs unconditionally and must now say *"before M9."* Anyone who acknowledged v1.2 signs again.

---

## 5. Verification

The usual documentation gates — `pytest`, `mkdocs build --strict`, `site_guard.py`, `tests_lock.py verify` (unchanged), `templates_sync.py verify`, and the byte-identity of any DoD list appearing in two files.

**What no command can check, and what therefore has to be done by hand:**

1. **The worked `MODIFIED` example round-trips through the real tool.** Write it, run `openspec validate`, and run `openspec archive` on a scratch branch to confirm the delta merges into the capability spec the way the example claims. This is M7's outstanding lesson applied in advance: *we shipped a curriculum around a format nobody had run.*
2. **The second-reader test is rehearsed by two mentors before a student meets it.** Its whole value rests on the reader being genuinely uninformed, and a protocol that has never been run will have leaks in it.
3. **`MODIFIED-EXAMPLE.md` is read for leaked answers** against every entry in `CAPABILITY-BACKLOG.md`.
4. **An actual drift is found in `Robot-2026`** before §2.5's project step is assigned. If no archived spec has drifted, that step has no content and should be cut rather than faked.

---

## 6. Prerequisites — not tasks

| # | Gate | Owner | Blocks |
|---|---|---|---|
| **Q1** | Three or four archived changes across at least two capabilities in `Robot-2026`'s `openspec/specs/` | Accumulates from M7; mentors must actually run `openspec archive` | Running M9 at all |
| **Q2** | An owner for `CAPABILITY-BACKLOG.md` | Programming lead | Merge. §15 already carries one unowned backlog and must not carry two |
| **Q3** | A mentor runs M9 end to end, including sitting on both sides of the second-reader test | Any M7-completed mentor | Teaching, not merge |

**Q1 is the interesting one.** It cannot be scheduled, only accrued, and it converts M7's archive step from hygiene into a dependency. That is worth saying out loud in M7's mentor note.

---

## 7. Risks

| Risk | Mitigation |
|---|---|
| **M9 has no inputs** because mentors skipped the archive step | Q1 names it; M7's `DEFENSE-REVIEW.md` already assigns `openspec archive` to the mentor who sat the review. Make the dependency explicit there |
| **The §2.6 relaxation gets rubber-stamped** — the agent drafts, the student ships it | The second-reader test fails a spec its author did not really write. The red-line is a DoD item and is shown, not asserted |
| **Spec rot outruns the curriculum** — specs accumulate faster than anyone maintains them | This is the real long-term risk and M9 does not solve it. §2.5 teaches detection; nobody owns repository-wide spec health, and this record does not pretend otherwise |
| **`CAPABILITY-BACKLOG.md` ships unowned** | Q2. This is the `FEATURE-BACKLOG.md` failure and it does not get the same answer twice |
| **The milestone is M7 with extra folders** | §2.4 is the guard: a different assessment, testing a different property. If the second-reader test is cut, this risk returns immediately and the milestone should be cut with it |
| **Session cost** — plausibly 2–3 sessions again, on top of a season | Measured on the first student through. M9 is offseason work for returning students, which is the cheapest session budget the team has |
| **OpenSpec churn** — `MODIFIED`/`REMOVED` semantics change upstream | Same posture as M7: the requirement syntax is the stable core; commands are named in one place per document |

---

## 8. Out of scope

- **Practising spec-as-source** (§2.1). Argued, not adopted.
- **Retrofitting specs onto M3–M6.** Those milestones are Yellow, and a spec-anchored loop presumes an agent the student is not permitted to use that way yet.
- **Full spec coverage of `Robot-2026`.** The delta model exists precisely so nobody has to do this, and attempting it would produce the stale documentation §2.5 warns about, at scale.
- **Wiring OpenSpec into CI**, here or in `Robot-2026`. Still nothing executes.
- **A `projects/m9-spec-anchored/` Gradle skeleton.** M9 ships no code, like M7.
- **Assigning repository-wide spec-health ownership.** Named in §7 as unmitigated.

---

## 9. Open questions for the programming lead

1. **Does §2.6 go too far?** It relaxes a rule written yesterday. The argument is that the tiers exist to be climbed and M9 is the earned rung; the counter-argument is that agent-drafted specs are the exact thing M7 exists to prevent and a second milestone should not undo it. This is the decision worth taking time over.
2. **Is M9 required, or a returning-student elective?** This record assumes elective-in-practice — nothing downstream depends on it — but does not decide.
3. **Does the second-reader test need a mentor present?** Cheaper without; more reliable with, at least for the first cohort.

---

## 10. Next

An implementation plan under `superpowers/plans/2026-08-XX-m9-spec-anchored/`, per `plan-conventions.md` — **but not yet.** Q1 is unmet and unschedulable, and M7 itself has not been run end to end by a mentor. Building M9 before an M7 student has archived a single change would be building on a format nobody has executed, which is the failure this repository has documented most often.

**The honest sequence:** run M7 for real, archive what ships, and revisit this record when `openspec/specs/` has something in it. The record is written now so the archive step is understood as load-bearing while M7 is being taught, rather than being rediscovered a season later.
