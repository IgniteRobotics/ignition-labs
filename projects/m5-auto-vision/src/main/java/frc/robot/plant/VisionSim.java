package frc.robot.plant;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.apriltag.AprilTagFields;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation3d;
import org.photonvision.PhotonCamera;
import org.photonvision.simulation.PhotonCameraSim;
import org.photonvision.simulation.SimCameraProperties;
import org.photonvision.simulation.VisionSystemSim;
import org.photonvision.targeting.PhotonPipelineResult;

/**
 * Wraps PhotonVision's real {@link VisionSystemSim} -- not a stand-in. Spike G (design record §1)
 * found that PhotonVision's simulation honours WPILib's simulated clock, unlike CTRE Phoenix's,
 * which is why this milestone can hand you the genuine vendor simulation instead of a substitute
 * the way M4 had to.
 *
 * <p><b>{@link #update} must be called AFTER {@link SwerveDriveSim#step}, every tick, or not at
 * all.</b> Passing a stale pose produces a stale frame.
 *
 * <p><b>Every given test that reads {@link #latestResult()} calls {@code SimHooks.pauseTiming()}
 * first.</b> {@link PhotonCameraSim} is FPS-limited against the same simulated clock {@link
 * #update} advances -- spike B/E found that in a free-running loop it returns one cached frame
 * while the robot drives away from it, because almost no simulated time passes between iterations
 * without {@code pauseTiming()} plus an explicit {@code SimHooks.stepTiming(...)} per tick.
 *
 * <p><b>Noise and latency are unseeded and configurable</b> -- {@link #setNoise} and {@link
 * #setLatency} let you make the vision worse on purpose. No given test may assert a frame-exact
 * vision value because of this; the three that exist are aggregate-over-a-run or bypass the camera
 * entirely (design record §2).
 */
public final class VisionSim {

  private static final String CAMERA_NAME = "M5_SimCamera";

  public static final Transform3d ROBOT_TO_CAMERA =
      new Transform3d(
          new Translation3d(0.28, 0.0, 0.22), new Rotation3d(0.0, Math.toRadians(-15.0), 0.0));

  private final VisionSystemSim visionSim = new VisionSystemSim("m5");
  private final PhotonCamera camera = new PhotonCamera(CAMERA_NAME);
  private final SimCameraProperties cameraProperties = new SimCameraProperties();
  private final PhotonCameraSim cameraSim;

  public VisionSim() {
    cameraProperties.setCalibration(960, 720, Rotation2d.fromDegrees(90.0));
    cameraProperties.setFPS(20.0);
    cameraProperties.setAvgLatencyMs(20.0);
    cameraProperties.setLatencyStdDevMs(5.0);
    cameraProperties.setCalibError(0.20, 0.05);

    cameraSim = new PhotonCameraSim(camera, cameraProperties);
    visionSim.addAprilTags(fieldLayout());
    visionSim.addCamera(cameraSim, ROBOT_TO_CAMERA);
  }

  /**
   * Advances the simulated camera from the robot's true pose. Call this once per tick, immediately
   * after {@link SwerveDriveSim#step} -- see the class Javadoc.
   *
   * @param trueRobotPose ground truth, e.g. {@link SwerveDriveSim#truePose()}
   */
  public void update(Pose2d trueRobotPose) {
    visionSim.update(trueRobotPose);
  }

  /**
   * @return the most recent frame the simulated camera published. May have no targets, and may be
   *     the same frame as last call -- see the class Javadoc on FPS limiting.
   */
  public PhotonPipelineResult latestResult() {
    return camera.getLatestResult();
  }

  /**
   * @return the underlying {@link PhotonCamera}
   */
  public PhotonCamera camera() {
    return camera;
  }

  /**
   * @return the fixed camera-to-robot mount transform this simulated camera was built with
   */
  public Transform3d robotToCamera() {
    return ROBOT_TO_CAMERA;
  }

  /**
   * Configures pixel-level noise on target detections. Unseeded -- repeated calls with the same
   * arguments do not reproduce the same frames.
   *
   * @param avgErrPixels average pixel error
   * @param errStdDevPixels pixel error standard deviation
   */
  public void setNoise(double avgErrPixels, double errStdDevPixels) {
    cameraProperties.setCalibError(avgErrPixels, errStdDevPixels);
  }

  /**
   * Configures simulated processing latency.
   *
   * @param avgMs average latency, milliseconds
   * @param stdDevMs latency standard deviation, milliseconds
   */
  public void setLatency(double avgMs, double stdDevMs) {
    cameraProperties.setAvgLatencyMs(avgMs);
    cameraProperties.setLatencyStdDevMs(stdDevMs);
  }

  /**
   * @return the 2026 field's AprilTag layout, {@code k2026RebuiltWelded} -- matching {@code
   *     Robot-2026/src/main/java/frc/robot/Constants.java:38} exactly
   */
  public static AprilTagFieldLayout fieldLayout() {
    return AprilTagFieldLayout.loadField(AprilTagFields.k2026RebuiltWelded);
  }
}
