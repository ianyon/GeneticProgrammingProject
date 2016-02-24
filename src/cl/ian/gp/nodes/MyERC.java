package cl.ian.gp.nodes;

import ec.app.regression.RegressionData;
import ec.EvolutionState;
import ec.Problem;
import ec.app.regression.func.RegERC;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;

/**
 * Created by Ian on 07/01/2016.
 */
public class MyERC extends RegERC {

    /**
     * Creates a random constant in the interval (0,1)
     *
     * @param state
     * @param thread
     */
    public void resetNode(final EvolutionState state, final int thread) {
        value = state.random[thread].nextDouble(false, false) * 2 - 1.0;
    }

    public void eval(final EvolutionState state,
                     final int thread,
                     final GPData input,
                     final ADFStack stack,
                     final GPIndividual individual,
                     final Problem problem) {
        RegressionData rd = ((RegressionData) (input));
        rd.x = value;
    }
}
