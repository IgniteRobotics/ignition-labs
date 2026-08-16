# Task 7: T4's pre-generated response plot, committed and regenerable

Spec §16 lists "regenerate T4's plot" as a seasonal maintenance task, which only means
something if a future maintainer can actually run one command and get the same picture back.
This task writes that command (`tools/gen_t4_plot.py`), runs it once to produce the committed
`plots/response.csv` and `plots/response.png`, and then — the non-negotiable step — runs it
again from scratch to prove it reproduces byte-identically before anything is committed.

**Files:**
- Create: `tools/gen_t4_plot.py`
- Create: `tests/test_gen_t4_plot.py`
- Create: `projects/testouts/t4-bad-kv/plots/response.csv`
- Create: `projects/testouts/t4-bad-kv/plots/response.png`
- Create: `projects/testouts/t4-bad-kv/plots/README.md`
- Modify: `pyproject.toml`, `poetry.lock` (add matplotlib to the dev dependency group)

**Consumes:** `BadlyTunedFlywheel` and the `generateResponse` Gradle task from Task 6 — this
task does not write any Java, only runs what Task 6 shipped.

**Produces:** the committed plot Task 8's answer key references (mentor-only, in
`../ignite-training-solutions/`), and the regenerator a future maintainer runs against it.

**Lane:** `testout`. Work in `.worktrees/t4`. Depends on Task 6 — run it in the same worktree,
after Task 6's commit lands.

---

- [ ] **Step 1: Add matplotlib to the Poetry dev group**

```bash
cd /Users/y0shi/workspace/ignite/docs
poetry add --group dev matplotlib
poetry run python -c "import matplotlib; print(matplotlib.__version__)"
```

Paste the resolved version. `pyproject.toml` and `poetry.lock` both change — that is expected
and is this task's only touch on either file.

- [ ] **Step 2: Write the plot generator**

`tools/gen_t4_plot.py`:

```python
"""Regenerates T4's committed response plot from its response data (spec §16).

    poetry run python tools/gen_t4_plot.py

reads projects/testouts/t4-bad-kv/plots/response.csv and overwrites
projects/testouts/t4-bad-kv/plots/response.png. The CSV itself comes from running the T4
fixture's own ResponseRecorder (see plots/README.md) -- this script only turns that data into
the picture, and only that half needs Python.
"""

from __future__ import annotations

import csv
import sys
from pathlib import Path

import matplotlib

matplotlib.use("Agg")

import matplotlib.pyplot as plt  # noqa: E402 -- backend must be selected first

REPO_ROOT = Path(__file__).resolve().parent.parent
DEFAULT_CSV = REPO_ROOT / "projects" / "testouts" / "t4-bad-kv" / "plots" / "response.csv"
DEFAULT_PNG = REPO_ROOT / "projects" / "testouts" / "t4-bad-kv" / "plots" / "response.png"


def read_response(csv_path: Path) -> tuple[list[float], list[float], list[float]]:
    """Read the time, setpoint, and measurement columns out of a T4 response CSV."""
    times: list[float] = []
    setpoints: list[float] = []
    measurements: list[float] = []
    with csv_path.open(newline="", encoding="utf-8") as f:
        for row in csv.DictReader(f):
            times.append(float(row["time_seconds"]))
            setpoints.append(float(row["setpoint_rotations_per_second"]))
            measurements.append(float(row["measurement_rotations_per_second"]))
    return times, setpoints, measurements


def render(
    times: list[float],
    setpoints: list[float],
    measurements: list[float],
    png_path: Path,
) -> None:
    """Draw setpoint vs. measurement and write the figure to `png_path`."""
    fig, ax = plt.subplots(figsize=(8, 4.5), dpi=100)
    ax.plot(times, setpoints, label="setpoint", linestyle="--", color="black", linewidth=1.2)
    ax.plot(times, measurements, label="measured", color="tab:blue", linewidth=1.5)
    ax.set_xlabel("time (s)")
    ax.set_ylabel("flywheel velocity (rotations/s)")
    ax.set_title("T4 bad-kv -- commanded step response")
    ax.legend(loc="lower right")
    ax.grid(True, linewidth=0.3)
    fig.tight_layout()
    fig.savefig(png_path, metadata={"Software": "gen_t4_plot.py"})
    plt.close(fig)


def main(argv: list[str]) -> int:
    csv_path = Path(argv[1]) if len(argv) > 1 else DEFAULT_CSV
    png_path = Path(argv[2]) if len(argv) > 2 else DEFAULT_PNG
    if not csv_path.is_file():
        print(f"no such CSV: {csv_path}", file=sys.stderr)
        return 2
    times, setpoints, measurements = read_response(csv_path)
    render(times, setpoints, measurements, png_path)
    print(f"wrote {png_path}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
```

