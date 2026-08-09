package frc.training.m1;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Given tests for {@link AtSetpoint}. Part of the milestone — do not edit. */
class AtSetpointTest {

  @Test
  void valuesThatAreEqualButNotIdenticalCountAsAtSetpoint() {
    // 0.1 + 0.2 is 0.30000000000000004, which is not == 0.3.
    assertTrue(AtSetpoint.isAtSetpoint(0.1 + 0.2, 0.3, 1e-9));
  }

  @Test
  void withNoToleranceAtAllThatSameDifferenceIsTooBig() {
    assertFalse(AtSetpoint.isAtSetpoint(0.1 + 0.2, 0.3, 0.0));
  }

  @Test
  void anExactMatchIsAtSetpointEvenWithNoTolerance() {
    assertTrue(AtSetpoint.isAtSetpoint(3000.0, 3000.0, 0.0));
  }

  @Test
  void withinToleranceFromBelow() {
    assertTrue(AtSetpoint.isAtSetpoint(95.0, 100.0, 10.0));
  }

  @Test
  void withinToleranceFromAbove() {
    assertTrue(AtSetpoint.isAtSetpoint(105.0, 100.0, 10.0));
  }

  @Test
  void tooFarBelow() {
    assertFalse(AtSetpoint.isAtSetpoint(80.0, 100.0, 10.0));
  }

  @Test
  void tooFarAbove() {
    assertFalse(AtSetpoint.isAtSetpoint(120.0, 100.0, 10.0));
  }

  @Test
  void aDifferenceExactlyEqualToTheToleranceCounts() {
    assertTrue(AtSetpoint.isAtSetpoint(90.0, 100.0, 10.0));
  }
}
