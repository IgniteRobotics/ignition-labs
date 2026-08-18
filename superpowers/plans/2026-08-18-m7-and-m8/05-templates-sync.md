# Task 5: `templates_sync.py` — make template drift impossible rather than unlikely

Task 4 deliberately left two copies of every template: one in `templates/` for students to copy, one inline in `student-deliverables.md` so the published page can show what it is explaining. Two copies of the same text drift. This repository's answer to duplication it cannot remove is a check that fails when the duplicate diverges — `tests_lock.py` for given tests, `site_guard.py` for the publishing boundary — and this task is that check for templates.

The risk is the one `CLAUDE.md`'s verification section names: shipping a guard that passes while the thing it protects is broken. Step 4 exists to attack it, and it is not optional.

**Files:**
- Create: `tools/templates_sync.py`
- Create: `tests/test_templates_sync.py`
- Modify: `.github/workflows/docs-links.yml`

**Consumes:** Task 4's six files in `templates/` and the six marked regions in `docs/programming/student-deliverables.md`. The marker name and the filename stem are the same string: `subsystem-readme`, `tuning-log`, `pr-description`, `competition-week-checklist`, `pit-documentation`, `post-season-retro`.
**Produces:** `poetry run python tools/templates_sync.py verify`, exiting `0` when every template matches its region and `1` naming each that does not.

**Lane:** `m8`. Work in `.worktrees/m8`, after Task 4's commit lands.

---

- [ ] **Step 1: Write the failing tests**

`tests/test_templates_sync.py`:

````python
"""Templates in templates/ must match their marked region in student-deliverables.md."""

import pytest

from tools.templates_sync import TEMPLATE_NAMES, extract_regions, verify


DOC = """# Deliverables

<!-- template:subsystem-readme start -->
```markdown
# [Subsystem Name]

**Owner:** [your name]
```
<!-- template:subsystem-readme end -->

Some prose that is not part of any template.

<!-- template:competition-week-checklist start -->
### Before leaving for the event

- [ ] Everything committed and pushed
<!-- template:competition-week-checklist end -->
"""


def _setup(tmp_path, doc=DOC, templates=None):
    (tmp_path / "docs" / "programming").mkdir(parents=True)
    (tmp_path / "docs" / "programming" / "student-deliverables.md").write_text(doc, encoding="utf-8")
    (tmp_path / "templates").mkdir()
    for name, body in (templates or {}).items():
        (tmp_path / "templates" / f"{name}.md").write_text(body, encoding="utf-8")
    return tmp_path


def test_fenced_region_yields_the_fence_contents_not_the_fence(tmp_path):
    root = _setup(tmp_path)
    regions = extract_regions(root)
    assert regions["subsystem-readme"].startswith("# [Subsystem Name]")
    assert "```" not in regions["subsystem-readme"]


def test_unfenced_region_is_taken_as_is(tmp_path):
    root = _setup(tmp_path)
    regions = extract_regions(root)
    assert regions["competition-week-checklist"].startswith("### Before leaving")
    assert "- [ ] Everything committed and pushed" in regions["competition-week-checklist"]


def test_verify_passes_when_every_template_matches(tmp_path):
    regions = extract_regions(_setup(tmp_path))
    root = _setup(tmp_path / "ok", templates={k: v + "\n" for k, v in regions.items()})
    assert verify(root, names=list(regions)) == []


def test_verify_reports_a_drifted_template(tmp_path):
    regions = extract_regions(_setup(tmp_path))
    drifted = {k: v + "\n" for k, v in regions.items()}
    drifted["subsystem-readme"] = drifted["subsystem-readme"].replace("[your name]", "[my name]")
    root = _setup(tmp_path / "drift", templates=drifted)
    problems = verify(root, names=list(regions))
    assert len(problems) == 1
    assert "subsystem-readme" in problems[0]


