package cl.ian;

import org.ejml.data.Complex64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.data.FixedMatrix3_64F;
import org.ejml.ops.CommonOps;

import javax.script.ScriptException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.lang.Math.PI;
import static java.lang.Math.pow;


/**
 * Created by ian on 6/15/15.
 */

public class QModeloFfrictionBAK {
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
	static final double atmPressure = 0;                    //Presion atmosferica [Pa]
	static final double errmax = 1E-3;                      //error corte

	public QModeloFfrictionBAK() {
		try {
			mEngine = new JavascriptEngine();
		} catch (ScriptException e) {
			e.printStackTrace();
		}
	}

	double compute(double I, double S, double Flow, double Tin, double Diamm, String str) {

		if (Tin == 0 && Diamm == 0 && str.equals("")) {
			Tin = 20;
			Diamm = 18;
		}

		// Constantes del modelo
		double[] result = Interpolation.q_paramdrag(S);
		final FixedMatrix3_64F a = new FixedMatrix3_64F(result[0], result[1], 0.653);

		//format long
		double flujo = Flow * 0.00047;//flujo de entrada del fluido [CFM]->[m3/s]
		double diam = Diamm / 1000;
		double vol = largo * pow(diam, 2) * PI / 4;//Volumen celda [m3]
		double volumetricQ = pow(I, 2) * r / vol;//Calor volumetrico
		double q = volumetricQ * z * pow(diam, 2) * PI / 4;//Calor total corte.
		double superficialArea = PI * diam * z;//Area de la celda
		double height = 2 * e + diam * n_fluido + S * diam * (n_celda);//Altura del pack
		double entranceArea = height * z;//Area de entrada pack
		double controlVolumeArea = (S + 1) * diam * z;//Area volumen control eje z

		// Inicializacion
		//Temperatura entrada [°C]
		DenseMatrix64F tf = new MDenseMatrix64F(1, col_fluido, Tin);                                //[°C]
		//Presion entrada [Pa]
		DenseMatrix64F pf = new MDenseMatrix64F(1, col_fluido, atmPressure);                        //[Pa]
		double innerArg = flujo * z / (largo * entranceArea);
		MCDenseMatrix64F vf = new MCDenseMatrix64F(1, col_fluido, innerArg);                        //[m/s]
		MCDenseMatrix64F vmf = new MCDenseMatrix64F(1, col_fluido, innerArg);                       //[m/s]
		DenseMatrix64F df = new MDenseMatrix64F(1, col_fluido, 1.204);                              //[kg/m3]
		DenseMatrix64F tc = new MDenseMatrix64F(1, col_celda, Tin);                                 //[°C]
		DenseMatrix64F ff = new DenseMatrix64F(1, col_fluido);                                      //N
		DenseMatrix64F rem = new DenseMatrix64F(1, col_fluido);                                     //Adimensional
		DenseMatrix64F k_fluido = new MDenseMatrix64F(1, col_fluido, Interpolation.q_conductividad(tf.get(0)));//[W/m k]

		// errores i-esimos columnas
		DenseMatrix64F error = new MDenseMatrix64F(1, col_celda, Double.MAX_VALUE);                 //error tceldas
		DenseMatrix64F errf = new MDenseMatrix64F(1, col_fluido, Double.MAX_VALUE);                  //error tfluido
		MCDenseMatrix64F errv = new MCDenseMatrix64F(1, col_fluido, Double.MAX_VALUE);                  //error velocidad
		DenseMatrix64F errp = new MDenseMatrix64F(1, col_celda, Double.MAX_VALUE);                  //error presion

		// Condiciones de borde
		double Vinicio = a.a2 * flujo * (z / largo) / entranceArea;    //Velocidad entrada[m/s]
		df.set(0, Interpolation.q_densidad(Tin));                      //Densidad de entrada [kg/m3]
		double m_punto = (S + 1) * diam * z * Vinicio * df.get(0);
		errv.set(0, 0, 0, 0);
		errf.set(0, 0);

		// Ecuaciones
		int x = 0;
		while (CommonOps.elementMax(error) > errmax || CommonOps.elementMax(errf) > errmax ||
				CommonOps.elementMax(errp) > errmax) {// || CommonOps.elementMax(errv) > errmax) {

			if (x >= 10) break;

			x++;

			//velocidad del fluido en 1
			double cdr3 = ModelUtils.q_cdr3(rem.get(0));
			double cdrag = a.a1 * cdr3;
			ff.set(0, 0.5 * diam * z * df.get(0) * pow(Vinicio, 2) * cdrag);

			errv.set(0, 0, vf);

			vf.set(0, 0, Vinicio - ff.get(0) / m_punto, 0);

			measureError(vf, errv);
/*
            // Presion del fluido en 1
            vmf.set(0, 0, MCDenseMatrix64F.multiply(0,0,S / (S + 1), vf));
            rem.set(0, ModelUtils.q_reynolds(vmf.get(0,0), tf.get(0), diam, df.get(0)));
            errp.set(0, pf.get(0));

            // Evaluate string with individual
            mEngine.updateVariable(Vinicio, vmf.get(0), df.get(0), S, rem.get(0));
            double ffrictionX = mEngine.eval(str);

            pf.set(0, pf.get(0) + 0.5 * ffrictionX * df.get(0) * pow(vmf.get(0), 2));
            errp.set(0, abs(pf.get(0) - errp.get(0)) / pf.get(0));

            for (int i = 0; i < col_fluido-1; i++) {
                vmf.set(i, (S / (S + 1)) * vf.get(i));
                rem.set(i + 1, ModelUtils.q_reynolds(vmf.get(i), tf.get(i), diam, df.get(i)));
                //Calculo de la presion
                errp.set(i, pf.get(i));

                mEngine.updateVariable(Vinicio, vmf.get(i), df.get(i), S, rem.get(i));
                ffrictionX = mEngine.eval(str);

                // Caída de presion en intercambiadores de calor
                pf.set(i, pf.get(i + 1) + 0.5 * ffrictionX * df.get(i) * pow(vmf.get(i), 2));
                errp.set(i, abs(pf.get(i) - errp.get(i)) / pf.get(i));

                //Calculo de la velocidad
                //TODO: Probar estos
                //MatrixFeatures.hasNaN(rem);
                //MatrixFeatures.hasUncountable(rem);
                cdrag = a.a1 * ModelUtils.q_cdr3(rem.get(i));
                ff.set(i + 1, 0.5 * diam * z * df.get(i) * pow(vf.get(i), 2) * cdrag);
                errv.set(i + 1, vf.get(i + 1));

                double innerTerm = (controlVolumeArea * (pf.get(i + 1) - pf.get(i)) - ff.get(i + 1)) / m_punto +
                        pow(vf.get(i), 2);

                vf.set(i + 1, sqrt());

                // Conservacion de momento
                errv.set(i + 1, abs(vf.get(i + 1) - errv.get(i + 1)) / vf.get(i + 1));

                //Calculo de la temperatura fluido
                errf.set(i + 1, tf.get(i + 1));
                double cp = Interpolation.q_cp(tf.get(i));

                // Conservacion de energia
                tf.set(i + 1, tf.get(i) + ((q / m_punto) - 0.5 * (pow(vf.get(i + 1), 2) - pow(vf.get(i), 2))) / cp);
                errf.set(i + 1, abs(errf.get(i + 1) - tf.get(i + 1)) / tf.get(i + 1));

                //Calculo de la densidad
                df.set(i + 1, Interpolation.q_densidad(tf.get(i + 1)));

                //Calculo de temperatura de celda
                double nu = Interpolation.q_cznusselt(i * 2, rem.get(i)) * ModelUtils.q_nusselt2(rem.get(i), a.a3);
                k_fluido.set(i, Interpolation.q_conductividad(tf.get(i)));
                double h = nu * k_fluido.get(i) / diam;
                error.set(i, tc.get(i));

                // Transferencia de energia
                tc.set(i, q / (superficialArea * h) + (tf.get(i) + tf.get(i + 1)) / 2);
                error.set(i, abs(error.get(i) - tc.get(i)) / tc.get(i));
            }*/
		}

		if (Double.MAX_VALUE == pf.get(0) || Double.isNaN(pf.get(0)))//TODO || ~isreal(pf[1]))
			return 0;
		else
			return pf.get(0);
	}

