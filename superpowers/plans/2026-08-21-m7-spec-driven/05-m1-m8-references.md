# Task 5: Close the loop in M1 and M8

Three milestones name the same gap — the team's standards exist in one file nobody reads —
and until task 2 none of them could do anything about it. M1 raises it as a complaint, M8
restates it as context for a different deliverable. Both should now point at the answer.

This is the smallest task in the plan and the easiest to get wrong by overreaching. **You
are adding a pointer, not rewriting either milestone.** M1's line is good writing and its
sting is the point; do not defuse it.

**Files:** Modify `docs/programming/milestones/m1-java-fundamentals.md`,
`docs/programming/milestones/m8-in-season-deliverables.md`
**Consumes:** task 2's `coding-standards.md`
**Produces:** nothing other tasks depend on
**Lane:** `m7prose`

- [ ] **Step 1: M1 — the `Constants.java` note gains a forward reference**
      The existing Resources line reads, of `Robot-2026/.../Constants.java`: *"Three
      conventions, one codebase. Nobody wrote the rule down. This is what inconsistency
      costs, and you will meet more of it."*

      **Keep every word of that.** Append one sentence pointing forward — the rule is written
      down now, in `coding-standards.md`, and the student will be handed it at M7. Present
      tense for the document, future for the milestone: an M1 student is Red tier and should
      not go read a standards document today, they should know it exists and that this
      complaint has an ending.

- [ ] **Step 2: M8 — replace the restated gap with a reference**
      M8 currently says: *"Meanwhile the team's actual coding standards live in exactly one
      place: `.github/prompts/code-review-prompt.md`, a file whose only reader is a bot."*

      That sentence is now false. Replace it with the true version: the standards live in
      `coding-standards.md`, code review enforces them, and **what M8 adds is the layer the
      constitution deliberately does not cover** — what *this* subsystem is and how not to
      break it. Keep the surrounding paragraph's argument intact: M8's deliverable is the
      per-subsystem map, and the constitution does not replace it.

      Check the surrounding lines for anything else that assumes the gap is open. The
      paragraph about conventions existing "only in the heads of the people who wrote them"
      is still true of *subsystem* conventions and should stay.

- [ ] **Step 3: Check for other stale references**
      ```bash
      grep -rn "only reader is a bot\|closest thing the team has to a style guide\|code-review-prompt" docs/
      ```
      Every hit is either updated by this plan (task 3 owns M7's) or is one you just found.
      Fix M1 and M8 here; **report anything else rather than fixing it** — a file this plan
      does not list is out of scope, and the fix belongs in the integration task or a
      follow-up.

- [ ] **Step 4: Verify**
      ```bash
      poetry run mkdocs build --strict
      poetry run python tools/site_guard.py _site
      poetry run pytest
      git status --short              # only the two milestone pages
      ```

- [ ] **Step 5: Commit**
