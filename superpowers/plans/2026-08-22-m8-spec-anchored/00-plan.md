# M8 spec-anchored development — implementation plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development`
> (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Steps
> use checkbox (`- [ ]`) syntax for tracking.

**Goal:** M7 teaches a student to write a spec, ship, and archive it. M8 is what M7
structurally cannot be — the milestone where a spec meets its *second* change. The student
extends their own archived M7 change with a `MODIFIED` or `REMOVED` requirement, and is
assessed by whether a second reader can act on the result.

**Architecture:** One new milestone page, one new project directory, one revision to a signed
document, a rename of the shipped M8 to M9, and a serialized tail for the contended files.
Nothing compiles; this is prose plus registration.

**Tech Stack:** MkDocs + Material, pytest, `tools/site_guard.py`, `tools/templates_sync.py`.
No Gradle, no Java, no tests.

**Spec:** [`superpowers/specs/2026-08-22-m8-spec-anchored-design.md`](../../specs/2026-08-22-m8-spec-anchored-design.md)
— the plan argues from the spec; executors read both. Its §2.4 (the second-reader test), §2.5
(spec rot), and §4 (the DoD draft) are the content sources. **Do not re-derive them.**

**Prior art:** [`2026-08-21-m7-openspec-required-design.md`](../../specs/2026-08-21-m7-openspec-required-design.md)
§4 records the verified OpenSpec behaviour every task here depends on — the delta syntax,
`openspec archive`, and the `MODIFIED`/`REMOVED` flow, all executed against 1.10.0.

---

## Global constraints — delta only

`CLAUDE.md` at the repository root loads automatically and already carries the toolchain,
pinned versions, package naming, the fade curve, authoring rules, the clean-clone invariant,
and verification discipline. **Do not restate any of it.** What is specific to this plan:

- **No Gradle project is modified.** No `build.gradle`, no source set, no given test.
- **`tests.lock` is not regenerated.** M8 ships no tests. `poetry run python
  tools/tests_lock.py verify` must return rc=0 **and unchanged** at every point.
- **Nothing in this repository runs OpenSpec.** No `openspec/` directory here, no CLI
  invocation, no CI wiring. The live `openspec/` lives in `Robot-2026`.
- **M8 is fade level L0 — worked example only.** Its one piece of scaffolding is
  `MODIFIED-EXAMPLE.md`, and it works a capability no student will be assigned.
- **M7's DoD pair must stay byte-identical.** `docs/programming/milestones/m7-ai-assisted-development.md`
  and `projects/m7-ai-assisted/README.md` carry the same eleven `- [ ]` lines. **No task here
  edits them**, and task 9 verifies they still match.
- **`ai-use-standard.md` is a signed document.** Task 3 takes it to v1.3; the revision must be
  visible in the header, not slipped in.

---

## Decisions taken before execution — these are settled, do not relitigate

Three questions the design record left open were answered by the programming lead on
2026-08-22:

1. **The agent may draft a spec at M8, red-lined.** Design §2.6 stands. `openspec-propose`
   becomes permitted as a first draft the student must red-line and show.
2. **M8 is required for AI-tooling users.** The Kickoff Readiness Gate row becomes *"+ M7 and
   M8, and a signed AI-use standard."* **This reverses design §2.8**, which said M8 would not
   join the gate. Task 9 corrects the record rather than letting it silently contradict the
   curriculum.
3. **The renamed page's URL break is accepted.** No `mkdocs-redirects`, no announcement — the
   site is v0 and will be republished.

---

## File structure

```
docs/programming/milestones/m8-spec-anchored-development.md   NEW   task 1
projects/m8-spec-anchored/README.md                           NEW   task 2
projects/m8-spec-anchored/MODIFIED-EXAMPLE.md                 NEW   task 2
projects/m8-spec-anchored/SPEC-REVIEW.md                      NEW   task 2
docs/programming/ai-use-standard.md                           edit  task 3
docs/programming/milestones/m9-in-season-deliverables.md      RENAME task 4
projects/m9-ownership/                                        RENAME task 4
mkdocs.yml                                                    edit  task 4
tools/site_guard.py                                           edit  task 4
tests/test_site_guard.py                                      edit  task 4
tools/changed_projects.py                                     edit  task 4
tests/test_changed_projects.py                                edit  task 4
.github/workflows/skeleton-integrity.yml                      edit  task 4
docs/programming/{curriculum-spec,resources,student-deliverables,robot-repo-ladder,git-workflow}.md
                                                              edit  task 4
docs/programming/tracks/*.md                                  edit  task 4
docs/programming/README.md                                    edit  task 9   CONTENDED
docs/programming/teaching-repo-spec.md                        edit  task 9   CONTENDED
superpowers/specs/2026-08-22-m8-spec-anchored-design.md       edit  task 9
```

---

## Task graph

| Task | File | Depends on | Touches | Lane |
|---|---|---|---|---|
| 1 | `01-milestone-page.md` | — | `docs/programming/milestones/m8-spec-anchored-development.md` | `m8prose` |
| 2 | `02-project-files.md` | — | `projects/m8-spec-anchored/` | `m8project` |
| 3 | `03-ai-use-standard.md` | — | `docs/programming/ai-use-standard.md` | `standard` |
| 4 | `04-renumber.md` | 1 | the rename, registration, and ~30 prose references | `renumber` |
| 9 | `99-integration.md` | all | `programming/README.md`, `teaching-repo-spec.md`, design record, sweep | `integration` |

**Lanes:** `m8prose` (1), `m8project` (2), `standard` (3) are mutually disjoint and may run
concurrently. `renumber` (4) waits on 1 because it registers the page task 1 creates.
**Maximum useful concurrency: 3**, and only at the start.
**Serialized tail:** task 9, single owner, after every lane lands.

### Why task 4 waits on task 1

Task 4 owns `mkdocs.yml` and `tools/site_guard.py`. Both the new M8 page and the renamed M9
page need entries there, and splitting one file across two agents is how nav edits get
clobbered. Task 1 writes the page; task 4 registers it. Task 4 cannot verify its own
`mkdocs build --strict` until the page it points at exists.

### Be honest about whether to fan out

Three lanes is *available*, not obviously *worth it*. This is prose and a rename, with no
compilation and no suite to wait on. **Running 1 → 2 → 3 → 4 → 9 sequentially in one lane is
a legitimate choice and needs no plan change.** The one thing that is not optional: task 9
runs last, alone, on the merged result.

If you do fan out: one worktree per lane under `.worktrees/<lane>`, never two agents in one.

---

## Constraints on the things no command can verify

1. **`MODIFIED-EXAMPLE.md` is read for leaked answers** — task 2. It must not contain the
   implementation of any capability a student will be assigned.
2. **The second-reader protocol is walked by two people before a student meets it** — its
   value rests entirely on the reader being genuinely uninformed, and a protocol nobody has
   run will have leaks in it. Task 2 writes it; rehearsing it is prerequisite Q2.
3. **Every DoD item is verifiable by a mentor without a test** — tasks 1 and 2. M8 has no
   suite; an item nobody can check is decoration.
4. **The renumber is classified, not substituted** — task 4. See its brief.

Report actual command output for every verification step. A task report that says "the build
is clean" without the run is not a completed task.

---

## Self-review

**Spec coverage.** Every design-record section maps to a task:

| Spec § | Task |
|---|---|
| 2.1 spec-anchored; spec-as-source argued not adopted | 1 (Concepts), 1 (DoD item) |
| 2.2 `MODIFIED` / `REMOVED`, and what validation does and does not catch | 1 (concept), 2 (worked example) |
| 2.3 the student extends their own M7 change | 1 (project steps), 2 (README) |
| 2.4 the second-reader test | 2 (`SPEC-REVIEW.md`), 1 (assessment section) |
| 2.5 spec rot; the `## Purpose` stub | 1 (Concepts), 2 (README first step) |
| 2.6 agent may draft, red-lined | 3 (governing), 1 (restated) |
| 2.7 fade L0, one worked `MODIFIED` delta | 2 |
| 2.8 placement and gating — **reversed, see Decisions** | 4 (ladder), 9 (gate + record) |
| 3 the renumber | 4 |
| 4 the DoD draft | 1 and 2, identical wording |
| 5 spec amendments | 9 |
| 7 risks | carried into the tasks that own them |

**What this plan does not cover, deliberately:** prerequisite Q1 (a student's own M7 change
being archived — accrues per student), Q2 (a mentor rehearsing the second-reader test), P2
(naming the constitution's owner), and the `pytest` 8.4.2 → 9.0.3 advisory, which is a major
bump behind a `^8.0` constraint and belongs in the §16 maintenance pass.
