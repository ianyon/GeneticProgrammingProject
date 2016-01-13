package cl.ian.gp.nodes;

import ec.EvolutionState;
import ec.Problem;
import ec.app.regression.RegressionData;
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
        return "%";
    }

    /*
      public void checkConstraints(final EvolutionState state,
      final int tree,
      final GPIndividual typicalIndividual,
      final Parameter individualBase)
      {
      super.checkConstraints(state,tree,typicalIndividual,individualBase);
      if (children.length!=2)
      state.output.error("Incorrect number of children for node " +
      toStringForError() + " at " +
      individualBase);
      }
    */
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
        if (rd.x == 0.0)
            // the answer is children[0] since the denominator was 0.0
            children[0].eval(state, thread, input, stack, individual, problem);
        else {
            double result;
            result = rd.x;

            children[0].eval(state, thread, input, stack, individual, problem);
            rd.x = rd.x / result;
        }
    }
}



