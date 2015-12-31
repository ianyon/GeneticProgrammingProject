/*
 * FitnessClassic.java
 *
 * Created on 13 de octubre de 2005, 08:52 PM
 *
 * Copyright (C) 2005, 2006 Neven Boric <nboric@gmail.com>
 *
 * This file is part of GPalta.
 *
 * GPalta is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * GPalta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GPalta; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package gpalta.core;

import java.io.*;

/**
 * Implementation of the classic fitness used in GP (MSE)
 *
 * @author neven
 */
public class FitnessClassic implements Fitness
{

    private SingleOutput desiredOutputs;
    private double[] weights;

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
    }

    public void calculate(Output outputs, Individual ind, ProblemData problemData)
    {
        double error = 0;
        for (int i = 0; i < problemData.nSamples; i++)
        {
            error += Math.pow(((SingleOutput)outputs).x[i] - desiredOutputs.x[i], 2);
        }
        ind.setFitness(1 / (1 + Math.sqrt(error)));
    }

    public Output getProcessedOutput(Output raw, ProblemData problemData)
    {
        return raw;
    }

}
