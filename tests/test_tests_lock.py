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
