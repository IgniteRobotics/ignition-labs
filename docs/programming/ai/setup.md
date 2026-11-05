# Setup: SDD and AI tools

Get one AI coding tool working inside the WPILib VS Code, pointed at Ignite's rules, with OpenSpec and the local hooks. Plan on one sitting, with no mentor needed.

Every vendor fact on this page links its source and carries the date it was checked: **2026-11-05**. Vendors change their docs monthly. If a link or a fact disagrees with what you see, trust the vendor page and tell a mentor so this page gets fixed.

---

## 1. Before you start

You need M6 complete. The AI-use standard opens agentic tools at the Green tier, which starts when M6 is done. [AI Use Standard §4](../ai-use-standard.md#4-the-three-tiers) has the full conditions and the reasoning. This page does not repeat them.

You also need what M0 gave you: the WPILib 2026 install with its bundled VS Code, git, a GitHub account, and push access to the team repos.

Open the VS Code that came with WPILib. On Windows and macOS that is the **WPILib VS Code** from the WPILib install (see [WPILib's install guide](https://docs.wpilib.org/en/stable/docs/zero-to-robot/step-2/wpilib-setup.html), accessed 2026-11-05), not a VS Code you installed yourself. Everything below happens in that one.

Check the version: **Help > About** should say `1.105.1`. WPILib pins its VS Code and turns off updates, so it stays that old on purpose. Do not update it, and do not install a second VS Code to get newer features. Section 2 says what that means for each tool.

---

## 2. Pick a tool

Pick one. You only need one.

| | **GitHub Copilot** | **OpenAI Codex** | **Claude Code** |
|---|---|---|---|
| Runs in WPILib VS Code as | Chat extension (old version, see below). Agent work: **Copilot CLI** in the VS Code terminal | Extension | Extension |
| Cost to you | [Free plan, Student plan, or Pro](https://docs.github.com/en/copilot/get-started/plans) | [Included in every ChatGPT plan, including Free](https://learn.chatgpt.com/docs/pricing) | [Paid Claude plan or Console account](https://code.claude.com/docs/en/ide-integrations) |
| Age terms | [GitHub: 13 or older](https://docs.github.com/en/site-policy/github-terms/github-terms-of-service). Student plan needs [verified GitHub Education status](https://docs.github.com/en/education/about-github-education/github-education-for-students/about-github-education-for-students) | [13 or older; under 18 needs a parent or guardian's permission](https://openai.com/policies/terms-of-use/) | [18 or older](https://www.anthropic.com/legal/consumer-terms) |
| Reads `AGENTS.md` | Yes | Yes | Through the repo's one-line `CLAUDE.md`, which imports it |
| OpenSpec commands | `/opsx-*` prompt files in chat | `$openspec-*` skills | `/opsx:*` commands |
| Hooks | **No** in WPILib VS Code. **Yes** in Copilot CLI | Yes | Yes |

Terms and prices change. All of this was checked on 2026-11-05; if a link disagrees, the link wins.

**If none of these three works for you (age, cost, or anything else): talk to a mentor.**

### GitHub Copilot

Inside WPILib VS Code you get **chat and the `/opsx` prompt files**, nothing more. WPILib's VS Code is 1.105.1, and the Marketplace gives it the newest Copilot Chat that supports that version: 0.32.5 (checked 2026-11-05 by installing it into WPILib's VS Code). That version reads `AGENTS.md` and `.github/prompts/*.prompt.md`. It has no skills and no hooks.

For agent work, with hooks, use the **Copilot CLI** from the terminal inside WPILib VS Code. It does not depend on the VS Code version.

1. In WPILib VS Code, open **Extensions** (`Ctrl+Shift+X`, or `Cmd+Shift+X` on macOS), search `GitHub Copilot Chat`, install it, and sign in with your GitHub account. [Vendor docs](https://docs.github.com/en/copilot/get-started/plans).
2. Confirm the setting **Chat: Use Agents Md File** (`chat.useAgentsMdFile`) is on. It is on by default in 1.105.1 (checked in the VS Code build itself).
3. For the CLI, install it from the VS Code terminal: `winget install GitHub.Copilot` on Windows, or `brew install --cask copilot-cli` on macOS (needs [Homebrew](https://brew.sh); without it, use the install script in the vendor docs). The npm route needs Node 22 or newer. Run `copilot`, then `/login`. [Vendor docs](https://docs.github.com/en/copilot/how-tos/copilot-cli/set-up-copilot-cli/install-copilot-cli).

Copilot CLI reads `AGENTS.md` and `CLAUDE.md` ([vendor docs](https://docs.github.com/en/copilot/how-tos/copilot-cli/customize-copilot/add-custom-instructions)). Since our `CLAUDE.md` is a one-line import of `AGENTS.md`, you get the same rules once.

### OpenAI Codex

1. In WPILib VS Code, install the extension `openai.chatgpt` (search "Codex"). The current version installs fine on 1.105.1 (26.1021.22027, checked 2026-11-05). [Vendor docs](https://learn.chatgpt.com/docs/ide).
2. Sign in with your ChatGPT account when the extension asks.
3. Codex runs hooks only from a **trusted** project. When Codex asks whether to trust the repo, say yes, then see section 4.

Codex reads `AGENTS.md` and nothing else. Codex has no repo-level commands, so OpenSpec appears as skills you call with `$openspec-explore`, `$openspec-apply`, and so on.

### Claude Code

1. In WPILib VS Code, install the extension `anthropic.claude-code`. It needs VS Code 1.94 or newer; current version 2.1.318 installs on 1.105.1 (checked 2026-11-05). [Vendor docs](https://code.claude.com/docs/en/ide-integrations).
2. Sign in with an Anthropic account that has a paid plan or a Console account.

Anthropic's consumer terms require you to be 18. If you are under 18, talk to a mentor. Do not use someone else's account.

Claude Code reads `AGENTS.md` only when a repo has no `CLAUDE.md`. Our repos have a `CLAUDE.md` that contains `@AGENTS.md`, which is the pattern [Anthropic documents](https://code.claude.com/docs/en/memory). Do not delete that file, and do not add a `CLAUDE.local.md` that replaces its rules.

### One extension at a time

All three extensions install side by side without errors in WPILib VS Code. Even so, enable **one AI extension per workspace**. Two agents editing the same files at once is a conflict no setting prevents. Disable the others for this workspace from the Extensions view.

---

## 3. Node and the OpenSpec CLI

OpenSpec is the spec tool from [the SDD loop](the-loop.md).

1. Install Node.js from [nodejs.org](https://nodejs.org/en/download), choosing an **LTS** release of version 22 or newer. OpenSpec 1.10.0 needs Node 20.19 or newer ([npm registry entry, `engines` field, checked 2026-11-05](https://registry.npmjs.org/@fission-ai/openspec/1.10.0)). The Copilot CLI's npm install needs 22, so 22 covers both. Close and reopen the terminal afterwards.
2. Install the pinned CLI version, in the terminal inside WPILib VS Code:

    ```bash
    npm install -g @fission-ai/openspec@1.10.0
    ```

    Use exactly `1.10.0`, not `latest`. The files committed in the repos were generated by that version, and a newer release (1.16.0 today) needs Node 22.12 and changes the output.

3. Turn off OpenSpec's usage reporting ([vendor README](https://github.com/Fission-AI/OpenSpec)):

    **macOS:**

    ```bash
    echo 'export OPENSPEC_TELEMETRY=0' >> ~/.zshrc
    ```

    **Windows (PowerShell):**

    ```powershell
    setx OPENSPEC_TELEMETRY 0
    ```

    Open a new terminal afterwards so the setting takes effect.

4. Check it:

    ```bash
    openspec --version
    ```

    It must print `1.10.0`.

!!! warning "Never run `openspec init` or `openspec update` in a team repo"
    The repos already carry every OpenSpec file. Both commands rewrite committed files using *your* personal OpenSpec settings, and CI will fail your PR on the difference. The hooks in section 4 also block an agent from running them. If a command seems missing, see section 6.

`openspec-propose` is in the repos, but you do not use it until you have finished M8. See [AI Use Standard §4](../ai-use-standard.md#4-the-three-tiers).

---

## 4. Hooks

Hooks are small programs your AI tool runs before it acts. They stop things a prompt can only ask the agent not to do. [The Harness](the-harness.md) explains why.

The team repos carry the hook scripts in `.ignite/hooks/`. They are plain Node, so they behave the same on Windows and macOS. **Turning them on is a local step**, using files that are gitignored in the repo. Never commit these files: they would also fire for mentors and inside Copilot.

What they do:

| Hook | Effect |
|---|---|
| `guard.mjs` | Blocks edits to `src/test/` while `.ignite/implementing` exists. Blocks `git push`, `--no-verify`, and `openspec init`/`update`. |
| `session-start.mjs` | Prints your tier when a session starts. |

**These are guardrails against the agent drifting, not against you.** The shell-command check is best effort: a creative command can still write into `src/test/`. CI and code review are what actually enforce the rules.

### Your tier file

Once, write your tier in one line in your home folder. Update it when a mentor signs off a milestone:

**macOS:**

```bash
echo "Green (M6 signed off)" > ~/.ignite-tier
```

**Windows (PowerShell):**

```powershell
Set-Content -Path $HOME\.ignite-tier -Value "Green (M6 signed off)"
```

The session-start hook prints it. If the file is missing it prints `Ignite tier: unknown — ask a mentor`.

### Turn the hooks on, per tool

Run these from the repo root. Create the folder if it does not exist.

**Claude Code and Copilot CLI** share one file, `.claude/settings.local.json`. [Copilot CLI reads it too](https://docs.github.com/en/copilot/reference/hooks-reference) (accessed 2026-11-05).

```json
{
  "hooks": {
    "PreToolUse": [
      {
        "matcher": "Edit|Write|MultiEdit|Bash",
        "hooks": [{ "type": "command", "command": "node .ignite/hooks/guard.mjs" }]
      }
    ],
    "SessionStart": [
      { "hooks": [{ "type": "command", "command": "node .ignite/hooks/session-start.mjs" }] }
    ]
  }
}
```

**Codex** uses `.codex/hooks.json`. [Codex loads project hooks only after you trust the project](https://learn.chatgpt.com/docs/hooks) (accessed 2026-11-05), and you review them with `/hooks` the first time.

```json
{
  "hooks": {
    "PreToolUse": [
      {
        "matcher": "Bash|apply_patch|Edit|Write",
        "hooks": [{ "type": "command", "command": "node .ignite/hooks/guard.mjs" }]
      }
    ],
    "SessionStart": [
      {
        "matcher": "startup|resume",
        "hooks": [{ "type": "command", "command": "node .ignite/hooks/session-start.mjs" }]
      }
    ]
  }
}
```

**Copilot in WPILib VS Code has no hooks.** The chat extension is too old for them. With Copilot there, CI and review are your only backstop, so be stricter with yourself about not letting it touch `src/test/`.

If a hook blocks something and you think it is wrong, stop and tell a mentor. Do not edit the hook files.

### The marker

The test block works off a marker file. It is local, and the repos gitignore it.

1. When you reach **rung 4, Apply** in [the loop](the-loop.md), create the marker. From the repo root:

    **macOS:**

    ```bash
    touch .ignite/implementing
    ```

    **Windows (PowerShell):**

    ```powershell
    New-Item .ignite/implementing -ItemType File
    ```

2. While it exists, the hook blocks any edit under `src/test/`.
3. After **rung 5, Green**, delete it:

    **macOS:**

    ```bash
    rm .ignite/implementing
    ```

    **Windows (PowerShell):**

    ```powershell
    Remove-Item .ignite/implementing
    ```

If the test is wrong, that is a rung 3 problem. Delete the marker only when a mentor agrees you are going back to Red.

---

## 5. Verify it works

Open a fresh clone of `Robot-2026` in WPILib VS Code on the `development` branch (see [Repo setup](../issues-and-projects.md#repo-setup) for why that is the default). Do each check in order.

1. **Toolchain.** In the terminal inside WPILib VS Code:

    ```bash
    openspec --version
    openspec list
    ./gradlew build
    ```

    Success: `1.10.0`, a list (possibly empty) with no error, and `BUILD SUCCESSFUL`. On Windows use `.\gradlew build`.

2. **Rules are read.** Ask your tool: *"What are this repo's coding standards and AI-use rules?"* Success: the answer mentions `AGENTS.md`, the coding standards, and the AI-use tiers, and says nothing edits `src/test/` once implementation starts. If the answer is generic, the tool is not reading the file. See section 6.

3. **OpenSpec files are found.** Run the explore command for your tool:

    | Tool | Type this |
    |---|---|
    | Claude Code | `/opsx:explore` |
    | Copilot | `/opsx-explore` |
    | Codex | `$openspec-explore` |

    Success: the tool finds it and starts an explore session. Close it without doing anything. If the tool says the command does not exist, see section 6.

4. **The test block works.** This check does not apply to Copilot in WPILib VS Code, which has no hooks; there, CI and review are the backstop. For every other setup, turn the hooks on as in section 4, then:

    1. Create the marker, from the repo root:

        **macOS:**

        ```bash
        touch .ignite/implementing
        ```

        **Windows (PowerShell):**

        ```powershell
        New-Item .ignite/implementing -ItemType File
        ```

    2. Ask your agent: *"Add a comment to a file under `src/test/`."* If the repo has no `src/test/` yet, ask it to create a new file there instead.
    3. Success: the edit is **blocked**, with a message starting `Blocked: editing ... src/test/`. If the agent edits the file, the hooks are not on. Re-check section 4.
    4. Delete the marker (commands in section 4) and ask again. Now the edit goes through. Discard it: `git checkout -- src/test` for an edited file, or delete the new file.


When all four pass, you are ready for M7.

---

## 6. When something is wrong

| Symptom | Fix |
|---|---|
| Help > About is not 1.105.1, or you are not sure which VS Code you opened | Close it. Open the VS Code from the WPILib install. The version number will tell you. |
| The tool's answer in check 2 is generic and ignores `AGENTS.md` | Make sure you opened the repo **folder** (File > Open Folder), not a single file. For Claude Code, confirm `CLAUDE.md` exists in the repo root and contains `@AGENTS.md`. For Copilot, confirm **Chat: Use Agents Md File** is on. |
| `/opsx` or `$openspec` commands are missing | The repo may not be seeded yet, or you are on the wrong branch. Check you are on `development`. If it is still missing, tell a mentor. Do not run `openspec init` or `openspec update`. |
| Each OpenSpec skill shows up two or three times in the list | Expected. Copilot reads three skill folders, and the copies differ only in how you call them. Ignore the extras. |
| `openspec` is not found after install | Close and reopen the terminal. If it still fails, `npm config get prefix` shows where npm put it. That folder's `bin` (or the folder itself on Windows) must be on your PATH. |
| `openspec --version` prints something other than `1.10.0` | `npm install -g @fission-ai/openspec@1.10.0` again. |
| The OpenSpec drift check is red on your PR | You changed generated OpenSpec files, probably by running `openspec update`. Undo it. For each file the check lists, run `git checkout -- <path>`, and delete any new files it lists. Push again. Mentors fix real changes (see below). |
| A hook blocked something you did not expect | Stop. Tell a mentor. Do not edit `.ignite/`. |
| In Copilot CLI, every tool call is denied | A broken hook command is denied outright in Copilot CLI ([vendor docs](https://docs.github.com/en/copilot/reference/hooks-reference)). Check `node --version` works in that terminal, and that you are in the repo root. |
| Codex ignores the hooks | The project is not trusted yet, or you have not approved the hooks. Trust the project, then run `/hooks`. |

---

## For mentors

The OpenSpec tool files (`.claude/`, `.agents/`, `.github/skills/`, `.github/prompts/opsx-*`, `openspec/config.yaml`) change in exactly one way:

1. **Bump:** edit `openspecVersion` (or any other field) in `.ignite/openspec-recipe.json`.
2. **Regenerate:** `node .ignite/openspec-regen.mjs`. It runs OpenSpec in a throwaway home, so your personal settings cannot leak in.
3. **Commit** the result. The `OpenSpec tool files` check runs the same regeneration in CI and fails on any difference.

To test locally, run `node .ignite/openspec-regen.mjs --check` on a **committed** tree. It compares against git, so uncommitted changes in the tool paths make it exit 1 by design.

Those paths, and `.ignite/` and `AGENTS.md`, are CODEOWNERS-protected, so a change needs a mentor's review. That includes `.github/prompts/`, so edits to Robot-2026's `code-review-prompt.md` need mentor review too.

When the pinned version moves, update the version on this page (section 3) and the Node floor for it. Check the new version's `engines` field on npm.

Seeding a repo, the rulesets that make the check required, and the rest of the repo-level configuration are covered in [Repo setup](../issues-and-projects.md#repo-setup).
