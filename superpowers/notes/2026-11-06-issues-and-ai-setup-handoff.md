# Hand-off: Issues & Projects + SDD/AI setup guide

For Josh. Branch `feature/issues-and-ai-setup` (integration commit `c8be1e2` on `lane/integration`; final-review fixes `08448cd` and `860e2d4` on `lane/final-fix`). Nothing below has been run against GitHub except read-only calls and the five PRs being opened. Nobody has run `--apply`, and nobody has merged anything.

The order matters. Each step says why.

## 0. Before anything: things that are not in the brief's list

- **Ship the site first, or with step 1.** The seeded `AGENTS.md` in all four repos links `https://igniterobotics.github.io/ignition-labs/programming/issues-and-projects/` (plus coding standards and the AI use standard). The issues page and the setup guide exist only on this feature branch. Until it merges to `main` and Pages deploys, those links 404 for every agent and student who follows them.
- **Expect ignition-labs' `docs-links` job to be red on this branch's PR.** `templates/team-repo/AGENTS.md` links `https://igniterobotics.github.io/ignition-labs/programming/issues-and-projects/`, which 404s until this branch merges and Pages deploys. That is the chicken-and-egg above, not a regression. It goes green on the first run after the deploy.
- **Before publishing the setup guide to students, run its §5 check 4 live, once per tool** (ruling R24). Nobody has watched the hooks fire inside a real agent session; the node suite only proves the scripts. On a seeded repo (any of the four PR branches), with the hooks turned on as in §4, do check 4 in **Claude Code**, **Codex**, and **Copilot CLI**: marker on, ask the agent to edit a file under `src/test/` → blocked with `Blocked: editing src/test/…`; marker off → the edit goes through. Then, still with the marker on, have the agent `cd src/main/java` in its shell and ask again: the guard now finds the repo root from its own location, but the hook *command* is the relative `node .ignite/hooks/guard.mjs`, so a tool that runs hooks from the agent's current directory would not find the script at all (Claude Code: the call is allowed with a hook error; Copilot CLI: every call denied). If that happens, the guide's snippets need an absolute path (`$CLAUDE_PROJECT_DIR` in Claude Code). **Copilot CLI specifically:** it reads the matcher `Edit|Write|MultiEdit|Bash` from `.claude/settings.local.json`, but its own tool names are `edit`, `create`, and `bash`. If the hook does not fire in Copilot CLI, change the matcher in the guide's §4 snippet (and the comment at the top of `guard.mjs`) to `Edit|Write|MultiEdit|Bash|edit|create|bash|powershell`, re-run check 4 in both Claude Code and Copilot CLI, and re-copy `guard.mjs` (below). `guard.mjs` already accepts the lowercase names; only the matcher decides whether it runs. If it still does not fire, the guide must say Copilot CLI has no hooks, like Copilot in VS Code, with CI and review as the backstop.
- **Any kit change must be re-copied into all four seeding PRs before they merge.** The kit lives in `templates/team-repo/`, and each PR carries a byte-identical copy (`python3 tools/templates_sync.py verify` covers only this repo). The final-review fixes were re-copied on 2026-11-06: Robot-2026#113 `7441164`, Phoenix-2026#13 `40b20f0`, flashpoint#47 `a50e5ce`, IgniteLib#20 `8183a45`. Any later change to `.ignite/`, `AGENTS.md`, `CLAUDE.md`, `.github/CODEOWNERS`, or `openspec-drift.yml` needs the same: copy, commit, `node .ignite/openspec-regen.mjs --check` on the committed tree, push, green checks.
- **IgniteLib: merge `main` → `development` first.** `main` is 1 commit ahead of `development` (checked 2026-11-06: `compare/development...main` ahead_by 1). Do it before the default-branch switch, so the new default is not behind the old one. Phoenix `development` is 44 ahead of `main`. That direction is fine.

## 1. Merge the five PRs: org templates and the four seeding PRs first

| # | PR | Base | Checks |
|---|---|---|---|
| 1 | https://github.com/IgniteRobotics/.github/pull/1 (issue forms + PR template) | `main` | none (repo has no CI) |
| 2 | https://github.com/IgniteRobotics/Robot-2026/pull/113 | `development` | Build and Check Code Format ✅, OpenSpec tool files ✅, Gemini ✅ |
| 3 | https://github.com/IgniteRobotics/Phoenix-2026/pull/13 | `development` | Build and Check Code Format ✅, OpenSpec tool files ✅, Gemini ✅ |
| 4 | https://github.com/IgniteRobotics/flashpoint/pull/47 | `development` | ci-ok ✅, OpenSpec tool files ✅ (docs job skipped) |
| 5 | https://github.com/IgniteRobotics/IgniteLib/pull/20 | `development` | Build and Check Code Format ✅, OpenSpec tool files ✅, Gemini ✅ |

