package frc.robot.harness;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.plant.ScoringMechanism;
import frc.robot.plant.SwerveDriveSim;
import frc.robot.plant.VisionSim;

/**
 * Exposes your M5 deliverables to the test harness and to {@code Robot}. {@code RobotContainer}
 * implements it.
 *
 * <p>The given tests cannot name your pose estimator or your filter logic — they don't know what
 * you called them — so they reach your work through these nine methods instead. {@code Robot} uses
 * the same seam: {@code robotPeriodic()} calls {@link #updateOdometry()} every 20ms, then offers
 * whatever the camera last produced to {@link #acceptsMeasurement} and, if accepted, {@link
 * #addVisionMeasurement}.
 *
 * <p><b>{@link #autoRoutine()} must return a new instance each time it is called</b>, the same rule
 * every milestone's command-returning deliverable has carried since M3: a command object that has
 * already run once cannot be scheduled again.
 *
 * <p><b>{@link #acceptsMeasurement} is the single most important method here.</b> It is the only
 * seam a given test drives with a synthetic measurement and no camera at all (`BadTimestampTest`) —
 * per the design record, it is "the only DoD item in M5 a student can otherwise fake by getting a
 * good result for the wrong reason."
 */
public interface M5Deliverables {

  /**
   * @return a NEW command composing your paths, named commands, and timeouts into one autonomous
   *     routine. A stored, already-run instance cannot be rescheduled — build it inside this
   *     method.
   */
  Command autoRoutine();

  /**
   * @return the chooser you built and populated yourself, ready to put on the dashboard
   */
  SendableChooser<Command> autoChooser();

  /**
   * Runs exactly one 20ms iteration of your pose estimator's odometry update: read {@link
   * #drivetrain()}'s {@link SwerveDriveSim#gyroAngle()} and {@link
   * SwerveDriveSim#modulePositions()}, and update your estimator from them. <b>Wheels and gyro
   * only</b> — never touch vision in here. {@code Robot.robotPeriodic()} calls this every tick
   * unconditionally; the given tests rely on it never fusing a vision measurement on its own so
   * that an odometry-only run means what it says.
   */
  void updateOdometry();

  /**
   * Fuses a vision-derived pose into your estimator, at the timestamp it was actually captured.
   * Only ever called after {@link #acceptsMeasurement} returned {@code true} for the same arguments
   * — this method does not re-check that itself.
   *
   * @param visionPose the candidate pose a vision frame produced
   * @param timestampSeconds when the frame that produced it was captured, not when this method is
   *     called
   */
  void addVisionMeasurement(Pose2d visionPose, double timestampSeconds);

  /**
   * Your filter. Decide whether {@code visionPose}/{@code timestampSeconds} is trustworthy enough
   * to fuse — {@link #estimatedPose()} is available to compare against. There is no {@code
   * MAX_TRUSTED_DISTANCE_METERS} constant anywhere in this project; choosing and defending that
   * number is the milestone.
   *
   * @param visionPose the candidate pose a vision frame produced
   * @param timestampSeconds when the frame that produced it was captured
   * @return whether {@link #addVisionMeasurement} should be called with these same arguments
   */
  boolean acceptsMeasurement(Pose2d visionPose, double timestampSeconds);

  /**
   * @return your pose estimator's current best estimate
   */
  Pose2d estimatedPose();

  /**
   * Seeds your pose estimator at a known pose — call this whenever you know for certain where the
   * robot is, e.g. at the start of autonomous.
   *
   * @param pose the known pose
   */
  void resetEstimator(Pose2d pose);

  /**
   * @return your drivetrain. The same instance every call.
   */
  SwerveDriveSim drivetrain();

  /**
   * @return your vision sim. The same instance every call.
   */
  VisionSim vision();

  /**
   * @return your scoring mechanism. The same instance every call. Your auto routine's deploy named
   *     command starts it with {@link ScoringMechanism#startDeploy()} and waits on {@link
   *     ScoringMechanism#isDeployed()}.
   */
  ScoringMechanism mechanism();
}
