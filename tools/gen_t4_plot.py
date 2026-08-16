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
