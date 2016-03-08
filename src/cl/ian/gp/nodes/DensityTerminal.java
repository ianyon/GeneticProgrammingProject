/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package cl.ian.gp.nodes;

import cl.ian.gp.PhenomenologicalModel;
import ec.EvolutionState;
import ec.Problem;
import ec.app.regression.RegressionData;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;

public class DensityTerminal extends GPNode {
  /**
   * Shorter for Df(i)/1.205
   * @return
   */
  public String toString() {
    return "Dfn";
  }

  public int expectedChildren() {
    return 0;
  }

  public void eval(final EvolutionState state,
                   final int thread,
                   final GPData input,
                   final ADFStack stack,
                   final GPIndividual individual,
                   final Problem problem) {
    RegressionData rd = ((RegressionData) (input));
    rd.x = ((PhenomenologicalModel) problem).normalizedDensity;
  }
}

