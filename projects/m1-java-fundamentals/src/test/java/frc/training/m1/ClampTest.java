package frc.training.m1;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** Given tests for {@link Clamp}. Part of the milestone — do not edit. */
class ClampTest {

  private static final double DELTA = 1e-9;

  @Test
  void aValueInsideTheRangeIsUntouched() {
    assertEquals(0.5, Clamp.clamp(0.5, 0.0, 1.0), DELTA);
  }

  @Test
  void aValueBelowTheRangeComesBackAsTheMinimum() {
    assertEquals(0.0, Clamp.clamp(-0.5, 0.0, 1.0), DELTA);
  }

  @Test
  void aValueAboveTheRangeComesBackAsTheMaximum() {
    assertEquals(1.0, Clamp.clamp(1.5, 0.0, 1.0), DELTA);
  }

  @Test
  void theLowerBoundIsInsideTheRange() {
    assertEquals(0.0, Clamp.clamp(0.0, 0.0, 1.0), DELTA);
  }

  @Test
  void theUpperBoundIsInsideTheRange() {
    assertEquals(1.0, Clamp.clamp(1.0, 0.0, 1.0), DELTA);
  }

  @Test
  void rangesCanBeNegative() {
    assertEquals(-2.0, Clamp.clamp(-3.0, -2.0, 2.0), DELTA);
  }

  @Test
  void aBackwardsRangeIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> Clamp.clamp(0.5, 1.0, 0.0));
  }
}
