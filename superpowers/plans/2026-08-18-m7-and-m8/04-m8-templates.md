# Task 4: M8's six real templates and the project pointer

§9.9's requirement is exact and it is about file-ness: the deliverables in `student-deliverables.md` become **real files** in `templates/`, "which is the difference between a template and a description of a template." A student copies rather than retypes. `curriculum-spec.md` §7 states the acceptance test — a student can produce a compliant document by filling in blanks.

The risk is duplication. That document already carries every template inline, so this task creates a second copy of each, and two copies of the same text drift. Task 5 builds the guard; **this task's job is to make that guard possible** by delimiting each source region explicitly. Extract verbatim — if you find yourself improving the wording while copying, stop, because the guard you are about to build will fail on exactly that.

**Files:**
- Modify: `docs/programming/student-deliverables.md` (add HTML comment markers only)
- Create: `templates/subsystem-readme.md`
- Create: `templates/tuning-log.md`
- Create: `templates/pr-description.md`
- Create: `templates/competition-week-checklist.md`
- Create: `templates/pit-documentation.md`
- Create: `templates/post-season-retro.md`
- Create: `projects/m8-ownership/README.md`

**Consumes:** Task 1's fix — `projects/m8-ownership/` is a non-Gradle directory and would otherwise enter the build matrix.
**Produces:** six files in `templates/` and six marked regions in `student-deliverables.md`, named `subsystem-readme`, `tuning-log`, `pr-description`, `competition-week-checklist`, `pit-documentation`, `post-season-retro`. **Task 5's `tools/templates_sync.py` is written against exactly those six names** — the marker name and the filename stem are the same string.

**Lane:** `m8`. Work in `.worktrees/m8`.

---

- [ ] **Step 1: Understand the two region shapes before touching anything**

```bash
cd /Users/y0shi/workspace/ignite/docs/.worktrees/m8
grep -n '^```' docs/programming/student-deliverables.md
grep -nE "^## [0-9]\." docs/programming/student-deliverables.md
```

Five fenced ```markdown blocks, six numbered sections. **Section 4, the competition-week git checklist, has no fence** — it is an unfenced list of `- [ ]` items with `[event-name]` placeholders, and `curriculum-spec.md` §7 classifies it as "Checklist" rather than "Template required: Yes" for that reason.

**Do not fence it to make the six uniform.** Inside a fence it renders as a code block on the published site and the checkboxes stop being checkboxes, which is worse for every student who reads it there. The markers exist precisely so the tool does not need the shapes to match.

- [ ] **Step 2: Add the six marker pairs**

Wrap each region in `docs/programming/student-deliverables.md`. HTML comments do not render, so the published page is unchanged.

For the five fenced sections, the markers go **outside** the fences:

```markdown
<!-- template:subsystem-readme start -->
```markdown
# [Subsystem Name]
...
```
<!-- template:subsystem-readme end -->
```

For section 4, they wrap the three `###` sub-sections and the checklist items — from `### Before leaving for the event` through the last item under `### After the event`. **Do not include** the `## 4. Competition week — git checklist` heading itself, its introductory paragraph, or the `### The rule underneath all of it` section that follows: those are prose about the checklist, not the checklist.

The six marker names, exactly:

| Section | Marker name |
|---|---|
| 1. Subsystem README | `subsystem-readme` |
| 2. Tuning log | `tuning-log` |
| 3. Pull request description | `pr-description` |
| 4. Competition week | `competition-week-checklist` |
| 5. Pit documentation | `pit-documentation` |
| 6. Post-season retro | `post-season-retro` |

Verify all twelve markers landed and are balanced:

```bash
grep -c '<!-- template:.* start -->' docs/programming/student-deliverables.md   # expect 6
grep -c '<!-- template:.* end -->' docs/programming/student-deliverables.md     # expect 6
grep -oE '<!-- template:[a-z-]+ (start|end) -->' docs/programming/student-deliverables.md
```

The last command's output must pair up: each name appearing exactly once as `start` and once as `end`, in that order.

- [ ] **Step 3: Extract the six files verbatim**

For each fenced region, the file gets the fence's **contents**, not the fence itself. For section 4, the file gets the marked lines as they are.

```bash
cd /Users/y0shi/workspace/ignite/docs/.worktrees/m8
python3 - <<'PY'
import pathlib, re

doc = pathlib.Path('docs/programming/student-deliverables.md').read_text()
out = pathlib.Path('templates')
out.mkdir(exist_ok=True)

names = ['subsystem-readme', 'tuning-log', 'pr-description',
         'competition-week-checklist', 'pit-documentation', 'post-season-retro']

for name in names:
    m = re.search(
        rf'<!-- template:{re.escape(name)} start -->\n(.*?)\n<!-- template:{re.escape(name)} end -->',
        doc, re.S)
    assert m, f'no marked region for {name}'
    body = m.group(1)
    # Five regions are wrapped in a ```markdown fence; the checklist is not.
    fenced = re.match(r'^```[a-z]*\n(.*)\n```$', body, re.S)
    if fenced:
        body = fenced.group(1)
    (out / f'{name}.md').write_text(body.rstrip('\n') + '\n', encoding='utf-8')
    print(f'wrote templates/{name}.md ({len(body.splitlines())} lines)')
