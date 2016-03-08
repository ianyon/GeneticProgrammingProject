package cl.ian.gp.nodes;

import ec.app.regression.RegressionData;
import cl.ian.gp.PhenomenologicalModel;
import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;

public class MeanReynoldsTerminal extends GPNode {
  /**
   * Shorter for Rem(i)
   *
   * @return
   */
  public String toString() {
    return "Rem";
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
    rd.x = ((PhenomenologicalModel) problem).reynolds;
  }
}

