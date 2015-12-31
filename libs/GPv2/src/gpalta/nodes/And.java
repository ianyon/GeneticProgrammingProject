/*
 * And.java
 *
 * Created on 10 de mayo de 2005, 11:33 PM
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
public class And extends Node
{

    public double eval(ProblemData problemData)
    {
        /* 
         * Note that this might not evaluate the second kid. This is useful to
         * speed things up, but could cause problems if some node on that branch
         * has side effects
         */
        return (getKid(0).eval(problemData) != 0 && getKid(1).eval(problemData) != 0 ? 1 : 0);
    }

    public void evalVectInternal(double[] outVect, double[][] kidsOutput, ProblemData problemData)
    {
        for (int wSample=0; wSample<outVect.length; wSample++)
        {
            outVect[wSample] = (kidsOutput[0][wSample] != 0 && kidsOutput[1][wSample] != 0 ? 1 : 0);
        }
    }

    public int nKids()
    {
        return 2;
    }

    public String name()
    {
        return "and";
    }

}
