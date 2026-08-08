# M0 — Tooling, Git & the Build

## At a glance

You get a working development environment, and you learn to contribute code the way the team actually does it: on a branch, through a pull request, past CI.

This milestone has almost nothing to do with programming. It is about surviving the mechanics of contribution. A student who cannot open a passing pull request in October cannot contribute in January, no matter how well they can code.

**Estimated time:** 2–4 sessions (Tracks A and B). Minutes (Tracks C and D, via test-out).
**Prerequisites:** None. This is the start of the ladder.
**AI posture:** 🔴 Red

## Who enters here

**Everyone.** All four tracks start at M0.

Tracks C and D will normally clear the test-out immediately — if you already have the toolchain and have used git professionally, this takes fifteen minutes. Tracks A and B work the milestone.

The reason M0 is universal even for experienced developers: the FRC toolchain is not a normal Java toolchain. A strong OO developer who has never touched Gradle, the WPILib VS Code extension, or a roboRIO deploy will stall immediately in M3 if they skip this.

## Test-out challenge

Two things, both demonstrated in front of a mentor:

1. Show a merged pull request on any repository that passed a CI check
2. Run `./gradlew build` successfully on an FRC project on your own machine

**Pass criteria:** both work. This is the only test-out in the curriculum that is pass-on-sight rather than assessed — either the environment works or it does not.

**Time:** 15 minutes.

## Concepts

### The toolchain

**WPILib VS Code.** Not regular VS Code. WPILib ships its own build with the FRC extension, the correct JDK, and project templates preinstalled. Install the version matching the current season — mixing seasons causes failures that look like code problems but are not.

**What Gradle does.** Gradle is the build system. When you run `./gradlew build` it downloads dependencies, compiles your Java, and runs any tests. When you run `./gradlew deploy` it does all that and then pushes the result to the roboRIO. The `gradlew` script is checked into the repo deliberately so that everyone builds with the identical Gradle version.

**Vendordeps.** The `vendordeps/` folder holds JSON files describing third-party libraries — CTRE Phoenix, PathPlanner, PhotonVision. They are how the build knows where to fetch vendor code. Ignite's `Robot-2026` has four. You will rarely edit these, but when a build breaks after someone else's commit, this is often where to look.

**Reading a stack trace.** When Java crashes it prints a trace. Read it top to bottom: the first line is the exception type and message, and the first line mentioning `frc.robot` is almost always your bug. Everything below that is library internals.

This is a skill, and it is one of the specific things the Red AI tier exists to protect. Learning to read a trace takes maybe two hours of frustration spread across a few sessions. Skipping that leaves you dependent forever.

### Git, as a concept before a command

Do not start with commands. Start with the model.

**A commit is a save point.** Not a backup — a labeled, permanent snapshot of the whole project with a note explaining why. Good projects have many small commits, each of which does one comprehensible thing.

**A branch is a parallel universe.** You copy the project, make changes without affecting anyone, and later decide whether to merge them back. This is what makes it safe for six people to work on one robot's code simultaneously.

**A merge reconciles universes.** Usually automatic. When two people changed the same lines, git asks you to resolve the conflict by hand.

**A remote is the shared copy** — GitHub. Your local repo and the remote are separate; `push` and `pull` move commits between them.

### The commands you actually need

Deliberately narrower than all of git. This mirrors a pattern used by other FRC teams: hand rookies an explicit allow-list rather than the whole tool.

| Command | What it does |
|---|---|
| `git clone <url>` | Copy a repository to your machine, once |
| `git checkout -b <name>` | Make a new branch and switch to it |
| `git status` | What have I changed? Run this constantly |
| `git add -u` | Stage the files you have modified |
| `git commit -m "message"` | Save a snapshot with a note |
| `git push` | Send your commits to GitHub |
| `git fetch --all` | Get everyone else's commits, without applying them |
| `git merge origin/main` | Apply main's changes to your branch |

**Not on this list: `rebase`, `reset`, `cherry-pick`, `force push`.** They are useful and you will learn them eventually. They can also destroy work irrecoverably, and you should not meet them before you can recover from them. If you think you need one, ask a mentor.

**Commit messages.** Explain *why*, not *what*. The diff already shows what. "Fixed bug" is useless six weeks later; "Fix indexer stalling when a game piece is already loaded at init" is not.

### Pull requests and CI

A pull request proposes merging your branch into `main`. It is where code review happens — and code review is not a judgment of you. It is how the team catches things and how you learn faster than by yourself.

