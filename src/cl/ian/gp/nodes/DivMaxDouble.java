package cl.ian.gp.nodes;

import ec.app.regression.RegressionData;
import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;

/**
 * @author Ian Yon
 * @version 1.1
 */

public class DivMaxDouble extends GPNode {
  public String toString() {
    return "/";
  }

  public int expectedChildren() {
    return 2;
  }

  public void eval(final EvolutionState state,
                   final int thread,
                   final GPData input,
                   final ADFStack stack,
                   final GPIndividual individual,
                   final Problem problem) {
    RegressionData rd = ((RegressionData) (input));

    // evaluate children[1] first to determine if the denominator is 0
    children[1].eval(state, thread, input, stack, individual, problem);
    if (rd.x == 0.0)
      // the answer is Double.MAX_VALUE since the denominator was 0.0
      rd.x = Double.MAX_VALUE;
  }
}



