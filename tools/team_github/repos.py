"""Plan the changes for one repo: default branch, labels, owned rulesets."""

import json

from tools.team_github.config import Config, RepoConfig
from tools.team_github.gh import GhError, Runner, json_call
from tools.team_github.model import Change
from tools.team_github.rulesets import OWNED, desired_rulesets, diff_ruleset, gate_contexts


def _reported_checks(runner: Runner, root: str) -> set[str]:
    try:
        data = json_call(runner, ["api", f"{root}/commits/development/check-runs?per_page=100"])
    except GhError:
        return set()  # no `development` branch yet: nothing has reported
    return {run["name"] for run in data.get("check_runs", [])}


def _classic_protection(runner: Runner, root: str) -> dict | None:
    try:
        return json_call(runner, ["api", f"{root}/branches/main/protection"])
    except GhError as err:
        if "404" in err.stderr:
            return None
        raise


def plan_repo(runner: Runner, cfg: Config, repo: RepoConfig) -> list[Change]:
    root = f"repos/{cfg.org}/{repo.name}"
    scope = repo.name
    changes: list[Change] = []

    info = json_call(runner, ["api", root])
    if info["default_branch"] != repo.default_branch:
        changes.append(
            Change(
                scope,
                "default-branch",
                f"default branch {info['default_branch']} → {repo.default_branch}",
                [],
                lambda: runner.run(
                    ["api", root, "-X", "PATCH", "-f", f"default_branch={repo.default_branch}"]
                ),
            )
        )

    have = {l["name"].lower() for l in json_call(runner, ["api", f"{root}/labels?per_page=100", "--paginate"])}
    for name, color in cfg.labels.items():
        if name.lower() not in have:
            changes.append(
                Change(
                    scope,
                    "label",
                    f"create label {name}",
                    [f"colour #{color}"],
                    lambda n=name, c=color: runner.run(
                        ["api", f"{root}/labels", "-X", "POST", "-f", f"name={n}", "-f", f"color={c}"]
                    ),
                )
            )

    reported = _reported_checks(runner, root)
    desired = desired_rulesets(cfg, repo, reported)
    listing = json_call(runner, ["api", f"{root}/rulesets"])
    by_name = {r["name"]: r["id"] for r in listing}

    for name in OWNED:
        want = desired[name]
        current = (
            json_call(runner, ["api", f"{root}/rulesets/{by_name[name]}"])
            if name in by_name
            else None
        )
        diff = diff_ruleset(current, want)
        if not diff:
            continue
        body = json.dumps(want)
        if current is None:
            args = ["api", f"{root}/rulesets", "-X", "POST", "--input", "-"]
            summary = f"create ruleset {name}"
        else:
            args = ["api", f"{root}/rulesets/{by_name[name]}", "-X", "PUT", "--input", "-"]
            summary = f"update ruleset {name}"
        changes.append(
            Change(
                scope,
                "ruleset",
                summary,
                diff,
                lambda a=args, b=body: runner.run(a, input=b),
            )
        )

    for check in repo.required_checks:
        if check not in reported:
            changes.append(
                Change(
                    scope,
                    "required-check",
                    f'require check "{check}"',
                    [
                        f'Not in dev-main-pr-gate yet: no check run named "{check}" on the '
                        "latest commit of development.",
                        "Requiring a check nothing reports blocks every PR in this repo. "
                        "Re-run once its workflow has run on development.",
                    ],
                    pending=True,
                )
            )

    protection = _classic_protection(runner, root)
    if protection is not None:
        classic = sorted(
            protection.get("required_status_checks", {}).get("contexts", [])
        )
        wanted = set(gate_contexts(desired["dev-main-pr-gate"]))
        missing = [c for c in classic if c not in wanted]
        verdict = (
            "The desired dev-main-pr-gate covers all of them."
            if not missing
            else f"NOT covered by the desired dev-main-pr-gate: {', '.join(missing)}."
        )
        changes.append(
            Change(
                scope,
                "classic-protection",
                "classic branch protection on main (left in place)",
                [f"Requires checks: {', '.join(classic) or 'none'}.", verdict,
                 "This tool never removes it; delete it by hand once the ruleset covers it."],
                report_only=True,
            )
        )

    for name in sorted(by_name):
        if name not in OWNED:
            changes.append(
                Change(
                    scope,
                    "unowned-ruleset",
                    f'ruleset "{name}" is not managed by this tool (left alone)',
                    [],
                    report_only=True,
                )
            )
    return changes
