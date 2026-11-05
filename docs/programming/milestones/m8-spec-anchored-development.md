# M8 — Spec-Anchored Development

## At a glance

What happens when a spec meets its second change.

**Estimated time:** 2 sessions
**Prerequisites:** **M7 complete, and your M7 PR merged (merging it archived your change).** Your PR carried the `openspec archive` commit — if it did not, M8 has nothing to anchor to. Ask your mentor before your first session
**AI posture:** 🟢 Green — and the one thing M7 held back is handed over here

## Who enters here

Everyone, after M7. You are extending the change *you* shipped, so there is nothing to wait for beyond your own merge.

Your mentor needs one thing ready before you start: a genuine second change to the capability you specified. Not a scenario you forgot — a real difference in what the robot does.

## Test-out challenge

**There is none.** Same as M7: the assessment is the work, and it is the part you cannot skip to.

## Concepts

### What M7 could not teach

Every requirement you have ever written is an `## ADDED Requirements` block against an empty `openspec/specs/`. There was nothing on disk to write *against*. That is not a hole in how you were taught — it is the whole of what a first spec can be.

Which means M7 could only check one property: **can you defend your decisions?** You wrote the document, you built against it, you walked a mentor through the diff and answered for every line. A spec that is never reopened is a deliverable you handed in, and a deliverable is graded on its author.

The moment it has to change, it stops being that. There is now a file asserting the system behaves one way, and you are about to make it behave another. Nobody is going to ask you to defend that file. They are going to **read** it and act on what it says, six weeks from now, with you not in the room.

So M8 checks a different property: **is your spec true, and can somebody else act on it?** Being articulate does not get you that one. The assessment is not a conversation you drive — it is somebody else trying to use your work while you stay quiet.

### `MODIFIED` and `REMOVED`

Two delta types you have not used. The syntax:

```markdown
## MODIFIED Requirements

### Requirement: <name, matching the existing one>
The system SHALL <the full new behaviour, restated>.

#### Scenario: <every scenario the current spec has, plus any new ones>
- GIVEN <state>
- WHEN <action>
- THEN <outcome>

## REMOVED Requirements

### Requirement: <name>
<why it is going>
```

**`MODIFIED` replaces the whole block, and the tool enforces it.** Drop a scenario the settled spec still carries and validation stops you by name:

> `✗ [ERROR] MODIFIED "<name>" omits scenario(s) the current spec still has: "<scenario>".
> Copy them into the MODIFIED block (a MODIFIED requirement replaces the whole block, so
> archive refuses to drop them).`

That message is the lesson stated better than we would state it. **You cannot change one clause of a requirement without accounting for every scenario attached to it.** Read the whole thing before you touch it is not advice here; it is a precondition, held by something other than a mentor's attention.

`REMOVED` is the harder one to do honestly. It claims that behaviour the team once decided on is no longer wanted, and once the change archives, the requirement is gone from the capability spec — the only surviving record of why is the sentence you wrote under it.

### What validation does not catch

`openspec validate`, with or without `--strict`, enforces **completeness, not truth.**

It will not let you silently drop a scenario. It will cheerfully accept one you reworded into a falsehood — a GIVEN/WHEN/THEN describing behaviour your code has never had, in a document that now reads as settled fact. Every heading is in the right place and the exit code is zero.

That gap is the entire reason the assessment is a person reading rather than a command running.

### Spec rot, and the `## Purpose` line

A spec describing what the code *used to* do is worse than no spec, for the reason [`coding-standards.md`](../coding-standards.md) gives about a constitution that quietly omits a standard: it tells a reader the rules while the system runs on different ones, and it is believed **because** it exists. Nobody double-checks a document that looks maintained.

Two instances of it are already waiting for you, and both are yours to fix:

- **The `## Purpose` stub.** Archiving a new capability creates its spec with `## Purpose` reading `TBD - created by archiving change <name>. Update Purpose after archive.` Left alone, every capability in `Robot-2026` says TBD, and the settled specs become a list of requirements with no statement of what the capability is *for*. You are reading the whole file anyway in order to modify it; writing that sentence costs a minute and forces you to say what the thing is, not just what it does.
- **Your own drift.** Your M7 implementation almost certainly diverged from the spec you wrote — M7's done list only asked you to *name* the divergence, not to repair the document. So the first spec you meet in a rotted state is one you rotted, six weeks ago, on purpose, and nobody was careless. That is the cheapest possible demonstration of how this happens.

The rule that follows: **if you change behaviour a spec describes, the spec change ships in the same pull request as the code.** Not a follow-up. A spec corrected later was wrong in the interim, and the interim is where somebody trusted it.

### Where Ignite stops, and the argument you now get to have

M7 named three levels. *Spec-first:* the spec guides the build, then you are done with it. *Spec-anchored:* it survives into maintenance. *Spec-as-source:* the spec is the artifact, the code is generated, and the tooling marks it `DO NOT EDIT`.

M7 did the first. **This milestone is the second, and it still refuses the third** — a student who never edits code did not learn to write code, which is the failure `ai-use-standard.md` §1 exists to prevent.

