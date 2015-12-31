/*
 * SingleOutput.java
 *
 * Created on 03-01-2007, 11:39:17 PM
 *
 * Copyright (C) 2007 Neven Boric <nboric@gmail.com>
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

/**
 * Implements an Output of dimension 1. The actual value is accessed through the x field
 */
public class SingleOutput extends Output
{
    /**
     * The actual value of the Output. It stores the output for all samples if using vectorial
     * evaluation. Otherwise, x will be of length 1 and will store a single value.
     */
    public final double[] x;

    public SingleOutput(int nSamples)
    {
        x = new double[nSamples];
    }

    public int getDim()
    {
        return 1;
    }

    public void store(double[] vector)
    {
        System.arraycopy(vector, 0, x, 0, x.length);
    }
}
