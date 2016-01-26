package cl.ian.gp;

import ec.EvolutionState;
import ec.gp.koza.KozaShortStatistics;
import ec.util.Parameter;

/**
 * Created by Ian on 26/01/2016.
 */
public class MyKozaShortStatistics extends KozaShortStatistics {
    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);

        state.output.println("Gen" +
                        (doTime ? " BreedTime EvalTime" : "") +
                        (doDepth ? " AvgDepth" : "") +
                        (doSize ? " AvgSize AvgSizeInd AvgSizeSoFar BestSize BestSizeSoFar" : "") +
                        "AvgFit BestFit BestFitSoFar"
                , statisticslog);
    }

    public void postInitializationStatistics(final EvolutionState state) {
        super.postInitializationStatistics(state);
    }


    protected void prepareStatistics(EvolutionState state) {
        super.prepareStatistics(state);
    }


    protected void gatherExtraSubpopStatistics(EvolutionState state, int subpop, int individual) {
        super.gatherExtraSubpopStatistics(state, subpop, individual);
    }

    protected void printExtraSubpopStatisticsBefore(EvolutionState state, int subpop) {
        super.printExtraSubpopStatisticsBefore(state, subpop);
    }

    protected void printExtraPopStatisticsBefore(EvolutionState state) {
        super.printExtraPopStatisticsBefore(state);
    }
}
