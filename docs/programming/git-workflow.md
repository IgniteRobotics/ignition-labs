# Git Workflow

How you use git at every milestone. One rule, three situations.

---

## The rule

> **Clone what you're reading. Fork what you'll change. Branch to work.**

That is the whole thing. Everything below is the rule applied to a specific week.

You **fork** this repository once, at M0, because it is the only thing you will change for six milestones. You **clone** `Robot-2026` at M2 to read it — no fork, no write, you are a visitor. You do not fork `Robot-2026` until M4, when you finally have something to send back.

---

## M0 — your first pull request, into your own fork

You fork once, here, and every pull request from M0 to M6 goes **into your own fork**. Getting this first one reviewed and merged past CI **is** what M0 is for.

```bash
# 1. Fork IgniteRobotics/ignition-labs on github.com  →  <your-handle>/ignition-labs

# 2. Turn on Actions in your fork (see the warning below). Do it now, before
#    anything else, so your first push already runs CI.

# 3. Clone YOUR fork, not the team's
git clone https://github.com/<your-handle>/ignition-labs.git
cd ignition-labs

# 4. Point "upstream" at the team repository so you can pull updates later
git remote add upstream https://github.com/IgniteRobotics/ignition-labs.git

# 5. Branch. Never work on main.
git checkout -b m0-onboarding

# 6. Do the exercise: run spotlessApply on the deliberately-ugly FormatMe.java,
#    read the diff, then revert it -- the fix is never committed. The next
#    student needs the same violation to find.
cd projects/m0-onboarding
./gradlew spotlessApply
git checkout src/main/java/frc/robot/FormatMe.java
cd ../..

# 7. Create contributors/<your-handle>.md, then commit and push.
#    Only the contributor file goes in -- there is no formatting fix left to
#    stage after step 6's revert.
git add contributors/<your-handle>.md
git commit -m "Add <your-handle> to contributors"
git push -u origin m0-onboarding
```

> **⚠️ Turn on Actions in your fork first.** GitHub disables Actions on forked repositories by default. Go to your fork → **Actions** tab → click **"I understand my workflows, go ahead and enable them."** If you skip it, your pull request shows *no checks at all*, which looks broken but isn't. This is the single most common M0 problem.

