# Task 3: Org issue templates and team PR template

Source of truth lives here in `templates/github/`; a PR copies it into `IgniteRobotics/.github`,
where GitHub serves it as the org-wide default for every repo with no templates of its own.
Risky: issue forms are YAML that GitHub validates only when rendered — a typo shows students
a blank chooser — and `ignition-labs` must keep its own PR template.

**Files:**
- Create: `templates/github/ISSUE_TEMPLATE/feature.yml`, `bug.yml`, `hotfix.yml`, `config.yml`; `templates/github/PULL_REQUEST_TEMPLATE.md`
- Test: `tests/test_team_repo_kit.py` (create the file; tasks 4–6 add to it)
**Consumes:** names table (labels).
**Produces:** files at those paths; the open PR URL in `IgniteRobotics/.github` (task 99 lists it).
**Lane:** `kit`

- [ ] **Step 1: Write failing tests** in `tests/test_team_repo_kit.py`:
      - `test_issue_forms_have_required_keys_and_label` — no YAML dependency (stdlib only, like every other tool here): line-based assertions that each file has top-level `name:`, `description:`, `body:`, and `labels: ["feature"]` / `["bug"]` / `["hotfix"]` respectively. Real YAML validity is checked by GitHub's renderer in step 4.
      - `test_feature_form_requires_problem_statement` — feature.yml has a textarea with `id: problem` and `required: true` (the Ready rule, issues design §3.3).
      - `test_bug_form_asks_for_log_or_replay` and `test_hotfix_form_records_event_and_branch` (`id: event`, `id: branch`, both required).
      - `test_pr_template_sections` — contains `Closes #`, an OpenSpec change path line with the alternative "no spec — reason", the three AI tiers matching `.github/PULL_REQUEST_TEMPLATE.md` in this repo **verbatim**, and "I can explain every line".
- [ ] **Step 2: Run — fail.** `poetry run pytest tests/test_team_repo_kit.py -v`.
- [ ] **Step 3: Write the files.** `config.yml`: `blank_issues_enabled: false`. PR template order: Closes → What changed → Spec → AI disclosure → I can explain this.
- [ ] **Step 4: Verify rendering for real.** Clone `IgniteRobotics/.github` into the scratchpad, branch `feature/issue-and-pr-templates`, copy `templates/github/*` to its `.github/` (paths: `.github/ISSUE_TEMPLATE/…`, `.github/PULL_REQUEST_TEMPLATE.md`), push, open a PR (body ends with the attribution line from the session). Then on the PR's branch view `https://github.com/IgniteRobotics/.github/blob/<branch>/.github/ISSUE_TEMPLATE/feature.yml` — GitHub shows a form preview or a validation error. Read all three; paste any error. Do not merge.
- [ ] **Step 5: Run — pass**, then `poetry run pytest`.
- [ ] **Step 6: Commit** — `git add templates/github tests/test_team_repo_kit.py && git commit -m "feat(templates): org issue forms and team PR template"`. Record the PR URL in the report.
