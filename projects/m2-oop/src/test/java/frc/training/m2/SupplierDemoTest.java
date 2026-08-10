package frc.training.m2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Given tests for {@link SupplierDemo}. Part of the milestone — do not edit. */
class SupplierDemoTest {

  private static final double DELTA = 1e-9;

  @Test
  void aStickThatNeverMovedReadsCorrectly() {
    double[] stick = {0.4};
    SupplierDemo demo = new SupplierDemo(() -> stick[0]);
    assertEquals(0.4, demo.currentStickPosition(), DELTA);
  }

  @Test
  void reportsWhereTheStickIsNowRatherThanWhereItWas() {
    double[] stick = {0.0};
    SupplierDemo demo = new SupplierDemo(() -> stick[0]);

    stick[0] = 0.75;

    assertEquals(0.75, demo.currentStickPosition(), DELTA);
  }

  @Test
  void keepsUpAsTheDriverKeepsMoving() {
    double[] stick = {0.0};
    SupplierDemo demo = new SupplierDemo(() -> stick[0]);

    stick[0] = 0.25;
    assertEquals(0.25, demo.currentStickPosition(), DELTA);

    stick[0] = -0.5;
    assertEquals(-0.5, demo.currentStickPosition(), DELTA);

    stick[0] = 0.0;
    assertEquals(0.0, demo.currentStickPosition(), DELTA);
  }
}
