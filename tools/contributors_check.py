"""Validates contributor files (spec §9.1).

Two jobs. The first is a schema check so a student's first pull request gets
useful feedback instead of a silent merge. The second matters more: this file
set is deliberately NOT a roster. GitHub handle only -- no legal names, no
emails, no school. The validator enforces that, because the privacy promise is
only as good as the thing that checks it.

Messages are addressed to a fifteen-year-old opening their first pull request.
They say what to change, not what was done wrong.
"""

from __future__ import annotations

import re
import sys
from pathlib import Path

EMAIL = re.compile(r"[\w.+-]+@[\w-]+\.[\w.]+")
GITHUB_LINE = re.compile(r"^\*\*GitHub:\*\*\s*@([A-Za-z0-9](?:[A-Za-z0-9-]{0,37}[A-Za-z0-9])?)\s*$", re.M)
PLACEHOLDERS = ("your-github-handle", "<your-handle>", "your-handle", "TODO", "FIXME")


def validate_file(path: Path) -> list[str]:
    """Return human-readable problems with one contributor file. Empty when valid."""
    problems: list[str] = []
    text = path.read_text(encoding="utf-8")
    stem = path.stem

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

    if EMAIL.search(text):
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
        print("contributor files OK")
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
