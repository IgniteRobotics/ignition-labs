# Deliberate regressions

Part 1 step 4 of this milestone asks you to introduce a deliberate regression, watch your
test fail, fix it, and watch it pass. **That step is the point of the whole exercise.** A
test you have never seen fail is not yet known to work.

These patches make that one reproducible command instead of an improvisation.

## Running one

From this project's directory:

```bash
git apply regressions/flip-sign.patch
./gradlew test          # your tests should now FAIL
git apply -R regressions/flip-sign.patch
./gradlew test          # and pass again
```

If `./gradlew test` stays green with a patch applied, your tests are not testing the thing
that broke. That is the finding. It is not a failure — it is the exercise working.

## What each one breaks

| Patch | Breaks |
|---|---|
| `drop-gear-ratio.patch` | The tick-to-rotation conversion stops accounting for the gearbox |
| `flip-sign.patch` | The look-ahead correction moves the aim point the wrong way |
| `widen-tolerance.patch` | "At setpoint" starts accepting a flywheel that is nowhere near speed |

None of them tells you the correct value. Read `LaunchProfile.java` for that — it is given,
it is correct, and it is yours to test, not to change.

## Which ones CI checks

`drop-gear-ratio.patch` is caught by the worked example in `src/test/`, which is how this
repository's own CI proves the mechanism works at all.

**`flip-sign.patch` and `widen-tolerance.patch` are not caught by anything you were given.**
When you open your pull request, CI applies them and requires your tests to fail. That is
the gate, and the only way through it is a test that genuinely catches the break.

`widen-tolerance.patch` is the one that catches a test which can never fail. A tolerance of
50 rotations per second accepts `atSetpoint(100.0, 100.0)` exactly as happily as a tolerance
of 0.5 does. Catching it needs a case where the flywheel is genuinely too slow.
