/*
 * MultiTreeIndividual.java
 *
 * Created on 03-01-2007, 10:54:13 PM
 *
 * Copyright (C) 2007 Neven Boric <nboric@gmail.com>
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

package gpalta.multitree;

import gpalta.core.*;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;
/**
 * An individual composed of multiple trees
 */
public class MultiTreeIndividual extends Individual implements Serializable
{
    private List<Tree> treeList;
    public MultiTreeIndividual(int nTrees)
    {
        treeList = new ArrayList<Tree>(nTrees);
        for (int i=0; i<nTrees; i++)
        {
            treeList.add(null);
        }
    }

    public int nTrees()
    {
        return treeList.size();
    }

    public int getSize()
    {
        int nodes = 0;
        for (int i = 0; i < nTrees(); i++)
            nodes += treeList.get(i).getSize();
        return nodes;
    }

    public Individual deepClone()
    {
        MultiTreeIndividual out = null;
        try
        {
            out = (MultiTreeIndividual) clone();
            /* Remember to also clone the tree array and each tree */
            out.treeList = new ArrayList<Tree>(nTrees());
            for (int i = 0; i < nTrees(); i++)
            {
                out.treeList.add(i, (Tree) getTree(i).deepClone());
            }
        }
        catch (CloneNotSupportedException ex)
        {
            Logger.log(ex);
        }
        return out;
    }

    public Output eval(ProblemData problemData)
    {
        MultiOutput out = new MultiOutput(nTrees(), 1);
        for (int i=0; i<nTrees(); i++)
        {
            out.getArray(i)[0] = ((SingleOutput)getTree(i).eval(problemData)).x[0];
        }
        return out;
    }

    public void evalVect(Output out, TempVectorFactory tempVectorFactory, ProblemData problemData)
    {
        for (int i=0; i<nTrees(); i++)
        {
            getTree(i).getKid(0).evalVect(((MultiOutput)out).getArray(i), tempVectorFactory, problemData);
        }
    }

    public void setTree(int pos, Tree t)
    {
        treeList.set(pos, t);
    }

    public Tree getTree(int pos)
    {
        return treeList.get(pos);
    }
}
