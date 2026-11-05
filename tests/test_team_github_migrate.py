import json

import pytest

from tests.team_github_helpers import (
    ORG,
    TARGET_ID,
    TARGET_NUMBER,
    items_call,
    list_call,
    load_config,
    project_fixture,
    projects_runner,
    target_fields,
)
from tools.team_github import __main__ as cli
from tools.team_github.gh import FakeRunner, GhError
from tools.team_github.migrate import STATUS_MAP, plan_close_old, plan_migration
from tools.team_github.org_templates import plan_templates
from tools.team_github.project import ProjectRef

TARGET = ProjectRef(TARGET_NUMBER, TARGET_ID)


def adds(changes):
    return [c for c in changes if c.kind == "migrate-items"]


def skips(changes):
    return [c for c in changes if c.kind == "skipped-archived"]


def warns(changes):
    return [c for c in changes if c.kind == "skipped-warning"]


def item(url, status, size=None, kind="Issue", repo="IgniteRobotics/IgniteLib"):
    out = {"id": "PVTI_" + url[-4:], "status": status, "title": "t",
           "content": {"type": kind, "url": url, "repository": repo, "number": 1, "title": "t"}}
    if size:
        out["size"] = size
    return out


def only_sources(monkey_items: dict[int, list]) -> FakeRunner:
    runner = projects_runner(target=target_fields())
    for n, items in monkey_items.items():
        runner.responses[items_call(n)] = json.dumps({"items": items, "totalCount": len(items)})
    return runner


def test_status_map_is_the_spec_table():
    assert STATUS_MAP == {"Needs triage": "Backlog", "Backlog": "Backlog", "Todo": "Backlog",
                          "In progress": "In progress", "Done": "Done"}


def test_real_fixtures_migrate_39_and_skip_8():
    changes = plan_migration(projects_runner(), load_config(), None)
    moved = sum(int(c.summary.split()[1]) for c in adds(changes))
    assert moved == 39
    assert len(skips(changes)[0].detail) == 8


def test_migration_maps_statuses_and_carries_size():
    runner = only_sources({
        3: [item("https://github.com/IgniteRobotics/flashpoint/issues/1", "Needs triage", "M"),
            item("https://github.com/IgniteRobotics/flashpoint/issues/2", "In progress")],
        5: [item("https://github.com/IgniteRobotics/IgniteLib/issues/1", "Todo")],
    })
    changes = adds(plan_migration(runner, load_config(), TARGET))
    text = "\n".join(line for c in changes for line in c.detail)
    assert "flashpoint/issues/1  Backlog  size=M" in text
    assert "flashpoint/issues/2  In progress  size=-" in text
    assert "IgniteLib/issues/1  Backlog  size=-" in text
    assert "Release" not in text


def test_migration_skips_flashpoint_private_items():
    changes = plan_migration(projects_runner(target=target_fields()), load_config(), TARGET)
    [skipped] = skips(changes)
    assert len(skipped.detail) == 8
    assert all("flashpoint-private" in d for d in skipped.detail)
    assert skipped.report_only


def test_migration_skips_drafts_and_unknown_status_with_warning():
    draft = {"id": "PVTI_d", "status": "Todo", "title": "A draft", "content": {"type": "DraftIssue", "title": "A draft"}}
    runner = only_sources({
        3: [draft, item("https://github.com/IgniteRobotics/flashpoint/issues/9", "Blocked")],
        5: [],
    })
    changes = plan_migration(runner, load_config(), TARGET)
    assert adds(changes) == []
    [warning] = warns(changes)
    joined = "\n".join(warning.detail)
    assert "A draft" in joined and "draft" in joined
    assert "issues/9" in joined and "Blocked" in joined


def test_migration_rerun_adds_nothing():
    src3 = [item("https://github.com/IgniteRobotics/flashpoint/issues/1", "Todo", "M")]
    src5 = [item("https://github.com/IgniteRobotics/IgniteLib/issues/1", "Todo")]
    runner = only_sources({3: src3, 5: src5})
    runner.responses[items_call(TARGET_NUMBER)] = json.dumps(
        {"items": [item(src3[0]["content"]["url"], "Backlog", "M"),
                   item(src5[0]["content"]["url"], "Backlog")], "totalCount": 2}
    )
    assert plan_migration(runner, load_config(), TARGET) == []


def test_pr_items_migrate_like_issues():
    url = "https://github.com/IgniteRobotics/IgniteLib/pull/7"
    runner = only_sources({3: [], 5: [item(url, "Done", kind="PullRequest")]})
    [change] = adds(plan_migration(runner, load_config(), TARGET))
    assert url in change.detail[0] and "Done" in change.detail[0]


def test_same_url_in_both_projects_migrates_once():
    url = "https://github.com/IgniteRobotics/flashpoint/issues/1"
    runner = only_sources({3: [item(url, "Todo")], 5: [item(url, "Todo")]})
    changes = adds(plan_migration(runner, load_config(), TARGET))
    assert sum(len(c.detail) for c in changes) == 1


def test_migration_apply_adds_then_sets_fields():
    url = "https://github.com/IgniteRobotics/flashpoint/issues/1"
    runner = only_sources({3: [item(url, "Todo", "M")], 5: []})
    add = ("project", "item-add", str(TARGET_NUMBER), "--owner", ORG, "--url", url, "--format", "json")
    edit = lambda field, opt: ("project", "item-edit", "--id", "PVTI_new", "--project-id", TARGET_ID,
                               "--field-id", field, "--single-select-option-id", opt)
    runner.responses[add] = json.dumps({"id": "PVTI_new"})
    runner.responses[edit("F_status", "s0")] = "{}"
    runner.responses[edit("F_size", "z2")] = "{}"
    [change] = adds(plan_migration(runner, load_config(), TARGET))
    change.apply()
    assert runner.calls[-3:] == [add, edit("F_status", "s0"), edit("F_size", "z2")]


