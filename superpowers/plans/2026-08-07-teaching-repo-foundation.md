# Teaching Repository Foundation — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ship steps 0–3 of `programming/teaching-repo-spec.md` §13 — the two student-facing process documents, the verification tooling, the four CI workflows, and a live GitHub Pages site — so that every later plan has a published home and a working check harness to land into.

**Architecture:** This repository is a documentation site that also holds the code it describes. Prose lives at the root and under `programming/`; Gradle skeleton projects live under `projects/` and are never published. Three small Python tools under `tools/` carry all the logic the CI workflows need — manifest verification, changed-project matrix derivation, and a published-site guard — so that logic is unit-tested locally instead of debugged by pushing commits. The workflows are thin wrappers around those tools.

**Tech Stack:** Python 3.11 + Poetry + pytest (verification tooling), `doc-builder` 2.0.0 / MkDocs Material (site), GitHub Actions (CI), GitHub Pages (hosting), Gradle 8.11 + GradleRIO 2026.2.1 (the skeleton projects this foundation will later verify).

## Global Constraints

Copied verbatim from `programming/teaching-repo-spec.md`. Every task's requirements implicitly include this section.

- **Canonical repository name is `IgniteRobotics/docs`.** Every workflow gate string must match it exactly. The repository has **no git remote as of 2026-08-07** — Task 10 creates it.
- **Canonical-only workflows are gated with `if: github.repository == 'IgniteRobotics/docs'`.** This skips grey, not red, on student forks. (§7.2)
- **Nothing under `projects/` is ever published.** The site renders every prose file and zero source files. (§14)
- **Python 3.11+, Poetry for packaging, pytest with strict markers.** House convention.
- **Gradle wrapper 8.11, GradleRIO 2026.2.1, Java 17, Spotless 8.2.1, `googleJavaFormat('1.19.2')`, JUnit `5.10.1`.** Matches `Robot-2026` and `Phoenix-2026`. (§4.3)
- **Each `projects/mN/` is a standalone Gradle project** — own `gradlew`, own `settings.gradle`, own `vendordeps/`, no root aggregation. (§4.2)
- **Never `pull_request_target`.** Fork PRs run on `pull_request` with the default `GITHUB_TOKEN`. (§7.3 mechanic 5)
- **`tests.lock` failure messages must invite disagreement**, not accuse: *"this test is part of the milestone — if you think it is wrong, say so in the pull request and a mentor will look."* (§12.2 layer 3)
- **No student-facing path may reference pre-2026 Ignite code** — not `2025-Robotics-Intro`, `Robot-2025`, or earlier. (§12.1)

## Interface Summary

Three modules under `tools/`, consumed by the workflows in Tasks 7 and 8.

| Module | Public function | Signature |
|---|---|---|
| `tools/tests_lock.py` | `compute_manifest` | `(root: Path, patterns: list[str]) -> dict[str, str]` |
| | `write_manifest` | `(root: Path, patterns: list[str], out: Path) -> None` |
| | `verify_manifest` | `(root: Path, manifest: Path) -> list[str]` |
| `tools/changed_projects.py` | `milestone_dirs` | `(changed_paths: Iterable[str]) -> list[str]` |
| `tools/site_guard.py` | `forbidden_files` | `(site_root: Path) -> list[str]` |
| | `missing_pages` | `(site_root: Path, required: list[str]) -> list[str]` |

---

## Task 1: Resolve the toolchain open item

Spec §17 lists "Does `./gradlew build` run tests under GradleRIO's `configureTestTasks`?" as near-certain but unverified. Three curriculum documents get corrected based on the answer (§7.3). This task answers it empirically, using `Phoenix-2026` as a ready-made GradleRIO project so no new project has to be authored.

**Files:**
- Create: `superpowers/notes/2026-08-07-gradle-test-lifecycle.md`
- Modify: `programming/teaching-repo-spec.md` (§17 open-items row)

**Interfaces:**
- Consumes: nothing
- Produces: a recorded yes/no that Task 3 of Plan 4 (doc corrections) depends on

- [ ] **Step 1: Copy Phoenix-2026 to a scratch directory**

```bash
rm -rf /tmp/gradle-probe && cp -R ~/workspace/ignite/Phoenix-2026 /tmp/gradle-probe
rm -rf /tmp/gradle-probe/.git
```

- [ ] **Step 2: Add a test that fails loudly if it runs**

Create `/tmp/gradle-probe/src/test/java/frc/robot/ProbeTest.java`:

```java
package frc.robot;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

class ProbeTest {
  @Test
  void thisTestRanWhichIsThePointOfTheProbe() {
    fail("PROBE: gradlew build DID invoke the test task");
  }
}
```

- [ ] **Step 3: Run build and observe**

```bash
cd /tmp/gradle-probe && ./gradlew build --console=plain 2>&1 | tail -40
```

Expected if tests run (near-certain): build FAILS with `PROBE: gradlew build DID invoke the test task`.
Expected if they do not: build SUCCEEDS, and `configureTestTasks` has detached `test` from `check`.

- [ ] **Step 4: Confirm the same probe under an explicit test invocation**

```bash
cd /tmp/gradle-probe && ./gradlew test --console=plain 2>&1 | tail -20
```

Expected: FAILS with the same message. If this passes while step 3 fails to run it, the test task exists but is not wired into `check` — record that precisely, because it changes the §7.3 correction from "already gated" to "needs a workflow change."

- [ ] **Step 5: Record the finding**

Create `superpowers/notes/2026-08-07-gradle-test-lifecycle.md` with: the exact command run, the exact tail of output, the verdict (`build` runs `test`: yes/no), and the date. Two sentences of prose maximum — this is evidence, not an essay.

- [ ] **Step 6: Update the spec's open-items row**

In `programming/teaching-repo-spec.md` §17, replace the first row's status with the verdict and a link to the note. If the answer is "no", also add a row to §15 Risks: the §7.3 correction is inverted and `Robot-2026`'s CI needs a `test` step added before M6's pull request lands.

- [ ] **Step 7: Clean up and commit**

```bash
rm -rf /tmp/gradle-probe
git add superpowers/notes/2026-08-07-gradle-test-lifecycle.md programming/teaching-repo-spec.md
git commit -m "Resolve gradle test-lifecycle open item with an empirical probe"
```

---

## Task 2: Python tooling foundation

Establishes Poetry, pytest, and the `tools/` package that Tasks 5, 6, and 7 all build on.

**Files:**
- Create: `pyproject.toml`, `tools/__init__.py`, `tests/__init__.py`, `tests/test_toolchain.py`
- Modify: `.gitignore`

**Interfaces:**
- Consumes: nothing
- Produces: an importable `tools` package and a working `poetry run pytest`

- [ ] **Step 1: Write the failing test**

Create `tests/test_toolchain.py`:

```python
"""Guards that the tooling package is importable and pytest is configured strictly."""

import tools


def test_tools_package_is_importable():
    assert tools.__name__ == "tools"
```

- [ ] **Step 2: Run it to verify it fails**

Run: `poetry run pytest tests/test_toolchain.py -v`
Expected: FAIL — Poetry is not initialised, so the command itself errors with "could not find pyproject.toml".

- [ ] **Step 3: Create the Poetry project**

Create `pyproject.toml`:

```toml
[tool.poetry]
name = "ignite-docs-tools"
version = "0.1.0"
description = "Verification tooling for the Ignite Robotics teaching repository"
authors = ["FRC 6829 Ignite Robotics"]
package-mode = false

[tool.poetry.dependencies]
python = "^3.11"

[tool.poetry.group.dev.dependencies]
pytest = "^8.0"

[tool.pytest.ini_options]
addopts = "--strict-markers --strict-config -q"
testpaths = ["tests"]

[build-system]
requires = ["poetry-core>=1.9.0"]
build-backend = "poetry.core.masonry.api"
```

- [ ] **Step 4: Create the package markers**

Create `tools/__init__.py`:

```python
"""Verification tooling for the Ignite Robotics teaching repository."""
```

Create `tests/__init__.py` as an empty file.

- [ ] **Step 5: Install and run the test**

```bash
poetry install
poetry run pytest tests/test_toolchain.py -v
```

Expected: PASS.

- [ ] **Step 6: Ignore Python build artefacts**

Append to `.gitignore`:

```
__pycache__/
.pytest_cache/
_site/
```

- [ ] **Step 7: Commit**

```bash
git add pyproject.toml poetry.lock tools/__init__.py tests/__init__.py tests/test_toolchain.py .gitignore
git commit -m "Add Poetry tooling foundation with strict pytest config"
```

---

## Task 3: Resolve the doc-builder unknown and build the site guard

`doc-builder build-from-source` takes a vault directory and an index. Two things are unverified and both are load-bearing: whether it copies non-indexed files (which would publish `projects/`), and whether it handles our standard markdown relative links (the sample vault uses Obsidian `[[wikilinks]]`). This task answers both with a test that stays useful forever.

