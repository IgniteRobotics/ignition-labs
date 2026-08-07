<!--
  Publication path: staged build (Task 3, step 7).
  doc-builder renders every markdown file it finds regardless of index
  linkage (confirmed: unindexed superpowers/ docs leaked into a raw
  build-from-source run), so this repo does not build straight from the
  repo root. Task 8's workflow must run, in order:
    poetry run python tools/stage_site.py
    doc-builder build --config mkdocs.yml
    poetry run python tools/site_guard.py ./_site
-->

# Ignite Robotics Documentation

FRC 6829 · Team documentation

## Programming Curriculum

- [Programming Curriculum](programming/README.md)
- [Git Workflow](programming/git-workflow.md)
- [Real-Repo Ladder](programming/robot-repo-ladder.md)
- [AI Use Standard](programming/ai-use-standard.md)
- [Student Deliverables](programming/student-deliverables.md)
- [Resources](programming/resources.md)

## Tracks

- [Track A — New to Programming](programming/tracks/track-a-new-to-programming.md)
- [Track B — Programmed, Not Object-Oriented](programming/tracks/track-b-non-oo-programmer.md)
- [Track C — OO Experience, No FRC](programming/tracks/track-c-oo-no-frc.md)
- [Track D — FRC Veteran, Leveling Up With AI](programming/tracks/track-d-frc-veteran-ai.md)

## Milestones

- [M0 — Tooling, Git & the Build](programming/milestones/m0-tooling-and-git.md)
- [M1 — Java Fundamentals](programming/milestones/m1-java-fundamentals.md)
- [M2 — OOP, Interfaces & Lambdas](programming/milestones/m2-oop-interfaces-lambdas.md)
- [M3 — WPILib Command-Based](programming/milestones/m3-wpilib-command-based.md)
- [M4 — Sensors, Actuators & Control](programming/milestones/m4-sensors-actuators-control.md)
- [M5 — Autonomous, Odometry & Vision](programming/milestones/m5-autonomous-odometry-vision.md)
- [M6 — Simulation, Telemetry & Testing](programming/milestones/m6-simulation-telemetry-testing.md)
- [M7 — AI-Assisted Development](programming/milestones/m7-ai-assisted-development.md)
- [M8 — In-Season Deliverables](programming/milestones/m8-in-season-deliverables.md)

## Vision

- [Vision Guide](VISION_GUIDE.md)
- [Vision Subsystem](vision-subsystem.md)
- [Field Boundary Speed Limiting](field-boundary-speed-limiting.md)

## For Mentors

- [Curriculum Spec](programming/curriculum-spec.md)
- [Teaching Repository Spec](programming/teaching-repo-spec.md)
