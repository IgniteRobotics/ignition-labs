# M6 — Simulation, Telemetry & Testing

## At a glance

How you find out your code is wrong before the robot tells you.

This milestone is the convergence point of the whole curriculum. **Every track completes it — there is no test-out.** It is also the gate to M7: you do not get agentic AI tooling until you have a verification layer.

Required before you commit to competition robot code.

**Estimated time:** 4–5 sessions
**Prerequisites:** M5 (or a passed test-out through M5, for Track D)
**AI posture:** 🟡 Yellow *during* this milestone. 🟢 Green unlocks on completion

## Who enters here

**Track D enters here** — FRC veterans, after testing out of M0 through M5.

**Tracks A, B, C** arrive having worked M5.

**Everyone completes this milestone.** No exceptions, including for veterans with three seasons of experience. See below for why.

## Test-out challenge

**There is none.** M6 cannot be tested out of.

This is deliberate and it is the most opinionated decision in the curriculum, so here is the reasoning in full.

Ignite's competition repositories currently contain, between them, **zero test files.** `Robot-2026` has JUnit 5 configured in `build.gradle` with a working `test { useJUnitPlatform() }` block and no `src/test` directory at all. `Robot-2025` has none. `Robot-2024` had exactly one, with four hardcoded assertions. Neither CI workflow runs `./gradlew test`, so even if tests existed they would not run. `Phoenix-2026` — a rig that exists specifically for simulated hardware work — has no tests either.

That is not a criticism of anyone. It is the normal state of FRC code and it is where most teams sit. But it means the team's most experienced programmers are precisely the people who have never written a robot test, and they are the ones who most want the AI tooling in M7. Handing agentic tools to a codebase with no verification layer produces velocity with no brakes.

So veterans test out of M1 through M5 in an afternoon, and then they write tests like everyone else.

## Concepts

### Simulation

WPILib ships a desktop simulator. Robot code runs on your laptop with no hardware at all — you get a Driver Station, joysticks, and a field view.

**This is not a lesser version of testing on the robot.** It is available at 2am, it is available when the robot is disassembled, it is available to every student simultaneously, and it cannot destroy a gearbox. Every project in this curriculum is simulation-mandatory for those reasons.

**What simulation models:** motor behavior through physics models like `DCMotorSim`, encoder feedback, battery voltage sag under load, and your full command scheduler.

**What it does not model:** the thing that actually breaks. Wiring faults, a chain skipping, a CAN wire vibrating loose, a mechanism binding at one specific angle. Simulation proves your logic. It does not prove your robot.

### Telemetry: Epilogue

**Ignite already runs Epilogue in production.** This is not aspirational; you are learning an existing team convention.

```java
@Logged
public class ShooterSubsystem extends SubsystemBase {

  @Logged(name = "Velocity Target rads/s", importance = Importance.CRITICAL)
  private AngularVelocity velocityTarget;
}
```

`Robot.java` calls `Epilogue.configure(...)` and `Epilogue.bind(this)`, and thirteen files across the codebase carry `@Logged` annotations.

**How it works:** annotate a class with `@Logged` and its fields get logged automatically every cycle. `Importance` levels let you control verbosity — `CRITICAL` values always log; lower-importance ones can be filtered.

**What to log.** Setpoints and measured values together, always — a velocity target without the actual velocity tells you nothing. Command names (this is why `.withName()` in M3 matters). State machine states. Anything you would want to know after a match you cannot re-run.

**What not to log.** Everything. Log bandwidth is finite and a log you cannot navigate is a log you will not read.

### AdvantageScope

The tool for reading logs. Plot signals over time, view field position, scrub through a match.

**The skill this milestone builds is diagnosis from a log**, not from watching the robot. At a competition you get one match, it lasts two and a half minutes, and you cannot reproduce it. The log is all you have.

Practice on real logs from last season — `Robot-2026/logs/` has hundreds.

### Unit testing

JUnit 5 is already available in every WPILib Java project. It is already configured in Ignite's `build.gradle`. Nothing needs installing.

```java
public class SensorProfileTest {
  private SensorProfile profile;

  @BeforeEach
  void setup() {
    profile = new SensorProfile(2048, 6.75);
  }

  @Test
  void ticksToRotations_appliesGearRatio() {
    assertEquals(1.0, profile.ticksToMechanismRotations(2048 * 6.75), 1e-6);
  }
}
```

**Note the tolerance argument.** Comparing doubles exactly fails — M1's lesson, arriving in test form.

**What is worth testing.** Pure logic: unit conversions, interpolation, state machine transitions, "is it at setpoint" checks, launch calculations. Ignite's `LaunchCalculator` and `SensorProfile` are near-perfect test targets — real math, no hardware.

**What is not worth testing.** Whether CTRE's simulated motor behaves like CTRE says it does. That tests CTRE, not you. A caution from the community that is worth taking seriously: over-relying on simulated-motor behavior in tests can "defeat the purpose" if the test just re-asserts the simulator's own model.

### Two pitfalls to know before you write your first test

**1. Do not instantiate the whole robot in a test.**

A WPILib maintainer's guidance is direct: `RobotBase` — and therefore `TimedRobot` — "is not designed to ever exit." A test that constructs the full robot leaves background threads running (`robot main`, `DataLogDS`, `NTListener`, `MotorSafety`), the JVM never exits, and CI hangs until it times out. A real team hit exactly this and spent significant time on it.

Test scoped pieces of logic, not the robot.

