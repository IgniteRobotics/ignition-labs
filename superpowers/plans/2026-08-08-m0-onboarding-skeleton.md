# M0 Onboarding Skeleton — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build `projects/m0-onboarding/` — the first skeleton project a student ever touches — and in doing so prove the CI apparatus that four workflows and three tools were written for but has never run against a real Gradle project.

**Architecture:** A standalone GradleRIO project seeded from `Phoenix-2026`'s build scaffolding, stripped to a minimal `TimedRobot` that builds and simulates, plus one deliberately mis-formatted file for the student to fix with Spotless. A Python validator checks the contributor file the student authors, since that file lives outside any Gradle project and no existing check covers it. One workflow fix resolves a spec contradiction that would otherwise fail CI by construction.

**Tech Stack:** GradleRIO 2026.2.1, Gradle 8.11, Java 17 (WPILib bundled JDK), Spotless 8.2.1 with `googleJavaFormat('1.19.2')`, JUnit 5.10.1, Python 3.11 / Poetry / pytest, GitHub Actions.

## Global Constraints

From `docs/programming/teaching-repo-spec.md` §4.3, §7, §9.1, §12.

- **Canonical repository name is `IgniteRobotics/ignition-labs`.** Seven workflow gate strings depend on it. A mismatch skips the job silently rather than failing.
- **`student-check.yml` must NEVER carry a repository gate.** It is the one workflow that runs on student forks.
- **Never `pull_request_target`.**
- **Build tooling, exact:** GradleRIO `2026.2.1`, Gradle wrapper `8.11`, `sourceCompatibility`/`targetCompatibility` 17, Spotless plugin `8.2.1`, `googleJavaFormat('1.19.2').reflowLongStrings()` + `removeUnusedImports()` + `trimTrailingWhitespace()` + `endWithNewline()`, `enforceCheck false`, JUnit `org.junit.jupiter:junit-jupiter:5.10.1`, `useJUnitPlatform()`, `wpi.java.configureTestTasks(test)`.
- **`ROBOT_MAIN_CLASS = "frc.robot.Main"`**, root package `frc.robot` (§4.4 — M3 onward uses `frc.robot`; M0 is a real robot project, not a pure exercise).
- **M0 vendordeps: none.** §4.3 — "Minimum set per milestone. Never all of them by default." A minimal `TimedRobot` needs none.
- **Never seed from `CommandTemplate`** (§12.1) — it is on WPILib 2025.2.1 with no README, no CI, no Ignite conventions. Seed from `Phoenix-2026`.
- **Every skeleton compiles on a clean clone** (§12.3). A student's first experience is a green `./gradlew assemble`.
- **No student-facing path may reference pre-2026 Ignite code** — not `2025-Robotics-Intro`, `Robot-2025`, `Robot-2024`, `Robot-2023`.
- **Privacy (§9.1):** the contributor file carries a GitHub handle only. No legal names, no emails, no school. It is not a roster.
- **Nothing under `projects/` is ever published.** Enforced by the `docs_dir` boundary; verified by `tools/site_guard.py`.

## Environment

- Activate: `eval "$(pyenv init -)" && pyenv activate ignite-docs`. Suite currently at **56 tests**.
- **Gradle needs Java 17.** This machine's default `JAVA_HOME` is Java 11 via `jenv`, which GradleRIO rejects at configuration time before any task runs. Every Gradle command in this plan must be prefixed:
  ```bash
  export JAVA_HOME=~/wpilib/2026/jdk
  ```
- Seed source: `~/workspace/ignite/Phoenix-2026` — do not modify it.

## File Structure

| Path | Responsibility | Task |
|---|---|---|
| `projects/m0-onboarding/build.gradle` | GradleRIO + Spotless + JUnit config, no vendordeps | 1 |
| `projects/m0-onboarding/settings.gradle` | GradleRIO plugin resolution from the local WPILib install | 1 |
| `projects/m0-onboarding/gradlew`, `gradle/wrapper/` | Gradle 8.11 wrapper | 1 |
| `projects/m0-onboarding/.wpilib/wpilib_preferences.json` | Team 6829, project year 2026 | 1 |
| `projects/m0-onboarding/src/main/java/frc/robot/{Main,Robot}.java` | Minimal working `TimedRobot` | 1 |
| `projects/m0-onboarding/src/main/java/frc/robot/FormatMe.java` | Deliberately mis-formatted; the student runs `spotlessApply` | 2 |
| `projects/m0-onboarding/README.md` | What the student does, in order | 2 |
| `templates/CONTRIBUTOR.md` | The file a student copies to `contributors/<handle>.md` | 3 |
| `tools/contributors_check.py` | Validates contributor files; no check covers them today | 3 |
| `tests/test_contributors_check.py` | Covers that validator | 3 |
| `.github/workflows/skeleton-integrity.yml` | Inverted check must skip projects with no `src/test` | 4 |
| `.github/workflows/student-check.yml` | Must run the contributor validator | 4 |

