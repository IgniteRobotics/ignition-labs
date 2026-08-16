package frc.robot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import frc.robot.harness.SimDriver;
import frc.robot.plant.ArmPlant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Given. Part of the milestone -- do not edit. */
class ArmHomingTest {

  private static final int ITERATIONS = 250; // 5s at Constants.SIM_STEP_SECONDS
  private static final double POSITION_TOLERANCE_RADIANS = 0.01;

  @BeforeEach
  void setUp() {
    SimDriver.setUp();
  }

  @AfterEach
  void tearDown() {
    SimDriver.tearDown();
  }

  @Test
  void homesToTheLimitSwitchFromAnUnknownStart() {
    SimDriver.runHomingLoop(ITERATIONS);

    assertTrue(
        SimDriver.container().armPlant().atHomeLimit(),
        "After running homingCommand() for five simulated seconds, the arm's reverse limit "
            + "switch never tripped. A homing routine that zeroes the encoder without ever "
            + "driving to the limit will pass a check on measuredPositionRadians() alone -- "
            + "zeroing rebases whatever position you are currently at. This assertion reads the "
            + "physical limit switch instead, which only trips if the arm actually got there.");

    assertEquals(
        ArmPlant.HOME_ANGLE_RADIANS,
        SimDriver.container().armPlant().measuredPositionRadians(),
        POSITION_TOLERANCE_RADIANS,
        "The arm reached its reverse limit but measuredPositionRadians() does not read "
            + ArmPlant.HOME_ANGLE_RADIANS
            + " afterward. Call zeroHere() once atHomeLimit() is true.");
  }
}
