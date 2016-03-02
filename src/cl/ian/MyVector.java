package cl.ian;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

import static java.lang.Math.abs;

/**
 * Created by ian on 6/16/15.
 */
public class MyVector extends DenseMatrix64F implements VectorWrapper {
  public MyVector(int cols, double value) {
    super(1, cols);
    CommonOps.fill(this, value);
  }

  public MyVector(int cols) {
    super(1, cols);
  }

  public void setValue(int index, double[] error, double value) {
    GeneralModelEvaluator.setValue(this, index, error, value);
  }

  public double unsafe_get(int i) {
    return unsafe_get(0, i);
  }

  @Override
  public void unsafe_set(int i, double value) {
    unsafe_set(0, i, value);
  }

}
