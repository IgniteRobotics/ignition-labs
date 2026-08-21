# Track D — FRC Veteran, Leveling Up With AI

**This track is for you if:** you have programmed for Ignite (or another FRC team) for at least a season. You have shipped autonomous routines, tuned mechanisms, and debugged something at a competition. You want to use AI tooling seriously.

You are going to disagree with where this track starts. Read the next section before deciding it is wrong.

---

## You enter at M6, not M7

Your instinct is that M7 is the milestone you want and M6 is a formality you can skip. Here is the actual reasoning, so you can argue with it rather than just comply.

**The state of our repositories, measured rather than asserted:**

- `Robot-2026` has JUnit 5 configured in `build.gradle` with a working `test { useJUnitPlatform() }` block, and **no `src/test` directory at all**
- `Robot-2025`: zero test files
- `Robot-2024`: exactly one, with four hardcoded assertions and no derivation comments
- `Phoenix-2026` — a rig that exists specifically for simulated hardware work — has no tests
- **CI already gates on tests** — `pr-build-check.yml` runs `./gradlew build`, and `build` → `check` → `test`. There are none to run, so the gate has never fired. The first test you merge is enforced from that moment on

That is not a criticism of anyone, and it is roughly where most FRC teams sit. But it produces a specific situation: the people on this team with the most experience are also the people who have never written a robot test. And those are the people who most want agentic AI tooling.

**Agentic tools produce code faster than you can review it by reading.** That is the entire value proposition. It is also why the thing standing between you and shipping a subtle bug cannot be your own careful reading — it has to be automated. Without tests you have velocity and no brakes.

So: you test out of M0 through M5 in an afternoon. Then you write tests like everyone else. Then M7 opens.

**M6 has no test-out.** Not for you either.

---

## Your path

| Milestone | Your move | Time |
|---|---|---|
| **[M0](../milestones/m0-tooling-and-git.md)** | Test out — show a merged PR, run `./gradlew build` | 15 min |
| **[M1](../milestones/m1-java-fundamentals.md)** | Test out | 30 min |
| **[M2](../milestones/m2-oop-interfaces-lambdas.md)** | Test out | 45 min |
| **[M3](../milestones/m3-wpilib-command-based.md)** | Test out | 60 min |
| **[M4](../milestones/m4-sensors-actuators-control.md)** | Test out | 60 min |
| **[M5](../milestones/m5-autonomous-odometry-vision.md)** | Test out | 60 min |
| **[M6 — Simulation, Telemetry & Testing](../milestones/m6-simulation-telemetry-testing.md)** | **Work it. No test-out exists** | 4–5 sessions |
| **[M7 — AI-Assisted Development](../milestones/m7-ai-assisted-development.md)** | Work it | 2–3 sessions |
| **[M8](../milestones/m8-in-season-deliverables.md)** | The season | Ongoing |

**Test-outs in one sitting:** about four hours. Do them back to back.

---

## The test-outs are not trivia

They are diagnostic, and a couple of them are genuinely worth your time.

**M3** — add a command requiring two subsystems, bind it, and explain what happens if it is scheduled while another command already requires one of them. The answer is that the running command is interrupted and its `end(true)` fires. If you have been writing FRC code without a precise model of the scheduler, this is where you find out.

**M4** — a simulated flywheel with deliberately wrong gains. Diagnose from the plot, fix it, and explain why the original tuning was fragile. **This is the one most veterans have to think about**, because a lot of working FRC code is exactly this shape: feedback tuned to paper over a model nobody characterized, at one setpoint on a full battery.

**M5** — an auto that drifts. Determine from logs whether the cause is odometry drift, a mistuned path controller, or a blocking named command, *and* what rules out the other two. Guessing right without evidence does not pass — the skill is log-driven diagnosis.

If you fail one, work that milestone. That is information, not a verdict.

---

## M6 is not a formality — here is what is actually in it

Even granting that you should write tests, there is real content here you probably have not hit.

