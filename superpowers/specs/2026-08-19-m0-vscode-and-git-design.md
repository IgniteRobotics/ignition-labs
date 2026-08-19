# M0 VS Code setup and team git usage — design record

**Date:** 2026-08-19
**Build-order step:** none — this is a content addition to a milestone already shipped (spec §13 step 3)
**Governing sections:** §5 (the git ritual), §9.1 (M0 inventory), §7.1 (Pages), §14 (acceptance)
**Source material:** [Ignite Git Usage Guidelines](https://igniterobotics.notion.site/Ignite-Git-Usage-Guidlines-2ece1d60205380c39926f80cb312612a) (Notion, team-authored)

M0 already teaches git. It teaches a *student* git — fork, branch, one pull request, an
eight-command allow-list — and it teaches it well. What it does not do is tell a student
that the team itself works differently, name the extensions the team standardizes on, or
acknowledge that the student is looking at a Source Control panel the whole time.

This adds those three things without disturbing what M0 already gets right.

---

## 1. What ships

| Deliverable | Location | New? |
|---|---|---|
| Team gitflow literacy page | `docs/programming/team-gitflow.md` | new file |
| Gitflow diagram, dark-mode-safe | `docs/images/gitflow.png` | new file, new directory |
| Extension setup | `docs/SETUP.md` — new step 1b | edit |
| Workspace extension recommendations | `projects/m0-onboarding/.vscode/extensions.json` | new file |
| `.vscode` scoping | `projects/m0-onboarding/.gitignore` | edit |
| CLI ↔ GUI command table | `docs/programming/milestones/m0-tooling-and-git.md` | edit |
| M0 DoD additions | same file | edit |
| Forward pointer to M7 | `docs/programming/git-workflow.md` | edit |
| Nav entry | `mkdocs.yml` | edit |
| Page + asset registration | `tools/site_guard.py`, `tests/test_site_guard.py` | edit |
| §5.7 and §9.1 updates | `docs/programming/teaching-repo-spec.md` | edit |

**No Gradle project changes.** Nothing here touches a `build.gradle`, a source set, or a
given test. `tests.lock` is untouched — `.vscode/extensions.json` is not under any
`src/test/`.

---

## 2. Decisions

### 2.1 M0 teaches gitflow as literacy, not as practice

The Notion guidelines describe how the team works on `Robot-2026`: `main` ← `development`
← `feature/*`, `hotfix/*` off `main` at competition, `release/<comp>` tags. The curriculum
teaches something else entirely — fork `ignition-labs`, branch, pull-request **into your own
fork**, and `main` is the only long-lived branch (spec §5.3).

Both are correct. They apply to different repositories at different points in the ladder.

M0 therefore ships the diagram and the *why*, and stops there. A student must be able to
read `Robot-2026`'s branch list at M2 — when they clone it read-only — without concluding
the documentation contradicts itself. They must not be asked to practice a flow they have no
repository to practice it in.

**The full gitflow becomes practice at M7**, when the Kickoff Readiness Gate grants commit
access and the student branches directly on `Robot-2026` (spec §5.4). That is the same
boundary §5.5 already calls the invariant. Gitflow lands on the far side of it because that
is where a student first has a `development` branch to merge into.

Rejected: teaching gitflow as the "real" target with fork→PR framed as training wheels. It
makes the thing a student does today sound provisional, at the exact milestone where the
goal is to get one pull request merged without losing confidence.

Rejected: adopting `development` in `ignition-labs` so students practice the real flow. It
contradicts §5.3 outright, and a `development` branch in a repository where every student
pull request targets their own fork has nothing to integrate.

### 2.2 The literacy content gets its own page, not a section in M0

Three documents need it: M0 (read it), M7 (do it), and `git-workflow.md` (contrast it
against the student flow). Content with three consumers is a page.

M0's milestone document is also already the longest in the curriculum at ~250 lines. Two of
the three additions in this design are edits *within* it; the third would push it past 400
and bury the command table that is its most-used part.

### 2.3 The page must resolve the "no need to fork" contradiction explicitly

The Notion guidelines say, in the standard developer flow, step 4:

> Create and switch to your feature branch `feature/foo`. Branch directly in the Ignite
> repo. No need to fork to a separate one.

Read by a Track A student in week two, this says `git-workflow.md` is wrong. It is not — the
sentence is about `Robot-2026` and about a person who already has commit access.

`team-gitflow.md` carries a callout resolving it, and this is the single highest-value
paragraph in the change:

> **This is not what you do yet.** These guidelines describe `Robot-2026`, the competition
> robot's code, written by people who already have commit access to it. You get that access
> at **M7**, when you clear the Kickoff Readiness Gate. Until then you fork `ignition-labs`
> and pull-request into your own fork — see [the git workflow](git-workflow.md). Nothing
> here contradicts that; it describes the far side of the same boundary.

Without it this change makes the curriculum *less* clear than leaving the Notion page
unreferenced.

### 2.4 The diagram ships as the existing PNG, flattened onto white

The Notion diagram is 960×343 RGBA with a **fully transparent background and black
strokes** — verified: all four corners are `(0, 0, 0, 0)`, alpha extrema `(0, 255)`. In
Material's dark scheme the branch labels `main`, `development`, `feature/sharklasers`,
`feature/bad_idea` and both tag annotations render black-on-black and vanish. Only the
coloured commit dots survive, which is worse than no diagram — it looks like a rendering
bug rather than a missing image.

Fix: composite onto opaque white with 16px padding, save as RGB PNG. One-time asset
generation, not a build step. Per the repo convention against utility files for one-off
operations, no script is committed; the provenance and reproduction command are recorded
here instead.

**Provenance:** the source PNG is the image embedded in the team's Notion guidelines page,
fetched through the Notion API (its S3 URL is presigned and expires in five minutes, so it
cannot be linked to — the file must be vendored). It is team-authored; no third-party
licence applies.

```python
from PIL import Image
src = Image.open("gitflow-raw.png").convert("RGBA")
out = Image.new("RGB", (src.width + 32, src.height + 32), "white")
out.paste(src, (16, 16), src)
out.save("docs/images/gitflow.png")
```

Pillow 12.3.0 is already in the Poetry environment; no new dependency.

Rejected: re-authoring as a mermaid `gitGraph`. It would diff and re-theme, but the site has
no mermaid configuration today, and the existing diagram is already the artefact the team
recognizes. Adding a renderer to publish one image is the wrong trade.

Accepted cost: on the dark scheme the diagram is a bright white panel. That is the intended
outcome — legible beats harmonious for the one image in the curriculum a student is asked to
read closely.

### 2.5 `docs/images/`, not `docs/assets/`

Material for MkDocs emits its own theme files under `_site/assets/` —
`assets/stylesheets/`, `assets/javascripts/`, `assets/images/`. A user file at
`docs/assets/gitflow.png` publishes to `_site/assets/gitflow.png` and does not collide
today, but it shares a namespace with theme output that changes between Material releases.
`docs/images/` cannot collide. This is the first image in `docs/`, so the directory is
chosen once, here.

### 2.6 The diagram's hotfix path disagrees with the rules, and the prose says so

The drawing shows `hotfix/gwinnett` branching from `main` and merging back into `main`
only. Notion rule 6 says a hotfix merges back into **both** `main` and `development`.

The image ships unedited and the page notes the gap:

> The drawing simplifies one thing: it shows the hotfix merging back into `main`. In
> practice it merges into `development` as well — otherwise the fix you made at competition
> is missing from every feature branched after it, and someone re-breaks it in March.

Editing someone else's diagram without its source file produces a line that does not match
the original's style, and the discrepancy is worth more as a teaching moment than as a
silent correction. The page is the curriculum's copy; Notion remains the team's.

### 2.7 The page calls out `feature/bad_idea`

The diagram contains a branch that leaves `development` and never returns. That is not an
error in the drawing — it is the most reassuring thing in it, and M0 is the milestone where
a student is most afraid of doing something irreversible:

> `feature/bad_idea` never merges. That is not a mistake in the diagram. Branches that go
> nowhere cost nothing and are thrown away constantly. This is the whole reason we work on
> branches.

### 2.8 Three extensions, chosen for git legibility

| Extension | Why |
|---|---|
| **GitLens** | Inline blame. Makes "who wrote this line, and why" answerable without a command — the single largest legibility win in the IDE. Free tier covers everything M0 needs. |
| **Git Graph** | Renders the student's actual history in the same shape as §2.4's diagram. This is what connects the drawing to something real on their own machine. |
| **Live Share** | Mentor pairing, including remotely. |

None is required to build, deploy, or pass CI. That is deliberate: spec §12.1 forbids a
skeleton whose setup assumes a tool, and a student on a locked-down laptop must still
complete M0.

**Not included: GitHub Pull Requests and Issues.** M0's "respond to at least one review
comment" stays a github.com task. The milestone's goal is that a student can navigate the
pull request UI where the team actually reviews; giving them a second surface for it at the
same moment splits attention with no gain.

**Copilot and equivalents remain off.** M0–M2 are Red tier and the existing DoD item
(*"Copilot or equivalent is disabled in your editor"*) is unchanged. Nothing in this design
weakens it — the extension list is deliberately git tooling only.

### 2.9 Live Share gets consent guidance

Live Share shares the host's files and terminal with whoever joins the session. This
repository's privacy posture is handle-only by design (spec §5.2: no legal names, no
emails, no school), and a student's home directory is visible in a shared terminal in a way
their `contributors/` file deliberately is not.

