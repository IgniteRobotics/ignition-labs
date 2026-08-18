package frc.robot;

import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.harness.SimDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Given. Part of the milestone -- do not edit. */
class AutoTerminationTest {

  private static final int BUDGET_ITERATIONS = 1000; // 20s at Constants.SIM_STEP_SECONDS

  @BeforeEach
  void setUp() {
    SimDriver.setUp();
  }

  @AfterEach
  void tearDown() {
    SimDriver.tearDown();
  }

  @Test
  void autoRoutineTerminatesWithinABoundedBudget() {
    Command routine = SimDriver.autoRoutine();

    boolean finished = SimDriver.runCommandToCompletion(routine, BUDGET_ITERATIONS);

    assertTrue(
        finished,
        "autoRoutine() never finished within "
            + (BUDGET_ITERATIONS * Constants.SIM_STEP_SECONDS)
            + " simulated seconds. Every step in your composition needs a bound: a path finishes "
            + "on its own once it reaches the end, but any other step -- a named command "
            + "standing in for an intake or a shot -- can hang forever unless you wrap it with "
            + ".withTimeout(...). A step that can hang forever hangs your whole autonomous "
            + "period on a real robot, not just this test.");
  }

  @Test
  void autoRoutineTerminatesEvenWhenTheScoringMechanismIsFaulted() {
    // Faulted before the routine is even built, so isDeployed() can never become true for the
    // rest of this run -- PathPlanner's own path-follow always terminates on its own, so this
    // is the one thing in the milestone that can actually be made to hang. SimDriver's
    // RobotContainer is a single shared instance for the whole test run (not rebuilt per test),
    // so the fault is always cleared in the finally block -- otherwise it would leak into
    // whichever test runs next in this JVM.
    SimDriver.mechanism().setFaulted(true);
    try {
      Command routine = SimDriver.autoRoutine();

      boolean finished = SimDriver.runCommandToCompletion(routine, BUDGET_ITERATIONS);

      assertTrue(
          finished,
          "With the scoring mechanism faulted -- isDeployed() can never become true this run -- "
              + "autoRoutine() never finished within "
              + (BUDGET_ITERATIONS * Constants.SIM_STEP_SECONDS)
              + " simulated seconds. PathPlanner's own path-following command always "
              + "terminates on its own, so this can only happen if a named command is waiting "
              + "on mechanism().isDeployed() with no .withTimeout(...) on it. Every step that "
              + "depends on the mechanism needs its own bound, the same as every other step.");
    } finally {
      SimDriver.mechanism().setFaulted(false);
    }
  }
}
