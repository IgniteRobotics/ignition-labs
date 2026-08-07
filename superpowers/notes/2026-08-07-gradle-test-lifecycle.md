# Gradle test-lifecycle probe — 2026-08-07

**Verdict: YES** — `./gradlew build` invokes `test` under GradleRIO's `configureTestTasks` (GradleRIO 2026.2.1, Gradle wrapper 8.11, JUnit 5.10.1): a probe test that always fails (`ProbeTest.thisTestRanWhichIsThePointOfTheProbe`, added to a `src/test`-less copy of Phoenix-2026 at `/tmp/gradle-probe`) made both `./gradlew build` and `./gradlew test` fail identically with the same assertion.

```
$ cd /tmp/gradle-probe && JAVA_HOME=/Users/y0shi/wpilib/2026/jdk ./gradlew build --console=plain
...
> Task :test FAILED

ProbeTest > thisTestRanWhichIsThePointOfTheProbe() FAILED
    org.opentest4j.AssertionFailedError at ProbeTest.java:10

1 test completed, 1 failed
...
FAILURE: Build failed with an exception.
> Execution failed for task ':test'.
BUILD FAILED in 12s
9 actionable tasks: 9 executed

$ cd /tmp/gradle-probe && JAVA_HOME=/Users/y0shi/wpilib/2026/jdk ./gradlew test --console=plain
> Task :test FAILED

ProbeTest > thisTestRanWhichIsThePointOfTheProbe() FAILED
    org.opentest4j.AssertionFailedError at ProbeTest.java:10

1 test completed, 1 failed

FAILURE: Build failed with an exception.
> Execution failed for task ':test'.
BUILD FAILED in 749ms
7 actionable tasks: 1 executed, 6 up-to-date
```
