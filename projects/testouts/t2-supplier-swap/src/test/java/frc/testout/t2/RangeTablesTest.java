package frc.testout.t2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** Given tests for the two range tables that already exist. Part of the fixture — do not edit. */
class RangeTablesTest {

  private static final double DELTA = 1e-9;

  @Test
  void theParabolicTableIsRegistered() {
    assertEquals(3312.0, RangeTables.byName("parabolic").rpmFor(4.0), DELTA);
  }

  @Test
  void theMappedTableIsRegistered() {
    assertEquals(3600.0, RangeTables.byName("mapped").rpmFor(4.0), DELTA);
  }

  @Test
  void anUnregisteredNameIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> RangeTables.byName("nonexistent"));
  }
}
