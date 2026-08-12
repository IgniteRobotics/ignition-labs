# Task 4: The diagnostic log, `QUESTIONS.md`, and the project docs

Part 3 of M6 is *diagnose from a real log* — the skill that matters when you get one match, it lasts two and a half minutes, and you cannot reproduce it. Spec §9.7's pass criterion is the correct answer **with evidence**, not the answer alone.

**This is the largest authoring cost in the task.** A question whose answer needs a signal the log does not carry is unanswerable, and nobody discovers that until a student is stuck in front of AdvantageScope with a mentor who also cannot answer it. So the log gets parsed and the answer established **before** the question is written — Step 4 is the gate, and writing `QUESTIONS.md` before it has passed is the failure this task is arranged to prevent.

**A defect this task works around.** `Robot-2026/.gitignore:177` ignores `logs/`. **Zero logs are tracked** — the 274 log directories exist only on one machine, and a student who forks and clones gets none. Two claims in `docs/programming/milestones/m6-simulation-telemetry-testing.md` (lines 74 and 215) tell students otherwise and are false. M6 ships its own log; Task 9 corrects the claims. `Robot-2026` itself is not modified by this plan.

**Files:**
- Create: `projects/m6-testing/logs/<chosen>.wpilog`
- Create: `projects/m6-testing/logs/QUESTIONS.md`
- Create: `projects/m6-testing/README.md`
- Create: `projects/m6-testing/READING.md`
- Scratch only, never committed: a throwaway WPILog reader

**Interfaces:**
- Consumes: Task 1's scaffold.
- Produces: a committed log, a question set proven answerable from it, and the **numeric evidence trail** Task 7 turns into `answer-keys/m6-log-diagnosis.md`.

**Lane:** `milestone`. Worktree `.worktrees/m6`.

---

- [ ] **Step 1: Shortlist candidate logs**

Only logs carrying the flywheel signals can support a shooter question, and only small ones belong in a git repository.

```bash
cd /Users/y0shi/workspace/ignite/Robot-2026/logs
for f in *.wpilog; do
  if strings -n 8 "$f" | grep -q 'Launch/Flywheel Velocity'; then
    printf '%s %s\n' "$(stat -f%z "$f")" "$f"
  fi
done | sort -n
```

Known candidates from the 2026-08-11 survey, by size: `FRC_20260303_003102.wpilog` (607 KB), `FRC_20260303_033012.wpilog` (893 KB), `FRC_20260303_034055.wpilog` (1.19 MB), `FRC_20260303_033623.wpilog` (1.83 MB), `FRC_20260302_205606.wpilog` (3.26 MB). Prefer the smallest that survives Step 4. Anything above ~4 MB is rejected on repository-weight grounds regardless of content.

- [ ] **Step 2: Write a throwaway WPILog reader**

Put it in the scratchpad. **It is not committed** — it is a measuring instrument, and `tools/` is for things CI runs.

The format is documented at `https://github.com/wpilibsuite/allwpilib/blob/main/wpiutil/doc/datalog.md`. The parts you need:

- **Header:** ASCII `WPILOG`, then `u16` version (little-endian, `0x0100`), then `u32` extra-header length and that many bytes.
- **Records:** a header bitfield byte, then entry ID, payload size, and timestamp — each of length `((bitfield >> 0) & 0x3) + 1`, `((bitfield >> 2) & 0x3) + 1`, and `((bitfield >> 4) & 0x7) + 1` bytes respectively, all little-endian unsigned. Then `payload size` bytes of payload.
- **Control records** have entry ID `0`. Payload byte `0` is Start: `u32` entry ID, then three length-prefixed (`u32`) UTF-8 strings — name, type, metadata. Type `1` is Finish, type `2` is Set Metadata.
- **Data records** carry the payload for their entry ID. `double` is 8 bytes little-endian; `int64` is 8 bytes; `boolean` is 1 byte; `string` is raw UTF-8.
- **Timestamps are microseconds** since log start.

Write it to emit, for a named entry, a `(timestamp_seconds, value)` series. Verify it against a signal whose shape you can sanity-check — a `boolean` like an enabled flag should be flat with a few transitions, not noise. **A parser you have not sanity-checked is a parser that will invent your answer for you.**

- [ ] **Step 3: Find a moment worth asking about**

Extract these series from each shortlisted log:

