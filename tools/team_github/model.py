from collections.abc import Callable
from dataclasses import dataclass, field


def noop() -> None:
    return None


@dataclass
class Change:
    scope: str
    kind: str
    summary: str
    detail: list[str] = field(default_factory=list)
    apply: Callable[[], None] = noop
    pending: bool = False
    report_only: bool = False

    @property
    def actionable(self) -> bool:
        """True if `--apply` would change GitHub for this item."""
        return not self.pending and not self.report_only