Ignite's CI runs automatically on every PR (`.github/workflows/student-check.yml`). It does two things:

- `./gradlew build` — does the code compile?
- `spotlessCheck` — is it formatted per Google Java Format?

**Why formatting is enforced by a robot.** So nobody argues about it, and so diffs show real changes instead of whitespace noise. If `spotlessCheck` fails, run `./gradlew spotlessApply` and commit the result. It is not a criticism, it is a rule that applies identically to everyone including mentors.

There is also a second workflow that runs an AI reviewer against every PR diff. Its comments are suggestions, not orders. Read them, and push back when it is wrong — it is frequently wrong.

## Project

**Ship a real pull request.**

The project is `projects/m0-onboarding/`. It ships with a real thing to fix — `FormatMe.java`, deliberately misformatted — and a real thing to add: yourself, to the contributors list. Its own README walks the build and the simulator. The exact git commands — fork, branch, commit, push, pull request — are in [the git workflow](../git-workflow.md); this milestone does not repeat them.

1. Fork and clone `ignition-labs`, per the git workflow's M0 section
2. Work through `projects/m0-onboarding/README.md`: build it, run the simulator, fix `FormatMe.java`'s formatting, add your contributor file
3. Commit both changes with a message that explains why

⚠️ **Enable Actions on your fork before you open the pull request.** GitHub disables Actions on forked repositories by default. Until you turn them on, your pull request shows **no checks at all** — which looks like the repository is broken, and it isn't. Go to your fork on github.com → **Actions** tab → click **"I understand my workflows, go ahead and enable them."** Spec §15 rates this the single most likely thing to go wrong at M0.

4. Push and open the pull request — upstream, the only one you send there
5. Get it through CI. If `spotlessCheck` fails, fix it yourself
6. Respond to at least one review comment
7. Merge it

The change deliberately does not need to be impressive. The point is the pipeline, not the diff.

**Optional hardware variant.** Deploy any working robot project to last season's robot and drive it. Not required, but seeing code you built reach real hardware is worth the twenty minutes.

## Definition of done

- [ ] WPILib VS Code installed; `./gradlew build` succeeds on your machine
- [ ] The simulator launches and you can identify the Robot State panel
- [ ] A merged pull request exists under your GitHub account
- [ ] `spotlessCheck` passed without a mentor fixing it for you
- [ ] You can explain what a branch is without using the word "branch"
- [ ] You can point at the first line of a stack trace that refers to our code
- [ ] Copilot or equivalent is disabled in your editor

## Common failure modes

**Wrong WPILib version.** Installing last season's WPILib, or using regular VS Code with an extension bolted on. Symptom: mystifying build errors that look like code problems. Check the version first, always.

**Committing to `main` directly.** Habit from solo projects. Symptom: your work and someone else's tangle immediately. Always branch first.

**Enormous first commits.** Fifty files changed with the message "stuff." Nobody can review it and you cannot undo part of it. Commit small.

**Panicking at a merge conflict.** They look alarming and are usually trivial. Git marks both versions in the file; pick the right one, delete the markers, commit. Ask for help the first time.

**Not running `git status`.** Most confusion at this stage dissolves the moment you look at what git thinks is going on. Run it constantly.

**Trying to fix a bad state with commands you found online.** This is how work gets destroyed. If git is in a state you do not understand, stop and get a mentor. Nothing is broken until someone types `reset --hard`.

## AI posture — 🔴 Red

**Allowed:** asking what an error message means. Asking what a git concept is. Asking for a different explanation when one did not land.

**Not allowed:** asking for git commands to paste without understanding them. This is the fastest way to lose work at this stage, because the commands that fix a bad git state are also the ones that destroy history.

Disable Copilot or equivalent inline completion now, and leave it off through M2. See `ai-use-standard.md` §4.

## Resources

See `resources.md` for the full library. For this milestone:

- **WPILib Zero to Robot** — the official setup path, start to finish
- **WPILib Git Version Control Introduction** — introductory git, and a warning not to hand-edit the generated `.gitignore`
- **Robot Code CI with GitHub Actions** — what the PR check is doing
- **Team 3255 FRC Java Tutorial** — a second explanation of environment setup if ours did not land
- **Chief Delphi: "How does your team use git and GitHub?"** — where the curated command list comes from
- **Ignite:** `Robot-2026/.github/workflows/pr-build-check.yml` — the CI your PR must pass
