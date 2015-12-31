/*
 * IndSelectorSUS.java
 *
 * Created on 30 de marzo de 2006, 03:28 PM
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
 * Implements Stochastic Universal sampling (SUS) method
 */
public class IndSelectorSUS extends IndSelector
{
    private Config config;
    private Comparator<Individual> comp;
    private double pointerDistance;
    private Ranking theRanking;

    public IndSelectorSUS(Config config, Ranking theRanking)
    {
        this.config = config;
        this.comp = new IndFitnessComparator();
        this.pointerDistance = config.SUSPointerDistance;
        this.theRanking = theRanking;
    }

    public <T extends Individual> List<T> select(List<T> population)
    {
        double pointerPos;
        int k;
        T temp1;


        List<T> out = new ArrayList<T>();


        theRanking.rankPop(population, comp);

        /*
        *SUS iterations
        */
        pointerPos = Math.random();
        k = 0;
        for (int i = 0; i < population.size(); i++)
        {
            while (pointerPos >= theRanking.acumulatedFit[k] / theRanking.totalFitness)
            {
                k++;
                if (k == population.size() - 1)
                {
                    break;
                }
            }
            temp1 = (T) theRanking.popArray[k];
            if (!temp1.isOnPop())
            {
                out.add(temp1);
                temp1.setOnPop(true);
            }
            else
            {
                out.add((T) temp1.deepClone());
            }

            pointerPos += pointerDistance;
            if (pointerPos > 1)
            {
                pointerPos = pointerPos - 1;
                k = 0;
            }
            else if (k >= population.size())
            {
                k = 0;
            }

        }

        return (out);
    }

}
