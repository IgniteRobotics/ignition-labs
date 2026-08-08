package frc.robot;

import edu.wpi.first.wpilibj.RobotBase;

/**
 * Do not modify this file. WPILib calls main() to start the robot program, and the roboRIO's
 * startup scripts expect this exact class.
 */
public final class Main {
  private Main() {}

  public static void main(String... args) {
    RobotBase.startRobot(Robot::new);
  }
}
