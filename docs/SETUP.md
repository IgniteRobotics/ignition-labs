# Setup

Get a working development environment. Do this once. Budget an hour the first time
and do not be discouraged — installing toolchains is genuinely the least fun part
of this and it never gets harder than today.

---

## 1. Install WPILib 2026

Download the 2026 WPILib installer for your platform from the
[WPILib releases page](https://github.com/wpilibsuite/allwpilib/releases) and run it.
Choose **"Everything"** when it asks, and **"Install for this User"** unless you
have a reason not to.

This installs a bundled JDK 17, a bundled VS Code, and the WPILib extension.
Use the VS Code it installs — not one you already had. The bundled one knows
where the JDK is.

## 2. Verify Java

Open the WPILib VS Code, then a terminal inside it (`` Ctrl+` ``):

```bash
java -version
```

Expected: version 17. If you see something else, VS Code is not using the bundled
JDK — ask a mentor rather than installing another Java.

## 3. Fork and clone this repository

Follow [the git workflow](programming/git-workflow.md), M0 section. It has the
exact commands.

## 4. ⚠️ Enable Actions on your fork

**Do this before you open your first pull request.**

GitHub disables Actions on forked repositories by default. Until you turn them on,
your pull requests show **no checks at all** — which looks like the repository is
broken, and it isn't.

1. Go to your fork on github.com
2. Click the **Actions** tab
3. Click the green **"I understand my workflows, go ahead and enable them"** button

If you have already opened a pull request without doing this, enable Actions and
then push any new commit to that branch — the checks will run.

## 5. Verify the build

```bash
cd projects/m0-onboarding
./gradlew build
```

Expected: `BUILD SUCCESSFUL`. The first run downloads a lot and can take several
minutes; later runs take seconds.

If it fails, copy the **whole** error — not a screenshot of part of it — and bring
it to a mentor. Build errors are diagnosable from their text and nearly impossible
to guess at from a description.

## 6. Verify the simulator

In VS Code: `Ctrl+Shift+P` (`Cmd+Shift+P` on macOS) → **"WPILib: Simulate Robot Code"** → check
**"Sim GUI"** → OK.

Expected: a window with a Robot State panel and a Joysticks panel. That window is
the robot, as far as your laptop is concerned. You will spend a lot of time here —
every project in this curriculum is completable without touching hardware.

---

## Known problems

**`./gradlew: Permission denied` on macOS or Linux.** Run `chmod +x gradlew`.

**"No Java executable found."** You are in a VS Code that isn't the WPILib one.
Close it and open the WPILib VS Code from your applications folder.

**The build downloads forever on shop wifi.** Expected the first time. Start it,
work on something else, come back. Later builds are cached.

**"Could not resolve all files for configuration."** Usually a network problem
mid-download. Run it again before assuming anything is broken.

**"Gradle rejects Java version at configuration time."** If your shell has a custom
`JAVA_HOME` pointing to Java 11 or earlier (from `jenv`, `sdkman`, or similar),
GradleRIO fails before any build task runs. Check `java -version` and `echo $JAVA_HOME`
in the same terminal where the build fails. Either unset `JAVA_HOME` to use the
WPILib bundled JDK, or point it explicitly to the WPILib one.
