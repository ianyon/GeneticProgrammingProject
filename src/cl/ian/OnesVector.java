package cl.ian;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

import static java.lang.Math.abs;

/**
 * Created by ian on 6/16/15.
 */
public class OnesVector extends DenseMatrix64F {
  public OnesVector(int cols, double value) {
    super(1, cols);
    CommonOps.fill(this, value);
  }

  public OnesVector(int cols) {
    super(1, cols);
  }

  /**
   * Sets the vector at index to "value" and the error between the past value and the new
   *
   * @param index The position in the vector to set
   * @param error The error array of the vector
   * @param value The value to set
   */
  public void setValue(int index, double[] error, double value) {
    error[index] = this.get(index);
    set(index, value);
    setFunctionError(error, index);
  }

  private void setFunctionError(double[] error, int index) {
    double functionValue = get(index);
    error[index] = abs(error[index] - functionValue) / functionValue;
  }

  public double unsafe_get(int i) {
    return unsafe_get(0, i);
  }
}
