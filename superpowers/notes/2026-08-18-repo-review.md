# Repository review — 2026-08-18

Full review of `ignition-labs` against three questions: is it correct, is it pitched right
for FRC students, and can anyone set it up. Every claim below was checked by running
something; commands and output are named where they matter.

Remediation plan: [`../plans/2026-08-18-review-remediation/`](../plans/2026-08-18-review-remediation/00-plan.md).

**Verdict.** The repository is in better shape than almost anything in this category. The
central invariant holds empirically across all twelve Gradle projects, the CI is unusually
thoughtful about the "check that passes while the thing it protects is broken" failure
mode, and the prose is genuinely well written for its reader. There are **two real code
defects**, both in M4, and a cluster of **doc-vs-behaviour contradictions concentrated in
M0** — which is the worst possible place for them, because M0 is every student's first
hour.

---

## What was run

| Check | Command | Result |
|---|---|---|
| Clean-clone invariant | fresh `git clone` → `assemble` / `compileTestJava` / `test` / `spotlessCheck` on all 12 projects | **holds everywhere** — table below |
| Tooling test suite | `poetry run pytest` | 135 passed |
| Lock manifest | `poetry run python tools/tests_lock.py verify` | `tests.lock OK` |
| Site build | `poetry run mkdocs build` | rc=0 |
| Regression gate, example tier | `regression_gate m6-testing drop-gear-ratio.patch --expect caught:…` | rc=0, patch reverted, tree clean |
| Regression gate, student tier | same, `flip-sign` and `widen-tolerance`, `--expect no-failure` | rc=0 both |
| External links | every `http(s)` URL in `docs/`, `templates/`, `README.md` (103 unique) | 102 resolve; 1 expected LAN failure |
| Pinned SHAs | `git cat-file -t` in `Robot-2026` and `Phoenix-2026` | both valid |

### Clean-clone invariant

`export JAVA_HOME=~/wpilib/2026/jdk`, fresh clone, per project:

| Project | assemble | compileTestJava | test | spotlessCheck |
|---|---|---|---|---|
| m0-onboarding | 0 | 0 | 0 | **1 (by design — `FormatMe.java`)** |
| m1-java-fundamentals | 0 | 0 | **1** | 0 |
| m2-oop | 0 | 0 | **1** | 0 |
| m3-command-based | 0 | 0 | **1** | 0 |
| m4-control | 0 | 0 | **1** | 0 |
| m5-auto-vision | 0 | 0 | **1** | 0 |
| m6-testing | 0 | 0 | 0 (by design — L0) | 0 |
| t1-find-the-bug | 0 | 0 | **1** | 0 |
| t2-supplier-swap | 0 | 0 | **1** | 0 |
| t3-scheduler-conflict | 0 | 0 | **1** | 0 |
| t4-ringing-flywheel | 0 | 0 | **1** | 0 |
| t5-drifting-auto | 0 | 0 | 0 (by design — no tests) | 0 |

`compileTestJava` green everywhere is the part that matters: the red comes from failing
assertions, never a compile error. Spec §12.3 is satisfied in fact, not just in claim.
Sample of what a student actually sees at M1:

```
AtSetpointTest > valuesThatAreEqualButNotIdenticalCountAsAtSetpoint() FAILED
    java.lang.UnsupportedOperationException at AtSetpointTest.java:14
```

---

# 1. Is it correct?

## What is right

- **Version pinning is uniform.** All twelve projects: GradleRIO `2026.2.1`, wrapper
  `8.11`, Spotless `8.2.1`, JUnit `5.10.1`, Java 17. Vendordeps match the spec's per-milestone
  minimum sets exactly. No drift anywhere.
- **The authoring rules are actually followed.** No given test asserts a stub throws. The
  harnesses (`SimDriver`, `DeliverablesFixture`, `SchedulerFixture`) catch
  `UnsupportedOperationException` and convert it into an actionable `fail()` —
  *"flywheelPlant() in RobotContainer still throws UnsupportedOperationException. Construct
  a FlywheelPlant in your constructor and return it."* That is the correct pattern and it
  is applied consistently.
