# Task 7: T5's three committed logs, the fixture README, and the regeneration guarantee

Task 6 built and proved out `LogRecorder`; this task turns it into the shipped fixture. The one
rule that shapes every step here: **the mapping from `a`/`b`/`c` to `ODOMETRY_DRIFT` /
`MISTUNED_CONTROLLER` / `BLOCKING_COMMAND` must not appear in any file this task commits** —
not `logs/README.md`, not the fixture `README.md`, not a code comment, not this task's own
report. `00-plan.md`'s known-limitation note calls out exactly this risk, using `t4-bad-kv`'s
directory name (caught after it shipped, in step 9) as the precedent for what happens when it
isn't caught in time. The mapping lives only in `../ignite-training-solutions/`'s answer key —
Task 8, not this one.

**Files:**
- Create: `projects/testouts/t5-drifting-auto/logs/a-run.wpilog`
- Create: `projects/testouts/t5-drifting-auto/logs/b-run.wpilog`
- Create: `projects/testouts/t5-drifting-auto/logs/c-run.wpilog`
- Create: `projects/testouts/t5-drifting-auto/logs/README.md`
- Create: `projects/testouts/t5-drifting-auto/README.md`

**Consumes:** `LogRecorder.record(Fault, Path)` and the `generateLog` Gradle task from Task 6.
This task runs what Task 6 shipped; it does not write any Java.

**Produces:** the three committed logs Task 8's answer key is written against, and the
student-facing fixture a mentor hands a Track D student.

**Lane:** `testout`. Work in `.worktrees/t5`. Depends on Task 6 — run this in the same worktree,
after Task 6's commit lands.

---

- [ ] **Step 1: Generate the three recordings and assign letters without writing the mapping down**

```bash
cd /Users/y0shi/workspace/ignite/docs/projects/testouts/t5-drifting-auto
export JAVA_HOME=~/wpilib/2026/jdk
mkdir -p /tmp/t5-generated
for f in ODOMETRY_DRIFT MISTUNED_CONTROLLER BLOCKING_COMMAND; do
  ./gradlew generateLog -Pfault=$f -Pout=/tmp/t5-generated/$f.wpilog --console=plain
done
```

These three scratch files are fine to have fault-revealing names — they never get committed.
Now assign each to a letter with a step that does not leave the assignment written down
anywhere that gets committed: shuffle the three faults, and read off the letters in that order.

```bash
python3 -c "
import random
faults = ['ODOMETRY_DRIFT', 'MISTUNED_CONTROLLER', 'BLOCKING_COMMAND']
random.shuffle(faults)
letters = ['a', 'b', 'c']
for letter, fault in zip(letters, faults):
    print(f'cp /tmp/t5-generated/{fault}.wpilog logs/{letter}-run.wpilog')
" > /tmp/t5-assign.sh
cat /tmp/t5-assign.sh   # read it before running it -- this is the one place the mapping is
                        # ever visible in this terminal session
bash /tmp/t5-assign.sh
rm /tmp/t5-assign.sh    # gone once run -- do not paste its contents into the task report
ls -la logs/
```

**Do not paste `/tmp/t5-assign.sh`'s contents, or which fault ended up as which letter, into
the task report or any commit message.** The report for this step should say the three files
were generated and assigned, and nothing about which is which.

- [ ] **Step 2: Write `logs/README.md` — mechanics only, never the mapping**

`projects/testouts/t5-drifting-auto/logs/README.md`:

```markdown
# T5 logs

Three recordings, one fault injected in each — `a-run.wpilog`, `b-run.wpilog`,
`c-run.wpilog`. Neither the filenames nor this file say which is which; see
"Identifying a log" below for why, and how to find out if you need to.

## Regenerating

One command per fault, from this directory:

```bash
export JAVA_HOME=~/wpilib/2026/jdk
./gradlew generateLog -Pfault=ODOMETRY_DRIFT -Pout=/some/path/recording.wpilog
./gradlew generateLog -Pfault=MISTUNED_CONTROLLER -Pout=/some/path/recording.wpilog
./gradlew generateLog -Pfault=BLOCKING_COMMAND -Pout=/some/path/recording.wpilog
```

`generateLog` runs `LogRecorder` (given, `src/main/java/frc/testout/t5/`) against
`T5DriveSim` and writes exactly the destination file requested. The routine, the
drivetrain, and the fault injection are fully deterministic — two regenerations of the
same fault produce byte-identical files. That determinism is what makes "regenerate T5's
logs if the sim stack changes" (spec §16) a real, checkable maintenance step rather than a
hope.

## Identifying a log

This repository does not record which of `a-run.wpilog`, `b-run.wpilog`, `c-run.wpilog`
carries which fault. If you need to know — for authoring, for grading, for building
mentor tooling — regenerate a candidate for a specific `Fault` value (see above) and
compare it byte-for-byte against each of the three shipped files:

```bash
cmp /some/path/recording.wpilog logs/a-run.wpilog
```

A match identifies that file. Because generation is deterministic, this is a complete and
reliable way to recover the mapping without it ever being written down here — the answer
key in `../ignite-training-solutions/` is the one place that mapping is recorded.
```

- [ ] **Step 3: Regenerate all three from scratch and confirm they reproduce — not optional**

This is the check `00-plan.md` requires by name: "Task 7 Step 4 — regenerate all three logs
from scratch and confirm they reproduce." Written here as a mapping-blind check — it proves
reproducibility and full coverage without ever stating which letter matched which fault.

