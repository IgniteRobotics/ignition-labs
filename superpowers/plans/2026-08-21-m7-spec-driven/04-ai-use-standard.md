# Task 4: `ai-use-standard.md` — Green's fourth condition

This task makes the spec rule governing rather than advisory. The standard is the document
that gets cited in an actual disagreement, and §9's worked-case table is how it is really
used — a rule that is not in that table will not be applied consistently.

The risky part is tone. This document was written to be argued with and it says so. It is
also a **signed** document: students have acknowledged v1.0. Adding a condition to Green is
a real change to what they signed, and the revision needs to be visible rather than slipped
in.

**Files:** Modify `docs/programming/ai-use-standard.md`
**Consumes:** task 2's `coding-standards.md` (linked); task 3's milestone phrasing, which
this task is **authoritative over** — if task 3's wording and yours differ, yours wins and
you fix task 3's file
**Produces:** the governing text of the spec rule
**Lane:** `m7prose`

- [ ] **Step 1: §4 Green — a fourth condition**
      Green currently lists three conditions that must all hold. It becomes four, with the
      spec **first**, because it is the only one that happens before the code exists:

      ```markdown
      1. **You wrote a spec before you started, and it is committed.** Not a paragraph in the
         pull request — a file, dated before your first implementation commit.
      2. **Tests exist, cover the new behavior, and pass.** Not "the code looks right." Tests.
      3. **AI use is disclosed in the pull request.** See §6.
      4. **You defend the diff in review.** See §7.
      ```

      Then the rule itself, in the paragraph immediately after — placed where the existing
      *"Note the boundary carefully"* paragraph sits, because it is the same boundary one
      milestone later:

      > **The spec is Yellow-tier work, even at Green.** You may use AI to explain API
      > surface, to critique a spec you wrote, and to point at cases you missed. You may not
      > ask it to write the spec, or to decide what the feature should do. You do not get to
      > use the agent to write the constraints that constrain the agent — for the same reason
      > you did not get to use it to write the tests that unlocked Green.

      Say what the spec is *for* in one sentence, so the condition is not arbitrary: it is
      where the decisions live, and §3 is the reason decisions are the thing you have to own.

- [ ] **Step 2: §6 Disclosure — a `Spec:` line**
      The block becomes:
      ```
      AI use: [none | assisted | generated]
      Spec:
      Tool:
      What it did:
      How I verified it:
      ```
      One sentence of definition, added to the table beneath it: the path or link to the
      committed spec, or `n/a` below Green. Update the filled-in example so it carries a
      plausible `Spec:` value — the example is what people actually copy.

      Do **not** disturb the paragraph explaining that "How I verified it" is the field that
      matters. It still is.

- [ ] **Step 3: §7 The defense review — the divergence question**
      Add to the expected-questions list:
      > - Where did the implementation diverge from your spec, and why?

      And one short paragraph after the list: the spec is the review's spine, it was written
      before the code, and every real project diverges — so the answer "it didn't" is itself
      information. Keep §7's existing reassurance intact. It is ten minutes, it is not an
      inquisition, and nothing here makes it one.

- [ ] **Step 4: §9 Worked cases — two rows**
      Insert adjacent to the existing M7 rows so the tier column reads sensibly:

      | Situation | Tier | Ruling |
      |---|---|---|
      | M7 student asks an agent to draft the spec for their feature | Green | **Not allowed.** The spec is the decisions. See §4 |
      | M7 student writes the spec, then asks an agent which cases it missed | Green | **Allowed.** Disclose as `assisted`. This is the highest-value prompt shape in the milestone |

- [ ] **Step 5: §12 Acknowledgment, and the version line**
      The acknowledgment list gains one bullet, matching the existing voice:
      > - If I cannot explain and defend it, it does not ship — and at Green, I write the spec
      >   before the agent writes the code

      Bump **Version** to 1.1 and add a dated line under it naming the change in one sentence.
      Leave §11's expiry (end of the 2027 build season) unchanged — it now covers this rule
      too, which is correct and needs no edit. Leave §3 untouched: §11 says it should not
      change without a real argument, and this change is downstream of §3, not a revision to it.

- [ ] **Step 6: Reconcile with task 3**
      Diff your §4 rule text against the milestone's restatement in
      `docs/programming/milestones/m7-ai-assisted-development.md`. They should not be
      identical — the milestone teaches, the standard governs — but they must not disagree on
      what is allowed. **If they disagree, this file is right and you fix the milestone.**
      Report both texts side by side.

- [ ] **Step 7: Verify**
      ```bash
      poetry run mkdocs build --strict
      poetry run python tools/site_guard.py _site
      poetry run pytest
      git status --short
      ```

- [ ] **Step 8: Commit**
