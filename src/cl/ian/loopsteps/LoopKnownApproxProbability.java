package cl.ian.loopsteps;

import ec.EvolutionState;
import ec.util.Parameter;
import ec.util.ParameterDatabase;

import java.util.ArrayList;

/**
 * Created by Ian on 16/02/2016.
 */
public class LoopKnownApproxProbability extends LoopCallable {

  public LoopKnownApproxProbability(ParameterDatabase database, EvolutionState state, ArrayList<LoopCallable> loopSteps) {
    // the last argument is the values to try
    super(database, state, loopSteps, new double[]{0.1, 0.25, 0.5, 0.75});
    parametersHeader.add("KnownProb=");
  }

  @Override
  public Void call() throws Exception {
    for (int i = 0; i < testValues.length; i++) {
      database.set(new Parameter("gp.tc.0.init.known-prob"), "" + testValues[i]);

      parametersValue.set(index, String.format("%d%%", (int) (testValues[i] * 100)));

      doExecutionOrContinueWithNextStep();
    }
    return null;
  }
}
