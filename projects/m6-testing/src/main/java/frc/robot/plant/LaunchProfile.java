package frc.robot.plant;

import edu.wpi.first.math.interpolation.InterpolatingDoubleTreeMap;

/**
 * The launch math for a flywheel shooter, reduced to pure logic.
 *
 * <p>This class is a deliberately simplified stand-in for {@code
 * Robot-2026/src/main/java/frc/robot/statemachines/LaunchCalculator.java}. The real one does the
 * same four jobs — convert encoder ticks to mechanism rotations, look up a time of flight by
 * distance, predict where the robot will be when the game piece actually leaves, and decide whether
 * the flywheel is up to speed — but it reaches them through a singleton, a pose estimator, and a
 * swerve drive's chassis speeds. None of that is testable without a robot. This one takes doubles
 * and returns doubles, which is the whole point: <b>pure logic is what is worth testing.</b>
 *
 * <p>Nothing here is yours to change. Your job is to find out whether it is right.
 */
public final class LaunchProfile {

  /**
   * How close the measured flywheel velocity must be to the target, in rotations per second, before
   * a launch is allowed.
   */
  private static final double VELOCITY_TOLERANCE_RPS = 0.5;

  /**
   * Seconds between the decision to launch and the game piece actually leaving the launcher. The
   * robot keeps moving during that window, so the distance the math should use is not the distance
   * right now.
   */
  private static final double PHASE_DELAY_SECONDS = 0.03;

  /** Distance in meters to time of flight in seconds, measured on the practice field. */
  private static final InterpolatingDoubleTreeMap TIME_OF_FLIGHT = new InterpolatingDoubleTreeMap();

  static {
    TIME_OF_FLIGHT.put(1.38, 0.90);
    TIME_OF_FLIGHT.put(1.88, 1.09);
    TIME_OF_FLIGHT.put(3.15, 1.11);
    TIME_OF_FLIGHT.put(4.55, 1.12);
    TIME_OF_FLIGHT.put(5.68, 1.16);
  }

  private final double ticksPerRevolution;
  private final double gearRatio;

  /**
   * @param ticksPerRevolution encoder ticks in one full revolution of the motor shaft; must be
   *     positive
   * @param gearRatio motor revolutions per one revolution of the mechanism; must be positive
   * @throws IllegalArgumentException if either argument is zero, negative, or not a number
   */
  public LaunchProfile(double ticksPerRevolution, double gearRatio) {
    if (!(ticksPerRevolution > 0.0)) {
      throw new IllegalArgumentException(
          "ticksPerRevolution must be positive, got " + ticksPerRevolution);
    }
    if (!(gearRatio > 0.0)) {
      throw new IllegalArgumentException("gearRatio must be positive, got " + gearRatio);
    }
    this.ticksPerRevolution = ticksPerRevolution;
    this.gearRatio = gearRatio;
  }

  /**
   * Converts a raw encoder reading to rotations of the mechanism the motor drives.
   *
   * @param ticks raw encoder ticks; may be negative, meaning the mechanism turned backwards
   * @return mechanism rotations
   */
  public double ticksToMechanismRotations(double ticks) {
    return ticks / ticksPerRevolution / gearRatio;
  }

  /**
   * Looks up how long a game piece stays in the air for a given launch distance.
   *
   * <p>Distances between measured points are interpolated. Distances outside the measured range
   * clamp to the nearest endpoint rather than extrapolating — the practice-field data says nothing
   * about what happens at nine meters, and a straight-line guess out there would be confidently
   * wrong.
   *
   * @param distanceMeters distance from launcher to target
   * @return time of flight in seconds
   */
  public double timeOfFlightSeconds(double distanceMeters) {
    return TIME_OF_FLIGHT.get(distanceMeters);
  }

  /**
   * Corrects a launch distance for the robot's own motion during the launch delay.
   *
   * @param distanceMeters distance to the target right now
   * @param closingSpeedMetersPerSecond how fast the robot is closing on the target; positive means
   *     approaching, negative means retreating
   * @return the distance the launch math should actually use
   */
  public double lookAheadDistanceMeters(double distanceMeters, double closingSpeedMetersPerSecond) {
    return distanceMeters - closingSpeedMetersPerSecond * PHASE_DELAY_SECONDS;
  }

  /**
   * Whether the flywheel is close enough to its commanded velocity to launch.
   *
   * @param measuredRps measured flywheel velocity, rotations per second
   * @param targetRps commanded flywheel velocity, rotations per second
   * @return true when the difference is within tolerance, inclusive of the boundary
   */
  public boolean atSetpoint(double measuredRps, double targetRps) {
    return Math.abs(measuredRps - targetRps) <= VELOCITY_TOLERANCE_RPS;
  }
}
