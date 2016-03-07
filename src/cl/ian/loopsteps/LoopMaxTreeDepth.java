package cl.ian.loopsteps;

import ec.EvolutionState;
import ec.util.Parameter;
import ec.util.ParameterDatabase;

import java.util.ArrayList;

/**
 * Created by Ian on 16/02/2016.
 */
public class LoopMaxTreeDepth extends LoopCallable {


  public LoopMaxTreeDepth(ParameterDatabase database, EvolutionState state, ArrayList<LoopCallable> loopSteps, int index) {
    super(database, state, loopSteps, index, new double[]{17,19});
    parametersHeader.add("MaxDepth=");
  }

  @Override
  public Void call() throws Exception {
    for (int i = 0; i < testValues.length; i++) {
      database.set(new Parameter("gp.koza.xover.maxdepth"), "" + testValues[i]);
      parametersValue.set(index,String.format("%d", (int) testValues[i]));
      doExecutionOrContinueWithNextStep();
    }
    return null;
  }

}
