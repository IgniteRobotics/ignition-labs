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
