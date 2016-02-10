package cl.ian.gp;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPIndividual;
import ec.simple.SimpleStatistics;
import ec.util.Parameter;

/**
 * Created by Ian on 26/01/2016.
 */
public class SimpleGPStatistics extends SimpleStatistics {
    protected boolean warned = false;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);

        if(children.length!=0 && children[0] instanceof MyKozaShortStatistics){
            ((MyKozaShortStatistics)children[0]).printHeader(state);
        }
    }

    @Override
    public void postEvaluationStatistics(final EvolutionState state) {
        super.postEvaluationStatistics(state);
        // for now we just print the best fitness per subpopulation.
        Individual[] best_i = new Individual[state.population.subpops.length];  // quiets compiler complaints
        for (int x = 0; x < state.population.subpops.length; x++) {
            best_i[x] = state.population.subpops[x].individuals[0];
            for (int y = 1; y < state.population.subpops[x].individuals.length; y++) {
                if (state.population.subpops[x].individuals[y] == null) {
                    if (!warned) {
                        state.output.warnOnce("Null individuals found in subpopulation");
                        warned = true;  // we do this rather than relying on warnOnce because it is much faster in a tight loop
                    }
                } else if (best_i[x] == null || state.population.subpops[x].individuals[y].fitness.betterThan(best_i[x].fitness))
                    best_i[x] = state.population.subpops[x].individuals[y];
                if (best_i[x] == null) {
                    if (!warned) {
                        state.output.warnOnce("Null individuals found in subpopulation");
                        warned = true;  // we do this rather than relying on warnOnce because it is much faster in a tight loop
                    }
                }
            }

            // now test to see if it's the new best_of_run
            if (best_of_run[x] == null || best_i[x].fitness.betterThan(best_of_run[x].fitness))
                best_of_run[x] = (Individual) (best_i[x].clone());
        }

        // print the best-of-generation individual
        if (doGeneration) state.output.println("\nBest Individual Generation " + state.generation + ":", statisticslog);
        if (doGeneration) best_i[0].printIndividualForHumans(state, statisticslog);
        if (doGeneration) state.output.message("Depth: "+((GPIndividual)best_i[0]).trees[0].child.depth()+" Size: "+ best_i[0].size());
        if (doMessage && !silentPrint) state.output.message("Best fitness: " +
                (best_i[0].evaluated ? " " : " (not evaluated): ") +
                best_i[0].fitness.fitnessToStringForHumans());

    }

    @Override
    public void finalStatistics(final EvolutionState state, final int result) {
        // for now we just print the best fitness
        if (doFinal) state.output.println("\nBest Individual of Run:", statisticslog);
        if (doFinal) best_of_run[0].printIndividualForHumans(state, statisticslog);
        if (doFinal) state.output.message("Depth: "+((GPIndividual)best_of_run[0]).trees[0].child.depth()+" Size: "+ best_of_run[0].size());
        if (doMessage && !silentPrint)
            state.output.message("\nBest fitness of run: " + best_of_run[0].fitness.fitnessToStringForHumans());
    }
}
