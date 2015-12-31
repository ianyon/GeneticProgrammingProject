/*
 * IndFitnessComparator.java
 *
 * Created on 29 de marzo de 2006, 04:24 PM
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

package gpalta.core;

import java.util.*;

/**
 * Implements a comparator for Individuals based on their fitness
 *
 * @author DSP
 */


public class IndFitnessComparator implements Comparator<Individual>
{

    public int compare(Individual o1, Individual o2)
    {
        if (o1.readFitness() < o2.readFitness())
        {
            return (-1);
        }
        else if (o1.readFitness() > o2.readFitness())
        {
            return (1);
        }
        else
        {
            return (0);
        }

    }

}
