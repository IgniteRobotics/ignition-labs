"""Fixture loading for the team_github tests: recorded `gh` JSON replayed through FakeRunner."""

import copy
import json
from pathlib import Path

from tools.team_github import config as config_mod
from tools.team_github.gh import FakeRunner, GhError

FIXTURES = Path(__file__).parent / "fixtures" / "team_github"
CONFIG_PATH = Path(__file__).resolve().parent.parent / "tools" / "team_github.toml"
ORG = "IgniteRobotics"


def load_config() -> config_mod.Config:
    return config_mod.load(CONFIG_PATH)


def repo_cfg(cfg: config_mod.Config, name: str) -> config_mod.RepoConfig:
    return next(r for r in cfg.repos if r.name == name)


def load_state(repo: str) -> dict:
    """Recorded state of one repo as plain dicts, editable by a test before it is replayed."""
    base = FIXTURES / repo
    rulesets = {}
    for path in base.glob("ruleset-*.json"):
        detail = json.loads(path.read_text())
        rulesets[detail["name"]] = detail
    protection = json.loads((base / "protection-main.json").read_text())
    return {
        "repo": json.loads((base / "repo.json").read_text()),
        "labels": json.loads((base / "labels.json").read_text()),
        "rulesets": rulesets,
        "check_runs": json.loads((base / "check-runs-development.json").read_text()),
        "pr_check_runs": json.loads((base / "check-runs-pr-heads.json").read_text()),
        "protection": None if "required_status_checks" not in protection else protection,
    }


def runner_for(repo: str, state: dict) -> FakeRunner:
    state = copy.deepcopy(state)
    root = f"repos/{ORG}/{repo}"
    responses: dict[tuple[str, ...], object] = {
        ("api", root): json.dumps(state["repo"]),
        ("api", f"{root}/labels?per_page=100", "--paginate"): json.dumps(state["labels"]),
        ("api", f"{root}/rulesets"): json.dumps(
            [{"id": d["id"], "name": name} for name, d in state["rulesets"].items()]
        ),
        ("api", f"{root}/commits/development/check-runs?per_page=100"): json.dumps(
            state["check_runs"]
        ),
    }
    prs = [{"headRefOid": sha} for sha in state["pr_check_runs"]]
    responses[
        ("pr", "list", "--repo", f"{ORG}/{repo}", "--base", "development", "--state", "all",
         "--limit", "20", "--json", "headRefOid")
    ] = json.dumps(prs)
    for sha, runs in state["pr_check_runs"].items():
        responses[("api", f"{root}/commits/{sha}/check-runs?per_page=100")] = json.dumps(runs)
    for detail in state["rulesets"].values():
        responses[("api", f"{root}/rulesets/{detail['id']}")] = json.dumps(detail)
    protection_args = ("api", f"{root}/branches/main/protection")
    if state["protection"] is None:
        responses[protection_args] = GhError(
            list(protection_args), 1, "gh: Branch not protected (HTTP 404)"
        )
    else:
        responses[protection_args] = json.dumps(state["protection"])
    return FakeRunner(responses)


def converged_state(repo: str) -> dict:
    """Recorded state rewritten so this tool has nothing to do."""
    from tools.team_github.rulesets import desired_rulesets

    cfg = load_config()
    rc = repo_cfg(cfg, repo)
    state = load_state(repo)
    state["repo"]["default_branch"] = "development"
    have = {label["name"] for label in state["labels"]}
    for name, color in cfg.labels.items():
        if name not in have:
            state["labels"].append({"name": name, "color": color})
    state["check_runs"] = {
        "total_count": len(rc.required_checks),
        "check_runs": [{"name": c} for c in rc.required_checks],
    }
    reported = set(rc.required_checks)
    state["pr_check_runs"] = {}
    state["rulesets"] = {}
    for index, (name, body) in enumerate(desired_rulesets(cfg, rc, reported).items()):
        detail = copy.deepcopy(body)
        detail.update({"id": 1000 + index, "node_id": "x", "source": f"{ORG}/{repo}"})
        for actor in detail["bypass_actors"]:
            if actor["actor_type"] == "OrganizationAdmin":
                actor["actor_id"] = None
        state["rulesets"][name] = detail
    state["protection"] = None
    return state
