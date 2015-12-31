/*
 * NodeSelector.java
 *
 * Created on 12 de mayo de 2005, 01:13 AM
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

import java.util.*;

import gpalta.nodes.*;
import gpalta.core.*;

public class NodeSelector
{

    private int currentNodeSearched;
    private Config config;
    private NodeFactory nodeFactory;

    public NodeSelector(Config config, NodeFactory nodeFactory)
    {
        this.config = config;
        this.nodeFactory = nodeFactory;
    }

    public Node pickRandomNode(Tree tree)
    {
        double type = Common.globalRandom.nextDouble();
        List<Node> l = new ArrayList<Node>();
        if (type <= config.upLimitProbSelectTerminal)
        {
            getTerminalNodes(l, tree.getKid(0));
        }
        else if (type <= config.upLimitProbSelectNonTerminal)
        {
            getFunctionNodes(l, tree.getKid(0));
            /* If there aren't function nodes, this is a tree with a terminal at its root
             * (Shoudn't we stop this from happening?)
             */
            if (l.size() == 0)
            {
                return tree.getKid(0);
            }
        }
        else if (type <= config.upLimitProbSelectRoot)
        {
            return tree.getKid(0);
        }
        else
        {
            return pickRandomAnyNode(tree);
        }
        int which = Common.globalRandom.nextInt(l.size());
        return l.get(which);
    }

    /**
     * Picks any node of the same type as node within the tree. It only checks if the node is real or logic. O(n)
     *
     * @param node The 'sample' Node.
     */
    public Node pickRandomNode(Tree tree, Node node)
    {
        double type = Common.globalRandom.nextDouble();
        List<Node> l = new ArrayList<Node>();

        if (type <= config.upLimitProbSelectTerminal)
        {
            getNodes(l, tree.getKid(0), node.getParent().typeOfKids(node.getWhichKidOfParent()).getTerminals());
        }
        else if (type <= config.upLimitProbSelectNonTerminal)
        {
            getNodes(l, tree.getKid(0), node.getParent().typeOfKids(node.getWhichKidOfParent()).getFunctions());
        }
        else
        {
            getNodes(l, tree.getKid(0), node.getParent().typeOfKids(node.getWhichKidOfParent()).getAll());
        }
        //TODO: what should we do if we don't find any node?
        if (l.size() == 0)
        {
            return null;
        }
        int which = Common.globalRandom.nextInt(l.size());
        return l.get(which);
    }

    private void getNodes(List<Node> l, Node node, List<Node> types)
    {
        if (NodeFactory.isInList(node, types))
        {
            l.add(node);
        }
        for (int i = 0; i < node.nKids(); i++)
        {
            getNodes(l, node.getKid(i), types);
        }
    }

    /**
     * Picks any node of any kind within the tree. O(logn)
     */
    private Node pickRandomAnyNode(Tree tree)
    {
        int which = Common.globalRandom.nextInt(tree.getNSubNodes());
        currentNodeSearched = 0;
        return getNode(tree.getKid(0), which);
    }

    private Node getNode(Node node, int which)
    {
        if (currentNodeSearched == which)
        {
            return node;
        }
        for (int i = 0; i < node.nKids(); i++)
        {
            if (which > currentNodeSearched + 1 + node.getKid(i).getNSubNodes())
            {
                currentNodeSearched += 1 + node.getKid(i).getNSubNodes();
            }
            else
            {
                currentNodeSearched++;
                return getNode(node.getKid(i), which);
            }
        }
        //we should never get here:
        Logger.log("Warning in PickNode: Reached dead end");
        return null;
    }

    private void getTerminalNodes(List<Node> l, Node node)
    {
        if (node.nKids() == 0)
        {
            l.add(node);
        }
        for (int i = 0; i < node.nKids(); i++)
        {
            getTerminalNodes(l, node.getKid(i));
        }
    }

    private void getFunctionNodes(List<Node> l, Node node)
    {
        if (node.nKids() > 0)
        {
            l.add(node);
        }
        for (int i = 0; i < node.nKids(); i++)
        {
            getFunctionNodes(l, node.getKid(i));
        }
    }

}
