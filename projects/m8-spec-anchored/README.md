# M8 — Spec-Anchored Development

**There is no skeleton here either, and this time there is not even a backlog.** Your
subject is already decided: you are extending the change you shipped at M7, in the
capability spec your own change archived into. Nothing to pick, nothing to wait for beyond
your own merge.

Two sessions is normal.

---

## What is in this directory

| File | What it is for |
|---|---|
| `MODIFIED-EXAMPLE.md` | A worked second change — the settled spec, the `MODIFIED` and `REMOVED` delta, and the reasoning for each edit. Read it before you write yours |
| `SPEC-REVIEW.md` | The second-reader test. This is the assessment. Published on purpose, and you should read it before you write the spec it will be used on |

That is the whole of the scaffolding, and it is deliberate. By M8 you have written a
conforming spec by hand, built against it, and defended it. What is left is one worked
example of the two delta types you have not used.

## Where the work happens

Your branch is on `Robot-2026`, not here — same as M7. **Nothing in this training repository
runs OpenSpec.** There is no `openspec/` directory here and no CI wiring; the tool runs in
your clone of `Robot-2026`.

You need one thing to exist before you start: **your M7 PR must be merged.**
Its last commit was your `openspec archive <your-m7-change>`, which folded your delta into
`openspec/specs/<capability>/spec.md`. If that commit was not there, M8 has nothing to
anchor to. Ask your mentor before your first session
rather than at the keyboard.

Your mentor also needs a second change in mind for your capability — a real difference in
what the robot does, not a scenario you forgot to write down. They pick it, not you.

## The commands

The same CLI you installed at M7. Nothing new to set up.

```bash
openspec new change <name>        # creates the folder. All three files are yours to write
openspec validate <name>          # must pass before you commit. This is a done item
openspec list                     # shows your change as ✓ Complete once every task box is checked
```

`openspec new change` scaffolds the folder and a `.openspec.yaml`, and stops there — no
`proposal.md`, no `tasks.md`, no `specs/`. You write all three, the way you did at M7.

Once your mentor has approved spec and code, you run `openspec archive <name>` as the last commit of your PR. **It refuses to run while any box in
your `tasks.md` is still open**, so check them off as you finish them rather than at the end.

### `openspec-propose`, and the rule that lifts here

`openspec-propose` is a **skill you ask your agent for**, not a command you run. OpenSpec
installs it under `.agents/skills/` and installs no slash-command adapter, so there is
nothing to type — you ask the agent to propose the change and it drafts the proposal, the
delta spec, and the tasks in one step.

M7 forbade it outright. **Here you may use it, as a first draft and nothing more.** You say
in the pull request that you used it, and you show the red-line: at minimum one requirement
you rewrote and one scenario you added or cut. A draft you accepted unchanged is not a pass —
it is evidence you did not review it. `ai-use-standard.md` §4 carries the boundary in full;
it is v1.3 as of this milestone, and if you signed v1.2 you sign again.

At M7 you wrote and the agent critiqued. Here the agent drafts and you critique. The skill
being checked has not moved.

## The order

1. **Open your capability spec and repair it.** `openspec/specs/<capability>/spec.md`, on
   your branch. Two things are waiting there and both are yours: the drift between what the
   spec says and what you actually shipped at M7 — M7 only asked you to *name* that
   divergence, not to fix the document — and the `## Purpose` line, which archiving left
   reading `TBD - created by archiving change <name>. Update Purpose after archive.` One
   sentence saying what the capability is *for*, not what it does
2. **Take the second change your mentor identified.** Read the whole capability spec first.
   You are about to restate a requirement in full, so you need to know every scenario hanging
   off it before you touch a word of it
3. **Write the change.** `openspec new change <name>`, then `proposal.md`, `tasks.md`, and
   the delta at `specs/<capability>/spec.md` — with the `MODIFIED` or `REMOVED` requirement
   restated in full. `MODIFIED-EXAMPLE.md` walks a complete one
4. **`openspec validate <name>` until it passes**, then commit the change folder — before
   your first implementation commit, same rule as M7
5. **Build it.** Tests pass and cover the new behaviour
6. **Sit the second-reader test**, per `SPEC-REVIEW.md`. Another student reads your updated
   spec alone and tells you what the system now does. If it does not match what you built,
   the spec is wrong and you fix it and they read again. **Report how many round trips it
   took**
7. **Open the pull request with the spec change and the code change together.** Not a
   follow-up. A spec corrected later was wrong in the interim, and the interim is where
   somebody trusted it
8. **Be ready to argue for spec-as-source** — the position Ignite refuses — well enough that
   somebody who holds it would recognise it. Then say why we stop short

## What is assessed, and how

**Step 6.** Not the build, not `openspec validate`, and not a conversation you drive.

`openspec validate` enforces **completeness, not truth.** It will refuse a `MODIFIED` block
that drops a scenario the settled spec still carries — by name, which is the most useful
error message in this milestone. It will also accept, at exit zero, a scenario you reworded
into something your code has never done. Every heading in the right place, every sentence
false.

That gap is the whole reason the assessment is a person reading rather than a command
running. Everything else here is mentor-verified, and there is no test to make green.

## Definition of done

- [ ] You repaired the drift between your M7 spec and what you actually shipped, and wrote the capability's `## Purpose` line
- [ ] Your change folder contains a `MODIFIED` or `REMOVED` requirement, restated in full
- [ ] `openspec validate` passes, and the spec change shipped in the same pull request as the code
- [ ] A second reader described the new behaviour correctly from your spec alone
- [ ] You can say how many round trips that took, and what you fixed each time
- [ ] You can name something `openspec validate` cannot check about a requirement
- [ ] If you used `openspec-propose`, you can show one requirement you rewrote and one scenario you added or deleted
- [ ] You can make the strongest case *for* spec-as-source, and then say why Ignite stops short of it
- [ ] Your change has an issue, and your PR says `Closes #<num>`
