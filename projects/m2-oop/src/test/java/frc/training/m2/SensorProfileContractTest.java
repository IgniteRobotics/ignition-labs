package frc.training.m2;

import org.junit.jupiter.api.Test;

/**
 * The contract from {@link SensorProfileSpec}, enforced by behaviour. Part of the milestone — do
 * not edit.
 */
class SensorProfileContractTest {

  private static final int TICKS_PER_ROTATION = 2048;
  private static final double GEAR_RATIO = 9.0;

  @Test
  void theClassExistsAndCanBeBuilt() {
    ContractProbe.newProfile(TICKS_PER_ROTATION, GEAR_RATIO);
  }
}
