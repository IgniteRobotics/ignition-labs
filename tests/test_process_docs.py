"""Encodes the spec §14 acceptance criteria for the two process documents.

These assert on load-bearing invariants only — the rule sentence, the presence
of each command, each milestone row. They deliberately do not assert on
surrounding prose, which should be free to improve.
"""

from pathlib import Path

import pytest

REPO = Path(__file__).resolve().parents[1]
GIT_WORKFLOW = REPO / "docs" / "programming" / "git-workflow.md"


@pytest.fixture(scope="module")
def git_workflow() -> str:
    return GIT_WORKFLOW.read_text(encoding="utf-8")


def test_states_the_rule_verbatim(git_workflow):
    assert "Clone what you're reading. Fork what you'll change. Branch to work." in git_workflow


def test_states_the_kickoff_gate_invariant(git_workflow):
    assert "Fork→PR before the Kickoff Gate. Branch after." in git_workflow


@pytest.mark.parametrize(
    "command",
    [
        "git remote add upstream",
        "git fetch upstream",
        "git merge upstream/main",
        "git checkout -b",
    ],
)
def test_gives_copy_pasteable_commands(git_workflow, command):
    assert command in git_workflow


def test_m0_pull_request_targets_upstream(git_workflow):
    assert "IgniteRobotics/ignition-labs:main" in git_workflow


def test_later_milestones_pull_request_into_own_fork(git_workflow):
    assert "into your own fork" in git_workflow


LADDER = REPO / "docs" / "programming" / "robot-repo-ladder.md"


@pytest.fixture(scope="module")
def ladder() -> str:
    return LADDER.read_text(encoding="utf-8")


@pytest.mark.parametrize("milestone", [f"M{n}" for n in range(9)])
def test_every_milestone_has_a_row(ladder, milestone):
    assert f"**{milestone}**" in ladder


@pytest.mark.parametrize("level", ["browse", "clone", "fork", "branch"])
def test_all_four_contact_levels_are_named(ladder, level):
    assert level in ladder


def test_explains_why_m6_is_not_circular(ladder):
    assert "the pull request comes from a fork" in ladder.lower()


def test_states_the_pure_logic_hard_rule(ladder):
    assert "No timing, no simulation, no hardware." in ladder


def test_no_reference_to_pre_2026_ignite_code(ladder):
    for stale in ("Robot-2025", "Robot-2024", "Robot-2023", "2025-Robotics-Intro"):
        assert stale not in ladder


SETUP = REPO / "docs" / "SETUP.md"
CONTRIBUTING = REPO / "docs" / "CONTRIBUTING.md"


def test_setup_documents_enabling_actions_on_a_fork():
    text = SETUP.read_text(encoding="utf-8")
    assert "I understand my workflows" in text


def test_setup_pins_the_toolchain_versions():
    text = SETUP.read_text(encoding="utf-8")
    for version in ("2026", "17"):
        assert version in text


def test_contributing_states_the_pure_logic_rule():
    text = CONTRIBUTING.read_text(encoding="utf-8")
    assert "No timing, no simulation, no hardware." in text


def test_contributing_links_the_git_workflow():
    text = CONTRIBUTING.read_text(encoding="utf-8")
    assert "programming/git-workflow.md" in text
