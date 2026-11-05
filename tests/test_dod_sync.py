"""A milestone's Definition of done exists twice and both copies must stay identical.

The published page under docs/ and the README in projects/ carry the same checklist,
for the same reason templates_sync.py exists: projects/ is outside docs_dir, so MkDocs
cannot reach the project README and the list has to be written out again there.

Two copies drift. Until now both pairs were hand-verified per change, which works right
up until the change that forgets. This makes the drift a CI failure rather than something
a mentor discovers while marking a box that no longer exists on the page.
"""

from pathlib import Path

import pytest

REPO = Path(__file__).resolve().parents[1]

# (milestone, published page, project README, item count)
DOD_PAIRS = [
    (
        "M7",
        "docs/programming/milestones/m7-ai-assisted-development.md",
        "projects/m7-ai-assisted/README.md",
        13,
    ),
    (
        "M8",
        "docs/programming/milestones/m8-spec-anchored-development.md",
        "projects/m8-spec-anchored/README.md",
        9,
    ),
]


def dod_items(path: Path) -> list[str]:
    """Every unchecked-box line in a file, in order, exactly as written."""
    return [
        line
        for line in path.read_text(encoding="utf-8").splitlines()
        if line.startswith("- [ ]")
    ]


@pytest.mark.parametrize("milestone,page,readme,count", DOD_PAIRS, ids=lambda v: str(v))
def test_the_two_copies_of_a_definition_of_done_are_identical(milestone, page, readme, count):
    page_items = dod_items(REPO / page)
    readme_items = dod_items(REPO / readme)
    assert page_items == readme_items, (
        f"{milestone}'s Definition of done has drifted between {page} and {readme}. "
        "Both copies move together or a student is marked against a list the page "
        "does not show."
    )


@pytest.mark.parametrize("milestone,page,readme,count", DOD_PAIRS, ids=lambda v: str(v))
def test_the_list_the_comparison_ran_on_is_the_expected_one(milestone, page, readme, count):
    """Guards the equality test against passing vacuously: two files that both stopped
    carrying a checklist compare equal as empty lists."""
    assert len(dod_items(REPO / page)) == count
    assert len(dod_items(REPO / readme)) == count


def test_a_one_character_difference_is_caught(tmp_path):
    """The extractor has to compare text, not shape -- a list of the right length whose
    wording drifted is exactly the failure this module exists to catch."""
    page = tmp_path / "page.md"
    readme = tmp_path / "README.md"
    page.write_text("## Definition of done\n\n- [ ] You shipped it\n", encoding="utf-8")
    readme.write_text("## Definition of done\n\n- [ ] You shipped them\n", encoding="utf-8")
    assert len(dod_items(page)) == len(dod_items(readme)) == 1
    assert dod_items(page) != dod_items(readme)


def test_prose_and_checked_boxes_are_not_collected(tmp_path):
    """Only unchecked items belong to the list a mentor marks. A checked box or an
    indented sub-item is somebody's example, not a done item."""
    doc = tmp_path / "doc.md"
    doc.write_text(
        "- [ ] A real item\n"
        "- [x] An example of a checked box\n"
        "  - [ ] An indented sub-item\n"
        "Prose mentioning - [ ] mid-line\n",
        encoding="utf-8",
    )
    assert dod_items(doc) == ["- [ ] A real item"]
