package frc.robot;

import static org.junit.jupiter.api.Assertions.assertTrue;

import frc.robot.Constants.FlywheelConstants;
import frc.robot.harness.SimDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Given. Part of the milestone -- do not edit. */
class FlywheelVelocityTest {

  private static final int ITERATIONS = 250; // 5s at Constants.SIM_STEP_SECONDS
  private static final int HOLD_WINDOW = 50; // the last 1s must stay settled
  private static final double VELOCITY_TOLERANCE_RPS = 2.0;
  private static final double RUNAWAY_LIMIT_RPS =
      FlywheelConstants.TARGET_ROTATIONS_PER_SECOND * 2.5;

  @BeforeEach
  void setUp() {
    SimDriver.setUp();
  }

  @AfterEach
  void tearDown() {
    SimDriver.tearDown();
  }

  @Test
  void reachesAndHoldsCommandedVelocity() {
    SimDriver.container().setFlywheelTarget(FlywheelConstants.TARGET_ROTATIONS_PER_SECOND);

    double[] measured = SimDriver.runFlywheelLoop(ITERATIONS);

    for (int i = 0; i < measured.length; i++) {
      assertTrue(
          Math.abs(measured[i]) <= RUNAWAY_LIMIT_RPS,
          "Flywheel velocity hit "
              + measured[i]
              + " rot/s at iteration "
              + i
              + ", more than 2.5x the "
              + FlywheelConstants.TARGET_ROTATIONS_PER_SECOND
              + " rot/s target. That is a runaway, not a fast response -- a loop with no "
              + "feedback term, or a feedback term with the wrong sign, looks like this.");
    }

    for (int i = ITERATIONS - HOLD_WINDOW; i < ITERATIONS; i++) {
      assertTrue(
          Math.abs(measured[i] - FlywheelConstants.TARGET_ROTATIONS_PER_SECOND)
              <= VELOCITY_TOLERANCE_RPS,
          "At iteration "
              + i
              + " (t="
              + (i * Constants.SIM_STEP_SECONDS)
              + "s) the flywheel measured "
              + measured[i]
              + " rot/s, more than "
              + VELOCITY_TOLERANCE_RPS
              + " rot/s away from the "
              + FlywheelConstants.TARGET_ROTATIONS_PER_SECOND
              + " rot/s target. Checked over the last second of a five-second run, so a response "
              + "that is merely slow to arrive is not enough -- it has to still be there.");
    }
  }
}
