"""CLI: python -m tools.team_github [--apply] [--close-old] [--only repos|project|migrate|templates]

Exit codes. Dry-run: 0 = nothing to do, 1 = changes would be made.
--apply: 0 = applied, 2 = a gh call failed. Dry-run also exits 2 if reading GitHub fails.
"""

import argparse
import sys
from collections.abc import Callable
from pathlib import Path

from tools.team_github import config as config_mod
from tools.team_github.config import Config
from tools.team_github.gh import GhError, GhRunner, Runner
from tools.team_github.model import Change
from tools.team_github.migrate import plan_close_old, plan_migration
from tools.team_github.org_templates import plan_templates
from tools.team_github.project import find_project, plan_project
from tools.team_github.repos import plan_repo

DEFAULT_CONFIG = Path(__file__).resolve().parent.parent / "team_github.toml"
DEFAULT_TEMPLATES = Path(__file__).resolve().parent.parent.parent / "templates" / "github"
ONLY_CHOICES = ["repos", "project", "migrate", "templates"]

Planner = Callable[[Runner, Config, argparse.Namespace], list[Change]]


def _plan_repos(runner: Runner, cfg: Config, args: argparse.Namespace) -> list[Change]:
    wanted = getattr(args, "repos", None)
    out: list[Change] = []
    for repo in cfg.repos:
        if wanted is None or repo.name in wanted:
            out.extend(plan_repo(runner, cfg, repo))
    return out


def _plan_project(runner: Runner, cfg: Config, args: argparse.Namespace) -> list[Change]:
    return plan_project(runner, cfg)


def _plan_migrate(runner: Runner, cfg: Config, args: argparse.Namespace) -> list[Change]:
    changes = plan_migration(runner, cfg, find_project(runner, cfg))
    if args.close_old:
        migrating = any(c.actionable for c in changes)
        changes += plan_close_old(runner, cfg, migration_planned=migrating)
    return changes


def _plan_templates(runner: Runner, cfg: Config, args: argparse.Namespace) -> list[Change]:
    return plan_templates(runner, cfg, args.templates_dir)


# Apply order follows this dict: repos, then project -> links -> migration (+ close-old), then templates.
TARGETS: dict[str, Planner] = {
    "repos": _plan_repos,
    "project": _plan_project,
    "migrate": _plan_migrate,
    "templates": _plan_templates,
}


def render(changes: list[Change], scopes: list[str]) -> str:
    lines: list[str] = []
    for scope in scopes:
        mine = [c for c in changes if c.scope == scope]
        lines.append(f"== {scope}")
        if not mine:
            lines.append("  (converged)")
        groups = (
            ("", [c for c in mine if c.actionable]),
            ("  PENDING (not applied yet)", [c for c in mine if c.pending]),
            ("  REPORT ONLY (this tool never changes these)", [c for c in mine if c.report_only]),
        )
        for heading, items in groups:
            if not items:
                continue
            if heading:
                lines.append(heading)
            indent = "    " if heading else "  "
            for change in items:
                lines.append(f"{indent}{change.summary}")
                lines.extend(f"{indent}  {d}" for d in change.detail)
        lines.append("")
    return "\n".join(lines)


def execute(
    runner: Runner, cfg: Config, planners: list[Planner], args: argparse.Namespace, apply: bool
) -> int:
    try:
        changes: list[Change] = []
        for planner in planners:
            changes.extend(planner(runner, cfg, args))
    except GhError as err:
        print(f"error reading GitHub: {err}", file=sys.stderr)
        return 2
    scopes = [r.name for r in cfg.repos if getattr(args, "repos", None) in (None, [])
              or r.name in args.repos] if _plan_repos in planners else []
    scopes += [s for s in dict.fromkeys(c.scope for c in changes) if s not in scopes]
    print(render(changes, scopes))
    todo = [c for c in changes if c.actionable]
    if not todo:
        print("Nothing to do." + (" (Pending items need their workflow first.)" if any(c.pending for c in changes) else ""))
        return 0
    if not apply:
        print(f"{len(todo)} change(s) would be made. Re-run with --apply to make them.")
        return 1
    for change in todo:
        try:
            change.apply()
        except GhError as err:
            print(f"FAILED {change.scope}: {change.summary}\n  {err}", file=sys.stderr)
            return 2
        print(f"applied {change.scope}: {change.summary}")
    return 0


def run_repos(runner: Runner, cfg: Config, *, apply: bool, repos: list[str] | None = None) -> int:
    args = argparse.Namespace(repos=repos)
    return execute(runner, cfg, [_plan_repos], args, apply)


def main(
    argv: list[str] | None = None,
    *,
    runner: Runner | None = None,
    config_path: Path | None = None,
) -> int:
    parser = argparse.ArgumentParser(prog="python -m tools.team_github")
    parser.add_argument("--apply", action="store_true", help="make the changes (default: dry-run)")
    parser.add_argument("--close-old", action="store_true", help="close the old Projects (with migrate)")
    parser.add_argument("--only", choices=ONLY_CHOICES, help="run one target only")
    parser.add_argument("--config", type=Path, default=None)
    parser.add_argument("--templates-dir", type=Path, default=DEFAULT_TEMPLATES,
                        help="source of the org templates (default: templates/github/)")
    args = parser.parse_args(argv)
    args.repos = None

    names = [args.only] if args.only else list(TARGETS)
    unknown = [n for n in names if n not in TARGETS]
    if unknown:
        parser.error(f"target not available yet: {', '.join(unknown)}")

    cfg = config_mod.load(args.config or config_path or DEFAULT_CONFIG)
    return execute(runner or GhRunner(), cfg, [TARGETS[n] for n in names], args, args.apply)


if __name__ == "__main__":
    sys.exit(main())
