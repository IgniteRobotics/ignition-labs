# Task 3: Close the hole before a student's first command

`SETUP.md` walks a student to `git clone git@github.com:…` and has never mentioned SSH keys,
a GitHub account, or `git config user.name`. On a fresh laptop the first command in M0 fails
with `Permission denied (publickey)` and the first commit fails with `Please tell me who you
are` — in the milestone whose entire stated purpose is surviving setup friction. Separately,
`git-workflow.md` hands students the one command M0 explicitly forbids.

The risk in this task is writing a git tutorial. Don't. M0's curated-command philosophy is
deliberate and good: add the minimum that makes the existing commands work.

**Files:**
- Modify: `docs/SETUP.md`
- Modify: `docs/programming/git-workflow.md`
- Create: `.gitattributes`

**Consumes:** nothing.
**Produces:** a setup path a student can follow start to finish without a mentor.
**Lane:** `student-docs`

---

## The findings this closes

**S1 — no git authentication setup anywhere.** Grepping `docs/`, `templates/`, and every
project README for `ssh-keygen`, `SSH key`, `personal access token`, `gh auth`, or
`credential` returns nothing. `SETUP.md`'s six steps go: install WPILib → verify Java → fork
and clone → enable Actions → verify build → verify simulator. Nothing about a GitHub account,
installing git, or configuring an identity.

> **Transport decided, 2026-08-18: HTTPS in everything student-facing.** Josh's call, and the
> right one — over HTTPS, *reading* needs no credentials at all now that both reference
> repositories are public, so `READING.md`'s clones and every M1–M3 repo read become
> zero-setup. That is a materially smaller M0 than teaching `ssh-keygen`.
>
> **Scope: the curriculum, not the team.** This is what we hand a student in their first
> hour. It is **not** a rule about how anyone works. Mentors use SSH — Josh's own remotes are
> SSH and stay that way — and nothing in this repository should imply otherwise. The
> verification in step 2 is deliberately scoped to `docs/`, `projects/`, and `templates/` for
> that reason: an SSH URL in a mentor's local config, in `superpowers/`, or in anyone's
> working clone is not a defect and must not be "fixed."
>
> **Already applied, ahead of this task** (do not redo; do verify):
> - All five SSH URLs in `docs/programming/git-workflow.md` → `https://`.
> - Both SSH URLs in `teaching-repo-spec.md` §5.2's illustration → `https://`.
> - A *"`git push` asks for a username and password"* entry added to `git-workflow.md`'s
>   **When it goes wrong** section, pointing at `gh auth login`.
>
> **Left untouched on purpose:** the SSH URLs in
> `superpowers/plans/2026-08-07-teaching-repo-foundation.md` and
> `superpowers/notes/2026-08-07-ignition-labs-handoff.md`. Both are execution records of
> completed work — `plan-conventions.md` is explicit that rewriting those is churn with no
> reader — and the handoff note predates the rename, so its URLs are historically correct.
>
> **The catch this task must close.** HTTPS fixes *reading*, not *pushing*. A push to a fork
> still needs a credential, and GitHub has not accepted account passwords for it since 2021.
> The entry now in `git-workflow.md` is a stopgap so today's instructions work end to end;
> step 1 below is where it gets done properly, once, in `SETUP.md`.

