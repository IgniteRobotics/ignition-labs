# Task 4: Renumber the shipped M8 to M9, and register both pages

Spec-anchored development takes the M8 slot, so In-Season Deliverables & Ownership becomes M9.
This task performs that rename across the repository and registers both pages in the two
hand-maintained lists.

**This is not a find-and-replace, and treating it as one will corrupt a shipped design
record.** Every `M8` occurrence is one of three kinds and only the first changes. Getting that
wrong is the failure mode here; the rename itself is mechanical.

**Files:** two renames, ~30 references across `docs/`, plus `mkdocs.yml`,
`tools/site_guard.py`, `tools/changed_projects.py`, `tests/test_site_guard.py`,
`tests/test_changed_projects.py`, `.github/workflows/skeleton-integrity.yml`
**Consumes:** task 1's `docs/programming/milestones/m8-spec-anchored-development.md` — you
register it, so it must exist
**Produces:** a site where both pages render and are guarded
**Lane:** `renumber`

- [ ] **Step 1: Classify before you change anything**
      Run the sweep and sort every hit into one of three buckets. **Write the classification
      into your task report** — this is the task's real deliverable.

      ```bash
      grep -rn "M8\|m8" docs/ projects/ tools/ tests/ .github/ mkdocs.yml
      ```

      | Kind | Example | Action |
      |---|---|---|
      | The in-season milestone | `docs/programming/README.md` ladder row; `mkdocs.yml:62` | → **M9** |
      | A range endpoint | `.github/workflows/skeleton-integrity.yml:72` — `M1-M8` | → **M1–M9** |
      | A historical document name | `tools/templates_sync.py:3` — *"the M7/M8 design record"* | **UNCHANGED** |

      That third row points at `superpowers/specs/2026-08-18-m7-m8-design.md`, a shipped record
      whose *name* contains M8. It is not a milestone number. **Anything under `superpowers/`
      is history and is not renumbered by this task**, including references to it from `tools/`.

      Note also that some `M8` hits will now legitimately mean **the new M8** — the milestone
      you are registering. Do not renumber those either.

- [ ] **Step 2: The two renames**
      ```bash
      git mv docs/programming/milestones/m8-in-season-deliverables.md \
             docs/programming/milestones/m9-in-season-deliverables.md
      git mv projects/m8-ownership projects/m9-ownership
      ```
      Use `git mv` so history follows. `projects/m8-ownership/` contains one file.

      **The published URL `programming/milestones/m8-in-season-deliverables/` breaks. That is
      accepted** — no `mkdocs-redirects`, no announcement; the site is v0 and will be
      republished. Do not add a plugin.

- [ ] **Step 3: The prose references**
      Work file by file against your step-1 classification. Weight, measured:
      `teaching-repo-spec.md` (11 — but see below), `programming/README.md` (7 — **not yours**),
      `curriculum-spec.md` (5), `resources.md` (3), the four `tracks/*.md` (1–2 each), and single
      hits in `student-deliverables.md`, `robot-repo-ladder.md`, `git-workflow.md`, and
      `milestones/m4-sensors-actuators-control.md`.

      **`docs/programming/README.md` and `docs/programming/teaching-repo-spec.md` are contended
      and belong to task 9.** Leave both alone entirely — including their M8 references. Report
      the hit counts you found in them so task 9 knows what it is inheriting.

      Also update the M9 page's own internal references, and any milestone page that names M8 as
      "the next milestone" now that a different milestone occupies that slot.

- [ ] **Step 4: Registration — both pages, both lists**
      1. **`mkdocs.yml` `nav:`** — line ~62 currently reads
         `- M8 — In-Season Deliverables: programming/milestones/m8-in-season-deliverables.md`.
         Retarget it to M9, and add the new M8 entry **before** it so the ladder reads in order.
      2. **`tools/site_guard.py` `REQUIRED_PAGES`** — line ~102 carries
         `"programming/milestones/m8-in-season-deliverables"`. Change it to the M9 path and add
         `"programming/milestones/m8-spec-anchored-development"`. Read the module comment above
         the list first: it is hardcoded on purpose (finding C11b), because a nav-derived list
         shrinks in lockstep with a bad nav edit and can never observe the failure it exists to
         catch.
      3. **`tests/test_site_guard.py`** — add an assertion that the new M8 page is in
         `REQUIRED_PAGES`, mirroring the existing `team-gitflow` assertion, and update any
         assertion naming the old M8 path.

      `REQUIRED_ASSETS` is **not** touched — this ships no images.

- [ ] **Step 5: Tooling and CI**
      - `tools/changed_projects.py:24` — a comment naming `projects/m8-ownership/`. Update the
        path. Check whether the module has logic keyed to project directory names, not just the
        comment.
      - `tests/test_changed_projects.py:104,106` — fixture paths using `m8-ownership`. Update.
      - `.github/workflows/skeleton-integrity.yml:72` — `M1-M8` in a comment. This is a **range
        endpoint**: extend to `M1-M9`.
      - `tools/templates_sync.py:3` — **do not touch.** Historical record name.

- [ ] **Step 6: Verify**
      ```bash
      rm -rf _site && poetry run mkdocs build --strict
      poetry run python tools/site_guard.py _site
      poetry run pytest
      poetry run python tools/tests_lock.py verify        # rc=0 AND unchanged
      poetry run python tools/templates_sync.py verify
      ls -l _site/programming/milestones/m8-spec-anchored-development/index.html
      ls -l _site/programming/milestones/m9-in-season-deliverables/index.html
      git status --short
      ```
      Both pages must render. `site_guard` now protects both.

- [ ] **Step 7: The checks that catch a careless rename**
      ```bash
      # no stale paths anywhere outside history
      grep -rn "m8-in-season\|m8-ownership" --exclude-dir=superpowers --exclude-dir=_site . || echo clean

      # the historical record name survived
      grep -n "M7/M8 design record" tools/templates_sync.py
      ```
      The first must be clean. The second **must still match** — if it does not, you renumbered
      a document title and need to put it back.

- [ ] **Step 8: Commit**
      Report the step-1 classification table and the step-7 output verbatim.
