/*
 * IndSelectorRoulette.java
 *
 * Created on 30 de marzo de 2006, 12:45 PM
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

import gpalta.core.*;

import java.util.*;

/**
 * Implements roulette selection method
 */
public class IndSelectorRoulette extends IndSelector
{
    private Config config;
    private Comparator<Individual> comp;
    private Ranking theRanking;

    /**
     * Creates a new instance of IndSelectorRoulette
     */
    public IndSelectorRoulette(Config config, Ranking theRanking)
    {
        this.config = config;
        this.comp = new IndFitnessComparator();
        this.theRanking = theRanking;
    }

    /*
    * TODO: arrange order to descendant on theRanking
    */
    public <T extends Individual> List<T> select(List<T> population)
    {
        double randomNumber;
        int k;
        T temp1;


        List<T> out = new ArrayList<T>();
        theRanking.rankPop(population, comp);

        /*
        * Roulette iterations
        */
        for (int i = 0; i < population.size(); i++)
        {
            randomNumber = Math.random();
            k = 0;
            while (theRanking.acumulatedFit[k] / theRanking.totalFitness <= randomNumber)
            {
                k++;
                if (k == population.size() - 1)
                {
                    break;
                }
            }
            temp1 = ((T) theRanking.popArray[k]);
            if (!temp1.isOnPop())
            {
                out.add(temp1);
                temp1.setOnPop(true);
            }
            else
            {
                out.add((T) temp1.deepClone());
            }


        }

        return (out);
    }

}
