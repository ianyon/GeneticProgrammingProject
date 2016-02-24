package cl.ian.gp;

import cl.ian.GeneralModelEvaluator;
import cl.ian.InputVariables;
import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPIndividual;
import ec.simple.SimpleProblemForm;
import ec.util.Parameter;

public class PhenomenologicalModelVerticalSlicing extends PhenomenologicalModel implements SimpleProblemForm {
  private static final long serialVersionUID = 1;

  public static final String P_NUMBER_OF_SLICES = "number-of-slices";
  public int slicesCount;
  private int slice;
  private int[] sliceLimits;
  private static int lastGeneration;

  /******************************************************************************************************************/
  // don't bother cloning the inputs and outputs; they're read-only :-)
  // don't bother cloning the currentValue; it's transitory
  public void setup(final EvolutionState state, final Parameter base) {
    super.setup(state, base);

    slicesCount = state.parameters.getInt(base.push(P_NUMBER_OF_SLICES), null, 1);
    if (slicesCount < 1)
      state.output.fatal("Training Set Size must be an integer greater than 0", base.push(P_NUMBER_OF_SLICES));

    sliceLimits = new int[slicesCount + 1];
    for (int i = 0; i <= slicesCount; i++)
      sliceLimits[i] = (int) (i * Math.floor(inputs.length / slicesCount));

    // Used to mark the advance in time
    lastGeneration = -1;
  }

  protected int getTestedElementsCount() {
    return sliceLimits[slice + 1] - sliceLimits[slice];
  }

  protected void updateControlVariables(EvolutionState state, int threadnum) {
    // We advanced to a new generation so reset the random slice
    if (state.generation != lastGeneration) {
      slice = state.random[threadnum].nextInt(slicesCount);
      lastGeneration = state.generation;
    }
  }

  protected int initLoop() {
    return sliceLimits[slice];
  }

  protected int endLoop() {
    return sliceLimits[slice + 1];
  }
}

