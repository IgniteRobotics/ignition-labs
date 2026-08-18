# M7 — AI-Assisted Development

**There is no skeleton here, and that is the point.** M7 ships a real feature into a
real repository. A sandbox would defeat the one requirement that makes this milestone
work: pick a real, useful feature, not a toy.

Two to three sessions is normal.

---

## What is in this directory

| File | What it is for |
|---|---|
| `FEATURE-BACKLOG.md` | Real, small, shippable features. Pick one from here rather than spending a session choosing |
| `DEFENSE-REVIEW.md` | The questions you will be asked. Published on purpose — a defense review you can prepare for is still a defense review |
| `MISCALIBRATION.md` | The deliberate-wrong-answer exercise, and somewhere to paste what the tool confidently told you |
| `pull-request-template.md` | Copy into your PR description. The disclosure block is required |
| `reference/` | The real AI review pipeline from `Robot-2026`, pinned to the commit this milestone was written against |

## About `reference/`

Those four files are a **copy for reading**, not configuration for this repository.
Nothing in this directory runs. The real versions are live on `Robot-2026`, where they
review every pull request into `main` or `development` — which is what yours will be.
You will meet this tool as a reviewer of your own work, which is the only way the lesson
lands.

Read `reference/code-review-prompt.md` before you open your pull request. It is the
prompt that generates the review you are about to receive, and one of this milestone's
done items is being able to name two standards it holds you to. Knowing what a reviewer
is looking for before it reviews you is not cheating; it is how code review is supposed
to work.

See `reference/README.md` for the pinned SHA, each file's original path in
`Robot-2026`, and how to re-verify a copy is still faithful.

## Where the work happens

Your branch is on `Robot-2026`, not here. You need commit access — ask a mentor if you
do not have it yet.

## The order

1. **Pick a feature** from `FEATURE-BACKLOG.md`. Read the selection criteria at the top
   of that file, because you will eventually be asked to add entries to it yourself
2. **Use AI assistance genuinely and substantially.** This is the one milestone where
   that is the assignment rather than a concession
3. **Write or verify tests covering the new behaviour.** They must pass. Note that
   `Robot-2026` has no tests yet — yours may be the first, and `build.gradle` is already
   wired for them
4. **Open the pull request** using `pull-request-template.md`, disclosure block filled
   in. "How I verified it" must be specific
5. **Defend the diff in review** — every decision, unaided, to a mentor or a peer who
   has finished M7. `DEFENSE-REVIEW.md` is the question bank
6. **Anything you cannot defend comes out** and gets rewritten by you. Not the whole
   pull request — just that part. There is no penalty. Finding the gap is the point
7. **Do the miscalibration exercise** in `MISCALIBRATION.md`

## What is assessed, and how

Nothing here is graded by a test. `Robot-2026` runs its own build on your pull request,
and the AI reviewer will comment — but neither of those is the assessment. **The
assessment is step 5.** A green build on a diff you cannot explain is a failed M7.

## Definition of done

- [ ] Feature merged with a filled-in AI-use disclosure
- [ ] Tests pass and cover the new behavior
- [ ] You defended every decision in the diff, unaided, in review
- [ ] Anything you could not defend was removed and rewritten by you
- [ ] You can name shepherding and drifting and describe a time you did one
- [ ] You deliberately elicited a confidently wrong answer and can describe what made it convincing
- [ ] You read `reference/code-review-prompt.md` and can name two standards in it
- [ ] You have read and signed the AI-use standard
- [ ] You can explain the difference between interface-level and decision-level trust

Every one of these is mentor-verified. There is no test to make green.
