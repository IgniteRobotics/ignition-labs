import json

import pytest

from tests.team_github_helpers import (
    ORG,
    converged_state,
    load_config,
    load_state,
    repo_cfg,
    runner_for,
)
from tools.team_github import __main__ as cli
from tools.team_github.gh import FakeRunner, GhError, json_call
from tools.team_github.repos import plan_repo

MUTATING = {"PUT", "POST", "PATCH", "DELETE"}


def mutating_calls(runner: FakeRunner) -> list[tuple[str, ...]]:
    out = []
    for call in runner.calls:
        for i, arg in enumerate(call[:-1]):
            if arg in ("-X", "--method") and call[i + 1] in MUTATING:
                out.append(call)
    return out


def test_load_reads_four_repos_with_their_required_checks():
    cfg = load_config()
    assert cfg.org == "IgniteRobotics"
    assert {r.name: r.required_checks for r in cfg.repos} == {
        "Robot-2026": ["Build and Check Code Format", "OpenSpec tool files"],
        "IgniteLib": ["Build and Check Code Format", "OpenSpec tool files"],
        "flashpoint": ["ci-ok", "OpenSpec tool files"],
        "Phoenix-2026": ["Build and Check Code Format", "OpenSpec tool files"],
    }
    assert all(r.default_branch == "development" for r in cfg.repos)
    assert cfg.project_title == "Ignite Programming"
    assert cfg.status_options == ["Backlog", "Ready", "In progress", "In review", "Done"]
    assert cfg.size_options == ["XS", "S", "M", "L", "XL"]
    assert set(cfg.labels) == {"feature", "bug", "hotfix", "spec"}
    assert cfg.mentor_team == "mentors"
    assert cfg.bypass_team_id == 9240266
    assert cfg.old_projects == [3, 5]
    assert cfg.templates_repo == ".github"


def test_fake_runner_raises_naming_unrecorded_args():
    runner = FakeRunner({("api", "a"): "{}"})
    assert runner.run(["api", "a"]) == "{}"
    with pytest.raises(KeyError, match="api.*b"):
        runner.run(["api", "b"])
    assert runner.calls == [("api", "a"), ("api", "b")]


def test_json_call_merges_paginated_arrays():
    runner = FakeRunner({("x",): '[{"a":1}][{"a":2}]'})
    assert json_call(runner, ["x"]) == [{"a": 1}, {"a": 2}]


def test_default_branch_main_plans_switch_to_development():
    cfg = load_config()
    runner = runner_for("Robot-2026", load_state("Robot-2026"))
    changes = plan_repo(runner, cfg, repo_cfg(cfg, "Robot-2026"))
    switch = [c for c in changes if c.kind == "default-branch"]
    assert len(switch) == 1
    assert "main → development" in switch[0].summary
    assert switch[0].scope == "Robot-2026"
    assert mutating_calls(runner) == []
    patch = ("api", f"repos/{ORG}/Robot-2026", "-X", "PATCH", "-f", "default_branch=development")
    runner.responses[patch] = "{}"
    switch[0].apply()
    assert runner.calls[-1] == patch


def test_missing_labels_planned_existing_labels_untouched():
    cfg = load_config()
    state = load_state("Robot-2026")  # has a default `bug` label, colour d73a4a
    state["labels"][0]["color"] = "ffffff"
    assert state["labels"][0]["name"] == "bug"
    runner = runner_for("Robot-2026", state)
    labels = [c for c in plan_repo(runner, cfg, repo_cfg(cfg, "Robot-2026")) if c.kind == "label"]
    assert sorted(c.summary for c in labels) == sorted(
        f"create label {n}" for n in ("feature", "hotfix", "spec")
    )
    post = ("api", f"repos/{ORG}/Robot-2026/labels", "-X", "POST", "-f", "name=feature", "-f", "color=0e8a16")
    runner.responses[post] = "{}"
    next(c for c in labels if c.summary.endswith("feature")).apply()
    assert runner.calls[-1] == post
    assert all("-X" not in call or call == post for call in runner.calls)


def test_label_match_ignores_case():
    cfg = load_config()
    state = converged_state("IgniteLib")
    for label in state["labels"]:
        label["name"] = label["name"].upper()
    runner = runner_for("IgniteLib", state)
    assert plan_repo(runner, cfg, repo_cfg(cfg, "IgniteLib")) == []


