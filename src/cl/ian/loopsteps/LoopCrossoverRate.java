package cl.ian.loopsteps;

import ec.EvolutionState;
import ec.util.Parameter;
import ec.util.ParameterDatabase;

import java.util.ArrayList;

/**
 * Created by Ian on 16/02/2016.
 */
public class LoopCrossoverRate extends LoopCallable {


  public LoopCrossoverRate(ParameterDatabase database, EvolutionState state, ArrayList<LoopCallable> loopSteps, int index) {
    super(database, state, loopSteps, index);
    parametersHeader.add("CrossoverRate=");
  }

  @Override
  public int numberOfLoops() {
    return 3;
  }


  @Override
  public Void call() throws Exception {
    // Loop through the crossover rate 0.7:0.1:0.9
    for (float crossoverRate = 0.7f; crossoverRate <= 0.9; crossoverRate += 0.1f) {
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
