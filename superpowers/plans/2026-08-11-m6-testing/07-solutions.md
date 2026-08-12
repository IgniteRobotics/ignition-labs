# Task 7: The solutions repository

Spec §11.2: **a milestone shipped without its solution is not shipped.** From build-order step 7 the solution ships with the milestone rather than being backfilled.

M6 makes the cross-check mean something different from every earlier milestone, and this is the part of the task that is easy to get wrong. For M1–M5 the cross-check overlays a reference implementation and requires the given tests to go green — a real signal, because the skeleton was red. **M6's skeleton is already green.** Overlaying reference *tests* onto it and finding them green proves nothing at all. The cross-check has to be extended, or M6 gets a permanently meaningless PASS row.

All work here is in `../ignite-training-solutions` — a separate repository, mentor-only (spec D6). **Never commit a reference implementation or a reference test to the labs repository.** `git status --short` before every commit.

**Files:**
- Create: `../ignite-training-solutions/solutions/m6-testing/src/test/java/frc/robot/LaunchProfileTest.java`
- Create: `../ignite-training-solutions/solutions/m6-testing/src/test/java/frc/robot/ShooterFlywheelTest.java`
- Create: `../ignite-training-solutions/answer-keys/m6-log-diagnosis.md`
- Create: `../ignite-training-solutions/authoring-notes/m6.md`
- Modify: `../ignite-training-solutions/tools/cross-check.sh`

**Interfaces:**
- Consumes: Task 2's `LaunchProfile`, `ShooterFlywheel`, and `PhoenixTestSupport` APIs; Task 3's three patches and their tier mapping; Task 4's filled-in evidence table; Task 5's `regression_gate.py` CLI.
- Produces: a green cross-check that actually tests something for M6.

**Lane:** tail. Runs on the merged branch after the milestone and ci-tooling lanes have **committed** — `cross-check.sh` archives the labs repository at `HEAD`, so work sitting uncommitted in a worktree is invisible to it.

---

- [ ] **Step 1: Write the reference `LaunchProfile` tests**

These are what a student who did M6 well would have written: three-plus tests, covering the **student territory** methods, and — critically — catching both student-tier patches.

Create `../ignite-training-solutions/solutions/m6-testing/src/test/java/frc/robot/LaunchProfileTest.java`:

```java
package frc.robot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import frc.robot.plant.LaunchProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Reference student tests for M6. Not shipped to the labs repository. */
class LaunchProfileTest {

  private LaunchProfile profile;

  @BeforeEach
  void setUp() {
    profile = new LaunchProfile(2048.0, 6.75);
  }

  @Test
  void timeOfFlight_interpolatesBetweenMeasuredPoints() {
    // Halfway between the 1.88m and 3.15m rows, so the answer must sit between their
    // times of flight and equal neither.
    double midpoint = profile.timeOfFlightSeconds((1.88 + 3.15) / 2.0);

    assertTrue(midpoint > 1.09 && midpoint < 1.11, "expected strictly between, got " + midpoint);
  }

  @Test
  void timeOfFlight_clampsOutsideTheMeasuredRange() {
    assertEquals(0.90, profile.timeOfFlightSeconds(0.1), 1e-9);
    assertEquals(1.16, profile.timeOfFlightSeconds(50.0), 1e-9);
  }

  /** Catches regressions/flip-sign.patch. */
  @Test
  void lookAhead_shortensTheDistanceWhenClosingOnTheTarget() {
    double now = 4.0;
    double approaching = profile.lookAheadDistanceMeters(now, 3.0);
    double retreating = profile.lookAheadDistanceMeters(now, -3.0);

    assertTrue(approaching < now, "closing on the target must shorten the shot, got " + approaching);
    assertTrue(retreating > now, "retreating must lengthen the shot, got " + retreating);
  }

  @Test
  void lookAhead_isTheCurrentDistanceWhenStationary() {
    assertEquals(4.0, profile.lookAheadDistanceMeters(4.0, 0.0), 1e-9);
  }

  /**
   * Catches regressions/widen-tolerance.patch. The negative case is the whole point: a test
   * that only asserts atSetpoint(100, 100) passes at any tolerance, including a nonsense one.
   */
  @Test
  void atSetpoint_rejectsAFlywheelThatIsNowhereNearSpeed() {
    assertTrue(profile.atSetpoint(100.0, 100.0));
    assertFalse(profile.atSetpoint(60.0, 100.0), "40 rps low must not count as at setpoint");
  }

  @Test
  void atSetpoint_includesTheBoundary() {
    assertTrue(profile.atSetpoint(100.5, 100.0));
    assertFalse(profile.atSetpoint(100.51, 100.0));
  }

  @Test
  void constructor_rejectsNonPositiveArguments() {
    assertThrows(IllegalArgumentException.class, () -> new LaunchProfile(0.0, 6.75));
    assertThrows(IllegalArgumentException.class, () -> new LaunchProfile(2048.0, -1.0));
  }
}
```

