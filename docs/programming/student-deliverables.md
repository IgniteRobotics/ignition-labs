# Student Deliverables — In-Season

**Companion to:** `milestones/m9-in-season-deliverables.md`

Everything a subsystem owner produces during a season, with templates you fill in rather than write from scratch.

**How to use this file:** copy the template, replace everything in `[brackets]`, delete what does not apply. If a section does not apply to your subsystem, write "N/A" and why — do not silently drop it, because the next person needs to know you considered it.

---

## The deliverables at a glance

| Deliverable | Who owns it | When | Lives where |
|---|---|---|---|
| Subsystem README | Subsystem owner | Created Week 1, updated on every behavior change | `docs/subsystems/[name].md` in the season repo |
| Tuning log | Subsystem owner | Every tuning session | `docs/tuning/[name]-tuning.md` |
| PR description | Whoever opens the PR | Every PR | The PR itself |
| Competition git tags | Programming lead | Before and after every event | Git tags |
| Pit documentation | Subsystem owner | Before the first event | `docs/pit/[name]-pit.md`, plus printed |
| Post-season retro | Whole subteam | Within two weeks of the last event | `docs/retro-[year].md` |

---

## 1. Subsystem README

**Acceptance test:** someone who did not write your subsystem can modify it using this document alone, without asking you.

Save as `docs/subsystems/[subsystem-name].md`.

<!-- template:subsystem-readme start -->
```markdown
# [Subsystem Name]

**Owner:** [your name]
**Last updated:** [date]
**Code:** `src/main/java/frc/robot/subsystems/[package]/`

## What it does

[Two or three sentences in plain language. What is this mechanism
for, in terms of the game? Someone from another subteam should
understand this paragraph.]

## Hardware

| Device | Type | CAN ID / Port | Notes |
|---|---|---|---|
| [name] | [TalonFX / CANcoder / etc] | [id] | [gear ratio, inversion, anything surprising] |

**Gear ratio:** [motor rotations per mechanism rotation]
**Range of motion:** [min] to [max], limited by [software / hard stop / limit switch]

## How to use it

[The public commands other code should call. For each: what it does
and when it finishes.]

| Command | What it does | Finishes when |
|---|---|---|
| `[methodName()]` | [description] | [condition] |

## How it works

[The parts that are not obvious from reading the code. Control
strategy, why it is structured this way, anything you had to figure
out the hard way.]

## Key constants

| Constant | Value | Where it came from |
|---|---|---|
| [NAME] | [value] | [measured / computed / tuned — see tuning log] |

## Failure modes

| Symptom | Likely cause | First thing to check |
|---|---|---|
| [what you see] | [why] | [action] |

## Tests

[What is covered, what is not, how to run them.]

## Things I would change

[Honest list. This is the most valuable section for whoever inherits
this and the easiest one to skip. Do not skip it.]
```
<!-- template:subsystem-readme end -->

**A worked example already exists in this repo:** `docs/vision-subsystem.md`. Read it before writing yours.

---

## 2. Tuning log

**Why this exists:** Ignite's constants are currently magic numbers with no recorded derivation anywhere. When a gearbox changes, nobody knows whether the old value was measured, computed, or landed on at 1am — so it gets re-derived from zero.

**Rule: one entry per tuning session. Write it during, not after.** You will not remember.

Save as `docs/tuning/[subsystem-name]-tuning.md`. Newest entry at the top.

<!-- template:tuning-log start -->
```markdown
# [Subsystem] — Tuning Log

## [Date] — [one-line summary]

**Who:** [names]
**Conditions:** battery [voltage]V, [on blocks / on carpet / on the field], [anything unusual]

**Goal:** [what you were trying to achieve]

**Starting point:**
| Constant | Value |
|---|---|
| [kP] | [value] |

**What I changed and why:**
[Narrative. "Raised kP from 0.1 to 0.2 because it was reaching
setpoint too slowly under load."]

**What I observed:**
[What actually happened. Include the plot if you have one — put it
in docs/tuning/images/ and link it.]

**Ending point:**
| Constant | Value | Derivation |
|---|---|---|
| [kP] | [value] | [SysId / hand-tuned / computed from X] |

**Still wrong / next time:**
[What you did not get to, or what you suspect.]
```
<!-- template:tuning-log end -->

**Minimum bar:** if you changed a number on the robot, there is an entry. Even "raised kP, it oscillated, put it back" is worth recording — it stops the next person repeating it.

---

## 3. Pull request description

Required on every PR. The AI disclosure block is mandatory at Yellow and Green tiers — see `ai-use-standard.md` §6.

