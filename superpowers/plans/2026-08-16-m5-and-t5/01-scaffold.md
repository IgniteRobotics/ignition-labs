# Task 1: `projects/m5-auto-vision/` scaffold

M5 is the third GradleRIO robot project in this repository and the first with four
vendordeps. The scaffold is M4's shape verbatim — same plugins, same `frc.robot.Main`,
same simulator GUI wiring — plus two new vendordeps (`PathplannerLib-2026.1.2.json`,
`photonlib.json`) alongside the two M4 already carries (`Phoenix6-26.1.1.json`,
`WPILibNewCommands.json`), and a `src/main/deploy/pathplanner/` directory that ships
before any Java exists, because Task 2's `SwerveDriveSim` reads
`RobotConfig.fromGUISettings()` from it the moment it is constructed.

**Nothing in `build.gradle` changes to "add" the two new vendordeps.** Exactly as M4's own
scaffold task noted for Phoenix 6: `wpi.java.vendor.java()` and friends discover every
`.json` file under `vendordeps/` automatically. `build.gradle` below is M4's file with only
the top comment changed, because M4's comment was M4-specific and would be wrong here.

**The two new vendordep JSONs are pinned verbatim from `Robot-2026` at
`28ce5397d88275001d656dbb717d22cf9ac0150d`** — confirmed against a local checkout of that
exact commit before writing this task (`git rev-parse HEAD` on
`/Users/y0shi/workspace/ignite/Robot-2026` resolved to that SHA). `PathplannerLib-2026.1.2.json`
and `photonlib.json` below are byte-for-byte copies of `Robot-2026/vendordeps/*.json` at
that commit, not hand-authored — do not "clean up" formatting or reorder keys.

