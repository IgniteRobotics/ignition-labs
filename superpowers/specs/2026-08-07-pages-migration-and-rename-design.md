# GitHub Pages Migration & Repository Rename — Design

**Status:** Approved design, not yet executed
**Owner:** Josh Reddick
**Created:** 2026-08-07
**Team:** FRC 6829 Ignite Robotics
**Amends:** `programming/teaching-repo-spec.md` (which this change moves to `docs/programming/teaching-repo-spec.md`) — decisions D1 and D7, sections §4.1, §7.1, §7.2, §11.2, §16, §18. Referenced as plain text rather than a link because the path changes partway through the work this document describes.

---

## 1. Why

Two problems, both found after the foundation shipped.

**`doc-builder` is the wrong tool for this repository.** It is a purpose-built pipeline for converting Obsidian vaults into engineering-notebook sites. This repository is not an Obsidian vault; its prose is ordinary markdown with relative links. Adopting it was justified as dogfooding the team's own tool, but the fit was wrong, and the cost showed up immediately: `doc-builder` renders every markdown file it finds regardless of index linkage, which forced `tools/stage_site.py` into existence purely to work around it. **A workaround for a tool we did not need is a cost with no benefit.**

**The repository name no longer describes it.** `docs` was accurate when this held vision documents. It now holds a nine-milestone curriculum, verification tooling, four CI workflows, and — soon — nine Gradle projects students fork and work in. It is renamed **`ignition-labs`**.

### 1.1 What this is not

This is not a rewrite of the curriculum, the git ritual, the real-repo ladder, or the CI topology. Those are correct and stay. This changes how the site is built, where prose files live, and what the repository is called.

---

## 2. Decisions

| # | Decision | Reasoning |
|---|---|---|
| **D7′** | **Supersedes D7. The site is built with MkDocs Material directly**, from an explicit `nav:` in `mkdocs.yml`, with prose in a `docs/` subdirectory | Standard, widely-documented tooling with no in-house wrapper to maintain. Explicit `nav:` replaces `00-index.md`. Crucially, MkDocs can only read `docs_dir` — so `projects/` is not *excluded* from publication, it is *invisible* to the builder. Publication scope stops being a rule that can be forgotten and becomes a directory boundary that cannot be crossed |
| **D10** | **Prose moves into `docs/`** | Forced, not chosen: MkDocs refuses `docs_dir: .` — verified, see §7. It is also the conventional MkDocs layout, and it is what makes D7′'s boundary property work. `programming/` moves wholesale so relative links inside the curriculum are unaffected |
| **D11** | **The repository is renamed `IgniteRobotics/ignition-labs`** | See §1. Executed as a coordinated edit across 35 references plus a GitHub-side rename |
| **D1′** | **Amends D1.** Skeletons still live in this repository at `projects/`, now a sibling of `docs/` rather than of `programming/` | The reasoning behind D1 is unchanged — one clone, one re-verification surface, CI that can verify the docs against the code they describe. Only the path changes |

