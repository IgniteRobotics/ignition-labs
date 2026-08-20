# Task 1: The team gitflow page, its diagram, and site registration

This task creates the curriculum's copy of the team's git guidelines, vendors the branch
diagram in a form that survives dark mode, and registers both with the two hand-maintained
lists that decide what the site guard protects.

The risky part is not the prose. It is that **a new page can render, publish, and be
completely unguarded** if it reaches `mkdocs.yml` but not `REQUIRED_PAGES` — and that a
missing image fails *silently*, as a broken-image icon on a green build. Both get real
tests here, not a manual check.

**Files:**
- Create: `docs/programming/team-gitflow.md`
- Create: `docs/images/gitflow.png` (new directory)
- Modify: `docs/programming/git-workflow.md` — the `M7 and M8 — commit access` section
- Modify: `mkdocs.yml` — `nav:`, under `Programming Curriculum`
- Modify: `tools/site_guard.py` — `REQUIRED_PAGES`, plus new `REQUIRED_ASSETS` / `missing_assets`
- Test: `tests/test_site_guard.py`
- Read-only source: `superpowers/plans/2026-08-20-m0-vscode-and-git/gitflow-raw.png`

**Consumes:** nothing from earlier tasks.

**Produces:**
- The page slug `programming/team-gitflow`, linked from task 3's M0 document.
- `missing_assets(site_root: Path, required: list[str]) -> list[str]` in `tools.site_guard`,
  returning sorted site-relative paths that did not publish.
- `REQUIRED_ASSETS: list[str]` at module level in `tools.site_guard`.

**Lane:** `gitflow`

---

- [ ] **Step 1: Generate the dark-mode-safe diagram**

The vendored source is 960×343 RGBA with a fully transparent background and black strokes.
On Material's dark scheme every label renders black-on-black and disappears. Composite it
onto opaque white with 16px padding.

```bash
mkdir -p docs/images
poetry run python - <<'PY'
from PIL import Image

src = Image.open(
    "superpowers/plans/2026-08-20-m0-vscode-and-git/gitflow-raw.png"
).convert("RGBA")
out = Image.new("RGB", (src.width + 32, src.height + 32), "white")
out.paste(src, (16, 16), src)
out.save("docs/images/gitflow.png")
print(out.mode, out.size)
PY
```

Expected output: `RGB (992, 375)`

Confirm the alpha channel is gone — if the result is still RGBA, the paste mask did not
apply and dark mode is still broken:

```bash
poetry run python -c "from PIL import Image; im=Image.open('docs/images/gitflow.png'); print(im.mode, im.size, im.getpixel((0,0)))"
```

Expected: `RGB (992, 375) (255, 255, 255)`

- [ ] **Step 2: Create the team gitflow page**

Create `docs/programming/team-gitflow.md` with exactly this content:

````markdown
# Team Gitflow

How Ignite works on the competition robot's code — and when that becomes how you work.

---

> **⚠️ This is not what you do yet.** This page describes `Robot-2026`, the competition
> robot's code, written by people who already have commit access to it. You get that access
> at **M7**, when you clear the Kickoff Readiness Gate. Until then you fork `ignition-labs`
> and pull-request into your own fork — see [the git workflow](git-workflow.md).
>
> Nothing here contradicts that. It describes the far side of the same boundary. Read it now
> so that when you clone `Robot-2026` at M2 and find a `development` branch that the git
> workflow never mentioned, you know what you are looking at.

---

## The shape

We use a slightly simplified gitflow. `main` is always what goes to competition, and is only
ever updated from `development` or from a `hotfix` branch. All normal coding happens in
`feature` branches.

![The Ignite gitflow: feature branches merge into development, development merges into main, and a hotfix branch comes off main at competition](../images/gitflow.png)

Three things in that picture are worth stopping on.

**`feature/bad_idea` never merges.** That is not a mistake in the diagram. Somebody tried
something, it did not work, and the branch was abandoned. Branches that go nowhere cost
nothing and get thrown away constantly. This is the entire reason we work on branches:
nothing you do on one can hurt anybody.

**The tags are competitions.** `release/gwinnett` and `release/albany` mark exactly what code
went to a specific event. When someone asks what we were running at Gwinnett, the tag is the
answer, and it is the only reliable one.