All five were open, unmerged, and green when checked on 2026-11-06. `.github#1` goes first: the seeded PR flow assumes the org templates exist. The four seeding PRs have no order among themselves.

Read these while reviewing them:

- **IgniteLib#20 removes four `.gitignore` lines** that ignored `openspec/`, `.claude/`, `.github/skills/`, and `.github/prompts/opsx-*`. The kit has to be committed, so those lines had to go. Make sure nobody relied on them.
- **flashpoint#47 rewrites the Branching paragraph** in `openspec/config.yaml` (ruling R20). It said "`main` is the only long-lived branch", and it now describes the development-default, issue-driven flow. `docs/rewrite/06-roadmap.md:294` in flashpoint still says main-only. If flashpoint's maintainer wants main-only, revert that one paragraph and take flashpoint out of `tools/team_github.toml`.
- **flashpoint's tests live in `tests/`, not `src/test/`** (ruling R19). The agent guard freezes `src/test/` only, so in flashpoint an agent can edit tests during implementation, and CI plus review are the backstop. flashpoint's `AGENTS.md` now says so in its repo-specific region. Making the test paths configurable is a kit follow-up.
- **IgniteLib#20 updates `.github/rulesets/dev-main-pr-gate.json`** to what `--apply` sets (`require_code_owner_review: true`, `OpenSpec tool files` required), so the committed reference matches the live ruleset. `dev-main-pushes.json` was left alone: it already matches except that it names the bypass team as `team:code-reviewers` where the tool uses its id, 9240266, which is the same team.
- **Robot-2026 and Phoenix-2026 `config.yaml`** carry one extra sentence that flashpoint and IgniteLib don't (ruling R18): don't run propose for students below M8. `AGENTS.md` is identical in all four.

## 2. `gh auth refresh -s project`, read the dry-run, then `--apply`

```bash
gh auth refresh -s project          # your token already shows 'project'; harmless to repeat
poetry run python -m tools.team_github          # dry-run. exit 1 = changes planned, 0 = converged, 2 = GitHub/tool error
poetry run python -m tools.team_github --apply
```

Exit 2 is usually GitHub returning 5xx on check-runs, which happened repeatedly during this work. Wait a minute and re-run.

Read these lines in the dry-run before applying:

- **Phoenix-2026 `dev-main-pushes` converges to IgniteLib's target** (ruling R6). Its extra `pull_request` rule is dropped (the new `dev-main-pr-gate` carries the PR requirement instead). Bypass goes from `OrganizationAdmin (always), Team 9530473 (always)` to `OrganizationAdmin (exempt), Team 9240266 (always)`. Team 9530473 is `commiters` (y0shi, Orcasphynx, bunny2027, TheGreatPintoJ) and 9240266 is `code-reviewers` (y0shi, Orcasphynx). **bunny2027 and TheGreatPintoJ lose ruleset bypass on Phoenix `main`/`development`.**

  What that means in practice: Phoenix's `dev-main-pushes` keeps its `update` rule ("restrict updates"), and a pull request merge is an update to the base branch. So after `--apply`, **only the bypass actors (`OrganizationAdmin` and `code-reviewers`, i.e. org admins, y0shi and Orcasphynx) can merge PRs into Phoenix `development` or `main`.** `commiters` members bunny2027 and TheGreatPintoJ, who can merge today through the team bypass, lose the ability to merge. Phoenix's `students` team has `maintain` on the repo (checked 2026-11-06; `mentors` also `maintain`, `commiters` `push`), but a repo role is not a bypass actor, so `maintain` does not let them merge either. This matches the other three repos. **Josh decides before `--apply`:** either accept it (leads merge), or add `commiters` (team 9530473) as a bypass actor in `tools/team_github/rulesets.py` for Phoenix first, because the tool will otherwise make the change.
