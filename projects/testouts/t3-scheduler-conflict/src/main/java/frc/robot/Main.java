package frc.robot;

import edu.wpi.first.wpilibj.RobotBase;

/** Do not edit. Hands control to {@link Robot}. */
public final class Main {
  private Main() {}

  public static void main(String... args) {
    RobotBase.startRobot(Robot::new);
  }
}