## Interface Summary

| Module | Function | Signature |
|---|---|---|
| `tools/contributors_check.py` | `validate_file` | `(path: Path) -> list[str]` — returns human-readable problems, empty when valid |
| | `validate_all` | `(root: Path) -> dict[str, list[str]]` — maps relative path to its problems, only for files with problems |

---

## Task 1: Seed the M0 Gradle project

**Files:**
- Create: `projects/m0-onboarding/{build.gradle,settings.gradle,gradlew,gradlew.bat,.gitignore,WPILib-License.md}`
- Create: `projects/m0-onboarding/gradle/wrapper/{gradle-wrapper.jar,gradle-wrapper.properties}`
- Create: `projects/m0-onboarding/.wpilib/wpilib_preferences.json`
- Create: `projects/m0-onboarding/src/main/java/frc/robot/{Main,Robot}.java`

**Interfaces:**
- Consumes: nothing
- Produces: the first directory under `projects/` — every later task and every workflow matrix depends on its existence

- [ ] **Step 1: Copy the build scaffolding from Phoenix-2026**

Seed from the in-house project, never from `CommandTemplate` (§12.1).

```bash
mkdir -p projects/m0-onboarding
P=~/workspace/ignite/Phoenix-2026
cp $P/gradlew $P/gradlew.bat $P/settings.gradle $P/WPILib-License.md projects/m0-onboarding/
mkdir -p projects/m0-onboarding/gradle/wrapper
cp $P/gradle/wrapper/gradle-wrapper.jar $P/gradle/wrapper/gradle-wrapper.properties projects/m0-onboarding/gradle/wrapper/
mkdir -p projects/m0-onboarding/.wpilib
cp $P/.wpilib/wpilib_preferences.json projects/m0-onboarding/.wpilib/
chmod +x projects/m0-onboarding/gradlew
grep distributionUrl projects/m0-onboarding/gradle/wrapper/gradle-wrapper.properties
```

Expected: `gradle-8.11-bin.zip`. If it differs, the seed is stale — stop and report.

- [ ] **Step 2: Write `build.gradle`**

Create `projects/m0-onboarding/build.gradle`. Spotless is `8.2.1` (Robot-2026's, newer than Phoenix's 6.25.0) per §4.3. No `vendordeps` block and no `gversion` plugin.

```groovy
plugins {
    id "java"
    id "com.diffplug.spotless" version "8.2.1"
    id "edu.wpi.first.GradleRIO" version "2026.2.1"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

spotless {
    // Not enforced during build; compileJava depends on spotlessApply below.
    enforceCheck false

    java {
        target 'src/**/*.java'
        removeUnusedImports()
        googleJavaFormat('1.19.2').reflowLongStrings()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

def ROBOT_MAIN_CLASS = "frc.robot.Main"

deploy {
    targets {
        roborio(getTargetTypeClass('RoboRIO')) {
            team = project.frc.getTeamOrDefault(6829)
            debug = project.frc.getDebugOrDefault(false)

            artifacts {
                frcJava(getArtifactTypeClass('FRCJavaArtifact')) {
                }
            }
        }
    }
}

def deployArtifact = deploy.targets.roborio.artifacts.frcJava

wpi.java.debugJni = false
def includeDesktopSupport = true

repositories {
    mavenCentral()
}

dependencies {
    implementation wpi.java.deps.wpilib()
    implementation wpi.java.vendor.java()

    roborioDebug wpi.java.deps.wpilibJniDebug(wpi.platforms.roborio)
    roborioDebug wpi.java.vendor.jniDebug(wpi.platforms.roborio)

    roborioRelease wpi.java.deps.wpilibJniRelease(wpi.platforms.roborio)
    roborioRelease wpi.java.vendor.jniRelease(wpi.platforms.roborio)

    nativeDebug wpi.java.deps.wpilibJniDebug(wpi.platforms.desktop)
    nativeDebug wpi.java.vendor.jniDebug(wpi.platforms.desktop)
    simulationDebug wpi.sim.enableDebug()

    nativeRelease wpi.java.deps.wpilibJniRelease(wpi.platforms.desktop)
    nativeRelease wpi.java.vendor.jniRelease(wpi.platforms.desktop)
    simulationRelease wpi.sim.enableRelease()

    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.1'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}

test {
    useJUnitPlatform()
    systemProperty 'junit.jupiter.extensions.autodetection.enabled', 'true'
}

wpi.sim.addGui().defaultEnabled = true
wpi.sim.addDriverstation()

jar {
    from { configurations.runtimeClasspath.collect { it.isDirectory() ? it : zipTree(it) } }
    from sourceSets.main.allSource
    manifest edu.wpi.first.gradlerio.GradleRIOPlugin.javaManifest(ROBOT_MAIN_CLASS)
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

deployArtifact.jarTask = jar
wpi.java.configureExecutableTasks(jar)
wpi.java.configureTestTasks(test)

tasks.withType(JavaCompile) {
    options.compilerArgs.add '-XDstringConcat=inline'
}

compileJava.dependsOn 'spotlessApply'
```