- **No answer leakage.** `m4-control/Constants.java` carries CAN IDs and setpoints and
  states in its own Javadoc that no gain lives there. `ArmPlant` exposes moment of inertia
  and gearing — physical quantities to reason about — and no kV. T4's README withholds
  which gain is wrong.
- **`tests.lock` covers what it claims.** 50 entries including `plant/`, `harness/`,
  `regressions/*.patch`, and the deeper `projects/testouts/*/…/plant/` path. `verify` only
  flags modified/deleted files, so the M2 student copying `starter/SensorProfileTest.java`
  into `src/test/` does **not** break their own PR. That is correct behaviour and worth
  knowing it was checked.
- **The regression gate works.** This is the best idea in the spec and it does what it
  says: the given worked example catches `drop-gear-ratio` and does *not* catch `flip-sign`
  or `widen-tolerance`, so a student who writes nothing cannot pass. Each run reverts its
  patch and leaves the tree clean.
- **Workflow design is defensive in the right places** — `spotlessCheck` ordered before any
  build step so a future auto-apply cannot silently disable the gate; `given_tests_red.py`
  refusing to read a non-zero exit as evidence of legitimate red; the `lock` job explicitly
  refusing to auto-commit a regenerated manifest.

## Defects

### C1 — M4's SysId workflow cannot work as documented · **High**

Three independent breaks, all in `projects/m4-control/`:

1. **The plants never see SysId's voltage.** `FlywheelPlant.step()` and `ArmPlant.step()`
   both do `sim.setInput(commandedVolts)`, and `commandedVolts` is written only by
   `setVoltage()`. `SysIdWiring.forMotor()` drives the mechanism through
   `motor.setControl(request.withOutput(volts))` on the `TalonFX`. Nothing routes the
   device's applied output back into the physics, so a SysId sweep moves nothing.
2. **The student's own loop fights the sweep.** `Robot.robotPeriodic()` calls
   `updateFlywheel()` and `updateArm()` unconditionally every tick, rewriting
   `commandedVolts` from the closed loop. There is no seam to suspend it.
3. **SysId routines cannot be scheduled at all.** M4's `Robot.java` never calls
   `CommandScheduler.getInstance().run()` — M3's and M5's both do — and the project
   contains no `Subsystem` and no controller. `SysIdWiring.forMotor()` takes a `Subsystem`
   requirement and returns Commands. The README's *"Bind `SysIdWiring.forMotor(...)`'s
   routines to controller buttons, run `./gradlew simulateJava`"* has no path to execution.

Also: `FlywheelPlant.step()` publishes rotor velocity but never rotor position; `ArmPlant`
publishes both. The flywheel `TalonFX`'s Position signal is permanently zero.

**Why it went unnoticed:** the given tests are a behavioural gate on the control loop and
never touch SysId, so neither `skeleton-integrity.yml` nor the solutions cross-check can
see it. This is precisely the failure mode `CLAUDE.md`'s verification-discipline section
exists to catch — the fixture was never completed end to end.

**Blast radius:** *"You ran SysId yourself and used the numbers it produced"* is a
Definition-of-Done item in both `docs/programming/milestones/m4-sensors-actuators-control.md`
and `projects/m4-control/README.md`. *"Stop guessing gains"* is the milestone's headline.
A student will hand-tune, pass the tests, and be marked as having characterized a system.

### C2 — `homingCommand()` cannot run in M4's simulator · **High**

Same root cause as C1(3). The given `ArmHomingTest` runs the scheduler itself, so the test
passes — but README Part 2 step 5 asks the student to build a homing command and there is
no way to watch it work. `Robot.java`'s Javadoc says *"You should not need to change
anything in this file to finish M4."*

### C3 — `docs/CONTRIBUTING.md` states the opposite of the build's real wiring · **High**

> "Do not argue with the formatter, and do not format by hand: `./gradlew spotlessApply`.
> **It runs automatically before every compile.**"

No project wires `compileJava.dependsOn 'spotlessApply'` — deliberately, per spec §4.3, and
`CLAUDE.md` forbids re-adding it. A student who believes this sentence never runs
`spotlessApply` and gets exactly the red check the sentence promises cannot happen.

### C4 — M0 describes its own CI wrongly, three ways · **High**

