package cl.ian.loopsteps;

import ec.EvolutionState;
import ec.util.Parameter;
import ec.util.ParameterDatabase;

import java.util.ArrayList;

/**
 * Created by Ian on 16/02/2016.
 */
public class LoopMaxTreeSize extends LoopCallable {


  public LoopMaxTreeSize(ParameterDatabase database, EvolutionState state, ArrayList<LoopCallable> loopSteps) {
    super(database, state, loopSteps, new double[]{50,100});
    parametersHeader.add("MaxSize=");
  }

  @Override
  public Void call() throws Exception {
    for (int i = 0; i < testValues.length; i++) {
      database.set(new Parameter("gp.koza.xover.maxsize"), "" + testValues[i]);
      database.set(new Parameter("gp.koza.mutate.maxsize"), "" + testValues[i]);
      parametersValue.set(index,String.format("%d", (int) testValues[i]));
      doExecutionOrContinueWithNextStep();
    }
    return null;
  }

}
