package cl.ian.loopsteps;

import ec.EvolutionState;
import ec.util.Parameter;
import ec.util.ParameterDatabase;

import java.util.ArrayList;

/**
 * Created by Ian on 16/02/2016.
 */
public class LoopDivMaxValue extends LoopCallable {


  public LoopDivMaxValue(ParameterDatabase database, EvolutionState state, ArrayList<LoopCallable> loopSteps, int index) {
    super(database, state, loopSteps, index);
    parametersHeader.add("DivMax=");
  }

  @Override
  public int numberOfLoops() {
    return 2;
  }


  @Override
  public Void call() throws Exception {
    // Change Div function to return Double.MAX_VALUE instead of x1
    for (int useMaxDoubleDiv = 0; useMaxDoubleDiv <= 1; useMaxDoubleDiv++) {
      String value;
      if (useMaxDoubleDiv == 0) value = "cl.ian.gp.nodes.Div";
      else value = "cl.ian.gp.nodes.DivMaxDouble";

      database.set(new Parameter("gp.fs.0.func.3"), value);
      parametersValue.set(index, "" + (useMaxDoubleDiv == 1));

      doExecutionOrContinueWithNextStep();
    }
    return null;
  }

}
