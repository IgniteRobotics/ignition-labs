import copy

from tests.team_github_helpers import (
    converged_state,
    load_config,
    load_state,
    repo_cfg,
    runner_for,
)
from tools.team_github.gh import GhError
from tools.team_github.repos import plan_repo
from tools.team_github.rulesets import desired_rulesets, diff_ruleset

BUILD = "Build and Check Code Format"
DRIFT = "OpenSpec tool files"


def rule(ruleset: dict, rule_type: str) -> dict:
    return next(r for r in ruleset["rules"] if r["type"] == rule_type)


def contexts(ruleset: dict) -> list[str]:
    params = rule(ruleset, "required_status_checks")["parameters"]
    return [c["context"] for c in params["required_status_checks"]]


def test_pr_gate_requires_code_owner_review():
    cfg = load_config()
    gate = desired_rulesets(cfg, repo_cfg(cfg, "IgniteLib"), {BUILD, DRIFT})["dev-main-pr-gate"]
    params = rule(gate, "pull_request")["parameters"]
    assert params["require_code_owner_review"] is True
    assert params["required_approving_review_count"] == 0
    assert contexts(gate) == [BUILD, DRIFT]


def test_bypass_actors_are_admin_and_code_reviewers():
    cfg = load_config()
    pushes = desired_rulesets(cfg, repo_cfg(cfg, "IgniteLib"), set())["dev-main-pushes"]
    actors = {(a["actor_type"], a["bypass_mode"]): a["actor_id"] for a in pushes["bypass_actors"]}
    assert actors == {("OrganizationAdmin", "exempt"): 1, ("Team", "always"): 9240266}
    # Same shape as what is live today in the recorded fixture.
    live = load_state("IgniteLib")["rulesets"]["dev-main-pushes"]
    assert diff_ruleset(live, pushes) == []


def test_unreported_required_check_is_pending_not_applied():
    cfg = load_config()
    rc = repo_cfg(cfg, "IgniteLib")
    gate = desired_rulesets(cfg, rc, {BUILD})["dev-main-pr-gate"]
    assert contexts(gate) == [BUILD]

    state = load_state("IgniteLib")  # BUILD reported, DRIFT not
    runner = runner_for("IgniteLib", state)
    changes = plan_repo(runner, cfg, rc)
    pending = [c for c in changes if c.kind == "required-check"]
    assert len(pending) == 1
    assert pending[0].pending is True
    assert DRIFT in pending[0].summary
    before = len(runner.calls)
    pending[0].apply()
    assert len(runner.calls) == before


def test_no_reported_checks_omits_status_rule_entirely():
    cfg = load_config()
    gate = desired_rulesets(cfg, repo_cfg(cfg, "Phoenix-2026"), set())["dev-main-pr-gate"]
    assert [r["type"] for r in gate["rules"]] == ["pull_request"]


def test_drifted_owned_ruleset_shows_field_diff():
    cfg = load_config()
    desired = desired_rulesets(cfg, repo_cfg(cfg, "IgniteLib"), {BUILD, DRIFT})["dev-main-pr-gate"]
    current = load_state("IgniteLib")["rulesets"]["dev-main-pr-gate"]
    lines = diff_ruleset(current, desired)
    assert any("require_code_owner_review: False → True" in line for line in lines)
    assert any(DRIFT in line for line in lines)
    # GitHub's extras (required_reviewers, node_id, _links...) are not drift.
    assert not any("required_reviewers" in line or "node_id" in line for line in lines)


def test_diff_missing_ruleset_says_created():
    cfg = load_config()
    desired = desired_rulesets(cfg, repo_cfg(cfg, "IgniteLib"), set())["dev-main-pushes"]
    assert diff_ruleset(None, desired) == ["does not exist; will be created"]


def test_diff_catches_enforcement_ref_and_bypass_drift():
    cfg = load_config()
    desired = desired_rulesets(cfg, repo_cfg(cfg, "IgniteLib"), set())["dev-main-pushes"]
    current = copy.deepcopy(load_state("IgniteLib")["rulesets"]["dev-main-pushes"])
    current["enforcement"] = "evaluate"
    current["conditions"]["ref_name"]["include"] = ["refs/heads/main"]
    current["bypass_actors"] = []
    current["rules"] = current["rules"][:2]
    lines = "\n".join(diff_ruleset(current, desired))
    assert "enforcement: 'evaluate' → 'active'" in lines
    assert "ref_name.include" in lines
    assert "bypass_actors" in lines
    assert "update" in lines or "creation" in lines


def test_classic_protection_reported_not_removed():
    cfg = load_config()
    rc = repo_cfg(cfg, "flashpoint")
    runner = runner_for("flashpoint", load_state("flashpoint"))
    changes = plan_repo(runner, cfg, rc)
    classic = [c for c in changes if c.kind == "classic-protection"]
    assert len(classic) == 1
    assert classic[0].report_only and not classic[0].actionable
    text = " ".join(classic[0].detail)
    assert "ci-ok" in text and "covers" in text
    assert not [c for c in runner.calls if "DELETE" in c]
    classic[0].apply()
    assert not [c for c in runner.calls if "DELETE" in c]


def test_classic_protection_not_covered_when_check_unreported():
    cfg = load_config()
    state = load_state("flashpoint")
    state["check_runs"] = {"total_count": 0, "check_runs": []}
    state["pr_check_runs"] = {}
    runner = runner_for("flashpoint", state)
    classic = next(c for c in plan_repo(runner, cfg, repo_cfg(cfg, "flashpoint")) if c.kind == "classic-protection")
    assert "NOT covered" in " ".join(classic.detail)


