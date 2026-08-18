# Task 6: Authoring notes for M7 and M8

Every milestone ships with something in the solutions repository (§11.2: *a milestone shipped without its solution is not shipped*). M7 and M8 are the two where that something is **not** a reference implementation: §9.8 says M7's assessment is the defense review and tests cover nothing; §9.9 says M8 ships no skeleton code. There is nothing for `cross-check.sh` to overlay, and because its project discovery keys on `settings.gradle`, neither directory is enumerated at all — they produce no line in its output, not even `SKIP`. `m0-onboarding` does print `SKIP`, because it has a `settings.gradle`; that difference is why the analogy misleads.

So the deliverable is authoring notes — the record of why these two milestones are shaped the way they are, so a future maintainer does not "fix" something load-bearing. The risk is writing notes that restate the milestone documents. Notes are for decisions and their reasons, not for content a reader can get from the thing itself.

**Files:**
- Create: `../ignite-training-solutions/authoring-notes/m7.md`
- Create: `../ignite-training-solutions/authoring-notes/m8.md`

**Consumes:** Tasks 3 and 5 — the notes cite the backlog's selection criteria and the sync guard's failure directions.
**Produces:** nothing later tasks depend on.

**Lane:** `tail`. Run on the merged branch in the main worktree, after both lanes land.

---

- [ ] **Step 1: Confirm both lanes are merged and the tree is clean**

```bash
cd /Users/y0shi/workspace/ignite/docs
git status --short          # expect empty
git log --oneline -1
```

- [ ] **Step 2: Confirm the cross-check reports SKIP rather than failing**

```bash
cd ../ignite-training-solutions && ./tools/cross-check.sh 2>&1 | tail -20
```

Expected: every previously-passing solution still `PASS`, and **no line for `m7-ai-assisted` or `m8-ownership` at all.** They have no `settings.gradle`, so `cross-check.sh`'s `find ... -name settings.gradle` never discovers them — they are not even reported as `SKIP`, unlike `m0-onboarding` which has one.

Note that difference in the notes. It is the kind of thing that looks like a bug to whoever next reads the cross-check output looking for M7.

- [ ] **Step 3: Write `authoring-notes/m7.md`**

```markdown
# M7 — authoring notes

Mentor-only. Why M7 is shaped the way it is. Build-order step 11; design record
`superpowers/specs/2026-08-18-m7-m8-design.md` in the labs repository.

## There is no skeleton, and adding one would break the milestone

§9.8: M7 ships a feature into a real repository with commit access, because building a
sandbox for it would defeat the "pick a real, useful feature, not a toy" requirement.

If a future maintainer feels the absence and adds a practice project here, the milestone
stops working. The defense review is the assessment, and you cannot defend a decision you
made about a toy — every hard question ("what happens at a competition, on a cold
robot?") depends on the feature being real.

## `reference/` is a copy for reading and must not be wired to CI

The four files under `projects/m7-ai-assisted/reference/` are `Robot-2026`'s AI review
pipeline at `28ce5397d88275001d656dbb717d22cf9ac0150d`. They look like CI configuration.
They are not, and moving them to `.github/` would be a real mistake, not a tidy-up:

- The workflow needs a `GEMINI_CODE_REVIEW_KEY` secret, which would have to be configured
  on the student-facing repository
- It costs money per pull request
- It would duplicate a workflow that already runs on `Robot-2026`, and the copy would
  drift from the original

And it buys nothing. M7's work happens on a `Robot-2026` branch, where that workflow
already reviews the student's own pull request. A student meets AI-as-reviewer by being
reviewed, which is the only version of the lesson that lands.

## Neither M7 nor M8 appears in the cross-check output at all

Not as `PASS`, not as `FAIL`, not even as `SKIP`. `cross-check.sh` discovers projects by
`find ... -name settings.gradle`, and neither has one. `m0-onboarding` *does* have one and
therefore does appear as `SKIP (no reference solution)`.

This looks like a bug the first time someone goes looking for M7 in that output. It is
not. Do not "fix" it by adding a `settings.gradle` — see the next note.

## Do not give either project a `settings.gradle`

`tools/changed_projects.py` selects CI build-matrix entries by looking for one. Adding it
to a directory with no `gradlew` puts that directory into `student-check.yml`'s matrix,
whose first step is `chmod +x gradlew`, and the student whose Markdown change triggered it
gets a failure that reads like a broken toolchain.

That defect was live until step 11 fixed it — before the fix, `changed_projects.py`
selected any `projects/<name>/...` path without checking. Every directory under
`projects/` had been a Gradle project, so it never fired. M7 and M8 are the first that are
not.

## The backlog goes stale by design

`FEATURE-BACKLOG.md` ships seeded from features mined out of `Robot-2026` at the pinned
SHA. Entries get consumed as students ship them — that is the intended lifecycle, not
rot.

Which is why the file leads with **selection criteria** and only then lists entries. A
mentor refilling it needs the rule (small, real, useful, defensible, testable without
hardware) far more than they need the examples. The single most common wrong entry is a
tuning task: `Robot-2026` has several `TODO: tune` markers, tuning is M4's lesson, it
needs hardware, and there is no diff to defend at the end of it.

The lead entry — publishing `VisionSubsystem`'s twelve write-only rejection counters — was
verified at the pinned commit: private, no accessors, referenced by no other file, and the
file's only `// Update telemetry` comment assigns to unrelated fields. It is also the exact
code M5's `READING.md` sends students to study, so an M7 student improves something they
have already read closely. If it has been shipped, that pairing is worth preserving when
choosing the next lead entry.

