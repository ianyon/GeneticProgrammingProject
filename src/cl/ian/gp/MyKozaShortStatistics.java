package cl.ian.gp;

import ec.EvolutionState;
import ec.gp.koza.KozaShortStatistics;

/**
 * Created by Ian on 26/01/2016.
 */
public class MyKozaShortStatistics extends KozaShortStatistics {
    public void printHeader(EvolutionState state) {
        state.output.println("Gen" +
                        (doTime ? " BreedTime EvalTime" : "") +
                        (doDepth ? " [AvgDepth]" : "") +
                        (doSize ? " [AvgSize] AvgSizeInd AvgSizeSoFar BestSize BestSizeSoFar" : "") +
                        " [AvgFit BestFitGen BestFitSoFar]"
                , statisticslog);
    }
}