- `NT:/SmartDashboard/Launch/Flywheel Velocity (Rotations Per Second)` — the measurement
- `NT:/Preferences/Shooter/Launch Speed` — the setpoint
- `NT:/SmartDashboard/Launch Request/Look Ahead Target Distance` — distance at launch
- `NT:/SmartDashboard/Launch/Hood Target (Rotations)` — the other half of the launch state

Look for a window where the measured velocity and the setpoint **diverge and then converge**, and where something else in the log marks a launch attempt. The shape you want is: the answer is determinable, and a plausible wrong answer is also available — a question with only one visible possibility teaches nothing.

Print the exact timestamps and values you find. Those numbers are the evidence trail.

- [ ] **Step 4: Prove the questions are answerable — before writing them**

For each question you intend to ask, write down:

1. The answer.
2. The **signal names and timestamp range** that prove it.
3. What a student who got it wrong would most plausibly have said, and which signal rules that out.

If you cannot fill in all three from the log's actual contents, **the question is unanswerable and does not ship.** Go back to Step 3 and find a different moment, or a different log.

Paste the filled-in table into your task report. Task 7 turns it into the answer key; a key written later from memory is a key that is wrong.

- [ ] **Step 5: Copy the log in**

```bash
cd /Users/y0shi/workspace/ignite/docs
mkdir -p projects/m6-testing/logs
cp /Users/y0shi/workspace/ignite/Robot-2026/logs/<chosen>.wpilog projects/m6-testing/logs/
ls -la projects/m6-testing/logs/
```

`projects/` sits outside `docs_dir`, so MkDocs cannot read it — the log is structurally unpublishable and needs no exclusion rule.

- [ ] **Step 6: Write `logs/QUESTIONS.md`**

Two questions. Each states what to open, what to answer, and that evidence is required. **No answers, and no hints that narrow the search to one signal** — finding the right signal is half the skill.

Create `projects/m6-testing/logs/QUESTIONS.md`:

```markdown
# Diagnose it from the log

One real log from a 2026 practice session. Open it in AdvantageScope.

You get one match. It lasts two and a half minutes. You cannot re-run it, and nobody
filmed the part you needed. The log is all you have — which is why this is a skill and
not a formality.

## The rules

**Show the evidence, not just the answer.** "The shooter was below setpoint" is not an
answer. "Between t=<x> and t=<y> the measured flywheel velocity sat <n> rps under the
commanded value, and here is the plot" is an answer.

**Rule out the alternative.** For each question there is a plausible wrong explanation.
Say what it is and name the signal that excludes it. An answer that happens to be right
without ruling anything out does not pass — a mentor cannot tell it apart from a guess.

## Question 1

Find a moment in this log where the shooter was commanded to launch.

**Did the flywheel reach its commanded velocity before the launch, or not?**

Give the timestamp, the commanded value, the measured value, and how you know which came
first.

## Question 2

Somewhere in this log the launch distance the robot was aiming for and the distance it was
actually at are not the same number.

**Find that, and explain why the two differ.**

This one is not a bug. Something in the code is doing it on purpose, and the sandbox class
you have been writing tests against does the same thing. Say what and why.

## How this is graded

A mentor asks you to walk through it on screen. Bring the plot. Expect to be asked "how do
you know it wasn't <the other thing>" — that question is the whole exercise.
```

Question 2 is deliberately tied back to `LaunchProfile.lookAheadDistanceMeters`, so the log work and the test work reinforce rather than sit in separate compartments. Confirm in Step 4 that the log actually shows the look-ahead divergence before shipping this question; if it does not, replace it with a second question drawn from what the log does contain.

- [ ] **Step 7: Write `README.md`**

Create `projects/m6-testing/README.md`:

