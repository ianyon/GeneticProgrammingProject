package cl.ian;

import org.ejml.data.DenseMatrix64F;

/**
 * Created by Ian on 29/12/2015.
 * <p>
 * Utility class to perform Matlab like operations on common java variables
 */
public class MatlabUtils {

    static public DenseMatrix64F range(double init, double step, double end) {
        final int numberOfElements = (int) (Math.floor((end - init) / step) + 1);

        DenseMatrix64F result = new DenseMatrix64F(1, numberOfElements);

        double accumulated = init;
        for (int i = 0; i < numberOfElements; i++, accumulated += step) {
            result.set(i, accumulated);
        }

        assert accumulated > end;
        assert accumulated - end < step;

        return result;
    }

    static public DenseMatrix64F cos(DenseMatrix64F x) {
        final int size = x.getNumElements();
        DenseMatrix64F result = new DenseMatrix64F(1, size);

        for (int i = 0; i < size; i++)
            result.set(i, Math.cos(x.get(i)));

        return result;
    }

    static public DenseMatrix64F sin(DenseMatrix64F x) {
        final int size = x.getNumElements();
        DenseMatrix64F result = new DenseMatrix64F(1, size);

        for (int i = 0; i < size; i++)
            result.set(i, Math.sin(x.get(i)));

        return result;
    }

    static public double[] substract(double[] a, double[] b) {
        assert a.length == b.length;
        double[] result = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] - b[i];
        }
        return result;
    }

    static public double[] square(double[] a) {
        double[] result = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] * a[i];
        }
        return result;
    }

    static public double mean(double[] a) {
        double result = 0;
        for (int i = 0; i < a.length; i++) {
            result += a[i];
        }
        return result / a.length;
    }

    public static double max(double... array) {
        double max = array[0];
        for (int i = 1; i < array.length; i++)
            max = Math.max(max, array[i]);
        return max;
    }
}
