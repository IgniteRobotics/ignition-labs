# Task 6: Give M6's logging Definition-of-Done item somewhere to happen

M6's DoD asks the student to add `@Logged` to something and see it appear in a log. The
sandbox contains no `Epilogue`, no `@Logged`, no `Robot`, and no `Main` — it is two plant
classes and two test files. The real-repo deliverable is pure-logic-only by hard rule, so
there is no loggable subsystem there either. The item is orphaned.

The risk is raising M6's bar. Spec §9.7 says the bar is deliberately low and *"the skeleton
must not raise it."* Whatever this task adds is given, complete, and runnable — not another
thing to build.

**Files:**
- Modify or create under: `projects/m6-testing/` (`src/main/`, `build.gradle`, `README.md`)
- Modify: `docs/programming/milestones/m6-simulation-telemetry-testing.md`
- Read, do not edit: `projects/m6-testing/src/test/**`

**Consumes:** nothing.
**Produces:** an M6 whose every DoD item has a vehicle.
**Lane:** `content`

---

## The findings this closes

**C10 — the DoD item has no vehicle.**
> "- [ ] You added `@Logged` to something and saw it appear in the log"

```bash
grep -rn "Epilogue\|@Logged" projects/m6-testing/    # returns nothing
find projects/m6-testing/src -type f                  # four files, no Robot, no Main
```

Its neighbours are fine: *"One test runs against a simulated Phoenix device with correct
timing"* is served by `PhoenixTestSupport` + `ShooterFlywheel`, and the log-diagnosis item is
served by `logs/` + `QUESTIONS.md`.

**C10 tail — M6 Part 2 step 3** says *"Make the subsystem `AutoCloseable` and confirm two
tests in the same file both pass."* There is no subsystem in the sandbox; there is
`ShooterFlywheel`, a plant. The instruction works, the noun does not.

---

- [ ] **Step 1: Confirm the gap and check M6's own invariants first.**
      ```bash
      export JAVA_HOME=~/wpilib/2026/jdk
      grep -rn "Epilogue\|@Logged" projects/m6-testing/ || echo "confirmed: none"
      cd projects/m6-testing && ./gradlew test    # expect rc=0 — M6's suite is green by design
      ```
      M6 is the one project whose `test` is green on a clean clone, and its integrity is
      enforced by the regression gate rather than by red tests. Anything added below must
      leave both properties intact.

- [ ] **Step 2: Choose the smallest thing that makes the item real.** Two candidates:
      - **A — a minimal logged robot.** Add `Main.java` and a thin `Robot.java` with
        `Epilogue.configure(...)` / `Epilogue.bind(this)` and `DataLogManager.start()`,
        exposing the existing `ShooterFlywheel`. The student adds `@Logged` to a field, runs
        `./gradlew simulateJava`, and opens the resulting log in AdvantageScope — which is
        the same tool Part 3 already requires. Matches M3/M4/M5's shape.
      - **B — drop the item.** Fold logging into Part 3's log-diagnosis work and delete the
        DoD line, on the grounds that M6's stated lesson is testing and diagnosis, and
        Epilogue was already taught in M4.

      **A is recommended**, because Track D's page leans on Epilogue explicitly (*"What M6
      adds is using it deliberately: setpoint and measurement together"*) and because
      producing a log the student then diagnoses closes a loop that Part 3 currently opens
      with someone else's file. Record the choice and the reason in the task report.

- [ ] **Step 3: Implement it, at L0 density.**
      If A: `Main.java` and `Robot.java` are given, complete, and never edited by the
      student — the same contract M4's and M5's `Robot.java` carry. Add one commented line
      showing where the student's own `@Logged` goes. No new vendordep (Epilogue is
      WPILib). Add the `ROBOT_MAIN_CLASS` wiring `build.gradle` needs for
      `simulateJava` — M6's `build.gradle` currently has no robot to launch, so check what
      the existing comment there says before changing it.

- [ ] **Step 4: Fix the "subsystem" noun in the milestone page.**
      Part 2 step 3: `AutoCloseable` is right, "the subsystem" is not. Name
      `ShooterFlywheel`, or say "the class under test." Also check the DoD line
      *"A subsystem implements `AutoCloseable` and two tests in one file both pass"* for the
      same problem.

- [ ] **Step 5: Update the milestone page and README to describe what you built.**
      If A: the DoD item becomes concrete — run the sim, add `@Logged`, find the signal in
      the log. If B: delete the item from `docs/programming/milestones/m6-simulation-telemetry-testing.md`
      and say in one sentence where logging is taught instead.

- [ ] **Step 6: Verify M6's invariants are unchanged.**
      ```bash
      cd projects/m6-testing
      ./gradlew assemble          # expect rc=0
      ./gradlew compileTestJava   # expect rc=0
      ./gradlew test              # expect rc=0 — still green, still by design
      ./gradlew spotlessCheck     # expect rc=0
      cd ../..
      poetry run python -m tools.regression_gate projects/m6-testing \
        projects/m6-testing/regressions/drop-gear-ratio.patch \
        --expect "caught:frc.robot.WorkedExampleTest#ticksToMechanismRotations_appliesBothTicksPerRevolutionAndGearRatio"
      for p in flip-sign widen-tolerance; do
        poetry run python -m tools.regression_gate projects/m6-testing \
          projects/m6-testing/regressions/$p.patch --expect no-failure
      done
      git status --short     # must be clean of patch residue
      ```
      All four regression-gate invocations must exit 0 and leave the tree clean. Paste the
      output.

- [ ] **Step 7: Complete the item end to end yourself.**
      Do what the DoD asks: add `@Logged` to something, run the simulator, open the log,
      find the signal. Report the signal name you saw. An unwalked DoD item is exactly what
      produced this finding.

- [ ] **Step 8: Format and commit.**
      ```bash
      cd projects/m6-testing && ./gradlew spotlessApply && ./gradlew spotlessCheck
      ```
      **Do not run `tools/tests_lock.py write`** — task 9 owns the manifest, and adding files
      under `src/main/` outside `plant/` and `harness/` should not change it anyway. If your
      change touched `plant/` or `harness/`, say so loudly in the report so task 9 expects
      the manifest delta.
