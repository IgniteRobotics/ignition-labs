# Task 9: Integration — contended files, the gate, and the record

Runs **last, alone, on the merged result.** Three files in this repository are edited by every
plan that touches a milestone, and two agents finishing concurrently clobber each other on
them every time. They belong here and to nobody else.

This task also owns the two things that make M8 real policy rather than a page: the Kickoff
Readiness Gate row, and the correction to the design record that the gate decision reverses.
And it owns the sweep — every prior task ran the gates against its own lane; none ran them
against all the changes at once.

**Files:** Modify `docs/programming/README.md`, `docs/programming/teaching-repo-spec.md`,
`superpowers/specs/2026-08-22-m8-spec-anchored-design.md`
**Consumes:** every task, merged
**Produces:** the shipped change
**Lane:** `integration` — single owner

- [ ] **Step 1: Confirm the lanes have landed**
      ```bash
      git log --oneline main..HEAD
      git status --short          # clean
      ```
      All four tasks present. If any is missing, stop — this task cannot amend a spec section
      describing files that do not exist, and it cannot renumber a ladder task 4 has not
      finished renaming.

- [ ] **Step 2: `docs/programming/README.md` — ladder, gate, and the not-gated line**
      Three edits, and the second is a real policy change:

      1. **The milestone ladder table** gains an M8 row for spec-anchored development, and the
         existing M8 row becomes M9. One-line summary in the table's voice — the existing rows
         read like *"Own a subsystem through a season"*, so match that register.
      2. **The Kickoff Readiness Gate.** The row currently reads:

         `| Using AI tooling on competition code | **+ M7**, and a signed AI-use standard |`

         It becomes **`+ M7 and M8`**, and a signed AI-use standard. **This was decided
         deliberately and reverses the design record — see step 4.** Add one sentence below the
         table naming the consequence plainly, in the section's existing blunt voice: a student
         who finishes M7 in December is not cleared for agentic use until M8 is done too. That
         section already states its consequences in advance rather than letting them be
         discovered in January; keep doing that.
      3. **The not-gated line.** *"M8 is not gated — it runs during the season"* becomes **M9**.
         The new M8 **is** gated; the in-season milestone still is not.

      Check the document-table rows near the bottom for anything the new milestone makes stale.

- [ ] **Step 3: `teaching-repo-spec.md` — §1.3, §8, §9, §13**
      - **§1.3** — a dated revision entry in the existing format, naming: M8 inserted as
        spec-anchored development; In-Season renumbered to M9; `ai-use-standard.md` to **v1.3**
        with the drafted-spec boundary at M8, meaning anyone who acknowledged v1.2 signs again;
        M8 added to the Kickoff Readiness Gate. Close with the preservation pointer:
        `The originating design document is preserved at ../superpowers/specs/2026-08-22-m8-spec-anchored-design.md.`
      - **§8** — the fade curve's L0 row currently reads `M6, M7, M8`. It becomes
        `M6, M7, M8, M9`. Add a sentence noting the curve ends at L0 and both new milestones sit
        there.
      - **§9** — M8 gains a subsection and the in-season subsection renumbers. **Check every
        cross-reference to §9.8 in the repository before you renumber**: some point at M7's
        subsection and some at in-season work, and they do not all move the same way.
      - **§13** — gains a numbered step. Unlike the M7 spec work, this is a **new milestone**,
        not content added to a shipped step. Give it a status column entry consistent with the
        others.
      - **§15** — no new unowned list. Design §2.3 removed the capability backlog; say so, since
        the risk register shrinking is worth recording.

- [ ] **Step 4: Correct the design record — §2.8 was reversed**
      `superpowers/specs/2026-08-22-m8-spec-anchored-design.md` §2.8 currently says **M8 is not
      added to the Kickoff Readiness Gate**, with an argument for why. The programming lead
      decided the opposite on 2026-08-22.

      **Do not silently overwrite it.** Follow the pattern this repository already used for the
      M7 CLI reversal: rewrite §2.8 to state the decision that was taken, and move the old
      position into §9 as the rejected alternative with its reasoning intact. The losing
      argument stays readable next to the winning one — that is how the M7 record handled its
      own reversal and it is why those records are still worth reading.

      Also in that record: **§3** — drop the `mkdocs-redirects` option and record that the URL
      break was accepted, and why. **§9** — the three open questions are now answered; convert
      them into decisions with their rationale rather than leaving them posed.

- [ ] **Step 5: The full gate sweep**
      ```bash
      rm -rf _site
      poetry run pytest
      poetry run mkdocs build --strict
      poetry run python tools/site_guard.py _site
      poetry run python tools/tests_lock.py verify
      git diff --stat tests.lock
      poetry run python tools/templates_sync.py verify
      ls -l _site/programming/milestones/m8-spec-anchored-development/index.html
      ls -l _site/programming/milestones/m9-in-season-deliverables/index.html
      git status --short
      ```
      Expected: suite green; build clean under `--strict`; `site guard OK`; `tests_lock` rc=0
      **and `git diff --stat tests.lock` empty** — this plan adds no test file, and a changed
      lock means something landed under a `src/test/` that should not have.

      **Report actual output for every one. Not a summary.**

- [ ] **Step 6: The cross-file consistency checks**
      ```bash
      # M8's own DoD pair must be identical -- expect no output
      diff <(grep '^- \[ \]' docs/programming/milestones/m8-spec-anchored-development.md) \
           <(grep '^- \[ \]' projects/m8-spec-anchored/README.md)

      # M7's pair must STILL be identical -- nothing here should have touched it
      diff <(grep '^- \[ \]' docs/programming/milestones/m7-ai-assisted-development.md) \
           <(grep '^- \[ \]' projects/m7-ai-assisted/README.md)

      # no stale renumber paths outside history -- expect clean
      grep -rn "m8-in-season\|m8-ownership" --exclude-dir=superpowers --exclude-dir=_site . || echo clean

      # the historical record name survived the rename
      grep -n "M7/M8 design record" tools/templates_sync.py

      # Green's conditions agree everywhere they are stated
      grep -rn "four conditions" docs/

      # nothing in this repository runs OpenSpec -- expect no hits
      ls -d openspec 2>/dev/null; grep -rn "openspec init" .github/ 2>/dev/null
      ```

- [ ] **Step 7: Report the prerequisites, do not resolve them**
      State plainly in the task report:
      - **Q1** — a student's own M7 change must be merged **and archived** before they can start
        M8. It accrues per student and is the mentor's `openspec archive` run. Not schedulable.
      - **Q2** — has a mentor rehearsed the second-reader test from both sides? Blocks
        **teaching**, not merge. Its value rests on the reader being genuinely uninformed, so a
        protocol nobody has run will have leaks in it.
      - **P2** — does `coding-standards.md` name an owner, or does it still read
        `⚠️ UNASSIGNED`? It is published in that state. **A blank owner does not merge** was the
        rule when it shipped and it has not been met.

      Do not attempt any of the three. Q1 accrues, Q2 is two humans in a room, P2 is a name.

- [ ] **Step 8: Commit**
      One commit for the integration edits. Report the full sweep output in the message body or
      the task report — whichever this repository's history does, match it.