**Files:**
- Create: `00-index.md`, `tools/site_guard.py`, `tests/test_site_guard.py`
- Reference: `programming/teaching-repo-spec.md` §4.1 (which prose is published)

**Interfaces:**
- Consumes: `tools` package from Task 2
- Produces: `forbidden_files(site_root: Path) -> list[str]` and `missing_pages(site_root: Path, required: list[str]) -> list[str]`, both used by `pages.yml` in Task 7

- [ ] **Step 1: Write the failing test**

Create `tests/test_site_guard.py`:

```python
"""The site guard enforces spec §14: every prose file renders, zero files from projects/."""

from pathlib import Path

from tools.site_guard import forbidden_files, missing_pages


def _make_site(tmp_path: Path, files: list[str]) -> Path:
    site = tmp_path / "_site"
    for rel in files:
        target = site / rel
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text("x", encoding="utf-8")
    return site


def test_clean_site_has_no_forbidden_files(tmp_path):
    site = _make_site(tmp_path, ["index.html", "programming/index.html"])
    assert forbidden_files(site) == []


def test_java_source_in_site_is_forbidden(tmp_path):
    site = _make_site(tmp_path, ["index.html", "projects/m1/Deadband.java"])
    assert forbidden_files(site) == ["projects/m1/Deadband.java"]


def test_gradle_and_vendordep_files_are_forbidden(tmp_path):
    site = _make_site(
        tmp_path,
        ["build.gradle", "gradlew", "vendordeps/Phoenix6.json", "index.html"],
    )
    assert forbidden_files(site) == ["build.gradle", "gradlew", "vendordeps/Phoenix6.json"]


def test_missing_required_page_is_reported(tmp_path):
    site = _make_site(tmp_path, ["index.html"])
    assert missing_pages(site, ["programming/git-workflow"]) == ["programming/git-workflow"]


def test_present_required_page_is_not_reported(tmp_path):
    site = _make_site(tmp_path, ["programming/git-workflow/index.html"])
    assert missing_pages(site, ["programming/git-workflow"]) == []
```

- [ ] **Step 2: Run it to verify it fails**

Run: `poetry run pytest tests/test_site_guard.py -v`
Expected: FAIL with `ModuleNotFoundError: No module named 'tools.site_guard'`.

- [ ] **Step 3: Write the implementation**

Create `tools/site_guard.py`:

```python
"""Guards the published site against leaking source files (spec §14).

A student's Java is not documentation. If any of it reaches the site, the
index-driven navigation assumption in spec D7 is wrong and the Pages build
must move to an explicit mkdocs.yml.
"""

from __future__ import annotations

import sys
from pathlib import Path

FORBIDDEN_SUFFIXES = frozenset({".java", ".gradle", ".jar", ".patch", ".wpilog"})
FORBIDDEN_NAMES = frozenset({"gradlew", "gradlew.bat", "gradle.properties", "tests.lock"})
FORBIDDEN_DIRS = frozenset({"vendordeps", "gradle"})


def forbidden_files(site_root: Path) -> list[str]:
    """Return site-relative paths that must never be published, sorted."""
    found: list[str] = []
    for path in site_root.rglob("*"):
        if not path.is_file():
            continue
        rel = path.relative_to(site_root)
        if (
            path.suffix in FORBIDDEN_SUFFIXES
            or path.name in FORBIDDEN_NAMES
            or FORBIDDEN_DIRS.intersection(rel.parts)
        ):
            found.append(rel.as_posix())
    return sorted(found)


def missing_pages(site_root: Path, required: list[str]) -> list[str]:
    """Return required page slugs that did not render, sorted.

    A slug renders as either ``<slug>/index.html`` or ``<slug>.html``.
    """
    missing = [
        slug
        for slug in required
        if not (site_root / slug / "index.html").is_file()
        and not (site_root / f"{slug}.html").is_file()
    ]
    return sorted(missing)


REQUIRED_PAGES = [
    "programming/git-workflow",
    "programming/robot-repo-ladder",
    "programming/README",
    "programming/teaching-repo-spec",
]


def main(argv: list[str]) -> int:
    if len(argv) != 2:
        print("usage: site_guard.py <site-root>", file=sys.stderr)
        return 2
    site_root = Path(argv[1])
    leaked = forbidden_files(site_root)
    absent = missing_pages(site_root, REQUIRED_PAGES)
    for path in leaked:
        print(f"PUBLISHED SOURCE FILE: {path}", file=sys.stderr)
    for slug in absent:
        print(f"MISSING PAGE: {slug}", file=sys.stderr)
    if leaked or absent:
        print(
            "\nThe published site does not match spec §14. "
            "If files under projects/ are being copied, switch the Pages build "
            "to an explicit mkdocs.yml (see plan Task 3, step 7).",
            file=sys.stderr,
        )
        return 1
    print(f"site guard OK: {site_root}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `poetry run pytest tests/test_site_guard.py -v`
Expected: PASS, 5 tests.

- [ ] **Step 5: Write the site index**

Create `00-index.md`. Only files listed here should be published:

```markdown
# Ignite Robotics Documentation

FRC 6829 · Team documentation

## Programming Curriculum

- [Programming Curriculum](programming/README.md)
- [Git Workflow](programming/git-workflow.md)
- [Real-Repo Ladder](programming/robot-repo-ladder.md)
- [AI Use Standard](programming/ai-use-standard.md)
- [Student Deliverables](programming/student-deliverables.md)
- [Resources](programming/resources.md)

## Tracks

- [Track A — New to Programming](programming/tracks/track-a-new-to-programming.md)
- [Track B — Programmed, Not Object-Oriented](programming/tracks/track-b-non-oo-programmer.md)
- [Track C — OO Experience, No FRC](programming/tracks/track-c-oo-no-frc.md)
- [Track D — FRC Veteran, Leveling Up With AI](programming/tracks/track-d-frc-veteran-ai.md)

## Milestones

- [M0 — Tooling, Git & the Build](programming/milestones/m0-tooling-and-git.md)
- [M1 — Java Fundamentals](programming/milestones/m1-java-fundamentals.md)
- [M2 — OOP, Interfaces & Lambdas](programming/milestones/m2-oop-interfaces-lambdas.md)
- [M3 — WPILib Command-Based](programming/milestones/m3-wpilib-command-based.md)
- [M4 — Sensors, Actuators & Control](programming/milestones/m4-sensors-actuators-control.md)
- [M5 — Autonomous, Odometry & Vision](programming/milestones/m5-autonomous-odometry-vision.md)
- [M6 — Simulation, Telemetry & Testing](programming/milestones/m6-simulation-telemetry-testing.md)
- [M7 — AI-Assisted Development](programming/milestones/m7-ai-assisted-development.md)
- [M8 — In-Season Deliverables](programming/milestones/m8-in-season-deliverables.md)

## Vision

- [Vision Guide](VISION_GUIDE.md)
- [Vision Subsystem](vision-subsystem.md)
- [Field Boundary Speed Limiting](field-boundary-speed-limiting.md)

## For Mentors

- [Curriculum Spec](programming/curriculum-spec.md)
- [Teaching Repository Spec](programming/teaching-repo-spec.md)
```

- [ ] **Step 6: Build the site locally and run the guard**

```bash
pip install --user 'doc-builder @ git+https://github.com/IgniteRobotics/doc-builder@main' \
  || pip install --user -e ~/workspace/ignite/doc-builder
doc-builder build-from-source . --output ./_site --site-name "Ignite Robotics" --no-split
poetry run python tools/site_guard.py ./_site
```

Expected: `site guard OK: ./_site`.

`--no-split` is required: the default `--split-threshold 500` would fragment `teaching-repo-spec.md` (746 lines) and `curriculum-spec.md` into section pages, breaking every `§`-anchored cross-reference in them.

- [ ] **Step 7: If the guard fails, switch to explicit MkDocs config**

Only if step 6 reported `PUBLISHED SOURCE FILE` lines. `doc-builder` also exposes `build --config <mkdocs.yml>`, which gives total control over what is even visible to the builder.

Create `mkdocs.yml`:

```yaml
site_name: Ignite Robotics
theme:
  name: material
docs_dir: _staging
site_dir: _site
```

Create `tools/stage_site.py`:

```python
"""Copy only the prose linked from 00-index.md into a staging directory.

Fallback path for spec D7: used when doc-builder's index-driven navigation
does not by itself keep projects/ out of the published site.
"""

from __future__ import annotations

import re
import shutil
import sys
from pathlib import Path

LINK = re.compile(r"\]\(([^)]+\.md)\)")


def staged_files(index: Path) -> list[str]:
    """Return the index itself plus every relative .md path it links, deduplicated."""
    seen = {"00-index.md"}
    for match in LINK.finditer(index.read_text(encoding="utf-8")):
        target = match.group(1)
        if not target.startswith(("http://", "https://")):
            seen.add(target)
    return sorted(seen)


