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
