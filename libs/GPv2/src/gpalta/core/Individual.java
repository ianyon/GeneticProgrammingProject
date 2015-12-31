/*
 * Individual.java
 *
 * Created on 31 de mayo de 2006, 03:48 PM
 *
 * Copyright (C) 2006 Neven Boric <nboric@gmail.com>
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

import java.io.Serializable;

/**
 * Individuals are the objects that form the population and are modified throughout the evolution.
 * The most common kind of Individual is a Tree, and this is what most applications should use. This
 * class can be extended to create other types of evolutionary objects
 *
 * @author neven
 */
public abstract class Individual implements Cloneable, Serializable
{
    private double fitness;
    private boolean isOnPop;
    public boolean fitCalculated;

    /**
     * Get the size of this Individual, hopefully without recalculating it (ie. in the case of a
     * Tree, without descending every node). In most cases, this will mean the number of nodes
     * present in the Individual
     */
    public abstract int getSize();

    /**
     * Get a new, totally independent copy of this individual. Subclasses must implement this method
     * properly, in order to insure that two instances are not modified unintentionally
     */
    public abstract Individual deepClone();


    /**
     * Read this individual's fitness, without recalculating it
     */
    public double readFitness()
    {
        return fitness;
    }

    /**
     * Store the fitness, so it doesn't need to be recalculated
     *
     * @param fit The fitness value
     */
    public void setFitness(double fit)
    {
        fitness = fit;
    }

    /**
     * Record that this Individual was selected and is part of the population
     */
    public void setOnPop(boolean flag)
    {
        isOnPop = flag;
    }

    /**
     * Ask whether this Individual is already part of the population
     */
    public boolean isOnPop()
    {
        return isOnPop;
    }

    public abstract Output eval(ProblemData problemData);

    public abstract void evalVect(Output out, TempVectorFactory tempVectorFactory, ProblemData problemData);

}
