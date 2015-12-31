/*
 * TreeBuilder.java
 *
 * Created on 18 de mayo de 2005, 08:22 PM
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

import gpalta.core.Tree;
import gpalta.nodes.*;

import java.util.*;

import gpalta.core.*;


/**
 * Implements ramped half and half Tree creation
 *
 * @author neven
 */
public class TreeBuilder
{

    private NodeBuilder nodeBuilderGrow;
    private NodeBuilder nodeBuilderFull;
    private int[] nTreesEachDepth;
    private Config config;
    private NodeFactory nodeFactory;

    /**
     * Creates a new instance of TreeBuilder
     */
    public TreeBuilder(Config config, NodeFactory nodeFactory)
    {
        this.config = config;
        this.nodeFactory = nodeFactory;

        nodeBuilderGrow = new NodeBuilderGrow(nodeFactory);
        nodeBuilderFull = new NodeBuilderFull(nodeFactory);

    }

    /**
     * Build a population of Trees, using the ramped half and half method
     */
    public <T extends Tree> void build(List<T> treeList)
    {
        nTreesEachDepth = new int[config.initialMaxDepth - config.initialMinDepth + 1];
        /* nTreesEachDepth will contain the number of trees created for each
         * depth from initialMinDepth to maxDepth
         * ie:
         * nTreesEachDepth[0] = number of trees crated with max depth initialMinDepth
         * nTreesEachDepth[1] = number of trees crated with max depth initialMinDepth + 1
         * and so on
         * This is done from greater to lower depth in order to favor larger trees.
         */
        int depth = config.initialMaxDepth;
        for (int i = 0; i < treeList.size(); i++)
        {
            if (depth == config.initialMinDepth - 1)
            {
                depth = config.initialMaxDepth;
            }
            nTreesEachDepth[depth - config.initialMinDepth]++;
            depth--;
        }

        depth = config.initialMinDepth;
        int treesDoneThisDepth = 0;
        for (T tree : treeList)
        {
            build(tree, depth);

            treesDoneThisDepth++;
            if (treesDoneThisDepth == nTreesEachDepth[depth - config.initialMinDepth])
            {
                depth++;
                treesDoneThisDepth = 0;
            }
        }
    }

    public NodeSet treeRoot()
    {
        return nodeFactory.treeRoot;
    }

    private void build(Tree tree, int maxDepth)
    {
        tree.setKid(0, nodeFactory.newRandomNode(tree.typeOfKids(0).getAll(), 0));

        if (Common.globalRandom.nextDouble() <= config.probGrowBuild)
        {
            nodeBuilderGrow.build(tree.getKid(0), maxDepth);
        }
        else
        {
            nodeBuilderFull.build(tree.getKid(0), maxDepth);
        }

        tree.getKid(0).setParent(tree);
    }

}
