"""The site guard enforces spec §14: every prose file renders, zero files from projects/."""

from pathlib import Path

from tools.site_guard import forbidden_files, missing_pages


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
    # doc-builder renders every markdown file it finds regardless of
    # 00-index.md linkage (verified against superpowers/ during Task 3's
    # investigation) — so anything under projects/, of any extension,
    # must be caught, not just the FRC source-file suffixes.
    site = _make_site(tmp_path, ["index.html", "projects/m2-oop/README.md"])
    assert forbidden_files(site) == ["projects/m2-oop/README.md"]


def test_markdown_derived_page_under_projects_is_forbidden(tmp_path):
    # The realistic shape of the leak: doc-builder converts a stray
    # projects/**/README.md into an .html page in the built site (verified
    # via the probe: projects/m-probe/README.md -> projects/m-probe/index.html).
    site = _make_site(tmp_path, ["index.html", "projects/m2-oop/index.html"])
    assert forbidden_files(site) == ["projects/m2-oop/index.html"]


def test_superpowers_path_in_site_is_forbidden(tmp_path):
    # doc-builder renders every markdown file it finds regardless of
    # 00-index.md linkage — superpowers/ (agent notes, plans, specs) leaked
    # into a raw build-from-source run even though nothing links it. The
    # guard must catch this on any build path, not just the staged one.
    site = _make_site(
        tmp_path, ["index.html", "superpowers/specs/whatever.html"]
    )
    assert forbidden_files(site) == ["superpowers/specs/whatever.html"]


def test_missing_required_page_is_reported(tmp_path):
    site = _make_site(tmp_path, ["index.html"])
    assert missing_pages(site, ["programming/git-workflow"]) == ["programming/git-workflow"]


def test_present_required_page_is_not_reported(tmp_path):
    site = _make_site(tmp_path, ["programming/git-workflow/index.html"])
    assert missing_pages(site, ["programming/git-workflow"]) == []
