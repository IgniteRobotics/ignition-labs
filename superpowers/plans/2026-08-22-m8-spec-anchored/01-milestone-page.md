# Task 1: The M8 milestone page

M7 ends at "shipped and archived" and the student never opens the spec again. This page is
where that stops being true. It teaches the delta vocabulary a student has not met —
`MODIFIED` and `REMOVED`, against a spec that already exists — and it introduces an
assessment M7 does not have.

The risky part is that this reads as M7 with extra folders. It is a different milestone
because it checks a different property: not *can you defend your decisions* but *is your
spec true, and can somebody else act on it*. If the Concepts section does not make that
distinction land, cut it and start again.

**Files:** Create `docs/programming/milestones/m8-spec-anchored-development.md`
**Consumes:** nothing. Task 4 registers this page; do not edit `mkdocs.yml` or
`tools/site_guard.py` yourself
**Produces:** the exact DoD wording task 2 copies verbatim into the project README
**Lane:** `m8prose`

- [ ] **Step 1: Match the house shape**
      Read `docs/programming/milestones/m7-ai-assisted-development.md` first and follow its
      structure: `## At a glance` (estimated time, prerequisites, AI posture), `## Who enters
      here`, `## Test-out challenge`, `## Concepts`, `## Project`, `## Definition of done`,
      `## Common failure modes`, `## AI posture`, `## Resources`.

      At a glance: **2 sessions**, prerequisites **M7 complete, and your M7 change archived**
      (the mentor runs `openspec archive` after your PR merges — if that has not happened,
      M8 has nothing to anchor to). AI posture 🟢 Green.

      **Test-out challenge: there is none.** Same reason as M7 — the assessment is the work.

- [ ] **Step 2: Concepts — four subsections, and keep them short**

      **1. What M7 could not teach.** Every requirement the student has written is an
      `## ADDED Requirements` block against an empty `openspec/specs/`. A spec that is never
      revisited is a deliverable you handed in. The moment it has to change, it becomes
      something else.

      **2. `MODIFIED` and `REMOVED`.** Show the syntax — it is verified and exact:

      ```markdown
      ## MODIFIED Requirements

      ### Requirement: <name, matching the existing one>
      The system SHALL <the full new behaviour, restated>.

      #### Scenario: <every scenario the current spec has, plus any new ones>
      - GIVEN / WHEN / THEN

      ## REMOVED Requirements

      ### Requirement: <name>
      <why it is going>
      ```

      **`MODIFIED` replaces the whole block, and the tool enforces it.** Quote the real error,
      because it is the lesson stated better than we would state it:

      > `✗ [ERROR] MODIFIED "<name>" omits scenario(s) the current spec still has: "<scenario>".
      > Copy them into the MODIFIED block (a MODIFIED requirement replaces the whole block, so
      > archive refuses to drop them).`

      You cannot change one clause without accounting for every scenario attached to it.

      **3. What validation does not catch.** It enforces **completeness, not truth**. It will
      not let you drop a scenario; it will happily accept one you reworded into a falsehood.
      That gap is the whole reason the assessment is a person reading rather than a command.

      **4. Spec rot, and the `## Purpose` line.** A spec describing what the code *used to* do
      is worse than no spec — it is trusted because it exists. Two concrete things: archiving
      a new capability leaves `## Purpose` reading `TBD - created by archiving change <name>`,
      and the student's own M7 implementation almost certainly diverged from the spec they
      wrote, because M7's done list only required them to *name* the divergence. Both get
      fixed here.

- [ ] **Step 3: Where Ignite stops, and the argument the student now gets to have**
      Spec-first, spec-anchored, spec-as-source. M7 named all three; M8 does the second and
      still refuses the third — a student who never edits code did not learn to write code
      (`ai-use-standard.md` §1). **State plainly that the student is now expected to argue
      with this**, and that making the strongest case *for* spec-as-source is a done item. A
      student who can only recite the Ignite position has not met it.

- [ ] **Step 4: The project steps**
      Six or seven, in the milestone's voice:
      1. Open your archived M7 change in `openspec/specs/<capability>/spec.md`. **Repair the
         drift** between what it says and what you shipped, and write its `## Purpose`
      2. Take the second change your mentor identified — a real change in behaviour, not a
         scenario you forgot
      3. `openspec new change <name>`, write the `MODIFIED` (or `REMOVED`) delta in full
      4. `openspec validate <name>` until it passes, commit before your first implementation
         commit
      5. Build it. Tests pass and cover the new behaviour
      6. **Sit the second-reader test** — `SPEC-REVIEW.md`. Report how many round trips it took
      7. Open the PR; the mentor archives after merge

      **On drafting:** say that `openspec-propose` is now permitted as a first draft you must
      red-line and show — and that this is the same shape as M6 unlocking Green. You earned it
      by writing one by hand. A draft accepted unchanged is not a pass.

- [ ] **Step 5: Definition of done — eight items**
      Take them from the design record §4 verbatim. **Copy the exact final wording into your
      task report** — task 2 pastes the identical list into `projects/m8-spec-anchored/README.md`
      and a `diff` gates it.

      **No `- [ ]` line may contain a markdown link or a docs-relative path.** `projects/` sits
      outside `docs_dir`, so a link that resolves here is meaningless there and the two copies
      could never stay identical. Plain text with backticked filenames; put real links in the
      surrounding prose.

- [ ] **Step 6: Common failure modes, AI posture, Resources**
      Failure modes worth naming: the touch-up that is not a change; the rubber-stamped draft;
      the spec updated in a *follow-up* PR rather than the same one — it was wrong in the
      interim, and the interim is where somebody trusted it.

      AI posture block: Green's four conditions, in the same terse voice M7 uses, matching
      `ai-use-standard.md` §4 as task 3 leaves it. Resources: the OpenSpec docs link already in
      `docs/programming/resources.md`, and `coding-standards.md`.

- [ ] **Step 7: Verify**
      ```bash
      poetry run mkdocs build --strict     # rc=0 -- your page is not in nav yet; that is task 4
      poetry run pytest
      poetry run python tools/tests_lock.py verify
      git status --short                   # only your new page
      ```
      **Do not** run `site_guard.py` expecting a pass — it requires nav registration, which is
      task 4's. Note in your report that it is deferred.

- [ ] **Step 8: The check no command can run**
      Read the eight DoD items as the mentor who has to verify them, with no test suite and ten
      minutes. Can they check each one? Reword until they can.

- [ ] **Step 9: Commit**
