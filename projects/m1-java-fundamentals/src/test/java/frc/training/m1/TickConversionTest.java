package frc.training.m1;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Given tests for {@link TickConversion}. Part of the milestone — do not edit. */
class TickConversionTest {

  private static final double DELTA = 1e-9;

  @Test
  void halfARotationIsNotZero() {
    assertEquals(0.5, TickConversion.ticksToRotations(1024, 2048), DELTA);
  }

  @Test
  void oneWholeRotation() {
    assertEquals(1.0, TickConversion.ticksToRotations(2048, 2048), DELTA);
  }

  @Test
  void oneAndAHalfRotationsKeepsTheHalf() {
    assertEquals(1.5, TickConversion.ticksToRotations(3072, 2048), DELTA);
  }

  @Test
  void negativeTicksGiveNegativeRotations() {
    assertEquals(-0.5, TickConversion.ticksToRotations(-1024, 2048), DELTA);
  }

  @Test
  void zeroTicksIsZeroRotations() {
    assertEquals(0.0, TickConversion.ticksToRotations(0, 2048), DELTA);
  }

  @Test
  void oneRotationInOneSecondIsSixtyRpm() {
    assertEquals(60.0, TickConversion.ticksToRpm(2048, 2048, 1.0), DELTA);
  }

  @Test
  void halfARotationInHalfASecondIsAlsoSixtyRpm() {
    assertEquals(60.0, TickConversion.ticksToRpm(1024, 2048, 0.5), DELTA);
  }

  @Test
  void oneRotationInTwoSecondsIsThirtyRpm() {
    assertEquals(30.0, TickConversion.ticksToRpm(2048, 2048, 2.0), DELTA);
  }

  @Test
  void aRealisticShooterReading() {
    assertEquals(5859.375, TickConversion.ticksToRpm(20000, 2048, 0.1), DELTA);
  }
}
