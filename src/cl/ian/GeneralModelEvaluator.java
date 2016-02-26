package cl.ian;

import cl.ian.gp.EvolutionStateBean;
import cl.ian.problemtype.ModelEvaluator;
import ec.gp.GPIndividual;
import org.ejml.data.DenseMatrix64F;
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

  public final ModelEvaluator eval;

  public GeneralModelEvaluator(ModelEvaluator evaluator) {
    this.eval = evaluator;
  }

  public double compute(double current, double separation, double flow, double initTemperature, double cellDiameter,
                        GPIndividual individual, EvolutionStateBean stateBean) {

    // This never happens, probably was used for testing and then became deprecated
    if (initTemperature == 0 && cellDiameter == 0 && individual.toString().equals("")) {
      initTemperature = 20;
      cellDiameter = 18;
    }

    eval.setStateIndividual(stateBean, individual);
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
    final OnesVector tf = new OnesVector(col_fluido, initTemperature);                                  //[°C]
    final OnesVector pf = new OnesVector(col_fluido, atmPressure);                                      //[Pa]

    // Speedup cache variable
    final double innerArg = flux * z / (largo * entranceArea);

    final OnesVector vf = new OnesVector(col_fluido, innerArg);                                        //[m/s]
    final DenseMatrix64F vmf = vf.copy();                                                              //[m/s]
    final OnesVector df = new OnesVector(col_fluido, 1.204);                                           //[kg/m3] Apparently 1.204 is never used
    final OnesVector tc = new OnesVector(col_celda, initTemperature);                                  //[°C]
    final DenseMatrix64F ff = new DenseMatrix64F(1, col_fluido);                                       //N
    final DenseMatrix64F rem = new DenseMatrix64F(1, col_fluido);                                      //Adimensional
    final DenseMatrix64F fluidK = new OnesVector(col_fluido, Interpolation.q_conductividad(tf.unsafe_get(0)));//[W/m k]
    /******************************************** Errores en columnas ********************************************/
    final double[] cellTempError = filledArray(col_celda, Double.MAX_VALUE);
    final double[] TFError = filledArray(col_fluido, Double.MAX_VALUE);
    final double[] velocityError = filledArray(col_fluido, Double.MAX_VALUE);
    final double[] pressureError = filledArray(col_celda, Double.MAX_VALUE);
    /******************************************** Condiciones de borde ********************************************/
    // Tf(1) = Tin;                       Temperatura entrada [ºC]
    // Vinicio = a(2)*Flujo*(z/Largo)/A;  Velocidad entrada[m/s]
    // Df(1) = q_densidad(Tin);           Densidad de entrada [kg/m3]
    // Pf(length(Pf)) = P_atm;            Presion entrada [Pa]. Presión de ¿salida? se asume la presión atmosférica [Pa]
    final double initVelocity = a.a2 * innerArg;           // Velocidad entrada[m/s]
    df.set(0, Interpolation.q_densidad(initTemperature));     // Densidad de entrada [kg/m3]
    /**************************************************************************************************************/
    // Speedup cache variable
    final double dfMultiplicationTerm = diamTimesZ * initVelocity * df.unsafe_get(0);    // df(0) never changes

    final double m_punto = sPlusOne * dfMultiplicationTerm;

    velocityError[0] = 0;
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
      double cdrag = eval.computeDragCoefficient(a.a1, rem.unsafe_get(0, 0), normalizedArea, df.unsafe_get(0) / 1.205, col_fluido);
      double initialFF = initialFFTerm * cdrag;
      ff.set(0, initialFF);
      double actualVF = initVelocity - initialFF / m_punto;
      vf.setValue(0, velocityError, actualVF);
      /*************************************** Calculo de la presion en 1 **************************************/
      double actualVMF = sTerm * actualVF;
      vmf.set(0, actualVMF);
      double actualDF = df.unsafe_get(0);
      double actualRem = ModelUtils.q_reynolds(actualVMF, tf.unsafe_get(0), cellDiameter, actualDF);
      rem.set(0, actualRem);
      double normalizedDF = actualDF / 1.205;
      double frictionFactor = eval.computeFrictionFactor(actualRem, separation, actualVMF / initVelocity, normalizedDF);
      double nextPF = pf.unsafe_get(1);
      double actualPF = nextPF + 0.5 * frictionFactor * actualDF * pow(actualVMF, 2);
      pf.setValue(0, pressureError, actualPF);

      // Tf(0), pf(end), Df(0), vmf(end) and rem(0) aren't modified in the loop
      for (int i = 0; i < col_fluido - 1; i++) {
        actualVF = vf.unsafe_get(i);
        actualVMF = sTerm * actualVF;
        vmf.set(i, actualVMF);
        final double actualTF = tf.unsafe_get(i);
        actualDF = df.unsafe_get(i);
        rem.set(i + 1, ModelUtils.q_reynolds(actualVMF, actualTF, cellDiameter, actualDF));
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
        ff.set(i + 1, nextFF);
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
        df.set(i + 1, Interpolation.q_densidad(nextTF));
        /********************************** Calculo de temperatura de celda ***********************************/
        final double nu = eval.evaluateNusseltNumber(i * 2, actualRem, a.a3);
        final double iniFluidK = Interpolation.q_conductividad(actualTF);
        fluidK.set(i, iniFluidK);
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