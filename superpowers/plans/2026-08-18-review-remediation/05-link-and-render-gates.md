# Task 5: Close the two blind spots in the verification gates

The link checker cannot see the files that carry the most rot-prone links, and the site guard
requires 6 of ~28 pages to render. Both are the shape of failure this repository keeps
producing: a green check over something it does not actually cover.

The risk here is turning CI red on things nobody can fix. Every widening below must be
verified to pass *today* before it is committed.

**Files:**
- Modify: `.github/workflows/docs-links.yml`
- Modify: `.lycheeignore`
- Modify: `tools/site_guard.py`
- Modify: `tests/test_site_guard.py`
- Modify (optional step 5 only): `mkdocs.yml`

**Consumes:** decision **D-3** for step 5 only. Steps 1–4 and 6–7 have no blocker.
**Produces:** gates that cover what they claim to.
**Lane:** `gates`

---

## The findings this closes

**C11a — `docs-links.yml` excludes `projects/`.** The workflow runs lychee with
`--exclude-path projects`, so every link in every `projects/*/README.md` and `READING.md` is
unchecked — including all five pinned-SHA reading assignments, which are the links most
likely to rot and the ones §16 item 2 schedules a seasonal re-pin for.

**C11b — `site_guard.py`'s `REQUIRED_PAGES` lists six pages.** Nine milestone pages and four
track pages are not required to render. `mkdocs build --strict` catches a *missing nav
target*; it does not catch a page silently dropped from `nav:`.

**C8 tail — three stale entries in `.lycheeignore`.** Two are self-declared:
```
# Created by Plan 3 (M6) alongside UNTESTED-TARGETS.md. Remove this line then.
.*UNTESTED-TARGETS\.md
# Not live until GitHub Pages is enabled (…).
https://igniterobotics\.github\.io/ignition-labs/
```
`UNTESTED-TARGETS.md` shipped at build-order step 8. The Pages URL returns 200.

The third is the blanket that hid finding C6 for as long as it existed:
```
# Requires authentication; verified manually each season.
https://github\.com/IgniteRobotics/.*
```
**That justification is no longer true.** `Robot-2026` was already public, and Josh made
`Phoenix-2026` public on 2026-08-18 (decision D-2, resolved — see `00-plan.md`). Every
pinned URL in the repository is now anonymously reachable, so the blanket can go entirely
rather than be narrowed. That is the single highest-value change in this task: it makes
teaching-repo-spec §16 item 2's seasonal SHA re-pin enforceable by CI instead of by memory.

**C6 — closed upstream, verification inherited.** M3's reading assignment needed no edit; the
clone command was always correct. Task 8 was deleted and this is where its residue landed.

**P3 / D-3 — three non-curriculum documents in the top-level site nav.** `VISION_GUIDE.md`
(958 lines, *"Target Audience: Experienced FRC developers"*, with a code review of the
current implementation) and `field-boundary-speed-limiting.md` (948 lines, carrying a blank
**"Assigned to: ______"**) sit beside Track A in the published navigation.

---

