package frc.robot.harness;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Records a setpoint-versus-measurement history and writes it to a CSV under {@code build/plots/}.
 *
 * <p>Given, locked. Use this from your own code — a command that runs during {@code simulateJava},
 * or a manual test you write yourself — to produce the plots this milestone asks you to read. No
 * given test uses it; it exists for you.
 *
 * <pre>{@code
 * PlotHarness harness = new PlotHarness("flywheel-step", "target_rps", "measured_rps");
 * double t = 0.0;
 * for (int i = 0; i < 250; i++) {
 *   // ... run one iteration of your loop ...
 *   harness.record(t, target, plant.velocityRotationsPerSecond());
 *   t += Constants.SIM_STEP_SECONDS;
 * }
 * Path written = harness.write();
 * }</pre>
 */
public final class PlotHarness {

  private final String name;
  private final String setpointLabel;
  private final String measurementLabel;
  private final List<double[]> rows = new ArrayList<>();

  /**
   * @param name file name, without extension — written to {@code build/plots/<name>.csv}
   * @param setpointLabel CSV column header for the setpoint
   * @param measurementLabel CSV column header for the measurement
   */
  public PlotHarness(String name, String setpointLabel, String measurementLabel) {
    this.name = name;
    this.setpointLabel = setpointLabel;
    this.measurementLabel = measurementLabel;
  }

  /**
   * Records one sample.
   *
   * @param timeSeconds elapsed time, seconds
   * @param setpoint the commanded value at this instant
   * @param measurement the measured value at this instant
   */
  public void record(double timeSeconds, double setpoint, double measurement) {
    rows.add(new double[] {timeSeconds, setpoint, measurement});
  }

  /**
   * Writes every recorded sample to {@code build/plots/<name>.csv}, creating the directory if it
   * does not exist.
   *
   * @return the path written
   * @throws IOException if the file could not be written
   */
  public Path write() throws IOException {
    Path dir = Path.of("build", "plots");
    Files.createDirectories(dir);
    Path file = dir.resolve(name + ".csv");
    StringBuilder csv = new StringBuilder();
    csv.append("time_seconds,")
        .append(setpointLabel)
        .append(',')
        .append(measurementLabel)
        .append('\n');
    for (double[] row : rows) {
      csv.append(row[0]).append(',').append(row[1]).append(',').append(row[2]).append('\n');
    }
    Files.writeString(file, csv.toString(), StandardCharsets.UTF_8);
    return file;
  }
}
