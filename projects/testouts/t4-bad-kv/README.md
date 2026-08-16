# T4 — Bad kV

**Time: 60 minutes.** Track D.

This is a flywheel velocity controller inherited from last season. It is not
broken in the sense of throwing an exception or refusing to compile — it
runs. Look at `plots/response.png` before you touch anything: it reaches the
setpoint, overshoots, rings for a few cycles, and settles a little short of
the target line rather than on it.

```bash
./gradlew test
```

Two tests, both red.

**Do not edit anything under `src/test/`.** Those tests are part of the
fixture and CI checks that they have not changed. If you think one is wrong,
say so — you might be right, and a mentor will look.

---

## Part 1 — diagnose it

Everything you can change lives in `BadlyTunedFlywheel.java`: two fields,
`kV` and `kP`. Nothing else in the project needs to change, and nothing else
should.

You cannot look up the flywheel's true parameters — `T4FlywheelPlant.java` is
given and locked, same as M4's plants. What you *can* do is exactly what
characterization means: command a few known voltages, read the velocity that
results, and work out what `kV` should have been. Re-run
`./gradlew generateResponse` as often as you like; it reprints the response
every time.

Fix `kV`. Then fix `kP` to match — not to compensate for `kV` being wrong,
which is how this fixture got built in the first place.

```bash
./gradlew test
```

Both green.

---

## Part 2 — the three questions

Answer these out loud, to a mentor. Nothing here is covered by a test — this
is the whole reason the test alone isn't the pass criterion.

1. **What was wrong with the original `kV`, and how do you know?** "It looked
   too low" is not characterization. Say what you measured and what it told
   you.

2. **You will be tempted to fix this by raising `kP` instead of touching
   `kV`. Try it — push `kP` up with `kV` left alone, and run
   `./gradlew generateResponse` again.** What happens to `settled`? Explain
   why more proportional gain does not close a gap that feedforward left
   open.

3. **Why is the original tuning fragile?** It reached this target, at this
   simulated battery voltage, and shipped anyway. What specifically would
   happen to this same controller at a different target speed, or with the
   battery sagging under load? Be specific about which term in `update()`
   is the one that stops being trustworthy, and why.

That is the whole test-out. Part 1 checks you can characterize a system and
size a feedforward gain to it. Part 2 checks you understand *why* the
original numbers were fragile, not just that they were wrong — and the
difference between a passing answer and a failing one is entirely in these
three explanations. "The gains were bad" is a failing answer to all three.

---

## How you pass

1. Part 1's tests are green.
2. Your three answers in Part 2 are correct.

If 60 minutes runs out, that is not a failure of anything. It means M4 is
where you start, which is exactly what this fixture exists to find out.
