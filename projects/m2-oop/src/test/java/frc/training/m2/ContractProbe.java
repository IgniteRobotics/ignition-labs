package frc.training.m2;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.function.DoubleSupplier;
import java.util.stream.Collectors;

/**
 * Finds the class you were asked to write, and says something useful when it cannot.
 *
 * <p>Part of the milestone — do not edit.
 *
 * <p>Why this exists: the contract test cannot mention {@code SensorProfile} by name in code,
 * because on a fresh clone that class does not exist yet and the test source would not compile.
 * Looking it up by name at run time is what lets this project build green before you have written
 * anything. It also means the names in {@code SensorProfileSpec} are the real contract: spell one
 * differently and the message below will tell you exactly what it looked for.
 */
final class ContractProbe {

  private static final String CLASS_NAME = "frc.training.m2.SensorProfile";

  private ContractProbe() {}

  /** Returns the student's class, or fails with instructions. */
  static Class<?> profileClass() {
    try {
      return Class.forName(CLASS_NAME);
    } catch (ClassNotFoundException e) {
      throw new AssertionError(
          "There is no class called "
              + CLASS_NAME
              + " yet. Read SensorProfileSpec.java — it describes exactly what to build — then"
              + " create SensorProfile.java beside it in src/main/java/frc/training/m2/.",
          e);
    }
  }

  /** Builds one, via the two-argument constructor the spec describes. */
  static Object newProfile(int ticksPerRotation, double gearRatio) {
    Class<?> type = profileClass();
    Constructor<?> constructor;
    try {
      constructor = type.getConstructor(int.class, double.class);
    } catch (NoSuchMethodException e) {
      throw new AssertionError(
          "SensorProfile needs a public constructor taking (int ticksPerRotation, double"
              + " gearRatio), in that order. Public constructors found: "
              + describeConstructors(type),
          e);
    }
    try {
      return constructor.newInstance(ticksPerRotation, gearRatio);
    } catch (InvocationTargetException e) {
      throw rethrow("the SensorProfile constructor", e);
    } catch (ReflectiveOperationException e) {
      throw new AssertionError("Could not build a SensorProfile: " + e, e);
    }
  }

  static double ticksToRotations(Object profile, int ticks) {
    return (Double) call(profile, "ticksToRotations", int.class, ticks, double.class);
  }

  static int rotationsToTicks(Object profile, double mechanismRotations) {
    return (Integer) call(profile, "rotationsToTicks", double.class, mechanismRotations, int.class);
  }

  static double ticksPer100msToRpm(Object profile, int ticksPer100ms) {
    return (Double) call(profile, "ticksPer100msToRpm", int.class, ticksPer100ms, double.class);
  }

  static double rpmToTicksPer100ms(Object profile, double mechanismRpm) {
    return (Double) call(profile, "rpmToTicksPer100ms", double.class, mechanismRpm, double.class);
  }

  static DoubleSupplier rotationsFrom(Object profile, DoubleSupplier rawTicks) {
    return (DoubleSupplier)
        call(profile, "rotationsFrom", DoubleSupplier.class, rawTicks, DoubleSupplier.class);
  }

  /** Fails if SensorProfile declares any field that is not private. */
  static void assertEveryFieldIsPrivate() {
    String offenders =
        Arrays.stream(profileClass().getDeclaredFields())
            .filter(field -> !field.isSynthetic())
            .filter(field -> !Modifier.isPrivate(field.getModifiers()))
            .map(Field::getName)
            .collect(Collectors.joining(", "));
    if (!offenders.isEmpty()) {
      throw new AssertionError(
          "Every field on SensorProfile must be private. These are not: "
              + offenders
              + ". Private fields mean there is exactly one place a value can change, which means"
              + " exactly one place to look when it is wrong.");
    }
  }

  private static Object call(
      Object profile, String name, Class<?> parameterType, Object argument, Class<?> returnType) {
    Method method;
    try {
      method = profile.getClass().getMethod(name, parameterType);
    } catch (NoSuchMethodException e) {
      throw new AssertionError(
          "SensorProfile needs a public method called "
              + name
              + " taking one "
              + parameterType.getSimpleName()
              + ". Public methods found: "
              + describeMethods(profile.getClass()),
          e);
    }
    if (!method.getReturnType().equals(returnType)) {
      throw new AssertionError(
          name
              + " must return "
              + returnType.getSimpleName()
              + ", but yours returns "
              + method.getReturnType().getSimpleName()
              + ". Re-read that entry in SensorProfileSpec.java.");
    }
    try {
      return method.invoke(profile, argument);
    } catch (InvocationTargetException e) {
      throw rethrow(name, e);
    } catch (IllegalAccessException e) {
      throw new AssertionError(name + " could not be called: " + e, e);
    }
  }

  /**
   * Surfaces whatever the student's own code threw, rather than burying it inside a reflection
   * wrapper. A student debugging their own ArithmeticException should see an ArithmeticException.
   */
  private static RuntimeException rethrow(String what, InvocationTargetException e) {
    Throwable cause = e.getCause();
    if (cause instanceof RuntimeException) {
      throw (RuntimeException) cause;
    }
    if (cause instanceof Error) {
      throw (Error) cause;
    }
    throw new AssertionError(what + " threw " + cause, cause);
  }

  private static String describeConstructors(Class<?> type) {
    String found =
        Arrays.stream(type.getConstructors())
            .map(c -> "(" + parameterList(c.getParameterTypes()) + ")")
            .collect(Collectors.joining(", "));
    return found.isEmpty() ? "none" : found;
  }

  private static String describeMethods(Class<?> type) {
    String found =
        Arrays.stream(type.getDeclaredMethods())
            .filter(m -> Modifier.isPublic(m.getModifiers()))
            .map(m -> m.getName() + "(" + parameterList(m.getParameterTypes()) + ")")
            .sorted()
            .collect(Collectors.joining(", "));
    return found.isEmpty() ? "none" : found;
  }

  private static String parameterList(Class<?>[] types) {
    return Arrays.stream(types).map(Class::getSimpleName).collect(Collectors.joining(", "));
  }
}