- [ ] **Step 1: Find out what widening lychee would report, before changing CI.**
      Run it locally over the currently-excluded scope first.
      ```bash
      grep -rhoE 'https?://[^ )>"`]+' projects/*/README.md projects/*/READING.md \
        projects/*/*.md projects/testouts/*/README.md 2>/dev/null | sed 's/[.,]$//' | sort -u
      ```
      Then check each one's status code. Record the full list with codes in the task report —
      this is the evidence for what the widened gate will do on its first run.

      **Expect all of them to pass.** As of 2026-08-18 both `Robot-2026` and `Phoenix-2026`
      are public and every pinned URL in the repository resolves anonymously. If something
      here 404s, that is a **new** finding, not the known one — investigate it rather than
      reaching for `.lycheeignore`. An earlier draft of this task told you to expect a
      Phoenix-2026 404; that is stale, and adding an ignore for it would silently recreate
      exactly the blind spot step 3 exists to remove.

- [ ] **Step 2: Widen lychee to cover `projects/`.**
      Drop `--exclude-path projects` (keep `superpowers` and `.superpowers` excluded — those
      are design records, and stale links in them are not a student-facing defect). Confirm
      the run is green locally against step 1's list before committing.

      **There is no longer a known case needing an ignore.** The only one — an org-private
      repository — went away when `Phoenix-2026` was made public on 2026-08-18. If you find
      yourself wanting to add an entry to `.lycheeignore`, that is a signal to check whether
      the link is actually broken, not a routine step. Every entry currently in that file was
      justified by a condition that has since expired, which is how finding C6 survived.

- [ ] **Step 3: Empty `.lycheeignore`, one line at a time, with evidence for each.**
      ```bash
      curl -s -o /dev/null -w '%{http_code}\n' https://igniterobotics.github.io/ignition-labs/
      for u in \
        https://github.com/IgniteRobotics/Phoenix-2026/tree/a4273829a8f64fda90c559e263b0f79164d94bb9 \
        https://github.com/IgniteRobotics/Robot-2026/tree/28ce5397d88275001d656dbb717d22cf9ac0150d ; do
        printf '%s  %s\n' "$(curl -s -o /dev/null -w '%{http_code}' "$u")" "$u"
      done
      ```
      - `.*UNTESTED-TARGETS\.md` — remove. Its own comment says to; the file shipped at
        build-order step 8.
      - The Pages URL — remove if the curl above returns 200.
      - `https://github\.com/IgniteRobotics/.*` — **remove**, now that both repositories are
        public. Do not narrow it to `Phoenix-2026`; that was the pre-D-2 plan and is
        obsolete.

      If that empties the file, delete `.lycheeignore` rather than leaving an empty one, and
      say so in the report — a stale ignore file is how C6 stayed invisible.

      **This is the step that has to be right.** Every entry removed here was justified by a
      condition that has since changed, and each one was hiding something. Re-run the widened
      lychee locally after each removal, not once at the end, so a failure names the line
      that caused it.

      Finally, walk M3's reading assignment the way a student does — a URL check is not a
      clone. From a shell with no org credentials in play:
      ```bash
      GIT_TERMINAL_PROMPT=0 git -c credential.helper= \
        clone https://github.com/IgniteRobotics/Phoenix-2026.git /tmp/phx-check
      cd /tmp/phx-check && git checkout a4273829a8f64fda90c559e263b0f79164d94bb9
      git ls-tree -r --name-only HEAD | grep -c '\.java$'     # expect 22
      ```
      Confirmed working on 2026-08-18; re-run it, because this is the assignment that opens
      the largest milestone in the Kickoff Gate block and a silent regression here costs a
      student a session.

- [ ] **Step 4: Make `REQUIRED_PAGES` cover the pages students actually read.**
      Add all nine milestones and all four tracks to `tools/site_guard.py`. Better: derive
      the list from `mkdocs.yml`'s `nav:` so it cannot drift again — but only if that can be
      done with the standard library, since `site_guard.py` runs in a job with no Poetry
      setup. If it cannot, hardcode the list and add a comment saying why.
      Extend `tests/test_site_guard.py` to cover whichever shape you chose.
      ```bash
      poetry run pytest tests/test_site_guard.py
      poetry run mkdocs build
      poetry run python tools/site_guard.py _site     # expect rc=0
      ```
      Then prove the gate can fail: temporarily drop one milestone from `nav:`, rebuild, and
      confirm `site_guard.py` exits non-zero and names the page. Restore it. Paste both runs.
      A gate nobody has watched fail is a gate nobody knows works.

- [ ] **Step 5: Re-home two reference documents (D-3, resolved 2026-08-18).**

      In `mkdocs.yml`, move **`VISION_GUIDE.md`** and **`field-boundary-speed-limiting.md`**
      out of the top-level `Vision:` section and into the existing **`For Mentors:`** section,
      alongside the two specs.

      **`vision-subsystem.md` stays under `Vision:`.** It is student-readable and M8
      explicitly models the subsystem-README deliverable on it (*"an existing, genuinely good
      subsystem document. Model yours on it"*). Do not move it. `Vision:` having one entry is
      fine.

      Then add a one-line audience banner to the top of each moved document, in the shape:
      > **Who this is for:** mentors, and students at M5 or beyond. It assumes you already
      > know what a pose estimator is — if you are before M5, read the milestone first.

      Write `field-boundary-speed-limiting.md`'s to match what it actually is: an assignable
      design project, not a reference.

      The citations in `resources.md`, M4, M5, and M8 are by repository path rather than by
      nav position, so none of them break. Confirm that rather than assuming:
      ```bash
      grep -rn "VISION_GUIDE\|field-boundary" docs/programming/
      ```

      **Independent of D-3, fix this:** `field-boundary-speed-limiting.md` carries a blank
      **"Assigned to: ______"** line on a public site. Fill it in or delete it.

- [ ] **Step 6: Fix the nav spelling.**
      `mkdocs.yml` says *"Track D — FRC Veteran, Levelling Up With AI"*; the page title says
      *"Leveling"*. Make them match.

- [ ] **Step 7: Verify and commit.**
      ```bash
      poetry run pytest                              # expect all green
      poetry run mkdocs build --strict               # expect rc=0
      poetry run python tools/site_guard.py _site    # expect rc=0
      git status --short
      ```
      Report the widened lychee scope's actual result, including anything it now flags that
      belongs to another task.
