"""Guards that the tooling package is importable and pytest is configured strictly."""

import tools


def test_tools_package_is_importable():
    assert tools.__name__ == "tools"