- [ ] **Step 3: Write the robot source**

Create `projects/m0-onboarding/src/main/java/frc/robot/Main.java`:

```java
package frc.robot;

import edu.wpi.first.wpilibj.RobotBase;

/**
 * Do not modify this file. WPILib calls main() to start the robot program, and the roboRIO's
 * startup scripts expect this exact class.
 */
public final class Main {
  private Main() {}

  public static void main(String... args) {
    RobotBase.startRobot(Robot::new);
  }
}
```

Create `projects/m0-onboarding/src/main/java/frc/robot/Robot.java`:

```java
package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;

/**
 * The smallest robot program that runs. It does nothing except tick, which is the point — M0 is
 * about the toolchain, git, and the build, not about making anything move.
 *
 * <p>Each of these methods is called by WPILib at a specific moment. robotPeriodic() runs every
 * 20 milliseconds, always. The others run when the robot enters that mode. You will meet all of
 * them properly in M3.
 */
public class Robot extends TimedRobot {
  @Override
  public void robotInit() {}

  @Override
  public void robotPeriodic() {}

  @Override
  public void autonomousInit() {}

  @Override
  public void autonomousPeriodic() {}

  @Override
  public void teleopInit() {}

  @Override
  public void teleopPeriodic() {}

  @Override
  public void disabledInit() {}

  @Override
  public void disabledPeriodic() {}

  @Override
  public void testInit() {}

  @Override
  public void testPeriodic() {}

  @Override
  public void simulationPeriodic() {}
}
```

- [ ] **Step 4: Write the project `.gitignore`**

Create `projects/m0-onboarding/.gitignore`:

```
build/
bin/
.gradle/
logs/
ctre_sim/
networktables.json
simgui-window.json
*.log
```

`simgui.json` is deliberately NOT ignored — later milestones ship a seeded one.

- [ ] **Step 5: Build it**

```bash
export JAVA_HOME=~/wpilib/2026/jdk
cd projects/m0-onboarding && ./gradlew build --console=plain 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL`. First run downloads dependencies and may take several minutes — that is not a hang.

If it fails on Java version, `JAVA_HOME` is not set for this shell. If it fails resolving the GradleRIO plugin, check `settings.gradle` points at `~/wpilib/2026`.

- [ ] **Step 6: Confirm the simulator launches headless**

```bash
cd projects/m0-onboarding && timeout 60 ./gradlew simulateJava --console=plain 2>&1 | tail -20 ; echo "exit=$?"
```

Expected: it starts and is killed by the timeout (`exit=124`), having printed simulation startup output rather than an error. A configuration failure exits fast with a stack trace — that is a real failure. Report which you saw.

- [ ] **Step 7: Confirm `assemble` is green on a clean clone**

Spec §12.3 requires a student's first command to succeed.

```bash
cd projects/m0-onboarding && ./gradlew clean assemble --console=plain 2>&1 | tail -5
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 8: Commit**

```bash
git add projects/m0-onboarding
git commit -m "Add M0 onboarding skeleton: minimal TimedRobot on GradleRIO 2026

