package cl.ian.loopsteps;

import ec.EvolutionState;
import ec.util.Parameter;
import ec.util.ParameterDatabase;

import java.util.ArrayList;

/**
 * Created by Ian on 16/02/2016.
 */
public class LoopMaxInitialTreeDepth extends LoopCallable {


  public LoopMaxInitialTreeDepth(ParameterDatabase database, EvolutionState state, ArrayList<LoopCallable> loopSteps, int index) {
    super(database, state, loopSteps, index);
    parametersHeader.add("MaxInitDepth=");
  }

  @Override
  public int numberOfLoops() {
    return 2;
  }

  @Override
  public Void call() throws Exception {
    // Loop through the maximum initial tree size 6:2:8
    for (int maxInitialTreeDepth = 6; maxInitialTreeDepth <= 8; maxInitialTreeDepth += 2) {
      // Maximum initial tree size
      database.set(new Parameter("gp.tc.0.init.max-depth"), "" + maxInitialTreeDepth);
      parametersValue.set(index, "" + maxInitialTreeDepth);

      doExecutionOrContinueWithNextStep();
    }
    return null;
  }

}