```markdown
# M6 — Simulation, Telemetry & Testing

How you find out your code is wrong before the robot tells you.

This project is the **warm-up**. The deliverable for this milestone is a pull request
against `Robot-2026` that adds tests to a class from
[`UNTESTED-TARGETS.md`](../../docs/programming/UNTESTED-TARGETS.md). This sandbox is where
you learn the moves first, on code where a mistake costs nothing.

---

## This one is different: green is the starting line

Every project before this one arrived red, and your job was to make it pass.

**This one arrives green.** `LaunchProfile` and `ShooterFlywheel` are written, correct, and
not yours to change. There is exactly one test in `src/test/`, and it already passes.

Your job is the tests. That is the entire milestone, and it is why the bar is set where it
is: **you have written a test that caught a real regression.** That is it. Three good tests
beat an abandoned attempt at full coverage.

---

## What you are given

| Given | Why |
|---|---|
| `plant/LaunchProfile.java` | The practice target. Real launch math, no hardware. Read it — you cannot test a contract you have not read |
| `plant/ShooterFlywheel.java` | One motor, and the reason `AutoCloseable` matters |
| `src/test/.../WorkedExampleTest.java` | The one worked example. Read it, then write your own. **Do not edit it** |
| `src/test/.../support/PhoenixTestSupport.java` | The CTRE simulation timing setup. It stays here — see below |
| `regressions/` | Deliberate one-line breaks. [`regressions/README.md`](regressions/README.md) |
| `logs/` | One real match log and two diagnostic questions |

Yours: **every other test in `src/test/`, and every test in your `Robot-2026` pull
request.**

---

## The three parts

**Part 1 — test the logic.** Write at least three tests against `LaunchProfile`. Then apply
a regression patch, watch your test fail, revert it, watch it pass. Step 4 there is the
point of the whole exercise: a test you have never seen fail is not yet known to work.

**Part 2 — test against simulated hardware.** One test against `ShooterFlywheel` using
`PhoenixTestSupport`. Then write a second test in the same file and confirm both pass —
that is what `AutoCloseable` buys you.

**Part 3 — diagnose from a log.** [`logs/QUESTIONS.md`](logs/QUESTIONS.md).

---

## Running it

```bash
./gradlew test                              # your tests
git apply regressions/flip-sign.patch       # break it
./gradlew test                              # your tests should now fail
git apply -R regressions/flip-sign.patch    # put it back
```

If your tests stay green with a patch applied, they are not testing the thing that broke.
That is a finding, not a failure.

---

## `PhoenixTestSupport` stays here

It never goes into `Robot-2026`. Tests you add to the season repository are subject to the
pure-logic hard rule: nothing that depends on timing, simulation, or hardware. A flaky test
here costs you an afternoon. A flaky test there blocks the team's pull requests during
competition season.

---

## Done when

Mechanically checked:

- [ ] At least three of your own passing tests against `LaunchProfile`
- [ ] `./gradlew test` green
- [ ] Your tests fail under a regression patch and pass again when it is reverted
- [ ] One test against `ShooterFlywheel` with correct Phoenix timing
- [ ] Two tests in one file both pass
- [ ] A merged pull request on `Robot-2026`, tests pure-logic, target from `UNTESTED-TARGETS.md`

Mentor verifies:

- [ ] Both log questions answered **with evidence**, and the alternative ruled out
- [ ] You added `@Logged` to something and saw it appear in the log
- [ ] You can explain why not to instantiate `TimedRobot` in a test
- [ ] You can explain the difference between what simulation proves and what it does not

Completing this unlocks 🟢 Green and M7.
```

- [ ] **Step 8: Write `READING.md`**

Create `projects/m6-testing/READING.md`:

```markdown
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
```

- [ ] **Step 9: Verify the docs' links and the project still builds**

```bash
cd /Users/y0shi/workspace/ignite/docs
export JAVA_HOME=~/wpilib/2026/jdk
(cd projects/m6-testing && ./gradlew test --console=plain); echo "rc=$?"    # expect 0
grep -rn '2025-Robotics-Intro\|Robot-2025\|/blob/main/\|/blob/master/' projects/m6-testing/*.md projects/m6-testing/logs/*.md
```

The grep must return nothing: no pre-2026 Ignite references, and every real-repo link pinned to a SHA rather than a branch.

- [ ] **Step 10: Confirm the scratch parser did not sneak in, then commit**

```bash
git status --short     # only logs/, README.md, READING.md
git add projects/m6-testing/logs projects/m6-testing/README.md projects/m6-testing/READING.md
git commit -m "Add M6's diagnostic log, questions, and project docs

Robot-2026 gitignores logs/, so the curriculum's 'practice on real logs from
Robot-2026/logs' claim is false for anyone who clones it. M6 ships its own.

Both questions were answered from the shipped log before they were written;
the evidence trail goes to the solutions repository in Task 7."
```
