package cl.ian;

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
public class GeneralModelEvaluator {

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

  public GeneralModelEvaluator(ModelEvaluator evaluator) {
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

    final MyVector vf = new MyVector(col_fluido, innerArg);                                           //[m/s]
    final MyVector vmf = new MyVector(col_fluido, innerArg);                                          //[m/s]
    final MyVector df = new MyVector(col_fluido);                                                     //[kg/m3]
    final MyVector tc = new MyVector(col_celda, initTemperature);                                     //[°C]
    final MyVector ff = new MyVector(col_fluido);                                                     //N
    final MyVector rem = new MyVector(col_fluido);                                                    //Adimensional
    final MyVector fluidK = new MyVector(col_fluido, Interpolation.q_conductividad(tf.unsafe_get(0)));//[W/m k]
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
    df.unsafe_set(0, Interpolation.q_densidad(initTemperature));     // Densidad de entrada [kg/m3]
    /**************************************************************************************************************/
    // Speedup cache variable
    final double dfMultiplicationTerm = diamTimesZ * initVelocity * df.unsafe_get(0);    // df(0) never changes

    final double m_punto = sPlusOne * dfMultiplicationTerm;

    TFError[0] = 0;

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
      double actualDF = df.unsafe_get(0);
      double normalizedDF = actualDF / 1.205;
      double cdrag = eval.computeDragCoefficient(a.a1, rem.unsafe_get(0, 0), normalizedArea, normalizedDF, col_fluido);
      double initialFF = initialFFTerm * cdrag;
      ff.unsafe_set(0, initialFF);
      double actualVF = initVelocity - initialFF / m_punto;
      vf.setValue(0, velocityError, actualVF);
      /*************************************** Calculo de la presion en 1 **************************************/
      double actualVMF = sTerm * actualVF;
      vmf.unsafe_set(0, actualVMF);
      double actualRem = ModelUtils.q_reynolds(actualVMF, tf.unsafe_get(0), cellDiameter, actualDF);
      rem.unsafe_set(0, actualRem);
      double frictionFactor = eval.computeFrictionFactor(actualRem, separation, actualVMF / initVelocity, normalizedDF);
      double nextPF = pf.unsafe_get(1);
      double actualPF = nextPF + 0.5 * frictionFactor * actualDF * pow(actualVMF, 2);
      pf.setValue(0, pressureError, actualPF);

      // Tf(0), pf(end), Df(0), vmf(end) and rem(0) aren't modified in the loop
      for (int i = 0; i < col_fluido - 1; i++) {
        actualVF = vf.unsafe_get(i);
        actualVMF = sTerm * actualVF;
        vmf.unsafe_set(i, actualVMF);
        final double actualTF = tf.unsafe_get(i);
        actualDF = df.unsafe_get(i);
        rem.unsafe_set(i + 1, ModelUtils.q_reynolds(actualVMF, actualTF, cellDiameter, actualDF));
        actualRem = rem.unsafe_get(0, i);
        /***************************************** Calculo de la presion **************************************/
        normalizedDF = actualDF / 1.205;
        frictionFactor = eval.computeFrictionFactor(actualRem, separation, vmf.unsafe_get(0, i) / initVelocity, normalizedDF);
        nextPF = pf.unsafe_get(i + 1);
        actualPF = nextPF + 0.5 * frictionFactor * actualDF * pow(actualVMF, 2);
        pf.setValue(i, pressureError, actualPF);
        /***************************************** Calculo de la velocidad ************************************/
        cdrag = eval.computeDragCoefficient(a.a1, actualRem, normalizedArea, normalizedDF, col_fluido);
        final double actualVFSquared = pow(actualVF, 2);
        final double nextFF = 0.5 * diamTimesZ * actualDF * actualVFSquared * cdrag;
        ff.unsafe_set(i + 1, nextFF);
        // Conservacion de momento
        final double nextVFSquared = (controlVolArea * (nextPF - actualPF) - nextFF) / m_punto + actualVFSquared;
        final double nextVF = sqrt(nextVFSquared);
        vf.setValue(i + 1, velocityError, nextVF);
        /********************************** Calculo de la temperatura fluido **********************************/
        final double cp = Interpolation.q_cp(actualTF);
        // Conservacion de energia
        final double nextTF = actualTF + (fluidTempTerm - 0.5 * (nextVFSquared - actualVFSquared)) / cp;
        tf.setValue(i + 1, TFError, nextTF);
        /***************************************** Calculo de la densidad *************************************/
        df.unsafe_set(i + 1, Interpolation.q_densidad(nextTF));
        /********************************** Calculo de temperatura de celda ***********************************/
        final double nu = eval.computeNusseltNumber(i * 2, actualRem, a.a3);
        final double iniFluidK = Interpolation.q_conductividad(actualTF);
        fluidK.unsafe_set(i, iniFluidK);
        final double h = nu * iniFluidK / cellDiameter;
        // Transferencia de energia
        tc.setValue(i, cellTempError, heatPerArea / h + (actualTF + nextTF) / 2);
      }
      if (max(cellTempError) <= errmax && max(TFError) <= errmax &&
          max(pressureError) <= errmax && max(velocityError) <= errmax) {
        break;
      }
    }

    return eval.returnValue(pf.unsafe_get(0), vf.unsafe_get(1), tc.unsafe_get(0));
  }

  private static double[] MAXFilledArray(int size) {
    double[] array = new double[size];
    Arrays.fill(array, Double.MAX_VALUE);
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
  public static void setValue(VectorWrapper vector, int i, double[] errorArray, double value) {
    errorArray[i] = vector.unsafe_get(i);
    vector.unsafe_set(i, value);
    setFunctionError(vector, errorArray, i);
  }

  public static void setValue(VectorWrapper vector, int i, MyFixedMatrix2 errorArray, double value) {
    errorArray.unsafe_set(i, vector.get(i));
    vector.unsafe_set(i, value);
    setFunctionError(vector, errorArray, i);
  }

  private static void setFunctionError(VectorWrapper vector, MyFixedMatrix2 error, int index) {
    double functionValue = vector.get(index);
    error.unsafe_set(index, abs(error.unsafe_get(index) - functionValue) / functionValue);
  }

  public static double setValue(VectorWrapper vector, int i, double value) {
    vector.unsafe_set(i, value);
    return setFunctionError(vector, vector.get(i), i);
  }

  private static double setFunctionError(VectorWrapper vector, double error, int index) {
    double functionValue = vector.get(index);
    return abs(error - functionValue) / functionValue;
  }

  private static void setFunctionError(VectorWrapper vector, double[] error, int index) {
    double functionValue = vector.get(index);
    error[index] = abs(error[index] - functionValue) / functionValue;
  }
}