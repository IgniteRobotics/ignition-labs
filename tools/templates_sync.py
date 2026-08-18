"""Templates in templates/ must match their source region in student-deliverables.md.

Two copies of every template exist on purpose (see the M7/M8 design record §2.2):
templates/ holds the real files a student copies, and student-deliverables.md holds
the same content inline so the published page can show what it is explaining --
templates/ is outside docs_dir and MkDocs cannot reach it.

Two copies drift. This makes that visible on the next CI run rather than on the day a
student notices the template they were handed disagrees with the page describing it.

Regions are delimited by HTML comment markers rather than line numbers or fences: line
numbers break on the next edit above them, and one of the six regions is not fenced at
all (the competition-week checklist renders as real checkboxes and must stay unfenced).
"""

from __future__ import annotations

import re
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[1]
SOURCE_DOC = Path("docs") / "programming" / "student-deliverables.md"
TEMPLATES_DIR = Path("templates")

TEMPLATE_NAMES = [
    "subsystem-readme",
    "tuning-log",
    "pr-description",
    "competition-week-checklist",
    "pit-documentation",
    "post-season-retro",
]

DRIFT_MESSAGE = (
    "This template and the copy in docs/programming/student-deliverables.md have "
    "diverged. Edit the document, then re-extract -- both copies move together or "
    "students get handed a template the curriculum page contradicts."
)


def extract_regions(root: Path) -> dict[str, str]:
    """Map each marker name to its region body, with any wrapping fence removed."""
    doc = (root / SOURCE_DOC).read_text(encoding="utf-8")
    regions: dict[str, str] = {}
    for match in re.finditer(
        r"<!-- template:([a-z0-9-]+) start -->\n(.*?)\n<!-- template:\1 end -->", doc, re.S
    ):
        name, body = match.group(1), match.group(2)
        fenced = re.match(r"^```[a-z]*\n(.*)\n```$", body, re.S)
        if fenced:
            body = fenced.group(1)
        regions[name] = body.rstrip("\n")
    return regions


def verify(root: Path, names: list[str] | None = None) -> list[str]:
    """Return a problem description per template that is missing or has drifted."""
    names = TEMPLATE_NAMES if names is None else names
    regions = extract_regions(root)
    problems: list[str] = []
    for name in names:
        path = root / TEMPLATES_DIR / f"{name}.md"
        if name not in regions:
            problems.append(f"{name}: no marked region in {SOURCE_DOC}")
            continue
        if not path.is_file():
            problems.append(f"{name}: {path.relative_to(root)} does not exist")
            continue
        if path.read_text(encoding="utf-8").rstrip("\n") != regions[name]:
            problems.append(f"{name}: {path.relative_to(root)} differs from its marked region")
    return problems


def main(argv: list[str]) -> int:
    if len(argv) != 2 or argv[1] != "verify":
        print("usage: templates_sync.py verify", file=sys.stderr)
        return 2
    problems = verify(REPO_ROOT)
    if problems:
        for problem in problems:
            print(f"TEMPLATE OUT OF SYNC: {problem}")
        print()
        print(DRIFT_MESSAGE)
        return 1
    print("templates in sync")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
