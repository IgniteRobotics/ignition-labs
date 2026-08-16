package frc.robot;

import static org.junit.jupiter.api.Assertions.assertTrue;

import frc.robot.Constants.ArmConstants;
import frc.robot.harness.SimDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Given. Part of the milestone -- do not edit. */
class ArmGravityTest {

  private static final int ITERATIONS = 250; // 5s at Constants.SIM_STEP_SECONDS
  private static final int HOLD_WINDOW = 50; // the last 1s must stay settled
  private static final double POSITION_TOLERANCE_RADIANS = 0.05; // ~3 degrees

  @BeforeEach
  void setUp() {
    SimDriver.setUp();
  }

  @AfterEach
  void tearDown() {
    SimDriver.tearDown();
  }

  @Test
  void holdsPositionAgainstGravity() {
    SimDriver.container().setArmTarget(ArmConstants.SCORING_ANGLE_RADIANS);

    double[] measured = SimDriver.runArmLoop(ITERATIONS);

    for (int i = ITERATIONS - HOLD_WINDOW; i < ITERATIONS; i++) {
      assertTrue(
          Math.abs(measured[i] - ArmConstants.SCORING_ANGLE_RADIANS) <= POSITION_TOLERANCE_RADIANS,
          "At iteration "
              + i
              + " (t="
              + (i * Constants.SIM_STEP_SECONDS)
              + "s) the arm measured "
              + measured[i]
              + " rad, more than "
              + POSITION_TOLERANCE_RADIANS
              + " rad away from the "
              + ArmConstants.SCORING_ANGLE_RADIANS
              + " rad target. An arm feedforward with no gravity term sags below its target and "
              + "settles there -- that is what this window is checking for, over the last second "
              + "of a five-second run.");
    }
  }
}
