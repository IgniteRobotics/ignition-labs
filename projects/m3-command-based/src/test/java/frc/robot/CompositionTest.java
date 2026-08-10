package frc.robot;

import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.harness.DeliverablesFixture;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Given tests for M3's composition. Part of the milestone — do not edit. */
class CompositionTest {

  @BeforeEach
  void setUp() {
    DeliverablesFixture.setUp();
  }

  @AfterEach
  void tearDown() {
    DeliverablesFixture.tearDown();
  }

  @Test
  void theCompositionNeedsBothSubsystems() {
    Command composition = DeliverablesFixture.composition();

    assertTrue(
        composition.getRequirements().contains(DeliverablesFixture.drivetrain()),
        "Your composition does not require the drivetrain. A composition requires everything its "
            + "parts require, so if the drivetrain is missing then nothing inside it uses the "
            + "drivetrain. M3 asks for a composition that combines both of your commands.");
    assertTrue(
        composition.getRequirements().contains(DeliverablesFixture.mechanism()),
        "Your composition does not require the subsystem mechanism() returns. A composition "
            + "requires everything its parts require, so nothing inside it is using that "
            + "mechanism.");
  }

  @Test
  void theCompositionRunsWhenScheduled() {
    Command composition = DeliverablesFixture.composition();
    AtomicBoolean started = new AtomicBoolean(false);
    CommandScheduler.getInstance()
        .onCommandInitialize(
            scheduled -> {
              if (scheduled == composition) {
                started.set(true);
              }
            });

    DeliverablesFixture.schedule("composition()", composition);
    DeliverablesFixture.runScheduler(5);

    assertTrue(
        started.get(),
        "Your composition never started, even though the scheduler ran five times after it was "
            + "scheduled.");
  }
}
