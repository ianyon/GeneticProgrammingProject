package cl.ian;

import org.ejml.data.RealMatrix64F;

/**
 * Created by Ian on 01-03-2016.
 */
public interface VectorWrapper extends RealMatrix64F {
  double unsafe_get(int i);

  void unsafe_set(int i, double value);

  void setValue(int i, double[] errorArray, double value);

  double set(int i, double value);

  double get(int index);
}
