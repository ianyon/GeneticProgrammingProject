package cl.ian;

import ec.EvolutionState;
import ec.Evolve;
import ec.util.ParameterDatabase;

public class Main {

    public static void main(String[] args) {

        double meanTime;
        long startTime = System.nanoTime();

        ParameterDatabase database = Evolve.loadParameterDatabase(new String[]{"-file", "friction_factor.params"});
        EvolutionState state = Evolve.initialize(database, 0);
        state.run(EvolutionState.C_STARTED_FRESH);

        Evolve.cleanup(state);
        meanTime = (System.nanoTime() - startTime) / 1000000000.0;
        System.out.println("Tiempo ejecución: " + meanTime + " s");

        ParameterDatabase child = new ParameterDatabase();
        child.addParent(database);


    }

}
