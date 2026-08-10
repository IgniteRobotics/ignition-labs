package frc.training.m2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Your tests for SensorProfile.
 *
 * <p>Copy this file into src/test/java/frc/training/m2/ once SensorProfile exists. It is yours from
 * that moment — change it, add to it, rename the cases. Nothing here is locked.
 *
 * <p>The numbers below describe one mechanism: an encoder reporting 2048 ticks per turn of the
 * motor, behind a gearbox that turns nine times for every one turn of the mechanism. Pick your own
 * numbers if you prefer; just work out the expected answers by hand first, because a test whose
 * expected value came from running the code proves nothing.
 */
class SensorProfileTest {

  private static final double DELTA = 1e-9;

  /**
   * The worked example. Read the shape: build the thing, ask it one question, say what the answer
   * has to be. A test name is a sentence about behaviour, not a method name with "test" stuck on
   * the front.
   */
  @Test
  void oneFullTurnOfTheMechanismIsOneRotation() {
    SensorProfile profile = new SensorProfile(2048, 9.0);

    assertEquals(1.0, profile.ticksToRotations(18432), DELTA);
  }

  /**
   * Yours. Pick a tick count that is not a whole number of motor turns — something like a quarter
   * turn — and assert the fraction you worked out by hand.
   *
   * <p>This is the test that catches an integer division. If yours passes against a SensorProfile
   * that divides two ints, it is not testing what you think it is.
   */
  @Test
  void aFractionOfATurnIsNotZero() {}

  /**
   * Yours. Convert some number of rotations to ticks and back to rotations, and assert you ended up
   * where you started.
   */
  @Test
  void convertingThereAndBackGivesTheSameAnswer() {}

  /**
   * Yours, and the important one. Build a DoubleSupplier whose value you can change, hand it to
   * rotationsFrom, then change it and ask again.
   *
   * <p>If your test would still pass when the value is read once and remembered, it is not testing
   * the thing this milestone is about.
   */
  @Test
  void theSupplierKeepsUpWhenTheEncoderMoves() {}
}
