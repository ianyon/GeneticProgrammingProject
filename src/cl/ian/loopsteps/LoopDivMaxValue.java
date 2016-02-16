package cl.ian.loopsteps;

import ec.EvolutionState;
import ec.util.Parameter;
import ec.util.ParameterDatabase;

import java.util.ArrayList;

/**
 * Created by Ian on 16/02/2016.
 */
public class LoopDivMaxValue extends ECJCallable {


  public LoopDivMaxValue(ParameterDatabase database, EvolutionState state, ArrayList<ECJCallable> loopSteps, int index) {
    super(database, state, loopSteps, index);
    parametersHeader.set(index, "UseDivMaxVal=");
  }

  @Override
  public Void call() throws Exception {
    // Change Div function to return Double.MAX_VALUE instead of x1
    for (int useMaxDoubleDiv = 0; useMaxDoubleDiv <= 1; useMaxDoubleDiv++) {
      String value;
      if (useMaxDoubleDiv == 0) value = "cl.ian.gp.nodes.Div";
      else value = "cl.ian.gp.nodes.DivMaxDouble";

      database.set(new Parameter("gp.fs.0.func.3"), value);
      parametersValue.set(index, "" + value);

      doExecutionOrContinueWithNextStep();
    }
    return null;
  }

}
