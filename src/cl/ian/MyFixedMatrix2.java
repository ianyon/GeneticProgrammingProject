package cl.ian;

import org.ejml.data.FixedMatrix2_64F;
import org.ejml.data.Matrix;

/**
 * Created by Ian on 01-03-2016.
 */
public class MyFixedMatrix2 extends FixedMatrix2_64F implements VectorWrapper {

  public MyFixedMatrix2(double value) {
    a1 = value;
    a2 = value;
  }

  public MyFixedMatrix2(double value, double value1) {
    a1 = value;
    a2 = value1;
  }

  public MyFixedMatrix2() {
  }

  @Override
  public <T extends Matrix> T copy() {
    return (T) new MyFixedMatrix2(a1, a2);
  }

  @Override
  public double unsafe_get(int i) {
    switch (i) {
      case 0:
        return a1;
      case 1:
        return a2;
      default:
        assert i < 2 : "Tried to access position " + i + " of MyFixedMatrix2";
        System.exit(-2);
    }
    // Unreachable. Used to calm statics checkers
    return 0;
  }

  @Override
  public void setValue(int i, double[] errorArray, double value) {
    GeneralModelEvaluator.setValue(this, i, errorArray, value);
  }

  public void setValue(int i, MyFixedMatrix2 errorArray, double value) {
    GeneralModelEvaluator.setValue(this, i, errorArray, value);
  }

  public double setValue(int i, double value) {
    return GeneralModelEvaluator.setValue(this, i, value);
  }

  @Override
  public double set(int i, double value) {
    return 0;
  }

  public void unsafe_set(int i, double val) {
    unsafe_set(0, i, val);
  }

  @Override
  public double get(int index) {
    return 0;
  }

  public double max() {
    return Math.max(a1, a2);
  }
}
