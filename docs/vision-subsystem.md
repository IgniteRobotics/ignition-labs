# Vision Subsystem

## Overview

Three PhotonVision cameras (FRONT, LEFT, RIGHT) feed pose estimates into a clustering and fusion pipeline every robot loop cycle (~20ms). The goal is to submit high-quality, timestamped pose measurements to the drivetrain's Kalman filter.

Camera transforms (robot center → camera) are defined in `VisionConstants.cameraTransformMap`. All tuning parameters are live-adjustable via `DoublePreference` and visible in AdvantageScope.

---

## Pipeline

### Step 1: Pose Estimation (per camera)

Each camera result goes through a two-tier pose solver.

**Tier 1 — Multi-tag PNP** (preferred)

Uses multiple AprilTags simultaneously. Rejected immediately if `ambiguity >= 0.25`. The camera-space pose is transformed to field coordinates, then the camera-to-robot offset is inverted to get robot center.

**Tier 2 — Single-tag fallback**

Picks the visible target with the lowest ambiguity. Same ambiguity threshold applies. Computes robot pose by walking backwards: tag field position → invert camera-to-target → invert camera-to-robot.

Both tiers produce a `PendingEstimate` with a pose, timestamp, camera name, tag count, ambiguity, distance, and pre-computed standard deviations.

---

### Step 2: Standard Deviation Calculation

Controls how much the Kalman filter trusts each measurement.

**Multi-tag:**
```
xyStdDev    = xyCoef × dist^1.2 / tagCount
thetaStdDev = thetaCoef × (1 + |ω| × omegaPenalty) × dist^1.2 / tagCount
```

**Single-tag:** Same formulas, but without the `/tagCount` divisor — single tags are inherently less certain.

The `omegaPenalty` term punishes rotation speed: a spinning robot gets motion blur, making heading estimates less reliable.

If `resettingPose` is active, both stddevs are forced to `0.001` — effectively telling the Kalman filter to trust vision completely.

---

### Step 3: Clustering

All estimates collected this cycle (across all cameras) are grouped by proximity. Two estimates cluster together if they are within **0.5m** and **15°** of each other.

| Cluster type | Behavior |
|---|---|
| Single estimate | Jump check → submit |
| Cluster of 1 (outlier) | One camera disagrees with others. Jump check → submit or reject |
| Cluster of 2–3 (consensus) | Multiple cameras agree. Fuse → drift check → jump check → submit |

**Fusion (multi-camera clusters)**

Estimates are fused via inverse-variance weighting:
- Weight per estimate = `1/σ²` — lower uncertainty = more influence
- Weighted average of X and Y positions
- Weighted average of sin/cos of heading (handles angle wraparound correctly)
- Oldest timestamp in the cluster is used (conservative)
- Fused stddev = `(averageStdDev / clusterSize) × trustScalingFactor`

More cameras agreeing produces a lower uncertainty submitted to the Kalman filter.

---

## Jump Detection

Jump detection asks: *"Is this pose estimate too far from where odometry thinks we are?"*

The limit is `maxSpeed × 0.02s` — one loop cycle's worth of travel at full speed. The robot physically couldn't have moved farther than that in a single cycle.

**Jump detection is skipped when:**
- The robot is disabled (no odometry baseline to compare against)
- `resettingPose` is active (intentionally accepting a large correction)
- Drift has been detected (see below)

For multi-camera clusters, jump detection runs on the **fused pose**, not the individual camera estimates.

---

## Drift Detection

Drift detection solves a specific problem: odometry slowly walking away from reality. Wheel encoder errors accumulate over time, and if jump detection is too strict it will reject legitimate vision corrections because the drift looks like a "jump."

Drift detection runs on multi-camera clusters only — it requires consensus to be trustworthy.

**Algorithm:**

1. Compute the vector from odometry pose → fused vision pose (the "jump vector")
2. If jump < `convergenceThreshold` (0.1m): drift has resolved, reset state
3. If jump < `minimumDistance` (0.2m): too small to analyze, return current drift state
4. First large jump: record direction, start counting
5. Subsequent cycles: check if jump is pointing in the same direction (within `directionTolerance` = 0.3m)
   - **Consistent direction for N cycles** (`driftDetectionCycles` = 2): set `driftDetected = true`
   - **Direction changed:** reset counter, clear drift flag

When `driftDetected` is true, jump detection is bypassed and the fused estimate is submitted unconditionally. This lets vision gradually pull odometry back to reality rather than repeatedly rejecting the correction.

Setting `driftDirectionTolerance = 0` disables the direction check entirely — drift triggers purely based on jump magnitude persisting for enough cycles.

---

## Full Cycle Flow

```
Each cycle (~20ms):

  For each camera (FRONT, LEFT, RIGHT):
    → Multi-tag PNP or single-tag fallback
    → Reject if ambiguity >= 0.25
    → Reject if outside field bounds (0–16.7m × 0–8.2m)
    → Compute stddevs (distance, tag count, rotation speed)
    → Add to pendingEstimates

  Cluster pendingEstimates by distance + angle:

    Single estimate:
      → Jump check → submit

    Outlier cluster (1 camera):
      → Jump check → submit or reject

    Multi-camera cluster (2–3 cameras):
      → Fuse via inverse-variance weighting
      → Check for drift
      → Jump check (skipped if drift detected)
      → Submit fused pose with reduced uncertainty
```

---

## Tuning Parameters

All parameters are live-adjustable via NetworkTables and visible in AdvantageScope under the `Vision/` key.

| Parameter | Default | Effect |
|---|---|---|
| `Vision/xyStdDevCoef` | 0.01 | XY uncertainty base coefficient |
| `Vision/thetaStdDevCoef` | 0.03 | Rotation uncertainty base coefficient |
| `Vision/Omega Penalty` | 0.5 | Extra uncertainty per rad/s of rotation |
| `Vision/Jump Limit` | `maxSpeed × 0.02` | Max acceptable pose jump per cycle (m) |
| `Vision/Clustering/Distance Threshold` | 0.5m | Max distance between estimates to cluster |
| `Vision/Clustering/Angle Threshold` | 15° | Max heading difference to cluster |
| `Vision/Clustering/Trust Scaling Factor` | 1.0 | Multiplier on fused stddev |
| `Vision/Drift/Detection Cycles` | 2 | Consecutive cycles before declaring drift |
| `Vision/Drift/Minimum Distance` | 0.2m | Min jump distance to analyze for drift |
| `Vision/Drift/Convergence Threshold` | 0.1m | Jump below this resets drift state |
| `Vision/Drift/Direction Tolerance` | 0.3m | Max direction change to consider consistent (0 = disable) |

---

## Camera Configuration

| Camera | Name | Position (robot center) | Angle |
|---|---|---|---|
| Front | `FRONT-CAMERA` | 0.13m forward, 0.696m up | −22° pitch |
| Left | `LEFT-CAMERA` | 0.252m back, 0.328m left, 0.715m up | 160° yaw |
| Right | `RIGHT-CAMERA` | 0.252m back, 0.328m right, 0.715m up | −160° yaw |
