# Task 7: Retire the M6 reservation model, and fix the prose that expires

`UNTESTED-TARGETS.md` was built as a first-come queue with a `claimed by` column, on the
premise that two students testing the same class is wasted effort. **Decision D-4 reverses
that premise:** every student may open their own pull request against any class. The queue
becomes a shortlist, and a shortlist does not deplete.

Separately, three documents still tell students `Robot-2026` has no tests — which stops being
true the moment the first M6 pull request lands, and is the exact framing spec §6.6 was
written to retire.

The risk in this task is over-correcting into a free-for-all. Removing *reservation* is not
removing *the hard rule*: §6.5's pure-logic-only constraint is untouched and load-bearing.

**Files:**
- Modify: `docs/programming/UNTESTED-TARGETS.md`
- Modify: `projects/m7-ai-assisted/FEATURE-BACKLOG.md`
- Modify: `projects/m7-ai-assisted/README.md`

**Consumes:** decision **D-4**, resolved — no reservation, any student, any class. Step 2 is
the only part still carrying an open question, and it is M7's, not M6's.
**Produces:** an M6 target list that scales to a cohort, and M7 prose that stays true after
student #1.
**Lane:** `content`

---

## The findings this closes

**P2 (M6 half) — dissolved by D-4, not mitigated.** The capacity arithmetic that produced
this finding — three targets against a cohort of ~15 — only bit because targets were
reserved. They are not any more.

**P2 (M7 half) — still real.** `FEATURE-BACKLOG.md` has one open entry. Two students cannot
both ship the same feature the way they can both test the same class, so M7 keeps a genuine
scarcity problem that D-4 does not address.

**C12 — the §6.6 staleness fix never reached M7.**
- `projects/m7-ai-assisted/README.md`: *"Note that `Robot-2026` has no tests yet — yours may
  be the first."*
- `FEATURE-BACKLOG.md`: *"`Robot-2026` has **no tests at all** — there is no `src/test/`
  directory… your feature's test may well be the first test in the repository."*

M6 is M7's prerequisite and its deliverable is merging tests into `Robot-2026`. M6's own page
was already fixed — *"your target comes from `UNTESTED-TARGETS.md` rather than from being
first"* — and M7 was missed.

---

- [ ] **Step 1: Rewrite `UNTESTED-TARGETS.md` as a shortlist.**

      Remove the `claimed by` column, and remove the paragraph that justified it:
      > **"Put your GitHub handle in the claimed-by column in the same pull request that adds
      > your tests.** Two students testing the same class is wasted effort, and the column is
      > how you find out before you start rather than in review."

      That is now the opposite of the policy. Replace it with the policy: **pick any class on
      this list; somebody else working on the same one does not block you.** Say why in one
      sentence, because a student who has read the old framing elsewhere will wonder — two
      people testing the same math from different angles produces better coverage than one,
      and neither of them learns less for it.

      Keep, unchanged: the hard-rule section (pure logic only, no timing, no simulation, no
      hardware), the `LaunchCalculator` caveat, the "Adding a target" section, the owner
      line, and the `Last verified` date. None of those depend on reservation.

      **Add the collision note**, because this is the one thing that actually bites:
      > If somebody has already added tests for the class you picked, their test class is
      > already there. Add your cases to it rather than creating a second class with the same
      > name in the same package — that will not compile. `git fetch upstream && git merge
      > upstream/main` before you start, and again before you open the pull request.

      That is the M0 merge lesson doing real work, and it belongs in the file rather than in a
      mentor's head.

- [ ] **Step 2: Widen the target list, and flag the M7 gap.**

      Measured 2026-08-18 against the §6.5 rule — verify rather than trusting this list:
      | Class | Seam worth testing | Notes |
      |---|---|---|
      | `subsystems/shooter/MappedLaunchRequestBuilder` | interpolated lookup by distance | already listed |
      | `subsystems/shooter/ParabolicLaunchRequestBuilder` | same interface, different math | already listed |
      | `statemachines/LaunchCalculator` | time-of-flight maps, phase-delay prediction | already listed, caveated |
      | `subsystems/drive/DriveMotorConfigs` | `Slot0Configs` construction, field by field | **new** — only import is `Slot0Configs`, no HAL. Pairs with M4's stretch refactor: test it, then refactor it to `.clone()` and watch the tests hold |
      | `subsystems/drive/SteerMotorConfigs` | same shape | **new** — same reasoning |
      | `statemachines/ShiftState` | `getShiftFromMatchTime` boundaries, countdown formatting | **new**, caveated — real boundary logic, but a singleton reaching `DriverStation`. Same shape as `LaunchCalculator`; say so |

      `DriveMotorConfigs` is the best first pull request on the list — genuinely pure, and the
      test-then-refactor pairing with M4 is the clearest demonstration in the curriculum of
      what a test is *for*.

      **`FEATURE-BACKLOG.md` is a different problem and D-4 does not solve it.** One open
      entry, and features cannot be duplicated between students. Add entries against the
      file's own criteria (small, real, useful, defensible, testable without hardware) if you
      can, and if you cannot, say so explicitly in the file with a date and leave it for a
      mentor session. An honestly short backlog with a named owner beats a padded one — but
      it must be *labelled* short, so the first student to arrive is not surprised.

- [ ] **Step 3: Retire the "first test" framing in M7.**
      `projects/m7-ai-assisted/README.md` step 3 and `FEATURE-BACKLOG.md`'s "A note before you
      start" both need M6's treatment: stop asserting the repository has no tests, and say
      what stays true instead — `build.gradle` is already wired for JUnit, most classes still
      have no coverage, and if `src/test/` does not exist yet you create it.

      Keep the load-bearing half: *"prefer a feature whose logic can be exercised without the
      HAL."* Good advice regardless of how many tests exist.

- [ ] **Step 4: Check whether any other document still claims zero tests.**
      ```bash
      grep -rn "no tests\|zero test\|first test\|no src/test\|src/test.*absent" docs/ projects/ --include="*.md"
      ```
      Sort the hits into three buckets and report them:
      - **Historical, dated, fine** — Track D's measured state-of-the-repositories list and
        M6's *"When this curriculum was written…"* are explicitly retrospective. Leave them.
      - **Present tense and about to expire** — fix.
      - **In `teaching-repo-spec.md` or `curriculum-spec.md`** — do not touch. Report them to
        task 9, which owns both specs.

- [ ] **Step 5: Verify and commit.**
      ```bash
      poetry run mkdocs build --strict     # expect rc=0 (UNTESTED-TARGETS.md is published)
      poetry run python tools/templates_sync.py verify
      git status --short
      ```
      Report the final target count and whether `FEATURE-BACKLOG.md` grew or was labelled
      short.
