# The second-reader test — the protocol

**Published openly, on purpose**, the same as M7's question bank. Knowing exactly how you
will be read is not a leak; it is the outcome this document exists to produce. A student who
writes a spec that survives this because they had this page open has written a spec that
survives it.

**This is M8's assessment.** Not a green build, not a passing `openspec validate`, and not a
conversation you drive. Somebody who has never seen your code reads your spec and tells you
what your robot does. If they get it wrong, the spec is wrong.

**It needs no mentor.** Two students and fifteen minutes. Run it yourselves.

---

## Why this instead of another defense review

M7 already assessed by defense review, and it assessed the right thing: can you account for
your own decisions? You could, out loud, with the diff on the screen.

A spec is worth having for a different reason. Its value is that a future person can trust it
**without reading the code** and **without you in the room** — which means the only honest
test of one is that person, early. Nothing about being articulate helps here. You are not
being asked anything.

It teaches both of you, and the reader arguably gets more out of it: consuming somebody
else's spec is the thing you will actually spend a career doing, and this is the first time
anyone has asked you to practise it.

---

## The protocol

### Who reads

**Any student who has finished M7 and has not seen your diff.** Not your mentor — a peer. If
they have already looked at your pull request, or you have talked them through the change,
they are disqualified as a reader for this change. Find someone else; they are not scarce.

### What they get

**One file: the capability spec as it will read once your change archives.** Nothing else.
Not the change folder, not the proposal, not the pull request, not the code.

That file does not exist on disk yet — `openspec archive` is the last commit of your PR, so at
this point the settled spec on your branch carries your drift repair and your `MODIFIED` and
`REMOVED` blocks are still in the change folder. Paste them together into a scratch copy and
hand that over. Two minutes, and it makes you read your own end state, which otherwise
nobody does until the next person needs it.

**Apply the three merge rules exactly, and change nothing else.**

- A `MODIFIED` block **replaces** the matching requirement in the settled spec, whole. Drop
  the `## MODIFIED Requirements` heading, then put the `### Requirement:` block and every
  scenario under it in place of the old one.
- A `REMOVED` block **deletes** the matching requirement. Its argument paragraph does not get
  pasted in — that text lives in the change folder and never reaches the capability spec.
- An `ADDED` block **appends** its requirement to `## Requirements`, in the position it will
  occupy after archive. Your change may well carry one alongside the `MODIFIED` or `REMOVED`
  requirement — new behaviour is frequently a new requirement. Leave it out and the reader
  cannot describe behaviour your spec does actually specify, which records a round trip you
  did not really fail.

**You are retyping the document you are about to be assessed on. Do not improve it while you
paste.** A clarifying word added here passes a test the spec that actually ships would have
failed, and it is the one way to fake this whole exercise without meaning to. Step 5 below
exists to catch it.

`proposal.md` is specifically excluded. It explains your reasoning, which is exactly the
crutch this test is trying to remove.

### What the reader does

Reads the file, then says out loud, in their own words:

1. **What the system now does** — the behaviour, not the headings back at you.
2. **At least one edge case**, and what happens in it. This is the part that finds problems.
   Anyone can paraphrase a SHALL sentence; predicting what happens at a boundary requires
   the spec to actually say something.

They should also say what they were unsure about, and where they had to guess. **A guess that
turned out right is still a finding.** Write it down.

### What the author does

**Nothing. You are silent while they read and while they talk.**

This is the whole difference between this and a defense review, and it is the rule most
likely to get broken by accident. No clarifying, no "well, what I meant was", no watching
their face and jumping in. Do not answer a question about the spec — if they have to ask,
that is the result. Write the question down and stay quiet.

You may say two things: *"keep going"* and *"is that everything?"*

### The verdict

**If their description does not match what the code does, the spec is wrong** — regardless
of whether the code is right.

You are the one who notices, because you are the only person in the room who knows the code
— which is the other reason you stay quiet through the description. The reader confirms it
afterwards, once they have read the diff.

That is not a technicality and it is not softened by the code being correct. A spec that
describes something other than the running system is the failure mode M8 is built around: it
is trusted *because* it exists, and it is wrong. Fix the spec. Then they read again.

If the reading turns up a genuine bug in the code, good — but fix that separately and do not
let it stand in for a spec fix.

