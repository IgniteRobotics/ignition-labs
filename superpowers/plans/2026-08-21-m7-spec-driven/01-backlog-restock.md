# Task 1: Restock `FEATURE-BACKLOG.md`

`FEATURE-BACKLOG.md` has **one** Open entry. Task 6 spends it as the worked spec example,
which under `CLAUDE.md`'s authoring rules disqualifies it as a student's pick — a worked
example must not contain the answer to the exercise. Without this task, M7 ships with a
backlog of zero and the milestone is unrunnable.

The risky part is not the mining. It is that **a backlog entry that is not actually
M7-sized is worse than no entry**: a student picks it, spends their two sessions on scope,
and never reaches the defense review that is the entire assessment. Every entry you add is
measured against the criteria already written at the top of that file — read them first,
they are not negotiable and they are not yours to loosen.

**Files:** Modify `projects/m7-ai-assisted/FEATURE-BACKLOG.md`
**Consumes:** nothing
**Produces:** at least three Open entries beyond `grabVisionEstimateList()`, so task 6 can
spend one and M7 still has choices
**Lane:** `m7project`

- [ ] **Step 1: Read the selection criteria and the existing entry**
      `projects/m7-ai-assisted/FEATURE-BACKLOG.md` §"What makes a feature M7-sized" and the
      single Open entry. Match their **format exactly** — the existing entry is the template.
      Note the pinned-SHA convention used to reference `Robot-2026` paths; every new entry
      needs one.

- [ ] **Step 2: Mine `Robot-2026` at a pinned SHA**
      Same approach the original backlog used (`superpowers/specs/2026-08-18-m7-m8-design.md`
      §4). Fetch `Robot-2026` at a pinned commit and look for work that is real, small,
      shippable in 2–3 sessions, testable without hardware, and **does not require a
      mechanism-specific constant** — anything needing a gain is an M4 problem wearing an M7
      costume.
      Productive seams, in rough order of yield: swallowed exceptions and empty `catch`
      blocks; missing bounds or null guards on public subsystem methods; commands with no
      `.withName()`; duplicated constants that should be one; `UNTESTED-TARGETS.md` overlaps
      where a feature and its test could ship together.
      **Record the SHA you mined.** An entry pointing at a moving `main` is stale the week
      after it is written.

- [ ] **Step 3: Write at least three new Open entries**
      Each entry states what is wrong now, what "done" looks like, and how a student would
      know it works — **without stating the implementation.** Same rule as a stub Javadoc:
      the contract, never the answer. If you cannot describe the finished state without
      writing the fix, the entry is too small to be a feature and belongs in a different list.
      Do **not** mark the `grabVisionEstimateList()` entry as taken or reserved. Task 6 spends
      it as a worked example; the mentor who owns this file decides its status, and D-4
      explicitly removed the reservation model from the sibling `UNTESTED-TARGETS.md`.

- [ ] **Step 4: Add one line recording the mining SHA and date**
      Under the existing note before `## Open`, so the next person restocking knows where the
      last sweep stopped. This file goes stale fast — spec §16 names it as in-season
      maintenance — and a dated high-water mark is the cheapest thing that makes the next
      sweep incremental.

- [ ] **Step 5: Verify**
      ```bash
      poetry run mkdocs build                        # rc=0 -- sanity only; projects/ is not published
      poetry run python tools/tests_lock.py verify   # rc=0, unchanged
      git status --short                             # only FEATURE-BACKLOG.md
      ```
      Then read your own entries as a student would: **for each, can you tell what "done"
      means without being told how?** An entry that fails that read gets rewritten, not
      shipped.

- [ ] **Step 6: Commit**
      Report the mined SHA and the entry count in the commit message.
