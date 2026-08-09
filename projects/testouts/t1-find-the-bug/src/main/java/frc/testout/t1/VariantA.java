package frc.testout.t1;

/** Test-out variant A. */
public final class VariantA {

  private VariantA() {}

  /**
   * Averages the first {@code count} samples in a rolling sensor buffer.
   *
   * @param samples the buffer to read from
   * @param count how many leading samples to average, at least 1 and at most {@code samples.length}
   * @return the mean of those samples
   */
  public static double meanOfFirst(double[] samples, int count) {
    double total = 0.0;
    for (int i = 0; i <= count; i++) {
      total += samples[i];
    }
    return total / count;
  }
}
