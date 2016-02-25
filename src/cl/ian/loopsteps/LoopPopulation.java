package cl.ian.loopsteps;

import ec.EvolutionState;
import ec.util.Parameter;
import ec.util.ParameterDatabase;

import java.util.ArrayList;

/**
 * Created by Ian on 16/02/2016.
 */
public class LoopPopulation extends LoopCallable {


  public LoopPopulation(ParameterDatabase database, EvolutionState state, ArrayList<LoopCallable> loopSteps, int index) {
    super(database, state, loopSteps, index);
    parametersHeader.add("Pop=");
  }

  @Override
  public int numberOfLoops() {
    return 2;
  }

  @Override
  public Void call() throws Exception {
    // Loop through the population size 2000,4000
    for (int popSize = 2000; popSize <= 4000; popSize += 2000) {//TODO Cambiar valores
      // Population size
      database.set(new Parameter("pop.subpop.0.size"), "" + popSize);
      parametersValue.set(index, "" + popSize);

      doExecutionOrContinueWithNextStep();
    }
    return null;
  }

}
