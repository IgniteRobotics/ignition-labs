"""Tests for the team-repo kit and the org-wide GitHub templates."""

import re
import shutil
import subprocess
from pathlib import Path

import pytest

REPO = Path(__file__).resolve().parent.parent
GITHUB_TEMPLATES = REPO / "templates" / "github"
ISSUE_DIR = GITHUB_TEMPLATES / "ISSUE_TEMPLATE"
PR_TEMPLATE = GITHUB_TEMPLATES / "PULL_REQUEST_TEMPLATE.md"
REPO_PR_TEMPLATE = REPO / ".github" / "PULL_REQUEST_TEMPLATE.md"
IGNITE_DIR = REPO / "templates" / "team-repo" / ".ignite"
HOOKS_DIR = IGNITE_DIR / "hooks"


def _lines(path: Path) -> list[str]:
    return path.read_text(encoding="utf-8").splitlines()


def _field_block(path: Path, field_id: str) -> str:
    """Return the single body item (one `  - type:` entry) whose `id:` is field_id."""
    text = path.read_text(encoding="utf-8")
    items = re.split(r"^(?=  - type:)", text, flags=re.M)[1:]
    matches = [i for i in items if re.search(rf"^    id: {re.escape(field_id)}$", i, re.M)]
    assert len(matches) == 1, f"{path.name}: expected one body item with id: {field_id}"
    block = matches[0]
    assert len(re.findall(r"^    id: ", block, re.M)) == 1, "block spans multiple fields"
    return block


def test_issue_forms_have_required_keys_and_label() -> None:
    for name, label in (("feature", "feature"), ("bug", "bug"), ("hotfix", "hotfix")):
        lines = _lines(ISSUE_DIR / f"{name}.yml")
        for key in ("name:", "description:", "body:"):
            assert any(line.startswith(key) for line in lines), f"{name}.yml missing {key}"
        assert f'labels: ["{label}"]' in lines, f"{name}.yml missing labels: [\"{label}\"]"


def test_feature_form_requires_problem_statement() -> None:
    block = _field_block(ISSUE_DIR / "feature.yml", "problem")
    assert block.lstrip().startswith("- type: textarea")
    assert "required: true" in block


def test_bug_form_asks_for_log_or_replay() -> None:
    text = (ISSUE_DIR / "bug.yml").read_text(encoding="utf-8").lower()
    assert "log" in text and "replay" in text
    block = _field_block(ISSUE_DIR / "bug.yml", "evidence")
    assert "required: true" in block


def test_hotfix_form_records_event_and_branch() -> None:
    for field_id in ("event", "branch"):
        block = _field_block(ISSUE_DIR / "hotfix.yml", field_id)
        assert "required: true" in block, f"hotfix {field_id} not required"


def test_issue_config_disables_blank_issues() -> None:
    assert "blank_issues_enabled: false" in _lines(ISSUE_DIR / "config.yml")


def _tier_lines(path: Path) -> list[str]:
    return [line for line in _lines(path) if line.startswith("- [ ] ")]


def test_pr_template_sections() -> None:
    text = PR_TEMPLATE.read_text(encoding="utf-8")
    assert "Closes #" in text
    assert "no spec — reason" in text
    assert "openspec/changes/" in text
    assert "I can explain every line" in text
    # The three AI tiers match this repo's PR template verbatim (continuation lines too).
    repo_text = REPO_PR_TEMPLATE.read_text(encoding="utf-8")
    start = repo_text.index("- [ ] 🔴")
    end = repo_text.index("**What the tool did")
    tiers = repo_text[start:end].strip()
    assert tiers in text
    order = ["Closes #", "## What changed", "## Spec", "## AI use disclosure", "## I can explain this"]
    positions = [text.index(s) for s in order]
    assert positions == sorted(positions)


def test_hook_suite_passes_under_node() -> None:
    node = shutil.which("node")
    if node is None:
        pytest.fail("Node >= 20.19 is required to run the hook suite (node not found)")
    version = subprocess.run([node, "--version"], capture_output=True, text=True).stdout.strip()
    major, minor = (int(x) for x in version.lstrip("v").split(".")[:2])
    if (major, minor) < (20, 19):
        pytest.fail(f"Node >= 20.19 is required to run the hook suite (found {version})")
    # Pass explicit files: newer Node treats a directory argument as a module, and globs fail on 20.
    test_files = sorted(str(p) for p in IGNITE_DIR.rglob("*.test.mjs"))
    assert test_files, "no *.test.mjs files found under .ignite/"
    result = subprocess.run([node, "--test", *test_files], capture_output=True, text=True)
    assert result.returncode == 0, result.stdout + result.stderr


# --- Task 6: root files -------------------------------------------------------------------

KIT_ROOT = REPO / "templates" / "team-repo"
AGENTS_MD = KIT_ROOT / "AGENTS.md"
SITE = "https://igniterobotics.github.io/ignition-labs/"


def _agents() -> str:
    return AGENTS_MD.read_text(encoding="utf-8")


def test_claude_md_is_only_the_import() -> None:
    assert (KIT_ROOT / "CLAUDE.md").read_text(encoding="utf-8").strip() == "@AGENTS.md"


def test_agents_md_under_4kib() -> None:
    assert len(AGENTS_MD.read_bytes()) < 4096


def test_agents_md_states_spec_rule() -> None:
    assert (
        "The person who owns the change writes the spec unless they have completed M8 "
        "or are a mentor. If you don't know which, ask before drafting."
    ) in _agents()


def test_agents_md_links() -> None:
    text = _agents()
    for page in (
        "programming/coding-standards/",
        "programming/ai-use-standard/",
        "programming/issues-and-projects/",
        "programming/ai/the-loop/",
    ):
        assert f"({SITE}{page}" in text, page
    # Absolute site URLs only: no repo-relative markdown links.
    assert not re.search(r"\]\((?!https?://|#)", text)


def test_agents_md_rules() -> None:
    text = _agents()
    assert "src/test/" in text
    assert "openspec init" in text and "openspec update" in text
    assert "feature/<num>-<name>" in text
    assert "Closes #<num>" in text
    assert "openspec archive" in text and "last commit" in text
    assert "<!-- repo-specific:start -->" in text
    assert text.index("<!-- repo-specific:start -->") < text.index("<!-- repo-specific:end -->")


def test_codeowners_covers_tool_paths() -> None:
    lines = _lines(KIT_ROOT / ".github" / "CODEOWNERS")
    for path in (
        "/.claude/", "/.agents/", "/.github/skills/", "/.github/prompts/",
        "/openspec/config.yaml", "/.ignite/", "/AGENTS.md", "/CLAUDE.md",
    ):
        assert f"{path} @IgniteRobotics/mentors" in [" ".join(l.split()) for l in lines], path


def test_gitignore_fragment() -> None:
    lines = _lines(KIT_ROOT / "gitignore.fragment")
    entries = [l for l in lines if l.strip() and not l.startswith("#")]
    assert entries == [
        ".claude/settings.local.json",
        ".claude/worktrees/",
        ".codex/",
        ".ignite/implementing",
    ]
    comments = [l for l in lines if l.startswith("#")]
    assert any("remove any bare `.claude/`" in c for c in comments)


def test_no_copilot_instructions_file() -> None:
    assert not (KIT_ROOT / ".github" / "copilot-instructions.md").exists()
