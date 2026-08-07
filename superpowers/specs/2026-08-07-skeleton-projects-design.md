# Skeleton Projects, Git Ritual & the Real-Repo Ladder — Design

**Status:** Approved design, not yet executed
**Owner:** Josh Reddick
**Created:** 2026-08-07
**Team:** FRC 6829 Ignite Robotics
**Parent:** [`programming/teaching-repo-spec.md`](../../programming/teaching-repo-spec.md)

---

## 0. Relationship to `teaching-repo-spec.md`

`teaching-repo-spec.md` is uncommitted work-in-progress. This design **overrides three of its decisions** and answers two questions it left open.

| Spec item | Said | This design |
|---|---|---|
| **D1** | Skeletons live in a new `ignite-training` repository | **Overridden.** They live in this repository (`IgniteRobotics/docs`), at `projects/`, beside the prose that describes them |
| **D2** | "Template repository + fork/PR" — never stated fork *what*, or PR *to where* | **Resolved.** Fork `docs` once at M0; branch per milestone; PR into your **own fork**. Upstream receives exactly one student PR, ever |
| **§4.2** | Multi-project GradleRIO build is the load-bearing unknown; spike it first; standalone projects are the fallback | **Obviated.** The repo root is a documentation site, not a Gradle root. Standalone-per-milestone is the only option, which was already the fallback. The spike is deleted |
| **§6.7** | M6 ships `LaunchProfile.java`, a Robot-2026 lookalike, "so the exercise does not depend on a second repo building" | **Overridden.** That becomes the warm-up. The M6 **deliverable is a real PR** adding `src/test` to Robot-2026 |
| **§13** open item | Does `./gradlew build` run tests under GradleRIO? | Near-certain yes (standard Gradle `build → check → test` wiring). **Still verified empirically in step 0** — `wpi.java.configureTestTasks` could in principle alter it |
| *(not addressed)* | — | **New:** GitHub Pages publication, and the four-workflow CI topology it requires |
| *(not addressed)* | — | **New:** the per-milestone git ritual and real-repo contact ladder — §3–§5 below |

**Unchanged and still in force:** D3 (tests cover mechanical DoD items only), D4 (local toolchain, no devcontainer), D5 (shared skeletons, per-track test-out fixtures), D6 (private solutions repo), the §5 scaffold-fade curve, the §6 per-milestone inventories, the §7 test-out fixtures, and the §9 anti-goals and tamper-evidence requirements.

### 0.1 The fold-back task

These two documents disagree until `teaching-repo-spec.md` is amended. That is a deliberate, accepted, **temporary** state.

Mitigations, both required:

1. A supersession banner at the top of `teaching-repo-spec.md` pointing here, added at the same time as this document — so a reader cannot encounter the stale decisions without seeing that they are stale.
2. The fold is **step 12** in §8, not an aspiration.

---

## 1. Problem

The curriculum is written; the code it assumes does not exist. `teaching-repo-spec.md` addresses that, but leaves two questions unanswered — and they are the two a student actually asks:

1. **What do I do with git, at each milestone?** M0 says "fork *or* clone the teaching repository" and never picks. Nothing states the ritual for M1–M8 at all.
2. **When do I stop working in a sandbox and touch the real robot code?** The touchpoints exist across M1–M8 but are scattered, unlabeled, and inconsistent in kind — some read-only, some a real PR, some in-season with commit access. No document states the rule.

There is also an unstated hard constraint the docs never connect: **the Kickoff Readiness Gate withholds commit access to `Robot-2026` until M0–M3 are cleared.** Everything before that point is fork-and-PR-in by definition, whether or not anyone wrote it down.

---

## 2. Repository layout

One repository. It publishes the curriculum as a site *and* holds the code the curriculum describes.

