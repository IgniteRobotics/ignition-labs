# M0 — Onboarding

This project exists so your first pull request has something real in it. You are
not going to write robot code yet. You are going to prove your laptop works and
that you can get a change reviewed and merged.

Read [the milestone](../../docs/programming/milestones/m0-tooling-and-git.md) first.
The exact git commands are in [the git workflow](../../docs/programming/git-workflow.md).

---

## What you do

**1. Build it.**

```bash
./gradlew build
```

The first run downloads a lot and can take several minutes. That is normal. It
gets fast after that.

**2. Run the simulator.**

In VS Code: `Ctrl+Shift+P` (`Cmd+Shift+P` on macOS) → **WPILib: Simulate Robot Code** → check **Sim GUI** → OK.

A window opens with a Robot State panel and a Joysticks panel. That window is the
robot as far as your laptop is concerned. Find the Robot State panel and switch
between Disabled, Autonomous, and Teleoperated. Nothing will happen — this robot
does nothing on purpose. You are checking that the toolchain works.

**3. Fix the broken formatting.**

`src/main/java/frc/robot/FormatMe.java` is formatted wrong on purpose. Do not fix
it by hand:

```bash
./gradlew spotlessApply
```

Look at the diff afterward. That is what the formatter does, and it is why nobody
on this team argues about braces in code review.

**4. Add yourself to the contributors list.**

Copy `templates/CONTRIBUTOR.md` from the repository root to
`contributors/<your-github-handle>.md` and fill it in. GitHub handle only — no
legal name, no email, no school. It is your file and you can delete it with
another pull request whenever you want.

**5. Open the pull request.**

One pull request containing both the formatting fix and your contributor file.
This one goes **upstream**, to `IgniteRobotics/ignition-labs` — it is the only
pull request you will ever send there. The commands are in the git workflow.

⚠️ **Before you open it:** go to your fork on github.com → **Actions** tab →
click **"I understand my workflows, go ahead and enable them."** GitHub disables
Actions on forks by default, and without this your pull request shows no checks
at all, which looks broken but isn't.

---

## Done when

- `./gradlew build` succeeds on your machine
- The simulator opens and you can point at the Robot State panel
- `./gradlew spotlessCheck` passes
- Your pull request is green and merged
- You can explain what a branch is without using the word "branch"
