package gpalta.nodes;

import gpalta.core.*;


/**
 * Created by Ian on 04/01/2016.
 */
public class MyDivide extends Node
{

    public double eval(ProblemData problemData)
    {
        double resultKid1 = getKid(1).eval(problemData);
        if (resultKid1 == 0)
            return getKid(0).eval(problemData);
        else
            return (getKid(0).eval(problemData) / resultKid1);
    }

    public void evalVectInternal(double[] outVect, double[][] kidsOutput, ProblemData problemData)
    {
        for (int wSample=0; wSample<outVect.length; wSample++)
        {
            if (kidsOutput[1][wSample] == 0)
                outVect[wSample] = kidsOutput[0][wSample];
            else
                outVect[wSample] = kidsOutput[0][wSample] / kidsOutput[1][wSample];
        }
    }

    public int nKids()
    {
        return 2;
    }

    public String name()
    {
        return "divide";
    }

}
