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


def test_email_with_spaces_around_at_and_dot_is_rejected(tmp_path):
    body = VALID + "\nReach me at jsmith @ example . com\n"
    problems = validate_file(_write(tmp_path, "jsmith.md", body))
    assert any("email" in p.lower() for p in problems)


def test_email_with_bracketed_at_is_rejected(tmp_path):
    body = VALID + "\nEmail: jsmith[at]example.com\n"
    problems = validate_file(_write(tmp_path, "jsmith.md", body))
    assert any("email" in p.lower() for p in problems)


def test_email_with_bracketed_dot_is_rejected(tmp_path):
    body = VALID + "\nEmail: jsmith@example[dot]com\n"
    problems = validate_file(_write(tmp_path, "jsmith.md", body))
    assert any("email" in p.lower() for p in problems)


def test_email_with_parenthesized_at_is_rejected(tmp_path):
    body = VALID + "\nEmail: jsmith(at)example.com\n"
    problems = validate_file(_write(tmp_path, "jsmith.md", body))
    assert any("email" in p.lower() for p in problems)


def test_email_with_parenthesized_dot_is_rejected(tmp_path):
    body = VALID + "\nEmail: jsmith@example(dot)com\n"
    problems = validate_file(_write(tmp_path, "jsmith.md", body))
    assert any("email" in p.lower() for p in problems)


def test_prose_with_at_and_shop_is_not_rejected(tmp_path):
    body = VALID + "\nI like to work at the shop in my spare time.\n"
    problems = validate_file(_write(tmp_path, "jsmith.md", body))
    # Should not have email problems
    assert not any("email" in p.lower() for p in problems)


def test_prose_with_dot_product_is_not_rejected(tmp_path):
    body = VALID + "\nI enjoy mathematics, especially the dot product.\n"
    problems = validate_file(_write(tmp_path, "jsmith.md", body))
    # Should not have email problems
    assert not any("email" in p.lower() for p in problems)


def test_github_handle_with_consecutive_hyphens_is_rejected(tmp_path):
    body = VALID.replace("@jsmith", "@j--smith")
    problems = validate_file(_write(tmp_path, "j--smith.md", body))
    assert any("filename" in p.lower() or "github" in p.lower() for p in problems)


def test_github_handle_with_39_chars_is_accepted(tmp_path):
    # GitHub allows up to 39 characters
    handle_39 = "a" * 39
    body = VALID.replace("jsmith", handle_39)
    problems = validate_file(_write(tmp_path, f"{handle_39}.md", body))
    assert not any("github" in p.lower() or "filename" in p.lower() for p in problems)


def test_github_handle_with_40_chars_is_rejected(tmp_path):
    # GitHub limits to 39 characters max
    handle_40 = "a" * 40
    body = VALID.replace("jsmith", handle_40)
    problems = validate_file(_write(tmp_path, f"{handle_40}.md", body))
    assert any("github" in p.lower() for p in problems)


def test_email_with_fullwidth_at_is_rejected(tmp_path):
    body = VALID + "\nEmail: jsmith＠example.com\n"
    problems = validate_file(_write(tmp_path, "jsmith.md", body))
    assert any("email" in p.lower() for p in problems)


def test_email_spelled_out_at_sign_is_rejected(tmp_path):
    body = VALID + "\nContact: jsmith AT SIGN example dot com\n"
    problems = validate_file(_write(tmp_path, "jsmith.md", body))
    assert any("email" in p.lower() for p in problems)




def test_email_bare_tokens_at_dot_is_rejected(tmp_path):
    body = VALID + "\nContact: jsmith at example dot com\n"
    problems = validate_file(_write(tmp_path, "jsmith.md", body))
    assert any("email" in p.lower() for p in problems)


def test_prose_with_article_before_at_is_not_rejected(tmp_path):
    body = VALID + "\nI work at the coffee shop dot net.\n"
    problems = validate_file(_write(tmp_path, "jsmith.md", body))
    # "the" in "at the" prevents matching as email pattern
    assert not any("email" in p.lower() for p in problems)


def test_prose_with_article_before_dot_is_not_rejected(tmp_path):
    body = VALID + "\nLet's meet at the place dot org tomorrow.\n"
    problems = validate_file(_write(tmp_path, "jsmith.md", body))
    # "the" in "at the" prevents matching as email pattern
    assert not any("email" in p.lower() for p in problems)
