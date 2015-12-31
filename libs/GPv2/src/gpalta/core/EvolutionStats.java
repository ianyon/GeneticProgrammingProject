/*
 * EvolutionStats.java
 *
 * Created on 26 de mayo de 2005, 01:59 AM
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

package gpalta.core;

import gpalta.nodes.*;


/**
 * Simple class that holds some statistics about the ongoing Evolution
 *
 * @author neven
 */
public class EvolutionStats
{
    public Individual bestSoFar;
    public boolean bestTreeChanged = true;
    public double bestFitThisGen;
    public int generation;
    public double avgFit;
    public double avgNodes;
    public int bestGen;

}