- [ ] **Step 3: Generate the committed CSV and PNG**

```bash
cd /Users/y0shi/workspace/ignite/docs/projects/testouts/t4-bad-kv
export JAVA_HOME=~/wpilib/2026/jdk
./gradlew generateResponse --console=plain
```

This writes `plots/response.csv` directly (the recorder's working directory is the project
root when run through Gradle). Read the printed summary line again — same check as Task 6 Step
6, confirming nothing drifted.

```bash
cd /Users/y0shi/workspace/ignite/docs
poetry run python tools/gen_t4_plot.py
```

Expected: `wrote .../projects/testouts/t4-bad-kv/plots/response.png`. Open it (or describe it
from the CSV) and confirm by eye: a rise toward 40 rot/s, a visible overshoot above the
setpoint line, a few cycles of decaying oscillation, then a flat settle measurably below the
setpoint line. A plot that looks like a clean exponential rise with no overshoot means Task 6's
Step 6 did not actually catch what it was supposed to catch — stop and go back to that task
rather than shipping this plot.

- [ ] **Step 4: Prove it reproduces byte-identically — not optional**

Spec §16 asks a future maintainer to do exactly this a year from now. If it does not reproduce
today, it will not reproduce then.

```bash
cd /Users/y0shi/workspace/ignite/docs
sha256sum projects/testouts/t4-bad-kv/plots/response.csv projects/testouts/t4-bad-kv/plots/response.png > /tmp/t4-plot-first.sha256
cat /tmp/t4-plot-first.sha256

rm projects/testouts/t4-bad-kv/plots/response.csv projects/testouts/t4-bad-kv/plots/response.png

cd projects/testouts/t4-bad-kv
export JAVA_HOME=~/wpilib/2026/jdk
./gradlew generateResponse --console=plain
cd /Users/y0shi/workspace/ignite/docs
poetry run python tools/gen_t4_plot.py

sha256sum projects/testouts/t4-bad-kv/plots/response.csv projects/testouts/t4-bad-kv/plots/response.png > /tmp/t4-plot-second.sha256
diff /tmp/t4-plot-first.sha256 /tmp/t4-plot-second.sha256; echo "diff rc=$? (expect 0)"
```

Paste both hash files and the diff result. If the PNG hash differs but the CSV hash matches,
the nondeterminism is in `gen_t4_plot.py` (matplotlib metadata is the usual culprit — the
`metadata={"Software": ...}` argument in `render()` exists to pin exactly this) and Step 2 needs
fixing before this task is done. If the CSV hash differs, the Java side is nondeterministic,
which is a Task 6 problem — stop and report rather than loosening this check.

- [ ] **Step 5: Write `plots/README.md`**

`projects/testouts/t4-bad-kv/plots/README.md`:

```markdown
# T4 response plot

`response.png` is generated from `response.csv`, which is itself generated by running the
fixture's shipped (bad) gains for 5 simulated seconds. Neither file is meant to be hand-edited.

## Regenerating

Two commands, from the repository root:

```bash
export JAVA_HOME=~/wpilib/2026/jdk
cd projects/testouts/t4-bad-kv
./gradlew generateResponse --console=plain
cd ../../..
poetry run python tools/gen_t4_plot.py
```

The first runs `ResponseRecorder` (given, `src/main/java/frc/testout/t4/`) against the shipped
`BadlyTunedFlywheel` and overwrites `response.csv`. The second turns that CSV into
`response.png`.

`git status --short` after both should show no changes to either file. If it does, either the
shipped gains in `BadlyTunedFlywheel.java` moved or `tools/gen_t4_plot.py` did, and that is
worth stopping on rather than committing over — see `tools/gen_t4_plot.py`'s docstring and
`tests/test_gen_t4_plot.py` for what "regenerates cleanly" is supposed to mean here.
```

