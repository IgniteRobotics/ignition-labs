# M0 — Onboarding

This project exists so your first pull request has something real in it. You are
not going to write robot code yet. You are going to prove your laptop works and
that you can get a change reviewed and merged.

Read [the milestone](../../docs/programming/milestones/m0-tooling-and-git.md) first.
The exact git commands are in [the git workflow](../../docs/programming/git-workflow.md).

---

## What you do

**1. Meet the formatter.**

```bash
./gradlew spotlessCheck
```

This fails, and names `src/main/java/frc/robot/FormatMe.java`. That is
deliberate — the file is formatted wrong on purpose, and this is what a
real formatting violation looks like when CI catches one.

**2. Fix it.**

Do not fix it by hand:

```bash
./gradlew spotlessApply
```

Look at the diff afterward. That is what the formatter does, and it is why nobody
on this team argues about braces in code review.

**3. Revert it.**

```bash
git checkout src/main/java/frc/robot/FormatMe.java
```

Yes, undo the fix you just made. The exercise's whole value was running the
formatter and reading the diff — not shipping the result. If your fix landed on
`main`, the next student to reach M0 would open `FormatMe.java` already
correctly formatted, and would run `spotlessApply` against nothing. Reverting
keeps the exercise honest for whoever comes after you.

**4. Build it.**

```bash
./gradlew build
```

The first run downloads a lot and can take several minutes. That is normal. It
gets fast after that. This proves the toolchain works end to end.

**5. Run the simulator.**

In VS Code: `Ctrl+Shift+P` (`Cmd+Shift+P` on macOS) → **WPILib: Simulate Robot Code** → check **Sim GUI** → OK.

A window opens with a Robot State panel and a Joysticks panel. That window is the
robot as far as your laptop is concerned. Find the Robot State panel and switch
between Disabled, Autonomous, and Teleoperated. Nothing will happen — this robot
does nothing on purpose. You are checking that the toolchain works.

**6. Add yourself to the contributors list.**

Copy `templates/CONTRIBUTOR.md` from the repository root to
`contributors/<your-github-handle>.md` and fill it in. GitHub handle only — no
legal name, no email, no school. Your fork is public, so anyone can read it. It
is your file and you can delete it whenever you want.

**7. Open the pull request.**

Your branch and your pull request carry **only** `contributors/<your-handle>.md`.
The formatting fix from steps 2–3 is not in there — you reverted it in step 3,
on purpose. This one goes **into your own fork**, `<your-handle>/ignition-labs`,
not to `IgniteRobotics/ignition-labs`. GitHub picks the team's repository as the
base by default, so change the base repository before you create it. The
commands, and that setting, are in the git workflow. A mentor or veteran
reviewer approves it, and then you merge it.

⚠️ **Before you open it:** go to your fork on github.com → **Actions** tab →
click **"I understand my workflows, go ahead and enable them."** GitHub disables
Actions on forks by default, and without this your pull request shows no checks
at all, which looks broken but isn't.

---

## Done when

- `./gradlew spotlessCheck` failed, naming `FormatMe.java`, before you touched it
- `./gradlew spotlessApply` fixed it, and you reverted the fix afterward
- `./gradlew build` succeeds on your machine
- The simulator opens and you can point at the Robot State panel
- `contributors/<your-handle>.md` is the only file in your pull request
- Your pull request into your own fork is green, approved, and merged
- You can explain what a branch is without using the word "branch"
