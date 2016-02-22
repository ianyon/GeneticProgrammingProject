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
  public final ArrayList<LoopCallable> loopSteps;
  public final int index;
  public ParameterDatabase database;
  public EvolutionState state;

  // Fields to show the actual loop information
  public static final ArrayList<String> parametersHeader = new ArrayList<>();
  public static final ArrayList<String> parametersValue = new ArrayList<>();

  private static int totalExecutionLoops;
  private static int completedExecutionLoops;
  private static double meanExecutionTime;
  private static double estimatedRemainingTime;

  public LoopCallable(ParameterDatabase database, EvolutionState state, ArrayList<LoopCallable> loopSteps, int index) {
    this.database = database;
    this.state = state;
    this.loopSteps = loopSteps;
    parametersValue.add("");
    this.index = index;
    completedExecutionLoops = 0;
    meanExecutionTime = 0;
    estimatedRemainingTime = 0;
  }

  public static ArrayList<LoopCallable> populateLoops(ParameterDatabase database, EvolutionState state) {
    ArrayList<LoopCallable> loopSteps = new ArrayList<>();
    loopSteps.add(new LoopPopulation(database, state, loopSteps, loopSteps.size()));
    loopSteps.add(new LoopCrossoverRate(database, state, loopSteps, loopSteps.size()));
    loopSteps.add(new LoopMaxInitialTreeDepth(database, state, loopSteps, loopSteps.size()));
    loopSteps.add(new LoopMaxTreeDepth(database, state, loopSteps, loopSteps.size()));
    loopSteps.add(new LoopDivMaxValue(database, state, loopSteps, loopSteps.size()));
    return loopSteps;
  }

  public static void InitiateLoops(ArrayList<LoopCallable> loopSteps) {
    try {
      loopSteps.get(0).call();
    } catch (Exception e) {
      System.out.println("Exception during loop call");
      e.printStackTrace();
      System.exit(-1);
    }
  }

  protected void doExecution() {
    printHeader();

    long startTime = System.nanoTime();
    state.run(EvolutionState.C_STARTED_FRESH);
    Evolve.cleanup(state);
    double thisTime = (System.nanoTime() - startTime) / 1000000000.0;
    completedExecutionLoops++;
    meanExecutionTime =
        (meanExecutionTime * (completedExecutionLoops - 1) + thisTime) / completedExecutionLoops;
    estimatedRemainingTime = Math.round(meanExecutionTime * (totalExecutionLoops - completedExecutionLoops));
    System.out.println("Execution time:" + thisTime + " s\n");
  }

  private void printHeader() {
    String message = "";
    for (int i = 0; i < parametersHeader.size(); i++)
      message += " " + parametersHeader.get(i) + parametersValue.get(i);

    // Delete the trailing comma
    String progressMessage = "Execution " + (completedExecutionLoops + 1) + "/" + totalExecutionLoops +
        " (" + 100 * completedExecutionLoops / totalExecutionLoops + "%)";

    // Format the remaining time
    if (completedExecutionLoops != 0) {
      int hr = (int) estimatedRemainingTime / 3600;
      int min = (int) (estimatedRemainingTime - 3600 * hr) / 60;
      int sec = (int) (estimatedRemainingTime - 3600 * hr - 60 * min);
      progressMessage += ": Estimated remaining time  " + String.format("%02d:%02d:%02d", hr, min, sec);
      hr = (int) (estimatedRemainingTime * 3) / 3600;
      min = (int) ((estimatedRemainingTime * 3) - 3600 * hr) / 60;
      sec = (int) ((estimatedRemainingTime * 3) - 3600 * hr - 60 * min);
      progressMessage += " (" + String.format("%02d:%02d:%02d", hr, min, sec) + ")";
    }
    System.out.println(progressMessage);

    System.out.println("\nParameters:" + message);
    database.set(new Parameter("stat.file.suffix"), message);
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
