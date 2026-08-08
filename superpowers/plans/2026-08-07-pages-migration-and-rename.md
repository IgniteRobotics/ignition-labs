# MkDocs Migration & Rename to ignition-labs — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the `doc-builder` dependency with MkDocs Material built directly from a `docs/` subdirectory, delete the staging workaround that only existed to accommodate `doc-builder`, and rename every reference from `IgniteRobotics/docs` to `IgniteRobotics/ignition-labs`.

**Architecture:** Prose moves into `docs/`, which becomes MkDocs' `docs_dir`. Because MkDocs can read nothing outside `docs_dir`, `projects/` and `superpowers/` become structurally unpublishable rather than excluded-by-rule — this is the point of the change, not a side effect. An explicit `nav:` in `mkdocs.yml` replaces `00-index.md`. `tools/site_guard.py` survives as a post-build check; `tools/stage_site.py` is deleted.

**Tech Stack:** MkDocs 1.6.1 + Material for MkDocs 9.7.7 (both pinned exactly), Python 3.11+ / Poetry, GitHub Actions, GitHub Pages.

## Global Constraints

Copied verbatim from `superpowers/specs/2026-08-07-pages-migration-and-rename-design.md`.

- **Canonical repository name is `IgniteRobotics/ignition-labs`.** All seven workflow gate strings must match it exactly. A mismatched gate does not fail — it silently skips the job forever.
- **`student-check.yml` must NEVER carry a repository gate.** It is the one workflow that must run on student forks.
- **Never `pull_request_target`.**
- **Nothing under `projects/` is ever published.** Enforced structurally by `docs_dir`, checked by `tools/site_guard.py`.
- **`mkdocs` and `mkdocs-material` are pinned to exact versions** — `mkdocs==1.6.1`, `mkdocs-material==9.7.7`. Material's maintainers warn MkDocs 2.0 removes the plugin system with no migration path.
- **Use `git mv`, never delete-and-add.** File history must survive the move.
- **`superpowers/plans/` is a historical record and is never rewritten.** The old repository name legitimately survives there.
- **No student-facing path may reference pre-2026 Ignite code** — not `2025-Robotics-Intro`, `Robot-2025`, `Robot-2024`, or `Robot-2023`.
- **`tests.lock` failure messages must invite disagreement, not accuse.**

## File Structure

| Path | Responsibility | Change |
|---|---|---|
| `mkdocs.yml` | Site config: `docs_dir`, `site_dir`, theme, explicit `nav` | Rewritten (Task 2) |
| `docs/index.md` | Site landing page | Created (Task 1) |
| `README.md` | GitHub repository landing page | Created (Task 1) |
| `docs/**` | All published prose | Moved (Task 1) |
| `tools/site_guard.py` | Post-build check: no forbidden paths, all required pages rendered | Unchanged — `REQUIRED_PAGES` survives the move verbatim |
| `tools/stage_site.py` | *Workaround for doc-builder* | **Deleted** (Task 3) |
| `tests/test_stage_site.py` | Defended that workaround | **Deleted** (Task 3) |
| `00-index.md` | doc-builder's nav source | **Deleted** (Task 3) |
| `.github/workflows/pages.yml` | Build and deploy the site | Rewritten (Task 3) |
| `tests/test_process_docs.py` | Asserts prose invariants | Paths updated (Task 1), rename assertion updated (Task 4) |
| `pyproject.toml` | Tooling deps and pins | Modified (Task 2, Task 4) |

## Interface Summary

Only one module's public surface matters across tasks, and it does not change:

| Module | Function | Signature | Status |
|---|---|---|---|
| `tools/site_guard.py` | `forbidden_files` | `(site_root: Path) -> list[str]` | unchanged |
| | `missing_pages` | `(site_root: Path, required: list[str]) -> list[str]` | unchanged |
| | `REQUIRED_PAGES` | `list[str]` | **unchanged** — verified: MkDocs strips the `docs_dir` prefix, so `docs/programming/README.md` still publishes to slug `programming` |

---

## Task 1: Move prose into `docs/`

The restructure. Nothing is deleted and nothing is renamed yet — this task only relocates files and repairs the tests that reference their paths.