def stage(root: Path, staging: Path) -> list[str]:
    if staging.exists():
        shutil.rmtree(staging)
    copied: list[str] = []
    for rel in staged_files(root / "00-index.md"):
        source = root / rel
        if not source.is_file():
            print(f"WARNING: 00-index.md links a missing file: {rel}", file=sys.stderr)
            continue
        dest = staging / rel
        dest.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(source, dest)
        copied.append(rel)
    (staging / "index.md").write_text(
        (root / "00-index.md").read_text(encoding="utf-8"), encoding="utf-8"
    )
    return copied


if __name__ == "__main__":
    files = stage(Path.cwd(), Path.cwd() / "_staging")
    print(f"staged {len(files)} files")
```

Then build and re-guard:

```bash
poetry run python tools/stage_site.py
doc-builder build --config mkdocs.yml
poetry run python tools/site_guard.py ./_site
```

Expected: `site guard OK: ./_site`. Record which path was taken in an HTML comment at the top of `00-index.md` — Task 8's workflow must run the same commands.

- [ ] **Step 8: Commit**

```bash
git add 00-index.md tools/site_guard.py tests/test_site_guard.py
git commit -m "Add site guard enforcing spec §14; resolve doc-builder publication scope"
```

---

## Task 4: `programming/git-workflow.md`

Spec §5 as student-facing prose. Zero dependencies on any other task — this is the "if only one thing gets built" deliverable.

**Files:**
- Create: `programming/git-workflow.md`, `tests/test_process_docs.py`
- Reference: `programming/teaching-repo-spec.md` §5

**Interfaces:**
- Consumes: `tools` package from Task 2
- Produces: a document `00-index.md` already links and `site_guard.REQUIRED_PAGES` already requires

- [ ] **Step 1: Write the failing test**

Create `tests/test_process_docs.py`:

```python
"""Encodes the spec §14 acceptance criteria for the two process documents.

These assert on load-bearing invariants only — the rule sentence, the presence
of each command, each milestone row. They deliberately do not assert on
surrounding prose, which should be free to improve.
"""

from pathlib import Path

import pytest

REPO = Path(__file__).resolve().parents[1]
GIT_WORKFLOW = REPO / "programming" / "git-workflow.md"


@pytest.fixture(scope="module")
def git_workflow() -> str:
    return GIT_WORKFLOW.read_text(encoding="utf-8")


def test_states_the_rule_verbatim(git_workflow):
    assert "Clone what you're reading. Fork what you'll change. Branch to work." in git_workflow


def test_states_the_kickoff_gate_invariant(git_workflow):
    assert "Fork→PR before the Kickoff Gate. Branch after." in git_workflow


@pytest.mark.parametrize(
    "command",
    [
        "git remote add upstream",
        "git fetch upstream",
        "git merge upstream/main",
        "git checkout -b",
    ],
)
def test_gives_copy_pasteable_commands(git_workflow, command):
    assert command in git_workflow


def test_m0_pull_request_targets_upstream(git_workflow):
    assert "IgniteRobotics/docs:main" in git_workflow


def test_later_milestones_pull_request_into_own_fork(git_workflow):
    assert "into your own fork" in git_workflow
```

- [ ] **Step 2: Run it to verify it fails**

Run: `poetry run pytest tests/test_process_docs.py -v`
Expected: FAIL — `FileNotFoundError` on `programming/git-workflow.md`.

- [ ] **Step 3: Write the document**

Create `programming/git-workflow.md`:

````markdown
# Git Workflow

How you use git at every milestone. One rule, three situations.

---

## The rule

> **Clone what you're reading. Fork what you'll change. Branch to work.**

That is the whole thing. Everything below is the rule applied to a specific week.

You **fork** this repository once, at M0, because it is the only thing you will change for six milestones. You **clone** `Robot-2026` at M2 to read it — no fork, no write, you are a visitor. You do not fork `Robot-2026` until M4, when you finally have something to send back.

---

## M0 — your one pull request to the team repository

This is the only pull request you will ever open against `IgniteRobotics/docs`. Getting it merged past CI **is** what M0 is for.

```bash
# 1. Fork IgniteRobotics/docs on github.com  →  <your-handle>/docs

# 2. Clone YOUR fork, not the team's
git clone git@github.com:<your-handle>/docs.git
cd docs

# 3. Point "upstream" at the team repository so you can pull updates later
git remote add upstream git@github.com:IgniteRobotics/docs.git

# 4. Branch. Never work on main.
git checkout -b add-<your-handle>-contributor

# 5. Do the work: create contributors/<your-handle>.md,
#    and run spotlessApply on the deliberately-ugly FormatMe.java
cd projects/m0-onboarding && ./gradlew spotlessApply && cd ../..

# 6. Commit and push
git add -u
git commit -m "Add <your-handle> to contributors"
git push -u origin add-<your-handle>-contributor
```

Then open the pull request on github.com: `<your-handle>/docs:add-<your-handle>-contributor` → `IgniteRobotics/docs:main`.

> **⚠️ Do this before you open the pull request.** GitHub disables Actions on forked repositories by default. Go to your fork → **Actions** tab → click **"I understand my workflows, go ahead and enable them."** If you skip it, your pull request shows *no checks at all*, which looks broken but isn't. This is the single most common M0 problem.

**What goes in `contributors/<your-handle>.md`:** your GitHub handle and what you want to work on. Nothing else — no legal name, no email, no school. It is yours, you made it, and you can delete it with another pull request whenever you like.

---

## M1 through M6 — same fork, forever

You never fork again. Every milestone is a branch in the fork you already have, and the pull request goes **into your own fork**, where a mentor reviews it.

```bash
# Start of every milestone: get the latest skeletons from the team repo
git checkout main
git fetch upstream
git merge upstream/main
git push

# Branch for this milestone
git checkout -b m3-command-based

# ...work in projects/m3-command-based/ ...

git add -u
git commit -m "M3: drivetrain subsystem and command composition"
git push -u origin m3-command-based
```

Then open the pull request: `<your-handle>/docs:m3-command-based` → `<your-handle>/docs:main`.

Yes — **into your own fork.** That is not a mistake. Your solutions stay yours, the team repository stays clean, and the next student to reach M3 cannot read your answer. CI runs on your fork and a mentor reviews it there.

| Milestone | Branch name |
|---|---|
| M1 | `m1-java-fundamentals` |
| M2 | `m2-oop` |
| M3 | `m3-command-based` |
| M4 | `m4-control` |
| M5 | `m5-auto-vision` |
| M6 | `m6-testing` |

---

## M4 and M6 — pull requests to the real robot code

At M4 and M6 you also work in `Robot-2026`. Same shape as M0, different repository:

```bash
# Fork IgniteRobotics/Robot-2026 on github.com — once. You keep it.
git clone git@github.com:<your-handle>/Robot-2026.git
cd Robot-2026
git remote add upstream git@github.com:IgniteRobotics/Robot-2026.git
git checkout -b m6-launchcalculator-tests
# ...work...
git push -u origin m6-launchcalculator-tests
```

Pull request goes to `IgniteRobotics/Robot-2026:main`. This is real competition code and a mentor reviews it like anyone else's.

See [the real-repo ladder](robot-repo-ladder.md) for what you do in that repository at each milestone, and the rules about what your tests may and may not depend on.

---

## M7 and M8 — commit access

Once you have cleared the Kickoff Readiness Gate you have write access to `Robot-2026`. No fork, no `upstream` remote — you clone the team repository directly and branch on it, like every other member of the team.

```bash
git clone git@github.com:IgniteRobotics/Robot-2026.git
cd Robot-2026
git checkout -b intake-current-limiting
```

---

## The one-line summary

> **Fork→PR before the Kickoff Gate. Branch after.**

The git ritual and the commit-access policy are the same boundary. That is deliberate: when you stop forking and start branching, it is because the team has decided you can be trusted with the competition robot's code.

---

## When it goes wrong

**"My pull request has no checks."** Actions are not enabled on your fork. See the warning in the M0 section.

**"Git says my branch is behind."** Someone updated the team repository. `git fetch upstream && git merge upstream/main`.

**"I committed to main by accident."** Common, fixable, not a big deal. Make a branch at your current position, then reset main back:

```bash
git branch m3-command-based        # save your work on a new branch
git reset --hard upstream/main     # put main back where it belongs
git checkout m3-command-based      # carry on
```

**"I have a merge conflict."** Stop and ask a mentor. Conflicts are the one git situation genuinely worth learning with someone next to you, and nobody will think less of you for it.
````

- [ ] **Step 4: Run the tests to verify they pass**

Run: `poetry run pytest tests/test_process_docs.py -v`
Expected: PASS, 8 tests.

- [ ] **Step 5: Commit**

```bash
git add programming/git-workflow.md tests/test_process_docs.py
git commit -m "Add git-workflow.md: the clone/fork/branch ritual per milestone"
```

---

## Task 5: `programming/robot-repo-ladder.md`

Spec §6 as student-facing prose, including the M6 non-circularity explanation and the pure-logic hard rule.

**Files:**
- Create: `programming/robot-repo-ladder.md`
- Modify: `tests/test_process_docs.py`
- Reference: `programming/teaching-repo-spec.md` §6

**Interfaces:**
- Consumes: `tests/test_process_docs.py` from Task 4
- Produces: a document `00-index.md` already links and `site_guard.REQUIRED_PAGES` already requires

- [ ] **Step 1: Write the failing test**

Append to `tests/test_process_docs.py`:

```python
LADDER = REPO / "programming" / "robot-repo-ladder.md"


