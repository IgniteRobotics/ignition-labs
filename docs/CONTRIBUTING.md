# Contributing

How work gets into this repository, and into the robot code.

Everything under `docs/` is published to the public site — there is no draft
state and no staging area, so a scratch note or a work-in-progress page does
not belong there.

---

## The git ritual

See [the git workflow](programming/git-workflow.md). The short version:

> **Clone what you're reading. Fork what you'll change. Branch to work.**

You fork this repository once at M0. Every milestone, M0 included, is a branch
in that same fork, with a pull request into your **own** fork — not this one.
This repository only accepts pull requests from collaborators, so a pull request
aimed here by mistake is refused rather than lost.

---

## Every pull request

- **Branch first.** Never commit to `main`, even your own.
- **CI must be green.** The Student Check workflow always verifies the given tests are
  unmodified and validates contributor files. It also runs `./gradlew build` and
  `./gradlew spotlessCheck` for you — but only once your pull request touches a
  milestone project, so an M0 contributor-only pull request won't see that part run at
  all. See [M0](programming/milestones/m0-tooling-and-git.md#pull-requests-and-ci) for
  the full picture.
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

Run it yourself before you push. It deliberately does **not** run on every compile —
if it did, the build would reformat your tree before CI could ever notice a
violation, and the formatting check would be decoration. See
`programming/teaching-repo-spec.md` §4.3. If CI complains about formatting, run it
and commit the result.

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

Once you have commit access (M7), work in `Robot-2026` starts from an issue; see
[Issues & Projects](programming/issues-and-projects.md).

---

## Working on the repository itself

Not a milestone project — this is for changing `ignition-labs`'s own docs, tools, or
skeletons.

```bash
poetry install
poetry run pytest                              # tools/ test suite
poetry run mkdocs build --strict                # the site itself
poetry run python tools/site_guard.py _site     # link and structure checks
poetry run python tools/tests_lock.py verify    # given tests unmodified
```

Added or changed a given test? Regenerate the manifest: `poetry run python
tools/tests_lock.py write`.

Running Gradle locally against any `projects/*`? `export JAVA_HOME=~/wpilib/2026/jdk`
first, in every terminal — see `SETUP.md`.

`mkdocs build` prints a red banner from the Material team warning that MkDocs 2.0 is
"currently unlicensed" and drops the plugin system. Expected; tracked in
`programming/teaching-repo-spec.md` §16.
