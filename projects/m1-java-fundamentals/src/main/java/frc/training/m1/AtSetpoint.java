package frc.training.m1;

/** Exercise 4 — the at-setpoint check. */
public final class AtSetpoint {

  private AtSetpoint() {}

  /**
   * Reports whether a measured value is close enough to its target.
   *
   * <p>This is how "is the shooter at speed?" gets answered in real robot code. Floating-point
   * arithmetic is inexact, so closeness is a tolerance question and never an equality question.
   *
   * <p>Direction does not matter: overshooting the target by less than the tolerance is just as
   * much "at setpoint" as undershooting it by the same amount.
   *
   * @param current the measured value
   * @param target the value being aimed at
   * @param tolerance the largest acceptable difference, non-negative; a difference exactly equal to
   *     it still counts as at setpoint
   * @return true when {@code current} is within {@code tolerance} of {@code target}
   */
  public static boolean isAtSetpoint(double current, double target, double tolerance) {
    throw new UnsupportedOperationException("TODO");
  }
}