def test_unowned_ruleset_reported_not_touched():
    cfg = load_config()
    state = converged_state("IgniteLib")
    other = copy.deepcopy(state["rulesets"]["dev-main-pushes"])
    other.update({"id": 4242, "name": "something-else"})
    state["rulesets"]["something-else"] = other
    runner = runner_for("IgniteLib", state)
    changes = plan_repo(runner, cfg, repo_cfg(cfg, "IgniteLib"))
    assert [c.kind for c in changes] == ["unowned-ruleset"]
    assert "something-else" in changes[0].summary
    assert not changes[0].actionable
    changes[0].apply()
    assert not [c for c in runner.calls if "-X" in c]


def test_pr_gate_bypass_is_admin_only():
    cfg = load_config()
    gate = desired_rulesets(cfg, repo_cfg(cfg, "IgniteLib"), set())["dev-main-pr-gate"]
    assert [(a["actor_type"], a["bypass_mode"]) for a in gate["bypass_actors"]] == [
        ("OrganizationAdmin", "always")
    ]
    live = load_state("IgniteLib")["rulesets"]["dev-main-pr-gate"]
    assert not [l for l in diff_ruleset(live, gate) if "bypass" in l]


def test_check_reported_on_recent_pr_is_required():
    cfg = load_config()
    rc = repo_cfg(cfg, "Robot-2026")
    state = load_state("Robot-2026")
    assert state["check_runs"]["check_runs"] == []  # absent on development HEAD
    assert any(
        c["name"] == BUILD for v in state["pr_check_runs"].values() for c in v["check_runs"]
    )
    changes = plan_repo(runner_for("Robot-2026", state), cfg, rc)
    pending = [c.summary for c in changes if c.kind == "required-check"]
    assert pending == [f'require check "{DRIFT}"']
    gate = next(c for c in changes if "dev-main-pr-gate" in c.summary)
    assert gate.kind == "ruleset"
    runner = runner_for("Robot-2026", state)
    runner.responses[("api", "repos/IgniteRobotics/Robot-2026/rulesets", "-X", "POST", "--input", "-")] = "{}"
    next(c for c in plan_repo(runner, cfg, rc) if "dev-main-pr-gate" in c.summary).apply()
    import json as _json

    assert contexts(_json.loads(runner.inputs[-1])) == [BUILD]


def test_live_required_check_is_never_stripped():
    cfg = load_config()
    state = converged_state("IgniteLib")  # live gate requires BUILD and DRIFT
    state["check_runs"] = {"total_count": 0, "check_runs": []}
    state["pr_check_runs"] = {}
    runner = runner_for("IgniteLib", state)
    changes = plan_repo(runner, cfg, repo_cfg(cfg, "IgniteLib"))
    assert changes == []


def test_reported_checks_error_propagates():
    import pytest

    cfg = load_config()
    state = load_state("IgniteLib")
    runner = runner_for("IgniteLib", state)
    key = ("api", "repos/IgniteRobotics/IgniteLib/commits/development/check-runs?per_page=100")
    runner.responses[key] = GhError(list(key), 1, "gh: Internal Server Error (HTTP 500)")
    with pytest.raises(GhError):
        plan_repo(runner, cfg, repo_cfg(cfg, "IgniteLib"))


def test_missing_branch_means_nothing_reported():
    cfg = load_config()
    state = load_state("IgniteLib")
    runner = runner_for("IgniteLib", state)
    key = ("api", "repos/IgniteRobotics/IgniteLib/commits/development/check-runs?per_page=100")
    runner.responses[key] = GhError(list(key), 1, "gh: No commit found (HTTP 422)")
    plan_repo(runner, cfg, repo_cfg(cfg, "IgniteLib"))  # does not raise


def test_check_without_workflow_on_branch_is_pending():
    cfg = load_config()
    state = load_state("Phoenix-2026")
    state["workflows"] = None  # no .github/workflows on the branch
    assert any(  # ...yet old PR heads reported it
        c["name"] == BUILD for v in state["pr_check_runs"].values() for c in v["check_runs"]
    )
    changes = plan_repo(runner_for("Phoenix-2026", state), cfg, repo_cfg(cfg, "Phoenix-2026"))
    pending = sorted(c.summary for c in changes if c.kind == "required-check")
    assert pending == [f'require check "{BUILD}"', f'require check "{DRIFT}"']


def test_check_defined_by_workflow_and_seen_is_required():
    cfg = load_config()
    state = load_state("Robot-2026")  # pr-build-check.yml on development, seen on PR heads
    changes = plan_repo(runner_for("Robot-2026", state), cfg, repo_cfg(cfg, "Robot-2026"))
    assert [c.summary for c in changes if c.kind == "required-check"] == [f'require check "{DRIFT}"']


def test_live_required_check_kept_even_without_workflow():
    cfg = load_config()
    state = converged_state("IgniteLib")
    state["workflows"] = None
    assert plan_repo(runner_for("IgniteLib", state), cfg, repo_cfg(cfg, "IgniteLib")) == []


def test_job_names_name_beats_key_and_key_is_fallback():
    from tools.team_github.repos import job_names

    text = (
        "name: W\non: push\njobs:\n"
        "  build:\n    name: \"Build and Check\"  # c\n    steps:\n      - name: step\n"
        "  ci-ok:\n    runs-on: x\n    steps:\n      - name: not-a-job\n"
        "  other:\n    name: Other Job\n"
    )
    assert job_names(text) == {"Build and Check", "ci-ok", "Other Job"}
    assert job_names("name: no jobs\n") == set()