```
docs/                            IgniteRobotics/docs — published to GitHub Pages
├── 00-index.md                  site nav — lists ONLY prose
├── VISION_GUIDE.md              ┐
├── vision-subsystem.md          │ rendered
├── field-boundary-speed-limiting.md
├── programming/
│   ├── README.md                │
│   ├── curriculum-spec.md       │
│   ├── teaching-repo-spec.md    │ rendered
│   ├── ai-use-standard.md       │
│   ├── student-deliverables.md  │
│   ├── resources.md             │
│   ├── git-workflow.md          ← NEW — the fork/branch/PR ritual (§3)
│   ├── robot-repo-ladder.md     ← NEW — when to touch real code (§4)
│   ├── UNTESTED-TARGETS.md      ← NEW — mentor-maintained M6 queue (§5.2)
│   ├── milestones/m0…m8.md      │ rendered
│   └── tracks/                  ┘
├── projects/                    ← NOT in nav, never rendered
│   ├── m0-onboarding/           each is a STANDALONE Gradle project:
│   ├── m1-java-fundamentals/    own gradlew, own settings.gradle,
│   ├── m2-oop/                  own vendordeps/, no root aggregation
│   ├── m3-command-based/
│   ├── m4-control/
│   ├── m5-auto-vision/
│   ├── m6-testing/
│   ├── m7-ai-assisted/
│   ├── m8-ownership/
│   └── testouts/t1…t5/
├── templates/                   M8 deliverables as real copyable files
├── superpowers/specs/           design records (this file)
└── .github/workflows/           four workflows — §7
```

**Why one repo.** One clone gets a student the curriculum and the code. CI can verify the docs against the code they describe — the failure mode `curriculum-spec.md` §1.2 exists to prevent. And the seasonal re-verification pass has one surface, not two.

**Accepted cost.** A student forking to do M1 also forks every mentor-facing document. This is harmless: those documents are public anyway, the README already directs students to them, and the alternative (splitting the repo) reintroduces the sync problem D1 was written to avoid.

**No root Gradle project.** Each `projects/mN/` carries its own wrapper and `settings.gradle`. Students open the milestone directory as their VS Code workspace, and the WPILib extension behaves exactly as it expects — including the "Simulate Robot Code" button, which a multi-project build would have broken.

---

## 3. The git ritual

### 3.1 The rule

> **Clone what you're reading. Fork what you'll change. Branch to work.**

This belongs on the M0 page, verbatim. Every git question in the curriculum reduces to it.

`docs` is the only thing forked at M0, because it is the only thing the student will change for six milestones. `Robot-2026` is *cloned* at M2 — read-only, no fork, no write. It is not forked until M4, when the student has something to send back.

### 3.2 The mechanics

**M0 — the one upstream PR.**

```
fork IgniteRobotics/docs  →  jsmith/docs
git clone git@github.com:jsmith/docs.git
git remote add upstream git@github.com:IgniteRobotics/docs.git
git checkout -b add-jsmith-contributor
    …create contributors/jsmith.md, run spotlessApply on FormatMe.java…
PR: jsmith/docs:add-jsmith-contributor → IgniteRobotics/docs:main
```

Merged by a mentor. This is the only student PR the canonical repository ever receives, and shipping it *is* M0's stated learning objective. Privacy terms are unchanged from `teaching-repo-spec.md` §6.1: GitHub handle only, self-created, removable by the student's own PR.

**M1–M6 — same fork, forever.**

```
git fetch upstream && git merge upstream/main      ← also the merge command M0 teaches
git checkout -b m3-command-based
    …work in projects/m3-command-based/…
PR: jsmith/docs:m3-command-based → jsmith/docs:main
```

CI runs on the fork. A mentor reviews on the fork. Upstream never sees a student solution.

**M7–M8 — commit access.** Post-Gate, the student has write access to `Robot-2026` and branches directly on it. No fork, no upstream remote. This is how the team works.

### 3.3 The invariant

**Fork→PR before the Kickoff Gate. Branch after.**

The git ritual and the commit-access policy are the same boundary. That is not a coincidence to explain away — it is what makes the Gate legible to a student instead of arbitrary.

### 3.4 Why not the alternatives

