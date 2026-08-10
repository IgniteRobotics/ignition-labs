package frc.robot;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.Subsystem;
import frc.robot.harness.DeliverablesFixture;
import frc.robot.harness.DeliverablesFixture.HoldCommand;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Given tests for the drivetrain's default command. Part of the milestone — do not edit. */
class DefaultCommandTest {

  @BeforeEach
  void setUp() {
    DeliverablesFixture.setUp();
  }

  @AfterEach
  void tearDown() {
    DeliverablesFixture.tearDown();
  }

  @Test
  void theDrivetrainHasADefaultCommand() {
    assertNotNull(
        CommandScheduler.getInstance().getDefaultCommand(DeliverablesFixture.drivetrain()),
        "The drivetrain has no default command, so the robot does nothing at all when no button "
            + "is held. Set one in RobotContainer with drivetrain.setDefaultCommand(...) — on a "
            + "real robot this is the command that reads the driver's sticks.");
  }

  @Test
  void theDefaultCommandRunsWhenNothingElseWantsTheDrivetrain() {
    Command byDefault =
        CommandScheduler.getInstance().getDefaultCommand(DeliverablesFixture.drivetrain());
    assertNotNull(byDefault, "The drivetrain has no default command.");

    DeliverablesFixture.runScheduler(2);

    assertTrue(
        CommandScheduler.getInstance().isScheduled(byDefault),
        "The drivetrain has a default command but the scheduler is not running it. A default "
            + "command must require the subsystem it is the default for.");
  }

  @Test
  void theDefaultCommandStandsAsideAndThenComesBack() {
    Subsystem drivetrain = DeliverablesFixture.drivetrain();
    Command byDefault = CommandScheduler.getInstance().getDefaultCommand(drivetrain);
    assertNotNull(byDefault, "The drivetrain has no default command.");
    DeliverablesFixture.runScheduler(2);

    HoldCommand occupant = DeliverablesFixture.holdCommand(drivetrain);
    DeliverablesFixture.schedule("the harness's hold command", occupant);
    DeliverablesFixture.runScheduler(1);
    assertFalse(
        CommandScheduler.getInstance().isScheduled(byDefault),
        "Something else took the drivetrain and the default command kept running anyway.");

    occupant.cancel();
    DeliverablesFixture.runScheduler(2);
    assertTrue(
        CommandScheduler.getInstance().isScheduled(byDefault),
        "The command that took the drivetrain ended and the default command did not come back. "
            + "That is what a default command is for — it is what runs whenever nothing else "
            + "needs the subsystem.");
  }
}
