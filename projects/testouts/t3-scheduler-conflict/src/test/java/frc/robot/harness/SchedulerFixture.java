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

/** Given test support for T3. Part of the fixture — do not edit. */
public final class SchedulerFixture {
  private static RobotContainer container;

  private SchedulerFixture() {}

  /** Call from {@code @BeforeEach}. */
  public static void setUp() {
    if (!HAL.initialize(500, 0)) {
      fail("The WPILib hardware layer would not start. Show a mentor — this is not your code.");
    }
    DriverStationSim.setDsAttached(true);
    DriverStationSim.setEnabled(true);
    for (int i = 0; i < 100 && !DriverStation.isEnabled(); i++) {
      DriverStationSim.notifyNewData();
    }
    if (!DriverStation.isEnabled()) {
      fail("The simulated driver station never enabled. Show a mentor — this is not your code.");
    }
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

  private static RobotContainer container() {
    if (container == null) {
      try {
        container = new RobotContainer();
      } catch (RuntimeException | Error e) {
        return fail("Building RobotContainer threw " + describe(e));
      }
    }
    return container;
  }

  private static String describe(Throwable e) {
    return e.getClass().getSimpleName() + (e.getMessage() == null ? "" : ": " + e.getMessage());
  }

  /** A fresh instance of the student's combined command, or a readable failure. */
  public static Command combinedCommand() {
    Command first = call();
    Command second = call();
    if (first == second) {
      fail(
          "combinedCommand() handed back the same Command object twice. Build a new one inside "
              + "the method — WPILib will not schedule a command that belongs to a composition.");
    }
    return second;
  }

  private static Command call() {
    Command command;
    try {
      command = container().combinedCommand();
    } catch (UnsupportedOperationException e) {
      return fail(
          "combinedCommand() still throws UnsupportedOperationException — that is the stub this "
              + "fixture ships with. Part 1 of the test-out is to replace it. See README.md.");
    } catch (RuntimeException e) {
      return fail("combinedCommand() threw " + describe(e) + " — that came out of your code.");
    }
    if (command == null) {
      return fail("combinedCommand() returned null. The tests have no other way to reach it.");
    }
    return command;
  }

  /** The elevator. */
  public static Subsystem elevator() {
    return container().elevator();
  }

  /** The gripper. */
  public static Subsystem gripper() {
    return container().gripper();
  }

  /** The drivetrain. */
  public static Subsystem drivetrain() {
    return container().drivetrain();
  }

  /** Schedules a command, turning WPILib's rejection into something readable. */
  public static void schedule(String what, Command command) {
    try {
      CommandScheduler.getInstance().schedule(command);
    } catch (IllegalArgumentException e) {
      fail(
          what
              + " could not be scheduled — "
              + e.getMessage()
              + "\nReturn a new command from combinedCommand() rather than a stored one.");
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