**`src/main/deploy/pathplanner/settings.json` is this project's own, not a copy of
`Robot-2026`'s.** `Robot-2026`'s settings.json describes a real competition robot (its own
mass, track width, eleven path folders full of that team's own paths) — none of which
exists here. M5 ships exactly one example path and a settings file whose geometry matches
`SwerveDriveSim`'s own kinematics, pinned in Task 2. `flModuleX`/`flModuleY`/etc. below are
**not arbitrary** — they must equal the module translations Task 2's `SwerveDriveKinematics`
is built from (±0.3m square), or `RobotConfig.fromGUISettings()` describes a robot that
does not match the plant PathPlanner is actually driving.

**Files:**
- Create: `projects/m5-auto-vision/` — `settings.gradle`, `build.gradle`, `gradlew`,
  `gradlew.bat`, `gradle/wrapper/`, `.gitignore`, `WPILib-License.md`, `.wpilib/`,
  `simgui.json`, `simgui-ds.json`, `vendordeps/Phoenix6-26.1.1.json`,
  `vendordeps/WPILibNewCommands.json`, `vendordeps/PathplannerLib-2026.1.2.json`,
  `vendordeps/photonlib.json`, `src/main/deploy/pathplanner/settings.json`,
  `src/main/deploy/pathplanner/paths/Example Path.path`
- Test: `./gradlew assemble` on the empty scaffold

**Consumes:** nothing.
**Produces:** a buildable `projects/m5-auto-vision/` with all four vendordeps resolving
and a deploy directory `RobotConfig.fromGUISettings()` and `PathPlannerPath.fromPathFile(...)`
can both read, ready for Task 2's plants.

**Lane:** `milestone`. Worktree `.worktrees/m5` — every path below is relative to
`/Users/y0shi/workspace/ignite/docs/.worktrees/m5`, and every `cd` back to the repo root
in this task means that path, not the main checkout.

---

- [ ] **Step 1: Copy the scaffold from M4**

```bash
cd /Users/y0shi/workspace/ignite/docs/.worktrees/m5
mkdir -p projects/m5-auto-vision
cp -r projects/m4-control/gradle projects/m5-auto-vision/
cp projects/m4-control/gradlew projects/m4-control/gradlew.bat \
   projects/m4-control/.gitignore projects/m4-control/WPILib-License.md \
   projects/m4-control/settings.gradle \
   projects/m4-control/simgui.json projects/m4-control/simgui-ds.json \
   projects/m5-auto-vision/
mkdir -p projects/m5-auto-vision/.wpilib projects/m5-auto-vision/vendordeps
cp projects/m4-control/.wpilib/wpilib_preferences.json projects/m5-auto-vision/.wpilib/
cp projects/m4-control/vendordeps/Phoenix6-26.1.1.json projects/m5-auto-vision/vendordeps/
cp projects/m4-control/vendordeps/WPILibNewCommands.json projects/m5-auto-vision/vendordeps/
chmod +x projects/m5-auto-vision/gradlew
```

`settings.gradle` is generic — no `rootProject.name` line, same as every project before
it; Gradle falls back to the directory name (`m5-auto-vision`). Do not add one.

**Do not create `networktables.json`.** Same rule as every prior milestone: it is a
runtime artifact `.gitignore` already excludes, and creating an empty placeholder is a
file nobody reads that Task 9's `git status --short` check has to look past.

- [ ] **Step 2: Write `build.gradle`**

Byte-for-byte M4's shape. The only content difference is the top comment.

Create `projects/m5-auto-vision/build.gradle`:

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

// M5 carries four vendordeps -- Phoenix6, WPILibNewCommands, PathplannerLib, photonlib --
// and none of the Gradle wiring below is specific to any of them: wpi.java.vendor.java()
// and friends discover every vendordep from vendordeps/*.json automatically, exactly as
// M4 discovered Phoenix6 with no build.gradle change. Dropping the two new JSONs into
// vendordeps/ (Task 1 Step 3) is the whole story.
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

- [ ] **Step 3: Write the two new vendordep JSONs, verbatim from `Robot-2026` @ `28ce539`**

Create `projects/m5-auto-vision/vendordeps/PathplannerLib-2026.1.2.json`:

```json
{
    "fileName": "PathplannerLib-2026.1.2.json",
    "name": "PathplannerLib",
    "version": "2026.1.2",
    "uuid": "1b42324f-17c6-4875-8e77-1c312bc8c786",
    "frcYear": "2026",
    "mavenUrls": [
        "https://3015rangerrobotics.github.io/pathplannerlib/repo"
    ],
    "jsonUrl": "https://3015rangerrobotics.github.io/pathplannerlib/PathplannerLib.json",
    "javaDependencies": [
        {
            "groupId": "com.pathplanner.lib",
            "artifactId": "PathplannerLib-java",
            "version": "2026.1.2"
        }
    ],
    "jniDependencies": [],
    "cppDependencies": [
        {
            "groupId": "com.pathplanner.lib",
            "artifactId": "PathplannerLib-cpp",
            "version": "2026.1.2",
            "libName": "PathplannerLib",
            "headerClassifier": "headers",
            "sharedLibrary": false,
            "skipInvalidPlatforms": true,
            "binaryPlatforms": [
                "windowsx86-64",
                "linuxx86-64",
                "osxuniversal",
                "linuxathena",
                "linuxarm32",
                "linuxarm64"
            ]
        }
    ]
}
```

Create `projects/m5-auto-vision/vendordeps/photonlib.json`:

```json
{
    "fileName": "photonlib.json",
    "name": "photonlib",
    "version": "v2026.2.1",
    "uuid": "515fe07e-bfc6-11fa-b3de-0242ac130004",
    "frcYear": "2026",
    "mavenUrls": [
        "https://maven.photonvision.org/repository/internal",
        "https://maven.photonvision.org/repository/snapshots"
    ],
    "jsonUrl": "https://maven.photonvision.org/repository/internal/org/photonvision/photonlib-json/1.0/photonlib-json-1.0.json",
    "jniDependencies": [
        {
            "groupId": "org.photonvision",
            "artifactId": "photontargeting-cpp",
            "version": "v2026.2.1",
            "skipInvalidPlatforms": true,
            "isJar": false,
            "validPlatforms": [
                "windowsx86-64",
                "linuxathena",
                "linuxx86-64",
                "osxuniversal"
            ]
        }
    ],
    "cppDependencies": [
        {
            "groupId": "org.photonvision",
            "artifactId": "photonlib-cpp",
            "version": "v2026.2.1",
            "libName": "photonlib",
            "headerClassifier": "headers",
            "sharedLibrary": true,
            "skipInvalidPlatforms": true,
            "binaryPlatforms": [
                "windowsx86-64",
                "linuxathena",
                "linuxx86-64",
                "osxuniversal"
            ]
        },
        {
            "groupId": "org.photonvision",
            "artifactId": "photontargeting-cpp",
            "version": "v2026.2.1",
            "libName": "photontargeting",
            "headerClassifier": "headers",
            "sharedLibrary": true,
            "skipInvalidPlatforms": true,
            "binaryPlatforms": [
                "windowsx86-64",
                "linuxathena",
                "linuxx86-64",
                "osxuniversal"
            ]
        }
    ],
    "javaDependencies": [
        {
            "groupId": "org.photonvision",
            "artifactId": "photonlib-java",
            "version": "v2026.2.1"
        },
        {
            "groupId": "org.photonvision",
            "artifactId": "photontargeting-java",
            "version": "v2026.2.1"
        }
    ]
}
```

- [ ] **Step 4: Write the PathPlanner deploy directory**

`RobotConfig.fromGUISettings()` (Task 2) reads `settings.json` below at
`SwerveDriveSim` construction time — get its geometry right now, because Task 2 does not
re-derive it. `flModuleX`/`flModuleY`/`frModuleX`/`frModuleY`/`blModuleX`/`blModuleY`/
`brModuleX`/`brModuleY` are `±0.3` — this is not a placeholder number, it is
`SwerveDriveSim`'s own module geometry (0.6m track width, 0.6m wheelbase, pinned in
`00-plan.md`'s API contract's surrounding design and restated in Task 2). If Task 2 ever
changes the track width or wheelbase, this file has to change with it.