**You are now expected to argue with that**, and making the strongest case *for* spec-as-source is a done item on this milestone. Not a strawman of it: the real case, the one its advocates make, well enough that somebody who believes it would recognise their position. Then say why we stop short.

The line is a judgement about what a training team should optimise for. It is not a law of nature, and a student who can only recite it has not met it. Argue us out of it and you will have done something worth more than the milestone.

## Project

**Extend the change you shipped at M7 — then hand your spec to somebody who has not seen the code.**

1. Open the capability spec your M7 change archived into, at `openspec/specs/<capability>/spec.md`. **Repair the drift** between what it says and what you actually shipped, and write its `## Purpose`
2. Take the second change your mentor identified. A real change in behaviour — a scenario you forgot to write down is not a modified requirement
3. Start from an issue: your change has one, and the PR says `Closes #<num>`. Then `openspec new change <name>`, then write the change the way you did at M7 — `proposal.md`, `tasks.md`, and the delta spec — with the `MODIFIED` (or `REMOVED`) requirement restated in full. The command scaffolds the folder and a `.openspec.yaml`, and stops there; all three files are yours. `MODIFIED-EXAMPLE.md` in the project walks a worked delta, before and after, with the reasoning for each edit
4. Run `openspec validate <name>` until it passes. Commit the change folder before your first implementation commit
5. Build it. Tests pass and cover the new behaviour
6. **Sit the second-reader test.** `SPEC-REVIEW.md` in the project is the protocol: another student reads your updated spec alone — not the diff, not the code — and tells you what the system now does, including at least one edge case. If what they describe is not what you built, the spec is wrong, whatever the code does. Fix it and they read again. Failing is normal and is not a retry; **report how many round trips it took**
7. Open the PR, spec change and code change together. Check off every box in `tasks.md` as you finish it — `openspec archive` refuses to run while any of them is open. Once your mentor has approved spec and code, you run it as the last commit of your PR

**On drafting.** M7 forbade `openspec-propose` outright. Here you may use the skill, as a first draft and nothing more: you say in the pull request that you used it, and you show the red-line — at minimum one requirement you rewrote and one scenario you added or cut. A draft accepted unchanged is not a pass.

This is the same trade M6 made when it unlocked Green. The tool arrives once you have demonstrated you do not need it, and you wrote a conforming spec by hand before anyone trusted you to correct one. At M7 you wrote and the agent critiqued; here the agent drafts and you critique. The skill being checked has not moved — it is still whether the decisions are yours.

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

## Common failure modes

**The touch-up that is not a change.** Adding a scenario you meant to write at M7, or tightening a sentence, is editing a document — not modifying a requirement. There is no second reader worth the name if nothing about the system is different. This is why the mentor picks the change and you do not.

**The rubber-stamped draft.** Taking what `openspec-propose` hands back and shipping it with the wording tidied. It is less work than an M7 student did, on a harder milestone, and the second-reader test finds it — you cannot answer for the edge cases in a document you never really wrote.

**The spec fixed in a follow-up PR.** The code merges Monday, the spec catches up Thursday, and for three days the file said something untrue to everyone who read it. The interim is exactly where somebody trusts a spec.

**Reading a green `validate` as a correct spec.** Exit zero means the shape is right and no scenario went missing. It says nothing about whether any sentence in the file is true.

**Leaving `## Purpose` at TBD.** It is one line, it is the only place the capability is described rather than enumerated, and it is the first thing the next person reads.

## AI posture — 🟢 Green

Full agentic use, on the same four conditions as M7, all of which must hold:

1. You applied spec-driven development successfully — a conforming OpenSpec change committed first, the feature built against it, and you can say where the two diverged
2. Tests exist, cover the new behavior, and pass
3. AI use is disclosed in the PR
4. You defend the diff in review

What changes at M8 is the spec-drafting rule, and only that: an agent may produce your first draft, provided you red-line it and show the red-line. Everything else about the tier is unchanged.

See [`ai-use-standard.md`](../ai-use-standard.md) §4 for the tier in full. It is v1.3 as of this milestone; if you signed v1.2, you sign again.

## Resources

See `resources.md` for the full library. For this milestone:

- **`ai-use-standard.md`** — §4 Green, and the drafted-spec boundary M8 unlocks. Read the boundary before you ask the agent to draft one, not after
- **OpenSpec** ([docs](https://github.com/Fission-AI/OpenSpec/tree/main/docs)) — `MODIFIED` and `REMOVED` are the parts of the delta format M7 never needed. Settled behaviour in `openspec/specs/`, in-flight changes in `openspec/changes/`, merged ones in `openspec/changes/archive/`
- **Ignite:** [`coding-standards.md`](../coding-standards.md) — the constitution. The decisions that hold across every change, as opposed to the ones your spec carries for this one
- **Böckeler, *Understanding Spec-Driven Development*** — the source of the spec-first / spec-anchored / spec-as-source ladder. Read it before you argue the last done item, from either side
