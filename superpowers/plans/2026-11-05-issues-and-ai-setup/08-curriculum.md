# Task 8: M7/M8 — archive inside the PR, changes start from an issue, guide link

Curriculum consequences (issues design §3.4; setup design §3.3). Risky: M7 and M8 Definitions
of done exist **twice** — page and project README — and `tests/test_dod_sync.py` pins them
byte-identical with fixed item counts (M7 = 11, M8 = 8). Change both copies together and the
count in the test.

**Files:**
- Modify: `docs/programming/milestones/m7-ai-assisted-development.md`, `projects/m7-ai-assisted/README.md`, `docs/programming/milestones/m8-spec-anchored-development.md`, `projects/m8-spec-anchored/README.md`, `docs/programming/curriculum-spec.md`, `tests/test_dod_sync.py`
- **Not** `teaching-repo-spec.md` — contended; task 99.
**Consumes:** fixed page names (setup guide, issues page).
**Produces:** curriculum consistent with archive-in-PR; task 99 mirrors the same wording into spec §9.8/§9.9.
**Lane:** `curriculum`

- [ ] **Step 1: Find every site.** `rg -n -i "archive" docs/programming projects/m7-ai-assisted projects/m8-spec-anchored` and `rg -n "project README" docs/programming/milestones/m7-ai-assisted-development.md`. Known: M7 page prerequisites line; `projects/m7-ai-assisted/README.md:79`; M8 page line 8 and step 7; `projects/m8-spec-anchored/README.md:29-30, :51`; `curriculum-spec.md:364`. List all hits in the report.
- [ ] **Step 2: Edit.** New rule everywhere: the student runs `openspec archive <change>` as the **last commit of their PR**, after the mentor has approved spec and code; the mentor checks that commit (tasks checked, `openspec validate` passing, `openspec/specs/` updated) before merging. M8's prerequisite becomes "your M7 PR merged (merging it archived your change)". M7 prerequisite line: Node + OpenSpec now link `../ai/setup.md` instead of "see the project README"; update the Node floor only if task 9's verification says it moved (else leave 20.19). M7 step 2's "you do not run `openspec init`" stays.
- [ ] **Step 3: DoD items.** M7 gains two items, identical in both copies: `- [ ] Your change has an issue, and your PR says \`Closes #<num>\`` and `- [ ] \`openspec archive\` is the last commit of your PR`. M8 gains the issue item only. Update `DOD_PAIRS` counts to 13 and 9.
- [ ] **Step 4: Gates.** `poetry run pytest tests/test_dod_sync.py -v` (pass), then `poetry run pytest`, `poetry run mkdocs build --strict`. `rg -n "after (your|the) (pull request|PR) merges" docs projects` → only hits that are not about archiving.
- [ ] **Step 5: Commit** — `git commit -m "docs(m7,m8): archive inside the PR; changes start from an issue"`.