@pytest.fixture(scope="module")
def ladder() -> str:
    return LADDER.read_text(encoding="utf-8")


@pytest.mark.parametrize("milestone", [f"M{n}" for n in range(9)])
def test_every_milestone_has_a_row(ladder, milestone):
    assert f"**{milestone}**" in ladder


@pytest.mark.parametrize("level", ["browse", "clone", "fork", "branch"])
def test_all_four_contact_levels_are_named(ladder, level):
    assert level in ladder


def test_explains_why_m6_is_not_circular(ladder):
    assert "the pull request comes from a fork" in ladder.lower()


def test_states_the_pure_logic_hard_rule(ladder):
    assert "No timing, no simulation, no hardware." in ladder


def test_no_reference_to_pre_2026_ignite_code(ladder):
    for stale in ("Robot-2025", "Robot-2024", "Robot-2023", "2025-Robotics-Intro"):
        assert stale not in ladder
```

- [ ] **Step 2: Run it to verify it fails**

Run: `poetry run pytest tests/test_process_docs.py -v`
Expected: FAIL — `FileNotFoundError` on `programming/robot-repo-ladder.md`.

- [ ] **Step 3: Write the document**

Create `programming/robot-repo-ladder.md`:

````markdown
# The Real-Repo Ladder

When you stop working in a practice project and start working in the code that runs the competition robot.

---

## Four levels

You move up this ladder and never back down. Each step is a bigger permission than the one before it.

| Level | What it means | What you can break |
|---|---|---|
| **browse** | Read a file on github.com | Nothing |
| **clone** | Copy the repository to your laptop to read and explore | Nothing — you have no way to push |
| **fork + PR** | Your own copy on GitHub; you propose changes back | Nothing without a mentor approving it |
| **branch** | You have commit access and work on the team's repository | Real things. This is why it comes last |

---

## The ladder, milestone by milestone

| | Your project | Git ritual | Real repo | What you do there |
|---|---|---|---|---|
| **M0** | `projects/m0-onboarding/` | fork `docs` → branch → **PR upstream** | — | — |
| **M1** | `m1-java-fundamentals/` | branch → PR into own fork | Robot-2026 | **browse** — `IntakeConstants.java`, `Constants.java` |
| **M2** | `m2-oop/` | branch → PR into own fork | Robot-2026 | **clone** — `preferences/`, `LaunchState`, `LaunchRequestBuilder` |
| **M3** | `m3-command-based/` | branch → PR into own fork | Phoenix-2026, Robot-2026 | **clone** — Phoenix end to end, then `RobotContainer` |
| **M4** | `m4-control/` | branch → PR into own fork | Robot-2026 | **clone** to read `ShooterSubsystem`; **fork + PR** for your first real contribution |
| **M5** | `m5-auto-vision/` | branch → PR into own fork | Robot-2026 | **clone** — `vision/`, `LaunchCalculator` |
| **M6** | `m6-testing/` *(warm-up)* | branch → PR into own fork | Robot-2026 | **fork + PR** — tests for a class that has none. **This is the milestone** |
| **M7** | — | **branch on upstream** | Robot-2026 | commit access. Ship a feature |
| **M8** | — | **branch on upstream** | Robot-2026 | commit access, in-season. Own a subsystem |

For the exact commands at each stage, see [the git workflow](git-workflow.md).

---

## Why you only read until M4

Through M3 you are learning what the code *is*. Everything `Robot-2026` is worth to you in those milestones is in the reading:

- **M1** — open `Constants.java` and find three different naming conventions in one file. Nobody wrote the rule down. That is what inconsistency costs.
- **M2** — `preferences/` is an abstract generic class, four subclasses, and `Supplier<T>` doing real work. It is the best file set in the codebase for this milestone.
- **M3** — `Phoenix-2026` is twenty-two files on the same stack as the competition robot, small enough to read in one sitting, and it contains both command idioms.

None of that needs write access. And a change to competition code needs someone who can judge whether it is safe — which is what M3 and M4 are building.

---

## M4 — your first real pull request

Opening one pull request against a real robot repository is a **required** part of finishing M4. A documentation fix or a typo correction counts. The point is the ritual, not the size of the change.

If you want a real one: `DriveMotorConfigs.java` has four near-identical `create*Slot0Configs()` methods, one per swerve corner, each rebuilding the same five fields. CTRE's `.clone()` pattern collapses them. It is about a twenty-minute change and it makes the file substantially shorter.

Do it now rather than at M6. M6's pull request is assessed; this one is practice.

---

## M6 — the milestone that is a pull request

**Your deliverable is a merged pull request adding tests to `Robot-2026`.** The `projects/m6-testing/` project is the warm-up that makes it writable; the real pull request is what you are actually graded on.

Pick your target from [`UNTESTED-TARGETS.md`](UNTESTED-TARGETS.md) — a list of classes in `Robot-2026` that have no test coverage yet. Cross it off when your pull request merges.

### "Wait, isn't that circular?"

Good catch, and no.

The Kickoff Gate says you need M6 before you get commit access to `Robot-2026`. M6 says your deliverable is a pull request to `Robot-2026`. That looks like a loop.

It isn't, because **the pull request comes from a fork.** A fork needs no commit access from anyone — that is the entire reason forks exist, and it is exactly why fork-and-pull-request is the ritual for everything before the Gate. You are proposing a change, not making one. A mentor still has to merge it.

### The hard rule about what you may test

> **Pure logic only. No timing, no simulation, no hardware.**

Test `LaunchCalculator`, the `LaunchRequestBuilder` implementations, unit conversions, state-machine transitions. Things that take numbers in and give numbers out.

**Why this is a rule and not a suggestion:** `Robot-2026`'s CI runs `./gradlew build`, which runs the tests. The moment your pull request merges, every team pull request from then on has to pass them. If your test depends on timing, it will pass on your laptop and fail intermittently in CI — and in February that means a test you wrote in November is blocking somebody's competition fix at 11pm.

`PhoenixTestSupport.java` and its simulation delays stay in `projects/m6-testing/`. That is what the practice project is for.

### What your pull request actually does for the team

`Robot-2026` has forty-seven Java files and, as of August 2026, **no `src/test` directory at all.** JUnit is configured in `build.gradle` and has never run.

So the first M6 pull request does not just add tests. `./gradlew build` runs `check`, `check` runs `test` — meaning that from the moment it merges, **the team's CI is gating on tests for the first time.** You are not doing an exercise. You are turning something on.

---

## M7 and M8 — you are on the team

Past the Gate, you have commit access and you branch directly on `Robot-2026`. No fork. At that point the ladder is finished and you work the way everyone else does.
````

- [ ] **Step 4: Run the tests to verify they pass**

Run: `poetry run pytest tests/test_process_docs.py -v`
Expected: PASS, 24 tests — 8 from Task 4, 16 added here (9 milestone rows + 4 contact levels + 3 single assertions).

- [ ] **Step 5: Verify the cross-links resolve**

```bash
grep -o '](.*\.md)' programming/robot-repo-ladder.md programming/git-workflow.md
```

Expected: `git-workflow.md`, `robot-repo-ladder.md`, `UNTESTED-TARGETS.md`. The first two exist. `UNTESTED-TARGETS.md` does not yet — it is built in Plan 3 alongside M6. Add it to `docs-links.yml`'s exclusion list in Task 7, and remove the exclusion when Plan 3 creates the file.

- [ ] **Step 6: Commit**

```bash
git add programming/robot-repo-ladder.md tests/test_process_docs.py
git commit -m "Add robot-repo-ladder.md: browse/clone/fork/branch per milestone"
```

---

## Task 6: `tests.lock` generator and verifier

Spec §12.2 layer 1. A SHA-256 manifest of every given test file and read-only source file, verified in CI.

**Files:**
- Create: `tools/tests_lock.py`, `tests/test_tests_lock.py`

**Interfaces:**
- Consumes: `tools` package from Task 2
- Produces: `compute_manifest(root, patterns) -> dict[str, str]`, `write_manifest(root, patterns, out) -> None`, `verify_manifest(root, manifest) -> list[str]`; used by `student-check.yml` and `skeleton-integrity.yml` in Task 8

- [ ] **Step 1: Write the failing test**

Create `tests/test_tests_lock.py`:

```python
"""tests.lock makes tampering with given tests visible (spec §12.2)."""

