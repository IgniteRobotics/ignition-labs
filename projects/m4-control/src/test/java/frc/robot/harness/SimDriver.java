package frc.robot.harness;

import static org.junit.jupiter.api.Assertions.fail;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.simulation.DriverStationSim;
import edu.wpi.first.wpilibj.simulation.SimHooks;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.Constants;
import frc.robot.RobotContainer;
import frc.robot.plant.ArmPlant;
import frc.robot.plant.FlywheelPlant;

/**
 * Given test support. Part of the milestone -- do not edit.
 *
 * <p>Builds exactly one {@code RobotContainer} for the whole run, turns a stub's {@code
 * UnsupportedOperationException} into a sentence you can act on, and owns the stepped loop every
 * given test drives your control code through: call your update method, then advance the plant's
 * physics by exactly one {@link Constants#SIM_STEP_SECONDS} tick. That pairing is exactly what
 * {@code Robot.robotPeriodic()} and {@code Robot.simulationPeriodic()} do, one call apart -- a
 * given test looks like one lap of the real robot loop.
 */
public final class SimDriver {
  private static RobotContainer container;
  private static AssertionError containerFailure;

  private SimDriver() {}

  /** Call from {@code @BeforeEach}. */
  public static void setUp() {
    if (!HAL.initialize(500, 0)) {
      fail(
          "The WPILib hardware layer would not start. This is a toolchain problem, not "
              + "something you did -- show a mentor.");
    }
    DriverStationSim.setDsAttached(true);
    DriverStationSim.setEnabled(true);
    waitUntilEnabled();
    SimHooks.pauseTiming();
    CommandScheduler.getInstance().cancelAll();
    container();
  }

  /** Call from {@code @AfterEach}. */
  public static void tearDown() {
    CommandScheduler.getInstance().cancelAll();
    CommandScheduler.getInstance().run();
    SimHooks.resumeTiming();
  }

  private static void waitUntilEnabled() {
    for (int i = 0; i < 100 && !DriverStation.isEnabled(); i++) {
      DriverStationSim.notifyNewData();
    }
    if (!DriverStation.isEnabled()) {
      fail(
          "The simulated driver station never reported the robot as enabled. This is a "
              + "toolchain problem -- show a mentor.");
    }
  }

  /** Your {@code RobotContainer}, or a readable failure explaining why it could not be had. */
  public static RobotContainer container() {
    if (containerFailure != null) {
      throw containerFailure;
    }
    if (container == null) {
      try {
        container = new RobotContainer();
      } catch (RuntimeException | Error e) {
        try {
          fail(
              "Building your RobotContainer threw "
                  + describe(e)
                  + "\nEvery test in this milestone has to build one first, so nothing can pass "
                  + "until that constructor runs cleanly. Start there.");
        } catch (AssertionError failure) {
          containerFailure = failure;
          throw failure;
        }
      }
    }
    return container;
  }

  private static String describe(Throwable e) {
    return e.getClass().getSimpleName() + (e.getMessage() == null ? "" : ": " + e.getMessage());
  }

  private static FlywheelPlant flywheelPlant() {
    FlywheelPlant plant;
    try {
      plant = container().flywheelPlant();
    } catch (UnsupportedOperationException e) {
      return fail(
          "flywheelPlant() in RobotContainer still throws UnsupportedOperationException. "
              + "Construct a FlywheelPlant in your constructor and return it.");
    }
    if (plant == null) {
      return fail("flywheelPlant() returned null. The harness needs the real plant back.");
    }
    return plant;
  }

  private static ArmPlant armPlant() {
    ArmPlant plant;
    try {
      plant = container().armPlant();
    } catch (UnsupportedOperationException e) {
      return fail(
          "armPlant() in RobotContainer still throws UnsupportedOperationException. Construct "
              + "an ArmPlant in your constructor and return it.");
    }
    if (plant == null) {
      return fail("armPlant() returned null. The harness needs the real plant back.");
    }
    return plant;
  }

  /**
   * Runs {@code iterations} loops of {@code updateFlywheel()} followed by one physics step, and
   * returns the measured velocity, rotations per second, after each iteration.
   */
  public static double[] runFlywheelLoop(int iterations) {
    double[] history = new double[iterations];
    for (int i = 0; i < iterations; i++) {
      try {
        container().updateFlywheel();
      } catch (UnsupportedOperationException e) {
        fail(
            "updateFlywheel() in RobotContainer still throws UnsupportedOperationException. "
                + "Implement one 20ms iteration of your feedforward + feedback loop.");
      }
      flywheelPlant().step(Constants.SIM_STEP_SECONDS);
      history[i] = flywheelPlant().velocityRotationsPerSecond();
      SimHooks.stepTiming(Constants.SIM_STEP_SECONDS);
    }
    return history;
  }

