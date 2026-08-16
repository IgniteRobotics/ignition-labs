# Task 1: `projects/m4-control/` scaffold

M4 is the second GradleRIO robot project in this repository (after M3) and the first that
carries a CTRE vendordep. The scaffold is almost entirely M3's shape — same plugins, same
`frc.robot.Main`, same simulator GUI wiring — plus `Phoenix6-26.1.1.json` dropped into
`vendordeps/`. Nothing in `build.gradle` needs to change to "add Phoenix": vendor
dependencies are auto-discovered from every `.json` file in `vendordeps/`, which is why M6
needed no Phoenix-specific Gradle lines either.

**Files:**
- Create: `projects/m4-control/` — `settings.gradle`, `build.gradle`, `gradlew`,
  `gradlew.bat`, `gradle/wrapper/`, `.gitignore`, `WPILib-License.md`, `.wpilib/`,
  `simgui.json`, `simgui-ds.json`, `vendordeps/WPILibNewCommands.json`,
  `vendordeps/Phoenix6-26.1.1.json`
- Test: `./gradlew assemble`, `./gradlew spotlessCheck` on the empty scaffold

**Consumes:** nothing.
**Produces:** a buildable `projects/m4-control/` with both vendordeps resolving and an empty
`frc.robot` source tree, ready for Task 2's plants.

**Lane:** `milestone`. Worktree `.worktrees/m4`.

---

- [ ] **Step 1: Copy the scaffold from M3**

```bash
cd /Users/y0shi/workspace/ignite/docs
mkdir -p projects/m4-control
cp -r projects/m3-command-based/gradle projects/m4-control/
cp projects/m3-command-based/gradlew projects/m3-command-based/gradlew.bat \
   projects/m3-command-based/.gitignore projects/m3-command-based/WPILib-License.md \
   projects/m3-command-based/settings.gradle \
   projects/m3-command-based/simgui.json projects/m3-command-based/simgui-ds.json \
   projects/m4-control/
mkdir -p projects/m4-control/.wpilib projects/m4-control/vendordeps
cp projects/m3-command-based/.wpilib/wpilib_preferences.json projects/m4-control/.wpilib/
cp projects/m3-command-based/vendordeps/WPILibNewCommands.json projects/m4-control/vendordeps/
cp projects/m6-testing/vendordeps/Phoenix6-26.1.1.json projects/m4-control/vendordeps/
chmod +x projects/m4-control/gradlew
```

`settings.gradle` is generic — no `rootProject.name` line, same as M3 and every project
before it; Gradle falls back to the directory name (`m4-control`), which is what we want.
Do not add one.

**Do not create `networktables.json`.** It is a runtime artifact `.gitignore` already
excludes (see M3's `.gitignore`); no project in this repository ships one, and creating an
empty placeholder would just be a file nobody reads that Task 9's `git status --short`
check has to look past.

- [ ] **Step 2: Write `build.gradle`**

Byte-for-byte M3's shape. The only content difference from M3's own `build.gradle` is the
top comment, which was M3-specific ("the first project that is actually a robot") and
would be wrong here.

Create `projects/m4-control/build.gradle`:

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

// M4 is the first project in this repository where Phoenix 6 is the lesson rather than
// incidental plumbing. No Gradle wiring below is Phoenix-specific: wpi.java.vendor.java()
// and friends discover every vendordep from vendordeps/*.json automatically, which is why
// adding Phoenix6-26.1.1.json to that directory (Task 1 Step 1) is the whole story.
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
    // Epilogue's annotation processor. Robot.java is annotated @Logged and calls
    // Epilogue.bind(this); without this line the generated Epilogue class does not
    // exist and Robot.java does not compile. Matches Robot-2026's build.gradle.
    annotationProcessor wpi.java.deps.wpilibAnnotations()

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

// Registers the simulator GUI and the simulated driver station as HAL extensions.
// Without these, ./gradlew simulateJava starts the robot program headless -- it runs,
// but no window opens, so there is nothing to map a joystick onto.
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
```

- [ ] **Step 3: Verify the empty scaffold builds**

```bash
export JAVA_HOME=~/wpilib/2026/jdk
cd projects/m4-control
./gradlew assemble --console=plain        # expect rc=0
./gradlew spotlessCheck --console=plain   # expect rc=0
```

`assemble` green on an empty source tree confirms the plugins and both vendordeps
resolve — `WPILibNewCommands.json` (already proven by M3) and `Phoenix6-26.1.1.json`
(already proven by M6). `test` is not run here; there are no sources yet.

- [ ] **Step 4: Commit**

```bash
cd /Users/y0shi/workspace/ignite/docs
git status --short   # only projects/m4-control/, nothing else
git add projects/m4-control
git commit -m "Scaffold projects/m4-control with Phoenix 6

M3's shape verbatim -- GradleRIO, frc.robot.Main, the simulator GUI and
driver station -- plus Phoenix6-26.1.1.json in vendordeps/. No build.gradle
change was needed to add the vendordep; wpi.java.vendor.java() discovers it
from the directory the same way M6 does."
```
