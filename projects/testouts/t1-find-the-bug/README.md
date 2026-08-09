# T1 — Find the Bug

The M1 test-out. If you pass this, you skip M1 and go to M2.

**Time: 30 minutes.**

---

## The task

A mentor will tell you which variant to open — A, B, or C. Open that one file
in `src/main/java/frc/testout/t1/` and read it.

It contains one short method. The method compiles, it runs, and it is wrong.

Run its test:

```bash
./gradlew test --tests '*VariantATest'
```

(Swap in `VariantBTest` or `VariantCTest` for the variant you were given. Running
`./gradlew test` on its own runs all three and shows you failures in variants you
were not asked about — ignore those.)

---

## How you pass

Three things, in order of how much they matter:

1. **Explain what the bug is** — in your own words, out loud, to the mentor.
2. **Explain why it produced the behaviour the test shows.** Not "it returns the
   wrong number" — *why that particular wrong number*.
3. **Fix it**, and watch the test go green.

The explanation is the pass criterion. Changing characters until the test passes
is not passing, and a mentor will ask you the "why" question either way. If you
found the fix by guessing, say so — that is a fine thing to say, and it is a
better answer than a confident wrong explanation.

If 30 minutes runs out, that is not a failure of anything. It means M1 is where
you start, which is the answer the test-out exists to find.

---

## Do not edit `src/test/`

Those tests are part of the fixture and CI checks that they have not changed.
If you think a test is wrong, say so — you might be right, and a mentor will
look.
