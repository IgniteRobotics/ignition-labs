# Task 1: Phoenix CI spike, then the project scaffold

M6 is the first project in this repository with a CTRE vendordep, and `student-check.yml` runs `./gradlew build` — which in the Gradle Java plugin runs `check`, which runs `test`. Every Phoenix test an M6 student writes therefore executes on `ubuntu-latest`. If Phoenix simulation is unreliable there, the failure lands on the student as a red pull request they cannot diagnose, which is the exact failure `CLAUDE.md`'s clean-clone invariant exists to prevent.

Native *resolution* is already confirmed: `Phoenix6-26.1.1.json` ships `api-cpp-sim`, `tools-sim`, and `simProTalonFX` for `linuxx86-64`. **The risk is flake**, because CTRE's documented waits are wall-clock and a shared runner under contention is where wall-clock waits become intermittent. The spike measures it before anything depends on it.

**Files:**
- Create: `projects/m6-testing/` — scaffold, `build.gradle`, `settings.gradle`, `vendordeps/`
- Create (temporary, deleted in Step 4): `projects/m6-testing/src/test/java/frc/robot/SpikeTest.java`, `.github/workflows/phoenix-spike.yml`
- Test: `./gradlew assemble`, `./gradlew test`, and the spike workflow's 20 runs

**Interfaces:**
- Consumes: nothing.
- Produces: a buildable `projects/m6-testing/` with Phoenix on the test classpath, and **a spike verdict** — either "signal waits are stable, Task 2 Step 6 proceeds as written" or "signal waits flake N/20, Task 2 Step 6 takes its fallback."

**Lane:** `milestone`. Worktree `.worktrees/m6`.

---

- [ ] **Step 1: Copy the scaffold from M3**

M3 is the nearest project by shape (GradleRIO, `frc.robot`, desktop natives in tests). Copy only the files that carry no M3-specific content:

```bash
cd /Users/y0shi/workspace/ignite/docs
mkdir -p projects/m6-testing
cp -r projects/m3-command-based/gradle projects/m6-testing/
cp projects/m3-command-based/gradlew projects/m3-command-based/gradlew.bat \
   projects/m3-command-based/.gitignore projects/m3-command-based/WPILib-License.md \
   projects/m6-testing/
mkdir -p projects/m6-testing/.wpilib projects/m6-testing/vendordeps
cp projects/m3-command-based/.wpilib/wpilib_preferences.json projects/m6-testing/.wpilib/
cp projects/m3-command-based/vendordeps/WPILibNewCommands.json projects/m6-testing/vendordeps/
cp /Users/y0shi/workspace/ignite/Robot-2026/vendordeps/Phoenix6-26.1.1.json projects/m6-testing/vendordeps/
chmod +x projects/m6-testing/gradlew
```

**Do not copy** `simgui*.json`, `networktables.json`, `Robot.java`, `Main.java`, or `Constants.java`. M6 is a testing sandbox, not a robot — see the constraints delta.

Write `projects/m6-testing/settings.gradle`:

```groovy
rootProject.name = 'm6-testing'
```

- [ ] **Step 2: Write `build.gradle`**

This is M2's shape — GradleRIO applied for the toolchain and vendordep machinery, with no robot — plus `wpi.java.configureTestTasks(test)`, which is what puts the desktop natives (WPILib HAL *and* Phoenix sim) on the test JVM's library path.

