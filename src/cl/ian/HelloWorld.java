package cl.ian;

import ec.EvolutionState;
import ec.Evolve;
import ec.util.ParameterDatabase;

public class HelloWorld {
    public static final String from = "(i)", to = "";

    public static void main(String[] args) {

        args = new String[]{"-file", "src/cl/ian/gp/base.params"};
        ParameterDatabase database = Evolve.loadParameterDatabase(args);
        EvolutionState state = Evolve.initialize(database, 0);
        state.run(EvolutionState.C_STARTED_FRESH);
        Evolve.cleanup(state);
    }

}
