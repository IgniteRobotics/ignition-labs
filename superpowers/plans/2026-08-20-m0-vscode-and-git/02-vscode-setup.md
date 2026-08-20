# Task 2: VS Code extensions — setup prose and the workspace prompt

This task names the three extensions the team standardizes on, puts them in `SETUP.md` where
a student is already installing things, and ships a `.vscode/extensions.json` so VS Code
offers them on first open of the M0 project.

Two things are easy to get wrong here. The first is **renumbering `SETUP.md`** — its steps
are cross-referenced by number from `git-workflow.md` and from inside `SETUP.md` itself, so
the new section is `1b` rather than a new `2`. The second is **scope creep into the AI
posture**: M0–M2 are Red tier, and the extension list is git tooling only.

**Files:**
- Modify: `docs/SETUP.md` — insert a new `## 1b.` section between step 1 and step 2
- Create: `projects/m0-onboarding/.vscode/extensions.json`
- Modify: `projects/m0-onboarding/.gitignore`

**Consumes:** task 1, landed. Not for its content — this task's prose is self-contained — but
because step 4 adds a relative link to `docs/programming/team-gitflow.md`, and
`mkdocs build --strict` fails on a link whose target does not exist. Without task 1 this task
cannot run its own gate.

**Produces:** the extension names and marketplace IDs that task 3's Definition of Done item
refers to. Task 3 already has that string written out; nothing needs to be passed.

**Lane:** `setup`

---

- [ ] **Step 1: Create the workspace recommendation file**

Spec §4.2 has students open the milestone directory as their VS Code workspace, and M0 is by
definition the first one. Extensions install at user level, so this prompt is only useful
once — it goes in M0 and **nowhere else**. Copying it into M1–M8 would re-prompt about
extensions the student already has.

Create `projects/m0-onboarding/.vscode/extensions.json`:

```json
{
  "recommendations": [
    "eamodio.gitlens",
    "mhutchie.git-graph",
    "ms-vsliveshare.vsliveshare"
  ]
}
```

- [ ] **Step 2: Scope `.vscode` in the M0 project's `.gitignore`**

The WPILib extension writes a generated `.vscode/settings.json` the first time a student
opens the project. Nothing currently ignores it, so it would show up in `git status` and
eventually in somebody's pull request. Track the recommendations, ignore the rest.

Append to `projects/m0-onboarding/.gitignore`:

```gitignore
.vscode/*
!.vscode/extensions.json
```

The file after this edit reads, in full:

```gitignore
build/
bin/
.gradle/
logs/
ctre_sim/
networktables.json
simgui-window.json
*.log
.vscode/*
!.vscode/extensions.json
```

- [ ] **Step 3: Verify the ignore rule does what it claims**

An ignore rule with a negation is easy to write backwards. Check both halves:

```bash
cd projects/m0-onboarding
git check-ignore -v .vscode/settings.json; echo "settings rc=$?"
git check-ignore -v .vscode/extensions.json; echo "extensions rc=$?"
cd ../..
```

Expected: `settings rc=0` with the `.vscode/*` rule printed (it **is** ignored), and
`extensions rc=1` with no output (it is **not** ignored, so it can be committed).

- [ ] **Step 4: Add step 1b to `SETUP.md`**

Insert between the end of step 1 and the `## 2. Verify Java` heading. Find:

```markdown
This installs a bundled JDK 17, a bundled VS Code, and the WPILib extension.
Use the VS Code it installs — not one you already had. The bundled one knows
where the JDK is.

## 2. Verify Java
```

and replace it with:

````markdown
This installs a bundled JDK 17, a bundled VS Code, and the WPILib extension.
Use the VS Code it installs — not one you already had. The bundled one knows
where the JDK is.

## 1b. Install three extensions

The installer already gave you the WPILib extension and Java language support. Add three
more. All three exist for the same reason: git is much easier to understand when you can
see it.

| Extension | Marketplace ID | What it gives you |
|---|---|---|
| **GitLens** | `eamodio.gitlens` | The name of whoever last changed a line, shown on the line, with the commit message. "Why is this here?" becomes a question you can answer yourself. |
| **Git Graph** | `mhutchie.git-graph` | Your branches and merges, drawn. This is the same picture as [the team gitflow](programming/team-gitflow.md) diagram, except it is your actual history. |
| **Live Share** | `ms-vsliveshare.vsliveshare` | A mentor can join your editor and help without standing behind you, including from home. |

