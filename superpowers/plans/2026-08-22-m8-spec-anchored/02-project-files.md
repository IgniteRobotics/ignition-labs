# Task 2: The M8 project directory

Three files a student has open while doing M8: the instructions, the worked `MODIFIED` delta,
and the second-reader protocol. The protocol is the one that matters — it is M8's entire
assessment and it does not exist anywhere yet.

Two risks. **The worked example must not contain the answer to any capability a student will
be assigned** — `CLAUDE.md`'s authoring rule, and a delta spec is a very complete answer. And
**the second-reader protocol has to be runnable by two students without a mentor present**, or
it will quietly turn into a defense review with extra steps.

**Files:** Create `projects/m8-spec-anchored/README.md`, `MODIFIED-EXAMPLE.md`, `SPEC-REVIEW.md`
**Consumes:** task 1's exact DoD wording, from that task's report
**Produces:** nothing other tasks depend on
**Lane:** `m8project`

- [ ] **Step 1: Read the model**
      `projects/m7-ai-assisted/SPEC-TEMPLATE.md` — its shape, voice and length discipline are
      what you are matching. `projects/m7-ai-assisted/DEFENSE-REVIEW.md` is the model for
      `SPEC-REVIEW.md`'s tone: published openly, written for the mentor and the student at once.

- [ ] **Step 2: `MODIFIED-EXAMPLE.md` — the worked delta**
      **Continue the vision-exception capability that `SPEC-TEMPLATE.md` already spends.** That
      is deliberate: it is the one capability guaranteed not to be any student's subject, and
      continuing it lets you show a *second* change to a spec the reader already knows.

      Three parts, in this order:

      1. **The capability spec as it stands after M7's archive** — `## Purpose`,
         `## Requirements`, the two requirements. Show the `Purpose` line filled in, and say
         that archiving left it reading `TBD`.
      2. **The change**, as a real `MODIFIED` + `REMOVED` delta. This exact content is verified
         to validate against OpenSpec 1.10.0 — the count requirement rescoped from *since boot*
         to *since the current enable cycle*, and the exception-type requirement removed:

         ```markdown
         ## MODIFIED Requirements

         ### Requirement: Vision exception occurrence count
         The system SHALL record the number of times the vision-estimate handler has caught an
         exception since the current enable cycle began, as a telemetry field published
         alongside the vision data.

         #### Scenario: Queue drains without throwing
         - GIVEN the vision estimate queue drains cleanly
         - WHEN the handler completes
         - THEN the recorded count is unchanged

         #### Scenario: A single caught exception
         - GIVEN the recorded count reads N
         - WHEN the handler catches one exception
         - THEN the recorded count reads N plus one

         #### Scenario: Repeated failures accumulate
         - GIVEN the handler has already caught an exception during this enable cycle
         - WHEN it catches a second one
         - THEN the recorded count reads two, having not reset between loops

         #### Scenario: The robot is enabled again
         - GIVEN the recorded count reads two from a previous enable cycle
         - WHEN the robot transitions from disabled to enabled
         - THEN the recorded count reads zero

         ## REMOVED Requirements

         ### Requirement: Most recent vision exception type
         <the argument for deleting it>
         ```

      3. **The reasoning, edit by edit.** Why the requirement was rescoped rather than a new one
         added. Why `Repeated failures accumulate` was **kept and reworded** rather than dropped
         — this is the part that teaches: the tool rejects a `MODIFIED` block that omits a
         scenario the current spec still has, so an obsolete scenario gets rewritten, never
         deleted. Quote that error. And why removing a requirement needs an argument.

      Keep it to roughly one screen of delta plus the reasoning. The length you model is the
      length you get.

- [ ] **Step 3: `SPEC-REVIEW.md` — the second-reader protocol**
      Design record §2.4 is the source. It must be executable by a student pair:

      - **Who reads:** any student who has finished M7 and has **not seen the diff**.
      - **What they get:** only the updated capability spec at
        `openspec/specs/<capability>/spec.md`. Not the change folder, not the pull request.
      - **What they do:** state what the system now does, in their own words, including at
        least one edge case. Then read the diff.
      - **The verdict:** if the description does not match the code, **the spec is wrong** —
        regardless of whether the code is right. The author fixes the spec and the reader
        re-reads.
      - **Round trips are the signal, not a penalty.** Report the number. Two or three is
        normal and interesting; zero first time usually means the change was too small.
      - **The author is silent during the reading.** Say this explicitly — it is what makes
        this different from a defense review, where the student drives.

      Add a short mentor note: this teaches both students, and the reader is practising the
      more common real-world skill.

- [ ] **Step 4: `README.md`**
      Match `projects/m7-ai-assisted/README.md`'s shape: what is in the directory, where the
      work happens, the numbered order, what is assessed, and the DoD.

      **Paste task 1's exact DoD wording. Do not paraphrase.** Then verify:
      ```bash
      diff <(grep '^- \[ \]' docs/programming/milestones/m8-spec-anchored-development.md) \
           <(grep '^- \[ \]' projects/m8-spec-anchored/README.md)
      ```
      Expected: **no output.** If task 1 has not landed, run it before committing and report the
      result either way. **You may not edit the milestone page to force a match.**

      The README's first numbered step is the drift repair and the `## Purpose` line — a student
      opens their own archived spec before they write anything new.

- [ ] **Step 5: The check no command can run — read it for leaked answers**
      Read `MODIFIED-EXAMPLE.md` against **every** Open entry in
      `projects/m7-ai-assisted/FEATURE-BACKLOG.md`, since a student's M8 capability descends
      from whichever M7 feature they shipped. For each entry: does this worked example hand
      them the implementation? Check prose, scenarios and headings. **Report the check per
      entry.**

- [ ] **Step 6: Verify**
      ```bash
      poetry run mkdocs build          # rc=0 -- projects/ is not published; sanity only
      poetry run python tools/tests_lock.py verify
      poetry run python tools/site_guard.py _site    # confirms nothing leaked into the site
      git status --short
      ```

- [ ] **Step 7: Commit**
      Report each file's line count. If `MODIFIED-EXAMPLE.md` runs past about 150 lines, say so
      and say why.
