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

Deliberately narrower than all of git. This mirrors a pattern used by other FRC teams: hand
rookies an explicit allow-list rather than the whole tool.

The third column is the same command, done by clicking. Neither one is the "real" way — the
panel is running git for you, and mostly these exact commands. Learn them as one thing, because when something goes
wrong you will need to describe it to a mentor, and "the checkmark is greyed out" is much
harder to help with than "`git commit` says nothing is staged."

| Command | What it does | In VS Code |
|---|---|---|
| `git clone <url>` | Copy a repository to your machine, once | `Ctrl+Shift+P` → **Git: Clone** |
| `git checkout -b <name>` | Make a new branch and switch to it | Branch name in the status bar (bottom left) → **Create new branch** |
| `git status` | What have I changed? Run this constantly | The Source Control panel's file list **is** `git status` |
| `git add -u` | Stage the files you have modified | **+** on a single file stages that file. **+** on the **Changes** header stages *everything*, including files git has never seen before — which `git add -u` does not do |
| `git commit -m "message"` | Save a snapshot with a note | Type the message in the box, then **✓ Commit** |
| `git push` | Send your commits to GitHub | **Sync Changes**, or the ⟳ in the status bar. Sync pulls before it pushes; on a branch you have not pushed before the button reads **Publish Branch** |
| `git fetch --all` | Get everyone else's commits, without applying them | Source Control **⋯** → **Fetch** → **Fetch From All Remotes** |
| `git merge origin/main` | Apply main's changes to your branch | Source Control **⋯** → **Branch** → **Merge** |

**VS Code moves these menu items between releases.** If a path here does not match what you
see, the application is right and this table is stale — tell a mentor so it gets fixed. You
have not done anything wrong and you are not lost.

**Not on this list: `rebase`, `reset`, `cherry-pick`, `force push`.** They are useful and you
will learn them eventually. They can also destroy work irrecoverably, and you should not meet
them before you can recover from them. If you think you need one, ask a mentor.

**That exclusion applies to the menus too.** The Source Control **⋯** menu contains `rebase`,
`reset` and force-push, and a menu item destroys work exactly as thoroughly as a typed
command — faster, in fact, because you did not have to think about spelling it. The list
above is the whole allow-list, whichever way you drive it.

**Commit messages.** Explain *why*, not *what*. The diff already shows what. "Fixed bug" is useless six weeks later; "Fix indexer stalling when a game piece is already loaded at init" is not.

### Pull requests and CI

A pull request proposes merging your branch into `main`. It is where code review happens — and code review is not a judgment of you. It is how the team catches things and how you learn faster than by yourself.

Until the Kickoff Gate, every pull request you open goes from a branch in your fork into
your fork's own `main`. CI runs in your fork, which is why Actions have to be on there.

Ignite's CI (`.github/workflows/student-check.yml`) runs in two stages, and which one
you see depends on what you changed:

- **Every pull request** runs a matrix job: it checks that the given tests are
  unmodified and validates contributor files. This always runs, including on your M0
  pull request.
- **The build-and-format job runs only for the milestone project your pull request
  touches** — `./gradlew build` (does it compile?) and `spotlessCheck` (is it
  formatted per Google Java Format?). A pull request that only touches
  `contributors/<your-handle>.md`, which is what M0 asks you to open, changes no
  milestone project, so **this job does not run at all.** That is expected, not a
  broken setup — and it is exactly why enabling Actions on your fork matters so much
  at M0: with Actions off, a pull request shows *no checks at all* too, and the two
  look identical unless you already know which one you're looking at.

From M1 onward your pull request touches exactly one project, so the build-and-format
job runs and checks it.

**Why formatting is enforced by a robot.** So nobody argues about it, and so diffs show real changes instead of whitespace noise. If `spotlessCheck` fails, run `./gradlew spotlessApply` and commit the result. It is not a criticism, it is a rule that applies identically to everyone including mentors.

`Robot-2026` runs a second workflow that AI-reviews every pull request diff. You meet
it at M7 — it is not part of `ignition-labs`.

### How the team works, and when you join it

Everything above is how *you* work right now: fork, branch, one pull request. The team
itself, on the competition robot's code, works differently — `main` is competition-ready code
only, features merge into a `development` branch first, and competitions get `hotfix`
branches and release tags.

You do not do any of that yet, and you do not have a repository to do it in. But you will
clone `Robot-2026` read-only at M2, and you will find a `development` branch that nothing in
[the git workflow](../git-workflow.md) mentions. Read [the team gitflow](../team-gitflow.md)
now so that is recognition rather than confusion.

It becomes what you actually do at **M7**, when clearing the Kickoff Readiness Gate gets you
commit access.

## Project

**Ship a real pull request.**

The project is `projects/m0-onboarding/`. It ships with a real thing to fix — `FormatMe.java`, deliberately misformatted — and a real thing to add: yourself, to the contributors list. Its own README walks the build and the simulator. The exact git commands — fork, branch, commit, push, pull request — are in [the git workflow](../git-workflow.md); this milestone does not repeat them.

1. Fork and clone `ignition-labs`, per the git workflow's M0 section
2. Work through `projects/m0-onboarding/README.md`: build it, run the simulator, fix `FormatMe.java`'s formatting and then revert the fix, add your contributor file
3. Commit only the contributor file, with a message that explains why — the formatting fix is never shipped, so the next student gets the same exercise

⚠️ **Enable Actions on your fork before you open the pull request.** GitHub disables Actions on forked repositories by default. Until you turn them on, your pull request shows **no checks at all** — which looks like the repository is broken, and it isn't. Go to your fork on github.com → **Actions** tab → click **"I understand my workflows, go ahead and enable them."** Spec §15 rates this the single most likely thing to go wrong at M0.

4. Push and open the pull request **into your own fork**. GitHub defaults the base repository to the team's, so change it: see [the git workflow](../git-workflow.md#opening-a-pull-request-in-your-own-fork)
5. Get it through CI. Student Check validates your contributor file; if it fails, the message says what to change
6. Respond to at least one review comment from a mentor or veteran reviewer
7. Once they approve, merge it yourself

The change deliberately does not need to be impressive. The point is the pipeline, not the diff.

**Optional hardware variant.** Deploy any working robot project to last season's robot and drive it. Not required, but seeing code you built reach real hardware is worth the twenty minutes.

## Definition of done

- [ ] WPILib VS Code installed; `./gradlew build` succeeds on your machine
- [ ] The simulator launches and you can identify the Robot State panel
- [ ] GitLens, Git Graph and Live Share are installed, and you can find the Source Control panel
- [ ] A pull request into your own fork, approved by a mentor or veteran reviewer, and merged by you
- [ ] You ran `spotlessApply` on `FormatMe.java`, read the diff, reverted it, and can say what the formatter changed
- [ ] You can explain what a branch is without using the word "branch"
- [ ] You can point at `development` on the team gitflow diagram and say why it exists
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
- **Ignite:** `docs/programming/team-gitflow.md` — how the team works on the competition robot, and when that becomes how you work
- **Ignite:** `ignition-labs/.github/workflows/student-check.yml` — the CI your M0 pull request must pass
