/*
 * IndSelectorTournament.java
 *
 * Created on 19 de mayo de 2005, 05:20 PM
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

package gpalta.ops;

import java.util.*;

import gpalta.core.*;

/**
 * Implements Tournament selection. Assumes populationSize mod tournamentSize = 0
 *
 * @author DSP
 */
public class IndSelectorTournament extends IndSelector
{

    private Config config;

    public IndSelectorTournament(Config config)
    {
        this.config = config;
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
        List<T> out = new ArrayList<T>();
        for (T t : population)
        {
            t.setOnPop(false);
        }
        double maxFit;
        int indMaxFit;
        //For every pass:
        for (int i = 0; i < config.tournamentSize; i++)
        {
            int[] perm = Common.randPerm(population.size());

            //For every tournament:
            for (int j = 0; j < population.size(); j += config.tournamentSize)
            {
                maxFit = population.get(perm[j]).readFitness();
                indMaxFit = j;

                //For every tree in the tournament:
                for (int k = j + 1; k < j + config.tournamentSize; k++)
                {
                    if (population.get(perm[k]).readFitness() > maxFit)
                    {
                        maxFit = population.get(perm[k]).readFitness();
                        indMaxFit = k;
                    }
                }

                /* If this tree has already been selected, we need a copy of it
                * (So we don't modify both when operating on one of them)
                */
                if (population.get(perm[indMaxFit]).isOnPop())
                {
                    T tmp;
                    tmp = (T) population.get(perm[indMaxFit]).deepClone();
                    tmp.setOnPop(true);
                    out.add(tmp);
                }
                else
                {
                    population.get(perm[indMaxFit]).setOnPop(true);
                    out.add(population.get(perm[indMaxFit]));
                }
            }
        }
        return out;
    }

}