- **`dev-main-pr-gate` bypass is `OrganizationAdmin` only** on all four, as IgniteLib has today (ruling R7).
- **IgniteLib** gets `require_code_owner_review: False → True`. Only PRs that touch CODEOWNERS paths (the OpenSpec tool files, owned by `@IgniteRobotics/mentors`) need a mentor's approval. The approval count for everything else stays 0.
- **Required checks:** `Build and Check Code Format` is required now on Robot-2026, IgniteLib, and Phoenix-2026. Phoenix already has `pr-build-check.yml` on `development` (ruling R13), and the design record's "Phoenix has no CI" was stale. `ci-ok` is required on flashpoint. flashpoint's classic protection on `main` is left in place and reported. The ruleset covers it, so delete it by hand when you're ready.
- **Release field** is seeded with `gwinnett`, `albany` (ruling R15). A single-select field needs at least one option. Delete or rename them in the UI as needed.
- **Migration:** 23 items from Project #3 (Flashpoint) + 16 from #5 (IgniteLib) = 39. 8 items from the archived `flashpoint-private` are skipped and listed.

## 3. After the seeding PRs have run CI on `development`, `--apply` again

`OpenSpec tool files` is PENDING in all four repos in the run below, because the seeding PRs aren't merged yet. The tool counts a check as reported only when two things hold: it has run (on `development`'s HEAD or on one of the last 20 PRs into `development`), and a workflow on `development` defines it (rulings R5, R12). Once a seeding PR merges, both hold. So if you merged them all before step 2, step 2's dry-run may already plan `require check "OpenSpec tool files"`, and this step is a confirmation run. Either way, finish with a dry-run that exits 0 **and has no PENDING section**. A pending check is left out of the plan, so the tool can exit 0 while `OpenSpec tool files` is still waiting to be required. Only a run with both is converged, and that run is the drift check from now on.

`node .ignite/openspec-regen.mjs --check` only means something on a committed tree. It compares against what's checked in, by design. Run it in CI or on a clean checkout, not on a working tree with uncommitted regen output.

### Right after the first `--apply`: two checks

- [ ] **Add flashpoint's open issues that are not on Project #3.** #37, #38, #39, #43, #44, and #45 are open in flashpoint but on no Project (checked 2026-11-06), so the migration does not carry them, and auto-add (step 4) only adds issues opened or changed after it is switched on. It does not backfill. Once **Ignite Programming** exists:

    ```bash
    N=$(gh project list --owner IgniteRobotics --format json --jq '.projects[] | select(.title=="Ignite Programming") | .number')
    for i in 37 38 39 43 44 45; do
      gh project item-add "$N" --owner IgniteRobotics --url "https://github.com/IgniteRobotics/flashpoint/issues/$i"
    done
    ```

    Or add them by hand from the Project's "+ Add item" row. They land with no Status; set them to Backlog.
- [ ] **Prove the code-owner gate works.** In one repo, open a throwaway PR into `development` that touches only `AGENTS.md` (one whitespace change). Confirm the merge box says it is blocked waiting for a review from a code owner (`@IgniteRobotics/mentors`), even though the ruleset requires 0 approvals. Then close the PR without merging and delete the branch. If it is mergeable without a mentor, `require_code_owner_review` or CODEOWNERS is not doing its job; stop and look before going on.

## 4. Turn on the Project automations by hand

Task 2's verdict was **auto-add: builtin**. GitHub has no API that enables a built-in Project workflow (the only workflow mutation is `deleteProjectV2Workflow`). The org plan is `team`, and the steps are on [Issues & Projects → Project automations (by hand)](https://igniterobotics.github.io/ignition-labs/programming/issues-and-projects/#project-automations-by-hand):

- [ ] Auto-add to project: one rule per managed repo (4). **The Team plan allows 5 auto-add workflows, and GitHub doesn't say whether that's per Project or per org.** Old Projects #3 and #5 hold one rule each, so if it counts per org, 2 + 4 = 6 > 5. Delete the old Projects' auto-add rules (or close them, step 5) before adding the fourth rule.
- [ ] Item closed → Done
- [ ] Pull request merged → Done
- [ ] Pull request linked to issue → In review. This exists: the live API lists it on Projects #2–#5, but GitHub's docs page doesn't.
- [ ] Auto-archive items (optional)

Then open a test issue and watch it land in Backlog.

## 5. Look at the migrated board, then `--close-old`

```bash
poetry run python -m tools.team_github --apply --close-old
```

This closes old Projects #3 and #5 without deleting them. **When you close them, also delete their auto-add rules.** A closed Project's workflow may still count toward the plan limit, and it would keep adding new issues to a dead board.

