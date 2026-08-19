# Task 9: Integration — specs, manifest, ledger, and the full gate run

The serialized tail. This task owns every contended file, retires the drift the lanes were
forbidden to touch, and runs every gate across every project on the merged result.

It runs **after every lane has landed**, on the merged branch, not on any single lane.

**Files:**
- Modify: `tests.lock`
- Modify: `docs/programming/README.md`
- Modify: `docs/programming/teaching-repo-spec.md`
- Modify: `docs/programming/curriculum-spec.md`
- Create: a prerequisite ledger (location decided in step 5)

**Consumes:** tasks 1–8, all merged.
**Produces:** a repository whose governing documents describe it accurately.
**Lane:** `integration` — single owner. Never run concurrently with anything.

---

- [x] **Step 1: Regenerate `tests.lock`.**
      Task 1 changed files under `projects/m4-control/src/main/java/frc/robot/plant/` and
      `harness/`, both of which are in the manifest. Task 6 may have touched M6's.
      ```bash
      poetry run python tools/tests_lock.py write
      git diff tests.lock
      poetry run python tools/tests_lock.py verify     # expect "tests.lock OK"
      ```
      **Read the diff before accepting it.** Every changed hash must correspond to a file a
      lane reported changing. An unexplained entry means something was edited that should not
      have been — that is the entire point of the mechanism, and it applies to us.

- [x] **Step 2: Fix the status line on both specs.**
      Both currently read:
      > `**Status:** Approved spec, not yet executed`

      `docs/programming/teaching-repo-spec.md:3` and `docs/programming/curriculum-spec.md:3`.
      Every row of teaching-repo-spec §13's build-order table is `Done`, all nine milestone
      projects exist, and the site is live. Replace with an accurate status and a date.

- [x] **Step 3: Tick the acceptance criteria, honestly.**
      teaching-repo-spec §14 and curriculum-spec §9.1 are entirely unchecked. Walk each box
      and mark it — checked, unchecked, or annotated with what is missing. Several are
      verifiable right now and several are not:
      - *"`SETUP.md` documents enabling Actions on a fork, **with a screenshot**"* — the step
        exists, the screenshot does not. Either add one or amend the criterion.
      - *"A student fork's `student-check.yml` produces a single green check"* — unverifiable
        without a real fork. Say so rather than ticking it.
      - *"The solutions cross-check job is green"* — verify against
        `../ignite-training-solutions` before ticking. If task 1 changed M4's given code, the
        M4 solution needs re-running against it, and that is a real possibility this task must
        check rather than assume.
      - *"Every file imports cleanly into Notion"* (curriculum-spec §9.1) is vestigial —
        D7/D10 moved publication to MkDocs. Replace it with the MkDocs equivalent or delete
        it.

- [x] **Step 4: Retire the verified-facts drift.** All in the two specs.

      | Claim | Where | Correct value |
      |---|---|---|
      | `templates/SUBSYSTEM-README.md`, `TUNING-LOG.md`, `PIT-DOC.md` | teaching-repo §4.1 | `subsystem-readme.md`, `tuning-log.md`, `pit-documentation.md`, plus `competition-week-checklist.md`, `post-season-retro.md`, `pr-description.md`, `CONTRIBUTOR.md` |
      | M5 ships "a `SendableChooser` already wired" | teaching-repo §9.6 | `autoChooser()` is a `TODO` throw — the student builds it |
      | `Robot-2026` has 47 Java files | teaching-repo §18 | **46**, at the pinned SHA and at HEAD |
      | `Robot-2026` has 48 Java files | curriculum-spec §8 | same — 46 |
      | Phoenix 6 `26.1.0` | curriculum-spec §8 | `26.1.1`, per every vendordep |
      | `UNTESTED-TARGETS.md` owner "unassigned"; *"the single largest unmitigated risk"* | teaching-repo §16, §17 | the file names Joshua Reddick; `FEATURE-BACKLOG.md` still has no owner, so the risk is halved, not gone. Task 7 has current capacity numbers — use them |
      | *"`ignition-labs` had **no git remote configured**"* as a live blocker | teaching-repo §7.1 | `origin` → `git@github.com:IgniteRobotics/ignition-labs.git`; the site returns 200 |

      Re-measure rather than copying this table — some of it will have moved again.
      ```bash
      ls templates/
      cd ~/workspace/ignite/Robot-2026 && git ls-tree -r --name-only HEAD | grep -c '\.java$'
      ```

