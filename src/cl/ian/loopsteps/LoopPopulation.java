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
    super(database, state, loopSteps, index, new double[]{2000,4000});
    parametersHeader.add("Pop=");
  }

  @Override
  public Void call() throws Exception {
    for (int i = 0; i < testValues.length; i++) {
      // Population size
      database.set(new Parameter("pop.subpop.0.size"), "" + testValues[i]);
      parametersValue.set(index, "" + testValues[i]);

      doExecutionOrContinueWithNextStep();
    }
    return null;
  }

}
