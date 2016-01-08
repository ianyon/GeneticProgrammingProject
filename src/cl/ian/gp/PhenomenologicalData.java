/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package cl.ian.gp;

import ec.gp.GPData;

public class PhenomenologicalData extends GPData
    {
    /** return value **/
    public double x;

    public void copyTo(final GPData gpd)   // copy my stuff to another PhenomenologicalData
        { ((PhenomenologicalData)gpd).x = x; }
    }

