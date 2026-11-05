# GitHub Issues & Projects for team development — design record

**Date:** 2026-11-04
**Companion change:** an SDD/AI setup guide, designed separately (same session, second record)
**Governing sections touched:** `teaching-repo-spec.md` §9.8–§9.9 (M7/M8 real-repo and mentor-verifies lines); **not** §13
**Decided with:** Josh, in a brainstorming session. Anything marked *(builder's call)* or *(Claude's call)* was not decided by him and may be overridden in review.

---

## 1. Intent

Ignite is moving all team development onto GitHub Issues and one GitHub Project. The driver is spec-driven development: with SDD, the **issue is the why, the OpenSpec change is the what, and the PR is the how**, and having all three linked on GitHub next to the code is what lets an agent or a new student pick up work cold. It applies to all development, not only SDD changes.

These docs become **canonical** for the team's programming process and **replace the Notion programming docs**. `team-gitflow.md` stops being "the curriculum's copy" of a Notion page.

Success: a student or agent can open any issue in a managed repo, follow it to its branch, its spec, and its PR, and see where it is on one board, without asking anyone; and a new repo can be wired into that system by adding one line to a config file and running a script.

---

## 2. Scope

**Managed repos (Josh):** `Robot-2026`, `Phoenix-2026`, `flashpoint`, `IgniteLib` — all in the `IgniteRobotics` org, all four already have a `development` branch, all four currently default to `main`.

**Ignored (Josh):** every other org repo. Explicitly unaffected: `ignition-labs` (this repo) and `ignite-training-solutions` (teaching repos), and the pre-M7 student-fork flow described in `git-workflow.md`. Students before M7 fork and PR into their own fork; issues are not part of that flow.

**Out of scope:** the managed repos' code, except the workflow files in §3.8; editing the Notion pages themselves (Josh does that by hand — a pointer back to the site); GitHub's own *Milestones* feature (the word already means M0–M9 to every student).

---

## 3. Decisions

### 3.1 `development` becomes the default branch (Josh)

GitHub only honours closing keywords (`Closes #42`) when a PR merges into the **default** branch. With `main` as default, every feature PR's closing link would sit idle until the next `development → main` merge. With `development` as default, a feature merge closes its issue, and a fresh clone lands on the branch people branch from anyway. `main` remains protected and only takes PRs from `development` and `hotfix/*`. The gitflow shape in `team-gitflow.md` is otherwise unchanged.

### 3.2 One org-level Project for the programming team (Josh)

GitHub Projects (v2) are owned by an org or user, never by a repo, so the real choice was one Project spanning repos vs. one per repo. One wins: work crosses repos (an `IgniteLib` change plus the `Robot-2026` change that consumes it), standup needs one board, and fields/automation are defined once. Per-repo views inside the Project give repo-only boards for free.

**Fields:**

