# Task 1: `changed_projects.py` must require a `settings.gradle`

The only task in this plan that fixes a live defect. `tools/changed_projects.py` documents itself as returning *"the sorted, deduplicated set of standalone Gradle projects to build"* and never checks that they are Gradle projects — any changed path shaped `projects/<name>/<something>` is selected. Every directory under `projects/` has been a Gradle project until now, so it has never fired. **M7 and M8 are the first that are not**, and `student-check.yml` feeds this list into a matrix whose first step is `chmod +x gradlew`. Left unfixed, the first student pull request touching a Markdown file in either new project fails CI on `main` with `chmod: cannot access 'gradlew': No such file or directory` — a failure that has nothing to do with the student's change.

**Read spec §3 before starting.** It carries the reproduction and the two constraints on the fix.

**Files:**
- Modify: `tools/changed_projects.py`
- Modify: `tests/test_changed_projects.py`

**Consumes:** nothing.
**Produces:** `milestone_dirs(changed_paths, root=REPO_ROOT)` — same return type as today (a sorted `list[str]` of root-relative POSIX directory paths), now filtered to directories that actually contain a `settings.gradle`, with an injectable `root` so tests can build fixture trees.

**Lane:** `prerequisite`. Everything else in this plan waits on this task. Work in the main worktree — this touches neither lane's files.

---

- [ ] **Step 1: Reproduce the defect before changing anything**

```bash
cd /Users/y0shi/workspace/ignite/docs
printf 'projects/m7-ai-assisted/FEATURE-BACKLOG.md\nprojects/m8-ownership/README.md\n' \
  | poetry run python tools/changed_projects.py --from-stdin
```

Expected, and the reason this task exists:

```
["projects/m7-ai-assisted", "projects/m8-ownership"]
```

Neither directory exists yet and neither will ever have a `gradlew`. Paste this output in the task report — a fix whose defect was never observed is a fix nobody can review.

- [ ] **Step 2: Write the failing tests**

Append to `tests/test_changed_projects.py`. Note the existing tests call `milestone_dirs(changed)` with one argument; the new `root` parameter must default so those keep working untouched.

```python
def _gradle_project(root: Path, rel: str) -> None:
    """Create a directory under root that looks like a standalone Gradle project."""
    project = root / rel
    project.mkdir(parents=True, exist_ok=True)
    (project / "settings.gradle").write_text("rootProject.name = 'x'\n", encoding="utf-8")


def test_directory_without_settings_gradle_is_not_selected(tmp_path):
    """M7 and M8 ship documents, not Gradle projects. Selecting them puts a directory
    with no gradlew into student-check.yml's build matrix, which fails at chmod."""
    (tmp_path / "projects" / "m7-ai-assisted").mkdir(parents=True)
    (tmp_path / "projects" / "m7-ai-assisted" / "FEATURE-BACKLOG.md").write_text("x\n", encoding="utf-8")
    assert milestone_dirs(["projects/m7-ai-assisted/FEATURE-BACKLOG.md"], root=tmp_path) == []


def test_gradle_project_is_still_selected(tmp_path):
    _gradle_project(tmp_path, "projects/m5-auto-vision")
    changed = ["projects/m5-auto-vision/src/main/java/frc/robot/Robot.java"]
    assert milestone_dirs(changed, root=tmp_path) == ["projects/m5-auto-vision"]


def test_testout_fixture_with_settings_gradle_is_still_selected(tmp_path):
    _gradle_project(tmp_path, "projects/testouts/t5-drifting-auto")
    changed = ["projects/testouts/t5-drifting-auto/logs/a-run.wpilog"]
    assert milestone_dirs(changed, root=tmp_path) == ["projects/testouts/t5-drifting-auto"]


def test_mixed_change_selects_only_the_gradle_project(tmp_path):
    _gradle_project(tmp_path, "projects/m5-auto-vision")
    (tmp_path / "projects" / "m8-ownership").mkdir(parents=True)
    changed = [
        "projects/m8-ownership/README.md",
        "projects/m5-auto-vision/build.gradle",
    ]
    assert milestone_dirs(changed, root=tmp_path) == ["projects/m5-auto-vision"]


def test_deleting_an_entire_project_selects_nothing(tmp_path):
    """A project whose settings.gradle is gone has nothing left to build. This is the one
    behaviour change the filter introduces, and it is the correct one -- do not weaken the
    check to preserve it. Contrast test_deleted_paths_are_still_selected, which deletes a
    file *inside* a project whose settings.gradle survives."""
    assert milestone_dirs(["projects/deleted-thing/src/main/java/Gone.java"], root=tmp_path) == []
```