**Files:**
- Move: `programming/` → `docs/programming/`
- Move: `VISION_GUIDE.md`, `vision-subsystem.md`, `field-boundary-speed-limiting.md`, `SETUP.md`, `CONTRIBUTING.md` → `docs/`
- Create: `docs/index.md`, `README.md`
- Modify: `tests/test_process_docs.py:12-13`, `:50`, `:81-82`

**Interfaces:**
- Consumes: nothing
- Produces: the `docs/` tree that Task 2's `mkdocs.yml` sets as `docs_dir`

- [ ] **Step 1: Move the prose with history intact**

```bash
mkdir -p docs
git mv programming docs/programming
git mv VISION_GUIDE.md docs/VISION_GUIDE.md
git mv vision-subsystem.md docs/vision-subsystem.md
git mv field-boundary-speed-limiting.md docs/field-boundary-speed-limiting.md
git mv SETUP.md docs/SETUP.md
git mv CONTRIBUTING.md docs/CONTRIBUTING.md
```

`00-index.md` stays at the root for now — Task 3 deletes it. Moving it here would break `tests/test_stage_site.py`, which still reads it.

- [ ] **Step 2: Verify history survived the move**

```bash
git log --follow --oneline docs/programming/git-workflow.md | tail -3
```

Expected: commits from before the move, including `Add git-workflow.md: the clone/fork/branch ritual per milestone`. If you see only one commit, `git mv` was not used — undo and redo the move.

- [ ] **Step 3: Run the tests to see them fail**

```bash
eval "$(pyenv init -)" && pyenv activate ignite-docs
poetry run pytest tests/test_process_docs.py -q
```

Expected: FAIL — 28 errors, `FileNotFoundError` on `programming/git-workflow.md`. The tests point at the old paths.

- [ ] **Step 4: Repair the test path constants**

In `tests/test_process_docs.py`, change these four lines. `REPO` at line 12 stays as it is — it resolves to the repository root, which is still correct.

```python
GIT_WORKFLOW = REPO / "docs" / "programming" / "git-workflow.md"
```

```python
LADDER = REPO / "docs" / "programming" / "robot-repo-ladder.md"
```

```python
SETUP = REPO / "docs" / "SETUP.md"
CONTRIBUTING = REPO / "docs" / "CONTRIBUTING.md"
```

- [ ] **Step 5: Run the full suite**

```bash
poetry run pytest -q
```

Expected: PASS, 61 tests. `tests/test_stage_site.py` still passes because `00-index.md` still exists.

- [ ] **Step 6: Create the site landing page**

Create `docs/index.md`:

```markdown
# Ignition Labs

FRC 6829 Ignite Robotics — programming curriculum and team documentation.

---

## Start here

**New to the programming subteam?** Go to the [Programming Curriculum](programming/README.md)
and answer the first question. It routes you to one of four tracks depending on
what you already know, and every track converges on the same nine milestones.

**Setting up your laptop?** [Setup](SETUP.md) walks the toolchain install, and
tells you about the one step everybody forgets.

**About to open a pull request?** [Contributing](CONTRIBUTING.md) has the rules,
and [Git Workflow](programming/git-workflow.md) has the exact commands.

---

## What is in here

| Section | What it covers |
|---|---|
| [Programming Curriculum](programming/README.md) | Nine milestones from "never written code" to owning a subsystem through a competition season |
| [Git Workflow](programming/git-workflow.md) | What to do with git at every milestone. Clone what you're reading. Fork what you'll change. Branch to work |
| [Real-Repo Ladder](programming/robot-repo-ladder.md) | When you stop working in a practice project and start contributing to the competition robot |
| [AI Use Standard](programming/ai-use-standard.md) | What AI tooling you may use, and when. Your tier comes from the milestones you have finished |
| [Vision Guide](VISION_GUIDE.md) | AprilTags, pose estimation, and the vision subsystem |

Mentors should start with the [Curriculum Spec](programming/curriculum-spec.md) —
it records every design decision and the reasoning behind it, including the ones
students push back on.
```

- [ ] **Step 7: Create the repository landing page**

Create `README.md` at the repository root. It is what GitHub shows, and it stays short — the site is the real front door.

