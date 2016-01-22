package cl.ian.gp.nodes;

import cl.ian.gp.PhenomenologicalData;
import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;

public class Mul extends GPNode {
    public String toString() {
        return "*";
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
        double result;
        PhenomenologicalData rd = ((PhenomenologicalData) (input));

        children[0].eval(state, thread, input, stack, individual, problem);
        result = rd.x;

        // can't shortcut because of NaN or +-Infinity

        children[1].eval(state, thread, input, stack, individual, problem);
        rd.x = result * rd.x;
    }
}


