# M2 — OOP, Interfaces & Lambdas

M1 gave you six methods to fill in. This one gives you a description and no
method at all. Writing the class — deciding what each signature has to be — is
the exercise.

Read [the milestone](../../docs/programming/milestones/m2-oop-interfaces-lambdas.md)
first. The git commands are in
[the git workflow](../../docs/programming/git-workflow.md), and this milestone
does not repeat them.

---

## Before you start

```bash
./gradlew assemble
```

Green. The project compiles.

```bash
./gradlew test
```

Red. That is the starting line, not a broken clone.

Now look in `src/main/java/frc/training/m2/`. There are two files, and **neither
of them is `SensorProfile.java`**. That is not a mistake and nothing is missing.
You are going to create that file.

---

## 1. Read the specification

`SensorProfileSpec.java` describes a class: what you build one from, what you can
ask it, what comes back, and what the awkward cases are. It contains no methods
to copy, on purpose. Turning that description into signatures is most of what
this milestone is teaching.

Read the whole thing before writing anything.

---

## 2. Write the class

Create `SensorProfile.java` beside the spec, in the same package.

Run the contract test as you go:

```bash
./gradlew test --tests '*SensorProfileContractTest'
```

It will not tell you the answer. It will tell you which of these it is: the class
is not there, a constructor or method it needs is not there, one returns the
wrong type, or one returns the wrong value. Read the message — they are written
to be read.

**Do not edit anything under `src/test/`.** Those tests are part of the
milestone and CI checks that they have not changed. If you think one is wrong,
say so in your pull request — that happens, and you might be right.

---

## 3. Write your own tests

Once the contract test is green:

```bash
cp starter/SensorProfileTest.java src/test/java/frc/training/m2/
```

That file has one finished test showing the shape, and three empty ones that say
what to cover. Fill them in. From the moment you copy it, the file is yours —
rename the cases, add more, nothing there is locked.

Four passing tests you wrote yourself is a definition-of-done item. Work the
expected values out by hand before you write them: a test whose expected value
came from running your own code proves only that your code does what it does.

---

## 4. Fix `SupplierDemo`

```bash
./gradlew test --tests '*SupplierDemoTest'
```

One of its three tests passes and two fail. `SupplierDemo` is short, it compiles,
and it looks reasonable.

Before you change anything, answer the question the green test asks: **why does
that one pass?** The answer is the bug.

---

## 5. Read the real code

`READING.md` sends you to five files in `Robot-2026` and asks you seven
questions. Do it after `SensorProfile` is green — it lands differently once you
have built something that does nearly the same job a worse way.

---

## There is no simulator here

M2 has no robot in it. The package is `frc.training.m2`, not `frc.robot`, for
that reason. **WPILib: Simulate Robot Code** has nothing to run. Robot code
starts at M3.

---

## Done when

- `./gradlew test` is green
- You wrote at least four tests of your own, and can say what each one would
  catch
- Every field on `SensorProfile` is private
- You can explain the difference between passing a value and passing a supplier
- You can say why `SupplierDemo`'s one passing test passes
- You can read `LaunchState.getInstance()` and explain what a singleton
  guarantees
- You can explain why the shooter's two launch-request builders both implement
  one interface
- You wrote at least one lambda and can name its type
- You read `READING.md` and can explain why `RobotPreference<T>` implements
  `Supplier<T>`
