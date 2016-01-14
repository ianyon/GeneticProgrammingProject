package cl.ian;

import org.ejml.data.Complex64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

/**
 * Created by ian on 6/17/15.
 */
public class CustomOperators {

    public static double plus(double x, double y) {
        return x + y;
    }

    public static Complex64F plus(Complex64F x, double y) {
        return x.plus(new Complex64F(y,0));
    }

    public static Complex64F plus(double x, Complex64F y) {
        return y.plus(new Complex64F(x,0));
    }

    public static Complex64F plus(Complex64F x, Complex64F y) {
        return x.plus(y);
    }

    public static double minus(double x, double y) { return x - y; }

    public static Complex64F minus(Complex64F x, double y) {
        return x.minus(new Complex64F(y,0));
    }

    public static Complex64F minus(double x, Complex64F y) {
        return y.minus(new Complex64F(x, 0));
    }

    public static Complex64F minus(Complex64F x, Complex64F y) {
        return x.minus(y);
    }

    public static double times(double x, double y) {
        return x * y;
    }

    public static Complex64F times(Complex64F x, double y) {
        return x.times(new Complex64F(y, 0));
    }

    public static Complex64F times(double x, Complex64F y) {
        return y.times(new Complex64F(x, 0));
    }

    public static Complex64F times(Complex64F x, Complex64F y) {
        return x.times(y);
    }

    /**
     * %MYDIVIDE    Protected division function.
     * %   MYDIVIDE(X1,X2) returns X1 if X2==0 and X1/X2 otherwise.
     * %
     * %   Input arguments:
     * %      X1 - the numerator of the division (double)
     * %      X2 - the denominator of the division (double)
     * %   Output arguments:
     * %      Y - the division of X1 by X2, or X1 (double)
     * %
     * %   See also MYPOWER, MYSQRT, MYLOG, MYLOG2, MYLOG10
     *
     * @param x1
     * @param x2
     * @return
     */
    public static double myDivide(double x1, double x2) {
        // fill the cells where x2=0 (make them x1):
        if (x2 != 0) return x1 / x2;
        else return x1;
    }

    public static Complex64F divide(Complex64F x, double y) {
        if (y != 0) return x.divide(new Complex64F(y, 0));
        else return x;
    }

    public static Complex64F divide(double x, Complex64F y) {
        if (y.real != 0 && y.imaginary !=0) return y.divide(new Complex64F(x, 0));
        else return new Complex64F(x,0);
    }

    public static Complex64F divide(Complex64F x, Complex64F y) {
        if (y.real != 0 && y.imaginary !=0) return x.divide(y);
        else return x;
    }

    public static double q_myPower2(double x1) {
        return q_myPower(x1, 2);
    }

    public static double myPower(double x1, double x2) {
        //y(find(imag(y)))=0;
        double value = Math.pow(x1, x2);
        // This happens if the result is NaN, Infinite or not real
        if (Double.isNaN(value) || Double.isInfinite(value))
            return 0;
        else
            return value;
    }

    /**
     * * function y=mypower(x1,x2)
     * %MYPOWER    Protected power function.
     * %   MYPOWER(X1,X2) returns 0 if X1^X2 is NaN or Inf,
     * %   or has imaginary part, otherwise returns X1^X2.
     * %
     * %   Input arguments:
     * %      X1 - the base of the power function (double)
     * %      X2 - the exponent of the power function (double)
     * %   Output arguments:
     * %      Y - the power X1^X2, or 0 (double)
     * %
     * %   See also MYDIVIDE, MYSQRT, MYLOG, MYLOG2, MYLOG10
     * %
     * %   Copyright (C) 2003-2007 Sara Silva (sara@dei.uc.pt)
     * %   This file is part of the GPLAB Toolbox
     *
     * @param x1
     * @return
     */
    public static double q_myPower(double x1, double exp) {
        return myPower(x1,exp);
    }


    public static double q_myPower3(double x1) {
        return q_myPower(x1, 3);
    }


    public static double cpower(double x, double y) {
        return Math.pow(x, y);
    }

    /**
     * MYSQRT    Protected SQRT function.
     * MYSQRT(X) returns zero if X<=0 and SQRT(X) otherwise.
     * See also MYPOWER, MYDIVIDE, MYLOG, MYLOG2, MYLOG10
     *
     * @param x the number to square root
     * @return the square root of X, or zero (double)
     */
    public static double mysqrt(double x) {
        //fill the cells where x<0 (make them 0):
        if (x >= 0)
            return Math.sqrt(x);
        else
            return 0;
    }


