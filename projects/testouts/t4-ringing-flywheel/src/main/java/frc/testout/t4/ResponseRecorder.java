package frc.testout.t4;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Runs the shipped {@link BadlyTunedFlywheel} for a fixed simulated window and writes its response
 * to {@code plots/response.csv}. This is how {@code plots/response.png} gets regenerated — see
 * {@code plots/README.md}.
 */
public final class ResponseRecorder {
  private static final double TARGET_ROTATIONS_PER_SECOND = 40.0;
  private static final int STEPS = 250; // 5 simulated seconds at 20 ms/step
  private static final int SETTLE_WINDOW = 50; // trailing 1 simulated second

  private ResponseRecorder() {}

  public static void main(String[] args) throws IOException {
    Path out = Path.of("plots", "response.csv");
    Files.createDirectories(out.getParent());

    BadlyTunedFlywheel flywheel = new BadlyTunedFlywheel();
    flywheel.setTarget(TARGET_ROTATIONS_PER_SECOND);

    double[] measured = new double[STEPS];
    StringBuilder csv = new StringBuilder();
    csv.append(
        "time_seconds,setpoint_rotations_per_second,measurement_rotations_per_second,"
            + "applied_volts\n");
    for (int i = 0; i < STEPS; i++) {
      flywheel.update();
      double t = (i + 1) * 0.02;
      measured[i] = flywheel.velocityRotationsPerSecond();
      csv.append(
          String.format(
              "%.3f,%.6f,%.6f,%.6f\n",
              t, TARGET_ROTATIONS_PER_SECOND, measured[i], flywheel.appliedVolts()));
    }
    Files.writeString(out, csv.toString());

    double peak = Double.NEGATIVE_INFINITY;
    for (double v : measured) {
      peak = Math.max(peak, v);
    }
    double settledSum = 0.0;
    for (int i = STEPS - SETTLE_WINDOW; i < STEPS; i++) {
      settledSum += measured[i];
    }
    double settled = settledSum / SETTLE_WINDOW;

    System.out.println("Wrote " + out.toAbsolutePath());
    System.out.printf(
        "target=%.3f rot/s  peak=%.3f rot/s (overshoot %.3f)  settled=%.3f rot/s (error %.3f)%n",
        TARGET_ROTATIONS_PER_SECOND,
        peak,
        peak - TARGET_ROTATIONS_PER_SECOND,
        settled,
        settled - TARGET_ROTATIONS_PER_SECOND);
  }
}
