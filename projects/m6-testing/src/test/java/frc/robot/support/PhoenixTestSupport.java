package frc.robot.support;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.StatusCode;
import com.ctre.phoenix6.hardware.TalonFX;
import edu.wpi.first.hal.HAL;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.simulation.DriverStationSim;

/**
 * The CTRE simulation setup that is not obvious and will waste an afternoon if you do not know it.
 *
 * <p><b>This class stays here.</b> It is scaffolding for a sandbox, and it must never be copied
 * into {@code Robot-2026}. The pull request you open for this milestone is subject to the
 * pure-logic hard rule: no test you add to the season repository may depend on timing, simulation,
 * or hardware, because a flaky test there blocks the whole team's pull requests during competition
 * season. A flake here costs one student an afternoon. There is the difference.
 *
 * <p>Note what these methods do <i>not</i> do: sleep. {@code Thread.sleep(100)} on your laptop and
 * {@code Thread.sleep(100)} on a loaded CI runner are not the same wait, which is how a test
 * becomes intermittent — and an intermittent failure is worse than a consistent one, because you
 * will assume your code is flaky when your test is. Waiting on the signal itself asks the question
 * you actually mean: <i>has the value arrived yet?</i>
 */
public final class PhoenixTestSupport {

  /**
   * CTRE documents roughly 100 ms after constructing devices and enabling before commanding an
   * actuator. That is how long the simulated device takes to come up and start publishing. The
   * timeout here is generous on purpose: it is a ceiling on how long we are willing to wait, not a
   * duration we intend to spend.
   */
  private static final double CONSTRUCTION_TIMEOUT_SECONDS = 0.5;

  /**
   * CTRE documents roughly 20 ms after a control request before the result is worth asserting on.
   * Same reasoning as above — this is the ceiling, and the wait returns as soon as the signal
   * updates.
   */
  private static final double CONTROL_REQUEST_TIMEOUT_SECONDS = 0.1;

  private PhoenixTestSupport() {}

  /**
   * Brings up the HAL and puts the simulated robot in enabled state.
   *
   * <p>A disabled robot ignores actuator commands, so a test that skips this asserts on a motor
   * that was never allowed to move — and the failure says nothing about why.
   */
  public static void enableRobot() {
    assertTrue(
        HAL.initialize(500, 0),
        "The WPILib HAL failed to initialize. This is an environment problem, not a problem "
            + "with your test: the desktop native libraries are not reaching the test JVM. "
            + "Check that JAVA_HOME points at ~/wpilib/2026/jdk and re-run.");
    DriverStationSim.setEnabled(true);
    DriverStationSim.notifyNewData();
    DriverStation.refreshData();
    assertTrue(
        DriverStation.isEnabled(),
        "The simulated Driver Station did not report enabled after setEnabled(true) and "
            + "notifyNewData(). Anything you command a motor to do from here will be ignored.");
  }

  /**
   * Waits for a freshly constructed device to start publishing.
   *
   * @param motor the device that was just constructed
   */
  public static void settleAfterConstruction(TalonFX motor) {
    StatusCode code =
        BaseStatusSignal.waitForAll(CONSTRUCTION_TIMEOUT_SECONDS, motor.getVelocity());
    assertTrue(
        code.isOK(),
        "A TalonFX did not start publishing within "
            + CONSTRUCTION_TIMEOUT_SECONDS
            + "s of being constructed (status: "
            + code
            + "). Every assertion after this point would be reading a value that was never "
            + "sent. If you see this, the device is not simulating — check that the robot was "
            + "enabled first.");
  }

  /**
   * Waits for a device to report the result of a control request.
   *
   * @param motor the device that was just commanded
   */
  public static void settleAfterControlRequest(TalonFX motor) {
    StatusCode code =
        BaseStatusSignal.waitForAll(CONTROL_REQUEST_TIMEOUT_SECONDS, motor.getVelocity());
    assertTrue(
        code.isOK(),
        "A TalonFX did not report a new velocity within "
            + CONTROL_REQUEST_TIMEOUT_SECONDS
            + "s of a control request (status: "
            + code
            + "). Asserting now would test the value from before the request, which passes or "
            + "fails at random.");
  }
}
