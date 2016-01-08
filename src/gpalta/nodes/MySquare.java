package gpalta.nodes;

import gpalta.core.ProblemData;


/**
 * Created by Ian on 04/01/2016.
 * <p>
 * Protected square function.
 * MySquare(X1) returns 0 if X1^2 is NaN or Inf, or has imaginary part, otherwise returns X1^2.
 */
public class MySquare extends Node {
    public double eval(ProblemData problemData) {
        double result = Math.pow(getKid(0).eval(problemData), 2);
        if (Double.isNaN(result) || Double.isInfinite(result) //TODO || !Double.isReal(result)
                )
            return 0;
        else
            return result;
    }

    public void evalVectInternal(double[] outVect, double[][] kidsOutput, ProblemData problemData) {
        for (int wSample = 0; wSample < outVect.length; wSample++) {
            double result = Math.pow(kidsOutput[0][wSample], 2);
            if (Double.isNaN(result) || Double.isInfinite(result) //TODO || !Double.isReal(result)
                    )
                outVect[wSample] = 0;
            else
                outVect[wSample] = result;
        }
    }

    public int nKids() {
        return 1;
    }

    public String name() {
        return "my_sq";
    }
}

