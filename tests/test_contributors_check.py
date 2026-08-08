"""Contributor files carry a GitHub handle and nothing identifying (spec §9.1).

The privacy rule is the reason this validator exists: no legal names, no emails,
no school. It is deliberately not a roster.
"""

from pathlib import Path

import pytest

from tools.contributors_check import validate_all, validate_file

VALID = """\
# jsmith

**GitHub:** @jsmith

**What I want to work on:** autonomous, and eventually vision.

**Joined:** 2026-09
"""


def _write(tmp_path: Path, name: str, body: str) -> Path:
    target = tmp_path / "contributors" / name
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(body, encoding="utf-8")
    return target


def test_valid_file_has_no_problems(tmp_path):
    assert validate_file(_write(tmp_path, "jsmith.md", VALID)) == []


def test_filename_must_match_the_handle_inside(tmp_path):
    problems = validate_file(_write(tmp_path, "someone-else.md", VALID))
    assert any("filename" in p for p in problems)


def test_missing_github_line_is_reported(tmp_path):
    body = VALID.replace("**GitHub:** @jsmith\n", "")
    problems = validate_file(_write(tmp_path, "jsmith.md", body))
    assert any("GitHub" in p for p in problems)


def test_email_address_is_rejected(tmp_path):
    body = VALID + "\nReach me at jsmith@example.com\n"
    problems = validate_file(_write(tmp_path, "jsmith.md", body))
    assert any("email" in p.lower() for p in problems)


def test_placeholder_handle_is_rejected(tmp_path):
    body = VALID.replace("jsmith", "your-github-handle")
    problems = validate_file(_write(tmp_path, "your-github-handle.md", body))
    assert any("template" in p.lower() for p in problems)


def test_readme_in_contributors_is_skipped(tmp_path):
    _write(tmp_path, "README.md", "# Contributors\n\nExplanatory text.\n")
    assert validate_all(tmp_path) == {}


def test_validate_all_reports_only_bad_files(tmp_path):
    _write(tmp_path, "jsmith.md", VALID)
    _write(tmp_path, "bad.md", "# bad\n")
    result = validate_all(tmp_path)
    assert list(result) == ["contributors/bad.md"]


def test_missing_contributors_directory_is_not_an_error(tmp_path):
    assert validate_all(tmp_path) == {}
