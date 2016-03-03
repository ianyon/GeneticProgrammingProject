package cl.ian.evaluatoralternatives;

import cl.ian.ArrayInterpolation;
import cl.ian.ModelUtils;
import cl.ian.gp.EvolutionStateBean;
import cl.ian.gp.PhenomenologicalModel;
import cl.ian.problemtype.ModelEvaluator;
import ec.gp.GPIndividual;
import org.ejml.data.FixedMatrix3_64F;

import java.util.Arrays;

import static java.lang.Math.*;


/**
 * Created by ian on 6/15/15.
 * This class computes the phenomenological model for the friction factor and uses an evolved individual to
 * evaluate it
 */
public class GeneralModelEvaluatorArrayVars {

  // Cantidad de celdas
  private static final int col_fluido = 2;
  private static final int col_celda = 1;
  private static final int n_fluido = 2;
  private static final int n_celda = 1;

  private static final double r = 32E-3;                          //Resistencia interna [Ohm]
  private static final double largo = 65E-3;                      //Largo de celdas [m]
  private static final double e = 15E-3;                          //Espaciado entre pared y celda [m]
  private static final double z = 5E-3;                           //Corte del estudio [m]
  private static final double errmax = 1E-3;                      //error corte
  private static final double piQuarter = PI / 4;
  private static final double doubleE = 2 * e;

  private final ModelEvaluator eval;

  public GeneralModelEvaluatorArrayVars(ModelEvaluator evaluator) {
    this.eval = evaluator;
  }

