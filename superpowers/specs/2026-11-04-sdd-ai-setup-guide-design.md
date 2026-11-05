# SDD & AI setup guide — design record

**Date:** 2026-11-04
**Companion change:** [`2026-11-04-github-issues-and-projects-design.md`](2026-11-04-github-issues-and-projects-design.md) — same session; the two are planned together as separate lanes
**Decided with:** Josh, in a brainstorming session. Anything marked *(Claude's call)* or *(builder's call)* was not decided by him and may be overridden in review.

---

## 1. Intent

Ignite needs one place that takes a student from "finished M6" to "ready for the first M7 session": an AI coding tool installed and working inside the WPILib VS Code, pointed at Ignite's rules, with OpenSpec working against the team repos.

Today that path is a single line in M7's prerequisites — *"Node 20.19+ with the OpenSpec CLI installed — see the project README"* — and nothing about the AI tool itself. Students bring their own tool access, and the team repos are only wired up for one tool each, so a student who picks a different tool opens `Robot-2026` and finds no `/opsx` commands.

**Don't duplicate public docs (Josh).** Vendors maintain install and account docs and change them often. This guide links to them and writes only what is Ignite-specific: WPILib VS Code integration, how each tool picks up Ignite's rules, OpenSpec against Ignite repos, hooks, and a check that it all works.

Success: a student who has finished M6 follows the page in one sitting, with any of the three tools, and ends it having run a verification step that proves the tool reads the repo's rules and OpenSpec works — without a mentor at their elbow and without reading anything the vendor already documents.

---

## 2. Reader and scope

**Reader (Josh):** a student who has **completed M6**, about to start M7. M0 already gave them WPILib 2026 + its bundled VS Code, git, a GitHub account, and push auth (`SETUP.md`). Also mentors and team developers working in the managed repos.

Not the reader: students at M0–M5. Red and Yellow tiers are governed by `ai-use-standard.md` and `SETUP.md`'s "turn Copilot off" line; this guide does not stage tools by tier and does not change those rules.

**Tools (Josh):** GitHub Copilot, OpenAI Codex, Claude Code — documented as **equal options; students pick**. All three must work inside the WPILib-installed VS Code (extension in that VS Code; any CLI run from its integrated terminal).

**Access (Josh):** students bring their own. The team provides no accounts. The guide links each vendor's **eligibility and age terms** rather than restating them — many students are under 18, vendor terms differ, and Claude's belief (unverified) is that at least one vendor's consumer plans require 18+. The builder states what each vendor's current terms say, with a link and an access date, and does not editorialise beyond that.

**A student who can't get any of the three (Josh):** the guide says *talk to a mentor*, and nothing more. No fallback route is named; mentors handle it case by case. This is the accepted exception to §5's "any student, one sitting" goal.

**Managed repos:** `Robot-2026`, `Phoenix-2026`, `flashpoint`, `IgniteLib` — the same four as the companion change.

---

## 3. What ships

### 3.1 The guide page *(location: Claude's call)*

`docs/programming/ai/setup.md`, first entry under "Working with AI" in `mkdocs.yml` nav. Sections, in order:

