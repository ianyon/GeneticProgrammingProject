/*
 * NodeBuilderGrow.java
 *
 * Created on 18 de mayo de 2005, 07:26 PM
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

import gpalta.nodes.*;
import gpalta.core.*;

/**
 * Implements the 'GROW' build method
 *
 * @author neven
 */
public class NodeBuilderGrow extends NodeBuilder
{
    private NodeFactory nodeFactory;

    public NodeBuilderGrow(NodeFactory nodeFactory)
    {
        this.nodeFactory = nodeFactory;
    }

    public void build(NodeParent node, int maxDepth)
    {
        build(node, -1, maxDepth);
    }

    public void build(NodeParent node, int whichKid, int maxDepth)
    {
        int currentGlobalDepth = node.getCurrentDepth();
        List<Integer> listOfKids = new ArrayList<Integer>();

        //whichKid = -1 means: build all kids
        if (whichKid == -1)
        {
            if (node.nKids() > 0)
                node.newKids();
            for (int i = 0; i < node.nKids(); i++)
            {
                listOfKids.add(i);
            }
        }

        //build only whichKid
        else
        {
            listOfKids.add(whichKid);
        }

        int maxDepthOfKids = 0;
        for (Integer i : listOfKids)
        {
            //If maxDepth = 1, we need terminals as kids:
            if (maxDepth == 1)
            {
                node.setKid(i, nodeFactory.newRandomNode(node.typeOfKids(i).getTerminals(), currentGlobalDepth + 1));
                node.setNSubNodes(node.getNSubNodes() + 1);
            }
            else
            {
                node.setKid(i, nodeFactory.newRandomNode(node.typeOfKids(i).getAll(), currentGlobalDepth + 1));
                build(node.getKid(i), -1, maxDepth - 1);

                /* If we are building only one child, these will be wrong for the 
                * first parent, but will be corrected by updateParents()
                */
                node.setNSubNodes(node.getNSubNodes() + (1 + node.getKid(i).getNSubNodes()));
                maxDepthOfKids = Math.max(maxDepthOfKids, node.getKid(i).getMaxDepthFromHere());
            }
            node.getKid(i).setParent(node);
            node.getKid(i).setWhichKidOfParent(i);
        }
        if (node.nKids() > 0)
            node.setMaxDepthFromHere(1 + maxDepthOfKids);
    }

}
