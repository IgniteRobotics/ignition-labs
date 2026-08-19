# Task 4: Make every statement about CI true

Three student-facing sentences describe CI behaviour that does not exist, and one of them
describes the exact opposite of a decision the spec calls load-bearing. All of them land on
M0, which is every student's first hour and the place where "the repository looks broken"
costs a whole session.

Everything in this task must be verified by running the tool, not by reading the spec. Two of
these findings exist because a document was checked against another document.

**Files:**
- Modify: `docs/CONTRIBUTING.md`
- Modify: `docs/programming/milestones/m0-tooling-and-git.md`

**Consumes:** nothing.
**Produces:** M0 and `CONTRIBUTING.md` describing the pipeline that actually runs.
**Lane:** `student-docs`

---

## The findings this closes

**C3 — `CONTRIBUTING.md` states the opposite of the build's wiring.**
> "Do not argue with the formatter, and do not format by hand: `./gradlew spotlessApply`.
> **It runs automatically before every compile.**"

No project wires `compileJava.dependsOn 'spotlessApply'`. It is omitted deliberately —
teaching-repo-spec §4.3 and §17 both explain that auto-apply-on-compile and a real CI format
check are mutually exclusive, and `CLAUDE.md` forbids re-adding it. A student who believes
this sentence never runs `spotlessApply` and gets exactly the red check it promises is
impossible.

**C4 — M0 describes its own CI wrongly, three ways.**
1. *"Ignite's CI runs automatically on every PR (`student-check.yml`). It does two things:
   `./gradlew build` … and `spotlessCheck`."* For a `contributors/`-only pull request — the
   one M0 tells the student to open — the build job is **skipped**, because
   `tools/changed_projects.py` returns `[]`. Spec §9.1 documents this correctly and says it
   is by design.
2. The DoD item *"`spotlessCheck` passed without a mentor fixing it for you"* is therefore
   unverifiable: the check never runs on that pull request, and the `FormatMe.java` fix is
   reverted before commit by design.
3. *"There is also a second workflow that runs an AI reviewer against every PR diff."*
   `ignition-labs` has no such workflow. That is `Robot-2026`'s `ai-code-review.yml`, which
   the student first meets at M7.

**S3 — no documented path for the repository's own tooling.** `pytest`, `mkdocs build`,
`site_guard.py`, and `tests_lock.py` are all CI-required and documented only in `CLAUDE.md`
(agent-facing) and workflow YAML. A mentor adding a milestone reverse-engineers it.

**S6 — `mkdocs build` prints an alarming red banner** warning that MkDocs 2.0 removes the
plugin system and is *"currently unlicensed — unsuitable for production use."* Expected, per
spec §16 item 6, but nothing says so.

---

- [ ] **Step 1: Establish the ground truth by running it.**
      ```bash
      printf 'contributors/jsmith.md\n' > /tmp/m0.txt
      poetry run python tools/changed_projects.py --from-file /tmp/m0.txt
      printf 'projects/m1-java-fundamentals/src/main/java/frc/training/m1/Clamp.java\n' > /tmp/m1.txt
      poetry run python tools/changed_projects.py --from-file /tmp/m1.txt
      grep -rn "compileJava.dependsOn" projects/*/build.gradle projects/testouts/*/build.gradle
      ls .github/workflows/
      ```
      Expected: `[]` then `["projects/m1-java-fundamentals"]`; no `compileJava.dependsOn`
      anywhere; four workflows and no AI reviewer. Paste all four outputs — they are the
      evidence for every edit below.

- [ ] **Step 2: Fix the formatting claim in `CONTRIBUTING.md`.**
      Delete *"It runs automatically before every compile"* and replace it with what is
      true and why it is a good thing:
      > Run it yourself before you push. It deliberately does **not** run on every compile —
      > if it did, the build would reformat your tree before CI could ever notice a
      > violation, and the formatting check would be decoration. See
      > `programming/teaching-repo-spec.md` §4.3.

      One sentence of reasoning is worth it here: this is the exact wiring that made M0's
      gate unreachable once already, and a future contributor is otherwise likely to "fix"
      it back.

- [ ] **Step 3: Rewrite M0's CI section to describe the two-stage reality.**
      It should say plainly that:
      - Every pull request runs the matrix job, which verifies the given tests are unmodified
        and validates contributor files.
      - The build-and-format job runs **only for the milestone project a pull request
        touches**, so an M0 contributor-file pull request will not run a Gradle build — that
        is expected, not a broken setup, and it is why the fork-Actions step matters even
        more (with Actions off, the student sees *nothing at all* and cannot tell the two
        apart).
      - From M1 onward, their pull request builds and format-checks the one project they
        changed.

      This is a better M0 lesson than the current text, not a worse one: it teaches that CI
      is targeted rather than magic.

- [ ] **Step 4: Fix the two orphaned M0 claims.**
      - Delete the AI-reviewer sentence, or move it to a forward reference — *"`Robot-2026`
        runs a second workflow that AI-reviews every pull request. You meet it at M7."* It is
        genuinely interesting; it is just not about this repository.
      - Rewrite the DoD item *"`spotlessCheck` passed without a mentor fixing it for you."*
        Make it about the exercise the student actually performs: they ran `spotlessApply` on
        `FormatMe.java`, read the diff, reverted it, and can say what the formatter changed.
        That is mentor-verifiable and it is what §9.1 already intends.

- [ ] **Step 5: Add a maintainer section to `CONTRIBUTING.md`.**
      Short. Everything a human needs to work on the repository itself rather than a
      milestone:
      ```bash
      poetry install
      poetry run pytest
      poetry run mkdocs build --strict
      poetry run python tools/site_guard.py _site
      poetry run python tools/tests_lock.py verify
      ```
      Plus: `tests_lock.py write` after adding or changing a given test, `export
      JAVA_HOME=~/wpilib/2026/jdk` before any local Gradle run, and one line saying the red
      MkDocs 2.0 banner from Material is expected and tracked in teaching-repo-spec §16.

- [ ] **Step 6: Sweep for any other CI claim in student-facing prose.**
      ```bash
      grep -rn "spotlessCheck\|gradlew build\|CI runs\|checks will run\|student-check" docs/ projects/*/README.md
      ```
      Read every hit against step 1's outputs. Report any you changed and any you left,
      with the reason.

- [ ] **Step 7: Build and commit.**
      ```bash
      poetry run mkdocs build --strict     # expect rc=0
      poetry run pytest                    # expect all green
      git status --short
      ```
