package cl.ian.loopsteps;

import ec.EvolutionState;
import ec.util.Parameter;
import ec.util.ParameterDatabase;

import java.util.ArrayList;

/**
 * Created by Ian on 16/02/2016.
 */
public class LoopMaxTreeDepth extends ECJCallable {


  public LoopMaxTreeDepth(ParameterDatabase database, EvolutionState state, ArrayList<ECJCallable> loopSteps, int index) {
    super(database, state, loopSteps, index);
    parametersHeader.set(index, "MaxDepth=");
  }

  @Override
  public Void call() throws Exception {
    // Maximum tree depth
    for (int maxTreeDepth = 15; maxTreeDepth <= 19; maxTreeDepth += 2) {
      database.set(new Parameter("gp.koza.xover.maxdepth"), "" + maxTreeDepth);
      parametersValue.set(index, "" + maxTreeDepth);
      doExecutionOrContinueWithNextStep();
    }
    return null;
  }

}