Seeded from Phoenix-2026 per spec §12.1, never CommandTemplate. Spotless
bumped to 8.2.1 (Robot-2026's) per §4.3. No vendordeps -- a minimal
TimedRobot needs none, and §4.3 forbids shipping them by default."
```

---

## Task 2: The Spotless exercise and the project README

M0's mechanical Definition of Done is that the student runs `spotlessApply` and opens a pull request. That needs something to format.

**Files:**
- Create: `projects/m0-onboarding/src/main/java/frc/robot/FormatMe.java`
- Create: `projects/m0-onboarding/README.md`

**Interfaces:**
- Consumes: the project from Task 1
- Produces: a file whose unformatted state Task 5 verifies `spotlessCheck` actually catches

- [ ] **Step 1: Write the deliberately mis-formatted file**

Create `projects/m0-onboarding/src/main/java/frc/robot/FormatMe.java` exactly as below — the bad formatting is the exercise. Four-space indents, a brace on its own line, trailing whitespace, and no terminating newline are all violations `googleJavaFormat` will correct.

```java
package frc.robot;

/** This file is formatted wrong on purpose. Fix it with the formatter, not by hand. */
public class FormatMe
{
    public static int addTwoNumbers( int first,int second )
    {
        int   result = first+second;
        return result;
    }

    public static boolean isPositive(int value)  {
            return value>0;
    }
}
```

> **Authoring note:** `compileJava.dependsOn 'spotlessApply'` means a plain `./gradlew build` silently reformats this file. Step 3 checks the violation exists using `spotlessCheck` on a clean tree BEFORE any build reformats it. If you have already run `build`, restore the file with `git checkout` first.

- [ ] **Step 2: Verify the file is genuinely mis-formatted**

```bash
export JAVA_HOME=~/wpilib/2026/jdk
cd projects/m0-onboarding && ./gradlew spotlessCheck --console=plain 2>&1 | tail -15
```

Expected: FAILS, naming `FormatMe.java`. If it passes, the file is already correctly formatted and the exercise is empty — rewrite it with worse formatting and re-check.

- [ ] **Step 3: Verify the fix works, then restore the broken state**

```bash
cd projects/m0-onboarding && ./gradlew spotlessApply --console=plain 2>&1 | tail -3
./gradlew spotlessCheck --console=plain 2>&1 | tail -3
cd ../.. && git checkout projects/m0-onboarding/src/main/java/frc/robot/FormatMe.java 2>/dev/null || true
```

Expected: `spotlessApply` succeeds, the following `spotlessCheck` passes. Then the `git checkout` restores the mis-formatted version — **the file must ship broken.** If it was not yet committed, re-write it from step 1 instead. Confirm with `git diff` or by reading the file that it is broken again before continuing.

- [ ] **Step 4: Write the project README**

Create `projects/m0-onboarding/README.md`:

```markdown
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

In VS Code: `Ctrl+Shift+P` → **WPILib: Simulate Robot Code** → check **Sim GUI** → OK.

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
- The simulator opens and you can point at the Driver Station panel
- `./gradlew spotlessCheck` passes
- Your pull request is green and merged
- You can explain what a branch is without using the word "branch"
```

- [ ] **Step 5: Commit**

```bash
git add projects/m0-onboarding
git commit -m "Add the M0 Spotless exercise and project README

FormatMe.java ships deliberately mis-formatted; the student fixes it with
spotlessApply rather than by hand, which is the point."
```

---

## Task 3: Contributor template and validator

The student authors `contributors/<handle>.md`. That file lives outside any Gradle project, so **no existing check covers it** — `student-check.yml` builds only changed `projects/mN/` directories, and a contributor-only pull request produces an empty matrix. Spec §9.1 requires the file be checked against a schema. This task builds that check.

**Files:**
- Create: `templates/CONTRIBUTOR.md`, `contributors/README.md`
- Create: `tools/contributors_check.py`, `tests/test_contributors_check.py`

**Interfaces:**
- Consumes: `tools` package
- Produces: `validate_file(path) -> list[str]` and `validate_all(root) -> dict[str, list[str]]`, called by `student-check.yml` in Task 4

- [ ] **Step 1: Write the failing test**

Create `tests/test_contributors_check.py`:

```python
"""Contributor files carry a GitHub handle and nothing identifying (spec §9.1).

The privacy rule is the reason this validator exists: no legal names, no emails,
no school. It is deliberately not a roster.
"""

from pathlib import Path

import pytest

from tools.contributors_check import validate_all, validate_file

VALID = """\
# jsmith

**GitHub:** @jsmith

**What I want to work on:** autonomous, and eventually vision.

**Joined:** 2026-09
"""


def _write(tmp_path: Path, name: str, body: str) -> Path:
    target = tmp_path / "contributors" / name
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(body, encoding="utf-8")
    return target


def test_valid_file_has_no_problems(tmp_path):
    assert validate_file(_write(tmp_path, "jsmith.md", VALID)) == []


def test_filename_must_match_the_handle_inside(tmp_path):
    problems = validate_file(_write(tmp_path, "someone-else.md", VALID))
    assert any("filename" in p for p in problems)


def test_missing_github_line_is_reported(tmp_path):
    body = VALID.replace("**GitHub:** @jsmith\n", "")
    problems = validate_file(_write(tmp_path, "jsmith.md", body))
    assert any("GitHub" in p for p in problems)


def test_email_address_is_rejected(tmp_path):
    body = VALID + "\nReach me at jsmith@example.com\n"
    problems = validate_file(_write(tmp_path, "jsmith.md", body))
    assert any("email" in p.lower() for p in problems)


def test_placeholder_handle_is_rejected(tmp_path):
    body = VALID.replace("jsmith", "your-github-handle")
    problems = validate_file(_write(tmp_path, "your-github-handle.md", body))
    assert any("template" in p.lower() for p in problems)


def test_readme_in_contributors_is_skipped(tmp_path):
    _write(tmp_path, "README.md", "# Contributors\n\nExplanatory text.\n")
    assert validate_all(tmp_path) == {}


def test_validate_all_reports_only_bad_files(tmp_path):
    _write(tmp_path, "jsmith.md", VALID)
    _write(tmp_path, "bad.md", "# bad\n")
    result = validate_all(tmp_path)
    assert list(result) == ["contributors/bad.md"]


def test_missing_contributors_directory_is_not_an_error(tmp_path):
    assert validate_all(tmp_path) == {}
```

- [ ] **Step 2: Run it to verify it fails**

```bash
eval "$(pyenv init -)" && pyenv activate ignite-docs
poetry run pytest tests/test_contributors_check.py -v
```

Expected: FAIL with `ModuleNotFoundError: No module named 'tools.contributors_check'`.

- [ ] **Step 3: Write the implementation**

Create `tools/contributors_check.py`:

```python
"""Validates contributor files (spec §9.1).

Two jobs. The first is a schema check so a student's first pull request gets
useful feedback instead of a silent merge. The second matters more: this file
set is deliberately NOT a roster. GitHub handle only -- no legal names, no
emails, no school. The validator enforces that, because the privacy promise is
only as good as the thing that checks it.

Messages are addressed to a fifteen-year-old opening their first pull request.
They say what to change, not what was done wrong.
"""

from __future__ import annotations

import re
import sys
from pathlib import Path

EMAIL = re.compile(r"[\w.+-]+@[\w-]+\.[\w.]+")
GITHUB_LINE = re.compile(r"^\*\*GitHub:\*\*\s*@([A-Za-z0-9](?:[A-Za-z0-9-]{0,37}[A-Za-z0-9])?)\s*$", re.M)
PLACEHOLDERS = ("your-github-handle", "<your-handle>", "your-handle", "TODO", "FIXME")


def validate_file(path: Path) -> list[str]:
    """Return human-readable problems with one contributor file. Empty when valid."""
    problems: list[str] = []
    text = path.read_text(encoding="utf-8")
    stem = path.stem

    match = GITHUB_LINE.search(text)
    if match is None:
        problems.append(
            "Add a line reading exactly '**GitHub:** @your-handle' with your real handle."
        )
    elif match.group(1) != stem:
        problems.append(
            f"The filename says '{stem}' but the GitHub line says '@{match.group(1)}'. "
            "Rename the file to match your handle."
        )

    if EMAIL.search(text):
        problems.append(
            "Remove the email address. This file is public and carries your GitHub "
            "handle only -- no emails, no legal names, no school."
        )

    for placeholder in PLACEHOLDERS:
        if placeholder in text:
            problems.append(
                f"'{placeholder}' is still in the file -- replace the template text with yours."
            )
            break

    return problems


def validate_all(root: Path) -> dict[str, list[str]]:
    """Map each problematic contributor file's relative path to its problems."""
    directory = root / "contributors"
    if not directory.is_dir():
        return {}
    results: dict[str, list[str]] = {}
    for path in sorted(directory.glob("*.md")):
        if path.name == "README.md":
            continue
        problems = validate_file(path)
        if problems:
            results[path.relative_to(root).as_posix()] = problems
    return results


def main() -> int:
    results = validate_all(Path.cwd())
    if not results:
        print("contributor files OK")
        return 0
    for rel, problems in results.items():
        print(f"\n{rel}", file=sys.stderr)
        for problem in problems:
            print(f"  - {problem}", file=sys.stderr)
    print(
        "\nThis is a checklist, not a telling-off. Fix these and push again.",
        file=sys.stderr,
    )
    return 1


if __name__ == "__main__":
    raise SystemExit(main())
```

- [ ] **Step 4: Run the tests to verify they pass**

```bash
poetry run pytest tests/test_contributors_check.py -v
```

Expected: PASS, 8 tests. Full suite should now be 64.

- [ ] **Step 5: Write the template and the directory README**

Create `templates/CONTRIBUTOR.md`:

```markdown
# your-github-handle

**GitHub:** @your-github-handle

**What I want to work on:** 

**Joined:** 2026-09
```

Create `contributors/README.md`:

```markdown
# Contributors

One file per person, named for your GitHub handle. Copy
[`templates/CONTRIBUTOR.md`](../templates/CONTRIBUTOR.md) to
`contributors/<your-handle>.md` and fill it in. You create it in M0.

**GitHub handle only.** No legal names, no email addresses, no school. This is
deliberately not a roster: nothing reads these files, nothing aggregates them,
and you can delete yours with a pull request at any time.
```

- [ ] **Step 6: Verify the template is rejected by its own validator**

The template contains placeholder text, so committing it as a contributor file must fail. That proves the check works.

```bash
poetry run python -c "
from pathlib import Path
from tools.contributors_check import validate_file
print(validate_file(Path('templates/CONTRIBUTOR.md')))
"
poetry run python tools/contributors_check.py
```

Expected: the first prints a non-empty problem list; the second prints `contributor files OK`, because `templates/` is not `contributors/` and the directory holds only `README.md`, which is skipped.

- [ ] **Step 7: Commit**

```bash
git add tools/contributors_check.py tests/test_contributors_check.py templates/CONTRIBUTOR.md contributors/README.md
git commit -m "Add contributor file validator and template

The contributor file lives outside any Gradle project, so student-check's
matrix never covers it -- a contributor-only PR produces an empty matrix and
no check at all. This closes that gap and enforces the §9.1 privacy rule:
handle only, no emails, no legal names."
```

---

## Task 4: Resolve the inverted-check contradiction and wire in the validator

**Files:**
- Modify: `.github/workflows/skeleton-integrity.yml`
- Modify: `.github/workflows/student-check.yml`

**Interfaces:**
- Consumes: `tools/contributors_check.py` from Task 3, `projects/m0-onboarding/` from Task 1
- Produces: workflows that pass against a repository that finally contains a real project

- [ ] **Step 1: Understand the contradiction before changing anything**

`skeleton-integrity.yml`'s inverted check asserts every skeleton's tests **fail** unmodified — a skeleton whose tests pass has a stub that accidentally implements itself (spec §7.2). But spec §9.1 defines M0 as "a complete, working, minimal `TimedRobot` project" and §8 lists it as *fade: N/A*. M0 has no stubs and ships no `src/test`, so `./gradlew test` succeeds trivially and the inverted check fails the build.

Read the current inverted-check step before editing so your change fits its shape.

- [ ] **Step 2: Make the inverted check skip projects with no given tests**

In `.github/workflows/skeleton-integrity.yml`, guard the inverted-check step so it runs only when the project has a `src/test` directory. Add a comment recording why, in the style of the neighbouring `lock` job's comment:

```yaml
      - name: Inverted check — given tests MUST fail on the unmodified skeleton
        working-directory: ${{ matrix.project }}
        run: |
          # A skeleton with no src/test has no given tests, so there is nothing to
          # invert -- M0 is a complete working project by design (spec §9.1, fade N/A).
          # Skipping on absence rather than an exemption list keeps this self-maintaining.
          if [ ! -d src/test ]; then
            echo "No src/test in ${{ matrix.project }} — no given tests to invert. Skipping."
            exit 0
          fi
          if ./gradlew test --console=plain; then
            echo "::error::${{ matrix.project }} passes its own tests unmodified."
            echo "A stub is accidentally implementing itself. See spec §7.2."
            exit 1
          fi
          echo "OK: tests fail as intended on the unmodified skeleton."
```

Preserve the existing failure message and `::error::` annotation exactly — only the guard is new.

- [ ] **Step 3: Add the contributor check to `student-check.yml`**

The `matrix` job already runs on every pull request regardless of what changed, so the validator belongs there rather than in the Gradle build job — a contributor-only pull request must still be checked. Add a step after the existing `tests.lock` verification:

```yaml
      - name: Validate contributor files
        run: python tools/contributors_check.py
```

`contributors_check.py` uses only the standard library, so it needs no Poetry install.

- [ ] **Step 4: Validate both workflows**

```bash
actionlint .github/workflows/*.yml
```

Expected: exit 0, no output.

- [ ] **Step 5: Confirm the constraints still hold**

```bash
grep -rn "github.repository ==" .github/workflows/ | wc -l | tr -d ' '
grep -c "github.repository" .github/workflows/student-check.yml || echo "student-check ungated: correct"
grep -rn "pull_request_target" .github/workflows/ || echo "no pull_request_target: correct"
```

Expected: 7 gates; `student-check.yml` ungated; no `pull_request_target`.

- [ ] **Step 6: Commit**

```bash
git add .github/workflows/skeleton-integrity.yml .github/workflows/student-check.yml
git commit -m "Skip the inverted check for skeletons with no given tests

M0 is a complete working project by design (spec §9.1, fade N/A), so it has
no stubs and no src/test. The inverted check would fail it by construction.
Skipping on absence of src/test rather than an exemption list keeps the rule
self-maintaining as later milestones land.

Also wires the contributor validator into student-check's matrix job, which
runs on every PR -- the Gradle build job would miss a contributor-only PR."
```

---

## Task 5: Prove the CI apparatus end to end

Four workflows and three tools were written against a repository with no `projects/` directory. This is the first time any of it meets a real Gradle project. Simulate each workflow's logic locally; do not assume the unit tests covered it.

**Files:**
- Modify: `tests.lock` (regenerated, still empty — M0 ships no given tests)

**Interfaces:**
- Consumes: everything from Tasks 1–4
- Produces: evidence the pipeline works, recorded in the report

- [ ] **Step 1: Confirm the skeleton-integrity project discovery finds M0**

This is the `find` in the `matrix` job. Run it exactly as the workflow does — GNU-style `-printf` is not available on macOS, so use the portable equivalent and confirm it yields the same answer:

```bash
find projects -mindepth 2 -maxdepth 3 -name settings.gradle -exec dirname {} \; | sort
```

Expected: exactly `projects/m0-onboarding`. If empty, `settings.gradle` is missing or nested at the wrong depth.

- [ ] **Step 2: Confirm the changed-project matrix deriver selects M0**

```bash
eval "$(pyenv init -)" && pyenv activate ignite-docs
printf 'projects/m0-onboarding/src/main/java/frc/robot/FormatMe.java\ndocs/programming/README.md\n' > /tmp/changed.txt
poetry run python tools/changed_projects.py --from-file /tmp/changed.txt
printf 'contributors/jsmith.md\n' > /tmp/changed2.txt
poetry run python tools/changed_projects.py --from-file /tmp/changed2.txt
rm -f /tmp/changed.txt /tmp/changed2.txt
```

Expected: `["projects/m0-onboarding"]` then `[]`. The second case is the contributor-only pull request — an empty matrix, which is exactly why Task 3's validator runs in the `matrix` job rather than the build job.

- [ ] **Step 3: Regenerate and verify `tests.lock`**

```bash
poetry run python tools/tests_lock.py write
poetry run python tools/tests_lock.py verify
git diff --stat tests.lock
```

Expected: `tests.lock OK`, and no change to the file — M0 ships no `src/test` and no `plant/` package, so the manifest is still empty. If it gained entries, something matched a pattern it should not have; investigate before continuing.

- [ ] **Step 4: Simulate the inverted check against M0**

The change from Task 4, exercised for real:

```bash
cd projects/m0-onboarding
if [ ! -d src/test ]; then echo "SKIP (correct — no given tests)"; else echo "would run inverted check"; fi
cd ../..
```

Expected: `SKIP (correct — no given tests)`.

- [ ] **Step 5: Simulate the student-check build job**

```bash
export JAVA_HOME=~/wpilib/2026/jdk
cd projects/m0-onboarding
./gradlew build --console=plain 2>&1 | tail -3
git checkout src/main/java/frc/robot/FormatMe.java
./gradlew spotlessCheck --console=plain 2>&1 | tail -5
cd ../..
```

Expected: `build` succeeds; then, with `FormatMe.java` restored to its broken state, `spotlessCheck` FAILS naming that file. That failure is what a student sees before they run `spotlessApply` — it is the check working, not a problem.

**Then restore the broken file again** and confirm with `git status` that `FormatMe.java` is unmodified relative to HEAD.

- [ ] **Step 6: Confirm the site still excludes `projects/`**

`projects/` now contains real Java for the first time — the boundary claim finally has something real to exclude.

```bash
eval "$(pyenv init -)" && pyenv activate ignite-docs
rm -rf _site && poetry run mkdocs build 2>&1 | grep -c "^WARNING" | xargs echo "warnings:"
poetry run python tools/site_guard.py ./_site
find _site \( -path '*projects*' -o -name '*.java' -o -name '*.gradle' \) | wc -l | tr -d ' '
rm -rf _site
```

Expected: 1 warning (the known `UNTESTED-TARGETS.md`), `site guard OK`, and `0` leaked files.

- [ ] **Step 7: Full suite and lint**

```bash
poetry run pytest
actionlint .github/workflows/*.yml
lychee --no-progress --accept 200,206,403,429 --exclude-path projects --exclude-path superpowers --exclude-path .superpowers './**/*.md'
git status --short
```

Expected: 64 passed; actionlint silent; lychee 0 errors; clean tree.

- [ ] **Step 8: Commit anything that changed**

```bash
git add -A
git commit -m "Verify the CI apparatus against the first real skeleton project" || echo "nothing to commit — verification only"
```

---

## Task 6: Update the milestone and status documents

**Files:**
- Modify: `docs/programming/milestones/m0-tooling-and-git.md`
- Modify: `docs/programming/README.md` (status table)
- Modify: `docs/programming/teaching-repo-spec.md` (§13 build order, §17 open items)

**Interfaces:**
- Consumes: the completed M0 project
- Produces: documents that describe what now exists

- [ ] **Step 1: Point the milestone at the real project**

`docs/programming/milestones/m0-tooling-and-git.md` step 1 currently reads "Fork or clone the teaching repository" — ambiguous, and the ambiguity was resolved months ago. Update its project section to reference `projects/m0-onboarding/` by path and link to `../git-workflow.md` for the commands. Do not restate the commands; one authoritative copy.

Add the fork-Actions warning if it is absent, matching the wording in `docs/SETUP.md` — spec §15 rates it the highest-likelihood M0 failure.

- [ ] **Step 2: Update the status table**

In `docs/programming/README.md`, the teaching-repository row currently says the foundation is built and skeletons are pending. Change it to record that **M0 is built** and M1–M8 remain. Keep the row's voice; do not add rows.

- [ ] **Step 3: Update the spec's build order and open items**

In `docs/programming/teaching-repo-spec.md`:
- §13 — mark step 4 (M0) as done, in whatever way the table already distinguishes completed steps. If it has no such convention, add a short "Status" column rather than inventing per-row prose.
- §17 — add a resolved row recording that the inverted check now skips projects with no `src/test`, and why (M0 is fade N/A and ships no stubs).

- [ ] **Step 4: Verify and commit**

```bash
eval "$(pyenv init -)" && pyenv activate ignite-docs
poetry run pytest
rm -rf _site && poetry run mkdocs build && poetry run python tools/site_guard.py ./_site && rm -rf _site
lychee --no-progress --accept 200,206,403,429 --exclude-path projects --exclude-path superpowers --exclude-path .superpowers './**/*.md'
git add -A && git commit -m "Point M0's milestone and the status tables at the built skeleton"
```

Expected: 64 passed; site guard OK; lychee 0 errors.

---

## Done when

- [ ] `projects/m0-onboarding/` builds green from a clean clone with `./gradlew assemble`
- [ ] `./gradlew simulateJava` starts the simulator rather than erroring
- [ ] `FormatMe.java` ships mis-formatted, `spotlessCheck` fails on it, `spotlessApply` fixes it
- [ ] `tools/contributors_check.py` rejects a missing GitHub line, a filename/handle mismatch, an email address, and leftover template text
- [ ] `templates/CONTRIBUTOR.md` is rejected by the validator when treated as a contributor file
- [ ] The inverted check skips M0 for the stated reason, and still fires for a project that has `src/test`
- [ ] `student-check.yml` validates contributor files in the `matrix` job, which runs even when no project changed
- [ ] All seven gate strings read `IgniteRobotics/ignition-labs`; `student-check.yml` carries none
- [ ] `find projects -mindepth 2 -maxdepth 3 -name settings.gradle` returns exactly M0
- [ ] `changed_projects.py` returns `["projects/m0-onboarding"]` for a source change and `[]` for a contributor-only change
- [ ] `tests.lock` remains empty and verifies clean
- [ ] The built site contains zero files from `projects/`, now that it holds real Java
- [ ] `pytest` is green at 64 tests; `actionlint` silent; `lychee` zero errors

## Deliberately not in this plan

- **M1, M2, M3 and fixtures T1–T3** — one plan each, following this one. M0 first because it is the smallest project and proves the CI apparatus before six stub classes and six test suites are added on top.
- **The solutions repository** — starts once a milestone ships given tests. M0 has none.
- **Enabling `mkdocs build --strict`** — blocked on `UNTESTED-TARGETS.md`, which arrives with M6. Recorded in spec §16.

## Risks carried into execution

| Risk | Surfaces at | Response |
|---|---|---|
| `JAVA_HOME` defaults to Java 11 via jenv; GradleRIO rejects it before any task runs | Task 1 step 5 | Every Gradle command is prefixed with `export JAVA_HOME=~/wpilib/2026/jdk` |
| `compileJava.dependsOn 'spotlessApply'` silently reformats `FormatMe.java` during any build | Task 2 step 3, Task 5 step 5 | Both tasks explicitly restore the broken file and verify with `git status` |
| The inverted check fails M0 by construction | Task 4 | Resolved by skipping when `src/test` is absent |
| A contributor-only pull request gets no check at all | Task 3, Task 5 step 2 | Validator runs in the `matrix` job, which is unconditional |
| Seeding from Phoenix-2026 carries stale Spotless 6.25.0 | Task 1 step 2 | `build.gradle` is written fresh with 8.2.1, not copied |
| First Gradle run downloads for minutes and looks like a hang | Task 1 step 5 | Called out in the step |
