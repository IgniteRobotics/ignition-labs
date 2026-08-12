"""The M6 regression gate: a planted break must be caught by a real test (spec §9.7).

M6 inverts every earlier milestone. The student writes tests, not code, so the clean-clone
invariant's red does not apply -- M6 is green on a clean clone by design. What replaces it
is this: apply a one-line regression to given, working code and require the suite to go
red. A student can satisfy a test-count check with three assertions on `true`. They cannot
satisfy this one.

The reason this is a tool and not three lines of shell is that a non-zero exit status is
not evidence of a caught regression. `git apply` exits non-zero when a patch does not
apply. `./gradlew test` exits non-zero on a link error, on a compile error, and on a build
that never ran a test. Reading exit statuses would report every one of those as success.
This reads the JUnit reports and confirms a named test went from passing to failing on an
assertion.
"""

from __future__ import annotations

import argparse
import shutil
import subprocess
import sys
from dataclasses import dataclass, field
from pathlib import Path
from xml.etree import ElementTree

from tools.given_tests_red import TOOLCHAIN_FAILURES


@dataclass(frozen=True)
class RunOutcome:
    """One `./gradlew test` run: its exit status and what its reports recorded."""

    status: int
    results: dict[str, list[str]] = field(default_factory=dict)
    reports_found: bool = True

    @property
    def failed_tests(self) -> dict[str, list[str]]:
        return {name: types for name, types in self.results.items() if types}


def parse_results(results_dir: Path) -> dict[str, list[str]]:
    """Map `classname#testname` to the failure/error types recorded for it.

    An empty list means the test passed. Raises ElementTree.ParseError if a report is
    unreadable -- callers must treat that as a failure to verify, never as an absence of
    problems.

    JUnit Jupiter's XML writer records a no-arg test method's `name` as `method()`, not
    `method` -- that is the "legacy reporting name" convention, and it is what a real
    Gradle run produces (verified against this project's own suite). The trailing `()`
    is stripped so a real run's keys match the bare `CLASS#METHOD` form the CLI contract
    and the synthetic fixtures both use. Only a literal, argument-free `()` is stripped;
    a parameterized or repeated test's `method(int)` is left alone; a `caught:` target
    naming one of those would need the full signature, matching what the report records.
    """
    results: dict[str, list[str]] = {}
    for report in sorted(results_dir.glob("*.xml")):
        root = ElementTree.parse(report).getroot()
        for case in root.iter("testcase"):
            case_name = case.get("name", "(unknown)")
            if case_name.endswith("()"):
                case_name = case_name[: -len("()")]
            name = f"{case.get('classname', '(unknown)')}#{case_name}"
            types = [
                child.get("type", "(untyped)")
                for child in case
                if child.tag in {"failure", "error"}
            ]
            results[name] = types
    return results


def read_outcome(results_dir: Path, status: int) -> RunOutcome:
    if not results_dir.is_dir():
        return RunOutcome(status=status, results={}, reports_found=False)
    return RunOutcome(status=status, results=parse_results(results_dir))


