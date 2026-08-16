# Task 6: The T4 `bad-kv` test-out fixture

A flywheel velocity controller with kV roughly a third of the truth and a large kP papering
over it: it reaches the target, overshoots, and rings before settling short. Sixty minutes,
Track D (spec §10). `FlywheelSim` extends `LinearSystemSim`, which is pure math — no Phoenix,
no vendordeps, no HAL — so this is a plain GradleRIO project shaped like `t2-supplier-swap`
(design record §6). The risk in a fixture like this is shipping gains that don't actually
produce the symptom; Step 6 below is the check that catches that before it ships.

**Files:**
- Create: `projects/testouts/t4-bad-kv/settings.gradle`, `gradlew`, `gradlew.bat`,
  `gradle/wrapper/gradle-wrapper.jar`, `gradle/wrapper/gradle-wrapper.properties`, `.gitignore`,
  `WPILib-License.md`, `.wpilib/wpilib_preferences.json` (copies from `t2-supplier-swap`)
- Create: `projects/testouts/t4-bad-kv/build.gradle`
- Create: `projects/testouts/t4-bad-kv/src/main/java/frc/testout/t4/plant/T4FlywheelPlant.java`
- Create: `projects/testouts/t4-bad-kv/src/main/java/frc/testout/t4/BadlyTunedFlywheel.java`
- Create: `projects/testouts/t4-bad-kv/src/main/java/frc/testout/t4/ResponseRecorder.java`
- Test: `projects/testouts/t4-bad-kv/src/test/java/frc/testout/t4/FlywheelTuningTest.java`
- Create: `projects/testouts/t4-bad-kv/README.md`

**Consumes:** nothing. This lane is independent of the M4 lane and shares no file with it —
each `projects/*/` is standalone (spec §4.2), and T4 gets its own plant rather than importing
M4's `frc.robot.plant.FlywheelPlant`.