```bash
mkdir -p "projects/m5-auto-vision/src/main/deploy/pathplanner/paths"
```

Create `projects/m5-auto-vision/src/main/deploy/pathplanner/settings.json`:

```json
{
  "robotWidth": 0.9,
  "robotLength": 0.9,
  "holonomicMode": true,
  "pathFolders": [],
  "autoFolders": [],
  "defaultMaxVel": 2.0,
  "defaultMaxAccel": 2.0,
  "defaultMaxAngVel": 360.0,
  "defaultMaxAngAccel": 540.0,
  "defaultNominalVoltage": 12.0,
  "robotMass": 45.0,
  "robotMOI": 4.0,
  "robotTrackwidth": 0.6,
  "driveWheelRadius": 0.0508,
  "driveGearing": 6.75,
  "maxDriveSpeed": 4.0,
  "driveMotorType": "krakenX60",
  "driveCurrentLimit": 60.0,
  "wheelCOF": 1.2,
  "flModuleX": 0.3,
  "flModuleY": 0.3,
  "frModuleX": 0.3,
  "frModuleY": -0.3,
  "blModuleX": -0.3,
  "blModuleY": 0.3,
  "brModuleX": -0.3,
  "brModuleY": -0.3,
  "bumperOffsetX": 0.0,
  "bumperOffsetY": 0.0,
  "robotFeatures": []
}
```

**One example path**, per spec §9.6 — a short, straight run near the `k2026RebuiltWelded`
tag cluster around `(4.6, 4.0)` (confirmed against the field layout shipped in
`apriltag-java-2026.2.1.jar`: tags 18–21 and 24–27 all sit within about a meter of that
point), so a student who follows it in `simulateJava` sees tags in frame without having to
go find them. Straight line, no rotation change, two waypoints — composing something more
interesting out of it is the milestone, not this file.

Create `projects/m5-auto-vision/src/main/deploy/pathplanner/paths/Example Path.path`:

```json
{
  "version": "2025.0",
  "waypoints": [
    {
      "anchor": {
        "x": 2.0,
        "y": 4.0
      },
      "prevControl": null,
      "nextControl": {
        "x": 2.5,
        "y": 4.0
      },
      "isLocked": false,
      "linkedName": null
    },
    {
      "anchor": {
        "x": 3.5,
        "y": 4.0
      },
      "prevControl": {
        "x": 3.0,
        "y": 4.0
      },
      "nextControl": null,
      "isLocked": false,
      "linkedName": null
    }
  ],
  "rotationTargets": [],
  "constraintZones": [],
  "pointTowardsZones": [],
  "eventMarkers": [],
  "globalConstraints": {
    "maxVelocity": 2.0,
    "maxAcceleration": 2.0,
    "maxAngularVelocity": 360.0,
    "maxAngularAcceleration": 540.0,
    "nominalVoltage": 12.0,
    "unlimited": false
  },
  "goalEndState": {
    "velocity": 0,
    "rotation": 0.0
  },
  "reversed": false,
  "folder": null,
  "idealStartingState": {
    "velocity": 0,
    "rotation": 0.0
  },
  "useDefaultConstraints": true
}
```

- [ ] **Step 5: Verify the empty scaffold builds**

```bash
export JAVA_HOME=~/wpilib/2026/jdk
cd /Users/y0shi/workspace/ignite/docs/.worktrees/m5/projects/m5-auto-vision
./gradlew assemble --console=plain        # expect rc=0
```

Green here confirms all four vendordeps resolve — `Phoenix6-26.1.1.json` and
`WPILibNewCommands.json` (already proven by M4), plus `PathplannerLib-2026.1.2.json` and
`photonlib.json` for the first time. `spotlessCheck`/`test` are not meaningful yet; there
is no Java source and no test source until Tasks 2–4.

- [ ] **Step 6: Commit**

```bash
cd /Users/y0shi/workspace/ignite/docs/.worktrees/m5
git status --short   # only projects/m5-auto-vision/, nothing else
git add projects/m5-auto-vision
git commit -m "Scaffold projects/m5-auto-vision with PathPlanner and photonlib

M4's shape verbatim -- GradleRIO, frc.robot.Main, the simulator GUI and
driver station -- plus PathplannerLib-2026.1.2.json and photonlib.json
alongside the two vendordeps M4 already carries. No build.gradle change
was needed for either; wpi.java.vendor.java() discovers both from the
directory. Ships its own src/main/deploy/pathplanner/settings.json, sized
to match SwerveDriveSim's module geometry (Task 2), and one example path
near the k2026RebuiltWelded tag cluster at (4.6, 4.0)."
```