### Round trips are the signal, not the penalty

Count them. Report the number, and what you changed each time. That is a done item, and it
is reported rather than hidden on purpose.

| Round trips | What it usually means |
|---|---|
| 0 | Suspicious. Usually the change was too small to have a second reader, or the reader had context they should not have had |
| 1 | Fine, and common on a small change. Say what the one fix was |
| 2–3 | Normal, and the most interesting outcome. This is what a spec meeting a real reader looks like |
| 4–5 | Getting long, but not a failure. Look at whether the same ambiguity keeps resurfacing in a new place |
| 6+ | Worth stopping and asking whether the requirement is trying to say two things at once |

**Those are expectations, not measurements.** Nobody has run this test yet — the numbers come
from what the milestone's design argues should happen, not from a cohort. The first mentor to
sit a real one should correct this table, and correcting it is worth more than agreeing with
it.

Failing is not a retry and carries no penalty. A student who reports three round trips and
what each one fixed has done the milestone better than one who reports zero.

---

## Running it, start to finish

1. Author prepares the one file and hands it over. Says nothing about it.
2. Reader reads. Ten minutes is plenty; this is one screen.
3. Reader describes the behaviour and at least one edge case. **Author is silent.**
4. Author writes down every mismatch, question, and guess — including the lucky ones.
5. Reader now reads the diff — **and the change folder with it**, checking the scratch copy
   they were handed against the `MODIFIED` and `REMOVED` blocks that will actually ship. Both
   of you can talk from here.
6. Author fixes the spec. Not the wording of the complaint — the thing that caused it. **Then
   re-derive the scratch copy from the corrected change folder**, by the same three merge
   rules. The old copy is now out of date, and handing it back reads a round the fixes never
   reached.
7. Round two — see below. Same reader, the re-derived file.
8. Author records the round-trip count and what each round fixed, and puts it in the pull
   request.

**Round two is not another cold read, and it cannot be** — the reader has seen the diff by
then, and nobody can un-see it. It asks a narrower question, which is the right one at that
point: *does the corrected spec now say the thing you had to read the code to find out?* Keep
the same reader rather than burning a fresh one, because they are the only person who knows
exactly what the old wording made them believe.

Every round after a fix reads a freshly derived copy, so the file in front of the reader is
always the one the corrected change folder produces. Round three re-derives again, and so on.

The cold read happens once, at the top, and it is the expensive part. Protect it.

**How the done box gets marked.** *"A second reader described the new behaviour correctly from
your spec alone"* is ticked on the reader's **final** description matching what you built. It
is not ticked on the cold read — the cold read is what the **round-trip count** records, and
the count is a separate done item precisely so this one does not have to double as a score. A
spec that took three rounds to get there has met the item. Read the other way, the honest
answer would be unmarkable and only a suspicious zero could tick the box.

---

## For the mentor

You do not need to be in the room, and mostly you should not be — the point is that this runs
on two students. What you own:

- **Check the reader is clean.** The most common way this test gets hollowed out is a reader
  who already knows the change, usually because they sat next to the author for a week.
- **Check the scratch copy matched what ships.** The author hands over a file they retyped,
  which means an author who clarifies while pasting can pass a test the real spec would have
  failed — without ever deciding to cheat. Step 5 puts the change folder in front of the
  reader for exactly this; if it did not happen, do it yourself before you sign the box. Any
  wording that exists only in the scratch copy is a finding, not a typo. **Compare the final
  scratch copy against the final change folder** — the copy is re-derived after every fix
  (step 6), so the one that has to match what ships is the last one, not the one the cold read
  used.
- **Read the round-trip count, not just the checkbox.** Zero is the answer worth a question.
  So is a count of three with no note of what changed each time.
- **This is where a rubber-stamped `openspec-propose` draft surfaces.** A student can tidy the
  wording of a document they did not think through; they cannot stay quiet while somebody
  else gets its edge cases wrong and then explain what they meant to say. If the author keeps
  interrupting, that is a signal about the spec, not about their manners.
- **Both students are being taught.** The reader is practising the more common real-world
  skill by a wide margin, and a student who has read three of these before writing their own
  M8 spec writes a noticeably better one. Pair people deliberately.
