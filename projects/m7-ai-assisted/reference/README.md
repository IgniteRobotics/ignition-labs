# Reference — four files from `Robot-2026`

These four files are **copies, for reading.** Nothing in this directory runs, and
nothing in it is wired into this repository's CI.

---

## Get it

```bash
cd ~/workspace/ignite            # or wherever you keep repositories
git clone https://github.com/IgniteRobotics/Robot-2026.git
cd Robot-2026
git checkout 28ce5397d88275001d656dbb717d22cf9ac0150d
```

Detached HEAD is expected — you are looking at a photograph of the repository, not
standing on a branch.

In a browser instead:
<https://github.com/IgniteRobotics/Robot-2026/tree/28ce5397d88275001d656dbb717d22cf9ac0150d>

---

## Where each file actually lives in `Robot-2026`

Flattening these into one directory loses their original paths. Here they are:

| File here | Original path in `Robot-2026` |
|---|---|
| `ai-code-review.yml` | `.github/workflows/ai-code-review.yml` |
| `gemini-code-review.py` | `.github/scripts/gemini-code-review.py` |
| `requirements.txt` | `.github/scripts/requirements.txt` |
| `code-review-prompt.md` | `.github/prompts/code-review-prompt.md` |

## Re-verifying a copy is still faithful

Each file is pinned to the commit above. To confirm a copy here still matches the real
one at that commit:

```bash
git -C ~/workspace/ignite/Robot-2026 show 28ce5397d88275001d656dbb717d22cf9ac0150d:.github/workflows/ai-code-review.yml \
  | diff - ai-code-review.yml
```

Substitute the matching original path from the table above for the other three files.
No output means the copy is faithful; any output means this directory has drifted from
the commit it claims to be pinned to and needs to be re-copied, not hand-patched.

## Why these are not wired into this repository's CI

Running `ai-code-review.yml` here for real would need a `GEMINI_CODE_REVIEW_KEY` secret
on a student-facing repository and per-PR API spend, to duplicate a workflow that
already runs on the `Robot-2026` pull requests M7's work actually goes to. There is
nothing for it to review in this repository anyway — M7 has no skeleton code and no
pull requests of its own. Keep it a reference copy; do not place it in `.github/`.
