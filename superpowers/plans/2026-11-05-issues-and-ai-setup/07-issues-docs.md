# Task 7: Issues & Projects page; gitflow and the loop become issue-driven

The canonical team-process prose (issues design §3.1–§3.4, §3.7). `team-gitflow.md` stops
deferring to Notion. Risky: `test_process_docs.py` and `test_site_guard.py` pin sentences in
`git-workflow.md` and the `team-gitflow` page — read both tests before editing, and do not touch
`git-workflow.md` (the pre-M7 fork flow is out of scope).

**Files:**
- Create: `docs/programming/issues-and-projects.md`
- Modify: `docs/programming/team-gitflow.md`, `docs/programming/ai/the-loop.md`, `docs/CONTRIBUTING.md`
**Consumes:** names table; task 2's automation verdict (if task 2 hasn't landed, write the manual-checklist section from issues design §3.6 and mark the one sentence that depends on the verdict with `<!-- verdict -->` for task 99 to resolve).
**Produces:** the page at the fixed path; anchors `#status`, `#ready`, `#hotfixes`, `#repo-setup` (task 9 and `AGENTS.md` link to them).
**Lane:** `team-docs`

- [ ] **Step 1: `issues-and-projects.md`.** Sections, from issues design: why (issue = why, spec = what, PR = how); `## Ready` (the problem-statement rule); `## Status` (the five-row table with "where the code is" and "what moves it", In progress by hand); Size and Release fields (Release = tag suffix); branch naming `feature/<num>-<name>`; `Closes #<num>` and why `development` is the default branch; draft PR as spec review; failures after merge open a new linked issue; `## Hotfixes`; `## Repo setup` (how to run `poetry run python -m tools.team_github` dry-run, read it, `--apply`, `--close-old`; `gh auth refresh -s project`; the manual automation checklist). Scope line: managed repos only; students before M7 keep the fork flow.
- [ ] **Step 2: `team-gitflow.md`.** Replace the closing "Source" section (Notion link + "Notion page is what the team does") with one sentence: this page is the team's standard. Soften the top banner: still true for pre-M7 students, no longer the page's main framing. Standard flow: insert "pick up a Ready issue" before branching, `feature/<num>-<name>`, draft PR once the spec is committed, `Closes #<num>`, archive as last commit, merge closes the issue; step 2 says `development` is the default branch. Rules list gains the issue/PR rules. Keep the diagram and every existing rule.
- [ ] **Step 3: `the-loop.md`.** Ladder: add `0b. ISSUE` (or annotate rung 2) — the issue precedes propose; rung 2 notes the draft PR is the spec review; **rung 11 ARCHIVE moves before merge**: "spec archived as the PR's last commit, then merge". Rung 9 replay procedure step 3: "open a new issue linking the old one". Keep the degraded-loop table's rows intact; add "issue may be filed after" to the competition rows.
- [ ] **Step 4: `CONTRIBUTING.md`.** One line in "Pull requests to `Robot-2026`" linking the new page.
- [ ] **Step 5: Gates.** `poetry run pytest` (green); `poetry run mkdocs build --strict` — the only acceptable warning is the new page not being in nav; `rg -n "notion" docs/programming/team-gitflow.md` → no hits.
- [ ] **Step 6: De-slop and read.** Run `audience-style:de-slop` (technical students) on the new page and the changed sections. Read the Status table as a student who has never used Projects: can they tell what to do with their card at each step?
- [ ] **Step 7: Commit** — `git commit -m "docs: issues and projects page; issue-driven gitflow and loop"`.