M0 is every student's first hour and its CI section is the least accurate page in the repo.

- *"Ignite's CI runs automatically on every PR (`student-check.yml`). It does two things:
  `./gradlew build` … and `spotlessCheck`."* Verified: `tools/changed_projects.py` returns
  `[]` for a `contributors/`-only diff, so the build job is **skipped** for exactly the PR
  M0 tells the student to open. Spec §9.1 documents the real behaviour correctly; the
  student-facing page does not.
- The DoD item *"`spotlessCheck` passed without a mentor fixing it for you"* is therefore
  unverifiable — the check never runs, and the `FormatMe.java` fix is reverted before
  commit by design.
- *"There is also a second workflow that runs an AI reviewer against every PR diff."*
  `ignition-labs` has no such workflow. That is `Robot-2026`'s `ai-code-review.yml`.

### C5 — `git-workflow.md` hands students the command M0 forbids · **High**

M0 says: *"Not on this list: `rebase`, **`reset`**, `cherry-pick`, `force push`. They can
destroy work irrecoverably, and you should not meet them before you can recover from
them,"* and *"Nothing is broken until someone types `reset --hard`."*

`git-workflow.md`'s "I committed to main by accident" recipe is:

```bash
git branch m3-command-based
git reset --hard upstream/main     # ← the forbidden command
git checkout m3-command-based
```

No preceding `git fetch upstream`, so it resets to a possibly-stale ref, and it destroys
any uncommitted work without warning. Two student-facing documents, opposite advice, and
the dangerous one is in the how-to.

### C6 — `Phoenix-2026` was not publicly reachable · **Medium** · ✅ **CLOSED 2026-08-18**

**Resolved the same day.** Josh made the repository public. Re-verified anonymously:
repository root, the pinned tree `a4273829…`, and `commands/DriveToPose.java` all return 200,
and a credential-free `git clone https://…` + `git checkout <sha>` succeeds and yields the 22
Java files M3's prose claims. No documentation change was needed — the clone command was
always correct.

**What stays open** is the reason it was invisible: `.lycheeignore`'s
`https://github\.com/IgniteRobotics/.*` blanket, carrying the justification *"Requires
authentication."* That is no longer true of either repository, so the whole entry can go and
every pinned-SHA URL becomes CI-checkable for the first time — see finding C11a and task 5.
The original finding is preserved below.

---

`curl https://github.com/IgniteRobotics/Phoenix-2026` → **404** anonymously.
`Robot-2026` → 200. M3 — the largest milestone and the last of the Kickoff Gate block —
opens with *"read `Phoenix-2026` end to end"* and `projects/m3-command-based/READING.md`
gives a `git clone https://…` plus a browser link, both of which 404 for anyone not in the
org. Every other pinned URL in the repo resolves.

CI cannot catch this: `docs-links.yml` passes `--exclude-path projects`, and
`.lycheeignore` excludes `https://github\.com/IgniteRobotics/.*` outright.

### C7 — both governing specs still say "not yet executed" · **Medium**

`teaching-repo-spec.md:3` and `curriculum-spec.md:3` both read **"Status: Approved spec,
not yet executed."** Every row in teaching-repo-spec §13's build-order table is `Done`, the
code exists, and the site is live. Every acceptance-criteria checkbox in §14 and
curriculum-spec §9.1 is unchecked.

### C8 — accumulated doc/reality drift · **Medium**

| Claim | Location | Reality |
|---|---|---|
| `templates/SUBSYSTEM-README.md`, `TUNING-LOG.md`, `PIT-DOC.md` | teaching-repo-spec §4.1 | `subsystem-readme.md`, `tuning-log.md`, `pit-documentation.md`, plus four more |
| M5 ships "a `SendableChooser` already wired" | teaching-repo-spec §9.6 | `autoChooser()` is a `TODO` throw in `RobotContainer` |
| SETUP.md documents fork-Actions "with a screenshot" | teaching-repo-spec §14 | no screenshot exists |
| `UNTESTED-TARGETS.md` owner "unassigned", "the single largest unmitigated risk" | teaching-repo-spec §16, §17 | the file names Joshua Reddick as owner |
| `Robot-2026` has 47 Java files / 48 Java files | teaching-repo-spec §18 / curriculum-spec §8 | **46** at the pinned SHA and at HEAD |
| Phoenix 6 `26.1.0` | curriculum-spec §8 | every vendordep pins `26.1.1` |
| "Every file imports cleanly into Notion" | curriculum-spec §9.1 | vestigial — D7/D10 moved publication to MkDocs |
| `.lycheeignore`: "Remove this line then" (UNTESTED-TARGETS), "not live until Pages is enabled" | `.lycheeignore` | the file shipped at step 8; the Pages URL returns 200 |
| "Levelling Up With AI" | `mkdocs.yml` nav | page title says "Leveling" |

