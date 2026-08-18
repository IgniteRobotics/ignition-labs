# Miscalibration — eliciting a confidently wrong answer

One done item on this milestone cannot be satisfied by reading: **you have to make an AI
tool be confidently, specifically wrong, and then look closely at how convincing it
was.**

Everyone is told that these tools hallucinate. Almost nobody has watched one do it about
something they personally know the answer to. That gap is the whole exercise. Being told
costs you nothing and teaches you nothing; being fooled for ten seconds by an answer in
your own domain recalibrates you permanently.

---

## How to do it

**Ask for something you already know the answer to, where being wrong is checkable.**

Two reliable prompts:

- **A tuning constant for your robot.** "What kP should I use for a shooter flywheel
  running two Kraken X60s at 6000 RPM?" There is no way to know this without the
  mechanism. You will very likely get a specific number anyway.
- **A WPILib API from a few seasons ago.** Ask how to do something using a class or
  method signature that changed. The answer will often be fluent, well-structured, and
  describe an API that no longer exists.

Then check it. The number against your own mechanism, the API against the current
WPILib documentation.

**What to look for is not that it was wrong — you knew that going in. It is what the
wrongness looked like.** Was it hedged, or confident? Did it show its work? Was the
format identical to the format it uses when it is right? That last one is the finding
that matters, and it is why "I'll be able to tell" does not survive contact.

---

## Write it up

Fill this in and bring it to your defense review.

### The prompt I used

```
[paste exactly what you asked, including any context you gave it]
```

### What it told me

```
[paste the answer verbatim. Do not clean it up or trim it -- the shape of the
wrong answer is the data. Include the confident bits.]
```

### The tool and version

```
[which tool, and roughly when -- these change fast, and an answer that was wrong
last month may not be this month]
```

### How I knew it was wrong

[What did you check it against? Be specific -- "the docs" is not an answer, which
page is.]

### What made it convincing

[The important one. What about the answer would have made you accept it if you had
not already known better? Confidence? Specificity? Correct-looking format? A
plausible explanation attached to a wrong number?]

### Would I have caught this in code review?

[Honestly. If this had arrived in a pull request from a teammate rather than from a
prompt you wrote to trap it, would you have flagged it?]

---

## The part that carries forward

The reason this is a done item and not a demo: **the failure mode is not that the tool is
wrong, it is that wrong output looks exactly like right output.** There is no tell. Which
is why every other rule in `ai-use-standard.md` is about verification rather than about
detection — you cannot detect it, so you have to check it.
