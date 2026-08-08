# Ignite Robotics — Teaching Repository Spec

**Status:** Approved spec, not yet executed
**Owner:** Josh Reddick
**Created:** 2026-08-06 · **Revised:** 2026-08-07
**Team:** FRC 6829 Ignite Robotics
**Parent:** [`curriculum-spec.md`](curriculum-spec.md) — this document executes the "Teaching repository for M0–M3 projects: **Not built**" line in that spec's status table, and extends it to all nine milestones.

---

## 1. Purpose

The curriculum is written. Every milestone specifies a project. **None of those projects has code to start from.**

Today a student who reaches M1 is told "write six robot-flavored exercises, each ships with assertions that must pass" — and no such exercises exist. A student at M4 is told "characterize a flywheel with SysId in simulation" and must first build a simulated flywheel. A mentor running an M5 test-out is told "give the student an autonomous routine that drifts" and must author one on the spot.

The curriculum also leaves two questions unanswered that every student asks in their first week:

1. **What do I do with git, at each milestone?** M0 said "fork *or* clone the teaching repository" and never picked. Nothing stated the ritual for M1–M8 at all.
2. **When do I stop working in a sandbox and touch the real robot code?** The touchpoints exist across M1–M8 but were scattered, unlabeled, and inconsistent in kind — some read-only, some a real pull request, some in-season with commit access.

This spec defines the **skeleton source code** that closes the first gap, the **git ritual** (§5) and **real-repo ladder** (§6) that close the other two, and the CI and publication pipeline that verifies all of it.

### 1.1 Scope

**In scope.** Per-milestone skeleton projects and per-track test-out fixtures living in this repository; the git ritual students follow; the rule for when work moves to `Robot-2026`; the CI that verifies the skeletons; the GitHub Pages pipeline that publishes the curriculum; and a private companion repository holding reference solutions.

**Out of scope.** Rewriting `CommandTemplate` (still excluded per `curriculum-spec.md` §8, still the highest-leverage separate fix). Any tracking site, roster system, or LMS. Changes to the curriculum prose beyond the corrections §6.5 and §7.3 identify — this spec adds code and process the existing documents already assume.

### 1.2 What this is not

It is not a course-in-a-box that replaces mentors. Every milestone retains explanation-level Definition-of-Done items that only a human can verify, and this spec deliberately does not try to automate them. The skeletons remove setup friction and make the mechanical half of each DoD objectively checkable. That is all.

### 1.3 Revision history

**2026-08-07.** Merged in the skeleton-projects design session. Changes: skeletons moved from a proposed `ignite-training` repository into this one (D1); the git ritual specified end to end (D2, §5); the real-repo ladder added (D8, §6); M6's deliverable changed from a sandbox exercise to a real pull request (D9, §9.7); GitHub Pages publication added (D7, §7); the multi-project GradleRIO spike deleted as obviated (§4.2); build order revised (§13). The originating design document is preserved at `../superpowers/specs/2026-08-07-skeleton-projects-design.md`.

---

## 2. Research basis

Six searches across peer FRC practice, CS-education literature, and the GitHub/WPILib tooling surface. The finding that shapes everything below: **no FRC team has published a milestone-ladder skeleton set.** Building one puts Ignite past the state of the art, which is the same position the curriculum documents already occupy.

### 2.1 What peer teams actually do

