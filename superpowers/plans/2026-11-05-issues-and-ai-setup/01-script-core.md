# Task 1: `team_github` core — runner, config, plan model, per-repo settings

The tool's spine plus everything that changes a single repo: default branch, labels, and the
two owned rulesets. The risky parts are (a) idempotency — a converged repo must plan nothing,
or the tool cannot double as a drift check — and (b) the required-check guard: requiring a
check no workflow reports blocks every PR in that repo, including the one that would fix it.

**Files:**
- Create: `tools/team_github/{__init__,__main__,gh,config,model,repos,rulesets}.py`, `tools/team_github.toml`
- Test: `tests/test_team_github_core.py`, `tests/test_team_github_rulesets.py`, `tests/fixtures/team_github/` (recorded `gh` JSON)
**Consumes:** nothing.
**Produces:**
- `gh.Runner` protocol: `run(args: list[str], *, input: str | None = None) -> str` (stdout; raises `gh.GhError(args, returncode, stderr)`). `gh.GhRunner` (real, `subprocess.run(["gh", *args])`). `gh.FakeRunner(responses: dict[tuple[str, ...], str])` — returns the recorded stdout for an exact args tuple, records every call in `.calls`, raises `KeyError` naming the args on an unrecorded call.
- `gh.json_call(runner, args) -> Any` — `run` + `json.loads`.
- `config.load(path: Path) -> Config`; dataclasses `Config(org, project_title, status_options, size_options, labels, mentor_team, bypass_team_id, repos: list[RepoConfig], old_projects: list[int], templates_repo)`, `RepoConfig(name, default_branch, required_checks: list[str])`.
- `model.Change(scope: str, kind: str, summary: str, detail: list[str], apply: Callable[[], None], pending: bool = False)`.
- `repos.plan_repo(runner, cfg: Config, repo: RepoConfig) -> list[Change]` — default branch + labels + rulesets.
- `rulesets.desired_rulesets(cfg, repo, reported_checks: set[str]) -> dict[str, dict]` and `rulesets.diff_ruleset(current: dict | None, desired: dict) -> list[str]` (human-readable field diffs; `[]` = converged).
- CLI `python -m tools.team_github [--apply] [--close-old] [--only repos|project|migrate|templates]` — task 2 registers the other `--only` targets. Exit codes: **dry-run 0 = nothing to do, 1 = changes would be made; `--apply` 0 = applied, 2 = a gh call failed.**
**Lane:** `script`

- [ ] **Step 1: Record fixtures (read-only)**
      For each of the four repos, save to `tests/fixtures/team_github/<repo>/`: `repo.json` (`gh api repos/IgniteRobotics/<r>`), `labels.json` (`gh api repos/IgniteRobotics/<r>/labels --paginate`), `rulesets.json` (list) and `ruleset-<name>.json` (each detail), `check-runs-development.json` (`gh api repos/IgniteRobotics/<r>/commits/development/check-runs`), and `protection-main.json` (`gh api repos/IgniteRobotics/<r>/branches/main/protection`; record the 404 body where unprotected). Also `IgniteLib/.github/rulesets/*.json` contents. These are today's real state: all default `main`; all have `dev-main-pushes`; only `IgniteLib` has `dev-main-pr-gate` (with `require_code_owner_review: false`, check `Build and Check Code Format`).

- [ ] **Step 2: Write `tools/team_github.toml`**
      Values from the 00-plan names table. Per repo `required_checks`: `Robot-2026` and `IgniteLib` → `["Build and Check Code Format", "OpenSpec tool files"]`; `flashpoint` → `["ci-ok", "OpenSpec tool files"]` (confirm `ci-ok` is a real job name in `flashpoint/.github/workflows/ci.yml`; if it is classic-protection-only, record what reports it); `Phoenix-2026` → `["Build and Check Code Format", "OpenSpec tool files"]`. `old_projects = [3, 5]`. `templates_repo = ".github"`.

