# Task 7: Make the M6 and M7 queues survive a cohort, and stop them lying about the past

Spec §6.6 exists because *"the first `src/test`"* expires after one student. The fix was a
queue. The queue currently holds three targets — one of which the file itself says is not
M6-sized — and the M7 backlog holds one entry, against a cohort of ~15. Three documents also
still tell students that `Robot-2026` has no tests, which stops being true the moment the
first M6 pull request lands.

This task cannot invent targets. It sizes the gap honestly, fixes the prose that will go
stale on day one, and puts a cadence on the page.

**Files:**
- Modify: `docs/programming/UNTESTED-TARGETS.md`
- Modify: `projects/m7-ai-assisted/FEATURE-BACKLOG.md`
- Modify: `projects/m7-ai-assisted/README.md`

**Consumes:** decision **D-4** — whether Josh (or someone) commits to a session with
`Robot-2026` to fill the queues, and to what cadence. Steps 1, 3, and 4 can run without it.
**Produces:** queues sized against the actual cohort, and prose that stays true after
student #1.
**Lane:** `content`

---

## The findings this closes

**P2 — capacity.** `UNTESTED-TARGETS.md` lists three classes:
`MappedLaunchRequestBuilder`, `ParabolicLaunchRequestBuilder`, `LaunchCalculator`. Of the
third, the file says: *"a refactor of a competition-season state machine is not an M6-sized
task… Take `LaunchCalculator` second, if at all."* So the real M6 queue is **two**.
`FEATURE-BACKLOG.md` has **one** open entry, acknowledged as deliberate. Spec §16 already
calls the ownership of these two files *"the single largest unmitigated risk in the
document"* — and `UNTESTED-TARGETS.md` has since been assigned to Joshua Reddick, which is
progress the specs have not caught up with (task 9 fixes that half).

**C12 — the §6.6 staleness fix never reached M7.**
- `projects/m7-ai-assisted/README.md`: *"Note that `Robot-2026` has no tests yet — yours may
  be the first."*
- `FEATURE-BACKLOG.md`: *"`Robot-2026` has **no tests at all** — there is no `src/test/`
  directory… your feature's test may well be the first test in the repository."*

M6 is M7's prerequisite and its deliverable is merging tests into `Robot-2026`. By the time
anyone reads these, they are false. This is the exact framing §6.6 was written to retire, and
M6's own milestone page has already been fixed — *"your target comes from
`UNTESTED-TARGETS.md` rather than from being first."*

---

- [ ] **Step 1: Measure the real capacity and put the number on the page.**
      ```bash
      cd ~/workspace/ignite/Robot-2026
      git log --oneline -1
      git ls-tree -r --name-only HEAD | grep -c '\.java$'
      git ls-tree -r --name-only HEAD | grep -c 'src/test'
      ```
      Then count the current queue depth in each file. Add a short, dated capacity note near
      the top of `UNTESTED-TARGETS.md`: how many targets are open, how many students are
      expected to need one, and what happens when it empties (a mentor adds rows — the file
      already documents how, under "Adding a target").

      Do the same in `FEATURE-BACKLOG.md`. It already says one entry is deliberate; make it
      also say what the refill trigger is.

- [ ] **Step 2 (D-4): add targets and features, if the decision says to.**
      Both files document their own admission criteria and both are good — `UNTESTED-TARGETS.md`'s
      *"a class belongs here if it has logic worth being wrong about and no test covering
      it,"* and `FEATURE-BACKLOG.md`'s small/real/useful/defensible plus the
      testable-without-hardware filter. Apply them; do not invent new ones.

      Every added row must respect the §6.5 hard rule — **pure logic only, no timing, no
      simulation, no hardware.** A target that needs a device constructed is not a target,
      and the file already says why in a paragraph worth not weakening.

      If D-4 says "not now," write that down with a date and skip to step 3. An honestly
      empty queue with a named owner beats a padded one.

- [ ] **Step 3: Retire the "first test" framing in M7.**
      `projects/m7-ai-assisted/README.md` step 3 and `FEATURE-BACKLOG.md`'s "A note before
      you start" both need the same treatment M6's page already got: stop asserting the
      repository has no tests, and instead say what stays true — `build.gradle` is already
      wired for JUnit, most classes still have no coverage, and if `src/test/` does not exist
      yet you create it.

      Keep the genuinely load-bearing half: *"prefer a feature whose logic can be exercised
      without the HAL."* That is good advice regardless of how many tests exist.

- [ ] **Step 4: Check whether any other document still claims zero tests.**
      ```bash
      grep -rn "no tests\|zero test\|first test\|no src/test\|src/test.*absent" docs/ projects/ --include="*.md"
      ```
      Sort the hits into three buckets and report them:
      - **Historical, dated, fine** — Track D's measured state-of-the-repositories list and
        M6's *"When this curriculum was written…"* are both explicitly retrospective. Leave
        them.
      - **Present tense and about to expire** — fix.
      - **In `teaching-repo-spec.md` or `curriculum-spec.md`** — do not touch. Report them to
        task 9, which owns both specs.

- [ ] **Step 5: Verify and commit.**
      ```bash
      poetry run mkdocs build --strict     # expect rc=0 (UNTESTED-TARGETS.md is published)
      poetry run python tools/templates_sync.py verify
      git status --short
      ```
      `templates_sync.py` is unrelated to these files but is cheap and catches a whole class
      of accidental damage to `docs/programming/`.
