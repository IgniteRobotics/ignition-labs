# Task 3: `ai-use-standard.md` — v1.3, and the boundary moves

M7 forbids asking an agent to write your spec, without qualification. M8 lifts that, for a
student who has written one by hand, watched an agent misread it, and defended the
divergence. This task moves the boundary in the document that governs it.

The risky part is tone and standing. This is a **signed** document — students have
acknowledged v1.2 — and it was written to be argued with. Relaxing a rule is a harder edit
than adding one, because it must not read as the rule having been soft all along. It was not:
the rule is about competence, and the competence is now demonstrable.

**Files:** Modify `docs/programming/ai-use-standard.md`
**Consumes:** nothing
**Produces:** the governing text of the drafted-spec allowance, which task 1's milestone page
restates
**Lane:** `standard`

- [ ] **Step 1: §4 — the spec rule gains a boundary**
      The Green tier currently carries this block:

      > **The spec is Yellow-tier work, even at Green.** You may use AI to explain API surface,
      > to critique a spec you wrote, and to point at cases you missed. You may not ask it to
      > write the spec, or to decide what the feature should do…

      It becomes **before M8**, and gains the M8 case. Keep the existing sentence intact and
      add the boundary after it, in the document's voice. The shape to land:

      - Before M8, you write the spec. Full stop.
      - At M8 and after, `openspec-propose` may produce a **first draft you red-line**. You
        disclose it, and you can show what you changed — at minimum one requirement you
        rewrote and one scenario you added or deleted.
      - **A draft accepted unchanged is not a pass.** It is evidence you did not review it.

      Say *why* the boundary sits there, in one sentence, or it reads arbitrary: you may review
      a drafted spec once you have written one, for the same reason you may lean on the agent
      once you have written the tests that catch it. **Name that parallel explicitly** — it is
      the third time this document uses the same argument and the repetition is the point.

- [ ] **Step 2: §9 — one or two worked-case rows**
      §9's table is how this document actually gets used. Add, adjacent to the existing M7 rows
      so the tier column reads sensibly:

      | Situation | Tier | Ruling |
      |---|---|---|
      | M8 student has an agent draft their delta spec, then rewrites two requirements | Green | **Allowed.** Disclose as `assisted` and show the red-line. See §4 |
      | M7 student does the same thing | Green | **Not allowed.** The boundary is M8, and it is about having written one yourself first |

      The pair matters more than either row alone — it is the boundary made concrete.

- [ ] **Step 3: §12 — the acknowledgment bullet**
      The Green bullet currently ends *"…I write the spec before the agent writes the code, I
      write it myself, and I can say where the build diverged from it."* Amend so it stays true
      after M8: the student writes it themselves **through M7**, and from M8 owns whatever they
      accepted from a draft. One clause, same voice.

- [ ] **Step 4: The header**
      Bump **Version** to **1.3** and extend the `**Revised:**` line with a dated sentence
      naming this change and stating that anyone who acknowledged v1.2 signs again. Follow the
      existing format — the line already carries v1.1 and v1.2's history and reads as a running
      record.

      **Leave §11's expiry unchanged** (end of the 2027 build season). It covers this rule too,
      which is correct and needs no edit. **Leave §3 untouched** — this change is downstream of
      §3, not a revision to it.

- [ ] **Step 5: Do not touch what is not yours**
      **Contended, forbidden:** `docs/programming/teaching-repo-spec.md` and
      `docs/programming/README.md`. `teaching-repo-spec.md` will need its §1.3 revision entry;
      that is task 9's. Report it, do not write it.

      **Do not edit any `- [ ]` line in any milestone file.** M7's eleven-item DoD pair is
      byte-identical across two files and gated.

- [ ] **Step 6: Verify**
      ```bash
      poetry run mkdocs build --strict
      poetry run python tools/site_guard.py _site
      poetry run pytest
      poetry run python tools/tests_lock.py verify
      git status --short          # only ai-use-standard.md
      ```

- [ ] **Step 7: The check no command can run**
      Read §4 as a student who signed v1.2 and is now being asked to sign again. Is it obvious
      what changed and why they are being asked? If the diff between "you may not" and "you may
      not, before M8" is buried in a paragraph, surface it.

- [ ] **Step 8: Commit**
      Quote the final §4 boundary text in your report — task 1's milestone page restates it and
      the two must not disagree about what is allowed.