- [x] **Step 4b: Fix the surviving copy of the M0 CI claim in `resources.md`.**
      Surfaced by the `student-docs` lane after task 4, outside that task's file list and
      unowned by any lane. `docs/programming/resources.md` describes
      `Robot-2026/.github/workflows/pr-build-check.yml` as *"The CI students' PRs must
      pass — build plus `spotlessCheck`"*, tagged **M0**. That is the same false claim task
      4 removed from `m0-tooling-and-git.md`: an M0 pull request lands in `ignition-labs`
      and is gated by this repository's own `student-check.yml`, not by `Robot-2026`'s
      workflow. The row is worth keeping — the file is genuinely worth reading — but the
      description and the milestone tag both have to change. Check task 4's committed
      wording on `lane/rr-student-docs` first and match it rather than inventing a second
      phrasing.

      **Reassigned 2026-08-19 to the `student-docs` lane**, which still held task 4's
      context. This step is therefore a **verification**, not an edit: confirm the row is
      fixed on the merged tree and that no third copy of the claim survives anywhere under
      `docs/`. If the lane did not land it, do it here.

      **The sweep found a third copy, in a file only this task may touch.**
      `curriculum-spec.md` §4.2, M0's Resources line (~line 182), carries the same
      `pr-build-check.yml` reference under M0 with the same false framing. Fix it here, and
      match the wording the lane committed in `3ebaf9d` — which keeps the row, states
      plainly that it is `Robot-2026`'s CI and not this repository's, names
      `student-check.yml` as what gates an M0 pull request, and re-tags it **M4** on the
      grounds that M4 is a student's first real pull request to `Robot-2026`
      (`robot-repo-ladder.md`; M2 and M3 are read-only clones). Check that reasoning before
      copying the tag — if it is wrong, it is now wrong in two files.

      **Same section, same origin:** `curriculum-spec.md` line ~177 still carries the
      unverifiable M0 Definition-of-Done item *"`spotlessCheck` passed without a mentor
      fixing it for them"*, which task 4 already replaced in `m0-tooling-and-git.md`. Bring
      it into line with task 4's committed version rather than writing a third phrasing.
      Nobody can observe whether a mentor helped, which is why it went.

- [x] **Step 4c: Retire the reservation model from `teaching-repo-spec.md`.**
      Found 2026-08-19 by a tree-wide sweep, after the plan scoped D-4's fix to
      `UNTESTED-TARGETS.md` and `projects/m7-ai-assisted/` alone. That scoping was wrong:
      the governing document asserts the retired model in three places, all verified
      verbatim. No lane may touch this file, so all three land here.

      | Line | Current text | Why it contradicts D-4 |
      |---|---|---|
      | 112 | `UNTESTED-TARGETS.md  ← mentor-maintained M6 queue (§6.5)` | calls it a queue in the tree legend |
      | 300 | *"…checked off as pull requests land. Turns a one-shot bragging right into a queue."* | asserts depletion as the file's entire purpose |
      | 655 | `- [ ] UNTESTED-TARGETS.md exists, has an owner, and is checked off as M6 pull requests land` | makes depletion an acceptance criterion |

      **Line 300 carries a second defect** — "`Robot-2026`'s 47 Java files" — which step 4's
      table already corrects to **46**. Fix both in one edit; do not fix the count and leave
      the queue framing, which is what reading these tables separately would produce.

      Line 666's risk-table row (*"`UNTESTED-TARGETS.md` goes stale and M6 loses its target
      list"*) was checked and is **innocent** — a shortlist can still go stale, and the row
      is about ownership, not reservation. Leave it.

      Match the language the lanes committed, in `b6d3be5` (`robot-repo-ladder.md`) and
      whatever task 7 lands in `UNTESTED-TARGETS.md`. Four files now describe this model and
      they must not describe it four different ways.