- [ ] **Step 2: Write the reference Phoenix test**

Two tests in one file, proving the `AutoCloseable` DoD item — the second only passes because the first closed its device.

Create `../ignite-training-solutions/solutions/m6-testing/src/test/java/frc/robot/ShooterFlywheelTest.java`:

```java
package frc.robot;

import static org.junit.jupiter.api.Assertions.assertTrue;

import frc.robot.plant.ShooterFlywheel;
import frc.robot.support.PhoenixTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Reference student tests for M6 Part 2. Not shipped to the labs repository. */
class ShooterFlywheelTest {

  @BeforeEach
  void setUp() {
    PhoenixTestSupport.enableRobot();
  }

  @Test
  void acceptsAControlRequest() {
    try (ShooterFlywheel flywheel = new ShooterFlywheel(2)) {
      PhoenixTestSupport.settleAfterConstruction(flywheel.motor());
      flywheel.motor().getSimState().setSupplyVoltage(12.0);
      flywheel.setVelocity(30.0);
      PhoenixTestSupport.settleAfterControlRequest(flywheel.motor());

      assertTrue(flywheel.motor().getVelocity().getStatus().isOK());
    }
  }

  /**
   * The second test on the same CAN ID. This passes only because the first one closed its
   * device -- which is the entire reason ShooterFlywheel implements AutoCloseable, and the
   * DoD item "two tests in one file both pass."
   */
  @Test
  void reportsAVelocityAfterASecondConstructionOnTheSameId() {
    try (ShooterFlywheel flywheel = new ShooterFlywheel(2)) {
      PhoenixTestSupport.settleAfterConstruction(flywheel.motor());

      assertTrue(Double.isFinite(flywheel.velocityRotationsPerSecond()));
    }
  }
}
```

If Task 2 Step 6 took its fallback, add `@Tag("phoenix-sim")` to both tests here so the cross-check behaves the same way CI does.

- [ ] **Step 3: Extend `cross-check.sh` so M6's PASS means something**

Without this, M6 gets a PASS for overlaying tests onto an already-green project.

In `../ignite-training-solutions/tools/cross-check.sh`, after the existing `spotlessCheck` block and before `echo "PASS  $name"`, insert:

```bash
  # M6 inverts the model: its skeleton is already green, so "the reference solution passes
  # the given tests" is trivially true and proves nothing. What must be true for M6 is that
  # the reference *tests* catch the student-tier regressions -- the same thing the student's
  # own fork is gated on. Any project shipping regressions/ gets this treatment; keying on
  # the directory rather than the project name keeps it self-maintaining.
  if [ -d "$project_dir/regressions" ]; then
    tier_failed=0
    for patch in "$project_dir"/regressions/*.patch; do
      case "$(basename "$patch")" in
        drop-gear-ratio.patch) continue ;;   # example tier: the given test owns it
      esac
      # `-m` from $WORK, not `python3 $WORK/tools/regression_gate.py`: the script imports
      # TOOLCHAIN_FAILURES from tools.given_tests_red, which needs the repository root on
      # sys.path. Invoking it by path fails on import before it checks anything.
      if ! (cd "$WORK" && python3 -m tools.regression_gate "$project_dir" "$patch" \
              --expect any-failure > "$WORK/$$.log" 2>&1); then
        echo "FAIL  $name  (student-tier regression $(basename "$patch") went uncaught)"
        sed -n '/::error::/,$p' "$WORK/$$.log" | sed 's/^/        /'
        tier_failed=1
        break
      fi
    done
    if [ "$tier_failed" -ne 0 ]; then
      failed=$((failed + 1))
      continue
    fi
  fi
```

