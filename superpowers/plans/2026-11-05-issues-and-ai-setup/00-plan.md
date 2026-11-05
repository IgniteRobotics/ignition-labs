# Issues & Projects + SDD/AI setup guide — implementation plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development`
> (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Steps
> use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Move all team development onto GitHub Issues and one org Project, wired into the
gitflow and the SDD loop; and give post-M6 students one setup guide that gets Copilot, Codex,
or Claude Code working in WPILib VS Code against team repos seeded for all three.

**Architecture:** Three kinds of work. (1) A dry-run-first Python tool in this repo that
configures the four managed repos and the org Project through `gh`. (2) A **team-repo kit** in
`templates/team-repo/` — `AGENTS.md`, Node agent hooks, the OpenSpec recipe, regen script and
drift-check workflow — authored and tested once here, then copied into each managed repo by a
seeding PR. (3) Prose: a new Issues & Projects page, the setup guide, and edits to the gitflow,
the loop, and M7/M8.

**Tech Stack:** Python 3.11 stdlib + pytest (tools), `gh` CLI ≥ 2.40 (REST + GraphQL), Node
≥ 20.19 (hooks, regen; `node:test` for tests, no npm deps), OpenSpec CLI `1.10.0`, GitHub
Actions, MkDocs Material.

**Specs — read both, the plan argues from them:**
- [`../../specs/2026-11-04-github-issues-and-projects-design.md`](../../specs/2026-11-04-github-issues-and-projects-design.md) ("issues design")
- [`../../specs/2026-11-04-sdd-ai-setup-guide-design.md`](../../specs/2026-11-04-sdd-ai-setup-guide-design.md) ("setup design"). **Its §8 supersedes its earlier sections wherever they disagree.**

---

## Global constraints — delta only

`CLAUDE.md` loads automatically; do not restate it. Specific to this plan:

- **No Gradle project, given test, or `tests.lock` changes in this repo.** `poetry run python tools/tests_lock.py verify` stays rc=0 and unchanged throughout.
- **Nothing writes to GitHub except where a step says so.** Allowed writes: pushing branches and opening PRs in `Robot-2026`, `Phoenix-2026`, `flashpoint`, `IgniteLib`, and `IgniteRobotics/.github` (Josh merges). **Never** run `python -m tools.team_github --apply` — Josh runs it after reading the dry-run (issues design §3.6). Read-only `gh` calls are always fine.
- **Never edit the local checkouts at `~/workspace/ignite/<repo>`** — `Robot-2026`'s has uncommitted work. Each managed repo is worked in a fresh worktree or clone (task 10).
- **Pinned:** OpenSpec CLI `1.10.0`. Node floor `20.19`. Org `IgniteRobotics`. Mentor team `@IgniteRobotics/mentors` (id 18901463). Ruleset bypass team `code-reviewers` (id 9240266) + `OrganizationAdmin`.
- **Every vendor fact on a published page carries a link and an access date** (setup design §4). Nothing about a tool is written from memory; re-verify the setup design §8 findings at build time.
- **Voice:** match the de-slopped AI pages; run the `audience-style:de-slop` skill (audience: technical students) on every new or substantially rewritten page before its commit.
- Published-page gates before any commit that touches `docs/`: `poetry run pytest`, `poetry run mkdocs build --strict`, `poetry run python tools/site_guard.py _site`. **`--strict` will warn on pages not in nav until task 99 adds them — that is expected in lanes; task 99 must be clean.**

## Names fixed here — every task uses exactly these

| Thing | Value |
|---|---|
| Issues page | `docs/programming/issues-and-projects.md`, nav label **Issues & Projects**, placed after Team Gitflow |
| Setup guide | `docs/programming/ai/setup.md`, nav label **Setup**, first under Working with AI |
| Org Project title | **Ignite Programming** |
| Status options | `Backlog`, `Ready`, `In progress`, `In review`, `Done` |
| Other Project fields | `Size` (`XS`,`S`,`M`,`L`,`XL`), `Release` (single-select, starts empty) |
| Labels | `feature`, `bug`, `hotfix`, `spec` |
| Rulesets owned by the tool | `dev-main-pushes`, `dev-main-pr-gate` |
| Tool | package `tools/team_github/`, config `tools/team_github.toml`, run `poetry run python -m tools.team_github` |
| Org templates source | `templates/github/` → `IgniteRobotics/.github` |
| Team-repo kit | `templates/team-repo/` — mirrors a managed repo's root |
| Kit namespace in a managed repo | `.ignite/` — `.ignite/hooks/guard.mjs`, `.ignite/hooks/session-start.mjs`, `.ignite/openspec-regen.mjs`, `.ignite/openspec-recipe.json`; marker `.ignite/implementing` (gitignored) |
| Tier file | `~/.ignite-tier`, one line, printed verbatim |
| Drift workflow | `.github/workflows/openspec-drift.yml`, job `name: OpenSpec tool files` (= required-check context) |
| Build check workflow | `.github/workflows/pr-build-check.yml`, job `name: Build and Check Code Format` |

## File structure

```
tools/team_github/__init__.py, __main__.py, gh.py, config.py, model.py,
                  repos.py, rulesets.py, project.py, migrate.py, org_templates.py   NEW  1–2
tools/team_github.toml                                                              NEW  1
tests/test_team_github_*.py, tests/fixtures/team_github/*.json                      NEW  1–2
templates/github/ISSUE_TEMPLATE/{feature,bug,hotfix}.yml, config.yml,
                 PULL_REQUEST_TEMPLATE.md                                            NEW  3
templates/team-repo/.ignite/hooks/{lib,guard,session-start}.mjs + *.test.mjs        NEW  4
templates/team-repo/.ignite/{openspec-recipe.json,openspec-regen.mjs}               NEW  5
templates/team-repo/.github/workflows/openspec-drift.yml                            NEW  5
templates/team-repo/{AGENTS.md,CLAUDE.md,gitignore.fragment}, .github/CODEOWNERS    NEW  6
tests/test_team_repo_kit.py                                                          NEW  4–6
docs/programming/issues-and-projects.md                                             NEW  7
docs/programming/{team-gitflow.md, ai/the-loop.md}, docs/CONTRIBUTING.md            edit 7
docs/programming/milestones/m7-*.md, m8-*.md; projects/m7-*/README.md,
  projects/m8-*/README.md; docs/programming/curriculum-spec.md; tests/test_dod_sync.py edit 8
docs/programming/ai/setup.md                                                        NEW  9
docs/programming/tracks/track-d-frc-veteran-ai.md, docs/programming/ai/README.md    edit 9
<managed repo>: AGENTS.md, CLAUDE.md, .gitignore, .github/CODEOWNERS, .ignite/**,
  openspec/**, .claude/**, .agents/**, .github/{skills,prompts,workflows}/**         seed 10
mkdocs.yml, tools/site_guard.py, tests/test_site_guard.py,
  docs/programming/teaching-repo-spec.md                                            edit 99  CONTENDED
```

## Task graph

| Task | File | Depends on | Touches | Lane |
|---|---|---|---|---|
| 1 | `01-script-core.md` | — | `tools/team_github/` (core, repos, rulesets), its tests, config | `script` |
| 2 | `02-script-project.md` | 1 | `tools/team_github/` (project, migrate, org_templates), tests | `script` |
| 3 | `03-github-templates.md` | — | `templates/github/`; PR to `IgniteRobotics/.github` | `kit` |
| 4 | `04-agent-hooks.md` | — | `templates/team-repo/.ignite/hooks/`, `tests/test_team_repo_kit.py` | `kit` |
| 5 | `05-openspec-recipe.md` | 4 | `templates/team-repo/.ignite/`, drift workflow, kit test | `kit` |
| 6 | `06-agents-md.md` | 5 | `templates/team-repo/` root files, kit test | `kit` |
| 7 | `07-issues-docs.md` | — | issues page, team-gitflow, the-loop, CONTRIBUTING | `team-docs` |
| 8 | `08-curriculum.md` | — | M7/M8 pages + READMEs, curriculum-spec, dod test | `curriculum` |
| 9 | `09-setup-guide.md` | 4, 5, 6 | setup page, track D, ai/README | `guide` |
| 10 | `10-seed-repo.md` ×4 | 3, 6 | one managed repo each | `seed-robot-2026`, `seed-phoenix-2026`, `seed-flashpoint`, `seed-ignitelib` |
| 99 | `99-integration.md` | all | contended files; final dry-run; report | `integration` |

**Lanes:** `script` (1→2), `kit` (3, then 4→5→6 — task 3 may run first or last; it shares no
file with 4–6 but the lane keeps one agent on `templates/`), `team-docs` (7), `curriculum` (8),
`guide` (9, after kit), four `seed-*` lanes (10, after kit; one agent per repo, each in its own
clone — they share nothing).
**Maximum useful concurrency:** 4 at the start (`script`, `kit`, `team-docs`, `curriculum`);
up to 5 after the kit lands (`guide` + four `seed-*`).
**Serialized tail:** task 99, single owner, on the merged feature branch.
**Feature branch:** `feature/issues-and-ai-setup` (already holds both specs). Worktrees under
`.worktrees/<lane>`.

### Why the edges are where they are
- **9 waits on 4–6** because the guide documents exact hook commands, the recipe's pinned version, and what `AGENTS.md` says; writing it first means guessing them.
- **10 waits on 3 and 6** because seeding copies the finished kit, and the seeded `AGENTS.md`/PR flow assumes the org templates PR exists.
- **7 and 8 do not wait on anything**: they link only to filenames fixed above.
- **The tool's required-check guard (task 1) is what makes 10 and 99 order-safe**: until a seeded repo's drift and build workflows have reported on `development`, the tool marks those checks *pending* instead of requiring them.

---

## Review Focus

Inputs the specs imply but are easy to leave untested. Each has a test in the owning task.

1. **A Windows path into `src/test/`** (`C:\…\src\test\java\Foo.java`, or `SRC\Test` case) with the marker present must **block** — a silent allow looks like protection. → task 4, `guard: blocks backslash and mixed-case test paths`.
2. **A required check that has never reported in a repo** must be listed *pending* and left out of the applied ruleset, or every PR in that repo is blocked forever. → task 1, `test_unreported_required_check_is_pending_not_applied`.
3. **Running the tool against an already-converged org** must plan zero changes and exit 0, or it cannot serve as a drift check. → task 1 `test_converged_repo_plans_nothing`, task 2 `test_migration_rerun_adds_nothing`.
4. **A student running `openspec update` under a different global profile** must turn the drift check red with a message a student can act on. → task 5, distractor run (b).
5. **An unrecognised or malformed hook payload** (new tool version, empty stdin) must allow the call and print a visible warning — never crash, because a crashing PreToolUse hook blocks every call. → task 4, `guard: unknown payload allows with warning`.

---

## Self-review (done at authoring time)

- **Coverage.** Issues design §3.1 default branch → 1; §3.2 Project/fields → 2; §3.3/§3.4 lifecycle + archive-in-PR → 7, 8, 99 (§9.8/9.9 of the spec file); §3.5 templates → 3; §3.6 tool incl. migration, close-old, pending guard, code-owner review → 1, 2; §3.7 docs → 7; §3.8 Phoenix build check + fallback auto-add → 10, 99. Setup design §3.1 guide → 9; §3.2 seeding → 6, 10; §3.3 edits → 8, 9; §8.1–8.6 → 4, 5, 6, 9, 10; §8.7 drift check → 5, 10.
- **Fallback auto-add workflow** (issues §3.8) is conditional on §7.1/§7.2 verification; task 2 step 1 performs that verification and records the answer; task 10 adds the workflow only if task 2 recorded "needed".
- **Consistency.** All names come from the table above; tasks reference it rather than restating values.