    /**
     * MYSQRT    Protected SQRT function.
     * MYSQRT(X) returns zero if X<=0 and SQRT(X) otherwise.
     * See also MYPOWER, MYDIVIDE, MYLOG, MYLOG2, MYLOG10
     *
     * @param x the number to square root
     * @return the square root of X, or zero (double)
     */
    public static DenseMatrix64F mysqrt(DenseMatrix64F x) {
        int size = x.getNumElements();

        //fill the cells where x<0 (make them 0):
        DenseMatrix64F y = new DenseMatrix64F(size);

        // Only apply to elements bigger or equals to 0
        for (int i = 0; i < size; i++) {
            double value = x.get(i);
            if (value >= 0)
                y.set(i, Math.sqrt(value));
        }

        return y;
    }

    /**
     * %MYDIVIDE    Protected division function.
     * %   MYDIVIDE(X1,X2) returns X1 if X2==0 and X1/X2 otherwise.
     * %
     * %   Input arguments:
     * %      X1 - the numerator of the division (double)
     * %      X2 - the denominator of the division (double)
     * %   Output arguments:
     * %      Y - the division of X1 by X2, or X1 (double)
     * %
     * %   See also MYPOWER, MYSQRT, MYLOG, MYLOG2, MYLOG10
     *
     * @param x1
     * @param x2
     * @return
     */
    public static DenseMatrix64F myDivide(DenseMatrix64F x1, DenseMatrix64F x2) {
        // first make both matrices the same size:
        int m = Math.max(x1.getNumElements(), x2.getNumElements());
        if (x1.getNumElements() < m)
            x1.reshape(m, 1);
        if (x2.getNumElements() < m)
            x2.reshape(m, 1);

        // fill the cells where x2=0 (make them x1):
        DenseMatrix64F y = new DenseMatrix64F(x1);

        // fill the remaining cells with the result of the division:
        int size = x1.getNumElements();

        // Only apply to elements bigger or equals to 0
        for (int i = 0; i < size; i++) {
            double value = x2.get(i);
            if (value != 0)
                y.set(i, x1.get(i) / value);
        }
        return y;
    }

    /**
     * * function y=mypower(x1,x2)
     * %MYPOWER    Protected power function.
     * %   MYPOWER(X1,X2) returns 0 if X1^X2 is NaN or Inf,
     * %   or has imaginary part, otherwise returns X1^X2.
     * %
     * %   Input arguments:
     * %      X1 - the base of the power function (double)
     * %      X2 - the exponent of the power function (double)
     * %   Output arguments:
     * %      Y - the power X1^X2, or 0 (double)
     * %
     * %   See also MYDIVIDE, MYSQRT, MYLOG, MYLOG2, MYLOG10
     *
     * @param x1
     * @param x2
     * @return
     */
    public static DenseMatrix64F myPower(DenseMatrix64F x1, DenseMatrix64F x2) {
        int size = x1.getNumElements();
        DenseMatrix64F y = new DenseMatrix64F(size);

        //y(find(isnan(y) | isinf(y) | imag(y)))=0;
        for (int i = 0; i < size; i++) {
            double value = Math.pow(x1.get(i), x2.get(i));
            // This happens if the result is NaN, Infinite or not real
            if (Double.isNaN(value) || Double.isInfinite(value))
                y.set(i, 0);
            else
                y.set(i, value);
        }

        return y;
    }

    /**
     * * function y=mypower(x1,x2)
     * %MYPOWER    Protected power function.
     * %   MYPOWER(X1,X2) returns 0 if X1^X2 is NaN or Inf,
     * %   or has imaginary part, otherwise returns X1^X2.
     * %
     * %   Input arguments:
     * %      X1 - the base of the power function (double)
     * %      X2 - the exponent of the power function (double)
     * %   Output arguments:
     * %      Y - the power X1^X2, or 0 (double)
     * %
     * %   See also MYDIVIDE, MYSQRT, MYLOG, MYLOG2, MYLOG10
     * %
     * %   Copyright (C) 2003-2007 Sara Silva (sara@dei.uc.pt)
     * %   This file is part of the GPLAB Toolbox
     *
     * @param x1
     * @return
     */
    public static DenseMatrix64F q_myPower(DenseMatrix64F x1, double exp) {
        int size = x1.getNumElements();
        DenseMatrix64F y = new DenseMatrix64F(size);

        //y(find(isnan(y) | isinf(y) | imag(y)))=0;
        for (int i = 0; i < size; i++) {
            double value = Math.pow(x1.get(i), exp);
            // This happens if the result is NaN, Infinite or not real
            if (Double.isNaN(value) || Double.isInfinite(value))
                y.set(i, 0);
            else
                y.set(i, value);
        }

        return y;
    }

    public static DenseMatrix64F q_myPower2(DenseMatrix64F x1) {
        return q_myPower(x1, 2);
    }

    public static DenseMatrix64F q_myPower3(DenseMatrix64F x1) {
        return q_myPower(x1, 3);
    }

