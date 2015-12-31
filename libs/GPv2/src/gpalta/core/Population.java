/*
 * Population.java
 *
 * Created on 31 de mayo de 2006, 03:30 PM
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

import gpalta.ops.*;

/**
 * Holds the problem's Individuals. It must evaluate them and organize them for selection and
 * evolution. Most applications should use SingleTreePopulation
 *
 * @author neven
 */
public interface Population
{

    /**
     * Evaluate every Individual and calculate their fitness
     * @param f
     * @param tempVectorFactory
     * @param problemData
     */
    public void eval(Fitness f, TempVectorFactory tempVectorFactory, ProblemData problemData);

    /**
     * Get the output directly from the Individual, without further processing
     *
     * @return A totally independent Output object
     */
    public Output getRawOutput(Individual ind, TempVectorFactory tempVectorFactory, ProblemData problemData);

    public Output getProcessedOutput(Individual ind, Fitness f, TempVectorFactory tempVectorFactory, ProblemData problemData);

    /**
     * Get a certain individual
     *
     * @param which The individual to get
     */
    public Individual get(int which);

    public void init(Config config, ProblemData problemData, TreeBuilder builder);

    /**
     * Perform Individual selection
     * @param sel
     */
    public void doSelection(IndSelector sel);

    /**
     * Apply genetic operators
     * @param op
     */
    public void evolve(TreeOperator op);

}
