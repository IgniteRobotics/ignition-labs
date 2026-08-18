# Defense review — the question bank

**Published openly, on purpose.** A defense review you can prepare for is still a
defense review. If reading this list first means you write a diff you can explain, the
list has done its job — that is the outcome it exists to produce, not a leak.

**For the mentor:** this is a bank, not a script. Pick the questions the diff actually
raises. The pass condition is not "answered all of them," it is that the student can
account for their own code without help.

---

## How this works

The student walks a mentor, or a peer who has finished M7, through **every decision in
the diff**, unaided — no AI open, no notes. Expect it to take twenty minutes.

The rule that makes it work: **anything the student cannot defend comes out and gets
rewritten by them.** Not the whole pull request, just that part. There is no penalty for
this, and a review that removes something is a successful review, not a failed one.
Finding the gap is the entire point.

## Questions about the code

Asked against specific lines in the diff. The four the milestone names:

- Why this control mode?
- What happens if this sensor reads zero?
- Where does this number come from?
- What happens if this is interrupted halfway?

And more that apply to most diffs:

- Walk me through what happens on the first loop after this is enabled.
- What did you consider and reject? Why?
- Which line here are you least sure about?
- What happens the first time this runs at a competition, on a cold robot?
- If this breaks in two months, what will the symptom look like?
- What in this diff did you write, and what did the tool write?
- Point at a line you changed after the tool produced it, and say why.

## Questions about the tests

- What would have to break for this test to fail?
- Does this test pass against code that does nothing? Show me.
- What does this test *not* cover?
- Why did you test at this level rather than the one below it?

## Questions from the done list

Each maps to a Definition-of-done item and can be asked directly:

- **Shepherding and drifting.** Name both. Describe a time in this feature where you did
  one of them. (A student who has genuinely used AI substantially has done both; an
  answer of "neither happened" usually means the session was short or the memory is
  generous.)
- **Interface-level versus decision-level trust.** What is the difference, and which one
  did you extend to the tool in this diff?
- **The review prompt.** You read `reference/code-review-prompt.md`. Name two standards
  it holds this repository to. (Two is a low bar on purpose — the point is that the
  student opened it.)
- **Miscalibration.** Show me the wrong answer you elicited. What made it convincing?

## What a pass sounds like

| Weak | Strong |
|---|---|
| "That's what the AI suggested." | "It suggested that; I checked it against how the subsystem is initialised and it holds because…" |
| "I think it's the standard way." | "I picked it over the alternative because of X. The tradeoff is Y." |
| "It works, the tests pass." | "It works, and here is the case I expected to break it, and why it doesn't." |
| "I'd have to look." | "I'd have to look" — *about one line, having explained the rest.* |

The last row is not a joke. Nobody holds a whole diff in their head perfectly. A student
who explains the diff and flags one line they want to re-check is doing better than one
who has an answer for everything.

## Failing well

A student who cannot defend three-quarters of their diff has not failed the milestone —
they have discovered that they shipped code they do not understand, which is the exact
thing M7 exists to surface. The response is: cut what they cannot defend, rewrite it
themselves, and come back. That is a completed M7, not a retry.
