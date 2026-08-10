package frc.robot.harness;

import static org.junit.jupiter.api.Assertions.fail;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.simulation.DriverStationSim;
import edu.wpi.first.wpilibj.simulation.SimHooks;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.Subsystem;
import frc.robot.RobotContainer;
import java.util.function.Function;

/**
 * Given test support. Part of the milestone — do not edit.
 *
 * <p>It owns four things the given tests would otherwise each have to get right: starting the
 * WPILib hardware layer, telling the simulated driver station the robot is enabled, stepping
 * simulated time so timeouts work, and building exactly one {@code RobotContainer} for the whole
 * run. It also turns every way a deliverable can be missing into a sentence you can act on.
 */
public final class DeliverablesFixture {
  private static final String COMPOSED_HINT =
      "WPILib will not schedule a command that belongs to a composition. Return a NEW command "
          + "from each M3Deliverables method rather than storing one in a field and handing the "
          + "same object back every time.";

  private static RobotContainer container;
  private static AssertionError containerFailure;

  private DeliverablesFixture() {}

  /** Call from {@code @BeforeEach}. */
  public static void setUp() {
    if (!HAL.initialize(500, 0)) {
      fail(
          "The WPILib hardware layer would not start. This is a toolchain problem, not something "
              + "you did — show a mentor.");
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
          "The simulated driver station never reported the robot as enabled, so the scheduler "
              + "would refuse to run anything. This is a toolchain problem — show a mentor.");
    }
  }

  private static RobotContainer container() {
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

  /** Your drivetrain, or a readable failure explaining why it could not be had. */
  public static Subsystem drivetrain() {
    return required("drivetrain()", M3Deliverables::drivetrain);
  }

  /** Your other subsystem, or a readable failure. */
  public static Subsystem mechanism() {
    return required("mechanism()", M3Deliverables::mechanism);
  }

  /** A fresh instance of your subclass command. */
  public static Command subclassCommand() {
    return fresh("subclassCommand()", M3Deliverables::subclassCommand);
  }

  /** A fresh instance of your factory command. */
  public static Command factoryCommand() {
    return fresh("factoryCommand()", M3Deliverables::factoryCommand);
  }

  /** A fresh instance of your composition. */
  public static Command composition() {
    return fresh("composition()", M3Deliverables::composition);
  }

  private static <T> T required(String member, Function<M3Deliverables, T> accessor) {
    T value;
    try {
      value = accessor.apply(container());
    } catch (UnsupportedOperationException e) {
      return fail(
          member
              + " in RobotContainer still throws UnsupportedOperationException.\nThat is the stub "
              + "this project ships with. Implement it and return the thing its Javadoc names. "
              + "README.md walks the order.");
    } catch (RuntimeException | Error e) {
      return fail(
          member
              + " in RobotContainer threw "
              + describe(e)
              + "\nThat came out of your code, not out of the test. Fix that first.");
    }
    if (value == null) {
      return fail(
          member
              + " in RobotContainer returned null. The harness needs the real object back — the "
              + "tests have no other way to reach your work.");
    }
    return value;
  }

  private static Command fresh(String member, Function<M3Deliverables, Command> accessor) {
    Command first = required(member, accessor);
    Command second = required(member, accessor);
    if (first == second) {
      fail(member + " handed back the same Command object twice.\n" + COMPOSED_HINT);
    }
    return second;
  }

  /** Schedules a command, turning WPILib's rejection into something readable. */
  public static void schedule(String member, Command command) {
    try {
      CommandScheduler.getInstance().schedule(command);
    } catch (IllegalArgumentException e) {
      fail(member + " could not be scheduled — " + e.getMessage() + "\n" + COMPOSED_HINT);
    }
  }

  /** Runs the scheduler {@code loops} times, advancing simulated time 20ms per loop. */
  public static void runScheduler(int loops) {
    for (int i = 0; i < loops; i++) {
      CommandScheduler.getInstance().run();
      SimHooks.stepTiming(0.02);
    }
  }

  /** A given command that requires whatever it is handed and never finishes on its own. */
  public static HoldCommand holdCommand(Subsystem... requirements) {
    return new HoldCommand(requirements);
  }

  /** Given. Occupies a subsystem so the tests can watch what interrupting it does. */
  public static final class HoldCommand extends Command {
    private boolean interruptedEndRan;

    private HoldCommand(Subsystem... requirements) {
      addRequirements(requirements);
      setName("Given Hold");
    }

    @Override
    public boolean isFinished() {
      return false;
    }

    @Override
    public void end(boolean interrupted) {
      if (interrupted) {
        interruptedEndRan = true;
      }
    }

    /** True once this command's {@code end(true)} has run. */
    public boolean wasInterrupted() {
      return interruptedEndRan;
    }
  }
}
