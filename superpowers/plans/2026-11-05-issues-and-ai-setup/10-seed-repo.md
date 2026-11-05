# Task 10: Seed one managed repo (run once per repo — four lanes)

Applies the kit to **one** of `Robot-2026`, `Phoenix-2026`, `flashpoint`, `IgniteLib` and opens a
PR into its `development`. Four agents may run this concurrently, one repo each. Risky: the
`.gitignore` trap (setup design §8.3(1) — `Robot-2026` ignores `.claude/` and git silently drops
the Claude files), and clobbering a repo's existing `openspec/config.yaml` context.

**Files (in the target repo):** `AGENTS.md`, `CLAUDE.md`, `.gitignore`, `.github/CODEOWNERS`, `.ignite/**`, `.github/workflows/openspec-drift.yml`, OpenSpec output (`openspec/**`, `.claude/{skills,commands}/**`, `.agents/skills/**`, `.github/{skills,prompts}/**`). `Phoenix-2026` only: `.github/workflows/pr-build-check.yml`. Only if task 2 recorded `auto-add: action-needed`: `.github/workflows/add-to-project.yml`.
**Consumes:** the kit (`templates/team-repo/`) from tasks 4–6 at the feature-branch HEAD; task 2's verdict.
**Produces:** an open PR URL per repo; its first run of `OpenSpec tool files` (and Phoenix's build check) on the PR.
**Lane:** `seed-<repo>` — never two agents on one repo.

- [ ] **Step 1: Fresh clone** into the scratchpad: `gh repo clone IgniteRobotics/<repo> <scratch>/<repo> -- -b development`; `git switch -c feature/ignite-agent-kit`. **Never** use `~/workspace/ignite/<repo>`.
- [ ] **Step 2: Inventory before touching.** Paste: `cat .gitignore`; `ls -a .claude .agents .github openspec 2>&1`; `cat openspec/config.yaml` if present; `ls .github/CODEOWNERS AGENTS.md CLAUDE.md 2>&1`. If the repo already has `AGENTS.md`, `CLAUDE.md`, or CODEOWNERS, **merge** — keep existing content, add the kit's — and say so in the PR body.
- [ ] **Step 3: `.gitignore`.** Remove any bare `.claude/` (or `.claude`) line; append `gitignore.fragment`. Verify: `git check-ignore -v .claude/skills/x/SKILL.md` → **not ignored**; `git check-ignore -v .claude/settings.local.json .ignite/implementing` → ignored.
- [ ] **Step 4: Copy the kit** (`.ignite/`, `AGENTS.md`, `CLAUDE.md`, `.github/CODEOWNERS`, `.github/workflows/openspec-drift.yml`). Fill `AGENTS.md`'s repo-specific region from the repo: what it is (one line), build/test commands (read `build.gradle` / `pyproject.toml` / README — `flashpoint` is Python). Keep it under 4 KiB.
- [ ] **Step 5: `openspec/config.yaml`.** If present, keep `context:`, but reword its "THE STUDENT WRITES THE SPEC…" paragraph to the `AGENTS.md` spec rule (task 6 sentence). If absent (`IgniteLib`, `Phoenix-2026`), the regen in step 6 creates it — then add a `context:` with the repo line + the same rule.
- [ ] **Step 6: Regenerate.** `node .ignite/openspec-regen.mjs`, then `node .ignite/openspec-regen.mjs --check` → exit 0. `git status --short` — paste it; confirm `.claude/skills/*` files **appear** (the §8.3(1) trap).
- [ ] **Step 7: Phoenix-2026 only.** Copy `Robot-2026`'s `.github/workflows/pr-build-check.yml` unchanged. Run `./gradlew build` locally (`JAVA_HOME=~/wpilib/2026/jdk`) and paste the tail. If it fails, keep the workflow, note "build is red on development — not fixed here" in the PR body, and tell task 99 so the ruleset ships without that check (issues design §3.8).
- [ ] **Step 8: Auto-add (conditional).** Only if task 2 said `action-needed`: `add-to-project.yml` using `actions/add-to-project` (pinned to a tag), project URL from task 2, token `${{ secrets.ADD_TO_PROJECT_PAT }}`. Note in the PR body that Josh must create that org secret.
- [ ] **Step 9: Hook smoke test in the seeded tree.** `mkdir -p .ignite && touch .ignite/implementing`; `node .ignite/hooks/guard.mjs < .ignite/hooks/fixtures/claude-edit.json` → exit 2 (fixture path under `src/test/`); `rm .ignite/implementing` → exit 0. Paste both.
- [ ] **Step 10: Commit, push, open the PR** into `development`. Title: `Ignite agent kit: AGENTS.md, OpenSpec for all three tools, drift check`. Body: what was added and why (link the setup guide path on the feature branch), anything merged rather than added, Phoenix build status if relevant, the secret note if relevant, and the attribution line. Paste the PR URL and the first `OpenSpec tool files` result (`gh pr checks <url> --watch` until it reports). **It must be green.** Do not merge.
