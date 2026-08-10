package frc.testout.t2;

/** Prints what each range table would do at one distance. */
public final class ShooterConsole {

  private ShooterConsole() {}

  /**
   * Formats one line of shooter telemetry.
   *
   * @param table the range table to ask
   * @param distanceMeters the distance to the target, in metres
   * @return a line of the form {@code "4.0 m -> 3600 RPM"}
   */
  public static String report(RangeTable table, double distanceMeters) {
    return String.format("%.1f m -> %.0f RPM", distanceMeters, table.rpmFor(distanceMeters));
  }

  /**
   * Prints a report for every table this fixture knows about.
   *
   * @param args ignored
   */
  public static void main(String[] args) {
    double distanceMeters = 4.0;
    for (String name : new String[] {"parabolic", "mapped"}) {
      System.out.println(name + ": " + report(RangeTables.byName(name), distanceMeters));
    }
  }
}
