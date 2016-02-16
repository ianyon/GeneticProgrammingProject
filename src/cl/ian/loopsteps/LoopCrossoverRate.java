package cl.ian.loopsteps;

import ec.EvolutionState;
import ec.util.Parameter;
import ec.util.ParameterDatabase;

import java.util.ArrayList;

/**
 * Created by Ian on 16/02/2016.
 */
public class LoopCrossoverRate extends ECJCallable {


  public LoopCrossoverRate(ParameterDatabase database, EvolutionState state, ArrayList<ECJCallable> loopSteps, int index) {
    super(database, state, loopSteps, index);
    parametersHeader.set(index, "CrossoverRate=");
  }

  @Override
  public Void call() throws Exception {
    // Loop through the crossover rate 0.5:0.1:1.0
    for (float crossoverRate = 0.5f; crossoverRate <= 1.0; crossoverRate += 0.1f) {
      // Crossover rate
      database.set(new Parameter("pop.subpop.0.species.pipe.source.0.prob"), "" + crossoverRate);
      // Mutation rate
      database.set(new Parameter("pop.subpop.0.species.pipe.source.1.prob"), "" + (1 - crossoverRate));

      parametersValue.set(index, "" + crossoverRate);

      doExecutionOrContinueWithNextStep();
    }
    return null;
  }

}
