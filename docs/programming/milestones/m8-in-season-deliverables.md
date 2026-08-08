# M8 — In-Season Deliverables & Ownership

## At a glance

What you own during a season, and how you hand it to the next person.

**This milestone runs during the season, not before it.** It is the only one not gated by the Kickoff Readiness Gate, because you cannot practice owning a subsystem until there is a subsystem to own.

**Estimated time:** a full season
**Prerequisites:** M0–M3 minimum; M4 and M6 if you are committing to competition code
**AI posture:** 🟢 Green, under the standard

## Who enters here

Everyone, during the season. All tracks have converged.

## Test-out challenge

**There is none.** You cannot test out of doing the season.

## Concepts

### Owning a subsystem

Ownership means: it is yours, and when it does not work at a competition, you are the person who fixes it.

Concretely you own the code, the tuning, the documentation, the tests, and knowing its failure modes well enough to diagnose from the driver's description of what happened.

**Ownership is not exclusivity.** Others will touch your subsystem. You review those changes and you keep the docs true.

### Why documentation is a real deliverable here

Right now, Ignite's repositories have essentially no documentation. `Robot-2026` — the current season, the most active repo — **has no README at all.** `Robot-2024` has none. `Robot-2025` has a two-line one that is just a title and the game name. `CommandTemplate` and `Phoenix-2026` have none.

Ignite's conventions — per-subsystem constants, the `preferences/` pattern, `statemachines/`, why the codebase is laid out the way it is — exist only in the heads of the people who wrote them and in git history. A new student has no map.

Meanwhile the team's actual coding standards live in exactly one place: `.github/prompts/code-review-prompt.md`, a file whose only reader is a bot.

**That is the gap this milestone closes**, one subsystem at a time. Nobody is going to write all of it at once. But if every subsystem owner writes their own, the team has documentation by the end of a season.

### The tuning log, specifically

Ignite's constants are magic numbers. `ShooterConstants`, `IntakeConstants`, `DriveConstants` — real values that work, with no recorded derivation anywhere.

The cost shows up when something changes. New gearbox, new wheels, a mechanism rebuilt between events — and nobody knows whether the old number was measured, computed, or landed on by trial and error at 1am. So it gets re-derived from zero.

**A tuning log turns "these gains work" into "these gains work because."** That is the difference between a value that survives a mechanism change and one that does not.

Full template in `student-deliverables.md`. The essential shape:

- What you changed, and to what
- Why — measured, computed, or tuned by hand
- What you observed
- Battery voltage and conditions
- What you would try next

### Competition-week git discipline

Competitions are where good practice collapses under pressure. Two rules.

**Tag before and after every event.** One community member's reasoning is exactly right: *"Crazy things happen in the heat of competition and it's nice to be able to quickly refer to a previously good state."*

**Never edit on the field without committing.** There is a cautionary account on Chief Delphi of a team whose robot silently ran stale code because changes were made and tested without being checked in — *"we have no idea why it broke."* That is a real match lost to a preventable process failure.

If you change code in the pit, commit it. Even a bad commit message beats an uncommitted change.

### Build season cadence

Adapted from Team 937's published programming schedule. A reference, not a mandate.

| Week | Programming deliverable |
|---|---|
| **1** | Subsystems and simple commands for **every** mechanism and the drivetrain. Minimum functional bot |
| **2** | Basic autonomous |
| **3** | Tune game-piece mechanisms |
| **4** | Advanced autonomous |
| **5** | Tune endgame mechanism |
| **6** | Stress testing, driver practice, pit documentation |
| **After** | Iterate |

**Note the ordering: make it work, then make it good.** Mechanism control code and basic auto come before tuning. That is why M4's tuning discipline gets taught in the fall — so it is not competing with Week 1's "the robot must drive" pressure.

### What programmers do in Week 0 and 1

A real problem: mechanical has not finished, there is no robot, and programmers feel blocked.

The community answer is concrete and it is two things:

1. **Start simulation-based subsystem logic as soon as the design is settled.** You do not need hardware to write and test a subsystem's logic. That is what M6 was for
2. **Work on code and pathing against last year's robot.** It drives, it has mechanisms, and it is sitting right there

There is no such thing as a blocked programmer in Week 1. There is only a programmer who has not opened the simulator.

### Pit documentation

What the pit crew needs when you are not standing there. Which subsystem does what, how to tell if it is working, common failure symptoms and first actions, and what absolutely must not be changed without you.

Written for someone stressed, in a loud room, with six minutes.

## Project

**Own one subsystem for a full season, with all of its deliverables.**

| Deliverable | When |
|---|---|
| Subsystem README | Created Week 1, updated whenever behavior changes |
| Tuning log | Every tuning session |
| Tests for its logic | As the logic stabilizes |
| PR disclosures | Every PR |
| Git tags | Before and after every event |
| Pit documentation | Before the first event |
| Post-season retro | End of season |

Templates for all of these are in `student-deliverables.md`.

## Definition of done

- [ ] Subsystem README exists, and a student who did not write it can follow it
- [ ] Tuning log has derivations, not just final values
- [ ] At least three tests cover the subsystem's logic
- [ ] Repo tagged before and after each competition
- [ ] Pit documentation exists and the pit crew used it at least once
- [ ] Every PR you opened carried a disclosure block
- [ ] Post-season retro written
- [ ] **You handed your subsystem to someone else and they made a change without your help**

**That last item is the real test.** Everything else is process. If someone else can pick up your subsystem and modify it from your documentation alone, you did the job. If they cannot, the documentation is decorative.

## Common failure modes

**Writing docs at the end.** They will not get written. Week 1, then keep them true.

**Documenting what instead of why.** The code shows what. Write down why the tolerance is 0.05, why the intake stows before the shooter spins.

**Tuning without logging.** You will land on numbers and lose how you got there. Every time.

**Uncommitted field changes.** See above. This loses matches.

**Skipping the tag because you are busy.** Ninety seconds now versus an hour of archaeology later.

**Pit docs written for yourself.** You are not the audience. The audience is stressed, in a hurry, and does not know your subsystem.

**Treating the retro as optional.** It is the only structured chance to convert a season into something the next one benefits from.

**Hoarding the subsystem.** If you are the only person who can touch it, that is a failure of documentation, and it becomes the team's problem the week you get sick.

## AI posture — 🟢 Green

Under the standard: tests, disclosure, defense review.

**Two in-season cautions:**

**Competition week is when the standard matters most and gets followed least.** Deadline pressure is exactly the condition under which someone ships AI-generated code they cannot explain. It is also exactly when nobody has time to debug code nobody understands. The standard does not relax at events.

**Documentation is a legitimate AI-assisted task.** Drafting a README or Javadoc from code you wrote is squarely allowed — it is one of the clearest good uses. Read every line before committing; the tool will confidently describe behavior your code does not have.

## Resources

See `resources.md` for the full library. For this milestone:

- **`student-deliverables.md`** — every template referenced here
- **Team 937 Build Season Schedule** — the source for the week-by-week cadence
- **Team 254 FRC-2025-Public** — the README to model yours on: setup, a prose code walkthrough, naming conventions
- **Chief Delphi: How does your team use git and GitHub?** — where competition tagging comes from
- **Chief Delphi: Biding Time for Programmers** — what to do in Week 0–1
- **Ignite:** `Robot-2026/.github/prompts/code-review-prompt.md` — the standards your subsystem should meet
- **Ignite:** `docs/vision-subsystem.md` — an existing, genuinely good subsystem document. Model yours on it