**Unchanged and still in force:** D2 (fork once, branch per milestone, PR into own fork), D3, D4, D5, D6, D8 (the real-repo ladder), D9 (M6's real pull request), the §8 fade curve, the §9 inventories, the §10 fixtures, and every anti-goal in §12.

---

## 3. Target layout

```
ignition-labs/                          (renamed from docs)
├── README.md                           GitHub landing page; points at the site
├── mkdocs.yml                          docs_dir: docs · site_dir: _site · explicit nav
├── pyproject.toml                      pins mkdocs + mkdocs-material exactly
├── tests.lock
├── docs/                               ← docs_dir. The ONLY tree MkDocs can see
│   ├── index.md                        site landing page
│   ├── SETUP.md
│   ├── CONTRIBUTING.md                 GitHub also honours docs/CONTRIBUTING.md
│   ├── VISION_GUIDE.md
│   ├── vision-subsystem.md
│   ├── field-boundary-speed-limiting.md
│   └── programming/                    moves wholesale — internal links unaffected
│       ├── README.md                   → slug `programming`
│       ├── curriculum-spec.md
│       ├── teaching-repo-spec.md
│       ├── ai-use-standard.md
│       ├── student-deliverables.md
│       ├── resources.md
│       ├── git-workflow.md
│       ├── robot-repo-ladder.md
│       ├── milestones/
│       └── tracks/
├── projects/                           OUTSIDE docs_dir — structurally unpublishable
├── templates/                          M8 deliverables
├── contributors/                       M0 output
├── tools/                              site_guard · tests_lock · changed_projects
├── tests/
├── superpowers/                        design records — outside docs_dir
└── .github/workflows/                  four workflows
```

**Why `README.md` exists at the root and `docs/index.md` separately.** GitHub renders the root `README.md` on the repository page; MkDocs renders `docs/index.md` as the site landing page. The root README stays short — what this repository is, and a link to the published site. The site's landing page is the real front door.

---

## 4. What is deleted

| Deleted | Why it existed |
|---|---|
| `tools/stage_site.py` | Only ever a workaround for `doc-builder` publishing non-indexed markdown. D7′ removes the condition it worked around |
| `tests/test_stage_site.py` (7 tests) | Defended that workaround |
| `00-index.md` | Replaced by explicit `nav:` in `mkdocs.yml` |
| The `doc-builder` dependency | Wrong tool — §1 |

**`tools/site_guard.py` stays,** with a changed job. Its `forbidden_files()` becomes belt-and-braces: the `docs_dir` boundary already makes a `projects/` leak impossible, so the guard now catches only the case where someone creates `docs/projects/` or `docs/superpowers/`. Cheap, keep it.

Its `missing_pages()` earns its keep unchanged — it catches a `nav:` entry pointing at a file that did not render, which would otherwise deploy a site with a dead navigation link and no failure anywhere.

**`REQUIRED_PAGES` needs no edit.** Verified in §7: MkDocs strips the `docs_dir` prefix, so `docs/programming/git-workflow.md` publishes to the slug `programming/git-workflow` — exactly what the guard already expects. Every current entry survives the move unchanged.

---

## 5. `mkdocs.yml`

```yaml
site_name: Ignition Labs
site_description: FRC 6829 Ignite Robotics — programming curriculum and team documentation
docs_dir: docs
site_dir: _site

theme:
  name: material
  features:
    - navigation.sections
    - navigation.top
    - search.highlight
    - content.code.copy
  palette:
    - media: "(prefers-color-scheme: light)"
      scheme: default
      toggle: { icon: material/brightness-7, name: Switch to dark mode }
    - media: "(prefers-color-scheme: dark)"
      scheme: slate
      toggle: { icon: material/brightness-4, name: Switch to light mode }

markdown_extensions:
  - admonition
  - attr_list
  - tables
  - toc: { permalink: true }
  - pymdownx.superfences
  - pymdownx.details

nav:
  - Home: index.md
  - Setup: SETUP.md
  - Contributing: CONTRIBUTING.md
  - Programming Curriculum:
      - Overview: programming/README.md
      - Git Workflow: programming/git-workflow.md
      - Real-Repo Ladder: programming/robot-repo-ladder.md
      - AI Use Standard: programming/ai-use-standard.md
      - Student Deliverables: programming/student-deliverables.md
      - Resources: programming/resources.md
  - Tracks:
      - Track A — New to Programming: programming/tracks/track-a-new-to-programming.md
      - Track B — Programmed, Not Object-Oriented: programming/tracks/track-b-non-oo-programmer.md
      - Track C — OO Experience, No FRC: programming/tracks/track-c-oo-no-frc.md
      - Track D — FRC Veteran, Levelling Up With AI: programming/tracks/track-d-frc-veteran-ai.md
  - Milestones:
      - M0 — Tooling, Git & the Build: programming/milestones/m0-tooling-and-git.md
      - M1 — Java Fundamentals: programming/milestones/m1-java-fundamentals.md
      - M2 — OOP, Interfaces & Lambdas: programming/milestones/m2-oop-interfaces-lambdas.md
      - M3 — WPILib Command-Based: programming/milestones/m3-wpilib-command-based.md
      - M4 — Sensors, Actuators & Control: programming/milestones/m4-sensors-actuators-control.md
      - M5 — Autonomous, Odometry & Vision: programming/milestones/m5-autonomous-odometry-vision.md
      - M6 — Simulation, Telemetry & Testing: programming/milestones/m6-simulation-telemetry-testing.md
      - M7 — AI-Assisted Development: programming/milestones/m7-ai-assisted-development.md
      - M8 — In-Season Deliverables: programming/milestones/m8-in-season-deliverables.md
  - Vision:
      - Vision Guide: VISION_GUIDE.md
      - Vision Subsystem: vision-subsystem.md
      - Field Boundary Speed Limiting: field-boundary-speed-limiting.md
  - For Mentors:
      - Curriculum Spec: programming/curriculum-spec.md
      - Teaching Repository Spec: programming/teaching-repo-spec.md
```

**Not `strict: true`.** Strict mode turns warnings into errors, and one warning is deliberate: `programming/robot-repo-ladder.md` links `UNTESTED-TARGETS.md`, which does not exist until the M6 plan lands. Revisit once that file exists.

---

## 6. The rename

35 references across six live files. Ordered by consequence.

| Where | What changes | Why it matters |
|---|---|---|
| **`.github/workflows/` (5 gate strings)** | `github.repository == 'IgniteRobotics/ignition-labs'` | **Highest consequence in the change.** A mismatched gate does not fail — it silently skips the job forever. Pages never deploys, skeleton integrity never runs, and every check is a passing grey nothing |
| **`docs/programming/git-workflow.md`** | Every fork, clone, remote, and PR-target command a student copy-pastes | These are transcribed by 14-year-olds. A stale URL here is a session lost to a 404 |
| **`tests/test_process_docs.py`** | `test_m0_pull_request_targets_upstream` asserts `IgniteRobotics/docs:main` | This is the test that *catches* a missed rename. Update it last, so it fails loudly first |
| **`docs/programming/teaching-repo-spec.md`** | D1, D7, §4.1 tree, §7.1, §7.2, §11.2, §16, §18 | The canonical spec must describe the repository that exists |
| **`docs/SETUP.md`, `docs/CONTRIBUTING.md`** | Clone paths and repository references | — |
| **`pyproject.toml`** | `name = "ignition-labs-tools"` | Cosmetic |
| **`superpowers/plans/2026-08-07-teaching-repo-foundation.md`** | **Left alone** | A historical record of what was executed. Rewriting it would be falsifying it |

**Two rename steps happen outside the working tree:**

1. **On GitHub** — rename the repository in Settings. GitHub redirects the old URL, so nothing breaks immediately, but every reference should still be updated because the redirect is a courtesy, not a contract. *(Moot until the remote exists — §9.)*
2. **Locally** — `~/workspace/ignite/docs` → `~/workspace/ignite/ignition-labs`. **This must be the last step of the whole migration**, because it invalidates the working directory of anything running inside it.

---

## 7. Verified facts

Measured on 2026-08-07 against `mkdocs` 1.6.1 and `mkdocs-material` 9.7.7, using throwaway probe repositories.

| Question | Answer | Evidence |
|---|---|---|
| Does MkDocs accept `docs_dir: .`? | **No.** `Config value 'docs_dir': The 'docs_dir' should not be the parent directory of the config file. Use a child directory instead.` Build aborts | This is what forces D10 |
| Does `docs_dir: docs` build? | Yes | Probe built cleanly |
| Do files outside `docs_dir` leak into the site? | **No.** A probe with `projects/m1/X.java` and `projects/m1/README.md` produced **zero** matching files in `_site` | This is D7′'s boundary property, confirmed |
| What slug does `docs/programming/README.md` produce? | `programming/index.html` | `REQUIRED_PAGES` survives the move unedited |
| Does MkDocs render markdown absent from `nav:`? | **Yes**, with a warning | Drives the risk in §8 |
| Nested prose not in `nav:` | Also rendered (`programming/tracks/a.md` → `programming/tracks/a/index.html`) | Same risk |

---

## 8. Risks

| Risk | Severity | Response |
|---|---|---|
| A workflow gate string is missed, silently disabling CI | **High** | Grep for the old name as an explicit verification step; assert zero hits before the work is called done |
| MkDocs 2.0 breaks everything | **High, deferred** | Material's own maintainers print a warning on every build: MkDocs 2.0 removes the plugin system, rewrites theming, and offers *no migration path*. **Pin `mkdocs` and `mkdocs-material` to exact versions** in `pyproject.toml` and add the bump to the §16 seasonal maintenance list. An unpinned floating dependency will break this site between seasons, which is precisely the drift the curriculum exists to prevent |
| Markdown dropped in `docs/` publishes without being in `nav:` | Medium | Accepted — everything in `docs/` is *meant* to be published; that is the boundary. Document it in one sentence in `CONTRIBUTING.md` so nobody parks a draft there |
| `git mv` history is lost on moved files | Medium | Use `git mv`, not delete-and-add. Verify with `git log --follow` on one moved file |
| Relative links break during the move | Medium | `programming/` moves wholesale, so intra-curriculum links are unaffected. The link checker is the net — it must be green before and after |
| Material's "Currently unlicensed" banner in CI logs | Low | Cosmetic nagware from Material 9.7; the package is MIT. Ignore |

---

## 9. Execution order

Dependency-ordered. Everything before step 8 is local and reversible.

| # | Step |
|---|---|
| 1 | `git mv` prose into `docs/`; create `docs/index.md` and a root `README.md` |
| 2 | Write `mkdocs.yml` (§5); delete `00-index.md`, `tools/stage_site.py`, `tests/test_stage_site.py` |
| 3 | Pin `mkdocs` and `mkdocs-material` exactly in `pyproject.toml`; drop the `doc-builder` install |
| 4 | Rewrite `pages.yml`: install pinned MkDocs → `mkdocs build` → `site_guard.py ./_site` → upload → deploy. Update `paths:` to `docs/**`, `mkdocs.yml`, `tools/site_guard.py` |
| 5 | Rename sweep — all 35 references, per §6. Update `tests/test_process_docs.py` last |
| 6 | Amend `docs/programming/teaching-repo-spec.md`: D1′, D7′, D10, D11, §4.1 tree, §7.1, §7.2, §11.2, §16, §18 |
| 7 | Verify: `pytest` green · `mkdocs build` clean · `site_guard` passes · zero `projects/` or `superpowers/` in `_site` · lychee zero errors · `grep -r "IgniteRobotics/docs"` returns nothing outside `superpowers/plans/` |
| 8 | **Owner, on GitHub:** rename the repository *(moot until the remote exists)* |
| 9 | **Owner, locally, last:** `mv ~/workspace/ignite/docs ~/workspace/ignite/ignition-labs` |

---

## 10. Acceptance criteria

- [ ] `mkdocs build` succeeds with no `doc-builder` anywhere in the repository
- [ ] `tools/stage_site.py`, `tests/test_stage_site.py`, and `00-index.md` are gone
- [ ] `_site` contains every page in `nav:` and **zero** files from `projects/`, `superpowers/`, `tools/`, or `tests/`
- [ ] `tools/site_guard.py` passes against the built site, with `REQUIRED_PAGES` unedited
- [ ] `grep -rn "IgniteRobotics/docs"` returns hits only inside `superpowers/plans/` (the historical record)
- [ ] All seven workflow gate strings read `IgniteRobotics/ignition-labs`
- [ ] `student-check.yml` still carries **no** repository gate
- [ ] `pytest` is green, minus the seven deleted staging tests
- [ ] `lychee` reports zero errors
- [ ] `mkdocs` and `mkdocs-material` are pinned to exact versions and named in §16 maintenance
- [ ] `git log --follow docs/programming/git-workflow.md` shows history from before the move
- [ ] The published site's `teaching-repo-spec` page renders whole, not split