```markdown
# Ignition Labs

FRC 6829 Ignite Robotics — programming curriculum, team documentation, and the
skeleton projects students work through.

**📖 Read the documentation site.** Everything here is written to be read there,
with navigation and search. This README exists so GitHub has something to show.

## What is in this repository

| Path | What it is |
|---|---|
| `docs/` | All prose. Published to GitHub Pages |
| `docs/programming/` | The nine-milestone programming curriculum |
| `projects/` | Skeleton Gradle projects, one per milestone. Never published |
| `tools/` | Verification scripts the CI workflows call |
| `tests/` | Tests for those scripts |

## Working here

Read [`docs/CONTRIBUTING.md`](docs/CONTRIBUTING.md) before opening a pull
request, and [`docs/SETUP.md`](docs/SETUP.md) before writing any code.

The short version of the git ritual: **clone what you're reading, fork what
you'll change, branch to work.**
```

- [ ] **Step 8: Confirm the suite is still green and commit**

```bash
poetry run pytest -q
git add -A
git commit -m "Move prose into docs/ for MkDocs docs_dir

Relocates all published prose under docs/ so it can serve as MkDocs'
docs_dir. programming/ moves wholesale, so relative links inside the
curriculum are unaffected. Adds a site landing page and a repository
README, which serve different readers.

Uses git mv throughout; history verified with git log --follow."
```

---

## Task 2: `mkdocs.yml` and pinned dependencies

Builds the site from the new layout. Nothing is deleted yet — after this task both pipelines exist side by side, which is deliberate: it lets the new one be proven before the old one is removed.

**Files:**
- Modify: `mkdocs.yml` (full rewrite)
- Modify: `pyproject.toml` (add a `docs` dependency group)

**Interfaces:**
- Consumes: the `docs/` tree from Task 1
- Produces: `_site/`, which `tools/site_guard.py` checks and Task 3's workflow uploads

- [ ] **Step 1: Add the pinned docs dependency group**

In `pyproject.toml`, add this block after the existing `[tool.poetry.group.dev.dependencies]` section:

```toml
[tool.poetry.group.docs.dependencies]
mkdocs = "1.6.1"
mkdocs-material = "9.7.7"
```

Exact versions with no caret. Material for MkDocs prints a warning on every build that MkDocs 2.0 removes the plugin system, rewrites theming, and provides no migration path. A floating pin will break this site between seasons.

- [ ] **Step 2: Install and confirm the versions**

```bash
eval "$(pyenv init -)" && pyenv activate ignite-docs
poetry install --only docs
poetry run mkdocs --version
```

Expected: `mkdocs, version 1.6.1`.

- [ ] **Step 3: Write `mkdocs.yml`**

Replace the entire contents of `mkdocs.yml`:

```yaml
site_name: Ignition Labs
site_description: FRC 6829 Ignite Robotics — programming curriculum and team documentation
docs_dir: docs
site_dir: _site

theme:
  name: material
  features:
    - navigation.sections
    - navigation.top
    - search.highlight
    - content.code.copy
  palette:
    - media: "(prefers-color-scheme: light)"
      scheme: default
      toggle:
        icon: material/brightness-7
        name: Switch to dark mode
    - media: "(prefers-color-scheme: dark)"
      scheme: slate
      toggle:
        icon: material/brightness-4
        name: Switch to light mode

markdown_extensions:
  - admonition
  - attr_list
  - tables
  - toc:
      permalink: true
  - pymdownx.superfences
  - pymdownx.details

nav:
  - Home: index.md
  - Setup: SETUP.md
  - Contributing: CONTRIBUTING.md
  - Programming Curriculum:
      - Overview: programming/README.md
      - Git Workflow: programming/git-workflow.md
      - Real-Repo Ladder: programming/robot-repo-ladder.md
      - AI Use Standard: programming/ai-use-standard.md
      - Student Deliverables: programming/student-deliverables.md
      - Resources: programming/resources.md
  - Tracks:
      - Track A — New to Programming: programming/tracks/track-a-new-to-programming.md
      - Track B — Programmed, Not Object-Oriented: programming/tracks/track-b-non-oo-programmer.md
      - Track C — OO Experience, No FRC: programming/tracks/track-c-oo-no-frc.md
      - Track D — FRC Veteran, Levelling Up With AI: programming/tracks/track-d-frc-veteran-ai.md
  - Milestones:
      - M0 — Tooling, Git & the Build: programming/milestones/m0-tooling-and-git.md
      - M1 — Java Fundamentals: programming/milestones/m1-java-fundamentals.md
      - M2 — OOP, Interfaces & Lambdas: programming/milestones/m2-oop-interfaces-lambdas.md
      - M3 — WPILib Command-Based: programming/milestones/m3-wpilib-command-based.md
      - M4 — Sensors, Actuators & Control: programming/milestones/m4-sensors-actuators-control.md
      - M5 — Autonomous, Odometry & Vision: programming/milestones/m5-autonomous-odometry-vision.md
      - M6 — Simulation, Telemetry & Testing: programming/milestones/m6-simulation-telemetry-testing.md
      - M7 — AI-Assisted Development: programming/milestones/m7-ai-assisted-development.md
      - M8 — In-Season Deliverables: programming/milestones/m8-in-season-deliverables.md
  - Vision:
      - Vision Guide: VISION_GUIDE.md
      - Vision Subsystem: vision-subsystem.md
      - Field Boundary Speed Limiting: field-boundary-speed-limiting.md
  - For Mentors:
      - Curriculum Spec: programming/curriculum-spec.md
      - Teaching Repository Spec: programming/teaching-repo-spec.md
```

