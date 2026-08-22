# M7 goes opinionated: OpenSpec required, and Green means the loop succeeded — design record

**Date:** 2026-08-21
**Supersedes:** parts of [`2026-08-21-m7-spec-driven-development-design.md`](2026-08-21-m7-spec-driven-development-design.md) — specifically §2.2's *CLI optional, manual path first* and the artifact-shaped reading of §2.3's Green condition. **That record is not edited.** It correctly records what was decided and why, and the reversal is more useful with the original argument still readable beside it.
**Governing sections:** §9.8 (M7 inventory), §5.7 (documentation ownership), §13 (build order)
**Decided by:** programming lead, on review of the shipped M7 material

The first record shipped M7 with the spec format as a suggestion: OpenSpec's shape loosely, the CLI optional, a hand-written fallback documented first, and Green's fourth condition satisfied by a committed file. Read back after it shipped, that is a hedge rather than a curriculum. This record replaces the hedge.

---

## 1. What changed, and why

### 1.1 The CLI is required. Node is an M7 prerequisite

**Reversed from §2.2.** The original argument was real: making `npm install` a gate on completing a milestone risks a student losing a session to something that was never their problem, which is the failure the clean-clone invariant (§12.3) exists to prevent.

It loses to a simpler point. Ignite already requires a toolchain to do M3 — nobody argues WPILib should be optional because an install might fail. A tool the curriculum *recommends* is a tool half the cohort will skip, and then M7 assesses two different practices under one done list. Node 20.19+ moves where it belongs: a **prerequisite installed before the first session**, and a laptop that cannot run it is a mentor's problem to solve in advance rather than a student's to discover at the keyboard.

The manual-fallback path is **removed**, not demoted. A documented fallback is an invitation, and its existence was what made the format negotiable.

### 1.2 The format is OpenSpec's real format, exactly

The original template collapsed a change into one file with headings of our own choosing. Upstream OpenSpec is a change folder containing `proposal.md`, `tasks.md`, an optional `design.md`, and delta specs at `specs/<capability>/spec.md`, whose requirements are checked structurally:

```markdown
## ADDED Requirements

### Requirement: Name
The system SHALL <observable behaviour>.

#### Scenario: Case
- GIVEN <state>
- WHEN <action>
- THEN <outcome>
```

`SPEC-TEMPLATE.md` is rewritten to that shape, showing all three files. Two upstream rules are taught by example because they are the ones people break: **one behaviour per requirement** (an *and also* means it was two), and **requirements state observable behaviour** — mechanism belongs in `## Approach` or `design.md`.

**This makes `openspec validate` meaningful, and it becomes a done item.** The previous arrangement had the milestone inviting students to run a command that would reject the template the milestone handed them.

### 1.3 Green means the loop succeeded, not that a file exists

**`ai-use-standard.md` v1.2.** Green's first condition changes from *you wrote a spec and it is committed* to *you applied spec-driven development successfully*: a conforming change committed before the first implementation commit, the feature built against it, and the student able to walk the spec and name where the build diverged from it and why.

The divergence question is what makes this assessable rather than aspirational. Every real build diverges; a student who cannot say where was not steering by the document. That is cheap to check in a defense review and expensive to fake.

This is a change to a **signed** document. Anyone who acknowledged v1.0 or v1.1 signs again.

### 1.4 One OpenSpec command is switched off, deliberately

`/opsx:propose` has the agent draft the proposal, the delta spec, and the task list. It is the tool's happy path and it is **forbidden at Ignite**, because it collides head-on with the original §2.3 — the spec is Yellow-tier work even at Green, since the spec is where the student's decisions live.

**Adopting a tool is not adopting its defaults.** This is taught rather than merely enforced: a tool whose default is to do the interesting part for you is a shape students will meet repeatedly, and recognising it is worth more than any prompt technique in the milestone. `/opsx:apply` is likewise not a route to a written spec. Handing a finished draft to an agent and asking *"what did I miss?"* remains encouraged and is still the highest-value prompt in M7.