**2. CTRE simulation has documented timing quirks.**

Testing Phoenix devices in simulation requires specific setup that is not obvious and will waste an afternoon if you do not know it:

- Enable the robot: `DriverStationSim.setEnabled(true)` then `DriverStation.notifyNewData()`
- Wait **~100ms** after constructing devices and enabling, before commanding actuators
- Wait **~20ms** after sending a control request, before asserting on the result
- Use `StatusSignal.waitForUpdate()` or `BaseStatusSignal.waitForAll()` rather than sleeping arbitrarily

These are documented in CTRE's own unit-testing guide. Read it before you start.

### AutoCloseable

WPILib recommends subsystems implement `AutoCloseable` so tests can tear down hardware between cases. Without it, the second test in a file may fail because the first one's simulated devices still hold their ports.

### The bar for this milestone

**Deliberately low, and set that way on purpose.**

Ignite is starting from zero tests. The goal is not to convert anyone to test-driven development. A widely-endorsed caution from the FRC community is worth quoting directly, because it describes exactly the trap a team in our position falls into:

> "There's a lot of hype around tdd in certain circles as the one true way... It's a trap for a lot of new folks because they see the hype, hate the process, and end up avoiding writing good tests (or any)... Really I'm trying to say 'just do what you can.'"

**What success looks like here:** you have written a test that caught a real regression. That is it. Three good tests beat an abandoned attempt at full coverage.

## Project

**Three parts.**

**Part 1 — test a subsystem's logic.**
1. Pick a subsystem with real logic. `LaunchCalculator` and `SensorProfile` are excellent targets
2. Write at least three JUnit tests against it
3. Make them run: `./gradlew test` green locally
4. **Introduce a deliberate regression** — flip a sign, drop a gear ratio. Watch the test fail
5. Fix it. Watch it pass

Part 1 step 4 is the point of the whole exercise. A test you have never seen fail is not yet known to work.

**Part 2 — test against simulated hardware.**
1. Write one test that constructs a simulated Phoenix device and asserts on its behavior
2. Get the timing right using CTRE's documented setup
3. Make the subsystem `AutoCloseable` and confirm two tests in the same file both pass

**Part 3 — diagnose from a real log.**
1. Open a real log from `Robot-2026/logs/` in AdvantageScope
2. Answer a specific question a mentor gives you — did the shooter reach setpoint before it fired? did the auto routine follow its path? when did the intake stall?
3. Show the evidence, not just the answer

## Definition of done

- [ ] At least three passing JUnit tests exist against real subsystem logic
- [ ] `./gradlew test` runs green locally
- [ ] You demonstrated a test failing on an introduced regression, then passing after the fix
- [ ] One test runs against a simulated Phoenix device with correct timing
- [ ] A subsystem implements `AutoCloseable` and two tests in one file both pass
- [ ] You answered a diagnostic question from a real match log, with evidence
- [ ] You added `@Logged` to something and saw it appear in the log
- [ ] You can explain why not to instantiate `TimedRobot` in a test
- [ ] You can explain the difference between what simulation proves and what it does not

## Common failure modes

**Testing the simulator instead of your code.** If your assertion would pass regardless of what you wrote, you tested CTRE.

**Instantiating the whole robot.** Hangs CI. See above.

**Skipping the CTRE timing delays.** Tests fail intermittently, which is worse than failing consistently — you will assume your code is flaky when your test is.

**Exact double comparison.** `assertEquals(1.0, result)` without a tolerance. Fails on floating-point noise.

**Writing tests that never fail.** A test written after the code, that passed the first time and has never been seen red, is unverified. Break it on purpose once.

**Logging everything.** A log with four hundred signals is a log nobody opens.

**Logging a setpoint without the measurement.** Half the information, and the useless half.

**Chasing coverage.** Three tests on `LaunchCalculator` are worth more than thirty on getters.

## AI posture — 🟡 Yellow during, 🟢 Green after

**Yellow applies while you are working this milestone.** The tests that unlock Green are written by you. You do not use the agent to build the safety net that makes the agent safe.

**Allowed:** explaining JUnit or AdvantageScope · Javadoc · triaging a failing test's stack trace

**Not allowed:** generating your tests, deciding what to test

**On completion**, your tier becomes Green and M7 opens. See `ai-use-standard.md` §4.

## Resources

See `resources.md` for the full library. For this milestone:

- **WPILib: Unit Testing** — JUnit 5, the `AutoCloseable` pattern
- **WPILib: Robot Simulation Introduction** — running without hardware
- **WPILib: Robot Telemetry with Annotations** — Epilogue, `@Logged`, `Importance`
- **CTRE: Unit Testing** — **read before your first Phoenix test.** The timing quirks are here
- **CTRE: Simulation Introduction** — driving a physics model from motor voltage
- **AdvantageScope** — log analysis
- **Chief Delphi: JUnit tests hanging in GitHub Action** — the `TimedRobot`-never-exits trap, explained by a WPILib maintainer
- **Chief Delphi: No, You Don't Need A Robot To Test Your Code** — where this milestone's "just do what you can" bar comes from
- **Ignite:** `Robot-2026/build.gradle` — JUnit, configured and unused. Your starting point
- **Ignite:** `Robot-2026/.../Robot.java` — Epilogue configuration in production
- **Ignite:** `Robot-2026/.../statemachines/LaunchCalculator.java` — the best test target in the codebase
- **Ignite:** `Robot-2026/logs/` — hundreds of real logs for Part 3
