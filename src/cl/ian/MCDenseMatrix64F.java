package cl.ian;

import org.ejml.data.CDenseMatrix64F;
import org.ejml.data.Complex64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CCommonOps;

import java.util.Arrays;

/**
 * Created by ian on 8/30/15.
 */
public class MCDenseMatrix64F extends CDenseMatrix64F {
    public MCDenseMatrix64F(int rows, int cols, double value) {
        super(rows, cols);
        CCommonOps.fill(this,value,0);
    }

    public void set (int row, int col, Complex64F value){
        this.set(row, col, value.real, value.imaginary);
    }

    public void set (int row, int col, MCDenseMatrix64F matrix){
        Complex64F aux = new Complex64F();
        matrix.get(row, col, aux);
        this.set(row, col, aux.real, aux.imaginary);
    }

    public Complex64F get(int row, int col){
        Complex64F result = new Complex64F();
        this.get(row, col, result);
        return result;
    }

    public static Complex64F multiply(int row, int col, double value, MCDenseMatrix64F matrix) {
        return matrix.get(row,col).times(new Complex64F(value,0));
    }
}
