/*
 * TreeOperator.java
 *
 * Created on 13 de mayo de 2005, 09:19 PM
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
 * Performs genetic operations between trees. Currently crossover, mutation
 * and reproduction are implemented
 *
 * @author neven
 */
public class TreeOperator
{

    private NodeSelector selector;
    private NodeBuilder nodeBuilder;
    private Config config;

    public TreeOperator(Config config, NodeFactory nodeFactory)
    {
        this.config = config;
        selector = new NodeSelector(config, nodeFactory);
        nodeBuilder = new NodeBuilderGrow(nodeFactory);
    }

    /**
     * Performs the genetic operations. Probabilities for each op are assigned
     * in the Config object
     *
     * @param population The list of trees that passed the selection process
     */
    public <T extends Tree> void operate(List<T> population)
    {
        int[] perm = Common.randPerm(population.size());
        double op;
        for (int i = 0; i < population.size(); i++)
        {
            op = Common.globalRandom.nextDouble();
            if (op <= config.upLimitProbCrossOver)
            {
                //Do cross over, except for the last tree:
                if (i != population.size() - 1)
                {
                    if (crossOver(population.get(perm[i]), population.get(perm[i+1])))
                    {
                        population.get(perm[i]).fitCalculated = false;
                        population.get(perm[i + 1]).fitCalculated = false;
                        i++;
                    }
                }
            }
            else if (op <= config.upLimitProbMutation)
            {
                population.get(perm[i]).fitCalculated = false;
                mutateBuild(population.get(perm[i]));
            }
            else
            {
                //Do nothing (reproduction)
            }
        }
    }

    public void mutateBuild(Tree tree)
    {
        Node tmp = selector.pickRandomNode(tree);
        //Choose a random depth between 1 and (config.maxDepth - currentDepth)
        int depthFromHere = 1 + Common.globalRandom.nextInt(config.maxDepth - tmp.getCurrentDepth() + 1);
        /* If the root node is selected and the NodeSet it belongs to doesn't
         * have any terminals, do not ask for a terminal (ie make sure depthFromHere >= 2)
         */
        if (tmp.getCurrentDepth()==0)
        {
            if (depthFromHere == 1 && tmp.getParent().typeOfKids(0).getTerminals().size()==0)
                depthFromHere = 2 + Common.globalRandom.nextInt(config.maxDepth - tmp.getCurrentDepth());
        }
        //System.out.println("Mut: " + tmp.currentDepth + " " + depthFromHere);
        nodeBuilder.build(tmp.getParent(), tmp.getWhichKidOfParent(), depthFromHere);
        updateParents(tmp);
    }

    public boolean crossOver(Tree tree1, Tree tree2)
    {
        Node node1;
        Node node2;

        for (int i = 0; i < config.maxCrossoverTries; i++)
        {
            node1 = selector.pickRandomNode(tree1);
            node2 = selector.pickRandomNode(tree2, node1);
            if (node2 != null)
            {
                if (node1.getCurrentDepth() + node2.getMaxDepthFromHere() <= config.maxDepth &&
                        node2.getCurrentDepth() + node1.getMaxDepthFromHere() <= config.maxDepth)
                {
                    //System.out.println("CO: " + node1.currentDepth + " " + node2.currentDepth);

                    /* TODO: we shouldn't need to deepClone(), because two trees are
                    * never the same (if a tree is selected more than once, it's deepCloned)
                    * But, we need to update the depth of the swaped nodes and their kids,
                    * so I'm leaving it
                    */

                    Node node1copy = node1.deepClone(node2.getCurrentDepth());
                    Node node2copy = node2.deepClone(node1.getCurrentDepth());

                    node1.getParent().setKid(node1.getWhichKidOfParent(), node2copy);
                    node2.getParent().setKid(node2.getWhichKidOfParent(), node1copy);

                    node1copy.setParent(node2.getParent());
                    node2copy.setParent(node1.getParent());

                    node1copy.setWhichKidOfParent(node2.getWhichKidOfParent());
                    node2copy.setWhichKidOfParent(node1.getWhichKidOfParent());

                    updateParents(node1copy);
                    updateParents(node2copy);
                    return true;
                }
            }
        }
        //if we reach here, neither tree was modified:
        return false;
        //System.out.println("Crossover failed after " + config.maxCrossoverTries + " tries");
    }

    /**
     * Recalculate nSubNodes and maxDepthFromHere for all parents up to rootNode
     * This is necessary to keep the assumption that nodes always know both these values
     */
    private void updateParents(NodeParent node)
    {
        while (node.getParent() != null)
        {
            node = node.getParent();
            node.setNSubNodes(0);
            int maxDepth = 0;
            for (int i = 0; i < node.nKids(); i++)
            {
                node.setNSubNodes(node.getNSubNodes() + (1 + node.getKid(i).getNSubNodes()));
                maxDepth = Math.max(maxDepth, node.getKid(i).getMaxDepthFromHere());
            }
            node.setMaxDepthFromHere(maxDepth + 1);
        }
    }

}
