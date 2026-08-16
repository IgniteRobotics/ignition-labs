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