| Source | What it is | What we take |
|---|---|---|
| [FRC Java Koans (Team 3550)](https://github.com/jletroui/FrcJavaKoans) | The only mature model found. A single Java project where a "master" runner walks sequential exercises, printing expected-vs-actual until each passes. Progression is gated — completing a koan reveals the next | The **stub-plus-given-assertion** shape for M1, and the principle that feedback is expected-vs-actual, not pass/fail |
| [Team 8744 — FRC_2026_SoftwareLessons](https://github.com/FRC-Team8744/FRC_2026_SoftwareLessons) | Cited in `curriculum-spec.md` §4.3 as an M1 resource. Inspecting it: markdown lesson outlines and visuals folders, **no starter code, no tests, no Gradle project.** Self-described as "just an outline of the season lessons for now" | A caution. Our own M1 resource link points at a repo that has the same gap this spec exists to close — worth knowing before recommending it to a student |
| [SciGuides (Team 1155)](https://github.com/SciBorgs/SciGuides) | "Public projects and lessons for teaching members." Has a `projects/` directory alongside `reference-sheets/` and `troubleshooting-logs/` | The idea of shipping **troubleshooting logs** as a first-class artifact next to the code |
| [FRC 1099 DiscoTechs training](https://sites.google.com/student.brookfieldps.org/discotechs/frc-java-programming-training) | Already the model for the curriculum's track architecture (`curriculum-spec.md` §2) | Nothing new — their published material is lesson-level, not code-level |
| [2026 KitBot Java Software Guide](https://firstfrc.blob.core.windows.net/frc2026/KitBot/2026-kitbot-java-guide.pdf) | Official FIRST walkthrough of a complete, working, minimal robot project | The **read-a-whole-small-thing-first** pattern, which M3 already uses with `Phoenix-2026` |

### 2.2 Tooling

- **GitHub Classroom has a working Java/Gradle/JUnit autograding path** ([classroom-resources/autograding-example-java](https://github.com/classroom-resources/autograding-example-java)), with tests re-running on every push. **Not adopted** — see decision D2.
- **Zero-install is technically viable.** `wpilib/roborio-cross-ubuntu:2026-22.04-py314` is current ([wpilibsuite/docker-images](https://github.com/wpilibsuite/docker-images)), and Codespaces-for-FRC is a documented community pattern ([Chief Delphi](https://www.chiefdelphi.com/t/github-codespaces-programming-in-your-browser-for-frc-2023-update/423814), [Evan Pratten](https://ewpratten.com/blog/codespaces-for-frc/)). **Not adopted** — see decision D4.
- **Simulation runs headless from Gradle**: `./gradlew simulateJava` ([WPILib robot simulation docs](https://docs.wpilib.org/en/stable/docs/software/wpilib-tools/robot-simulation/introduction.html)), which is what makes CI-verified simulation projects possible at all.

### 2.3 The CS-education finding that sets the scaffold curve

Faded worked examples come out of cognitive load theory and are established in mathematics, engineering, and psychology. In programming specifically, the strongest result is that **faded worked examples combined with metacognitive scaffolding beat both constant scaffolding and none** ([Shin, Jung, Zumbach & Yi, 2023](https://journals.sagepub.com/doi/abs/10.1177/07356331231174454)). Separately, novices "welcome additional scaffolds at the outset" and that need "tended to fade as they gained familiarity with programming terminology and constructs" ([fade-in vs. fade-out scaffolding, *J. Computing in Higher Education*](https://link.springer.com/article/10.1007/s12528-021-09307-w)).

**Consequence for this spec:** skeleton density is not uniform. It is a deliberate decreasing curve across the ladder — §8. A student who gets the same amount of help in M6 as in M1 has been failed by the scaffolding, not served by it.

The metacognitive half of that finding is already satisfied by the curriculum's existing **Common failure modes** sections. This spec adds nothing there and should not.

---

## 3. Decisions

Eleven decisions, all made, recorded with reasoning so they can be argued with rather than rediscovered.

| # | Decision | Reasoning |
|---|---|---|
| **D1** | **Skeletons live in this repository**, at `projects/m0-onboarding/` … `projects/m8-ownership/` and `projects/testouts/`, a sibling of `docs/` rather than of `programming/` | One clone gets a student the curriculum and the code. CI can verify the docs against the code they describe — the drift failure mode `curriculum-spec.md` §1.2 exists to prevent. One seasonal re-verification surface, not two. A separate `ignite-training` repo was the original proposal and was rejected: it reintroduces exactly the sync problem it was meant to avoid. Accepted cost: a student forking to do M1 also forks every mentor-facing document, which is harmless — those documents are public, and the README already sends students to them |
| **D2** | **Fork this repository once at M0. Branch per milestone. Pull request into your own fork.** Upstream receives exactly one student PR, ever | Mentor review and CI happen on the student's fork; the canonical repository never accumulates thirty students' solutions, and no cohort can read the previous cohort's answers. The single upstream PR is M0's contributor file, because shipping a real PR past CI **is** M0's stated learning objective. GitHub Classroom's autograding is genuinely better verification, but it is a roster-linked tracking system, and `curriculum-spec.md` §8 excluded tracking systems after a peer resource drew real criticism for collecting student names and emails with no deletion path. Reversing that needs a privacy review, not a convenience argument. Full mechanics in §5 |
| **D3** | **Tests enforce the mechanical Definition-of-Done items only** | A JUnit assertion can prove a deadband function clamps correctly. It cannot prove a student can explain why `1/2` is `0`. Wiring CI to "check" the explanation items would produce a green check that means less than the mentor conversation it replaced. The test suite is the floor; the mentor conversation remains the ceiling. Every milestone's DoD is split explicitly in §9 |
| **D4** | **Local WPILib toolchain only. No devcontainer, no Codespaces** | Installing the toolchain and getting `./gradlew build` green **is** M0's stated learning objective. A devcontainer lets a student skip the thing M0 exists to teach. Revisit only if a real student is genuinely blocked by a locked-down machine — at which point it is a one-off accommodation, not a shipped artifact |
| **D5** | **Shared milestone skeletons + per-track test-out fixtures** | The curriculum is "one spine, four entry points" — tracks differ by *where you get on*, not what you learn. Per-track skeleton variants would be up to 36 artifacts duplicating the same content four ways, which is exactly the drift `curriculum-spec.md` §2 rejected. The track axis surfaces where it actually exists: the test-out fixtures, which are the load-bearing mechanism for Tracks B, C, and D |
| **D6** | **Reference solutions live in a separate private repository** | `ignite-training-solutions`, mentor-access only. A `solutions/` directory in the student repo relies on honor, and Red-tier students under time pressure are precisely the population that will look. Accepted cost: two repos to keep in sync, mitigated by the cross-check CI in §11.2 |
| **D7** | **This repository publishes to GitHub Pages via MkDocs Material**, built directly from an explicit `nav:` in `mkdocs.yml` | The curriculum is a document set that students read on a phone in the shop, not a folder they browse on github.com. Prose lives in `docs/`, which is `docs_dir` (D10) — MkDocs can read nothing outside it, so `projects/` and `superpowers/` are not excluded from publication, they are invisible to the builder. That is a boundary, not a rule: the old staging-script approach could be forgotten or skipped on some future change; a boundary can't be crossed by accident. `tools/site_guard.py` still verifies the result — every required page rendered, nothing forbidden leaked through — because a boundary proves what the builder can't see, not what actually got published |
| **D8** | **A four-level real-repo contact ladder: browse → clone → fork+PR → branch on upstream**, with the fork/branch boundary set at the Kickoff Readiness Gate | The curriculum already withholds commit access to `Robot-2026` until M0–M3 clear, which means everything before that point is fork-and-PR by definition. Making the git ritual and the commit-access policy the same boundary is what makes the Gate legible to a student rather than arbitrary. Full matrix in §6 |
| **D9** | **M6's deliverable is a real pull request** adding `src/test` to `Robot-2026`, not a sandbox exercise | The sandbox version was a `Robot-2026` lookalike, chosen so the exercise would not depend on a second repository building. Defensible, but it is a copy — and M6's own prose names `LaunchCalculator` as "the best test target in the codebase" while the repository has zero tests across three seasons. The sandbox project remains as the warm-up; the real PR is the DoD item, and it leaves the team something it keeps. Consequences and the hard rule this forces are in §6.5 |
| **D10** | **Prose lives in `docs/`, not at the repository root** | Forced, not chosen. MkDocs rejects `docs_dir: .` outright: *"The 'docs_dir' should not be the parent directory of the config file. Use a child directory instead."* Nesting prose one level down is the only configuration MkDocs will build, and it is also what makes D7's boundary property hold — the builder needs a directory it structurally cannot see past, and the repository root isn't one |
| **D11** | **The repository is `IgniteRobotics/ignition-labs`**, not `IgniteRobotics/docs` | Once prose moved into a `docs/` subdirectory (D10), a repository also named `docs` made every path ambiguous between "the repository" and "the folder inside it." `ignition-labs` names the product rather than a directory, and matches `site_name` in `mkdocs.yml` |

---

## 4. Repository architecture

### 4.1 Layout

```
ignition-labs/                   IgniteRobotics/ignition-labs — the repository
├── docs/                        docs_dir — the ONLY directory MkDocs can read from
│   ├── index.md                 ┐
│   ├── SETUP.md                 │ rendered — toolchain install, verified per OS
│   ├── CONTRIBUTING.md          │ rendered — fork → branch → PR → review ritual
│   ├── VISION_GUIDE.md          │
│   ├── vision-subsystem.md      │ rendered
│   ├── field-boundary-speed-limiting.md
│   └── programming/             ┘
│       ├── README.md            │
│       ├── curriculum-spec.md   │
│       ├── teaching-repo-spec.md│ rendered
│       ├── ai-use-standard.md   │
│       ├── student-deliverables.md
│       ├── resources.md         │
│       ├── git-workflow.md      ← the §5 ritual, student-facing
│       ├── robot-repo-ladder.md ← the §6 matrix, student-facing
│       ├── UNTESTED-TARGETS.md  ← mentor-maintained M6 queue (§6.5)
│       ├── milestones/m0…m8.md  │ rendered
│       └── tracks/              ┘
├── projects/                    a sibling of docs/, not inside it — unpublishable,
│   ├── m0-onboarding/           because MkDocs cannot read outside docs_dir at all.
│   ├── m1-java-fundamentals/    each is a STANDALONE Gradle project: own gradlew,
│   ├── m2-oop/                  own settings.gradle, own vendordeps/, no root
│   ├── m3-command-based/        aggregation
│   ├── m4-control/
│   ├── m5-auto-vision/
│   ├── m6-testing/
│   ├── m7-ai-assisted/
│   ├── m8-ownership/
│   └── testouts/
│       ├── t1-find-the-bug/
│       ├── t2-supplier-swap/
│       ├── t3-scheduler-conflict/
│       ├── t4-bad-kv/
│       └── t5-drifting-auto/
├── templates/                   M8 deliverables as real copyable files — unpublishable, same reason
│   ├── SUBSYSTEM-README.md
│   ├── TUNING-LOG.md
│   └── PIT-DOC.md
├── contributors/                M0 output, one file per student handle — unpublishable, same reason
├── tools/                       verification scripts the CI workflows call, incl. site_guard.py
├── tests/                       tests for tools/
├── superpowers/specs/           design records — unpublishable, same reason
├── tests.lock                   SHA-256 manifest of every given test file
├── mkdocs.yml                   docs_dir: docs, pinned MkDocs Material, explicit nav: — §7.1
└── .github/
    ├── workflows/               four workflows — §7.2
    ├── PULL_REQUEST_TEMPLATE.md includes the AI-use disclosure block
    └── CODEOWNERS               given test files require mentor review
```

Everything outside `docs/` is unpublishable in the structural sense D7 and D10 describe: not filtered, excluded, or ignored by convention, but outside the one directory the builder is configured to read.

### 4.2 Standalone projects, no Gradle root

Each `projects/mN/` carries its own `gradlew`, its own `settings.gradle`, and its own `vendordeps/`. There is no root aggregation and no root `build.gradle`.

This was originally the *fallback* to a multi-project GradleRIO build, and that build was flagged as the spec's one load-bearing technical unknown, to be spiked before anything else. **D1 obviates the spike.** The repository root is not a Gradle project — it builds a documentation site from `docs/`, and sits alongside `projects/`, `tools/`, and `tests/` as siblings, with no `build.gradle` of its own to aggregate from. Standalone-per-milestone is the only available shape. The risk is gone rather than mitigated.

It is also the better shape on its own merits. GradleRIO is designed for single-project builds; every WPILib example and every peer team repository surveyed is one Gradle project at its root. Students open the milestone directory as their VS Code workspace and the WPILib extension behaves exactly as it expects — including the "Simulate Robot Code" button, which a multi-project build would have broken by assuming a root project.

Cost: a matrix strategy in CI instead of a single build. See §7.3.

### 4.3 Build conventions

Every skeleton project matches the current in-house stack, verified against `Robot-2026` and `Phoenix-2026` on 2026-08-06:

| Setting | Value | Source of truth |
|---|---|---|
| GradleRIO | `2026.2.1` | Both repos |
| Gradle wrapper | `8.11` | Both repos |
| Java | `sourceCompatibility` / `targetCompatibility` 17 | Both repos |
| Spotless plugin | `8.2.1` | `Robot-2026` (newer than `Phoenix-2026`'s 6.25.0) |
| Formatter | `googleJavaFormat('1.19.2').reflowLongStrings()`, `removeUnusedImports()`, `trimTrailingWhitespace()`, `endWithNewline()` | Both repos, identical |
| Spotless enforcement | `enforceCheck false` plus `compileJava.dependsOn 'spotlessApply'` | Both repos |
| JUnit | `org.junit.jupiter:junit-jupiter:5.10.1`, `useJUnitPlatform()`, `wpi.java.configureTestTasks(test)` | Both repos |
| Vendordeps | Minimum set per milestone — see §9. Never "all of them by default" | — |

**Not carried over:** the `com.peterabeles.gversion` plugin and generated `BuildConstants` from `Robot-2026`. Useful in a season repo, noise in a teaching one.

### 4.4 Package naming

| Milestones | Root package | Why |
|---|---|---|
| M1, M2 | `frc.training.m1`, `frc.training.m2` | Pure exercises with no robot. Naming them `frc.robot` would be a lie about what they are, and invites pasting training code into a season repo out of muscle memory |
| M3 onward | `frc.robot` | From M3 the entire point is that the structure matches the real repositories. A student who reads `Phoenix-2026` and then opens `projects/m3-command-based/` should see the same shape, same package, same file names |

---

## 5. The git ritual

### 5.1 The rule

> **Clone what you're reading. Fork what you'll change. Branch to work.**

This belongs on the M0 page verbatim, and it is the whole of `git-workflow.md`'s opening. Every git question in the curriculum reduces to it.

This repository is the only thing a student forks at M0, because it is the only thing they will change for six milestones. `Robot-2026` is *cloned* at M2 — read-only, no fork, no write. It is not forked until M4, when the student finally has something to send back.

### 5.2 M0 — the one upstream pull request

```
fork IgniteRobotics/ignition-labs  →  jsmith/ignition-labs
git clone git@github.com:jsmith/ignition-labs.git
git remote add upstream git@github.com:IgniteRobotics/ignition-labs.git
git checkout -b add-jsmith-contributor
    …create contributors/jsmith.md, run spotlessApply on FormatMe.java…
PR: jsmith/ignition-labs:add-jsmith-contributor → IgniteRobotics/ignition-labs:main
```

Merged by a mentor. This is the only student pull request the canonical repository ever receives, and shipping it *is* M0's stated learning objective.

> **Privacy note.** GitHub handle only — no legal names, no emails, no school. The file is self-created and removable by the student's own PR at any time. This is deliberately *not* a roster: nothing reads it, nothing aggregates it, and the Kickoff Gate checklist stays where `curriculum-spec.md` §3 put it, with a mentor.

### 5.3 M1–M6 — same fork, forever

```
git fetch upstream && git merge upstream/main      ← also the merge command M0 teaches
git checkout -b m3-command-based
    …work in projects/m3-command-based/…
PR: jsmith/ignition-labs:m3-command-based → jsmith/ignition-labs:main
```

CI runs on the fork. A mentor reviews on the fork. Upstream never sees a student solution. Skeleton updates arrive through `fetch upstream` — the same command M0 already teaches, now doing real work.

### 5.4 M7–M8 — commit access

Post-Gate, the student has write access to `Robot-2026` and branches directly on it. No fork, no upstream remote. This is how the team works, and by M7 the student is part of the team.

### 5.5 The invariant

**Fork→PR before the Kickoff Gate. Branch after.**

The git ritual and the commit-access policy are the same boundary. That is not a coincidence to explain away — it is what makes the Gate legible to a student instead of arbitrary.

### 5.6 Why not the alternatives

| Rejected | Why |
|---|---|
| A fresh fork per milestone | GitHub permits one fork per account per repository. Requires template-generation or manual re-clone, which breaks `fetch upstream` for skeleton updates and re-teaches setup nine times instead of once |
| One cumulative branch, PR twice a season | Pull request review **is** the feedback loop. Batching it to twice a season means a student can carry a misunderstanding through three milestones before anyone reads their code |
| Write access on this repository, branch directly | Contradicts the Kickoff Gate outright, and puts every student's in-progress work in the repository that publishes the site |
| Students PR solutions upstream | Thirty students' solutions in the canonical repository, and every solution publicly visible to the next cohort |

---

## 6. The real-repo ladder

### 6.1 The matrix

| | Sandbox project | Git ritual | Real repo | Contact |
|---|---|---|---|---|
| **M0** | `projects/m0-onboarding/` | fork `ignition-labs` → branch → **PR upstream** | — | none |
| **M1** | `m1-java-fundamentals/` | branch → PR into own fork | Robot-2026 | **browse** on github.com — `IntakeConstants.java`, `Constants.java` |
| **M2** | `m2-oop/` | branch → PR into own fork | Robot-2026 | **clone** — first real clone. `preferences/`, `LaunchState`, `LaunchRequestBuilder` |
| **M3** | `m3-command-based/` | branch → PR into own fork | Phoenix-2026, Robot-2026 | **clone** — Phoenix end to end, then `RobotContainer` |
| **M4** | `m4-control/` | branch → PR into own fork | Robot-2026 | **clone** to read `ShooterSubsystem`; **fork → PR upstream** for the stretch |
| **M5** | `m5-auto-vision/` | branch → PR into own fork | Robot-2026 | **clone** — `vision/`, `LaunchCalculator` |
| **M6** | `m6-testing/` *(warm-up)* | branch → PR into own fork | Robot-2026 | **fork → PR upstream.** Tests for an untested class. **This is the DoD item** |
| **M7** | — | **branch on upstream** | Robot-2026 | commit access. Ship a feature |
| **M8** | — | **branch on upstream** | Robot-2026 | commit access, in-season. Own a subsystem |

The four contact levels — **browse → clone → fork+PR → branch** — are deliberately monotonic. A student never moves backward, and each step is a strictly larger permission than the last.

### 6.2 Why M4 and M6, and not earlier

**Timing makes it viable.** Verified 2026-08-07: `Robot-2026` has had 2 commits in 90 days (last 2026-06-09); `Phoenix-2026` none since 2026-02-10. Students working August through December fork a stable target. That window closes at kickoff — which is precisely why the ladder puts the fork-and-PR milestones before the Gate and the commit-access milestones after it.

**M4 is the rehearsal; M6 is the graded run.** The `DriveMotorConfigs.clone()` refactor is small, safe, real, and already specified as a pull request. It is a student's first contribution to competition code and the only practice before M6's PR is assessed.

**M6 is where the sandbox stops being honest.** See D9.

### 6.3 Everything before M4 is read-only, on purpose

M1–M3 are Red and early-Yellow tier. A student who has not cleared M3 cannot evaluate whether a change to competition code is safe, and the value those milestones draw from `Robot-2026` is entirely in the reading: naming inconsistency (M1), `Supplier<T>` doing real work (M2), the two command idioms (M3). None of that requires write access, and granting it early would make the Gate meaningless.

### 6.4 M6 looks circular and is not

The Gate requires M6 before commit access to `Robot-2026`. M6's deliverable is a pull request to `Robot-2026`. This resolves because **the PR comes from a fork**, which needs no commit access — which is exactly why fork→PR must be the pre-Gate ritual.

State this outright in `m6-simulation-telemetry-testing.md` and in the mentor notes. A sharp student will spot the apparent contradiction, and a mentor should have the answer ready rather than improvising it.

### 6.5 The hazard M6 creates, and the rule that contains it

`Robot-2026`'s `pr-build-check.yml` runs `./gradlew build`. Gradle's Java plugin wires `build → check → test`. **The team's CI already gates on tests — vacuously, because there is no `src/test` directory.**

Two consequences, one excellent and one dangerous.

**Excellent, and it is the strongest sentence available for M6 and for Track D:** the M6 pull request does not merely add tests. It **switches on test gating for every team pull request from that moment forward.** Neither `m6-simulation-telemetry-testing.md` nor `tracks/track-d-frc-veteran-ai.md` currently makes this point; both should.

**Dangerous:** one flaky student test blocks every team pull request in February.

> **Hard rule — M6 real-repo tests target pure logic only.**
> `LaunchCalculator`, the two `LaunchRequestBuilder` implementations, unit conversions, state-machine transitions. **No timing, no simulation, no hardware.**
> `PhoenixTestSupport.java` and its `~100ms` / `~20ms` delays stay in `projects/m6-testing/`, where a flake costs one student an afternoon instead of costing the team a competition pull request.

This is a rule, not a guideline. It belongs in `CONTRIBUTING.md` and in the M6 milestone file.

### 6.6 "The first `src/test`" expires after one student

Track D's pitch and M6's prose both lean on *zero test files across three seasons*. Student #1 lands the pull request in October and that framing is dead — students #2–15 are adding to an existing suite.

**Fix:** `programming/UNTESTED-TARGETS.md`, mentor-maintained, listing which of `Robot-2026`'s 47 Java files still have no coverage, checked off as pull requests land. Turns a one-shot bragging right into a queue. Same maintenance shape as M7's `FEATURE-BACKLOG.md` — and the same owner problem, which §16 flags as unassigned.

Milestone prose changes from "the first tests" to "tests for a class that has none," which stays true all season.

### 6.7 M4's stretch stops being entirely optional

The refactor itself may stay optional. But **"you have opened one pull request against a real robot repository" becomes a required M4 Definition-of-Done item** — a documentation fix or a typo correction counts. The skill being rehearsed is the ritual, not the refactor, and M6 is a bad place to attempt it for the first time.

---

## 7. Publication and CI

### 7.1 GitHub Pages

Built with **MkDocs Material** (`mkdocs` 1.6.1, `mkdocs-material` 9.7.7, pinned in `pyproject.toml`'s `docs` dependency group), per D7 and D10.

`docs_dir: docs` in `mkdocs.yml` is what makes publication scope structural rather than enforced. MkDocs reads nothing outside `docs_dir`, so Java, Gradle, and vendordep files under `projects/`, and the design records under `superpowers/`, are never candidates for publication — not because anything filters them out, but because the builder cannot accidentally see them. Every published page is also named explicitly in `mkdocs.yml`'s `nav:` rather than discovered by scanning for markdown files, so a stray file dropped into `docs/` renders but stays out of navigation until someone adds it. `tools/site_guard.py` still verifies the result — every required page rendered, nothing forbidden leaked through — because a structural boundary proves what the builder can't reach, not what a particular build actually produced.

> **Prerequisite.** `IgniteRobotics/ignition-labs` had **no git remote configured** as of 2026-08-07. Creating it blocks every workflow below.

### 7.2 Four workflows, running in three places

This distinction matters and is easy to get wrong.

| Workflow | Runs where | Trigger | Does |
|---|---|---|---|
| `pages.yml` | canonical only | push `main`, `paths: ['docs/**', 'mkdocs.yml', 'tools/site_guard.py', 'pyproject.toml', '.github/workflows/pages.yml']` | `mkdocs build` → `site_guard.py` → deploy Pages |
| `skeleton-integrity.yml` | canonical only | PR touching `projects/**` | `assemble` + `spotlessCheck` + `tests.lock` + inverted check |
| `student-check.yml` | **student forks** | `pull_request` | `build` + `spotlessCheck` + `tests.lock`, changed milestone only |
| `docs-links.yml` | canonical only | weekly + PR on `**.md` | link check |

Gate the canonical-only workflows with `if: github.repository == 'IgniteRobotics/ignition-labs'`. That skips **grey**, not red — a student's fork does not sprout three broken checks on day one.

**`skeleton-integrity.yml`** must **not** run `./gradlew build`. A freshly cloned skeleton is *supposed* to fail its tests — that is the student's target. It runs `assemble` (every skeleton compiles on a clean clone), `spotlessCheck` (every skeleton is already formatted), `tests.lock` verification (§12.2), and an **inverted check** per milestone: the given tests must **fail** on the unmodified skeleton. A skeleton whose tests pass out of the box has a stub that accidentally implements itself, which is a real and silent authoring bug.

**`student-check.yml`** ships in the repository and runs on the student's fork. Model it on `Robot-2026/.github/workflows/pr-build-check.yml`, which already does build-plus-format-plus-comment well: JDK 17 Temurin, Gradle cache, `./gradlew build --console=plain`, then `spotlessCheck` with `continue-on-error` and a `github-script` comment step.

**`docs-links.yml`** is cheap insurance. Building this curriculum found two already-stale URLs inside research written five months earlier; the seasonal re-verification pass should not be the first line of defense.

### 7.3 Five mechanics that will otherwise bite

1. **Path filters are what make one repository tolerable.** A mentor fixing a typo in `m3-wpilib-command-based.md` must not trigger nine Gradle builds. A student pushing to `projects/m1/` must not trigger a Pages deploy. Without filters, colocation becomes its own tax.

2. **Derive the student build matrix from the diff.** `student-check.yml` builds only the `projects/mN/` the pull request touched. A Track A student waiting out nine project builds is a bad first impression of the first CI they have ever seen.

3. **⚠️ Actions are disabled by default on forked repositories.** GitHub requires the fork owner to click through *"I understand my workflows, go ahead and enable them."* If that step is missing from `SETUP.md`, every student's first pull request shows **no checks at all** — which reads as broken rather than unconfigured. **This is the single most likely thing to go wrong in M0**, and it costs a whole session when it does.

4. **`./gradlew build` runs tests** — see §6.5. Verified empirically in step 0 regardless, since `wpi.java.configureTestTasks` could in principle alter the wiring.

5. **Fork pull requests need no secrets.** `student-check.yml` runs on `pull_request` within the student's own fork; the default `GITHUB_TOKEN` suffices for commenting on their own PR. Never `pull_request_target`.

> **Correction owed to three documents.** `curriculum-spec.md` §2.2, `tracks/track-d-frc-veteran-ai.md`, and `milestones/m6-simulation-telemetry-testing.md` all state "Neither CI workflow runs `./gradlew test`." Literally true — neither invokes `test` by name — but `pr-build-check.yml` runs `./gradlew build`, and in the Gradle Java plugin `build` → `check` → `test`. **Ignite's CI already gates on tests; there simply are none to run.** That is a materially better starting position than the docs claim, and the M6 argument gets stronger rather than weaker. Fix all three at build-order step 12.

---

## 8. The scaffold-fade curve

Four density levels, decreasing across the ladder, per §2.3.

| Level | Milestones | What the student is given | What the student writes |
|---|---|---|---|
| **L3 — Stub and fill** | M1 | Class, method signature, and a Javadoc contract stating exact expected behavior. Body is `throw new UnsupportedOperationException("TODO")`. Tests given and complete | Method bodies only |
| **L2 — Contract and build** | M2 | A Javadoc-only specification of the required public API, and a read-only contract test that enforces it. No interface file — writing the signatures is part of the exercise | Class, signatures, bodies, plus their own tests |
| **L1 — Harness and fill** | M3, M4, M5 | Everything that is not the lesson: `Main`/`Robot` wiring, simulated plants, vendordeps, sim config, logging, a test harness. Plus given tests that assert observable behavior | Subsystems, commands, control logic, gains, auto routines — the lesson |
| **L0 — Worked example only** | M6, M7, M8 | One fully-commented worked example, the awkward setup encapsulated (CTRE sim timing, `AutoCloseable` teardown), and the raw material (a log, a regression patch, a feature backlog) | Everything else |

Two rules that hold at every level:

- **A stub never contains the answer in a comment.** `// TODO: divide by ticksPerRotation` is not scaffolding, it is the solution with extra steps. The Javadoc states the *contract* — inputs, outputs, edge cases — never the implementation.
- **A given test never asserts on implementation.** It asserts on behavior through the public API. A test that requires a specific private field name teaches compliance, not correctness.

---

## 9. Per-milestone skeleton inventory

Each entry lists what ships, which DoD items the tests cover, and which stay mentor-verified per D3.

### 9.1 M0 — `projects/m0-onboarding/` · fade: N/A

M0 is not about code. The skeleton exists so the pull request has something real to change.

**Given:** a complete, working, minimal `TimedRobot` project that builds and whose simulator launches. `CONTRIBUTING.md` with the curated command set from `curriculum-spec.md` §4.2 — `clone`, `checkout -b`, `add -u`, `commit -m`, `fetch --all`, `merge`, `push` — and nothing else. `student-check.yml`. A deliberately mis-formatted `FormatMe.java`.

**Student authors:** `contributors/<github-handle>.md` from a template, and runs `./gradlew spotlessApply` on `FormatMe.java`, then opens one pull request containing both — upstream, per §5.2.

**Tests cover:** the contributor file's schema, when one is present — GitHub line, no placeholder text, no accidental email disclosure. `spotlessCheck` passes.
**Mentor verifies:** the contributor file exists for this student's PR (the checker cannot know which student authored a given pull request, so it cannot enforce this — an empty `contributors/` directory still exits 0); simulator launches and the student can point at the Robot State panel; can explain a branch without using the word "branch"; **Actions are enabled on their fork** (§7.3, mechanic 3).

### 9.2 M1 — `projects/m1-java-fundamentals/` · fade: L3

**Given:** six stub classes in `frc.training.m1`, one per exercise in `milestones/m1-java-fundamentals.md` — `Deadband`, `TickConversion`, `GearRatio`, `AtSetpoint`, `Clamp`, `Interpolate`. Each: one static method, a Javadoc contract, an `UnsupportedOperationException` body. Six complete, read-only test classes.

Every test suite includes the trap cases the milestone's failure-mode section names:
- `TickConversion` — an input that returns `0` under integer division and the correct non-zero value otherwise
- `AtSetpoint` — values equal to within `1e-9` but not `==`
- `Clamp` — both boundaries inclusive, plus min-greater-than-max
- `Interpolate` — a query exactly at each known point, and one outside the range

Plus two non-test artifacts:
- `CompilerErrorGarden.java` — three commented-out broken snippets, uncommented one at a time, for the "read a compiler error unaided" DoD item
- `BlockingWaitDemo.java` — a runnable `main` showing a `while` loop starving the 20ms loop

**Vendordeps:** none. This is plain Java.
**Real repo:** browse only — `IntakeConstants.java`, `Constants.java`.

**Tests cover:** all six exercises pass their assertions.
**Mentor verifies:** reads a compiler error unaided; explains `1/2 == 0`; explains why `==` is wrong for doubles; explains the blocking-`while` danger.

### 9.3 M2 — `projects/m2-oop/` · fade: L2

**Given:** `SensorProfileSpec.java` — a Javadoc-only specification class describing the required public API (constructor taking ticks-per-rotation and gear ratio; ticks↔rotations; ticks-per-100ms↔RPM; one method taking a `DoubleSupplier`) with no signatures to copy. A read-only `SensorProfileContractTest` that enforces the API by behavior. A student test file with one worked example and three empty `@Test` methods carrying TODOs.

`SupplierDemo.java` — a working class with the milestone's headline bug already planted: a constructor that captures `supplier.getAsDouble()` once instead of storing the supplier, so the value appears frozen. A given test fails until the student fixes it. This is the single most common M2 misunderstanding, and giving it to them broken is cheaper than waiting for them to write it broken.

`READING.md` — the `Robot-2026/src/main/java/frc/robot/preferences/` reading assignment, with **a pinned commit SHA**, so the read is reproducible and the seasonal re-verification pass has something concrete to check.

**Vendordeps:** none.
**Real repo:** first clone. Read-only.

**Tests cover:** contract test passes; four student tests exist and pass; `SupplierDemo` fixed.
**Mentor verifies:** value-vs-supplier explanation; `LaunchState.getInstance()` and what a singleton guarantees; why both launch-request builders implement one interface; why `RobotPreference<T>` implements `Supplier<T>`.

### 9.4 M3 — `projects/m3-command-based/` · fade: L1

**Given:** `Main.java`; a thin `Robot.java` with the scheduler running and Epilogue configured (`Epilogue.configure(...)` and `Epilogue.bind(this)`, matching `Robot-2026`); a `RobotContainer.java` with marked TODO regions and the controller already constructed; a `Constants.java` stub; a seeded `simgui.json` so the simulator opens with a joystick already mapped.

**Deliberately plain WPILib.** Simulated `PWMSparkMax` and `DifferentialDrive`, no Phoenix. M3's lesson is the command-based model, and adding a swerve stack triples the surface for zero pedagogical gain. Phoenix arrives in M4 where it is the lesson.

**The test-harness design.** Given tests must assert on classes the student has not written yet. Solving this by reflection over guessed class names is brittle and teaches students to name things to please a grader. Instead the skeleton ships a small given interface:

```java
/** Exposes your M3 deliverables to the test harness. Implement it in RobotContainer. */
public interface M3Deliverables {
  Subsystem drivetrain();
  Subsystem mechanism();
  Command subclassCommand();   // your explicit Command subclass
  Command factoryCommand();    // your subsystem factory method
  Command composition();       // your sequence or parallel-with-timeout
}
```

Given tests then run against the interface: both commands have a `.withName()`, the composition requires both subsystems, and — the important one — `SchedulerInterruptionTest` schedules two commands requiring the same subsystem and asserts the first is interrupted and its `end(true)` fires. That is the exact model the M3 test-out and DoD both turn on, and a student can watch it pass.

**Vendordeps:** `WPILibNewCommands` only.
**Real repo:** clone `Phoenix-2026`, read end to end; then `Robot-2026/RobotContainer.java`. Read-only.

**Tests cover:** both commands exist, are named, and run; composition requires both subsystems; interruption semantics hold.
**Mentor verifies:** articulates the subclass-vs-factory tradeoff; traces one full loop iteration out loud; subsystems are private fields rather than globals.

### 9.5 M4 — `projects/m4-control/` · fade: L1

**Given:** two simulated plants — `FlywheelPlant.java` (`FlywheelSim`) and `ArmPlant.java` (`SingleJointedArmSim`) — with **true parameters the student must not read.** They live in a `plant/` package listed in `CODEOWNERS` and covered by `tests.lock`. The point of the milestone is characterizing a system you cannot look up, and a student who reads kV out of a constants file has skipped it.

Also given: `SysIdRoutine` wiring (boilerplate, explicitly permitted under Yellow tier); Epilogue `@Logged` on setpoint and measurement together, per the M6 convention; a `PlotHarness` that dumps setpoint-vs-measurement CSV for the "produce a plot and read it" DoD item; a limit-switch sim for homing.

**Student writes:** feedforward application, gains, PID, motion profile, homing sequence.

**Vendordeps:** `Phoenix6` (26.1.0), `WPILibNewCommands`.
**Real repo:** clone to read `ShooterSubsystem`; **fork and open a pull request** — see below.

**Tests cover:** flywheel reaches and holds commanded velocity within tolerance and settling time; arm holds position against gravity; homing zeroes on limit-switch contact.
**Mentor verifies:** what each of kS, kV, kA, kG does; why feedforward is tuned before P; reads their own plot.

**The real pull request.** Per §6.7, opening one PR against a real robot repository is a **required** DoD item; the `DriveMotorConfigs.java` `.clone()` refactor is the recommended vehicle but remains optional in itself. The PR is against `Robot-2026` from the student's fork — real code, real review.

### 9.6 M5 — `projects/m5-auto-vision/` · fade: L1

**Given:** a working simulated swerve drivetrain with `AutoBuilder` already configured; the AprilTag field layout; a `VisionSim` harness built on PhotonVision's `VisionSystemSim` with **configurable noise and latency** so the student can make it worse on purpose; a `SendableChooser` already wired; one example path file.

Building a swerve drivetrain is not M5's lesson. Composing autos and fusing vision is.

**Student writes:** paths, named commands, the composition with timeouts, the chooser entries; pose-estimator fusion, timestamp handling, distance-based filtering.

**Vendordeps:** `PathplannerLib` (2026.1.2), `photonlib`, `Phoenix6`, `WPILibNewCommands` — matching `Robot-2026` exactly.
**Real repo:** clone to read `vision/`, `LaunchCalculator`. Read-only.

**Tests cover:** the routine terminates even when a step is forced to fail (the milestone's step 7); fused pose error is lower than odometry-only error over a run; a measurement submitted with a deliberately wrong timestamp is rejected by the student's filter.

That last test is the one that matters. The milestone says "everyone learns that vision helps; far fewer learn that vision applied wrongly is worse than no vision" — and it is the only DoD item in M5 a student can otherwise fake by getting a good result for the wrong reason.

**Mentor verifies:** explains odometry drift and why vision corrects it; articulates one real PathPlanner-vs-Choreo tradeoff.

### 9.7 M6 — `projects/m6-testing/` (warm-up) + a real pull request · fade: L0

The gate to M7. The bar is deliberately low and the skeleton must not raise it.

**The deliverable is the pull request** (D9): tests added to `Robot-2026` for a class drawn from `UNTESTED-TARGETS.md`, opened from the student's fork, subject to the **pure-logic hard rule** in §6.5. The sandbox project below is the warm-up that makes that pull request writable.

**Given in the sandbox, and nothing more:**

- **One** fully-commented worked-example test, showing the `AutoCloseable` teardown pattern
- `PhoenixTestSupport.java` — the CTRE simulation timing quirks encapsulated: `DriverStationSim.setEnabled(true)` plus `DriverStation.notifyNewData()`, the ~100ms delay after constructing devices, the ~20ms delay after a control request before asserting. `milestones/m6-simulation-telemetry-testing.md` says these must be taught explicitly rather than discovered; a helper class with a comment explaining each delay is how you teach it once instead of losing three students to intermittent failures. **This class stays in the sandbox and never ships to `Robot-2026`** — §6.5
- `LaunchProfile.java` — a class with real logic worth testing, shaped after `Robot-2026`'s `LaunchCalculator`. Now explicitly the *practice* target, with the real one one repository away
- `regressions/*.patch` — `git apply`-able one-line regressions (flip a sign, drop a gear ratio). Makes "introduce a deliberate regression" one reproducible command instead of an improvisation
- `logs/` — one real `.wpilog` copied from `Robot-2026/logs/`, plus `QUESTIONS.md` with the diagnostic questions. Answer key lives in the solutions repository

**Student writes:** every test, in both places.

**The verification mechanism, which remains the best idea in this spec.** CI does not count tests. In the sandbox it runs:

```
./gradlew test                                      → must pass
git apply regressions/flip-sign.patch
./gradlew test                                      → must FAIL
git apply -R regressions/flip-sign.patch
```

A student can satisfy a test-count check with three assertions on `true`. They cannot satisfy this one without a test that actually catches the regression — which is the entire stated point of M6, and the one DoD item most likely to be technically met and substantively missed.

**Tests cover:** three-plus sandbox tests pass; the regression check fails then passes.
**Mentor verifies:** the `Robot-2026` pull request is merged and its tests are pure-logic; the AdvantageScope diagnostic answer *with evidence*; explains why not to instantiate `TimedRobot` in a test.

### 9.8 M7 — `projects/m7-ai-assisted/` · fade: L0

Almost no code. M7 ships a feature into a real repository with commit access — building a sandbox for it would defeat the "pick a real, useful feature, not a toy" requirement.

**Given:** `ai-code-review.yml`, copied from `Robot-2026`, so students meet AI-as-reviewer as a running thing rather than a description. A pull request template with the disclosure block from `ai-use-standard.md` §6. `FEATURE-BACKLOG.md` — a mentor-maintained list of real, small, shippable features, so nobody spends their M7 session choosing one. `DEFENSE-REVIEW.md` — the mentor-side question bank, published openly because a defense review students can prepare for is still a defense review. `MISCALIBRATION.md` — the deliberate-wrong-answer exercise, with a place to paste what the tool confidently said.

**Real repo:** branch on `Robot-2026` upstream. Commit access.

**Tests cover:** nothing. M7's assessment is the defense review, by design.
**Mentor verifies:** all five DoD items.

### 9.9 M8 — `projects/m8-ownership/` · fade: L0

No skeleton code. The deliverables in `student-deliverables.md` become **real files** in `templates/` so a student copies rather than retypes — which is the difference between a template and a description of a template, and is already the stated acceptance criterion in `curriculum-spec.md` §7.

`projects/m8-ownership/README.md` points at `templates/` and at the week-by-week cadence.

**Real repo:** branch on `Robot-2026` upstream, in-season. Commit access.

---

## 10. Test-out fixtures

The track axis, per D5. Each fixture is a standalone project under `projects/testouts/` with a student-facing `README.md` stating the task and the time box, and an answer key in the solutions repository.

| Fixture | Milestone | Attempted by | What ships |
|---|---|---|---|
| — | M0 | B, C, D | No fixture. Pass-on-sight: a merged pull request that passed CI, plus `./gradlew build` on the student's own machine while a mentor watches |
| **`t1-find-the-bug/`** | M1 | B, C, D | **Three rotating variants** — off-by-one, inverted comparison, integer truncation. Each is one short method plus a failing test. Mentor picks one |
| **`t2-supplier-swap/`** | M2 | C, D | An interface with two implementations. Add a third and wire it in; then convert one method from `double` to `DoubleSupplier`. Given tests cover the third implementation; the supplier explanation is mentor-verified |
| **`t3-scheduler-conflict/`** | M3 | C, D | A working `RobotContainer` with three subsystems. Add a command requiring two, bind it, explain interruption. The code half is test-covered; the explanation is the pass criterion and is mentor-verified |
| **`t4-bad-kv/`** | M4 | D | A simulated mechanism with kV far too low and kP compensating, shipping with a pre-generated response plot. Reaches target, overshoots, oscillates |
| **`t5-drifting-auto/`** | M5 | D | **Three pre-recorded log sets**, one per root cause: odometry drift, mistuned path controller, blocking named command. Mentor picks one |

**Why the rotating variants.** A single fixed T1 leaks: the second student to attempt it hears the answer from the first. Three variants and a mentor's choice cost little to author and make the fixture reusable across a cohort. The same logic drives T5's three log sets.

**T5 is the most expensive artifact in this spec.** Three *convincing* logs — each showing a genuinely different failure signature, each with enough surrounding data that the other two causes can be ruled out by evidence — is real work. The pass criterion is explicitly "correct diagnosis *and* the ruling-out," so a log that only supports the right answer without excluding the wrong ones fails the fixture, not the student. Budget accordingly; it is scheduled last in §13 for that reason.

---

## 11. Solutions repository

### 11.1 Contents

`ignite-training-solutions`, private, mentor access only:

- A complete reference implementation of every milestone project
- An answer key per test-out fixture, including *why* each distractor is wrong — a mentor running T5 needs to know what evidence rules out the other two causes, not just which cause it is
- The M6 AdvantageScope diagnostic answers with the supporting log evidence
- Authoring notes: which trap each given test is aimed at, so a future maintainer does not "simplify" a test case that exists on purpose

### 11.2 Keeping it in sync

The obvious failure mode of D6 is a skeleton test changing and the solution silently no longer passing it. A scheduled CI job in the solutions repository:

1. Checks out `IgniteRobotics/ignition-labs` at `main`
2. Overlays the solution sources onto `projects/`
3. Runs the full test suite
4. Fails loudly if anything is red

Run it weekly and on every push to either repository. Without this, D6's accepted cost is not actually mitigated — it is just deferred to whoever discovers the drift during a session.

---

## 12. Requirements the skeletons must satisfy

### 12.1 Anti-goals

| Must not | Why |
|---|---|
| Contain the answer in a comment | `// TODO: divide by ticksPerRotation` is the solution with extra steps |
| Be seeded from `CommandTemplate` | Stock WPILib sample code, still on 2025.2.1, no README, no CI, no Ignite conventions. `curriculum-spec.md` §8 is explicit: seed from `Robot-2026` and `Phoenix-2026` |
| Reference pre-2026 Ignite code | `curriculum-spec.md` §1.2. No student-facing path may point at `2025-Robotics-Intro`, `Robot-2025`, or earlier |
| Require hardware for any step | Simulation is mandatory for every project. Hardware variants stay optional enrichment |
| Require an AI tool | M0–M2 are Red tier. A skeleton whose setup assumes Copilot is unusable by the students it is aimed at |
| Assert on private implementation | Teaches naming-to-please-a-grader instead of correctness |
| Ship a test the student can trivially delete | See §12.2 |
| Introduce a timing-dependent test into a real robot repository | §6.5. One flake blocks every team pull request |

### 12.2 Given tests must be tamper-evident

A student who cannot make a test pass can delete the test. This is not hypothetical and it is not malice — it is what a frustrated fifteen-year-old at minute forty does.

Three layers, in order of strength:

1. **`tests.lock`** — a SHA-256 manifest of every given test file and every read-only source file (M4's `plant/` package especially). `student-check.yml` verifies it and fails the pull request on any mismatch, naming the modified file.
2. **`CODEOWNERS`** — given test paths require mentor review, so a change is at least visible.
3. **The failure message says so.** When the lock check fails, the CI comment reads *"this test is part of the milestone — if you think it is wrong, say so in the pull request and a mentor will look."* A student who has found a genuinely bad test should be able to say so and be right. That happens, and the response should not be an accusation.

Layer 3 matters more than the other two. The check exists to make tampering visible, not to be uncircumventable — anyone determined can rewrite history. Visible is enough.

### 12.3 Every skeleton compiles on a clean clone

Stubs throw `UnsupportedOperationException`; they do not fail to compile. A student's first experience must be a green `./gradlew assemble` and a red `./gradlew test`, in that order. Compile errors on clone are indistinguishable from a broken toolchain, and a Track A student cannot tell the difference — which loses them a session to a problem that was never theirs.

---

## 13. Build order

Ordered by dependency, then by value. Each step is independently reviewable and independently useful.

| # | Step | Notes | Status |
|---|---|---|---|
| **0** | Sanity check: one standalone project builds from a clean clone; confirm `build` runs `test` under GradleRIO | 30 minutes. Not a spike — §4.2 removed that risk | Done |
| **1** | **`programming/git-workflow.md` + `programming/robot-repo-ladder.md`** | §5 and §6 as student-facing prose. **Zero dependencies. Ships immediately** | Done |
| **2** | Create the git remote; repository scaffold: four workflows, `tests.lock` tooling, `CODEOWNERS`, pull request template, `CONTRIBUTING.md`, `SETUP.md` (including the fork-Actions step) | — | Done |
| **3** | Pages live | Proves the pipeline before there is much to publish | Done |
| **4** | **M0** | Smallest, and the one every single student hits | **Done** |
| **5** | **M1** + T1 | Highest volume of students. L3 is the most authoring-heavy fade level per unit of content | — |
| **6** | **M2** + T2 | — | — |
| **7** | **M3** + T3 | Largest milestone. The `M3Deliverables` harness is the risky part — build it first and validate against a throwaway solution | — |
| — | — | **Kickoff Gate block complete (M0–M3). Ship it and stop if time runs out.** Everything below can land after the season starts | — |
| **8** | **M6** + `UNTESTED-TARGETS.md` | Out of numeric order deliberately: it is the M7 gate, and the regression-patch verification is novel enough to want proving early | — |
| **9** | **M4** + T4 | — | — |
| **10** | **M5** + T5 | T5's three log sets are the single largest authoring cost in the spec | — |
| **11** | **M7**, **M8 templates** | Mostly documents | — |
| **12** | **Doc corrections.** The §7.3 CI claim in three files; M6's "first tests" → "a class that has none"; M4's required-PR DoD item; `curriculum-spec.md` §8 and `README.md` status tables; the `resources.md` seasonal re-verification list | — | — |
| **—** | Solutions repository, built continuously alongside steps 4–11, with the §11.2 cross-check job live from step 4 | Not a final step. A milestone shipped without its solution is not shipped | — |

**Timing.** 2026-08-07, kickoff roughly five months out. Steps 1–7 fit comfortably *if step 1 lands now*. Steps 1–3 are almost entirely writing and configuration — they do not wait on the toolchain being settled.

**If only one thing gets built: step 1.** The two documents make the existing curriculum coherent on their own, whether or not a single skeleton is ever written.

---

## 14. Acceptance criteria

- [ ] `programming/git-workflow.md` states the §5.1 rule and gives copy-pasteable commands for M0 and for M1–M6
- [ ] `programming/robot-repo-ladder.md` contains the §6.1 matrix and explains the M6 non-circularity
- [ ] A student clones `projects/m1-java-fundamentals/`, runs `./gradlew assemble`, and it is green before they write a line
- [ ] Every milestone's mechanical DoD items are covered by a given test; every explanation item is documented as mentor-verified, in the milestone file, unchanged
- [ ] `skeleton-integrity.yml` passes on `main`, including the inverted check that each skeleton's tests fail unmodified
- [ ] A student fork's `student-check.yml` produces a single green check a mentor can read without opening the code
- [ ] `pages.yml`, `skeleton-integrity.yml`, and `docs-links.yml` are skipped — not failed — on a fork
- [ ] The Pages site renders every prose file and **zero** files from `projects/`
- [ ] `SETUP.md` documents enabling Actions on a fork, with a screenshot
- [ ] No skeleton references pre-2026 Ignite code, and every referenced path is pinned to a commit SHA
- [ ] Every project completes in simulation with no hardware
- [ ] Every test-out fixture has an answer key that explains the distractors, not just the answer
- [ ] The solutions cross-check job is green
- [ ] `tests.lock` verification correctly fails a pull request that modifies a given test, with a message that invites disagreement
- [ ] The M6 regression check demonstrably fails for a student whose tests pass but catch nothing
- [ ] Every M4 student has opened at least one pull request against a real robot repository
- [ ] `UNTESTED-TARGETS.md` exists, has an owner, and is checked off as M6 pull requests land
- [ ] No real-repo test introduced by a student depends on timing, simulation, or hardware

---

## 15. Risks

| Risk | Severity | Mitigation |
|---|---|---|
| Fork Actions disabled → M0 shows no CI, reads as broken | **High** | Explicit, screenshotted step in `SETUP.md`; mentor checklist item at M0 (§9.1) |
| A flaky student test blocks team pull requests in-season | **High** | §6.5 hard rule: pure logic only in real-repo tests |
| `UNTESTED-TARGETS.md` goes stale and M6 loses its target list | Medium | Same unassigned-owner problem as `FEATURE-BACKLOG.md`. Assign both to one person — §16 |
| Student forks of `Robot-2026` drift once the season starts | Medium | The ladder puts fork-and-PR milestones before kickoff by design; M7/M8 branch on upstream |
| Skeletons rot against next season's WPILib | Medium | Seasonal re-verification pass, §16. Order preserved: bump, re-pin, cross-check |
| Solutions repository silently diverges from the skeletons | Medium | §11.2 cross-check, live from build step 4 |
| Pages workflow fires on student forks | Low | `if: github.repository ==` gate, §7.2 |

---

## 16. Maintenance

The teaching projects join the per-season re-verification pass alongside `resources.md`'s two halves. `README.md` currently names two things to re-verify each season; this adds a third.

Each season, before the fall:

1. Bump GradleRIO, vendordeps, and the Gradle wrapper to match `Robot-2026` and `Phoenix-2026`. **A skeleton that does not build with the toolchain M0 just installed is worse than no skeleton at all** — that is the exact failure mode `curriculum-spec.md` §1.2 was written to prevent, and it applies with more force to code than to prose
2. Re-pin every commit SHA in the `READING.md` files
3. Re-run the solutions cross-check
4. Re-generate T4's plot and T5's logs if the sim stack changed
5. Refresh `UNTESTED-TARGETS.md` against the new season's repository
6. Bump and re-pin `mkdocs` and `mkdocs-material` in `pyproject.toml`. Material's own build already warns that MkDocs 2.0 removes the plugin system entirely and ships no migration path — treat that warning as live every season it appears, not a one-time notice, and require a full `mkdocs build` plus a passing `tools/site_guard.py` before merging the bump. A version number that resolves is not the same thing as a site that still builds
7. Once `UNTESTED-TARGETS.md` exists (item 5 above), enable `mkdocs build --strict` in `pages.yml`. It is deliberately not strict today because `programming/robot-repo-ladder.md` links that one file before it exists, which is a legitimate warning, not a defect. Without `--strict`, a broken internal link is a silent 404 that neither `lychee` nor `tools/site_guard.py` can catch — both check different failure modes

Order matters: bump first, then re-pin, then cross-check. A cross-check against an un-bumped skeleton proves nothing useful.

**Investigated, not adopted: `lychee --include-fragments`.** `docs-links.yml` does not pass it, so a dead in-page anchor (the class of bug fixed in `VISION_GUIDE.md`) is invisible to link checking. Tried locally against `mkdocs build --strict`'s two now-correct MkDocs anchors (`#7-filtering-validation`, `#10-recommended-fixes-improvements`): lychee's own fragment slugifier is GitHub-style, not MkDocs-style, and reports "Cannot find fragment" on exactly the anchors that are correct against the built site. Adopting the flag would fail CI permanently on headings containing `&`, with no fix available that does not re-break the real site. Revisit only if lychee ships an MkDocs-aware slugifier.

**In-season, continuously:** `FEATURE-BACKLOG.md` (M7) and `UNTESTED-TARGETS.md` (M6). Both go stale fast, both have a milestone that stops working when they do, and **neither has an owner.** This is the single largest unmitigated risk in the document.

---

## 17. Open items

| Item | Status |
|---|---|
| Does `./gradlew build` run tests under GradleRIO's `configureTestTasks`? | **Resolved: yes.** Empirically verified with a fail-loudly probe test on a Phoenix-2026 copy (GradleRIO 2026.2.1, wrapper 8.11, JUnit 5.10.1) — `./gradlew build` and `./gradlew test` both failed identically on the probe assertion. See `superpowers/notes/2026-08-07-gradle-test-lifecycle.md`. No §7.3 correction needed |
| Who maintains `FEATURE-BACKLOG.md` and `UNTESTED-TARGETS.md` in-season? | **Unassigned.** M7 and M6 both depend on it. See §16 |
| Should `templates/` live here or stay inline in `student-deliverables.md`? | Proposed: **both** — the prose stays for reading, the files exist for copying. Duplication risk is real; a CI check that the two match would resolve it |
| Is one worked-example test enough scaffolding for M6? | **Genuinely uncertain.** L0 is the sharpest drop on the fade curve and M6 has no test-out, so every student hits it at full density — now with a real pull request attached. Watch the first cohort; if they stall, the fix is a second worked example, not a lower bar |
| Does a student's `Robot-2026` fork survive from M4 to M6 usefully? | Unknown. Expected yes during the quiet season; re-evaluate if M4 and M6 end up separated by kickoff for anyone |
| Does the inverted check in `skeleton-integrity.yml` fail M0, which ships no given tests to fail? | **Resolved: it skips.** The inverted check now skips any project with no `src/test` directory. M0 is fade N/A (§9.1) and ships no stubs by design, so there is nothing for the check to require failing — skipping is correct, not a workaround. It still fires for a project that has `src/test` |

---

## 18. Verified facts

Checked directly against `~/workspace/ignite/` on the dates shown.

| Claim | Value | Checked |
|---|---|---|
| `Robot-2026` Java files | 47 | 2026-08-07 |
| `Robot-2026` `src/test` | **absent** | 2026-08-07 |
| `Robot-2026` logs | 274 | 2026-08-07 |
| `Robot-2026` activity | 2 commits in 90 days; last 2026-06-09 | 2026-08-07 |
| `Robot-2026` workflows | `pr-build-check.yml`, `ai-code-review.yml` | 2026-08-07 |
| `Phoenix-2026` Java files | 22 | 2026-08-07 |
| `Phoenix-2026` `src/test` | **absent** | 2026-08-07 |
| `Phoenix-2026` activity | 0 commits in 90 days; last 2026-02-10 | 2026-08-07 |
| `mkdocs` with `docs_dir: .` | **rejected outright** — `The 'docs_dir' should not be the parent directory of the config file. Use a child directory instead.` No build produced | 2026-08-07 |
| `mkdocs` (1.6.1) with `docs_dir: docs`, probe files under `projects/m-probe/` | **zero** matching files in the built site. Verified independently twice | 2026-08-07 |
| `docs/programming/README.md` | publishes to the slug `programming` (`_site/programming/index.html`) — `tools/site_guard.py`'s `REQUIRED_PAGES` needed no edit | 2026-08-07 |
| `ignition-labs` git remote | **none configured** | 2026-08-07 |
| Build tooling versions, vendordep set, `pr-build-check.yml` contents | §4.3 | 2026-08-06 |

---

## 19. Sources

**Peer FRC practice**
[FRC Java Koans — Team 3550](https://github.com/jletroui/FrcJavaKoans) · [FRC_2026_SoftwareLessons — Team 8744](https://github.com/FRC-Team8744/FRC_2026_SoftwareLessons) · [SciGuides — Team 1155](https://github.com/SciBorgs/SciGuides) · [FRC 1099 DiscoTechs Java Training](https://sites.google.com/student.brookfieldps.org/discotechs/frc-java-programming-training) · [Chief Delphi — How do you do programming lessons to new students?](https://www.chiefdelphi.com/t/how-do-you-do-programming-lessons-to-new-students/516927) · [Chief Delphi — FRC Programming Course: Share Your Methods](https://www.chiefdelphi.com/t/frc-programming-course-share-your-methods/475292)

**Tooling**
[classroom-resources/autograding-example-java](https://github.com/classroom-resources/autograding-example-java) · [GitHub Classroom — adding tests for autograding](https://classroom.github.com/help/adding-tests-for-auto-grading) · [wpilibsuite/docker-images](https://github.com/wpilibsuite/docker-images) · [Chief Delphi — Codespaces for FRC](https://www.chiefdelphi.com/t/github-codespaces-programming-in-your-browser-for-frc-2023-update/423814) · [Evan Pratten — Integrating GitHub Codespaces with FRC](https://ewpratten.com/blog/codespaces-for-frc/) · [WPILib Robot Simulation Introduction](https://docs.wpilib.org/en/stable/docs/software/wpilib-tools/robot-simulation/introduction.html) · [2026 KitBot Java Software Guide](https://firstfrc.blob.core.windows.net/frc2026/KitBot/2026-kitbot-java-guide.pdf)

**CS-education literature**
[Shin, Jung, Zumbach & Yi (2023) — Worked-Out Example and Metacognitive Scaffolding on Problem-Solving Programming](https://journals.sagepub.com/doi/abs/10.1177/07356331231174454) · [Fade-in vs. fade-out scaffolding for novice programmers, *J. Computing in Higher Education*](https://link.springer.com/article/10.1007/s12528-021-09307-w) · [Suggestions for graduated exposure to programming concepts using fading worked examples (ICER)](https://dl.acm.org/doi/abs/10.1145/1288580.1288594) · [Scaffolding Novices: When and How Parsons Problems Impact Novice Programming (ICER 2024)](https://dl.acm.org/doi/10.1145/3632620.3671110)

---

## 20. Audit

```
2026-08-06 — research session
Queries sent: 6 | Sources received: 41 | Sources cited: 17
Failures: 1 (Chief Delphi returned HTTP 403 to automated fetch — same bot-blocking
             class as the ReadTheDocs issue in curriculum-spec.md §1.1; the thread
             is cited from search-result metadata only, not fetched content)
Routing decision: fallback (no research specialist matched)
Sub-questions: (1) how peers distribute starter code; (2) what "skeleton" means per
level; (3) how completion is verified automatically; (4) what environment floor
removes install friction

2026-08-07 — design session, merged into this document
Queries sent: 0 (no external research; all claims verified against local repos)
Decisions added: D7 (Pages), D8 (real-repo ladder), D9 (M6 real PR)
Decisions overridden: D1, D2 · Sections deleted: the §4.2 GradleRIO spike
Sub-questions: (1) where skeletons live; (2) the per-milestone git ritual;
(3) which milestones work in Robot-2026 instead; (4) layout vs. the Pages pipeline
```
