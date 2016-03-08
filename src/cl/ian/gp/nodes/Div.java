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

public class Div extends GPNode {
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

    // evaluate children[1] first to determine if the demoniator is 0
    children[1].eval(state, thread, input, stack, individual, problem);

    double result = 1.0;
    // If the denominator is 0.0, the answer is children[0]
    if (rd.x != 0.0) result = rd.x;
    children[0].eval(state, thread, input, stack, individual, problem);
    rd.x = rd.x / result;
  }
}


