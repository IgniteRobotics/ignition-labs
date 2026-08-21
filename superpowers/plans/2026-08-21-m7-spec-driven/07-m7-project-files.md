# Task 7: The M7 project directory — defense questions, disclosure, and both setup paths

The three files a student actually has open while doing M7. This task adds the spec to the
question bank, the `Spec:` line to the disclosure block, and the setup instructions —
including the one decision most likely to cost a student a session if we get it wrong.

The risk is the setup instructions. **The manual path is written first and the CLI second**,
deliberately: a student blocked on an npm install has lost a session to something that was
never their problem, which is the same failure the clean-clone invariant exists to prevent.
If your README reads as "install Node, then optionally do it by hand," you have inverted the
decision.

**Files:** Modify `projects/m7-ai-assisted/DEFENSE-REVIEW.md`,
`projects/m7-ai-assisted/pull-request-template.md`, `projects/m7-ai-assisted/README.md`
**Consumes:** task 6's `SPEC-TEMPLATE.md` and its section headings; task 3's exact DoD
wording, from that task's report
**Produces:** nothing other tasks depend on
**Lane:** `m7project`

- [ ] **Step 1: `DEFENSE-REVIEW.md` — a new question group**
      Insert **"Questions about the spec"** *before* "Questions about the code." Order is the
      point: the review walks the spec first, then the diff against it.

      Six to eight questions in the file's existing voice, covering: why this approach and
      what you rejected; which scenario was hardest to write and why; what you deliberately
      put out of scope; **where the implementation diverged from the spec, and why**; what you
      would specify differently now; and which part of the spec the agent ignored.

      That last one is worth its place. Agents over-interpret and under-interpret specs
      routinely, and a student who noticed is a student who read the output.

      Add one line to the mentor note at the top: divergence is **expected**, not a fault. A
      student reporting none either picked something too small or did not look — and either is
      a conversation, not a failure.

- [ ] **Step 2: `pull-request-template.md` — the `Spec:` line**
      Update the AI-use block to match `ai-use-standard.md` §6 exactly as task 4 leaves it:
      ```
      AI use: [none | assisted | generated]
      Spec:
      Tool:
      What it did:
      How I verified it:
      ```
      Add a bracketed prompt under it in the file's existing style — the path to your
      committed spec in `Robot-2026`. Check the surrounding sections: *How I tested it* stays
      exactly as it is, and nothing here weakens it.

- [ ] **Step 3: `README.md` — the file table and the order**
      - File table gains `SPEC-TEMPLATE.md`: *"A worked spec, and where yours goes. Read it
        before you write yours."*
      - The numbered order gains the spec as step 2, pushing the rest down — matching task 3's
        split so the milestone page and the project README tell the same story.
      - The **"Where the work happens"** section gains the spec's destination:
        `openspec/changes/<your-feature>/` on your branch in `Robot-2026`.

- [ ] **Step 4: `README.md` — both setup paths, manual first**
      New short section. Manual first:

      > **By hand (works everywhere).** On your branch, create
      > `openspec/changes/<your-feature>/` and put `proposal.md` in it, copied from
      > `SPEC-TEMPLATE.md`. That is the whole setup. The folder structure is already in the
      > repository — a mentor set it up.
      >
      > **With the CLI (optional).** If you have Node 20.19 or newer and want
      > `openspec validate`, `openspec list`, and the dashboard:
      > `npm install -g @fission-ai/openspec`, then `openspec init --tools <your-tool>` in
      > your clone. **Do not commit what that writes outside `openspec/`** — the `.claude/`,
      > `.cursor/` and `.agents/` directories it creates are yours, not the team's.

      State plainly that the CLI is optional and nothing in the milestone requires it. A
      student on a lab laptop with a Java toolchain and nothing else completes M7 fully.

- [ ] **Step 5: `README.md` — DoD, nine items to eleven**
      Paste task 3's exact final wording. **Do not paraphrase.** These two lists are verbatim
      copies of each other today and the moment one is reworded they start drifting.
      Verify:
      ```bash
      diff <(grep '^- \[ \]' docs/programming/milestones/m7-ai-assisted-development.md) \
           <(grep '^- \[ \]' projects/m7-ai-assisted/README.md)
      ```
      Expected: **no output.** If task 3 has not landed yet, run this before you commit and
      report the result either way.

- [ ] **Step 6: Verify**
      ```bash
      poetry run mkdocs build --strict
      poetry run python tools/site_guard.py _site
      poetry run python tools/tests_lock.py verify   # rc=0 -- nothing here is under src/test/
      poetry run pytest
      git status --short
      ```

- [ ] **Step 7: The check no command can run**
      Read the README end to end as a student who has just finished M6 and has never seen a
      spec. Can they get from "pick a feature" to "spec committed" without asking anyone and
      without installing anything? If not, the manual path is not clear enough — and it is the
      path most of them will take.

- [ ] **Step 8: Commit**
      Flag in the report whether P1 (the `Robot-2026` seeding commit) has landed. This
      README tells a student the folder is already there. **If it is not, this task's text is
      a lie and the merge waits.**
