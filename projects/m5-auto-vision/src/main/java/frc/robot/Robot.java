package frc.robot;

import edu.wpi.first.epilogue.Epilogue;
import edu.wpi.first.epilogue.Logged;
import edu.wpi.first.epilogue.NotLogged;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.harness.VisionPoseHelper;
import java.util.Optional;
import org.photonvision.targeting.PhotonPipelineResult;

/**
 * Given. You should not need to change anything in this file to finish M5.
 *
 * <p>{@link #robotPeriodic()} runs the {@code CommandScheduler} first -- your auto routine and any
 * named command only progress if this runs every tick -- then calls {@link
 * frc.robot.harness.M5Deliverables#updateOdometry()} for the wheels-and-gyro half of your pose
 * estimate, then reads whatever the camera last produced and offers it to your fusion seam through
 * {@link VisionPoseHelper}: {@link frc.robot.harness.M5Deliverables#acceptsMeasurement} decides
 * whether it is trusted, {@link frc.robot.harness.M5Deliverables#addVisionMeasurement} is where it
 * lands if so. {@link VisionPoseHelper} is given, not something you write -- it always resolves to
 * the single lowest-ambiguity tag in view. What you write is what happens to that candidate once it
 * reaches your seam.
 *
 * <p>{@link #simulationPeriodic()} -- only while simulating -- advances the drivetrain's physics,
 * updates the vision sim from the drivetrain's true pose, and steps the scoring mechanism, in that
 * order. Given tests reproduce the drivetrain/vision pairing by hand, without constructing a {@code
 * Robot} at all.
 */
@Logged
public class Robot extends TimedRobot {
  @NotLogged private final RobotContainer robotContainer;

  public Robot() {
    robotContainer = new RobotContainer();

    Epilogue.configure(
        config -> {
          config.root = "Robot";
        });
    Epilogue.bind(this);
  }

  @Override
  public void robotPeriodic() {
    CommandScheduler.getInstance().run();
    robotContainer.updateOdometry();

    PhotonPipelineResult result = robotContainer.vision().latestResult();
    Optional<Pose2d> candidate = VisionPoseHelper.candidatePose(result);
    if (candidate.isPresent()) {
      double timestamp = VisionPoseHelper.timestampSeconds(result);
      if (robotContainer.acceptsMeasurement(candidate.get(), timestamp)) {
        robotContainer.addVisionMeasurement(candidate.get(), timestamp);
      }
    }
  }

  @Override
  public void simulationPeriodic() {
    robotContainer.drivetrain().step(Constants.SIM_STEP_SECONDS);
    robotContainer.vision().update(robotContainer.drivetrain().truePose());
    robotContainer.mechanism().step(Constants.SIM_STEP_SECONDS);
  }
}
