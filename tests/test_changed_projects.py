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


def _gradle_project(root: Path, rel: str) -> None:
    """Create a directory under root that looks like a standalone Gradle project."""
    project = root / rel
    project.mkdir(parents=True, exist_ok=True)
    (project / "settings.gradle").write_text("rootProject.name = 'x'\n", encoding="utf-8")


def test_directory_without_settings_gradle_is_not_selected(tmp_path):
    """M7, M8 and M9 ship documents, not Gradle projects. Selecting them puts a directory
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
    (tmp_path / "projects" / "m9-ownership").mkdir(parents=True)
    changed = [
        "projects/m9-ownership/README.md",
        "projects/m5-auto-vision/build.gradle",
    ]
    assert milestone_dirs(changed, root=tmp_path) == ["projects/m5-auto-vision"]


def test_deleting_an_entire_project_selects_nothing(tmp_path):
    """A project whose settings.gradle is gone has nothing left to build. This is the one
    behaviour change the filter introduces, and it is the correct one -- do not weaken the
    check to preserve it. Contrast test_deleted_paths_are_still_selected, which deletes a
    file *inside* a project whose settings.gradle survives."""
    assert milestone_dirs(["projects/deleted-thing/src/main/java/Gone.java"], root=tmp_path) == []