<!-- template:pr-description start -->
```markdown
## What this changes

[One or two sentences.]

## Why

[What problem this solves. Link an issue if there is one.]

## How I tested it

[Specific. "Ran in sim, flywheel reached 3000 RPM within 0.4s and
held it." Not "it works."]

- [ ] Builds locally (`./gradlew build`)
- [ ] Tests pass (`./gradlew test`)
- [ ] Tested in simulation
- [ ] Tested on hardware — [which robot, or N/A]

## AI use

AI use: [none | assisted | generated]
Spec: [path to the committed spec, or n/a below Green]
Tool:
What it did:
How I verified it:

## Anything reviewers should look at closely

[Where you are unsure. This is not a weakness — it is the most
useful thing you can tell a reviewer.]
```
<!-- template:pr-description end -->

**"How I verified it" is the field that matters.** "I read it and it looked right" is not verification. "Tests pass," "checked against the CTRE docs," "ran in sim and watched it reach setpoint" are.

---

## 4. Competition week — git checklist

Competitions are where good practice collapses. Two failure modes, both real, both cost matches: uncommitted field changes, and no known-good state to fall back to.

<!-- template:competition-week-checklist start -->
### Before leaving for the event

- [ ] Everything committed and pushed. `git status` is clean
- [ ] `main` builds: `./gradlew build`
- [ ] Tag it: `git tag pre-[event-name] && git push --tags`
- [ ] Confirm the code on the robot matches `main`
- [ ] At least two people have the repo cloned and can deploy

### Every day at the event

- [ ] Morning: confirm what is deployed matches a known commit
- [ ] **Every change made in the pit gets committed.** Even a bad message beats an uncommitted change
- [ ] Before each match: is deployed code the code you think it is?
- [ ] End of day: push everything

### After the event

- [ ] Tag it: `git tag post-[event-name] && git push --tags`
- [ ] Write down what broke and what you changed — feeds the retro
<!-- template:competition-week-checklist end -->

### The rule underneath all of it

**Never edit code on the field without committing it.** There is a documented case on Chief Delphi of a team whose robot silently ran stale code because changes were made and tested without being checked in — *"we have no idea why it broke."* That is a real match lost to process, not to engineering.

---

## 5. Pit documentation

**Audience:** someone stressed, in a loud room, with six minutes before the next match, who does not know your subsystem. Not you.

Keep it to one page. Print it. Put it in the pit.

<!-- template:pit-documentation start -->
```markdown
# [Subsystem] — Pit Card

**Owner:** [name] — find me if this card does not solve it

## Is it working?

[How to tell in ten seconds, without a laptop. What should it do on
enable? What LED, what sound, what motion?]

## Quick checks, in order

1. [First thing to check]
2. [Second thing]
3. [Third thing]

## Symptoms

| It is doing this | Try this |
|---|---|
| [symptom] | [action] |

## Do not change these without me

- [constant or setting] — [why it matters]

## If all else fails

[The safe fallback. Disable the subsystem? Run a degraded mode? What
lets the robot still play a match?]
```
<!-- template:pit-documentation end -->

**That last section is the most important one** and the one people leave out. A robot that plays a match with one mechanism disabled scores more than a robot that does not leave the pit.

---

## 6. Post-season retro

Written within two weeks of the last event, while it is still accurate. Whole subteam, one document.

Save as `docs/retro-[year].md`.

<!-- template:post-season-retro start -->
```markdown
# [Year] Programming Retro

**Written:** [date]
**Present:** [names]

## What we built

[Brief. Subsystems, autos, notable features.]

## What worked

[Be specific. "Command factory methods made compositions easy to
read" is useful. "Good teamwork" is not.]

## What did not

[Specific and blameless. Describe the failure, not the person.]

## What broke at competition

| What | Why | Did we fix it | Would documentation have helped |
|---|---|---|---|
| [thing] | [cause] | [yes/no/partly] | [honest answer] |

## What we would do differently

[Concrete and actionable. Something the next team can actually do.]

## What the next team should know

[The stuff not written down anywhere else. Gotchas, dead ends,
things that took a week to work out.]

## Curriculum feedback

[Which milestones prepared you and which did not. Where were the
gaps? This is how the curriculum improves — it does not improve on
its own.]
```
<!-- template:post-season-retro end -->

**The curriculum feedback section is not filler.** A cross-team teaching resource has been attempted in FRC before and did not sustain, because nobody maintained it. This section is the maintenance mechanism.

---

## Why these specific artifacts

Everything here traces to a gap that currently exists.

| Deliverable | The gap it closes |
|---|---|
| Subsystem README | `Robot-2026` has **no README at all**. `Robot-2025`'s is two lines. Conventions live only in developers' heads |
| Tuning log | Constants across every subsystem are magic numbers with no recorded derivation |
| PR disclosure | The AI standard needs an artifact to check against; without one it is unenforceable |
| Competition tags | No documented competition-week workflow exists |
| Pit card | The pit crew currently depends on the subsystem owner being physically present |
| Retro | Nothing currently converts a season's hard-won knowledge into the next season's starting point |

Every one is small. None takes more than thirty minutes. The reason they do not exist is not difficulty — it is that nobody has owned them. M9 assigns that ownership.
