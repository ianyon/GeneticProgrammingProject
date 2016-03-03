package cl.ian;

import org.ejml.data.Matrix;
import org.ejml.data.RealMatrix64F;

/**
 * Created by Ian on 29-02-2016.
 */
public class SingleVector implements VectorWrapper {
  private double value;

  public SingleVector(double value) {
    this.value = value;
  }

  public SingleVector() {
  }

  @Override
  public double get(int row, int col) {
    return value;
  }

  @Override
  public double unsafe_get(int row, int col) {
    return value;
  }

  @Override
  public void set(int row, int col, double val) {
    value = val;
  }

  @Override
  public void unsafe_set(int row, int col, double val) {
    value = val;
  }

  @Override
  public int getNumElements() {
    return 1;
  }

  @Override
  public int getNumRows() {
    return 1;
  }

  @Override
  public int getNumCols() {
    return 1;
  }

  @Override
  public <T extends Matrix> T copy() {
    return (T) new SingleVector(value);
  }

  @Override
  public void set(Matrix original) {
    RealMatrix64F m = (RealMatrix64F) original;

    if (m.getNumCols() == 1 && m.getNumRows() == 1) {
      value = m.get(0, 0);
    } else {
      throw new IllegalArgumentException("Incompatible shape");
    }
  }

  @Override
  public void print() {
    System.out.println(String.format("%6.3f", value));
  }

  @Override
  public double unsafe_get(int i) {
    return unsafe_get(0, i);
  }

  @Override
  public void unsafe_set(int i, double value) {
    this.value=value;
  }

  @Override
  public void setValue(int i, double[] errorArray, double value) {
    GeneralModelEvaluator.setValue(this, i, errorArray, value);
  }

  public double setValue(int i, double value) {
    return GeneralModelEvaluator.setValue(this,i, value);
  }

  @Override
  public double set(int i, double value) {
    this.value = value;
    return value;
  }

  @Override
  public double get(int index) {
    return value;
  }
}
