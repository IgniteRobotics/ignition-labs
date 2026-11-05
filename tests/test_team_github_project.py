from tests.team_github_helpers import (
    TARGET_NUMBER,
    load_config,
    projects_runner,
    target_fields,
)
from tools.team_github.model import Change
from tools.team_github.project import ProjectRef, find_project, plan_project, status_mutation


def test_find_project_by_exact_title():
    cfg = load_config()
    assert find_project(projects_runner(), cfg) is None
    ref = find_project(projects_runner(target=target_fields()), cfg)
    assert ref == ProjectRef(TARGET_NUMBER, "PVT_target")


def test_missing_project_plans_create_fields_and_four_links():
    changes = plan_project(projects_runner(), load_config())
    assert [c.kind for c in changes] == [
        "create-project", "status-options", "field-size", "field-release",
        "link-repo", "link-repo", "link-repo", "link-repo",
    ]
    assert all(c.actionable for c in changes)
    linked = {c.summary.split()[-1] for c in changes if c.kind == "link-repo"}
    assert linked == {"Robot-2026", "IgniteLib", "flashpoint", "Phoenix-2026"}


def test_status_options_exact_order():
    runner = projects_runner(target=target_fields(status=["Todo", "In Progress", "Done"]))
    expected = tuple(status_mutation("F_status", load_config().status_options))
    runner.responses[expected] = "{}"
    status = next(c for c in plan_project(runner, load_config()) if c.kind == "status-options")
    status.apply()
    assert runner.calls[-1] == expected
    query = expected[-1]
    names = ["Backlog", "Ready", "In progress", "In review", "Done"]
    positions = [query.index(f'name:"{n}"') for n in names]
    assert positions == sorted(positions)
    assert "updateProjectV2Field" in query and "F_status" in query


def test_existing_project_with_right_fields_plans_nothing():
    runner = projects_runner(target=target_fields())
    assert plan_project(runner, load_config()) == []


def test_existing_project_missing_size_release_and_one_link():
    runner = projects_runner(
        target=target_fields(size=None, release=False), linked=["Robot-2026", "IgniteLib", "flashpoint"]
    )
    changes = plan_project(runner, load_config())
    assert [c.kind for c in changes] == ["field-size", "field-release", "link-repo"]
    assert changes[-1].summary.endswith("Phoenix-2026")


def test_plan_project_changes_are_changes():
    assert all(isinstance(c, Change) for c in plan_project(projects_runner(), load_config()))


def test_status_options_right_names_wrong_order_is_drift():
    order = ["Ready", "Backlog", "In progress", "In review", "Done"]
    changes = plan_project(projects_runner(target=target_fields(status=order)), load_config())
    assert [c.kind for c in changes] == ["status-options"]