| Rejected | Why |
|---|---|
| A fresh fork per milestone | GitHub permits one fork per account per repo. Requires template-generation or manual re-clone, which breaks `fetch upstream` for skeleton updates and re-teaches setup nine times |
| One cumulative branch, PR twice a season | PR review *is* the feedback loop. Batching it means a student can carry a misunderstanding through three milestones before anyone reads their code |
| Write access on the team repo, branch directly | Contradicts the Kickoff Gate outright, and puts every student's in-progress work in the repo that publishes the site |
| Students PR solutions *upstream* | Thirty students' solutions in the canonical repo, and every solution publicly visible to the next cohort |

---

## 4. The real-repo ladder

### 4.1 Per-milestone matrix

| | Sandbox project | Git ritual | Real repo | Contact |
|---|---|---|---|---|
| **M0** | `projects/m0-onboarding/` | fork `docs` → branch → **PR upstream** | — | none |
| **M1** | `m1-java-fundamentals/` | branch → PR into own fork | Robot-2026 | **browse** on github.com — `IntakeConstants.java`, `Constants.java` |
| **M2** | `m2-oop/` | branch → PR into own fork | Robot-2026 | **clone** — first real clone. `preferences/`, `LaunchState`, `LaunchRequestBuilder` |
| **M3** | `m3-command-based/` | branch → PR into own fork | Phoenix-2026, Robot-2026 | **clone** — Phoenix end-to-end, then `RobotContainer` |
| **M4** | `m4-control/` | branch → PR into own fork | Robot-2026 | **clone** to read `ShooterSubsystem`; **fork → PR upstream** for the stretch |
| **M5** | `m5-auto-vision/` | branch → PR into own fork | Robot-2026 | **clone** — `vision/`, `LaunchCalculator` |
| **M6** | `m6-testing/` *(warm-up)* | branch → PR into own fork | Robot-2026 | **fork → PR upstream.** Tests for an untested class. **This is the DoD item** |
| **M7** | — | **branch on upstream** | Robot-2026 | commit access. Ship a feature |
| **M8** | — | **branch on upstream** | Robot-2026 | commit access, in-season. Own a subsystem |

The four contact levels — **browse → clone → fork+PR → branch** — are deliberately monotonic. A student never moves backward, and each step is a strictly larger permission than the last.

### 4.2 Why M4 and M6 and not earlier

**Timing makes it viable.** Verified 2026-08-07: `Robot-2026` has had 2 commits in 90 days (last: 2026-06-09); `Phoenix-2026` none since 2026-02-10. Students working August→December fork a stable target. That window closes at kickoff, which is precisely why the ladder puts the fork-and-PR milestones before the Gate and the commit-access milestones after it.

**M4 is the rehearsal, M6 is the graded run.** The `DriveMotorConfigs.clone()` refactor is small, safe, real, and already specified as a PR. It is a student's first contribution to competition code and the only practice before M6's PR is assessed.

**M6 is where the sandbox stops being honest.** `teaching-repo-spec.md` §6.7 ships a Robot-2026 lookalike to keep the exercise self-contained — defensible, but a copy. Meanwhile M6's own prose names `LaunchCalculator` as "the best test target in the codebase" and the repository has zero tests across three seasons. The sandbox exercise teaches the mechanics; the real PR closes an actual gap and leaves something the team keeps.

### 4.3 Everything before M4 is read-only, on purpose

M1–M3 are Red and early-Yellow tier. A student who has not yet cleared M3 cannot evaluate whether a change to competition code is safe, and the milestones' value from `Robot-2026` is entirely in the reading: naming inconsistency (M1), `Supplier<T>` doing real work (M2), the two command idioms (M3). None of that requires write access, and granting it early would make the Gate meaningless.

---

## 5. Three consequences

### 5.1 M6 looks circular and is not

The Gate requires M6 before commit access to `Robot-2026`. M6's deliverable is a PR to `Robot-2026`. This resolves because **the PR comes from a fork**, which needs no commit access — which is exactly why fork→PR must be the pre-Gate ritual.

