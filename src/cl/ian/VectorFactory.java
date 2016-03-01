package cl.ian;

import org.ejml.data.*;

import static java.lang.Math.abs;

/**
 * Created by Ian on 29-02-2016.
 */
public class VectorFactory {
  public static VectorWrapper buildFilledVector(int cols, double value) {
    switch (cols) {
      case 1:
        return new SingleVector(value);
      case 2:
        return new MyFixedMatrix2(value, value);
      // Implement these when needed
      case 3:
      case 4:
      case 5:
      case 6:
      default:
        return new OnesVector(cols, value);
    }
  }

  public static VectorWrapper buildVector(int rows, int cols) {
    int maxSize = Math.max(rows, cols);
    switch (maxSize) {
      case 1:
        return new SingleVector();
      case 2:
        return new MyFixedMatrix2();
      // Implement these when needed
      case 3:
      case 4:
      case 5:
      case 6:
      default:
        return new OnesVector(maxSize);
    }
  }

}
