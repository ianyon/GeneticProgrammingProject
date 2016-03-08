package cl.ian.loopsteps;

import ec.EvolutionState;
import ec.util.Parameter;
import ec.util.ParameterDatabase;

import java.util.ArrayList;

/**
 * Created by Ian on 16/02/2016.
 */
public class LoopCrossoverRate extends LoopCallable {


  public LoopCrossoverRate(ParameterDatabase database, EvolutionState state, ArrayList<LoopCallable> loopSteps) {
    // the last argument is the values to try
    //super(database, state, loopSteps, index, new double[]{0.8, 0.9, 1.0});
    super(database, state, loopSteps, new double[]{0.9});
    parametersHeader.add("Xover=");
  }

  @Override
  public Void call() throws Exception {
    for (int i = 0; i < testValues.length; i++) {
      // Crossover rate
      database.set(new Parameter("pop.subpop.0.species.pipe.source.0.prob"), "" + testValues[i]);
      // Mutation rate
      database.set(new Parameter("pop.subpop.0.species.pipe.source.1.prob"), "" + (1 - testValues[i]));

      parametersValue.set(index, String.format("%d%%", (int) (testValues[i] * 100)));

      doExecutionOrContinueWithNextStep();
    }
    return null;
  }
}
