# Task 5: Reference solutions, authoring notes, and the cross-check

Spec §11.2 is blunt about this: a milestone shipped without its solution is not shipped. Tasks 2 and 4 already produced working implementations and left them uncommitted in the solutions repository's working tree; this task reviews them, writes the notes a future maintainer needs, and runs the cross-check that keeps all of it true next month. Everything here happens in **`../ignite-training-solutions`**, a different repository — nothing in this task touches `ignition-labs`.

**Files** (all under `/Users/y0shi/workspace/ignite/ignite-training-solutions/`):
- Create: `solutions/m3-command-based/src/main/java/frc/robot/RobotContainer.java` (already on disk from Task 2 — review and keep)
- Create: `solutions/m3-command-based/src/main/java/frc/robot/subsystems/DrivetrainSubsystem.java`, `ElevatorSubsystem.java` (already on disk)
- Create: `solutions/m3-command-based/src/main/java/frc/robot/commands/DriveForSeconds.java` (already on disk)
- Create: `solutions/testouts/t3-scheduler-conflict/src/main/java/frc/robot/RobotContainer.java` (already on disk from Task 4)
- Create: `authoring-notes/m3.md`
- Create: `answer-keys/t3-scheduler-conflict.md`
- Modify: `README.md` — the coverage table

**Consumes:** from Task 2, the M3 control implementation with `DrivetrainSubsystem.driveWithSticks(DoubleSupplier, DoubleSupplier)`, `DrivetrainSubsystem.arcade(double, double)`, `ElevatorSubsystem.raiseCommand()`, `ElevatorSubsystem.stopCommand()`, `DriveForSeconds(DrivetrainSubsystem, double)`, and a `RobotContainer` implementing all five `M3Deliverables` members. From Task 4, the T3 `RobotContainer` with `combinedCommand()` implemented and the binding added.

**Produces:** a green `tools/cross-check.sh`. Nothing else consumes this task.

**Lane:** tail. Single owner, on the merged feature branch, after both lanes have landed **and been committed** — `cross-check.sh` runs `git archive` against the labs repository at `HEAD`, so anything sitting uncommitted in a worktree is invisible to it.

---

- [ ] **Step 1: Confirm what the earlier tasks left behind**

```bash
cd /Users/y0shi/workspace/ignite/ignite-training-solutions
git status --short
find solutions/m3-command-based solutions/testouts/t3-scheduler-conflict -name '*.java' | sort
```

Expected, all untracked:

```
solutions/m3-command-based/src/main/java/frc/robot/RobotContainer.java
solutions/m3-command-based/src/main/java/frc/robot/commands/DriveForSeconds.java
solutions/m3-command-based/src/main/java/frc/robot/subsystems/DrivetrainSubsystem.java
solutions/m3-command-based/src/main/java/frc/robot/subsystems/ElevatorSubsystem.java
solutions/testouts/t3-scheduler-conflict/src/main/java/frc/robot/RobotContainer.java
```

If any is missing, Task 2 Step 7 or Task 4 Step 6 did not finish — go and get it from there rather than rewriting it here. A second, independently written solution is a second thing that can drift.

`solutions/` contains **only files the student writes or changes**. Confirm no `build.gradle`, no wrapper, no given test, and no `Main.java`/`Robot.java`/`Constants.java` came along for the ride — those live in the labs repository and are picked up from there at overlay time, and a stale copy here would mask exactly the skeleton change the cross-check exists to catch:

```bash
find solutions/m3-command-based solutions/testouts/t3-scheduler-conflict -type f \
  ! -name '*.java' -o -name 'Main.java' -o -name 'Robot.java' -o -name 'Constants.java' \
  -o -name 'M3Deliverables.java'
```

Expected: no output. Delete anything it lists.

- [ ] **Step 2: Read the M3 solution as a mentor would**

The solution is not only a cross-check input — it is what a mentor shows a stuck student. Read all four files and check three things:

1. **It is a good answer, not merely a passing one.** `DriveForSeconds` calls `addRequirements` in the constructor and stops the drivetrain in `end`. Both commands are named. `subclassCommand()`, `factoryCommand()` and `composition()` each build a new instance. Subsystems are private fields.
2. **It demonstrates both idioms clearly**, because that is the milestone's headline — one explicit subclass, one factory method on the subsystem.
3. **It is formatted.** The cross-check runs `spotlessCheck` after overlaying, and a solution that fails it hands the mentor a red build that is not their fault.

- [ ] **Step 3: Write `authoring-notes/m3.md`**

