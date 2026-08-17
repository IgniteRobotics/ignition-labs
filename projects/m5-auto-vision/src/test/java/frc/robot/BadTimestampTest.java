package frc.robot;

import static org.junit.jupiter.api.Assertions.assertFalse;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import frc.robot.harness.SimDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Given. Part of the milestone -- do not edit. */
class BadTimestampTest {

  @BeforeEach
  void setUp() {
    SimDriver.setUp();
  }

  @AfterEach
  void tearDown() {
    SimDriver.tearDown();
  }

  @Test
  void rejectsAMeasurementWithAnImpossibleTimestamp() {
    Pose2d startingPose = new Pose2d(2.0, 4.0, Rotation2d.kZero);
    SimDriver.resetEstimator(startingPose);

    // Exactly where the estimator already believes it is -- a filter that only checks
    // distance from the current estimate would accept this. Only the timestamp is wrong:
    // -1.0 is never a valid FPGA timestamp.
    boolean accepted = SimDriver.acceptsMeasurement(startingPose, -1.0);

    assertFalse(
        accepted,
        "acceptsMeasurement() accepted a measurement at a pose identical to the current "
            + "estimate, timestamped -1.0 seconds -- a timestamp that can never be valid. A "
            + "filter that only checks how far a vision pose is from your current estimate will "
            + "accept this one, because the position looks perfect. Check the timestamp too.");
  }
}
