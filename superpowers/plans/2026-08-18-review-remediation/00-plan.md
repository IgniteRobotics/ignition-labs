# Plan — review remediation

Fixes the findings in [`../../notes/2026-08-18-repo-review.md`](../../notes/2026-08-18-repo-review.md).
Finding IDs (`C1`, `S1`, `P2`, …) refer to that document and are quoted in each task file so
a worker does not have to load it.

## Goal

Two real code defects in M4, one cluster of doc-vs-behaviour contradictions concentrated in
M0, one hole in the setup path before a student's first command, and a tail of drift.

When this plan lands:

- M4's SysId and homing instructions describe something a student can actually run.
- Every statement a student reads about what CI does to their pull request is true.
- A student on a fresh laptop can get from zero to a merged M0 pull request without a
  mentor guessing at what broke.
- No governing document says the repository is unbuilt.

**Explicitly not in scope.** Curriculum content changes. New milestones. Any change to the
fade curve, the tier boundaries, or the Kickoff Gate. Bumping any pinned version — that is
the §16 seasonal pass and it is a separate, ordered ritual.

## Architecture

No new mechanisms. The M4 fix is the only one with design content in it, and it is a choice
between two shapes — see decision **D-1** below. Everything else is prose, configuration, or
one file moving.

| Area | Files |
|---|---|
| M4 simulator loop | `projects/m4-control/src/main/java/frc/robot/{Robot,RobotContainer,Constants}.java`, `plant/{FlywheelPlant,ArmPlant}.java`, `harness/{SysIdWiring,M4Deliverables}.java` |
| M4 prose | `projects/m4-control/README.md`, `docs/programming/milestones/m4-sensors-actuators-control.md` |
| First hour | `docs/SETUP.md`, `docs/programming/git-workflow.md`, `.gitattributes` |
| CI truth | `docs/CONTRIBUTING.md`, `docs/programming/milestones/m0-tooling-and-git.md` |
| Gates | `.github/workflows/docs-links.yml`, `.lycheeignore`, `tools/site_guard.py`, `tests/test_site_guard.py`, `mkdocs.yml` |
| M6 logging | `projects/m6-testing/`, `docs/programming/milestones/m6-simulation-telemetry-testing.md` |
| Queues | `docs/programming/UNTESTED-TARGETS.md`, `projects/m7-ai-assisted/{README.md,FEATURE-BACKLOG.md}` |
| ~~M3 reading access~~ | ~~`projects/m3-command-based/READING.md`~~ — no file change needed; D-2 resolved upstream |

## Open decisions

Answer these before the tasks that depend on them. Each task names its blocker.

**D-1 — how M4 gets a working SysId path.** Blocks task 1.

| Option | Shape | Cost |
|---|---|---|
| **A. Route the device output into the plant** | `step()` reads the `TalonFX`'s applied voltage through `simState` instead of a cached `commandedVolts`; add a "characterizing" flag that suspends `updateFlywheel()`/`updateArm()`; add a `Subsystem` + controller + `CommandScheduler.run()` | Highest fidelity, closest to the real robot, most new given code at L1 |
| **B. Give SysId its own harness entry point** | Keep the plants as they are; add a given `CharacterizationHarness` that sweeps voltage through `plant.setVoltage()` and writes a CSV the WPILib SysId analyser can ingest | Smaller, testable, but diverges from the real SysId workflow the milestone claims to teach |
| **C. Drop SysId from M4's DoD** | Replace with a manual characterization procedure the plants already support (command known voltages, read steady-state velocity, derive kV) | Zero new code; loses "stop guessing gains — run the tool," which is the milestone's headline |

Recommendation: **A**, with the scheduler and a single `Subsystem` added as given wiring. The
milestone already promises the real workflow, and B/C both make the promise smaller rather
than making it true. But A adds given code at a fade level (L1) that is supposed to be
thinning, so this is Josh's call, not a worker's.

**D-2 — `Phoenix-2026` visibility. ✅ RESOLVED 2026-08-18 — Josh made the repository public.**
Verified anonymously the same day: repository root, the pinned tree
`a4273829a8f64fda90c559e263b0f79164d94bb9`, and `commands/DriveToPose.java` all return 200,
and a credential-free `git clone https://…` followed by `git checkout <sha>` succeeds and
yields the 22 Java files M3's prose claims.

**Consequences.** M3's reading assignment needed no edit — the clone command was always
correct, the repository simply was not reachable. Task 8 is therefore **deleted** and its
one surviving item, narrowing `.lycheeignore`, folded into task 5, which already owned that
file. This also makes the narrowing far more valuable than originally scoped: with both
`Robot-2026` and `Phoenix-2026` public, the whole `https://github\.com/IgniteRobotics/.*`
blanket can go, and every pinned-SHA reading URL in the repository becomes CI-checkable for
the first time — which is exactly the re-pin drift teaching-repo-spec §16 item 2 schedules a
manual seasonal pass for.

