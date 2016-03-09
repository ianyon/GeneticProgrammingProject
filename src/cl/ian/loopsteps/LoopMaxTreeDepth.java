package cl.ian.loopsteps;

import ec.EvolutionState;
import ec.util.Parameter;
import ec.util.ParameterDatabase;

import java.util.ArrayList;

/**
 * Created by Ian on 16/02/2016.
 */
public class LoopMaxTreeDepth extends LoopCallable {


  public LoopMaxTreeDepth(ParameterDatabase database, EvolutionState state, ArrayList<LoopCallable> loopSteps) {
    super(database, state, loopSteps, new double[]{13, 15, 17, 19, 20});
    parametersHeader.add("MaxDepth=");
  }

  @Override
  public Void call() throws Exception {
    for (int i = 0; i < testValues.length; i++) {
      database.set(new Parameter("gp.koza.xover.maxdepth"), "" + testValues[i]);
      database.set(new Parameter("gp.koza.mutate.maxdepth"), "" + testValues[i]);
      parametersValue.set(index, String.format("%d", (int) testValues[i]));
      doExecutionOrContinueWithNextStep();
    }
    return null;
  }

}
