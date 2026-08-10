package frc.testout.t2;

/** Hands out range tables by name. This is how the rest of the code gets one. */
public final class RangeTables {

  private RangeTables() {}

  /**
   * Returns the range table registered under a name.
   *
   * @param name the table's name
   * @return the table
   * @throws IllegalArgumentException if no table is registered under that name
   */
  public static RangeTable byName(String name) {
    switch (name) {
      case "parabolic":
        return new ParabolicRangeTable();
      case "mapped":
        return new MappedRangeTable();
      default:
        throw new IllegalArgumentException("unknown range table: " + name);
    }
  }
}
