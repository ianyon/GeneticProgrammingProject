package cl.ian.gp;

import ec.EvolutionState;
import ec.app.regression.RegressionData;
import ec.gp.ADFStack;

/**
 * Created by Ian on 13/01/2016.
 *
 * This class is used only to avoid spamming arguments in some function calls
 */
public class EvolutionStateBean {
    public EvolutionState state;
    public int threadNumber;
    public RegressionData input;
    public ADFStack stack;
    public PhenomenologicalModel phenomenologicalModel;

    public void set(EvolutionState state, int threadNumber, RegressionData input,
                    ADFStack stack, PhenomenologicalModel phenomenologicalModel) {
        this.state = state;
        this.threadNumber = threadNumber;
        this.input = input;
        this.stack = stack;
        this.phenomenologicalModel = phenomenologicalModel;
    }
}