State this outright in `m6-simulation-telemetry-testing.md` and in the mentor notes. A sharp student will spot the apparent contradiction, and a mentor should have the answer ready rather than improvising it.

### 5.2 "The first `src/test`" expires after one student

Track D's pitch and M6's prose both lean on *zero test files across three seasons*. Student #1 lands the PR in October and that framing is dead — students #2–15 are adding to an existing suite.

**Fix:** `programming/UNTESTED-TARGETS.md`, mentor-maintained, listing which of `Robot-2026`'s 47 Java files still have no coverage, checked off as PRs land. Turns a one-shot bragging right into a queue. Same maintenance shape as M7's `FEATURE-BACKLOG.md` — and the same owner problem, which `teaching-repo-spec.md` §13 already flags as unassigned.

Milestone prose changes from "the first tests" to "tests for a class that has none," which stays true all season.

### 5.3 The M4 stretch is optional and shouldn't entirely be

The refactor may stay optional. But **"you have opened one PR against a real robot repo" becomes a required M4 DoD item** — a doc fix or a typo correction counts. The skill being rehearsed is the ritual, not the refactor, and M6 is a bad place to attempt it for the first time.

---

## 6. The hazard, and the rule that contains it

`Robot-2026`'s `pr-build-check.yml` runs `./gradlew build`. Gradle's Java plugin wires `build → check → test`. **The team's CI already gates on tests — vacuously, because there is no `src/test` directory.**

Two consequences, one good and one dangerous.

**Good, and it is the strongest sentence available for M6 and Track D:** the M6 PR does not merely add tests. It **switches on test gating for every team PR from that moment forward.** Neither `m6-simulation-telemetry-testing.md` nor `track-d-frc-veteran-ai.md` currently makes this point; both should.

**Dangerous:** one flaky student test blocks every team PR in February.

> **Hard rule: M6 real-repo tests target pure logic only.**
> `LaunchCalculator`, the two `LaunchRequestBuilder` implementations, unit conversions, state-machine transitions. **No timing, no simulation, no hardware.**
> `PhoenixTestSupport.java` and its `~100ms` / `~20ms` delays stay in `projects/m6-testing/`, where a flake costs one student an afternoon instead of costing the team a competition PR.

This is a rule, not a guideline, and it belongs in `CONTRIBUTING.md` and in the M6 milestone file.

**Correction owed to three documents.** `curriculum-spec.md` §2.2, `tracks/track-d-frc-veteran-ai.md`, and `milestones/m6-simulation-telemetry-testing.md` all state "Neither CI workflow runs `./gradlew test`." Literally true, materially misleading: the harness is already wired. Ignite's starting position is better than the docs claim, and the M6 argument gets stronger, not weaker.

---

## 7. Pages and CI

### 7.1 Publication

Built with **`doc-builder`** (`IgniteRobotics/doc-builder`, v2.0.0 — MkDocs + Material, installable CLI, last updated 2026-03-20). Dogfooding the team's own tool on the team's own documentation is how it stays alive.

`doc-builder` is index-driven via `00-index.md`, which solves the central layout problem for free: only listed prose is rendered, so Java, Gradle, and vendordep files under `projects/` are never candidates for publication. No exclusion globs to maintain.

> **Note.** `IgniteRobotics/docs` has **no git remote configured** as of 2026-08-07. Creating it is a prerequisite to every workflow below.

### 7.2 Four workflows, running in three places

| Workflow | Runs where | Trigger | Does |
|---|---|---|---|
| `pages.yml` | canonical only | push `main`, `paths: ['**.md', '00-index.md']` | `doc-builder` → deploy Pages |
| `skeleton-integrity.yml` | canonical only | PR touching `projects/**` | `assemble` + `spotlessCheck` + `tests.lock` + inverted check |
| `student-check.yml` | **student forks** | `pull_request` | `build` + `spotlessCheck` + `tests.lock`, changed milestone only |
| `docs-links.yml` | canonical only | weekly + PR on `**.md` | link check |

