"""Plan the changes for one repo: default branch, labels, owned rulesets."""

import base64
import json
import re

from tools.team_github.config import Config, RepoConfig
from tools.team_github.gh import GhError, Runner, json_call
from tools.team_github.model import Change
from tools.team_github.rulesets import OWNED, desired_rulesets, diff_ruleset, gate_contexts


def _absent(err: GhError) -> bool:
    """True for "no such branch/commit" (404/422); anything else is a real failure."""
    return "404" in err.stderr or "422" in err.stderr


def _check_names(runner: Runner, root: str, ref: str) -> set[str]:
    try:
        data = json_call(runner, ["api", f"{root}/commits/{ref}/check-runs?per_page=100"])
    except GhError as err:
        if _absent(err):
            return set()
        raise
    return {run["name"] for run in data.get("check_runs", [])}


def _reported_checks(runner: Runner, cfg: Config, repo: RepoConfig) -> set[str]:
    """Check names seen on the default branch HEAD or on the head of any of its last 20 PRs."""
    root = f"repos/{cfg.org}/{repo.name}"
    names = _check_names(runner, root, repo.default_branch)
    prs = json_call(
        runner,
        ["pr", "list", "--repo", f"{cfg.org}/{repo.name}", "--base", repo.default_branch,
         "--state", "all", "--limit", "20", "--json", "headRefOid"],
    )
    for sha in dict.fromkeys(pr["headRefOid"] for pr in prs):
        names |= _check_names(runner, root, sha)
    return names


def job_names(workflow: str) -> set[str]:
    """Check names a workflow file defines: each job's `name:`, else its key. Line-based, no YAML."""
    names: set[str] = set()
    lines = workflow.splitlines()
    try:
        start = next(i for i, l in enumerate(lines) if re.match(r"jobs:\s*(#.*)?$", l))
    except StopIteration:
        return names

    def indent(line: str) -> int:
        return len(line) - len(line.lstrip(" "))

    def unquote(text: str) -> str:
        text = re.sub(r"\s+#.*$", "", text.strip())
        if len(text) >= 2 and text[0] == text[-1] and text[0] in "\"'":
            text = text[1:-1]
        return text

    body = [l for l in lines[start + 1 :] if l.strip() and not l.lstrip().startswith("#")]
    if not body:
        return names
    key_indent = indent(body[0])
    key = None
    explicit = False
    for line in body:
        ind = indent(line)
        if ind < key_indent:
            break  # left the jobs block
        if ind == key_indent:
            if key is not None and not explicit:
                names.add(key)
            match = re.match(r"\s*([^\s:#][^:]*?)\s*:\s*(#.*)?$", line)
            key, explicit = (unquote(match.group(1)) if match else None), False
        elif key is not None and not explicit:
            match = re.match(r"\s*name:\s*(.+)$", line)
            if match and ind == _child_indent(body, key_indent):
                names.add(unquote(match.group(1)))
                explicit = True
    if key is not None and not explicit:
        names.add(key)
    return names


def _child_indent(body: list[str], key_indent: int) -> int:
    return next(len(l) - len(l.lstrip(" ")) for l in body if len(l) - len(l.lstrip(" ")) > key_indent)


def _defined_checks(runner: Runner, cfg: Config, repo: RepoConfig) -> set[str]:
    """Job names defined by workflow files currently on the repo's default branch."""
    root = f"repos/{cfg.org}/{repo.name}"
    try:
        files = json_call(runner, ["api", f"{root}/contents/.github/workflows?ref={repo.default_branch}"])
    except GhError as err:
        if _absent(err):
            return set()
        raise
    names: set[str] = set()
    for entry in files:
        if not entry["name"].endswith((".yml", ".yaml")):
            continue
        blob = json_call(runner, ["api", f"{root}/contents/{entry['path']}?ref={repo.default_branch}"])
        names |= job_names(base64.b64decode(blob["content"]).decode())
    return names


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

    listing = json_call(runner, ["api", f"{root}/rulesets"])
    by_name = {r["name"]: r["id"] for r in listing}
    live = {
        name: json_call(runner, ["api", f"{root}/rulesets/{by_name[name]}"])
        for name in OWNED
        if name in by_name
    }

    # A check the live gate already requires counts as reported: never strip live protection
    # because the tool could not see the check run.
    # Reported = seen running AND defined by a workflow now on the branch.
    reported = _reported_checks(runner, cfg, repo) & _defined_checks(runner, cfg, repo)
    if "dev-main-pr-gate" in live:
        reported |= set(gate_contexts(live["dev-main-pr-gate"]))
    desired = desired_rulesets(cfg, repo, reported)

    for name in OWNED:
        want = desired[name]
        current = live.get(name)
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
                        f"latest commit of {repo.default_branch} or its last 20 PRs.",
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
