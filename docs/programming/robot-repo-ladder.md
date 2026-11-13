# The Real-Repo Ladder

When you stop working in a practice project and start working in the code that runs the competition robot.

---

## Four levels

You move up this ladder and never back down. Each step is a bigger permission than the one before it.

| Level | What it means | What you can break |
|---|---|---|
| **browse** | Read a file on github.com | Nothing |
| **clone** | Copy the repository to your laptop to read and explore | Nothing — you have no way to push |
| **fork + PR** | Your own copy on GitHub; you propose changes back | Nothing without a mentor approving it |
| **branch** | You have commit access and work on the team's repository | Real things. This is why it comes last |

---

## The ladder, milestone by milestone

| | Your project | Git ritual | Real repo | What you do there |
|---|---|---|---|---|
| **M0** | `projects/m0-onboarding/` | fork `ignition-labs` → branch → PR into own fork | — | — |
| **M1** | `m1-java-fundamentals/` | branch → PR into own fork | Robot-2026 | **browse** — `IntakeConstants.java`, `Constants.java` |
| **M2** | `m2-oop/` | branch → PR into own fork | Robot-2026 | **clone** — `preferences/`, `LaunchState`, `LaunchRequestBuilder` |
| **M3** | `m3-command-based/` | branch → PR into own fork | Phoenix-2026, Robot-2026 | **clone** — Phoenix end to end, then `RobotContainer` |
| **M4** | `m4-control/` | branch → PR into own fork | Robot-2026 | **clone** to read `ShooterSubsystem`; **fork + PR** for your first real contribution |
| **M5** | `m5-auto-vision/` | branch → PR into own fork | Robot-2026 | **clone** — `vision/`, `LaunchCalculator` |
| **M6** | `m6-testing/` *(warm-up)* | branch → PR into own fork | Robot-2026 | **fork + PR** — tests for a class that has none. **This is the milestone** |
| **M7** | — | **branch on upstream** | Robot-2026 | commit access. Ship a feature |
| **M8** | — | **branch on upstream** | Robot-2026 | commit access. Extend the spec you shipped at M7 |
| **M9** | — | **branch on upstream** | Robot-2026 | commit access, in-season. Own a subsystem |

For the exact commands at each stage, see [the git workflow](git-workflow.md).

---

## Why you only read until M4

Through M3 you are learning what the code *is*. Everything `Robot-2026` is worth to you in those milestones is in the reading:

- **M1** — open `Constants.java` and find three different naming conventions in one file. Nobody wrote the rule down. That is what inconsistency costs.
- **M2** — `preferences/` is an abstract generic class, four subclasses, and `Supplier<T>` doing real work. It is the best file set in the codebase for this milestone.
- **M3** — `Phoenix-2026` is twenty-two files on the same stack as the competition robot, small enough to read in one sitting, and it contains both command idioms.

None of that needs write access. And a change to competition code needs someone who can judge whether it is safe — which is what M3 and M4 are building.

---

## M4 — your first real pull request

Opening one pull request against a real robot repository is a **required** part of finishing M4. A documentation fix or a typo correction counts. The point is the ritual, not the size of the change.

If you want a real one: `DriveMotorConfigs.java` has four near-identical `create*Slot0Configs()` methods, one per swerve corner, each rebuilding the same five fields. CTRE's `.clone()` pattern collapses them. It is about a twenty-minute change and it makes the file substantially shorter.

Do it now rather than at M6. M6's pull request is assessed; this one is practice.

---

## M6 — the milestone that is a pull request

**Your deliverable is a merged pull request adding tests to `Robot-2026`.** The `projects/m6-testing/` project is the warm-up that makes it writable; the real pull request is what you are actually graded on.

Pick your target from [`UNTESTED-TARGETS.md`](UNTESTED-TARGETS.md) — a shortlist of classes in `Robot-2026` worth testing. It is not a queue: nothing is reserved, and it is fine if another student picks the same class you did. If you both land on the same one, whoever merges second rebases and merges their test cases into the class the first student already added, rather than either of you fighting over who called it first.

### "Wait, isn't that circular?"

Good catch, and no.

The Kickoff Gate says you need M6 before you get commit access to `Robot-2026`. M6 says your deliverable is a pull request to `Robot-2026`. That looks like a loop.

It isn't, because **the pull request comes from a fork.** A fork needs no commit access from anyone — that is the entire reason forks exist, and it is exactly why fork-and-pull-request is the ritual for everything before the Gate. You are proposing a change, not making one. A mentor still has to merge it.

### The hard rule about what you may test

> **Pure logic only. No timing, no simulation, no hardware.**

Test `LaunchCalculator`, the `LaunchRequestBuilder` implementations, unit conversions, state-machine transitions. Things that take numbers in and give numbers out.

**Why this is a rule and not a suggestion:** `Robot-2026`'s CI runs `./gradlew build`, which runs the tests. The moment your pull request merges, every team pull request from then on has to pass them. If your test depends on timing, it will pass on your laptop and fail intermittently in CI — and in February that means a test you wrote in November is blocking somebody's competition fix at 11pm.

`PhoenixTestSupport.java` and its simulation delays stay in `projects/m6-testing/`. That is what the practice project is for.

### What your pull request actually does for the team

`Robot-2026` has forty-seven Java files and, as of August 2026, **no `src/test` directory at all.** JUnit is configured in `build.gradle` and has never run.

So the first M6 pull request does not just add tests. `./gradlew build` runs `check`, `check` runs `test` — meaning that from the moment it merges, **the team's CI is gating on tests for the first time.** You are not doing an exercise. You are turning something on.

---

## M7, M8 and M9 — you are on the team

Past the Gate, you have commit access and you branch directly on `Robot-2026`. No fork. At that point the ladder is finished and you work the way everyone else does.
