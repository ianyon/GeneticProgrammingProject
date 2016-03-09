package cl.ian.loopsteps;

import ec.EvolutionState;
import ec.util.Parameter;
import ec.util.ParameterDatabase;

import java.util.ArrayList;

/**
 * Created by Ian on 16/02/2016.
 */
public class LoopMaxInitialTreeDepth extends LoopCallable {


  public LoopMaxInitialTreeDepth(ParameterDatabase database, EvolutionState state, ArrayList<LoopCallable> loopSteps) {
    super(database, state, loopSteps, new double[]{4, 6, 8, 10});
    parametersHeader.add("MaxIniDepth=");
  }


  @Override
  public Void call() throws Exception {
    for (int i = 0; i < testValues.length; i++) {
      // Maximum initial tree size
      database.set(new Parameter("gp.tc.0.init.max-depth"), "" + testValues[i]);
      database.set(new Parameter("gp.koza.grow.max-depth"), "" + testValues[i]);
      parametersValue.set(index, String.format("%d", (int) testValues[i]));

      doExecutionOrContinueWithNextStep();
    }
    return null;
  }

}
