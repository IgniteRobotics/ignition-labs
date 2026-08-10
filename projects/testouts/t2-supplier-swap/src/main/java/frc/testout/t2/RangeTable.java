package frc.testout.t2;

/**
 * Answers one question: how fast should the flywheel spin to score from here?
 *
 * <p>Ignite's real shooter has this shape. {@code ParabolicLaunchRequestBuilder} computes a launch
 * from physics and {@code MappedLaunchRequestBuilder} looks one up from a table somebody tuned on
 * the practice field, and the shooter uses either one without knowing which it has.
 */
public interface RangeTable {

  /**
   * Returns the flywheel speed to use at a given distance.
   *
   * @param distanceMeters distance to the target, in metres
   * @return flywheel speed in RPM
   */
  double rpmFor(double distanceMeters);
}
