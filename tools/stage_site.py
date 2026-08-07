"""Copy only the prose linked from 00-index.md into a staging directory.

Fallback path for spec D7: used when doc-builder's index-driven navigation
does not by itself keep projects/ out of the published site.
"""

from __future__ import annotations

import re
import shutil
import sys
from pathlib import Path

LINK = re.compile(r"\]\(([^)#]+\.md)(?:#[^)]*)?\)")


def staged_files(index: Path) -> list[str]:
    """Return the index itself plus every relative .md path it links, deduplicated."""
    seen = {"00-index.md"}
    for match in LINK.finditer(index.read_text(encoding="utf-8")):
        target = match.group(1)
        if not target.startswith(("http://", "https://")):
            seen.add(target)
    return sorted(seen)


def stage(root: Path, staging: Path) -> list[str]:
    if staging.exists():
        shutil.rmtree(staging)
    copied: list[str] = []
    for rel in staged_files(root / "00-index.md"):
        source = root / rel
        if not source.is_file():
            print(f"WARNING: 00-index.md links a missing file: {rel}", file=sys.stderr)
            continue
        dest = staging / rel
        dest.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(source, dest)
        copied.append(rel)
    (staging / "index.md").write_text(
        (root / "00-index.md").read_text(encoding="utf-8"), encoding="utf-8"
    )
    return copied


if __name__ == "__main__":
    files = stage(Path.cwd(), Path.cwd() / "_staging")
    print(f"staged {len(files)} files")
