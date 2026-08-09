package frc.training.m1;

/** Exercise 1 — joystick deadband. */
public final class Deadband {

  private Deadband() {}

  /**
   * Applies a deadband to a joystick axis value.
   *
   * <p>A joystick at rest rarely reads exactly zero. Values whose magnitude is strictly below
   * {@code threshold} are treated as zero. Everything else is returned unchanged — not rescaled,
   * not shifted.
   *
   * @param value a joystick axis value, normally between -1.0 and 1.0
   * @param threshold a non-negative magnitude
   * @return 0.0 when the magnitude of {@code value} is strictly less than {@code threshold},
   *     otherwise {@code value} itself
   */
  public static double apply(double value, double threshold) {
    throw new UnsupportedOperationException("TODO");
  }
}