    /**
     * * function y=kozasqrt(x)
     * %KOZASQRT    Koza protected SQRT function.
     * %   KOZASQRT(X) returns SQRT(ABS(X)).
     * %
     * %   Input arguments:
     * %      X - the number to square root (double)
     * %   Output arguments:
     * %      Y - the square root of X, or zero (double)
     * %
     * %   See also MYSQRT, MYPOWER, MYDIVIDE, MYLOG, MYLOG2, MYLOG10
     *
     * @param x1
     * @return
     */
    public static DenseMatrix64F kozasqrt(DenseMatrix64F x1) {
        final int size = x1.getNumElements();
        DenseMatrix64F y = new DenseMatrix64F(size);

        for (int i = 0; i < size; i++) {
            y.set(i, Math.abs(x1.get(i)));
        }

        return y;
    }

    /**
     * * function y=kozadivide(x1,x2)
     * %KOZADIVIDE    Koza protected division function.
     * %   KOZADIVIDE(X1,X2) returns 1 if X2==0 and X1/X2 otherwise.
     * %
     * %   Input arguments:
     * %      X1 - the numerator of the division (double)
     * %      X2 - the denominator of the division (double)
     * %   Output arguments:
     * %      Y - the division of X1 by X2, or 1 (double)
     * %
     * %   See also MYDIVIDE, MYPOWER, MYSQRT, MYLOG, MYLOG2, MYLOG10
     *
     * @param x1
     * @param x2
     * @return
     */
    public static DenseMatrix64F kozadivide(DenseMatrix64F x1, DenseMatrix64F x2) {

        int x1Cols = x1.getNumCols(),
                x2Cols = x2.getNumCols(),
                x1Rows = x1.getNumRows(),
                x2Rows = x2.getNumRows();

        // first make both matrices the same size:
        if (x1Cols != x2Cols || x1Rows != x2Rows) {
            DenseMatrix64F newX1 = new DenseMatrix64F(x1Rows * x2Rows, x1Cols * x2Cols);
            DenseMatrix64F newX2 = new DenseMatrix64F(x1Rows * x2Rows, x1Cols * x2Cols);

            // nx1 = repmat(x1, size(x2));
            CommonOps.insert(x1, newX1, 0, 0);
            if (x1Cols < newX1.getNumCols()) {
                CommonOps.insert(x1, newX1, 0, x1Cols);
                if (x1Rows < newX1.getNumRows())
                    CommonOps.insert(x1, newX1, x1Rows, x1Cols);
            }
            if (x1Rows < newX1.getNumRows()) {
                CommonOps.insert(x1, newX1, x1Rows, 0);
                if (x1Cols < newX1.getNumCols())
                    CommonOps.insert(x1, newX1, x1Rows, x1Cols);
            }

            // nx2=repmat(x2,size(x1));
            CommonOps.insert(x2, newX2, 0, 0);
            if (x2Cols < newX2.getNumCols()) {
                CommonOps.insert(x2, newX2, 0, x2Cols);
                if (x2Rows < newX2.getNumRows())
                    CommonOps.insert(x2, newX2, x2Rows, x2Cols);
            }
            if (x2Rows < newX2.getNumRows()) {
                CommonOps.insert(x2, newX2, x2Rows, 0);
                if (x2Cols < newX2.getNumCols())
                    CommonOps.insert(x2, newX2, x2Rows, x2Cols);
            }
            x1 = newX1;
            x2 = newX2;
        }

        int size = x1.getNumElements();

        // fill the cells where x2=0 (make them 1):
        DenseMatrix64F y = new DenseMatrix64F(size);
        CommonOps.fill(y, 1);

        // fill the remaining cells with the result of the division:
        for (int i = 0; i < size; i++) {
            double value = x2.get(i);
            if (value != 0)
                y.set(i, x1.get(i) / value);
        }
        return y;
    }

    public static DenseMatrix64F mylogCustom(DenseMatrix64F x1, double base) {
        int size = x1.getNumElements();

        // fill the cells where x=0 (make them 0):
        DenseMatrix64F y = new DenseMatrix64F(size);

        // fill the remaining cells with the result of the logarithm:
        for (int i = 0; i < size; i++) {
            double value = x1.get(i);
            if (value != 0) {
                if (base == 2)
                    y.set(i, Math.log(Math.abs(value)) / Math.log(2));
                else if (base == 10)
                    y.set(i, Math.log10(Math.abs(value)));
                else if (base == Math.E)
                    y.set(i, Math.log(Math.abs(value)));
            }
        }
        return y;
    }

    public static DenseMatrix64F mylog2(DenseMatrix64F x1) {
        return mylogCustom(x1, 2);
    }
    public static DenseMatrix64F mylog(DenseMatrix64F x1) {
        return mylogCustom(x1, Math.E);
    }

    public static DenseMatrix64F mylog10(DenseMatrix64F x1) {
        return mylogCustom(x1, 10);
    }

}
