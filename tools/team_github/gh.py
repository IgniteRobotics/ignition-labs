"""Subprocess boundary. Everything that talks to GitHub goes through a Runner."""

import json
import subprocess
from typing import Any, Protocol


class GhError(Exception):
    def __init__(self, args: list[str], returncode: int, stderr: str) -> None:
        self.args_list = list(args)
        self.returncode = returncode
        self.stderr = stderr
        super().__init__(f"gh {' '.join(args)} failed (rc={returncode}): {stderr.strip()}")


class Runner(Protocol):
    def run(self, args: list[str], *, input: str | None = None) -> str: ...


class GhRunner:
    def run(self, args: list[str], *, input: str | None = None) -> str:
        proc = subprocess.run(
            ["gh", *args], input=input, capture_output=True, text=True, check=False
        )
        if proc.returncode != 0:
            raise GhError(args, proc.returncode, proc.stderr)
        return proc.stdout


class FakeRunner:
    """Replays recorded stdout for an exact args tuple. A GhError value is raised instead."""

    def __init__(self, responses: dict[tuple[str, ...], "str | GhError"]) -> None:
        self.responses = dict(responses)
        self.calls: list[tuple[str, ...]] = []
        self.inputs: list[str | None] = []

    def run(self, args: list[str], *, input: str | None = None) -> str:
        key = tuple(args)
        self.calls.append(key)
        self.inputs.append(input)
        if key not in self.responses:
            raise KeyError(f"unrecorded gh call: {list(key)}")
        response = self.responses[key]
        if isinstance(response, Exception):
            raise response
        return response


def json_call(runner: Runner, args: list[str]) -> Any:
    """Run and parse stdout. `--paginate` prints one JSON array per page; those are merged."""
    text = runner.run(args)
    try:
        return json.loads(text)
    except json.JSONDecodeError:
        decoder = json.JSONDecoder()
        merged: list[Any] = []
        index = 0
        while index < len(text):
            if text[index].isspace():
                index += 1
                continue
            value, index = decoder.raw_decode(text, index)
            if not isinstance(value, list):
                raise
            merged.extend(value)
        return merged
