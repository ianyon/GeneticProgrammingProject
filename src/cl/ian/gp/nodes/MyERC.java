package cl.ian.gp.nodes;

import cl.ian.gp.PhenomenologicalData;
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

    public void resetNode(final EvolutionState state, final int thread)
    { value = state.random[thread].nextDouble() * 2 - 1.0; }

    public void eval(final EvolutionState state,
                     final int thread,
                     final GPData input,
                     final ADFStack stack,
                     final GPIndividual individual,
                     final Problem problem)
    {
        PhenomenologicalData rd = ((PhenomenologicalData)(input));
        rd.x = value;
    }
}
