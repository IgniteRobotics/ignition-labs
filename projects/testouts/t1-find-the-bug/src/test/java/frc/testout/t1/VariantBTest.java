package frc.testout.t1;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Given test for variant B. Part of the fixture — do not edit. */
class VariantBTest {

  @Test
  void anElevatorWellAboveItsLimitMayKeepGoingDown() {
    assertTrue(VariantB.mayDescend(0.80, 0.10));
  }

  @Test
  void anElevatorBelowItsLimitMayNot() {
    assertFalse(VariantB.mayDescend(0.05, 0.10));
  }
}