## `Robot-2026` has no tests, and its build is already wired for them

`git ls-tree` reports nothing under `src/test`. But `build.gradle` carries
`org.junit.jupiter:junit-jupiter:5.10.1` and `useJUnitPlatform()` — the same version the
training projects pin. So M7's done item "tests pass and cover the new behavior" is
satisfiable, and a student's feature may create the repository's first test.

Say so when assigning. A student who assumes a test harness exists and finds no `src/test`
will reasonably think they are in the wrong repository.

## `DEFENSE-REVIEW.md` is published on purpose

It is the question bank, and students can read it before their review. That is not a leak.
A student who reads the questions and then writes a diff they can answer them about has
done exactly what the milestone wants. The questions are not memorisable — they are about
the student's own code, which does not exist yet when they read the list.
```

- [ ] **Step 4: Write `authoring-notes/m8.md`**

```markdown
# M8 — authoring notes

Mentor-only. Build-order step 11; design record
`superpowers/specs/2026-08-18-m7-m8-design.md` in the labs repository.

## Every template exists twice, and the duplication is deliberate

`templates/<name>.md` holds the real file a student copies.
`docs/programming/student-deliverables.md` holds the same content inline.

The obvious cleanup — delete one — is wrong in both directions:

- **Delete the inline copy** and the published curriculum page can no longer show the
  template it spends a page explaining. `templates/` is outside `docs_dir`; MkDocs cannot
  read past it, structurally. Readers would have to leave the site to see what is being
  described.
- **Delete the file** and §9.9's requirement fails. The deliverables must be *real files*
  a student copies rather than retypes — "which is the difference between a template and a
  description of a template."

So both stay, and `tools/templates_sync.py` makes the divergence fail CI rather than
surface the day a student is handed a template that contradicts the page describing it.

## Fix drift in the document, then re-extract — never the other way

The tool reports which template drifted, not which copy is right. The convention is that
`student-deliverables.md` is the source: edit the marked region there, re-run the
extraction, commit both. The failure message says so.

Editing `templates/<name>.md` directly makes the check pass again while leaving the
curriculum page wrong, which is the failure this guard exists to prevent, achieved through
the guard.

## Regions are marked with HTML comments, not fences or line numbers

Three shapes were considered:

- **Line numbers** break on the next edit above the region.
- **Fences** cannot express section 4. The competition-week git checklist is an unfenced
  list of `- [ ]` items, and `curriculum-spec.md` §7 classifies it as "Checklist" rather
  than "Template required: Yes" for that reason. Fencing it to make the six uniform would
  render it as a code block on the site and destroy the checkboxes.
- **HTML comment markers** are invisible in rendered output, work for fenced and unfenced
  regions alike, and survive edits above them.

If a seventh deliverable is added: mark the region, add the name to `TEMPLATE_NAMES` in
`tools/templates_sync.py`, and extract. The name is both the marker name and the filename
stem — they are the same string by design, so there is no mapping table to keep in sync.

## M8 is graded across a season, not in a session

There is nothing to run and nothing to make green. Every done item is mentor-verified over
weeks, and several cannot be assessed until after an event. When scheduling, treat M8 as a
standing commitment rather than a milestone with a completion date.
```

- [ ] **Step 5: Commit in the solutions repository**

```bash
cd /Users/y0shi/workspace/ignite/ignite-training-solutions
git status --short
git add authoring-notes/m7.md authoring-notes/m8.md
git commit -m "Add M7 and M8 authoring notes

Neither milestone ships a reference implementation -- M7's assessment is the
defense review and M8 has no code -- so authoring notes are the whole
solutions-repo deliverable for build-order step 11.

Both record the same load-bearing trap: do not give either project a
settings.gradle. Neither appears in cross-check output at all, not even as SKIP,
because discovery keys on that file. It looks like a bug and is not, and adding
one would put a directory with no gradlew into student-check.yml's build matrix."
```

- [ ] **Step 6: Confirm nothing leaked into the labs repository**

```bash
cd /Users/y0shi/workspace/ignite/docs
git status --short          # expect empty
```

The notes belong in the solutions repository only (spec D6, mentor-only). Nothing in this task writes to the labs tree.