A future maintainer looking at these tests needs to know which trap each one is aimed at, so they do not "simplify" a case that exists on purpose. Match the shape of the existing `authoring-notes/m1.md` and `m2.md`.

```markdown
# M3 — authoring notes

## The harness, and why it is an interface rather than reflection

M2's `ContractProbe` reaches the student's class reflectively because L2 forbids
giving them any signature to copy. L1 has no such constraint, so M3 uses a real
interface — `frc.robot.harness.M3Deliverables` — that `RobotContainer` implements.
A student who gets it wrong gets a compile error naming the method, which is a far
better failure than a reflective lookup missing by a character.

It lives in `src/main/java/frc/robot/harness/`, not under `src/test/`, because
`RobotContainer` has to implement it. `tools/tests_lock.py` and `CODEOWNERS` both
carry a `harness/` pattern for exactly this reason — the same way `plant/` is
covered for M4.

## The one constraint that surprises students

Every command method must return a **new** instance per call. This is not
fastidiousness: WPILib refuses to schedule a command that belongs to a
composition, `composition()` is built from the other two, and the given tests
schedule all three. A stored field cannot satisfy that.

Expect to explain this once per cohort. The failure message names it and says
what to do; a student who has hit it has learned something real about command
ownership, so it is worth the cost.

## Which trap each given test is aimed at

| Test | Trap |
|---|---|
| `theRobotContainerExposesASecondSubsystemThatIsNotTheDrivetrain` | Building one giant subsystem, which the milestone names as a failure mode. Nothing about requirements or interruption can be shown with one subsystem |
| `subclassCommandIsANewInstanceEveryTime` / `factoryCommandIsANewInstanceEveryTime` | The stored-instance mistake above |
| `subclassCommandHasBeenGivenAName` / `factoryCommandHasBeenGivenAName` | Skipping `.withName()`. Asserted as "the name is not the class's own name", because that is what WPILib falls back to. Do not weaken this to a non-null check — `InstantCommand` is non-null and useless in Glass |
| `subclassCommandDeclaresWhichSubsystemItNeeds` | **Forgetting `addRequirements()`** — the milestone's named failure mode. Produces a robot that looks fine in the simulator and fights over a motor on the field |
| `factoryCommandDeclaresWhichSubsystemItNeeds` | A "factory method" that builds a bare `InstantCommand` instead of using `run`/`runOnce` on the subsystem, so it gets no requirement |
| `subclassCommandActuallyRunsWhenScheduled` / `factoryCommandActuallyRunsWhenScheduled` | A command that compiles and is named but never initialises — usually because its requirements were already taken |
| `theCompositionNeedsBothSubsystems` | A composition assembled out of one subsystem's commands, which is not the exercise |
| `theCompositionRunsWhenScheduled` | As above, for a composition that cannot start |
| `schedulingYourCommandInterruptsWhateverWasAlreadyUsingItsSubsystem` | The headline. Catches missing requirements from the other direction, and is deterministic because the command being interrupted is the harness's, not the student's |
| `yourCommandIsInterruptedWhenSomethingElseNeedsItsSubsystem` | The same model in reverse. **Aborts rather than fails** if the student's command finishes within two loops — an instantaneous command cannot be interrupted, and demanding a long-running one would constrain a design decision M3 leaves open |
| `theDrivetrainHasADefaultCommand` and the other two in that class | The DoD item. The third case — stands aside, then comes back — is the one that shows the student what a default command actually is |

## What is deliberately not tested

Button bindings. Reaching them means driving simulated HID state and asserting on
which button the student chose, which is brittle and prescriptive. The bindings are
mentor-verified and are visible the moment the simulator runs.

Composition termination. A sequence ending in a hold-until-released command is a
legitimate answer to "a sequence or a parallel with a timeout". If you ever add a
termination assertion, change `M3Deliverables`' Javadoc first.

## Distractors that were run against this suite

Six, at authoring time: no `.withName()`; no `addRequirements()`; a
drivetrain-only composition; a stored command instance; no default command; and
`mechanism()` returning the drivetrain. Each failed the case aimed at it and
nothing else. If you change a test, re-run all six.
```

- [ ] **Step 4: Write `answer-keys/t3-scheduler-conflict.md`**

The mentor running T3 needs the correct answer *and* the failing ones, because part 3 is the pass criterion.

```markdown
# T3 — scheduler conflict: answer key

**Time box:** 60 minutes. Tracks C and D. Completed cold from the README in
<TIME> at authoring time (Task 4, Step 6) — record the real figure here.

## Part 1 — the command

Any command requiring the elevator and the gripper, named, and returned fresh
from `combinedCommand()`. The reference answer:

```java
  public Command combinedCommand() {
    return elevator
        .raiseCommand()
        .alongWith(gripper.closeCommand())
        .withTimeout(2.0)
        .withName("Raise And Grip");
  }