- [ ] **Step 3: Write failing tests**
      `test_core`:
      - `test_load_reads_four_repos_with_their_required_checks` — exact names and lists above.
      - `test_fake_runner_raises_naming_unrecorded_args`.
      - `test_default_branch_main_plans_switch_to_development` — `Change(kind="default-branch", summary` contains `main → development`).
      - `test_missing_labels_planned_existing_labels_untouched` — fixture with `bug` present (any colour) → plans only `feature`, `hotfix`, `spec`; never a recolour.
      - `test_converged_repo_plans_nothing` — fixtures rewritten to the desired state → `plan_repo(...) == []`.
      - `test_cli_dry_run_exit_codes` — fake with pending changes → 1; converged → 0; never calls a mutating endpoint (assert no recorded call has `-X` in `PUT|POST|PATCH|DELETE`).
      `test_rulesets`:
      - `test_pr_gate_requires_code_owner_review` — desired `dev-main-pr-gate` has `pull_request.parameters.require_code_owner_review is True` and `required_approving_review_count == 0`.
      - `test_bypass_actors_are_admin_and_code_reviewers` — `[{OrganizationAdmin, exempt}, {Team 9240266, always}]` matching today's `dev-main-pushes`.
      - `test_unreported_required_check_is_pending_not_applied` — `reported_checks={"Build and Check Code Format"}` → desired gate omits `OpenSpec tool files`, and `plan_repo` emits a `Change(kind="required-check", pending=True)` naming it whose `apply` is a no-op.
      - `test_drifted_owned_ruleset_shows_field_diff` — current gate with `require_code_owner_review: false` → `diff_ruleset` returns a line containing `require_code_owner_review: False → True`.
      - `test_classic_protection_reported_not_removed` — `flashpoint` fixture's classic protection on `main` (`ci-ok`) → a report-only `Change(kind="classic-protection")` whose detail says whether the desired ruleset covers it; no DELETE planned.
      - `test_unowned_ruleset_reported_not_touched` — a ruleset named `something-else` → a `Change(kind="unowned-ruleset")` with no-op `apply`, and no DELETE is ever planned.

- [ ] **Step 4: Run — expect failures**
      `poetry run pytest tests/test_team_github_core.py tests/test_team_github_rulesets.py -v` → FAIL (module not found).

- [ ] **Step 5: Implement**
      `reported_checks` = names of check runs on `development` HEAD (`check-runs-development.json`). Ruleset desired state = `IgniteLib`'s two JSON files with the two changes above and per-repo checks; compare only the fields the tool sets (`target`, `enforcement`, `conditions.ref_name.include`, `rules[].type` + `parameters`, `bypass_actors`) so GitHub's normalised extras never register as drift. Apply = `PUT repos/{org}/{r}/rulesets/{id}` for an existing owned ruleset, `POST …/rulesets` for a missing one, `PATCH repos/{org}/{r}` `default_branch=development`, `POST …/labels`. Dry-run prints, per scope, each `Change.summary` then its `detail` lines indented; pending changes print under a `PENDING` heading with the reason.

- [ ] **Step 6: Run — expect pass**
      Same command → all pass. Then `poetry run pytest` → whole suite green.

- [ ] **Step 7: Read the output a human sees**
      `poetry run python -m tools.team_github --only repos` (real, read-only). Paste the full output into the task report. Check by eye: four repos each show the default-branch switch; `Robot-2026`, `Phoenix-2026`, `flashpoint` show a new `dev-main-pr-gate`; `IgniteLib` shows the code-owner diff; `OpenSpec tool files` is PENDING everywhere (no repo has the workflow yet); `Phoenix-2026`'s build check is PENDING. Exit code 1. If any line would confuse Josh reading it cold, fix the wording now.

- [ ] **Step 8: Commit**
      `git add tools/team_github tools/team_github.toml tests/test_team_github_*.py tests/fixtures/team_github && git commit -m "feat(tools): team_github dry-run for repo settings and rulesets"`
