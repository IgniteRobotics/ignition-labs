package frc.robot.harness;

import edu.wpi.first.math.geometry.Pose2d;
import frc.robot.plant.VisionSim;
import java.util.Optional;
import org.photonvision.PhotonPoseEstimator;
import org.photonvision.targeting.PhotonPipelineResult;

/**
 * Converts a raw {@link PhotonPipelineResult} into a candidate field pose. Given, not student work
 * -- but it lives in exactly one place in {@code src/main} that both {@code Robot} and the given
 * test harness (Task 4's {@code SimDriver}) call. A test-source copy and a main-source copy of the
 * same conversion would drift, and the day they disagree the given test measures something the
 * robot does not do.
 *
 * <p>Wraps {@link PhotonPoseEstimator#estimateLowestAmbiguityPose(PhotonPipelineResult)} --
 * <b>not</b> {@code PhotonPoseEstimator.update(PhotonPipelineResult)}, which is {@code
 * Deprecated(forRemoval = true, since = "2026")} in the pinned {@code v2026.2.1}.
 */
public final class VisionPoseHelper {

  private static final PhotonPoseEstimator ESTIMATOR =
      new PhotonPoseEstimator(VisionSim.fieldLayout(), VisionSim.ROBOT_TO_CAMERA);

  private VisionPoseHelper() {}

  /**
   * @param result the camera's latest frame
   * @return the lowest-ambiguity candidate field pose; empty when the frame has no usable target
   */
  public static Optional<Pose2d> candidatePose(PhotonPipelineResult result) {
    return ESTIMATOR.estimateLowestAmbiguityPose(result).map(est -> est.estimatedPose.toPose2d());
  }

  /**
   * @param result the camera's latest frame
   * @return when the frame that produced {@code result} was actually captured -- not when this
   *     method is called
   */
  public static double timestampSeconds(PhotonPipelineResult result) {
    return result.getTimestampSeconds();
  }
}
