# Task 5: `tools/regression_gate.py`

The tool both workflows call. It exists for the same reason `given_tests_red.py` does: **a non-zero exit status is not evidence.** `git apply` exits non-zero when a patch does not apply at all. `./gradlew test` exits non-zero on `UnsatisfiedLinkError`, on a compile error, and on a build that never ran a test. Every one of those reads as "the regression was caught" to a naive `if ! ./gradlew test`, and a gate that passes when the thing it protects is broken is the failure this repository keeps producing.

This lane touches nothing the milestone lane touches, so it runs concurrently with Tasks 1–4. It is written and tested against **synthetic JUnit XML** — it never needs a real Gradle build to be correct.

**Files:**
- Create: `tools/regression_gate.py`
- Create: `tests/test_regression_gate.py`

**Interfaces:**
- Consumes: `tools/given_tests_red.py`'s `TOOLCHAIN_FAILURES` frozenset. Import it; do not re-declare the list, or the two checks will drift and only one will be updated next time.
- Produces the CLI both workflows call in Task 9:

  ```
  python3 -m tools.regression_gate PROJECT_DIR PATCH \
      --expect {caught:CLASS#METHOD | any-failure | no-failure}
  ```

  Exit 0 when the expectation held, 1 when it did not, 2 on usage error.

  **It must be invoked as a module, from the repository root.** Running `python3 tools/regression_gate.py` puts `tools/` on `sys.path` rather than the repository root, so `from tools.given_tests_red import ...` fails with `ModuleNotFoundError` — and it fails *before* any check runs, which reads as a red job with no useful message. The repository root is also where the patches apply from: they carry repository-relative paths.

**Lane:** `ci-tooling`. Worktree `.worktrees/m6-tools`.

---

- [ ] **Step 1: Write the failing test for result parsing**

Create `tests/test_regression_gate.py`:

```python
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
```

- [ ] **Step 2: Run it and confirm it fails**

```bash
cd /Users/y0shi/workspace/ignite/docs
poetry run pytest tests/test_regression_gate.py -v
```

Expected: `ModuleNotFoundError: No module named 'tools.regression_gate'`.

- [ ] **Step 3: Write `tools/regression_gate.py`**

```python
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
    """
    results: dict[str, list[str]] = {}
    for report in sorted(results_dir.glob("*.xml")):
        root = ElementTree.parse(report).getroot()
        for case in root.iter("testcase"):
            name = f"{case.get('classname', '(unknown)')}#{case.get('name', '(unknown)')}"
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
```

- [ ] **Step 4: Run the parse test and confirm it passes**

```bash
poetry run pytest tests/test_regression_gate.py -v
```

Expected: the one test passes.

- [ ] **Step 5: Write the `judge` tests — one per rejection path**

Append to `tests/test_regression_gate.py`:

```python
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
```

- [ ] **Step 6: Run the full suite**

```bash
poetry run pytest tests/test_regression_gate.py -v
poetry run pytest    # the whole suite must stay green
```

Expected: all pass. Report the actual output.

- [ ] **Step 7: Confirm `TOOLCHAIN_FAILURES` is imported, and that the module invocation works**

```bash
grep -n 'TOOLCHAIN_FAILURES' tools/regression_gate.py tools/given_tests_red.py
```

Expected: `regression_gate.py` shows only the `from tools.given_tests_red import ...` line and one use. If the frozenset appears twice in the repository, the two gates will drift and only one will be updated the next time a new linkage error shows up.

Then prove both invocation forms behave as documented — this is the difference between a gate that runs and one that dies on import before checking anything:

```bash
cd /Users/y0shi/workspace/ignite/docs
python3 -m tools.regression_gate --help          # expect rc=0, usage printed
python3 tools/regression_gate.py --help; echo "rc=$?"   # expect ModuleNotFoundError
```

The second form failing is the expected result, not a defect. Task 9 wires the workflows to the first form, and this step is what proves that mattered.

- [ ] **Step 8: Read every failure message the gate can emit**

Six rejection paths, and each one will be somebody's whole afternoon. Print them and read them as a student would:

```bash
poetry run python - <<'PY'
from tools.regression_gate import RunOutcome, judge

GREEN = RunOutcome(status=0, results={"A#one": [], "A#two": []})
RED = RunOutcome(status=1, results={"A#one": ["org.opentest4j.AssertionFailedError"], "A#two": []})
LINK = RunOutcome(status=1, results={"A#one": ["java.lang.UnsatisfiedLinkError"], "A#two": []})
EMPTY = RunOutcome(status=1, results={}, reports_found=False)

for label, args in [
    ("baseline never ran", (EMPTY, GREEN, "any-failure")),
    ("baseline already red", (RED, GREEN, "any-failure")),
    ("patch broke the build", (GREEN, EMPTY, "any-failure")),
    ("toolchain failure", (GREEN, LINK, "any-failure")),
    ("nothing caught it", (GREEN, GREEN, "any-failure")),
    ("wrong test caught it", (GREEN, RED, "caught:A#two")),
    ("given test caught a student patch", (GREEN, RED, "no-failure")),
]:
    ok, message = judge(*args)
    print(f"--- {label} (ok={ok}) ---\n{message}\n")
PY
```

Paste the output into your task report. The one that matters most is **"nothing caught it"** — that is the message a student sees on their own pull request, and it must read as the exercise working rather than as an accusation.

- [ ] **Step 9: Commit**

```bash
git add tools/regression_gate.py tests/test_regression_gate.py
git commit -m "Add tools/regression_gate.py for M6's regression check

A non-zero exit status is not evidence: git apply exits non-zero on a patch
that does not apply, and gradlew test exits non-zero on a link error, a
compile error, and an empty suite. The gate reads the JUnit reports, requires
the baseline to be green, distinguishes the three ways a post-patch run can
fail without catching anything, and verifies the revert left the tree clean.

TOOLCHAIN_FAILURES is imported from given_tests_red rather than copied so the
two gates cannot drift."
```
