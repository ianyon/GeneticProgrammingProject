package cl.ian.loopsteps;

import cl.ian.Case;
import cl.ian.gp.HitLevelKozaFitness;
import cl.ian.gp.MyGPIndividual;
import cl.ian.gp.statistics.SimpleGPStatistics;
import ec.EvolutionState;
import ec.Evolve;
import ec.util.Parameter;
import ec.util.ParameterDatabase;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Callable;

/**
 * Created by Ian on 16/02/2016.
 */
public abstract class LoopCallable implements Callable {
  private static String summaryActualName;
  public final ArrayList<LoopCallable> loopSteps;
  public final int index;
  public ParameterDatabase database;
  public EvolutionState state;
  public double[] testValues;

  // Fields to show the actual loop information
  public static final ArrayList<String> parametersHeader = new ArrayList<>();
  /**
   * Name of the expression being executed. Initialized in populateLoops
   */
  private static Case expressionName;
  public static final ArrayList<String> parametersValue = new ArrayList<>();

  private static int totalLoops;
  private static int executedLoops;
  private static double avgExecutionTime;
  private static double estimatedRemainingTime;

  public static MyGPIndividual bestOfLoops;
  public static String headerBestOfLoops;
  public static String stringBestOfLoops;
  private static final String summaryFilename = "Summary";
  private static final String summaryExtension = ".stat";

  public LoopCallable(ParameterDatabase database, EvolutionState state, ArrayList<LoopCallable> loopSteps, int index,
                      double[] testValues) {
    this.database = database;
    this.state = state;
    this.loopSteps = loopSteps;
    parametersValue.add("");
    this.index = index;
    executedLoops = 0;
    avgExecutionTime = 0;
    estimatedRemainingTime = 0;

    this.testValues = testValues;
  }

  public static ArrayList<LoopCallable> populateLoops(ParameterDatabase database, EvolutionState state, Case expressionName) {
    LoopCallable.parametersHeader.clear();
    LoopCallable.parametersValue.clear();
    LoopCallable.expressionName = expressionName;

    ArrayList<LoopCallable> loopSteps = new ArrayList<>();
    loopSteps.add(new LoopElitism(database, state, loopSteps, loopSteps.size()));
    loopSteps.add(new LoopPopulation(database, state, loopSteps, loopSteps.size()));
    loopSteps.add(new LoopCrossoverRate(database, state, loopSteps, loopSteps.size()));
    loopSteps.add(new LoopMaxInitialTreeDepth(database, state, loopSteps, loopSteps.size()));
    loopSteps.add(new LoopMaxTreeDepth(database, state, loopSteps, loopSteps.size()));
    loopSteps.add(new LoopDivMaxValue(database, state, loopSteps, loopSteps.size()));
    return loopSteps;
  }

  /**
   * Initiate the execution of the loops
   *
   * @param loopSteps
   */
  public static void InitiateLoops(ArrayList<LoopCallable> loopSteps) {

    // Create a new summary file
    summaryActualName = String.format("%s %s %s%s", summaryFilename, expressionName,
        new SimpleDateFormat("yyyyMMddhhmm").format(new Date()), summaryExtension);
    try {
      new File(summaryActualName).createNewFile();
      Files.write(Paths.get(summaryActualName), (expressionName + "\n\n").getBytes(), StandardOpenOption.APPEND);
    } catch (IOException e) {
      loopSteps.get(0).state.output.warning("Couldn't create and or write to summary file");
      e.printStackTrace();
    }

    try {
      loopSteps.get(0).call();
    } catch (Exception e) {
      System.out.println("Exception during loop call");
      e.printStackTrace();
      System.exit(-1);
    }

    // Print the final results
    String finalMessage = String.format("\n\nBest of all loops is: %s\n%s", headerBestOfLoops, stringBestOfLoops);
    try {
      Files.write(Paths.get(summaryActualName), finalMessage.getBytes(), StandardOpenOption.APPEND);
    } catch (IOException e) {
      loopSteps.get(0).state.output.warning("Couldn't write summary for best of all loops");
    }
  }

