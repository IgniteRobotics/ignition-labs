"""The two rulesets this tool owns, and the field-level diff against what GitHub has."""

import copy
from typing import Any

from tools.team_github.config import Config, RepoConfig

OWNED = ("dev-main-pushes", "dev-main-pr-gate")
GITHUB_ACTIONS_APP_ID = 15368
REFS = ["refs/heads/main", "refs/heads/development"]


def _bypass(cfg: Config) -> list[dict[str, Any]]:
    return [
        {"actor_id": 1, "actor_type": "OrganizationAdmin", "bypass_mode": "exempt"},
        {"actor_id": cfg.bypass_team_id, "actor_type": "Team", "bypass_mode": "always"},
    ]


def desired_rulesets(
    cfg: Config, repo: RepoConfig, reported_checks: set[str]
) -> dict[str, dict[str, Any]]:
    """Target state of the owned rulesets. Checks nobody has reported are left out."""
    conditions = {"ref_name": {"include": list(REFS), "exclude": []}}
    pushes = {
        "name": "dev-main-pushes",
        "target": "branch",
        "enforcement": "active",
        "conditions": copy.deepcopy(conditions),
        "rules": [
            {"type": "deletion"},
            {"type": "non_fast_forward"},
            {"type": "update"},
            {"type": "creation"},
        ],
        "bypass_actors": _bypass(cfg),
    }
    gate_rules: list[dict[str, Any]] = [
        {
            "type": "pull_request",
            "parameters": {
                "required_approving_review_count": 0,
                "dismiss_stale_reviews_on_push": False,
                "require_code_owner_review": True,
                "require_last_push_approval": False,
                "required_review_thread_resolution": False,
                "allowed_merge_methods": ["merge", "squash", "rebase"],
            },
        }
    ]
    checks = [c for c in repo.required_checks if c in reported_checks]
    if checks:
        gate_rules.append(
            {
                "type": "required_status_checks",
                "parameters": {
                    "strict_required_status_checks_policy": False,
                    "do_not_enforce_on_create": False,
                    "required_status_checks": [
                        {"context": c, "integration_id": GITHUB_ACTIONS_APP_ID} for c in checks
                    ],
                },
            }
        )
    gate = {
        "name": "dev-main-pr-gate",
        "target": "branch",
        "enforcement": "active",
        "conditions": copy.deepcopy(conditions),
        "rules": gate_rules,
        "bypass_actors": [
            {"actor_id": 1, "actor_type": "OrganizationAdmin", "bypass_mode": "always"}
        ],
    }
    return {"dev-main-pushes": pushes, "dev-main-pr-gate": gate}


def gate_contexts(ruleset: dict[str, Any]) -> list[str]:
    for rule in ruleset.get("rules", []):
        if rule["type"] == "required_status_checks":
            return [c["context"] for c in rule["parameters"]["required_status_checks"]]
    return []


def _actors(actors: list[dict[str, Any]]) -> list[tuple[str, int, str]]:
    # GitHub reports the org-admin actor_id as null; the API wants 1 when writing.
    return sorted(
        (
            a["actor_type"],
            0 if a["actor_type"] == "OrganizationAdmin" else int(a.get("actor_id") or 0),
            a["bypass_mode"],
        )
        for a in actors
    )


def _describe(actors: list[dict[str, Any]]) -> str:
    names = []
    for kind, actor_id, mode in _actors(actors):
        who = kind if kind == "OrganizationAdmin" else f"{kind} {actor_id}"
        names.append(f"{who} ({mode})")
    return ", ".join(names) or "none"


def _check_rule_params(params: dict[str, Any]) -> dict[str, Any]:
    flat = {k: v for k, v in params.items() if k != "required_status_checks"}
    flat["contexts"] = sorted(c["context"] for c in params.get("required_status_checks", []))
    return flat


def diff_ruleset(current: dict[str, Any] | None, desired: dict[str, Any]) -> list[str]:
    """Human-readable differences over the fields this tool sets. [] means converged."""
    if current is None:
        return ["does not exist; will be created"]
    lines: list[str] = []
    for key in ("target", "enforcement"):
        if current.get(key) != desired[key]:
            lines.append(f"{key}: {current.get(key)!r} → {desired[key]!r}")
    cur_refs = sorted(current.get("conditions", {}).get("ref_name", {}).get("include", []))
    des_refs = sorted(desired["conditions"]["ref_name"]["include"])
    if cur_refs != des_refs:
        lines.append(f"conditions.ref_name.include: {cur_refs} → {des_refs}")

    cur_rules = {r["type"]: r for r in current.get("rules", [])}
    des_rules = {r["type"]: r for r in desired["rules"]}
    for rule_type in des_rules:
        if rule_type not in cur_rules:
            lines.append(f"rule {rule_type}: missing → added")
    for rule_type in cur_rules:
        if rule_type not in des_rules:
            lines.append(f"rule {rule_type}: present → removed")
    for rule_type, des_rule in des_rules.items():
        if rule_type not in cur_rules or "parameters" not in des_rule:
            continue
        cur_params = cur_rules[rule_type].get("parameters", {})
        des_params = des_rule["parameters"]
        if rule_type == "required_status_checks":
            cur_params, des_params = _check_rule_params(cur_params), _check_rule_params(des_params)
        for field, want in des_params.items():
            have = cur_params.get(field)
            if have != want:
                lines.append(f"{rule_type}.{field}: {have} → {want}")

    if _actors(current.get("bypass_actors", [])) != _actors(desired["bypass_actors"]):
        lines.append(
            f"bypass_actors: {_describe(current.get('bypass_actors', []))} "
            f"→ {_describe(desired['bypass_actors'])}"
        )
    return lines