	private static void measureError(MCDenseMatrix64F function, MCDenseMatrix64F error) {
		Complex64F errorValue = new Complex64F(),
				functionValue = new Complex64F();
		error.get(0, 0, errorValue);
		function.get(0, 0, functionValue);
		Complex64F absoluteDifference = new Complex64F(errorValue.minus(functionValue).getMagnitude(), 0);

		error.set(0, 0, absoluteDifference.divide(functionValue));
	}

	public static void main(String[] args) throws ScriptException {
		QModeloFfrictionBAK model = new QModeloFfrictionBAK();

		model.compute(2.978500, 0.957700, 58.915000, 18.862000, 22.000000, "times(3.3983,mypower(Rem(i),0.86792))");

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

		for (String line : lines) {
			//for (int h=0;h<=500;h++) {
			//  String line = lines.get(h);

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
			double result = model.compute(doubleVal[0], doubleVal[1], doubleVal[2], doubleVal[3], doubleVal[4], str);
			meanTime += (System.nanoTime() - startTime) / 1000000;

			if (100 * Math.abs(result - doubleVal[5]) / doubleVal[5] > 0.05 / 100) {
				System.out.format("Resultado esperado es %f y se obtuvo %f%n", doubleVal[5], result);
				System.out.format("Entrada: %f %f %f %f %f %s%n", doubleVal[0], doubleVal[1], doubleVal[2], doubleVal[3], doubleVal[4], str);
				return;
			}
		}

		System.out.println("Tiempo medio: " + meanTime / lines.size() + "ms");
	}
}