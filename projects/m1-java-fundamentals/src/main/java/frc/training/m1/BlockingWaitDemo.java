package frc.training.m1;

/**
 * Shows what waiting does to a robot.
 *
 * <p>Run it with {@code ./gradlew blockingWaitDemo}.
 *
 * <p>Robot code runs one loop every 20 milliseconds, and everything happens inside that loop:
 * reading the driver's sticks, checking limit switches, deciding whether to stop. This program
 * stands in for that loop. On iteration 10 it does the thing that feels natural and is wrong — it
 * waits for something to be ready instead of checking whether it is ready and coming back next
 * time.
 *
 * <p>Watch the timestamps.
 */
public final class BlockingWaitDemo {

  private static final int LOOP_PERIOD_MS = 20;
  private static final int ITERATIONS = 20;
  private static final int STALL_AT_ITERATION = 10;
  private static final long STALL_MS = 2000;

  private BlockingWaitDemo() {}

  /**
   * Runs the demonstration.
   *
   * @param args ignored
   * @throws InterruptedException if the sleep between loops is interrupted
   */
  public static void main(String[] args) throws InterruptedException {
    long start = System.currentTimeMillis();

    for (int i = 0; i < ITERATIONS; i++) {
      if (i == STALL_AT_ITERATION) {
        waitUntilReady();
      }
      System.out.printf("loop %2d  at %6d ms%n", i, System.currentTimeMillis() - start);
      Thread.sleep(LOOP_PERIOD_MS);
    }

    System.out.println();
    System.out.printf(
        "Loops 0-%d each came round about every %d ms.%n", STALL_AT_ITERATION - 1, LOOP_PERIOD_MS);
    System.out.printf(
        "Loop %d did not. For %d ms this robot read no sticks, checked no limit switches, and"
            + " could not be told to stop.%n",
        STALL_AT_ITERATION, STALL_MS);
    System.out.println(
        "On a field, that is a robot still driving at whatever it was last told, into whatever is"
            + " in front of it.");
  }

  /** The mistake: waiting for a condition instead of checking it and returning. */
  private static void waitUntilReady() {
    long deadline = System.currentTimeMillis() + STALL_MS;
    while (System.currentTimeMillis() < deadline) {
      // Deliberately spinning. This is the bug being demonstrated.
    }
  }
}
