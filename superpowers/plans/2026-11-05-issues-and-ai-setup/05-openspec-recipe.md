# Task 5: OpenSpec recipe, regen script, drift-check workflow

One recipe file decides what OpenSpec generates; one script regenerates from it with no user
config leaking in; one workflow fails a PR when committed tool files differ from that output
(setup design §8.3, §8.7). Risky: OpenSpec's `profile`/`delivery` are global-only, so the
script must isolate `HOME`/XDG; and the drift check is worthless unless its four distractor
runs behave.

**Files:**
- Create: `templates/team-repo/.ignite/openspec-recipe.json`, `templates/team-repo/.ignite/openspec-regen.mjs`, `templates/team-repo/.ignite/openspec-regen.test.mjs`, `templates/team-repo/.github/workflows/openspec-drift.yml`
- Modify: `tests/test_team_repo_kit.py` (`test_hook_suite_passes_under_node` already runs `node --test` over `.ignite/` — widen its path to the whole `.ignite/` dir)
**Consumes:** task 4's `node --test` wiring.
**Produces:**
- Recipe: `{"openspecVersion": "1.10.0", "profile": "custom", "delivery": "both", "workflows": ["propose","explore","apply","update","sync","archive"], "tools": ["claude","codex","github-copilot"], "copilotCloud": false}`.
- `node .ignite/openspec-regen.mjs [--check]` — regenerates tool files in the repo root. `--check`: after regenerating, runs `git status --porcelain` limited to the tool paths (`.claude/skills`, `.claude/commands`, `.agents/skills`, `.github/skills`, `.github/prompts/opsx-*`, `openspec/config.yaml`) and exits 1 printing the drift message below if anything changed; exit 0 otherwise.
- Workflow job `name: OpenSpec tool files` on `pull_request` to `main`, `development`.
**Lane:** `kit`

- [ ] **Step 1: Confirm the CLI's behaviour (scratch only).** In the scratchpad, export `Robot-2026@origin/development` (`git archive`), then with `HOME` and `XDG_CONFIG_HOME` pointed at an empty temp dir and `OPENSPEC_TELEMETRY=0`: find where `openspec config path` puts config; write the recipe's `profile`/`delivery`/`workflows` there; run `npx -y @fission-ai/openspec@1.10.0 init --tools claude,codex,github-copilot --no-copilot-cloud --no-animation .`; commit; **run it a second time and confirm `git status --porcelain` is empty** (init must be idempotent, or `--check` can never pass). If it isn't, try `update --force` as the second-run command and record which one is idempotent. Confirm the npm package name. Paste outputs.
- [ ] **Step 2: Write failing tests** (`openspec-regen.test.mjs`, `node:test`): `regen: builds an isolated env` (env has temp `HOME`/`XDG_CONFIG_HOME`/`APPDATA`, `OPENSPEC_TELEMETRY=0`, nothing from `process.env.HOME`); `regen: command uses pinned version and recipe tools` (pure function `buildCommand(recipe) -> string[]` — `npx -y @fission-ai/openspec@1.10.0 …` with `--tools claude,codex,github-copilot --no-copilot-cloud --no-animation`); `regen: drift message names files and both audiences`.
- [ ] **Step 3: Run — fail.** `node --test templates/team-repo/.ignite/`.
- [ ] **Step 4: Implement** `openspec-regen.mjs` and the workflow (checkout, `actions/setup-node` with Node 20, `node .ignite/openspec-regen.mjs --check`). Drift message, verbatim:
      ```
      OpenSpec tool files changed:
        <one path per line>
      Students: revert these files with `git checkout -- <paths>`. Never run `openspec init` or `openspec update` in a team repo.
      Mentors: regenerate with `node .ignite/openspec-regen.mjs` (recipe: .ignite/openspec-recipe.json) and commit the result.
      ```
- [ ] **Step 5: Run — pass**; `poetry run pytest` green.
- [ ] **Step 6: Distractor runs on the scratch export** (setup design §8.7), copying the kit's `.ignite/` in first and regenerating once so the baseline is the recipe's output:
      (a) unmodified → `--check` exit 0;
      (b) `openspec config profile core` + `delivery both` in a *second* temp HOME, run `openspec update`, then `--check` → exit 1, message lists the rewritten files;
      (c) hand-edit one `.claude/skills/*/SKILL.md` → exit 1 naming it;
      (d) create a change, `openspec archive` it (touches `openspec/specs/` and `changes/archive/` only) → exit 0.
      Paste all four outputs. Any wrong exit = stop and fix.
- [ ] **Step 7: Commit** — `git commit -m "feat(kit): OpenSpec recipe, regen script, drift check"`.
