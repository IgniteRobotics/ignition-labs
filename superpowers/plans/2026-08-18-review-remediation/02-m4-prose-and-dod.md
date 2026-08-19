# Task 2: Align M4's prose and Definitions of Done with what task 1 built

Two documents describe M4's workflow and they disagree with each other about what is
required. Task 1 changed what is possible. This task makes all three agree. The risk is
writing to the plan rather than to the code — every instruction here must be checked against
the tree task 1 actually produced.

**Files:**
- Modify: `projects/m4-control/README.md`
- Modify: `docs/programming/milestones/m4-sensors-actuators-control.md`

**Consumes:** task 1's finished `projects/m4-control/`, including whatever seam it added for
characterization and whatever the student now has to bind themselves.
**Produces:** an M4 whose milestone page, project README, and code say the same thing.
**Lane:** `m4`

---

## The findings this closes

**C1/C2 tail.** The README's Part 1 step 2 currently reads *"Bind `SysIdWiring.forMotor(...)`'s
routines to controller buttons, run `./gradlew simulateJava`, and run each of
quasistatic-forward, quasistatic-reverse, dynamic-forward, dynamic-reverse."* Whatever task 1
did, that paragraph and the milestone's SysId section have to describe it exactly.

**C9 — the README makes an optional stretch mandatory.**

- `docs/programming/milestones/m4-sensors-actuators-control.md` DoD:
  > **"You have opened at least one pull request against a real robot repository.** Required,
  > and the refactor above is only the recommended vehicle — a documentation fix or a typo
  > correction counts."
- `projects/m4-control/README.md` DoD:
  > "You opened the stretch pull request against `Robot-2026`."

The milestone page is right (it matches teaching-repo-spec §6.7). The README is stricter than
the spec allows and a student following it gets a harder bar than the curriculum sets.

---

- [ ] **Step 1: Read task 1's diff before writing a word.**
      ```bash
      git diff main -- projects/m4-control/src/
      ```
      Write down, in the task report, the exact sequence a student now performs to get
      kS/kV/kA: what they bind, what they press, what they run, where the log lands, what
      they feed to the analyser. Everything below is written from that list, not from the
      old README.

- [ ] **Step 2: Rewrite the README's characterization instructions.**
      `projects/m4-control/README.md`, Part 1 step 2 and Part 2 step 2. They must name the
      real seam, the real command, and the real artifact. D-1 resolved as **option A**, so
      SysId stays — the student really does bind the routines, run `simulateJava`, sweep, and
      feed the log to the analyser. What changes is that it now works, plus whatever task 1
      added that the student has to bind themselves (a `Subsystem`, a controller already
      constructed for them).

      **Then rewrite the section option A invalidates.** `projects/m4-control/README.md`
      carries a section headed *"A modelling limit, on purpose"*:
      > "Both plants integrate the voltage you command them with directly — they do not read
      > it back through the `TalonFX`'s own simulation. One consequence: supply-current
      > limits, voltage compensation, and ramp rates you configure through `motor()` are inert
      > in this simulation. If you configure a current limit and see no change in your plot,
      > you have not found a bug — you have found the edge of what this model represents."

      After task 1 this is false in both directions: the plants *do* read the device back, and
      a configured current limit *will* change the plot. Leaving it is worse than the original
      defect, because a student who reads it will disbelieve a correct result. Task 1 fixed
      the matching plant Javadoc; this is the README half.

      Do not simply delete it. The honest replacement is stronger than what it replaces — the
      plants now model what you configure, so configuration is worth getting right, which is
      exactly the CAN-and-configuration lesson this milestone already teaches. Say that,
      briefly, and note any limit that genuinely remains after task 1 (check the diff rather
      than assuming there are none).

- [ ] **Step 3: Fix the README's DoD item.**
      Replace *"You opened the stretch pull request against `Robot-2026`"* with the
      milestone page's wording — one pull request against a real robot repository is
      required, the `.clone()` refactor is the recommended vehicle, a documentation fix
      counts. Keep it short; the reasoning already lives on the milestone page.

- [ ] **Step 4: Update the milestone page's SysId section and DoD.**
      `docs/programming/milestones/m4-sensors-actuators-control.md`:
      - The **SysId** concept section (*"Stop guessing gains… Do it in simulation first. The
        workflow is identical and you cannot break anything"*) must be true of this project.
      - The DoD item *"You ran SysId yourself and used the numbers it produced"* now
        describes something the student can actually do. Keep it; verify the surrounding
        instructions match task 1's seam.
      - Leave the AI-posture line *"never ask an AI for a gain value"* exactly as it is. It
        is the best sentence on the page.

- [ ] **Step 5: Check the two documents against each other, item by item.**
      Put the two DoD lists side by side. Every mechanical item on the README's list must
      appear on the milestone's, and no item may be stricter in one than the other.
      ```bash
      sed -n '/## Definition of done/,/^---\|^## /p' projects/m4-control/README.md
      sed -n '/## Definition of done/,/## Common failure/p' docs/programming/milestones/m4-sensors-actuators-control.md
      ```
      Paste both into the report with the differences called out and justified.

- [ ] **Step 6: Verify no answer leaked in.**
      Prose describing characterization is exactly where a gain slips into a document.
      ```bash
      grep -inE "k[SVAGPD] *= *[0-9]|kV of|about 0\.|roughly 0\." projects/m4-control/README.md docs/programming/milestones/m4-sensors-actuators-control.md
      ```
      Expected: no numeric gain anywhere. `CLAUDE.md`'s first authoring rule applies to
      READMEs, not only to stubs.

- [ ] **Step 7: Build the site and commit.**
      ```bash
      poetry run mkdocs build --strict     # expect rc=0
      git status --short
      ```
      Do not touch `docs/programming/README.md` or either spec — task 9 owns them.
