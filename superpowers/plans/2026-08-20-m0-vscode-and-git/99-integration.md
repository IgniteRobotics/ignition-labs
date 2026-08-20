# Task 9: Spec amendments and the full gate sweep

The serialized tail. This task owns the one contended file in the plan —
`docs/programming/teaching-repo-spec.md` — and runs every gate on the merged result of all
three lanes.

It exists for two reasons. The governing spec is edited by every plan and two agents
finishing concurrently clobber each other there every time. And the checks that matter most
here — lychee across the whole tree, `mkdocs build --strict` — are the ones that can only
run once every lane's links point at files that exist.

**Files:**
- Modify: `docs/programming/teaching-repo-spec.md` — new §5.7, §9.1, §1.3

**Consumes:** tasks 1, 2 and 3, all merged.

**Produces:** nothing. This is the tail.

**Lane:** `integration` — single owner, runs on the merged tree, never on one lane.

---

- [ ] **Step 1: Confirm all three lanes have landed**

```bash
git status --short
test -f docs/programming/team-gitflow.md && echo "task 1 present"
test -f docs/images/gitflow.png && echo "task 1 asset present"
test -f projects/m0-onboarding/.vscode/extensions.json && echo "task 2 present"
grep -q 'In VS Code' docs/programming/milestones/m0-tooling-and-git.md && echo "task 3 present"
```

Expected: a clean tree and all four lines printed. **If any is missing, stop.** This task's
verification is meaningless on a partial tree — it will report failures that belong to a lane
that has not merged yet.

- [ ] **Step 2: Add spec §5.7**

The four-document split is what stops `team-gitflow.md` from becoming the third place someone
adds a `git push` example. Advisory in the design record; governing here.

In `docs/programming/teaching-repo-spec.md`, find the end of §5.6 — the `Students PR
solutions upstream` table row — and insert the new subsection between that table and the `---`
that precedes `## 6. The real-repo ladder`:

````markdown
| Students PR solutions upstream | Thirty students' solutions in the canonical repository, and every solution publicly visible to the next cohort |

### 5.7 Team gitflow, and where it enters

`Robot-2026` runs a simplified gitflow — `main` ← `development` ← `feature/*`, `hotfix/<comp>`
branched from `main` at competition and merged back into both, `release/<comp>` tags, no
release branches. That is the team's own standard, maintained on Notion; `team-gitflow.md` is
the curriculum's copy of it.

**M0 teaches it as literacy only.** The student reads the diagram and can say what
`development` is for. They do not practise it, because until M7 they have no repository to
practise it in — §5.2 and §5.3 put every student pull request on a fork whose only long-lived
branch is `main`.

**M7 is where it becomes practice**, when the Kickoff Readiness Gate grants commit access and
§5.4 has the student branching directly on `Robot-2026`. Gitflow therefore lands on the far
side of the §5.5 invariant, and for the same reason the invariant exists: it is the point at
which the team has decided this student can be trusted with the competition robot's code.

The literacy step is not optional politeness. A student clones `Robot-2026` read-only at M2
(§6.3) and finds a `development` branch that `git-workflow.md` never mentions. Without §5.7's
page that reads as a documentation contradiction, and the Notion guidelines' own *"Branch
directly in the Ignite repo. No need to fork"* reads as a direct instruction to abandon §5.2.

**Documentation ownership.** Four documents now discuss git. Each owns exactly one question
and must not contain the others' content:

| Document | Owns | Never contains |
|---|---|---|
| `SETUP.md` | *Is my machine ready?* — install, authentication, extensions | Any workflow. No branching, no pull requests |
| `git-workflow.md` | *What do I type, this week?* — the exact commands per milestone | Rationale beyond a line; team process |
| `milestones/m0-tooling-and-git.md` | *What does git mean?* — the model, the allow-list, the GUI mapping | Per-milestone command sequences; links out instead |
| `team-gitflow.md` | *How does the team work, and why?* — gitflow, and when it becomes yours | Anything a student does before M7 |

Without this table the fourth git document becomes the third place someone adds a `git push`
example, and the set drifts within one season.

---

## 6. The real-repo ladder
````

- [ ] **Step 3: Amend §9.1's `Given:` and `Mentor verifies:`**

In §9.1, append to the end of the **`Given:`** paragraph (after the sentence ending
*"...formatting check exempts."*):

```markdown
`.vscode/extensions.json` recommending `eamodio.gitlens`, `mhutchie.git-graph` and `ms-vsliveshare.vsliveshare` — M0 only, since extensions install at user level and the prompt is useful exactly once. `team-gitflow.md` and its diagram, as required reading per §5.7.
```

And append to the end of the **`Mentor verifies:`** paragraph (after
*"**Actions are enabled on their fork** (§7.3, mechanic 3)."*):

```markdown
The three recommended extensions are installed and the student can find the Source Control panel; the student can point at `development` on the team gitflow diagram and say what it is for (§5.7).
```

Neither is machine-checkable — no test can know which student authored a pull request, and
none can see their editor — so both belong in the mentor list rather than the tests list, per
D3.

- [ ] **Step 4: Add the revision history entry**

In §1.3, after the `**2026-08-07.**` paragraph:

```markdown
**2026-08-20.** Merged in the M0 VS Code and team-git design session. Changes: team gitflow specified as M0 literacy and M7 practice (§5.7); documentation ownership across the four git documents made governing (§5.7); M0's given inventory gains `.vscode/extensions.json` and `team-gitflow.md`, and its mentor-verified list gains two items (§9.1). The originating design document is preserved at `../superpowers/specs/2026-08-19-m0-vscode-and-git-design.md`.
```

