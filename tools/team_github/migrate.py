"""Migrate items from the old Projects into the team Project; optionally close the old ones.

Identity key is the item's content URL, so a re-run adds nothing it already added.
"""

import json

from tools.team_github.config import Config
from tools.team_github.gh import GhError, Runner, json_call
from tools.team_github.model import Change
from tools.team_github.project import ProjectRef, find_project, project_fields

SCOPE = "migrate"
ARCHIVED_REPO = "flashpoint-private"
STATUS_MAP = {
    "Needs triage": "Backlog",
    "Backlog": "Backlog",
    "Todo": "Backlog",
    "In progress": "In progress",
    "Done": "Done",
}


class CloseRefused(GhError):
    def __init__(self, message: str) -> None:
        super().__init__(["project", "close"], 1, message)


def _items(runner: Runner, cfg: Config, number: int) -> list[dict]:
    data = json_call(
        runner, ["project", "item-list", str(number), "--owner", cfg.org, "--limit", "200", "--format", "json"]
    )
    return data.get("items", [])


def _titles(runner: Runner, cfg: Config) -> dict[int, str]:
    data = json_call(runner, ["project", "list", "--owner", cfg.org, "--format", "json", "--limit", "100"])
    return {p["number"]: p["title"] for p in data.get("projects", [])}


class _Source:
    def __init__(self) -> None:
        self.add: dict[int, list[tuple[str, str, str | None]]] = {}  # project -> (url, status, size)
        self.archived: list[str] = []
        self.warnings: list[str] = []

    @property
    def urls(self) -> set[str]:
        return {u for items in self.add.values() for u, _, _ in items}


def _read_sources(runner: Runner, cfg: Config) -> _Source:
    out = _Source()
    seen: set[str] = set()
    for number in cfg.old_projects:
        out.add[number] = []
        for entry in _items(runner, cfg, number):
            content = entry.get("content") or {}
            url = content.get("url")
            title = entry.get("title") or content.get("title") or "(untitled)"
            if content.get("type") == "DraftIssue" or not url:
                out.warnings.append(f"#{number}: draft item '{title}' has no repo; skipped")
                continue
            if (content.get("repository") or "").rsplit("/", 1)[-1] == ARCHIVED_REPO:
                out.archived.append(url)
                continue
            status = entry.get("status")
            if status not in STATUS_MAP:
                out.warnings.append(f"#{number}: {url} has status {status!r}, not in the mapping; skipped")
                continue
            if url in seen:
                continue
            seen.add(url)
            out.add[number].append((url, STATUS_MAP[status], entry.get("size")))
    return out


def _option_id(fields: list[dict], field: str, option: str) -> tuple[str, str]:
    found = next((f for f in fields if f["name"] == field), None)
    if found is None:
        raise GhError(["project", "field-list"], 1, f"target project has no {field} field")
    for opt in found.get("options", []):
        if opt["name"] == option:
            return found["id"], opt["id"]
    raise GhError(["project", "field-list"], 1, f"target {field} field has no option {option!r}")


# One planned item: (url, status, size, target item id or None, edits still needed).
_Planned = tuple[str, str, "str | None", "str | None", tuple[str, ...]]


def _needed(status: str, size: str | None, have: dict | None) -> tuple[str, ...]:
    """Field edits a target item still needs; () means fully migrated."""
    if have is None:
        return ("Status",) + (("Size",) if size else ())
    out = []
    if have.get("status") != status:
        out.append("Status")
    if size and have.get("size") != size:
        out.append("Size")
    return tuple(out)


def _target_index(runner: Runner, cfg: Config, target: ProjectRef | None) -> dict[str, dict]:
    if target is None:
        return {}
    return {
        (e.get("content") or {}).get("url"): e for e in _items(runner, cfg, target.number)
    }


def _apply_items(runner: Runner, cfg: Config, items: list[_Planned]) -> None:
    ref = find_project(runner, cfg)
    if ref is None:
        raise GhError(["project", "list"], 1, f"project {cfg.project_title!r} does not exist")
    fields = project_fields(runner, cfg, ref)
    for url, status, size, item_id, edits in items:
        if item_id is None:
            added = json.loads(runner.run(
                ["project", "item-add", str(ref.number), "--owner", cfg.org, "--url", url, "--format", "json"]
            ))
            item_id = added["id"]
        for field in edits:
            option = status if field == "Status" else size
            field_id, option_id = _option_id(fields, field, option)
            runner.run(["project", "item-edit", "--id", item_id, "--project-id", ref.node_id,
                        "--field-id", field_id, "--single-select-option-id", option_id])


def plan_migration(runner: Runner, cfg: Config, target: ProjectRef | None) -> list[Change]:
    source = _read_sources(runner, cfg)
    index = _target_index(runner, cfg, target)
    titles = _titles(runner, cfg)
    changes: list[Change] = []
    for number, items in source.add.items():
        todo: list[_Planned] = []
        for url, status, size in items:
            have = index.get(url)
            edits = _needed(status, size, have)
            if edits:
                todo.append((url, status, size, have.get("id") if have else None, edits))
        if not todo:
            continue
        where = "" if target else " (project does not exist yet; items resolved at apply)"
        detail = []
        for url, status, size, item_id, edits in todo:
            line = f"{url}  {status}  size={size or '-'}"
            detail.append(line if item_id is None else f"{line}  (already in target; set {', '.join(edits)})")
        changes.append(Change(
            SCOPE, "migrate-items",
            f"add {len(todo)} item(s) from Project #{number} ({titles.get(number, '?')}){where}",
            detail=detail,
            apply=lambda t=todo: _apply_items(runner, cfg, t),
        ))
    if source.archived:
        changes.append(Change(
            SCOPE, "skipped-archived",
            f"skipped {len(source.archived)} item(s) from archived {ARCHIVED_REPO}",
            detail=list(source.archived), report_only=True,
        ))
    if source.warnings:
        changes.append(Change(
            SCOPE, "skipped-warning", f"WARNING: skipped {len(source.warnings)} item(s)",
            detail=list(source.warnings), report_only=True,
        ))
    return changes


def _close(runner: Runner, cfg: Config, number: int) -> None:
    problems = _incomplete(runner, cfg)
    if problems:
        raise CloseRefused(f"refusing to close #{number}: {problems[0]}")
    runner.run(["project", "close", str(number), "--owner", cfg.org])


def _incomplete(runner: Runner, cfg: Config) -> list[str]:
    """Why the migration is not complete (empty list = complete): same criterion as plan_migration."""
    target = find_project(runner, cfg)
    if target is None:
        return [f"project '{cfg.project_title}' does not exist"]
    index = _target_index(runner, cfg, target)
    out = []
    for items in _read_sources(runner, cfg).add.values():
        for url, status, size in items:
            if _needed(status, size, index.get(url)):
                out.append(url)
    return [f"{len(out)} item(s) are not fully migrated"] + out if out else []


def plan_close_old(runner: Runner, cfg: Config, *, migration_planned: bool = False) -> list[Change]:
    """Close (never delete) the old Projects. `migration_planned`: this run also migrates, first."""
    problems = _incomplete(runner, cfg)
    if problems and not migration_planned:
        return [Change(
            SCOPE, "close-refused",
            f"refusing to close old Projects: {problems[0]}; run the migration first",
            detail=problems[1:], report_only=True,
        )]
    titles = _titles(runner, cfg)  # open projects only: a closed one is already done
    return [
        Change(SCOPE, "close-project", f"close Project #{n} ({titles[n]})",
               apply=lambda n=n: _close(runner, cfg, n))
        for n in cfg.old_projects if n in titles
    ]