**The `TimedRobot` trap.** `RobotBase` is not designed to ever exit. Instantiate the robot in a test and background threads keep running, the JVM never exits, and CI hangs until timeout. A WPILib maintainer had to explain this to a team that lost real time to it. Test scoped logic, not the robot.

**CTRE's simulation timing.** Testing Phoenix devices requires `DriverStationSim.setEnabled(true)` plus `DriverStation.notifyNewData()`, a ~100ms delay after constructing devices, and a ~20ms delay after sending a control request before asserting. Skip these and your tests fail intermittently, which is worse than failing consistently.

**Epilogue.** You are already using it — thirteen files carry `@Logged`, and `Robot.java` calls `Epilogue.configure()` and `Epilogue.bind(this)`. What M6 adds is using it deliberately: setpoint *and* measurement together, importance levels that mean something, and diagnosing a match from a log instead of from memory.

**Log diagnosis.** `Robot-2026/logs/` has 274 real logs. M6's third project is answering a specific question about a real match from the log alone. If you have been debugging by re-running the robot, this is a genuinely different skill and it is the one that matters at competition, where you get one shot and cannot reproduce anything.

**The bar is low on purpose.** Three tests that catch a real regression. Not coverage, not TDD conversion. The community framing this milestone adopts is that perfect testing practice is a trap for a team with none — "just do what you can."

---

## What M7 actually gives you

Not permission. You could already use these tools. What M7 gives you is calibration and a framework for defending the output.

**The line that matters:** interface-level trust versus decision-level trust. You do not defend CTRE's internals — you defend that you chose the right control request for the right reason. You *do* defend AI-generated control logic, because that is an engineering decision, not a dependency.

**The counter-argument you should be able to answer**, because a student will make it to you: calling `.set()` into 50,000 lines of closed-source vendor firmware and calling it "student-written" is already a compromise, so AI is a difference of degree, not kind. That argument is good. `../ai-use-standard.md` §3 answers it. Read it before you have to.

**Named failure modes** — shepherding and drifting — that you have almost certainly done and not had words for.

**The defense review**, which is the actual mechanism: you walk someone through every decision in the diff. Anything you cannot defend comes out. That is the only check that cannot be gamed by a tool.

---

## If you are a lead

Two additional things.

**You set the tier ceiling by example.** If students see you shipping AI-generated code without disclosure or tests, the standard is dead regardless of what the document says. The version of this that fails is not students ignoring the rules; it is leads exempting themselves.

**Competition week is when this matters most and gets followed least.** Deadline pressure is exactly the condition under which someone ships code they cannot explain, and it is exactly when nobody has time to debug code nobody understands. The standard does not relax at events.

Also read `../ai-use-standard.md` §10, which is written for mentors but applies to you: the mentor-takeover problem and the AI-takeover problem are the same failure wearing two faces.

---

## Worth your time even though you could skip it

Three things you can read in twenty minutes that are probably new:

**[`coding-standards.md`](../coding-standards.md)** — the team's actual coding standards. Superstructure pattern, type-safe units, command factory methods, homing state machines, current limiting, fault detection. It is good, and **most of it is not implemented in the codebase it governs.** It was pulled out of `Robot-2026/.github/prompts/code-review-prompt.md`, which is now downstream of it: the document is the source, the bot enforces it. You are handed both at M7.

**`DriveMotorConfigs.java`** — four near-identical `create*Slot0Configs()` methods, one per swerve corner, each rebuilding the same five-field config. CTRE's `.clone()` pattern collapses them. Assigned as an M4 stretch project; it is a twenty-minute PR and a good one.

**Our CAN signal handling** — `Robot-2026` contains zero uses of `refreshAll`, `waitForAll`, or `optimizeBusUtilization`. Everything relies on CTRE's generated swerve defaults. Not broken. Not deliberate either.

---

## Start here

Book an afternoon. Run the M0–M5 test-outs back to back with a mentor. Then **[M6](../milestones/m6-simulation-telemetry-testing.md)**.

If you think the M6 gate is wrong, make the argument — `../curriculum-spec.md` §2.2 is where the decision is recorded and it was written to be argued with. Bring evidence rather than preference, and if you are right it should change.
