# Task 2: The coding-standards constitution, and its site registration

Ignite's coding standards live in exactly one place — `code-review-prompt.md` — and its only
reader is a bot. M1, M7 and M8 each name this and none of them closes it. This task closes it
by promoting the standards into a document a human reads, and makes the review prompt
downstream of that document rather than parallel to it (design §2.5).

Two things are risky. The first: **a constitution that silently drops a standard the bot
still enforces is worse than no constitution** — it tells a student the rules while a
reviewer applies different ones. The second: a new page can render, publish, and be
completely unguarded, because the two lists that decide what the site guard protects are
hand-maintained on purpose.

**Files:** Create `docs/programming/coding-standards.md` · Modify `mkdocs.yml`,
`tools/site_guard.py`, `tests/test_site_guard.py`
**Consumes:** nothing
**Produces:** `docs/programming/coding-standards.md` at a stable path — tasks 3, 4, 5 and 7
all link to it, and task 3's link is checked under `--strict`
**Lane:** `constitution`

- [ ] **Step 1: Read the source, and separate standards from bot mechanics**
      `projects/m7-ai-assisted/reference/code-review-prompt.md` is the reference copy, 311
      lines. It is **not** all standards. The split:

      | Lines | Section | Moves to the constitution? |
      |---|---|---|
      | 1–13 | Title, Tone and Approach | **No** — how a reviewer should speak |
      | 14–52 | What to Review, What NOT to Focus On | **No** — review scope, not a standard |
      | 53–76 | Custom Rules → Architecture Standards | **Yes** |
      | 77–224 | Team-Specific Guidelines: type-safe units, command factory methods, position/velocity control, automatic homing, thread priority, cached sensor reads, Javadoc | **Yes** |
      | 225–300 | Safety & Error Handling: mechanism limits, current limiting, fault detection, match logging | **Yes** |
      | 301–311 | Output Format | **No** — comment formatting for the bot |

      That split *is* decision 2.5 in concrete form: the constitution owns what good code
      looks like; the prompt owns how a review is conducted.

- [ ] **Step 2: Write `docs/programming/coding-standards.md`**
      Header first, mirroring `ai-use-standard.md`'s front matter and its §11 expiry pattern:

      ```markdown
      # Ignite Robotics — Coding Standards

      **Applies to:** All Java in `Robot-2026` and every season repository after it
      **Status:** **Draft.** Not ratified
      **Owner:** <NAME — see P2; this line must not merge blank>
      **Ratified by:** —
      **Effective:** on ratification
      **Review date:** end of the 2027 build season
      ```

      Then the standards, grouped as the source groups them: **Architecture**, **Team
      conventions**, **Safety and error handling**. Keep the source's code examples — they are
      the part that makes a standard checkable — and keep its ordering, so a reader can diff
      the two documents by eye.

      Two additions the prompt does not have, because a bot does not need them and a student
      does:
      - **One sentence per standard saying *why*.** "Use type-safe units" is a rule; "because
        a `double` that means radians and a `double` that means degrees are the same type, and
        the compiler cannot help you" is a standard someone can apply to a case not listed.
      - **A short opening paragraph** stating what this document is: the decisions that hold
        across every change, as opposed to a spec, which is the decisions for one change
        (design §2.6). Task 3 teaches that distinction; this document should be recognisable
        as the thing being taught.

      **Do not invent standards.** If it is not in the source, it does not go in. This is a
      promotion, not a rewrite. Anything you think is missing goes in the task report for a
      human to decide, not in the document.

- [ ] **Step 3: Record the coverage check, and any deliberate drops**
      At the bottom, a short **Provenance** section: the source file, the pinned SHA of the
      `Robot-2026` copy it was taken from (`reference/README.md` has it), and — if you dropped
      anything from lines 53–300 — what and why. An empty drop list is the expected result and
      should be stated as such rather than omitted.

- [ ] **Step 4: Register the page — both hand-maintained lists**
      1. `mkdocs.yml` `nav:` — under *Programming Curriculum*, immediately **after**
         `AI Use Standard` (line ~44). The two are the pair a student is handed at M7.
      2. `tools/site_guard.py` `REQUIRED_PAGES` — add `"programming/coding-standards"`.
         Read the module comment above that list before editing it: the list is hardcoded
         because a nav-derived one shrinks in lockstep with a bad nav edit and can never
         observe the failure it exists to catch (finding C11b). A page in `nav:` but not in
         `REQUIRED_PAGES` renders today and silently stops being guarded.
      3. `tests/test_site_guard.py` — add an assertion that
         `"programming/coding-standards"` is in `REQUIRED_PAGES`, mirroring the existing
         `team-gitflow` assertion.

      `REQUIRED_ASSETS` is **not** touched — this task ships no images.

- [ ] **Step 5: Verify**
      ```bash
      poetry run pytest                                    # green, incl. the new assertion
      poetry run mkdocs build --strict                     # rc=0 -- --strict, as CI runs it
      poetry run python tools/site_guard.py _site          # site guard OK
      poetry run python tools/tests_lock.py verify         # rc=0, unchanged
      ls -l _site/programming/coding-standards/index.html  # present, non-zero
      git status --short
      ```

- [ ] **Step 6: The check no command can run — line-by-line coverage**
      Open `code-review-prompt.md` and the new document side by side. Walk lines 53–300 and
      confirm **every** standard has a home. Report the walk in the task report as a list, not
      as a claim: a coverage check nobody can see is a coverage check nobody ran. This is the
      task's real deliverable; the markdown is the easy half.

- [ ] **Step 7: Commit**
      Flag in the report if the `**Owner:**` line is still a placeholder — that is P2, it
      blocks merge, and it is a human's to resolve.
