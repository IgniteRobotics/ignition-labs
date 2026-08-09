package frc.training.m1;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Given tests for {@link Deadband}. Part of the milestone — do not edit. */
class DeadbandTest {

  private static final double DELTA = 1e-9;

  @Test
  void smallPositiveInputIsZeroed() {
    assertEquals(0.0, Deadband.apply(0.02, 0.1), DELTA);
  }

  @Test
  void smallNegativeInputIsZeroed() {
    assertEquals(0.0, Deadband.apply(-0.02, 0.1), DELTA);
  }

  @Test
  void largePositiveInputPassesThroughUnchanged() {
    assertEquals(0.5, Deadband.apply(0.5, 0.1), DELTA);
  }

  @Test
  void largeNegativeInputPassesThroughUnchanged() {
    assertEquals(-0.5, Deadband.apply(-0.5, 0.1), DELTA);
  }

  @Test
  void inputExactlyAtTheThresholdPassesThrough() {
    assertEquals(0.1, Deadband.apply(0.1, 0.1), DELTA);
  }

  @Test
  void aZeroThresholdChangesNothing() {
    assertEquals(0.5, Deadband.apply(0.5, 0.0), DELTA);
  }
}