def test_verify_reports_a_missing_template_file(tmp_path):
    """The region exists but nobody created the file -- a different failure from drift,
    and it must not be reported as drift or the fix suggested will be the wrong one."""
    regions = extract_regions(_setup(tmp_path))
    present = {k: v + "\n" for k, v in regions.items()}
    del present["subsystem-readme"]
    root = _setup(tmp_path / "missing", templates=present)
    problems = verify(root, names=list(regions))
    assert len(problems) == 1
    assert "subsystem-readme" in problems[0]
    assert "does not exist" in problems[0]


def test_a_missing_marker_is_an_error_not_a_silent_skip(tmp_path):
    """The failure that would make this whole guard worthless: a template whose region
    markers were never added passes vacuously if extraction just skips it."""
    root = _setup(tmp_path)
    with pytest.raises(KeyError):
        extract_regions(root)["pit-documentation"]


def test_the_real_repository_is_in_sync():
    """Runs against the actual checkout -- this is the test CI relies on."""
    from tools.templates_sync import REPO_ROOT

    assert verify(REPO_ROOT) == []


def test_every_named_template_has_a_file_and_a_region():
    from tools.templates_sync import REPO_ROOT

    regions = extract_regions(REPO_ROOT)
    assert sorted(regions) == sorted(TEMPLATE_NAMES)
    for name in TEMPLATE_NAMES:
        assert (REPO_ROOT / "templates" / f"{name}.md").is_file()
````

- [ ] **Step 2: Run them and watch them fail**

```bash
cd /Users/y0shi/workspace/ignite/docs/.worktrees/m8
poetry run pytest tests/test_templates_sync.py -v
```

Expected: every test errors on `ModuleNotFoundError: No module named 'tools.templates_sync'`. That is the right failure — the module does not exist yet.

- [ ] **Step 3: Write the tool**

`tools/templates_sync.py`:

```python
"""Templates in templates/ must match their source region in student-deliverables.md.

Two copies of every template exist on purpose (see the M7/M8 design record §2.2):
templates/ holds the real files a student copies, and student-deliverables.md holds
the same content inline so the published page can show what it is explaining --
templates/ is outside docs_dir and MkDocs cannot reach it.

Two copies drift. This makes that visible on the next CI run rather than on the day a
student notices the template they were handed disagrees with the page describing it.

Regions are delimited by HTML comment markers rather than line numbers or fences: line
numbers break on the next edit above them, and one of the six regions is not fenced at
all (the competition-week checklist renders as real checkboxes and must stay unfenced).
"""

from __future__ import annotations

import re
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[1]
SOURCE_DOC = Path("docs") / "programming" / "student-deliverables.md"
TEMPLATES_DIR = Path("templates")

TEMPLATE_NAMES = [
    "subsystem-readme",
    "tuning-log",
    "pr-description",
    "competition-week-checklist",
    "pit-documentation",
    "post-season-retro",
]

DRIFT_MESSAGE = (
    "This template and the copy in docs/programming/student-deliverables.md have "
    "diverged. Edit the document, then re-extract -- both copies move together or "
    "students get handed a template the curriculum page contradicts."
)


def extract_regions(root: Path) -> dict[str, str]:
    """Map each marker name to its region body, with any wrapping fence removed."""
    doc = (root / SOURCE_DOC).read_text(encoding="utf-8")
    regions: dict[str, str] = {}
    for match in re.finditer(
        r"<!-- template:([a-z0-9-]+) start -->\n(.*?)\n<!-- template:\1 end -->", doc, re.S
    ):
        name, body = match.group(1), match.group(2)
        fenced = re.match(r"^```[a-z]*\n(.*)\n```$", body, re.S)
        if fenced:
            body = fenced.group(1)
        regions[name] = body.rstrip("\n")
    return regions


def verify(root: Path, names: list[str] | None = None) -> list[str]:
    """Return a problem description per template that is missing or has drifted."""
    names = TEMPLATE_NAMES if names is None else names
    regions = extract_regions(root)
    problems: list[str] = []
    for name in names:
        path = root / TEMPLATES_DIR / f"{name}.md"
        if name not in regions:
            problems.append(f"{name}: no marked region in {SOURCE_DOC}")
            continue
        if not path.is_file():
            problems.append(f"{name}: {path.relative_to(root)} does not exist")
            continue
        if path.read_text(encoding="utf-8").rstrip("\n") != regions[name]:
            problems.append(f"{name}: {path.relative_to(root)} differs from its marked region")
    return problems


def main(argv: list[str]) -> int:
    if len(argv) != 2 or argv[1] != "verify":
        print("usage: templates_sync.py verify", file=sys.stderr)
        return 2
    problems = verify(REPO_ROOT)
    if problems:
        for problem in problems:
            print(f"TEMPLATE OUT OF SYNC: {problem}")
        print()
        print(DRIFT_MESSAGE)
        return 1
    print("templates in sync")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
```

