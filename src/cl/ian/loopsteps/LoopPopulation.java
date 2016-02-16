package cl.ian.loopsteps;

import ec.EvolutionState;
import ec.util.Parameter;
import ec.util.ParameterDatabase;

import java.util.ArrayList;

/**
 * Created by Ian on 16/02/2016.
 */
public class LoopPopulation extends ECJCallable {


  public LoopPopulation(ParameterDatabase database, EvolutionState state, ArrayList<ECJCallable> loopSteps, int index) {
    super(database, state, loopSteps, index);
    parametersHeader.set(index, "Pop=");
  }

  @Override
  public Void call() throws Exception {
    // Loop through the population size 2000,4000
    for (int popSize = 200; popSize <= 400; popSize += 200) {//TODO Cambiar valores
      // Population size
      database.set(new Parameter("pop.subpop.0.size"), "" + popSize);
      parametersValue.set(index, "" + popSize);

      doExecutionOrContinueWithNextStep();
    }
    return null;
  }

}
