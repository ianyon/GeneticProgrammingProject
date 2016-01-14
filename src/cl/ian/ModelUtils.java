package cl.ian;

/**
 * Created by ian on 6/16/15.
 */

public class ModelUtils {

	/**
	 * Drag en función del Reynolds
	 *
	 * @param Re Reynolds function?
	 * @return
	 */
	public static double q_cdr3(double Re) {
		// This happens if the result is NaN or not real
		if (Re < 0 || Double.isNaN(Re)) {
			return -1;
		}
		double c = 3.3481;
		double n = -0.179;
		// Note: Commented size checking it's unnesesary
		//if (Re.length == 1){
		//return c*Math.pow(Re[0],-n);
		//} else
		return c * Math.pow(Re, -n);   //c.*Re.^-n;
	}

	public static double q_reynolds(double v, double T, double Diam, double rho) {
		double visc = Interpolation.q_viscosidad(T);
		return rho * v * Diam / visc;
	}

	public static double q_nusselt2(double re, double a) {
		//   Prandlt = 0.7 y Pr = 0.689
		double Pr = 0.713;
		double Pr_w = 0.689;

		double c, m, n;

		// This happens if the result is NaN or not real
		if (re <= 0 || Double.isNaN(re)) {
			return 0.0001;
		}

		if (re <= 1E2) {
			c = 0.9;
			m = a;
			n = 0.37;
		} else if (re > 1E2 && re <= 1E3) {
			c = 0.51;
			m = a;
			n = 0.37;
		} else if (re > 1E3 && re <= 2E5) {
			c = 0.35;
			m = a;
			n = 0.37;
		} else {
			// re > 2E5
			c = 0.023;
			m = a;
			n = 0.37;
		}

		return c * Math.pow(re, m) * Math.pow(Pr, n) * Math.pow(Pr / Pr_w, 0.25);
	}

	public static double max(double... array) {
		double max = array[0];
		for (int i = 1; i < array.length; i++)
			max = Math.max(max, array[i]);
		return max;
	}
}
