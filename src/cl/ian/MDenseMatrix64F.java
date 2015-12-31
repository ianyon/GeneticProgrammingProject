package cl.ian;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

import static java.lang.Math.abs;

/**
 * Created by ian on 6/16/15.
 */
public class MDenseMatrix64F extends DenseMatrix64F {
	public MDenseMatrix64F(int rows, int cols, double value) {
		super(rows, cols);
		CommonOps.fill(this, value);
	}

	public MDenseMatrix64F(int rows, int cols) {
		super(rows, cols);
	}

	public void set(int index, DenseMatrix64F matrix) {
		set(index, matrix.get(index));
	}

	public void setValue(int index, double[] error, double value) {
		error[index] = this.get(index);
		set(index, value);
		setFunctionError(error,index);
	}

	private void setFunctionError(double[] error, int index) {
		double functionValue = get(index);
		error[index] = abs(error[index] - functionValue) / functionValue;
	}
}
