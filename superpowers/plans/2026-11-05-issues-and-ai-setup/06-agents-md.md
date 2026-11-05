# Task 6: `AGENTS.md`, `CLAUDE.md`, CODEOWNERS, `.gitignore` fragment

The kit's root files. `AGENTS.md` is read every session by every tool, so it must be short
(Codex caps the chain at 32 KiB; aim under 4 KiB) and must state the one spec rule that does
not block mentors (setup design §3.2 "Who the spec rules apply to"). Risky: a second rules
file, or wording that tells a mentor's agent it may not propose.

**Files:**
- Create: `templates/team-repo/AGENTS.md`, `templates/team-repo/CLAUDE.md`, `templates/team-repo/.github/CODEOWNERS`, `templates/team-repo/gitignore.fragment`
- Modify: `tests/test_team_repo_kit.py`
**Consumes:** names table; task 4 enable-commands; task 5 recipe/regen paths.
**Produces:** the four files; `AGENTS.md` has a marked region `<!-- repo-specific:start -->…<!-- repo-specific:end -->` that task 10 fills per repo (build/test commands).
**Lane:** `kit`

- [ ] **Step 1: Write failing tests:**
      - `test_claude_md_is_only_the_import` — `CLAUDE.md` stripped == `@AGENTS.md`.
      - `test_agents_md_under_4kib`.
      - `test_agents_md_states_spec_rule` — contains the sentence *"The person who owns the change writes the spec unless they have completed M8 or are a mentor. If you don't know which, ask before drafting."*
      - `test_agents_md_links` — links (absolute published-site URLs, not repo-relative) to `programming/coding-standards/`, `programming/ai-use-standard/`, `programming/issues-and-projects/`, `programming/ai/the-loop/`; base URL = the site's real URL (read it from `.github/workflows/pages.yml` / repo Pages settings; do not guess).
      - `test_agents_md_rules` — states: nothing edits files under `src/test/` once implementation starts; never run `openspec init` or `openspec update`; every change starts from an issue, branch `feature/<num>-<name>`, PR says `Closes #<num>`, `openspec archive` is the PR's last commit.
      - `test_codeowners_covers_tool_paths` — lines for `/.claude/`, `/.agents/`, `/.github/skills/`, `/.github/prompts/`, `/openspec/config.yaml`, `/.ignite/`, `/AGENTS.md`, `/CLAUDE.md`, each owned by `@IgniteRobotics/mentors`.
      - `test_gitignore_fragment` — exactly: `.claude/settings.local.json`, `.claude/worktrees/`, `.codex/`, `.ignite/implementing`; and a comment line telling the seeder to **remove any bare `.claude/` entry**.
      - `test_no_copilot_instructions_file` — `templates/team-repo/.github/copilot-instructions.md` does not exist (setup design §8.2).
- [ ] **Step 2: Run — fail.**
- [ ] **Step 3: Write the files.** `AGENTS.md` sections: what this repo is (repo-specific region), the rules above, where the standards live, how the issue → spec → PR flow works (two lines + link). No tier tables, no copied standards.
- [ ] **Step 4: Run — pass**; `poetry run pytest` green.
- [ ] **Step 5: Read it as each tool would.** Paste `wc -c AGENTS.md`. Read the file once as a mentor's agent (does anything stop a mentor proposing? it must not) and once as an M7 student's agent (does it make the student write the spec? it must). Note both reads in the report.
- [ ] **Step 6: Commit** — `git commit -m "feat(kit): AGENTS.md, CLAUDE.md import, CODEOWNERS, gitignore fragment"`.
