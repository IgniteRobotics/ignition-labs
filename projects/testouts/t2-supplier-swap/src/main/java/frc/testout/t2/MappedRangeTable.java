package frc.testout.t2;

/** A range table that reads a speed off two measurements taken on the practice field. */
public final class MappedRangeTable implements RangeTable {

  private static final double NEAR_METRES = 2.0;
  private static final double NEAR_RPM = 3000.0;
  private static final double FAR_METRES = 6.0;
  private static final double FAR_RPM = 4200.0;

  @Override
  public double rpmFor(double distanceMeters) {
    if (distanceMeters <= NEAR_METRES) {
      return NEAR_RPM;
    }
    if (distanceMeters >= FAR_METRES) {
      return FAR_RPM;
    }
    double fraction = (distanceMeters - NEAR_METRES) / (FAR_METRES - NEAR_METRES);
    return NEAR_RPM + fraction * (FAR_RPM - NEAR_RPM);
  }
}