**Produces:** `BadlyTunedFlywheel` (`setTarget`, `update`, `velocityRotationsPerSecond`,
`appliedVolts` — signatures pinned in `00-plan.md`'s API contract) and the `generateResponse`
Gradle task that Task 7 runs to produce `plots/response.csv`. Both are consumed by Task 7 and,
later, by Task 8's reference fix in `../ignite-training-solutions/`.

**Lane:** `testout`. Work in `.worktrees/t4`.

---

- [ ] **Step 1: Copy the scaffold and write `build.gradle`**

```bash
cd /Users/y0shi/workspace/ignite/docs
mkdir -p projects/testouts/t4-bad-kv
cd projects/testouts/t2-supplier-swap
for f in settings.gradle gradlew gradlew.bat gradle/wrapper/gradle-wrapper.jar \
         gradle/wrapper/gradle-wrapper.properties .gitignore WPILib-License.md \
         .wpilib/wpilib_preferences.json; do
  mkdir -p "../t4-bad-kv/$(dirname "$f")"
  cp "$f" "../t4-bad-kv/$f"
done
chmod +x ../t4-bad-kv/gradlew
```

**No `vendordeps/` directory** — `t2-supplier-swap` doesn't have one either, and T4 needs none:
`FlywheelSim`, `LinearSystemId`, and `DCMotor` all come from `wpi.java.deps.wpilib()`, the same
dependency every project already has.

`projects/testouts/t4-bad-kv/build.gradle` — `t2-supplier-swap/build.gradle` verbatim, with the
main class swapped and the `shooterConsole` task replaced by `generateResponse`:

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

// This is a test-out fixture, not a robot project. A plant, a controller with two editable
// gains, and a recorder that writes its response to plots/response.csv. GradleRIO is applied
// for the same reason as in M1, M2, and T2: the toolchain and the JUnit wiring, so a student
// runs the same ./gradlew commands here as everywhere else.
def ROBOT_MAIN_CLASS = "frc.testout.t4.ResponseRecorder"

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

// The fixture's one runnable thing. Writes plots/response.csv (relative to this project
// directory) from the shipped gains, and prints a one-line summary. Task 7 runs this to
// (re)generate the committed plot -- see plots/README.md.
tasks.register('generateResponse', JavaExec) {
    group = 'training'
    description = 'Runs the shipped BadlyTunedFlywheel and writes plots/response.csv.'
    // FlywheelSim's discretization goes through Matrix.exp, which calls
    // System.loadLibrary("wpimathjni") -- that needs java.library.path to point at the
    // extracted native library, not just the classpath. extractReleaseNative is the
    // NativeUtils task that puts it at build/jni/release; wpi.java.configureTestTasks(test)
    // wires this same thing onto :test's java.library.path. Discovered by running this task
    // and reading `no wpimathjni in java.library.path` -- without both lines below,
    // generateResponse fails with an UnsatisfiedLinkError even though assemble is green.
    dependsOn 'extractReleaseNative'
    classpath = sourceSets.main.runtimeClasspath
    mainClass = 'frc.testout.t4.ResponseRecorder'
    systemProperty 'java.library.path', "$buildDir/jni/release"
}
```

- [ ] **Step 2: Write the given, locked plant**

`src/main/java/frc/testout/t4/plant/T4FlywheelPlant.java` — physical parameters only, never
gains, same rule M4's plants follow (design record §4):

```java
package frc.testout.t4.plant;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.system.plant.LinearSystemId;
import edu.wpi.first.wpilibj.simulation.FlywheelSim;

/**
 * A simulated flywheel built from physical quantities, never from gains. Given, locked — part
 * of the fixture. {@code tests.lock} and {@code CODEOWNERS} both cover this file (spec §12.2).
 */
public final class T4FlywheelPlant {
  private static final DCMotor GEARBOX = DCMotor.getNEO(1);
  private static final double GEARING = 1.0;
  private static final double MOMENT_OF_INERTIA_KG_METERS_SQUARED = 0.0006;

  private final FlywheelSim sim =
      new FlywheelSim(
          LinearSystemId.createFlywheelSystem(
              GEARBOX, MOMENT_OF_INERTIA_KG_METERS_SQUARED, GEARING),
          GEARBOX);

  /**
   * Commands a voltage to the flywheel, clamped to what a battery can deliver.
   *
   * <p>Deliberately {@code setInput}, never {@code setInputVoltage}: the latter re-clamps
   * through {@code RobotController.getBatteryVoltage()}, which goes through the HAL. This
   * fixture never initializes the HAL, so an uninitialized battery-voltage read would silently
   * clamp every command to zero.
   */
  public void setVoltage(double volts) {
    sim.setInput(MathUtil.clamp(volts, -12.0, 12.0));
  }

  /** Advances the simulated physics by {@code dtSeconds}. */
  public void step(double dtSeconds) {
    sim.update(dtSeconds);
  }

  /** The flywheel's current angular velocity. */
  public double velocityRotationsPerSecond() {
    return sim.getAngularVelocityRPM() / 60.0;
  }
}
```

- [ ] **Step 3: Write the fixture — the one file whose gains are visible and editable**

`src/main/java/frc/testout/t4/BadlyTunedFlywheel.java`:

```java
package frc.testout.t4;

import edu.wpi.first.math.MathUtil;
import frc.testout.t4.plant.T4FlywheelPlant;

/**
 * A flywheel velocity controller inherited from last season, with a plot in {@code plots/} of
 * what it actually does today. {@link #kV} and {@link #kP} are the entire fixture — edit them,
 * run {@code ./gradlew test}, and see whether the plot in {@code plots/} would change the way
 * you expect. Do not edit anything else in this file or {@link T4FlywheelPlant}.
 */
public final class BadlyTunedFlywheel {
  private static final double STEP_SECONDS = 0.02;

  /** Volts commanded per rotation/second of target velocity. Edit this. */
  private double kV = 0.0416;

  /** Volts commanded per rotation/second of velocity error. Edit this. */
  private double kP = 1.5;

  private final T4FlywheelPlant plant = new T4FlywheelPlant();
  private double targetRotationsPerSecond;
  private double appliedVolts;

  /** Sets the velocity this controller drives toward. */
  public void setTarget(double rotationsPerSecond) {
    targetRotationsPerSecond = rotationsPerSecond;
  }

  /** Runs exactly one 20&nbsp;ms iteration: measure, compute, command, then step the plant. */
  public void update() {
    double measured = plant.velocityRotationsPerSecond();
    double feedforward = kV * targetRotationsPerSecond;
    double feedback = kP * (targetRotationsPerSecond - measured);
    appliedVolts = MathUtil.clamp(feedforward + feedback, -12.0, 12.0);
    plant.setVoltage(appliedVolts);
    plant.step(STEP_SECONDS);
  }

  /** The plant's velocity as of the most recent {@link #update()}. */
  public double velocityRotationsPerSecond() {
    return plant.velocityRotationsPerSecond();
  }

  /** The voltage most recently commanded. */
  public double appliedVolts() {
    return appliedVolts;
  }
}
```

- [ ] **Step 4: Write the response recorder**

`src/main/java/frc/testout/t4/ResponseRecorder.java` — not a test, nothing here is graded. This
is what `./gradlew generateResponse` runs, and what Task 7's plot is built from.

```java
package frc.testout.t4;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Runs the shipped {@link BadlyTunedFlywheel} for a fixed simulated window and writes its
 * response to {@code plots/response.csv}. This is how {@code plots/response.png} gets
 * regenerated — see {@code plots/README.md}.
 */
public final class ResponseRecorder {
  private static final double TARGET_ROTATIONS_PER_SECOND = 40.0;
  private static final int STEPS = 250; // 5 simulated seconds at 20 ms/step
  private static final int SETTLE_WINDOW = 50; // trailing 1 simulated second

  private ResponseRecorder() {}

  public static void main(String[] args) throws IOException {
    Path out = Path.of("plots", "response.csv");
    Files.createDirectories(out.getParent());

    BadlyTunedFlywheel flywheel = new BadlyTunedFlywheel();
    flywheel.setTarget(TARGET_ROTATIONS_PER_SECOND);

    double[] measured = new double[STEPS];
    StringBuilder csv = new StringBuilder();
    csv.append(
        "time_seconds,setpoint_rotations_per_second,measurement_rotations_per_second,"
            + "applied_volts\n");
    for (int i = 0; i < STEPS; i++) {
      flywheel.update();
      double t = (i + 1) * 0.02;
      measured[i] = flywheel.velocityRotationsPerSecond();
      csv.append(
          String.format(
              "%.3f,%.6f,%.6f,%.6f\n",
              t, TARGET_ROTATIONS_PER_SECOND, measured[i], flywheel.appliedVolts()));
    }
    Files.writeString(out, csv.toString());

    double peak = Double.NEGATIVE_INFINITY;
    for (double v : measured) {
      peak = Math.max(peak, v);
    }
    double settledSum = 0.0;
    for (int i = STEPS - SETTLE_WINDOW; i < STEPS; i++) {
      settledSum += measured[i];
    }
    double settled = settledSum / SETTLE_WINDOW;

    System.out.println("Wrote " + out.toAbsolutePath());
    System.out.printf(
        "target=%.3f rot/s  peak=%.3f rot/s (overshoot %.3f)  settled=%.3f rot/s (error %.3f)%n",
        TARGET_ROTATIONS_PER_SECOND,
        peak,
        peak - TARGET_ROTATIONS_PER_SECOND,
        settled,
        settled - TARGET_ROTATIONS_PER_SECOND);
  }
}
```

- [ ] **Step 5: Write the given test**

`src/test/java/frc/testout/t4/FlywheelTuningTest.java` — behaviour through the public API only,
no field names, no thrown exception. Fails against the shipped gains; passes once `kV` is
characterized correctly and `kP` is sized to match, whatever specific numbers a student lands
on. Do not edit after this step.

```java
package frc.testout.t4;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Given. Part of the fixture — do not edit. */
class FlywheelTuningTest {

  private static final double TARGET_ROTATIONS_PER_SECOND = 40.0;
  private static final int STEPS = 250; // 5 simulated seconds
  private static final int SETTLE_WINDOW = 50; // trailing 1 simulated second
  private static final double OVERSHOOT_TOLERANCE_ROTATIONS_PER_SECOND = 0.5;
  private static final double SETTLE_TOLERANCE_ROTATIONS_PER_SECOND = 1.0;

  @Test
  void doesNotOvershootTargetByMoreThanTolerance() {
    double[] history = run();
    double peak = Double.NEGATIVE_INFINITY;
    for (double v : history) {
      peak = Math.max(peak, v);
    }
    assertTrue(
        peak <= TARGET_ROTATIONS_PER_SECOND + OVERSHOOT_TOLERANCE_ROTATIONS_PER_SECOND,
        String.format(
            "Velocity peaked at %.3f rot/s against a %.1f rot/s target — %.3f rot/s of "
                + "overshoot, more than the %.2f rot/s this test allows. A feedforward gain "
                + "that undercommands voltage forces the proportional term to make up the "
                + "difference, and an aggressive proportional term is what produces this kind "
                + "of overshoot.",
            peak,
            TARGET_ROTATIONS_PER_SECOND,
            peak - TARGET_ROTATIONS_PER_SECOND,
            OVERSHOOT_TOLERANCE_ROTATIONS_PER_SECOND));
  }

  @Test
  void settlesWithinToleranceOfTarget() {
    double[] history = run();
    double sum = 0.0;
    for (int i = history.length - SETTLE_WINDOW; i < history.length; i++) {
      sum += history[i];
    }
    double settled = sum / SETTLE_WINDOW;
    assertTrue(
        Math.abs(settled - TARGET_ROTATIONS_PER_SECOND) <= SETTLE_TOLERANCE_ROTATIONS_PER_SECOND,
        String.format(
            "Averaged over the last %d iterations, velocity settled at %.3f rot/s — %.3f rot/s "
                + "away from the %.1f rot/s target, more than the %.2f rot/s this test allows. "
                + "Raising kP further will not close this gap on its own; it did not close it "
                + "to ship this fixture either.",
            SETTLE_WINDOW,
            settled,
            settled - TARGET_ROTATIONS_PER_SECOND,
            TARGET_ROTATIONS_PER_SECOND,
            SETTLE_TOLERANCE_ROTATIONS_PER_SECOND));
  }

  private static double[] run() {
    BadlyTunedFlywheel flywheel = new BadlyTunedFlywheel();
    flywheel.setTarget(TARGET_ROTATIONS_PER_SECOND);
    double[] history = new double[STEPS];
    for (int i = 0; i < STEPS; i++) {
      flywheel.update();
      history[i] = flywheel.velocityRotationsPerSecond();
    }
    return history;
  }
}
```

- [ ] **Step 6: Confirm the shipped gains produce the symptom, and run the distractors — not optional**

A fixture whose plot shows a clean first-order rise teaches nothing and cannot be diagnosed, and
a gate that can be passed by the wrong diagnosis is as broken as one no right answer passes
(non-negotiable, per the dispatch and `CLAUDE.md`'s verification discipline). Prove both: the
given test failing for the right reason on the shipped gains, and failing for a *different*
reason on the one wrong fix a student is most likely to try.

**6a. Shipped gains — clean-clone invariant plus the raw numbers.**

```bash
cd /Users/y0shi/workspace/ignite/docs/projects/testouts/t4-bad-kv
export JAVA_HOME=~/wpilib/2026/jdk
./gradlew clean --console=plain > /dev/null
./gradlew assemble --console=plain;        echo "assemble rc=$? (expect 0)"
./gradlew compileTestJava --console=plain; echo "compileTestJava rc=$? (expect 0)"
./gradlew test --console=plain;            echo "test rc=$? (expect 1)"
```

Both `FlywheelTuningTest` cases must fail, on the messages written above, not on a compile error
or a thrown exception. Paste the output.

```bash
./gradlew generateResponse --console=plain
```

Read the printed line. Design record §6.1's spike (independently reproduced before this task was
written) put the shipped gains at `peak≈41.15` rot/s (`+1.15` over the 40.0 target) and
`settled≈37.95` rot/s (`-2.05`, outside both test tolerances). If your numbers land elsewhere,
retune `kV`/`kP` in `BadlyTunedFlywheel.java` to get back inside that neighborhood — not the
test.

Then look at the shape, not just the two numbers — print a few rows of `plots/response.csv` and
confirm there is real ringing (the measurement crossing back and forth around its settled value
across at least a few consecutive rows), not a single overshoot that immediately flatlines:

```bash
head -20 plots/response.csv
```

**6b. The distractor — fix `kV`, leave `kP` where it was.** This is the fix a student reaches for
first, because it is the smaller edit, and design record §6.2 is explicit that it must still
fail: a `kP` chosen to paper over a bad feedforward is the wrong `kP` once the feedforward is
correct.

```bash
cp src/main/java/frc/testout/t4/BadlyTunedFlywheel.java /tmp/BadlyTunedFlywheel.java.orig
```

Edit `kV` in `BadlyTunedFlywheel.java` to `0.1247` — leave `kP` at `1.5`:

```bash
./gradlew test --console=plain; echo "test rc=$? (expect 1)"
```

Expected: `doesNotOvershootTargetByMoreThanTolerance` fails (peak overshoot grows to
`≈+1.99` rot/s — worse than the shipped gains, not better), and
`settlesWithinToleranceOfTarget` now passes on its own (the steady-state offset disappears once
`kV` is correct). Read the overshoot failure message. Then restore the file:

```bash
cp /tmp/BadlyTunedFlywheel.java.orig src/main/java/frc/testout/t4/BadlyTunedFlywheel.java
git diff --stat src/main/java/frc/testout/t4/BadlyTunedFlywheel.java   # expect no output
```

**6c. The correct-shape fix — both terms retuned.** Confirms the gate is passable by *something*,
per `CLAUDE.md`'s "run a correct implementation" rule.

Edit `kV` to `0.1247` and `kP` to `0.5`:

```bash
./gradlew test --console=plain; echo "test rc=$? (expect 0)"
```

Expected: both tests green. Restore the file the same way as 6b and confirm `test` is red again:

```bash
cp /tmp/BadlyTunedFlywheel.java.orig src/main/java/frc/testout/t4/BadlyTunedFlywheel.java
./gradlew test --console=plain; echo "test rc=$? (expect 1, shipped gains again)"
git diff --stat src/main/java/frc/testout/t4/BadlyTunedFlywheel.java   # expect no output
```

Paste all three `test` outputs (6a, 6b, 6c) with their exit codes. A report that only shows 6a is
not this step done — 6b is the one that proves the gate can't be passed by the wrong diagnosis.

- [ ] **Step 7: Write `README.md`**

`projects/testouts/t4-bad-kv/README.md`:

````markdown
# T4 — Bad kV

**Time: 60 minutes.** Track D.

This is a flywheel velocity controller inherited from last season. It is not
broken in the sense of throwing an exception or refusing to compile — it
runs. Look at `plots/response.png` before you touch anything: it reaches the
setpoint, overshoots, rings for a few cycles, and settles a little short of
the target line rather than on it.

```bash
./gradlew test
```

Two tests, both red.

**Do not edit anything under `src/test/`.** Those tests are part of the
fixture and CI checks that they have not changed. If you think one is wrong,
say so — you might be right, and a mentor will look.

---

## Part 1 — diagnose it

Everything you can change lives in `BadlyTunedFlywheel.java`: two fields,
`kV` and `kP`. Nothing else in the project needs to change, and nothing else
should.

You cannot look up the flywheel's true parameters — `T4FlywheelPlant.java` is
given and locked, same as M4's plants. What you *can* do is exactly what
characterization means: command a few known voltages, read the velocity that
results, and work out what `kV` should have been. Re-run
`./gradlew generateResponse` as often as you like; it reprints the response
every time.

Fix `kV`. Then fix `kP` to match — not to compensate for `kV` being wrong,
which is how this fixture got built in the first place.

```bash
./gradlew test
```

Both green.

---

## Part 2 — the three questions

Answer these out loud, to a mentor. Nothing here is covered by a test — this
is the whole reason the test alone isn't the pass criterion.

1. **What was wrong with the original `kV`, and how do you know?** "It looked
   too low" is not characterization. Say what you measured and what it told
   you.

2. **You will be tempted to fix this by raising `kP` instead of touching
   `kV`. Try it — push `kP` up with `kV` left alone, and run
   `./gradlew generateResponse` again.** What happens to `settled`? Explain
   why more proportional gain does not close a gap that feedforward left
   open.

3. **Why is the original tuning fragile?** It reached this target, at this
   simulated battery voltage, and shipped anyway. What specifically would
   happen to this same controller at a different target speed, or with the
   battery sagging under load? Be specific about which term in `update()`
   is the one that stops being trustworthy, and why.

That is the whole test-out. Part 1 checks you can characterize a system and
size a feedforward gain to it. Part 2 checks you understand *why* the
original numbers were fragile, not just that they were wrong — and the
difference between a passing answer and a failing one is entirely in these
three explanations. "The gains were bad" is a failing answer to all three.

---

## How you pass

1. Part 1's tests are green.
2. Your three answers in Part 2 are correct.

If 60 minutes runs out, that is not a failure of anything. It means M4 is
where you start, which is exactly what this fixture exists to find out.
````

- [ ] **Step 8: Check nothing gives the answer away, then format and commit**

```bash
cd /Users/y0shi/workspace/ignite/docs/projects/testouts/t4-bad-kv
grep -rniE '0\.124|0\.1247|kv_true|kvtrue|true.?kv|50\.39|kv.{0,15}=.{0,15}(motor|free.?speed)' \
    README.md src/main/java src/test/java
```

Expected: nothing. The true `kV` this fixture was built against must never appear, in a comment,
a variable name, or the README — only the shipped (wrong) `0.0416` and `1.5`, which are the
fixture's given starting point, not the answer.

```bash
export JAVA_HOME=~/wpilib/2026/jdk
./gradlew spotlessApply --console=plain && ./gradlew spotlessCheck --console=plain
./gradlew test --console=plain; echo "test rc=$? (expect 1)"

cd /Users/y0shi/workspace/ignite/docs
git status --short   # no build/, no .gradle/, no plots/ yet (Task 7's job)
git add projects/testouts/t4-bad-kv
git commit -m "T4 bad-kv test-out fixture"
```
