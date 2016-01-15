package cl.ian;

import cl.ian.gp.EvolutionStateBean;
import ec.gp.GPIndividual;
import org.ejml.data.DenseMatrix64F;
import org.ejml.data.FixedMatrix3_64F;

import javax.script.ScriptException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.*;


/**
 * Created by ian on 6/15/15.
 */

public class QModeloFfriction {
    JavascriptEngine mEngine;

    // Cantidad de celdas
    static final int col_fluido = 2;
    static final int col_celda = 1;
    static final int n_fluido = 2;
    static final int n_celda = 1;

    static final double r = 32E-3;                          //Resistencia interna [Ohm]
    static final double largo = 65E-3;                      //largo de celdas [m]
    static final double e = 15E-3;                          //Espaciado entre pared y celda [m]
    static final double z = 5E-3;                           //Corte del estudio [m]
    // La presión atmosferica en pascales es 101325, aunque no importa porque la ecuación está en función de la diferencia
    static final double atmPressure = 0;//101325                    //Presion atmosferica [Pa]
    static final double errmax = 1E-3;                      //error corte
    static final double piCuarto = PI / 4;
    static final double doubleE = 2 * e;

    public QModeloFfriction() {
        /*try {
            mEngine = new JavascriptEngine();
        } catch (ScriptException e) {
            e.printStackTrace();
        }*/
    }

    public double compute(double current, double separation, double flow, double initTemperature, double cellDiameter, GPIndividual individual, EvolutionStateBean stateBean) {

        //TODO: This can happen? individual.trees[0].child.toString().equals("")
        if (initTemperature == 0 && cellDiameter == 0 && individual.trees[0].child.toString().equals("")) {
            initTemperature = 20;
            cellDiameter = 18;
        }

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
        final MDenseMatrix64F tf = new MDenseMatrix64F(1, col_fluido);                                 //[°C]
        //Presion entrada [Pa]
        final MDenseMatrix64F pf = new MDenseMatrix64F(1, col_fluido);                                 //[Pa]

        // Speedup cache variable
        final double innerArg = flux * z / (largo * entranceArea);

        final MDenseMatrix64F vf = new MDenseMatrix64F(1, col_fluido, innerArg);                       //[m/s]
        final DenseMatrix64F vmf = new MDenseMatrix64F(1, col_fluido, innerArg);                       //[m/s]
        final MDenseMatrix64F df = new MDenseMatrix64F(1, col_fluido);                                 //[kg/m3]
        final MDenseMatrix64F tc = new MDenseMatrix64F(1, col_celda, initTemperature);                 //[°C]
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

        final DenseMatrix64F fluidK = new MDenseMatrix64F(1, col_fluido, Interpolation.q_conductividad(tf.get(0)));//[W/m k]
        velocityError[0] = 0;
        fluidTempError[0] = 0;

        // Speedup cache variables
        final double sTerm = separation / sPlusOne;
        final double heatPerArea = q / superficialArea;
        final double initialFFTerm = 0.5 * dfMultiplicationTerm * initialVelocity;
        final double fluidTempTerm = q / m_punto;
        final JavascriptEngine engine = this.mEngine;

        // Ecuaciones
        for (int x = 0; x < 10; x++) {
            // This gets uptated with the first value from the last attempt to converge
            double cdrag = a.a1 * ModelUtils.q_cdr3(rem.get(0));
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
                stateBean.phenomenologicalModel.rem = rem.get(i);
                stateBean.phenomenologicalModel.separation = separation;
                stateBean.phenomenologicalModel.normalizedMeanVelocity = vmf.get(i)/initialVelocity;
                stateBean.phenomenologicalModel.normalizedFluidDensity = df.get(i)/1.205;
                individual.trees[0].child.eval(stateBean.state, stateBean.threadnum, stateBean.input, stateBean.stack,
                        individual, stateBean.phenomenologicalModel);
                double ffrictionX = stateBean.input.x;
                //double ffrictionX = engine.eval(
                //        stateBean, initialVelocity, iniFluidMedVel, iniFluidDensity, separation, iniRem);
                // Caída de presion en intercambiadores de calor
                // Note:salía (+) antes pero la ecuación dice que es con menos.
                // Además es necesario usar los valores de la columna apropiada
                //int inverseIndex = col_fluido - 1 - i;
                final double endFluidPressure = pf.get(i + 1);
                final double iniFluidPressure = endFluidPressure - 0.5 * ffrictionX * iniFluidDensity * pow(iniFluidMedVel, 2);
                pf.setValue(i, pressureError, iniFluidPressure);

                /***************************************** Calculo de la velocidad ************************************/
                cdrag = a.a1 * ModelUtils.q_cdr3(iniRem);
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
                final double nu = Interpolation.q_cznusselt(i << i, iniRem) * ModelUtils.q_nusselt2(iniRem, a.a3);
                final double iniFluidK = Interpolation.q_conductividad(iniFluidTemp);
                fluidK.set(i, iniFluidK);
                final double h = nu * iniFluidK / diam;
                // Transferencia de energia
                tc.setValue(i, cellTempError, heatPerArea / h + (iniFluidTemp + endFluidTemp) / 2);
            }
            if (MatlabUtils.max(cellTempError) <= errmax && MatlabUtils.max(fluidTempError) <= errmax &&
                    MatlabUtils.max(pressureError) <= errmax && MatlabUtils.max(velocityError) <= errmax) {
                break;
            }
        }

