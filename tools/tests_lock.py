"""SHA-256 manifest of given test files (spec §12.2).

The check exists to make tampering *visible*, not to be uncircumventable.
Anyone determined can rewrite history. Visible is enough — which is why the
failure message invites disagreement rather than making an accusation.
"""

from __future__ import annotations

import hashlib
import sys
from pathlib import Path

TAMPER_MESSAGE = (
    "This test is part of the milestone. If you think it is wrong, say so in "
    "the pull request and a mentor will look."
)


def _sha256(path: Path) -> str:
    digest = hashlib.sha256()
    digest.update(path.read_bytes())
    return digest.hexdigest()


def compute_manifest(root: Path, patterns: list[str]) -> dict[str, str]:
    """Map each matched file's root-relative POSIX path to its SHA-256 hex digest."""
    manifest: dict[str, str] = {}
    for pattern in patterns:
        for path in sorted(root.glob(pattern)):
            if path.is_file():
                manifest[path.relative_to(root).as_posix()] = _sha256(path)
    return manifest


def write_manifest(root: Path, patterns: list[str], out: Path) -> None:
    """Write a `sha256  path` manifest, sorted by path for stable diffs."""
    manifest = compute_manifest(root, patterns)
    lines = [f"{digest}  {rel}" for rel, digest in sorted(manifest.items())]
    out.write_text("\n".join(lines) + "\n", encoding="utf-8")


def verify_manifest(root: Path, manifest: Path) -> list[str]:
    """Return root-relative paths that were modified or deleted, sorted."""
    changed: list[str] = []
    for line in manifest.read_text(encoding="utf-8").splitlines():
        if not line.strip():
            continue
        expected, rel = line.split("  ", 1)
        target = root / rel
        if not target.is_file() or _sha256(target) != expected:
            changed.append(rel)
    return sorted(changed)


PATTERNS = [
    "projects/*/src/test/**/*.java",
    "projects/*/src/main/java/**/plant/*.java",
    "projects/*/src/main/java/**/harness/*.java",
    "projects/*/regressions/*.patch",
    "projects/testouts/*/src/test/**/*.java",
    "projects/testouts/*/src/main/java/**/harness/*.java",
    # Test-out fixtures nest one directory deeper than milestones, so the
    # `projects/*/...plant/...` pattern above does not reach them. Without this line
    # `t4-ringing-flywheel`'s T4FlywheelPlant is unlocked, and a student can make the fixture
    # pass by editing the plant instead of fixing the gains -- which is the whole
    # exercise. Its README tells them the plant is locked; this is what makes that true.
    "projects/testouts/*/src/main/java/**/plant/*.java",
]


def main(argv: list[str]) -> int:
    if len(argv) != 2 or argv[1] not in {"write", "verify"}:
        print("usage: tests_lock.py [write|verify]", file=sys.stderr)
        return 2
    root = Path.cwd()
    lock = root / "tests.lock"
    if argv[1] == "write":
        write_manifest(root, PATTERNS, lock)
        print(f"wrote {lock}")
        return 0
    changed = verify_manifest(root, lock)
    if changed:
        for rel in changed:
            print(f"MODIFIED GIVEN TEST: {rel}", file=sys.stderr)
        print(f"\n{TAMPER_MESSAGE}", file=sys.stderr)
        return 1
    print("tests.lock OK")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
