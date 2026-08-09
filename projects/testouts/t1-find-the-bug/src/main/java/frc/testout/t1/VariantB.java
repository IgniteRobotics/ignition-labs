package frc.testout.t1;

/** Test-out variant B. */
public final class VariantB {

  private VariantB() {}

  /**
   * Reports whether the elevator is clear to keep moving down.
   *
   * <p>The elevator must never travel below its lower limit.
   *
   * @param positionMeters the elevator's current height above the floor
   * @param lowerLimitMeters the lowest height it is allowed to reach
   * @return true when the elevator is still above its limit and may continue descending
   */
  public static boolean mayDescend(double positionMeters, double lowerLimitMeters) {
    return positionMeters < lowerLimitMeters;
  }
}
