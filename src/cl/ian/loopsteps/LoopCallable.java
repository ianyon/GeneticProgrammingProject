package cl.ian.loopsteps;

import ec.EvolutionState;
import ec.Evolve;
import ec.util.Parameter;
import ec.util.ParameterDatabase;

import java.util.ArrayList;
import java.util.concurrent.Callable;

/**
 * Created by Ian on 16/02/2016.
 */
public abstract class LoopCallable implements Callable {
  private static int totalExecutionLoops;
  private static int completedExecutionLoops;
  public final ArrayList<LoopCallable> loopSteps;
  public final int index;
  public ParameterDatabase database;
  public EvolutionState state;

  // Fields to show the actual loop information
  public static final ArrayList<String> parametersHeader = new ArrayList<>();
  public static final ArrayList<String> parametersValue = new ArrayList<>();

  public LoopCallable(ParameterDatabase database, EvolutionState state, ArrayList<LoopCallable> loopSteps, int index) {
    this.database = database;
    this.state = state;
    this.loopSteps = loopSteps;
    parametersHeader.add("");
    parametersValue.add("");
    this.index = index;
    completedExecutionLoops = 0;
  }

  protected void doExecution() {
    printHeader();

    long startTime = System.nanoTime();
    state.run(EvolutionState.C_STARTED_FRESH);
    Evolve.cleanup(state);
    System.out.println("Tiempo ejecución:" + (System.nanoTime() - startTime) / 1000000000.0 + " s\n");
    completedExecutionLoops++;
  }

  private void printHeader() {
    String message = "";
    for (int i = 0; i < parametersHeader.size(); i++)
      message += " " + parametersHeader.get(i) + parametersValue.get(i) + ",";

    // Delete the trailing comma
    System.out.println("Ejecución " + (completedExecutionLoops + 1) + "/" + totalExecutionLoops +
        " (" + 100 * completedExecutionLoops / totalExecutionLoops + "%)");
    System.out.println("Parámetros:" + message.substring(0, message.length() - 1));
    database.set(new Parameter("stat.file.suffix"), message.substring(0, message.length() - 1));
  }

  protected void doExecutionOrContinueWithNextStep() throws Exception {
    if (loopSteps.size() > index + 1) {
      try {
        loopSteps.get(index + 1).call();
      } catch (Exception e) {
        System.out.println("Exception during loop call");
        e.printStackTrace();
        System.exit(-1);
      }
    } else
      doExecution();
  }

  public static int totalChainedLoops(ArrayList<LoopCallable> loopSteps) {
    int acc = 1;
    for (LoopCallable step : loopSteps) {
      acc *= step.numberOfLoops();
    }
    totalExecutionLoops = acc;
    return acc;
  }

  public abstract int numberOfLoops();
}
