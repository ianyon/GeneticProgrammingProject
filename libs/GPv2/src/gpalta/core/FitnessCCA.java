/*
 * FitnessCCA.java
 *
 * Created on 11 de agosto de 2007, 04:00 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package gpalta.core;
import gpalta.multitree.*;
import java.io.*;

/**
 *
 * @author Jorge
 */
public class FitnessCCA implements Fitness
{
   
    private SingleOutput desiredOutputs;
    private double[] weights;
    private double[][] dt_orig;
    private double[] dist_orig;
    private double[][] mtx_dist_orig;

    public void init(Config config, ProblemData problemData, String fileName)
    {
        try
        {
            double[][] matrix = Common.transpose(Common.readFromFile(fileName, "\\s+"));
            desiredOutputs = new SingleOutput(matrix[0].length);
            desiredOutputs.store(matrix[0]);
            boolean useWeight = false;
            if (matrix.length == 2)
            {
                useWeight = true;
                weights = matrix[1];
            }

            Logger.log("Using classic (generic) fitness");
            Logger.log("Fitness initialized from file \"" + fileName + "\"");
            if (useWeight)
            {
                Logger.log("\t Using weight data");
            }
            Logger.log("\t Samples:              " + problemData.nSamples);
        }

        /* TODO: This exception shouldn't be caught here, but thrown to the
         * evolution and then to the controller
         */
        catch (IOException e)
        {
            Logger.log(e);
        }
    }

    public void init(Config config, ProblemData problemData, Output desiredOutputs, double[] weights)
    {
        this.desiredOutputs = (SingleOutput)desiredOutputs;
        this.weights = weights;
        // Matriz de datos y distancias originales
        double[][] dt_orig = new double[problemData.nVars][];
        for (int j = 0; j < problemData.nVars; j++)
        {
            dt_orig[j] = problemData.getDataVect(j+1);
        }
        dist_orig     = Common.pdist(dt_orig, true);
        mtx_dist_orig = Common.squareform(dist_orig);
        this.dist_orig     = dist_orig;
        this.dt_orig       = dt_orig;
        this.mtx_dist_orig = mtx_dist_orig;
    }

    public void calculate(Output outputs, Individual ind, ProblemData problemData)
    {
        // Matrices de datos de la forma [Vars x Sample]
        // Matrices de distancias de la forma [Sampes x Samples]
        
        // Matriz de datos y  distancias proyectados
        double[][] dt_proy = new double[outputs.getDim()][];
        for (int j = 0; j < outputs.getDim(); j++)
        {
            dt_proy[j] = ((MultiOutput)outputs).getArrayCopy(j);
        }
        double[] dist_proy = new double[dt_proy.length];
        double[][] mtx_dist_proy = new double[problemData.nSamples][];
        dist_proy = Common.pdist(dt_proy, true);
        mtx_dist_proy = Common.squareform(dist_proy);
        
        
        // Curvilinear Component Analysis
        double error = 0;
        double fij;
        for (int i = 0; i < problemData.nSamples; i++)
        {
            for (int j = 0; j < problemData.nSamples; j++)
            {
                if (i != j)
                {
                    fij = 1/(0.1 + mtx_dist_proy[i][j]);
                    error += 0.5*Math.pow(mtx_dist_orig[i][j] - mtx_dist_proy[i][j], 2)*fij;
                }
            }
        }
        if (Double.isInfinite(error) || Double.isNaN(error)) 
        {
            error = Double.POSITIVE_INFINITY;
        }
        ind.setFitness(1 / (1 + error));
    }

    public Output getProcessedOutput(Output raw, ProblemData problemData)
    {
        return raw;
    } 
}