Gate the canonical-only workflows with `if: github.repository == 'IgniteRobotics/docs'`. That skips **grey**, not red — a student's fork does not sprout three broken checks on day one.

`student-check.yml` is modeled on `Robot-2026/.github/workflows/pr-build-check.yml`, which already does build + format + PR-comment well: JDK 17 Temurin, Gradle cache, `./gradlew build --console=plain`, `spotlessCheck` with `continue-on-error` and a `github-script` comment step.

The **inverted check** in `skeleton-integrity.yml` is unchanged from `teaching-repo-spec.md` §4.5 and remains essential: a freshly cloned skeleton's given tests must **fail**. A skeleton whose tests pass out of the box has a stub that accidentally implements itself — a real and silent authoring bug.

`docs-links.yml` is new and cheap. Building this curriculum found two already-stale URLs inside research written five months earlier; the seasonal re-verification pass should not be the first line of defense.

### 7.3 Five mechanics that will otherwise bite

1. **Path filters make one repo tolerable.** A mentor fixing a typo in `m3-wpilib-command-based.md` must not trigger nine Gradle builds. A student pushing to `projects/m1/` must not trigger a Pages deploy. Without filters, colocation becomes its own tax.

2. **Derive the student matrix from the diff.** `student-check.yml` builds only the `projects/mN/` the PR touched. A Track A student waiting out nine project builds is a bad first impression of the first CI they have ever seen.

3. **⚠️ Actions are disabled by default on forked repositories.** GitHub requires the fork owner to click through *"I understand my workflows, go ahead and enable them."* If that step is missing from `SETUP.md`, every student's first PR shows **no checks at all** — which reads as broken rather than unconfigured. **This is the single most likely thing to go wrong in M0** and it costs a whole session when it does.

4. **`./gradlew build` runs tests** — see §6. Verified empirically in step 0 regardless, since `wpi.java.configureTestTasks` could in principle alter the wiring.

5. **Fork PRs need no secrets.** `student-check.yml` runs on `pull_request` within the student's own fork; the default `GITHUB_TOKEN` suffices for commenting on their own PR. Never `pull_request_target`.

---

## 8. Build order

**The layout decision deleted the riskiest step.** `teaching-repo-spec.md` §4.2 made multi-project GradleRIO the load-bearing unknown and time-boxed a spike, with standalone projects as the fallback. With `projects/` beside a documentation site there is no Gradle root to aggregate — standalone-per-milestone is the only option, and it was the fallback anyway. The spike is deleted; a 30-minute sanity check replaces it.

| # | Step | Notes |
|---|---|---|
| **0** | Sanity check: one standalone project builds from a clean clone; confirm `build` runs `test` under GradleRIO | 30 minutes, not a spike |
| **1** | **`programming/git-workflow.md` + `programming/robot-repo-ladder.md`** | **Zero dependencies. Ships immediately** |
| **2** | Create the git remote; repo scaffold: `00-index.md`, four workflows, `tests.lock` tooling, `CODEOWNERS`, PR template, `CONTRIBUTING.md`, `SETUP.md` (including the fork-Actions step) | — |
| **3** | Pages live | Proves the pipeline before there is much to publish |
| **4** | **M0** | Smallest; every student hits it |
| **5** | **M1** + T1 | Highest volume; L3 is the most authoring-heavy fade level |
| **6** | **M2** + T2 | — |
| **7** | **M3** + T3 | Largest. Build the `M3Deliverables` harness first, validate against a throwaway solution |
| — | — | **Kickoff Gate block complete. Ship it and stop if time runs out** |
| **8** | **M6** + `UNTESTED-TARGETS.md` | Out of order: it is the M7 gate, and the regression-patch check is novel enough to want proven early |
| **9** | **M4** + T4 | — |
| **10** | **M5** + T5 | T5's three log sets are the largest single authoring cost |
| **11** | **M7**, **M8** templates | Mostly documents |
| **12** | **Fold this design into `teaching-repo-spec.md`**; correct the CI claim in three files; update the README status table | §0.1. Not optional |
| **—** | Solutions repository, built continuously alongside steps 4–11 with the §8.2 cross-check live from step 4 | A milestone shipped without its solution is not shipped |