from pathlib import Path

import pytest

from tools.tests_lock import compute_manifest, verify_manifest, write_manifest

PATTERNS = ["projects/*/src/test/**/*.java", "projects/*/src/main/java/**/plant/*.java"]


@pytest.fixture
def project(tmp_path: Path) -> Path:
    given = tmp_path / "projects" / "m1" / "src" / "test" / "java"
    given.mkdir(parents=True)
    (given / "DeadbandTest.java").write_text("class DeadbandTest {}", encoding="utf-8")
    plant = tmp_path / "projects" / "m4" / "src" / "main" / "java" / "plant"
    plant.mkdir(parents=True)
    (plant / "FlywheelPlant.java").write_text("class FlywheelPlant {}", encoding="utf-8")
    return tmp_path


def test_manifest_covers_every_matched_file(project):
    manifest = compute_manifest(project, PATTERNS)
    assert set(manifest) == {
        "projects/m1/src/test/java/DeadbandTest.java",
        "projects/m4/src/main/java/plant/FlywheelPlant.java",
    }


def test_manifest_hashes_are_sha256_hex(project):
    manifest = compute_manifest(project, PATTERNS)
    assert all(len(digest) == 64 and int(digest, 16) >= 0 for digest in manifest.values())


def test_unmodified_tree_verifies_clean(project, tmp_path):
    lock = tmp_path / "tests.lock"
    write_manifest(project, PATTERNS, lock)
    assert verify_manifest(project, lock) == []


def test_modified_given_test_is_reported(project, tmp_path):
    lock = tmp_path / "tests.lock"
    write_manifest(project, PATTERNS, lock)
    target = project / "projects/m1/src/test/java/DeadbandTest.java"
    target.write_text("class DeadbandTest { /* I deleted the assertion */ }", encoding="utf-8")
    assert verify_manifest(project, lock) == ["projects/m1/src/test/java/DeadbandTest.java"]


def test_deleted_given_test_is_reported(project, tmp_path):
    lock = tmp_path / "tests.lock"
    write_manifest(project, PATTERNS, lock)
    (project / "projects/m1/src/test/java/DeadbandTest.java").unlink()
    assert verify_manifest(project, lock) == ["projects/m1/src/test/java/DeadbandTest.java"]


def test_student_own_test_file_is_not_locked(project, tmp_path):
    lock = tmp_path / "tests.lock"
    write_manifest(project, PATTERNS, lock)
    student = project / "projects" / "m2" / "src" / "test" / "java"
    student.mkdir(parents=True)
    (student / "MyOwnTest.java").write_text("class MyOwnTest {}", encoding="utf-8")
    assert verify_manifest(project, lock) == []
```

- [ ] **Step 2: Run it to verify it fails**

Run: `poetry run pytest tests/test_tests_lock.py -v`
Expected: FAIL with `ModuleNotFoundError: No module named 'tools.tests_lock'`.

- [ ] **Step 3: Write the implementation**

Create `tools/tests_lock.py`:

```python
"""SHA-256 manifest of given test files (spec §12.2).

The check exists to make tampering *visible*, not to be uncircumventable.
Anyone determined can rewrite history. Visible is enough — which is why the
failure message invites disagreement rather than making an accusation.
"""

from __future__ import annotations

import hashlib
import sys
from pathlib import Path

TAMPER_MESSAGE = (
    "This test is part of the milestone. If you think it is wrong, say so in "
    "the pull request and a mentor will look."
)


def _sha256(path: Path) -> str:
    digest = hashlib.sha256()
    digest.update(path.read_bytes())
    return digest.hexdigest()


def compute_manifest(root: Path, patterns: list[str]) -> dict[str, str]:
    """Map each matched file's root-relative POSIX path to its SHA-256 hex digest."""
    manifest: dict[str, str] = {}
    for pattern in patterns:
        for path in sorted(root.glob(pattern)):
            if path.is_file():
                manifest[path.relative_to(root).as_posix()] = _sha256(path)
    return manifest


def write_manifest(root: Path, patterns: list[str], out: Path) -> None:
    """Write a `sha256  path` manifest, sorted by path for stable diffs."""
    manifest = compute_manifest(root, patterns)
    lines = [f"{digest}  {rel}" for rel, digest in sorted(manifest.items())]
    out.write_text("\n".join(lines) + "\n", encoding="utf-8")


def verify_manifest(root: Path, manifest: Path) -> list[str]:
    """Return root-relative paths that were modified or deleted, sorted."""
    changed: list[str] = []
    for line in manifest.read_text(encoding="utf-8").splitlines():
        if not line.strip():
            continue
        expected, rel = line.split("  ", 1)
        target = root / rel
        if not target.is_file() or _sha256(target) != expected:
            changed.append(rel)
    return sorted(changed)


PATTERNS = [
    "projects/*/src/test/**/*.java",
    "projects/*/src/main/java/**/plant/*.java",
    "projects/testouts/*/src/test/**/*.java",
]


def main(argv: list[str]) -> int:
    if len(argv) != 2 or argv[1] not in {"write", "verify"}:
        print("usage: tests_lock.py [write|verify]", file=sys.stderr)
        return 2
    root = Path.cwd()
    lock = root / "tests.lock"
    if argv[1] == "write":
        write_manifest(root, PATTERNS, lock)
        print(f"wrote {lock}")
        return 0
    changed = verify_manifest(root, lock)
    if changed:
        for rel in changed:
            print(f"MODIFIED GIVEN TEST: {rel}", file=sys.stderr)
        print(f"\n{TAMPER_MESSAGE}", file=sys.stderr)
        return 1
    print("tests.lock OK")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `poetry run pytest tests/test_tests_lock.py -v`
Expected: PASS, 6 tests.

- [ ] **Step 5: Generate the initial empty manifest**

```bash
poetry run python tools/tests_lock.py write
cat tests.lock
```

Expected: an empty file. `projects/` has no skeletons yet — Plan 2 populates it, and `skeleton-integrity.yml` regenerates the manifest as each milestone lands.

- [ ] **Step 6: Commit**

```bash
git add tools/tests_lock.py tests/test_tests_lock.py tests.lock
git commit -m "Add tests.lock generator and verifier with non-accusatory failure message"
```

---

## Task 7: Changed-project matrix deriver

Spec §7.3 mechanic 2. A Track A student must not wait out nine project builds to check one milestone.

**Files:**
- Create: `tools/changed_projects.py`, `tests/test_changed_projects.py`

**Interfaces:**
- Consumes: `tools` package from Task 2
- Produces: `milestone_dirs(changed_paths: Iterable[str]) -> list[str]`, and a CLI emitting a JSON array for `fromJSON()` in `student-check.yml` (Task 8)

- [ ] **Step 1: Write the failing test**

Create `tests/test_changed_projects.py`:

```python
"""Derives the CI build matrix from a diff (spec §7.3 mechanic 2)."""

import json
import subprocess
import sys
from pathlib import Path

from tools.changed_projects import milestone_dirs

REPO = Path(__file__).resolve().parents[1]


def test_source_change_selects_its_project():
    changed = ["projects/m1-java-fundamentals/src/main/java/frc/training/m1/Deadband.java"]
    assert milestone_dirs(changed) == ["projects/m1-java-fundamentals"]


def test_prose_change_selects_nothing():
    changed = ["programming/milestones/m3-wpilib-command-based.md", "README.md"]
    assert milestone_dirs(changed) == []


def test_multiple_projects_are_deduplicated_and_sorted():
    changed = [
        "projects/m3-command-based/src/main/java/frc/robot/Robot.java",
        "projects/m1-java-fundamentals/build.gradle",
        "projects/m3-command-based/build.gradle",
    ]
    assert milestone_dirs(changed) == [
        "projects/m1-java-fundamentals",
        "projects/m3-command-based",
    ]


def test_testout_fixtures_are_selected_at_fixture_level():
    changed = ["projects/testouts/t1-find-the-bug/src/main/java/Bug.java"]
    assert milestone_dirs(changed) == ["projects/testouts/t1-find-the-bug"]


def test_file_directly_in_projects_root_selects_nothing():
    assert milestone_dirs(["projects/README.md"]) == []


def test_deleted_paths_are_still_selected():
    """A deleted file still means its project must rebuild."""
    assert milestone_dirs(["projects/m2-oop/src/main/java/Gone.java"]) == ["projects/m2-oop"]


def test_cli_emits_a_json_array(tmp_path):
    changed = tmp_path / "changed.txt"
    changed.write_text("projects/m1-java-fundamentals/build.gradle\nREADME.md\n", encoding="utf-8")
    result = subprocess.run(
        [sys.executable, "tools/changed_projects.py", "--from-file", str(changed)],
        capture_output=True,
        text=True,
        cwd=REPO,
        check=True,
    )
    assert json.loads(result.stdout) == ["projects/m1-java-fundamentals"]


def test_cli_emits_empty_array_when_nothing_matched(tmp_path):
    changed = tmp_path / "changed.txt"
    changed.write_text("programming/README.md\n", encoding="utf-8")
    result = subprocess.run(
        [sys.executable, "tools/changed_projects.py", "--from-file", str(changed)],
        capture_output=True,
        text=True,
        cwd=REPO,
        check=True,
    )
    assert json.loads(result.stdout) == []
```

