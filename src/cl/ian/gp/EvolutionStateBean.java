package cl.ian.gp;

import ec.EvolutionState;
import ec.gp.ADFStack;
import ec.gp.GPIndividual;

/**
 * Created by Ian on 13/01/2016.
 */
public class EvolutionStateBean {
    public EvolutionState state;
    public int threadnum;
    public PhenomenologicalData input;
    public ADFStack stack;
    public PhenomenologicalModel phenomenologicalModel;

    public EvolutionStateBean(EvolutionState state, int threadnum, PhenomenologicalData input,
                              ADFStack stack, PhenomenologicalModel phenomenologicalModel) {
        this.state = state;
        this.threadnum = threadnum;
        this.input = input;
        this.stack = stack;
        this.phenomenologicalModel = phenomenologicalModel;
    }
}
