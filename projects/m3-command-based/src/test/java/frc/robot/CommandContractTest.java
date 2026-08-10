package frc.robot;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import frc.robot.harness.DeliverablesFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Given tests for the commands M3 asks you to write. Part of the milestone — do not edit. */
class CommandContractTest {

  @BeforeEach
  void setUp() {
    DeliverablesFixture.setUp();
  }

  @AfterEach
  void tearDown() {
    DeliverablesFixture.tearDown();
  }

  @Test
  void theRobotContainerExposesADrivetrain() {
    assertNotNull(DeliverablesFixture.drivetrain());
  }
}
