package frc.training.m2;

/**
 * The specification for the class you are going to write. This file is not that class — it is the
 * description of it, and it stays exactly as it is.
 *
 * <p>Create {@code SensorProfile.java} beside this file, in this same package, and give it the
 * public API described below. Nothing here is a method you can copy; deciding what each signature
 * has to look like is the exercise.
 *
 * <h2>What a sensor profile is</h2>
 *
 * <p>An encoder counts in ticks. A mechanism turns in rotations, and a gearbox sits between them.
 * Every subsystem on the robot needs the same four conversions and every one of them has different
 * numbers, so this is a class with state rather than a pile of static methods: you build one
 * profile per mechanism and ask it questions.
 *
 * <h2>Construction</h2>
 *
 * <p>A public constructor taking two things, in this order:
 *
 * <ul>
 *   <li>{@code ticksPerRotation}, an {@code int} — how many ticks the encoder reports for one full
 *       turn <em>of the motor shaft</em>. Greater than zero.
 *   <li>{@code gearRatio}, a {@code double} — how many turns of the motor make one turn of the
 *       mechanism. A ratio of 9.0 means the motor turns nine times per mechanism turn. Greater than
 *       zero.
 * </ul>
 *
 * <p>Both values are settings of one physical mechanism. They are decided when the profile is built
 * and they never change afterwards, and the class should say so in the way it declares them. Every
 * field must be private (M2's definition of done, and the contract test checks it).
 *
 * <h2>The conversions</h2>
 *
 * <p>Everywhere below, "rotations" and "RPM" mean the <em>mechanism</em>, not the motor. That is
 * what the rest of the robot cares about — nobody aims a shooter in motor turns.
 *
 * <ol>
 *   <li><b>{@code ticksToRotations}</b> — takes an {@code int} raw encoder reading, which may be
 *       negative, and returns a {@code double}: how far the mechanism has turned. The fractional
 *       part is part of the answer. A reading of one quarter of a motor turn is not zero.
 *   <li><b>{@code rotationsToTicks}</b> — the inverse. Takes a {@code double} count of mechanism
 *       rotations and returns an {@code int} encoder reading. An encoder cannot report a fraction
 *       of a tick, so the answer is the <em>nearest</em> whole tick, not the one below it.
 *   <li><b>{@code ticksPer100msToRpm}</b> — takes an {@code int}, a change in the encoder reading
 *       measured over one tenth of a second, and returns a {@code double}: the mechanism's speed in
 *       revolutions per minute. One tenth of a second is what CTRE's older velocity signals report
 *       over, which is why this conversion exists at all.
 *   <li><b>{@code rpmToTicksPer100ms}</b> — the inverse. Takes a {@code double} mechanism speed in
 *       RPM and returns a {@code double} tick delta per hundred milliseconds. This one returns a
 *       {@code double}, not an {@code int}: it is an answer about a rate, not a reading off a real
 *       encoder.
 * </ol>
 *
 * <h2>The one that matters</h2>
 *
 * <p><b>{@code rotationsFrom}</b> — takes a {@code java.util.function.DoubleSupplier} that reads
 * the raw tick count, and returns a {@code DoubleSupplier} that reports mechanism rotations.
 *
 * <p>Read that again. It takes a way of getting ticks and gives back a way of getting rotations. It
 * does not take a number and it does not return a number.
 *
 * <p>What it must do: every single time the returned supplier is asked for a value, it asks the
 * supplier it was given for a fresh tick count and converts that. Nothing is read when {@code
 * rotationsFrom} is called; everything is read when the result is used. Hand a command one of these
 * and it can ask fifty times a second and get fifty different answers.
 *
 * <p>This is the whole milestone in one method, and it is the shape {@code RobotPreference<T>} has
 * in the real codebase. See {@code READING.md}.
 */
public final class SensorProfileSpec {

  private SensorProfileSpec() {}
}
