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
| `SPEC-TEMPLATE.md` | A worked spec, and where yours goes. Read it before you write yours |
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
prompt that generates the review you are about to receive. The standards it enforces are
written down for humans in `coding-standards.md` (**Draft, not ratified**), and that
document — not the prompt — is the source; naming two standards out of it is one of this
milestone's done items. Knowing
what a reviewer is looking for before it reviews you is not cheating; it is how code
review is supposed to work.

See `reference/README.md` for the pinned SHA, each file's original path in
`Robot-2026`, and how to re-verify a copy is still faithful.

## Where the work happens

Your branch is on `Robot-2026`, not here. You need commit access — ask a mentor if you
do not have it yet.

Your spec goes on that branch too, in `openspec/changes/<your-change>/`. Nothing in this
training repository runs OpenSpec — the tool runs in your clone of `Robot-2026`.

## Setting up OpenSpec

**Ignite uses OpenSpec, and M7 requires it.** Node 20.19 or newer is a prerequisite for
this milestone in the same way the WPILib toolchain is a prerequisite for M3. Install it
before your first session; if your laptop cannot run it, that is a mentor problem to solve
in advance, not something to discover at the keyboard.

```bash
node --version                                    # 20.19 or newer
npm install -g @fission-ai/openspec@latest
openspec --version
```

The `openspec/` folder already exists on `Robot-2026` — a mentor seeded it, deliberately,
so that one student's tool choice does not become the season repo's. **You do not run
`openspec init`.** If `openspec/` is not there, stop and ask a mentor; that first commit
sets the layout for everyone who follows you and it is not yours to make.

Documentation: <https://github.com/Fission-AI/OpenSpec/tree/main/docs>

### Your commands

```bash
openspec new change <your-change>        # scaffolds the folder and its files
openspec validate <your-change>          # must pass before you commit. This is a done item
openspec list                            # every change in flight
openspec show <your-change>              # print yours
openspec view                            # dashboard
```

After your pull request merges, a mentor runs `openspec archive <your-change>`, which
folds your delta into `openspec/specs/` and moves the folder into `changes/archive/`.

### The one command you do not run

`/opsx:propose` has the agent draft your proposal, delta spec, and tasks. **Do not use
it.** `ai-use-standard.md` §4 makes the spec Yellow-tier work even at Green, because the
spec is where your decisions live — an agent that writes it has made the decisions and
left you the typing, and the defense review becomes you defending a document you never
thought through. Same reason you wrote the tests that unlocked Green at M6 instead of
generating them.

Hand the agent your finished draft and ask *"what did I miss?"* That is the best prompt in
this milestone. Critique, not authorship.

## The order

1. **Pick a feature** from `FEATURE-BACKLOG.md`. Read the selection criteria at the top
   of that file, because you will eventually be asked to add entries to it yourself
2. **Write the spec, and commit it before you start.** `openspec new change <your-change>`,
   then write `proposal.md`, the delta spec under `specs/<capability>/spec.md`, and
   `tasks.md`. Run `openspec validate <your-change>` until it passes, then commit the
   folder. `SPEC-TEMPLATE.md` walks a complete one. You write this yourself — you may ask
   an agent what your spec missed, never to write it
3. **Use AI assistance genuinely and substantially, against the spec you wrote.** This is
   the one milestone where that is the assignment rather than a concession
4. **Write or verify tests covering the new behaviour.** They must pass. `build.gradle` is
   already wired for JUnit, most classes still have no coverage, and if `src/test/` does
   not exist yet on your branch, you create it
5. **Open the pull request** using `pull-request-template.md`, disclosure block filled
   in. "How I verified it" must be specific
6. **Defend the diff in review** — every decision, unaided, to a mentor or a peer who
   has finished M7, including where the build diverged from your spec and why.
   `DEFENSE-REVIEW.md` is the question bank
7. **Anything you cannot defend comes out** and gets rewritten by you. Not the whole
   pull request — just that part. There is no penalty. Finding the gap is the point
8. **Do the miscalibration exercise** in `MISCALIBRATION.md`

## What is assessed, and how

Nothing here is graded by a test. `Robot-2026` runs its own build on your pull request,
and the AI reviewer will comment — but neither of those is the assessment. **The
assessment is step 6.** A green build on a diff you cannot explain is a failed M7.

## Definition of done

- [ ] Feature merged with a filled-in AI-use disclosure
- [ ] Tests pass and cover the new behavior
- [ ] You defended every decision in the diff, unaided, in review
- [ ] Anything you could not defend was removed and rewritten by you
- [ ] You can name shepherding and drifting and describe a time you did one
- [ ] You deliberately elicited a confidently wrong answer and can describe what made it convincing
- [ ] You read the coding standards (`coding-standards.md`) and can name two standards in it
- [ ] You have read and signed the AI-use standard
- [ ] You can explain the difference between interface-level and decision-level trust
- [ ] `openspec validate` passes on your change, and the change folder was committed before your first implementation commit
- [ ] You can name one place the implementation diverged from your spec, and say why

Every one of these is mentor-verified. There is no test to make green.
