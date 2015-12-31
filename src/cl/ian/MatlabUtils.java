package cl.ian;

import org.ejml.data.DenseMatrix64F;

/**
 * Created by Ian on 29/12/2015.
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

}
