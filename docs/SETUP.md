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

## 1b. Install three extensions

The installer already gave you the WPILib extension and Java language support. Add three
more. All three exist for the same reason: git is much easier to understand when you can
see it.

| Extension | Marketplace ID | What it gives you |
|---|---|---|
| **GitLens** | `eamodio.gitlens` | The name of whoever last changed a line, shown on the line, with the commit message. "Why is this here?" becomes a question you can answer yourself. |
| **Git Graph** | `mhutchie.git-graph` | Your branches and merges, drawn. This is the same picture as [the team gitflow](programming/team-gitflow.md) diagram, except it is your actual history. |
| **Live Share** | `ms-vsliveshare.vsliveshare` | A mentor can join your editor and help without standing behind you, including from home. |

Install them from the Extensions view — `Ctrl+Shift+X` (`Cmd+Shift+X` on macOS), paste the
ID, click **Install**. Or open `projects/m0-onboarding` as a folder and VS Code will offer
all three at once; the repository ships the recommendation.

**None of these is required.** The build, the deploy, and every CI check work without them.
If you are on a locked-down laptop that will not install extensions, you can still complete
every milestone — say so to a mentor rather than quietly falling behind.

> **⚠️ Live Share shares your screen's contents, not just a file.** Whoever joins can see
> your files and your terminal. A mentor starts the session, you accept it, and **you end it
> when you are done** — check that the Live Share panel shows no active session before you
> close your laptop.

**Not on this list: Copilot, or any other AI autocomplete.** M0 through M2 are 🔴 Red tier
and inline AI completion is turned off for all three. If your editor came with it, disable it
now — see [the AI use standard](programming/ai-use-standard.md).

## 2. Verify Java

Open the WPILib VS Code, then a terminal inside it (`` Ctrl+` ``):

```bash
java -version
```

Expected: version 17. If you see something else, VS Code is not using the bundled
JDK — ask a mentor rather than installing another Java.

## 3. Git and GitHub

You need four things before the next step, and nothing more:

1. **A GitHub account.** Free, at [github.com](https://github.com).
2. **Git, installed and working.** The WPILib installer does **not** ship git.
   ```bash
   git --version
   ```
   macOS gets it from the Xcode Command Line Tools (`xcode-select --install` if the
   command above fails). Windows gets it from
   [Git for Windows](https://gitforwindows.org/), which also gives you a terminal.
3. **Your identity.** Every commit records who made it:
   ```bash
   git config --global user.name "Your Name"
   git config --global user.email "you@example.com"
   ```
4. **Authentication for pushing.** Every repository in this curriculum is public, so
   **cloning and reading need no credentials at all.** Pushing your own commits does —
   GitHub has not accepted your account password for that since 2021. Do this once:
   ```bash
   gh auth login        # choose GitHub.com, then HTTPS, then log in with a browser
   ```
   No `gh`? A **personal access token** works in place of a password. A token is a
   password — ask a mentor rather than pasting one in from a tutorial.

What it looks like when you skip one of these:

> `remote: Support for password authentication was removed`, or a username-and-password
> prompt on `git push` — you skipped step 4. Run `gh auth login`.
>
> `Author identity unknown` / `Please tell me who you are` — you skipped `git config`.

## 4. Fork and clone this repository

Follow [the git workflow](programming/git-workflow.md), M0 section. It has the
exact commands.

## 5. ⚠️ Enable Actions on your fork

**Do this before you open your first pull request.**

GitHub disables Actions on forked repositories by default. Until you turn them on,
your pull requests show **no checks at all** — which looks like the repository is
broken, and it isn't.

1. Go to your fork on github.com
2. Click the **Actions** tab
3. Click the green **"I understand my workflows, go ahead and enable them"** button

If you have already opened a pull request without doing this, enable Actions and
then push any new commit to that branch — the checks will run.

## 6. Verify the build

Before you build anything, check the JDK the WPILib installer bundled is actually the
one in use — this is the single most likely build failure on a machine that already
had a JDK on it:

```bash
java -version        # must say 17
echo $JAVA_HOME       # if this points anywhere but the WPILib JDK, that is your problem
```

If GradleRIO's actual error is *"Dependency requires at least JVM runtime version
17,"* this is why — you landed here for a reason.

```bash
cd projects/m0-onboarding
./gradlew build
```

Expected: `BUILD SUCCESSFUL`. The first run downloads a lot and can take several
minutes; later runs take seconds.

If it fails, copy the **whole** error — not a screenshot of part of it — and bring
it to a mentor. Build errors are diagnosable from their text and nearly impossible
to guess at from a description.

## 7. Verify the simulator

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

**"Gradle rejects Java version at configuration time," or GradleRIO says
*"Dependency requires at least JVM runtime version 17."*** If your shell has a custom
`JAVA_HOME` pointing to Java 11 or earlier (from `jenv`, `sdkman`, or similar),
GradleRIO fails before any build task runs. Check `java -version` and `echo $JAVA_HOME`
in the same terminal where the build fails — step 6 above runs this same check before
you hit it. Either unset `JAVA_HOME` to use the WPILib bundled JDK, or point it
explicitly to the WPILib one.

**Windows: git complains about line endings, or `gradlew` won't run in Git Bash.**
The repository ships a `.gitattributes` that normalizes line endings for you — make
sure `git --version` is recent enough to honor it (any Git for Windows install is) and
re-clone if you set up your clone before it existed.
