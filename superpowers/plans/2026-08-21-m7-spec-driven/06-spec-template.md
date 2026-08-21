# Task 6: `SPEC-TEMPLATE.md` — the worked example

M7 is fade level **L0 — worked example only.** This is that worked example: one complete
spec for a real feature, in the shape a student copies. It is the only scaffolding the
milestone gets, and everything downstream — the defense review's spec questions, the
`Spec:` disclosure line, the divergence question — assumes a student has seen one.

Two risks, both fatal if missed. **It must be a worked example of a feature no student will
ship** — `CLAUDE.md`'s authoring rule is that a worked example never contains the answer to
the exercise, and a spec is a *very* complete answer. And **it must be short.** A generous
template produces the sixteen-acceptance-criteria failure that task 3's prose warns about,
and a student will match whatever length you model far more reliably than they will follow
an instruction not to.

**Files:** Create `projects/m7-ai-assisted/SPEC-TEMPLATE.md`
**Consumes:** task 1's restocked `FEATURE-BACKLOG.md` — you spend the
`grabVisionEstimateList()` entry here, and task 1 guarantees others remain
**Produces:** the section headings task 7's `DEFENSE-REVIEW.md` questions are written against
**Lane:** `m7project`

- [ ] **Step 1: Read the source material**
      - `projects/m7-ai-assisted/FEATURE-BACKLOG.md` — the `grabVisionEstimateList()`
        exception-signal entry is your subject. Confirm task 1 has landed and other Open
        entries exist; if it is still the only one, **stop** — shipping this leaves the
        backlog empty.
      - `docs/programming/coding-standards.md` if task 2 has landed, for the conventions the
        worked spec should visibly respect. Not a dependency: reference it by name, not by
        relative path. `projects/` is outside `docs_dir` and MkDocs never reads it.

- [ ] **Step 2: Write the file — two halves, clearly separated**

      **Half one: the worked example.** A complete `proposal.md` for the vision-exception
      feature, in OpenSpec's shape:

      ```markdown
      ## Why
      <the problem in the code today, and what it costs. Two or three sentences>

      ## What changes
      <the observable difference. Not the implementation>

      ## Approach
      <the technical decision and the alternative you rejected, with the reason>

      ## Scenarios
      - **WHEN** <condition> **THEN** <observable outcome>
      - **WHEN** <edge case> **THEN** <observable outcome>

      ## Out of scope
      <what a reviewer might expect and will not find>

      ## Tasks
      - [ ] <checklist a student works through>
      ```

      Keep it to roughly one screen. Five or six scenarios, not sixteen. **The `Approach`
      section is the one that carries the lesson** — it is where a decision gets written down
      with its rejected alternative, which is precisely what the defense review asks about and
      precisely what an agent will otherwise decide silently.

      **Half two: how to use it.** Short. Where the file goes
      (`openspec/changes/<your-feature>/proposal.md` in `Robot-2026`), that the folder already
      exists because a mentor set it up, that it is committed **before** the first
      implementation commit, and the one rule: you write this, not the agent. "Here is my
      spec, what did I miss?" is allowed and is the best prompt in the milestone.

- [ ] **Step 3: Say what a small change gets**
      Two sentences, in the file: a two-line fix does not get a spec, and a feature that needs
      one gets a spec this size, not three times it. Point at the `Out of scope` heading as
      the cheapest way to keep a spec short — most over-specification is a spec that never
      said what it was not doing.

- [ ] **Step 4: The check no command can run — read it for leaked answers**
      Read the finished file against **every** Open entry in `FEATURE-BACKLOG.md`. For each:
      does this worked example hand a student the implementation of the feature they might
      pick? Check the prose, the scenarios, and the headings — a scenario naming the exact
      guard clause a different feature needs is a leak even if the feature is never mentioned.
      **Report the check per entry.** A leak found here costs a rewrite; a leak found by a
      student costs the milestone.

- [ ] **Step 5: Verify**
      ```bash
      poetry run mkdocs build                        # rc=0 -- projects/ is not published; sanity only
      poetry run python tools/tests_lock.py verify   # rc=0 -- SPEC-TEMPLATE.md is not under src/test/
      poetry run python tools/site_guard.py _site    # site guard OK -- confirms nothing leaked into the site
      git status --short                             # only SPEC-TEMPLATE.md
      ```

- [ ] **Step 6: Commit**
      Report the file's line count. If it is over about 120 lines, say so and say why — the
      length is the lesson.
