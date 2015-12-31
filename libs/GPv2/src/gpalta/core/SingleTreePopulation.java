/*
 * SingleTreePopulation.java
 *
 * Created on 31 de mayo de 2006, 05:16 PM
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

import gpalta.core.Tree;
import gpalta.ops.*;

import java.util.*;
import java.io.Serializable;

/**
 * Implements a simple population formed by a fixed number of trees, each interpreded separatedly
 *
 * @author neven
 */
public class SingleTreePopulation implements Population, Serializable
{
    private List<Tree> treeList;
    private Config config;
    private SingleOutput outputs;

    public void eval(Fitness f, TempVectorFactory tempVectorFactory, ProblemData problemData)
    {
        for (Tree t : treeList)
        {
            if (!config.rememberLastEval || !t.fitCalculated)
            {
                getOutput(t, outputs, tempVectorFactory, problemData);
                f.calculate(outputs, t, problemData);
                t.fitCalculated = true;
            }
        }
    }

    private void getOutput(Tree t, SingleOutput o, TempVectorFactory tempVectorFactory, ProblemData problemData)
    {
        double[] results = o.x;
        problemData.reset();
        if (config.useVect)
        {
            t.evalVect(o, tempVectorFactory, problemData);
        }
        else
        {
            for (int i = 0; i < problemData.nSamples; i++)
            {
                results[i] = ((SingleOutput) t.eval(problemData)).x[0];
                problemData.update();
            }
        }
    }

    public Output getRawOutput(Individual ind, TempVectorFactory tempVectorFactory, ProblemData problemData)
    {
        SingleOutput out = new SingleOutput(problemData.nSamples);
        getOutput((Tree) ind, out, tempVectorFactory, problemData);
        return out;
    }

    public Output getProcessedOutput(Individual ind, Fitness f, TempVectorFactory tempVectorFactory, ProblemData problemData)
    {
        Output raw = getRawOutput(ind, tempVectorFactory, problemData);
        return f.getProcessedOutput(raw, problemData);
    }

    public Individual get(int which)
    {
        return treeList.get(which);
    }

    public void doSelection(IndSelector sel)
    {
        treeList = sel.select(treeList);
    }

    public void evolve(TreeOperator op)
    {
        op.operate(treeList);
    }

    public void init(Config config, ProblemData problemData, TreeBuilder builder)
    {
        this.config = config;
        treeList = new ArrayList<Tree>(config.populationSize);
        for (int i = 0; i < config.populationSize; i++)
        {
            treeList.add(new Tree(builder.treeRoot()));
        }
        builder.build(treeList);
        outputs = new SingleOutput(problemData.nSamples);
    }

}
