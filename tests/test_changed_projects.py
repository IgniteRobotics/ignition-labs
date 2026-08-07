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
