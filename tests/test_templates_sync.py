"""Templates in templates/ must match their marked region in student-deliverables.md."""

import pytest

from tools.templates_sync import TEMPLATE_NAMES, extract_regions, verify


DOC = """# Deliverables

<!-- template:subsystem-readme start -->
```markdown
# [Subsystem Name]

**Owner:** [your name]
```
<!-- template:subsystem-readme end -->

Some prose that is not part of any template.

<!-- template:competition-week-checklist start -->
### Before leaving for the event

- [ ] Everything committed and pushed
<!-- template:competition-week-checklist end -->
"""


def _setup(tmp_path, doc=DOC, templates=None):
    (tmp_path / "docs" / "programming").mkdir(parents=True)
    (tmp_path / "docs" / "programming" / "student-deliverables.md").write_text(doc, encoding="utf-8")
    (tmp_path / "templates").mkdir()
    for name, body in (templates or {}).items():
        (tmp_path / "templates" / f"{name}.md").write_text(body, encoding="utf-8")
    return tmp_path


def test_fenced_region_yields_the_fence_contents_not_the_fence(tmp_path):
    root = _setup(tmp_path)
    regions = extract_regions(root)
    assert regions["subsystem-readme"].startswith("# [Subsystem Name]")
    assert "```" not in regions["subsystem-readme"]


def test_unfenced_region_is_taken_as_is(tmp_path):
    root = _setup(tmp_path)
    regions = extract_regions(root)
    assert regions["competition-week-checklist"].startswith("### Before leaving")
    assert "- [ ] Everything committed and pushed" in regions["competition-week-checklist"]


def test_verify_passes_when_every_template_matches(tmp_path):
    regions = extract_regions(_setup(tmp_path))
    root = _setup(tmp_path / "ok", templates={k: v + "\n" for k, v in regions.items()})
    assert verify(root, names=list(regions)) == []


def test_verify_reports_a_drifted_template(tmp_path):
    regions = extract_regions(_setup(tmp_path))
    drifted = {k: v + "\n" for k, v in regions.items()}
    drifted["subsystem-readme"] = drifted["subsystem-readme"].replace("[your name]", "[my name]")
    root = _setup(tmp_path / "drift", templates=drifted)
    problems = verify(root, names=list(regions))
    assert len(problems) == 1
    assert "subsystem-readme" in problems[0]


def test_verify_reports_a_missing_template_file(tmp_path):
    """The region exists but nobody created the file -- a different failure from drift,
    and it must not be reported as drift or the fix suggested will be the wrong one."""
    regions = extract_regions(_setup(tmp_path))
    present = {k: v + "\n" for k, v in regions.items()}
    del present["subsystem-readme"]
    root = _setup(tmp_path / "missing", templates=present)
    problems = verify(root, names=list(regions))
    assert len(problems) == 1
    assert "subsystem-readme" in problems[0]
    assert "does not exist" in problems[0]


def test_a_missing_marker_is_an_error_not_a_silent_skip(tmp_path):
    """The failure that would make this whole guard worthless: a template whose region
    markers were never added passes vacuously if extraction just skips it."""
    root = _setup(tmp_path)
    with pytest.raises(KeyError):
        extract_regions(root)["pit-documentation"]


def test_the_real_repository_is_in_sync():
    """Runs against the actual checkout -- this is the test CI relies on."""
    from tools.templates_sync import REPO_ROOT

    assert verify(REPO_ROOT) == []


def test_every_named_template_has_a_file_and_a_region():
    from tools.templates_sync import REPO_ROOT

    regions = extract_regions(REPO_ROOT)
    assert sorted(regions) == sorted(TEMPLATE_NAMES)
    for name in TEMPLATE_NAMES:
        assert (REPO_ROOT / "templates" / f"{name}.md").is_file()
