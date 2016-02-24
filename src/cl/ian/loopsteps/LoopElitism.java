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
    super(database, state, loopSteps, index);
    parametersHeader.add("Elitism=");
  }

  @Override
  public int numberOfLoops() {
    return 3;
  }


  @Override
  public Void call() throws Exception {
    eliteCount();
    //eliteFraction();
    return null;
  }

  /**
   * Expresses elitism in terms of a factor of the population
   * @throws Exception
   */
  private void eliteFraction() throws Exception {
    float[] eliteFactorValues = new float[]{0, 0.001f, 0.05f};
    for (int eliteFactor = 0; eliteFactor <= eliteFactorValues.length; eliteFactor++) {
      database.set(new Parameter("breed.elite-fraction.0"), "" + eliteFactorValues[eliteFactor]);
      parametersValue.set(index, "" + eliteFactorValues[eliteFactor] * 100 + "%");

      doExecutionOrContinueWithNextStep();
    }
  }

  /**
   * Expresses elitism in terms of number of individuals of the population
   * @throws Exception
   */
  private void eliteCount() throws Exception {
    int[] eliteCountValues = new int[]{0, 5, 50};
    for (int eliteCount = 0; eliteCount <= eliteCountValues.length; eliteCount++) {
      database.set(new Parameter("breed.elite.0"), "" + eliteCountValues[eliteCount]);
      parametersValue.set(index, "" + eliteCountValues[eliteCount]);
      doExecutionOrContinueWithNextStep();
    }
  }

}
