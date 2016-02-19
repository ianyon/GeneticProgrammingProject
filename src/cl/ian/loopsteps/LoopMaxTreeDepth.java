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
    super(database, state, loopSteps, index);
    parametersHeader.set(index, "MaxDepth=");
  }

  @Override
  public int numberOfLoops() {
    return 2;
  }

  @Override
  public Void call() throws Exception {
    // Maximum tree depth 15:2:17
    for (int maxTreeDepth = 15; maxTreeDepth <= 17; maxTreeDepth += 2) {
      database.set(new Parameter("gp.koza.xover.maxdepth"), "" + maxTreeDepth);
      parametersValue.set(index, "" + maxTreeDepth);
      doExecutionOrContinueWithNextStep();
    }
    return null;
  }

}
