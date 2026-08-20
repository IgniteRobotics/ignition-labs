# Task 3: M0's command table gains a VS Code column

This task adds a third column to M0's eight-command allow-list, mapping each command to the
Source Control panel the student has had open the whole time; restates the
`rebase`/`reset`/`cherry-pick`/force-push exclusion for the menus, because this change makes
those reachable by clicking; points at the team gitflow page; and adds two Definition of Done
items.

**The riskiest step in this plan is step 3.** The menu paths below are written from knowledge
of VS Code, **not from clicking them in the WPILib 2026 build.** They are a starting point to
verify, not content to paste. Where this plan and the running application disagree, the
application is right and this plan was wrong. A mapping table nobody has clicked is a mapping
table that is wrong in front of a Track A student in their first session.

**Files:**
- Modify: `docs/programming/milestones/m0-tooling-and-git.md` — four separate edits

**Consumes:**
- **Task 1, landed.** Steps 4 and 6 add relative links to `docs/programming/team-gitflow.md`,
  and `mkdocs build --strict` fails on a link whose target does not exist. The link text is
  written out below — the dependency is on task 1's file existing, not on reading it.
- The extension names from task 2, for the Definition of Done item. Written out below; task 2
  does not have to have landed for this task's edits to be correct.

**Produces:** nothing later tasks depend on.

**Lane:** `m0doc`

---

- [ ] **Step 1: Leave the model section alone**

Not a no-op — a constraint. The section `### Git, as a concept before a command`, and its
opening line *"Do not start with commands. Start with the model,"* are **unchanged** and
still come first. That framing is what makes the rest of the milestone work.

Confirm you have not touched it:

```bash
grep -n "Do not start with commands" docs/programming/milestones/m0-tooling-and-git.md
```

Expected: exactly one match, the original line.

- [ ] **Step 2: Replace the command table**

Find the section beginning `### The commands you actually need` and replace everything from
that heading through the end of the `**Not on this list:` paragraph with:

````markdown
### The commands you actually need

Deliberately narrower than all of git. This mirrors a pattern used by other FRC teams: hand
rookies an explicit allow-list rather than the whole tool.

The third column is the same command, done by clicking. Neither one is the "real" way — the
panel is running exactly these commands. Learn them as one thing, because when something goes
wrong you will need to describe it to a mentor, and "the checkmark is greyed out" is much
harder to help with than "`git commit` says nothing is staged."

| Command | What it does | In VS Code |
|---|---|---|
| `git clone <url>` | Copy a repository to your machine, once | `Ctrl+Shift+P` → **Git: Clone** |
| `git checkout -b <name>` | Make a new branch and switch to it | Branch name in the status bar (bottom left) → **Create new branch** |
| `git status` | What have I changed? Run this constantly | The Source Control panel's file list **is** `git status` |
| `git add -u` | Stage the files you have modified | **+** on the **Changes** header |
| `git commit -m "message"` | Save a snapshot with a note | Type the message in the box, then **✓ Commit** |
| `git push` | Send your commits to GitHub | **Sync Changes**, or the ⟳ in the status bar |
| `git fetch --all` | Get everyone else's commits, without applying them | Source Control **⋯** → **Fetch** → **Fetch From All Remotes** |
| `git merge origin/main` | Apply main's changes to your branch | Source Control **⋯** → **Branch** → **Merge** |

**Not on this list: `rebase`, `reset`, `cherry-pick`, `force push`.** They are useful and you
will learn them eventually. They can also destroy work irrecoverably, and you should not meet
them before you can recover from them. If you think you need one, ask a mentor.

**That exclusion applies to the menus too.** The Source Control **⋯** menu contains `rebase`,
`reset` and force-push, and a menu item destroys work exactly as thoroughly as a typed
command — faster, in fact, because you did not have to think about spelling it. The list
above is the whole allow-list, whichever way you drive it.
````

- [ ] **Step 3: Click through every path in that table — do not skip this**

Open the **WPILib 2026 VS Code**, open `projects/m0-onboarding` as a folder, and confirm each
of the eight right-hand cells actually names something that exists, where the table says it
is. VS Code moves menu items between releases and the WPILib build is not always current.

For each row, record what you found:

| Row | Confirmed as written? | Actual path if different |
|---|---|---|
| `git clone` | | |
| `git checkout -b` | | |
| `git status` | | |
| `git add -u` | | |
| `git commit -m` | | |
| `git push` | | |
| `git fetch --all` | | |
| `git merge` | | |

**Edit the table to match what you found.** Ship the application's reality, not this plan's
guess. Include the filled-in table above in your task report.

While you are in there, confirm the hazard the step-2 note claims: open the Source Control
**⋯** menu and check that `rebase`, `reset` and a force-push option are in fact reachable. If
they are not present in this build, the warning is wrong and must be softened rather than left
as a scary claim a student cannot verify.

- [ ] **Step 4: Add the team-gitflow pointer**

