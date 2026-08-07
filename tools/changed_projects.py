"""Derive the CI build matrix from a list of changed paths (spec §7.3 mechanic 2).

Emits a JSON array for GitHub Actions `fromJSON()`. An empty array means no
Gradle project needs building, and the calling job should skip entirely.
"""

from __future__ import annotations

import argparse
import json
import sys
from collections.abc import Iterable
from pathlib import PurePosixPath

PROJECTS_ROOT = "projects"
TESTOUTS = "testouts"


def milestone_dirs(changed_paths: Iterable[str]) -> list[str]:
    """Return the sorted, deduplicated set of standalone Gradle projects to build."""
    selected: set[str] = set()
    for raw in changed_paths:
        rel = raw.strip()
        if not rel:
            continue
        parts = PurePosixPath(rel).parts
        if len(parts) < 2 or parts[0] != PROJECTS_ROOT:
            continue
        if parts[1] == TESTOUTS:
            # Fixtures nest one level deeper: projects/testouts/<fixture>/...
            if len(parts) >= 4:
                selected.add(f"{PROJECTS_ROOT}/{TESTOUTS}/{parts[2]}")
            continue
        if len(parts) >= 3:
            selected.add(f"{PROJECTS_ROOT}/{parts[1]}")
    return sorted(selected)


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    source = parser.add_mutually_exclusive_group(required=True)
    source.add_argument("--from-file", help="File containing one changed path per line")
    source.add_argument("--from-stdin", action="store_true", help="Read paths from stdin")
    args = parser.parse_args()

    if args.from_stdin:
        lines = sys.stdin.read().splitlines()
    else:
        with open(args.from_file, encoding="utf-8") as handle:
            lines = handle.read().splitlines()

    print(json.dumps(milestone_dirs(lines)))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
