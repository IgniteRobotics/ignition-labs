# M7 AI-Assisted, M8 Ownership, and the Gradle-discovery fix — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans. Steps use checkbox (`- [ ]`) syntax for tracking.
>
> This is a **split plan** per [`superpowers/plan-conventions.md`](../../plan-conventions.md). Read this file plus the one task file you were handed. Do not read sibling task files — each is self-sufficient, and reading them all is the cost this layout exists to remove.

**Goal:** Ship `projects/m7-ai-assisted/` (five given documents; no code, no tests — M7's assessment is a defense review) and `projects/m8-ownership/` plus six real fill-in-the-blank files in `templates/`, guarded by a new drift check. This is build-order step 11 (spec §13).

**Architecture:** Nothing here is a Gradle project and nothing here ships a given test. That is the whole shape of this plan, and it is why Task 1 exists: `tools/changed_projects.py` assumes every directory under `projects/` is a Gradle project, and M7 and M8 are the first that are not. Task 1 lands before either lane, because it is the thing that breaks when they land.

**Tech Stack:** Markdown, Python 3.11 (Poetry), pytest. No Java, no Gradle, no vendordeps.

**Spec:** [`superpowers/specs/2026-08-18-m7-m8-design.md`](../../specs/2026-08-18-m7-m8-design.md). **Read §3 before Task 1 and §4 before Task 3.**

---

## Constraints delta

`CLAUDE.md` at the repository root loads automatically and already carries the toolchain, pinned versions, Spotless wiring, package naming, the fade curve, the authoring rules, the clean-clone invariant, and the verification discipline. **Read it. It is not repeated here.** Only what is specific to M7 and M8 appears below.

- **Fade level L0 — worked-example-only** (spec §8) for both milestones. There is no scaffold to fill in. Everything shipped here is a document a student reads, copies, or fills in.
- **The clean-clone invariant does not apply.** Neither project has a `settings.gradle`, an `assemble`, or a test. Do not add one to "make it consistent" — §9.8 says M7 ships almost no code and `Tests cover: nothing`, and §9.9 says M8 ships no skeleton code. A Gradle wrapper in either directory would put it back into the CI matrix that Task 1 exists to keep it out of.
- **No test-out fixture for either milestone.** §10's table stops at `t5-drifting-auto`.
- **No reference implementation for either milestone.** There is nothing for `cross-check.sh` to overlay, and because its project discovery keys on `settings.gradle`, neither directory is enumerated at all — they produce no line in its output, not even `SKIP`. `m0-onboarding` does print `SKIP`, because it has a `settings.gradle`; that difference is why the analogy misleads. Task 6 ships authoring notes only.
- **Pinned SHA.** `Robot-2026` → `28ce5397d88275001d656dbb717d22cf9ac0150d`. Every copied file and every symbol named in a student-facing document is pinned to it and must be verified to exist there before shipping.
- **`ai-code-review.yml` and its three supporting files are reference copies, never wired to CI here.** They must not be placed in `.github/`. See spec §2.1: running them in this repository would need a `GEMINI_CODE_REVIEW_KEY` secret on the student-facing repo and per-PR spend, to duplicate a workflow that already runs on the `Robot-2026` pull requests M7's work actually goes to.
- **No AI-tool API key, secret, or paid service may be required by anything this plan ships.** M7 is 🟢 Green tier, but the tier governs how a student may use AI, not what this repository must pay for.
- **`docs/programming/README.md` and `docs/programming/teaching-repo-spec.md` belong to Task 7 only.** They are contended; two lanes finishing together clobber each other there, every time. `tests.lock` is *not* touched by this plan — nothing here matches its patterns.

---

## File structure

**Created — `projects/m7-ai-assisted/`**

| File | Responsibility |
|---|---|
| `README.md` | What M7 is, the order to work in, what the five artifacts are for |
| `reference/ai-code-review.yml` | Copy of the real workflow, pinned. Reference only — not wired |
| `reference/gemini-code-review.py` | The script the workflow runs |
| `reference/requirements.txt` | Its dependencies |
| `reference/code-review-prompt.md` | The prompt. A DoD item requires the student read this |
| `pull-request-template.md` | Carries §6's disclosure block verbatim |
| `FEATURE-BACKLOG.md` | Mentor-maintained candidate features, seeded from real ones |
| `DEFENSE-REVIEW.md` | The mentor question bank, published openly |
| `MISCALIBRATION.md` | Worksheet for the deliberate-wrong-answer exercise |

**Created — `projects/m8-ownership/`**

| File | Responsibility |
|---|---|
| `README.md` | Points at `templates/` and the week-by-week cadence |

**Created — `templates/`** (currently holds only `CONTRIBUTOR.md`)

| File | Source region in `student-deliverables.md` |
|---|---|
| `subsystem-readme.md` | §1, fenced |
| `tuning-log.md` | §2, fenced |
| `pr-description.md` | §3, fenced |
| `competition-week-checklist.md` | §4, **unfenced checklist** |
| `pit-documentation.md` | §5, fenced |
| `post-season-retro.md` | §6, fenced |

**Created — tooling**

| File | Responsibility |
|---|---|
| `tools/templates_sync.py` | Verify each `templates/` file matches its marked region |
| `tests/test_templates_sync.py` | Coverage for the above |

**Modified**

| File | Change |
|---|---|
| `tools/changed_projects.py` | Require `settings.gradle`; take an injectable root |
| `tests/test_changed_projects.py` | Regression for non-Gradle directories |
| `docs/programming/student-deliverables.md` | Add HTML comment markers around six regions |
| `.github/workflows/docs-links.yml` | Run `templates_sync.py verify` |

---

## Task graph

| Task | File | Depends on | Touches | Lane |
|---|---|---|---|---|
| 1 | `01-gradle-discovery-fix.md` | — | `tools/changed_projects.py`, `tests/` | prerequisite |
| 2 | `02-m7-scaffold-and-reference.md` | 1 | `projects/m7-ai-assisted/` reference + README + PR template | m7 |
| 3 | `03-m7-backlog-defense-miscalibration.md` | 2 | `projects/m7-ai-assisted/` three documents | m7 |
| 4 | `04-m8-templates.md` | 1 | `templates/`, `projects/m8-ownership/`, `student-deliverables.md` markers | m8 |
| 5 | `05-templates-sync.md` | 4 | `tools/templates_sync.py`, `tests/`, CI | m8 |
| 6 | `06-authoring-notes.md` | 3, 5 | `../ignite-training-solutions/` | tail |
| 7 | `99-integration.md` | all | `docs/programming/README.md`, spec §13 | integration |

**Lanes:** `prerequisite` (task 1, alone — everything waits on it), `m7` (2 → 3, sequential), `m8` (4 → 5, sequential).
**Maximum useful concurrency: 2**, and only after task 1 lands. Do not fan out further.
**Serialized tail:** task 6, then task 7, on the merged branch.
**Worktrees:** `.worktrees/m7` and `.worktrees/m8`. Never two agents in one worktree.

**Task 1 is not optional and cannot be deferred to integration.** It is the only task here that fixes a live defect, and the defect fires on the first student pull request that touches anything either lane creates.

---

## Self-review

**Spec coverage.**

| Spec requirement | Task |
|---|---|
| §9.8 `ai-code-review.yml` given, from `Robot-2026` | 2 (reference copy, per design §2.1) |
| §9.8 pull request template with §6 disclosure block | 2 |
| §9.8 `FEATURE-BACKLOG.md`, mentor-maintained, real features | 3 (seeded from design §4) |
| §9.8 `DEFENSE-REVIEW.md`, published openly | 3 |
| §9.8 `MISCALIBRATION.md` with a place to paste what the tool said | 3 |
| §9.8 tests cover nothing; assessment is the defense review | 3 (no test ships) |
| §9.9 deliverables become real files in `templates/` | 4 |
| §9.9 `projects/m8-ownership/README.md` points at `templates/` and the cadence | 4 |
| Design §2.2 both copies survive behind a drift guard | 5 |
| Design §3 `changed_projects.py` selects non-Gradle directories | 1 |
| §11.1 authoring notes | 6 |
| §13 step 11 status | 7 |

**Not covered, deliberately.** Step 12's doc-correction pass — §7.3's CI claim in three files, M6's "first tests" wording, M4's required-PR DoD item, `curriculum-spec.md` §8's status table, `resources.md`'s re-verification list, §9.5's stale `26.1.0`, §9.6's missing photonlib version, and the `t4-bad-kv` rename. Step 11 ships what step 11 owns.

**Known limitation, recorded rather than fixed.** `FEATURE-BACKLOG.md` ships seeded but goes stale by design — entries get consumed as students ship them. Task 3 writes the selection criteria first and the entries second, so a mentor refilling it has the rule rather than only the examples. The file says so in its own header.
