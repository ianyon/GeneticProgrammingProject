/*
 * RankingRaw.java
 *
 * Created on 31 de marzo de 2006, 11:29 AM
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

/**
 * Inherits methods from Ranking. RankingRaw sorts population by fitness criteria
 * and asigns to each tree a "surviving probability" proportional to the fitness
 * of the individual normalized by the sum of all population fitness
 */

public class RankingRaw extends Ranking
{

    public RankingRaw()
    {
        this.filled = false;
    }

    /*
    *Obtain number expentances and sort population
    */
    public void rankPop(List<? extends Individual> population, Comparator comp)
    {
        this.init(population, comp);

        /*
        *Calculate acumulated fitness and acumulated probabilities
        */

        totalFitness = 0;
        for (int i = 0; i < popSize; i++)
        {
            adjustedFitness[i] = popArray[i].readFitness() - min;
            totalFitness += adjustedFitness[i];
            acumulatedFit[i] = totalFitness;
        }


    }

}