`strict: true` is deliberately absent. Strict mode turns warnings into errors, and one warning is intentional: `programming/robot-repo-ladder.md` links `UNTESTED-TARGETS.md`, which does not exist until the M6 plan lands.

- [ ] **Step 4: Build the site**

```bash
rm -rf _site
poetry run mkdocs build
```

Expected: `INFO - Documentation built in ...`. Warnings about `UNTESTED-TARGETS.md` are expected. Any warning about a file in `nav:` that does not exist is NOT expected — fix the path rather than removing the nav entry.

- [ ] **Step 5: Run the site guard**

```bash
poetry run python tools/site_guard.py ./_site
```

Expected: `site guard OK: _site`.

If it reports `MISSING PAGE`, a nav path and a file path disagree — fix the file path, not `REQUIRED_PAGES`. Those slugs were verified against a real build and are correct.

- [ ] **Step 6: Prove the boundary holds**

This is the property the whole change exists for. Create a probe, build, and confirm nothing leaks:

```bash
mkdir -p projects/m-probe
echo 'class Probe {}' > projects/m-probe/Probe.java
echo '# leak check' > projects/m-probe/README.md
rm -rf _site && poetry run mkdocs build >/dev/null 2>&1
find _site \( -path '*projects*' -o -path '*superpowers*' -o -name '*.java' \) | wc -l
```

Expected: `0`. Then clean up and confirm the tree is clean:

```bash
rm -rf projects/m-probe
git status --short
```

Expected: no `projects/` entries.

- [ ] **Step 7: Commit**

```bash
poetry run pytest -q
git add mkdocs.yml pyproject.toml poetry.lock
git commit -m "Build the site with MkDocs Material directly

Replaces the doc-builder wrapper with MkDocs Material and an explicit
nav. docs_dir: docs means the builder cannot read projects/ or
superpowers/ at all -- publication scope becomes a directory boundary
rather than a rule that can be forgotten.

Pins mkdocs and mkdocs-material exactly: Material's maintainers warn
MkDocs 2.0 removes the plugin system with no migration path."
```

---

## Task 3: Delete the doc-builder scaffolding and rewrite the Pages workflow

**Files:**
- Delete: `tools/stage_site.py`, `tests/test_stage_site.py`, `00-index.md`
- Modify: `.github/workflows/pages.yml` (full rewrite of the `build` job)
- Modify: `tests/test_site_guard.py` (comments referencing doc-builder)

**Interfaces:**
- Consumes: `mkdocs.yml` and the `docs` dependency group from Task 2
- Produces: a Pages workflow that later tasks only rename

- [ ] **Step 1: Delete the workaround**

```bash
git rm tools/stage_site.py tests/test_stage_site.py 00-index.md
```

All three exist only because `doc-builder` rendered every markdown file regardless of index linkage. MkDocs with an explicit `docs_dir` has no such behaviour.

- [ ] **Step 2: Confirm the suite is green minus the deleted tests**

```bash
eval "$(pyenv init -)" && pyenv activate ignite-docs
poetry run pytest -q
```

Expected: PASS, **54 tests** (61 minus the 7 in `test_stage_site.py`).

