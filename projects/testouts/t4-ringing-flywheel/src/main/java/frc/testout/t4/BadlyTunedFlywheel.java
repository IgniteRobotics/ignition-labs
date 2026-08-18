package frc.testout.t4;

import edu.wpi.first.math.MathUtil;
import frc.testout.t4.plant.T4FlywheelPlant;

/**
 * A flywheel velocity controller inherited from last season, with a plot in {@code plots/} of what
 * it actually does today. {@link #kV} and {@link #kP} are the entire fixture — edit them, run
 * {@code ./gradlew test}, and see whether the plot in {@code plots/} would change the way you
 * expect. Do not edit anything else in this file or {@link T4FlywheelPlant}.
 */
public final class BadlyTunedFlywheel {
  private static final double STEP_SECONDS = 0.02;

  /** Volts commanded per rotation/second of target velocity. Edit this. */
  private double kV = 0.0416;

  /** Volts commanded per rotation/second of velocity error. Edit this. */
  private double kP = 1.5;

  private final T4FlywheelPlant plant = new T4FlywheelPlant();
  private double targetRotationsPerSecond;
  private double appliedVolts;

  /** Sets the velocity this controller drives toward. */
  public void setTarget(double rotationsPerSecond) {
    targetRotationsPerSecond = rotationsPerSecond;
  }

  /** Runs exactly one 20&nbsp;ms iteration: measure, compute, command, then step the plant. */
  public void update() {
    double measured = plant.velocityRotationsPerSecond();
    double feedforward = kV * targetRotationsPerSecond;
    double feedback = kP * (targetRotationsPerSecond - measured);
    appliedVolts = MathUtil.clamp(feedforward + feedback, -12.0, 12.0);
    plant.setVoltage(appliedVolts);
    plant.step(STEP_SECONDS);
  }

  /** The plant's velocity as of the most recent {@link #update()}. */
  public double velocityRotationsPerSecond() {
    return plant.velocityRotationsPerSecond();
  }

  /** The voltage most recently commanded. */
  public double appliedVolts() {
    return appliedVolts;
  }
}
