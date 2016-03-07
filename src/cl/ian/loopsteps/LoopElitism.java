package cl.ian.loopsteps;

import ec.EvolutionState;
import ec.util.Parameter;
import ec.util.ParameterDatabase;

import java.util.ArrayList;

/**
 * Created by Ian on 16/02/2016.
 */
public class LoopElitism extends LoopCallable {


  public LoopElitism(ParameterDatabase database, EvolutionState state, ArrayList<LoopCallable> loopSteps, int index) {
    // Use elite factor
    super(database, state, loopSteps, index, new double[]{0.001, 0, 0.05});
    // Use elite count
    //super(database, state, loopSteps, index, new double[]{5, 0, 50});
    parametersHeader.add("Elite=");
  }

  @Override
  public Void call() throws Exception {
    //eliteCount();
    eliteFraction();
    return null;
  }

  /**
   * Expresses elitism in terms of a factor of the population
   *
   * @throws Exception
   */
  private void eliteFraction() {
    for (int i = 0; i < testValues.length; i++) {
      database.set(new Parameter("breed.elite-fraction.0"), "" + testValues[i]);
      parametersValue.set(index, String.format("%d%%", (int) testValues[i] * 100));

      doExecutionOrContinueWithNextStep();
    }
  }

  /**
   * Expresses elitism in terms of number of individuals of the population
   *
   * @throws Exception
   */
  private void eliteCount() {
    for (int i = 0; i < testValues.length; i++) {
      database.set(new Parameter("breed.elite.0"), "" + testValues[i]);
      parametersValue.set(index, "" + testValues[i]);
      doExecutionOrContinueWithNextStep();
    }
  }

}
