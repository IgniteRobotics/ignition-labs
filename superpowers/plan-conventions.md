# Plan conventions

How implementation plans in `superpowers/plans/` are laid out, and how work on them is
split across parallel agents.

Applies from **build-order step 7 (M3 + T3) onward**. The five existing single-file plans
stay as they are — they are execution records now, and rewriting them would be churn with
no reader.

---

## Why this exists

Measured on 2026-08-10:

| Plan | Lines | Tokens |
|---|---|---|
| M0 | 994 | ~29K |
| M1 + T1 | 1,727 | ~50K |
| M2 + T2 | 1,964 | ~57K |
| `teaching-repo-spec.md` | 765 | ~35K |

The trend is monotonic and M3 is the largest milestone in the spec — expect 70–90K for its
plan in the same style. Reading the spec plus one plan is over 100K before a single file
gets written.

The plans are large because they inline every file's full content, and **that is correct**
— it is what lets a worker execute one task knowing nothing else. The problem is not total
size, it is that **the unit of loading is the whole file.** Handing an agent "do Task 3"
makes it read 57K to reach 4K of instructions.

Splitting the file fixes that without changing a word of the content.

---

## Layout

One **directory** per plan, not one file:

```
superpowers/plans/2026-08-XX-m3-and-t3/
├── 00-plan.md          goal, architecture, task graph, constraints delta, self-review
├── 01-scaffold.md      one task per file, self-sufficient
├── 02-harness.md
├── 03-…
└── 99-integration.md   the serialized tail
```

A worker loads `00-plan.md` (~4–6K) plus its own task file (~4–8K). Same total bytes on
disk, an order of magnitude less context per agent.

### `00-plan.md`

Goal, architecture, tech stack, file-structure table, the **task graph** (below), a
**constraints delta**, and the self-review.

**Constraints delta, not a constraints block.** `CLAUDE.md` at the repo root is loaded
automatically for every agent and already carries the toolchain, pinned versions, spotless
wiring, package naming, fade curve, authoring rules, clean-clone invariant, and
verification discipline. A plan restates **only what is specific to it** — the milestone's
fade level and what that forbids, its vendordeps, its pinned SHAs, its novel mechanism.
Every plan so far carried ~40 lines of duplicated global constraints; that is now one line
saying "see `CLAUDE.md`."

### Task files

Named `NN-<short-name>.md`, numbered in dependency order. Each is **self-sufficient**: an
agent handed one file plus `00-plan.md` can execute it without reading any sibling.

```markdown
# Task N: <name>

<one paragraph: what this task is for, and what is risky about it>

**Files:** Create / Modify / Test — full paths
**Consumes:** what earlier tasks produced that this needs (name the API, don't hand-wave)
**Produces:** the API later tasks are written against
**Lane:** <lane name from the task graph>

- [ ] **Step 1: …**
      <the work, with full file contents inline>
- [ ] **Step 2: Verify …**
      <exact commands and expected exit codes>
- [ ] **Step N: Format and commit**
```

Do not factor verification steps out into the integration task. The control
implementation, the distractor matrix, and the failure-message reads belong to the task
that owns the code they check — that is what makes a task independently reviewable.

---

## The task graph

`00-plan.md` must contain this section. It is the part every previous plan lacked: M2's
tasks carried per-task `Consumes`/`Produces`, but the graph was never stated, so working
out what could run concurrently meant reading all seven tasks.

```markdown
## Task graph

| Task | File | Depends on | Touches | Lane |
|---|---|---|---|---|
| 1 | 01-scaffold.md | — | projects/m3-command-based/ | milestone |
| 2 | 02-harness.md | 1 | projects/m3-command-based/ | milestone |
| 3 | 03-fixture.md | — | projects/testouts/t3-…/ | testout |
| 9 | 99-integration.md | all | tests.lock, docs/ | integration |

**Lanes:** `milestone` (tasks 1–2, sequential), `testout` (task 3).
**Maximum useful concurrency: 2.**
**Serialized tail:** task 9, single owner, after both lanes land.
```

Two agents must never be in the same lane. A lane is defined by the files it touches, not
by task count.

---

## Contended files — the serialization points

These are edited by **every** milestone plan, and two agents finishing concurrently will
conflict on them every time:

| File | Why it is contended |
|---|---|
| `tests.lock` | Regenerated wholesale; every new given test adds entries |
| `docs/programming/README.md` | The one status-table row naming which milestones are built |
| `docs/programming/teaching-repo-spec.md` | §13's build-order status column |

**Rule: no per-milestone task file may touch any of them.** They move to
`99-integration.md`, owned by one agent, run after every lane has landed. That task also
runs the CI gates across all projects and the solutions cross-check.

This is the single change that makes concurrent milestone work safe. Without it, two
agents on M4 and M5 will both regenerate `tests.lock` and one will clobber the other.

---

## Where the parallelism actually is

Be honest about the width available, and do not fan out past it. Idle or colliding agents
are worse than one working agent.

**Within one milestone plan: ~2 lanes.** The milestone project and its test-out fixture
are genuinely independent — different directories, different packages, no shared files.
Everything inside the milestone project is sequential: same Gradle project, same `build/`,
tasks that build on each other's APIs. M2's real graph was `1→2→3→4` plus an independent
`5`, then a serialized tail. Fanning five agents at it gets three of them blocked.

**Across build-order steps: genuinely 3 lanes.** This is the parallelism worth having, and
it is invisible in the spec because §13 is written as a sequence. Because §4.2 makes every
project standalone — own `gradlew`, own `settings.gradle`, no root aggregation — steps 8
(M6), 9 (M4), and 10 (M5) touch **completely disjoint file trees**. They can be built
concurrently by three agents with one shared integration task at the end.

Step 7 (M3 + T3) is the Kickoff Gate blocker and should be finished before opening that
fan-out, not alongside it.

**Backfill work is N-wide.** Reconstructing a reference solution per milestone, or writing
per-milestone authoring notes, has no shared state at all. One agent per milestone.

---

## Worktrees

One worktree per lane, under `.worktrees/<lane>` (gitignored). Never two agents in one
worktree.

Gradle handles this fine: `~/.gradle` is shared and lock-protected, so a second worktree
costs no dependency re-download. What is **not** safe is two agents running Gradle against
the same project directory — the daemon and `build/` collide. Separate worktrees give
separate project directories, which is the whole point.

Each lane merges to the feature branch when its tasks are done; the integration task runs
on the merged result, not on any single lane.

---

## Handing a task to an agent

The prompt needs three things and nothing else:

1. The plan directory path and the task file to execute.
2. Its lane, and which worktree to work in.
3. A reminder that `00-plan.md` holds the architecture and task graph.

`CLAUDE.md` loads automatically — do not paste toolchain facts, pinned versions, or
authoring rules into the prompt. If an agent needed something that is not in `CLAUDE.md`,
the plan, or its task file, that is a gap in one of those three, and the fix goes there
rather than into the next prompt.

Require the agent to report **actual command output** for every verification step. A task
report that says "tests pass" without the run is not a completed task.