1. **Before you start.** Prerequisite: M6 complete. One paragraph on why the gate is M6 (link `ai-use-standard.md` §4, don't restate it).
2. **Pick a tool.** A short comparison table — Copilot / Codex / Claude Code — with, per tool: where it runs (VS Code extension, CLI, both), how a student gets access (link), eligibility/age terms (link + what they say, dated), which Ignite rules file it reads (§3.2), whether it supports blocking hooks. Then one subsection per tool covering only the Ignite-specific part: install the extension **into the WPILib VS Code** (not a separately installed VS Code), sign in, where the CLI runs if there is one, and any setting a student must change. Everything else is a link to the vendor's doc.
3. **Node and the OpenSpec CLI.** Node version per OpenSpec's current requirement (M7 says 20.19+; the builder checks and updates M7 if it has moved), install the CLI, `openspec --version`. **Students do not run `openspec init`** — the repos already carry it (§3.2). Note that `openspec-propose` is present in the repos but is off-limits before M8 (link `ai-use-standard.md` §4; the repo's `openspec/config.yaml` context already tells agents this).
4. **Hooks** *(Claude's call; **revised by §8.4** — read that first)*. Link `the-harness.md` for the why. For each tool that supports blocking hooks, give the config for the starting set that page lists (block `src/test/` edits once implementation starts; block `git push` and `--no-verify`; format on edit; print tier at session start), as **local, per-student config** — not committed to the repos, because the `src/test/` block depends on which rung the work is on. For a tool without blocking hooks, say so plainly and name the backstop: CI and review. *(The team repos have no test-file checksum check — that exists only in `ignition-labs`. See §8.4.)*

   **Where the hooks get their state** *(Claude's call)*: the `src/test/` block is keyed on a **gitignored marker file in the working tree** (e.g. `.ignite/implementing`) that the student creates when they reach rung 4 and deletes after rung 5; present → block, absent → allow. The tier comes from a **one-line local file in the student's home directory** they write once and update when a mentor signs off a milestone; the session-start hook prints it, and prints "tier unknown — ask a mentor" if it is missing. Both are honour-system by design: hooks are a guardrail against the agent, not against the student, and CI is the enforcement layer. The builder adds the marker path to each managed repo's `.gitignore` as part of seeding.
5. **Verify it works.** In the WPILib VS Code, open a fresh clone of `Robot-2026` on `development` and:
   - `openspec --version` and `openspec list` succeed
   - ask the agent "what are this repo's coding standards and AI-use rules?" — its answer must cite `AGENTS.md` / the coding standards (proves the rules file is read)
   - run the explore command — `/opsx:explore` in Claude Code and Copilot, `$openspec-explore` in Codex (§8.3) — it must be found (proves the per-tool OpenSpec files are in place)
   - `./gradlew build` still passes from the same terminal (proves nothing broke the toolchain)
   Each check states what success looks like and what to do if it fails.

   **Version pinning** *(Claude's call)*: the guide names the OpenSpec CLI version that generated the committed files and tells students to install that version. Bumping it is part of the season maintenance pass, the same as GradleRIO.
6. **When something is wrong.** Short troubleshooting: wrong VS Code, tool doesn't see `AGENTS.md`, `/opsx` commands missing (→ the repo isn't seeded; tell a mentor, don't run `init` or `update`).

### 3.2 Repo seeding (Josh: "all three")

Every managed repo carries, committed, everything all three tools need, so a student installs their tool and nothing else:

- **OpenSpec integration files for all three tools**, generated by OpenSpec's own `init`/`update` for Copilot, Codex, and Claude Code. Today `Robot-2026` has only `.agents/skills/` and `flashpoint` only `.claude/`; `IgniteLib` and `Phoenix-2026` have no `openspec/` at all and get it created. Existing `openspec/config.yaml` content (notably `Robot-2026`'s "the student writes the spec" context) is preserved; new repos get a `config.yaml` with equivalent context *(builder's call on wording; must carry the same student-writes-the-spec rule)*.
- **A shared `AGENTS.md`** at each repo root — one rules file every tool reads. It points to (does not copy) the coding standards (`coding-standards.md` on the published site), the AI-use tiers and disclosure (`ai-use-standard.md`), and states the "nothing edits the tests once implementation starts" rule and the issue → spec → PR flow from the companion change. Repo-specific build/test commands go here too. *(builder's call on content beyond that; keep it short — it is read on every session.)*
- **`CLAUDE.md`** that imports `AGENTS.md` (`@AGENTS.md`) and adds nothing else — required, see §8.2. **Copilot**: whatever file current Copilot reads for repo instructions — if it reads `AGENTS.md`, nothing extra; if it needs `.github/copilot-instructions.md`, that file points at `AGENTS.md`. The builder verifies each tool's current behaviour and adds the minimum.

**If OpenSpec cannot generate files for one of the three tools** *(Claude's call)*: document that tool as degraded on the guide's comparison table (what works, what doesn't), and do **not** hand-author command files — hand-written copies drift from the CLI on the next `openspec update`. The tool stays listed; the verification step's explore check is marked as not applicable for it.

**Who the spec rules apply to** *(Claude's call)*: `AGENTS.md` and the `config.yaml` context state one rule that works for every person in every managed repo, rather than a blanket "the student writes the spec": *the person who owns the change writes the spec unless they have completed M8 or are a mentor; if the agent doesn't know which, it asks before drafting.* `Robot-2026`'s existing context is reworded to match. This keeps mentors and post-M8 students unblocked without separate files per audience.

Seeding lands in each repo **through a PR into `development`, which the implementation opens (Josh); Josh merges**, following the companion change's flow. `the-loop.md`'s rung 11 already says "AGENTS.md updated" — once these exist, that line is true.

### 3.3 Edits elsewhere

| File | Change |
|---|---|
| `docs/programming/milestones/m7-ai-assisted-development.md` | Prerequisite line links the new guide instead of "see the project README"; Node version updated if it moved. The step-2 sentence "you do not run `openspec init`" stays and now matches the guide |
| `docs/programming/tracks/track-d-frc-veteran-ai.md` | Link to the guide where it discusses tooling *(builder: only if there is a natural place)* |
| `docs/programming/ai/README.md` | Add the guide to the section's own index if it lists pages |
| `mkdocs.yml` | Nav entry (§3.1) |
| `tools/site_guard.py` + its test | Register the page if `site_guard` keeps a page registry |

Not touched: `SETUP.md` (M0 setup, and its Red-tier "turn Copilot off" line), `ai-use-standard.md`, `tests.lock`, `docs/programming/README.md`'s status row, `teaching-repo-spec.md` §13.

---

## 4. Constraints

- **Every factual claim about a tool links its source and carries an access date.** These change monthly. The builder verifies against current vendor docs at build time; nothing is written from memory. Facts to verify: each tool's VS Code extension and CLI status; which instructions file(s) each reads; hook support and config format; OpenSpec's supported-tools list and the exact `init`/`update` invocation for the three; OpenSpec's Node requirement; each vendor's student/free access route and age/eligibility terms.
- **Must work in the WPILib-installed VS Code** on Windows and macOS. The builder confirms each extension installs from the Marketplace there; if any tool's extension cannot, the guide says so and gives the CLI path.
- **No hardware, no specific paid plan required** to complete the verification step beyond the student's own tool access.
- **`docs/` publishes publicly.** No tokens, no account details. Links to private repos are fine (the link check skips private Ignite repos).
- **Voice** matches the de-slopped AI pages: technical student audience, direct, no invented aphorisms. Run the `de-slop` skill before review.
- **No pre-2026 Ignite references.**
- Before merging: `poetry run pytest`, `poetry run mkdocs build --strict`, `poetry run python tools/site_guard.py _site`.

---

## 5. Goals, non-goals, anti-goals

**Goals:** any of three tools, chosen by the student, working against any managed repo after one sitting; one rules file per repo that every tool reads; a verification step that fails loudly when setup is incomplete.

**Non-goals:** recommending one tool; providing accounts; tier-staged setup for M0–M5 students; restating vendor install or account docs; committing hooks to the repos.

**Anti-goals — failure even if it technically works:**
- A page that copies vendor install steps and is wrong within a season.
- A student whose tool works on `Robot-2026` but finds nothing on `IgniteLib`, because seeding was done in one repo.
- Three rules files per repo with three slightly different sets of rules. There is one, `AGENTS.md`; anything else points at it.
- A verification step that passes when the agent isn't reading the rules.
- A student told to run `openspec init` or `openspec update`.

---

## 6. Left to the builder

- Page wording, comparison-table columns beyond those in §3.1, troubleshooting entries.
- `AGENTS.md` content beyond §3.2's required points; new repos' `config.yaml` wording.
- Exact hook config per tool.
- ~~Whether `CLAUDE.md` / `copilot-instructions.md` are needed~~ — settled in §8.2.

## 7. Coordination with the companion change

**Sequencing** *(Claude's call)*: this lane depends on the companion's docs lane for two things — the name of its new Issues & Projects page (which `AGENTS.md` and the guide link) and the archive-in-PR wording. The plan fixes the page's filename before either lane starts. `mkdocs.yml` is edited by both lanes, so nav entries for both new pages go in the plan's integration task, not in either lane. `tools/site_guard.py`'s `REQUIRED_PAGES` list and its test likewise.

Local clones of all four managed repos exist under `~/workspace/ignite/` (`Robot-2026`, `Phoenix-2026`, `flashpoint`, `IgniteLib`). Seeding work happens in a worktree per repo, never in those checkouts directly.

Both changes open PRs into the same four repos (companion: `Phoenix-2026` build check, possible auto-add workflow; this: seeding). *(Claude's call)* one PR per repo per change is fine; they touch disjoint files. The guide's verification step assumes `development` is the default branch, which the companion change sets — if this lands first, the step says "check out `development`" explicitly (it does anyway).

---

## 8. Pressure test (2026-11-05) — supersedes earlier sections where they disagree

Josh asked for the setup to be pressure-tested for interference between agents and conflicts. Method: three research passes against current vendor docs (one per tool, every claim URL-cited in the session), plus hands-on tests — `openspec init`/`update` (v1.10.0) run on a scratch export of `Robot-2026@development`, and each extension installed into the actual WPILib 2026 VS Code (1.105.1) using throwaway `--extensions-dir`/`--user-data-dir` so the real install was untouched. Evidence is summarised here; the builder re-verifies anything vendor-side at build time per §4.

### 8.1 What actually runs inside WPILib VS Code 1.105.1

WPILib's VS Code ships with `update.mode: none` and `extensions.autoUpdate: false`. The Marketplace serves the newest extension compatible with 1.105.1, and that version then stays put.

| Tool | Version the Marketplace installed on 1.105.1 | Status |
|---|---|---|
| Claude Code | 2.1.318 (`^1.94.0`) | current — full features |
| Codex (`openai.chatgpt`) | 26.1021.22027 (`^1.96.2`) | current — full features |
| Copilot Chat | **0.32.5** (`^1.105.0`) | **about a year old.** Current Chat requires `^1.146.0`. 1.105-era Copilot reads root `AGENTS.md` and `.github/prompts/*.prompt.md`; **no skills** (arrived 1.108), **no hooks**, no `CLAUDE.md` |

**Decision *(Claude's call)*:** Copilot stays an equal option, but the guide is honest about what it is inside WPILib VS Code: chat plus `/opsx` prompt files, with `AGENTS.md` read. For agent work with hooks and skills, the guide points Copilot users at the **Copilot CLI** run from the WPILib VS Code terminal — it is independent of the VS Code version (builder verifies plan/eligibility for the CLI). The guide does **not** tell students to install a second, newer VS Code; that breaks "all of it works in WPILib VS Code".

### 8.2 Instruction files — no conflict with the planned set

- **Codex** reads `AGENTS.md` only (32 KiB cap across the chain). Ignores `CLAUDE.md` and `copilot-instructions.md`.
- **Claude Code** reads `AGENTS.md` natively *only if no `CLAUDE.md` exists*; with a `CLAUDE.md` it reads that only. A `CLAUDE.md` containing `@AGENTS.md` is the documented pattern and loads it once. So `CLAUDE.md` is **required**, not optional — a student's personal `CLAUDE.local.md` would otherwise silently switch off native `AGENTS.md` reading.
- **Copilot** (1.105) reads `AGENTS.md`. Newer Copilot and Copilot CLI also read `CLAUDE.md`; since ours is a one-line import, the duplication is harmless.
- **Do not add `.github/copilot-instructions.md`.** Copilot loads it alongside `AGENTS.md`; a second rules file is exactly the drift §5's anti-goal names.
- Keep `AGENTS.md` short (well under 32 KiB) — it is read every session by every tool.

### 8.3 OpenSpec files — four real problems, found by running it

1. **`Robot-2026`'s `.gitignore` ignores `.claude/` entirely.** `openspec init --tools claude,...` wrote `.claude/skills/*` and git silently ignored all six. Seeding would have "succeeded" and shipped nothing for Claude Code. **Fix:** replace `.claude/` with `.claude/settings.local.json` and `.claude/worktrees/` in each managed repo's `.gitignore`. Check all four repos, not just `Robot-2026`.
2. **Output depends on whoever runs it.** OpenSpec's `profile` and `delivery` are *global-only* user settings (`openspec config --scope` supports only `global`). Re-running `openspec update` on the seeded copy under a different user profile (`core`, `delivery: both`) rewrote 12 committed files and added 12 more. One student running `update` with different settings churns every committed skill. Nothing can stop the rewrite on a laptop; the design stops it reaching `development`. **Fix:** (a) the guide and `AGENTS.md` say never run `openspec init` or `update` in a team repo; (b) **CODEOWNERS** on `.claude/`, `.agents/`, `.github/skills/`, `.github/prompts/`, `openspec/config.yaml` so changes there need a mentor's review; (c) the regeneration recipe — exact CLI version, `profile`, `delivery`, `--tools`, `--no-copilot-cloud` — is written down in one place (the guide's mentor section *(builder's call on location)*) and is the only way those files change; (d) **a CI drift check** in each managed repo (Josh, 2026-11-05) — see §8.7; (e) CODEOWNERS only blocks a merge if the ruleset requires code-owner review, so the companion change's `dev-main-pr-gate` turns that on (companion §3.6).

`init`/`update` were observed to touch only tool files and `config.yaml`, never `openspec/specs/` or `openspec/changes/`. `Robot-2026`'s `specs/` holds only `.gitkeep`, so this was not confirmed against real spec content — the builder confirms it on a repo with archived specs (`flashpoint`).
3. **Copilot on 1.105 cannot use skills, so OpenSpec must also emit prompt files.** `delivery: skills` (what produced today's files) gives Copilot nothing it can run in WPILib VS Code. **Fix:** seed with `delivery: both` — skills **and** commands. Observed output: `.claude/skills/` + `.claude/commands/opsx/`, `.agents/skills/` (Codex; Codex has no repo-level commands, so `$openspec-explore` is its invocation), `.github/skills/` + `.github/prompts/opsx-*.prompt.md`. Robot-2026's existing `.github/prompts/code-review-prompt.md` (used by `ai-code-review.yml`) is untouched and is not a `*.prompt.md`, so Copilot doesn't load it.
4. **Duplicate skills are clutter, not breakage.** Current Copilot reads `.github/skills`, `.claude/skills` and `.agents/skills`, so it lists each OpenSpec skill up to three times. The copies differ only in invocation syntax. Documented as expected in troubleshooting; not worked around (symlinks break on Windows checkouts, and hand-deleting a directory is undone by the next regeneration).

`openspec init` also adds `githubCopilot: cloudAgent: false` to `config.yaml` — expected, keep it. The existing `context:` block was preserved.

**Telemetry:** OpenSpec sends anonymous usage stats by default. The guide tells students to set `OPENSPEC_TELEMETRY=0` *(Claude's call — minors, and nothing is lost)*.

### 8.4 Hooks — the design in §3.1(4) was wrong in four ways

1. **Windows shell.** Claude Code runs hooks in bash, or PowerShell when Git Bash is absent. A bash + `jq` hook breaks on a Windows laptop without Git Bash, and a PreToolUse hook that errors out **blocks every call**.
2. **Windows paths.** `tool_input.file_path` arrives with backslashes even under Git Bash. A naive `/src/test/` check never matches and **silently allows** the edit — the worst failure mode, because it looks like protection.
3. **Edit/Write blocking is bypassable through the shell tool** (`sed`, redirects, Gradle tasks writing into `src/test/`). The block is a guardrail against an agent drifting, not a security boundary.
4. **Format-on-edit runs Gradle (~10 s) after every edit** and stalls the agent loop. And the claimed backstop — a test-file checksum in CI — **does not exist in the team repos**.

**Revised hook design *(Claude's call)*:**
- **Hook scripts are written in Node**, which every student already has for OpenSpec — no bash, no `jq`, same behaviour on Windows and macOS. Paths are normalised (`\` → `/`, case-insensitive on Windows) before matching.
- **The scripts are committed** (e.g. `tools/agent-hooks/` in each managed repo — *builder's call on path*) so they are tested and identical for everyone; **enabling them stays local**: `.claude/settings.local.json` for Claude Code (per-repo, not `~/.claude/settings.json`, so a student's hooks never affect their other projects), `.codex/hooks.json` for Codex (only loads once the student trusts the project — the guide says so), Copilot CLI's hook config for Copilot CLI. Nothing is enabled by a committed settings file. This matters because **current Copilot (VS Code and CLI) also reads repo `.claude/settings.json` and `.claude/settings.local.json`** — a committed Claude hook would fire inside Copilot too.
- The `src/test/` block also checks the shell tool's command for writes into `src/test/` — best effort, documented as such.
- **The shell-tool hook also blocks `openspec init` and `openspec update`** (Josh, 2026-11-05), so an agent cannot trigger the §8.3(2) rewrite. Students running it by hand are caught by §8.7.
- **Drop format-on-edit.** Formatting stays what `CONTRIBUTING.md` already says: `./gradlew spotlessApply` before you push, enforced by `spotlessCheck` in CI. *(Optional: a Stop-hook that runs it once per turn — builder's call, off by default.)*
- **The backstop is CI + review, said plainly.** Adding a test-checksum check to the team repos is a candidate follow-up, not part of this change.
- Codex alternative for the `git push` block: a committed `.codex/rules/*.rules` `forbidden` rule is harmless to other tools, but it applies to mentors too — so it is **not** committed; the guide shows it as local config like the rest.
- Copilot in WPILib VS Code has no hooks (§8.1) — the guide says so in the comparison table.

### 8.5 Accounts — the guide states these plainly

- **Claude Code:** paid plan required (free claude.ai has no Claude Code), and Anthropic's consumer terms require users to be **18+**. Most students cannot hold their own account. Per Josh's rule, the guide states the terms and says "talk to a mentor"; it does not suggest workarounds.
- **Codex:** a paid ChatGPT plan; OpenAI's terms allow 13–17 with parental consent. An OpenAI employee's May 2026 Chief Delphi post offered FRC teams Codex seats — unofficial; the builder checks whether it is still open and mentions it only if a mentor confirms the team has seats.
- **Copilot:** builder verifies current free tier and GitHub Education student access and their age terms.

### 8.6 Side-by-side extensions

No documented conflict between the Claude Code, Codex and Copilot extensions installed together. All three installed into the same WPILib VS Code without error. None writes to `.vscode/settings.json` (WPILib's file); Claude Code writes `.claude/settings.local.json` and `.claude/worktrees/`, which the §8.3(1) `.gitignore` fix covers. The guide still recommends **one AI extension enabled per workspace** *(Claude's call)* — two agents editing the same tree at once is a conflict no file layout fixes.

### 8.7 OpenSpec drift check (Josh, 2026-11-05)

A small workflow committed to each managed repo, run on every PR into `development` and `main`:

1. Install the **pinned** OpenSpec CLI version with `OPENSPEC_TELEMETRY=0` and a throwaway `HOME`, so no runner or user config leaks in.
2. Apply the recorded recipe — `profile`, `delivery: both`, `--tools claude,codex,github-copilot`, `--no-copilot-cloud` — and regenerate the tool files.
3. Fail if anything differs from what is committed, tracked or untracked, with a message that names the changed files and says *"OpenSpec tool files changed. Students: revert these files (`git checkout -- <paths>`); never run `openspec init` or `update` here. Mentors: regenerate with the recipe in <link>."* — the builder reads that message as a student would (CLAUDE.md verification discipline).

**One recipe, one place.** The pinned version and recipe live in a single file in each repo that both the workflow and the mentor regeneration instructions read *(builder's call on file and format)*, so a season bump is one edit.

It becomes a **required status check** in that repo's `dev-main-pr-gate` (companion §3.6 config gains the check name), otherwise a red drift check could still merge.

**Verify before relying on it (CLAUDE.md discipline):** on a scratch branch, (a) unmodified seeded repo → green; (b) run `openspec update` under a different global profile and commit → red, message names the files; (c) hand-edit one `SKILL.md` → red; (d) a real archive commit (specs change, tool files don't) → green.
