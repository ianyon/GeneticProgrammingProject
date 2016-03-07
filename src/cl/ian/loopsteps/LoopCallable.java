package cl.ian.loopsteps;

import cl.ian.Case;
import cl.ian.Main;
import cl.ian.SummaryFile;
import cl.ian.gp.MyGPIndividual;
import cl.ian.gp.statistics.SimpleGPStatistics;
import ec.EvolutionState;
import ec.Evolve;
import ec.util.Parameter;
import ec.util.ParameterDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.Callable;

import static cl.ian.Main.elapsed;

/**
 * Created by Ian on 16/02/2016.
 */
public abstract class LoopCallable implements Callable {
  private static boolean execute = false;
  private final ArrayList<LoopCallable> loopSteps;
  protected final int index;
  protected final ParameterDatabase database;
  private final EvolutionState state;
  protected final double[] testValues;

  // Fields to show the actual loop information
  protected static final ArrayList<String> parametersHeader = new ArrayList<>();
  /**
   * Name of the expression being executed. Initialized in populateLoops
   */
  private static Case expressionName;
  protected static final ArrayList<String> parametersValue = new ArrayList<>();

  private static int totalLoops;
  private static int executedLoops;
  private static double avgExecutionTime;
  private static double estimatedRemainingTime;

  private static MyGPIndividual bestOfLoops;
  private static String headerBestOfLoops;
  private static String stringBestOfLoops;

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
    // We are using 100 elites
    //loopSteps.add(new LoopElitism(database, state, loopSteps, loopSteps.size()));
    loopSteps.add(new LoopPopulation(database, state, loopSteps, loopSteps.size()));
    loopSteps.add(new LoopCrossoverRate(database, state, loopSteps, loopSteps.size()));
    loopSteps.add(new LoopMaxInitialTreeDepth(database, state, loopSteps, loopSteps.size()));
    loopSteps.add(new LoopMaxTreeDepth(database, state, loopSteps, loopSteps.size()));
    // The div max doesn't make any difference in the results
    //loopSteps.add(new LoopDivMaxValue(database, state, loopSteps, loopSteps.size()));
    return loopSteps;
  }

  /**
   * Initiate the execution of the loops
   *
   * @param loopSteps
   * @param state
   * @param nameAndFile
   */
  public static void initiateLoops(ArrayList<LoopCallable> loopSteps, EvolutionState state, String[] nameAndFile) {
    if (!execute) {
      StringBuilder s = new StringBuilder();
      for (LoopCallable loopStep : loopSteps) s.append(" " + loopStep.loopIdentifier());
      state.output.println(String.format("Running the following loops [%s]: %s",
          LoopCallable.totalChainedLoops(loopSteps), s.toString()), 0);
      state.output.println("Would you like to continue? (y/n): ", 0);
      Scanner input = new Scanner(System.in);
      if (input.nextLine().equalsIgnoreCase("n")) {
        System.out.println("\n\nNot executing.");
        System.exit(0);
      }
    }

    state.output.println(nameAndFile[1] + ", number of loops to run: " + LoopCallable.totalChainedLoops(loopSteps), 0);
    long startTime = System.nanoTime();

    SummaryFile.createSummaryFile(expressionName);

    try {
      loopSteps.get(0).call();
    } catch (Exception e) {
      System.out.println("Exception during loop call");
      e.printStackTrace();
      System.exit(-1);
    }

    // Print the final results
    SummaryFile.writeToSummary(String.format("\n\nBest of all loops is: %s\n%s", headerBestOfLoops, stringBestOfLoops),
        expressionName);

    state.output.println(String.format("Finished %s (%g s)", nameAndFile[1], elapsed(startTime)), 0);
  }

  protected void doExecution() {
    String paramIdentifier = printHeader();
    double thisTime = averagedExecution(paramIdentifier);

    executedLoops++;
    avgExecutionTime = (avgExecutionTime * (executedLoops - 1) + thisTime) / executedLoops;
    estimatedRemainingTime = avgExecutionTime * (totalLoops - executedLoops);
    System.out.println(expressionName + " Execution time: " + thisTime + " s\n");


  }

  private double averagedExecution(String paramIdentifier) {

    final SimpleGPStatistics statistics = (SimpleGPStatistics) state.statistics;
    MyGPIndividual bestInd = null;
    MyGPIndividual bestValInd = null;
    MyGPIndividual bestTestInd = null;

    double avgTime = 0;
    for (int i = 0; i < 3; i++) {
      long startTime = System.nanoTime();
      state.run(EvolutionState.C_STARTED_FRESH);
      Evolve.cleanup(state);
      avgTime += elapsed(startTime);

      // Print the info to the summary file and check for the best of all time
      bestInd = MyGPIndividual.getErrorBest(bestInd, statistics.getBestSoFar()[0]);
      bestValInd = MyGPIndividual.getErrorBest(bestValInd, statistics.best_of_validation);
      bestTestInd = MyGPIndividual.getErrorBest(bestTestInd, statistics.best_of_test);
    }

    final String bestMessage = String.format("%s\n%s", paramIdentifier, bestInd.fitnessAndTree());
    final String bestValMessage = String.format("%s (Test= %s)\n%s",
        bestValInd.fitness.fitnessToStringForHumans(),
        bestTestInd.fitness.fitnessToStringForHumans(),
        bestValInd.stringRootedTreeForHumans());

    SummaryFile.writeToSummary(String.format("\nBest fitness of run: %s\n", bestMessage), expressionName);
    SummaryFile.writeToSummary(String.format("\nValidation: %s\n", bestValMessage), expressionName);

    bestOfLoops = MyGPIndividual.getErrorBest(bestOfLoops, bestTestInd);
    headerBestOfLoops = paramIdentifier;
    stringBestOfLoops = bestMessage;

    return avgTime / 3.0;
  }

  private String printHeader() {
    String paramIdentifier = "";
    for (int i = 0; i < parametersHeader.size(); i++)
      paramIdentifier += " " + parametersHeader.get(i) + parametersValue.get(i);

    String progressMessage = String.format("%s Execution %d/%d (%d%%)", expressionName, executedLoops + 1, totalLoops,
        Math.round(100 * executedLoops / totalLoops));

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
    return String.format("%02d:%02d:%02d", hr, min, sec);
  }

  private void setStatisticFilesIdentifier(String message) {
    // Set the identifier for the file used in SimpleGPStatistics
    database.set(new Parameter("stat.file.suffix"), message);
    // Set the identifier for the file used in MyKozaShortStatistics
    //database.set(new Parameter("stat.child.0.file.suffix"), message);
  }

  protected void doExecutionOrContinueWithNextStep() {
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

  public String loopIdentifier() {
    return parametersHeader.get(index) + Arrays.toString(testValues);
  }
}
