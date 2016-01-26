package cl.ian.gp;

import ec.EvolutionState;
import ec.gp.GPIndividual;

/**
 * Created by Ian on 26/01/2016.
 */
public class MyGPIndividual extends GPIndividual {

    @Override
    public void printTrees(EvolutionState state, int log) {
        trees[0].printTreeForHumans(state, log);
    }

    @Override
    public void printIndividualForHumans(final EvolutionState state, final int log) {
        if (!evaluated)
            state.output.println("Not evaluated!", log);
        fitness.printFitnessForHumans(state, log);
        printTrees(state, log);
    }
}