- [ ] **Step 4: Run the tests, then prove the guard actually fails on real drift**

```bash
poetry run pytest tests/test_templates_sync.py -v
```

Expected: all pass.

Unit tests use fixtures. **A guard nobody has watched fail against the real tree is a guard nobody knows works**, so drift the real thing:

```bash
cd /Users/y0shi/workspace/ignite/docs/.worktrees/m8
poetry run python tools/templates_sync.py verify; echo "clean rc=$? (expect 0)"

printf '\nDRIFT\n' >> templates/tuning-log.md
poetry run python tools/templates_sync.py verify; echo "drifted rc=$? (expect 1)"

git checkout -- templates/tuning-log.md
poetry run python tools/templates_sync.py verify; echo "restored rc=$? (expect 0)"
```

Now the other direction — drift the *document* rather than the template, because that is the direction a real edit takes:

```bash
python3 - <<'PY'
import pathlib
p = pathlib.Path('docs/programming/student-deliverables.md')
s = p.read_text()
p.write_text(s.replace('**Owner:** [your name]', '**Owner:** [drifted]', 1))
PY
poetry run python tools/templates_sync.py verify; echo "doc-drift rc=$? (expect 1)"
git checkout -- docs/programming/student-deliverables.md
poetry run python tools/templates_sync.py verify; echo "restored rc=$? (expect 0)"
```

Paste all six outcomes. **Read the failure message itself** — a mentor or student hitting this needs to know which direction to fix, and the message says "edit the document, then re-extract" for that reason.

- [ ] **Step 5: Wire it into CI**

`.github/workflows/docs-links.yml` already runs on documentation changes and has Poetry available. Add a step after the existing link check:

```yaml
      - name: Templates match their source regions
        run: poetry run python tools/templates_sync.py verify
```

Confirm the workflow's triggers actually cover both sides of the check — a guard that never runs when `templates/` changes is not a guard:

```bash
sed -n '1,20p' .github/workflows/docs-links.yml
```

If the `paths:` filter does not include `templates/**` and `docs/programming/student-deliverables.md`, add them. If there is no `paths:` filter, it runs on everything and nothing needs adding.

- [ ] **Step 6: Full suite, then commit**

```bash
poetry run pytest -q; echo "pytest rc=$? (expect 0)"
poetry run mkdocs build --strict > /dev/null 2>&1; echo "mkdocs rc=$? (expect 0)"
poetry run python tools/site_guard.py _site; echo "site_guard rc=$? (expect 0)"
```

```bash
git add tools/templates_sync.py tests/test_templates_sync.py .github/workflows/docs-links.yml
git commit -m "Add templates_sync: fail when a template and its source region diverge

M8 ships every template twice on purpose -- templates/ for students to copy,
student-deliverables.md inline so the published page can show what it explains,
since templates/ is outside docs_dir and MkDocs cannot reach it. Two copies
drift, so this makes the divergence fail CI instead of surfacing the day a
student is handed a template the curriculum page contradicts.

Regions are matched by HTML comment markers, not line numbers or fences: line
numbers break on the next edit above them, and the competition-week checklist is
deliberately unfenced so its checkboxes render.

Proven to fail in both directions against the real tree, not only against
fixtures -- drifting the template and drifting the document each exit 1, and
restoring each exits 0."
```