```

An explicit `Command` subclass with `addRequirements(elevator, gripper)` is
equally correct and some students will reach for it. Both idioms pass.

## Part 2 — the binding

Any button. `driverController.rightBumper().onTrue(combinedCommand());` in
`configureBindings()`.

## Part 3 — the explanation, which is the actual test

**Passing answer.** The elevator command is scheduled and requires the elevator.
The student's command is already running and also requires the elevator. Both
cannot own it, so the scheduler **interrupts** the running command — calling its
`end(true)` — and starts the new one. The interrupted command does not resume
and was never queued.

Three specifics to listen for, and to ask for if they do not come:

1. **Which command survives** — the newly scheduled one.
2. **What happened to the other one** — interrupted, not paused, not queued.
3. **Which method ran** — `end(boolean)`, with `interrupted` true.

**Failing answers, and what each one means.**

| What they say | What they are missing |
|---|---|
| "They both run" | The requirement model entirely. This is the answer the fixture exists to find |
| "It queues" / "it waits its turn" | Thinking of the scheduler as a job queue. It is not; there is no backlog |
| "The new one is rejected" | Right that only one runs, wrong about which. Worth probing — they are close |
| "The first one stops" (and nothing more) | Half marks at best. Ask which method ran. If `end(true)` does not come out, they do not have the model, and this is the specific thing that bites on a real robot: a flywheel that never gets told to stop |

**Follow-up if they answered well and quickly.** "What if you press the same
button twice — does your own command interrupt itself?" (Yes: a second instance
requires the same subsystems, so the first is interrupted. This is why fresh
instances matter.)

## If they do not finish

Not a failure of anything. It means M3 is where they start, which is what the
fixture is for. A student who writes the command but fumbles part 3 has *not*
tested out — part 3 is the pass criterion, by design (spec §10).
```

Replace `<TIME>` with the real figure Task 4 Step 6 reported. If that figure is missing, the fixture was never completed end to end and this task cannot be finished honestly — go back and do it.

- [ ] **Step 5: Update the coverage table in `README.md`**

In `../ignite-training-solutions/README.md`, the coverage table currently says `M3–M8, T3–T5 | **not built**`. Split that row:

```diff
-| M3–M8, T3–T5 | **not built** | Track alongside the skeletons; a milestone shipped without its solution is not shipped |
+| M3 | two subsystems, both command idioms, the composition, the default command | The `frc.robot.harness/` files are given and are **not** overlaid — they come from the labs repository |
+| T3 | `combinedCommand()` plus the binding | Part 3's explanation is in `answer-keys/t3-scheduler-conflict.md` and is the pass criterion |
+| M4–M8, T4–T5 | **not built** | Track alongside the skeletons; a milestone shipped without its solution is not shipped |
```

Keep the surrounding rows untouched.

- [ ] **Step 6: Run the cross-check**

```bash
cd /Users/y0shi/workspace/ignite/ignite-training-solutions
./tools/cross-check.sh
```

Expected: `PASS m3-command-based`, `PASS testouts/t3-scheduler-conflict`, the four existing `PASS`/`SKIP` lines unchanged, and `CROSS-CHECK OK: 6 solution(s) pass their skeleton's given tests` — count the number in the real output rather than trusting this line.

Paste the whole output into the task report. Two failure modes to read carefully rather than retry:

- `FAIL … (given tests)` — the solution does not satisfy the skeleton. Either the solution is wrong or a given test is. Both are real bugs; work out which before changing anything.
- `FAIL … (formatting)` — run `spotlessApply` on the overlaid copy is *not* the fix, because the overlay is a temporary directory. Format the file in `solutions/` and re-run.

The cross-check never touches the labs working tree — it archives `HEAD` into a temp directory — so a `FAIL` here cannot have left a solution sitting in the student repository. Confirm anyway:

```bash
cd /Users/y0shi/workspace/ignite/docs && git status --short
```

- [ ] **Step 7: Commit, in the solutions repository**

```bash
cd /Users/y0shi/workspace/ignite/ignite-training-solutions
git status --short
git add solutions/m3-command-based solutions/testouts/t3-scheduler-conflict \
        authoring-notes/m3.md answer-keys/t3-scheduler-conflict.md README.md
git commit -m "M3 and T3 reference solutions, authoring notes, and answer key"
```

Nothing in this task is committed to `ignition-labs`. If `git status` in the labs repository shows anything, something went into the wrong tree — find it before committing here.
