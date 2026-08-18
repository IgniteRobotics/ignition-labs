# Task 2: M7's project directory, the pinned reference copies, and the PR template

M7 ships no code. This task creates the directory, copies the four files that make up `Robot-2026`'s AI review pipeline as **pinned reference material**, and writes the pull request template carrying the disclosure block. The risk here is a category error: these files look like CI configuration and are not. Placing any of them under `.github/` would wire a paid, key-requiring workflow into the student-facing repository to duplicate one that already runs where M7's work actually goes.

**Files:**
- Create: `projects/m7-ai-assisted/README.md`
- Create: `projects/m7-ai-assisted/reference/ai-code-review.yml`
- Create: `projects/m7-ai-assisted/reference/gemini-code-review.py`
- Create: `projects/m7-ai-assisted/reference/requirements.txt`
- Create: `projects/m7-ai-assisted/reference/code-review-prompt.md`
- Create: `projects/m7-ai-assisted/pull-request-template.md`

**Consumes:** Task 1's fix — without it, the first change to this directory breaks CI.
**Produces:** the directory `projects/m7-ai-assisted/` and its `reference/` subdirectory. Task 3 adds three more documents beside them and links to `reference/code-review-prompt.md` from `DEFENSE-REVIEW.md`.

**Lane:** `m7`. Work in `.worktrees/m7`.

---

- [ ] **Step 1: Create the directory and copy the four reference files at the pinned SHA**

```bash
cd /Users/y0shi/workspace/ignite/docs/.worktrees/m7
mkdir -p projects/m7-ai-assisted/reference
R=/Users/y0shi/workspace/ignite/Robot-2026
S=28ce5397d88275001d656dbb717d22cf9ac0150d
git -C $R show $S:.github/workflows/ai-code-review.yml    > projects/m7-ai-assisted/reference/ai-code-review.yml
git -C $R show $S:.github/scripts/gemini-code-review.py   > projects/m7-ai-assisted/reference/gemini-code-review.py
git -C $R show $S:.github/scripts/requirements.txt        > projects/m7-ai-assisted/reference/requirements.txt
git -C $R show $S:.github/prompts/code-review-prompt.md   > projects/m7-ai-assisted/reference/code-review-prompt.md
wc -l projects/m7-ai-assisted/reference/*
```

Expected line counts, as measured at that SHA: `ai-code-review.yml` 71, `gemini-code-review.py` 281, `requirements.txt` 2, `code-review-prompt.md` 311. A mismatch means the SHA moved or the copy went wrong — stop and check before writing prose that describes these files.

**These live under `projects/m7-ai-assisted/reference/` and nowhere else.** Do not create `.github/workflows/ai-code-review.yml` in this repository. See `00-plan.md`'s constraints delta.

- [ ] **Step 2: Verify the copies are byte-identical to the pinned originals**

```bash
R=/Users/y0shi/workspace/ignite/Robot-2026
S=28ce5397d88275001d656dbb717d22cf9ac0150d
for pair in "ai-code-review.yml:.github/workflows/ai-code-review.yml" \
            "gemini-code-review.py:.github/scripts/gemini-code-review.py" \
            "requirements.txt:.github/scripts/requirements.txt" \
            "code-review-prompt.md:.github/prompts/code-review-prompt.md"; do
  local_f="projects/m7-ai-assisted/reference/${pair%%:*}"
  remote_f="${pair##*:}"
  if git -C $R show $S:$remote_f | diff -q - "$local_f" > /dev/null; then
    echo "OK    $local_f"
  else
    echo "DIFFERS $local_f -- stop"
  fi
done
```

Expected: four `OK` lines. A reference copy that has drifted from the thing it claims to be a copy of is worse than no copy.

- [ ] **Step 3: Write the pull request template**

`projects/m7-ai-assisted/pull-request-template.md` — the disclosure block is copied verbatim from `docs/programming/ai-use-standard.md` §6. **Do not paraphrase it.** The standard is the thing students sign; a template that says something slightly different is a second standard nobody agreed to.

````markdown
# Pull request template — M7

Copy this into the description of the pull request you open on `Robot-2026`.
The disclosure block is required on every pull request at Yellow and Green
(`ai-use-standard.md` §6), which includes every pull request you open for this
milestone.

---

## What this changes

[One or two sentences. What does this do that the code did not do before?]

## Why

[Why is this worth doing? If it came from FEATURE-BACKLOG.md, say so.]

## How I tested it

[Specific. "Ran the sim" is not specific. Which test, what did you watch,
what would have told you it was broken?]

## AI use

```
AI use: [none | assisted | generated]
Tool:
What it did:
How I verified it:
```

## Anything reviewers should look at closely

[The part you are least sure about. Naming it is not a weakness -- it is the
single most useful line in a pull request.]
````

**On the three values:** `none` means no AI beyond ordinary non-AI autocomplete; `assisted` means AI helped but you wrote the logic; `generated` means AI produced code that is in the diff substantially as it wrote it. Those definitions are `ai-use-standard.md` §6's, and §6 also carries a filled-in example worth reading before you write yours.

- [ ] **Step 4: Write `README.md`**

`projects/m7-ai-assisted/README.md`:

````markdown
# M7 — AI-Assisted Development

**There is no skeleton here, and that is the point.** M7 ships a real feature into a
real repository. A sandbox would defeat the one requirement that makes this milestone
work: pick a real, useful feature, not a toy.

Two to four sessions is normal.

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
review every pull request — including the one you are about to open. You will meet this
tool as a reviewer of your own work, which is the only way the lesson lands.

Read `reference/code-review-prompt.md` before you open your pull request. It is the
prompt that generates the review you are about to receive, and one of this milestone's
done items is being able to name two standards it holds you to. Knowing what a reviewer
is looking for before it reviews you is not cheating; it is how code review is supposed
to work.

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
````

- [ ] **Step 5: Confirm this directory is invisible to the Gradle tooling**

Task 1 fixed the selector; this confirms it holds for the directory that now actually exists.

```bash
cd /Users/y0shi/workspace/ignite/docs/.worktrees/m7
printf 'projects/m7-ai-assisted/README.md\n' | poetry run python tools/changed_projects.py --from-stdin
```

Expected: `[]`

```bash
ls projects/m7-ai-assisted/settings.gradle 2>&1
```

Expected: `No such file or directory`. There must not be one.

- [ ] **Step 6: Confirm nothing here publishes and nothing here is wired**

```bash
git status --short
```

Expected: only files under `projects/m7-ai-assisted/`. **Nothing under `.github/`.** If `.github/` appears in that list, the reference copy went to the wrong place — remove it and re-read Step 1.

- [ ] **Step 7: Commit**

```bash
git add projects/m7-ai-assisted
git commit -m "Add M7's project directory, pinned reference copies, and PR template

The four files under reference/ are Robot-2026's AI review pipeline at
28ce5397d88275001d656dbb717d22cf9ac0150d, verified byte-identical to the
originals, shipped for reading rather than wired to CI. Running them here would
need a GEMINI_CODE_REVIEW_KEY on the student-facing repository and per-PR spend,
to duplicate a workflow that already reviews the Robot-2026 pull requests M7's
work actually goes to -- where a student meets it as a reviewer of their own
code, which is the only way the lesson lands.

The PR template carries ai-use-standard.md §6's disclosure block verbatim rather
than paraphrased: the standard is what students sign, and a template that says
something slightly different is a second standard nobody agreed to."
```