M0's `### Pull requests and CI` section currently ends with the M7 note. Find:

```markdown
`Robot-2026` runs a second workflow that AI-reviews every pull request diff. You meet
it at M7 — it is not part of `ignition-labs`.
```

and append immediately after it, before the `## Project` heading:

````markdown
### How the team works, and when you join it

Everything above is how *you* work right now: fork, branch, one pull request. The team
itself, on the competition robot's code, works differently — `main` is competition-ready code
only, features merge into a `development` branch first, and competitions get `hotfix`
branches and release tags.

You do not do any of that yet, and you do not have a repository to do it in. But you will
clone `Robot-2026` read-only at M2, and you will find a `development` branch that nothing in
[the git workflow](../git-workflow.md) mentions. Read [the team gitflow](../team-gitflow.md)
now so that is recognition rather than confusion.

It becomes what you actually do at **M7**, when clearing the Kickoff Readiness Gate gets you
commit access.
````

- [ ] **Step 5: Add the two Definition of Done items**

Find the Definition of done list and add two items. Placement is by kind rather than both at
the end of the toolchain block: the extensions item belongs with the environment checks, and
the diagram item belongs with the "can you explain it" checks. The list after this edit reads,
in full:

```markdown
## Definition of done

- [ ] WPILib VS Code installed; `./gradlew build` succeeds on your machine
- [ ] The simulator launches and you can identify the Robot State panel
- [ ] GitLens, Git Graph and Live Share are installed, and you can find the Source Control panel
- [ ] A merged pull request exists under your GitHub account
- [ ] You ran `spotlessApply` on `FormatMe.java`, read the diff, reverted it, and can say what the formatter changed
- [ ] You can explain what a branch is without using the word "branch"
- [ ] You can point at `development` on the team gitflow diagram and say why it exists
- [ ] You can point at the first line of a stack trace that refers to our code
- [ ] Copilot or equivalent is disabled in your editor
```

The Copilot item is unchanged and stays last. Nothing in this task weakens it — M0 is 🔴 Red
tier and the three new extensions are git tooling only.

- [ ] **Step 6: Add the resource line**

In the `## Resources` list, immediately before the final `**Ignite:**` entry:

```markdown
- **Ignite:** `docs/programming/team-gitflow.md` — how the team works on the competition robot, and when that becomes how you work
- **Ignite:** `ignition-labs/.github/workflows/student-check.yml` — the CI your M0 pull request must pass
```

- [ ] **Step 7: Verify the document**

```bash
poetry run mkdocs build --strict
poetry run python tools/site_guard.py _site
```

Expected: `rc=0` from both, and `site guard OK: _site`.

`--strict` is what CI runs (`pages.yml`). It turns MkDocs' unresolved-link warning into a
failure — which is the check that catches the two links added in steps 4 and 6 pointing at
nothing. If it fails that way, task 1 has not landed and this task should not have started.

Check the structural claims that a build cannot:

```bash
grep -c '^- \[ \]' docs/programming/milestones/m0-tooling-and-git.md
grep -n 'Copilot or equivalent is disabled' docs/programming/milestones/m0-tooling-and-git.md
grep -n 'team-gitflow' docs/programming/milestones/m0-tooling-and-git.md
```

Expected: `9` DoD items (seven original plus two); the Copilot line present and unchanged;
`team-gitflow` appearing twice — once in step 4's pointer, once in step 6's resource line.

- [ ] **Step 8: Read it as a Track A student would**

Read `### The commands you actually need` through `### How the team works, and when you join
it` start to finish, out loud if you can stand it. You are checking one thing: **does the
milestone still open with the model, or does it now open with tooling?** If the three-column
table and the gitflow pointer have buried *"A commit is a save point"*, this task overshot and
the fix is to cut from the new material, not from the old.

Say in your task report which way it read.

- [ ] **Step 9: Commit**

```bash
git status --short
git add docs/programming/milestones/m0-tooling-and-git.md
git commit -m "Map M0's git allow-list onto the Source Control panel

The milestone taught eight commands and never acknowledged the panel the
student has had open the entire time, which implicitly claims the buttons
are some other thing. They are not -- the panel runs exactly these
commands. Same table, third column, both taught as one thing.

'Do not start with commands, start with the model' is unchanged and still
comes first. The change is confined to the table that follows it.

The exclusion note had to be restated for the menus, because this change
introduces that hazard: rebase, reset and force-push are reachable from the
Source Control overflow menu, and a menu item destroys work faster than a
typed one since you did not have to think about spelling it.

Every path in the third column was clicked in the WPILib 2026 VS Code
before shipping. A mapping table nobody has clicked is wrong in front of a
Track A student in their first session.

Two DoD items, placed by kind rather than appended: the extensions check
sits with the environment checks, the diagram check with the 'can you
explain it' checks. The Copilot item is untouched -- M0 is Red tier and the
three new extensions are git tooling only.

Co-Authored-By: Claude Opus 5 <noreply@anthropic.com>"
```
