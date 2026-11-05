"""Report whether IgniteRobotics/.github matches templates/github/. Report-only; task 3 ships the PR."""

import base64
from pathlib import Path

from tools.team_github.config import Config
from tools.team_github.gh import GhError, Runner, json_call
from tools.team_github.model import Change

SCOPE = "templates"
DEST_PREFIX = ".github/"  # org-default templates live under .github/ in the org .github repo
REF = "main"


def _no_apply() -> None:
    print("templates are not pushed by this tool: open a PR - see task 3")


def plan_templates(runner: Runner, cfg: Config, source: Path) -> list[Change]:
    if not source.is_dir():
        return [Change(SCOPE, "template-source", f"template source dir not found: {source}",
                       report_only=True, apply=_no_apply)]
    repo = f"{cfg.org}/{cfg.templates_repo}"
    changes: list[Change] = []
    for path in sorted(p for p in source.rglob("*") if p.is_file()):
        dest = DEST_PREFIX + path.relative_to(source).as_posix()
        try:
            remote = json_call(runner, ["api", f"repos/{repo}/contents/{dest}?ref={REF}"])
        except GhError as err:
            if "404" not in err.stderr:
                raise
            changes.append(Change(SCOPE, "template-missing", f"missing in {repo}: {dest}",
                                  report_only=True, apply=_no_apply))
            continue
        if base64.b64decode(remote["content"]) != path.read_bytes():
            changes.append(Change(SCOPE, "template-differs", f"differs in {repo}: {dest}",
                                  report_only=True, apply=_no_apply))
    return changes
