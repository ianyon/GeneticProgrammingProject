package cl.ian;

import org.ejml.data.*;

import java.util.Arrays;

/**
 * Created by ian on 6/15/15.
 * Contains all the methods that need interpolation
 */
public class ArrayInterpolation {

  /* *********************** q_paramdrag variables *********************** */
  private static final double[] b1 = new double[]{0.039, 0.028, 0.027, 0.028, 0.005};
  private static final double[] b2 = new double[]{3.270, 2.416, 2.907, 2.974, 2.063};
  private static final double[] X = new double[]{0.1, 0.25, 0.5, 0.75, 1};

  private static final double[] slopeB1 = new double[4];
  private static final double[] interceptB1 = new double[4];

  private static final double[] slopeB2 = new double[4];
  private static final double[] interceptB2 = new double[4];

  static {
    initializeStaticArrays(X, b1, slopeB1, interceptB1);
    initializeStaticArrays(X, b2, slopeB2, interceptB2);
  }
      /* *********************** END *********************** */

  /* *********************** q_conductividad *********************** */
  private static final double[] Y_q_conductividad = new double[]{22.3e-3, 26.3e-3, 30e-3, 33.8e-3, 37.3e-3};
  private static final double[] X_q_conductividad = new double[]{250, 300, 350, 400, 450};

  private static final double[] slope_q_conductividad = new double[4];
  private static final double[] intercept_q_conductividad = new double[4];

  static {
    initializeStaticArrays(X_q_conductividad, Y_q_conductividad, slope_q_conductividad, intercept_q_conductividad);
  }
    /* *********************** END *********************** */

  /* *********************** q_densidad *********************** */
  private static final double[] Y_q_densidad = new double[]{1.293, 1.205, 1.127};
  private static final double[] X_q_densidad = new double[]{0, 20, 40};

  private static final double[] slope_q_densidad = new double[2];
  private static final double[] intercept_q_densidad = new double[2];

  static {
    initializeStaticArrays(X_q_densidad, Y_q_densidad, slope_q_densidad, intercept_q_densidad);
  }
    /* *********************** END *********************** */

  /* *********************** q_viscosidad *********************** */
  private static final double[] Y_q_viscosidad = new double[]{159.6e-7, 184.6e-7, 208.2e-7, 230.1e-7, 250.7E-7};
  private static final double[] X_q_viscosidad = new double[]{250, 300, 350, 400, 450};

  private static final double[] slope_q_viscosidad = new double[4];
  private static final double[] intercept_q_viscosidad = new double[4];

  static {
    initializeStaticArrays(X_q_viscosidad, Y_q_viscosidad, slope_q_viscosidad, intercept_q_viscosidad);
  }
    /* *********************** END *********************** */

  /* *********************** q_cp *********************** */
  private static final double[] Y_q_cp = new double[]{1.006e3, 1.007e3, 1.009e3, 1.014e3, 1.021e3};
  private static final double[] X_q_cp = new double[]{250, 300, 350, 400, 450};

  private static final double[] slope_q_cp = new double[4];
  private static final double[] intercept_q_cp = new double[4];

  static {
    initializeStaticArrays(X_q_cp, Y_q_cp, slope_q_cp, intercept_q_cp);
  }
    /* *********************** END *********************** */

  /* *********************** q_cznusselt *********************** */
  private static final double[] Y_q_cznusselt = new double[]{0.64, 0.76, 0.84, 0.89, 0.92, 0.95, 0.97, 0.98, 0.99, 1};
  private static final double[] X_q_cznusselt = new double[]{1, 2, 3, 4, 5, 7, 10, 13, 16, 20};

  private static final double[] slope_q_cznusselt = new double[9];
  private static final double[] intercept_q_cznusselt = new double[9];

  static {
    initializeStaticArrays(X_q_cznusselt, Y_q_cznusselt, slope_q_cznusselt, intercept_q_cznusselt);
  }
    /* *********************** END *********************** */

  /* *********************** phenomenologicalFrictionFactor *********************** */
  private static final double[] xFrictionFactor = new double[]{0.25, 0.5, 1, 1.5};
  private static final double[] y1FrictionFactor = new double[]{82.188, 38.446, 11.728, 1.2095};
  private static final double[] y2FrictionFactor = new double[]{-0.605, -0.54, -0.402, -0.211};

  private static final double[] slopeFrictionFactor1 = new double[3];
  private static final double[] interceptFrictionFactor1 = new double[3];
  private static final double[] slopeFrictionFactor2 = new double[3];
  private static final double[] interceptFrictionFactor2 = new double[3];

  static {
    initializeStaticArrays(xFrictionFactor, y1FrictionFactor, slopeFrictionFactor1, interceptFrictionFactor1);
    initializeStaticArrays(xFrictionFactor, y2FrictionFactor, slopeFrictionFactor2, interceptFrictionFactor2);
  }
    /* ********************************* END **************************************** */

  private static void initializeStaticArrays(double X[], double[] Y,
                                             double[] slope, double[] intercept) {
    double dx;
    double dy;

    // Calculate the line equation (i.e. slope and intercept) between each point
    for (int i = 0; i < X.length - 1; i++) {
      dx = X[i + 1] - X[i];

      assert dx > 0 : "ERROR: X must be strictly monotonic.";

      dy = Y[i + 1] - Y[i];
      slope[i] = dy / dx;
      intercept[i] = Y[i] - X[i] * slope[i];
    }
  }

