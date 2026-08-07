"""Guards the staging fallback (spec §14 / D7): only index-linked prose is staged.

doc-builder was measured (Task 3 investigation) to render every markdown file
it finds in the source tree regardless of 00-index.md linkage — the
`superpowers/` directory (agent notes/plans/specs) leaked into the built site
even though nothing in 00-index.md links to it. `tools/stage_site.py` is the
fix: it copies only the files 00-index.md actually links into `_staging/`,
which doc-builder then builds from instead of the repo root.

Most assertions here build a fixture `00-index.md` under `tmp_path` rather
than reading the real one, so an ordinary edit to the live index doesn't
break tests that aren't about the index's current content. Exactly one test
(the last one) touches the real `00-index.md`, and it asserts only a
structural invariant that should hold no matter what prose gets added or
removed.
"""

from pathlib import Path

from tools.stage_site import staged_files

REPO = Path(__file__).resolve().parents[1]


def _make_index(tmp_path: Path, body: str) -> Path:
    index = tmp_path / "00-index.md"
    index.write_text(body, encoding="utf-8")
    return index


def test_staged_files_excludes_unlinked_superpowers_docs(tmp_path):
    index = _make_index(
        tmp_path,
        "[Linked](programming/git-workflow.md)\n"
        "See superpowers/plans/foo.md for background (not a link).\n",
    )
    files = staged_files(index)
    assert "programming/git-workflow.md" in files
    assert not any(f.startswith("superpowers/") for f in files)


def test_staged_files_includes_the_index_itself(tmp_path):
    index = _make_index(tmp_path, "[Linked](programming/git-workflow.md)\n")
    files = staged_files(index)
    assert "00-index.md" in files


def test_staged_files_includes_every_linked_md(tmp_path):
    index = _make_index(
        tmp_path,
        "[A](programming/git-workflow.md)\n"
        "[B](programming/robot-repo-ladder.md)\n"
        "[C](programming/teaching-repo-spec.md)\n",
    )
    files = staged_files(index)
    assert "programming/git-workflow.md" in files
    assert "programming/robot-repo-ladder.md" in files
    assert "programming/teaching-repo-spec.md" in files


def test_staged_files_excludes_http_targets(tmp_path):
    index = _make_index(
        tmp_path,
        "[External](https://example.com/page.md)\n[Local](programming/README.md)\n",
    )
    files = staged_files(index)
    assert "programming/README.md" in files
    assert not any(f.startswith("http") for f in files)


def test_staged_files_includes_link_with_anchor(tmp_path):
    index = _make_index(
        tmp_path,
        "[Section](programming/git-workflow.md#branching)\n",
    )
    files = staged_files(index)
    assert "programming/git-workflow.md" in files
    assert not any("#" in f for f in files)


def test_staged_files_deduplicates_repeated_links(tmp_path):
    index = _make_index(
        tmp_path,
        "[First](programming/git-workflow.md)\n[Again](programming/git-workflow.md)\n",
    )
    files = staged_files(index)
    assert files.count("programming/git-workflow.md") == 1


def test_real_index_stages_at_least_the_index_itself():
    """The one test that touches the live 00-index.md: a structural
    invariant (non-empty, includes the index itself) that stays true
    regardless of which prose the index links on any given day."""
    files = staged_files(REPO / "00-index.md")
    assert "00-index.md" in files
    assert len(files) > 1
