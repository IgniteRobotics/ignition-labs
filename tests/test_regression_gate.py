"""Coverage for the M6 regression gate."""

from __future__ import annotations

from pathlib import Path

import pytest

from tools.regression_gate import RunOutcome, judge, parse_results

SUITE = """<?xml version="1.0" encoding="UTF-8"?>
<testsuite name="frc.robot.WorkedExampleTest" tests="2" failures="{failures}">
  <testcase name="ticksToMechanismRotations_appliesBothTicksPerRevolutionAndGearRatio"
            classname="frc.robot.WorkedExampleTest">{first}</testcase>
  <testcase name="shooterFlywheel_acceptsAControlRequest_andClosesCleanly"
            classname="frc.robot.WorkedExampleTest"/>
</testsuite>
"""

FAILURE = '<failure type="org.opentest4j.AssertionFailedError">expected 0.5</failure>'


def write_suite(directory: Path, *, failing: bool) -> Path:
    directory.mkdir(parents=True, exist_ok=True)
    (directory / "TEST-frc.robot.WorkedExampleTest.xml").write_text(
        SUITE.format(failures=1 if failing else 0, first=FAILURE if failing else ""),
        encoding="utf-8",
    )
    return directory


def test_parse_results_reports_pass_and_fail_per_test(tmp_path):
    results = parse_results(write_suite(tmp_path / "green", failing=False))
    assert results == {
        "frc.robot.WorkedExampleTest#ticksToMechanismRotations_appliesBothTicksPerRevolutionAndGearRatio": [],
        "frc.robot.WorkedExampleTest#shooterFlywheel_acceptsAControlRequest_andClosesCleanly": [],
    }

    failed = parse_results(write_suite(tmp_path / "red", failing=True))
    assert failed[
        "frc.robot.WorkedExampleTest#ticksToMechanismRotations_appliesBothTicksPerRevolutionAndGearRatio"
    ] == ["org.opentest4j.AssertionFailedError"]


REAL_GRADLE_SUITE = """<?xml version="1.0" encoding="UTF-8"?>
<testsuite name="frc.robot.WorkedExampleTest" tests="1" failures="1">
  <testcase name="ticksToMechanismRotations_appliesBothTicksPerRevolutionAndGearRatio()"
            classname="frc.robot.WorkedExampleTest">
    <failure message="expected: &lt;0.5&gt; but was: &lt;2.0&gt;"
             type="org.opentest4j.AssertionFailedError">boom</failure>
  </testcase>
</testsuite>
"""


def test_parse_results_strips_the_junit_no_arg_legacy_reporting_parens(tmp_path):
    """A real Gradle/JUnit-Jupiter run writes no-arg method names as `method()`, not
    `method` -- confirmed against this project's own suite. `caught:CLASS#METHOD` must
    still match, or every real run of the `caught:` mode fails even when the right test
    caught the regression.
    """
    directory = tmp_path / "real"
    directory.mkdir()
    (directory / "TEST-frc.robot.WorkedExampleTest.xml").write_text(
        REAL_GRADLE_SUITE, encoding="utf-8"
    )
    results = parse_results(directory)
    assert results == {
        "frc.robot.WorkedExampleTest#ticksToMechanismRotations_appliesBothTicksPerRevolutionAndGearRatio": [
            "org.opentest4j.AssertionFailedError"
        ],
    }


GREEN = RunOutcome(status=0, results={"A#one": [], "A#two": []})


def outcome(**failures: list[str]) -> RunOutcome:
    results = {"A#one": [], "A#two": []}
    results.update({name.replace("__", "#"): types for name, types in failures.items()})
    return RunOutcome(status=1, results=results)


def test_baseline_that_never_ran_is_rejected():
    ok, message = judge(RunOutcome(status=1, results={}, reports_found=False), GREEN, "any-failure")
    assert not ok
    assert "nothing ran" in message


def test_baseline_that_is_already_red_is_rejected():
    ok, message = judge(outcome(A__one=["AssertionFailedError"]), GREEN, "any-failure")
    assert not ok
    assert "not green BEFORE" in message


def test_patch_that_breaks_the_build_is_not_a_caught_regression():
    after = RunOutcome(status=1, results={}, reports_found=False)
    ok, message = judge(GREEN, after, "any-failure")
    assert not ok
    assert "broke the BUILD" in message


def test_toolchain_failure_is_not_a_caught_regression():
    ok, message = judge(GREEN, outcome(A__one=["java.lang.UnsatisfiedLinkError"]), "any-failure")
    assert not ok
    assert "WRONG reason" in message


def test_all_still_passing_is_rejected():
    ok, message = judge(GREEN, RunOutcome(status=0, results={"A#one": [], "A#two": []}), "any-failure")
    assert not ok
    assert "still PASS" in message


def test_any_failure_accepts_a_real_assertion_failure():
    ok, message = judge(GREEN, outcome(A__two=["org.opentest4j.AssertionFailedError"]), "any-failure")
    assert ok
    assert "A#two" in message


def test_caught_by_requires_the_named_test():
    ok, message = judge(
        GREEN, outcome(A__two=["org.opentest4j.AssertionFailedError"]), "caught:A#one"
    )
    assert not ok
    assert "not by A#one" in message

    ok, _ = judge(GREEN, outcome(A__one=["org.opentest4j.AssertionFailedError"]), "caught:A#one")
    assert ok


def test_no_failure_rejects_a_student_tier_patch_the_given_test_caught():
    ok, message = judge(
        GREEN, outcome(A__one=["org.opentest4j.AssertionFailedError"]), "no-failure"
    )
    assert not ok
    assert "student tier" in message

    ok, _ = judge(GREEN, RunOutcome(status=0, results={"A#one": [], "A#two": []}), "no-failure")
    assert ok
