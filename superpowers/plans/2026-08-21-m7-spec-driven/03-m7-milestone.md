# Task 3: M7's milestone page — the spec, the constitution, and eleven DoD items

M7 currently teaches everything downstream of code existing. This task adds the front half:
a Concepts section on writing the spec first, the project step that makes it real, two DoD
items, one retargeted DoD item, and the failure mode that catches students who write the
spec afterwards to satisfy the checklist.

The risky part is length. M7 is already the longest milestone page in the curriculum and
its value is that every paragraph earns its place. **The target is roughly one page added,
not three.** If your Concepts section is longer than "Failure modes, by name," cut it.

**Files:** Modify `docs/programming/milestones/m7-ai-assisted-development.md`
**Consumes:** task 2's `docs/programming/coding-standards.md` — the relative link is checked
under `mkdocs build --strict`
**Produces:** the milestone's phrasing of the spec rule, which task 4 is free to correct
against the governing text
**Lane:** `m7prose`

- [ ] **Step 1: New Concepts subsection — "Writing it down first"**
      Insert **after** "Prompting, as an actual skill" and **before** "AI as reviewer, not
      author." That position is deliberate: it reads as the next thing after prompting, and
      it sets up the reviewer section rather than interrupting it.

      Four beats, in this order:

      1. **The gap, stated plainly.** Everything this milestone has taught so far fires after
         the agent has already decided. Cite the failure the student can check against their
         own experience: novices prompting an agent produce low-context prompts at roughly
         three times the rate of experienced programmers, spend most of their prompts
         debugging rather than building, and a fifth of them restart the whole project when
         the accumulated complexity gets away from them. That last one is `drifting`,
         measured — and M7 already taught the word.
      2. **The two scales, which is the whole idea:**
         > **A spec is the decisions for this change. A constitution is the decisions that
         > hold across every change.** Both are things you write down *so the agent cannot
         > make them for you.*
         Link `coding-standards.md` here as the team's constitution.
      3. **Where Ignite stops.** Name **spec-first**, **spec-anchored**, **spec-as-source**.
         Ignite does spec-first. Say why it stops there: tooling exists that marks generated
         code `DO NOT EDIT`, and a student who never edits code did not learn to write code —
         which is the failure `ai-use-standard.md` §1 exists to prevent. A student will meet
         that tooling within a season and the line should be theirs first.
      4. **The honest overhead argument.** A two-line fix does not get a spec. Over-specifying
         is its own failure — quote the practitioner's version of it, a small bug inflated into
         four user stories and sixteen acceptance criteria, *"like using a sledgehammer to
         crack a nut."* M7's voice already argues with itself; keep doing it here.

      Also state the rule, in the milestone's voice, marked as coming from the standard:
      **the spec is Yellow-tier work even at Green.** You may ask an agent what your spec
      missed. You may not ask it to write your spec. Note the parallel the student has
      already lived: at M6 you wrote the tests that unlocked Green; here you write the
      constraints that constrain the agent.

- [ ] **Step 2: Split the project step**
      Current step 2 is *"Use AI assistance genuinely and substantially."* It becomes:

      > 2. **Write the spec, and commit it before you start.** One change folder in
      >    `Robot-2026`: `openspec/changes/<your-feature>/proposal.md`. What is changing, why,
      >    the technical approach, and the cases that must hold. `SPEC-TEMPLATE.md` is a
      >    worked example. The folder is already there — a mentor set it up.
      > 3. **Use AI assistance genuinely and substantially, against the spec you wrote.**
      >    This is the one milestone where that is the assignment rather than a concession.

      Renumber the rest. In the defense-review step, add the question that makes this
      assessable: **"where did the build diverge from your spec, and why?"** Say plainly that
      divergence is expected and is the interesting part — a student who reports none either
      picked something too small or is not looking.

- [ ] **Step 3: DoD — nine items to eleven**
      Append:
      ```markdown
      - [ ] Your spec was committed before your first implementation commit
      - [ ] You can name one place the implementation diverged from your spec, and say why
      ```
      Retarget the existing item:
      ```markdown
      - [ ] You read the coding standards and can name two standards in it
      ```
      (was: `You read .github/prompts/code-review-prompt.md and can name two standards in it`)
      Link `coding-standards.md`.

      **Copy the exact final wording into your task report.** Task 7 puts the identical list
      in `projects/m7-ai-assisted/README.md` and the two must not drift.

- [ ] **Step 4: New Common failure mode — "Spec theatre"**
      Writing the spec after the code, to satisfy a checklist. It is checkable: the commit
      timestamps say so. Add the inverse in the same paragraph — a sixteen-criterion spec for
      a two-file change is the same mistake pointed the other way, and it costs a session.

- [ ] **Step 5: Resources**
      Two entries, in the existing style:
      - **Böckeler, *Understanding Spec-Driven Development*** — the critical case, and the
        source of the three-level ladder. Included *because* it is skeptical.
      - **OpenSpec** — the format M7 uses. Note that the CLI is optional.
      Retarget the existing `code-review-prompt.md` resource line at `coding-standards.md`,
      and change its gloss: it is no longer "our standards, currently read only by a bot."

- [ ] **Step 6: Verify**
      ```bash
      poetry run mkdocs build --strict              # rc=0 -- catches a bad link to coding-standards.md
      poetry run python tools/site_guard.py _site   # site guard OK
      poetry run pytest                             # green
      git status --short                            # only the milestone page
      ```

- [ ] **Step 7: The check no command can run**
      Read the two new DoD items as the mentor who has to verify them, with no test suite and
      ten minutes. *"Your spec was committed before your first implementation commit"* — can
      they check that in one command? If not, reword until they can. An item nobody can verify
      is decoration on a milestone whose entire assessment is human.

- [ ] **Step 8: Commit**