def judge(before: RunOutcome, after: RunOutcome, expect: str) -> tuple[bool, str]:
    """Decide whether a regression was caught the way `expect` requires.

    `expect` is one of `caught:CLASS#METHOD`, `any-failure`, or `no-failure`. Every path
    that cannot positively confirm the expectation returns False: this fails loudly rather
    than passing by default, because passing by default is the exact failure it prevents.
    """
    if not before.reports_found or not before.results:
        return False, (
            "The baseline run produced no test results at all, so nothing ran. That is a "
            "compile error or a broken toolchain, not a working suite -- and a regression "
            "gate on top of a suite that never ran proves nothing."
        )

    if before.status != 0 or before.failed_tests:
        broken = sorted(before.failed_tests)
        return False, (
            "The suite is not green BEFORE the patch is applied "
            f"(exit {before.status}; failing: {', '.join(broken) or 'none recorded'}).\n"
            "The gate can only tell you a patch broke something if the tree was working "
            "first. Fix the baseline, then re-run."
        )

    if not after.reports_found or not after.results:
        return False, (
            "The post-patch run produced no test results at all. The patch broke the "
            "BUILD, not a test -- most likely it no longer applies cleanly to this file. "
            "A build failure is not a caught regression, and reporting it as one would "
            "let this gate pass forever without a single real test behind it."
        )

    toolchain = sorted(
        {t for types in after.failed_tests.values() for t in types if t in TOOLCHAIN_FAILURES}
    )
    if toolchain:
        return False, (
            "The post-patch run failed for the WRONG reason: "
            + ", ".join(toolchain)
            + ".\nThat is the toolchain failing, not a test catching a regression. On this "
            "project it usually means the desktop natives are not reaching the test JVM."
        )

    if expect == "no-failure":
        if after.failed_tests:
            return False, (
                "A test that was NOT supposed to catch this patch caught it: "
                + ", ".join(sorted(after.failed_tests))
                + ".\nThis patch belongs to the student tier, and the given worked example "
                "must not catch it -- otherwise a student passes the gate in their own "
                "fork having written no tests at all. Narrow what the worked example "
                "asserts on, or repoint the patch at a method it does not touch."
            )
        return True, "OK: nothing given caught this patch, so it is the student's to catch."

    if not after.failed_tests:
        return False, (
            f"The patch was applied and all {len(after.results)} test(s) still PASS.\n"
            "Nothing here tests the thing that broke. If this is your pull request: that "
            "is the finding, and it is the exercise working. Write a test that fails when "
            "this patch is applied, and it will fail for the right reason."
        )

    if expect == "any-failure":
        caught = ", ".join(sorted(after.failed_tests))
        return True, f"OK: the patch was caught by {caught}."

    if not expect.startswith("caught:"):
        return False, f"Unknown expectation {expect!r}."

    wanted = expect[len("caught:") :]
    if wanted not in after.failed_tests:
        return False, (
            f"The patch was caught, but not by {wanted}.\n"
            "Failing tests were: " + ", ".join(sorted(after.failed_tests)) + ".\n"
            "This tier exists to prove the mechanism through one specific given test. If "
            "that test no longer covers this patch, the two-tier split has drifted and the "
            "student tier may have drifted with it."
        )
    return True, f"OK: {wanted} caught the patch."


def _run(command: list[str], cwd: Path) -> int:
    print(f"$ {' '.join(command)}", flush=True)
    return subprocess.run(command, cwd=cwd).returncode


def main(argv: list[str]) -> int:
    parser = argparse.ArgumentParser(prog="regression_gate.py")
    parser.add_argument("project_dir", type=Path)
    parser.add_argument("patch", type=Path)
    parser.add_argument("--expect", required=True)
    parser.add_argument("--gradlew", default="./gradlew")
    args = parser.parse_args(argv[1:])

    project: Path = args.project_dir.resolve()
    patch: Path = args.patch.resolve()
    repo_root = Path.cwd()
    results_dir = project / "build" / "test-results" / "test"

    baseline_status = _run([args.gradlew, "test", "--console=plain"], cwd=project)
    try:
        before = read_outcome(results_dir, baseline_status)
    except ElementTree.ParseError as e:
        print(f"::error::Could not read the baseline JUnit reports ({e}).")
        return 1

    if _run(["git", "apply", str(patch)], cwd=repo_root) != 0:
        print(
            f"::error::{patch.name} does not apply to this tree. That is not a caught "
            "regression -- it means the patch and the file it targets have drifted apart. "
            "Regenerate the patch with `git diff` rather than editing it by hand."
        )
        return 1

    try:
        after_status = _run(
            [args.gradlew, "test", "--console=plain", "--rerun-tasks"], cwd=project
        )
        try:
            after = read_outcome(results_dir, after_status)
        except ElementTree.ParseError as e:
            print(f"::error::Could not read the post-patch JUnit reports ({e}).")
            return 1
        ok, message = judge(before, after, args.expect)
    finally:
        # Always revert, even when judging raised. A gate that leaves a patch applied
        # poisons every later step in the job, and the failure surfaces somewhere with no
        # visible connection to this one.
        _run(["git", "apply", "-R", str(patch)], cwd=repo_root)

    dirty = subprocess.run(
        ["git", "status", "--porcelain", str(project)],
        cwd=repo_root,
        capture_output=True,
        text=True,
    ).stdout.strip()
    if dirty:
        print(
            "::error::The tree is not clean after reverting the patch:\n"
            + dirty
            + "\nRefusing to report a result from a tree this gate has modified."
        )
        return 1

    if not ok:
        print(f"::error::{patch.name}: {message}")
        return 1
    print(f"{patch.name}: {message}")
    return 0


if __name__ == "__main__":  # pragma: no cover
    if shutil.which("git") is None:
        print("git is not on PATH", file=sys.stderr)
        raise SystemExit(2)
    raise SystemExit(main(sys.argv))
