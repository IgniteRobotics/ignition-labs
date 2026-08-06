# PhotonVision Setup, Configuration & Best Practices Guide

**Target Audience:** Experienced FRC developers
**Hardware:** 3x Arducam OV9281 cameras on Orange Pi 5 coprocessors
**Season:** 2026 Rebuilt

---

## Table of Contents

1. [Camera Hardware Setup](#1-camera-hardware-setup)
2. [Step-by-Step Camera Configuration Walkthrough](#2-step-by-step-camera-configuration-walkthrough)
3. [PhotonVision Settings Reference](#3-photonvision-settings-reference)
4. [Camera Calibration](#4-camera-calibration)
5. [AprilTag Pipeline Tuning](#5-apriltag-pipeline-tuning)
6. [Pose Estimation Strategies](#6-pose-estimation-strategies)
7. [Filtering & Validation](#7-filtering--validation)
8. [Standard Deviation Tuning](#8-standard-deviation-tuning)
9. [Code Review: Current Implementation](#9-code-review-current-implementation)
10. [Recommended Fixes & Improvements](#10-recommended-fixes--improvements)
11. [Troubleshooting: Issue/Resolution Checklist](#11-troubleshooting-issueresolution-checklist)
12. [References](#12-references)

---

## 1. Camera Hardware Setup

### Arducam OV9281 Overview

The OV9281 is ideal for AprilTag detection:
- **Global shutter** - eliminates rolling shutter distortion during motion
- **Monochromatic sensor** - optimized for high contrast tag detection
- **70-degree horizontal FOV** - good coverage without excessive distortion
- **High framerate capable** - critical for reducing motion blur

### Camera Naming

Each camera must have a unique device name. Use the Arducam utility to rename cameras (e.g., `FRONT-CAMERA`, `LEFT-CAMERA`, `RIGHT-CAMERA`). This utility only works with Arducam cameras.

### Physical Mounting Considerations

Current transforms from `CameraConstants.java`:

| Camera | Translation (X, Y, Z) | Rotation (Roll, Pitch, Yaw) |
|--------|----------------------|----------------------------|
| Front  | (-0.1638, 0, 0.7192) | (0, -20deg, 0) |
| Left   | (-0.2680, 0.3164, 0.451) | (0, -20deg, 120deg) |
| Right  | (-0.2680, -0.3164, 0.451) | (0, -20deg, -120deg) |

**Critical:** Camera transforms must be measured precisely. Errors in these transforms directly translate to pose estimation errors. Use calipers and verify measurements multiple times.

**Mounting Best Practice:** Mount cameras at oblique angles (not perpendicular to typical tag viewing angles) to reduce pose ambiguity. Your -20 degree pitch is good for this.

---

## 2. Step-by-Step Camera Configuration Walkthrough

This section walks through configuring a camera from initial connection to competition-ready AprilTag detection.

### Step 1: Initial Connection & Camera Discovery

1. **Connect to PhotonVision web interface**
   - Connect laptop to robot network
   - Navigate to `http://photonvision.local:5800` (or IP address)
   - Each Orange Pi runs its own PhotonVision instance

2. **Verify camera is detected**
   - Camera should appear in left sidebar
   - If not detected, check USB connection and power

3. **Rename the camera** (Critical for multi-camera setups)
   - Use Arducam utility (on a Windows PC) to set unique device names
   - Names should match your code: `FRONT-CAMERA`, `LEFT-CAMERA`, `RIGHT-CAMERA`
   - PhotonVision will recognize these names after restart

### Step 2: Select Camera Type & Resolution

1. **Set camera type**
   - Go to **Settings** tab
   - Under camera settings, select **OV9281 (quirk)** for Arducam OV9281
   - This applies appropriate driver quirks for the sensor

2. **Select resolution**
   - Higher resolution = better accuracy, lower framerate
   - **Recommended:** 1280x800 @ 60fps or 1920x1200 @ 30fps
   - For competition, prioritize framerate if lighting is good

3. **Verify stream is working**
   - You should see live camera feed in the web interface
   - Check that image is in focus (OV9281 has manual focus ring)

### Step 3: Configure Exposure Settings

**Goal:** Minimize motion blur while maintaining tag visibility

1. **Disable auto exposure**
   - In the **Input** tab, toggle off auto exposure
   - Auto exposure causes inconsistent behavior under field lights

2. **Set exposure as LOW as possible**
   - Start at the minimum value
   - Gradually increase until tags are barely visible
   - Lower exposure = less motion blur = better detection while moving

3. **Compensate with gain/brightness**
   - Increase **Gain** to brighten the image without adding motion blur
   - Adjust **Brightness** as needed
   - Target: Tags clearly visible, no flicker, minimal blur when moving

4. **Test with motion**
   - Move the robot and verify tags are still detected
   - If tags disappear during motion, exposure is too high

**Typical OV9281 values:**
| Setting | Starting Value | Notes |
|---------|---------------|-------|
| Exposure | 5-15 ms | As low as possible |
| Gain | 20-50 | Compensate for low exposure |
| Brightness | 50 | Adjust as needed |

### Step 4: Create AprilTag Pipeline

1. **Create new pipeline**
   - Click **+** next to Pipelines
   - Name it descriptively: `AprilTag-3D` or `Competition`

2. **Select pipeline type**
   - Choose **AprilTag** from dropdown

3. **Set tag family**
   - Select **36h11** (2026 FRC standard)

### Step 5: Configure Detection Parameters

Navigate to the **Threshold** or **AprilTag** tab:

1. **Decimate**
   - **What it does:** Downscales image before detection
   - **Value 1:** Full resolution, slower, max detection distance
   - **Value 2:** Half resolution, faster, reduced range
   - **Recommended:** 2 for competition (good balance)

2. **Blur**
   - **What it does:** Applies Gaussian blur before detection
   - **Value 0:** No blur (recommended)
   - **Higher values:** Computationally expensive, rarely needed
   - **Recommended:** 0

3. **Threads**
   - **What it does:** CPU threads for detection algorithm
   - **Recommended:** Number of CPU cores minus 1
   - **Orange Pi 5:** Set to 7 (has 8 cores)

4. **Refine Edges**
   - **What it does:** Snaps detected edges to high-contrast boundaries
   - **Recommended:** Enabled (improves accuracy, especially with decimate)

5. **Decision Margin Cutoff**
   - **What it does:** Rejects low-confidence detections
   - **Higher value:** More strict, fewer false positives
   - **Lower value:** More detections, more noise
   - **Recommended:** 30-50

6. **Max Error Bits (Hamming Distance)**
   - **What it does:** Bit errors allowed for tag ID
   - **For 36h11:** Maximum 3 (the "11" in 36h11)
   - **Recommended:** 1-2 (rejects damaged/noisy tags)

### Step 6: Enable 3D Mode

1. **Verify calibration exists**
   - Go to **Calibration** tab
   - Ensure calibration data exists for your selected resolution
   - If not, complete calibration first (see Section 4)

2. **Enable 3D mode**
   - In **Output** tab, enable **3D**
   - This enables pose estimation from detected tags

3. **Verify 3D data**
   - Point camera at a tag
   - You should see X, Y, Z coordinates and rotation values

### Step 7: Enable Multi-Tag Estimation

1. **Enable multi-target estimation**
   - In **Output** tab, enable **"Do Multi-Target Estimation"**

2. **Upload correct field layout**
   - Go to **Settings** > **Device Control**
   - Click **Import Settings**
   - Select **AprilTag Layout** type
   - Upload `2026-rebuilt-welded.json` (must match robot code)

3. **Verify multi-tag working**
   - Point camera at 2+ tags simultaneously
   - Multi-tag result should appear with combined pose

4. **Optional: Enable single-target fallback**
   - Enable **"Always Do Single-Target Estimation"**
   - Provides fallback when only one tag visible
   - Slight performance impact

### Step 8: Verify & Test

1. **Check detection at various distances**
   - Near (1m): Should detect reliably
   - Mid (3m): Should detect reliably
   - Far (5m+): May have issues, tune if needed

2. **Check detection at angles**
   - Head-on: Should work
   - 45 degrees: Should work
   - 60+ degrees: May have ambiguity issues

3. **Check during motion**
   - Drive robot slowly while watching detections
   - Tags should remain detected
   - If losing tags, reduce exposure further

4. **Verify pose accuracy**
   - Place robot at known position
   - Compare PhotonVision pose to actual position
   - Errors > 10cm indicate calibration or transform issues

### Step 9: Save Configuration

1. **Export settings**
   - Go to **Settings** > **Device Control**
   - Click **Export Settings**
   - Save backup before competition

2. **Verify settings persist**
   - Restart PhotonVision
   - Verify all settings are retained

---

## 3. PhotonVision Settings Reference

### Input Tab Settings

| Setting | What It Controls | Recommended | Notes |
|---------|-----------------|-------------|-------|
| **Resolution** | Image dimensions | 1280x800 | Balance accuracy vs speed |
| **Camera Type** | Driver/quirks | OV9281 (quirk) | Required for Arducam |
| **Auto Exposure** | Automatic exposure | OFF | Disable for consistency |
| **Exposure** | Shutter time (ms) | 5-15 | Lower = less motion blur |
| **Gain** | Signal amplification | 20-50 | Compensate for low exposure |
| **Brightness** | Image brightness | 50 | Adjust for visibility |
| **Red/Blue Balance** | Color balance | N/A | Not relevant for mono camera |

### AprilTag Pipeline Settings

| Setting | What It Controls | Recommended | Notes |
|---------|-----------------|-------------|-------|
| **Tag Family** | Tag encoding type | 36h11 | 2026 FRC standard |
| **Decimate** | Image downscale factor | 2 | 1=full res, 2=half |
| **Blur** | Gaussian blur sigma | 0 | Keep at 0 |
| **Threads** | CPU threads | 7 | Cores minus 1 |
| **Refine Edges** | Edge refinement | ON | Improves accuracy |
| **Pose Iterations** | Solver iterations | 40-100 | See tuning notes |
| **Decision Margin** | Detection threshold | 30-50 | Higher = stricter |
| **Max Error Bits** | Bit error tolerance | 1-2 | Max 3 for 36h11 |

### Output Tab Settings

| Setting | What It Controls | Recommended | Notes |
|---------|-----------------|-------------|-------|
| **3D Mode** | Enable pose estimation | ON | Required for pose |
| **Multi-Target** | Multi-tag estimation | ON | More accurate poses |
| **Always Single** | Single-tag fallback | ON/OFF | Performance trade-off |

### Effect of Decimate Values

| Decimate | Resolution (1280x800) | Detection Range | Speed |
|----------|----------------------|-----------------|-------|
| 1 | 1280x800 | Maximum | Slowest |
| 2 | 640x400 | Good | Fast |
| 3 | 426x266 | Reduced | Faster |
| 4 | 320x200 | Limited | Fastest |

### Effect of Pose Iterations

| Value | Behavior | Use Case |
|-------|----------|----------|
| 10-30 | Noisy poses, fast | Not recommended |
| 40-60 | Balanced | Good starting point |
| 80-100 | Stable poses, may stick to wrong solution | High accuracy needs |
| 100+ | Diminishing returns | Not recommended |

---

## 4. Camera Calibration

### Why Calibration Matters

Calibration is **mandatory** for 3D mode and AprilTag pose estimation. Inaccurate calibration directly causes:
- Incorrect distance estimates
- Incorrect angle estimates
- Increased pose ambiguity

### Calibration Board Requirements

- **Board type:** ChArUco board
- **Tag family:** 4x4
- **Pattern spacing:** 1.00 inches
- **Marker size:** 0.75 inches
- **Board size:** 8x8

**Critical:** The calibration board must be **perfectly flat**. Wrinkles or bends introduce calibration errors.

### Calibration Procedure

1. **Measure your board** - Use calipers to measure actual square and marker dimensions. Do not trust nominal values.

2. **Capture 50+ images** - 12 is the bare minimum but often produces poor results.

3. **Board positioning:**
   - Move the board, not the camera
   - Cover the entire camera FOV
   - Capture from numerous angles (up to 45 degrees)
   - Include close-up shots
   - **Avoid** positioning the board directly facing the camera

4. **Quality indicators:**
   - FOV should be within +/-10 degrees of manufacturer spec
   - Mean pixel error should be **less than 1**

### Alternative: Cowlibration

[The Holy Cows' cowlibration-camera](https://github.com/TheHolyCows) tool uses 30-40 seconds of video and can significantly reduce mean pixel error. Requires manual JSON editing after a mock calibration.

### Known OV9281 Calibration Issue

Some users report "camera lost" graphics after calibration. Solution: Unplug and replug USB cable.

---

## 5. AprilTag Pipeline Tuning

### 2D vs 3D Mode

- **2D mode:** Only provides tag position in image (yaw/pitch angles)
- **3D mode:** Provides full 6-DOF pose estimate (requires calibration)

For pose estimation, you must use 3D mode.

### Pose Iterations

Controls how many iterations the algorithm uses to converge on a pose solution:
- **Lower values (0-100):** More noisy poses, especially when viewing tags head-on
- **Higher values:** More consistent poses, but may stick to wrong pose solutions

Recommended: Start with default, tune based on observed behavior.

### Enable Multi-Target Estimation

In the Output tab:
1. Enable **"Do Multi-Target Estimation"**
2. Optionally enable **"Always Do Single-Target Estimation"** for fallback (impacts performance)

---

## 6. Pose Estimation Strategies

### Multi-Tag PnP (Recommended)

Combines all visible tag corners into a single pose calculation. Benefits:
- More accurate than single-tag
- Reduces pose ambiguity
- Uses field layout to constrain solution

**Requirements:**
- Accurate field layout JSON
- 3D mode enabled
- Camera calibrated
- Multiple tags visible

### Single-Tag PnP (Fallback)

Uses the four corners of a single tag to solve for pose. Limitations:
- **Pose ambiguity:** Can flip between two valid solutions
- Less accurate at distance
- Ambiguity ratio indicates quality (lower = better)

### PhotonPoseEstimator Strategies

| Strategy | Description | Use Case |
|----------|-------------|----------|
| MULTI_TAG_PNP_ON_COPROCESSOR | Runs multi-tag on Orange Pi | **Recommended default** |
| LOWEST_AMBIGUITY | Picks pose with lowest ambiguity | Single-tag fallback |
| CLOSEST_TO_REFERENCE_POSE | Picks pose closest to expected | Filtering outliers |
| AVERAGE_BEST_TARGETS | Averages poses from all tags | Smoothing |
| PNP_DISTANCE_TRIG_SOLVE | Uses heading data for solve | Special cases |

### Ambiguity

Ambiguity ratio measures relative reprojection errors between competing pose solutions:
- **< 0.2:** Generally safe to trust
- **> 0.25:** Likely ambiguous, consider rejecting

Current code uses `MAXIMUM_ALLOWED_AMBIGUITY = 0.25` which is appropriate.

---

## 7. Filtering & Validation

### Pre-Fusion Filtering

Before adding vision measurements to the pose estimator, validate:

1. **Ambiguity check** - Reject high-ambiguity poses
2. **Distance sanity check** - Reject poses too far from current estimate
3. **Tag validity check** - Ensure tag IDs exist in field layout
4. **Bounds check** - Reject poses outside field boundaries
5. **Height check** - Robot Z should be near zero (for 2D estimators)

### Jump Detection

Detects if the pose "jumps" unrealistically between frames:

```java
double maxJump = maxSpeed * dt;  // Maximum possible movement
if (estimateDistance > maxJump) {
    reject();
}
```

Current code has this **commented out** for multi-tag results. This should be reconsidered.

### Angular Velocity Filtering

When rotating quickly, vision accuracy degrades. Consider:
- Increasing standard deviations during high angular velocity
- Rejecting measurements when `omega > threshold`

### Multi-Tag vs Single-Tag Trust

Multi-tag results should be trusted more than single-tag:
- Use lower standard deviations for multi-tag
- Apply stricter filtering to single-tag

---

## 8. Standard Deviation Tuning

### How Standard Deviations Work

The Kalman filter uses standard deviations to weight measurements:
- **Smaller std dev** = more trust in that measurement
- **Larger std dev** = less trust

WPILib defaults:
- Odometry: [0.1m, 0.1m, 0.1rad]
- Vision: [0.9m, 0.9m, 0.9rad]

### Distance-Based Scaling

Pose accuracy degrades with distance. Current code scales by `distance^1.2`:

```java
double xyStdDev = XY_STD_DEV_COEFFICIENT * Math.pow(distance, 1.2);
```

This is reasonable. Teams commonly use exponents from 1.0 to 2.0.

### Tag Count Scaling

More visible tags = more accurate. Current code divides by `numTags^2`:

```java
xyStdDev = coefficient * distance^1.2 / numTags^2;
```

The squared division may be too aggressive. Consider `numTags^1` or `numTags^1.5`.

### Heading Standard Deviation

**Key insight from WPILib docs:** When using AprilTags, make the vision heading standard deviation **very large** and the gyro heading standard deviation **small**.

Rationale: Gyros measure heading changes very accurately. Vision heading is less reliable.

Current coefficients:
- `XY_STD_DEV_COEFFICIENT = 0.01`
- `THETA_STD_DEV_COEFFICIENT = 0.03`

**Recommendation:** Consider increasing `THETA_STD_DEV_COEFFICIENT` significantly (e.g., 0.1 - 0.5) to trust gyro more for heading.

### Dynamic Trust Adjustment

Consider adjusting trust based on:
- Robot angular velocity (less trust when spinning)
- Number of tags visible
- Tag distance
- Camera-to-tag angle (less trust at extreme angles)

---

## 9. Code Review: Current Implementation

### Architecture Overview

```
PhotonCamera → VisionSubsystem.evaluateResult() → DriveState (queue)
    → DrivetrainSubsystem.periodic() → addVisionMeasurement() → Kalman Filter
    → LaunchCalculator → Aiming
```

### VisionSubsystem.java Issues

#### Issue 1: Jump Detection Inconsistency

Multi-tag path (lines 166-170): Jump detection is **commented out**
```java
// if (!RobotModeTriggers.disabled().getAsBoolean()
//     && estimateDistance > TunerConstants.kSpeedAt12Volts.in(MetersPerSecond) * 0.02) {
//   logBadResult(cameraName, "JUMPING");
//   return;
// }
```

Single-tag path (lines 205-206): Jump detection is **active**
```java
if (!RobotModeTriggers.disabled().getAsBoolean()
    && estimateDistance > TunerConstants.kSpeedAt12Volts.in(MetersPerSecond) * 0.02)
    return;
```

**Problem:** Inconsistent filtering between multi-tag and single-tag paths. Also, single-tag path doesn't log the rejection.

#### Issue 2: Timestamp Handling

Line 193 uses current time instead of camera timestamp:
```java
Utils.getCurrentTimeSeconds()
```

Should use the PhotonVision result timestamp for latency compensation.

#### Issue 3: Missing Field Bounds Check

No validation that estimated pose is within field boundaries.

#### Issue 4: No Angular Velocity Consideration

Standard deviations don't account for robot angular velocity during measurement.

#### Issue 5: Aggressive Tag Count Scaling

Dividing by `numTags^2` may over-trust multi-tag when only 2 tags visible:
- 1 tag: divide by 1
- 2 tags: divide by 4 (4x better trust)
- 3 tags: divide by 9 (9x better trust)

#### Issue 6: Missing Logging for Accepted Results

Only rejections are logged. Consider logging accepted result details for debugging.

### CameraConstants.java Issues

#### Issue 7: Missing Maximum Distance Filter

No maximum distance constant defined. Tags at long range are less reliable.

#### Issue 8: Static Camera Transforms

Consider making transforms tunable via NetworkTables for field adjustments.

### LaunchCalculator.java Issues

#### Issue 9: Pose Not Updated After Twist

Line 70 calls `exp()` but doesn't use the result:
```java
estimatedRobotPose.exp(new Twist2d(...));
```

`Pose2d.exp()` returns a new Pose2d, it doesn't mutate. Should be:
```java
estimatedRobotPose = estimatedRobotPose.exp(new Twist2d(...));
```

**This is a bug that affects aiming accuracy.**

#### Issue 10: Angular Velocity Calculation Issue

Lines 128-131 calculate angular velocity incorrectly:
```java
RadiansPerSecond.of(
    targetRobotAngle
        .minus(DriveState.getInstance().getPreviousDriveStats().Pose.getRotation())
        .getRadians());
```

This isn't divided by time, so it's not velocity - it's just angular difference.

### DrivetrainSubsystem.java Issues

#### Issue 11: PathPlanner PID Not Dynamic

AutoBuilder is configured once in constructor with static PID values. If preferences change, PathPlanner won't see them until robot restart.

---

## 10. Recommended Fixes & Improvements

### High Priority (Directly Affects Accuracy)

#### Fix 1: Correct the Pose Prediction Bug

In `LaunchCalculator.java` line 70-74:
```java
// BEFORE (bug):
estimatedRobotPose.exp(new Twist2d(...));

// AFTER (fixed):
estimatedRobotPose = estimatedRobotPose.exp(new Twist2d(...));
```

#### Fix 2: Use Camera Timestamps

In `VisionSubsystem.java`, replace:
```java
Utils.getCurrentTimeSeconds()
```
with:
```java
result.getTimestampSeconds()
```

#### Fix 3: Enable Jump Detection for Multi-Tag

Uncomment and fix the jump detection for multi-tag results.

#### Fix 4: Increase Theta Standard Deviation

In `CameraConstants.java`:
```java
// Consider changing from:
public static final double THETA_STD_DEV_COEFFICIENT = 0.03;
// To something like:
public static final double THETA_STD_DEV_COEFFICIENT = 0.1;
```

This trusts the gyro more for heading.

### Medium Priority (Improves Robustness)

#### Fix 5: Add Field Bounds Validation

```java
private static final double FIELD_LENGTH = 16.54;  // meters
private static final double FIELD_WIDTH = 8.21;    // meters

private boolean isWithinField(Pose3d pose) {
    double x = pose.getX();
    double y = pose.getY();
    double margin = 0.5;  // Allow some margin for measurement noise
    return x > -margin && x < FIELD_LENGTH + margin
        && y > -margin && y < FIELD_WIDTH + margin;
}
```

#### Fix 6: Add Maximum Distance Filter

```java
public static final double MAXIMUM_TAG_DISTANCE = 5.0;  // meters

// In evaluateEstimation:
if (averageRobotToTagDistance > MAXIMUM_TAG_DISTANCE) {
    logBadResult(cameraName, "DISTANCE");
    return;
}
```

#### Fix 7: Reduce Tag Count Scaling Aggressiveness

```java
// Change from:
/ Math.pow(targetsUsed.size(), 2)
// To:
/ Math.pow(targetsUsed.size(), 1.5)
```

#### Fix 8: Add Angular Velocity Penalty

```java
double omega = Math.abs(driveStats.Speeds.omegaRadiansPerSecond);
double omegaPenalty = 1.0 + omega * 0.5;  // Increase std dev when rotating

double xyStdDev = XY_STD_DEV_COEFFICIENT
    * Math.pow(averageRobotToTagDistance, 1.2)
    * omegaPenalty
    / Math.pow(targetsUsed.size(), 1.5);
```

### Lower Priority (Nice to Have)

#### Fix 9: Make Camera Transforms Tunable

Use NetworkTables-backed preferences for camera transforms to allow field adjustments.

#### Fix 10: Log Accepted Results

Add logging for accepted vision measurements to aid debugging:
```java
SmartDashboard.putNumber("Vision/" + cameraName + "/Distance", distance);
SmartDashboard.putNumber("Vision/" + cameraName + "/NumTags", targetsUsed.size());
SmartDashboard.putNumber("Vision/" + cameraName + "/StdDev", xyStdDev);
```

#### Fix 11: Add Camera Latency Compensation

Use `samplePoseAt()` to compare vision pose against odometry at the same timestamp rather than current pose.

---

## 11. Troubleshooting: Issue/Resolution Checklist

### Detection Issues

#### Issue: No Tags Detected

| Possible Cause | How to Verify | Resolution |
|---------------|---------------|------------|
| Camera not connected | Check PhotonVision sidebar | Verify USB connection, try different port |
| Wrong pipeline selected | Check pipeline dropdown | Select AprilTag pipeline |
| Tag family mismatch | Check pipeline settings | Set to 36h11 for 2026 |
| Exposure too low | Image is very dark | Increase gain/brightness, not exposure |
| Exposure too high | Tags blur when moving | Decrease exposure |
| Camera out of focus | Image appears blurry | Adjust manual focus ring on OV9281 |
| Decimate too high | Can't detect distant tags | Reduce decimate value |
| Decision margin too high | Detecting intermittently | Lower decision margin (try 20-30) |

#### Issue: Tags Detected Intermittently

| Possible Cause | How to Verify | Resolution |
|---------------|---------------|------------|
| Low light conditions | Image appears dark | Increase gain, improve field lighting |
| Motion blur | Tags disappear when moving | Reduce exposure, increase gain |
| Tag partially obscured | Check camera view | Clear obstructions, adjust mounting |
| Network latency | Check NetworkTables lag | Verify network configuration |
| CPU overload | Check Orange Pi temps | Reduce threads, increase decimate |

#### Issue: False Positive Detections

| Possible Cause | How to Verify | Resolution |
|---------------|---------------|------------|
| Decision margin too low | Getting random IDs | Increase decision margin (40-50) |
| Max error bits too high | Detecting non-tags | Reduce max error bits to 1 |
| Reflections on field | Tags appearing in wrong places | Adjust camera angle, check for mirrors |

### 3D Pose Issues

#### Issue: No 3D Data (Only 2D)

| Possible Cause | How to Verify | Resolution |
|---------------|---------------|------------|
| 3D mode disabled | Check Output tab | Enable 3D mode |
| Missing calibration | Check Calibration tab | Complete camera calibration |
| Wrong resolution calibration | Resolution doesn't match | Recalibrate at current resolution |

#### Issue: Pose is Completely Wrong (Meters Off)

| Possible Cause | How to Verify | Resolution |
|---------------|---------------|------------|
| Wrong field layout | Compare layout to actual | Upload correct field layout JSON |
| Camera transform wrong | Check constants | Re-measure camera position/rotation |
| Calibration corrupted | Check calibration quality | Recalibrate camera |
| Wrong coordinate system | Compare axes | Verify WPILib coordinate conventions |

#### Issue: Pose Has Consistent Offset

| Possible Cause | How to Verify | Resolution |
|---------------|---------------|------------|
| Camera transform error | Measure with robot at known pose | Correct X, Y, Z translation values |
| Camera rotation error | Check pose angle vs actual | Correct roll, pitch, yaw values |
| Field layout offset | Check tag positions | Verify field layout matches physical tags |

#### Issue: Pose Jitters/Oscillates

| Possible Cause | How to Verify | Resolution |
|---------------|---------------|------------|
| Poor calibration | Check mean pixel error | Recalibrate with 50+ images, error < 1 |
| Low pose iterations | Check pipeline settings | Increase to 60-100 |
| Single-tag ambiguity | Only 1 tag visible | Enable multi-tag, add more cameras |
| High exposure | Motion blur visible | Reduce exposure |
| Low resolution | Image quality poor | Increase resolution if FPS allows |

#### Issue: Pose Flips Between Two Values (Ambiguity)

| Possible Cause | How to Verify | Resolution |
|---------------|---------------|------------|
| Camera perpendicular to tag | Check camera angle | Mount camera at angle (20-30 deg tilt) |
| Single tag only | Only 1 tag in view | Use multi-tag estimation |
| Ambiguity ratio high | Log ambiguity values | Lower MAXIMUM_ALLOWED_AMBIGUITY |
| Tag at long range | Check tag distance | Get closer or use multiple cameras |

#### Issue: Pose Drifts Over Time

| Possible Cause | How to Verify | Resolution |
|---------------|---------------|------------|
| Vision not being applied | Check addVisionMeasurement calls | Verify vision subsystem is running |
| Std dev too high | Vision being ignored | Reduce vision standard deviations |
| Timestamps wrong | Check latency | Use camera timestamps, not current time |
| Gyro drift | Heading accumulates error | Vision heading std dev may be too high |

### Multi-Tag Issues

#### Issue: Multi-Tag Not Working

| Possible Cause | How to Verify | Resolution |
|---------------|---------------|------------|
| Multi-tag disabled | Check Output tab | Enable "Do Multi-Target Estimation" |
| Field layout missing | Check settings | Upload field layout JSON |
| Only 1 tag visible | Check camera view | Position to see multiple tags |
| Field layout mismatch | Compare to robot code | Ensure layouts match exactly |

#### Issue: Multi-Tag Less Accurate Than Single-Tag

| Possible Cause | How to Verify | Resolution |
|---------------|---------------|------------|
| Field layout error | Check tag positions | Verify physical tag positions match JSON |
| One tag miscalibrated | Test individual tags | Identify and replace damaged tag |
| Camera transform error | Test at various positions | Re-measure camera mounting |

### Performance Issues

#### Issue: Low Framerate

| Possible Cause | How to Verify | Resolution |
|---------------|---------------|------------|
| Resolution too high | Check FPS display | Lower resolution |
| Decimate too low | Check CPU usage | Increase decimate to 2 |
| Too many threads | CPU thermal throttling | Reduce threads |
| USB bandwidth | Multiple cameras on same bus | Distribute across USB controllers |

#### Issue: High Latency

| Possible Cause | How to Verify | Resolution |
|---------------|---------------|------------|
| Network congestion | Check other NT traffic | Optimize network usage |
| Processing delay | Check pipeline time | Increase decimate, reduce resolution |
| Queue backup | Vision measurements delayed | Process all unread results each loop |

### Robot Code Issues

#### Issue: Vision Measurements Not Applied

| Possible Cause | How to Verify | Resolution |
|---------------|---------------|------------|
| Subsystem not registered | Check scheduler | Register VisionSubsystem |
| DriveState not initialized | Check singleton | Verify DriveState.getInstance() |
| Queue never read | Check DrivetrainSubsystem | Verify grabVisionEstimateList called |
| Timestamps converted wrong | Check addVisionMeasurement | Use proper timestamp conversion |

#### Issue: Too Many Rejections

| Possible Cause | How to Verify | Resolution |
|---------------|---------------|------------|
| Ambiguity threshold too strict | Check rejection logs | Increase MAXIMUM_ALLOWED_AMBIGUITY |
| Jump threshold too strict | Check rejection logs | Tune jump detection threshold |
| Distance threshold too strict | Check rejection logs | Increase max tag distance |

### Quick Diagnostic Commands

```java
// Add these to see what's happening:

// In VisionSubsystem - log every result
SmartDashboard.putBoolean("Vision/HasTargets", result.hasTargets());
SmartDashboard.putNumber("Vision/TargetCount", result.getTargets().size());
SmartDashboard.putBoolean("Vision/HasMultiTag", !result.multitagResult.isEmpty());

// In DrivetrainSubsystem - verify measurements received
SmartDashboard.putNumber("Vision/QueueSize/Front",
    driveState.grabVisionEstimateList(CameraConstants.photonCameraName_Front).size());

// Compare vision vs odometry
Pose2d visionPose = estimate.getEstimatedPose();
Pose2d odoPose = getState().Pose;
SmartDashboard.putNumber("Vision/DeltaX", visionPose.getX() - odoPose.getX());
SmartDashboard.putNumber("Vision/DeltaY", visionPose.getY() - odoPose.getY());
```

### Configuration Validation Checklist

Before each event, verify:

- [ ] **Camera names match code** - `FRONT-CAMERA`, `LEFT-CAMERA`, `RIGHT-CAMERA`
- [ ] **Camera type set correctly** - OV9281 (quirk)
- [ ] **Auto exposure disabled** - Manual exposure only
- [ ] **Exposure minimized** - Lowest value with visible tags
- [ ] **Resolution matches calibration** - Same resolution used for both
- [ ] **Calibration quality good** - Mean error < 1, FOV within spec
- [ ] **3D mode enabled** - On Output tab
- [ ] **Multi-tag enabled** - On Output tab
- [ ] **Field layout uploaded** - Matches robot code exactly
- [ ] **Tag family correct** - 36h11
- [ ] **Decimate reasonable** - 2 for most cases
- [ ] **Decision margin set** - 30-50 range

---

## 12. References

### PhotonVision Documentation
- [MultiTag Localization](https://docs.photonvision.org/en/latest/docs/apriltag-pipelines/multitag.html)
- [3D Tracking](https://docs.photonvision.org/en/latest/docs/apriltag-pipelines/3D-tracking.html)
- [PhotonPoseEstimator](https://docs.photonvision.org/en/latest/docs/programming/photonlib/robot-pose-estimator.html)
- [Coordinate Systems](https://docs.photonvision.org/en/latest/docs/apriltag-pipelines/coordinate-systems.html)
- [Advanced Strategies](https://docs.photonvision.org/en/latest/docs/integration/advancedStrategies.html)

### WPILib Documentation
- [Pose Estimators](https://docs.wpilib.org/en/stable/docs/software/advanced-controls/state-space/state-space-pose-estimators.html)
- [SwerveDrivePoseEstimator API](https://github.wpilib.org/allwpilib/docs/release/java/edu/wpi/first/math/estimator/SwerveDrivePoseEstimator.html)
- [AprilTags Introduction](https://docs.wpilib.org/en/stable/docs/software/vision-processing/apriltag/apriltag-intro.html)

### Team Resources
- [FRC 6328 Mechanical Advantage 2026 Build Thread](https://www.chiefdelphi.com/t/frc-6328-mechanical-advantage-2026-build-thread/509595)
- [FRC 1678 Citrus Circuits GitHub](https://github.com/frc1678)
- [Chief Delphi: Kalman Filter for Pose Estimation](https://www.chiefdelphi.com/t/is-a-kalman-filter-a-viable-way-to-get-an-accurate-pose-estimation/494180)
- [OV9281 Calibration Thread](https://www.chiefdelphi.com/t/photonvision-ov9281-camera-calibration/494087)
- [PhotonVision 2026 Releases](https://www.chiefdelphi.com/t/photonvision-2026-releases-2026-3-1/512436)

### Calibration Tools
- [TheHolyCows cowlibration-camera](https://github.com/TheHolyCows)

---

## Appendix A: Quick Checklist

### Pre-Competition Checklist

- [ ] All cameras uniquely named
- [ ] Camera transforms measured and verified
- [ ] Each camera calibrated at competition resolution (50+ images, mean error < 1)
- [ ] Field layout JSON matches robot code (`k2026RebuiltWelded`)
- [ ] Multi-Target Estimation enabled
- [ ] Exposure minimized, gain/brightness adjusted
- [ ] All cameras detecting tags from expected positions
- [ ] Verify pose estimate accuracy by driving to known positions
- [ ] Test vision during rotation to check for jitter
- [ ] Log rejection statistics and tune thresholds

### Debugging Pose Issues

1. **Jitter:** Check calibration quality, reduce exposure, increase resolution
2. **Drift:** Verify camera transforms, check field layout, verify gyro
3. **Jumps:** Enable/tune jump detection, check for network latency
4. **Inaccurate at distance:** Lower trust for distant tags, verify calibration
5. **Ambiguity flipping:** Mount cameras at angles, use multi-tag when possible

---

*Document generated: March 2026*
*Last updated: March 16, 2026*
