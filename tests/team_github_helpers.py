"""Fixture loading for the team_github tests: recorded `gh` JSON replayed through FakeRunner."""

import base64
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
        "workflows": json.loads((base / "workflows-development.json").read_text()),
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
    wf_dir = ("api", f"{root}/contents/.github/workflows?ref=development")
    if state["workflows"] is None:
        responses[wf_dir] = GhError(list(wf_dir), 1, "gh: Not Found (HTTP 404)")
    else:
        responses[wf_dir] = json.dumps(
            [{"name": p.rsplit("/", 1)[1], "path": p} for p in state["workflows"]]
        )
        for path, text in state["workflows"].items():
            responses[("api", f"{root}/contents/{path}?ref=development")] = json.dumps(
                {"content": base64.b64encode(text.encode()).decode()}
            )
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
    state["workflows"] = {
        ".github/workflows/all.yml": "name: x\njobs:\n"
        + "".join(f"  j{i}:\n    name: {c}\n    runs-on: ubuntu-latest\n" for i, c in enumerate(rc.required_checks))
    }
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


# --- org Project / migration helpers -------------------------------------------------

PROJECTS = FIXTURES / "projects"
TARGET_NUMBER = 6
TARGET_ID = "PVT_target"


def project_fixture(name: str) -> dict:
    return json.loads((PROJECTS / name).read_text())


def list_call() -> tuple[str, ...]:
    return ("project", "list", "--owner", ORG, "--format", "json", "--limit", "100")


def fields_call(number: int) -> tuple[str, ...]:
    return ("project", "field-list", str(number), "--owner", ORG, "--format", "json")


def items_call(number: int) -> tuple[str, ...]:
    return ("project", "item-list", str(number), "--owner", ORG, "--limit", "200", "--format", "json")


def links_call(number: int) -> tuple[str, ...]:
    query = (
        f'query={{organization(login:"{ORG}"){{projectV2(number:{number})'
        "{repositories(first:50){nodes{name}}}}}"
    )
    return ("api", "graphql", "-f", query)


def target_fields(status=None, size=("XS", "S", "M", "L", "XL"), release=True) -> dict:
    status = ["Backlog", "Ready", "In progress", "In review", "Done"] if status is None else status
    fields = [
        {"id": "F_status", "name": "Status", "type": "ProjectV2SingleSelectField",
         "options": [{"id": f"s{i}", "name": n} for i, n in enumerate(status)]},
    ]
    if size is not None:
        fields.append({"id": "F_size", "name": "Size", "type": "ProjectV2SingleSelectField",
                       "options": [{"id": f"z{i}", "name": n} for i, n in enumerate(size)]})
    if release:
        fields.append({"id": "F_release", "name": "Release", "type": "ProjectV2SingleSelectField",
                       "options": [{"id": "r0", "name": "gwinnett"}]})
    return {"fields": fields, "totalCount": len(fields)}


def projects_runner(*, target: dict | None = None, target_items=None, linked=None,
                    sources=(3, 5), extra=None) -> FakeRunner:
    """Replays the recorded org Projects. `target` is the project-list entry's fields/items state."""
    listing = project_fixture("list.json")
    if target is not None:
        listing["projects"].append({
            "id": TARGET_ID, "number": TARGET_NUMBER, "title": "Ignite Programming",
            "closed": False, "url": f"https://github.com/orgs/{ORG}/projects/{TARGET_NUMBER}",
        })
    responses: dict[tuple[str, ...], object] = {list_call(): json.dumps(listing)}
    for n in sources:
        responses[items_call(n)] = json.dumps(project_fixture(f"items-{n}.json"))
        responses[fields_call(n)] = json.dumps(project_fixture(f"fields-{n}.json"))
    if target is not None:
        responses[fields_call(TARGET_NUMBER)] = json.dumps(target)
        items = target_items or []
        responses[items_call(TARGET_NUMBER)] = json.dumps({"items": items, "totalCount": len(items)})
        names = ["Robot-2026", "IgniteLib", "flashpoint", "Phoenix-2026"] if linked is None else linked
        responses[links_call(TARGET_NUMBER)] = json.dumps(
            {"data": {"organization": {"projectV2": {"repositories": {"nodes": [{"name": n} for n in names]}}}}}
        )
    responses.update(extra or {})
    return FakeRunner(responses)