**C5 — `git-workflow.md` contradicts M0 and is dangerous.** M0 says *"Not on this list:
`rebase`, `reset`, `cherry-pick`, `force push`… you should not meet them before you can
recover from them"* and *"Nothing is broken until someone types `reset --hard`."*
`git-workflow.md`'s "I committed to main by accident" recipe is `git reset --hard
upstream/main`, with no preceding `git fetch upstream` (so it can reset to a stale ref) and
no warning that it destroys uncommitted work.

**S2 — the `JAVA_HOME` failure is documented last and detected nowhere.** It is correctly
described in `SETUP.md`'s final "Known problems" entry, but it is the single most likely
build failure on a machine that already has a JDK, and nothing surfaces it at the moment it
bites. On the review machine, system `java` is 11.0.28 — exactly the failing configuration.

**S5 — Windows.** `gradlew.bat` ships and the build is portable, but the known-problems
section is macOS/Linux-flavoured and there is no `.gitattributes` in a Spotless-gated Java
repo edited across platforms.

---

- [ ] **Step 1: Add a git-and-GitHub step to `SETUP.md`, before the fork step.**
      New section between the current step 2 (Verify Java) and step 3 (Fork and clone). It
      needs exactly four things and nothing more:
      1. A GitHub account.
      2. Git installed and working (`git --version`), with the per-platform note — macOS
         gets it from Xcode Command Line Tools, Windows from Git for Windows, and the WPILib
         installer does **not** ship it.
      3. Identity: `git config --global user.name` and `user.email`, with the sentence that
         explains why commits carry it.
      4. Authentication for **pushing**. The transport is settled — HTTPS — so this is one
         command, `gh auth login` (GitHub.com → HTTPS → browser), plus a sentence saying a
         personal access token is the fallback and that a token is a password, so a mentor
         helps rather than a tutorial. Say explicitly that **cloning needs none of this**:
         every repository in the curriculum is public and reads are anonymous. That framing
         is the whole benefit of the HTTPS decision and it should not be buried.

      Then include what a failure looks like, because that is what a student will actually
      match against:
      > `remote: Support for password authentication was removed` / a username-and-password
      > prompt on `git push` — you skipped step 4. Run `gh auth login`.
      > `Author identity unknown / Please tell me who you are` — you skipped `git config`.

      When this lands, collapse the stopgap entry already in `git-workflow.md`'s **When it
      goes wrong** section into a one-line pointer here. Two copies of credential setup is
      exactly the drift this repository's D1 decision exists to prevent.

- [ ] **Step 2: Verify the HTTPS sweep held, and that nothing new crept in.**
      The sweep is already applied (see the transport note above). Confirm it, rather than
      redoing it:
      ```bash
      git ls-files -z | xargs -0 grep -InE 'git@github\.com|ssh://git@'
      ```
      Expected: hits **only** in `superpowers/plans/2026-08-07-teaching-repo-foundation.md`
      and `superpowers/notes/2026-08-07-ignition-labs-handoff.md`, both execution records.
      Any hit under `docs/`, `projects/`, or `templates/` is a regression — fix it.

      Then confirm every student-path clone is HTTPS and consistent:
      ```bash
      git ls-files -z | xargs -0 grep -In 'git clone\|git remote add' -- docs projects templates
      ```
      Paste both outputs.

- [ ] **Step 3: Replace the `reset --hard` recipe in `git-workflow.md`.**
      The "I committed to main by accident" case must be recoverable with commands on M0's
      allow-list, or must escalate to a mentor. Both are fine answers. Suggested shape:
      ```bash
      # Save your work on a branch — this is the whole fix, and it is not destructive.
      git branch m3-command-based
      # Now main still has the extra commit on it. Show a mentor: putting main back
      # uses a command that can destroy work, and it is worth doing with someone the
      # first time.
      ```
      If a self-service form is kept, it must `git fetch upstream` first and must carry an
      explicit warning that uncommitted work is lost. Do not leave `reset --hard` in a
      student document with neither.

- [ ] **Step 4: Cross-check the two documents.**
      ```bash
      grep -n "reset\|rebase\|cherry-pick\|force" docs/programming/git-workflow.md docs/programming/milestones/m0-tooling-and-git.md
      ```
      Every command surviving in `git-workflow.md` must either be on M0's allow-list or be
      accompanied by the reason it is an exception. Paste the output.

- [ ] **Step 5: Promote the `JAVA_HOME` failure.**
      Move it out of "Known problems" and into the verify-the-build step (`SETUP.md` step 5)
      as a pre-flight check, keeping the existing text as the diagnosis:
      ```bash
      java -version        # must say 17
      echo $JAVA_HOME      # if this points anywhere but the WPILib JDK, that is your problem
      ```
      One sentence naming GradleRIO's actual error string — *"Dependency requires at least
      JVM runtime version 17"* — so a student searching for it lands here.

- [ ] **Step 6: Add `.gitattributes` and a Windows line in known-problems.**
      Repository root:
      ```
      * text=auto eol=lf
      *.bat text eol=crlf
      gradlew text eol=lf
      *.jar binary
      *.png binary
      *.wpilog binary
      ```
      Then confirm it changes nothing in the tree as it stands:
      ```bash
      git add --renormalize . && git status --short
      ```
      Expected: empty. If it is not empty, stop — that is a real line-ending problem and it
      needs its own decision, not a silent bulk rewrite.

- [ ] **Step 7: Walk the whole path on a clean checkout.**
      Follow `SETUP.md` top to bottom in a scratch clone, doing every step literally,
      including the git ones. Report where it broke or confirm it did not. A setup document
      nobody has walked is a setup document nobody knows works — the same rule
      `CLAUDE.md` applies to fixtures.

- [ ] **Step 8: Build and commit.**
      ```bash
      poetry run mkdocs build --strict     # expect rc=0
      git status --short
      ```
