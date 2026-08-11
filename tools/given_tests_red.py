"""The inverted check: given tests must fail, and fail for the *right reason* (spec §12.3).

A skeleton whose tests pass unmodified has a stub that implements itself. But a skeleton
whose tests fail because the toolchain is broken is just as useless, and looks identical
to CI if the only thing being checked is a non-zero exit status -- which is how this gate
was written until M3 arrived. M0-M2 were plain Java and could not link-fail; from M3 the
tests need desktop HAL natives, so "the tests failed" stopped being sufficient evidence.

What this enforces is the clean-clone invariant's actual wording: the red must never come
from a compile error, because a Track A student cannot tell a compile error from a broken
toolchain and it costs them a session that was never their problem.

It deliberately does NOT require failures to be assertions. Across this curriculum most
given tests go red by calling a stub that throws: M1 produces 43
UnsupportedOperationExceptions, T2 produces IllegalArgumentException, and T1's
ArrayIndexOutOfBoundsException *is the bug the student is being asked to find*. Those are
all legitimate red. Only failures a student cannot act on are rejected.
"""

from __future__ import annotations

import sys
from pathlib import Path
from xml.etree import ElementTree

# Failures that mean the environment is broken rather than the code being unwritten.
# A student shown one of these has been handed someone else's problem.
TOOLCHAIN_FAILURES = frozenset(
    {
        "java.lang.UnsatisfiedLinkError",
        "java.lang.ExceptionInInitializerError",
        "java.lang.NoClassDefFoundError",
        "java.lang.NoSuchMethodError",
        "java.lang.NoSuchFieldError",
        "java.lang.UnsupportedClassVersionError",
        "java.lang.LinkageError",
        "java.lang.ClassNotFoundException",
    }
)


def collect_failures(results_dir: Path) -> list[str]:
    """Every failure/error `type` recorded in a directory of JUnit XML reports.

    Raises ElementTree.ParseError if a report is unreadable -- callers must treat that as
    a failure to verify rather than as an absence of problems.
    """
    types: list[str] = []
    for report in sorted(results_dir.glob("*.xml")):
        root = ElementTree.parse(report).getroot()
        for element in root.iter():
            if element.tag in {"failure", "error"}:
                types.append(element.get("type", "(untyped)"))
    return types


def count_tests(results_dir: Path) -> int:
    """Total tests recorded across a directory of JUnit XML reports."""
    total = 0
    for report in sorted(results_dir.glob("*.xml")):
        root = ElementTree.parse(report).getroot()
        total += int(root.get("tests", "0"))
    return total


def verdict(gradle_status: int, results_dir: Path) -> tuple[bool, str]:
    """Decide whether a skeleton is red for the right reason.

    Returns (ok, message). Every path that cannot positively confirm a legitimate red
    returns False -- this check fails loudly rather than passing by default, because
    passing by default is the exact failure it exists to prevent.
    """
    if gradle_status == 0:
        return False, (
            "The given tests PASS on the unmodified skeleton. A stub is accidentally "
            "implementing itself, so the milestone ships already solved. See spec §7.2."
        )

    if not results_dir.is_dir():
        return False, (
            f"The tests failed but wrote no results at all ({results_dir} does not "
            "exist), which means they never ran. That is a compile error or a broken "
            "toolchain, not a skeleton waiting to be filled in -- and spec §12.3 "
            "requires the red to come from the tests themselves."
        )

    try:
        failures = collect_failures(results_dir)
        tests = count_tests(results_dir)
    except ElementTree.ParseError as e:
        return False, (
            f"Could not read the JUnit reports in {results_dir} ({e}). Treating an "
            "unreadable report as a real failure rather than swallowing it."
        )

    if tests == 0:
        return False, (
            f"The tests failed but {results_dir} records zero tests, so nothing ran. "
            "That is a compile error or a broken toolchain, not a red skeleton."
        )

    if not failures:
        return False, (
            f"The build failed but none of the {tests} test(s) in {results_dir} "
            "recorded a failure. Something other than a test went wrong; refusing to "
            "report that as 'red as intended'."
        )

    broken = sorted({f for f in failures if f in TOOLCHAIN_FAILURES})
    if broken:
        return False, (
            "The given tests are red for the WRONG reason: "
            + ", ".join(broken)
            + ".\nThat is the toolchain failing, not a stub waiting to be implemented. "
            "A student cannot tell this apart from a broken environment, and spec §12.3 "
            "requires the red to come from the tests. On M3 and later this usually means "
            "the desktop HAL natives are not reaching the test JVM."
        )

    return True, f"OK: {len(failures)} failure(s) across {tests} test(s), none toolchain-shaped."


def main(argv: list[str]) -> int:
    if len(argv) != 3:
        print("usage: given_tests_red.py PROJECT_DIR GRADLE_TEST_STATUS", file=sys.stderr)
        return 2
    project = Path(argv[1])
    try:
        status = int(argv[2])
    except ValueError:
        print(f"GRADLE_TEST_STATUS must be an integer, got {argv[2]!r}", file=sys.stderr)
        return 2

    ok, message = verdict(status, project / "build" / "test-results" / "test")
    if not ok:
        print(f"::error::{project}: {message}")
        return 1
    print(f"{project}: {message}")
    return 0


if __name__ == "__main__":  # pragma: no cover
    raise SystemExit(main(sys.argv))