PY
ls templates/
```

Expected: seven files listed — the six new ones plus the pre-existing `CONTRIBUTOR.md`.

- [ ] **Step 4: Read all six, as a student would**

Not optional, and not satisfiable by the extraction succeeding. Open each file and confirm it stands on its own **without the surrounding document**. The prose in `student-deliverables.md` explains each template; the file does not carry that prose.

```bash
head -20 templates/subsystem-readme.md
head -20 templates/competition-week-checklist.md
```

What to check in each:
- Does it start somewhere sensible, or mid-thought?
- Are the `[bracketed placeholders]` obviously placeholders?
- Would a student who opened only this file know what to do with it?

If a file fails that last question, **do not fix it by editing the file** — the guard in Task 5 would fail. Fix it in `student-deliverables.md` and re-run Step 3, so both copies move together. That is the workflow this whole design exists to enforce, and this is the first chance to practise it.

- [ ] **Step 5: Write `projects/m8-ownership/README.md`**

````markdown
# M8 — In-Season Ownership

**No skeleton, no code, no tests.** M8 is what happens after the curriculum ends and the
season starts: you own a subsystem, on the real robot, for real matches.

The whole milestone is deliverables. This directory points at them.

---

## The templates

Real files, in [`templates/`](../../templates/) at the repository root. Copy them — do
not retype them, and do not write your own from memory.

| Template | When |
|---|---|
| `subsystem-readme.md` | Week 1, then updated whenever the subsystem changes |
| `tuning-log.md` | Every tuning session, while you are tuning |
| `pr-description.md` | Every pull request |
| `competition-week-checklist.md` | Before, during, and after each event |
| `pit-documentation.md` | Before the first event |
| `post-season-retro.md` | End of season |

Each one is explained, with the reasoning behind it, in
[`student-deliverables.md`](../../docs/programming/student-deliverables.md). Read the
explanation once; copy the file every time.

## The cadence

Adapted from Team 937's published schedule. A reference, not a mandate — but the
**ordering** is the part that matters.

| Week | Programming deliverable |
|---|---|
| 1 | Subsystems and simple commands for every mechanism and the drivetrain. Minimum functional bot |
| 2 | Basic autonomous |
| 3 | Tune game-piece mechanisms |
| 4 | Advanced autonomous |
| 5 | Tune endgame mechanism |
| 6 | Stress testing, driver practice, pit documentation |
| After | Iterate |

**Make it work, then make it good.** Control code and basic auto come before tuning. A
subsystem tuned before it is finished gets tuned twice.

## Week 0 and 1, when there is no robot yet

There is no such thing as a blocked programmer in Week 1.

1. **Start simulation-based subsystem logic as soon as the design is settled.** You do
   not need hardware to write and test a subsystem's logic — that is what M6 was for
2. **Work against last year's robot.** It drives, it has mechanisms, and it is sitting
   right there

## Definition of done

M8's done list is in
[the milestone document](../../docs/programming/milestones/m8-in-season-deliverables.md).
Everything on it is mentor-verified across a season; there is nothing here to run.
````

- [ ] **Step 6: Confirm neither new directory reaches the build matrix or the site**

```bash
cd /Users/y0shi/workspace/ignite/docs/.worktrees/m8
printf 'projects/m8-ownership/README.md\ntemplates/tuning-log.md\n' \
  | poetry run python tools/changed_projects.py --from-stdin
```

Expected: `[]`

```bash
poetry run mkdocs build --strict > /dev/null 2>&1; echo "mkdocs rc=$? (expect 0)"
poetry run python tools/site_guard.py _site; echo "site_guard rc=$? (expect 0)"
find _site -path "*templates*" -type f | head
```

The `find` must print nothing. `templates/` is outside `docs_dir` and is unpublishable structurally — the markers added in Step 2 must not have changed that, and `--strict` must still pass with the relative links this README uses out of `docs/`.

**Note:** `projects/m8-ownership/README.md` links to `../../templates/` and `../../docs/`. Those are relative links from *outside* `docs_dir`, so MkDocs never sees them and cannot check them — verify by eye that each resolves on disk.

```bash
cd projects/m8-ownership && ls ../../templates/ ../../docs/programming/student-deliverables.md > /dev/null && echo "links resolve"; cd -
```

- [ ] **Step 7: Commit**

```bash
git add templates projects/m8-ownership docs/programming/student-deliverables.md
git commit -m "Add M8's six templates as real files, and the project pointer

§9.9 requires the deliverables to become real files a student copies rather than
retypes -- the difference between a template and a description of one. Extracted
verbatim from student-deliverables.md, which keeps its copy: templates/ sits
outside docs_dir, so consolidating to one copy would mean the published
curriculum page could no longer show a student the template it is explaining.

Each region is delimited with HTML comment markers rather than line numbers or
fences. Line numbers break on the next edit, and fences cannot express section
4 -- the competition-week checklist is an unfenced list of checkboxes, and
fencing it to make the six uniform would render it as a code block and destroy
the checkboxes. Markers are invisible in the rendered page and work for both
shapes. Task 5 verifies the two copies against them."
```