`SETUP.md` carries one sentence:

> Live Share shares your files and your terminal with whoever joins. A mentor starts the
> session, you accept it, and you end it when you are done — check the Live Share panel
> shows no active session before you close your laptop.

### 2.10 `extensions.json` ships in M0 only

VS Code prompts for recommended extensions on first open of a workspace. Spec §4.2 has
students open the milestone directory as their workspace, and M0 is by definition the first
one. Extensions install at user level, not workspace level, so the prompt is only useful
once — copying the file into all ten projects would re-prompt about extensions the student
already has.

`projects/m0-onboarding/.gitignore` gains the standard scoping so the WPILib extension's
generated workspace settings do not follow the recommendations into git:

```gitignore
.vscode/*
!.vscode/extensions.json
```

The file itself is student-visible and student-editable; it is not a given test and does not
belong in `tests.lock`.

### 2.11 One table, two columns

M0's "Do not start with commands. Start with the model" section is **unchanged**, and still
comes first. The change is confined to the table that follows it, which gains an **In VS
Code** column:

| Command | What it does | In VS Code |
|---|---|---|
| `git clone <url>` | Copy a repository to your machine, once | `Ctrl+Shift+P` → **Git: Clone** |
| `git checkout -b <name>` | Make a new branch and switch to it | Branch name in the status bar → **Create new branch** |
| `git status` | What have I changed? Run this constantly | The Source Control panel's file list **is** `git status` |
| `git add -u` | Stage the files you have modified | **+** on the "Changes" header |
| `git commit -m "message"` | Save a snapshot with a note | Message box, then **✓ Commit** |
| `git push` | Send your commits to GitHub | **Sync Changes**, or the ⟳ in the status bar |
| `git fetch --all` | Get everyone else's commits, without applying them | Source Control **⋯** → **Fetch From All Remotes** |
| `git merge origin/main` | Apply main's changes to your branch | Source Control **⋯** → **Branch** → **Merge** |

