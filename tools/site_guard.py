"""Guards the published site against leaking source files (spec §14).

A student's Java is not documentation. If any of it reaches the site, the
index-driven navigation assumption in spec D7 is wrong and the Pages build
must move to an explicit mkdocs.yml.
"""

from __future__ import annotations

import sys
from pathlib import Path

FORBIDDEN_SUFFIXES = frozenset({".java", ".gradle", ".jar", ".patch", ".wpilog"})
FORBIDDEN_NAMES = frozenset({"gradlew", "gradlew.bat", "gradle.properties", "tests.lock"})
FORBIDDEN_DIRS = frozenset({"vendordeps", "gradle", "projects"})


def forbidden_files(site_root: Path) -> list[str]:
    """Return site-relative paths that must never be published, sorted."""
    found: list[str] = []
    for path in site_root.rglob("*"):
        if not path.is_file():
            continue
        rel = path.relative_to(site_root)
        if (
            path.suffix in FORBIDDEN_SUFFIXES
            or path.name in FORBIDDEN_NAMES
            or FORBIDDEN_DIRS.intersection(rel.parts)
        ):
            found.append(rel.as_posix())
    return sorted(found)


def missing_pages(site_root: Path, required: list[str]) -> list[str]:
    """Return required page slugs that did not render, sorted.

    A slug renders as either ``<slug>/index.html`` or ``<slug>.html``.
    """
    missing = [
        slug
        for slug in required
        if not (site_root / slug / "index.html").is_file()
        and not (site_root / f"{slug}.html").is_file()
    ]
    return sorted(missing)


REQUIRED_PAGES = [
    "programming/git-workflow",
    "programming/robot-repo-ladder",
    "programming",  # renders programming/README.md -> programming/index.html
    "programming/teaching-repo-spec",
]


def main(argv: list[str]) -> int:
    if len(argv) != 2:
        print("usage: site_guard.py <site-root>", file=sys.stderr)
        return 2
    site_root = Path(argv[1])
    leaked = forbidden_files(site_root)
    absent = missing_pages(site_root, REQUIRED_PAGES)
    for path in leaked:
        print(f"PUBLISHED SOURCE FILE: {path}", file=sys.stderr)
    for slug in absent:
        print(f"MISSING PAGE: {slug}", file=sys.stderr)
    if leaked or absent:
        print(
            "\nThe published site does not match spec §14. "
            "If files under projects/ are being copied, switch the Pages build "
            "to an explicit mkdocs.yml (see plan Task 3, step 7).",
            file=sys.stderr,
        )
        return 1
    print(f"site guard OK: {site_root}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
