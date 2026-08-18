package frc.robot.plant;

/**
 * A scoring mechanism that becomes deployed a fixed number of ticks after {@link #startDeploy()} --
 * unless {@link #setFaulted(boolean)} is {@code true}, in which case {@link #isDeployed()} never
 * becomes {@code true}.
 *
 * <p>This is the one step in the milestone {@code AutoTerminationTest} (Task 4) can force to fail.
 * PathPlanner's path-following command is time-based and terminates on its own whether or not the
 * robot went anywhere, so nothing in a path-only routine can actually hang. A named command that
 * starts this mechanism's deploy and waits on {@link #isDeployed()}, with no timeout, will hang
 * when this is faulted -- which is exactly the lesson project step 4 ("add timeouts so no step can
 * hang") teaches.
 */
public final class ScoringMechanism {

  private static final int DEPLOY_TICKS = 25;

  private boolean deploying = false;
  private boolean deployed = false;
  private boolean faulted = false;
  private int ticksSinceStart = 0;

  public ScoringMechanism() {}

  /** Starts (or restarts) the deploy sequence. */
  public void startDeploy() {
    deploying = true;
    deployed = false;
    ticksSinceStart = 0;
  }

  /**
   * @return {@code true} once {@link #step} has advanced enough ticks since {@link #startDeploy()}
   *     was called -- unless {@link #setFaulted} is {@code true}, in which case this never becomes
   *     {@code true}.
   */
  public boolean isDeployed() {
    return deployed;
  }

  /**
   * Advances the deploy sequence by one tick, unless faulted or not currently deploying.
   *
   * @param dtSeconds unused by the tick count; accepted for consistency with the other given
   *     plants' {@code step(dt)} signature
   */
  public void step(double dtSeconds) {
    if (!deploying || faulted) {
      return;
    }
    ticksSinceStart++;
    if (ticksSinceStart >= DEPLOY_TICKS) {
      deployed = true;
      deploying = false;
    }
  }

  /**
   * @param faulted when {@code true}, {@link #isDeployed()} can never become {@code true} while a
   *     deploy is in progress -- simulating a mechanism that physically cannot finish. Does not
   *     un-deploy a mechanism that already finished before the fault was set.
   */
  public void setFaulted(boolean faulted) {
    this.faulted = faulted;
  }

  /**
   * Resets to the not-deploying, not-deployed state. Does not clear a fault -- a fault models a
   * hardware condition that a software reset does not repair on a real robot either.
   */
  public void reset() {
    deploying = false;
    deployed = false;
    ticksSinceStart = 0;
  }
}