⚠️ **The right-hand column above is provisional.** These paths are written from knowledge of
VS Code's Source Control UI, not from clicking them in the WPILib 2026 build. Every one is
confirmed against that build during implementation per §6, and the table as shipped is
whatever that click-through finds — not what is written here.

Teaching both at once was chosen over sequencing either first. A student has the panel open
regardless; a table that names only the commands implicitly claims the buttons are something
else, and a table that names only the buttons leaves them unable to describe a broken state
to a mentor.

The `rebase` / `reset` / `cherry-pick` / force-push exclusion is **restated for the GUI**,
because it is easier to reach those accidentally from a menu than to type them:

> The same exclusion applies to the menus. The Source Control **⋯** menu contains
> `rebase`, `reset` and force-push, and they can destroy work just as thoroughly from a
> menu item as from a terminal. If you think you need one, ask a mentor.

This is a genuine new hazard the change introduces, and it is the reason the exclusion note
cannot simply be left where it is.

---

## 3. Documentation ownership — the boundary that keeps this from rotting

After this change, four documents mention git. Each owns exactly one question:

| Document | Owns | Never contains |
|---|---|---|
| `SETUP.md` | *Is my machine ready?* — install, auth, extensions | Any workflow. No branching, no PRs. |
| `git-workflow.md` | *What do I type, this week?* — the exact commands per milestone | Rationale beyond one line; team process |
| `m0-tooling-and-git.md` | *What does git mean?* — the model, the allow-list, the GUI mapping | Per-milestone command sequences (links out) |
| `team-gitflow.md` | *How does the team work, and why?* — gitflow, and when it becomes yours | Anything a student does before M7 |

Spec §5.7 states this table. Without it the fourth git document becomes the third
place someone adds a `git push` example, and the set drifts within one season.

---

## 4. Spec amendments

**New §5.7 — "Team gitflow, and where it enters":** states that `Robot-2026` runs
`main`/`development`/`feature`/`hotfix` per the team guidelines; that M0 teaches it as
literacy only; that M7 is where it becomes practice, on the far side of the §5.5 invariant;
and the §3 ownership table above.

