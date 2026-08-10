package frc.training.m2;

import java.util.function.DoubleSupplier;

/**
 * A small piece of robot-shaped code that reports where the driver's stick is.
 *
 * <p>Something built this with a way of reading the stick, and asks it for the stick's position
 * whenever it needs one — every twenty milliseconds, for as long as the robot is enabled.
 *
 * <p>It does not do that. Its tests say so. Work out why, and fix it.
 */
public class SupplierDemo {

  private final double stickPosition;

  /**
   * Builds a reporter around a way of reading the driver's stick.
   *
   * @param stick a way to read the stick's position, from -1.0 to 1.0
   */
  public SupplierDemo(DoubleSupplier stick) {
    this.stickPosition = stick.getAsDouble();
  }

  /**
   * Returns where the stick is at the moment this is called.
   *
   * @return the stick's current position
   */
  public double currentStickPosition() {
    return stickPosition;
  }
}
