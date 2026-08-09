package frc.training.m1;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Given tests for {@link Interpolate}. Part of the milestone — do not edit. */
class InterpolateTest {

  private static final double DELTA = 1e-9;

  @Test
  void halfwayBetweenTwoPoints() {
    assertEquals(20.0, Interpolate.between(1.0, 10.0, 3.0, 30.0, 2.0), DELTA);
  }

  @Test
  void aQuarterOfTheWayAlong() {
    assertEquals(15.0, Interpolate.between(1.0, 10.0, 3.0, 30.0, 1.5), DELTA);
  }

  @Test
  void exactlyAtTheFirstKnownPoint() {
    assertEquals(10.0, Interpolate.between(1.0, 10.0, 3.0, 30.0, 1.0), DELTA);
  }

  @Test
  void exactlyAtTheSecondKnownPoint() {
    assertEquals(30.0, Interpolate.between(1.0, 10.0, 3.0, 30.0, 3.0), DELTA);
  }

  @Test
  void belowTheKnownRangeHoldsAtTheFirstValue() {
    assertEquals(10.0, Interpolate.between(1.0, 10.0, 3.0, 30.0, 0.0), DELTA);
  }

  @Test
  void aboveTheKnownRangeHoldsAtTheSecondValue() {
    assertEquals(30.0, Interpolate.between(1.0, 10.0, 3.0, 30.0, 5.0), DELTA);
  }

  @Test
  void theOutputMayDecreaseAsTheInputRises() {
    assertEquals(40.0, Interpolate.between(2.0, 50.0, 4.0, 30.0, 3.0), DELTA);
  }

  @Test
  void aDecreasingTableStillHoldsBelowItsRange() {
    assertEquals(50.0, Interpolate.between(2.0, 50.0, 4.0, 30.0, 1.0), DELTA);
  }

  @Test
  void aRealisticShooterLookup() {
    assertEquals(3200.0, Interpolate.between(2.0, 3000.0, 4.0, 3800.0, 2.5), DELTA);
  }
}
