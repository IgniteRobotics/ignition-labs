package frc.testout.t1;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Given test for variant C. Part of the fixture — do not edit. */
class VariantCTest {

  private static final double DELTA = 1e-9;

  @Test
  void aFullMatchIsAllOfIt() {
    assertEquals(1.0, VariantC.matchFraction(150, 150), DELTA);
  }

  @Test
  void partWayThroughIsAFraction() {
    assertEquals(0.3, VariantC.matchFraction(45, 150), DELTA);
  }
}
