/*
 * Divide.java
 *
 * Created on 11 de noviembre de 2005, 07:47 PM
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

package gpalta.nodes;

import gpalta.core.*;

/**
 * @author neven
 */
public class Divide extends Node
{

    public double eval(ProblemData problemData)
    {
        double resultKid1 = getKid(1).eval(problemData);
        if (resultKid1 == 0)
            return 1;
        else
            return (getKid(0).eval(problemData) / resultKid1);
    }

    public void evalVectInternal(double[] outVect, double[][] kidsOutput, ProblemData problemData)
    {
        for (int wSample=0; wSample<outVect.length; wSample++)
        {
            if (kidsOutput[1][wSample] == 0)
                outVect[wSample] = 1;
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
