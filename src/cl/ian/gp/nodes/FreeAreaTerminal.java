package cl.ian.gp.nodes;

import cl.ian.gp.PhenomenologicalModel;
import ec.EvolutionState;
import ec.Problem;
import ec.app.regression.RegressionData;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;

public class FreeAreaTerminal extends GPNode {
  /**
   * Shorter for A_sup/A_vol
   * @return
   */
  public String toString() {
    return "An";
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
    rd.x = ((PhenomenologicalModel) problem).normalizedArea;
  }
}