**The drawing simplifies the hotfix.** It shows `hotfix/gwinnett` merging back into `main`
only. In practice it merges into `development` as well — otherwise the fix somebody made at
competition is missing from every feature branched after it, and someone re-breaks it in
March.

---

## The branches

| Branch | What it holds | Who writes to it |
|---|---|---|
| `main` | Competition-ready code, and nothing else | Nobody directly. Only merges, from `development` or a `hotfix` |
| `development` | Features merged together so they can be tested as a set | Nobody directly. Only merges, from `feature/*` |
| `feature/<name>` | One feature or fix. One, 95% of the time | You |
| `hotfix/<comp>` | Changes made at a competition, branched from `main` | Whoever is at the event |

We do not use `release` branches. Code goes from `development` straight to `main`.

---

## The standard flow

1. Fetch from all remotes, so you are working from what everyone else has already done.
2. Check out `development` if you are not already on it.
3. Pull, to pick up any updates.
4. Create your feature branch, `feature/<name>`, and switch to it. Branch directly in the
   Ignite repository — no fork.
5. Write the code.
6. Test it. You can deploy to the robot directly from your feature branch.
7. Open a pull request from your feature branch back into `development`.
8. If the automated build fails, fix it and push again. The pull request updates itself.
9. Review with the other developers and a mentor. Walk them through what you did.
10. A lead merges it. Delete the feature branch on GitHub afterward to keep the list short.

---

## The rules

1. **`main` is competition-ready code only.** If it is on `main`, we are willing to drive it.
2. **`development` is where features meet.** You should always have high confidence that
   what is on `development` is ready to go to `main`.
3. **A feature branch holds one feature.** Ninety-five per cent of the time. The exceptions
   are real, and they are exceptions.
4. **Everything merges through a pull request** — features into `development`, and
   `development` into `main`. Branch protection rules on GitHub enforce this.
   - The build, the formatting check, and an automated code review all run when a pull
     request is opened.
   - Every developer and mentor should review every pull request.
   - Fixes during review are just more commits on the same branch; the pull request picks
     them up automatically.
5. **We tag a release before each competition**, named `release/<comp>`, and immediately cut
   a `hotfix/<comp>` branch from `main` for changes at the event.
6. **After the competition the hotfix merges back into both `main` and `development`.**
7. **No `release` branches.** `development` goes straight to `main`.

---

## When this becomes yours

At **M7**. Clearing the Kickoff Readiness Gate is what gets you commit access to
`Robot-2026`, and from that point this page stops being background reading and starts
describing your week: your branch is `feature/<name>`, your pull request targets
`development`, and you never push to `main`.

That is the same boundary as the git ritual. Before the Gate you fork and send pull
requests; after it you branch, like everyone else on the team. The two changed together on
purpose — when you stop forking, it is because the team has decided you can be trusted with
the competition robot's code.

---

## Source

