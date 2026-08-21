# M7 spec-driven development and the coding-standards constitution — implementation plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development`
> (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Steps
> use checkbox (`- [ ]`) syntax for tracking.

**Goal:** M7 gains the front half of its loop — a spec the student writes before the agent
starts — and Ignite gains the standing half: a coding-standards constitution that exists
somewhere a human can read it, and that code review enforces rather than replaces.

**Architecture:** One new published page, four prose edits across `docs/`, four file changes
under `projects/m7-ai-assisted/`, and a serialized tail for the two contended files. The new
page must land before anything links to it — CI builds with `mkdocs build --strict`, which
turns an unresolved relative link into a build failure.

**Tech Stack:** MkDocs + Material, pytest, `tools/site_guard.py`. No Gradle, no Java, no
tests.

**Spec:** [`superpowers/specs/2026-08-21-m7-spec-driven-development-design.md`](../../specs/2026-08-21-m7-spec-driven-development-design.md)
— the plan argues from the spec; executors read both.

**Report:** [`superpowers/notes/2026-08-21-spec-driven-development-for-m7.md`](../../notes/2026-08-21-spec-driven-development-for-m7.md)
— the framework survey behind decision 2.2. Read it only if you need to know *why* OpenSpec;
the spec is sufficient to execute.

---

## Global constraints — delta only

`CLAUDE.md` at the repository root loads automatically and already carries the toolchain,
pinned versions, Spotless wiring, package naming, the fade curve, authoring rules, the
clean-clone invariant, and verification discipline. **Do not restate any of it.** What is
specific to this plan:

- **No Gradle project is modified.** No `build.gradle`, no source set, no given test. If you
  run `./gradlew` for anything but a sanity check, stop — you are outside the plan.
- **`tests.lock` is not regenerated.** M7 ships no tests (spec §9.8) and nothing here adds
  one. `poetry run python tools/tests_lock.py verify` must return rc=0 unchanged at every
  point. If it does not, you touched something under a `src/test/`.
- **Nothing in this repository runs OpenSpec** (design §2.8). No `openspec/` directory here,
  no CLI invocation, no CI wiring. M7 ships a template and instructions; the live
  `openspec/` lives in `Robot-2026`. This mirrors the reference-copy pattern step 11 already
  set for `ai-code-review.yml`.
- **M7 is fade level L0 — worked example only.** One worked spec, and it is a worked example
  of a feature no student will ship (design §2.7).
- **The DoD list exists in two files and they must not drift.**
  `docs/programming/milestones/m7-ai-assisted-development.md` and
  `projects/m7-ai-assisted/README.md` carry the same nine items verbatim. Both go to eleven,
  in tasks 3 and 7 respectively, with identical wording.

---

## Prerequisites — not tasks, because no agent in this repository can do them

Three gates. The first two block **merge**; the third blocks **teaching**, and the
distinction matters — the change is correct and mergeable before a student ever runs it.

| # | Gate | Owner | Blocks |
|---|---|---|---|
| **P1** | One commit on `Robot-2026`: `openspec init --tools agents`, plus empty `openspec/specs/` and `openspec/changes/`. Generic target only — nothing vendor-specific (design §2.4) | Season lead. It is their repository | Merge of task 7, whose README tells students the folder is already there |
| **P2** | A named owner and ratifier for `coding-standards.md` | Programming lead | Merge of task 2. An unowned constitution is `FEATURE-BACKLOG.md` again (spec §15) |
| **P3** | A mentor runs the whole M7 loop once — writes a spec from the template, ships it into `Robot-2026`, and sits the defense review from the other side | Any M7-completed mentor | Running M7 with a student. Not merge |

**P3 is the one this repository has historically skipped.** `CLAUDE.md`: *a fixture nobody
has finished is a fixture nobody knows is completable.* Its most valuable output is a
negative result — if the honest answer to *"where did it diverge from your spec?"* is *"it
didn't,"* the worked example is teaching the wrong scale and task 6 needs redoing.

---

## File structure

```
docs/programming/coding-standards.md                        NEW   task 2
docs/programming/milestones/m7-ai-assisted-development.md   edit  task 3
docs/programming/ai-use-standard.md                         edit  task 4
docs/programming/milestones/m1-java-fundamentals.md         edit  task 5
docs/programming/milestones/m8-in-season-deliverables.md    edit  task 5
projects/m7-ai-assisted/FEATURE-BACKLOG.md                  edit  task 1
projects/m7-ai-assisted/SPEC-TEMPLATE.md                    NEW   task 6
projects/m7-ai-assisted/DEFENSE-REVIEW.md                   edit  task 7
projects/m7-ai-assisted/pull-request-template.md            edit  task 7
projects/m7-ai-assisted/README.md                           edit  task 7
mkdocs.yml                                                  edit  task 2
tools/site_guard.py                                         edit  task 2
tests/test_site_guard.py                                    edit  task 2
docs/programming/teaching-repo-spec.md                      edit  task 9   CONTENDED
docs/programming/README.md                                  edit  task 9   CONTENDED
```

---

## Task graph

| Task | File | Depends on | Touches | Lane |
|---|---|---|---|---|
| 1 | `01-backlog-restock.md` | — | `projects/m7-ai-assisted/FEATURE-BACKLOG.md` | `m7project` |
| 2 | `02-constitution.md` | — | `docs/programming/coding-standards.md`, `mkdocs.yml`, `tools/site_guard.py`, `tests/test_site_guard.py` | `constitution` |
| 3 | `03-m7-milestone.md` | 2 | `docs/programming/milestones/m7-ai-assisted-development.md` | `m7prose` |
| 4 | `04-ai-use-standard.md` | 2, 3 | `docs/programming/ai-use-standard.md` | `m7prose` |
| 5 | `05-m1-m8-references.md` | 2 | `docs/programming/milestones/m1-java-fundamentals.md`, `m8-in-season-deliverables.md` | `m7prose` |
| 6 | `06-spec-template.md` | 1 | `projects/m7-ai-assisted/SPEC-TEMPLATE.md` | `m7project` |
| 7 | `07-m7-project-files.md` | 6 | `projects/m7-ai-assisted/{DEFENSE-REVIEW,pull-request-template,README}.md` | `m7project` |
| 9 | `99-integration.md` | all | `docs/programming/teaching-repo-spec.md`, `docs/programming/README.md`, full gate sweep | `integration` |

**Lanes:** `m7project` (1 → 6 → 7), `constitution` (2), then `m7prose` (3 → 4 → 5).
**Maximum useful concurrency: 2**, throughout.
**Serialized tail:** task 9, single owner, after both lanes land.

Two agents: one takes `m7project` and runs 1 → 6 → 7 start to finish. The other takes task 2,
and when it lands, picks up `m7prose` and runs 3 → 4 → 5. Nobody waits.

### Why `m7prose` waits on task 2

Their edits are disjoint from task 2's — different files. Their **verification is not.**
Tasks 3, 4 and 5 each add a relative link to `docs/programming/coding-standards.md`, and
`pages.yml` builds with `mkdocs build --strict`, which promotes MkDocs' unresolved-link
warning to a failure. A lane shipping a link to a page that does not exist yet cannot run its
own gate — it can only run a weaker one and hope, which is how a real failure gets waved
through.

Task 7 links to the constitution too, but by **name**, not by relative path: `projects/` is
outside `docs_dir` and MkDocs never reads it. That is why `m7project` needs no task-2
dependency.

### Why tasks 3 and 4 are ordered, not concurrent

They are content-coupled in one direction. Task 3 writes the Yellow-tier spec rule into M7's
Concepts section in the milestone's voice; task 4 writes the governing version into
`ai-use-standard.md` §4 and the two worked-case rows into §9. **The policy text is
authoritative and the milestone restates it**, so task 4 must be free to correct task 3's
phrasing without a third round trip. Running 4 before 3 inverts that and produces a milestone
that paraphrases a rule it cannot see.

### Be honest about whether to fan out at all

Two agents is *safe* here rather than obviously *worth it*. This is roughly 900 lines of
prose across eleven files with no compilation, no Gradle, and no suite to wait on. **Running
1 → 2 → 3 → 4 → 5 → 6 → 7 → 9 sequentially in one lane is a legitimate choice and needs no
plan change.** The one thing that is not optional: task 9 runs last, alone, on the merged
result.

If you do fan out: **one worktree per lane**, under `.worktrees/<lane>`, never two agents in
one worktree.

---

## Constraints on the things no command can verify

The spec names four checks a green build does not prove. Three belong to tasks; one is P3.

1. **The constitution against the review prompt, line by line** — task 2. Every standard in
   `code-review-prompt.md` appears in the document or is dropped with a recorded reason. A
   constitution that silently omits a standard the bot still enforces is *worse* than no
   constitution: it tells a student the rules while a reviewer applies different ones.
2. **`SPEC-TEMPLATE.md` read for leaked answers** — task 6. It must not contain the
   implementation of any feature a student will pick, in prose, in a scenario, or in a
   heading.
3. **Every DoD item is verifiable by a mentor without a test** — tasks 3 and 7. M7 has no
   suite; an item nobody can check is decoration.
4. **The end-to-end run** — P3, outside this repository.

Report actual command output for every verification step. A task report that says "the build
is clean" without the run is not a completed task.

---

## Self-review

Run after the plan is written, against the spec, before execution.

**Spec coverage.** Every spec section maps to a task or a prerequisite:

| Spec § | Task |
|---|---|
| 2.1 spec-first, and where it stops | 3 |
| 2.2 OpenSpec format-first, CLI optional, both paths | 3 (concept), 7 (both setup paths) |
| 2.3 Yellow-tier rule; Green's fourth condition | 4 (governing), 3 (restated) |
| 2.4 specs ship; mentor seeds the scaffolding | P1, 7 (README), 3 (project step) |
| 2.5 constitution is the source | 2 |
| 2.6 the concept is taught | 3 (Concepts), 5 (M1, M8), 3 + 7 (DoD retarget) |
| 2.7 worked example of a feature nobody ships | 1, 6 |
| 2.8 nothing here runs OpenSpec | global constraint |
| 3 documentation ownership | 2 (header), 9 (§5.7 promotion) |
| 4 spec amendments | 9 |
| 5 DoD nine → eleven | 3 and 7, identical wording |
| 6 registration — nav and `REQUIRED_PAGES` | 2 |
| 7 verification | per task, swept in 9 |

**What this plan does not cover, deliberately:** the `Robot-2026` seeding commit (P1), naming
the constitution's owner (P2), the end-to-end run (P3), and `FEATURE-BACKLOG.md` gaining a
standing in-season owner — which spec §15 still lists as unassigned and task 1 does not fix.
Task 1 restocks the list; it does not staff it.
