import tomllib
from dataclasses import dataclass
from pathlib import Path


@dataclass(frozen=True)
class RepoConfig:
    name: str
    default_branch: str
    required_checks: list[str]


@dataclass(frozen=True)
class Config:
    org: str
    project_title: str
    status_options: list[str]
    size_options: list[str]
    labels: dict[str, str]  # name -> hex colour (no '#')
    mentor_team: str
    bypass_team_id: int
    repos: list[RepoConfig]
    old_projects: list[int]
    templates_repo: str


def load(path: Path) -> Config:
    with open(path, "rb") as handle:
        raw = tomllib.load(handle)
    try:
        return Config(
            org=raw["org"],
            project_title=raw["project_title"],
            status_options=list(raw["status_options"]),
            size_options=list(raw["size_options"]),
            labels=dict(raw["labels"]),
            mentor_team=raw["mentor_team"],
            bypass_team_id=int(raw["bypass_team_id"]),
            repos=[
                RepoConfig(
                    name=r["name"],
                    default_branch=r["default_branch"],
                    required_checks=list(r["required_checks"]),
                )
                for r in raw["repos"]
            ],
            old_projects=[int(n) for n in raw["old_projects"]],
            templates_repo=raw["templates_repo"],
        )
    except KeyError as missing:
        raise ValueError(f"{path}: missing config key {missing}") from missing
