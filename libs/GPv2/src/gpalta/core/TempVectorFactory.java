/*
 * TempVectorFactory.java
 *
 * Created on 11 de noviembre de 2005, 04:35 PM
 *
 * Copyright (C) 2005 Neven Boric <nboric@gmail.com>
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

import java.util.*;

/**
 * Hold arrays in which the nodes will be evaluated (when using evalVect). This is done to avoid
 * allocating memory every time a node is evaluated, greatly improving execution speed with large
 * data sets
 *
 * @author neven
 */
public class TempVectorFactory
{

    private List<double[]> outputs;
    private int currentOutput;
    private int vectorSize;

    /**
     * Creates a new instance of TempVectorFactory
     *
     * @param vectorSize The size of each array (number of samples)
     */
    public TempVectorFactory(int vectorSize)
    {
        this.vectorSize = vectorSize;
        outputs = new ArrayList<double[]>(0);
        currentOutput = -1;
    }

    /**
     * Get a new array. If previously allocated outputs are available (not already in use), it will
     * return one of them. Else, it will allocate memory for a new one
     */
    public synchronized double[] get()
    {
        currentOutput++;
        if (currentOutput == outputs.size())
        {
            outputs.add(new double[vectorSize]);
        }
        return outputs.get(currentOutput);
    }

    /**
     * Tell that the latest requested array is no longer needed by the Node. The Node that requested
     * an array must release it inmediately after stopping using it, or every time a node of that
     * kind is evaluated, an output will be created and never released, leading to HUGE MEMORY
     * LEAKS.
     */
    public synchronized void release()
    {
        currentOutput--;
    }

}
