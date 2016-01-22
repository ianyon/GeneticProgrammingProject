package cl.ian.gp.nodes;

import ec.EvolutionState;
import ec.Problem;
import ec.app.regression.RegressionData;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;

/**
 * Created by Ian on 04/01/2016.
 * <p>
 * Protected square function.
 * MySquare(X1) returns 0 if X1^2 is NaN or Inf, or has imaginary part, otherwise returns X1^2.
 */
public class Square extends GPNode {
    public String toString() {
        return "pow2";
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
        return 1;
    }

    public void eval(final EvolutionState state,
                     final int thread,
                     final GPData input,
                     final ADFStack stack,
                     final GPIndividual individual,
                     final Problem problem) {
        RegressionData rd = ((RegressionData) (input));

        children[0].eval(state,thread,input,stack,individual,problem);
        rd.x = rd.x * rd.x;

        // This happens if the result is NaN, Infinite or not real
        if (Double.isNaN(rd.x) || Double.isInfinite(rd.x)
                )
            rd.x = 0;
    }
}



