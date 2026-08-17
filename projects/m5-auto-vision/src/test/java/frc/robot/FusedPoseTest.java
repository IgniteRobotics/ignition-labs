package frc.robot;

import static org.junit.jupiter.api.Assertions.assertTrue;

import frc.robot.harness.SimDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Given. Part of the milestone -- do not edit.
 *
 * <p><b>{@code ITERATIONS} is 1500 (30s), not 500 (10s) -- deliberately.</b> At 10s, odometry-only
 * error (~0.14m, from the drivetrain's fixed-rate gyro drift) and fused error (~0.07-0.12m) are
 * close enough in magnitude (a 1.2-2x margin) that a single noisy vision frame can flip the
 * comparison; measured failure rate at 10s was ~7-15% against a correct reference implementation,
 * regardless of the distance threshold or pose-estimator trust weighting chosen -- tightening
 * either did not help, and over-tightening trust made it worse. Position error from a fixed-rate
 * heading drift grows over time (heading error integrates into cross-track error), while fused
 * error stays flat at the vision noise floor, so the margin is a function of run length: at 30s the
 * two are no longer comparable, and the failure probability collapses. Confirmed empirically: 30+
 * consecutive runs against a correct implementation, zero failures (see Task 4's report).
 *
 * <p><b>The assertion requires a margin, not merely "lower" -- and that is the whole point of this
 * test.</b> A bare {@code fusedMean < odometryOnlyMean} passes when nothing is fused at all: both
 * runs are then the same odometry-only run, and which mean comes out smaller is decided by
 * floating-point noise in the last significant digit. Measured against a filter that rejects every
 * measurement, the two means agreed to 13 significant figures and the bare comparison passed on a
 * difference of 2.5e-14 -- a test that would have gone green for a student who computed vision
 * poses correctly and then never trusted one. Requiring fused error to be at most {@link
 * #REQUIRED_ERROR_RATIO} of odometry-only error closes that: a correct implementation measured
 * 0.165-0.211 over eight runs, and never-fusing measures 1.000 exactly.
 */
class FusedPoseTest {

  private static final int ITERATIONS = 1500; // 30s at Constants.SIM_STEP_SECONDS

  /**
   * Fused mean error must be at most this fraction of odometry-only mean error. Halving is far
   * short of what fusion actually achieves here (~0.2), so this leaves roughly 2.4x of headroom
   * against the noisiest correct run measured, while a run that fuses nothing sits at 1.0 and
   * cannot slip through.
   */
  private static final double REQUIRED_ERROR_RATIO = 0.5;

  @BeforeEach
  void setUp() {
    SimDriver.setUp();
  }

  @AfterEach
  void tearDown() {
    SimDriver.tearDown();
  }

  @Test
  void fusingVisionReducesPoseErrorComparedToOdometryAlone() {
    double[] odometryOnlyError = SimDriver.runOdometryLoop(ITERATIONS, false);
    double[] fusedError = SimDriver.runOdometryLoop(ITERATIONS, true);

    double odometryOnlyMean = mean(odometryOnlyError);
    double fusedMean = mean(fusedError);

    assertTrue(
        fusedMean < odometryOnlyMean * REQUIRED_ERROR_RATIO,
        "Over a "
            + (ITERATIONS * Constants.SIM_STEP_SECONDS)
            + "s run, mean pose error with vision fused ("
            + fusedMean
            + "m) was not at most "
            + REQUIRED_ERROR_RATIO
            + " of mean pose error from odometry alone ("
            + odometryOnlyMean
            + "m) -- it came out at "
            + (fusedMean / odometryOnlyMean)
            + " of it. The drivetrain's gyro carries a real drift your odometry-only estimate has "
            + "nothing to correct it with -- vision is supposed to be the correction. A ratio near "
            + "1.0 means no measurement is reaching your pose estimator at all: check that "
            + "acceptsMeasurement() is actually accepting plausible measurements, and that "
            + "addVisionMeasurement() actually reaches your pose estimator. A ratio only somewhat "
            + "below 1.0 means some are getting through but most are being rejected -- check that "
            + "your distance threshold is not so tight it rejects normal corrections.");
  }

  private static double mean(double[] values) {
    double sum = 0.0;
    for (double value : values) {
      sum += value;
    }
    return sum / values.length;
  }
}
