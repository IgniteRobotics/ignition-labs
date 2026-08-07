# Skeleton Projects, Git Ritual & the Real-Repo Ladder — Session Record

**Date:** 2026-08-07 · **Owner:** Josh Reddick · **Status:** Folded in

> **This is a record, not a spec.** The full design lives in
> [`programming/teaching-repo-spec.md`](../../programming/teaching-repo-spec.md).
> Read that. Nothing here overrides it.

---

## What this session decided

Four questions, answered in order. Each answer is now a numbered decision or section in the canonical spec.

| Question | Answer | Lives at |
|---|---|---|
| Where do the skeleton projects live? | **This repository**, at `projects/`, beside the prose — not a separate `ignite-training` repo. Published to GitHub Pages via `doc-builder` | D1, D7, §4.1 |
| What is the git ritual per milestone? | **Clone what you're reading. Fork what you'll change. Branch to work.** Fork `docs` once at M0, branch per milestone, PR into your own fork. Upstream receives exactly one student PR, ever | D2, §5 |
| Which milestones work in `Robot-2026` instead of a sandbox? | A four-level ladder — **browse → clone → fork+PR → branch on upstream** — with the fork/branch boundary set at the Kickoff Gate. Fork-and-PR at M4 (stretch) and M6; commit access at M7/M8 | D8, §6 |
| How do the Gradle projects coexist with a published site? | Top-level `projects/`, index-driven nav so nothing under it renders. Four workflows across three run locations | §4.1, §7 |

## What it changed in the original spec

Three decisions overridden — **D1** (separate repo → this repo), **D2** (fork/PR venue was unstated → fully specified), **§6.7** (M6 sandbox exercise → real pull request, now D9). One section deleted: the **§4.2 multi-project GradleRIO spike**, obviated because a documentation-site root has no Gradle project to aggregate from, making standalone-per-milestone the only available shape rather than a fallback.

Three decisions added: **D7** Pages publication, **D8** the real-repo ladder, **D9** M6's real pull request.

## The three findings worth remembering

1. **`Robot-2026`'s CI already gates on tests, vacuously.** `pr-build-check.yml` runs `./gradlew build`, and Gradle wires `build → check → test`; there is simply no `src/test` to run. So M6's pull request does not merely add tests — it switches on test gating for every team PR. Which forces the hard rule that real-repo tests are pure logic only: one flaky test would block the team in February. → spec §6.5

2. **M6 looks circular and is not.** The Gate requires M6 before commit access to `Robot-2026`; M6's deliverable is a PR to `Robot-2026`. It resolves because the PR comes from a fork. A student will spot this; a mentor should have the answer ready. → spec §6.4

3. **"The first `src/test` in three seasons" expires after one student.** Student #1 lands it in October and the framing dies. Hence `UNTESTED-TARGETS.md` — a queue, not a bragging right. → spec §6.6

## Process note

The original plan was to keep this as a standalone design and fold it into `teaching-repo-spec.md` later. The fold happened the same day instead, which is why this file is nine paragraphs rather than four hundred lines. The full original text is preserved in git at commit `7a63dd8` if the reasoning behind a decision is ever needed at length.
