# Field Boundary Speed Limiting System - Design Document

> **Who this is for:** mentors assigning a design project, and the student or pair who
> picks it up. This is not reference material to read — it is a spec to implement.

**Purpose**: Guide for implementing a system that protects the robot from wall collisions by gradually reducing speed near field boundaries.

**Estimated complexity**: Medium (4 new files, 1 modification)

---

## 1. Problem Statement

During teleop, drivers can accidentally drive the robot into field walls at full speed, potentially damaging mechanisms or tipping the robot. This is especially dangerous when the intake is deployed, as it extends 9 inches beyond the bumpers.

**Goals:**
1. Gradually slow the robot as it approaches any field wall
2. Smoothly stop the robot before collision
3. Account for robot orientation (a rotated robot has corners that reach further)
4. Protect extended mechanisms (intake) by adjusting the "stopping zone" when deployed

---

## 2. Concepts & Background

### 2.1 Field Coordinate System

The FRC field uses a coordinate system with:
- **Origin (0, 0)**: Blue alliance driver station corner
- **X-axis**: Runs the length of the field (0 to 16.54 meters)
- **Y-axis**: Runs the width of the field (0 to 8.21 meters)
- **Rotation**: Counter-clockwise positive, 0° pointing toward red alliance

```
        Y = 8.21m
        ┌────────────────────────────────────┐
        │                                    │
        │              FIELD                 │
  X=0   │                                    │  X = 16.54m
 (Blue) │                                    │  (Red)
        │                                    │
        └────────────────────────────────────┘
        Y = 0
```

### 2.2 Robot Geometry

