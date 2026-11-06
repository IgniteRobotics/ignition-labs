# Issues & Projects

How Ignite tracks programming work: one GitHub issue per change, one board for all of it.

This covers the managed repos only: `Robot-2026`, `Phoenix-2026`, `flashpoint`, and `IgniteLib`. If you have not reached M7 yet, you still fork `ignition-labs` and send pull requests to your own fork, and issues are not part of that flow. See [the git workflow](git-workflow.md).

---

## Why every change starts with an issue

Three records describe a change, and each answers a different question:

| Record | Answers | Lives in |
|---|---|---|
| The **issue** | Why are we doing this? | GitHub Issues |
| The **spec** (an OpenSpec change) | What must be true afterward? | `openspec/` in the repo |
| The **pull request** | How did we do it? | GitHub Pull Requests |

With all three linked next to the code, a new student or an agent can open any issue, follow it to its branch, spec, and pull request, and see where it sits on the board without asking anyone. This applies to all development, not only changes with a spec. See [the loop](ai/the-loop.md) for where the issue sits in spec-driven development.

---

## Ready

An issue is **Ready** only when it states the problem it solves. "Add a jam-recovery routine" is a solution. "The indexer stalls when a game piece enters at an angle, and the driver has to power-cycle the robot" is a problem.

A lead moves an issue to Ready by hand, after checking that rule. Do not start work on an issue that is not Ready. Ask a lead, or add the missing problem statement yourself and ask them to look again.

---

## Status

All programming issues from the four repos appear on one organization Project, **Ignite Programming**. Every card has a Status. The Status tells you where the code is and who moves the card.

| Status | Where the code is | What moves the card |
|---|---|---|
| Backlog | Nowhere. The issue was just opened. | Automatic: a new issue is added to the board. |
| Ready | Nowhere. The issue states its problem and has acceptance criteria. | A lead, by hand. |
| In progress | Your branch, `feature/<num>-<name>` | You, by hand, when you create the branch. GitHub does not react to branch creation. |
| In review | A pull request into `development`, ready for review | Adding `Closes #<num>` when you mark the pull request ready for review. If the board does not do this itself, move it by hand. |
| Done | Merged into `development` | Automatic: merging the pull request closes the issue, and the board marks it Done. |

You touch your own card twice: Ready to **In progress** when you branch, and (if the board does not do it for you) **In progress** to **In review** when you mark the pull request ready for review. Everything else happens on its own or belongs to a lead.

### Size and Release

Two more fields sit on each card.

- **Size** is `XS`, `S`, `M`, `L`, or `XL`. Set it when the issue is Ready, so standup can see how much work is on the board.
- **Release** names the event or version the work ships in. For the robot repos it matches the tag suffix: `gwinnett` for the tag `release/gwinnett`. For the other repos it is a version such as `2026.1.0`. It starts empty and a lead fills it in.

---

## Branches, pull requests, and closing the issue

- **Branch name:** `feature/<num>-<name>`, where `<num>` is the issue number. Issue 42 about an indexer jam becomes `feature/42-indexer-jam`.
- **Pull request body:** include `Closes #<num>`. GitHub only acts on that line when the pull request merges into the repo's **default branch**, which is why `development` is the default branch in every managed repo and `main` is not. A fresh clone also lands on the branch you branch from.
- **Draft pull request as the spec review.** For a change with an OpenSpec spec, open a **draft** pull request into `development` as soon as the spec is committed, before any implementation code. Reviewers read the spec there. Leave `Closes #<num>` out of the draft and add it when you mark the pull request ready for review ([Team Gitflow](team-gitflow.md) step 9), so the card stays in **In progress** during the spec review. There is no separate "spec review" Status.
- **Archive last.** Run `openspec archive` as the last commit on the branch, after the mentor has approved the spec and the code. The mentor checks that commit (all tasks checked, `openspec validate` passing, `openspec/specs/` updated) before merging.
- **Failures after merge open a new issue.** If you find a problem after merge, whether at the field or in a log replay, open a new issue and link the old one. Do not reopen the old one. This matches the loop: a replay failure is a new `MODIFIED` requirement, not an edit to a finished one.

See [Team Gitflow](team-gitflow.md) for the full flow and rules.

---

## Hotfixes

At an event you do not need an issue first. If something breaks ten minutes before you queue, fix it on `hotfix/<comp>` and deal with the paperwork later.

Afterwards:

1. Open an issue for the fix and give it the `hotfix` label.
2. When `hotfix/<comp>` merges into `main`, **close the issue by hand**. `main` is not the default branch, so `Closes #<num>` does not fire there.
3. Merge `main` back into `development`, as [Team Gitflow](team-gitflow.md) describes.

---

## Repo setup

This section is for the people who set up repos and the board, not for every contributor. Labels are `feature`, `bug`, `hotfix`, and `spec`.

A script configures the managed repos and the Project from a checked-in config file, `tools/team_github.toml`. For each repo it sets `development` as the default branch, reconciles the branch rulesets it owns (`dev-main-pushes` and `dev-main-pr-gate`), creates the labels, and links the repo to the Project. Once, for the org, it creates the Project with its fields. It never deletes anything.

Prerequisite: the `gh` CLI, signed in with permission to manage Projects.

```bash
gh auth refresh -s project
```

Run it from a clone of this repository:

```bash
poetry run python -m tools.team_github
```

1. **Dry-run first.** With no flags the script only prints what it would change. Read all of it. For a ruleset that differs from the config, it prints the field-level difference.
2. **Apply** only after the dry-run reads correctly:

    ```bash
    poetry run python -m tools.team_github --apply
    ```

3. **Re-run to check for drift.** A configured org is converged only when the run exits 0 **and** prints no PENDING section. A pending check is left out of the plan, so a run can exit 0 while a required check is still waiting to be added.
4. **Close the old Projects** once you have looked at the migrated board:

    ```bash
    poetry run python -m tools.team_github --apply --close-old
    ```

    This closes the old "Flashpoint" and "IgniteLib" Projects. It does not delete them.

A required status check that no workflow has reported yet would block every pull request in that repo, so the script lists such a check as *pending* and leaves it out until the workflow has run once on `development`.

**Adding a repo:** add an entry for it to `tools/team_github.toml`, then do the dry-run and apply steps above.

### Project automations (by hand)

GitHub Project automations are the Project's built-in workflows. GitHub has no API that turns them on; they are switched on only in the Project's web UI ([GitHub docs](https://docs.github.com/en/issues/planning-and-tracking-with-projects/automating-your-project/using-the-built-in-automations), accessed 2026-11-05). The script cannot set them, so a person does this once.

Open **Ignite Programming**, then **Settings**, then **Workflows**, and turn on:

- [ ] **Auto-add to project**, one rule per managed repo, so every new issue lands in Backlog. The org is on GitHub's Team plan, which allows five auto-add workflows ([GitHub docs](https://docs.github.com/en/issues/planning-and-tracking-with-projects/automating-your-project/adding-items-automatically), accessed 2026-11-05). The docs do not say whether that limit counts per Project or per org, and the old Flashpoint and IgniteLib Projects hold one rule each. If GitHub refuses a rule, delete the old Projects' auto-add rules first.
- [ ] **Item closed**, set Status to Done.
- [ ] **Pull request merged**, set Status to Done.
- [ ] **Pull request linked to issue**, set Status to In review. GitHub's automation docs do not list this one, but it is in the Workflows menu of the team's existing Projects.
- [ ] **Auto-archive items**, if you want old Done cards to leave the board.

Check one by opening a test issue and watching it appear in Backlog.
