package frc.training.m1;

/** Exercise 5 — constraining a value to a safe range. */
public final class Clamp {

  private Clamp() {}

  /**
   * Constrains a value to a closed range.
   *
   * <p>Used everywhere in robot code to keep a mechanism inside its safe travel.
   *
   * @param value the value to constrain
   * @param min the lowest allowed value; a value equal to it is allowed
   * @param max the highest allowed value; a value equal to it is allowed
   * @return {@code value} when it lies within the range, otherwise whichever bound it passed
   * @throws IllegalArgumentException when {@code min} is greater than {@code max}. No value can
   *     satisfy such a range, and quietly returning one of the bounds would hide the caller's
   *     mistake instead of reporting it
   */
  public static double clamp(double value, double min, double max) {
    throw new UnsupportedOperationException("TODO");
  }
}