- [ ] **Step 3: Update the stale doc-builder comments**

`tests/test_site_guard.py` has three comments explaining the guard in terms of a tool the repository no longer uses. Replace each comment body exactly as below. **Do not touch any assertion or fixture** — only the comment lines.

At `test_markdown_file_under_projects_is_forbidden` (~line 36), replace the four comment lines with:

```python
    # MkDocs cannot read outside docs_dir, so projects/ is normally
    # unpublishable by construction. This guards the remaining case:
    # someone creating docs/projects/. Any extension, not just the FRC
    # source-file suffixes.
```

At `test_markdown_derived_page_under_projects_is_forbidden` (~line 45), replace the three comment lines with:

```python
    # The realistic shape of a leak: a stray README.md inside docs_dir
    # becomes an .html page in the built site
    # (docs/projects/m2-oop/README.md -> projects/m2-oop/index.html).
```

At `test_superpowers_path_in_site_is_forbidden` (~line 53), replace the four comment lines with:

```python
    # superpowers/ holds design records, plans, and session notes, and is
    # never published. It sits outside docs_dir, so this is belt-and-braces
    # against a future layout change that pulls it back inside.
```

- [ ] **Step 4: Rewrite the Pages workflow build job**

Replace `.github/workflows/pages.yml` entirely:

```yaml
name: Publish Pages

on:
  push:
    branches: [main]
    paths:
      - 'docs/**'
      - 'mkdocs.yml'
      - 'tools/site_guard.py'
      - 'pyproject.toml'
      - '.github/workflows/pages.yml'
  workflow_dispatch:

permissions:
  contents: read
  pages: write
  id-token: write

concurrency:
  group: pages
  cancel-in-progress: false

jobs:
  build:
    if: github.repository == 'IgniteRobotics/docs'
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-python@v5
        with:
          python-version: '3.11'

      - name: Install Poetry
        run: pip install poetry==2.4.1

      - name: Install pinned docs dependencies
        run: poetry install --only docs

      - name: Build site
        run: poetry run mkdocs build

      - name: Guard against published source files
        run: python tools/site_guard.py ./_site

      - uses: actions/upload-pages-artifact@v3
        with:
          path: ./_site

  deploy:
    needs: build
    if: github.repository == 'IgniteRobotics/docs'
    runs-on: ubuntu-latest
    environment:
      name: github-pages
      url: ${{ steps.deployment.outputs.page_url }}
    steps:
      - id: deployment
        uses: actions/deploy-pages@v4
```

The gate strings still say `IgniteRobotics/docs` — Task 4 renames every occurrence in one sweep, so that nothing is half-renamed at any point.

- [ ] **Step 5: Validate the workflow**

```bash
actionlint .github/workflows/pages.yml
```

Expected: exit 0, no output.

- [ ] **Step 6: Confirm doc-builder is gone**

```bash
grep -rn "doc-builder\|doc_builder" --include="*.md" --include="*.yml" --include="*.py" --include="*.toml" . | grep -v "^./superpowers/"
```

Expected: no output. Hits inside `superpowers/` are historical records and are correct.

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "Delete the doc-builder staging workaround

stage_site.py, its tests, and 00-index.md existed only because
doc-builder published every markdown file regardless of index linkage.
MkDocs reads nothing outside docs_dir, so the condition they worked
around no longer exists.

Rewrites pages.yml to build with pinned MkDocs via Poetry."
```

---

## Task 4: Rename to `ignition-labs`

One sweep, so the repository is never half-renamed. The test that asserts the old name is updated **last**, so it fails loudly if any earlier edit was missed.

**Files:**
- Modify: `.github/workflows/pages.yml` (2), `.github/workflows/skeleton-integrity.yml` (4), `.github/workflows/docs-links.yml` (1)
- Modify: `docs/programming/git-workflow.md` (4), `docs/programming/teaching-repo-spec.md` (7)
- Modify: `docs/SETUP.md`, `docs/CONTRIBUTING.md` (any clone paths)
- Modify: `pyproject.toml` (project name)
- Modify: `tests/test_process_docs.py` (1) — **last**

**Interfaces:**
- Consumes: everything above
- Produces: a repository consistently named `IgniteRobotics/ignition-labs`

- [ ] **Step 1: See the full extent of the rename**

```bash
grep -rn "IgniteRobotics/docs\|<your-handle>/docs\|/docs\.git" \
  --include="*.md" --include="*.yml" --include="*.py" --include="*.toml" . \
  | grep -v "^./superpowers/"