  public double compute(double current, double separation, double flow, double initTemperature, double cellDiameter,
                        GPIndividual individual, EvolutionStateBean stateBean, PhenomenologicalModel model) {

    // This never happens, probably was used for testing and then became deprecated
    /*if (initTemperature == 0 && cellDiameter == 0 && ((MyGPIndividual)individual).stringRootedTreeForHumans().equals("")) {
      initTemperature = 20;
      cellDiameter = 18;
    }*/

    eval.setStateIndividual(stateBean, individual, model);
    final double atmPressure = eval.atmosphericPressure();                                        //Presion atmosferica [Pa]

    // Constantes del modelo
    final double[] result = ArrayInterpolation.q_paramdrag(separation);
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
    final double[] tf = filledArray(col_fluido, initTemperature);                                  //[°C]
    final double[] pf = filledArray(col_fluido, atmPressure);                                      //[Pa]

    // Speedup cache variable
    final double innerArg = flux * z / (largo * entranceArea);

    final double[] vf = filledArray(col_fluido, innerArg);                                          //[m/s]
    final double[] vmf = filledArray(col_fluido, innerArg);                                         //[m/s]
    final double[] df = new double[col_fluido];                                                     //[kg/m3]
    final double[] tc = filledArray(col_celda, initTemperature);                                    //[°C]
    final double[] ff = new double[col_fluido];                                                     //N
    final double[] rem = new double[col_fluido];                                                    //Adimensional
    final double[] fluidK = filledArray(col_fluido, ArrayInterpolation.q_conductividad(tf[0]));     //[W/m k]
    /******************************************** Errores en columnas ********************************************/
    final double[] cellTempError = MAXFilledArray(col_celda);
    final double[] TFError = MAXFilledArray(col_fluido);
    final double[] velocityError = MAXFilledArray(col_fluido);
    final double[] pressureError = MAXFilledArray(col_celda);
    /******************************************** Condiciones de borde ********************************************/
    // Tf(1) = Tin;                       Temperatura entrada [ºC]
    // Vinicio = a(2)*Flujo*(z/Largo)/A;  Velocidad entrada[m/s]
    // Df(1) = q_densidad(Tin);           Densidad de entrada [kg/m3]
    // Pf(length(Pf)) = P_atm;            Presion entrada [Pa]. Presión de ¿salida? se asume la presión atmosférica [Pa]
    final double initVelocity = a.a2 * innerArg;                     // Velocidad entrada[m/s]
    df[0] = ArrayInterpolation.q_densidad(initTemperature);     // Densidad de entrada [kg/m]
    /**************************************************************************************************************/
    // Speedup cache variable
    final double dfMultiplicationTerm = diamTimesZ * initVelocity * df[0];    // df(0) never changes

    final double m_punto = sPlusOne * dfMultiplicationTerm;

    TFError[0] = 0.0;

    // Speedup cache variables
    final double sTerm = separation / sPlusOne;
    final double heatPerArea = q / superficialArea;
    final double initialFFTerm = 0.5 * dfMultiplicationTerm * initVelocity;
    final double fluidTempTerm = q / m_punto;
    final double normalizedArea = superficialArea / controlVolArea;

    /*********************************************** Equations *******************************************************/
    // We create copies of the values in the DenseMatrix variables to avoid memory accesses. Those variables are
    // prefixed with "actual" for the element at "i" position (or initial element) and with "next" for the position i+1.
    // There are other cache variables as well, and they represent intermediate computations.
    for (int x = 0; x < 10; x++) { // Try 10 times
      // This gets updated with the first value from the last attempt to converge
      /**************************************** Calculo de la velocidad en 1 ***********************************/
      double actualDF = df[0];
      double normalizedDF = actualDF / 1.205;
      double cdrag = eval.computeDragCoefficient(a.a1, rem[0], normalizedArea, normalizedDF, col_fluido);
      double initialFF = initialFFTerm * cdrag;
      ff[0] = initialFF;
      double actualVF = initVelocity - initialFF / m_punto;
      setValue(vf, 0, velocityError, actualVF);
      /*************************************** Calculo de la presion en 1 **************************************/
      double actualVMF = sTerm * actualVF;
      vmf[0] = actualVMF;
      double actualRem = ModelUtils.q_reynolds(actualVMF, tf[0], cellDiameter, actualDF);
      rem[0] = actualRem;
      double frictionFactor = eval.computeFrictionFactor(actualRem, separation, actualVMF / initVelocity, normalizedDF);
      double nextPF = pf[1];
      double actualPF = nextPF + 0.5 * frictionFactor * actualDF * pow(actualVMF, 2);
      setValue(pf, 0, pressureError, actualPF);

      // Tf(0), pf(end), Df(0), vmf(end) and rem(0) aren't modified in the loop
      for (int i = 0; i < col_fluido - 1; i++) {
        actualVF = vf[i];
        actualVMF = sTerm * actualVF;
        vmf[i] = actualVMF;
        final double actualTF = tf[i];
        actualDF = df[i];
        final int i2 = i + 1;
        rem[i2] = ModelUtils.q_reynolds(actualVMF, actualTF, cellDiameter, actualDF);
        actualRem = rem[i];
        /***************************************** Calculo de la presion **************************************/
        normalizedDF = actualDF / 1.205;
        frictionFactor = eval.computeFrictionFactor(actualRem, separation, vmf[i] / initVelocity, normalizedDF);
        nextPF = pf[i2];
        actualPF = nextPF + 0.5 * frictionFactor * actualDF * pow(actualVMF, 2);
        setValue(pf, i, pressureError, actualPF);
        /***************************************** Calculo de la velocidad ************************************/
        cdrag = eval.computeDragCoefficient(a.a1, actualRem, normalizedArea, normalizedDF, col_fluido);
        final double actualVFSquared = pow(actualVF, 2);
        final double nextFF = 0.5 * diamTimesZ * actualDF * actualVFSquared * cdrag;
        ff[i2] = nextFF;
        // Conservacion de momento
        final double nextVFSquared = (controlVolArea * (nextPF - actualPF) - nextFF) / m_punto + actualVFSquared;
        final double nextVF = sqrt(nextVFSquared);
        setValue(vf, i2, velocityError, nextVF);
        /********************************** Calculo de la temperatura fluido **********************************/
        final double cp = ArrayInterpolation.q_cp(actualTF);
        // Conservacion de energia
        final double nextTF = actualTF + (fluidTempTerm - 0.5 * (nextVFSquared - actualVFSquared)) / cp;
        setValue(tf, i2, TFError, nextTF);
        /***************************************** Calculo de la densidad *************************************/
        df[i2] = ArrayInterpolation.q_densidad(nextTF);
        /********************************** Calculo de temperatura de celda ***********************************/
        final double nu = eval.computeNusseltNumber(i * 2, actualRem, a.a3);
        final double iniFluidK = ArrayInterpolation.q_conductividad(actualTF);
        fluidK[i] = iniFluidK;
        final double h = nu * iniFluidK / cellDiameter;
        // Transferencia de energia
        setValue(tc, i, cellTempError, heatPerArea / h + (actualTF + nextTF) / 2);
      }
      if (max(cellTempError) <= errmax && max(TFError) <= errmax &&
          max(pressureError) <= errmax && max(velocityError) <= errmax) {
        break;
      }
    }

    return eval.returnValue(pf[0], vf[1], tc[0]);
  }

  private static double[] MAXFilledArray(int size) {
    double[] array = new double[size];
    Arrays.fill(array, Double.MAX_VALUE);
    return array;
  }

  private static double[] filledArray(int size, double value) {
    double[] array = new double[size];
    Arrays.fill(array, value);
    return array;
  }

  private static double max(double... array) {
    double max = array[0];
    for (int i = 1; i < array.length; i++)
      max = Math.max(max, array[i]);
    return max;
  }


  /**
   * Sets the vector at index to "value" and the error between the past value and the new
   *
   * @param i          The position in the vector to set
   * @param errorArray The error array of the vector
   * @param value      The value to set
   */
  public static void setValue(double[] vector, int i, double[] errorArray, double value) {
    errorArray[i] = vector[i];
    vector[i] = value;
    setFunctionError(vector, errorArray, i);
  }

  private static void setFunctionError(double[] vector, double[] error, int index) {
    double functionValue = vector[index];
    error[index] = abs(error[index] - functionValue) / functionValue;
  }

}