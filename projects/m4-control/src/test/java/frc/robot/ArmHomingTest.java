package frc.robot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import frc.robot.harness.SimDriver;
import frc.robot.harness.SimDriver.HomingResult;
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
    HomingResult result = SimDriver.runHomingLoop(ITERATIONS);

    assertTrue(
        result.finishedWithinBudget,
        "homingCommand() never finished within five simulated seconds. It has to end on its "
            + "own once the arm is home -- check isFinished() actually returns true once "
            + "armPlant().atHomeLimit() does.");

    assertTrue(
        result.atHomeLimitWhenFinished,
        "homingCommand() reported finished, but the arm's reverse limit switch was not tripped "
            + "at that exact instant. Checking at the moment of finish, not after however many "
            + "seconds the test happens to run for, is deliberate: a gravity-loaded arm can "
            + "drift into its own limit switch given enough uncontrolled time, so a command that "
            + "finishes for the wrong reason could otherwise still look right by the end of a "
            + "long window. Your homingCommand() must not finish until armPlant().atHomeLimit() "
            + "is actually true.");

    assertEquals(
        ArmPlant.HOME_ANGLE_RADIANS,
        result.measuredPositionRadiansWhenFinished,
        POSITION_TOLERANCE_RADIANS,
        "homingCommand() finished at the reverse limit, but measuredPositionRadians() does not "
            + "read "
            + ArmPlant.HOME_ANGLE_RADIANS
            + " at that moment. Call zeroHere() once atHomeLimit() is true, not before.");
  }
}