  /**
   * Runs {@code iterations} loops of {@code updateArm()} followed by one physics step, and returns
   * the measured position, radians, after each iteration.
   */
  public static double[] runArmLoop(int iterations) {
    double[] history = new double[iterations];
    for (int i = 0; i < iterations; i++) {
      try {
        container().updateArm();
      } catch (UnsupportedOperationException e) {
        fail(
            "updateArm() in RobotContainer still throws UnsupportedOperationException. "
                + "Implement one 20ms iteration of your feedforward + feedback loop.");
      }
      armPlant().step(Constants.SIM_STEP_SECONDS);
      history[i] = armPlant().measuredPositionRadians();
      SimHooks.stepTiming(Constants.SIM_STEP_SECONDS);
    }
    return history;
  }

  /**
   * The outcome of {@link #runHomingLoop(int)}: whether {@code homingCommand()} finished within
   * budget, and whether the arm was actually at its reverse limit at the exact moment it did.
   *
   * <p>Checking at the finish instant, rather than at the end of the loop's full iteration budget,
   * catches a homing command that finishes <em>immediately</em> without ever driving anywhere -- at
   * t=0 the arm is nowhere near the limit, so {@code atHomeLimitWhenFinished} is false. It does
   * <b>not</b> catch a homing command that never drives at all but simply waits before zeroing:
   * this arm's gravity carries an un-driven arm down to a stable rest exactly at the limit switch
   * and it stays there, so any wait past the fall time reads as "arrived" by this check too.
   * Distinguishing "actively driven here" from "gravity did it" would require instrumenting whether
   * {@link ArmPlant#setVoltage} was ever called with a meaningfully nonzero value, which is outside
   * what the pinned plant contract exposes to a test. Documented here rather than silently left for
   * a future maintainer to rediscover.
   */
  public static final class HomingResult {
    public final boolean finishedWithinBudget;
    public final boolean atHomeLimitWhenFinished;
    public final double measuredPositionRadiansWhenFinished;

    private HomingResult(
        boolean finishedWithinBudget,
        boolean atHomeLimitWhenFinished,
        double measuredPositionRadiansWhenFinished) {
      this.finishedWithinBudget = finishedWithinBudget;
      this.atHomeLimitWhenFinished = atHomeLimitWhenFinished;
      this.measuredPositionRadiansWhenFinished = measuredPositionRadiansWhenFinished;
    }
  }

  /**
   * Schedules a fresh {@code homingCommand()} and runs the scheduler for up to {@code iterations}
   * loops, stepping the arm's physics once per loop -- the same pairing {@code Robot} does. Stops
   * the instant the scheduler reports the command finished, and reports the arm's state at that
   * exact tick rather than after however many more ticks the loop budget happens to allow.
   */
  public static HomingResult runHomingLoop(int iterations) {
    Command homing;
    try {
      homing = container().homingCommand();
    } catch (UnsupportedOperationException e) {
      return fail(
          "homingCommand() in RobotContainer still throws UnsupportedOperationException. Return "
              + "a new command that drives to the reverse limit, then zeroes.");
    }
    if (homing == null) {
      return fail("homingCommand() returned null. The harness needs a real command back.");
    }
    try {
      CommandScheduler.getInstance().schedule(homing);
    } catch (IllegalArgumentException e) {
      return fail("homingCommand() could not be scheduled -- " + e.getMessage());
    }
    for (int i = 0; i < iterations; i++) {
      boolean wasScheduled = CommandScheduler.getInstance().isScheduled(homing);
      CommandScheduler.getInstance().run();
      armPlant().step(Constants.SIM_STEP_SECONDS);
      SimHooks.stepTiming(Constants.SIM_STEP_SECONDS);
      boolean isScheduledNow = CommandScheduler.getInstance().isScheduled(homing);
      if (wasScheduled && !isScheduledNow) {
        return new HomingResult(
            true, armPlant().atHomeLimit(), armPlant().measuredPositionRadians());
      }
    }
    return new HomingResult(false, armPlant().atHomeLimit(), armPlant().measuredPositionRadians());
  }
}