Our robot is square when viewed from above:
- **Frame perimeter**: 27" x 27"
- **With bumpers**: 34" x 34" (bumpers add ~3.5" per side)
- **Half-width/length to bumper edge**: 17 inches

**Intake extension (when deployed):**
- Extends 12" outside frame perimeter
- Extends **9 inches beyond the bumpers**
- This is the critical measurement for collision avoidance

### 2.3 Orientation Matters

When the robot is rotated, its corners extend further toward the walls than when aligned with the field axes.

```
Aligned (0°):              Rotated (45°):
  ┌───┐                        ◇
  │   │                       ╱ ╲
  │ R │                      ╱ R ╲
  │   │                      ╲   ╱
  └───┘                       ╲ ╱
                               ◇
  Width: 34"                 Width: ~48" (diagonal)
```

At 45°, the robot's "reach" to each wall increases by √2 ≈ 1.41x. The system must calculate actual corner positions based on current heading.

### 2.4 Three-Zone Speed Model

The system uses three concentric zones around each wall:

| Zone | Distance from Wall | Behavior |
|------|-------------------|----------|
| **Safe Zone** | > 5 feet | Full speed allowed |
| **Warning Zone** | 20 inches - 5 feet | Gradual speed reduction |
| **Slow Zone** | 6 inches - 20 inches | Crawl speed only (0.5 m/s) |
| **Stop Zone** | < 6 inches | Block movement toward wall |

**Why these values?**
- **5 feet warning**: At max speed (4.58 m/s), this gives ~0.3 seconds to slow down
- **20 inches slow**: Enough distance to stop from crawl speed
- **6 inches stop**: Safety margin for pose estimation latency and driver reaction

### 2.5 Directional Limiting (Key Concept)

We don't just uniformly reduce speed - we only limit velocity components that move **toward** a nearby wall.

**Example**: Robot is 12 inches from the X=0 wall (blue side)
- Driving toward blue wall (-X direction): **LIMITED** to crawl speed
- Driving away from blue wall (+X direction): **FULL SPEED** allowed
- Driving parallel (Y direction): **FULL SPEED** allowed

This makes the system feel natural - you can always "escape" from a wall at full speed.

---

## 3. Architecture

### 3.1 New Package Structure

Create a new package: `frc.robot.subsystems.drive.boundary`

```
src/main/java/frc/robot/subsystems/drive/boundary/
├── FieldBoundaryConstants.java    # Fixed values (field size, zones)
├── FieldBoundaryPreferences.java  # Tunable parameters (NetworkTables)
├── RobotGeometry.java             # Corner/distance calculations
└── FieldBoundaryLimiter.java      # Main logic class
```

### 3.2 Data Flow

```
┌─────────────────┐     ┌──────────────────┐     ┌────────────────────┐
│  DriveState     │────▶│ FieldBoundary    │────▶│  RobotContainer    │
│  (Robot Pose)   │     │ Limiter          │     │  (Apply to drive)  │
└─────────────────┘     └──────────────────┘     └────────────────────┘
                               │
                               ▼
                        ┌──────────────────┐
                        │  RobotGeometry   │
                        │  (Corner calc)   │
                        └──────────────────┘
```

---

## 4. Implementation Details

### 4.1 WPILib Units Overview

This implementation uses WPILib's type-safe units system to prevent unit conversion errors:

```java
import static edu.wpi.first.units.Units.*;
import edu.wpi.first.units.measure.Distance;
import edu.wpi.first.units.measure.LinearVelocity;

// Creating distances - specify in any unit, convert automatically
Distance stopZone = Inches.of(6);           // 6 inches
Distance slowZone = Inches.of(20);          // 20 inches
Distance warningZone = Feet.of(5);          // 5 feet

// Converting to meters for calculations
double stopZoneMeters = stopZone.in(Meters);  // 0.1524

// Velocities work the same way
LinearVelocity crawlSpeed = MetersPerSecond.of(0.5);
```

**Benefits:**
- Can't accidentally mix inches and meters
- Self-documenting code (units are explicit)
- Easy conversion between any units
- Compile-time type checking

---

### 4.2 FieldBoundaryConstants.java

This file contains fixed values that don't need tuning. All distances use WPILib's `Distance` type.

```java
package frc.robot.subsystems.drive.boundary;

import static edu.wpi.first.units.Units.*;

import edu.wpi.first.units.measure.Distance;
import edu.wpi.first.units.measure.LinearVelocity;

/**
 * Constants for field boundary speed limiting.
 *
 * Field coordinate system:
 * - Origin at blue alliance driver station corner
 * - X runs length of field (0 to 16.54m)
 * - Y runs width of field (0 to 8.21m)
 *
 * All distances use WPILib Units for type safety and easy conversion.
 */
public final class FieldBoundaryConstants {

    // ==================== FIELD DIMENSIONS ====================
    // 2025 FRC field size (same as recent years)
    public static final Distance FIELD_LENGTH = Meters.of(16.54);  // X dimension
    public static final Distance FIELD_WIDTH = Meters.of(8.21);    // Y dimension

    // ==================== ZONE DISTANCES ====================
    // Distance from wall where each zone begins

    /** Hard stop - block all movement toward this wall */
    public static final Distance STOP_ZONE_DISTANCE = Inches.of(6);

    /** Crawl speed only - very slow movement allowed */
    public static final Distance SLOW_ZONE_DISTANCE = Inches.of(20);

    /** Begin speed reduction - gradual slowdown starts here */
    public static final Distance WARNING_ZONE_DISTANCE = Feet.of(5);

    // ==================== SPEED LIMITS ====================

    /** Maximum speed allowed in slow zone */
    public static final LinearVelocity SLOW_ZONE_MAX_SPEED = MetersPerSecond.of(0.5);

    // ==================== ROBOT GEOMETRY ====================
    // Based on TunerConstants module positions (±10.8645") plus bumpers (~3.5")

    /** Distance from robot center to bumper edge (front/back) */
    public static final Distance ROBOT_HALF_LENGTH = Inches.of(17);

    /** Distance from robot center to bumper edge (left/right) */
    public static final Distance ROBOT_HALF_WIDTH = Inches.of(17);

    // ==================== MECHANISM EXTENSIONS ====================
    // Additional reach beyond bumpers when mechanisms are deployed

    /**
     * Intake extension beyond front bumper when deployed.
     * Measured: 12" outside frame = 9" beyond bumpers
     */
    public static final Distance INTAKE_EXTENSION = Inches.of(9);

    // Private constructor - this is a constants class
    private FieldBoundaryConstants() {}
}
```

**Key learning points:**
- Use `Inches.of()`, `Feet.of()`, `Meters.of()` for readability
- Store as `Distance` type, convert with `.in(Meters)` when needed
- Document WHY each value was chosen (not just what it is)
- Make the class `final` with private constructor (Java pattern for constants)

---

### 4.3 FieldBoundaryPreferences.java

Tunable parameters that can be adjusted via SmartDashboard/Shuffleboard without redeploying code.

**Note:** NetworkTables only supports primitive doubles, so preferences store meters internally but the constants provide the default values with proper units.

```java
package frc.robot.subsystems.drive.boundary;

import static edu.wpi.first.units.Units.*;

import frc.robot.preferences.BooleanPreference;
import frc.robot.preferences.DoublePreference;

/**
 * Tunable parameters for field boundary limiting.
 * These appear in SmartDashboard under "Drive/Boundary/..."
 *
 * Note: NetworkTables stores doubles, so we convert units to meters
 * when creating preferences. The FieldBoundaryConstants provide
 * type-safe defaults.
 *
 * Follows the pattern established in DrivePreferences.java
 */
public final class FieldBoundaryPreferences {

    // ==================== MASTER ENABLE ====================

    /** Global enable/disable for the boundary limiter */
    public static final BooleanPreference enabled =
        new BooleanPreference("Drive/Boundary/Enabled", true);

    // ==================== ZONE DISTANCES (tunable, stored in meters) ====================

    public static final DoublePreference stopZoneDistance =
        new DoublePreference("Drive/Boundary/StopZone_m",
            FieldBoundaryConstants.STOP_ZONE_DISTANCE.in(Meters));

    public static final DoublePreference slowZoneDistance =
        new DoublePreference("Drive/Boundary/SlowZone_m",
            FieldBoundaryConstants.SLOW_ZONE_DISTANCE.in(Meters));

    public static final DoublePreference warningZoneDistance =
        new DoublePreference("Drive/Boundary/WarningZone_m",
            FieldBoundaryConstants.WARNING_ZONE_DISTANCE.in(Meters));

    // ==================== SPEED LIMITS (tunable, stored in m/s) ====================

    public static final DoublePreference slowZoneMaxSpeed =
        new DoublePreference("Drive/Boundary/SlowZoneSpeed_mps",
            FieldBoundaryConstants.SLOW_ZONE_MAX_SPEED.in(MetersPerSecond));

    // ==================== DYNAMIC EXTENSIONS (stored in meters) ====================
    // These can be updated at runtime when mechanisms deploy/retract

    public static final DoublePreference extensionFront =
        new DoublePreference("Drive/Boundary/ExtensionFront_m", 0.0);

    public static final DoublePreference extensionBack =
        new DoublePreference("Drive/Boundary/ExtensionBack_m", 0.0);

    public static final DoublePreference extensionLeft =
        new DoublePreference("Drive/Boundary/ExtensionLeft_m", 0.0);

    public static final DoublePreference extensionRight =
        new DoublePreference("Drive/Boundary/ExtensionRight_m", 0.0);

    private FieldBoundaryPreferences() {}
}
```

**Why use preferences instead of constants?**
- Tune values during practice without redeploying
- Quickly disable if something goes wrong at competition
- Adjust extensions as mechanisms are modified

**Note:** Requires adding `setValue(double value)` method to `DoublePreference.java`:
```java
public void setValue(double value) {
    Preferences.setDouble(super.key, value);
}
```

---

### 4.4 RobotGeometry.java

This class handles the math for orientation-aware distance calculations. Uses `Distance` type for inputs and returns `BoundaryDistances` with `Distance` fields.

```java
package frc.robot.subsystems.drive.boundary;

import static edu.wpi.first.units.Units.*;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.units.measure.Distance;

/**
 * Calculates robot bounding box and distances to field walls.
 *
 * The robot is modeled as a rectangle that rotates with the robot's heading.
 * We find the four corners in field coordinates, then calculate the minimum
 * distance from any corner to each wall.
 *
 * All distance inputs and outputs use WPILib Distance type for type safety.
 */
public class RobotGeometry {

    /**
     * Container for distances to each field boundary.
     * All distances use WPILib Distance type.
     */
    public static class BoundaryDistances {
        /** Distance to X=0 wall (blue alliance side) */
        public final Distance toMinX;

        /** Distance to X=max wall (red alliance side) */
        public final Distance toMaxX;

        /** Distance to Y=0 wall */
        public final Distance toMinY;

        /** Distance to Y=max wall */
        public final Distance toMaxY;

        /** The smallest distance to any wall */
        public final Distance minimum;

        public BoundaryDistances(Distance toMinX, Distance toMaxX,
                                  Distance toMinY, Distance toMaxY) {
            this.toMinX = toMinX;
            this.toMaxX = toMaxX;
            this.toMinY = toMinY;
            this.toMaxY = toMaxY;

            // Find minimum of all four distances
            double minMeters = Math.min(
                Math.min(toMinX.in(Meters), toMaxX.in(Meters)),
                Math.min(toMinY.in(Meters), toMaxY.in(Meters))
            );
            this.minimum = Meters.of(minMeters);
        }
    }

    /**
     * Calculate the four corners of the robot in field coordinates.
     *
     * The corners are calculated by:
     * 1. Starting with corner offsets in robot frame (based on dimensions + extensions)
     * 2. Rotating by robot heading
     * 3. Translating to robot position
     *
     * @param robotPose Current robot pose (position + heading)
     * @param halfLength Distance from center to front/back edge
     * @param halfWidth Distance from center to left/right edge
     * @param extFront Additional extension beyond front edge
     * @param extBack Additional extension beyond back edge
     * @param extLeft Additional extension beyond left edge
     * @param extRight Additional extension beyond right edge
     * @return Array of 4 Translation2d representing corner positions in field frame
     */
    public static Translation2d[] getRobotCorners(
            Pose2d robotPose,
            Distance halfLength,
            Distance halfWidth,
            Distance extFront,
            Distance extBack,
            Distance extLeft,
            Distance extRight) {

        Rotation2d heading = robotPose.getRotation();
        Translation2d center = robotPose.getTranslation();

        // Convert to meters for Translation2d (which uses meters internally)
        double reachFront = halfLength.in(Meters) + extFront.in(Meters);   // +X in robot frame
        double reachBack = halfLength.in(Meters) + extBack.in(Meters);     // -X in robot frame
        double reachLeft = halfWidth.in(Meters) + extLeft.in(Meters);      // +Y in robot frame
        double reachRight = halfWidth.in(Meters) + extRight.in(Meters);    // -Y in robot frame

        // Define corners in robot-relative coordinates
        // Robot frame: +X is forward, +Y is left
        Translation2d[] robotFrameCorners = {
            new Translation2d(reachFront, reachLeft),    // Front-left
            new Translation2d(reachFront, -reachRight),  // Front-right
            new Translation2d(-reachBack, reachLeft),    // Back-left
            new Translation2d(-reachBack, -reachRight)   // Back-right
        };

        // Transform each corner to field coordinates
        Translation2d[] fieldCorners = new Translation2d[4];
        for (int i = 0; i < 4; i++) {
            // Rotate by heading, then translate to robot position
            fieldCorners[i] = robotFrameCorners[i]
                .rotateBy(heading)
                .plus(center);
        }

        return fieldCorners;
    }

    /**
     * Calculate distances from robot bounding box to each field wall.
     *
     * Process:
     * 1. Get all four corners in field coordinates
     * 2. Find the axis-aligned bounding box (min/max X and Y)
     * 3. Calculate distance from bounding box edges to field walls
     *
     * @param robotPose Current robot pose
     * @param halfLength Distance from center to front/back bumper
     * @param halfWidth Distance from center to side bumpers
     * @param extFront Front extension (e.g., deployed intake)
     * @param extBack Back extension
     * @param extLeft Left extension
     * @param extRight Right extension
     * @return BoundaryDistances containing distance to each wall
     */
    public static BoundaryDistances calculateBoundaryDistances(
            Pose2d robotPose,
            Distance halfLength,
            Distance halfWidth,
            Distance extFront,
            Distance extBack,
            Distance extLeft,
            Distance extRight) {

        // Get corner positions in field frame
        Translation2d[] corners = getRobotCorners(
            robotPose, halfLength, halfWidth,
            extFront, extBack, extLeft, extRight);

        // Find axis-aligned bounding box
        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;

        for (Translation2d corner : corners) {
            minX = Math.min(minX, corner.getX());
            maxX = Math.max(maxX, corner.getX());
            minY = Math.min(minY, corner.getY());
            maxY = Math.max(maxY, corner.getY());
        }

        // Calculate distances to each wall (convert back to Distance type)
        // Note: If robot is outside field, distance will be negative
        Distance toMinXWall = Meters.of(minX);
        Distance toMaxXWall = Meters.of(
            FieldBoundaryConstants.FIELD_LENGTH.in(Meters) - maxX);
        Distance toMinYWall = Meters.of(minY);
        Distance toMaxYWall = Meters.of(
            FieldBoundaryConstants.FIELD_WIDTH.in(Meters) - maxY);

        return new BoundaryDistances(toMinXWall, toMaxXWall, toMinYWall, toMaxYWall);
    }
}
```

**Algorithm explanation:**

The key insight is that we're finding the "worst case" corner for each wall:
- For the X=0 wall, we care about the corner with the smallest X coordinate
- For the X=max wall, we care about the corner with the largest X coordinate
- Same logic for Y walls

By finding the bounding box (min/max of all corners), we get all four "worst case" values at once.

---

### 4.5 FieldBoundaryLimiter.java

The main class that ties everything together. Uses `Distance` and `LinearVelocity` types for clarity.

```java
package frc.robot.subsystems.drive.boundary;

import static edu.wpi.first.units.Units.*;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.units.measure.Distance;
import edu.wpi.first.units.measure.LinearVelocity;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.subsystems.drive.DriveConstants;

/**
 * Limits robot velocity based on proximity to field boundaries.
 *
 * This class implements directional speed limiting - it only restricts
 * velocity components that move the robot TOWARD a nearby wall.
 * Movement parallel to or away from walls is unrestricted.
 *
 * Usage in RobotContainer:
 * <pre>
 * private final FieldBoundaryLimiter boundaryLimiter = new FieldBoundaryLimiter();
 *
 * // In default drive command:
 * Translation2d limited = boundaryLimiter.applyDirectionalLimit(vx, vy, pose);
 * </pre>
 */
public class FieldBoundaryLimiter {

    // Manual override (in addition to NetworkTables preference)
    private boolean manualEnabled = true;

    // Cached for telemetry
    private Distance lastMinDistance = Meters.of(Double.MAX_VALUE);
    private double lastSpeedMultiplier = 1.0;
    private RobotGeometry.BoundaryDistances lastDistances = null;

    /**
     * Check if the limiter is currently active.
     * Disabled during autonomous (PathPlanner handles its own limits).
     */
    public boolean isEnabled() {
        return manualEnabled
            && FieldBoundaryPreferences.enabled.getValue()
            && DriverStation.isTeleop();
    }

    /**
     * Toggle the limiter on/off (for driver button binding).
     */
    public void setEnabled(boolean enabled) {
        this.manualEnabled = enabled;
    }

    /**
     * Toggle enabled state and return new state.
     */
    public boolean toggleEnabled() {
        this.manualEnabled = !this.manualEnabled;
        return this.manualEnabled;
    }

    /**
     * Update the front extension (call when intake deploys/retracts).
     *
     * @param deployed true if intake is deployed
     */
    public void setIntakeDeployed(boolean deployed) {
        Distance extension = deployed ? FieldBoundaryConstants.INTAKE_EXTENSION : Meters.of(0);
        FieldBoundaryPreferences.extensionFront.setValue(extension.in(Meters));
    }

    /**
     * Apply directional speed limiting to velocity commands.
     *
     * This is the main method to call from RobotContainer.
     * It limits velocity components that move toward nearby walls,
     * while allowing full speed parallel to or away from walls.
     *
     * @param requestedVx Requested X velocity (field-relative, m/s)
     * @param requestedVy Requested Y velocity (field-relative, m/s)
     * @param robotPose Current robot pose (null if unavailable)
     * @return Limited velocity as Translation2d
     */
    public Translation2d applyDirectionalLimit(
            double requestedVx,
            double requestedVy,
            Pose2d robotPose) {

        // If disabled or no pose, return original values
        if (!isEnabled() || robotPose == null) {
            lastSpeedMultiplier = 1.0;
            return new Translation2d(requestedVx, requestedVy);
        }

        // Get extensions from preferences (stored in meters)
        Distance extFront = Meters.of(FieldBoundaryPreferences.extensionFront.getValue());
        Distance extBack = Meters.of(FieldBoundaryPreferences.extensionBack.getValue());
        Distance extLeft = Meters.of(FieldBoundaryPreferences.extensionLeft.getValue());
        Distance extRight = Meters.of(FieldBoundaryPreferences.extensionRight.getValue());

        // Calculate distances to all walls
        RobotGeometry.BoundaryDistances distances = RobotGeometry.calculateBoundaryDistances(
            robotPose,
            FieldBoundaryConstants.ROBOT_HALF_LENGTH,
            FieldBoundaryConstants.ROBOT_HALF_WIDTH,
            extFront,
            extBack,
            extLeft,
            extRight
        );

        lastDistances = distances;
        lastMinDistance = distances.minimum;

        // Apply directional limits
        double limitedVx = requestedVx;
        double limitedVy = requestedVy;

        // Limit +X velocity based on distance to red wall (max X)
        if (requestedVx > 0) {
            limitedVx = requestedVx * calculateMultiplier(distances.toMaxX);
        }
        // Limit -X velocity based on distance to blue wall (min X)
        else if (requestedVx < 0) {
            limitedVx = requestedVx * calculateMultiplier(distances.toMinX);
        }

        // Limit +Y velocity based on distance to max Y wall
        if (requestedVy > 0) {
            limitedVy = requestedVy * calculateMultiplier(distances.toMaxY);
        }
        // Limit -Y velocity based on distance to min Y wall
        else if (requestedVy < 0) {
            limitedVy = requestedVy * calculateMultiplier(distances.toMinY);
        }

        // Track overall multiplier for telemetry
        double overallMultiplier = calculateMultiplier(distances.minimum);
        lastSpeedMultiplier = overallMultiplier;

        // Publish telemetry
        publishTelemetry();

        return new Translation2d(limitedVx, limitedVy);
    }

    /**
     * Calculate speed multiplier (0.0 to 1.0) based on distance to wall.
     *
     * The curve is:
     * - Stop zone (< 6"): returns 0.0
     * - Slow zone (6" - 20"): linear ramp from 0 to slowZoneMaxSpeed/maxSpeed
     * - Warning zone (20" - 5ft): linear ramp to 1.0
     * - Safe zone (> 5ft): returns 1.0
     */
    private double calculateMultiplier(Distance distanceToWall) {
        // Get zone thresholds from preferences (stored in meters)
        Distance stopZone = Meters.of(FieldBoundaryPreferences.stopZoneDistance.getValue());
        Distance slowZone = Meters.of(FieldBoundaryPreferences.slowZoneDistance.getValue());
        Distance warningZone = Meters.of(FieldBoundaryPreferences.warningZoneDistance.getValue());
        LinearVelocity slowSpeed = MetersPerSecond.of(
            FieldBoundaryPreferences.slowZoneMaxSpeed.getValue());
        LinearVelocity maxSpeed = MetersPerSecond.of(DriveConstants.MAX_DRIVE_SPEED);

        // Convert to meters for comparison
        double distMeters = distanceToWall.in(Meters);
        double stopMeters = stopZone.in(Meters);
        double slowMeters = slowZone.in(Meters);
        double warningMeters = warningZone.in(Meters);

        // Handle edge case: robot outside field (negative distance)
        if (distMeters <= 0) {
            return 0.0;
        }

        if (distMeters <= stopMeters) {
            // STOP ZONE: No movement toward wall
            return 0.0;
        }
        else if (distMeters <= slowMeters) {
            // SLOW ZONE: Ramp from 0 at stop edge to slowSpeed at slow edge
            double t = (distMeters - stopMeters) / (slowMeters - stopMeters);
            return t * (slowSpeed.in(MetersPerSecond) / maxSpeed.in(MetersPerSecond));
        }
        else if (distMeters <= warningMeters) {
            // WARNING ZONE: Ramp from slowSpeed to full speed
            double t = (distMeters - slowMeters) / (warningMeters - slowMeters);
            double minMultiplier = slowSpeed.in(MetersPerSecond) / maxSpeed.in(MetersPerSecond);
            return MathUtil.interpolate(minMultiplier, 1.0, t);
        }
        else {
            // SAFE ZONE: Full speed allowed
            return 1.0;
        }
    }

    /**
     * Publish diagnostic information to SmartDashboard.
     */
    private void publishTelemetry() {
        SmartDashboard.putBoolean("Boundary/Enabled", isEnabled());
        SmartDashboard.putNumber("Boundary/MinDistance_in", lastMinDistance.in(Inches));
        SmartDashboard.putNumber("Boundary/SpeedMultiplier", lastSpeedMultiplier);

        if (lastDistances != null) {
            SmartDashboard.putNumber("Boundary/DistToBlue_in", lastDistances.toMinX.in(Inches));
            SmartDashboard.putNumber("Boundary/DistToRed_in", lastDistances.toMaxX.in(Inches));
            SmartDashboard.putNumber("Boundary/DistToMinY_in", lastDistances.toMinY.in(Inches));
            SmartDashboard.putNumber("Boundary/DistToMaxY_in", lastDistances.toMaxY.in(Inches));
        }
    }

    // Getters for testing/debugging
    public Distance getLastMinDistance() { return lastMinDistance; }
    public double getLastSpeedMultiplier() { return lastSpeedMultiplier; }
}
```

---

## 5. Integration into RobotContainer

### 5.1 Add Field and Imports

At the top of `RobotContainer.java`:

```java
// Add these imports
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation2d;
import frc.robot.statemachines.DriveState;
import frc.robot.subsystems.drive.boundary.FieldBoundaryLimiter;
import frc.robot.subsystems.drive.boundary.FieldBoundaryConstants;

// Add this field (around line 38, with other subsystems)
private final FieldBoundaryLimiter boundaryLimiter = new FieldBoundaryLimiter();
```

### 5.2 Modify Default Drive Command

Replace the existing `configureSubsystemDefaultCommands()` method (lines 65-91):

```java
private void configureSubsystemDefaultCommands() {
    drivetrain.setDefaultCommand(
        drivetrain.applyRequest(() -> {
            // ===== STEP 1: Get raw joystick inputs with squared response =====
            double rawVx = -1
                * Math.copySign(Math.pow(driverJoystick.getLeftY(), 2), driverJoystick.getLeftY())
                * DriveConstants.MAX_DRIVE_SPEED;

            double rawVy = -1
                * Math.copySign(Math.pow(driverJoystick.getLeftX(), 2), driverJoystick.getLeftX())
                * DriveConstants.MAX_DRIVE_SPEED;

            double rawOmega = -1
                * Math.copySign(Math.pow(driverJoystick.getRightX(), 2), driverJoystick.getRightX())
                * DriveConstants.MAX_ANGULAR_SPEED;

            // ===== STEP 2: Get current robot pose =====
            Pose2d currentPose = null;
            if (DriveState.getInstance().hasDriveStats()) {
                currentPose = DriveState.getInstance().getCurrentDriveStats().Pose;
            }

            // ===== STEP 3: Apply boundary limiting =====
            Translation2d limitedVelocity = boundaryLimiter.applyDirectionalLimit(
                rawVx, rawVy, currentPose);

            // ===== STEP 4: Build and return the drive request =====
            return DriveConstants.DEFAULT_DRIVE_REQUEST
                .withVelocityX(limitedVelocity.getX())
                .withVelocityY(limitedVelocity.getY())
                .withRotationalRate(rawOmega)
                .withDeadband(DriveConstants.MAX_DRIVE_SPEED * 0.1)
                .withRotationalDeadband(DriveConstants.MAX_ANGULAR_SPEED * 0.1);
        }));
}
```

### 5.3 Add Driver Toggle Button

In `configureBindings()`, add a button to toggle the limiter:

```java
// Toggle boundary limiter with A button
// SmartDashboard shows current state
driverJoystick.a().onTrue(
    Commands.runOnce(() -> {
        boolean newState = boundaryLimiter.toggleEnabled();
        System.out.println("Boundary limiter: " + (newState ? "ENABLED" : "DISABLED"));
    })
);
```

### 5.4 Connect Intake State (Optional Enhancement)

To automatically track intake deployment, you have two options:

**Option A: Poll intake state in the limiter**

In `applyDirectionalLimit()`, before calculating distances:
```java
// Update front extension based on intake state
setIntakeDeployed(intake.isExtended());  // Need reference to IntakeSubsystem
```

**Option B: Update from IntakeSubsystem**

In `IntakeSubsystem`, when intake state changes:
```java
// Notify boundary limiter of state change
RobotContainer.getInstance().getBoundaryLimiter().setIntakeDeployed(isExtended);
```

---

## 6. Testing Plan

### 6.1 Unit Tests

Create `src/test/java/frc/robot/subsystems/drive/boundary/RobotGeometryTest.java`:

```java
import static edu.wpi.first.units.Units.*;

@Test
void testCornersAtZeroDegrees() {
    Pose2d pose = new Pose2d(8.0, 4.0, Rotation2d.fromDegrees(0));
    Distance halfSize = Inches.of(17);  // 17" = 0.4318m
    Distance zero = Meters.of(0);

    Translation2d[] corners = RobotGeometry.getRobotCorners(
        pose, halfSize, halfSize, zero, zero, zero, zero);

    // Front-left should be at (8.0 + 0.432, 4.0 + 0.432) = (8.432, 4.432)
    assertEquals(8.432, corners[0].getX(), 0.001);
    assertEquals(4.432, corners[0].getY(), 0.001);
}

@Test
void testCornersAt90Degrees() {
    Pose2d pose = new Pose2d(8.0, 4.0, Rotation2d.fromDegrees(90));
    Distance halfSize = Inches.of(17);  // 17" = 0.4318m
    Distance zero = Meters.of(0);

    Translation2d[] corners = RobotGeometry.getRobotCorners(
        pose, halfSize, halfSize, zero, zero, zero, zero);

    // At 90°, front-left corner rotates: robot +X becomes field +Y, robot +Y becomes field -X
    // Front-left (robot frame +X,+Y) becomes field (-0.432, +0.432) relative to center
    assertEquals(7.568, corners[0].getX(), 0.001);
    assertEquals(4.432, corners[0].getY(), 0.001);
}

@Test
void testDistancesNearWall() {
    // Robot centered at (0.5m, 4.0m), should be close to X=0 wall
    Pose2d pose = new Pose2d(0.5, 4.0, Rotation2d.fromDegrees(0));
    Distance halfSize = Inches.of(17);
    Distance zero = Meters.of(0);

    RobotGeometry.BoundaryDistances distances = RobotGeometry.calculateBoundaryDistances(
        pose, halfSize, halfSize, zero, zero, zero, zero);

    // Distance to X=0 wall = 0.5m - 17" = 0.5 - 0.4318 = 0.068m = 2.7 inches
    assertEquals(2.7, distances.toMinX.in(Inches), 0.1);
}
```

### 6.2 Simulation Testing

1. Deploy to simulation
2. Use SmartDashboard to monitor `Boundary/*` values
3. Drive toward each wall and verify:
   - SpeedMultiplier decreases as you approach
   - Robot stops before hitting wall
4. Test at different orientations (45°, 90°, etc.)

### 6.3 On-Robot Testing

1. **Start conservative**: Increase zone distances (7ft warning, 30" slow)
2. **Test slowly**: Drive toward walls at 25% speed first
3. **Watch telemetry**: Verify distance calculations are accurate
4. **Tune zones**: Adjust based on driver feedback
5. **Test intake**: Deploy intake and verify front extension adds protection

### 6.4 Competition Checklist

- [ ] Verify toggle button works to disable limiter
- [ ] Confirm system is disabled during autonomous
- [ ] Test with intake deployed and retracted
- [ ] Check SmartDashboard shows reasonable values
- [ ] Have backup plan: `FieldBoundaryPreferences.enabled.setValue(false)`

---

## 7. Troubleshooting

| Symptom | Likely Cause | Solution |
|---------|--------------|----------|
| Robot doesn't slow down | Limiter disabled or pose unavailable | Check `isEnabled()` and `hasDriveStats()` |
| Robot stops too far from wall | Zone distances too large | Reduce `warningZoneDistance` |
| Robot still hits wall | Pose estimation error or latency | Increase `stopZoneDistance` |
| Jerky speed changes | Zone transitions too sharp | Adjust zone overlap or add smoothing |
| Limits active in autonomous | `isTeleop()` check failing | Verify DriverStation mode detection |

---

## 8. Future Enhancements

1. **Rotation limiting**: Prevent fast spins near walls that could swing extended mechanisms into walls
2. **Pose confidence**: Reduce effectiveness when vision pose confidence is low
3. **Smart deadzone**: Instead of hard stop, allow "nudging" at very low speed
4. **Obstacle avoidance**: Use CANrange proximity sensor for dynamic obstacles
5. **Visual feedback**: Change LEDs or driver station indicator when in slow/stop zones

---

## Summary

This system provides wall collision protection through:
- **Pose-aware** calculations using odometry and vision
- **Orientation-aware** geometry accounting for robot rotation
- **Mechanism-aware** extension tracking (intake)
- **Directional** limiting that only restricts movement toward walls
- **Tunable** parameters for easy adjustment without redeploying
- **Type-safe** units using WPILib's Distance and LinearVelocity classes

The implementation requires creating 4 new files and modifying `RobotContainer.java`. All zone distances and speed limits are tunable via SmartDashboard.