This page is the curriculum's copy of the team's
[Ignite Git Usage Guidelines](https://igniterobotics.notion.site/Ignite-Git-Usage-Guidlines-2ece1d60205380c39926f80cb312612a).
Where the two disagree, the Notion page is what the team does and this page is out of date —
tell a mentor.
````

- [ ] **Step 3: Register the page in `mkdocs.yml`**

Find this block under `nav:` and add the one new line, so the student flow and the team flow
sit next to each other:

```yaml
  - Programming Curriculum:
      - Overview: programming/README.md
      - Git Workflow: programming/git-workflow.md
      - Team Gitflow: programming/team-gitflow.md
      - Real-Repo Ladder: programming/robot-repo-ladder.md
```

- [ ] **Step 4: Write the failing test for page registration**

Append to `tests/test_site_guard.py`:

```python
def test_team_gitflow_is_a_required_page():
    # A page in mkdocs.yml's nav: but not in REQUIRED_PAGES renders today and
    # is guarded by nothing -- the exact C11b failure the hardcoded list
    # exists to catch. team-gitflow.md is M0 reading and M7 procedure; it
    # must actually publish.
    assert "programming/team-gitflow" in REQUIRED_PAGES
```

- [ ] **Step 5: Run it and watch it fail**

```bash
poetry run pytest tests/test_site_guard.py::test_team_gitflow_is_a_required_page -v
```

Expected: **FAIL**, `AssertionError: assert 'programming/team-gitflow' in [...]`

- [ ] **Step 6: Add the slug to `REQUIRED_PAGES`**

In `tools/site_guard.py`, immediately after `"programming/git-workflow",`:

```python
    "programming/git-workflow",
    "programming/team-gitflow",
    "programming/robot-repo-ladder",
```

Run it again:

```bash
poetry run pytest tests/test_site_guard.py::test_team_gitflow_is_a_required_page -v
```

Expected: **PASS**

- [ ] **Step 7: Write the failing tests for asset registration**

A missing image is not a missing page — `missing_pages` looks for `index.html` and will
never notice it. On the published site it renders as a broken-image icon on an otherwise
green build, which is precisely the "check passes while the thing it protects is broken"
pattern this repository keeps producing.

Append to `tests/test_site_guard.py`, and change the import line at the top of the file to
match:

```python
from tools.site_guard import (
    REQUIRED_ASSETS,
    REQUIRED_PAGES,
    forbidden_files,
    missing_assets,
    missing_pages,
)
```

```python
def test_missing_required_asset_is_reported(tmp_path):
    site = _make_site(tmp_path, ["index.html"])
    assert missing_assets(site, ["images/gitflow.png"]) == ["images/gitflow.png"]


def test_present_required_asset_is_not_reported(tmp_path):
    site = _make_site(tmp_path, ["index.html", "images/gitflow.png"])
    assert missing_assets(site, ["images/gitflow.png"]) == []


def test_gitflow_diagram_is_a_required_asset():
    # The diagram is the only thing on team-gitflow.md that a student cannot
    # get from the prose. mkdocs copies docs/images/ without ever checking
    # the result, so a rename or a deleted file publishes a broken-image
    # icon on a build that exits 0.
    assert "images/gitflow.png" in REQUIRED_ASSETS
```

- [ ] **Step 8: Run them and watch them fail**

```bash
poetry run pytest tests/test_site_guard.py -v -k "asset"
```

Expected: **collection error** — `ImportError: cannot import name 'REQUIRED_ASSETS' from
'tools.site_guard'`. That is the correct failure; nothing is defined yet.

- [ ] **Step 9: Implement `REQUIRED_ASSETS` and `missing_assets`**

In `tools/site_guard.py`, add the function immediately after `missing_pages`:

```python
def missing_assets(site_root: Path, required: list[str]) -> list[str]:
    """Return required non-page assets that did not publish, sorted.

    Images are not pages: missing_pages() looks for <slug>/index.html and can
    never observe a dropped image. An image that fails to copy renders as a
    broken-image icon on a site build that exits 0.
    """
    return sorted(rel for rel in required if not (site_root / rel).is_file())
```

Add the list immediately after `REQUIRED_PAGES`'s closing bracket:

```python
# Site-relative paths to non-page files that must publish. Hand-maintained for
# the same reason REQUIRED_PAGES is: nothing derives it, so nothing shrinks it
# to match a mistake.
REQUIRED_ASSETS = [
    "images/gitflow.png",
]
```

Wire it into `main()`. Replace the body between `absent = ...` and the `return 1` block:

```python
    leaked = forbidden_files(site_root)
    absent = missing_pages(site_root, REQUIRED_PAGES)
    absent_assets = missing_assets(site_root, REQUIRED_ASSETS)
    for path in leaked:
        print(f"PUBLISHED SOURCE FILE: {path}", file=sys.stderr)
    for slug in absent:
        print(f"MISSING PAGE: {slug}", file=sys.stderr)
    for rel in absent_assets:
        print(f"MISSING ASSET: {rel}", file=sys.stderr)
    if leaked or absent or absent_assets:
```

- [ ] **Step 10: Run the whole guard suite**

```bash
poetry run pytest tests/test_site_guard.py -v
```

Expected: **all PASS**, including the four tests added in steps 4 and 7.

- [ ] **Step 11: Add the M7 forward pointer to `git-workflow.md`**

In the `## M7 and M8 — commit access` section, the existing example branch is
`intake-current-limiting` — with no `feature/` prefix, which contradicts the team convention
this change is documenting. Fix it and add the pointer. Replace:

````markdown
```bash
git clone https://github.com/IgniteRobotics/Robot-2026.git
cd Robot-2026
git checkout -b intake-current-limiting
```
````

with:

````markdown
```bash
git clone https://github.com/IgniteRobotics/Robot-2026.git
cd Robot-2026
git checkout -b feature/intake-current-limiting
```

This is where [the team gitflow](team-gitflow.md) stops being background reading and starts
being what you do: your branch is `feature/<name>`, your pull request targets `development`
rather than `main`, and `main` is competition code you never push to directly. You read that
page at M0 — read it again now, because from here it describes your week.
````

- [ ] **Step 12: Build the site and run the guard**

```bash
poetry run mkdocs build --strict
poetry run python tools/site_guard.py _site
ls -l _site/images/gitflow.png
```

Expected: a clean build, `site guard OK: _site`, and a non-zero-size PNG.

Then prove the new asset check actually fires — a guard nobody has seen fail is a guard
nobody knows works:

```bash
mv _site/images/gitflow.png /tmp/gitflow-holdout.png
poetry run python tools/site_guard.py _site; echo "rc=$?"
mv /tmp/gitflow-holdout.png _site/images/gitflow.png
```

Expected: `MISSING ASSET: images/gitflow.png` on stderr and `rc=1`.

- [ ] **Step 13: Check the links**

The Notion URL added in step 2 is now a CI dependency — `docs-links.yml` runs lychee with
`fail: true`.

```bash
lychee --no-progress --accept 200,206,403,429 docs/programming/team-gitflow.md docs/programming/git-workflow.md
```

Expected: all links OK, including the relative `../images/gitflow.png` and the Notion URL.

No `lychee` binary locally? Check the relative links by hand instead and say so in the task
report rather than claiming the run:

```bash
test -f docs/images/gitflow.png && echo "image link resolves"
test -f docs/programming/git-workflow.md && echo "git-workflow link resolves"
```

If the Notion link fails in CI because the page moved or went private, **do not delete the
link and move on** — the page is the source of truth for this content, and losing the
pointer is worse than a red gate. Tell a mentor and get the URL fixed.

- [ ] **Step 14: Read the diagram in dark mode — this is the point of the whole task**

No command proves this. `mkdocs build` succeeding means the file copied, not that anybody
can read it.

```bash
poetry run mkdocs serve
```

Open `http://127.0.0.1:8000/programming/team-gitflow/`, use the palette toggle in the header
to switch to the dark scheme, and confirm **every one of these is legible**:

- the branch labels `main`, `development`, `feature/sharklasers`, `feature/bad_idea`,
  `feature/trebuchet`
- the `hotfix/gwinnett` label
- both tag annotations, `tag: release/gwinnett` and `tag: release/albany`

If any of them is invisible, step 1 did not work and the task is not done. Report what you
saw, in both schemes.

- [ ] **Step 15: Commit**

```bash
git status --short
git add docs/programming/team-gitflow.md docs/images/gitflow.png \
        docs/programming/git-workflow.md mkdocs.yml \
        tools/site_guard.py tests/test_site_guard.py
git commit -m "Add the team gitflow page, its diagram, and an asset guard

M0 teaches a student git and never mentions that the team works differently.
A student clones Robot-2026 read-only at M2 and finds a development branch
that git-workflow.md does not describe. This is the page that answers that,
positioned as literacy: the practice lands at M7 with commit access.

The diagram is the team's own PNG, vendored because Notion serves it from a
presigned URL that expires in five minutes. It arrived 960x343 RGBA with a
fully transparent background and black strokes -- on the dark scheme every
label rendered black-on-black and vanished, leaving only the coloured commit
dots, which reads as a rendering bug rather than a missing image. Composited
onto opaque white with 16px padding.

Shipped unedited otherwise, including the place where it disagrees with the
written rules: the drawing shows the hotfix merging back into main only,
while rule 6 says both main and development. The prose names the gap rather
than silently correcting someone else's diagram.

site_guard grows REQUIRED_ASSETS. A missing image is not a missing page --
missing_pages looks for index.html and can never see it -- so it publishes a
broken-image icon on a build that exits 0. Verified by holding the file out
and watching the guard go red, not by assuming it would.

git-workflow.md's M7 example branched intake-current-limiting with no
feature/ prefix, contradicting the convention this change documents. Fixed.

Co-Authored-By: Claude Opus 5 <noreply@anthropic.com>"
```
