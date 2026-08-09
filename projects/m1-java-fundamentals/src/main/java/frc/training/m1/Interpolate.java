package frc.training.m1;

/** Exercise 6 — estimating a value between two measurements. */
public final class Interpolate {

  private Interpolate() {}

  /**
   * Estimates an output for an input that falls between two known measurements.
   *
   * <p>A simplified version of what a shooter's launch calculator does: somebody measured the right
   * flywheel speed at two distances, and the robot needs a speed for a distance in between. The
   * estimate follows the straight line joining the two known points.
   *
   * <p>Inputs outside the measured range do not follow that line off the end. An input at or below
   * {@code x0} returns {@code y0}, and an input at or above {@code x1} returns {@code y1}. A
   * shooter asked about a distance nobody measured should fall back on the closest thing that *was*
   * measured rather than invent a number.
   *
   * @param x0 the lower known input
   * @param y0 the measured output at {@code x0}
   * @param x1 the upper known input, strictly greater than {@code x0}
   * @param y1 the measured output at {@code x1}, which may be smaller than {@code y0}
   * @param x the input to estimate an output for
   * @return the estimated output
   */
  public static double between(double x0, double y0, double x1, double y1, double x) {
    throw new UnsupportedOperationException("TODO");
  }
}
