# Task 6: `LogRecorder` and `T5DriveSim` — the fixture that produces T5's three logs

§10 calls T5 "the most expensive artifact in this spec," and the cost is not the Gradle
scaffold — it is proving that three recorded runs of one auto, one drivetrain, one fault each,
actually disagree with each other in exactly the way the fixture's diagnosis depends on, and
agree with each other everywhere else. This task builds the recorder and the plant it runs
against, then spends most of its budget proving that before anything ships. Step 5 is that
proof and is not optional — see design record §3 and `00-plan.md`'s self-review.

Everything in this task was spiked end to end before being written here: a throwaway project
carrying the same vendordep, the same recorder logic, and the same verification code, run
against real PathPlannerLib and a real `.wpilog` writer. Three real bugs turned up during that
spike and are called out at the step that would otherwise walk into them — a silently-degenerate
`RobotConfig` (Step 1 comment in `LogRecorder.robotConfig()`), a WPILib "timestamp 0 means now"
sentinel that breaks determinism if you don't know about it (`LogRecorder`'s `startMicros`
comment), and a real `DataLogIterator.hasNext()` bug that silently drops a log's last record
(Step 5). None of the three are hypothetical — each one produced a wrong or misleading result
until it was found and fixed.

**Files:**
- Create: `projects/testouts/t5-drifting-auto/settings.gradle`, `gradlew`, `gradlew.bat`,
  `gradle/wrapper/gradle-wrapper.jar`, `gradle/wrapper/gradle-wrapper.properties`, `.gitignore`,
  `WPILib-License.md`, `.wpilib/wpilib_preferences.json` (copies from `t4-bad-kv`, **not**
  `t4-bad-kv`'s `.gitignore` verbatim — see Step 1)
- Create: `projects/testouts/t5-drifting-auto/vendordeps/PathplannerLib.json`
- Create: `projects/testouts/t5-drifting-auto/build.gradle`
- Create: `projects/testouts/t5-drifting-auto/src/main/java/frc/testout/t5/plant/T5DriveSim.java`
- Create: `projects/testouts/t5-drifting-auto/src/main/java/frc/testout/t5/LogRecorder.java`
- Create: `projects/testouts/t5-drifting-auto/src/main/java/frc/testout/t5/GenerateLog.java`

No `src/test/` — see the comment atop `build.gradle` in Step 1 for why, and `CLAUDE.md`'s
known-good-exceptions for the M0 precedent this follows.

**Consumes:** nothing. This lane is independent of the M5 lane and shares no file with it — each
`projects/*/` is standalone (spec §4.2), and T5 gets its own drivetrain rather than importing
M5's `frc.robot.plant.SwerveDriveSim`.

**Produces:** `frc.testout.t5.LogRecorder` (`record(Fault, Path)` — signature pinned in
`00-plan.md`'s API contract, byte-for-byte) and the `generateLog` Gradle task. Task 7 runs
`generateLog` three times to produce the committed logs; Task 8's answer key in
`../ignite-training-solutions/` is written against the same `LogRecorder`.

**Lane:** `testout`. Work in `.worktrees/t5`.

---

- [ ] **Step 1: Copy the scaffold, add the one vendordep, write `build.gradle`**

```bash
cd /Users/y0shi/workspace/ignite/docs
mkdir -p projects/testouts/t5-drifting-auto
cd projects/testouts/t4-bad-kv
for f in settings.gradle gradlew gradlew.bat gradle/wrapper/gradle-wrapper.jar \
         gradle/wrapper/gradle-wrapper.properties WPILib-License.md \
         .wpilib/wpilib_preferences.json; do
  mkdir -p "../t5-drifting-auto/$(dirname "$f")"
  cp "$f" "../t5-drifting-auto/$f"
done
chmod +x ../t5-drifting-auto/gradlew
```

**Do not copy `t4-bad-kv`'s `.gitignore` verbatim** — it excludes `logs/`, which is exactly the
directory Task 7 has to commit into. Write T5's own:

`projects/testouts/t5-drifting-auto/.gitignore`:

```gitignore
build/
bin/
.gradle/
networktables.json
simgui-window.json
```

`projects/testouts/t5-drifting-auto/vendordeps/PathplannerLib.json` — fetched from
`https://3015rangerrobotics.github.io/pathplannerlib/PathplannerLib.json` and confirmed to
resolve `PathplannerLib-java:2026.1.2` (matches the pinned version in `00-plan.md`'s tech
stack) during the spike:

```json
{
    "fileName": "PathplannerLib.json",
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

**No `WPILibNewCommands` vendordep.** It was in the spike's first pass on the theory that
PathPlanner's event-marker machinery might need `edu.wpi.first.wpilibj2.command.Command` on the
classpath — it doesn't, because this recorder never touches `PathPlannerPath`'s event-marker or
named-command APIs at all (see `LogRecorder`'s Javadoc in Step 3 for why: named commands are
simulated directly, not scheduled). The spike's project compiled, and `generateLog` ran and
produced a correct log, with the vendordep removed. Spec §9's "minimum vendordep set" applies
here as much as to any milestone.

`projects/testouts/t5-drifting-auto/build.gradle` — `t4-bad-kv/build.gradle` verbatim in its
plugin/dependency/jar wiring, with the main class swapped and `generateResponse` replaced by
`generateLog`:

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

// This is a test-out fixture, not a robot project, and it ships no src/test -- there is no
// code here for a student to fix. T5 is a log-diagnosis exercise (spec §10): the given,
// locked recorder below produces the three logs under logs/, and the student's job is to read
// one of them, not to make a test go green. See CLAUDE.md's known-good-exceptions for the
// precedent (M0 ships no stubs either, and the inverted tests_lock/clean-clone check skips
// projects with no src/test rather than failing them). GradleRIO is applied anyway, same
// reason as every other project: the toolchain, so `generateLog` runs through the same
// `./gradlew` a student already knows.
def ROBOT_MAIN_CLASS = "frc.testout.t5.GenerateLog"

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
wpi.java.configureTestTasks(test)

tasks.withType(JavaCompile) {
    options.compilerArgs.add '-XDstringConcat=inline'
}

// The fixture's one runnable thing. Runs LogRecorder for one fault and writes it to one exact
// destination file -- see GenerateLog's Javadoc and logs/README.md for why the fault and the
// destination are both required arguments rather than something this task hardcodes.
//
//   ./gradlew generateLog -Pfault=ODOMETRY_DRIFT -Pout=/some/path/recording.wpilog
//
// Same java.library.path wiring as T4's generateResponse task: FlywheelSim isn't in this
// project, but PathPlannerLib's trajectory generation goes through the same wpimathjni native
// library, and extractReleaseNative is what puts it where JavaExec can find it.
tasks.register('generateLog', JavaExec) {
    group = 'training'
    description = 'Runs LogRecorder for -Pfault=<name> and writes -Pout=<path>.'
    dependsOn 'extractReleaseNative'
    classpath = sourceSets.main.runtimeClasspath
    mainClass = 'frc.testout.t5.GenerateLog'
    systemProperty 'java.library.path', "$buildDir/jni/release"
    doFirst {
        if (!project.hasProperty('fault') || !project.hasProperty('out')) {
            throw new GradleException(
                "generateLog requires -Pfault=<ODOMETRY_DRIFT|MISTUNED_CONTROLLER|BLOCKING_COMMAND> " +
                "and -Pout=<destination path>")
        }
    }
    args = [
        project.findProperty('fault') ?: '',
        project.findProperty('out') ?: ''
    ]
}
```

- [ ] **Step 2: Write the given, locked drivetrain**

`src/main/java/frc/testout/t5/plant/T5DriveSim.java` — kinematic only, same reason M4's and
T4's plants are built from physical quantities in code, not a vendor sim: nothing about *this*
fixture's lesson is about the drivetrain.

```java
package frc.testout.t5.plant;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;

/**
 * A kinematic swerve drivetrain -- integrates commanded {@link ChassisSpeeds}, no motors, no
 * Phoenix, deterministic. This is the drivetrain {@code LogRecorder}'s recordings ran against.
 * Given, locked -- part of the fixture. {@code tests.lock} and {@code CODEOWNERS} both cover this
 * file (spec §12.2).
 *
 * <p>Shaped after M5's {@code frc.robot.plant.SwerveDriveSim} (same reason M4's plants and T4's
 * plant are built from physical quantities in code: nothing here needs a vendor simulator, and a
 * plain kinematic integrator is enough to make three recordings that disagree with each other only
 * in the one way each fault is supposed to make them disagree).
 */
public final class T5DriveSim {
  private static final double TRACK_WIDTH_METERS = 0.60;
  private static final double WHEEL_BASE_METERS = 0.60;

  private static final SwerveDriveKinematics KINEMATICS =
      new SwerveDriveKinematics(
          new Translation2d(WHEEL_BASE_METERS / 2, TRACK_WIDTH_METERS / 2),
          new Translation2d(WHEEL_BASE_METERS / 2, -TRACK_WIDTH_METERS / 2),
          new Translation2d(-WHEEL_BASE_METERS / 2, TRACK_WIDTH_METERS / 2),
          new Translation2d(-WHEEL_BASE_METERS / 2, -TRACK_WIDTH_METERS / 2));

  private Pose2d pose = new Pose2d();
  private ChassisSpeeds commanded = new ChassisSpeeds();
  private final double[] wheelDistanceMeters = new double[4];
  private SwerveModuleState[] moduleStates =
      new SwerveModuleState[] {
        new SwerveModuleState(), new SwerveModuleState(),
        new SwerveModuleState(), new SwerveModuleState()
      };

  /** Commands robot-relative chassis speeds; takes effect on the next {@link #step}. */
  public void drive(ChassisSpeeds robotRelativeSpeeds) {
    this.commanded = robotRelativeSpeeds;
  }

  /** Advances true pose, wheel distances, and module states by {@code dtSeconds}. */
  public void step(double dtSeconds) {
    moduleStates = KINEMATICS.toSwerveModuleStates(commanded);
    for (int i = 0; i < 4; i++) {
      wheelDistanceMeters[i] += moduleStates[i].speedMetersPerSecond * dtSeconds;
    }
    Translation2d fieldDelta =
        new Translation2d(
                commanded.vxMetersPerSecond * dtSeconds, commanded.vyMetersPerSecond * dtSeconds)
            .rotateBy(pose.getRotation());
    Rotation2d newRotation =
        pose.getRotation().plus(new Rotation2d(commanded.omegaRadiansPerSecond * dtSeconds));
    pose = new Pose2d(pose.getTranslation().plus(fieldDelta), newRotation);
  }

  /** Ground truth pose. Never available to a real robot's own code -- exists here for logging. */
  public Pose2d truePose() {
    return pose;
  }

  /** Ground truth heading. A recording's odometry estimator may choose to corrupt this. */
  public Rotation2d gyroAngle() {
    return pose.getRotation();
  }

  /** Ground truth per-module velocity and angle, as of the most recent {@link #step}. */
  public SwerveModuleState[] moduleStates() {
    return moduleStates;
  }

  /** Ground truth per-module distance and angle, as of the most recent {@link #step}. */
  public SwerveModulePosition[] modulePositions() {
    SwerveModulePosition[] out = new SwerveModulePosition[4];
    for (int i = 0; i < 4; i++) {
      out[i] = new SwerveModulePosition(wheelDistanceMeters[i], moduleStates[i].angle);
    }
    return out;
  }

  /** The kinematics every module position and state above is defined against. */
  public static SwerveDriveKinematics kinematics() {
    return KINEMATICS;
  }

  /** Teleports the true pose. Does not touch wheel distances -- only used before a run starts. */
  public void resetTo(Pose2d newPose) {
    pose = newPose;
  }
}
```

- [ ] **Step 3: Write the recorder**

`src/main/java/frc/testout/t5/LogRecorder.java` — the pinned contract from `00-plan.md`,
byte-for-byte on the public surface (`enum Fault`, `record(Fault, Path)`). Everything below the
pinned surface — the path shape, the two tunings, the drift rate, the holding-step duration — is
this task's own design, validated by Step 5.

One routine, three phases: follow a path, run named command `step2`, run named command `step3`.
Both named commands are simulated directly (the drivetrain holds position; `step2`/`step3` are
just durations with start/end markers) rather than scheduled through
`edu.wpi.first.wpilibj2.command.Command` — same reason T4's plant deliberately avoids the HAL
(`T4FlywheelPlant.setVoltage`'s Javadoc): this fixture never initializes the HAL, and routing a
named command through the real `CommandScheduler` would need it.

```java
package frc.testout.t5;

import com.pathplanner.lib.config.ModuleConfig;
import com.pathplanner.lib.config.RobotConfig;
import com.pathplanner.lib.path.GoalEndState;
import com.pathplanner.lib.path.IdealStartingState;
import com.pathplanner.lib.path.PathConstraints;
import com.pathplanner.lib.path.PathPlannerPath;
import com.pathplanner.lib.path.Waypoint;
import com.pathplanner.lib.trajectory.PathPlannerTrajectory;
import com.pathplanner.lib.trajectory.PathPlannerTrajectoryState;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveOdometry;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.util.datalog.DataLogWriter;
import edu.wpi.first.util.datalog.StringLogEntry;
import edu.wpi.first.util.datalog.StructArrayLogEntry;
import edu.wpi.first.util.datalog.StructLogEntry;
import frc.testout.t5.plant.T5DriveSim;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Runs one auto routine against {@link T5DriveSim} with exactly one fault injected, and writes
 * every tick to a {@code .wpilog}. Given, locked -- part of the fixture. {@code tests.lock} and
 * {@code CODEOWNERS} both cover this file (spec §12.2).
 *
 * <p>This is a recording, not a synthesized trace: the same auto runs against the same drivetrain
 * three times, once per {@link Fault}, and every signal in the resulting log came from that one
 * execution. See {@code superpowers/specs/2026-08-16-m5-auto-vision-design.md} §3 for why that
 * matters.
 *
 * <p>The routine is three steps: follow a path, run a named command ({@code step2}), run a second
 * named command ({@code step3}). {@link Fault#ODOMETRY_DRIFT} biases the gyro angle fed to the
 * odometry estimator (never the true pose) so the path-following controller -- which only ever sees
 * the estimate, exactly like a real robot -- drives the *true* robot off the intended path while
 * believing it is on target. {@link Fault#MISTUNED_CONTROLLER} swaps in a proportional gain too low
 * to track the path's speed, producing a large, persistent gap between the path's target pose and
 * the estimate while the estimate itself stays accurate. {@link Fault#BLOCKING_COMMAND} makes
 * {@code step2} never report finished; the recorder caps total run time and stops, so {@code step3}
 * never starts and its markers never appear.
 *
 * <p>The output file is always named {@code recording.wpilog} inside {@code outputDirectory} --
 * deliberately independent of which fault was recorded. Renaming it into {@code logs/} is the
 * caller's job (see {@link GenerateLog}), specifically so nothing in this class ever has to know or
 * reveal the letter a run will end up as.
 */
public final class LogRecorder {
  /** One root cause per recorded run. */
  public enum Fault {
    ODOMETRY_DRIFT,
    MISTUNED_CONTROLLER,
    BLOCKING_COMMAND
  }

  private static final double DT_SECONDS = 0.02;
  private static final double STEP2_NOMINAL_SECONDS = 0.5;
  private static final double STEP3_NOMINAL_SECONDS = 0.4;
  private static final double MAX_STEP_SECONDS = 8.0;
  private static final double GYRO_DRIFT_RAD_PER_TICK = 0.012;
  private static final double GOOD_KP = 12.0;
  private static final double GOOD_KP_THETA = 10.0;
  private static final double BAD_KP = 2.5;
  private static final double BAD_KP_THETA = 2.0;

  private LogRecorder() {}

  /**
   * Runs the fixture's one auto routine once, with {@code fault} injected, and writes the result to
   * {@code outputDirectory/recording.wpilog}.
   *
   * @param fault which root cause to inject into this run
   * @param outputDirectory directory to write into; created if it does not exist
   * @return the path actually written
   */
  public static Path record(Fault fault, Path outputDirectory) throws IOException {
    Files.createDirectories(outputDirectory);
    Path out = outputDirectory.resolve("recording.wpilog");

    RobotConfig config = robotConfig();
    PathPlannerTrajectory trajectory = trajectory(config);

    double kP = fault == Fault.MISTUNED_CONTROLLER ? BAD_KP : GOOD_KP;
    double kPTheta = fault == Fault.MISTUNED_CONTROLLER ? BAD_KP_THETA : GOOD_KP_THETA;

    T5DriveSim drive = new T5DriveSim();
    SwerveDriveOdometry odometry =
        new SwerveDriveOdometry(
            T5DriveSim.kinematics(), drive.gyroAngle(), drive.modulePositions(), new Pose2d());

    // Timestamp 0 means "use wall-clock now" to every WPILib DataLog call that takes one --
    // entry creation as well as data appends. Passing 0 anywhere here would make two
    // regenerations of the same fault differ byte-for-byte even though every value in them is
    // identical. Start one simulated tick in so every timestamp this method ever passes,
    // including each entry's own creation time, is the deterministic simulated clock.
    long startMicros = (long) (DT_SECONDS * 1_000_000);

    try (DataLogWriter log = new DataLogWriter(out.toString())) {
      StructLogEntry<Pose2d> truePoseLog =
          StructLogEntry.create(log, "/t5/truePose", Pose2d.struct, startMicros);
      StructLogEntry<Pose2d> estimatedPoseLog =
          StructLogEntry.create(log, "/t5/estimatedPose", Pose2d.struct, startMicros);
      StructLogEntry<Pose2d> pathTargetPoseLog =
          StructLogEntry.create(log, "/t5/pathTargetPose", Pose2d.struct, startMicros);
      StructArrayLogEntry<SwerveModuleState> moduleStatesLog =
          StructArrayLogEntry.create(
              log, "/t5/moduleStates", SwerveModuleState.struct, startMicros);
      StringLogEntry eventsLog = new StringLogEntry(log, "/t5/events", startMicros);

      long micros = startMicros;
      double gyroDriftRad = 0.0;

      eventsLog.append("path:start", micros);
      double pathEndSeconds = trajectory.getTotalTimeSeconds();
      double tSeconds = 0.0;
      while (tSeconds < pathEndSeconds) {
        PathPlannerTrajectoryState target = trajectory.sample(tSeconds);
        Pose2d estimated = odometry.getPoseMeters();

        Translation2d posError = target.pose.getTranslation().minus(estimated.getTranslation());
        double rotErrorRad = target.pose.getRotation().minus(estimated.getRotation()).getRadians();
        ChassisSpeeds fieldSpeeds =
            new ChassisSpeeds(kP * posError.getX(), kP * posError.getY(), kPTheta * rotErrorRad);
        drive.drive(ChassisSpeeds.fromFieldRelativeSpeeds(fieldSpeeds, estimated.getRotation()));
        drive.step(DT_SECONDS);

        Rotation2d gyroForOdometry = drive.gyroAngle();
        if (fault == Fault.ODOMETRY_DRIFT) {
          gyroDriftRad += GYRO_DRIFT_RAD_PER_TICK;
          gyroForOdometry = gyroForOdometry.plus(new Rotation2d(gyroDriftRad));
        }
        odometry.update(gyroForOdometry, drive.modulePositions());

        truePoseLog.append(drive.truePose(), micros);
        estimatedPoseLog.append(odometry.getPoseMeters(), micros);
        pathTargetPoseLog.append(target.pose, micros);
        moduleStatesLog.append(drive.moduleStates(), micros);

        tSeconds += DT_SECONDS;
        micros += (long) (DT_SECONDS * 1_000_000);
      }
      eventsLog.append("path:end", micros);

      Pose2d endPose = trajectory.getEndState().pose;
      boolean step2Blocks = fault == Fault.BLOCKING_COMMAND;
      eventsLog.append("step2:start", micros);
      micros =
          runHoldingStep(
              drive,
              odometry,
              fault,
              gyroDriftRad,
              endPose,
              micros,
              truePoseLog,
              estimatedPoseLog,
              pathTargetPoseLog,
              moduleStatesLog,
              step2Blocks ? MAX_STEP_SECONDS : STEP2_NOMINAL_SECONDS);
      if (step2Blocks) {
        // step2 never reports finished. The recorder gives up after MAX_STEP_SECONDS, exactly as
        // a mentor watching a real robot would eventually stop it -- step2:end and everything
        // after it never gets written, because it never happened.
        log.flush();
        return out;
      }
      eventsLog.append("step2:end", micros);

      eventsLog.append("step3:start", micros);
      micros =
          runHoldingStep(
              drive,
              odometry,
              fault,
              gyroDriftRad,
              endPose,
              micros,
              truePoseLog,
              estimatedPoseLog,
              pathTargetPoseLog,
              moduleStatesLog,
              STEP3_NOMINAL_SECONDS);
      eventsLog.append("step3:end", micros);

      log.flush();
    }
    return out;
  }

  /**
   * Holds the robot at rest (a stand-in for a named command that does not move the drivetrain, such
   * as an intake or shooter action) for {@code durationSeconds} of simulated time, logging every
   * tick the same way the path-following phase does.
   */
  private static long runHoldingStep(
      T5DriveSim drive,
      SwerveDriveOdometry odometry,
      Fault fault,
      double gyroDriftRadAtStart,
      Pose2d holdTargetPose,
      long startMicros,
      StructLogEntry<Pose2d> truePoseLog,
      StructLogEntry<Pose2d> estimatedPoseLog,
      StructLogEntry<Pose2d> pathTargetPoseLog,
      StructArrayLogEntry<SwerveModuleState> moduleStatesLog,
      double durationSeconds) {
    long micros = startMicros;
    double gyroDriftRad = gyroDriftRadAtStart;
    double elapsedSeconds = 0.0;
    while (elapsedSeconds < durationSeconds) {
      drive.drive(new ChassisSpeeds());
      drive.step(DT_SECONDS);

      Rotation2d gyroForOdometry = drive.gyroAngle();
      if (fault == Fault.ODOMETRY_DRIFT) {
        gyroDriftRad += GYRO_DRIFT_RAD_PER_TICK;
        gyroForOdometry = gyroForOdometry.plus(new Rotation2d(gyroDriftRad));
      }
      odometry.update(gyroForOdometry, drive.modulePositions());

      truePoseLog.append(drive.truePose(), micros);
      estimatedPoseLog.append(odometry.getPoseMeters(), micros);
      pathTargetPoseLog.append(holdTargetPose, micros);
      moduleStatesLog.append(drive.moduleStates(), micros);

      elapsedSeconds += DT_SECONDS;
      micros += (long) (DT_SECONDS * 1_000_000);
    }
    return micros;
  }

  private static RobotConfig robotConfig() {
    double halfTrackMeters = 0.30;
    // .withReduction(6.75) matters: PathPlannerLib derives each module's available accel torque
    // from the motor's current draw AT the module's max wheel speed, with no separate gear-ratio
    // parameter of its own -- it expects the DCMotor passed in to already represent the module's
    // output shaft. Passing the bare motor (no reduction) makes that "max speed" correspond to a
    // motor shaft speed close to stall, so the torque budget for actually accelerating collapses
    // to near zero and every generated trajectory silently comes back with getTotalTimeSeconds()
    // == 0 -- no exception, no warning, just a path that "takes no time" to drive.
    return new RobotConfig(
        55.0,
        6.0,
        new ModuleConfig(0.048, 4.5, 1.2, DCMotor.getKrakenX60(1).withReduction(6.75), 60.0, 1),
        new Translation2d(halfTrackMeters, halfTrackMeters),
        new Translation2d(halfTrackMeters, -halfTrackMeters),
        new Translation2d(-halfTrackMeters, halfTrackMeters),
        new Translation2d(-halfTrackMeters, -halfTrackMeters));
  }

  private static PathPlannerTrajectory trajectory(RobotConfig config) {
    List<Waypoint> waypoints =
        PathPlannerPath.waypointsFromPoses(
            new Pose2d(0, 0, Rotation2d.kZero), new Pose2d(3.0, 1.0, Rotation2d.fromDegrees(90)));
    PathPlannerPath path =
        new PathPlannerPath(
            waypoints,
            new PathConstraints(3.0, 3.0, 2 * Math.PI, 4 * Math.PI),
            new IdealStartingState(0.0, Rotation2d.kZero),
            new GoalEndState(0.0, Rotation2d.fromDegrees(90)));
    return path.generateTrajectory(new ChassisSpeeds(), Rotation2d.kZero, config);
  }
}
```

- [ ] **Step 4: Write the CLI wrapper**

`src/main/java/frc/testout/t5/GenerateLog.java` — takes the fault *and* the exact destination
file as required arguments, on purpose: this is the seam that keeps the letter-to-fault mapping
out of every committed file (see `00-plan.md`'s known-limitation note, and Task 7's Step 2).

```java
package frc.testout.t5;

import frc.testout.t5.LogRecorder.Fault;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Command-line wrapper around {@link LogRecorder}. Given, locked. Takes the fault to record and the
 * exact file to write it to, so which letter a fault ends up as is decided by whoever invokes this
 * -- once, out of band -- and is never written down in this project. See {@code logs/README.md}.
 *
 * <p>Usage: {@code GenerateLog <ODOMETRY_DRIFT|MISTUNED_CONTROLLER|BLOCKING_COMMAND> <destPath>}
 */
public final class GenerateLog {
  private GenerateLog() {}

  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.err.println("usage: GenerateLog <fault> <destPath>");
      System.exit(1);
      return;
    }
    Fault fault = Fault.valueOf(args[0]);
    Path dest = Path.of(args[1]);
    Path tmpDir = Files.createTempDirectory("t5-log-");
    Path recorded = LogRecorder.record(fault, tmpDir);
    Files.createDirectories(dest.toAbsolutePath().getParent());
    Files.move(recorded, dest, StandardCopyOption.REPLACE_EXISTING);
    System.out.println("wrote " + dest.toAbsolutePath());
  }
}
```

- [ ] **Step 5: The discrimination matrix — not optional, the single most important step in this plan**

`00-plan.md`'s self-review names this explicitly: for each of the three faults, confirm the
*other two* causes can be ruled out from signals actually present in that recording. Design
record §3's minimum signal set is true pose, estimated pose, path target pose, per-module state,
event-marker timestamps, and each command's start/end — this fixture logs all six as
`/t5/truePose`, `/t5/estimatedPose`, `/t5/pathTargetPose`, `/t5/moduleStates`, and `/t5/events`
(which carries both the path's own start/end markers and each named command's start/end). A log
missing one makes a ruling-out impossible, and nobody discovers that until a mentor is mid-fixture
with a student — so open the logs and check. Do not reason about what you intended to write.

**5a. Clean-clone-shaped sanity check first.**

```bash
cd /Users/y0shi/workspace/ignite/docs/projects/testouts/t5-drifting-auto
export JAVA_HOME=~/wpilib/2026/jdk
./gradlew clean --console=plain > /dev/null
./gradlew assemble --console=plain;        echo "assemble rc=$? (expect 0)"
./gradlew compileTestJava --console=plain; echo "compileTestJava rc=$? (expect 0)"
./gradlew test --console=plain;            echo "test rc=$? (expect 0 -- NO-SOURCE, no src/test)"
```

Unlike every other project in this repo, `test` is expected to be **green**, not red — there is
no student-owned fix here for a test to gate. That is Step 1's build.gradle comment, not a defect.

**5b. Add a throwaway inspector, run it, read every number.**

This file is not part of the fixture and must not be committed. It exists to decode the logs the
same way a mentor's tooling (AdvantageScope, DataLogTool) would, rather than trusting that the
Java objects `LogRecorder` built in memory are what actually landed on disk.

`src/main/java/frc/testout/t5/Inspect.java` (temporary):

```java
package frc.testout.t5;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.util.datalog.DataLogReader;
import frc.testout.t5.LogRecorder.Fault;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * THROWAWAY -- not part of the fixture, not committed. Generates one recording per {@link Fault}
 * into a scratch directory and decodes each one back the same way a mentor's tooling would, to
 * confirm the three logs actually discriminate before anything ships. Delete this file (and the
 * temporary Gradle task that runs it) before committing Task 6.
 */
public final class Inspect {
  public static void main(String[] args) throws IOException {
    for (Fault f : Fault.values()) {
      Path dir = Path.of("/tmp/t5-discrimination-check", f.name());
      Path written = LogRecorder.record(f, dir);
      System.out.println(f + " -> " + written + " size=" + java.nio.file.Files.size(written));
      inspect(written.toString());
    }
  }

  private static void inspect(String path) throws IOException {
    DataLogReader reader = new DataLogReader(path);
    Map<Integer, String> names = new HashMap<>();
    Map<Integer, Integer> counts = new HashMap<>();
    List<String> events = new ArrayList<>();
    Map<Integer, Pose2d> lastByEntry = new HashMap<>();
    double[] maxControllerError = {0};
    double[] maxTrueVsEst = {0};
    double[] lastTrueVsEst = {0};

    // Deliberately reader.forEach(...), not the for-each/Iterator protocol: DataLogIterator's
    // hasNext() requires (pos + 16 <= size), which silently drops a trailing record if it lands
    // within 16 bytes of end-of-file -- exactly what a short final event string does. forEach()
    // does not have this bug. Using the Iterable form here would report step3:end missing from
    // every log that actually has it.
    reader.forEach(
        rec -> {
          if (rec.isStart()) {
            names.put(rec.getStartData().entry, rec.getStartData().name);
            return;
          }
          if (rec.isControl()) {
            return;
          }
          counts.merge(rec.getEntry(), 1, Integer::sum);
          String nm = names.get(rec.getEntry());
          if ("/t5/events".equals(nm)) {
            events.add(rec.getTimestamp() + "us " + rec.getString());
            return;
          }
          if (!"/t5/truePose".equals(nm)
              && !"/t5/estimatedPose".equals(nm)
              && !"/t5/pathTargetPose".equals(nm)) {
            return;
          }
          lastByEntry.put(rec.getEntry(), Pose2d.struct.unpack(rec.getRawBuffer()));
          Pose2d trueP = byName(names, lastByEntry, "/t5/truePose");
          Pose2d estP = byName(names, lastByEntry, "/t5/estimatedPose");
          Pose2d targetP = byName(names, lastByEntry, "/t5/pathTargetPose");
          if (trueP != null && estP != null) {
            double d = trueP.getTranslation().getDistance(estP.getTranslation());
            maxTrueVsEst[0] = Math.max(maxTrueVsEst[0], d);
            lastTrueVsEst[0] = d;
          }
          if (targetP != null && estP != null) {
            maxControllerError[0] =
                Math.max(
                    maxControllerError[0],
                    targetP.getTranslation().getDistance(estP.getTranslation()));
          }
        });

    for (var e : names.entrySet()) {
      if (e.getValue().startsWith("/t5/")) {
        System.out.println("  " + e.getValue() + " records=" + counts.getOrDefault(e.getKey(), 0));
      }
    }
    System.out.println("  events=" + events);
    System.out.printf(
        "  maxControllerError(target-vs-est)=%.3f m  maxTrueVsEst=%.3f m  lastTrueVsEst=%.3f m%n",
        maxControllerError[0], maxTrueVsEst[0], lastTrueVsEst[0]);
  }

  private static Pose2d byName(Map<Integer, String> names, Map<Integer, Pose2d> last, String name) {
    for (var e : last.entrySet()) {
      if (name.equals(names.get(e.getKey()))) {
        return e.getValue();
      }
    }
    return null;
  }
}
```

Add a matching temporary task to `build.gradle` (delete it in 5d along with `Inspect.java`):

```groovy
tasks.register('inspect', JavaExec) {
    dependsOn 'extractReleaseNative'
    classpath = sourceSets.main.runtimeClasspath
    mainClass = 'frc.testout.t5.Inspect'
    systemProperty 'java.library.path', "$buildDir/jni/release"
}
```

```bash
./gradlew inspect --console=plain
```

**5c. Read the output against the discrimination table below.** These are the exact numbers this
task's own spike produced against the code above — expect a close match; a large deviation means
something was transcribed differently than Step 1–4 above and is worth stopping on before it
becomes Task 7's problem.

| Fault | events (count) | maxControllerError (target vs. estimate) | maxTrueVsEst | Rules out |
|---|---|---|---|---|
| `ODOMETRY_DRIFT` | 6 (path:start/end, step2:start/end, step3:start/end — routine completes normally) | 0.177 m — small | **2.318 m — large** | Mistuned controller (error small, so the controller is tracking its input fine) and blocking command (all six events present, on a normal ~3.2 s timeline) |
| `MISTUNED_CONTROLLER` | 6 (routine completes normally) | **0.887 m — large, ~5× the other two faults' baseline** | 0.047 m — small | Odometry drift (estimate matches true pose throughout) and blocking command (all six events present, normal timeline) |
| `BLOCKING_COMMAND` | **3 only** — `path:start`, `path:end`, `step2:start`; no `step2:end`, no `step3:start`, no `step3:end` | 0.177 m — small | 0.057 m — small | Odometry drift and mistuned controller (both pose signals agree closely right up to the point recording stops) |

Confirm, reading the actual `inspect` output, not this table from memory:
1. `ODOMETRY_DRIFT`'s `maxTrueVsEst` is an order of magnitude larger than its
   `maxControllerError`, and both other faults' `maxTrueVsEst` stay under 0.06 m.
2. `MISTUNED_CONTROLLER`'s `maxControllerError` is the largest of the three by a wide margin, and
   its `maxTrueVsEst` is the smallest.
3. `BLOCKING_COMMAND` is the only one of the three with fewer than 6 `/t5/events` records, and its
   `truePose`/`estimatedPose`/`pathTargetPose`/`moduleStates` record counts (515 each) are more
   than 3× the other two faults' (159 each) — the recorder ran past the nominal ~3.2 s routine
   length up to the `MAX_STEP_SECONDS` cap because `step2` never finished.

If any of the three fails, the fixture is not ready — go back to Step 3's tunings
(`GYRO_DRIFT_RAD_PER_TICK`, `GOOD_KP`/`BAD_KP`, `GOOD_KP_THETA`/`BAD_KP_THETA`) rather than
loosening this check.

**5d. Determinism — regenerate each fault twice, confirm byte-identical.**

```bash
rm -rf /tmp/t5-determinism-check
for f in ODOMETRY_DRIFT MISTUNED_CONTROLLER BLOCKING_COMMAND; do
  ./gradlew generateLog -Pfault=$f -Pout=/tmp/t5-determinism-check/${f}_1.wpilog --console=plain
  sleep 1
  ./gradlew generateLog -Pfault=$f -Pout=/tmp/t5-determinism-check/${f}_2.wpilog --console=plain
  cmp /tmp/t5-determinism-check/${f}_1.wpilog /tmp/t5-determinism-check/${f}_2.wpilog \
    && echo "$f: byte-identical" || echo "$f: DIFFERS -- stop"
done
```

Expected: all three `byte-identical`. This is what makes Task 7 Step 4's regeneration check (and
§16's "regenerate T5's logs if the sim stack changes") meaningful rather than aspirational.

**5e. Remove the throwaway inspector.**

```bash
rm src/main/java/frc/testout/t5/Inspect.java
```

Also remove the `inspect` task block added to `build.gradle` in 5b. Confirm nothing of it
remains:

```bash
grep -n "Inspect\|inspect" build.gradle src/main/java -r
```

Expected: no output.

Paste the full `inspect` output from 5b/5c and the determinism results from 5d in the task
report — a report that only asserts "logs discriminate" without the numbers is not this step
done.

- [ ] **Step 6: Format, verify nothing leaked, and commit**

```bash
cd /Users/y0shi/workspace/ignite/docs/projects/testouts/t5-drifting-auto
export JAVA_HOME=~/wpilib/2026/jdk
./gradlew spotlessApply --console=plain && ./gradlew spotlessCheck --console=plain
./gradlew assemble --console=plain;        echo "assemble rc=$? (expect 0)"
./gradlew compileTestJava --console=plain; echo "compileTestJava rc=$? (expect 0)"
```

No log content is committed by this task — `logs/` is Task 7's job entirely. Confirm:

```bash
cd /Users/y0shi/workspace/ignite/docs
git status --short
```

Expected: only files under `projects/testouts/t5-drifting-auto/{settings.gradle,gradlew,
gradlew.bat,gradle/,.gitignore,WPILib-License.md,.wpilib/,vendordeps/,build.gradle,src/}` — no
`build/`, no `.gradle/`, no `logs/`, nothing under `/tmp`.

```bash
git add projects/testouts/t5-drifting-auto
git commit -m "T5 drifting-auto recorder: T5DriveSim, LogRecorder, GenerateLog"
```
