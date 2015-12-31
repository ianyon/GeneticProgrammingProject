/*
 * RankingLFR.java
 *
 * Created on 31 de marzo de 2006, 02:58 PM
 *
 * Copyright (C) 2006 Juan Ramirez <tiomemo@gmail.com>
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


package gpalta.ops;

import gpalta.core.Individual;
import gpalta.nodes.*;

import java.util.*;

/*
 * Implements ranking selection and linear fitness adaptaion, ie, after sorting
 * the population assigns a "surviving probability" to each individual
 * proportional to ranking position
 */

public class RankingLFR extends Ranking
{


    /**
     * Creates a new instance of RankingLFR
     */
    public RankingLFR()
    {
        this.filled = false;


    }

    /*
    *Ranks population
    */
    public void rankPop(List<? extends Individual> population, Comparator comp)
    {
        int j;
        double m;
        this.init(population, comp);
        //m=m/(-(popSize-1) );
        m = (max - min) / (popSize - 1);

        /*
        *Calculate acumulated fitness and acumulated probabilities
        */
        totalFitness = 0;
        for (int i = 0; i < popSize; i++)
        {

            adjustedFitness[i] = i * m;
            totalFitness += adjustedFitness[i];
            acumulatedFit[i] = totalFitness;
        }

    }

}
