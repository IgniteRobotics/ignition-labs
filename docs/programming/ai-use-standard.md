# Ignite Robotics — AI Use Standard for Programming

**Applies to:** All programming subteam members and mentors, FRC 6829 Ignite Robotics
**Covers:** Robot code, scouting tools, internal tooling, and anything else the programming subteam ships
**Version:** 1.3
**Revised:** 2026-08-21 — v1.1 added a fourth condition to Green: a spec, written and committed before the first implementation commit (§4). v1.2 sharpens that condition from *a spec exists* to *you applied spec-driven development successfully*, and makes OpenSpec's format and tooling the standard rather than a suggestion. Anyone who acknowledged an earlier version signs again. 2026-08-22 — v1.3 moves the spec-drafting boundary in §4 to M8: before M8 you still write the spec yourself, full stop; at M8 and after, `openspec-propose` may produce a first draft you disclose and red-line. Anyone who acknowledged v1.2 signs again.
**Effective:** 2026-08-05
**Expires:** End of the 2027 build season — see §11

---

## 1. Why this document exists

FIRST has no rule about AI-generated code. We checked the 2026 Game Manual directly: there is no mention of AI, ChatGPT, Copilot, LLMs, or generative AI anywhere in the robot construction rules, the inspection rules, or anywhere else. No other FRC team we could find has published a written policy either.

So this is not us following someone else's rule. This is us deciding, on purpose, before the pressure of a build season decides for us.

**What we are actually protecting.** The goal of the programming subteam is not to produce robot code. If it were, we would buy it. The goal is to produce people who can produce robot code. A season where the robot works and nobody learned anything is a failed season that happens to have a trophy.

That is the same reason we tell mentors not to grab the keyboard when autonomous breaks in Week 5. A rookie handing a broken subsystem to an AI and a mentor quietly rewriting it produce the identical outcome: working code, no learning, and a student who cannot fix it at the next competition. Those are the same failure. This document closes one of them; the mentor guide closes the other.

**What this document is not.** It is not anti-AI. Several of us use these tools daily and they are genuinely good. Team 254 — the most credentialed programming team in FRC — presented at the 2026 Championship on running AI agents that drive their simulations. The tools are real and they are not going away. This document is about *sequencing*: what you need to know before a given tool helps you more than it hurts you.

---

## 2. What FIRST actually requires

Almost nothing, and you should know that going in rather than assuming this document is passing along a rule from above.

FIRST's only official statement is a November 2023 post from their EVP/COO. It is permissive. It says AI "can assist with code generation" and treats AI tools "in the same way that CAD programs, Programming Languages, and 3D printers are tools available for their use." It asks for one thing: **attribution.** "Whether you're reusing or modifying code from a GitHub library or using generative language model-based tools, acknowledging the source is crucial."

Two things worth noting about that statement. It predates the current generation of coding agents by more than two years. And FIRST said explicitly that they "may revisit this subject in future seasons."

So: FIRST asks you to disclose. Everything else in this document is Ignite's own call, and we own it.

---

## 3. The principle everything else follows from

Read this part even if you skim the rest.

> **The question is never who typed the characters. The question is whether you can explain and defend every decision in what you submit.**

Here is why that framing and not "don't use AI."

Every line of code you write already sits on top of enormous amounts of code you did not write and have never read. When you call `.set()` on a TalonFX, you are invoking tens of thousands of lines of closed-source CTRE firmware and library code. Nobody thinks that makes your code less yours. When you use PathPlanner to follow a trajectory, you are trusting somebody else's math.

Someone will eventually make this argument to you as a reason the whole idea of "student-written code" is already a fiction. It is a good argument and it deserves a real answer rather than a dismissal.

The answer is that there are two different kinds of trust:

**Interface-level trust.** You trust that CTRE's `VelocityVoltage` control request does what its documentation says. You do not need to defend how it works internally. You *do* need to know what it does, what units it takes, and when it is the right choice. The contract is yours to understand; the implementation is not.

**Decision-level trust.** When you pick a control strategy, decompose a mechanism into subsystems, choose a gain, or structure an autonomous routine, you are making an engineering decision. That decision is the thing the programming subteam exists to teach you how to make. If an AI made it and you cannot explain why it is correct, you did not learn the thing, and — more practically — you cannot fix it when it breaks at 9pm before a match.