```

Read every hit before editing. Note that `git-workflow.md` contains student-facing clone commands using placeholder handles (`<your-handle>/docs`) as well as the canonical name — both forms must change.

- [ ] **Step 2: Rename in the three workflows**

Replace `IgniteRobotics/docs` with `IgniteRobotics/ignition-labs` in all seven gate strings across `pages.yml` (2), `skeleton-integrity.yml` (4 — note one may be in a comment), and `docs-links.yml` (1).

Then verify **both** halves of the constraint:

```bash
grep -rn "github.repository ==" .github/workflows/
grep -c "github.repository" .github/workflows/student-check.yml || echo "student-check ungated: correct"
```

Expected: every gate reads `'IgniteRobotics/ignition-labs'`, and `student-check.yml` has zero.

- [ ] **Step 3: Rename in the student-facing prose**

In `docs/programming/git-workflow.md`, update every occurrence — the fork target, the clone URL, the `git remote add upstream` URL, and the M0 pull-request target. Change `<your-handle>/docs` to `<your-handle>/ignition-labs` as well.

These are commands 14-year-olds transcribe by hand. Read each fenced block after editing and confirm it would run.

Do the same in `docs/SETUP.md` and `docs/CONTRIBUTING.md` for any clone path or repository reference.

- [ ] **Step 4: Rename in the spec**

In `docs/programming/teaching-repo-spec.md`, update all 7 occurrences. Task 5 rewrites the surrounding decisions; this step changes only the name.

- [ ] **Step 5: Rename the tooling project**

In `pyproject.toml`:

```toml
name = "ignition-labs-tools"
description = "Verification tooling for the Ignition Labs teaching repository"
```

- [ ] **Step 6: Prove the sweep is complete before touching the test**

```bash
grep -rn "IgniteRobotics/docs" --include="*.md" --include="*.yml" --include="*.py" --include="*.toml" . \
  | grep -v "^./superpowers/"
```

Expected: exactly one hit — `tests/test_process_docs.py`. If there are more, you missed them; fix them now.

- [ ] **Step 7: Watch the test fail, then fix it**

```bash
eval "$(pyenv init -)" && pyenv activate ignite-docs
poetry run pytest tests/test_process_docs.py::test_m0_pull_request_targets_upstream -q
```

Expected: FAIL. That failure is the safety net working — it proves the assertion was genuinely checking the name.

Now update the assertion:

```python
def test_m0_pull_request_targets_upstream(git_workflow):
    assert "IgniteRobotics/ignition-labs:main" in git_workflow
```

- [ ] **Step 8: Verify and commit**

```bash
poetry run pytest -q
grep -rn "IgniteRobotics/docs" --include="*.md" --include="*.yml" --include="*.py" --include="*.toml" . | grep -v "^./superpowers/"
```

Expected: 54 tests pass; the grep returns nothing.

```bash
git add -A
git commit -m "Rename IgniteRobotics/docs to IgniteRobotics/ignition-labs

Sweeps all 35 references: five workflow gate strings, the student-facing
clone and pull-request commands, the spec, and the tooling project name.

A mismatched gate string does not fail -- it silently skips the job
forever -- so the sweep is verified by grep as well as by the test that
asserts the M0 pull-request target.

