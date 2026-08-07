# Git Workflow

How you use git at every milestone. One rule, three situations.

---

## The rule

> **Clone what you're reading. Fork what you'll change. Branch to work.**

That is the whole thing. Everything below is the rule applied to a specific week.

You **fork** this repository once, at M0, because it is the only thing you will change for six milestones. You **clone** `Robot-2026` at M2 to read it — no fork, no write, you are a visitor. You do not fork `Robot-2026` until M4, when you finally have something to send back.

---

## M0 — your one pull request to the team repository

This is the only pull request you will ever open against `IgniteRobotics/docs`. Getting it merged past CI **is** what M0 is for.

```bash
# 1. Fork IgniteRobotics/docs on github.com  →  <your-handle>/docs

# 2. Clone YOUR fork, not the team's
git clone git@github.com:<your-handle>/docs.git
cd docs

# 3. Point "upstream" at the team repository so you can pull updates later
git remote add upstream git@github.com:IgniteRobotics/docs.git

# 4. Branch. Never work on main.
git checkout -b add-<your-handle>-contributor

# 5. Do the work: create contributors/<your-handle>.md,
#    and run spotlessApply on the deliberately-ugly FormatMe.java
cd projects/m0-onboarding && ./gradlew spotlessApply && cd ../..

# 6. Commit and push
git add -u
git commit -m "Add <your-handle> to contributors"
git push -u origin add-<your-handle>-contributor
```

Then open the pull request on github.com: `<your-handle>/docs:add-<your-handle>-contributor` → `IgniteRobotics/docs:main`.

> **⚠️ Do this before you open the pull request.** GitHub disables Actions on forked repositories by default. Go to your fork → **Actions** tab → click **"I understand my workflows, go ahead and enable them."** If you skip it, your pull request shows *no checks at all*, which looks broken but isn't. This is the single most common M0 problem.

**What goes in `contributors/<your-handle>.md`:** your GitHub handle and what you want to work on. Nothing else — no legal name, no email, no school. It is yours, you made it, and you can delete it with another pull request whenever you like.

---

## M1 through M6 — same fork, forever

You never fork again. Every milestone is a branch in the fork you already have, and the pull request goes **into your own fork**, where a mentor reviews it.

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

Then open the pull request: `<your-handle>/docs:m3-command-based` → `<your-handle>/docs:main`.

Yes — **into your own fork.** That is not a mistake. Your solutions stay yours, the team repository stays clean, and the next student to reach M3 cannot read your answer. CI runs on your fork and a mentor reviews it there.

| Milestone | Branch name |
|---|---|
| M1 | `m1-java-fundamentals` |
| M2 | `m2-oop` |
| M3 | `m3-command-based` |
| M4 | `m4-control` |
| M5 | `m5-auto-vision` |
| M6 | `m6-testing` |

---

## M4 and M6 — pull requests to the real robot code

At M4 and M6 you also work in `Robot-2026`. Same shape as M0, different repository:

```bash
# Fork IgniteRobotics/Robot-2026 on github.com — once. You keep it.
git clone git@github.com:<your-handle>/Robot-2026.git
cd Robot-2026
git remote add upstream git@github.com:IgniteRobotics/Robot-2026.git
git checkout -b m6-launchcalculator-tests
# ...work...
git push -u origin m6-launchcalculator-tests
```

Pull request goes to `IgniteRobotics/Robot-2026:main`. This is real competition code and a mentor reviews it like anyone else's.

See [the real-repo ladder](robot-repo-ladder.md) for what you do in that repository at each milestone, and the rules about what your tests may and may not depend on.

---

## M7 and M8 — commit access

Once you have cleared the Kickoff Readiness Gate you have write access to `Robot-2026`. No fork, no `upstream` remote — you clone the team repository directly and branch on it, like every other member of the team.

```bash
git clone git@github.com:IgniteRobotics/Robot-2026.git
cd Robot-2026
git checkout -b intake-current-limiting
```

---

## The one-line summary

> **Fork→PR before the Kickoff Gate. Branch after.**

The git ritual and the commit-access policy are the same boundary. That is deliberate: when you stop forking and start branching, it is because the team has decided you can be trusted with the competition robot's code.

---

## When it goes wrong

**"My pull request has no checks."** Actions are not enabled on your fork. See the warning in the M0 section.

**"Git says my branch is behind."** Someone updated the team repository. `git fetch upstream && git merge upstream/main`.

**"I committed to main by accident."** Common, fixable, not a big deal. Make a branch at your current position, then reset main back:

```bash
git branch m3-command-based        # save your work on a new branch
git reset --hard upstream/main     # put main back where it belongs
git checkout m3-command-based      # carry on
```

**"I have a merge conflict."** Stop and ask a mentor. Conflicts are the one git situation genuinely worth learning with someone next to you, and nobody will think less of you for it.