**So the line is not AI versus not-AI. It is dependency versus decision.** A vendor library is a dependency. AI-generated control logic is a decision wearing a dependency's clothes.

---

## 4. The three tiers

We do not have one blanket rule, because the risk genuinely is not the same for a student in week two as for a student who has been doing this for three years. MIT's EECS department does the same thing — their AI policy varies by course, based on what each course is trying to build in a student.

Your tier is determined by the highest curriculum milestone you have completed. See `curriculum-spec.md` for the milestone ladder.

### 🔴 Red — milestones M0 through M2

**You are learning what code is.**

**Allowed:**
- Asking an AI to explain a concept: "what is a constructor," "explain the difference between `int` and `double`"
- Asking what an error message means
- Asking for a concept to be explained a different way when the first explanation did not land
- Asking it to quiz you

**Not allowed:**
- Asking it to write code for you, in any amount, including one line
- Asking it to fix your code
- Inline autocomplete of anything beyond a variable name. **Turn Copilot off.** If you cannot turn it off, tell a mentor and we will help

**Why this tier is the strictest.** This is the only tier where the research is unambiguous. A 2024 study tracking novice programmers using Copilot found that students who were struggling accepted AI suggestions at a substantially higher rate than students who were doing well — 34.1% versus 24.5%. The students least equipped to tell good code from bad were the most likely to accept whatever they were handed. The same study found that most of the struggling students "thought they understood more than they actually did."

That last part is the real danger. Not bad code — bad code gets caught. False confidence does not get caught until January.

An instructor who taught rookie programming for two years put it more bluntly: students who lean on AI early "create LOTS of really bad code really quickly," and he found himself re-teaching "fundamental programming concepts like declaring/initializing variables, constructors, what a parameter is."

**It will feel slow. That is the point.** The struggle where you stare at a compiler error for fifteen minutes is not wasted time; it is the actual mechanism by which you learn to read compiler errors. Skipping it does not save you the fifteen minutes, it just moves them to a worse moment.

### 🟡 Yellow — milestones M3 through M5

**You can write robot code. You are learning to make engineering decisions.**

**Allowed:**
- Boilerplate and repetitive patterned code — if you are writing the third near-identical `stopFlywheel()` / `stopHood()` / `stopTurn()` method, let it finish the third one
- Javadoc and comments on code you wrote
- Stack trace triage: "what does this exception mean and where should I look"
- Explaining unfamiliar WPILib or vendor API surface
- Asking it to review code you already wrote and point out problems

**Not allowed:**
- Control logic — PID structure, feedforward application, state machine design
- Subsystem decomposition — deciding what the subsystems are and what each owns
- Autonomous routine structure
- Gain values. Never ask an AI for a kP. It does not know your mechanism

**Why the excluded list looks like that.** Those four things are precisely what M3 through M5 exist to teach. Everything on the allowed list is typing. Everything on the not-allowed list is thinking.

**Disclosure becomes mandatory at this tier.** See §6.

### 🟢 Green — on completion of M6, and after

**You can verify your own work. Now the tools are safe to lean on.**

Note the boundary carefully: Yellow still applies *during* M6. The tests that unlock Green are written by you, not generated. You do not get to use the agent to build the safety net that makes the agent safe.

**Allowed:** essentially everything, including agentic tools that write, run, and iterate on code — subject to four conditions that must **all** hold:

1. **You applied spec-driven development successfully.** Not "a spec exists" — you did the loop. A conforming OpenSpec change, committed before your first implementation commit; the feature built against it; and you can walk the spec in review, name where the build diverged from it, and say why.
2. **Tests exist, cover the new behavior, and pass.** Not "the code looks right." Tests.
3. **AI use is disclosed in the pull request.** See §6.
4. **You defend the diff in review.** See §7.

The spec is first because it is the only one of the four that happens before the code exists. It is where the decisions live — what the feature does, what it does not do, and what has to hold when it is finished — and §3 is the reason the decisions are the part you have to own.

**"Successfully" is doing real work in that first condition, and it is meant to.** A spec written to satisfy a checklist is worth nothing, and the failure is easy to spot: the student cannot say where the implementation diverged from the document, because they were never steering by it. Every real build diverges. Being able to point at where, and explain the call you made when it did, is the evidence that the spec was load-bearing rather than decorative. That is the bar — not a file in a folder.

