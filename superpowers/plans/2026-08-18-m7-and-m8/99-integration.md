# Task 7: Integration — status rows, gates, and the checks that matter when nothing compiles

The serialized tail. Every prior step's integration task regenerated `tests.lock` and ran the clean-clone invariant; **neither applies here**, and that is the thing to get right. Nothing this plan shipped is a Gradle project, matches a `tests.lock` pattern, or has a test to go red. Running the usual checklist by habit would produce a green report that verified nothing about M7 or M8.

What replaces it: proving the two new directories are correctly *invisible* to the Java tooling, and correctly *visible* to the documentation tooling.

**Files:**
- Modify: `docs/programming/README.md`
- Modify: `docs/programming/teaching-repo-spec.md`

**Consumes:** every prior task.
**Produces:** the shipped state of build-order step 11.

**Lane:** `integration`. Single owner, on the merged branch, after Task 6.

---

- [ ] **Step 1: Merge both lanes and confirm the tree**

```bash
cd /Users/y0shi/workspace/ignite/docs
git merge --no-ff lane/m7 -m "Merge M7 lane"
git merge --no-ff lane/m8 -m "Merge M8 lane"
git status --short          # expect empty
```

- [ ] **Step 2: Confirm `tests.lock` is genuinely unaffected**

```bash
poetry run python tools/tests_lock.py verify; echo "rc=$? (expect 0)"
poetry run python tools/tests_lock.py write
git diff --stat tests.lock
```

Expected: `tests.lock OK`, and **the `write` produces no diff at all.** Nothing this plan shipped is a `.java` file under a `src/test/`, `plant/`, `harness/`, or `regressions/` path, so no pattern matches it.

If `write` produces a diff, stop — something landed in a source tree that was supposed to be documents, and the question is what, not whether to commit the new lock.

```bash
git checkout -- tests.lock 2>/dev/null; true
```

- [ ] **Step 3: Prove both new directories stay out of the Java tooling**

This is the check that replaces the clean-clone invariant. Four ways in, all of which must not find M7 or M8.

```bash
echo "--- 1. the CI build matrix ---"
printf 'projects/m7-ai-assisted/FEATURE-BACKLOG.md\nprojects/m8-ownership/README.md\ntemplates/tuning-log.md\n' \
  | poetry run python tools/changed_projects.py --from-stdin
```

Expected: `[]`

```bash
echo "--- 2. skeleton-integrity's discovery ---"
find projects -mindepth 2 -maxdepth 3 -name settings.gradle -print | sed 's#/settings.gradle##' | sort
```

Expected: the eight existing Gradle projects. **Neither `projects/m7-ai-assisted` nor `projects/m8-ownership` may appear.** They land in no branch of that workflow — not the regression gate, not the inverted check, not the skip branch — because discovery never reaches them.

```bash
echo "--- 3. no stray Gradle wrapper ---"
ls projects/m7-ai-assisted/gradlew projects/m8-ownership/gradlew 2>&1 | head -2
```

Expected: `No such file or directory` for both.

```bash
echo "--- 4. the cross-check ---"
cd ../ignite-training-solutions && ./tools/cross-check.sh 2>&1 | tail -18; cd -
```

Expected: `CROSS-CHECK OK`, every existing solution still passing, and **no line mentioning m7 or m8** — not even `SKIP`, for the reason in Task 6 Step 2.

- [ ] **Step 4: Prove the documentation tooling does see what it should**

```bash
poetry run python tools/templates_sync.py verify; echo "templates_sync rc=$? (expect 0)"
poetry run pytest -q; echo "pytest rc=$? (expect 0)"
poetry run mkdocs build --strict > /dev/null 2>&1; echo "mkdocs rc=$? (expect 0)"
poetry run python tools/site_guard.py _site; echo "site_guard rc=$? (expect 0)"
```

Then confirm the publishing boundary held against the two directories that are new:

```bash
find _site -path "*templates*" -o -path "*m7-ai-assisted*" -o -path "*m8-ownership*" | head
```

Expected: no output. `templates/` and `projects/` are both outside `docs_dir` and unpublishable structurally — the markers Task 4 added to a published document must not have changed that.

- [ ] **Step 5: Status rows**

Both files are contended and belong to this task alone.

**`docs/programming/README.md`** — the built-milestones row currently reads `M0–M6 built` and ends `M7 and M8 skeleton projects are not yet built; they arrive in later plans`. M7 and M8 are now built, but **they are not skeleton projects and must not be described as though they were.** Neither assembles, neither has a test, and a future reader who takes "built" to mean "has a green build" will go looking for one.

Replace that trailing sentence with something true, in these words or better:

> `projects/m7-ai-assisted/` and `projects/m8-ownership/` are also built, and are deliberately unlike every project above them: no Gradle build, no tests, no skeleton code. M7 ships five documents and a pinned reference copy of `Robot-2026`'s AI review pipeline, because its assessment is a defense review on a real pull request; M8 ships a pointer at `templates/`, whose six fill-in-the-blank files are the deliverables. Both are verified by conversation, not by CI.

Update the row's header cell from `Teaching repository for M0–M6 projects` to cover M0–M8, and the `**M0–M6 built**` lead accordingly.

**`docs/programming/teaching-repo-spec.md` §13** — step 11's status column `—` becomes `Done`. Leave step 12's row alone; it is still outstanding and this plan deliberately did not touch its items.

Also update the solutions row's status, which currently reads `M0–M6, T1–T5 done; cross-check live`. M7 and M8 ship authoring notes rather than solutions, so write it as `M0–M6, T1–T5 done; M7–M8 notes only (no code to solve); cross-check live` — recording *why* there is no solution, so the next reader does not log it as a gap.

- [ ] **Step 6: Re-run the docs gates after editing docs**

```bash
poetry run mkdocs build --strict > /dev/null 2>&1; echo "mkdocs rc=$? (expect 0)"
poetry run python tools/site_guard.py _site; echo "site_guard rc=$? (expect 0)"
poetry run python tools/templates_sync.py verify; echo "templates_sync rc=$? (expect 0)"
```

The last one matters more than it looks: Step 5 edited a file in `docs/programming/`, and if anyone edited `student-deliverables.md` by reflex while in there, this catches it.

- [ ] **Step 7: Commit**

```bash
git add -A
git status --short
git commit -m "Integrate M7 + M8: status rows, and the checks that apply when nothing compiles

Build-order step 11 complete. tests.lock is untouched and verified to be
untouchable by this work -- nothing shipped matches its patterns -- and the
clean-clone invariant does not apply, because neither project has a Gradle build
or a test. Running either by habit would have produced a green report that
verified nothing about M7 or M8.

What was verified instead: both directories stay out of the CI build matrix, out
of skeleton-integrity's discovery, and out of cross-check entirely -- not even as
SKIP, since discovery keys on settings.gradle and neither has one. And the
documentation tooling does see what it should: templates_sync green, mkdocs
--strict clean, site_guard reporting nothing from templates/ or projects/ in
_site.

The README row says plainly that these two are unlike every project above them --
no Gradle build, no tests, no skeleton code -- so nobody reads 'built' as 'has a
green build' and goes looking for one."
```

- [ ] **Step 8: Report, then stop**

Report what shipped, the actual output of every check in Steps 3 and 4, and the two status rows. Then stop — step 12's doc-correction pass is a separate plan and this task does not begin it.
