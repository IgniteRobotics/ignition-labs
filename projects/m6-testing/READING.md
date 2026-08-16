# Reading for M6

Read these before you pick a pull-request target. All links are pinned to a commit so what
you read is what was there.

## The real version of what you have been testing

[`statemachines/LaunchCalculator.java`](https://github.com/IgniteRobotics/Robot-2026/blob/28ce5397d88275001d656dbb717d22cf9ac0150d/src/main/java/frc/robot/statemachines/LaunchCalculator.java)

`LaunchProfile` is this class with the robot taken out. Look for the interpolating
time-of-flight maps and the phase-delay pose prediction — you have written tests against
simplified versions of both.

Then notice what makes it hard to test: it is a singleton, and it reaches
`DriveState.getInstance()` for the robot's pose and speeds. **That is the lesson.** The math
is testable; the way it is reached is not. When you pick a target, you are partly picking
how much of that you are willing to work around.

## The two builders

[`MappedLaunchRequestBuilder.java`](https://github.com/IgniteRobotics/Robot-2026/blob/28ce5397d88275001d656dbb717d22cf9ac0150d/src/main/java/frc/robot/subsystems/shooter/MappedLaunchRequestBuilder.java)
· [`ParabolicLaunchRequestBuilder.java`](https://github.com/IgniteRobotics/Robot-2026/blob/28ce5397d88275001d656dbb717d22cf9ac0150d/src/main/java/frc/robot/subsystems/shooter/ParabolicLaunchRequestBuilder.java)

Two implementations of one interface — the M2 lesson, in production. Real math, no hardware,
and not one line of test coverage today. These are the best targets in the codebase.

## Where the tests would go

[`build.gradle`](https://github.com/IgniteRobotics/Robot-2026/blob/28ce5397d88275001d656dbb717d22cf9ac0150d/build.gradle)

JUnit 5 is already configured. `test { useJUnitPlatform() }` is already there. There is no
`src/test` directory at all.

Nothing needs installing. The tests are simply not written, and CI already runs
`./gradlew build`, which runs `check`, which runs `test`. **Your pull request does not just
add tests — it switches on test gating for every team pull request from that moment
forward.** That is the strongest sentence available about this milestone, and it is why it
gates M7.