- [ ] **Step 2: Run it to verify it fails**

Run: `poetry run pytest tests/test_changed_projects.py -v`
Expected: FAIL with `ModuleNotFoundError: No module named 'tools.changed_projects'`.

- [ ] **Step 3: Write the implementation**

Create `tools/changed_projects.py`:

```python
"""Derive the CI build matrix from a list of changed paths (spec §7.3 mechanic 2).

Emits a JSON array for GitHub Actions `fromJSON()`. An empty array means no
Gradle project needs building, and the calling job should skip entirely.
"""

from __future__ import annotations

import argparse
import json
import sys
from collections.abc import Iterable
from pathlib import PurePosixPath

PROJECTS_ROOT = "projects"
TESTOUTS = "testouts"


def milestone_dirs(changed_paths: Iterable[str]) -> list[str]:
    """Return the sorted, deduplicated set of standalone Gradle projects to build."""
    selected: set[str] = set()
    for raw in changed_paths:
        rel = raw.strip()
        if not rel:
            continue
        parts = PurePosixPath(rel).parts
        if len(parts) < 2 or parts[0] != PROJECTS_ROOT:
            continue
        if parts[1] == TESTOUTS:
            # Fixtures nest one level deeper: projects/testouts/<fixture>/...
            if len(parts) >= 4:
                selected.add(f"{PROJECTS_ROOT}/{TESTOUTS}/{parts[2]}")
            continue
        if len(parts) >= 3:
            selected.add(f"{PROJECTS_ROOT}/{parts[1]}")
    return sorted(selected)


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    source = parser.add_mutually_exclusive_group(required=True)
    source.add_argument("--from-file", help="File containing one changed path per line")
    source.add_argument("--from-stdin", action="store_true", help="Read paths from stdin")
    args = parser.parse_args()

    if args.from_stdin:
        lines = sys.stdin.read().splitlines()
    else:
        with open(args.from_file, encoding="utf-8") as handle:
            lines = handle.read().splitlines()

    print(json.dumps(milestone_dirs(lines)))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `poetry run pytest tests/test_changed_projects.py -v`
Expected: PASS, 8 tests.

- [ ] **Step 5: Commit**

```bash
git add tools/changed_projects.py tests/test_changed_projects.py
git commit -m "Add changed-project matrix deriver so students build only their milestone"
```

---

## Task 8: Publication and link-check workflows

**Files:**
- Create: `.github/workflows/pages.yml`, `.github/workflows/docs-links.yml`, `.lycheeignore`

**Interfaces:**
- Consumes: `tools/site_guard.py` (Task 3), `00-index.md` (Task 3)
- Produces: a deployed site; the gating pattern Task 9 reuses

- [ ] **Step 1: Write the Pages workflow**

Create `.github/workflows/pages.yml`:

```yaml
name: Publish Pages

on:
  push:
    branches: [main]
    paths:
      - '**.md'
      - '00-index.md'
      - 'tools/site_guard.py'
      - '.github/workflows/pages.yml'
  workflow_dispatch:

permissions:
  contents: read
  pages: write
  id-token: write

concurrency:
  group: pages
  cancel-in-progress: false

jobs:
  build:
    if: github.repository == 'IgniteRobotics/docs'
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-python@v5
        with:
          python-version: '3.11'

      - name: Install doc-builder
        run: pip install 'doc-builder @ git+https://github.com/IgniteRobotics/doc-builder@main'

      - name: Build site
        run: |
          doc-builder build-from-source . \
            --output ./_site \
            --site-name "Ignite Robotics" \
            --no-split

      - name: Guard against published source files
        run: python tools/site_guard.py ./_site

      - uses: actions/upload-pages-artifact@v3
        with:
          path: ./_site

  deploy:
    needs: build
    if: github.repository == 'IgniteRobotics/docs'
    runs-on: ubuntu-latest
    environment:
      name: github-pages
      url: ${{ steps.deployment.outputs.page_url }}
    steps:
      - id: deployment
        uses: actions/deploy-pages@v4
```

> **If `IgniteRobotics/doc-builder` is a private repository**, the `pip install` line fails with a 128. Fix by adding a repository secret `DOC_BUILDER_TOKEN` (a fine-grained PAT with read access to that one repo) and changing the URL to `git+https://x-access-token:${{ secrets.DOC_BUILDER_TOKEN }}@github.com/IgniteRobotics/doc-builder@main`. Check visibility before assuming — `gh` is not installed on this machine, so check in a browser.

- [ ] **Step 2: Write the link-check workflow**

Create `.github/workflows/docs-links.yml`:

```yaml
name: Check Documentation Links

on:
  pull_request:
    paths: ['**.md']
  schedule:
    - cron: '0 12 * * 1'
  workflow_dispatch:

permissions:
  contents: read
  issues: write

jobs:
  links:
    if: github.repository == 'IgniteRobotics/docs'
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Check links
        uses: lycheeverse/lychee-action@v2
        with:
          args: >-
            --no-progress
            --accept 200,206,403,429
            --exclude-path ./projects
            './**/*.md'
          fail: true
```

`403` and `429` are accepted because Chief Delphi bot-blocks automated fetches — a known-good link would otherwise fail the build weekly. This is the same class of failure recorded in the spec's §20 audit block.

- [ ] **Step 3: Add the link-check exclusions**

Create `.lycheeignore`:

```
# Created by Plan 3 (M6) alongside UNTESTED-TARGETS.md. Remove this line then.
.*UNTESTED-TARGETS\.md
# Requires authentication; verified manually each season.
https://github\.com/IgniteRobotics/.*
```

- [ ] **Step 4: Validate both workflows parse**

```bash
pip install --user yamllint
yamllint -d relaxed .github/workflows/pages.yml .github/workflows/docs-links.yml
```

Expected: no errors. If `actionlint` is available (`brew install actionlint`), run it too — it catches `fromJSON` and context errors that yamllint cannot see.

- [ ] **Step 5: Verify the gate string matches the intended remote**

```bash
grep -c "github.repository == 'IgniteRobotics/docs'" .github/workflows/pages.yml
```

Expected: `2` — the `build` and `deploy` jobs. If Task 10 creates the remote under a different owner or name, every gate string in this task and Task 9 must change with it.

- [ ] **Step 6: Commit**

```bash
git add .github/workflows/pages.yml .github/workflows/docs-links.yml .lycheeignore
git commit -m "Add Pages and link-check workflows, gated to the canonical repository"
```

---

## Task 9: Skeleton and student-check workflows

The two Gradle workflows. Neither can be exercised end to end until Plan 2 creates `projects/m0-onboarding/` — path filters mean they are inert no-ops until then, which is correct and expected.

**Files:**
- Create: `.github/workflows/skeleton-integrity.yml`, `.github/workflows/student-check.yml`, `.github/PULL_REQUEST_TEMPLATE.md`, `.github/CODEOWNERS`

**Interfaces:**
- Consumes: `tools/tests_lock.py` (Task 6), `tools/changed_projects.py` (Task 7)
- Produces: the check students see on their fork pull requests

- [ ] **Step 1: Write the student check workflow**

Create `.github/workflows/student-check.yml`:

```yaml
name: Student Check

on:
  pull_request:

permissions:
  contents: read
  pull-requests: write

jobs:
  matrix:
    runs-on: ubuntu-latest
    outputs:
      projects: ${{ steps.derive.outputs.projects }}
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - uses: actions/setup-python@v5
        with:
          python-version: '3.11'

      - name: Derive changed projects
        id: derive
        run: |
          git fetch origin "${{ github.base_ref }}" --depth=1
          git diff --name-only "origin/${{ github.base_ref }}...HEAD" > changed.txt
          echo "Changed files:"; cat changed.txt
          echo "projects=$(python tools/changed_projects.py --from-file changed.txt)" >> "$GITHUB_OUTPUT"

      - name: Verify given tests are unmodified
        run: python tools/tests_lock.py verify

  build:
    needs: matrix
    if: needs.matrix.outputs.projects != '[]'
    runs-on: ubuntu-latest
    strategy:
      fail-fast: false
      matrix:
        project: ${{ fromJSON(needs.matrix.outputs.projects) }}
    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: 'gradle'

      - name: Grant execute permission for gradlew
        working-directory: ${{ matrix.project }}
        run: chmod +x gradlew

      - name: Build with Gradle
        working-directory: ${{ matrix.project }}
        run: ./gradlew build --console=plain

      - name: Check code formatting
        id: spotless
        working-directory: ${{ matrix.project }}
        continue-on-error: true
        run: |
          set -o pipefail
          if ./gradlew spotlessCheck --console=plain 2>&1 | tee spotless-output.txt; then
            echo "formatted=true" >> "$GITHUB_OUTPUT"
          else
            echo "formatted=false" >> "$GITHUB_OUTPUT"
          fi

      - name: Comment on formatting violations
        if: steps.spotless.outputs.formatted == 'false'
        uses: actions/github-script@v7
        with:
          script: |
            github.rest.issues.createComment({
              issue_number: context.issue.number,
              owner: context.repo.owner,
              repo: context.repo.repo,
              body: [
                '### Formatting check failed in `${{ matrix.project }}`',
                '',
                'Run this and commit the result:',
                '',
                '```bash',
                'cd ${{ matrix.project }} && ./gradlew spotlessApply',
                '```',
                '',
                'This is not a judgement of your code. The formatter is automatic ' +
                'so nobody has to argue about braces in review.'
              ].join('\n')
            })
