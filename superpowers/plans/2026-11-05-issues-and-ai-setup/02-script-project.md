# Task 2: `team_github` — org Project, migration, close-old, org-template drift

Adds the org-level half: create and shape the **Ignite Programming** Project, link the four
repos, migrate items from Projects #3 and #5, close them on request, and report whether
`IgniteRobotics/.github` matches `templates/github/`. Risky: Projects v2 is GraphQL-only for
field options and item field values, and migration must be re-runnable without duplicates.

**Files:**
- Create: `tools/team_github/{project,migrate,org_templates}.py`
- Modify: `tools/team_github/__main__.py` (register `--only project|migrate|templates`, `--close-old`)
- Test: `tests/test_team_github_project.py`, `tests/test_team_github_migrate.py`, new fixtures under `tests/fixtures/team_github/projects/`
**Consumes:** task 1's `Runner`, `FakeRunner`, `json_call`, `Config`, `Change`, CLI exit-code contract.
**Produces:**
- `project.find_project(runner, cfg) -> ProjectRef | None` (by exact title; `ProjectRef(number, node_id)`).
- `project.plan_project(runner, cfg) -> list[Change]` — create if missing; Status options exactly `Backlog, Ready, In progress, In review, Done` (GraphQL `updateProjectV2Field`); `Size` and `Release` single-selects; link each repo (`gh project link`).
- `migrate.STATUS_MAP = {"Needs triage": "Backlog", "Backlog": "Backlog", "Todo": "Backlog", "In progress": "In progress", "Done": "Done"}`.
- `migrate.plan_migration(runner, cfg, target: ProjectRef | None) -> list[Change]` and `migrate.plan_close_old(runner, cfg) -> list[Change]`.
- `org_templates.plan_templates(runner, cfg, source: Path) -> list[Change]` — **report-only** (`apply` is a no-op that prints "open a PR — see task 3"); compares each file under `templates/github/` to the same path in `IgniteRobotics/.github@main`.
- `docs/programming/issues-and-projects.md` consumes this task's **automation verdict** (step 1) — record it in the task report and in a comment at the top of `project.py`.
**Lane:** `script`

- [ ] **Step 1: Verify what can't be scripted (issues design §7.1–7.3)**
      From current GitHub docs, with URLs + access date: (a) can Projects built-in workflows (auto-add, item closed → Done, PR merged → Done, PR linked → status) be enabled through the API? (b) auto-add workflow limit for the org's plan (`gh api orgs/IgniteRobotics --jq .plan.name`); (c) does a built-in "pull request linked to issue → set status" workflow exist? Record verdict: **`auto-add: builtin` if 4 auto-add rules fit, else `auto-add: action-needed`**. Task 10 reads this.

- [ ] **Step 2: Record fixtures (read-only)**
      `gh project list --owner IgniteRobotics --format json`, `gh project field-list 3|5 --format json`, `gh project item-list 3|5 --limit 200 --format json`. Real counts today: #3 has 31 items (8 from archived `flashpoint-private`, all Done); #5 has 16, all `Todo`.

- [ ] **Step 3: Write failing tests**
      - `test_missing_project_plans_create_fields_and_four_links` — no project titled Ignite Programming → Changes: create, status options, Size, Release, 4 links.
      - `test_status_options_exact_order` — the GraphQL input carries the five names in the fixed order.
      - `test_existing_project_with_right_fields_plans_nothing`.
      - `test_migration_maps_statuses_and_carries_size` — `Todo` → `Backlog`; `Needs triage` → `Backlog`; `Size: M` carried; `Release` left empty.
      - `test_migration_skips_flashpoint_private_items` — 8 skipped, each listed by URL in detail.
      - `test_migration_skips_drafts_and_unknown_status_with_warning` — draft item and status `Blocked` → skipped, warning line names each.
      - `test_migration_rerun_adds_nothing` — target already contains the content URL with matching fields → `[]`.
      - `test_pr_items_migrate_like_issues`.
      - `test_close_old_only_with_flag` — CLI without `--close-old` never plans closing #3/#5; with it, plans `gh project close` for each and **never** `delete`; #2 and #4 never touched.
      - `test_templates_drift_reported_not_applied` — differing file → `Change` whose apply performs no `gh` call.

- [ ] **Step 4: Run — expect failures**, `poetry run pytest tests/test_team_github_project.py tests/test_team_github_migrate.py -v`.

- [ ] **Step 5: Implement**
      Identity key for migration is the item's content URL. When the target project does not exist yet (dry-run), migration plans "would add N items" without IDs. Apply order inside `--apply`: project → links → migration; close-old only under its flag and only after migration has run in the same invocation or a previous one (check target contains every migratable URL; refuse otherwise with a message).

- [ ] **Step 6: Run — expect pass**; then `poetry run pytest` (full suite green).

- [ ] **Step 7: Read the full dry-run**
      `poetry run python -m tools.team_github` (all targets, read-only). Paste the full output into the report. Check: project creation planned; 39 migratable items (23 from #3 + 16 from #5), 8 skipped with reasons; templates show "missing in IgniteRobotics/.github" for every file (task 3's PR is not merged yet); exit 1.

- [ ] **Step 8: Commit** — `git commit -m "feat(tools): team_github org project, migration, template drift"`.