**§9.1 M0 inventory — `Given:` gains:** `.vscode/extensions.json` recommending GitLens, Git
Graph and Live Share; `team-gitflow.md` and its diagram as required reading.

**§9.1 M0 inventory — `Mentor verifies:` gains:** the three extensions are installed; the
student can point at `development` on the diagram and say what it is for.

No change to §13 (M0 is already `built`), §10 (M0 has no fixture), or §11 (nothing to
cross-check).

---

## 5. Definition of done additions

M0's DoD gains exactly two items, appended after the existing toolchain items:

- [ ] GitLens, Git Graph and Live Share are installed and you can find the Source Control panel
- [ ] You can point at `development` on the team gitflow diagram and say why it exists

Deliberately two, not five. §8 puts M0 at no fade level because it is not a coding
milestone, but the DoD is already eight items long and it gates a student's first session.

---

## 6. Registration — the two hand-maintained lists

`team-gitflow.md` is a new page, and two lists in this repository are deliberately static
rather than derived. Both must be updated by hand or the page ships unreachable:

1. **`mkdocs.yml` `nav:`** — under *Programming Curriculum*, immediately after
   *Git Workflow*, so the student flow and the team flow sit adjacent.
2. **`tools/site_guard.py` `REQUIRED_PAGES`** — add `"programming/team-gitflow"`. The
   module comment explains why this list is hardcoded: a nav-derived list shrinks in
   lockstep with a bad nav edit and can never observe the failure it exists to catch
   (finding C11b). A page added to nav but not to `REQUIRED_PAGES` renders today and
   silently stops being guarded.

`tests/test_site_guard.py` gains an assertion that `"programming/team-gitflow"` is in
`REQUIRED_PAGES`, mirroring the existing `SETUP`/`CONTRIBUTING` test.

---

## 7. Verification

Per the repository's verification discipline, each of these is run and its actual output
reported — not summarized.

| Check | Command | Expected |
|---|---|---|
| Suite | `poetry run pytest` | green, including the two new `test_site_guard.py` assertions |
| Build | `poetry run mkdocs build` | clean; `team-gitflow.md` renders |
| Guard | `poetry run python tools/site_guard.py _site` | `site guard OK` |
| Asset published | `ls -l _site/images/gitflow.png` | present, non-zero |
| Lock unchanged | `poetry run python tools/tests_lock.py verify` | rc=0 |
| No stray commits | `git status --short` | only intended files |

**Two things must be verified by eye, because no command can:**

1. **The diagram in dark mode.** Load the built page with the OS in dark scheme and confirm
   every label is legible. This is the entire purpose of §2.4 and a build that succeeds
   proves nothing about it.
2. **The GUI column is accurate in the shipped VS Code.** Every menu path in §2.11's table
   is clicked through in the WPILib 2026 VS Code build and confirmed to exist where the
   table says it does. A mapping table nobody has clicked is a mapping table that is wrong,
   and it will be wrong in front of a Track A student in their first session.

`site_guard.py` gains `REQUIRED_ASSETS = ["images/gitflow.png"]` and a matching check.
Rationale: a missing image renders as a broken-image icon and fails silently on the
published site — precisely the class of "check passes, protected thing is broken" failure
this repository has produced before. Two lines and a test is cheap insurance.

---

## 8. Risks

| Risk | Mitigation |
|---|---|
| A student reads the Notion "no need to fork" line and abandons the fork flow | §2.3's callout, placed above the guidelines rather than below them |
| The GUI column drifts as VS Code changes menu labels | Named in §16's maintenance pass alongside the version bumps; re-clicked once a season |
| Four git documents converge and start duplicating | §3's ownership table, promoted into spec §5.7 so it is governing rather than advisory |
| Live Share used student-to-student to share solutions | Out of scope for tooling to prevent; `ai-use-standard.md` already governs collaboration boundaries and is unchanged |
| The white diagram panel reads as a rendering artefact in dark mode | Accepted, §2.4. Legibility beats harmony for the one image students must read. |

---

## 9. Out of scope

- Editing the Notion page. It stays the team's source of truth; `team-gitflow.md` is the
  curriculum's copy and links to it.
- Adding `development` to `ignition-labs` (§2.1).
- Extensions in projects M1–M8 (§2.10).
- A mermaid pipeline for the site (§2.4).
- Any change to the AI posture or the Red-tier Copilot prohibition (§2.8).