## 6. Point the Notion programming pages at the site, by hand

`team-gitflow.md` is now the source of the team's git standard (spec §5.7, §1.3). The Notion gitflow page and the other programming pages should link to `https://igniterobotics.github.io/ignition-labs/programming/team-gitflow/` and `…/issues-and-projects/` rather than carry their own copy.

## Also for a mentor

- **Confirm the OpenAI age sentence** in the setup guide (`docs/programming/ai/setup.md`, the "Age terms" row: "13 or older; under 18 needs a parent or guardian's permission"). The OpenAI terms page returned 403 when task 9 checked it, so it isn't verified at the source.
- "Codex included in every ChatGPT plan, including Free" is secondary-sourced too (task 9 minor). Verify it or soften it.

## Final dry-run (step 7), 2026-11-06, `poetry run python -m tools.team_github`, first attempt

```
== Robot-2026
  default branch main → development
  create label feature
    colour #0e8a16
  create label hotfix
    colour #b60205
  create label spec
    colour #5319e7
  create ruleset dev-main-pr-gate
    does not exist; will be created
  PENDING (not applied yet)
    require check "OpenSpec tool files"
      Not in dev-main-pr-gate yet: no check run named "OpenSpec tool files" on the latest commit of development or its last 20 PRs.
      Requiring a check nothing reports blocks every PR in this repo. Re-run once its workflow has run on development.

== IgniteLib
  default branch main → development
  create label feature
    colour #0e8a16
  create label hotfix
    colour #b60205
  create label spec
    colour #5319e7
  update ruleset dev-main-pr-gate
    pull_request.require_code_owner_review: False → True
  PENDING (not applied yet)
    require check "OpenSpec tool files"
      Not in dev-main-pr-gate yet: no check run named "OpenSpec tool files" on the latest commit of development or its last 20 PRs.
      Requiring a check nothing reports blocks every PR in this repo. Re-run once its workflow has run on development.

== flashpoint
  default branch main → development
  create label feature
    colour #0e8a16
  create label hotfix
    colour #b60205
  create label spec
    colour #5319e7
  create ruleset dev-main-pr-gate
    does not exist; will be created
  PENDING (not applied yet)
    require check "OpenSpec tool files"
      Not in dev-main-pr-gate yet: no check run named "OpenSpec tool files" on the latest commit of development or its last 20 PRs.
      Requiring a check nothing reports blocks every PR in this repo. Re-run once its workflow has run on development.
  REPORT ONLY (this tool never changes these)
    classic branch protection on main (left in place)
      Requires checks: ci-ok.
      The desired dev-main-pr-gate covers all of them.
      This tool never removes it; delete it by hand once the ruleset covers it.

== Phoenix-2026
  default branch main → development
  create label feature
    colour #0e8a16
  create label hotfix
    colour #b60205
  create label spec
    colour #5319e7
  update ruleset dev-main-pushes
    rule pull_request: present → removed
    bypass_actors: OrganizationAdmin (always), Team 9530473 (always) → OrganizationAdmin (exempt), Team 9240266 (always)
  create ruleset dev-main-pr-gate
    does not exist; will be created
  PENDING (not applied yet)
    require check "OpenSpec tool files"
      Not in dev-main-pr-gate yet: no check run named "OpenSpec tool files" on the latest commit of development or its last 20 PRs.
      Requiring a check nothing reports blocks every PR in this repo. Re-run once its workflow has run on development.

== project
  create org Project 'Ignite Programming'
  set Status options to Backlog, Ready, In progress, In review, Done
  create single-select field Size (XS, S, M, L, XL)
  create single-select field Release (gwinnett, albany)
  link repo Robot-2026
  link repo IgniteLib
  link repo flashpoint
  link repo Phoenix-2026

== migrate
  add 23 item(s) from Project #3 (Flashpoint) (project does not exist yet; items resolved at apply)
    https://github.com/IgniteRobotics/flashpoint/issues/3  Backlog  size=-
    https://github.com/IgniteRobotics/flashpoint/issues/4  Backlog  size=-
    https://github.com/IgniteRobotics/flashpoint/issues/5  Backlog  size=-
    https://github.com/IgniteRobotics/flashpoint/issues/6  Backlog  size=-
    https://github.com/IgniteRobotics/flashpoint/issues/7  Backlog  size=-
    https://github.com/IgniteRobotics/flashpoint/issues/8  Backlog  size=-
    https://github.com/IgniteRobotics/flashpoint/issues/9  Backlog  size=-
    https://github.com/IgniteRobotics/flashpoint/issues/10  Backlog  size=-
    https://github.com/IgniteRobotics/flashpoint/issues/11  In progress  size=-
    https://github.com/IgniteRobotics/flashpoint/issues/12  Backlog  size=-
    https://github.com/IgniteRobotics/flashpoint/issues/13  Backlog  size=-
    https://github.com/IgniteRobotics/flashpoint/issues/14  Done  size=-
    https://github.com/IgniteRobotics/flashpoint/issues/15  Done  size=-
    https://github.com/IgniteRobotics/flashpoint/issues/16  In progress  size=S
    https://github.com/IgniteRobotics/flashpoint/issues/17  In progress  size=S
    https://github.com/IgniteRobotics/flashpoint/issues/18  Backlog  size=M
    https://github.com/IgniteRobotics/flashpoint/issues/19  Backlog  size=M
    https://github.com/IgniteRobotics/flashpoint/issues/20  In progress  size=L
    https://github.com/IgniteRobotics/flashpoint/issues/21  Backlog  size=L
    https://github.com/IgniteRobotics/flashpoint/issues/22  Backlog  size=M
    https://github.com/IgniteRobotics/flashpoint/issues/23  Backlog  size=M
    https://github.com/IgniteRobotics/flashpoint/issues/24  In progress  size=L
    https://github.com/IgniteRobotics/flashpoint/issues/25  Backlog  size=S
  add 16 item(s) from Project #5 (IgniteLib) (project does not exist yet; items resolved at apply)
    https://github.com/IgniteRobotics/IgniteLib/issues/4  Backlog  size=-
    https://github.com/IgniteRobotics/IgniteLib/issues/5  Backlog  size=-
    https://github.com/IgniteRobotics/IgniteLib/issues/6  Backlog  size=-
    https://github.com/IgniteRobotics/IgniteLib/issues/7  Backlog  size=-
    https://github.com/IgniteRobotics/IgniteLib/issues/8  Backlog  size=-
    https://github.com/IgniteRobotics/IgniteLib/issues/9  Backlog  size=-
    https://github.com/IgniteRobotics/IgniteLib/issues/10  Backlog  size=-
    https://github.com/IgniteRobotics/IgniteLib/issues/11  Backlog  size=-
    https://github.com/IgniteRobotics/IgniteLib/issues/12  Backlog  size=-
    https://github.com/IgniteRobotics/IgniteLib/issues/13  Backlog  size=-
    https://github.com/IgniteRobotics/IgniteLib/issues/14  Backlog  size=-
    https://github.com/IgniteRobotics/IgniteLib/issues/15  Backlog  size=-
    https://github.com/IgniteRobotics/IgniteLib/issues/16  Backlog  size=-
    https://github.com/IgniteRobotics/IgniteLib/issues/17  Backlog  size=-
    https://github.com/IgniteRobotics/IgniteLib/issues/18  Backlog  size=-
    https://github.com/IgniteRobotics/IgniteLib/issues/19  Backlog  size=-
  REPORT ONLY (this tool never changes these)
    skipped 8 item(s) from archived flashpoint-private
      https://github.com/IgniteRobotics/flashpoint-private/issues/54
      https://github.com/IgniteRobotics/flashpoint-private/issues/67
      https://github.com/IgniteRobotics/flashpoint-private/issues/68
      https://github.com/IgniteRobotics/flashpoint-private/issues/69
      https://github.com/IgniteRobotics/flashpoint-private/issues/71
      https://github.com/IgniteRobotics/flashpoint-private/issues/72
      https://github.com/IgniteRobotics/flashpoint-private/issues/73
      https://github.com/IgniteRobotics/flashpoint-private/issues/74

== templates
  REPORT ONLY (this tool never changes these)
    missing in IgniteRobotics/.github: .github/ISSUE_TEMPLATE/bug.yml
    missing in IgniteRobotics/.github: .github/ISSUE_TEMPLATE/config.yml
    missing in IgniteRobotics/.github: .github/ISSUE_TEMPLATE/feature.yml
    missing in IgniteRobotics/.github: .github/ISSUE_TEMPLATE/hotfix.yml
    missing in IgniteRobotics/.github: .github/PULL_REQUEST_TEMPLATE.md

31 change(s) would be made. Re-run with --apply to make them.
rc=1
```
