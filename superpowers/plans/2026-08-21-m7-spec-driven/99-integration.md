# Task 9: Integration — the contended files and the full gate sweep

Runs **last, alone, on the merged result.** Two files in this repository are edited by every
plan that touches a milestone, and two agents finishing concurrently will clobber each other
on them every time. They belong here and to nobody else.

This task also owns the sweep. Every prior task ran the gates against its own lane; none of
them ran against all the changes at once, and the failure this repository has produced
repeatedly is a check that passes on a slice while the whole is broken.

**Files:** Modify `docs/programming/teaching-repo-spec.md`, `docs/programming/README.md`
**Consumes:** every task, merged
**Produces:** the shipped change
**Lane:** `integration` — single owner

- [ ] **Step 1: Confirm the lanes have landed**
      ```bash
      git log --oneline main..HEAD
      git status --short          # clean
      ```
      All seven tasks present. If any is missing, stop — this task cannot amend a spec section
      describing files that do not exist.

- [ ] **Step 2: `teaching-repo-spec.md` §1.3 — revision history**
      Append a dated entry in the existing format, matching the 2026-08-07 and 2026-08-20
      entries. One paragraph naming: spec-first SDD added to M7 (§9.8); the spec made Green's
      fourth condition in `ai-use-standard.md` §4; `coding-standards.md` added as a governing
      document with an ownership boundary (§5.7); `SPEC-TEMPLATE.md` added to M7's given
      inventory; M7's mentor-verified count raised from nine to eleven. Close with the
      preservation pointer:
      `The originating design document is preserved at ../superpowers/specs/2026-08-21-m7-spec-driven-development-design.md.`

- [ ] **Step 3: §9.8 — M7's inventory**
      Three edits to the M7 subsection:
      - **Given:** add `SPEC-TEMPLATE.md` — the worked spec, L0's one piece of scaffolding —
        and a pointer to `coding-standards.md` as the standing context a student hands their
        agent alongside it.
      - **Mentor verifies:** `all nine DoD items` → `all eleven DoD items`.
      - Add one sentence: M7's specs commit to `Robot-2026` as
        `openspec/changes/<feature>/`, and the `openspec/` scaffolding there is **mentor-seeded
        once**, not created by the first student's pull request (design §2.4).

- [ ] **Step 4: §5.7 — promote the ownership table**
      §5.7 currently makes documentation ownership governing for the four git documents. Add
      the standards set from design §3 — `ai-use-standard.md`, `coding-standards.md`, the
      review prompt, and M8's `subsystem-readme.md` template — as a second table with the same
      force, and one sentence of rationale in §5.7's existing voice: without the boundary,
      `coding-standards.md` becomes the third place someone writes a convention, and the set
      drifts within one season.

- [ ] **Step 5: `docs/programming/README.md` — the document table**
      Add a row for `coding-standards.md` alongside the existing `ai-use-standard.md` and
      `team-gitflow.md` rows (~line 103–107): *what good Ignite code looks like; the standards
      code review enforces. M7 reading.* Check the §"Using AI tooling on competition code"
      gate row and the *"You sign it at M7"* line for anything the fourth Green condition
      makes stale.

      **Do not touch the milestone status table.** M7 and M8 are already Done and this change
      does not alter build-order status. §13 gains no numbered step — this is a content
      addition to shipped step 11, matching the 2026-08-19 M0 precedent.

- [ ] **Step 6: The full gate sweep**
      ```bash
      poetry run pytest
      poetry run mkdocs build --strict
      poetry run python tools/site_guard.py _site
      poetry run python tools/tests_lock.py verify
      ls -l _site/programming/coding-standards/index.html
      git status --short
      ```
      Expected: suite green; build clean under `--strict`; `site guard OK`; `tests_lock`
      rc=0 **and unchanged** — this plan adds no test file, and a changed lock means something
      landed under a `src/test/` that should not have.

      Report **actual output** for every one. Not a summary.

- [ ] **Step 7: The cross-file consistency checks**
      ```bash
      # The DoD lists must be identical -- expect no output
      diff <(grep '^- \[ \]' docs/programming/milestones/m7-ai-assisted-development.md) \
           <(grep '^- \[ \]' projects/m7-ai-assisted/README.md)

      # Nothing should still describe the standards gap as open -- expect no hits
      grep -rn "only reader is a bot\|closest thing the team has to a style guide" docs/

      # Nothing in this repository runs OpenSpec (design 2.8) -- expect no hits
      ls -d openspec 2>/dev/null; grep -rn "openspec init" .github/ 2>/dev/null
      ```

- [ ] **Step 8: Report the three prerequisites, do not resolve them**
      State plainly in the task report:
      - **P1** — has the `Robot-2026` seeding commit landed, with the season lead's agreement?
        Task 7's README asserts the folder exists. **If P1 has not landed, this does not merge.**
      - **P2** — does `coding-standards.md` name an owner, or is the line still a placeholder?
        **A blank owner does not merge.** Spec §15 already carries one unowned file and it is
        the largest unmitigated risk in the document; this change does not add a second.
      - **P3** — has a mentor run the loop end to end? This blocks **teaching**, not merge.
        Say which of the two it is rather than reporting a single pass/fail.

      Do not attempt any of the three. P1 is another repository, P2 is a person's name, P3 is
      a human doing a milestone.

- [ ] **Step 9: Commit**
      One commit for the integration edits. Report the full sweep output in the message body
      or the task report — whichever this repository's history does, match it.