**§2.3 is unchanged and this record does not weaken it.** It is the reason for the carve-out.

---

## 2. What is not changed

- **Spec-first, not spec-anchored or spec-as-source** (original §2.1). Unchanged, including the principled refusal of spec-as-source.
- **A mentor seeds `openspec/`, not the first student** (original §2.4). Unchanged and now more load-bearing: students never run `openspec init`, so the vendor-shim problem cannot arise from a student's clone at all.
- **Nothing in `ignition-labs` runs OpenSpec** (original §2.8). Unchanged. The CLI runs in a student's clone of `Robot-2026`; this repository still ships reference material only, with no `openspec/` directory and no CI wiring.
- **The constitution, and its ownership boundary** (original §2.5, §3). Untouched.
- **`SPEC-TEMPLATE.md` remains a worked example of a feature no student ships** (original §2.7), and remains short. Showing three real files costs length; the sixteen-criteria warning stays, and the template models two requirements and five scenarios.

---

## 3. Consequences

| Consequence | Where |
|---|---|
| Node 20.19+ becomes an M7 prerequisite | milestone At-a-glance; project README |
| Manual spec-writing path removed | project README |
| `SPEC-TEMPLATE.md` rewritten to real OpenSpec, three files | `projects/m7-ai-assisted/` |
| Green condition 1 is competence-based; v1.2; re-acknowledgment | `ai-use-standard.md` §4, §12, header |
| DoD item gates on `openspec validate` passing | milestone and project README, byte-identical |
| `openspec archive` named as the mentor's post-merge step | `DEFENSE-REVIEW.md` |
| `/opsx:propose` forbidden, and the reason taught | milestone Concepts; project README |

---

## 4. Verification, and the thing nobody has done

The documentation gates are the usual set — `pytest`, `mkdocs build --strict`, `site_guard.py`, `tests_lock.py verify`, `templates_sync.py verify`, and the byte-identity of M7's two DoD lists.

**None of them run OpenSpec, and that is now the largest gap in this change.** Every command sequence in the shipped material is written from upstream documentation, not from execution. Before a student meets this:

1. `npm install -g @fission-ai/openspec@latest` on a lab-representative machine.
2. `openspec init --tools agents` on `Robot-2026` — prerequisite P1, still unmet.
3. `openspec new change <name>`, then reproduce `SPEC-TEMPLATE.md`'s worked example as real files.
4. **`openspec validate <name>` — and it must pass.** If it does not, the template is wrong and this record's §1.2 is the thing to fix, not the student's spec.
5. `openspec archive <name>` and confirm the delta lands in `openspec/specs/`.

That is the mentor dry-run (P3) with a concrete checklist instead of a vibe. Its most valuable outcome is still a negative result: if step 4 fails, we shipped a curriculum around a format we never ran.

**Upstream documentation is not fully self-consistent about `proposal.md`'s headings** — `concepts.md` describes intent/scope/approach, `examples.md` glosses it as "why and what". The delta-spec format, which is what validation actually checks, is quoted unambiguously and is what the template follows exactly. Step 4 settles the rest.

---

## 5. Risks

| Risk | Mitigation |
|---|---|
| **A student is blocked by `npm install`** — the exact failure §2.2 avoided | Moved to a prerequisite installed before session one, with a named mentor owner. Accepted deliberately: the cost of an optional format is two practices under one done list |
| **The template does not validate** | §4 step 4. Unmitigated until someone runs it |
| **OpenSpec churns** — a young tool, and its docs already disagree with themselves | Requirement syntax is the stable core and is what we teach; commands are named in one place per document, so a rename is a small edit |
| **Session cost rises again** | Measured on the first student through. If M7 lands past three sessions, the miscalibration exercise is the first thing to cut, not the spec |
| `/opsx:propose` used anyway | Not detectable from the artifact. The divergence question is the backstop — it is hard to answer about a spec you did not write |
