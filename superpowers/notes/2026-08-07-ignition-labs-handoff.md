# Owner handoff — `ignition-labs` rename — 2026-08-07

Task 6 verified the whole repository end to end. Everything automatable is done and green (see verification output below). Three things remain that need a human, and cannot be scripted:

## 1. Create the GitHub remote under the new name directly

This repository has **no git remote configured yet** (confirmed 2026-08-07). That means there is nothing to rename on GitHub's side — the "rename `IgniteRobotics/docs` to `ignition-labs`" step from the original plan is moot. Whoever creates the remote should simply create it as `IgniteRobotics/ignition-labs` from the start:

```bash
# on github.com: create a new PUBLIC repository named "ignition-labs"
# under the IgniteRobotics org. Do not initialise it with a README,
# licence, or .gitignore — this repository already has history.
git remote add origin git@github.com:IgniteRobotics/ignition-labs.git
git push -u origin feature/pages-migration-ignition-labs
```

Once the remote exists, enabling GitHub Pages (Settings → Pages → Source: GitHub Actions) is also a one-time manual step — `pages.yml` is already written to deploy correctly once Pages is turned on.

After enabling Pages, verify that `https://igniterobotics.github.io/ignition-labs/` (the link in the root `README.md`) actually resolves — it will 404 until Pages is turned on and the first deploy completes.

## 2. Rename the local checkout — do this LAST

Only after the remote is created (or the owner decides not to create one yet) and all other work is merged. Renaming the working directory invalidates the cwd of any shell, editor, or agent session currently pointed at it, so it must be the very last step of the whole migration, not something folded into this task:

```bash
mv ~/workspace/ignite/docs ~/workspace/ignite/ignition-labs
```

## 3. Optional cosmetic cleanup

Rename the pyenv virtualenv from `ignite-docs` to `ignition-labs` and update `.python-version` to match:

```bash
pyenv virtualenv-delete ignite-docs   # after creating the new one and confirming it works
pyenv virtualenv 3.11.x ignition-labs
echo ignition-labs > .python-version
```

This is purely cosmetic — the environment works fine under the old name indefinitely. Do it whenever convenient, or never.

---

## What green looked like — Task 6 verification output (2026-08-07)

Environment: `eval "$(pyenv init -)" && pyenv activate ignite-docs`. All checks below were run against branch `feature/pages-migration-ignition-labs`.

### `pytest -q`

```
......................................................                   [100%]
54 passed in 0.11s
```

**54 passed.** Matches expected count exactly.

### `actionlint .github/workflows/*.yml`

Silent, exit 0. No issues.

### `mkdocs build`

