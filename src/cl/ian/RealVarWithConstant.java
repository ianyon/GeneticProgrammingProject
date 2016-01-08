
package gpalta.nodes;

import gpalta.core.*;

/**
 * Real variable terminal
 * Created by Ian on 05/01/2016.
 */
public class RealVarWithConstant extends Node
{
    public int whichVar;
    public double realValue;

    /**
     * Creates a new instance of RealVar
     */
    public RealVarWithConstant(int whichVar)
    {
        this.whichVar = whichVar;
    }

    public double eval(ProblemData problemData)
    {
        return problemData.getData(whichVar);
    }

    public void evalVectInternal(double[] outVect, double[][] kidsOutput, ProblemData problemData)
    {
        System.arraycopy(problemData.getDataVect(whichVar), 0, outVect, 0, problemData.nSamples);
    }

    public int nKids()
    {
        return 0;
    }

    public String name()
    {
        return ("X" + whichVar);
    }

}