        // This happens if the result is NaN, Infinite or not real
        if (Double.MAX_VALUE == pf.get(0) || Double.isNaN(pf.get(0)))
            return 0;

        return -pf.get(0);
    }

    public static void main(String[] args) throws ScriptException {
        QModeloFfriction model = new QModeloFfriction();

        Path path = Paths.get("/home/ian/Workspace/Evolutiva/Proyecto/matlab code/GPLab reducido",
                "model_args.txt");
        Charset charset = Charset.forName("UTF-8");

        List<String> lines;
        try {
            lines = Files.readAllLines(path, charset);
        } catch (IOException e) {
            System.out.println(e);
            return;
        }

        lines.remove(0);

        double meanTime = 0;
        double min = Double.MAX_VALUE, max = -Double.MAX_VALUE;

        //long totalStartTime = System.nanoTime();
        for (String line : lines) {
            //for (int h = 0; h < 500; h++) {
            //String line = lines.get(h);

            String[] elements = line.split(" ");

            double[] doubleVal = new double[6];
            String str;

            for (int i = 0; i < 5; i++) {
                if (i < 5)
                    doubleVal[i] = Double.parseDouble(elements[i]);
            }
            str = elements[5];
            doubleVal[5] = Double.parseDouble(elements[6]);

            long startTime = System.nanoTime();
            double result = 0;//model.compute(doubleVal[0], doubleVal[1], doubleVal[2], doubleVal[3], doubleVal[4], individual, str);
            double elapsed = (System.nanoTime() - startTime) / 1000;
            min = min(min, elapsed);
            max = max(max, elapsed / 1000);
            meanTime += elapsed / 1000;

            double percentError = 100 * Math.abs(result - doubleVal[5]) / doubleVal[5];

            if (percentError > 0.5) {
                System.out.format("Error %g%%. Esperado %f y se obtuvo %f %n", percentError, doubleVal[5], result);
                System.out.format("Entrada: %f %f %f %f %f %s%n", doubleVal[0], doubleVal[1], doubleVal[2], doubleVal[3], doubleVal[4], str);
                //return;
            }
        }
        //double totalMeanTime = (System.nanoTime() - totalStartTime) / 1000000;

        System.out.println("Tamaño dataset: " + lines.size());
        System.out.format("Tiempo total prueba: %f ms%n", meanTime);
        System.out.format("Tiempo medio en dataset: %f ms%n", meanTime / lines.size());
        System.out.format("Tiempo máximo: %f ms%n", max);
        System.out.format("Tiempo mínimo: %f us%n", min);

		/*long startTime = System.nanoTime();
        int nTest = 10;
		for (int i = 0; i < nTest; i++) {
			//long localTime = System.nanoTime();
			double result = model.compute(9.0554, 1.0176, 181.9, 18.93, 18,
					"minus(times(minus((Vmf(i)/Vinicio),(Vmf(i)/Vinicio)),times(Rem(i),(Df(i)/1.205)))," +
					"mypower(q_mypower2((Df(i)/1.205)),q_mypower2((Df(i)/1.205)))) -152.917");
			//double localMeanTime = (System.nanoTime() - localTime) / 1000000;
			//System.out.println("Tiempo prueba: " + localMeanTime + "ms");
		}
		meanTime = (System.nanoTime() - startTime) / 1000000;

		System.out.println("Número de pruebas: " + nTest);
		System.out.println("Tiempo total prueba repetida: " + meanTime + "ms");*/
    }
}