Then open the pull request on github.com: `<your-handle>/ignition-labs:m0-onboarding` → `<your-handle>/ignition-labs:main`. [Opening a pull request in your own fork](#opening-a-pull-request-in-your-own-fork), below, shows the one setting GitHub gets wrong for you.

A mentor or a veteran reviewer approves it. **Then you merge it yourself**: it is your fork, so the merge button is yours.

**What goes in `contributors/<your-handle>.md`:** your GitHub handle and what you want to work on. Nothing else — no legal name, no email, no school. Your fork is public, so anyone can read this file. It is yours, you made it, and you can delete it whenever you like.

---

## M1 through M6 — same fork, forever

You never fork again. Every milestone is a branch in the fork you already have, and the pull request goes **into your own fork**, where a mentor or veteran reviewer approves it, exactly like M0.

```bash
# Start of every milestone: get the latest skeletons from the team repo
git checkout main
git fetch upstream
git merge upstream/main
git push

# Branch for this milestone
git checkout -b m3-command-based

# ...work in projects/m3-command-based/ ...

git add -u
git commit -m "M3: drivetrain subsystem and command composition"
git push -u origin m3-command-based
```

Then open the pull request: `<your-handle>/ignition-labs:m3-command-based` → `<your-handle>/ignition-labs:main`.

Yes — **into your own fork.** That is not a mistake. The team repository stays clean, and your work stays in your fork, where CI runs and your reviewer reads it. Your fork is public, though: it is not a place to hide an answer, and other students can read it the same way you can read theirs.

| Milestone | Branch name |
|---|---|
| M0 | `m0-onboarding` |
| M1 | `m1-java-fundamentals` |
| M2 | `m2-oop` |
| M3 | `m3-command-based` |
| M4 | `m4-control` |
| M5 | `m5-auto-vision` |
| M6 | `m6-testing` |

### Opening a pull request in your own fork

GitHub assumes a pull request from a fork is meant for the repository you forked from. On the **Open a pull request** page, check the top line before you click anything else:

- **base repository:** change it from `IgniteRobotics/ignition-labs` to **`<your-handle>/ignition-labs`**
- **base:** `main`
- **compare:** your milestone branch

From the terminal, name the base repository explicitly:

```bash
gh pr create --repo <your-handle>/ignition-labs --base main
```

If GitHub tells you that you cannot open a pull request on `IgniteRobotics/ignition-labs`, this is why. The team repository only accepts pull requests from collaborators, and you were never meant to send one there. Change the base repository and try again.

### For reviewers

You are approving a gate, so the approval is the record that this student passed it. The student owns their fork and could merge without you, or change the CI that checks them, so read the diff with that in mind.

- [ ] Student Check is green on the pull request
- [ ] The diff touches only the milestone's own project (at M0, only `contributors/<handle>.md`)
- [ ] Nothing under `.github/`, nothing in `tests.lock`, and nothing under any `src/test/` that the milestone ships. If one of those changed, talk to the student before you approve. They may have found a genuinely bad test.
- [ ] The milestone's mentor-verified items, from its Definition of done, are covered in review
- [ ] Approve. The student merges.

---

## M4 and M6 — pull requests to the real robot code

At M4 and M6 you also work in `Robot-2026`. You set up the fork the same way as M0, but this pull request goes to the team's repository, not your fork:

```bash
# Fork IgniteRobotics/Robot-2026 on github.com — once. You keep it.
git clone https://github.com/<your-handle>/Robot-2026.git
cd Robot-2026
git remote add upstream https://github.com/IgniteRobotics/Robot-2026.git
git checkout -b m6-launchcalculator-tests
# ...work...
git push -u origin m6-launchcalculator-tests
```

Pull request goes to `IgniteRobotics/Robot-2026:main`. This is real competition code and a mentor reviews it like anyone else's.

See [the real-repo ladder](robot-repo-ladder.md) for what you do in that repository at each milestone, and the rules about what your tests may and may not depend on.

---

## M7, M8 and M9 — commit access

Once you have cleared the Kickoff Readiness Gate you have write access to `Robot-2026`. No fork, no `upstream` remote — you clone the team repository directly and branch on it, like every other member of the team.

```bash
git clone https://github.com/IgniteRobotics/Robot-2026.git
cd Robot-2026
git checkout -b feature/intake-current-limiting
```

This is where [the team gitflow](team-gitflow.md) stops being background reading and starts
being what you do: your branch is `feature/<name>`, your pull request targets `development`
rather than `main`, and `main` is competition code you never push to directly. You read that
page at M0 — read it again now, because from here it describes your week.

---

## The one-line summary

> **Fork→PR before the Kickoff Gate. Branch after.**

The git ritual and the commit-access policy are the same boundary. That is deliberate: when you stop forking and start branching, it is because the team has decided you can be trusted with the competition robot's code.

---

## When it goes wrong

**"My pull request has no checks."** Actions are not enabled on your fork. See the warning in the M0 section.

**"GitHub won't let me open the pull request" or "it went to `IgniteRobotics`."** The base repository is the team's, not yours. See [Opening a pull request in your own fork](#opening-a-pull-request-in-your-own-fork).

**`git push` asks for a username and password, and my GitHub password does not work.** You skipped the authentication step. See [Setup](../SETUP.md), step 3.

**"Git says my branch is behind."** Someone updated the team repository. `git fetch upstream && git merge upstream/main`.

**"I committed to main by accident."** Common, fixable, not a big deal. Make a branch at your current position — this is the whole fix, and it is not destructive:

```bash
git branch m3-command-based        # save your work on a new branch
```

Main still has the extra commit sitting on it after that. Putting main back uses a
command that can destroy uncommitted work (`reset --hard`), and it is not on M0's
command list for exactly that reason — show a mentor and do it together the first
time.

**"I have a merge conflict."** Stop and ask a mentor. Conflicts are the one git situation genuinely worth learning with someone next to you, and nobody will think less of you for it.
