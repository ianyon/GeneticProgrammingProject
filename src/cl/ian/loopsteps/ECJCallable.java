package cl.ian.loopsteps;

import ec.EvolutionState;
import ec.Evolve;
import ec.util.ParameterDatabase;

import java.util.ArrayList;
import java.util.concurrent.Callable;

/**
 * Created by Ian on 16/02/2016.
 */
public abstract class ECJCallable implements Callable {
  public final ArrayList<ECJCallable> loopSteps;
  public final int index;
  public final ArrayList<String> parametersValue = new ArrayList<>();
  public ParameterDatabase database;
  public EvolutionState state;
  //public String parametersHeader;
  public static final ArrayList<String> parametersHeader = new ArrayList<>();

  public ECJCallable(ParameterDatabase database, EvolutionState state, ArrayList<ECJCallable> loopSteps, int index) {
    this.database = database;
    this.state = state;
    this.loopSteps = loopSteps;
    parametersHeader.add("");
    parametersValue.add("");
    this.index = index;
  }

  protected void doExecution() {
    printHeader();

    long startTime = System.nanoTime();
    state.run(EvolutionState.C_STARTED_FRESH);
    Evolve.cleanup(state);
    System.out.println("Tiempo ejecución:" + (System.nanoTime() - startTime) / 1000000000.0 + " s");
  }

  private void printHeader() {
    String message = "";
    for (int i = 0; i < parametersHeader.size(); i++)
      message += " " + parametersHeader.get(i) + parametersValue.get(i) + ",";

    // Delete the trailing comma
    System.out.println("Parámetros:" + message.substring(0, message.length() - 2));
  }

  protected void doExecutionOrContinueWithNextStep() throws Exception {
    if (loopSteps.size() > index + 1) loopSteps.get(index + 1).call();
    else doExecution();
  }
}