@pytest.mark.parametrize("repo", ["Robot-2026", "IgniteLib", "flashpoint", "Phoenix-2026"])
def test_converged_repo_plans_nothing(repo):
    cfg = load_config()
    runner = runner_for(repo, converged_state(repo))
    assert plan_repo(runner, cfg, repo_cfg(cfg, repo)) == []
    assert mutating_calls(runner) == []


def run_cli(argv, runner):
    return cli.main(argv, runner=runner, config_path=None)


def full_runner(states: dict) -> FakeRunner:
    merged: dict = {}
    for repo, state in states.items():
        merged.update(runner_for(repo, state).responses)
    return FakeRunner(merged)


def test_cli_dry_run_exit_codes(capsys):
    repos = ["Robot-2026", "IgniteLib", "flashpoint", "Phoenix-2026"]
    pending = full_runner({r: load_state(r) for r in repos})
    assert run_cli(["--only", "repos"], pending) == 1
    out = capsys.readouterr().out
    assert "main → development" in out
    assert "PENDING" in out
    assert mutating_calls(pending) == []

    converged = full_runner({r: converged_state(r) for r in repos})
    assert run_cli(["--only", "repos"], converged) == 0
    assert "nothing to do" in capsys.readouterr().out.lower()
    assert mutating_calls(converged) == []


def test_cli_pending_only_exits_zero(capsys):
    repos = ["Robot-2026", "IgniteLib", "flashpoint", "Phoenix-2026"]
    states = {r: converged_state(r) for r in repos}
    for state in states.values():
        state["check_runs"] = {"total_count": 0, "check_runs": []}
        for ruleset in state["rulesets"].values():
            ruleset["rules"] = [r for r in ruleset["rules"] if r["type"] != "required_status_checks"]
    assert run_cli([], full_runner(states)) == 0
    assert "PENDING" in capsys.readouterr().out


def test_cli_apply_runs_changes_and_returns_2_on_gh_failure():
    cfg = load_config()
    state = load_state("Robot-2026")
    runner = runner_for("Robot-2026", state)

    # Apply path: every Change.apply goes through the runner; a failing PATCH -> rc 2.
    runner.responses[
        ("api", f"repos/{ORG}/Robot-2026", "-X", "PATCH", "-f", "default_branch=development")
    ] = GhError(["api"], 1, "boom")
    code = cli.run_repos(runner, cfg, apply=True, repos=["Robot-2026"])
    assert code == 2


def test_cli_apply_success_returns_0():
    cfg = load_config()
    runner = runner_for("Robot-2026", load_state("Robot-2026"))
    # Accept any mutating call.
    class Accepting(FakeRunner):
        def run(self, args, *, input=None):
            if "-X" in args:
                self.calls.append(tuple(args))
                self.inputs.append(input)
                return "{}"
            return super().run(args, input=input)

    accepting = Accepting(runner.responses)
    assert cli.run_repos(accepting, cfg, apply=True, repos=["Robot-2026"]) == 0
    assert mutating_calls(accepting)
    assert not any(c[:3] == ("api",) and "DELETE" in c for c in accepting.calls)


def test_json_body_sent_on_stdin_for_rulesets():
    cfg = load_config()
    runner = runner_for("Robot-2026", load_state("Robot-2026"))
    changes = plan_repo(runner, cfg, repo_cfg(cfg, "Robot-2026"))
    gate = next(c for c in changes if c.kind == "ruleset" and "dev-main-pr-gate" in c.summary)
    runner.responses[
        ("api", f"repos/{ORG}/Robot-2026/rulesets", "-X", "POST", "--input", "-")
    ] = "{}"
    gate.apply()
    body = json.loads(runner.inputs[-1])
    assert body["name"] == "dev-main-pr-gate"


def test_cli_exits_2_and_plans_nothing_when_check_lookup_fails(capsys):
    repos = ["Robot-2026", "IgniteLib", "flashpoint", "Phoenix-2026"]
    runner = full_runner({r: load_state(r) for r in repos})
    key = ("api", f"repos/{ORG}/IgniteLib/commits/development/check-runs?per_page=100")
    runner.responses[key] = GhError(list(key), 1, "gh: Bad credentials (HTTP 401)")
    assert run_cli(["--only", "repos", "--apply"], runner) == 2
    assert mutating_calls(runner) == []
    assert "error reading GitHub" in capsys.readouterr().err
