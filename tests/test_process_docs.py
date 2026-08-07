"""Encodes the spec §14 acceptance criteria for the two process documents.

These assert on load-bearing invariants only — the rule sentence, the presence
of each command, each milestone row. They deliberately do not assert on
surrounding prose, which should be free to improve.
"""

from pathlib import Path

import pytest

REPO = Path(__file__).resolve().parents[1]
GIT_WORKFLOW = REPO / "programming" / "git-workflow.md"


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
    assert "IgniteRobotics/docs:main" in git_workflow


def test_later_milestones_pull_request_into_own_fork(git_workflow):
    assert "into your own fork" in git_workflow