| Field | Values | Source |
|---|---|---|
| Status | Backlog → Ready → In progress → In review → Done | Josh |
| Size | XS, S, M, L, XL | carried over — the only non-Status field the existing Projects actually use |
| Release | competition names matching tag suffixes (`gwinnett`, `albany`), or versions for non-robot repos (`2026.1.0`) | *(Claude's call)* — named to line up with `release/<comp>` tags; replaces an "Event" field floated earlier |

Priority, Estimate, Iteration, Start/Target date exist on the current Projects and are empty on every item; they are not carried over.

**Status, against the branching strategy:**

| Status | Where the code is | What moves it |
|---|---|---|
| Backlog | nowhere | issue opened (auto-add) |
| Ready | nowhere; scoped, has acceptance criteria / a stated problem | a lead, by hand |
| In progress | `feature/<num>-<name>` | the assignee, by hand, when they create the branch *(Claude's call: manual — nothing in GitHub fires on branch creation)* |
| In review | PR → `development` | PR opened carrying `Closes #<num>` — the built-in "pull request linked to issue" workflow if it exists (verify, §7), otherwise by hand |
| Done | `development` | PR merged → issue closes → Project marks Done |

### 3.3 Issue lifecycle rules (agreed in session)

- **An issue is Ready only when it states the problem it solves.** Ready is the gate a lead applies.
- **Branch names carry the issue number:** `feature/<num>-<name>`, e.g. `feature/42-indexer-jam`.
- **Every feature PR body carries `Closes #<num>`.**
- **A draft PR opens as soon as the OpenSpec change is committed**, before implementation code. That draft is the spec review, and it makes "spec committed before the first implementation commit" visible. *(builder's call)* whether this is its own "Spec review" Status or simply a draft PR within In progress — default to **no extra column**; a draft PR in In progress is enough.
- **A failure found after merge (rung 8 field, rung 9 replay) opens a new issue that links the old one. The old one is not reopened.** This mirrors the loop: a replay failure is a new `MODIFIED` requirement.
- **Hotfixes at an event need no issue first** (degraded-loop table: ten minutes to queue). They are filed afterwards, labelled `hotfix`, and closed **by hand** when `hotfix/<comp>` merges into `main`, because `main` is not the default branch and closing keywords will not fire.

### 3.4 SDD integration (Josh: "both the gitflow and SDD should integrate with issues")

- The issue sits **before rung 2 (propose)**. `proposal.md` links the issue.
- **`openspec archive` happens inside the PR, as its last commit, before merge** (Josh). A merged PR therefore leaves specs and code in sync, and there is no post-merge window where the spec describes a change as pending.
- Rung 11 (archive) in `the-loop.md` moves accordingly: it is the last step *before* merge, not after.

**Curriculum consequence (Claude's call, flagged to Josh, not objected to):** the curriculum currently says a mentor runs `openspec archive` **after** the PR merges. That changes to match. Known sites:

- `docs/programming/milestones/m8-spec-anchored-development.md:8` — the prerequisite "your M7 change archived; the mentor runs `openspec archive` after your PR merges" becomes "your M7 PR merged (which archives it)".
- `docs/programming/teaching-repo-spec.md:604` and `:611` — §9.9 "Given" and "Mentor verifies … runs `openspec archive` after the pull request merges".
- `docs/programming/curriculum-spec.md:364`.
- M7's step list / DoD gains "your change has an issue" and "archive is the last commit of your PR".

**Who runs archive (Claude's call):** the student, as the last commit on their PR, after the mentor has approved the spec and code; the mentor verifies that commit in review (tasks all checked, `openspec validate` passing, `openspec/specs/` updated) before merging. This adds a dated entry to the revision log at the top of `teaching-repo-spec.md`, the way the 2026-08-21/22 entries were recorded.

The builder should grep for `archive` across `docs/` and `projects/m7*`/`projects/m8*` READMEs to catch any site not listed.

### 3.5 Templates live in the org `.github` repo *(Claude's call)*

Issue templates (feature, bug, hotfix) and the team PR template are written once into `IgniteRobotics/.github`, where GitHub uses them as **org-wide defaults** for every repo that has no templates of its own. None of the four managed repos has issue or PR templates today; `ignition-labs` has its own PR template and keeps it.

Side effect, accepted: ignored repos without their own templates also get these as defaults. Harmless.

The **source of truth for the template content lives in this repo** under `templates/github/` so it is reviewed here. *(builder's call)* how it reaches `IgniteRobotics/.github` — the setup script (§3.6) pushing it is the default; a documented manual copy is acceptable.

The team PR template carries: issue link (`Closes #`), what changed, the OpenSpec change path (or "no spec — reason"), AI-use disclosure (same tiers as `ai-use-standard.md` §6), and "I can explain this". The feature template requires the problem statement that makes an issue Ready. The bug template asks for the log / replay that reproduces it. The hotfix template records the event and the `hotfix/<comp>` branch.

`templates_sync.py` currently pairs every file in `templates/` with an inline region in `student-deliverables.md`. *(builder's call)* whether `templates/github/` joins that mechanism or is excluded from it; it must not break the existing check either way.

### 3.6 The setup script (Josh: "either run the gh commands for config changes, or at least provide a script")

Both, in order: a script, then run it only after its dry-run output has been read.

- **Python, in `tools/`, with pytest coverage in `tests/`**, like the other tools. Calls `gh` (CLI and `gh api`); the subprocess runner is injectable so tests never hit GitHub.
- **Config file checked in**, listing managed repos and per-repo settings. Adding a repo = adding an entry. Python 3.11 `tomllib`, so no new dependency *(Claude's call: TOML)*.
- **Dry-run by default; `--apply` to act. Idempotent** — a second run changes nothing, so it doubles as a drift check.
- **Prerequisite:** `gh auth refresh -s project` (Josh's current token already has `project`, `repo`, `read:org`).

What it does, per managed repo:

1. Set default branch to `development`.
2. **Reconcile branch rulesets by name; never delete a ruleset it does not own.** The script *owns* the rulesets named in its config (`dev-main-pushes`, `dev-main-pr-gate`). For an owned ruleset that differs from config, the dry-run prints the field-level diff and `--apply` updates it — visible, therefore not silent. Rulesets not named in config are reported and left alone. Comparison is over the fields the config sets (target, include refs, enforcement, rules, bypass actors), not GitHub's full normalized JSON *(Claude's call)*. Bypass actors: org admin plus the same team `IgniteLib` uses today, resolved to its ID at run time. Current state, observed 2026-11-04:
   - all four have `dev-main-pushes` (blocks deletion, force-push, creation, and updates on `main`+`development`; bypass: org admin + a team)
   - `IgniteLib` also has `dev-main-pr-gate` (PR required, 0 approvals, required check `Build and Check Code Format`), and keeps both as JSON in `.github/rulesets/`
   - `flashpoint` has classic branch protection on `main` requiring `ci-ok`
   **Two changes to `IgniteLib`'s `dev-main-pr-gate` before it becomes the target (Josh, 2026-11-05):** `require_code_owner_review` goes from `false` to **`true`**, so the CODEOWNERS entries the setup-guide change adds on OpenSpec tool files actually block a merge (only PRs touching owned paths are affected — the approval count for everything else stays 0); and the **OpenSpec drift check** (setup-guide design §8.7) is added to each repo's required checks. CODEOWNERS names a mentor team — the builder looks up the team, does not invent one. **Ordering trap:** a required check that no workflow reports yet blocks every PR forever — including the PR that adds the workflow. The script must not require the drift check (or `Phoenix-2026`'s build check) in a repo until that workflow is on `development` and has reported at least once; it detects this and reports the check as *pending*, rather than applying it.
   The target is `IgniteLib`'s pair, with those two changes, applied to all four, with the **required status-check names per repo in the config**: `Robot-2026` and `IgniteLib` → `Build and Check Code Format` (both have `pr-build-check.yml`); `flashpoint` → `ci-ok`; `Phoenix-2026` has **no CI workflow at all** (no `.github/` directory) — see §3.8. This newly requires PRs into `main`/`development` on three repos; that is intended (`team-gitflow.md` rule 4 already says branch protection enforces it). Classic protection on `flashpoint` is left in place and reported; the builder removes it only after confirming the ruleset fully covers it.
3. Create labels `feature`, `bug`, `hotfix`, `spec` (skip existing; don't recolour existing).
4. Link the repo to the team Project.

Org-level, once:

5. Create the team Project (title *(builder's call)*, e.g. "Ignite Programming"; the title is in config and the script finds the Project by title, so the script never edits its own config), set its Status options to the five above, create Size and Release fields. Editing Status options needs GraphQL (`updateProjectV2Field`) rather than `gh project field-create`.
6. **Migrate** items from the existing Projects — "Flashpoint" (#3, 31 items) and "IgniteLib" (#5, 16 items):
   - Status: Needs triage / Backlog / Todo → **Backlog**; In progress → **In progress**; Done → **Done**. IgniteLib's 16 "Todo" items land in Backlog, not Ready, because none has been checked against the Ready rule.
   - Size carries over.
   - Items whose repo is the archived `flashpoint-private` (8, all Done) are **skipped**.
   - Identity key is the item's content URL, so re-runs never duplicate. PR items migrate exactly like issues. Draft items (no repo) and any status outside the mapping are skipped with a warning. Release is left blank — the old Projects have no values for it.
7. **Close** (not delete) Projects #3 and #5 — behind a separate flag (e.g. `--close-old`), run only after Josh has looked at the migrated board. Projects #2 and #4 (Josh's empty untitled projects) are not touched.
8. Push the templates to `IgniteRobotics/.github` (§3.5), if the builder takes that route. That repo exists and today holds only `README.md` and `.github/workflows/` — no templates, no `profile/` — so nothing is clobbered. Same dry-run/apply split: dry-run shows the files it would add.

**Not scriptable (believed — verify before the plan depends on it):** GitHub Projects' built-in automations (auto-add from a repo, item closed → Done, PR merged → Done, auto-archive) are not exposed through the API as far as Claude knows. These become a short **manual checklist** on the new docs page. The builder confirms this against current GitHub docs, and also confirms the auto-add workflow count limit for the org's plan, since four repos may need four auto-add rules.

**Auth:** Josh's `gh` token (`gist`, `project`, `read:org`, `repo`) already reads every managed repo, both Projects, and the rulesets — confirmed in session. Tests still use recorded fixtures through the injectable runner; they never call GitHub.

**Who runs `--apply`:** not the plan. The plan builds and tests the script and produces a dry-run against the real org; Josh reads the dry-run and either runs `--apply` himself or tells the agent to. A dry-run is read-only and may be run during implementation.

### 3.7 Docs (agreed in session)

| File | Change |
|---|---|
| **new** `docs/programming/issues-and-projects.md` *(name: builder's call)* | What an issue is for, the Ready rule, the status table, Size and Release, branch naming, `Closes #`, draft-PR-as-spec-review, new-issue-not-reopen, hotfix handling, the manual automation checklist, and the repo setup checklist (= how to run the script for a new repo). Add to `mkdocs.yml` nav under Programming Curriculum, next to Team Gitflow |
| `docs/programming/team-gitflow.md` | Becomes canonical: the "Source" section pointing at Notion is replaced (this page is the source). `development` is the default branch. The standard flow gains: pick up a Ready issue; branch `feature/<num>-<name>`; open a draft PR once the spec is committed; `Closes #<num>`; archive as the last commit; merge closes the issue. The "this is not what you do yet" banner stays but softens — still true for students before M7, no longer the page's main framing. Rules list gains the issue/PR rules |
| `docs/programming/ai/the-loop.md` | Issue before rung 2; draft PR at rung 2 is the spec review; rung 11 (archive) moves into the PR, before merge; rung 8/9 failures open a new issue (the replay procedure step 3 gains this) |
| M7, M8 milestone pages; `teaching-repo-spec.md` §9.8/§9.9; `curriculum-spec.md` M7/M8 entries | Archive timing (§3.4) and "your change has an issue" |
| `docs/CONTRIBUTING.md` | One line in "Pull requests to `Robot-2026`" pointing at the new page *(Claude's call)* |
| `templates/github/` | Issue and PR template sources (§3.5) |
| `tools/<script>.py`, its config, `tests/test_<script>.py` | §3.6 |

`docs/programming/README.md`'s built-milestones row, `teaching-repo-spec.md` §13, and `tests.lock` are **not** touched by this change — no milestone is built, no given test changes.

---

### 3.8 Workflow files added to the managed repos

Josh approved both. This narrows §2's "anything in the four managed repos' code" exclusion to these files only:

- **`Phoenix-2026` gets a PR build check** — a copy of `Robot-2026`'s `.github/workflows/pr-build-check.yml` (job name `Build and Check Code Format`), so its `dev-main-pr-gate` ruleset has a check to require. The builder confirms `Phoenix-2026` builds with it before the ruleset starts requiring it; if it doesn't build cleanly, the ruleset ships PR-required without the check and the build failure is reported to Josh — it is not fixed as part of this change.
- **Fallback auto-add workflow**, only if §7.1/§7.2 show built-in auto-add can't cover all four repos: a short workflow per repo using `actions/add-to-project`, which needs a token with Project write access stored as an org secret. Josh creates the secret; the docs page says how.

Both are added through a PR into each repo's `development`, following the flow these docs describe — not pushed directly. The implementation **opens** these PRs (Josh); Josh merges them.

---

## 4. Constraints

- **`docs/` publishes publicly with no draft state.** Nothing student-facing may leak secrets. Links to private repos (`Robot-2026`, `ignite-training-solutions`) are allowed and the link check already skips private Ignite repos (commit `22e59d1`).
- **Voice:** match the existing pages after the two recent de-slop passes — technical student audience, direct, no invented aphorisms. Run the `de-slop` skill on new prose before review.
- **No pre-2026 references** in student-facing material.
- Before merging: `poetry run pytest`, `poetry run mkdocs build --strict`, `poetry run python tools/site_guard.py _site`. If `site_guard.py` keeps a registry of pages, the new page is registered.
- The script must never delete: not rulesets it doesn't own, not labels, not Projects (close only).

---

## 5. Goals, non-goals, anti-goals

**Goals:** one board for all team programming work; issue → spec → PR traceable in both directions; a merge closes its issue with no manual step in the normal case; any managed repo reproducibly configured from a config file.

**Non-goals:** changing the pre-M7 student-fork flow; adopting GitHub Milestones; carrying over unused fields; migrating Notion content beyond the programming process docs.

**Anti-goals — failure even if it technically works:**
- A board that drifts from the docs because the setup was clicked by hand in four places.
- A script that silently overwrites a ruleset or protection someone set on purpose.
- Docs that describe the issue flow as optional for SDD changes. An SDD change without an issue is the thing this exists to stop.
- Curriculum pages still saying the mentor archives after merge, contradicting the team pages.

---

## 6. Left to the builder

- Script name, module layout, config schema, CLI flags beyond `--apply`.
- The Project's title; the new page's filename and exact nav placement.
- How templates reach `IgniteRobotics/.github`; whether `templates/github/` joins `templates_sync.py`.
- Whether "Spec review" gets its own Status (default: no).
- Required-check names per repo, by inspection.

## 7. To verify before the plan relies on it

1. Built-in Project automations are not configurable by API (§3.6). If they are, script them and drop the manual checklist.
2. Auto-add workflow limit for the org's plan vs. four repos.
3. Whether a built-in "pull request linked to issue → set status" workflow exists (drives In review).
4. Whether `flashpoint`'s classic protection duplicates the target ruleset.

Checked in session, 2026-11-04: no workflow in the four repos references `default_branch`; `Robot-2026` and `IgniteLib` trigger on `pull_request` into `main`/`development`, `flashpoint` on any `pull_request` and pushes to `main`. Switching the default branch breaks none of them.
