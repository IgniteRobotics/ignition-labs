package frc.testout.t2;

/** A range table that computes a speed from a fitted curve. */
public final class ParabolicRangeTable implements RangeTable {

  private static final double CONSTANT_TERM = 2400.0;
  private static final double LINEAR_TERM = 180.0;
  private static final double QUADRATIC_TERM = 12.0;

  @Override
  public double rpmFor(double distanceMeters) {
    return CONSTANT_TERM
        + LINEAR_TERM * distanceMeters
        + QUADRATIC_TERM * distanceMeters * distanceMeters;
  }
}