`git archive` extracts the labs repository into `$WORK`, so `tools/regression_gate.py` and the project's `regressions/` are both already there — nothing extra needs copying, and the gate runs from `$WORK` because it applies patches with repository-relative paths.

- [ ] **Step 4: Run the cross-check and read what it says**

```bash
cd /Users/y0shi/workspace/ignite/ignite-training-solutions
./tools/cross-check.sh
```

Expected: every project `PASS`, including `m6-testing`. Report the full output.

- [ ] **Step 5: Prove the extension can fail — the step that makes Step 4 mean something**

A gate nobody has seen fail is a gate nobody knows works.

Temporarily delete the `atSetpoint_rejectsAFlywheelThatIsNowhereNearSpeed` test from `LaunchProfileTest.java` and re-run:

```bash
./tools/cross-check.sh; echo "rc=$?"
```

**Expected: `FAIL m6-testing (student-tier regression widen-tolerance.patch went uncaught)` and `rc=1`.**

If it still passes, the extension is not wired in and Step 4's PASS was meaningless. Restore the test and re-run to confirm green again. Paste both outputs.

- [ ] **Step 6: Write the answer key**

Use Task 4's filled-in evidence table verbatim. **Do not reconstruct it from memory** — a key written later from recollection is a key that is wrong, and it is the document a mentor trusts when a student disagrees with them.

Create `../ignite-training-solutions/answer-keys/m6-log-diagnosis.md` covering, for each question: the answer, the exact signal names and timestamp range that prove it, the most plausible wrong answer, and which signal rules that out. Spec §9.7's pass criterion is the answer **with evidence** and the ruling-out — so the key must let a mentor grade the ruling-out, not just the conclusion. A student who names the right answer without excluding the alternative has not passed, and the key has to say so plainly enough that a mentor does not have to make the call alone.

- [ ] **Step 7: Write the authoring notes**

Create `../ignite-training-solutions/authoring-notes/m6.md`, covering:

- **The territory partition and why it exists.** Worked example owns `ticksToMechanismRotations`; student owns the other three. Any change to the worked example risks catching a student-tier patch and silently disabling the fork gate. State the check: run the worked example against `flip-sign.patch` and `widen-tolerance.patch`; it must **pass** both.
- **Which trap each patch aims at.** `flip-sign` catches a student who tested `lookAheadDistanceMeters` only at zero speed. `widen-tolerance` catches the milestone's named failure mode — a test that can never fail, because it only ever asserted the true case.
- **Why M6 is green on a clean clone**, and why that is not a bug to be fixed by adding a throwing stub.
- **That `PhoenixTestSupport` never ships to `Robot-2026`** (§6.5), and what to say to the student who asks why not.
- **The `Robot-2026` logs defect**: `logs/` is gitignored, zero logs are tracked, M6 ships its own. If someone later un-ignores them, M6's shipped log can stay — a known log with a known answer key is worth more to this exercise than a fresh one.

- [ ] **Step 8: Confirm nothing leaked into the labs repository, then commit both**

```bash
cd /Users/y0shi/workspace/ignite/docs
git status --short          # MUST be clean of any reference test
find projects/m6-testing/src/test -name '*Test.java'   # expect only WorkedExampleTest.java

cd /Users/y0shi/workspace/ignite/ignite-training-solutions
git status --short
git add solutions/m6-testing answer-keys/m6-log-diagnosis.md authoring-notes/m6.md tools/cross-check.sh
git commit -m "Add M6 reference tests, log answer key, and authoring notes

M6's skeleton is green by design, so the existing cross-check -- overlay a
solution, require the given tests to pass -- is trivially satisfied and proves
nothing. Extended it: any project shipping regressions/ must have its reference
tests catch every student-tier patch, which is the same gate the student's own
fork faces.

Verified the extension can fail: deleting the negative atSetpoint case makes
widen-tolerance.patch go uncaught and the cross-check go red."
```
