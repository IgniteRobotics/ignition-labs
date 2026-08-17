package frc.robot;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.harness.M5Deliverables;
import frc.robot.plant.ScoringMechanism;
import frc.robot.plant.SwerveDriveSim;
import frc.robot.plant.VisionSim;

/**
 * Where M5 is really built. Everything else in this project is given.
 *
 * <p>Work through README.md in order: Part 1 is the auto routine, Part 2 is vision fusion. The four
 * TODO regions below are the whole job, and the nine methods at the bottom are how the tests and
 * {@code Robot} reach what you built — implement each to do or return the thing its Javadoc in
 * {@link M5Deliverables} names.
 */
public class RobotContainer implements M5Deliverables {

  // ===================== TODO 1 — construct your plants =======================
  // One SwerveDriveSim, one VisionSim, and one ScoringMechanism, private final
  // fields. All three classes are given -- see plant/. README.md Part 1 step 1.
  // ==============================================================================

  // ===================== TODO 2 — pose estimator, odometry half ================
  // A SwerveDrivePoseEstimator field, seeded in your constructor from
  // drivetrain().gyroAngle() and drivetrain().modulePositions(). updateOdometry(),
  // estimatedPose(), and resetEstimator() are built on it. README.md Part 2 step 1.
  // ==============================================================================

  // ===================== TODO 3 — vision fusion and the filter =================
  // acceptsMeasurement() is your filter -- estimatedPose() is available to compare
  // a candidate against. addVisionMeasurement() feeds an accepted candidate, and
  // its exact timestamp, into the same pose estimator. README.md Part 2 steps 2-3.
  // ==============================================================================

  // ===================== TODO 4 — the auto routine and chooser =================
  // Paths (AutoBuilder.followPath), named commands, timeouts, composed with
  // Commands.sequence(...) or similar. A SendableChooser<Command> you build and
  // populate yourself. README.md Part 1 steps 2-4.
  // ==============================================================================

  public RobotContainer() {}

  @Override
  public Command autoRoutine() {
    throw new UnsupportedOperationException(
        "TODO(M5): return a NEW command composing your paths, named commands, and timeouts");
  }

  @Override
  public SendableChooser<Command> autoChooser() {
    throw new UnsupportedOperationException(
        "TODO(M5): build and populate your own SendableChooser<Command>");
  }

  @Override
  public void updateOdometry() {
    throw new UnsupportedOperationException(
        "TODO(M5): one 20ms wheels-and-gyro-only update of your pose estimator");
  }

  @Override
  public void addVisionMeasurement(Pose2d visionPose, double timestampSeconds) {
    throw new UnsupportedOperationException(
        "TODO(M5): fuse an accepted vision pose into your pose estimator, at its own timestamp");
  }

  @Override
  public boolean acceptsMeasurement(Pose2d visionPose, double timestampSeconds) {
    throw new UnsupportedOperationException(
        "TODO(M5): decide whether to trust this vision pose enough to fuse it");
  }

  @Override
  public Pose2d estimatedPose() {
    throw new UnsupportedOperationException("TODO(M5): return your pose estimator's estimate");
  }

  @Override
  public void resetEstimator(Pose2d pose) {
    throw new UnsupportedOperationException("TODO(M5): seed your pose estimator at this pose");
  }

  @Override
  public SwerveDriveSim drivetrain() {
    throw new UnsupportedOperationException("TODO(M5): return your SwerveDriveSim");
  }

  @Override
  public VisionSim vision() {
    throw new UnsupportedOperationException("TODO(M5): return your VisionSim");
  }

  @Override
  public ScoringMechanism mechanism() {
    throw new UnsupportedOperationException("TODO(M5): return your ScoringMechanism");
  }
}