  /**
   * Perform linear interpolation with extrapolation in lower values and máximum value for the higher ones.
   *
   * @param S Value to intepolate
   * @return Matrix with 2 interpolated values
   */
  public static double[] q_paramdrag(double S) {

    double[] result = new double[2];

    // Check special bounds
    if (S > X[4]) {                             // Use máx value
      result[0] = b1[4];
      result[1] = b2[4];
    } else {                                    // Normal interpolation
      result[0] = Math.max(0, interpolateLinearly(X, slopeB1, interceptB1, b1, S));
      result[1] = Math.max(0, interpolateLinearly(X, slopeB2, interceptB2, b2, S));
    }

    return result;
  }

  /**
   * Linear interpolation with extrapolation
   *
   * @param T Temperature
   * @return Interpolated value
   */
  public static double q_conductividad(double T) {
    T += 273.15;

    // Check bounds
    // This happens if the result is NaN or not real
    if (T < 0 || Double.isNaN(T)) return 0.0001;

    // General case
    return interpolateLinearly(X_q_conductividad, slope_q_conductividad, intercept_q_conductividad,
        Y_q_conductividad, T);
  }

  /**
   * Linear interpolation with extrapolation
   *
   * @param T Temperature
   * @return Interpolated value
   */
  public static double q_densidad(double T) {
    // This happens if the result is NaN or not real
    if (Double.isNaN(T)) return 1.0;

    // General case
    return interpolateLinearly(X_q_densidad, slope_q_densidad, intercept_q_densidad, Y_q_densidad, T);
  }

  /**
   * Linear interpolation with extrapolation
   *
   * @param T Temperature
   * @return Interpolated value
   */
  public static double q_viscosidad(double T) {
    // This happens if the result is NaN or not real
    if (Double.isNaN(T)) return 0.0;

    T += 273.15;

    // General case
    return interpolateLinearly(X_q_viscosidad, slope_q_viscosidad, intercept_q_viscosidad, Y_q_viscosidad, T);
  }

  /**
   * Linear interpolation with extrapolation for computing the Nusselt Number using the phenomenological model.
   * This is part of the expression. The complete expression is in the class FrictionFactorNumberEvaluator,
   * in the method evaluateNusseltNumber
   *
   * @param doubleColumn The double of the actual fluid column
   * @param Re           Reinolds number
   * @return Interpolated value
   */
  public static double q_cznusselt(int doubleColumn, double Re) {
    // This happens if the result is NaN or not real
    if (Re < 0 || Double.isNaN(Re)) return 0.0001;

    if (doubleColumn > 20 || Re < 1e3) return 1.0;

    // General case
    return interpolateLinearly(X_q_cznusselt, slope_q_cznusselt, intercept_q_cznusselt, Y_q_cznusselt, doubleColumn);
  }

  /**
   * Linear interpolation with extrapolation for computing the friction factor using the phenomenological model
   *
   * @param reynolds   The reynolds number
   * @param separation The separation parameter
   * @return The friction factor
   */
  public static double phenomenologicalFrictionFactor(double reynolds, double separation) {
    // This happens if the result is NaN or not real
    if (reynolds < 0 || Double.isNaN(reynolds)) return 0.0;

    double aux1 = interpolateLinearly(xFrictionFactor, slopeFrictionFactor1, interceptFrictionFactor1, y1FrictionFactor, separation);
    double aux2 = interpolateLinearly(xFrictionFactor, slopeFrictionFactor2, interceptFrictionFactor2, y2FrictionFactor, separation);
    return 0.7 * (aux1 * Math.pow(reynolds, aux2));
  }

  /**
   * Linear interpolation with extrapolation
   *
   * @param T Temperature
   * @return Interpolated value
   */
  public static double q_cp(double T) {
    // This happens if the result is NaN or not real
    if (Double.isNaN(T)) return 1.0;

    T += 273.15;

    // General case
    return interpolateLinearly(X_q_cp, slope_q_cp, intercept_q_cp, Y_q_cp, T);
  }

  /**
   * General interpolator with extrapolation
   *
   * @param x     Contains the sample points.
   * @param y     Contains the corresponding values y(x).
   * @param value Contains the coordinate of the query point.
   * @return The value of the query point interpolated
   */
  private static double interpolateLinearly(final double[] x, final double[] slope,
                                            final double[] intercept, final double[] y,
                                            final double value) {
    // Check bounds for extrapolation
    if (value < x[0])
      return slope[0] * value + intercept[0];                   // Below bottom bound
    else if (value > x[x.length - 1])                                                   // Over upper bound
      return slope[slope.length] * value + intercept[intercept.length];
    return interpolate(x, slope, intercept, y, value);
  }

  /**
   * General case interpolation
   *
   * @param x         Contains the sample points.
   * @param slope     The slope of the line between each point
   * @param intercept the intercept of the line in the y-axis, for each line between points
   * @param y         Contains the corresponding values y(x).
   * @param value     Contains the coordinate of the query point.
   * @return The value of the query point interpolated
   */
  private static double interpolate(double[] x, double[] slope, double[] intercept,
                                    double[] y, double value) {
    // loc is the position where value should be inserted
    int loc = Arrays.binarySearch(x, value);
    if (loc < -1) {                                                 // Value isn't in array
      loc = -loc - 2;
      return slope[loc] * value + intercept[loc];
    } else return y[loc];                                    // Value found in array

  }
}
