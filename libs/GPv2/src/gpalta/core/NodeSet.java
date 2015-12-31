/*
 * NodeSet.java
 *
 * Created on 13 de octubre de 2005, 10:26 PM
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

import java.io.Serializable;
import java.util.*;

import gpalta.nodes.*;

/**
 * Simple class that defines a set of Nodes. It has three subsets: functions,
 * terminals and all. The "all" list should contain both functions and terminals
 *
 * @author neven
 */
public class NodeSet implements Serializable
{

    private List<Node> all;
    private List<Node> terminals;
    private List<Node> functions;
    private String name;

    public NodeSet(String name)
    {
        this.name = name;
        all = new ArrayList<Node>();
        terminals = new ArrayList<Node>();
        functions = new ArrayList<Node>();
    }

    public List<Node> getAll()
    {
        return all;
    }

    public List<Node> getTerminals()
    {
        return terminals;
    }

    public void addTerminal(Node node)
    {
        terminals.add(node);
        all.add(node);
    }

    public List<Node> getFunctions()
    {
        return functions;
    }

    public void addFunction(Node node)
    {
        functions.add(node);
        all.add(node);
    }

    public String getName()
    {
        return name;
    }

}