Install them from the Extensions view — `Ctrl+Shift+X` (`Cmd+Shift+X` on macOS), paste the
ID, click **Install**. Or open `projects/m0-onboarding` as a folder and VS Code will offer
all three at once; the repository ships the recommendation.

**None of these is required.** The build, the deploy, and every CI check work without them.
If you are on a locked-down laptop that will not install extensions, you can still complete
every milestone — say so to a mentor rather than quietly falling behind.

> **⚠️ Live Share shares your screen's contents, not just a file.** Whoever joins can see
> your files and your terminal. A mentor starts the session, you accept it, and **you end it
> when you are done** — check that the Live Share panel shows no active session before you
> close your laptop.

**Not on this list: Copilot, or any other AI autocomplete.** M0 through M2 are 🔴 Red tier
and inline AI completion is turned off for all three. If your editor came with it, disable it
now — see [the AI use standard](programming/ai-use-standard.md).

## 2. Verify Java
````

Note the heading is **`1b`, not a new `2`.** `git-workflow.md` points at "step 3" and
`SETUP.md`'s own step 6 refers to itself by number. Renumbering silently breaks both.

- [ ] **Step 5: Verify the cross-references still resolve**

Both links added in step 4 are relative from `docs/SETUP.md`, and `docs-links.yml` runs
lychee over every markdown file with `fail: true`.

```bash
test -f docs/programming/team-gitflow.md && echo "team-gitflow link resolves"
test -f docs/programming/ai-use-standard.md && echo "ai-use-standard link resolves"
```

Expected: both print. If the first does not, **task 1 has not landed and you should not be
running this task yet** — stop and check the task graph in `00-plan.md`.

Confirm the numbering was not disturbed:

```bash
grep -n '^## [0-9]' docs/SETUP.md
```

Expected, in order: `1.`, `1b.`, `2.`, `3.`, `4.`, `5.`, `6.`, `7.` — with the original
numbers unchanged.

- [ ] **Step 6: Build the site**

```bash
poetry run mkdocs build --strict
poetry run python tools/site_guard.py _site
```

Expected: `rc=0` from both, and `site guard OK: _site`.

`--strict` is what CI runs (`pages.yml`). It turns MkDocs' unresolved-link warning into a
failure, which is exactly the check that catches step 4's new link to `team-gitflow.md`
pointing at nothing. If it fails that way, task 1 has not landed.

- [ ] **Step 7: Confirm no Gradle project was disturbed**

This task adds a dotfile to a Gradle project directory. It must not have changed the build
or the test manifest.

```bash
poetry run python tools/tests_lock.py verify; echo "rc=$?"
git status --short
```

Expected: `rc=0`, and `git status` showing only `docs/SETUP.md`,
`projects/m0-onboarding/.gitignore`, and `projects/m0-onboarding/.vscode/extensions.json`.

`extensions.json` must **not** appear in `tests.lock` — it is not under any `src/test/` and
it is a file students may edit.

- [ ] **Step 8: Commit**

```bash
git status --short
git add docs/SETUP.md projects/m0-onboarding/.gitignore \
        projects/m0-onboarding/.vscode/extensions.json
git commit -m "Name the three extensions we standardize on, and prompt for them

SETUP.md installed a toolchain and never said which extensions the team
actually uses. GitLens, Git Graph and Live Share, all chosen for the same
reason: git is much easier to understand when you can see it. Git Graph in
particular draws the student's own history in the same shape as the team
gitflow diagram, which is what connects that drawing to something real.

Explicitly not GitHub Pull Requests and Issues. M0's 'respond to at least
one review comment' stays a github.com task -- one surface for reviewing a
pull request, not two, at the milestone where the point is learning to
navigate the one the team actually uses.

None of the three is required to build, deploy, or pass CI. Spec 12.1
forbids a skeleton whose setup assumes a tool, and a student on a
locked-down laptop still has to be able to finish.

Live Share gets consent guidance because it shares files and terminal, not
a file. This repository's privacy posture is handle-only by design and a
home directory in a shared terminal undoes that in a way contributors/
deliberately does not.

extensions.json ships in M0 only. Extensions install at user level, so the
prompt is useful exactly once, and M0 is by definition the first project
opened. The .gitignore gains .vscode/* with extensions.json negated so the
WPILib extension's generated settings.json does not follow it into git --
both halves verified with git check-ignore rather than assumed.

Numbered 1b rather than a new step 2: git-workflow.md points at 'step 3'
and SETUP.md's step 6 refers to itself by number.

Co-Authored-By: Claude Opus 5 <noreply@anthropic.com>"
```