```

- [ ] **Step 2: Write the skeleton integrity workflow**

Create `.github/workflows/skeleton-integrity.yml`:

```yaml
name: Skeleton Integrity

on:
  pull_request:
    paths: ['projects/**', 'tests.lock', '.github/workflows/skeleton-integrity.yml']
  push:
    branches: [main]
    paths: ['projects/**', 'tests.lock']

permissions:
  contents: read

jobs:
  matrix:
    if: github.repository == 'IgniteRobotics/docs'
    runs-on: ubuntu-latest
    outputs:
      projects: ${{ steps.list.outputs.projects }}
    steps:
      - uses: actions/checkout@v4
      - name: List every skeleton project
        id: list
        run: |
          found=$(find projects -mindepth 2 -maxdepth 3 -name settings.gradle -printf '%h\n' \
            | sort | jq -R -s -c 'split("\n") | map(select(length > 0))')
          echo "projects=$found" >> "$GITHUB_OUTPUT"
          echo "Found: $found"

  integrity:
    needs: matrix
    if: needs.matrix.outputs.projects != '[]'
    runs-on: ubuntu-latest
    strategy:
      fail-fast: false
      matrix:
        project: ${{ fromJSON(needs.matrix.outputs.projects) }}
    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: 'gradle'

      - working-directory: ${{ matrix.project }}
        run: chmod +x gradlew

      - name: Skeleton compiles on a clean clone
        working-directory: ${{ matrix.project }}
        run: ./gradlew assemble --console=plain

      - name: Skeleton is already formatted
        working-directory: ${{ matrix.project }}
        run: ./gradlew spotlessCheck --console=plain

      - name: Inverted check — given tests MUST fail on the unmodified skeleton
        working-directory: ${{ matrix.project }}
        run: |
          if ./gradlew test --console=plain; then
            echo "::error::${{ matrix.project }} passes its own tests unmodified."
            echo "A stub is accidentally implementing itself. See spec §7.2."
            exit 1
          fi
          echo "OK: tests fail as intended on the unmodified skeleton."

  lock:
    if: github.repository == 'IgniteRobotics/docs'
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-python@v5
        with:
          python-version: '3.11'
      - name: tests.lock matches the tree
        run: |
          python tools/tests_lock.py write
          git diff --exit-code tests.lock \
            || { echo "::error::tests.lock is stale. Run: python tools/tests_lock.py write"; exit 1; }
```

- [ ] **Step 3: Write the pull request template**

Create `.github/PULL_REQUEST_TEMPLATE.md`:

```markdown
## What this changes

<!-- One or two sentences. What does this do that main does not? -->

## Milestone

<!-- e.g. M3, or "not a milestone" -->

## AI use disclosure

Required by `programming/ai-use-standard.md` §6. Your tier is set by the
milestones you have completed — check the one that applies.

- [ ] 🔴 **Red (M0–M2)** — no AI code generation was used
- [ ] 🟡 **Yellow (M3–M5)** — AI used for boilerplate, Javadoc, or stack-trace
      triage only. Not control logic, subsystem design, or gains
- [ ] 🟢 **Green (after M6)** — full agentic use

**What the tool did, in your words:**

<!-- e.g. "Copilot wrote the Javadoc on three methods. I wrote the logic and
     can explain every line." Be specific. "Some autocomplete" is not a
     disclosure. -->

## I can explain this

- [ ] I can explain every line of this diff to a mentor without notes
```

- [ ] **Step 4: Write CODEOWNERS**

Create `.github/CODEOWNERS`:

```
# Given tests and read-only source require mentor review (spec §12.2 layer 2).
# Replace @IgniteRobotics/mentors with the real team slug at Task 10.

/projects/*/src/test/                @IgniteRobotics/mentors
/projects/*/src/main/java/**/plant/   @IgniteRobotics/mentors
/projects/testouts/                   @IgniteRobotics/mentors
/tests.lock                           @IgniteRobotics/mentors
/tools/                               @IgniteRobotics/mentors
/.github/workflows/                   @IgniteRobotics/mentors
/programming/teaching-repo-spec.md    @IgniteRobotics/mentors
/programming/ai-use-standard.md       @IgniteRobotics/mentors
```

- [ ] **Step 5: Validate both workflows parse**

```bash
yamllint -d relaxed .github/workflows/skeleton-integrity.yml .github/workflows/student-check.yml
```

Expected: no errors.

- [ ] **Step 6: Verify the matrix deriver against a realistic diff**

```bash
printf 'projects/m1-java-fundamentals/src/main/java/frc/training/m1/Deadband.java\nprogramming/README.md\n' > /tmp/changed.txt
poetry run python tools/changed_projects.py --from-file /tmp/changed.txt
```

Expected: `["projects/m1-java-fundamentals"]`.

- [ ] **Step 7: Commit**

```bash
git add .github/workflows/skeleton-integrity.yml .github/workflows/student-check.yml \
        .github/PULL_REQUEST_TEMPLATE.md .github/CODEOWNERS
git commit -m "Add skeleton-integrity and student-check workflows, PR template, CODEOWNERS"
```

---

## Task 10: `CONTRIBUTING.md` and `SETUP.md`

The two documents a student reads before writing a line. `SETUP.md` carries the fork-Actions step, which spec §15 rates the highest-likelihood failure in M0.

**Files:**
- Create: `CONTRIBUTING.md`, `SETUP.md`
- Modify: `tests/test_process_docs.py`

**Interfaces:**
- Consumes: `programming/git-workflow.md` (Task 4)
- Produces: nothing later tasks import

- [ ] **Step 1: Write the failing test**

Append to `tests/test_process_docs.py`:

```python
SETUP = REPO / "SETUP.md"
CONTRIBUTING = REPO / "CONTRIBUTING.md"


def test_setup_documents_enabling_actions_on_a_fork():
    text = SETUP.read_text(encoding="utf-8")
    assert "I understand my workflows" in text


def test_setup_pins_the_toolchain_versions():
    text = SETUP.read_text(encoding="utf-8")
    for version in ("2026", "17"):
        assert version in text


def test_contributing_states_the_pure_logic_rule():
    text = CONTRIBUTING.read_text(encoding="utf-8")
    assert "No timing, no simulation, no hardware." in text


def test_contributing_links_the_git_workflow():
    text = CONTRIBUTING.read_text(encoding="utf-8")
    assert "programming/git-workflow.md" in text
```

- [ ] **Step 2: Run it to verify it fails**

Run: `poetry run pytest tests/test_process_docs.py -v`
Expected: FAIL — `FileNotFoundError` on `SETUP.md`.

- [ ] **Step 3: Write `SETUP.md`**

```markdown
# Setup

Get a working development environment. Do this once. Budget an hour the first time
and do not be discouraged — installing toolchains is genuinely the least fun part
of this and it never gets harder than today.

---

## 1. Install WPILib 2026