### C9 — M4's README turns the optional stretch into a DoD item · **Medium**

Milestone doc: the real-repo PR is required and *"the refactor above is only the
recommended vehicle — a documentation fix or a typo correction counts."* README DoD:
*"You opened the stretch pull request against `Robot-2026`."* A student following the
README gets a stricter bar than the spec allows.

### C10 — an M6 DoD item has no vehicle · **Medium**

*"You added `@Logged` to something and saw it appear in the log."* `projects/m6-testing/`
contains zero `Epilogue`/`@Logged` references and no `Robot`/`Main` — it is two plant
classes and two test files. The `Robot-2026` deliverable is pure-logic-only by the §6.5
hard rule, so there is no loggable subsystem there either.

(Related, smaller: M6 Part 2 says *"Make the subsystem `AutoCloseable`"* — the sandbox has
no subsystem, it has `ShooterFlywheel`, a plant.)

### C11 — two blind spots in the verification gates · **Low-Medium**

- `docs-links.yml` runs lychee with `--exclude-path projects`, so **every link in every
  `projects/*/README.md` and `READING.md` is unchecked** — including all five pinned-SHA
  reading assignments, which are the links most likely to rot.
- `site_guard.py`'s `REQUIRED_PAGES` lists 6 of ~28 published pages. The nine milestone
  pages and four track pages are not required to render. `mkdocs build --strict` catches a
  *missing nav target*, but not a page silently dropped from `nav:`.

### C12 — §6.6's own staleness fix has not reached M7 · **Low**

`projects/m7-ai-assisted/README.md`: *"Note that `Robot-2026` has no tests yet — yours may
be the first."* `FEATURE-BACKLOG.md`: *"your feature's test may well be the first test in
the repository."* M6 is M7's prerequisite and requires merging tests into `Robot-2026`, so
by the time anyone reads these they are false. This is the exact framing §6.6 was written
to retire.

---

# 2. Is the level of detail and complexity right for FRC students?

**Broadly yes, and it is the strongest thing about this repository.** The calibration is
deliberate and it shows up in the artifacts, not just in the spec's claims about them.

## What is working

- **The fade curve is real.** M1's stubs carry contract-only Javadoc — *"Values whose
  magnitude is strictly below `threshold` are treated as zero. Everything else is returned
  unchanged — not rescaled, not shifted"* — with no formula and no hint. M2 ships a
  specification with no signatures to copy. M4/M5 hand over plants, harness, and wiring and
  nothing else. M6/M7/M8 ship one worked example and documents. Density genuinely decreases.
- **Test names are sentences.** `valuesThatAreEqualButNotIdenticalCountAsAtSetpoint`,
  `aFractionOfATurnIsNotZero`, `theSupplierKeepsUpWhenTheEncoderMoves`. A student who reads
  only the failure list learns what the exercise is about.
- **Every DoD splits mechanical from explanatory**, and the explanatory half is explicitly
  mentor-verified rather than faked with a test. That is what keeps the automated half
  honest.
- **The prose knows its reader.** *"Check, do not wait."* *"At minute sixteen, ask
  someone."* *"Getting stuck is the job, not a sign you are bad at this."* M1's warning that
  a `while` loop that waits will hang the whole robot is delivered with a runnable demo
  (`./gradlew blockingWaitDemo`) rather than as an assertion.
- **Time estimates are consistent and honest.** Track A: 15–20 sessions to the Gate. Track
  D's six test-outs sum to 4.5 h against a claimed "about four hours." Nothing oversells.