```bash
cd /Users/y0shi/workspace/ignite/docs/projects/testouts/t5-drifting-auto
export JAVA_HOME=~/wpilib/2026/jdk
rm -rf /tmp/t5-reproduce-check
matched=0
for f in ODOMETRY_DRIFT MISTUNED_CONTROLLER BLOCKING_COMMAND; do
  ./gradlew generateLog -Pfault=$f -Pout=/tmp/t5-reproduce-check/$f.wpilog --console=plain > /dev/null
  hit=""
  for letter in a b c; do
    if cmp -s /tmp/t5-reproduce-check/$f.wpilog logs/$letter-run.wpilog; then
      hit=$letter
    fi
  done
  if [ -z "$hit" ]; then
    echo "$f: NO MATCH -- stop, something in Task 6 or Step 1 above is not reproducing"
  else
    echo "$f: matches exactly one shipped file"
    matched=$((matched + 1))
  fi
done
echo "matched=$matched/3 (expect 3)"
```

Expected: `matched=3/3`, and each fault printed `matches exactly one shipped file` — never
"NO MATCH", never a fault matching more than one letter (which would mean two of the shipped
files are identical, i.e., one fault didn't actually get recorded). Paste this output in the
task report; it is intentionally silent on which letter matched which fault, and the report
should stay that way too.

```bash
rm -rf /tmp/t5-reproduce-check /tmp/t5-generated
```

- [ ] **Step 4: Write the fixture `README.md`**

Per §10's self-review, a mentor hands one student one specific log — the fixture ships all
three so a mentor can rotate which one a given student sees, not so a student compares all
three against each other. Voice matched to `t4-bad-kv/README.md`.

`projects/testouts/t5-drifting-auto/README.md`:

````markdown
# T5 — Test-Out

**Time: 60 minutes.** Track D.

This project has no code for you to edit. Your mentor will tell you which file
under `logs/` to open — `a-run.wpilog`, `b-run.wpilog`, or `c-run.wpilog`. It is
a recording of one autonomous routine, driven by one real (simulated) run, that
drifts off its intended path partway through. Your job is to work out why.

Open your assigned log in **AdvantageScope** or **DataLogTool** (both ship with
WPILib — `~/wpilib/2026/tools/`). Every log carries the same six signal
streams:

| Entry | What it is |
|---|---|
| `/t5/truePose` | The robot's actual field position and heading |
| `/t5/estimatedPose` | What the robot's own pose estimator believed |
| `/t5/pathTargetPose` | Where the path-following controller was aiming, over time |
| `/t5/moduleStates` | Each swerve module's commanded velocity and angle |
| `/t5/events` | Timestamped markers: when the path phase and each of the two named commands (`step2`, `step3`) started and ended |

---

## The three candidates

Exactly one of these is what happened in your log:

1. **Odometry drift** — the robot's pose estimator lost track of where the
   robot actually was.
2. **A mistuned path-following controller** — the controller steering the
   robot toward the path was not aggressive enough to keep up.
3. **A named command that blocked longer than expected** — one of the two
   named commands (`step2` or `step3`) did not finish when it should have.

## What you're doing for the next 60 minutes

1. Open your assigned log. Look at all five signal categories above, not just
   the ones that seem obviously relevant — a cause you can rule out is as much
   evidence as a cause you can confirm.
2. Work out **which of the three candidates actually happened.**
3. Work out **what specifically in the log rules out the other two.** Not "I
   don't think it's that one" — point at a signal and say what it does or
   doesn't show.

## How you pass

Say both of the following out loud, to a mentor:

1. **Which of the three candidates it was**, and the specific evidence that
   supports it.
2. **What rules out each of the other two.** A correct diagnosis without this
   half has not passed — guessing right is not the same as showing why the
   other two don't fit.

Nothing here is graded by a test; there is nothing to run and nothing to turn
green. The three questions above, answered correctly and with evidence, are
the entire pass criterion.

If 60 minutes runs out without a confident answer, that is not a failure of
anything — it means log-reading under time pressure is where you start, which
is exactly what this fixture exists to find out.
````

- [ ] **Step 5: Confirm nothing leaks the mapping, then commit**

```bash
cd /Users/y0shi/workspace/ignite/docs/projects/testouts/t5-drifting-auto
grep -rniE 'odometry.?drift|mistuned|blocking.?command|a-run|b-run|c-run.*(drift|mistuned|block)' \
    README.md logs/README.md
```

Expected: the three candidate-cause *names* appear in the fixture `README.md`'s neutral list
(that's the menu, not the answer) — confirm by eye that neither `README.md` nor
`logs/README.md` ever pairs one of those names with a specific letter. There should be no line
anywhere containing both a fault name and `a-run`/`b-run`/`c-run` together.

```bash
cd /Users/y0shi/workspace/ignite/docs
git status --short
```

Expected: exactly `projects/testouts/t5-drifting-auto/logs/a-run.wpilog`,
`logs/b-run.wpilog`, `logs/c-run.wpilog`, `logs/README.md`, and `README.md` — nothing under
`src/` (Task 6's territory, not this one's), no `build/`, no `.gradle/`.

```bash
git add projects/testouts/t5-drifting-auto/logs projects/testouts/t5-drifting-auto/README.md
git commit -m "T5 drifting-auto: three committed logs, fixture README, regeneration guarantee"
```
