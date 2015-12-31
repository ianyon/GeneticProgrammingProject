/*
 * RealDataHolder.java
 *
 * Created on 12 de mayo de 2005, 02:43 AM
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
 * Holds the problem's data and provides methods to access it.
 *
 * @author neven
 */
public class ProblemData
{

    /* Every row in data correponds to all the samples for a variable.
    * This is done to be able to return all the samples for a certain variable
    * as a vector.
    */
    private double[][] data;
    private double[][] dataT;
    private double[][] ranges;
    public int nSamples;
    private int currentSample;
    public int nVars;
    private double e1;

    /**
     * Get the current value for a variable
     * @param whichVar The variable (between 1 and nVars)
     * @return The current value
     */
    public double getData(int whichVar)
    {
        return data[whichVar - 1][currentSample];
    }

    /**
     * Get all values (in all samples) for a variable
     * @param whichVar The variable (between 1 and nVars)
     * @return an array with the values the variable takes for every sample
     */
    public double[] getDataVect(int whichVar)
    {
        return data[whichVar - 1];
    }

    /**
     * Initialize the data from a file
     */
    public ProblemData(String fileName)
    {
        try
        {
            dataT = Common.readFromFile(fileName, "\\s+");
            data = Common.transpose(dataT);
            nVars = data.length;
            nSamples = data[0].length;
            calcRange();
            Logger.log("Finished reading " + nSamples + " samples from file \"" + fileName + "\"");
        }

        /* TODO: This exception shouldn't be caught here, but thrown to the
         * evolution and then to the controller
         */
        catch (IOException e)
        {
            Logger.log(e);
        }
    }

    /**
     * Initialize the data from the given matrix. Every row is a variable and
     * every column is a sample
     */
    public ProblemData(double[][] data)
    {
        this.data = data;
        dataT = Common.transpose(data);
        nVars = data.length;
        nSamples = data[0].length;
        currentSample = 0;
        calcRange();
    }

    private void calcRange()
    {
        ranges = new double[nVars][3];
        double[] mean = new double[nVars];
        for (int wVar=0; wVar<nVars; wVar++)
        {
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;
            for (int wSample=0; wSample<nSamples; wSample++)
            {
                min = Math.min(min, data[wVar][wSample]);
                max = Math.max(max, data[wVar][wSample]);
            }
            ranges[wVar][0] = min;
            ranges[wVar][1] = max;
            ranges[wVar][2] = max-min;
            for (int wSample=0; wSample<nSamples; wSample++)
            {
                mean[wVar] += data[wVar][wSample];
            }
            mean[wVar] /= nSamples;
        }
        e1 = 0;
        for (int wSample=0; wSample<nSamples; wSample++)
        {
            e1 += Common.dist2(mean, dataT[wSample]);
        }

    }

    /**
     * reset() must be called every time a new tree is being evaluated
     * (when using eval() instead of evalVect() )
     */
    public void reset()
    {
        currentSample = 0;
    }

    /**
     * update() must be called every time a new sample is required
     * (when using eval() instead of evalVect() )
     */
    public void update()
    {
        currentSample++;
    }

    /**
     * Get the minimum value of a certain variable
     * @param whichVar The variable (between 1 and nVars)
     */
    public double getMin(int whichVar)
    {
        return ranges[whichVar-1][0];
    }

    /**
     * Get the maximum value of a certain variable
     * @param whichVar The variable (between 1 and nVars)
     */
    public double getMax(int whichVar)
    {
        return ranges[whichVar-1][1];
    }

    /**
     * Get the range (maximum - minimum) of a certain variable
     * @param wVar The variable (between 1 and nVars)
     */
    public double getRange(int wVar)
    {
        return ranges[wVar-1][2];
    }

    /**
     * Get the current sample (all variables)
     * @return an array with the whole sample
     */
    public double[] getCurrentSample()
    {
        return dataT[currentSample];
    }

    /**
     * Get a sample (all variables)
     * @param wSample Which sample (between 0 and nSamples)
     * @return an array with the whole sample
     */
    public double[] getSample(int wSample)
    {
        return dataT[wSample];
    }

    public double getE1()
    {
        return e1;
    }

}