Note the link is written as `../superpowers/...` to match the existing entry's form. It is a
provenance note in prose, not a working link — `superpowers/` is outside `docs_dir` and is
excluded from lychee, exactly as the 2026-08-07 entry already is.

- [ ] **Step 5: Confirm the spec still says what it said**

The §13 build-order status column and §14's acceptance criteria are **not** touched by this
plan. M0 was already `built`; nothing here changes that.

```bash
git diff --stat docs/programming/teaching-repo-spec.md
grep -n '^### 5\.' docs/programming/teaching-repo-spec.md
grep -n '^### 1\.3' -A 4 docs/programming/teaching-repo-spec.md | grep -c '2026-08'
```

Expected: one file changed; §5 subsections running `5.1` through `5.7` in order; two revision
history dates.

- [ ] **Step 6: The full gate sweep — run every one, report actual output**

Not a summary. Paste what each command printed.

```bash
poetry run pytest
poetry run python tools/tests_lock.py verify; echo "tests_lock rc=$?"
poetry run python tools/templates_sync.py verify; echo "templates_sync rc=$?"
poetry run mkdocs build --strict; echo "mkdocs rc=$?"
poetry run python tools/site_guard.py _site; echo "site_guard rc=$?"
ls -l _site/images/gitflow.png
git status --short
```

Expected:

| Check | Expected |
|---|---|
| `pytest` | all pass, four more than before this plan |
| `tests_lock verify` | `rc=0` — no manifest change; `extensions.json` is not under any `src/test/` |
| `templates_sync verify` | `rc=0` — untouched by this plan |
| `mkdocs build --strict` | `rc=0`. **`--strict` is the CI gate** (`pages.yml`): it turns MkDocs' warnings, including unresolved relative links, into failures. A plain `mkdocs build` can pass where CI fails |
| `site_guard` | `site guard OK: _site` |
| `_site/images/gitflow.png` | present, non-zero |
| `git status --short` | clean apart from the spec edit |

- [ ] **Step 7: Link-check the merged tree**

This is the run that decides. Each lane's links pointed at files owned by other lanes; only
here do they all exist.

```bash
lychee --no-progress --accept 200,206,403,429 \
  --exclude-path superpowers --exclude-path .superpowers './**/*.md'
```

Expected: zero failures. This mirrors `docs-links.yml`, which runs with `fail: true`.

Pay attention to two specific links:

1. **The Notion URL** in `team-gitflow.md`. It is an external dependency on somebody else's
   site — the same class of gate that has gone red here before. If it fails, do **not** delete
   the link; the Notion page is the source of truth for that content and losing the pointer is
   worse than a red gate. Get the URL fixed.
2. **`../images/gitflow.png`** from `team-gitflow.md`, and the two links task 3 added.

No `lychee` binary? Say so explicitly in the report and check the relative links by hand:

```bash
grep -rhoE '\]\(\.\.?/[^)]+\)' docs/programming/team-gitflow.md \
  docs/programming/milestones/m0-tooling-and-git.md docs/SETUP.md
```

Then confirm each resolves from its own file's directory. Report which route you took.

- [ ] **Step 8: Walk the design record's acceptance surface**

Open `superpowers/specs/2026-08-19-m0-vscode-and-git-design.md` and go through §1's *What
ships* table row by row. For each, confirm the file exists and contains what the spec says.

Then confirm the three claims the spec makes that no gate covers:

- **§2.4** — the diagram is legible on the dark scheme. Task 1 verified this; confirm it
  survived the merge by loading the page once more with `mkdocs serve`.
- **§2.11** — the GUI column matches the running WPILib 2026 VS Code. Task 3 verified this;
  read its task report and confirm the shipped table is the one it found, not the plan's
  guess.
- **§2.8** — M0 is still Red tier. `grep` for the Copilot DoD item and confirm it is present
  and unweakened.

```bash
grep -n 'Copilot or equivalent is disabled' docs/programming/milestones/m0-tooling-and-git.md
```

Anything you could not verify, say so plainly and say why. An unchecked box with a reason is
worth more than a checked one without a run.

- [ ] **Step 9: Commit**

```bash
git add docs/programming/teaching-repo-spec.md
git commit -m "Integration: make the four-way git documentation split governing

Spec 5.7. The team runs gitflow on Robot-2026; the curriculum runs fork-and-PR
on ignition-labs; both are correct and they are not the same thing. M0 teaches
the team model as literacy and M7 as practice, which puts it on the far side of
the 5.5 invariant for the same reason the invariant exists.

The literacy step is not politeness. A student clones Robot-2026 read-only at
M2 per 6.3 and finds a development branch git-workflow.md never mentions, and
the team guidelines' own 'Branch directly in the Ignite repo. No need to fork'
reads cold as an instruction to abandon 5.2.

This plan adds a fourth document that discusses git, which is the real risk it
introduces. 5.7 now carries an ownership table -- one question each, and what
each must never contain -- because otherwise team-gitflow.md becomes the third
place someone adds a git push example and the set drifts inside one season.

9.1 gains extensions.json and team-gitflow.md as given, and two mentor-verified
items. Both are mentor-verified rather than tested because no check can see a
student's editor or know who authored a pull request, per D3.

Section 13 and section 14 are untouched. M0 was already built.

Co-Authored-By: Claude Opus 5 <noreply@anthropic.com>"
```

- [ ] **Step 10: Report**

State, with actual output:

- every command in steps 6 and 7, and its exit code
- which route you took for the link check
- what task 3's click-through actually found, and whether the shipped table differs from
  this plan
- anything left unverified, and why
