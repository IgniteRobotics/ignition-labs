# T2 — Supplier Swap

The M2 test-out. If you pass this, you skip M2 and go to M3.

**Time: 45 minutes.**

---

## What is here

An interface, `RangeTable`, with one method: given a distance to the target,
return the flywheel speed to shoot from there.

Two classes implement it. One computes a speed from a curve; the other reads one
off two measurements somebody took on the practice field. Something in this
project hands them out, and `ShooterConsole` prints what each would do.

Ignite's real shooter has this exact shape — `LaunchRequestBuilder` with a
parabolic implementation and a mapped one.

Start by seeing it work:

```bash
./gradlew shooterConsole
./gradlew test
```

Three tests pass and four fail. The four are part 1.

---

## Part 1 — add a third implementation and wire it in

The failing tests describe a third range table: it is registered under the name
`flat`, and it returns 3000 RPM no matter how far away the target is. A dumb
table, and a useful one — it is what you use when the tuned numbers are wrong and
you need something predictable.

Write it, and wire it in so that asking for it by name gets it. Finding where
that wiring lives is part of the task.

Either a class or a lambda is a real implementation of an interface with one
method. Pick one and be ready to say why.

```bash
./gradlew test
```

All seven green.

**Do not edit anything under `src/test/`.** Those tests are part of the fixture
and CI checks that they have not changed. If you think one is wrong, say so —
you might be right, and a mentor will look.

---

## Part 2 — take a supplier instead of a value

`ShooterConsole.report` takes a `double` distance. Change it to take a
`java.util.function.DoubleSupplier` instead, and make everything that calls it
work again.

```bash
./gradlew shooterConsole
```

Same output as before. No test covers this part.

**Then answer the question, out loud, to the mentor:**

> What changed about *when the distance is read*?

That is the whole test-out. Part 1 checks you can implement an interface. Part 2
checks you understand the thing M2 exists to teach, and the difference between a
passing answer and a failing one is entirely in this explanation. "It's a fancy
double" is a failing answer.

Follow-up you should be ready for: the console prints once, so nothing about it
actually needed a supplier. Say what kind of caller *would* need one, and why.

---

## How you pass

1. Part 1's tests are green.
2. Your part-2 explanation is correct.

If 45 minutes runs out, that is not a failure of anything. It means M2 is where
you start, which is the answer the test-out exists to find.