- [x] **Step 5: Create a prerequisite ledger.**
      Three separate one-line operational facts live in three places with nothing tracking
      which are done:
      - `.github/CODEOWNERS` warns in its own header that `@IgniteRobotics/mentors` must be
        created or **GitHub silently ignores every rule in the file** — no error, no warning.
        Whether that team exists is currently unverifiable from the repository. **Verify it,
        and record the answer.** If it does not exist, every `tests.lock` and given-test
        review requirement in that file is inert today.
      - teaching-repo §7.1's git-remote prerequisite (done).
      - `.lycheeignore`'s Pages-not-live note (done; task 5 removes the line).
      - **`Phoenix-2026` public** (done 2026-08-18, decision D-2). Worth a ledger row rather
        than being forgotten: M3's reading assignment — the opening instruction of the largest
        milestone in the Kickoff Gate block — depends on it, and a repository's visibility can
        be flipped back by anyone with admin rights without anything in this repository
        noticing. Once task 5 removes the `.lycheeignore` blanket, CI notices; record that as
        the mitigation.

      Put them in one table — teaching-repo-spec §16 is the natural home, or a short
      `docs/programming/OPERATIONS.md` if it grows past a handful. One place, each row
      dated, each row either done or naming who owns it.

- [x] **Step 6: Update the built-milestones status row.**
      `docs/programming/README.md`'s Status table. The current entry is one ~250-word
      sentence — it has been appended to at every build-order step and is now the least
      readable paragraph in the repository. Rewrite it as a table with one row per milestone
      and its state. Same information, legible at a glance, and it stops growing by
      accretion.

      Also refresh teaching-repo §13's status column if anything in this plan changed a
      step's state.

- [x] **Step 7: Run every gate, on the merged tree.**
      ```bash
      export JAVA_HOME=~/wpilib/2026/jdk
      poetry run pytest
      poetry run python tools/tests_lock.py verify
      poetry run python tools/templates_sync.py verify
      poetry run mkdocs build --strict
      poetry run python tools/site_guard.py _site
      ```
      Then the clean-clone invariant across all twelve projects, from a **fresh clone**, not
      the working tree:
      ```bash
      git clone . /tmp/ignition-verify && cd /tmp/ignition-verify
      for p in projects/m*/ projects/testouts/*/; do
        [ -f "$p/gradlew" ] || continue
        for t in assemble compileTestJava test spotlessCheck; do
          (cd "$p" && ./gradlew $t -q >/dev/null 2>&1); echo "$(basename $p) $t rc=$?"
        done
      done
      ```
      Expected, and any deviation is a failure of this task:

      | Project | assemble | compileTestJava | test | spotlessCheck |
      |---|---|---|---|---|
      | m0-onboarding | 0 | 0 | 0 | **1** (FormatMe.java, by design) |
      | m1–m5, t1–t4 | 0 | 0 | **1** | 0 |
      | m6-testing, t5-drifting-auto | 0 | 0 | 0 | 0 |

      And the regression gate, all three patches, confirming a clean tree afterward.

- [x] **Step 8: Re-run the solutions cross-check.**
      `../ignite-training-solutions`, `tools/cross-check.sh`. Spec §11.2: a milestone shipped
      without a passing solution is not shipped. Task 1 changed M4's given code, so the M4
      solution is the one most likely to have broken. Report actual output.

- [x] **Step 9: Read the whole diff for leaked answers and stray files.**
      ```bash
      git diff main --stat
      git status --short
      git diff main | grep -inE "k[SVAGPD] *=|// TODO: (divide|multiply|subtract)"
      ```
      No reference implementation, no scratch file, no gain in a comment. `CLAUDE.md`'s
      never-commit-a-reference-implementation rule applies to this task as much as to any
      other, and this is the task most likely to be carrying someone else's leftovers.

- [x] **Step 10: Write the review record.**
      Append a short outcome section to
      `superpowers/notes/2026-08-18-repo-review.md`: which findings are closed, which were
      deliberately not fixed and why, and which decisions (D-1 … D-4) were taken and by whom.
      A review whose outcome is not written down gets re-run from scratch next season.

      **Carry this forward as an open item, found 2026-08-19 while reviewing task 2.**
      `FlywheelPlant.appliedVolts()` and `ArmPlant`'s equivalent return `0.0` for any
      request that is not a `VoltageOut` — `request instanceof VoltageOut ? clamp(...) :
      0.0`. A student who tries `DutyCycleOut`, or reaches for `VelocityVoltage` to feel
      what on-device control does, gets a motionless mechanism with no exception, no
      warning, and no log line, at the milestone whose lesson is CTRE configuration. Task 2
      documents it in the README and the plants' Javadoc already state it, which is the
      correct fix at L1 and is done. What is **not** decided is whether the plants should
      say so at runtime — a one-line warning on the first non-`VoltageOut` request would
      turn a silent dead end into a sentence. That is a code change to given files, it is
      not in this plan's scope, and it wants Josh's call on whether an M4 plant is allowed
      to talk. Record it; do not implement it here.
