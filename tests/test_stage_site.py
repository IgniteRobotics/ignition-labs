"""Guards the staging fallback (spec §14 / D7): only index-linked prose is staged.

doc-builder was measured (Task 3 investigation) to render every markdown file
it finds in the source tree regardless of 00-index.md linkage — the
`superpowers/` directory (agent notes/plans/specs) leaked into the built site
even though nothing in 00-index.md links to it. `tools/stage_site.py` is the
fix: it copies only the files 00-index.md actually links into `_staging/`,
which doc-builder then builds from instead of the repo root.
"""

from pathlib import Path

from tools.stage_site import staged_files

REPO = Path(__file__).resolve().parents[1]
INDEX = REPO / "00-index.md"


def test_staged_files_excludes_unlinked_superpowers_docs():
    files = staged_files(INDEX)
    assert not any(f.startswith("superpowers/") for f in files)


def test_staged_files_includes_the_index_itself():
    files = staged_files(INDEX)
    assert "00-index.md" in files


def test_staged_files_includes_linked_programming_prose():
    files = staged_files(INDEX)
    assert "programming/git-workflow.md" in files
    assert "programming/robot-repo-ladder.md" in files
    assert "programming/teaching-repo-spec.md" in files


def test_staged_files_excludes_unlinked_http_targets(tmp_path):
    index = tmp_path / "00-index.md"
    index.write_text(
        "[External](https://example.com/page.md)\n[Local](programming/README.md)\n",
        encoding="utf-8",
    )
    files = staged_files(index)
    assert "programming/README.md" in files
    assert not any(f.startswith("http") for f in files)