**Conforming means OpenSpec's format, and Ignite is opinionated about this.** A proposal, a delta spec whose requirements are observable and whose scenarios read GIVEN/WHEN/THEN, and a task list — the shape `openspec validate` checks. A spec everyone writes differently is a spec nobody can review quickly. M7's `SPEC-TEMPLATE.md` walks a complete one, and the tooling is a milestone prerequisite rather than a nice-to-have.

A spec carries the decisions for *one* change. The decisions that hold across every change are written down separately, in [`coding-standards.md`](coding-standards.md) — Ignite's coding standards. This document says what you may use a tool for; that one says what the code it produces has to look like. Hand your agent both.

> **The spec is Yellow-tier work, even at Green.** You may use AI to explain API surface, to critique a spec you wrote, and to point at cases you missed. You may not ask it to write the spec, or to decide what the feature should do. You do not get to use the agent to write the constraints that constrain the agent — for the same reason you did not get to use it to write the tests that unlocked Green.

That prohibition has an edge, and the edge is M8, not never.

**Before M8, you write the spec. Full stop.** At M8 and after, `openspec-propose` may produce a first draft — one you are required to red-line. You disclose that you used it, and you show what you changed: at minimum, one requirement you rewrote and one scenario you added or deleted. **A draft accepted unchanged is not a pass. It is evidence you did not review it.**