Build succeeded. One expected `WARNING` (target doesn't exist yet — later plan):

```
WARNING -  Doc file 'programming/robot-repo-ladder.md' contains a link 'UNTESTED-TARGETS.md', but the target 'programming/UNTESTED-TARGETS.md' is not found among documentation files.
```

Two pre-existing `INFO`-level anchor notices in `VISION_GUIDE.md` (not warnings at this MkDocs version, but the same known, harmless issue referenced in the migration plan):

```
INFO    -  Doc file 'VISION_GUIDE.md' contains a link '#7-filtering--validation', but there is no such anchor on this page.
INFO    -  Doc file 'VISION_GUIDE.md' contains a link '#10-recommended-fixes--improvements', but there is no such anchor on this page.
```

Two additional `INFO`-level notices, not previously called out, but not warnings and not build failures — noted for completeness:

```
INFO    -  Doc file 'programming/README.md' contains an unrecognized relative link 'tracks/', it was left as is.
INFO    -  Doc file 'programming/README.md' contains an unrecognized relative link 'milestones/', it was left as is.
```

These are directory-style relative links (`[`tracks/`](tracks/)`, `[`milestones/`](milestones/)`) in a reference table in `docs/programming/README.md`; MkDocs doesn't resolve links to directories, so it leaves them as-is at INFO level. Harmless in the rendered site (they render as literal relative hrefs), but worth a look if a future pass wants zero INFO-level noise.

The Material-for-MkDocs "MkDocs 2.0 is coming" banner also prints on every build — expected, unrelated to this migration, and the reason spec §16 item 6 requires re-verifying the pin every season.

### `tools/site_guard.py ./_site`

```
site guard OK: _site
```

Pass.

### `find _site (projects|superpowers|tools|tests) | wc -l`

**0** on a normal build. Because `projects/` doesn't exist yet (skeleton projects are a later plan), this check is trivially satisfiable on its own — so it was verified with a probe: `projects/m-probe/Probe.java` and `projects/m-probe/README.md` were created, the site was rebuilt, and the find check was re-run.

**Probe result: 0.** `docs_dir: docs` means anything at the repository root under `projects/` is structurally invisible to the builder, not merely absent — the exclusion doesn't depend on `projects/` being empty. The probe directory was then deleted and `git status --short` confirmed a clean tree (no probe residue).

### `lychee`

```
🔍 228 Total (in 2s 545ms) 🔗 133 Unique ✅ 227 OK 🚫 0 Errors 👻 1 Excluded 🔀 8 Redirects
```

**0 errors.** 8 redirects noted (informational, not failures).

### `grep -rn "IgniteRobotics/docs" ... | grep -v "^./superpowers/"`

One caveat: on this machine's BSD `grep` (macOS), `grep -r ... .` does not prefix matched paths with `./`, so the literal `grep -v "^./superpowers/"` filter in the brief does not actually remove `superpowers/` hits here — the raw command's output includes many `superpowers/plans/` and `superpowers/specs/` lines that look unfiltered. Re-running with a filter anchored to the actual path format (`grep -v "^superpowers/"`) confirms the real result:

```
docs/programming/teaching-repo-spec.md
```

Exactly one hit outside `superpowers/`, and it's the intentional one: decision D11 in `teaching-repo-spec.md` names the old repository name as a deliberate contrast ("the repository is `ignition-labs`, not `IgniteRobotics/docs`"). Not a missed rename.

### `grep -rn "doc-builder" ... | grep -v "^./superpowers/"`

Same BSD-grep caveat as above. Correctly filtered: **zero hits outside `superpowers/`.**

### `git log --follow --oneline docs/programming/git-workflow.md | wc -l`

**3.** More than 1 — pre-move history survived the `git mv`.

### `git status --short`

Clean, both before and after the probe cleanup.

### Step 2 — workflow gate strings

```
.github/workflows/docs-links.yml:15:    if: github.repository == 'IgniteRobotics/ignition-labs'
.github/workflows/pages.yml:25:    if: github.repository == 'IgniteRobotics/ignition-labs'
.github/workflows/pages.yml:52:    if: github.repository == 'IgniteRobotics/ignition-labs'
.github/workflows/skeleton-integrity.yml:21:    if: github.repository == 'IgniteRobotics/ignition-labs'
.github/workflows/skeleton-integrity.yml:43:    if: github.repository == 'IgniteRobotics/ignition-labs' && needs.matrix.outputs.projects != '[]'
.github/workflows/skeleton-integrity.yml:80:    if: github.repository == 'IgniteRobotics/ignition-labs'
.github/workflows/skeleton-integrity.yml:99:    if: github.repository == 'IgniteRobotics/ignition-labs'
```

**Seven gates**, all reading `IgniteRobotics/ignition-labs`: `pages.yml` (2), `skeleton-integrity.yml` (4), `docs-links.yml` (1). Matches the corrected count (not five).

`pull_request_target`: not found anywhere in `.github/workflows/` — correct.

`student-check.yml`: `grep -c "github.repository"` → `0` — ungated, as required. This is the one workflow that must always run on student forks with no repository gate.

### Additional "Done when" checks (not covered by an explicit Step 1/2 command)

- `tools/stage_site.py`, `tests/test_stage_site.py`, `00-index.md` — all confirmed deleted.
- `pyproject.toml`: `mkdocs = "1.6.1"`, `mkdocs-material = "9.7.7"` — exact pins, no `^`/`~`.
- `docs/programming/teaching-repo-spec.md` §16 item 6 explicitly requires bumping and re-pinning `mkdocs` and `mkdocs-material` each season, with a full rebuild plus `site_guard.py` before merging the bump.

## Bottom line

All 54 tests pass, the build is clean apart from known/expected notices, `site_guard` and `lychee` both pass, all seven gate strings are correct, `student-check.yml` is correctly ungated, and history survived the move. The only actions left are the three above, and none of them can be done from inside this repository.
