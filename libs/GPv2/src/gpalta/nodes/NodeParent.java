/*
 * NodeParent.java
 *
 * Created on 7 de junio de 2006, 04:45 PM
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

package gpalta.nodes;

import gpalta.core.NodeSet;

/**
 * Any object that can be set as the parent of a Node must conform to this Interface
 * @author neven
 */
public interface NodeParent
{
    public NodeParent getParent();

    public NodeSet typeOfKids(int i);

    public int getNSubNodes();

    public void setNSubNodes(int nSubNodes);

    public Node getKid(int whichKid);

    public void setKid(int whichKid, Node kid);

    public int getMaxDepthFromHere();

    public void setMaxDepthFromHere(int maxDepthFromHere);

    public int nKids();

    public int getCurrentDepth();

    public void newKids();
}
