package frc.testout.t2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Given test for the implementation you are adding. Part of the fixture — do not edit. */
class FlatRangeTableTest {

  private static final double DELTA = 1e-9;

  @Test
  void theFlatTableIsRegisteredUnderItsName() {
    RangeTables.byName("flat");
  }

  @Test
  void theFlatTableReturnsThreeThousandRpmCloseIn() {
    assertEquals(3000.0, RangeTables.byName("flat").rpmFor(1.0), DELTA);
  }

  @Test
  void theFlatTableReturnsThreeThousandRpmFarOut() {
    assertEquals(3000.0, RangeTables.byName("flat").rpmFor(7.5), DELTA);
  }

  @Test
  void theFlatTableDoesNotCareAboutDistanceAtAll() {
    RangeTable flat = RangeTables.byName("flat");
    assertEquals(flat.rpmFor(0.0), flat.rpmFor(12.0), DELTA);
  }
}
