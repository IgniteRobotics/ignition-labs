package frc.training.m2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.DoubleSupplier;
import org.junit.jupiter.api.Test;

/**
 * The contract from {@link SensorProfileSpec}, enforced by behaviour. Part of the milestone — do
 * not edit.
 *
 * <p>An encoder reporting 2048 ticks per motor rotation, behind a 9:1 reduction. One rotation of
 * the mechanism is 18432 ticks.
 */
class SensorProfileContractTest {

  private static final int TICKS_PER_ROTATION = 2048;
  private static final double GEAR_RATIO = 9.0;
  private static final int TICKS_PER_MECHANISM_ROTATION = 18432;
  private static final double DELTA = 1e-9;

  private static Object profile() {
    return ContractProbe.newProfile(TICKS_PER_ROTATION, GEAR_RATIO);
  }

  @Test
  void theClassExistsAndCanBeBuilt() {
    profile();
  }

  @Test
  void everyFieldIsPrivate() {
    ContractProbe.assertEveryFieldIsPrivate();
  }

  @Test
  void oneMechanismRotation() {
    assertEquals(1.0, ContractProbe.ticksToRotations(profile(), 18432), DELTA);
  }

  @Test
  void oneMotorRotationIsOneNinthOfTheMechanism() {
    assertEquals(1.0 / 9.0, ContractProbe.ticksToRotations(profile(), 2048), DELTA);
  }

  @Test
  void aQuarterOfAMotorTurnIsNotZero() {
    assertEquals(0.25 / 9.0, ContractProbe.ticksToRotations(profile(), 512), DELTA);
  }

  @Test
  void nothingMovedIsNoRotations() {
    assertEquals(0.0, ContractProbe.ticksToRotations(profile(), 0), DELTA);
  }

  @Test
  void runningBackwardsStaysBackwards() {
    assertEquals(-1.0, ContractProbe.ticksToRotations(profile(), -18432), DELTA);
  }

  @Test
  void oneMechanismRotationBackToTicks() {
    assertEquals(TICKS_PER_MECHANISM_ROTATION, ContractProbe.rotationsToTicks(profile(), 1.0));
  }

  @Test
  void halfAMechanismRotationBackToTicks() {
    assertEquals(9216, ContractProbe.rotationsToTicks(profile(), 0.5));
  }

  @Test
  void aTinyRotationRoundsToTheNearestTickRatherThanDown() {
    // 0.0001 rotations is 1.8432 ticks. The nearest whole tick is 2, not 1.
    assertEquals(2, ContractProbe.rotationsToTicks(profile(), 0.0001));
  }

  @Test
  void aRotationSmallEnoughToRoundAwayDoesSo() {
    // 0.00001 rotations is 0.18432 ticks.
    assertEquals(0, ContractProbe.rotationsToTicks(profile(), 0.00001));
  }

  @Test
  void oneMechanismRotationEveryHundredMillisecondsIsSixHundredRpm() {
    assertEquals(600.0, ContractProbe.ticksPer100msToRpm(profile(), 18432), DELTA);
  }

  @Test
  void halfThatSpeedIsHalfThoseRpm() {
    assertEquals(300.0, ContractProbe.ticksPer100msToRpm(profile(), 9216), DELTA);
  }

  @Test
  void aStoppedMechanismIsZeroRpm() {
    assertEquals(0.0, ContractProbe.ticksPer100msToRpm(profile(), 0), DELTA);
  }

  @Test
  void sixHundredRpmBackToTicksPerHundredMilliseconds() {
    assertEquals(18432.0, ContractProbe.rpmToTicksPer100ms(profile(), 600.0), DELTA);
  }

  @Test
  void anRpmThatDoesNotLandOnAWholeTickIsNotRounded() {
    // 1 RPM is 30.72 ticks per 100 ms. This conversion answers about a rate, not a reading.
    assertEquals(30.72, ContractProbe.rpmToTicksPer100ms(profile(), 1.0), DELTA);
  }

  @Test
  void theSupplierConvertsWhatItIsGiven() {
    double[] rawTicks = {18432};
    DoubleSupplier rotations = ContractProbe.rotationsFrom(profile(), () -> rawTicks[0]);
    assertEquals(1.0, rotations.getAsDouble(), DELTA);
  }

  @Test
  void theSupplierReadsAFreshValueEveryTimeItIsAsked() {
    double[] rawTicks = {0};
    DoubleSupplier rotations = ContractProbe.rotationsFrom(profile(), () -> rawTicks[0]);

    rawTicks[0] = 18432;
    assertEquals(1.0, rotations.getAsDouble(), DELTA);

    rawTicks[0] = 9216;
    assertEquals(0.5, rotations.getAsDouble(), DELTA);

    rawTicks[0] = -18432;
    assertEquals(-1.0, rotations.getAsDouble(), DELTA);
  }

  @Test
  void theSupplierReadsNothingUntilItIsAsked() {
    int[] timesRead = {0};
    ContractProbe.rotationsFrom(
        profile(),
        () -> {
          timesRead[0]++;
          return 0;
        });
    assertEquals(0, timesRead[0]);
  }
}
