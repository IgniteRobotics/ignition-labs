package frc.testout.t1;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Given test for variant A. Part of the fixture — do not edit. */
class VariantATest {

  private static final double DELTA = 1e-9;

  @Test
  void averagesOnlyTheRequestedSamples() {
    double[] samples = {1.0, 2.0, 3.0, 4.0};
    assertEquals(1.5, VariantA.meanOfFirst(samples, 2), DELTA);
  }

  @Test
  void averagesTheWholeBufferWhenAskedForAllOfIt() {
    double[] samples = {1.0, 2.0, 3.0, 4.0};
    assertEquals(2.5, VariantA.meanOfFirst(samples, 4), DELTA);
  }
}
