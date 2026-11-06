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
FORBIDDEN_DIRS = frozenset(
    {"vendordeps", "gradle", "projects", "superpowers", "tools", "tests"}
)


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


def missing_assets(site_root: Path, required: list[str]) -> list[str]:
    """Return required non-page assets that did not publish, sorted.

    Images are not pages: missing_pages() looks for <slug>/index.html and can
    never observe a dropped image. An image that fails to copy renders as a
    broken-image icon on a site build that exits 0.
    """
    return sorted(rel for rel in required if not (site_root / rel).is_file())


# Deliberately hardcoded, not derived from mkdocs.yml's nav: at runtime.
#
# Derivation from nav: was tried and rejected. `mkdocs build` (even --strict)
# renders every .md file under docs_dir into its site path regardless of
# whether nav: references it -- a page removed from nav still gets an
# index.html at its usual slug, as an unlinked orphan. That means a
# nav-derived REQUIRED_PAGES shrinks in lockstep with a bad nav edit and can
# never observe the failure it exists to catch: a page silently dropped from
# nav (finding C11b). A static list stays a page short when nav does,
# independent of whatever nav currently says, which is the only way this
# check can fail on a real regression. Verified by dropping a milestone's
# nav: entry and rebuilding -- the orphaned page still renders, and only a
# hardcoded REQUIRED_PAGES catches the drop (tests/test_site_guard.py has
# the regression test).
#
# Update this list by hand when a milestone or track page is added, renamed,
# or removed on purpose -- keep it in step with mkdocs.yml's nav:.
REQUIRED_PAGES = [
    "",  # Home: index.md -> _site/index.html
    "SETUP",
    "CONTRIBUTING",
    "programming",  # renders programming/README.md -> programming/index.html
    "programming/git-workflow",
    "programming/team-gitflow",
    "programming/issues-and-projects",
    "programming/robot-repo-ladder",
    "programming/UNTESTED-TARGETS",
    "programming/ai-use-standard",
    "programming/coding-standards",
    "programming/student-deliverables",
    "programming/resources",
    "programming/ai",
    "programming/ai/setup",
    "programming/ai/the-program",
    "programming/ai/the-harness",
    "programming/ai/the-loop",
    "programming/ai/the-rules",
    "programming/ai/context-and-prompts",
    "programming/ai/research-and-memory",
    "programming/ai/session-guide",
    "programming/tracks/track-a-new-to-programming",
    "programming/tracks/track-b-non-oo-programmer",
    "programming/tracks/track-c-oo-no-frc",
    "programming/tracks/track-d-frc-veteran-ai",
    "programming/milestones/m0-tooling-and-git",
    "programming/milestones/m1-java-fundamentals",
    "programming/milestones/m2-oop-interfaces-lambdas",
    "programming/milestones/m3-wpilib-command-based",
    "programming/milestones/m4-sensors-actuators-control",
    "programming/milestones/m5-autonomous-odometry-vision",
    "programming/milestones/m6-simulation-telemetry-testing",
    "programming/milestones/m7-ai-assisted-development",
    "programming/milestones/m8-spec-anchored-development",
    "programming/milestones/m9-in-season-deliverables",
    "VISION_GUIDE",
    "vision-subsystem",
    "field-boundary-speed-limiting",
    "programming/curriculum-spec",
    "programming/teaching-repo-spec",
]

# Site-relative paths to non-page files that must publish. Hand-maintained for
# the same reason REQUIRED_PAGES is: nothing derives it, so nothing shrinks it
# to match a mistake.
REQUIRED_ASSETS = [
    "images/gitflow.png",
    "programming/ai/session-deck.html",
]


def main(argv: list[str]) -> int:
    if len(argv) != 2:
        print("usage: site_guard.py <site-root>", file=sys.stderr)
        return 2
    site_root = Path(argv[1])
    leaked = forbidden_files(site_root)
    absent = missing_pages(site_root, REQUIRED_PAGES)
    absent_assets = missing_assets(site_root, REQUIRED_ASSETS)
    for path in leaked:
        print(f"PUBLISHED SOURCE FILE: {path}", file=sys.stderr)
    for slug in absent:
        print(f"MISSING PAGE: {slug}", file=sys.stderr)
    for rel in absent_assets:
        print(f"MISSING ASSET: {rel}", file=sys.stderr)
    if leaked or absent or absent_assets:
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
