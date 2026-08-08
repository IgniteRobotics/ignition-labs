"""Validates contributor files (spec §9.1).

PURPOSE: Catch accidental email disclosure. A student who pastes an email
without thinking about where the file goes. NOT: stop deliberate obfuscation.
A determined student will succeed; a mentor must still read the file.

Two jobs. The first is a schema check so a student's first pull request gets
useful feedback instead of a silent merge. The second is privacy: GitHub handle
only -- no legal names, no emails, no school. The validator catches common
accidental disclosures (standard emails, spaced variants, bracketed/parenthesized
replacements, fullwidth chars, spelled-out forms). It does not attempt to
catch determined obfuscation or detect names/schools in free text -- those
require human review.

Messages are addressed to a fifteen-year-old opening their first pull request.
They say what to change, not what was done wrong.
"""

from __future__ import annotations

import re
import sys
from pathlib import Path

EMAIL = re.compile(r"[\w.+-]+@[\w-]+\.[\w.]+")
# Spaced around @ and/or dots (e.g. "jsmith @ example . com")
EMAIL_SPACED = re.compile(r"\w+\s*@\s*\w+\s*\.\s*\w+")
# Bracketed [at] marker (e.g. "jsmith[at]example.com")
EMAIL_AT_BRACKETS = re.compile(r"\w+\s*\[\s*at\s*\]\s*\w+", re.I)
# Bracketed [dot] marker (e.g. "jsmith@example[dot]com")
EMAIL_DOT_BRACKETS = re.compile(r"\w+\s*\[\s*dot\s*\]\s*\w", re.I)
# Parenthesized (at) marker (e.g. "jsmith(at)example.com")
EMAIL_AT_PARENS = re.compile(r"\w+\s*\(\s*at\s*\)\s*\w+", re.I)
# Parenthesized (dot) marker (e.g. "jsmith@example(dot)com")
EMAIL_DOT_PARENS = re.compile(r"\w+\s*\(\s*dot\s*\)\s*\w", re.I)
# Spelled-out forms: AT SIGN (e.g. "jsmith AT SIGN example dot com")
EMAIL_AT_SIGN = re.compile(r"\b\w+\s+(?:at\s+sign|at-sign)\s+\w+", re.I)
# Bare token form: word at word dot word (e.g. "jsmith at example dot com")
EMAIL_BARE_TOKENS = re.compile(r"\b\w+\s+at\s+\w+\s+dot\s+\w+\b", re.I)
# GitHub handle: no consecutive hyphens, max 39 characters
GITHUB_LINE = re.compile(
    r"^\*\*GitHub:\*\*\s*@([A-Za-z0-9](?:(?:[A-Za-z0-9]|-(?=[A-Za-z0-9])){0,37}[A-Za-z0-9])?)\s*$",
    re.M,
)
PLACEHOLDERS = ("your-github-handle", "<your-handle>", "your-handle", "TODO", "FIXME")


def validate_file(path: Path) -> list[str]:
    """Return human-readable problems with one contributor file. Empty when valid."""
    problems: list[str] = []
    text = path.read_text(encoding="utf-8")
    stem = path.stem

    # Normalize fullwidth Unicode characters to ASCII equivalents
    # Fullwidth @ (U+FF20) and fullwidth . (U+FF0E) are common obfuscations
    text_normalized = text.translate(str.maketrans({
        '＠': '@',  # Fullwidth @
        '．': '.',  # Fullwidth .
    }))

    match = GITHUB_LINE.search(text)
    if match is None:
        problems.append(
            "Add a line reading exactly '**GitHub:** @your-handle' with your real handle."
        )
    elif match.group(1) != stem:
        problems.append(
            f"The filename says '{stem}' but the GitHub line says '@{match.group(1)}'. "
            "Rename the file to match your handle."
        )

    if (
        EMAIL.search(text_normalized)
        or EMAIL_SPACED.search(text_normalized)
        or EMAIL_AT_BRACKETS.search(text_normalized)
        or EMAIL_DOT_BRACKETS.search(text_normalized)
        or EMAIL_AT_PARENS.search(text_normalized)
        or EMAIL_DOT_PARENS.search(text_normalized)
        or EMAIL_AT_SIGN.search(text_normalized)
        or EMAIL_BARE_TOKENS.search(text_normalized)
    ):
        problems.append(
            "Remove the email address. This file is public and carries your GitHub "
            "handle only -- no emails, no legal names, no school."
        )

    for placeholder in PLACEHOLDERS:
        if placeholder in text:
            problems.append(
                f"'{placeholder}' is still in the file -- replace the template text with yours."
            )
            break

    return problems


def validate_all(root: Path) -> dict[str, list[str]]:
    """Map each problematic contributor file's relative path to its problems."""
    directory = root / "contributors"
    if not directory.is_dir():
        return {}
    results: dict[str, list[str]] = {}
    for path in sorted(directory.glob("*.md")):
        if path.name == "README.md":
            continue
        problems = validate_file(path)
        if problems:
            results[path.relative_to(root).as_posix()] = problems
    return results


def main() -> int:
    results = validate_all(Path.cwd())
    if not results:
        print(
            "contributor files OK: schema valid, common accidental email disclosures caught. "
            "Mentor: please review for deliberate obfuscation and any legal names or school references."
        )
        return 0
    for rel, problems in results.items():
        print(f"\n{rel}", file=sys.stderr)
        for problem in problems:
            print(f"  - {problem}", file=sys.stderr)
    print(
        "\nThis is a checklist, not a telling-off. Fix these and push again.",
        file=sys.stderr,
    )
    return 1


if __name__ == "__main__":
    raise SystemExit(main())
