# Task 9: The SDD & AI setup guide

`docs/programming/ai/setup.md` — the M7 prerequisite page (setup design §3.1, revised by §8).
Risky: every vendor fact goes stale; the page must link, date, and verify, never restate
install docs. Re-verify every §8 finding against current docs and the real WPILib VS Code
before writing it down — §8 is a snapshot from 2026-11-05.

**Files:**
- Create: `docs/programming/ai/setup.md`
- Modify: `docs/programming/tracks/track-d-frc-veteran-ai.md` (one link, only where tooling is discussed), `docs/programming/ai/README.md` (add to its page list if it has one)
**Consumes:** task 4 enable-snippets (header of `guard.mjs`), task 5 recipe + regen command, task 6 `AGENTS.md`; issues page anchors.
**Produces:** the page; the verified Node floor (task 8 reads it from the report).
**Lane:** `guide`

- [ ] **Step 1: Verify facts, with URLs and access dates, into a scratch notes file:** WPILib 2026 VS Code version on a clean install (expect 1.105.1, `update.mode: none`); which Copilot Chat / Codex / Claude Code versions the Marketplace installs there (method: setup design §8 — throwaway `--extensions-dir`/`--user-data-dir`); what 1.105-era Copilot reads (AGENTS.md, `.github/prompts`); Copilot CLI availability, plan, and hook support; Codex trust model for `.codex/`; Claude Code AGENTS.md/CLAUDE.md behaviour; OpenSpec 1.10.0 Node requirement; each vendor's plan requirement and age terms (Anthropic 18+, OpenAI 13–17 with consent, GitHub/Copilot + GitHub Education). Anything that changed since §8 → note it in the report and write the page to the current truth.
- [ ] **Step 2: Write the page.** Sections exactly per setup design §3.1 1–6 as amended by §8:
      1. Before you start (M6 complete; link `ai-use-standard.md` §4).
      2. Pick a tool — comparison table: runs where (extension / CLI), access (link), eligibility (what the terms say, dated, link), reads `AGENTS.md`?, OpenSpec how (`/opsx:*` prompts vs `$openspec-*` skills), hooks (yes / CLI only / no). Copilot row states plainly: inside WPILib VS Code it is chat + `/opsx` prompts; for agent work use Copilot CLI in the WPILib terminal. No-eligible-tool line: **"Talk to a mentor."** and nothing more. One subsection per tool: install into the **WPILib** VS Code, sign in, any setting to change — everything else is a vendor link. Recommend one AI extension enabled per workspace.
      3. Node + OpenSpec: install Node (link), `npm i -g @fission-ai/openspec@1.10.0`, set `OPENSPEC_TELEMETRY=0` (per-OS one-liner), `openspec --version` → `1.10.0`. Never run `init` or `update`; the repo carries it, CI checks it.
      4. Hooks: what each does (link `the-harness.md`), per-tool enable snippet from task 4 (local files only), the marker workflow (`.ignite/implementing` at rung 4, delete after 5), the tier file. Say plainly: Copilot in WPILib VS Code has no hooks; CI and review are the backstop; the shell check is best effort.
      5. Verify: per setup design §3.1(5) with per-tool invocation (`/opsx:explore` vs `$openspec-explore`), plus `node .ignite/hooks/guard.mjs < .ignite/hooks/fixtures/claude-edit.json` with the marker present → blocked.
      6. When something is wrong: wrong VS Code; `AGENTS.md` not seen (Claude Code: needs the committed `CLAUDE.md`); `/opsx` missing (repo not seeded → mentor); duplicate skills in the list (expected); drift check red (revert, see message).
      Plus a short **For mentors** section: regenerate with `node .ignite/openspec-regen.mjs`, bump procedure (edit recipe version → regen → commit), pointer to `#repo-setup`.
- [ ] **Step 3: Gates.** `poetry run pytest`, `poetry run mkdocs build --strict` (only the not-in-nav warning allowed); every external link resolves (`docs-links` job locally if available, else `curl -sI` each, paste status codes).
- [ ] **Step 4: Read it cold.** De-slop (technical students). Then read top to bottom as a Windows student with Codex and as a macOS student with Copilot: can each finish without a mentor? List any step that assumed something unstated, fix it.
- [ ] **Step 5: Commit** — `git commit -m "docs(ai): SDD and AI setup guide"`.
