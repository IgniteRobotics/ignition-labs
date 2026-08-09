package frc.training.m1;

/** Exercise 3 — gear reductions. */
public final class GearRatio {

  private GearRatio() {}

  /**
   * Converts rotations measured at the motor shaft to rotations of the mechanism it drives.
   *
   * <p>A gear ratio of 9.0 means the motor turns nine times for every one turn of the mechanism.
   *
   * @param motorRotations rotations measured at the motor shaft, which may be negative
   * @param gearRatio the reduction between motor and mechanism, greater than zero
   * @return rotations of the mechanism
   */
  public static double mechanismRotations(double motorRotations, double gearRatio) {
    throw new UnsupportedOperationException("TODO");
  }
}
