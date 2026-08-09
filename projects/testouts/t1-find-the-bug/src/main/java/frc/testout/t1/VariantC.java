package frc.testout.t1;

/** Test-out variant C. */
public final class VariantC {

  private VariantC() {}

  /**
   * Returns how far through the match we are, as a fraction from 0.0 to 1.0.
   *
   * @param elapsedSeconds whole seconds since the match started, zero or more
   * @param matchLengthSeconds the match length in whole seconds, greater than zero
   * @return the fraction of the match that has elapsed
   */
  public static double matchFraction(int elapsedSeconds, int matchLengthSeconds) {
    return elapsedSeconds / matchLengthSeconds;
  }
}
