"""tests.lock makes tampering with given tests visible (spec §12.2)."""

from pathlib import Path

import pytest

from tools.tests_lock import PATTERNS, compute_manifest, verify_manifest, write_manifest


@pytest.fixture
def project(tmp_path: Path) -> Path:
    given = tmp_path / "projects" / "m1" / "src" / "test" / "java"
    given.mkdir(parents=True)
    (given / "DeadbandTest.java").write_text("class DeadbandTest {}", encoding="utf-8")
    plant = tmp_path / "projects" / "m4" / "src" / "main" / "java" / "plant"
    plant.mkdir(parents=True)
    (plant / "FlywheelPlant.java").write_text("class FlywheelPlant {}", encoding="utf-8")
    harness = tmp_path / "projects" / "m3" / "src" / "main" / "java" / "frc" / "robot" / "harness"
    harness.mkdir(parents=True)
    (harness / "M3Deliverables.java").write_text("interface M3Deliverables {}", encoding="utf-8")
    regressions = tmp_path / "projects" / "m6" / "regressions"
    regressions.mkdir(parents=True)
    (regressions / "flip-sign.patch").write_text("--- a/Foo.java\n+++ b/Foo.java\n", encoding="utf-8")
    # Test-out fixtures nest a directory deeper than milestones -- projects/testouts/<name>/
    # rather than projects/<name>/ -- so they need their own patterns. t4-bad-kv's plant is
    # the case that caught this: unlocked, a student passes the fixture by editing the plant
    # instead of fixing the gains.
    testout_plant = (
        tmp_path
        / "projects"
        / "testouts"
        / "t4-bad-kv"
        / "src"
        / "main"
        / "java"
        / "frc"
        / "testout"
        / "t4"
        / "plant"
    )
    testout_plant.mkdir(parents=True)
    (testout_plant / "T4FlywheelPlant.java").write_text(
        "class T4FlywheelPlant {}", encoding="utf-8"
    )
    return tmp_path


def test_manifest_covers_every_matched_file(project):
    manifest = compute_manifest(project, PATTERNS)
    assert set(manifest) == {
        "projects/m1/src/test/java/DeadbandTest.java",
        "projects/m4/src/main/java/plant/FlywheelPlant.java",
        "projects/m3/src/main/java/frc/robot/harness/M3Deliverables.java",
        "projects/m6/regressions/flip-sign.patch",
        "projects/testouts/t4-bad-kv/src/main/java/frc/testout/t4/plant/T4FlywheelPlant.java",
    }


def test_testout_plant_is_locked(project, tmp_path):
    """A test-out fixture's plant must be tamper-evident, not just its tests.

    Regression guard: the `projects/*/src/main/java/**/plant/*.java` pattern does not reach
    `projects/testouts/<name>/...`, which is one directory deeper. t4-bad-kv shipped with its
    plant unlocked while its README told the student it was locked.
    """
    lock = tmp_path / "tests.lock"
    write_manifest(project, PATTERNS, lock)
    target = (
        project
        / "projects/testouts/t4-bad-kv/src/main/java/frc/testout/t4/plant/T4FlywheelPlant.java"
    )
    target.write_text("class T4FlywheelPlant { /* tampered */ }", encoding="utf-8")
    assert verify_manifest(project, lock) == [
        "projects/testouts/t4-bad-kv/src/main/java/frc/testout/t4/plant/T4FlywheelPlant.java"
    ]


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


def test_modified_harness_interface_is_reported(project, tmp_path):
    lock = tmp_path / "tests.lock"
    write_manifest(project, PATTERNS, lock)
    target = project / "projects/m3/src/main/java/frc/robot/harness/M3Deliverables.java"
    target.write_text("interface M3Deliverables { /* removed a method */ }", encoding="utf-8")
    assert verify_manifest(project, lock) == [
        "projects/m3/src/main/java/frc/robot/harness/M3Deliverables.java"
    ]


def test_modified_regression_patch_is_reported(project, tmp_path):
    lock = tmp_path / "tests.lock"
    write_manifest(project, PATTERNS, lock)
    target = project / "projects/m6/regressions/flip-sign.patch"
    target.write_text("--- a/Foo.java\n+++ b/Foo.java\n@@ -1 +1 @@\n-x\n+x\n", encoding="utf-8")
    assert verify_manifest(project, lock) == ["projects/m6/regressions/flip-sign.patch"]


def test_student_own_test_file_is_not_locked(project, tmp_path):
    lock = tmp_path / "tests.lock"
    write_manifest(project, PATTERNS, lock)
    student = project / "projects" / "m2" / "src" / "test" / "java"
    student.mkdir(parents=True)
    (student / "MyOwnTest.java").write_text("class MyOwnTest {}", encoding="utf-8")
    assert verify_manifest(project, lock) == []
