package frc.testout.t5;

import frc.testout.t5.LogRecorder.Fault;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Command-line wrapper around {@link LogRecorder}. Given, locked. Takes the fault to record and the
 * exact file to write it to, so which letter a fault ends up as is decided by whoever invokes this
 * -- once, out of band -- and is never written down in this project. See {@code logs/README.md}.
 *
 * <p>Usage: {@code GenerateLog <ODOMETRY_DRIFT|MISTUNED_CONTROLLER|BLOCKING_COMMAND> <destPath>}
 */
public final class GenerateLog {
  private GenerateLog() {}

  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.err.println("usage: GenerateLog <fault> <destPath>");
      System.exit(1);
      return;
    }
    Fault fault = Fault.valueOf(args[0]);
    Path dest = Path.of(args[1]);
    Path tmpDir = Files.createTempDirectory("t5-log-");
    Path recorded = LogRecorder.record(fault, tmpDir);
    Files.createDirectories(dest.toAbsolutePath().getParent());
    Files.move(recorded, dest, StandardCopyOption.REPLACE_EXISTING);
    System.out.println("wrote " + dest.toAbsolutePath());
  }
}
