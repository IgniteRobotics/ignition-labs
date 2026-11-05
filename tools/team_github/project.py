"""The org-level "Ignite Programming" Project: create it, shape its fields, link the repos.

AUTOMATION VERDICT (checked 2026-11-05; sources in the task 2 report):
auto-add: builtin

- GitHub Team allows 5 auto-add workflows (Free 1, Enterprise 20); the org is on `team`, so the
  four repos fit. The limit page does not say whether it counts per project or per org; old
  Projects #3 and #5 hold one auto-add rule each today, so if it is per org the old ones must be
  closed/pruned first (6 > 5).
- Built-in workflows are NOT scriptable: the GraphQL schema's only workflow mutation is
  `deleteProjectV2Workflow`; nothing creates or enables one. They stay a manual checklist.
- "Pull request linked to issue" exists as a built-in workflow (seen on every current Project via
  GraphQL `ProjectV2.workflows`), though GitHub's built-in-automations page does not list it.
"""

from dataclasses import dataclass

from tools.team_github.config import Config
from tools.team_github.gh import GhError, Runner, json_call
from tools.team_github.model import Change

SCOPE = "project"
# Release options are free-form versions / competition names; a single-select cannot be created
# with zero options, so seed it with the two competition suffixes the design names.
RELEASE_SEED = ["gwinnett", "albany"]


@dataclass(frozen=True)
class ProjectRef:
    number: int
    node_id: str


def find_project(runner: Runner, cfg: Config) -> ProjectRef | None:
    data = json_call(runner, ["project", "list", "--owner", cfg.org, "--format", "json", "--limit", "100"])
    for project in data.get("projects", []):
        if project["title"] == cfg.project_title:
            return ProjectRef(project["number"], project["id"])
    return None


def project_fields(runner: Runner, cfg: Config, ref: ProjectRef) -> list[dict]:
    data = json_call(
        runner, ["project", "field-list", str(ref.number), "--owner", cfg.org, "--format", "json"]
    )
    return data.get("fields", [])


def status_mutation(field_id: str, options: list[str]) -> list[str]:
    """`gh api graphql` args replacing a single-select field's options, in the given order."""
    listed = ",".join(f'{{name:"{name}",color:GRAY,description:""}}' for name in options)
    query = (
        "mutation{updateProjectV2Field(input:{"
        f'fieldId:"{field_id}",singleSelectOptions:[{listed}]'
        "}){projectV2Field{... on ProjectV2SingleSelectField{id}}}}"
    )
    return ["api", "graphql", "-f", f"query={query}"]


def _has_items(runner: Runner, cfg: Config, ref: ProjectRef) -> bool:
    data = json_call(
        runner, ["project", "item-list", str(ref.number), "--owner", cfg.org, "--limit", "200", "--format", "json"]
    )
    return bool(data.get("items"))


def _linked_repos(runner: Runner, cfg: Config, ref: ProjectRef) -> set[str]:
    query = (
        f'{{organization(login:"{cfg.org}"){{projectV2(number:{ref.number})'
        "{repositories(first:50){nodes{name}}}}}"
    )
    data = json_call(runner, ["api", "graphql", "-f", f"query={query}"])
    nodes = data["data"]["organization"]["projectV2"]["repositories"]["nodes"]
    return {node["name"] for node in nodes}


def _resolve(runner: Runner, cfg: Config) -> ProjectRef:
    ref = find_project(runner, cfg)
    if ref is None:
        raise GhError(["project", "list"], 1, f"project {cfg.project_title!r} does not exist")
    return ref


def _create(runner: Runner, cfg: Config) -> None:
    runner.run(["project", "create", "--owner", cfg.org, "--title", cfg.project_title, "--format", "json"])


def _set_status(runner: Runner, cfg: Config) -> None:
    ref = _resolve(runner, cfg)
    field = next(f for f in project_fields(runner, cfg, ref) if f["name"] == "Status")
    runner.run(status_mutation(field["id"], cfg.status_options))


def _add_field(runner: Runner, cfg: Config, name: str, options: list[str]) -> None:
    ref = _resolve(runner, cfg)
    runner.run(
        ["project", "field-create", str(ref.number), "--owner", cfg.org, "--name", name,
         "--data-type", "SINGLE_SELECT", "--single-select-options", ",".join(options)]
    )


def _link(runner: Runner, cfg: Config, repo: str) -> None:
    ref = _resolve(runner, cfg)
    runner.run(["project", "link", str(ref.number), "--owner", cfg.org, "--repo", f"{cfg.org}/{repo}"])


def plan_project(runner: Runner, cfg: Config) -> list[Change]:
    ref = find_project(runner, cfg)
    fields = project_fields(runner, cfg, ref) if ref else []
    by_name = {f["name"]: f for f in fields}
    changes: list[Change] = []

    if ref is None:
        changes.append(Change(SCOPE, "create-project", f"create org Project '{cfg.project_title}'",
                              apply=lambda: _create(runner, cfg)))

    status = by_name.get("Status")
    have = [o["name"] for o in status.get("options", [])] if status else []
    if ref is not None and have != cfg.status_options and _has_items(runner, cfg, ref):
        changes.append(Change(
            SCOPE, "status-drift",
            f"Status options differ from {', '.join(cfg.status_options)} (now: {', '.join(have)}): "
            "replacing the options would reset every item's Status, so this tool will not; "
            "fix them by hand in the Project UI",
            report_only=True,
        ))
    elif ref is None or have != cfg.status_options:
        changes.append(Change(
            SCOPE, "status-options",
            f"set Status options to {', '.join(cfg.status_options)}",
            detail=[f"currently: {', '.join(have)} (replacing options resets existing items' Status)"]
            if have else [],
            apply=lambda: _set_status(runner, cfg),
        ))

    for name, options in (("Size", cfg.size_options), ("Release", RELEASE_SEED)):
        if name not in by_name:
            changes.append(Change(
                SCOPE, f"field-{name.lower()}",
                f"create single-select field {name} ({', '.join(options)})",
                apply=lambda n=name, o=options: _add_field(runner, cfg, n, o),
            ))

    linked = _linked_repos(runner, cfg, ref) if ref else set()
    for repo in cfg.repos:
        if repo.name not in linked:
            changes.append(Change(SCOPE, "link-repo", f"link repo {repo.name}",
                                  apply=lambda r=repo.name: _link(runner, cfg, r)))
    return changes