**Timing.** 2026-08-07, kickoff roughly five months out. Steps 1–7 fit comfortably *if step 1 lands now*. Steps 1–3 are almost entirely writing and configuration — they do not wait on the toolchain being settled.

**If only one thing gets built: step 1.** The two documents make the existing curriculum coherent on their own, whether or not a single skeleton is ever written.

---

## 9. Risks

| Risk | Severity | Mitigation |
|---|---|---|
| The §0.1 fold never happens; two specs disagree indefinitely | **High** | Supersession banner on `teaching-repo-spec.md` at the same commit as this file; fold is step 12, not a wish |
| Fork Actions disabled → M0 shows no CI, reads as broken | **High** | Explicit, screenshotted step in `SETUP.md`; mentor checklist item at M0 |
| A flaky student test blocks team PRs in-season | **High** | §6 hard rule: pure logic only in real-repo tests |
| `UNTESTED-TARGETS.md` goes stale, M6 loses its target list | Medium | Same unassigned-owner problem as `FEATURE-BACKLOG.md`. Assign both to one person |
| Student forks of `Robot-2026` drift once the season starts | Medium | Ladder puts fork-and-PR milestones before kickoff by design; M7/M8 use branches on upstream |
| Skeletons rot against next season's WPILib | Medium | Joins the seasonal re-verification pass — `teaching-repo-spec.md` §12, order preserved: bump, re-pin, cross-check |
| Pages workflow fires on student forks | Low | `if: github.repository ==` gate |

---

## 10. Acceptance criteria

- [ ] `programming/git-workflow.md` states the clone/fork/branch rule and gives copy-pasteable commands for M0 and for M1–M6
- [ ] `programming/robot-repo-ladder.md` contains the §4.1 matrix and explains the M6 non-circularity
- [ ] `teaching-repo-spec.md` carries a supersession banner from the moment this design is committed
- [ ] A student clones `projects/m1-java-fundamentals/`, runs `./gradlew assemble`, and it is green before they write a line
- [ ] A student's fork produces a single green check a mentor can read without opening the code
- [ ] The Pages site renders every prose file and **zero** files from `projects/`
- [ ] `pages.yml`, `skeleton-integrity.yml`, and `docs-links.yml` are skipped — not failed — on a fork
- [ ] `SETUP.md` documents enabling Actions on a fork, with a screenshot
- [ ] Every M4 student has opened at least one PR against a real robot repository
- [ ] `UNTESTED-TARGETS.md` exists, has an owner, and is checked off as M6 PRs land
- [ ] No real-repo test introduced by a student depends on timing, simulation, or hardware
- [ ] `teaching-repo-spec.md` is amended and the banner removed (step 12)

---

## 11. Verified facts

Checked directly on 2026-08-07 against `~/workspace/ignite/`:

| Claim | Value |
|---|---|
| `Robot-2026` Java files | 47 |
| `Robot-2026` `src/test` | **absent** |
| `Robot-2026` logs | 274 |
| `Robot-2026` activity | 2 commits in 90 days; last 2026-06-09 |
| `Robot-2026` workflows | `pr-build-check.yml`, `ai-code-review.yml` |
| `Phoenix-2026` Java files | 22 |
| `Phoenix-2026` `src/test` | **absent** |
| `Phoenix-2026` activity | 0 commits in 90 days; last 2026-02-10 |
| `doc-builder` | v2.0.0, MkDocs + Material, `IgniteRobotics/doc-builder`, last 2026-03-20 |
| `docs` git remote | **none configured** |

Unverified and deferred to step 0: whether `./gradlew build` invokes `test` under GradleRIO's `wpi.java.configureTestTasks`.
