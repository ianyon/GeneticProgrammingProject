/*
 * IndSelectorProportional.java
 *
 * Created on 28 de marzo de 2006, 06:03 PM
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
 * Implements proportional selection (deterministic). First, a number of copies
 * from each individual, equal to each expectated number of copies integer, are
 * selected for next population. Then, the following individuals are selected
 * from the decimals of the expectated number of copies of each tree.
 */
public class IndSelectorProportional extends IndSelector
{
    private Config config;
    private Comparator<Individual> comp;
    private Comparator<Individual> comp2;
    private Ranking theRanking;

    /**
     * Creates a new instance of IndSelectorProportional
     */
    public IndSelectorProportional(Config config, Ranking theRanking)
    {
        this.config = config;
        this.comp = new IndFitnessComparator();
        this.comp2 = new IndFitnessComparatorDec();
        this.theRanking = theRanking;
    }

    /**
     * Performs the selection
     *
     * @param population A list of Trees from where to select the individuals
     * @return A new list of Trees with the selected individuals. If a Tree is
     *         selected more than once, each instance of that Tree will be a totally
     *         independant individual (no other Trees will be modified when modifying that Tree)
     */
    public <T extends Individual> List<T> select(List<T> population)
    {
        double fitness;
        //double totalFitness;
        int treeCounter, treeCopies, k;
        T temp1, temp2;


        List<T> out = new ArrayList<T>();


        theRanking.rankPop(population, comp);


        treeCounter = 0;
        for (int i = 0; i < population.size(); i++)
        {
            fitness = theRanking.adjustedFitness[population.size() - 1 - i];

            temp1 = ((T) theRanking.popArray[population.size() - 1 - i]);
            treeCopies = (int) Math.floor(fitness / theRanking.totalFitness * population.size());
            for (int j = 1; j <= treeCopies; j++)
            {
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
            treeCounter += treeCopies;
        }

        k = 0;

        /*
        *Select best fitted for unsigned population slots
        */
        Arrays.sort(theRanking.popArray, comp2);
        while (treeCounter < population.size())
        {
            temp1 = (T) theRanking.popArray[population.size() - 1 - k];
            if (!temp1.isOnPop())
            {
                out.add(temp1);
                temp1.setOnPop(true);
            }
            else
            {
                out.add((T) temp1.deepClone());
            }

            k++;
            treeCounter++;
        }

        return (out);
    }


}

class IndFitnessComparatorDec implements Comparator<Individual>
{

    public int compare(Individual o1, Individual o2)
    {
        double f1 = o1.readFitness() - Math.floor(o1.readFitness());
        double f2 = o2.readFitness() - Math.floor(o2.readFitness());

        if (f1 < f2)
        {
            return (-1);
        }
        else if (f1 > f2)
        {
            return (1);
        }
        else
        {
            return (0);
        }

    }

}