def test_close_old_plans_close_never_delete_and_leaves_2_and_4():
    runner = projects_runner(target=target_fields())
    # target already holds every migratable url
    urls = [i["content"]["url"] for n in (3, 5) for i in project_fixture(f"items-{n}.json")["items"]
            if "flashpoint-private" not in i["content"]["repository"]]
    runner.responses[items_call(TARGET_NUMBER)] = json.dumps(
        {"items": [item(u, "Backlog") for u in urls], "totalCount": len(urls)})
    changes = plan_close_old(runner, load_config())
    assert [c.summary for c in changes] == ["close Project #3 (Flashpoint)", "close Project #5 (IgniteLib)"]
    close = {("project", "close", "3", "--owner", ORG): "{}", ("project", "close", "5", "--owner", ORG): "{}"}
    runner.responses.update(close)
    for c in changes:
        c.apply()
    mutating = [call for call in runner.calls if call[:2] == ("project", "close")]
    assert mutating == list(close)
    assert not any("delete" in " ".join(call) for call in runner.calls)
    assert not any(call[2:3] in (("2",), ("4",)) for call in runner.calls if call[:2] == ("project", "close"))


def test_close_old_refuses_when_target_lacks_migratable_urls():
    runner = projects_runner(target=target_fields())
    changes = plan_close_old(runner, load_config())
    assert changes and all(c.report_only for c in changes)
    assert "refus" in changes[0].summary.lower()


def test_close_old_expected_after_migration_in_same_run():
    changes = plan_close_old(projects_runner(), load_config(), migration_planned=True)
    assert [c.kind for c in changes] == ["close-project", "close-project"]


def test_close_old_skips_already_closed_projects():
    runner = projects_runner(target=target_fields())
    listing = project_fixture("list.json")
    listing["projects"] = [p for p in listing["projects"] if p["number"] != 3]
    runner.responses[list_call()] = json.dumps({"projects": listing["projects"]})
    changes = plan_close_old(runner, load_config(), migration_planned=True)
    assert [c.summary for c in changes] == ["close Project #5 (IgniteLib)"]


def run_cli(argv, runner):
    return cli.main(argv, runner=runner)


def test_cli_without_close_old_never_plans_closing(capsys):
    runner = projects_runner(target=target_fields())
    code = run_cli(["--only", "migrate"], runner)
    out = capsys.readouterr().out
    assert code == 1
    assert "close Project" not in out
    assert not any(c[:2] == ("project", "close") for c in runner.calls)


def test_cli_close_old_flag_plans_closing(capsys):
    runner = projects_runner()
    code = run_cli(["--only", "migrate", "--close-old"], runner)
    out = capsys.readouterr().out
    assert code == 1
    assert "close Project #3" in out and "close Project #5" in out
    assert not any(c[:2] == ("project", "close") for c in runner.calls)  # dry-run


def test_cli_project_dry_run_converged_exits_zero(capsys):
    assert run_cli(["--only", "project"], projects_runner(target=target_fields())) == 0


def test_cli_read_failure_exits_2(capsys):
    runner = FakeRunner({list_call(): GhError(["project", "list"], 1, "boom")})
    assert run_cli(["--only", "project"], runner) == 2


# --- org template drift --------------------------------------------------------------


def _contents_call(path):
    return ("api", f"repos/{ORG}/.github/contents/.github/{path}?ref=main")


def _src(tmp_path):
    (tmp_path / "ISSUE_TEMPLATE").mkdir()
    (tmp_path / "ISSUE_TEMPLATE" / "bug.yml").write_text("same\n")
    (tmp_path / "PULL_REQUEST_TEMPLATE.md").write_text("new text\n")
    (tmp_path / "ISSUE_TEMPLATE" / "feature.yml").write_text("brand new\n")
    return tmp_path


def _remote(text):
    import base64
    return json.dumps({"content": base64.encodebytes(text.encode()).decode()})


def test_templates_drift_reported_not_applied(tmp_path, capsys):
    src = _src(tmp_path)
    runner = FakeRunner({
        _contents_call("ISSUE_TEMPLATE/bug.yml"): _remote("same\n"),
        _contents_call("PULL_REQUEST_TEMPLATE.md"): _remote("old text\n"),
        _contents_call("ISSUE_TEMPLATE/feature.yml"): GhError(["api"], 1, "gh: Not Found (HTTP 404)"),
    })
    changes = plan_templates(runner, load_config(), src)
    kinds = {c.summary: c.kind for c in changes}
    assert kinds == {
        "differs in IgniteRobotics/.github: .github/PULL_REQUEST_TEMPLATE.md": "template-differs",
        "missing in IgniteRobotics/.github: .github/ISSUE_TEMPLATE/feature.yml": "template-missing",
    }
    assert all(c.report_only and not c.actionable for c in changes)
    before = len(runner.calls)
    for c in changes:
        c.apply()
    assert len(runner.calls) == before
    assert "task 3" in capsys.readouterr().out


def test_templates_non_404_error_propagates(tmp_path):
    src = _src(tmp_path)
    runner = FakeRunner({_contents_call("ISSUE_TEMPLATE/bug.yml"): GhError(["api"], 1, "HTTP 500")})
    with pytest.raises(GhError):
        plan_templates(runner, load_config(), src)


def test_templates_missing_source_dir_is_reported(tmp_path):
    [change] = plan_templates(FakeRunner({}), load_config(), tmp_path / "nope")
    assert change.report_only and "not found" in change.summary
