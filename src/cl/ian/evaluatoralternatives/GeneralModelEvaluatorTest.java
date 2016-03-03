package cl.ian.evaluatoralternatives;

import cl.ian.Interpolation;
import cl.ian.ModelUtils;
import cl.ian.MyVector;
import org.ejml.data.DenseMatrix64F;
import org.ejml.data.FixedMatrix3_64F;

import java.util.Arrays;

import static java.lang.Math.*;


/**
 * Created by ian on 6/15/15.
 * This class computes the phenomenological model for the friction factor and uses an evolved individual to
 * evaluate it
 */
public class GeneralModelEvaluatorTest {

  // Cantidad de celdas
  static final int col_fluido = 2;
  static final int col_celda = 1;
  static final int n_fluido = 2;
  static final int n_celda = 1;

  static final double r = 32E-3;                          //Resistencia interna [Ohm]
  static final double largo = 65E-3;                      //Largo de celdas [m]
  static final double e = 15E-3;                          //Espaciado entre pared y celda [m]
  static final double z = 5E-3;                           //Corte del estudio [m]
  static final double errmax = 1E-3;                      //error corte
  static final double piQuarter = PI / 4;
  static final double doubleE = 2 * e;


  public double compute(double current, double separation, double flow, double initTemperature, double cellDiameter) {

    // This never happens, probably was used for testing and then became deprecated
    if (initTemperature == 0 && cellDiameter == 0) {
      initTemperature = 20;
      cellDiameter = 18;
    }

    final double atmPressure = 0;

    // Constantes del modelo
    final double[] result = Interpolation.q_paramdrag(separation);
    final FixedMatrix3_64F a = new FixedMatrix3_64F(result[0], result[1], 0.653);

    final double flux = flow * 0.00047;                                                                //flujo de entrada del fluido [CFM]->[m3/s]
    cellDiameter /= 1000;

    // Speedup cache variable
    final double cellArea = pow(cellDiameter, 2) * piQuarter;

    final double vol = largo * cellArea;                                                               //Volumen celda [m3]
    final double volumetricQ = pow(current, 2) * r / vol;                                              //Calor volumetrico
    final double q = volumetricQ * z * cellArea;                                                       //Calor total corte.

    // Speedup cache variables
    final double diamTimesZ = cellDiameter * z;
    final double superficialArea = PI * diamTimesZ;                                                    //Area de la celda

    final double height = doubleE + cellDiameter * (n_fluido + separation * n_celda);                  //Altura del pack
    final double entranceArea = height * z;                                                            //Area de entrada pack

    // Speedup cache variable
    final double sPlusOne = separation + 1;
    final double controlVolArea = sPlusOne * diamTimesZ;                                            //Area volumen control eje z

    /*********************************************** Initialization **********************************************/
    final MyVector tf = new MyVector(col_fluido, initTemperature);                                  //[°C]
    final MyVector pf = new MyVector(col_fluido, atmPressure);                                      //[Pa]

    // Speedup cache variable
    final double innerArg = flux * z / (largo * entranceArea);

    final MyVector vf = new MyVector(col_fluido, innerArg);                                        //[m/s]
    final DenseMatrix64F vmf = vf.copy();                                                              //[m/s]
    final MyVector df = new MyVector(col_fluido, 1.204);                                           //[kg/m3] Apparently 1.204 is never used
    final MyVector tc = new MyVector(col_celda, initTemperature);                                  //[°C]
    final DenseMatrix64F ff = new DenseMatrix64F(1, col_fluido);                                       //N
    final DenseMatrix64F rem = new DenseMatrix64F(1, col_fluido);                                      //Adimensional
    final DenseMatrix64F fluidK = new MyVector(col_fluido, Interpolation.q_conductividad(tf.get(0)));//[W/m k]
    /******************************************** Errores en columnas ********************************************/
    final double[] cellTempError = filledArray(col_celda, Double.MAX_VALUE);
    final double[] fluidTempError = filledArray(col_fluido, Double.MAX_VALUE);
    final double[] velocityError = filledArray(col_fluido, Double.MAX_VALUE);
    final double[] pressureError = filledArray(col_celda, Double.MAX_VALUE);
    /******************************************** Condiciones de borde ********************************************/
    // Tf(1) = Tin;                       Temperatura entrada [ºC]
    // Vinicio = a(2)*Flujo*(z/Largo)/A;  Velocidad entrada[m/s]
    // Df(1) = q_densidad(Tin);           Densidad de entrada [kg/m3]
    // Pf(length(Pf)) = P_atm;            Presion entrada [Pa]. Presión de ¿salida? se asume la presión atmosférica [Pa]
    final double initialVelocity = a.a2 * innerArg;           // Velocidad entrada[m/s]
    df.set(0, Interpolation.q_densidad(initTemperature));     // Densidad de entrada [kg/m3]
    /**************************************************************************************************************/
    // Speedup cache variable
    final double dfMultiplicationTerm = diamTimesZ * initialVelocity * df.get(0);    // df(0) never changes

    final double m_punto = sPlusOne * dfMultiplicationTerm;

    velocityError[0] = 0;
    fluidTempError[0] = 0;

    // Speedup cache variables
    final double sTerm = separation / sPlusOne;
    final double heatPerArea = q / superficialArea;
    final double initialFFTerm = 0.5 * dfMultiplicationTerm * initialVelocity;
    final double fluidTempTerm = q / m_punto;
    final double normalizedArea = superficialArea / controlVolArea;

    /*********************************************** Equations *******************************************************/
    for (int x = 0; x < 10; x++) {
      // This gets uptated with the first value from the last attempt to converge
      double cdrag = a.a1 * ModelUtils.q_cdr3(rem.get(0));
      double initialFF = initialFFTerm * cdrag;                                     // Store variable to speedup access
      ff.set(0, initialFF);
      vf.set(0, initialVelocity - initialFF / m_punto);

      // Tf(0), pf(0), Df(0) and rem aren't modified in the loop
      for (int i = 0; i < col_fluido - 1; i++) {
        /***************************************** Cached variables ********************************************
         * This variables are cached to allow faster accesses in the rest of the loop *************************/
        final double iniFluidVel = vf.get(i);
        final double iniFluidTemp = tf.get(i);
        final double iniFluidDensity = df.get(i);
        final double iniFluidMedVel = sTerm * iniFluidVel;
        final double iniRem = ModelUtils.q_reynolds(iniFluidMedVel, iniFluidTemp, cellDiameter, iniFluidDensity);
        final double iniFluidVelSquared = pow(iniFluidVel, 2);
        final double normalizedDensity = iniFluidDensity / 1.205;
        /******************************************************************************************************/
        vmf.set(i, iniFluidMedVel);
        rem.set(i, iniRem);
        /***************************************** Calculo de la presion **************************************/
        double ffrictionX = 5;
        // Caída de presion en intercambiadores de calor
        // Note:salía (+) antes pero la ecuación dice que es con menos.
        // Además es necesario usar los valores de la columna apropiada
        //int inverseIndex = col_fluido - 1 - i;
        final double endFluidPressure = pf.get(i + 1);
        final double iniFluidPressure = endFluidPressure - 0.5 * ffrictionX * iniFluidDensity * pow(iniFluidMedVel, 2);
        pf.setValue(i, pressureError, iniFluidPressure);
        /***************************************** Calculo de la velocidad ************************************/
        cdrag = a.a1 * ModelUtils.q_cdr3(rem.get(0));
        final double endFF = 0.5 * diamTimesZ * iniFluidDensity * iniFluidVelSquared * cdrag;
        ff.set(i + 1, endFF);
        // Conservacion de momento
        final double sqrtInnerTerm = (controlVolArea * (endFluidPressure - iniFluidPressure) - endFF) / m_punto
            + iniFluidVelSquared;
        final double endFluidVel = sqrt(sqrtInnerTerm);
        vf.setValue(i + 1, velocityError, endFluidVel);
        /********************************** Calculo de la temperatura fluido **********************************/
        final double cp = Interpolation.q_cp(iniFluidTemp);
        // Conservacion de energia
        final double endFluidTemp = iniFluidTemp + (fluidTempTerm - 0.5 * (sqrtInnerTerm - iniFluidVelSquared)) / cp;
        tf.setValue(i + 1, fluidTempError, endFluidTemp);
        /***************************************** Calculo de la densidad *************************************/
        df.set(i + 1, Interpolation.q_densidad(endFluidTemp));
        /********************************** Calculo de temperatura de celda ***********************************/
        // Shift operator multiplies by 2
        final double nu = Interpolation.q_cznusselt(i * 2, iniRem) * ModelUtils.q_nusselt2(iniRem, a.a3);
        final double iniFluidK = Interpolation.q_conductividad(iniFluidTemp);
        fluidK.set(i, iniFluidK);
        final double h = nu * iniFluidK / cellDiameter;
        // Transferencia de energia
        tc.setValue(i, cellTempError, heatPerArea / h + (iniFluidTemp + endFluidTemp) / 2);
      }
      if (max(cellTempError) <= errmax && max(fluidTempError) <= errmax &&
          max(pressureError) <= errmax && max(velocityError) <= errmax) {
        break;
      }
    }

    if (Double.isNaN(pf.get(0)) || Double.isInfinite(pf.get(0)))
      return 0;

    return pf.get(0);
  }

  public static void main(String... args) {
    GeneralModelEvaluatorTest test = new GeneralModelEvaluatorTest();
    test.compute(8.7127, 0.042458, 0.50714, 26.43, 26);
  }

  public static double[] filledArray(int size, double value) {
    double[] array = new double[size];
    Arrays.fill(array, value);
    return array;
  }

  public static double max(double... array) {
    double max = array[0];
    for (int i = 1; i < array.length; i++)
      max = Math.max(max, array[i]);
    return max;
  }
}