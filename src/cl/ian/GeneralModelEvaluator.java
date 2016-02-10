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
    static final double largo = 65E-3;                      //largo de celdas [m]
    static final double e = 15E-3;                          //Espaciado entre pared y celda [m]
    static final double z = 5E-3;                           //Corte del estudio [m]
    static final double errmax = 1E-3;                      //error corte
    static final double piCuarto = PI / 4;
    static final double doubleE = 2 * e;

    public final ModelEvaluator evaluator;

    public GeneralModelEvaluator(ModelEvaluator evaluator) {
        this.evaluator = evaluator;
    }

  public double compute(double current, double separation, double flow, double initTemperature, double cellDiameter,
                          GPIndividual individual, EvolutionStateBean stateBean) {

        if (initTemperature == 0 && cellDiameter == 0 && individual.toString().equals("")) {
            initTemperature = 20;
            cellDiameter = 18;
        }

        evaluator.setStateIndividual(stateBean, individual);
        final double atmPressure = evaluator.atmosphericPressure();                  //Presion atmosferica [Pa]

        // Constantes del modelo
        final double[] result = Interpolation.q_paramdrag(separation);
        final FixedMatrix3_64F a = new FixedMatrix3_64F(result[0], result[1], 0.653);

        final double flux = flow * 0.00047;                                          //flujo de entrada del fluido [CFM]->[m3/s]
        final double diam = cellDiameter / 1000;

        // Speedup cache variable
        final double cellArea = pow(diam, 2) * piCuarto;

        final double vol = largo * cellArea;                                          //Volumen celda [m3]
        final double volumetricQ = pow(current, 2) * r / vol;                         //Calor volumetrico
        final double q = volumetricQ * z * cellArea;                                  //Calor total corte.

        // Speedup cache variables
        final double diamTimesZ = diam * z;
        final double superficialArea = PI * diamTimesZ;                               //Area de la celda

        final double height = doubleE + diam * (n_fluido + separation * n_celda);     //Altura del pack
        final double entranceArea = height * z;                                       //Area de entrada pack

        // Speedup cache variable
        final double sPlusOne = separation + 1;

        final double controlVolumeArea = sPlusOne * diamTimesZ;                       //Area volumen control eje z

        // Inicializacion
        //Temperatura entrada [°C]
        final MDenseMatrix64F tf = new MDenseMatrix64F(col_fluido);                                 //[°C]
        //Presion entrada [Pa]
        final MDenseMatrix64F pf = new MDenseMatrix64F(col_fluido);                                 //[Pa]

        // Speedup cache variable
        final double innerArg = flux * z / (largo * entranceArea);

        final MDenseMatrix64F vf = new MDenseMatrix64F(col_fluido, innerArg);                       //[m/s]
        final DenseMatrix64F vmf = new MDenseMatrix64F(col_fluido, innerArg);                       //[m/s]
        final MDenseMatrix64F df = new MDenseMatrix64F(col_fluido);                                 //[kg/m3]
        final MDenseMatrix64F tc = new MDenseMatrix64F(col_celda, initTemperature);                 //[°C]
        final DenseMatrix64F ff = new DenseMatrix64F(1, col_fluido);                                   //N
        final DenseMatrix64F rem = new DenseMatrix64F(1, col_fluido);                                  //Adimensional

        /******************************************** Errores en columnas ********************************************/
        final double[] cellTempError = new double[col_celda];
        final double[] fluidTempError = new double[col_fluido];
        final double[] velocityError = new double[col_fluido];
        final double[] pressureError = new double[col_celda];
        Arrays.fill(cellTempError, Double.MAX_VALUE);
        Arrays.fill(fluidTempError, Double.MAX_VALUE);
        Arrays.fill(velocityError, Double.MAX_VALUE);
        Arrays.fill(pressureError, Double.MAX_VALUE);
        /******************************************** Condiciones de borde ********************************************/
        tf.set(0, initTemperature);                               // Temperatura entrada [°C]
        final double initialVelocity = a.a2 * innerArg;           // Velocidad entrada[m/s]
        df.set(0, Interpolation.q_densidad(initTemperature));     // Densidad de entrada [kg/m3]
        pf.set(pf.getNumElements() - 1, atmPressure);             // Presión de salida se asume la presión atmosférica [Pa]
        /**************************************************************************************************************/
        // Speedup cache variable
        final double dfMultiplicationTerm = diamTimesZ * initialVelocity * df.get(0);

        final double m_punto = sPlusOne * dfMultiplicationTerm;

        final DenseMatrix64F fluidK = new MDenseMatrix64F(col_fluido, Interpolation.q_conductividad(tf.get(0)));//[W/m k]
        velocityError[0] = 0;
        fluidTempError[0] = 0;

        // Speedup cache variables
        final double sTerm = separation / sPlusOne;
        final double heatPerArea = q / superficialArea;
        final double initialFFTerm = 0.5 * dfMultiplicationTerm * initialVelocity;
        final double fluidTempTerm = q / m_punto;
        final double normalizedArea = superficialArea / controlVolumeArea;

        // Ecuaciones
        for (int x = 0; x < 10; x++) {
            // This gets uptated with the first value from the last attempt to converge
            double cdrag = evaluator.evaluateDragCoefficient(
                    a.a1, rem.get(0), normalizedArea, df.get(0) / 1.205, col_fluido);
            double initialFF = initialFFTerm * cdrag;
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
                final double iniRem = ModelUtils.q_reynolds(iniFluidMedVel, iniFluidTemp, diam, iniFluidDensity);
                final double iniFluidVelSquared = pow(iniFluidVel, 2);
                /******************************************************************************************************/

                vmf.set(i, iniFluidMedVel);
                rem.set(i, iniRem);

                /***************************************** Calculo de la presion **************************************/
                double ffrictionX = evaluator.evaluateFrictionFactor(
                        iniRem, separation, vmf.get(i) / initialVelocity, iniFluidDensity / 1.205);

                // Caída de presion en intercambiadores de calor
                // Note:salía (+) antes pero la ecuación dice que es con menos.
                // Además es necesario usar los valores de la columna apropiada
                //int inverseIndex = col_fluido - 1 - i;
                final double endFluidPressure = pf.get(i + 1);
                final double iniFluidPressure = endFluidPressure - 0.5 * ffrictionX * iniFluidDensity * pow(iniFluidMedVel, 2);
                pf.setValue(i, pressureError, iniFluidPressure);

                /***************************************** Calculo de la velocidad ************************************/
                cdrag = evaluator.evaluateDragCoefficient(
                        a.a1,iniRem, normalizedArea, iniFluidDensity / 1.205, col_fluido);
                final double endFF = 0.5 * diamTimesZ * iniFluidDensity * iniFluidVelSquared * cdrag;
                ff.set(i + 1, endFF);
                // Conservacion de momento
                final double sqrtInnerTerm = (controlVolumeArea * (endFluidPressure - iniFluidPressure) - endFF) / m_punto
                        + iniFluidVelSquared;
                final double endFluidVel = sqrt(sqrtInnerTerm);
                vf.setValue(i + 1, velocityError, endFluidVel);

                /********************************** Calculo de la temperatura fluido **********************************/
                final double cp = Interpolation.q_cp(iniFluidTemp);
                // Conservacion de energia
                final double endFluidTemp = iniFluidTemp + (fluidTempTerm - 0.5 * (pow(endFluidVel, 2) - iniFluidVelSquared)) / cp;
                tf.setValue(i + 1, fluidTempError, endFluidTemp);

                /***************************************** Calculo de la densidad *************************************/
                df.set(i + 1, Interpolation.q_densidad(endFluidTemp));

                /********************************** Calculo de temperatura de celda ***********************************/
                // Shift operator multiplies by 2
                final double nu = evaluator.evaluateNusseltNumber(i*2,iniRem,a.a3);
                final double iniFluidK = Interpolation.q_conductividad(iniFluidTemp);
                fluidK.set(i, iniFluidK);
                final double h = nu * iniFluidK / diam;
                // Transferencia de energia
                tc.setValue(i, cellTempError, heatPerArea / h + (iniFluidTemp + endFluidTemp) / 2);
            }
            if (max(cellTempError) <= errmax && max(fluidTempError) <= errmax &&
                    max(pressureError) <= errmax && max(velocityError) <= errmax) {
                break;
            }
        }

        return evaluator.returnValue(pf.get(0), vf.get(1), tc.get(0));
    }

  public static double max(double... array) {
    double max = array[0];
    for (int i = 1; i < array.length; i++)
      max = Math.max(max, array[i]);
    return max;
  }
}