- **Contested decisions are argued, not asserted** — Track D entering at M6, Red tier
  forbidding generation through M2, the Gate gating commit access. Each carries measured
  evidence and an explicit invitation to argue back.

## Concerns

### P1 — M4, not M3, is the real complexity cliff

The docs call M3 and M4 *"tied as the largest."* The artifacts disagree. M3 is L1 and hands
over everything but the lesson, with a scheduler running and a fixture that names exactly
what is missing. M4 asks for: characterization of two unknown plants, feedforward, PID, a
trapezoid profile, and a homing state machine — all in a bare `RobotContainer` with **no
subsystem scaffolding and no scheduler running** — one milestone after the student's first
command-based project. With C1 and C2 unfixed, the student's only feedback loop at M4 is
the given tests plus a CSV they open by hand.

### P2 — the M6→M7 pipeline holds about four students

`UNTESTED-TARGETS.md` lists **three** targets, and says of one of them that it is not
M6-sized (*"expect to spend your session arguing with a mentor about where the seam should
go"*). `FEATURE-BACKLOG.md` has **one** open entry (acknowledged as deliberate). Spec §6.6
built the queue precisely so *"the first `src/test`"* would survive a cohort — but against
a stated cohort of ~15, a three-deep queue exhausts in October. Both files are
mentor-authored per entry, so this is recurring load, not a one-time backfill, and §16
already flags it as the largest unmitigated risk.

### P3 — three documents in the published nav are not written for students

`VISION_GUIDE.md` (958 lines, *"Target Audience: Experienced FRC developers"*, with §9
"Code Review: Current Implementation" and §10 "Recommended Fixes") and
`field-boundary-speed-limiting.md` (948 lines, carrying a blank **"Assigned to: ______"**
line) are internal engineering artifacts sitting in the site's top-level nav beside Track
A. They are correctly cited from `resources.md`, M4, M5, and M8 — but a Track A student
clicking "Vision" lands on a code review of a subsystem they have never seen, on a public
site.

### P4 — the site never gives a student a clickable link to the code it keeps citing

`docs/` mentions `Robot-2026` 173 times and `Phoenix-2026` 41 times. Exactly one file
(`UNTESTED-TARGETS.md`) carries real URLs. All the pinned-SHA links live in
`projects/*/READING.md`, which is structurally unpublishable by design (D7/D10).
Architecturally correct; friction for the stated use case, which is a student reading on a
phone in the shop.

### P5 — M0 is the highest-stakes session and has the least mentor support

M0 asks a student to install a toolchain, configure git, fork, branch, and open a PR while
forbidden from asking an AI for git commands. Defensible and well-argued. But there is no
mentor-facing runbook for that session, and C4, C5, and the SSH gap (S1) all land inside
it.

---

# 3. Is it easy to set up and use on any machine?

**For a student with a GitHub account and working git: yes.** For a student without one:
there is a hole in the first ten minutes.

## Verified working

- All twelve `gradlew` files are mode `100755` in the index, so a fresh clone needs **no**
  `chmod` despite SETUP.md's note. `gradlew.bat` ships alongside every one.
- Every project builds offline from a fresh clone with `JAVA_HOME` set correctly.
- The Python tooling installs and passes on Python 3.13.6 locally while CI pins 3.11 —
  `pyproject.toml` declares `^3.11`, so both are in range.
- The published site returns 200 and `mkdocs build` is clean.

## Gaps

### S1 — no git authentication setup anywhere, and M0's first command is SSH · **High**

`git-workflow.md`'s M0 step 2 is:

```bash
git clone git@github.com:<your-handle>/ignition-labs.git
```

Grepping `docs/`, `templates/`, and every project README for `ssh-keygen`, "SSH key",
"personal access token", `gh auth`, or `credential` returns **nothing**. SETUP.md's six
steps never mention creating a GitHub account, installing git, or setting `user.name` /
`user.email`.

A student on a fresh laptop therefore hits `Permission denied (publickey)` on the clone and
`Please tell me who you are` on the commit — in the milestone whose entire stated purpose
is surviving setup friction. Compounded by mixed transports: 7 `git@github.com:`
occurrences in student docs against 4 `https://…` (the `READING.md` clones).

This is almost certainly a bigger real-world M0 blocker than the fork-Actions step, which
gets a screaming warning in three separate documents.

### S2 — the known `JAVA_HOME` failure is documented last and enforced nowhere · **Medium**

SETUP.md's final "Known problem" describes it correctly. But it is the last item on the
page, and `CLAUDE.md` treats `export JAVA_HOME=~/wpilib/2026/jdk` as mandatory for every
local Gradle run. On this machine, system `java` is **11.0.28** (Homebrew) — exactly the
configuration that fails. No project README, no SETUP.md build step, and no build script
detects it and says so; the student gets GradleRIO's *"Dependency requires at least JVM
runtime version 17"* with no pointer to the fix.

### S3 — no documented setup path for the Python tooling · **Medium**

`pytest`, `mkdocs build`, `site_guard.py`, and `tests_lock.py write` are all CI-required and
documented only in `CLAUDE.md` (agent-facing) and workflow YAML. `CONTRIBUTING.md` — the
human contributor doc — says nothing about Poetry, the `.python-version` pyenv virtualenv,
or how to regenerate `tests.lock`. A mentor adding a milestone reverse-engineers it from
the workflows.

### S4 — operational prerequisites are noted in three places and tracked in none · **Medium**

- `.github/CODEOWNERS` warns in its own header that `@IgniteRobotics/mentors` must be
  created first or **GitHub silently ignores every rule in the file** — no error, no
  warning. Whether that team exists is unverifiable from here and unrecorded anywhere.
- teaching-repo-spec §7.1 still carries *"`ignition-labs` had no git remote configured"* as
  a live prerequisite. It now does (`origin` → `IgniteRobotics/ignition-labs`).
- `.lycheeignore` still says the Pages URL is *"not live until GitHub Pages is enabled"*;
  it returns 200.

Three one-line facts, three locations, no single place that says which are done.

### S5 — Windows is under-served · **Low-Medium**

`gradlew.bat` ships and the build is portable, but SETUP.md's known-problems section is
macOS/Linux-flavoured (`chmod +x`), every example is bash, and the `~/workspace/ignite`
clone convention in all five `READING.md` files has no Windows equivalent. There is no
`.gitattributes`, so line-ending normalisation is unmanaged in a Spotless-gated Java repo
that will be edited on mixed platforms.

### S6 — the docs build prints an alarming banner nobody warns about · **Low**

`mkdocs build` emits a full-width red block from Material warning that MkDocs 2.0 removes
the plugin system, ships no migration path, and is *"currently unlicensed — unsuitable for
production use."* Spec §16 item 6 anticipates the version question but nothing tells a
first-time mentor that the red block is expected.

---

## Priority

| | Fix |
|---|---|
| **Done** | ~~C6~~ — `Phoenix-2026` made public 2026-08-18 |
| **First** | C1, C2 (M4 code) · C3, C4, C5 (M0/CONTRIBUTING contradictions) · S1 (git auth in SETUP) |
| **Next** | C9, C10 (orphaned DoD items) · S2, S3 (setup guidance) |
| **Then** | C7, C8, C12 (spec status and drift) · C11 (gate blind spots) · S4, S5, S6 |
| **Ongoing** | P2 (queue capacity) — needs an owner and a cadence, not a patch |

P1, P3, and P4 are judgement calls rather than defects; the plan proposes an option for
each and does not assume the answer.


---

## Outcome — 2026-08-19

Remediated by `superpowers/plans/2026-08-18-review-remediation/`, executed across three
lanes plus a serialized integration task. Every claim below was verified by running
something; the commands and their output are in the lane commits.

### Closed

| Finding | How |
|---|---|
| **C1, C2** | M4's plants take one input path through the device, and `Robot.robotPeriodic()` runs the scheduler and suspends the student's loop per mechanism. A SysId sweep moves the mechanism and `homingCommand()` is reachable. Verified headlessly: kV = 0.17902 V/(rot/s) fitted over 2004 samples against 0.17902 analytic |
| **C3, C4** | Every CI sentence in `CONTRIBUTING.md` and M0 was re-derived by running the tool. The fabricated AI-reviewer workflow and the false spotless-on-compile claim are gone |
| **C5** | `git-workflow.md`'s `git reset --hard` recipe replaced with branch-and-escalate |
| **C6** | Closed upstream by D-2; `.lycheeignore`'s blanket removed, so CI now notices if it regresses |
| **C7** | Both specs say Executed, dated |
| **C8** | Re-measured, not copied: 46 Java files, Phoenix 26.1.1, seven lowercase templates, M5's chooser is a TODO |
| **C9** | M4's stretch item is a stretch item again |
| **C10** | M6 ships a given `Robot.java`/`Main.java`; the `@Logged` item has a vehicle, verified end to end by toggling the annotation and reading the `.wpilog` |
| **C11** | `docs-links.yml` covers `projects/`, `.lycheeignore` is deleted, `site_guard` requires 13 pages instead of 6 |
| **C12** | The expiring "zero tests" framing is dated or rewritten wherever it appears |
| **P2 (M6 half)** | Dissolved by D-4 rather than mitigated — see below |
| **P3** | D-3: both mentor docs re-homed under For Mentors with audience banners |
| **S1, S2, S3, S5** | `SETUP.md` now covers accounts, identity, credentials, and Python tooling; the `JAVA_HOME` failure is a pre-flight check rather than a footnote; `.gitattributes` added |
| **S4** | teaching-repo-spec §16.1, a dated prerequisite ledger with an owner per row |

### Decisions

- **D-1** (Josh) — route the device's applied output into the plant. **Shipped as
  `getAppliedControl()`, not `getMotorVoltage()`**: the literal form reads 0.0 rot/s in a
  stepped test because Phoenix advances that signal on wall-clock time. The bonus D-1
  predicted — current limits and ramp rates becoming live — **did not happen**, and the
  decision record was corrected before any prose was written from it.
- **D-2** (Josh) — `Phoenix-2026` made public.
- **D-3** (Josh) — mentor docs re-homed; `vision-subsystem.md` stays student-facing.
- **D-4** (Josh) — **there is no queue.** Every student may open a pull request against any
  class; duplication is fine. This retired a model asserted in **four** files, not the two
  the plan scoped — the two it missed were both in the governing spec, one of them a §14
  acceptance criterion that would have certified the retired model as correctly built.

### Not fixed, deliberately

- **P1** — M4 being the real complexity cliff. Tasks 1 and 2 make M4 completable; whether it
  should be split is a curriculum question for a design session.
- **P4** — no clickable link from the site to the code it cites.
- **P5** — M0's mentor support. S1/S2/S3 removed the setup failures; the staffing question
  is unchanged.
- **P2 (M7 half)** — `FEATURE-BACKLOG.md` is still one entry and still unowned. Features
  cannot be duplicated between students the way test targets can, so D-4 does not reach it.
  Now recorded as the largest unmitigated risk in §15 and as the only open row in §16.1.

### Open, found during remediation

1. **M4's plants read only `VoltageOut`.** Any other request — `DutyCycleOut`,
   `VelocityVoltage` — is silently 0.0 V: no exception, no log line, a motionless mechanism,
   at the milestone whose lesson is CTRE configuration. Documented in both plants' Javadoc
   and M4's README, which is the correct fix at L1. **Undecided:** whether a plant should
   say so at runtime. Needs Josh.
2. **`@IgniteRobotics/mentors` has one member.** CODEOWNERS is live, but one reviewer for
   every protected path, who cannot approve their own pull request, is a bottleneck of one.
3. **The `SETUP.md` screenshot has never existed.** §14 has asked for it since the spec was
   written. Capture one or amend the criterion.
4. **`.lycheeignore` is gone**, so a link rotting in `Robot-2026` or `Phoenix-2026` now
   turns this repository's CI red. Correct, and new.

### Verification run on the merged tree

```
pytest 137 passed · tests_lock verify rc=0 · templates_sync verify rc=0
mkdocs build --strict rc=0 · site_guard rc=0 · lychee 275/275 OK
cross-check: 10 of 10 solutions pass  (M4's needed re-syncing to get there)
clean clone, 12 projects x 4 tasks: 38 green, 10 red — every red the expected one
regression gate: drop-gear-ratio caught; flip-sign and widen-tolerance uncaught, as designed
```