**Why the edge sits there and not earlier.** This is the same argument for the third time: you may lean on the agent once you have written the tests that catch it (this section's own gate into Green), and you write the spec yourself before you are allowed to review one, for the identical reason. By M8 you have written a conforming spec by hand, watched an agent misread it, and defended where your build diverged from it — which is exactly the qualification this section said you lacked. The tool is earned by demonstrating you do not need it.

**Why M6 is the gate and not something softer.** M6 is where you learn simulation, telemetry, and testing. Those are the things that tell you an AI's output is wrong before the robot does. Without them you have velocity and no brakes.

This applies to veterans too, and we know that is annoying. If you have been programming for Ignite for three seasons, you can test out of M1 through M5 in an afternoon. You still write the tests. Our competition repos have JUnit configured in `build.gradle`, CI already gates on it (`pr-build-check.yml` runs `./gradlew build`, which depends on `test`), and most classes still have no coverage. We are not handing agentic tooling to a codebase that thin on verification.

---

## 5. True at every tier

These do not change based on where you are on the ladder.

1. **You are responsible for what you submit.** Not the tool. If code you shipped breaks a match, "the AI wrote it" is not an explanation, an excuse, or a mitigating factor. Disclosure tells people what happened. It does not transfer responsibility, and it never will.

2. **Never paste anything you would not want public.** Team strategy, scouting data on other teams, anything a sponsor gave us in confidence.

3. **If you cannot explain it, it does not ship.** This is the enforcement mechanism behind the whole document. It is checkable and it cannot be gamed by a tool.

4. **Deprecated API is a real and frequent failure.** These models are confidently wrong about WPILib in a specific way: they suggest APIs from older seasons. WPILib changes every year. A mentor reported AI recommending "deprecated classes from former versions of wpilib" as a routine occurrence. When AI output disagrees with the current WPILib docs, the docs win. Always.

5. **When you are stuck, a human is usually better.** This is not sentiment — the highest-rated advice on this in the FRC community is blunt about it: "Getting advice from real, experienced humans will obliterate anything any LLM will throw your way." We have mentors. Use them.

---

## 6. Disclosure

Required on every pull request at Yellow and Green. Put this block in the PR description:

```
AI use: [none | assisted | generated]
Spec:
Tool:
What it did:
How I verified it:
```

`Spec:` is the path or link to the committed spec this change implements. Below Green, where no
spec is required, write `n/a`.

**Definitions of the `AI use:` values, so people use the same words:**

| Value | Meaning |
|---|---|
| `none` | No AI involvement beyond the editor's ordinary non-AI autocomplete |
| `assisted` | AI helped — explained something, wrote boilerplate, caught a bug — but you wrote the logic |
| `generated` | AI produced code that is in this diff substantially as it wrote it |

**A filled-in example:**

```
AI use: assisted
Spec: openspec/changes/indexer-jam-recovery/proposal.md
Tool: Claude
What it did: Explained the Debouncer API I used for the stall
  detector, wrote the Javadoc for the four public methods on
  IndexerSubsystem, and caught that I had the limit switch
  polarity inverted in isLoaded(). I decided the recovery states
  and picked the stall threshold myself.
How I verified it: Read every Javadoc line against what the method
  actually does. Checked the Debouncer explanation against the
  current WPILib docs. Confirmed the polarity fix against the wiring
  diagram, and ran the indexer sim test with a stalled-motor fixture
  to watch the reverse-and-retry actually fire.
```

**"How I verified it" is the field that matters.** The other four are bookkeeping. If you cannot fill in the verification line with something specific, you are not ready to open the PR. "I read it and it looked right" is not verification. "The tests pass" is verification. "I checked it against the CTRE docs" is verification. "I ran it in sim and watched the flywheel reach setpoint" is verification.

Filling this in honestly is never held against you. Filling it in dishonestly is a serious problem — not because of the AI, but because you lied to your team about the state of the code they are about to trust in a match.

---

## 7. The defense review

Required for every Green-tier PR that discloses `generated`.

**What it is.** You walk a mentor or the programming lead through the diff and explain every decision in it. Not what the code does — what it does is usually obvious. *Why it is the right thing to do.*

Expect questions like:
- Why this control mode and not that one?
- What happens if this sensor reads zero?
- Why is this in the subsystem rather than the command?
- What happens if this command is interrupted halfway through?
- Where does this number come from?
- Where did the implementation diverge from your spec, and why?

That last one is the review's spine. The spec was written before the code, so it is the only record of what you intended that the code cannot quietly rewrite. Every real project diverges from its spec — so "it didn't" is itself information, and usually means the feature was too small to be worth specifying or that nobody has looked closely.

**How long it takes.** Ten minutes for a small diff. It is not an inquisition and it is not a test you can fail in front of the team.

**What happens if you cannot defend something.** That specific part comes out and gets rewritten by you. Not the whole PR — just the part you could not explain. No penalty, no record, no drama. The point is to find the gap, not to punish it.

That is the actual mechanism this entire standard runs on. Everything else is scaffolding around it.

**Who runs it.** A mentor, or a student who has completed M7. A peer defense review by another student is legitimate and often better, because a peer asks the questions a mentor assumes you already know.

---

## 8. Failure modes worth knowing by name

These have names because researchers watched enough novice programmers to see the same patterns repeatedly. You will do at least one of these. Knowing the name makes it much easier to catch yourself.

**Shepherding.** You spend your time trying to coerce the tool into producing the thing you already knew you wanted. Twenty minutes of rephrasing prompts to get a method you could have typed in four. This is a reliable sign of over-reliance, and it correlates with worse outcomes.

*How to catch it:* if you have rewritten a prompt three times, close the panel and write the code.

**Drifting.** You accept a suggestion you only half understand. Then another one on top of it. Then something breaks and you backtrack, but you no longer really know what your own code does or which part is wrong. Each step felt reasonable; the destination is a codebase you are lost in.

*How to catch it:* if you cannot explain the last thing you accepted, stop and go back to the last point you understood.

**False confidence.** The one that does not feel like anything. Your code works, so you conclude you understand it. You do not find out otherwise until you are asked to change it, or until it breaks at a competition and you are the person who is supposed to know why. This is the failure the research found most consistently and it is the reason the Red tier exists.

*How to catch it:* you cannot, from the inside. That is what the defense review is for.

---

## 9. Worked cases

For adjudicating real situations. If your case is not here, reason from §3.

| Situation | Tier | Ruling |
|---|---|---|
| M1 student asks an AI what `static final` means | Red | **Allowed.** Explanation, not code |
| M1 student asks an AI to fix a compiler error in their code | Red | **Not allowed.** Bring it to a mentor or another student. Reading errors is the skill being built |
| M2 student uses Copilot inline completion while writing a loop | Red | **Not allowed.** Turn it off. Ask a mentor for help disabling it |
| M4 student asks an AI to generate Javadoc for a subsystem they wrote | Yellow | **Allowed.** Disclose as `assisted`. Read every line before committing |
| M4 student asks an AI what kP to use for their arm | Yellow | **Not allowed.** It cannot know. Characterize with SysId — that is M4's whole point |
| M4 student asks an AI to explain what `BaseStatusSignal.waitForAll()` does | Yellow | **Allowed.** API explanation. Verify against CTRE's docs |
| M5 student asks an AI to structure their three-piece autonomous routine | Yellow | **Not allowed.** Routine structure is a decision, and it is what M5 teaches |
| M5 student asks an AI to help read a stack trace from a failing path-following command | Yellow | **Allowed.** Triage |
| M6 student uses an agent to write the tests for their subsystem | Yellow during M6 | **Not allowed.** You write the tests that unlock Green |
| M7 student asks an agent to draft the spec for their feature | Green | **Not allowed.** The spec is the decisions. See §4 |
| M7 student writes the spec, then asks an agent which cases it missed | Green | **Allowed.** Disclose as `assisted`. This is the highest-value prompt shape in the milestone |
| M8 student has an agent draft their delta spec, then rewrites two requirements and cuts a scenario | Green | **Allowed.** Disclose as `assisted` and show the red-line. See §4 |
| M7 student does the same thing | Green | **Not allowed.** The boundary is M8, and it is about having written one yourself first |
| M7 student uses an agent to implement a full new subsystem, writes tests, discloses, defends in review | Green | **Allowed.** This is the system working as designed |
| M7 student uses an agent, tests pass, but cannot explain why a particular timeout value was chosen | Green | **That part comes out.** Rest of the PR can proceed |
| Any student pastes our scouting data into a chatbot | All | **Not allowed.** See §5, rule 2 |
| A mentor uses an agent to write competition code at 11pm in Week 5 | — | **Not allowed, and it is worse when a mentor does it.** See §10 |

---

## 10. For mentors

Three things.

**This closes your loophole too.** Prior research on this team identified programming as FRC's number-one covert mentor-takeover zone — a broken autonomous visibly loses matches, so the temptation to quietly fix it yourself is strongest exactly where it does the most damage. AI does not create that problem, it makes it faster and easier to hide. If you would not silently rewrite a student's subsystem by hand at 11pm in Week 5, do not do it with an agent either. The student wakes up to working code they cannot maintain, and now they also think they wrote it.

**Enforce the tiers by helping, not by policing.** The Red tier is going to frustrate students who know these tools work. The answer to "why can't I use it, it would take two seconds" is not "because the rules say so." It is: because in eight weeks you will be tuning a shooter at a competition with no internet and a match in twenty minutes, and the version of you that struggled through compiler errors in October is the one who fixes it. Say that. It is true, and students respond to it.

**The gate is a roster checklist, not an honor system.** Someone maintains it. Milestone completion determines tier, tier determines what is allowed, and commit access to the season repo requires M0 through M3. That has to be stated in the fall, not discovered in January.

---

## 11. This document expires

**Review date: end of the 2027 build season.** Whoever is programming lead then owns re-writing it.

This is not boilerplate. The ground under this document is moving fast enough that a multi-year policy would be dishonest:

- FIRST has no rule and has said it may write one
- Community sentiment shifted visibly *within* the 2026 season — one community member described the mood as "overwhelmingly negative even as recently as kickoff this year" before 254 went public mid-season with heavy AI use
- OpenAI began directly offering Codex seats to FRC teams in May 2026, explicitly citing "training new programmers" as a use case. Vendors are actively pushing adoption into the vacuum FIRST left

Anyone writing v2 should re-read the three research files in the team wiki, check whether FIRST has published an actual rule, and re-examine whether the tier boundaries still sit in the right places. They are a judgment call, not a law of nature.

**What should not change without a real argument:** §3. The dependency-versus-decision distinction and "can you defend it" survive changes in tooling, because they are about engineering judgment rather than about any particular tool.

---

## 12. Acknowledgment

I have read this standard. Specifically, I understand that:

- I am responsible for every line of code I submit, regardless of what wrote it
- My tier is set by the milestones I have completed, not by what I feel ready for
- If I cannot explain and defend it, it does not ship — and at Green, I write the spec before the agent writes the code, I write it myself through M7, I own whatever I accept from a drafted one from M8 on, and I can say where the build diverged from it
- Disclosing AI use is required, and being honest about it is never held against me

**Name:** ______________________________

**Current tier:** 🔴 Red · 🟡 Yellow · 🟢 Green   (circle one)

**Highest milestone completed:** ______________

**Date:** ______________

**Mentor:** ______________________________

---

*Sources for every claim in this document are in `resources.md` §6 and §7. If you think something here is wrong, bring the argument — this document was written to be argued with, and §3 exists because a student's objection was good enough to need a real answer.*