Create `projects/m6-testing/build.gradle`:

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
    // Not enforced during build. Formatting is checked by CI (spotlessCheck) as a
    // separate, deliberately failable step -- see teaching-repo-spec.md §4.3. Do not
    // wire compileJava to spotlessApply: that reformats the tree on every build, which
    // makes the formatting check structurally unable to fail.
    enforceCheck false

    java {
        target 'src/**/*.java'
        removeUnusedImports()
        googleJavaFormat('1.19.2').reflowLongStrings()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

// M6 is a testing sandbox, not a robot. There is no RobotBase subclass here on purpose:
// this milestone's first documented pitfall is "do not instantiate the whole robot in a
// test" (TimedRobot is not designed to ever exit, so a test that constructs one leaves
// background threads running and hangs CI). Shipping a TimedRobot in the project whose
// lesson is not to construct one invites the mistake. ROBOT_MAIN_CLASS only names the
// jar manifest's main class; nothing runs it. Same arrangement as M2.
def ROBOT_MAIN_CLASS = "frc.robot.plant.LaunchProfile"

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

jar {
    from { configurations.runtimeClasspath.collect { it.isDirectory() ? it : zipTree(it) } }
    from sourceSets.main.allSource
    manifest edu.wpi.first.gradlerio.GradleRIOPlugin.javaManifest(ROBOT_MAIN_CLASS)
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

deployArtifact.jarTask = jar
wpi.java.configureExecutableTasks(jar)

// This is what supplies the desktop natives to the test JVM -- WPILib's HAL and, new in
// M6, Phoenix's simulation libraries. Without it every Phoenix test dies with
// UnsatisfiedLinkError, which tools/given_tests_red.py classifies as a toolchain failure
// precisely because a student cannot tell it apart from a broken install.
wpi.java.configureTestTasks(test)

tasks.withType(JavaCompile) {
    options.compilerArgs.add '-XDstringConcat=inline'
}
```

- [ ] **Step 3: Write the throwaway spike test**

This file is deleted in Step 4. It exists to answer one question — does a Phoenix device settle deterministically under `BaseStatusSignal.waitForAll` on a CI runner — and nothing else. It is not a template for the worked example.

Create `projects/m6-testing/src/test/java/frc/robot/SpikeTest.java`:

```java
package frc.robot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.StatusCode;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.controls.VelocityVoltage;
import com.ctre.phoenix6.hardware.TalonFX;
import edu.wpi.first.hal.HAL;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.simulation.DriverStationSim;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SpikeTest {

  private TalonFX motor;

  @BeforeEach
  void setUp() {
    assertTrue(HAL.initialize(500, 0), "HAL failed to initialize");
    DriverStationSim.setEnabled(true);
    DriverStationSim.notifyNewData();
    DriverStation.refreshData();

    motor = new TalonFX(1);
    StatusSignal<?> velocity = motor.getVelocity();
    StatusCode settled = BaseStatusSignal.waitForAll(0.5, velocity);
    assertTrue(settled.isOK(), "device did not settle after construction: " + settled);
  }

  @AfterEach
  void tearDown() {
    motor.close();
    HAL.shutdown();
  }

  @Test
  void simulatedTalonReachesCommandedVelocity() {
    motor.getSimState().setSupplyVoltage(12.0);
    motor.setControl(new VelocityVoltage(10.0));

    StatusSignal<?> velocity = motor.getVelocity();
    StatusCode settled = BaseStatusSignal.waitForAll(0.1, velocity);
    assertTrue(settled.isOK(), "signal did not update after control request: " + settled);

    assertEquals(0.0, motor.getSimState().getMotorVoltageMeasure().baseUnitMagnitude(), 24.0);
  }
}
```

The final assertion is deliberately loose. The spike measures **determinism of the wait**, not correctness of the physics — a tight assertion would conflate "Phoenix sim is flaky on CI" with "my expected value was wrong," and the whole point is to tell those apart.

- [ ] **Step 4: Run the spike 20× on `ubuntu-latest`**

Local runs prove nothing about a shared runner. Push a temporary workflow.

Create `.github/workflows/phoenix-spike.yml`:

```yaml
name: Phoenix Spike (temporary — delete after Task 1)

on: workflow_dispatch

jobs:
  spike:
    runs-on: ubuntu-latest
    strategy:
      fail-fast: false
      matrix:
        run: [1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20]
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: 'gradle'
      - working-directory: projects/m6-testing
        run: chmod +x gradlew
      - working-directory: projects/m6-testing
        run: ./gradlew test --console=plain --rerun-tasks
```

Then:

```bash
git checkout -b spike/m6-phoenix-ci
git add -A && git commit -m "spike: measure Phoenix sim flake on ubuntu CI"
git push -u origin spike/m6-phoenix-ci
gh workflow run phoenix-spike.yml --ref spike/m6-phoenix-ci
# wait for completion, then:
gh run list --workflow=phoenix-spike.yml --limit 1
gh run view <run-id>
```

**Record the actual pass count out of 20.** Not "it worked" — the number.

- [ ] **Step 5: Decide, and report the verdict**

| Result | Verdict | Effect on Task 2 |
|---|---|---|
| 20/20 pass | Signal waits are stable | Task 2 Step 6 proceeds as written |
| 18–19/20 | Marginal | Re-run once. If it does not reach 20/20, treat as the failure row |
| ≤17/20, or any `UnsatisfiedLinkError` | Signal waits are not sufficient on CI | Task 2 Step 6 takes its **fallback**: the Phoenix test carries `@Tag("phoenix-sim")`, `build.gradle`'s `test` block excludes that tag when `System.getenv("CI") != null`, and the simulated-device DoD item becomes mentor-verified locally. Task 9 must then state the exclusion in `README.md` so a student is not confused by a test that runs at home and not in CI |

An `UnsatisfiedLinkError` in any run is an automatic failure row regardless of count — it means the natives are not reaching the test JVM at all, and a 19/20 built on top of that is measuring the wrong thing.

- [ ] **Step 6: Tear the spike down**

```bash
rm projects/m6-testing/src/test/java/frc/robot/SpikeTest.java
rm .github/workflows/phoenix-spike.yml
git add -A && git commit -m "spike: remove Phoenix CI probe"
git push
gh api -X DELETE repos/IgniteRobotics/ignition-labs/git/refs/heads/spike/m6-phoenix-ci
git checkout - && git branch -D spike/m6-phoenix-ci
```

The spike is a measurement, not an artifact. Leaving `SpikeTest.java` in the tree would ship M6 with two given tests, breaking L0.

- [ ] **Step 7: Verify the empty scaffold builds**

```bash
export JAVA_HOME=~/wpilib/2026/jdk
cd projects/m6-testing
./gradlew assemble --console=plain    # expect rc=0
./gradlew spotlessCheck --console=plain  # expect rc=0
```

`test` is not run here — there are no sources yet. `assemble` green on an empty source tree confirms the plugins, the toolchain, and both vendordeps resolve.

- [ ] **Step 8: Commit**

```bash
cd /Users/y0shi/workspace/ignite/docs
git status --short   # confirm no reference implementation, no SpikeTest.java, no spike workflow
git add projects/m6-testing
git commit -m "Scaffold projects/m6-testing with Phoenix 6

GradleRIO without a robot, following M2: no TimedRobot in the project whose
lesson is not to construct one in a test. wpi.java.configureTestTasks(test)
supplies the desktop natives Phoenix simulation needs.

Phoenix CI spike: <N>/20 on ubuntu-latest. <verdict>"
```

Replace `<N>` and `<verdict>` with the spike's actual result. A commit message that omits the number defeats the purpose of having run it.
