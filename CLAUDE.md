# CLAUDE.md — ignition-labs

FRC 6829 Ignite Robotics teaching repository: the curriculum prose *and* the skeleton
projects students build against. Governing document is
[`docs/programming/teaching-repo-spec.md`](docs/programming/teaching-repo-spec.md) —
when this file and the spec disagree, the spec wins and this file is wrong.

## Layout, and the one boundary that matters

```
docs/        docs_dir — the ONLY directory MkDocs can read. Prose.
projects/    standalone Gradle projects, one per milestone + testouts/. Unpublishable.
templates/   M8 deliverables as copyable files
contributors/ M0 output, one file per student handle
tools/       verification scripts CI calls (site_guard.py, tests_lock.py)
tests/       pytest suite for tools/
superpowers/ specs, plans, notes — design records
tests.lock   SHA-256 manifest of every given test file
```

Everything outside `docs/` is unpublishable **structurally**, not by filter — MkDocs
cannot read past `docs_dir`. Don't "fix" this by adding excludes; there is nothing to
exclude.

Each `projects/*/` is standalone: own `gradlew`, own `settings.gradle`, own
`vendordeps/`. **There is no root Gradle build and must not be one** (spec §4.2).

## Toolchain

```bash
export JAVA_HOME=~/wpilib/2026/jdk
```

Required for every local Gradle run. Without it the wrapper finds a Java 11 JVM and
both plugins fail with *"Dependency requires at least JVM runtime version 17."*
**That is an environment fact, not a build defect — never edit `build.gradle` in
response to it.**

## Pinned versions — never bump as part of a feature

| Setting | Value |
|---|---|
| GradleRIO | `2026.2.1` |
| Gradle wrapper | `8.11` |
| Java source/target | 17 |
| Spotless plugin | `8.2.1` |
| Formatter | `googleJavaFormat('1.19.2').reflowLongStrings()` |
| JUnit | `org.junit.jupiter:junit-jupiter:5.10.1` |

Bumping happens once a season, deliberately, in the §16 maintenance pass — bump, then
re-pin SHAs, then re-run the solutions cross-check, in that order.

**`enforceCheck false`, and never `compileJava.dependsOn 'spotlessApply'`.**
Auto-apply-on-compile and a real CI format check are mutually exclusive: the compile
reformats the tree before `spotlessCheck` can observe a violation. That wiring is what
made M0's gate unreachable, and it is still broken in `Robot-2026` (spec §17).

## Package naming

| Milestones | Package |
|---|---|
| M1, M2 | `frc.training.m1`, `frc.training.m2` |
| M3 onward | `frc.robot` |

Never `frc.robot` for M1/M2 — they contain no robot, and the name invites pasting
training code into a season repo out of muscle memory.

## The scaffold-fade curve (spec §8)

| Level | Milestones | Student writes |
|---|---|---|
| L3 stub-and-fill | M1 | method bodies only |
| L2 contract-and-build | M2 | class, signatures, bodies, own tests |
| L1 harness-and-fill | M3, M4, M5 | subsystems, commands, control logic |
| L0 worked-example-only | M6, M7, M8 | everything else |

Density decreases on purpose. A student given as much help at M6 as at M1 has been
failed by the scaffolding.

## Authoring rules — these are the ones that get broken

- **A stub never contains the answer in a comment.** The Javadoc states the contract —
  inputs, outputs, edge cases — never the implementation. No formula, in Javadoc,
  inline comment, README, or variable name.
- **A given test never asserts on implementation.** Behaviour through the public API
  only. A test requiring a private field name teaches compliance, not correctness.
- **No test may assert that a stub throws.** A test asserting
  `UnsupportedOperationException` passes against the unmodified skeleton, inverting the
  whole gate.
- **Double assertions always use the delta overload:** `assertEquals(expected, actual, 1e-9)`.
- **Never commit a reference implementation to this repo.** Plans write one to verify
  the test suite is satisfiable — it belongs in `ignite-training-solutions`, not here.
  Committing it ships the milestone already solved. `git status --short` before every
  commit.

## The clean-clone invariant (spec §12.3)

On a fresh clone of any skeleton, all three must hold:

```bash
./gradlew assemble          # rc=0 — green
./gradlew compileTestJava   # rc=0 — green
./gradlew test              # rc=1 — RED, from failing assertions
```

The red must never come from a compile error. A Track A student cannot tell a compile
error from a broken toolchain, and it costs them a session that was never their problem.

When a given test would have to name a class the student hasn't written yet, reach it
reflectively through a given probe (see `projects/m2-oop/.../ContractProbe.java`) rather
than letting `compileTestJava` fail.

## Known-good exceptions

- **`projects/m0-onboarding` fails `spotlessCheck` by design.** `FormatMe.java` is M0's
  formatting exercise; `skeleton-integrity.yml` exempts exactly that filename. Its
  `test` is also green — M0 ships no stubs, and the inverted check skips projects with
  no `src/test`.
- `starter/` directories are outside both source sets and outside Spotless's
  `src/**/*.java` target. Hand-format them to `googleJavaFormat` style so a student's
  first act after copying one in isn't a format failure.

## tests.lock

SHA-256 manifest of every `.java` under any `src/test/`, including non-test helpers like
`ContractProbe.java` — a student who edits the probe can make the contract test pass
against anything.

```bash
poetry run python tools/tests_lock.py write    # regenerate
poetry run python tools/tests_lock.py verify   # rc=1 on tamper
```

Student-owned files (anything under `starter/`) must **not** appear in it.

## Verification discipline

This repository has repeatedly produced checks that pass while the thing they protect is
broken. Plans carry explicit steps to attack that, and they are not optional:

- **Read every failure message a student will see.** A probe whose messages have never
  been read is a probe whose messages are wrong.
- **Run a correct implementation against the suite.** A suite no right answer passes is
  as broken as one every wrong answer passes.
- **Run the distractors.** Confirm each plausible-but-wrong version fails the case that
  is supposed to catch it.
- **Complete the fixture end to end.** A fixture nobody has finished is a fixture nobody
  knows is completable.

Report actual command output, not a summary of what should have happened.

## Other constraints

- **No pre-2026 Ignite references.** Nothing student-facing may point at
  `2025-Robotics-Intro`, `Robot-2025`, or earlier. Every referenced real-repo path is
  pinned to a commit SHA.
- **No hardware required.** Every project completes in simulation.
- **No AI tool required.** M0–M2 are Red tier.
- **Vendordeps: minimum set per milestone** (spec §9). Never "all of them by default."
  M0–M2 have none.

## Git

- Default branch `main`; feature branches `feature/<name>`; worktrees under
  `.worktrees/` (gitignored).
- `trash` over `rm` where possible.
- `poetry run pytest`, `poetry run mkdocs build`, `poetry run python tools/site_guard.py _site`
  before touching anything that publishes.

## Companion repository

`../ignite-training-solutions` — reference implementations, answer keys, authoring
notes. Private, mentor-only (spec D6). Its `tools/cross-check.sh` overlays solutions
onto this repo's skeletons and runs the suites; a milestone shipped without its solution
is not shipped (spec §11.2).