  protected void doExecution() {
    String paramIdentifier = printHeader();

    actualExecution();

    // Print the info to the summary file and check for the best of all time
    final MyGPIndividual bestIndLastLoop = (MyGPIndividual) ((SimpleGPStatistics) state.statistics).best_of_run[0];
    final String bestMessage = String.format("%s\n%s\n%s", paramIdentifier,
        bestIndLastLoop.fitness.fitnessToStringForHumans(), bestIndLastLoop.stringRootedTreeForHumans());

    try {
      Files.write(Paths.get(summaryActualName),
          String.format("\nBest fitness of run: %s\n", bestMessage).getBytes(), StandardOpenOption.APPEND);
    } catch (IOException e) {
      state.output.warning("Couldn't write summary for last loop");
    }

    if (bestOfLoops == null || ((HitLevelKozaFitness) bestIndLastLoop.fitness).errorBetterThan(bestOfLoops.fitness)) {
      bestOfLoops = bestIndLastLoop;
      headerBestOfLoops = paramIdentifier;
      stringBestOfLoops = bestMessage;
    }
  }

  private void actualExecution() {
    long startTime = System.nanoTime();
    state.run(EvolutionState.C_STARTED_FRESH);
    Evolve.cleanup(state);
    double thisTime = (System.nanoTime() - startTime) / 1000000000.0;

    executedLoops++;
    avgExecutionTime = (avgExecutionTime * (executedLoops - 1) + thisTime) / executedLoops;
    estimatedRemainingTime = avgExecutionTime * (totalLoops - executedLoops);
    System.out.println(expressionName + " Execution time:" + thisTime + " s\n");
  }

  private String printHeader() {
    String paramIdentifier = "";
    for (int i = 0; i < parametersHeader.size(); i++)
      paramIdentifier += " " + parametersHeader.get(i) + parametersValue.get(i);

    // Delete the trailing comma
    String progressMessage = String.format("%s Execution %d/%d (%d%%)", expressionName, executedLoops + 1, totalLoops,
        100 * executedLoops / totalLoops);

    // Format the remaining time
    if (executedLoops != 0) {
      progressMessage += ": Estimated remaining time  " + getFormattedTime(estimatedRemainingTime);
      progressMessage += getRemainingTimeAllExpr();
    }
    System.out.println(progressMessage);

    System.out.println("\nParameters:" + paramIdentifier);
    setStatisticFilesIdentifier(paramIdentifier);
    return paramIdentifier;
  }

  private String getRemainingTimeAllExpr() {
    int remainingExpr;

    switch (expressionName) {
      case FRICTION_FACTOR:
        remainingExpr = 2;
        break;
      case DRAG_COEFFICIENT:
        remainingExpr = 1;
        break;
      case NUSSELT_NUMBER:
      default:
        remainingExpr = 0;
        break;
    }

    final double estimatedTimeAllLoops = estimatedRemainingTime + remainingExpr * avgExecutionTime * totalLoops;
    return " (" + getFormattedTime(estimatedTimeAllLoops) + ")";
  }

  private String getFormattedTime(double estimatedTimeAllLoops) {
    int hr;
    int min;
    int sec;
    hr = (int) estimatedTimeAllLoops / 3600;
    min = (int) (estimatedTimeAllLoops - 3600 * hr) / 60;
    sec = (int) (estimatedTimeAllLoops - 3600 * hr - 60 * min);
    return String.format("' ('%02d:%02d:%02d')'", hr, min, sec);
  }

  private void setStatisticFilesIdentifier(String message) {
    // Set the identifier for the file used in SimpleGPStatistics
    database.set(new Parameter("stat.file.suffix"), message);
    // Set the identifier for the file used in MyKozaShortStatistics
    //database.set(new Parameter("stat.child.0.file.suffix"), message);
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

  /**
   * Total number of loops executions in an array of LoopCallables
   */
  public static int totalChainedLoops(ArrayList<LoopCallable> loopSteps) {
    int acc = 1;
    for (LoopCallable step : loopSteps) {
      acc *= step.numberOfLoops();
    }
    totalLoops = acc;
    return acc;
  }

  /**
   * Returns the number of loops of this callable as the size of the values to test
   */
  public final int numberOfLoops() {
    return testValues.length;
  }
}