**D-3 — the three non-curriculum docs in the site nav** (finding P3). Options: leave them
(they are cited from `resources.md`, M4, M5, M8); move them under a "Reference / For
Mentors" nav section; or add an audience banner to each. Blocks the optional step in task 5.
Not a defect — do nothing is a legitimate answer.

**D-4 — queue capacity** (finding P2). `UNTESTED-TARGETS.md` holds three targets and
`FEATURE-BACKLOG.md` one, against a cohort of ~15. Task 7 records the gap and proposes a
cadence; it cannot invent targets. Someone has to sit with `Robot-2026` and add them.

## Task graph

| Task | File | Depends on | Touches | Lane |
|---|---|---|---|---|
| 1 | `01-m4-simulator-loop.md` | D-1 | `projects/m4-control/src/**` | m4 |
| 2 | `02-m4-prose-and-dod.md` | 1 | `projects/m4-control/README.md`, `docs/.../m4-*.md` | m4 |
| 3 | `03-first-hour-setup.md` | — | `docs/SETUP.md`, `docs/programming/git-workflow.md`, `.gitattributes` | student-docs |
| 4 | `04-ci-truth.md` | — | `docs/CONTRIBUTING.md`, `docs/.../m0-tooling-and-git.md` | student-docs |
| 5 | `05-link-and-render-gates.md` | D-3 (optional step only) | `.github/workflows/docs-links.yml`, `.lycheeignore`, `tools/site_guard.py`, `tests/test_site_guard.py`, `mkdocs.yml` | gates |
| 6 | `06-m6-logging-dod.md` | — | `projects/m6-testing/**`, `docs/.../m6-*.md` | content |
| 7 | `07-queue-capacity.md` | D-4 | `docs/programming/UNTESTED-TARGETS.md`, `projects/m7-ai-assisted/**` | content |
| ~~8~~ | ~~`08-phoenix-access.md`~~ | — | — | **deleted — D-2 resolved upstream; residue folded into task 5** |
| 9 | `99-integration.md` | all | `tests.lock`, `docs/programming/README.md`, both specs, prerequisite ledger | integration |

**Lanes:** `m4` (1→2, sequential), `student-docs` (3→4, sequential), `gates` (5),
`content` (6→7, sequential).

**Maximum useful concurrency: 4.** **Realistic: 3.** Tasks 5 and 7 are each under an hour of
prose and configuration; a dedicated agent per lane costs more coordination than it saves.
Suggested split:

- **Agent 1** — lane `m4` (tasks 1, 2). The only lane with real code and the only one running
  Gradle.
- **Agent 2** — lane `student-docs` (tasks 3, 4). The highest-value lane and the one most
  worth reviewing carefully.
- **Agent 3** — lanes `gates` and `content` (tasks 5, 6, 7) in that order. Only task 6
  touches Gradle, against a different project directory than agent 1.

With task 8 gone, `.lycheeignore` now has exactly one owner and the cross-task coordination
note that used to sit in task 5 step 3 is retired.

**Serialized tail:** task 9, single owner, after every lane has landed.

## Constraints delta

`CLAUDE.md` loads automatically and carries the toolchain, pinned versions, spotless wiring,
package naming, fade curve, authoring rules, the clean-clone invariant, and the verification
discipline. Read it. Only what is specific to *this* plan is restated here:

- **This plan bumps nothing.** GradleRIO, the wrapper, Spotless, JUnit, and every vendordep
  stay exactly where they are. If a task seems to need a bump, stop and say so.
- **M4 stays L1.** Task 1 adds given wiring. Every line it adds must be wiring the student
  was never going to write — scheduler, subsystem shell, controller, a characterization
  seam. It must not add feedforward, PID, a profile, or a homing implementation, and it
  must not put a gain anywhere, including in a comment or a test.
- **No task below may touch `tests.lock`, `docs/programming/README.md`, or
  `docs/programming/teaching-repo-spec.md`.** Those are contended and belong to task 9.
  `docs/programming/curriculum-spec.md` joins them for this plan — task 9 owns it too.
- **Task 1 changes files under `plant/` and `harness/`, which are in `tests.lock`.** That is
  expected and correct here; the manifest is regenerated once, by task 9. Do not run
  `tests_lock.py write` from a lane.
- **Every prose fix must be verified against behaviour, not against the spec.** Several of
  these findings exist because a document was checked against another document. If a task
  says "state what CI does," run the tool and read the output first.

## Self-review

**The thing most likely to go wrong** is task 1 under option D-1(A): adding a scheduler, a
subsystem, and a controller to M4 is enough new given surface to quietly change the
milestone's fade level. The task carries an explicit budget and a step that re-reads the
diff against §8's L1 definition before committing.

**The second** is that task 1 changes files under `tests.lock` while four other lanes run.
Nothing else touches `projects/m4-control/`, so the only conflict surface is the manifest —
which is why no lane regenerates it.

**Deliberately not fixed here:** finding P1 (M4 being the real complexity cliff, not M3).
Tasks 1 and 2 make M4 completable as written; whether M4 should be split, or M3 should carry
more of the load, is a curriculum question and belongs in a design session, not a
remediation plan.
