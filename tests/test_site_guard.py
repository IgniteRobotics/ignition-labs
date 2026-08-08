"""The site guard enforces spec §14: every prose file renders, zero files from projects/."""

from pathlib import Path

from tools.site_guard import REQUIRED_PAGES, forbidden_files, missing_pages


def _make_site(tmp_path: Path, files: list[str]) -> Path:
    site = tmp_path / "_site"
    for rel in files:
        target = site / rel
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text("x", encoding="utf-8")
    return site


def test_clean_site_has_no_forbidden_files(tmp_path):
    site = _make_site(tmp_path, ["index.html", "programming/index.html"])
    assert forbidden_files(site) == []


def test_java_source_in_site_is_forbidden(tmp_path):
    site = _make_site(tmp_path, ["index.html", "projects/m1/Deadband.java"])
    assert forbidden_files(site) == ["projects/m1/Deadband.java"]


def test_gradle_and_vendordep_files_are_forbidden(tmp_path):
    site = _make_site(
        tmp_path,
        ["build.gradle", "gradlew", "vendordeps/Phoenix6.json", "index.html"],
    )
    assert forbidden_files(site) == ["build.gradle", "gradlew", "vendordeps/Phoenix6.json"]


def test_markdown_file_under_projects_is_forbidden(tmp_path):
    # MkDocs cannot read outside docs_dir, so projects/ is normally
    # unpublishable by construction. This guards the remaining case:
    # someone creating docs/projects/. Any extension, not just the FRC
    # source-file suffixes.
    site = _make_site(tmp_path, ["index.html", "projects/m2-oop/README.md"])
    assert forbidden_files(site) == ["projects/m2-oop/README.md"]


def test_markdown_derived_page_under_projects_is_forbidden(tmp_path):
    # The realistic shape of a leak: a stray README.md inside docs_dir
    # becomes an .html page in the built site
    # (docs/projects/m2-oop/README.md -> projects/m2-oop/index.html).
    site = _make_site(tmp_path, ["index.html", "projects/m2-oop/index.html"])
    assert forbidden_files(site) == ["projects/m2-oop/index.html"]


def test_superpowers_path_in_site_is_forbidden(tmp_path):
    # superpowers/ holds design records, plans, and session notes, and is
    # never published. It sits outside docs_dir, so this is belt-and-braces
    # against a future layout change that pulls it back inside.
    site = _make_site(
        tmp_path, ["index.html", "superpowers/specs/whatever.html"]
    )
    assert forbidden_files(site) == ["superpowers/specs/whatever.html"]


def test_tools_path_in_site_is_forbidden(tmp_path):
    # tools/ holds CI verification scripts (spec §10) and sits outside
    # docs_dir; this is belt-and-braces against a future layout change
    # that pulls it back inside.
    site = _make_site(tmp_path, ["index.html", "tools/site_guard.html"])
    assert forbidden_files(site) == ["tools/site_guard.html"]


def test_tests_path_in_site_is_forbidden(tmp_path):
    # tests/ holds tests for tools/ (spec §10) and sits outside docs_dir;
    # same belt-and-braces reasoning as tools/ and superpowers/.
    site = _make_site(tmp_path, ["index.html", "tests/test_site_guard.html"])
    assert forbidden_files(site) == ["tests/test_site_guard.html"]


def test_missing_required_page_is_reported(tmp_path):
    site = _make_site(tmp_path, ["index.html"])
    assert missing_pages(site, ["programming/git-workflow"]) == ["programming/git-workflow"]


def test_present_required_page_is_not_reported(tmp_path):
    site = _make_site(tmp_path, ["programming/git-workflow/index.html"])
    assert missing_pages(site, ["programming/git-workflow"]) == []


def test_setup_and_contributing_are_required_pages():
    # SETUP.md is the first document a student opens and CONTRIBUTING.md
    # governs the PR workflow; both must actually be published, not just
    # present on disk. This makes that a checked invariant rather than a
    # hope (spec §15: SETUP's fork-Actions step is the highest-likelihood
    # M0 failure).
    assert "SETUP" in REQUIRED_PAGES
    assert "CONTRIBUTING" in REQUIRED_PAGES


def test_missing_setup_page_is_reported(tmp_path):
    site = _make_site(tmp_path, ["index.html", "CONTRIBUTING/index.html"])
    assert missing_pages(site, ["SETUP", "CONTRIBUTING"]) == ["SETUP"]


def test_present_setup_and_contributing_pages_are_not_reported(tmp_path):
    site = _make_site(
        tmp_path, ["SETUP/index.html", "CONTRIBUTING/index.html"]
    )
    assert missing_pages(site, ["SETUP", "CONTRIBUTING"]) == []
