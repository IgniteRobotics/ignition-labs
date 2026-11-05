# Task 4: Agent hooks — Node, cross-platform, fail-loud-open

The guardrail scripts every managed repo will carry under `.ignite/hooks/`. Setup design §8.4
is the spec: Node (no bash, no `jq`), paths normalised, shell commands checked best-effort,
committed scripts but **local** enablement. Risky in exactly the ways §8.4 found: a backslash
path silently allowed, and a crashing hook blocking every call.

**Files:**
- Create: `templates/team-repo/.ignite/hooks/lib.mjs`, `guard.mjs`, `session-start.mjs`, `lib.test.mjs`
- Modify: `tests/test_team_repo_kit.py` (add `test_hook_suite_passes_under_node`)
**Consumes:** names table (marker, tier file).
**Produces:**
- `lib.mjs` exports: `normalizePath(p: string, platform: string) -> string` (backslashes → `/`, lower-cased when `platform === "win32"`); `parsePayload(raw: string) -> {tool: "edit"|"shell"|"other", path?: string, command?: string, cwd: string} | null` (null = unrecognised); `decide(event, {markerExists: boolean, platform}) -> {block: boolean, reason: string}`.
- `guard.mjs` (PreToolUse entry): reads stdin, calls the above; **exit 2 + reason on stderr to block; exit 0 otherwise**. Any exception or `null` payload → exit 0 with `ignite guard: could not read hook input — not checking this call` on stderr.
- `session-start.mjs`: prints `Ignite tier: <first line of ~/.ignite-tier>` or `Ignite tier: unknown — ask a mentor`; always exit 0.
- Enable-commands per tool (consumed by task 9): Claude Code `.claude/settings.local.json` PreToolUse matcher `Edit|Write|MultiEdit|Bash` → `node .ignite/hooks/guard.mjs`, SessionStart → `node .ignite/hooks/session-start.mjs`; Codex `.codex/hooks.json` equivalents; Copilot CLI equivalent if its hook payload is documented. Put the exact snippets in a header comment of `guard.mjs`.
**Lane:** `kit`

- [ ] **Step 1: Pin the payload shapes from docs.** From current docs (URL + date in a comment in `lib.mjs`): Claude Code PreToolUse stdin (`tool_name`, `tool_input.file_path` / `.command`, `cwd`); Codex PreToolUse stdin; Copilot CLI preToolUse stdin. Save one real-shaped example of each as `templates/team-repo/.ignite/hooks/fixtures/<tool>-<edit|shell>.json`.
- [ ] **Step 2: Write failing tests** in `lib.test.mjs` (`node:test`, `node:assert/strict`), named:
      - `guard: blocks test edit when marker present` (posix path `…/src/test/java/X.java`)
      - `guard: blocks backslash and mixed-case test paths` — `C:\\robot\\src\\test\\java\\X.java` and `C:\\robot\\SRC\\Test\\X.java` on `win32` → block.
      - `guard: allows test edit when marker absent`
      - `guard: allows src/main edit with marker present`
      - `guard: blocks git push and --no-verify` (`git push origin x`, `git commit --no-verify -m y`; also `git -C . push`)
      - `guard: blocks openspec init and update` (`openspec init`, `openspec update --force`, `npx @fission-ai/openspec update`)
      - `guard: allows openspec list, status, validate, archive`
      - `guard: blocks shell writes into src/test with marker` (`echo x > src/test/A.java`, `sed -i s/a/b/ src/test/A.java`, `rm src/test/A.java`, `cp a src/test/`) and **allows** `cat src/test/A.java`, `./gradlew test`.
      - `guard: unknown payload allows with warning` — `""`, `"{}"`, `"not json"`, a payload with an unknown tool name → parse returns null / tool `other` → guard exits 0 and stderr contains `not checking`.
      - `guard: every fixture parses` — each file in `fixtures/` parses to a non-null event.
      - `session-start: prints tier or unknown` — with a temp `HOME` containing / lacking `.ignite-tier`.
      Run guard end-to-end in tests via `child_process.spawnSync(process.execPath, [guardPath], {input, cwd: tmpRepo})` so exit codes are tested, not just `decide`.
- [ ] **Step 3: Run — fail.** `node --test templates/team-repo/.ignite/hooks/` → failures (no modules).
- [ ] **Step 4: Implement.** Marker check = `existsSync(join(event.cwd, ".ignite", "implementing"))`; resolve the edited path relative to `cwd` before matching `^src/test/` (normalised). Shell write detection: a command mentions a normalised `src/test/` path **and** contains one of `>`, `>>`, `tee `, `sed -i`, `rm `, `mv `, `cp `, `truncate` — documented in-code as best effort.
- [ ] **Step 5: Run — pass.** `node --test templates/team-repo/.ignite/hooks/` → all pass. Paste output.
- [ ] **Step 6: Wire into pytest.** `test_hook_suite_passes_under_node`: `subprocess.run(["node", "--test", <dir>])`, assert rc 0; **fail** (not skip) with "Node ≥ 20.19 is required to run the hook suite" if `node` is missing or older — a skipped guard test in CI is a guard nobody checked. `poetry run pytest` green.
- [ ] **Step 7: Read the messages.** Run guard by hand on each block case; paste stderr. Each must say what was blocked and why in one line a student understands (e.g. `Blocked: editing src/test/… while .ignite/implementing exists. Tests are frozen during implementation — see the SDD loop.`).
- [ ] **Step 8: Commit** — `git commit -m "feat(kit): cross-platform agent guard hooks"`.
