# Contributing

How work gets into this repository, and into the robot code.

---

## The git ritual

See [the git workflow](programming/git-workflow.md). The short version:

> **Clone what you're reading. Fork what you'll change. Branch to work.**

You fork this repository once at M0. Every milestone after that is a branch in
that same fork, with a pull request into your **own** fork — not this one.

---

## Every pull request

- **Branch first.** Never commit to `main`, even your own.
- **CI must be green.** `./gradlew build` and `./gradlew spotlessCheck`, which the
  Student Check workflow runs for you.
- **Fill in the AI disclosure.** It is in the pull request template and it is not
  optional. Your tier comes from the milestones you have finished, and
  `programming/ai-use-standard.md` explains why the boundaries are where they are.
- **You can explain every line.** This is the actual bar. A mentor will ask.

---

## Formatting

Do not argue with the formatter, and do not format by hand:

```bash
./gradlew spotlessApply
```

It runs automatically before every compile. If CI complains about formatting,
run it and commit the result.

---

## Given tests are not yours to change

Every milestone ships tests you must make pass. They are recorded in `tests.lock`
and CI checks them.

If you think a given test is **wrong** — say so in the pull request. That happens,
you might be right, and a mentor will look. What does not work is quietly deleting
it: the lock check will notice, and it will notice in a way that is more awkward
than just asking.

---

## Pull requests to `Robot-2026`

At M4 and M6 you contribute to the real competition code from a fork. One extra
rule applies there, and it is a hard one:

> **Pure logic only. No timing, no simulation, no hardware.**

Test things that take numbers in and give numbers out — `LaunchCalculator`, the
`LaunchRequestBuilder` implementations, unit conversions, state-machine transitions.

**Why.** `Robot-2026`'s CI runs `./gradlew build`, which runs the tests. Once your
test merges, every team pull request has to pass it. A timing-dependent test passes
on your laptop and fails intermittently in CI — and in February that means a test
you wrote in November is blocking somebody's competition fix at 11pm.

The simulation-timing helpers live in `projects/m6-testing/`. That is what the
practice project is for.
