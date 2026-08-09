package frc.training.m1;

/** Exercise 2 — encoder tick conversions. */
public final class TickConversion {

  private TickConversion() {}

  /**
   * Converts a raw encoder reading to rotations.
   *
   * <p>An encoder reports whole ticks, so both parameters are {@code int}. The answer is not a
   * whole number, and any fractional part is part of the answer.
   *
   * @param ticks a raw encoder reading, which may be negative
   * @param ticksPerRotation how many ticks this encoder reports per full rotation, greater than
   *     zero
   * @return the number of rotations the reading represents, fractional part included
   */
  public static double ticksToRotations(int ticks, int ticksPerRotation) {
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * Converts an encoder tick delta measured over an interval to revolutions per minute.
   *
   * @param ticks the change in the encoder reading across the interval
   * @param ticksPerRotation how many ticks this encoder reports per full rotation, greater than
   *     zero
   * @param seconds the length of the interval in seconds, greater than zero
   * @return revolutions per minute over that interval
   */
  public static double ticksToRpm(int ticks, int ticksPerRotation, double seconds) {
    throw new UnsupportedOperationException("TODO");
  }
}