superpowers/plans/ keeps the old name: it is a record of what was
executed, not a description of what exists."
```

---

## Task 5: Amend the canonical spec

`docs/programming/teaching-repo-spec.md` still describes a repository built with `doc-builder`, with prose at the root, called `docs`. Three claims that are now false.

**Files:**
- Modify: `docs/programming/teaching-repo-spec.md` — D1, D7, §4.1, §7.1, §7.2, §11.2, §16, §18

**Interfaces:**
- Consumes: the completed migration
- Produces: a spec that describes the repository that exists

- [ ] **Step 1: Update the decisions table**

**D1** — change the layout description: skeletons live at `projects/`, a sibling of `docs/` rather than of `programming/`. The reasoning is unchanged; only the path moves.

**D7** — replace wholesale. It currently reads that the site is built with `doc-builder` and that publication scope is enforced by `stage_site.py`. Rewrite it to state: the site is built with MkDocs Material directly from an explicit `nav:`; prose lives in `docs/`, which is `docs_dir`; MkDocs can read nothing outside `docs_dir`, so `projects/` and `superpowers/` are not excluded from publication but invisible to the builder; `tools/site_guard.py` verifies the result. Keep it to the same length as the other decision rows.

Add two rows:

**D10** — prose lives in `docs/`. Forced, not chosen: MkDocs rejects `docs_dir: .` with *"The 'docs_dir' should not be the parent directory of the config file."* It is also what makes D7's boundary property work.

**D11** — the repository is `IgniteRobotics/ignition-labs`.

- [ ] **Step 2: Update the layout tree in §4.1**

Replace the tree with the actual current layout: `docs/` containing `index.md`, `SETUP.md`, `CONTRIBUTING.md`, the three vision documents, and `programming/`; with `projects/`, `templates/`, `contributors/`, `tools/`, `tests/`, `superpowers/`, and `.github/` as siblings. Mark `docs/` as `docs_dir` and note that everything outside it is unpublishable.

- [ ] **Step 3: Update §7.1 and §7.2**

§7.1 currently describes `doc-builder` and the staging step. Rewrite it for MkDocs Material with pinned versions, `docs_dir: docs`, and explicit `nav:`. State that the boundary is structural.

§7.2's workflow table row for `pages.yml` currently reads `` `doc-builder` → deploy Pages ``. Change it to `mkdocs build` → guard → deploy, and update its `paths:` column to match the workflow written in Task 3.

- [ ] **Step 4: Update §11.2, §16, and §18**

§11.2 — the solutions cross-check job checks out `IgniteRobotics/docs`. Update the name.

§16 — add a maintenance item: **bump and re-pin `mkdocs` and `mkdocs-material`**, with the note that Material's maintainers warn MkDocs 2.0 removes the plugin system with no migration path, so the bump needs a real build check rather than a version bump alone.

§18 — the verified-facts table has a `doc-builder` row. Replace it with the three facts measured for this migration, dated 2026-08-07: MkDocs rejects `docs_dir: .`; `docs_dir: docs` yields zero files from `projects/` in the built site; `docs/programming/README.md` publishes to the slug `programming`.

- [ ] **Step 5: Verify the spec still renders and links resolve**

```bash
eval "$(pyenv init -)" && pyenv activate ignite-docs
rm -rf _site && poetry run mkdocs build >/dev/null 2>&1
ls _site/programming/teaching-repo-spec/index.html
poetry run python tools/site_guard.py ./_site
```

Expected: the file exists and the guard passes.

- [ ] **Step 6: Commit**

```bash
poetry run pytest -q
git add docs/programming/teaching-repo-spec.md
git commit -m "Amend spec for the MkDocs migration and rename

D7 rewritten: MkDocs Material directly, publication scope enforced by
the docs_dir boundary rather than a staging script. D10 records that
the docs/ layout is forced by MkDocs rejecting docs_dir: '.'. D11
records the rename. Updates the layout tree, the CI section, the
verified-facts table, and adds the version-pin bump to seasonal
maintenance."
```

---

## Task 6: Whole-repository verification

Every check in one place, so a reviewer can confirm the migration without reconstructing it.

**Files:** none modified — this task verifies and records.

**Interfaces:**
- Consumes: Tasks 1–5
- Produces: a verified branch and a written handoff for the owner-only steps

- [ ] **Step 1: Run every check**

```bash
eval "$(pyenv init -)" && pyenv activate ignite-docs
poetry run pytest -q
actionlint .github/workflows/*.yml
rm -rf _site && poetry run mkdocs build
poetry run python tools/site_guard.py ./_site
find _site \( -path '*projects*' -o -path '*superpowers*' -o -path '*tools*' -o -path '*tests*' \) | wc -l
rm -rf _site
lychee --no-progress --accept 200,206,403,429 \
  --exclude-path projects --exclude-path superpowers --exclude-path .superpowers './**/*.md'
