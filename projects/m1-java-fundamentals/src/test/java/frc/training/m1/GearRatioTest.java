package frc.training.m1;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Given tests for {@link GearRatio}. Part of the milestone — do not edit. */
class GearRatioTest {

  private static final double DELTA = 1e-9;

  @Test
  void nineMotorTurnsThroughANineToOneIsOneMechanismTurn() {
    assertEquals(1.0, GearRatio.mechanismRotations(9.0, 9.0), DELTA);
  }

  @Test
  void halfAsManyMotorTurnsIsHalfAMechanismTurn() {
    assertEquals(0.5, GearRatio.mechanismRotations(4.5, 9.0), DELTA);
  }

  @Test
  void noMotionIsNoMotion() {
    assertEquals(0.0, GearRatio.mechanismRotations(0.0, 9.0), DELTA);
  }

  @Test
  void runningBackwardsStaysBackwards() {
    assertEquals(-1.0, GearRatio.mechanismRotations(-9.0, 9.0), DELTA);
  }

  @Test
  void aRatioThatIsNotAWholeNumber() {
    assertEquals(2.0, GearRatio.mechanismRotations(13.5, 6.75), DELTA);
  }
}
