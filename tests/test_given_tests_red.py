"""The inverted check fails loudly when a skeleton is red for the wrong reason (spec §12.3)."""

from pathlib import Path

import pytest

from tools.given_tests_red import TOOLCHAIN_FAILURES, verdict


def write_report(results: Path, name: str, tests: int, failures: list[str]) -> None:
    """Write a JUnit XML report shaped like the ones Gradle produces."""
    results.mkdir(parents=True, exist_ok=True)
    cases = "".join(
        f'<testcase name="case{i}" classname="{name}">'
        f'<failure message="boom" type="{failure_type}">stack</failure>'
        "</testcase>"
        for i, failure_type in enumerate(failures)
    )
    cases += "".join(
        f'<testcase name="pass{i}" classname="{name}"/>' for i in range(tests - len(failures))
    )
    (results / f"TEST-{name}.xml").write_text(
        f'<?xml version="1.0" encoding="UTF-8"?>'
        f'<testsuite name="{name}" tests="{tests}" skipped="0" '
        f'failures="{len(failures)}" errors="0">{cases}</testsuite>',
        encoding="utf-8",
    )


@pytest.fixture
def results(tmp_path: Path) -> Path:
    return tmp_path / "build" / "test-results" / "test"


def test_passing_tests_are_rejected(results):
    """A skeleton whose tests pass has a stub implementing itself."""
    write_report(results, "AnyTest", tests=3, failures=[])
    ok, message = verdict(0, results)
    assert not ok
    assert "PASS on the unmodified skeleton" in message


def test_assertion_failures_are_accepted(results):
    write_report(results, "M3Test", tests=17, failures=["org.opentest4j.AssertionFailedError"] * 17)
    ok, _ = verdict(1, results)
    assert ok


def test_stub_thrown_exceptions_are_accepted(results):
    """M1's stubs throw UnsupportedOperationException directly -- that is legitimate red."""
    write_report(
        results, "M1Test", tests=44, failures=["java.lang.UnsupportedOperationException"] * 43
    )
    ok, _ = verdict(1, results)
    assert ok


def test_the_bug_a_student_must_find_is_accepted(results):
    """T1's ArrayIndexOutOfBoundsException IS the exercise, not a broken toolchain."""
    write_report(
        results,
        "T1Test",
        tests=5,
        failures=["org.opentest4j.AssertionFailedError", "java.lang.ArrayIndexOutOfBoundsException"],
    )
    ok, _ = verdict(1, results)
    assert ok


@pytest.mark.parametrize("failure_type", sorted(TOOLCHAIN_FAILURES))
def test_toolchain_failures_are_rejected(results, failure_type):
    write_report(results, "M3Test", tests=17, failures=[failure_type])
    ok, message = verdict(1, results)
    assert not ok
    assert failure_type in message


def test_one_toolchain_failure_among_many_assertions_is_rejected(results):
    """A single link error hiding in an otherwise legitimate red must not pass."""
    write_report(
        results,
        "M3Test",
        tests=17,
        failures=["org.opentest4j.AssertionFailedError"] * 16 + ["java.lang.UnsatisfiedLinkError"],
    )
    ok, message = verdict(1, results)
    assert not ok
    assert "java.lang.UnsatisfiedLinkError" in message


def test_missing_results_directory_is_rejected(results):
    """A test compile error produces no reports at all."""
    ok, message = verdict(1, results)
    assert not ok
    assert "never ran" in message


def test_zero_tests_recorded_is_rejected(results):
    write_report(results, "Empty", tests=0, failures=[])
    ok, message = verdict(1, results)
    assert not ok
    assert "zero tests" in message


def test_failure_with_no_recorded_test_failures_is_rejected(results):
    """Gradle failed for some other reason -- refuse to call that 'red as intended'."""
    write_report(results, "AnyTest", tests=3, failures=[])
    ok, message = verdict(1, results)
    assert not ok
    assert "none of the 3 test(s)" in message


def test_unreadable_report_is_rejected(results):
    results.mkdir(parents=True)
    (results / "TEST-Broken.xml").write_text("<testsuite tests='1'>truncated", encoding="utf-8")
    ok, message = verdict(1, results)
    assert not ok
    assert "Could not read" in message