grep -rn "IgniteRobotics/docs" --include="*.md" --include="*.yml" --include="*.py" --include="*.toml" . | grep -v "^./superpowers/"
grep -rn "doc-builder" --include="*.md" --include="*.yml" --include="*.py" --include="*.toml" . | grep -v "^./superpowers/"
git log --follow --oneline docs/programming/git-workflow.md | wc -l
git status --short
```

Expected, in order: 54 passed · actionlint silent · build succeeds · `site guard OK` · `0` · lychee zero errors · no rename hits · no doc-builder hits · more than 1 commit in the follow log · clean tree.

- [ ] **Step 2: Confirm the two constraints that fail silently**

```bash
grep -rn "github.repository ==" .github/workflows/
grep -rn "pull_request_target" .github/workflows/ || echo "no pull_request_target: correct"
grep -c "github.repository" .github/workflows/student-check.yml || echo "student-check ungated: correct"
```

Expected: all seven gates read `'IgniteRobotics/ignition-labs'`; no `pull_request_target`; `student-check.yml` ungated.

- [ ] **Step 3: Write the owner handoff**

Create `superpowers/notes/2026-08-07-ignition-labs-handoff.md` recording the steps that need a human and cannot be automated:

1. Rename the repository on GitHub to `ignition-labs` — Settings → General → Repository name. Moot until the remote exists; if the remote is created after this migration, simply create it under the new name.
2. Rename the local checkout, **last of everything**, because it invalidates the working directory of anything running inside it:
   ```bash
   mv ~/workspace/ignite/docs ~/workspace/ignite/ignition-labs
   ```
3. Optionally rename the pyenv virtualenv from `ignite-docs` to `ignition-labs` and update `.python-version` to match. Cosmetic; the environment works either way.

Include the current verification output from Step 1 so the next person knows what green looked like.

- [ ] **Step 4: Commit**

```bash
git add superpowers/notes/2026-08-07-ignition-labs-handoff.md
git commit -m "Record owner-only steps for the ignition-labs rename"
```

---

## Done when

- [ ] `poetry run mkdocs build` succeeds and no `doc-builder` reference remains outside `superpowers/`
- [ ] `tools/stage_site.py`, `tests/test_stage_site.py`, and `00-index.md` are deleted
- [ ] `_site` contains every page in `nav:` and **zero** files from `projects/`, `superpowers/`, `tools/`, or `tests/`
- [ ] `tools/site_guard.py` passes with `REQUIRED_PAGES` unedited
- [ ] `grep -rn "IgniteRobotics/docs"` returns hits only inside `superpowers/`
- [ ] All seven workflow gate strings read `IgniteRobotics/ignition-labs`
- [ ] `student-check.yml` carries no repository gate
- [ ] `pytest` is green at 54 tests
- [ ] `lychee` reports zero errors
- [ ] `mkdocs` and `mkdocs-material` are pinned exactly and named in spec §16 maintenance
- [ ] `git log --follow docs/programming/git-workflow.md` shows pre-move history
- [ ] The owner handoff note exists

## Deliberately not in this plan

- **Creating the GitHub remote, renaming it there, enabling Pages** — needs a human with a GitHub account. Recorded in the Task 6 handoff note.
- **Renaming the local checkout directory** — must be last; recorded in the handoff.
- **`superpowers/plans/2026-08-07-teaching-repo-foundation.md`** — a record of what was executed. Rewriting it would falsify it.
- **Skeleton projects, test-out fixtures, `UNTESTED-TARGETS.md`** — later plans, unchanged by this one.

## Risks carried into execution

| Risk | Surfaces at | Response |
|---|---|---|
| A gate string is missed, silently disabling CI forever | Task 4 step 2, Task 6 step 2 | Grep is an explicit verification step, run twice |
| `git mv` not used, history lost | Task 1 step 2 | Explicit `git log --follow` check before proceeding |
| A `nav:` entry points at a moved file with the wrong path | Task 2 step 4 | MkDocs warns on missing nav targets; the step says to fix the path, not delete the entry |
| `REQUIRED_PAGES` slugs wrong after the move | Task 2 step 5 | Verified against a real build before this plan was written; the step says to fix the file path rather than the guard |
| Relative links inside the curriculum break | Task 6 step 1 | `programming/` moves wholesale, so intra-curriculum links are unaffected; lychee is the net |
| MkDocs 2.0 breaks the build in a future season | Not in this plan | Exact pins plus a spec §16 maintenance item added in Task 5 |
