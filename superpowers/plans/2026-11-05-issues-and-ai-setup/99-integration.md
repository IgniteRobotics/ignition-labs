# Task 99: Integration — contended files, the sweep, and the hand-off

Runs **last, alone, on the merged feature branch.** Owns the files every lane would otherwise
fight over, runs every gate against everything at once, and produces what Josh needs to decide
on `--apply`.

**Files:** Modify `mkdocs.yml`, `tools/site_guard.py`, `tests/test_site_guard.py`, `docs/programming/teaching-repo-spec.md`; resolve any `<!-- verdict -->` marker left by task 7.
**Consumes:** every task, merged; the five external PR URLs (task 3 + four from task 10).
**Produces:** the shipped branch and a report.
**Lane:** `integration` — single owner.

- [ ] **Step 1: Lanes landed.** `git log --oneline main..HEAD`; `git status --short` clean. Tasks 1–9 present; five PR URLs in hand. If anything is missing, stop.
- [ ] **Step 2: Nav.** `mkdocs.yml`: `Issues & Projects: programming/issues-and-projects.md` immediately after `Team Gitflow`; `Setup: programming/ai/setup.md` as the first entry under `Working with AI`.
- [ ] **Step 3: Site guard.** Add `"programming/issues-and-projects"` and `"programming/ai/setup"` to `REQUIRED_PAGES`; add `test_issues_page_is_required` and `test_setup_guide_is_required` in the style of the existing team-gitflow test. Run them red first (before the list edit), then green.
- [ ] **Step 4: `teaching-repo-spec.md`.** (a) §1.3: dated revision entry (2026-11-05) in the existing format — Issues & Projects for team repos, `development` default, archive moves inside the PR (student runs it, mentor verifies), setup guide replaces "see the project README", team repos seeded for three tools; close with preservation pointers to both design records. (b) §9.8/§9.9 "Given", "Real repo", and "Mentor verifies … runs `openspec archive` after the pull request merges" → archive-in-PR wording identical to task 8's. (c) **§13 untouched** — no milestone is built.
- [ ] **Step 5: Reconcile cross-lane facts.** If task 7 left `<!-- verdict -->`, replace it with task 2's recorded automation verdict. If task 9 reported a Node floor other than 20.19, update M7's prerequisite line (page and nothing else — it is not in the DoD).
- [ ] **Step 6: Full gates.** `poetry run pytest`; `poetry run mkdocs build --strict` (zero warnings now); `poetry run python tools/site_guard.py _site`; `poetry run python tools/tests_lock.py verify` (rc 0, unchanged); `python3 tools/templates_sync.py verify`; `node --test templates/team-repo/.ignite/`. Paste every result.
- [ ] **Step 7: Final dry-run.** `poetry run python -m tools.team_github` — paste the full output. Expected: default-branch switches and rulesets planned for four repos; `OpenSpec tool files` still PENDING (seeding PRs not merged); Phoenix build check PENDING; project creation + 39-item migration planned; templates "missing" (task 3 PR not merged). Exit 1.
- [ ] **Step 8: Whole-branch review** — `superpowers:requesting-code-review` on `main..HEAD`, then fix what it finds.
- [ ] **Step 9: Hand-off report to Josh**, in this order: (1) the five PRs to merge and the order — `.github` templates and the four seeding PRs first; (2) then `gh auth refresh -s project`, read the dry-run, run `--apply`; (3) after the seeding PRs have run CI on `development`, run `--apply` again so the pending checks become required; (4) click the manual Project automations (from task 2's verdict); (5) look at the migrated board, then `--close-old`; (6) point the Notion programming pages at the site by hand. Include the dry-run output from step 7.
