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
    public double vmf;
    public double initialVelocity;
    public double rem;
    public double s;

    public void copyTo(final GPData gpd)   // copy my stuff to another PhenomenologicalData
        {
            ((PhenomenologicalData)gpd).x = x;
            ((PhenomenologicalData)gpd).vmf = vmf;
            ((PhenomenologicalData)gpd).rem = rem;
            ((PhenomenologicalData)gpd).s = s;
            ((PhenomenologicalData)gpd).initialVelocity = initialVelocity;
        }
    }