Add `from pathlib import Path` to the imports if it is not already there — it is, at the top of the existing file.

- [ ] **Step 3: Run the new tests and watch them fail**

```bash
poetry run pytest tests/test_changed_projects.py -v
```

Expected: `test_gradle_project_is_still_selected`, `test_testout_fixture_with_settings_gradle_is_still_selected` and the rest **error** with `TypeError: milestone_dirs() got an unexpected keyword argument 'root'`, and the two exclusion tests fail on that same error. That is the correct failure — the parameter does not exist yet.

**Do not skip this step.** A guard nobody has seen fail is a guard nobody knows works, and this repository has shipped exactly that before.

- [ ] **Step 4: Fix the tool**

`tools/changed_projects.py` — replace the `milestone_dirs` function and add the root constant. The existing module already has `PROJECTS_ROOT` and `TESTOUTS`; keep them.

```python
REPO_ROOT = Path(__file__).resolve().parents[1]


def _is_gradle_project(root: Path, rel: str) -> bool:
    """A standalone Gradle project has its own settings.gradle (spec §4.2).

    Checked against the filesystem rather than inferred from the path, because
    projects/m7-ai-assisted/ and projects/m8-ownership/ are directories of documents --
    the first entries under projects/ that are not Gradle projects at all. Selecting one
    puts a directory with no gradlew into student-check.yml's build matrix, where the
    first step is `chmod +x gradlew` and the failure reads like a broken toolchain to
    the student whose Markdown change triggered it.
    """
    return (root / rel / "settings.gradle").is_file()


def milestone_dirs(changed_paths: Iterable[str], root: Path = REPO_ROOT) -> list[str]:
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
    return sorted(d for d in selected if _is_gradle_project(root, d))
```

`Path` must be imported. The module currently imports `PurePosixPath` from `pathlib`; change that import to bring in both:

```python
from pathlib import Path, PurePosixPath
```

- [ ] **Step 5: Run the tests and watch them pass**

```bash
poetry run pytest tests/test_changed_projects.py -v
```

Expected: all tests pass, including the eight that existed before. If `test_deleted_paths_are_still_selected` fails, stop and read it: it deletes a file inside `projects/m2-oop`, whose `settings.gradle` is still on disk, so it must still pass. A failure there means the filter is checking the wrong path.

- [ ] **Step 6: Confirm the original reproduction is now dead**

```bash
printf 'projects/m7-ai-assisted/FEATURE-BACKLOG.md\nprojects/m8-ownership/README.md\n' \
  | poetry run python tools/changed_projects.py --from-stdin
```

Expected: `[]`

And confirm a real project still builds its matrix entry:

```bash
printf 'projects/m5-auto-vision/src/main/java/frc/robot/Robot.java\n' \
  | poetry run python tools/changed_projects.py --from-stdin
```

Expected: `["projects/m5-auto-vision"]`

Paste both. The second matters as much as the first — a filter that excludes everything would also make the first command print `[]`.

- [ ] **Step 7: Full suite, then commit**

```bash
poetry run pytest -q
```

Expected: all green.

```bash
git add tools/changed_projects.py tests/test_changed_projects.py
git commit -m "Require settings.gradle before selecting a CI build-matrix project

changed_projects.py documented itself as returning standalone Gradle projects
and never checked for one. Every directory under projects/ has been a Gradle
project so far, so the gap never fired -- M7 and M8 ship documents and are the
first that are not. student-check.yml feeds this list into a matrix whose first
step is chmod +x gradlew, so without this the first student PR touching a
Markdown file in either project fails on main with a toolchain-shaped error
that has nothing to do with their change.

milestone_dirs now takes an injectable root so the filter can be tested against
tmp_path fixtures rather than the checkout. Deleting a file inside a project
still selects it; deleting an entire project no longer does, which is correct --
there is nothing left to build."
```
