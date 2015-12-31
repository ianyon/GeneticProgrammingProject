/*
 * FitnessSNE.java
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
public class FitnessSNE implements Fitness
{
   
    private SingleOutput desiredOutputs;
    private double[] weights;
    private double sigma = 0.5;
    private double[][] dt_orig;
    private double[] dist_orig;
    private double[][] mtx_dist_orig;
    private double[][] p;

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
        double[] dist_orig = new double[dt_orig.length];
        double[][] mtx_dist_orig = new double[problemData.nSamples][];
        double[] den_p = new double[problemData.nSamples];
        dist_orig     = Common.pdist(dt_orig, true);
        mtx_dist_orig = Common.squareform(dist_orig);
        for (int i = 0; i < problemData.nSamples; i++)
        {
            for (int j = 0; j < problemData.nSamples; j++)
            {
                if (j != i)
                {
                    den_p[i] += Math.exp(-Math.pow(mtx_dist_orig[i][j], 2)/(2*Math.pow(sigma, 2)));
                }
            }
        }
        double[][] p = new double[problemData.nSamples][problemData.nSamples];
        for (int i = 0; i < problemData.nSamples; i++)
        {
            for (int j = 0; j < problemData.nSamples; j++)
            {
                p[i][j] = Math.exp(-mtx_dist_orig[i][j]/(2*Math.pow(sigma, 2))) / den_p[i];
            }
        }
        this.dist_orig     = dist_orig;
        this.dt_orig       = dt_orig;
        this.mtx_dist_orig = mtx_dist_orig;
        this.p             = p;
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
        double[] den_q = new double[problemData.nSamples];
        dist_proy = Common.pdist(dt_proy, true);
        mtx_dist_proy = Common.squareform(dist_proy);
        for (int i = 0; i < problemData.nSamples; i++)
        {
            for (int j = 0; j < problemData.nSamples; j++)
            {
                if (j != i)
                {
                    den_q[i] += Math.exp(-mtx_dist_proy[i][j]);
                }
            }
        }
        
        // Stochastic Neighbor Embedding
        
        double error = 0;
        double q;
        for (int i = 0; i < problemData.nSamples; i++)
        {
            for (int j = 0; j < problemData.nSamples; j++)
            {
                q = Math.exp(-mtx_dist_proy[i][j]) / den_q[i];
                error += (p[i][j])*Math.log((p[i][j])/(q));
            }
        }
        if (Double.isInfinite(error) || Double.isNaN(error) || (error < 0)) 
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