Download the 2026 WPILib installer for your platform from the
[WPILib releases page](https://github.com/wpilibsuite/allwpilib/releases) and run it.
Choose **"Everything"** when it asks, and **"Install for this User"** unless you
have a reason not to.

This installs a bundled JDK 17, a bundled VS Code, and the WPILib extension.
Use the VS Code it installs — not one you already had. The bundled one knows
where the JDK is.

## 2. Verify Java

Open the WPILib VS Code, then a terminal inside it (`` Ctrl+` ``):

```bash
java -version
```

Expected: version 17. If you see something else, VS Code is not using the bundled
JDK — ask a mentor rather than installing another Java.

## 3. Fork and clone this repository

Follow [the git workflow](programming/git-workflow.md), M0 section. It has the
exact commands.

## 4. ⚠️ Enable Actions on your fork

**Do this before you open your first pull request.**

GitHub disables Actions on forked repositories by default. Until you turn them on,
your pull requests show **no checks at all** — which looks like the repository is
broken, and it isn't.

1. Go to your fork on github.com
2. Click the **Actions** tab
3. Click the green **"I understand my workflows, go ahead and enable them"** button

If you have already opened a pull request without doing this, enable Actions and
then push any new commit to that branch — the checks will run.

## 5. Verify the build

```bash
cd projects/m0-onboarding
./gradlew build
```

Expected: `BUILD SUCCESSFUL`. The first run downloads a lot and can take several
minutes; later runs take seconds.

If it fails, copy the **whole** error — not a screenshot of part of it — and bring
it to a mentor. Build errors are diagnosable from their text and nearly impossible
to guess at from a description.

## 6. Verify the simulator

In VS Code: `Ctrl+Shift+P` → **"WPILib: Simulate Robot Code"** → check
**"Sim GUI"** → OK.

Expected: a window with a Robot State panel and a Joysticks panel. That window is
the robot, as far as your laptop is concerned. You will spend a lot of time here —
every project in this curriculum is completable without touching hardware.

---

## Known problems

**`./gradlew: Permission denied` on macOS or Linux.** Run `chmod +x gradlew`.

**"No Java executable found."** You are in a VS Code that isn't the WPILib one.
Close it and open the WPILib VS Code from your applications folder.

**The build downloads forever on shop wifi.** Expected the first time. Start it,
work on something else, come back. Later builds are cached.

**"Could not resolve all files for configuration."** Usually a network problem
mid-download. Run it again before assuming anything is broken.
```

- [ ] **Step 4: Write `CONTRIBUTING.md`**

```markdown
# Contributing

How work gets into this repository, and into the robot code.

---

## The git ritual

See [the git workflow](programming/git-workflow.md). The short version:

> **Clone what you're reading. Fork what you'll change. Branch to work.**

You fork this repository once at M0. Every milestone after that is a branch in
that same fork, with a pull request into your **own** fork — not this one.

---

## Every pull request

- **Branch first.** Never commit to `main`, even your own.
- **CI must be green.** `./gradlew build` and `./gradlew spotlessCheck`, which the
  Student Check workflow runs for you.
- **Fill in the AI disclosure.** It is in the pull request template and it is not
  optional. Your tier comes from the milestones you have finished, and
  `programming/ai-use-standard.md` explains why the boundaries are where they are.
- **You can explain every line.** This is the actual bar. A mentor will ask.

---

## Formatting

Do not argue with the formatter, and do not format by hand:

```bash
./gradlew spotlessApply
```

It runs automatically before every compile. If CI complains about formatting,
run it and commit the result.

---

## Given tests are not yours to change

Every milestone ships tests you must make pass. They are recorded in `tests.lock`
and CI checks them.

If you think a given test is **wrong** — say so in the pull request. That happens,
you might be right, and a mentor will look. What does not work is quietly deleting
it: the lock check will notice, and it will notice in a way that is more awkward
than just asking.

---

## Pull requests to `Robot-2026`

At M4 and M6 you contribute to the real competition code from a fork. One extra
rule applies there, and it is a hard one:

> **Pure logic only. No timing, no simulation, no hardware.**

Test things that take numbers in and give numbers out — `LaunchCalculator`, the
`LaunchRequestBuilder` implementations, unit conversions, state-machine transitions.

**Why.** `Robot-2026`'s CI runs `./gradlew build`, which runs the tests. Once your
test merges, every team pull request has to pass it. A timing-dependent test passes
on your laptop and fails intermittently in CI — and in February that means a test
you wrote in November is blocking somebody's competition fix at 11pm.

The simulation-timing helpers live in `projects/m6-testing/`. That is what the
practice project is for.
```

- [ ] **Step 5: Run the tests to verify they pass**

Run: `poetry run pytest -v`
Expected: PASS, all tests across all files.

- [ ] **Step 6: Commit**

```bash
git add CONTRIBUTING.md SETUP.md tests/test_process_docs.py
git commit -m "Add CONTRIBUTING.md and SETUP.md with the fork-Actions step"
```

---

## Task 11: Create the remote and go live

The only task with manual steps. Everything before this runs locally; nothing before this is visible to a student.

**Files:**
- Modify: `programming/README.md` (status table)

**Interfaces:**
- Consumes: every prior task
- Produces: a live site and working CI

- [ ] **Step 1: Confirm the full local suite is green**

```bash
poetry run pytest -v
```

Expected: PASS, every test. Do not proceed on a red suite — the workflows about to go live run this same code.

- [ ] **Step 2: Create the remote**

Create `IgniteRobotics/docs` on github.com as a **public** repository. Do not initialise it with a README, licence, or `.gitignore` — this repository already has history.

```bash
git remote add origin git@github.com:IgniteRobotics/docs.git
git remote -v
```

- [ ] **Step 3: Confirm the gate strings match reality**

```bash
grep -rn "github.repository ==" .github/workflows/
```

Every hit must read `'IgniteRobotics/docs'` and match the remote exactly. If the repository ended up under a different owner or name, fix every occurrence now — a mismatched gate silently skips CI forever, which is worse than failing.

- [ ] **Step 4: Push**

```bash
git push -u origin HEAD
```

- [ ] **Step 5: Open the pull request into `main`**

This branch is `feature/curriculum-2026-codebase-audit`. Open a pull request into `main` and confirm `Check Documentation Links` runs and passes. `Student Check` will report no projects to build — correct, `projects/` is empty until Plan 2.

- [ ] **Step 6: Merge, then enable Pages**

After merging: repository **Settings** → **Pages** → **Source: GitHub Actions**. Then **Actions** tab → **Publish Pages** → **Run workflow** on `main`.

- [ ] **Step 7: Verify the published site**

Open the URL from the deploy job. Check, by eye:

- The four track guides and all nine milestones render
- `programming/git-workflow.md` and `programming/robot-repo-ladder.md` render
- `teaching-repo-spec.md` renders **whole**, not split into section pages — if it is split, `--no-split` is not taking effect and the `§` cross-references are broken
- Navigating to `/projects/` 404s

- [ ] **Step 8: Create the mentors team**

Create the `@IgniteRobotics/mentors` team on GitHub and give it write access, or edit `.github/CODEOWNERS` to name individual mentors. CODEOWNERS silently does nothing if the team does not exist — and silently doing nothing is exactly the failure mode §12.2 layer 2 exists to prevent.

- [ ] **Step 9: Update the status table**

In `programming/README.md`, change the "Teaching repository for M0–M3 projects" row from **Not built** to reflect reality: foundation shipped, skeletons pending Plan 2. Add `git-workflow.md` and `robot-repo-ladder.md` to the "Everything in this folder" table.

- [ ] **Step 10: Commit**

```bash
git add programming/README.md
git commit -m "Update status table: teaching repository foundation is live"
git push
```

---

## Done when

- [ ] `poetry run pytest` is green — 48 tests across five files (1 toolchain, 5 site guard, 28 process docs, 6 tests.lock, 8 changed projects)
- [ ] The site is live and renders every prose page
- [ ] `python tools/site_guard.py ./_site` exits 0
- [ ] Navigating to `/projects/` on the published site 404s
- [ ] All four workflows are present and parse under `yamllint`
- [ ] `pages.yml`, `docs-links.yml`, and `skeleton-integrity.yml` skip grey on a fork
- [ ] `SETUP.md` documents enabling Actions on a fork
- [ ] `git-workflow.md` states the §5.1 rule and gives copy-pasteable commands
- [ ] `robot-repo-ladder.md` contains the matrix and explains the M6 non-circularity
- [ ] The gradle test-lifecycle open item in spec §17 is answered with evidence
- [ ] `@IgniteRobotics/mentors` exists, or CODEOWNERS names real people

## Deliberately not in this plan

- **Nine skeleton projects and five test-out fixtures** — Plan 2 (M0–M3) and Plan 3 (M4–M8)
- **`UNTESTED-TARGETS.md`** — Plan 3, with M6. `.lycheeignore` excludes the dead link until then
- **The solutions repository and its cross-check** — starts with Plan 2's first milestone
- **Curriculum corrections** — Plan 4, and the §7.3 correction depends on Task 1's finding

## Risks carried into execution

| Risk | Surfaces at | Response |
|---|---|---|
| `doc-builder` copies non-indexed files, publishing `projects/` | Task 3 step 6 | Switch to explicit `mkdocs.yml` — step 7 |
| `doc-builder` mishandles standard markdown links (its samples use `[[wikilinks]]`) | Task 3 step 6 | Same fallback; or convert `00-index.md` to wikilink syntax |
| `IgniteRobotics/doc-builder` is private, breaking `pip install` in CI | Task 8 step 1 | `DOC_BUILDER_TOKEN` secret — noted inline |
| `--no-split` does not prevent splitting of the 746-line spec | Task 11 step 7 | Split `teaching-repo-spec.md` by section, or raise `--split-threshold` above 800 |
| Gate string does not match the created repository | Task 11 step 3 | Explicit verification step before the first push |
| `@IgniteRobotics/mentors` does not exist, so CODEOWNERS is inert | Task 11 step 8 | Explicit creation step; CODEOWNERS fails silently otherwise |
