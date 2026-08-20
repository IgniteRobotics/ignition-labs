# M0 VS Code setup and team git usage — implementation plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development`
> (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Steps
> use checkbox (`- [ ]`) syntax for tracking.

**Goal:** M0 names the extensions the team standardizes on, maps its eight-command git
allow-list onto the Source Control panel a student already has open, and explains how the
team works on `Robot-2026` without asking a student to practise a flow they have no
repository for yet.

**Architecture:** Three disjoint documentation lanes plus a serialized tail. A new page
carries the team gitflow and its diagram; `SETUP.md` gains an extensions step and the M0
project gains a `.vscode/extensions.json`; M0's existing command table gains a third
column. The governing spec is amended only in the integration task, because it is a
contended file.

**Tech Stack:** MkDocs + Material, Pillow (already in the Poetry environment), pytest,
`tools/site_guard.py`.

**Spec:** [`superpowers/specs/2026-08-19-m0-vscode-and-git-design.md`](../../specs/2026-08-19-m0-vscode-and-git-design.md)
— the plan argues from the spec; executors read both.

---

## Global constraints — delta only

`CLAUDE.md` at the repository root loads automatically and already carries the toolchain,
pinned versions, Spotless wiring, package naming, the fade curve, authoring rules, the
clean-clone invariant, and verification discipline. **Do not restate any of it.** What is
specific to this plan:

- **No Gradle project is modified.** Nothing here touches a `build.gradle`, a source set,
  or a given test. If you find yourself running `./gradlew` for any reason other than a
  sanity check, stop — you are outside the plan.
- **`tests.lock` is not regenerated.** `projects/m0-onboarding/.vscode/extensions.json` is
  not under any `src/test/` and must not appear in the manifest. `tests_lock.py verify`
  must stay at rc=0 throughout, untouched.
- **`docs/` is `docs_dir`.** Every file created under it publishes to the site. That is
  intended for the two new files here and is the reason this plan and its spec live under
  `superpowers/`, which `mkdocs` cannot read.
- **M0 stays Red tier.** The extension list is git tooling only. The existing DoD item
  *"Copilot or equivalent is disabled in your editor"* is not weakened, reworded, or moved.
- **Extension marketplace IDs**, exact: `eamodio.gitlens`, `mhutchie.git-graph`,
  `ms-vsliveshare.vsliveshare`.
- **`docs-links.yml` runs lychee over `./**/*.md` with `fail: true`.** Every link this plan
  adds is a live CI dependency. Relative links must resolve on disk; the one external link
  added (the Notion source) must return a status in `200,206,403,429`.

---

## File structure

| File | Responsibility | Task |
|---|---|---|
| `docs/programming/team-gitflow.md` | *How does the team work, and why?* — gitflow, and when it becomes yours | 1 |
| `docs/images/gitflow.png` | The branch diagram, legible on both colour schemes | 1 |
| `docs/programming/git-workflow.md` | *What do I type this week?* — gains the M7 forward pointer only | 1 |
| `mkdocs.yml` | Nav registration for the new page | 1 |
| `tools/site_guard.py` | `REQUIRED_PAGES` + new `REQUIRED_ASSETS` | 1 |
| `tests/test_site_guard.py` | Coverage for both | 1 |
| `docs/SETUP.md` | *Is my machine ready?* — gains step 1b, extensions | 2 |
| `projects/m0-onboarding/.vscode/extensions.json` | The workspace recommendation prompt | 2 |
| `projects/m0-onboarding/.gitignore` | Scope `.vscode` so only `extensions.json` is tracked | 2 |
| `docs/programming/milestones/m0-tooling-and-git.md` | *What does git mean?* — GUI column, menu hazard, DoD | 3 |
| `docs/programming/teaching-repo-spec.md` | §5.7 and §9.1 — **contended, integration only** | 9 |

The four-way split of git documentation is the design's §3 and becomes governing as spec
§5.7 in task 9. Each document owns exactly one question and never contains the others'
content. If a task tempts you to add a `git push` example to a document whose column says
it never contains one, that is the drift the table exists to stop.

---

## Task graph

| Task | File | Depends on | Touches | Lane |
|---|---|---|---|---|
| 1 | `01-gitflow-page.md` | — | `docs/programming/team-gitflow.md`, `docs/images/`, `docs/programming/git-workflow.md`, `mkdocs.yml`, `tools/site_guard.py`, `tests/test_site_guard.py` | `gitflow` |
| 2 | `02-vscode-setup.md` | 1 | `docs/SETUP.md`, `projects/m0-onboarding/.vscode/`, `projects/m0-onboarding/.gitignore` | `setup` |
| 3 | `03-m0-command-table.md` | 1 | `docs/programming/milestones/m0-tooling-and-git.md` | `m0doc` |
| 9 | `99-integration.md` | 1, 2, 3 | `docs/programming/teaching-repo-spec.md`, full gate sweep | `integration` |

**Lanes:** `gitflow` (task 1), then `setup` (task 2) and `m0doc` (task 3) concurrently.
**Maximum useful concurrency: 2**, and only after task 1 has landed.
**Serialized tail:** task 9, single owner, after both lanes land.

### Why tasks 2 and 3 wait on task 1

Their *edits* are disjoint — different files, no shared state. Their **verification is not.**
Both add a relative link to `docs/programming/team-gitflow.md`, and CI builds the site with
`mkdocs build --strict` (`pages.yml`), which turns MkDocs' unresolved-link warning into a
build failure. A lane that ships a link to a file task 1 has not created yet cannot run its
own gate; it can only run a weaker one and hope.

That is a dependency, so the graph states it rather than leaving two task files full of
"expected to fail, ignore this" instructions — which is how a real failure gets waved
through.

### Be honest about whether to fan out at all

Even after task 1, two agents is *safe* rather than obviously *worth it*. This is roughly 500
lines of prose across seven files with no compilation, no Gradle, and no suite to wait on —
the coordination cost of two worktrees plausibly exceeds the wall-clock saved. **Running
1 → 2 → 3 → 9 sequentially in one lane is a legitimate choice and needs no plan change.**

What is *not* optional: task 9 runs last, alone, on the merged result.

### Cross-task content coupling

Task 3's new DoD item names the extensions task 2 installs, and both tasks link to task 1's
page. Every one of those strings is written out in full in the task that needs it, so no task
has to read a sibling — the dependency is on task 1's *files existing*, not on its prose.

---

## Constraints on the two things no command can verify

The spec names two checks that a green build does not prove. They belong to the tasks that
own them, not to integration:

1. **The diagram in dark mode** — task 1. Flattening the PNG is the entire point of that
   task; a `mkdocs build` that succeeds proves the file copied, not that anyone can read it.
2. **The GUI column against the real WPILib 2026 VS Code** — task 3. The menu paths in this
   plan are written from knowledge of VS Code, **not from clicking them.** Task 3 clicks
   every one and ships what it finds. Where this plan and the running application disagree,
   **the application wins and the plan was wrong.**

Report actual command output for every verification step. A task report that says "tests
pass" without the run is not a completed task.

---

## Self-review

Run after the plan is written, against the spec, before execution.

**Spec coverage.** Every spec section maps to a task:

| Spec § | Task |
|---|---|
| 2.1 literacy not practice | 1 (page body), 9 (§5.7) |
| 2.2 its own page | 1 |
| 2.3 the "no need to fork" callout | 1 |
| 2.4 PNG flattened onto white | 1 |
| 2.5 `docs/images/` not `docs/assets/` | 1 |
| 2.6 hotfix discrepancy noted in prose | 1 |
| 2.7 `feature/bad_idea` called out | 1 |
| 2.8 three extensions | 2 |
| 2.9 Live Share consent guidance | 2 |
| 2.10 `extensions.json` in M0 only, `.gitignore` scoping | 2 |
| 2.11 two-column table, menu hazard restated | 3 |
| 3 documentation ownership table | 9 (as spec §5.7) |
| 4 spec amendments | 9 |
| 5 DoD additions | 3 |
| 6 nav + `REQUIRED_PAGES` registration | 1 |
| 7 verification | 1, 3 (theirs), 9 (sweep) |

No gaps.

**Placeholder scan.** No `TBD`, no "add appropriate error handling", no "similar to Task N".
Every file's full content is inline in the task that creates it, including the two files
that appear in more than one task's prose.

**Type consistency.** `missing_assets(site_root, required)` is defined in task 1 step 4 and
called in task 1 step 6 with that exact signature; `REQUIRED_ASSETS` is the module-level
name in both. The page slug is `programming/team-gitflow` in `mkdocs.yml`, in
`REQUIRED_PAGES`, and in the test — identical in all three.