- [ ] **Step 6: Write the pytest coverage**

`tests/test_gen_t4_plot.py` — against a synthetic CSV, never the committed T4 data, so this
suite does not silently start failing the day someone retunes the fixture:

```python
"""Tests for tools/gen_t4_plot.py, against a synthetic CSV -- never the committed T4 data."""

from __future__ import annotations

import csv
from pathlib import Path

from tools.gen_t4_plot import read_response, render

PNG_MAGIC = b"\x89PNG\r\n\x1a\n"

_ROWS = [
    {
        "time_seconds": "0.00",
        "setpoint_rotations_per_second": "40.0",
        "measurement_rotations_per_second": "0.0",
        "applied_volts": "12.0",
    },
    {
        "time_seconds": "0.02",
        "setpoint_rotations_per_second": "40.0",
        "measurement_rotations_per_second": "20.0",
        "applied_volts": "8.0",
    },
    {
        "time_seconds": "0.04",
        "setpoint_rotations_per_second": "40.0",
        "measurement_rotations_per_second": "41.0",
        "applied_volts": "3.0",
    },
    {
        "time_seconds": "0.06",
        "setpoint_rotations_per_second": "40.0",
        "measurement_rotations_per_second": "38.0",
        "applied_volts": "4.5",
    },
]


def _write_synthetic_csv(path: Path) -> None:
    with path.open("w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=list(_ROWS[0].keys()))
        writer.writeheader()
        writer.writerows(_ROWS)


def test_read_response_parses_every_row(tmp_path: Path) -> None:
    csv_path = tmp_path / "response.csv"
    _write_synthetic_csv(csv_path)

    times, setpoints, measurements = read_response(csv_path)

    assert times == [0.0, 0.02, 0.04, 0.06]
    assert setpoints == [40.0, 40.0, 40.0, 40.0]
    assert measurements == [0.0, 20.0, 41.0, 38.0]


def test_render_writes_a_valid_png(tmp_path: Path) -> None:
    csv_path = tmp_path / "response.csv"
    _write_synthetic_csv(csv_path)
    png_path = tmp_path / "response.png"

    times, setpoints, measurements = read_response(csv_path)
    render(times, setpoints, measurements, png_path)

    assert png_path.is_file()
    assert png_path.read_bytes()[:8] == PNG_MAGIC


def test_render_is_deterministic(tmp_path: Path) -> None:
    csv_path = tmp_path / "response.csv"
    _write_synthetic_csv(csv_path)
    first_path = tmp_path / "first.png"
    second_path = tmp_path / "second.png"

    times, setpoints, measurements = read_response(csv_path)
    render(times, setpoints, measurements, first_path)
    render(times, setpoints, measurements, second_path)

    assert first_path.read_bytes() == second_path.read_bytes()
```

```bash
cd /Users/y0shi/workspace/ignite/docs
poetry run pytest tests/test_gen_t4_plot.py -v
```

Expected: three tests, all green. Paste the output.

- [ ] **Step 7: Run the full pytest suite, then commit**

```bash
cd /Users/y0shi/workspace/ignite/docs
poetry run pytest -q
```

Expected: everything green, no regressions in the tools this task did not touch.

```bash
git status --short
git add tools/gen_t4_plot.py tests/test_gen_t4_plot.py pyproject.toml poetry.lock \
    projects/testouts/t4-bad-kv/plots
git commit -m "Generate and commit T4's response plot, regenerable via tools/gen_t4_plot.py"
```

`git status --short` before staging must show nothing under `projects/testouts/t4-bad-kv/` other
than the four new `plots/` files — if `src/` shows as modified, something in Step 3 or Step 4
edited the fixture itself, which is Task 6's territory, not this one's.
