package cl.ian;

import cl.ian.gp.MyGPIndividual;
import cl.ian.gp.statistics.SimpleGPStatistics;
import cl.ian.loopsteps.LoopCallable;
import ec.EvolutionState;
import ec.Evolve;
import ec.util.Parameter;
import ec.util.ParameterDatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;

public class Main {

  private static final Map<Case, MyGPIndividual> bestOfRuns = new EnumMap<>(Case.class);

  public static void main(String[] args) {
    if (args.length > 0 && args[0].equalsIgnoreCase("best")) {
      doBest();
      return;
    }

    createDirIfNotExist();

    executeExpression(Case.FRICTION_FACTOR);
    executeExpression(Case.DRAG_COEFFICIENT);
    executeExpression(Case.NUSSELT_NUMBER);
  }

  private static void doBest() {
    SummaryFile.createSummaryFile(Case.FRICTION_FACTOR);
    SummaryFile.createSummaryFile(Case.DRAG_COEFFICIENT);
    SummaryFile.createSummaryFile(Case.NUSSELT_NUMBER);

    // Run it 10 times to average results
    for (int i = 0; i < 5; i++) {
      doBestOnce();
    }

    String bestMessage = bestOfRuns.get(Case.FRICTION_FACTOR).fitnessAndTree();
    SummaryFile.writeToSummary(String.format("\nBest fitness of all: %s\n", bestMessage), Case.FRICTION_FACTOR);
    bestMessage = bestOfRuns.get(Case.DRAG_COEFFICIENT).fitnessAndTree();
    SummaryFile.writeToSummary(String.format("\nBest fitness of all: %s\n", bestMessage), Case.DRAG_COEFFICIENT);
    bestMessage = bestOfRuns.get(Case.NUSSELT_NUMBER).fitnessAndTree();
    SummaryFile.writeToSummary(String.format("\nBest fitness of all: %s\n", bestMessage), Case.NUSSELT_NUMBER);
  }

  private static void doBestOnce() {
    System.out.println("Initiated Best parameters evolution");

    runExpressionOnce(Case.FRICTION_FACTOR);
    runExpressionOnce(Case.DRAG_COEFFICIENT);
    runExpressionOnce(Case.NUSSELT_NUMBER);
  }

  private static void runExpressionOnce(Case expressionCase) {
    String[] nameAndFile = getNameAndFile(expressionCase, true);
    ParameterDatabase database = Evolve.loadParameterDatabase(new String[]{"-file", nameAndFile[0]});
    database.set(new Parameter("stat.file.suffix"), "Best parameters");
    database.set(new Parameter("gp.tc.0.init.use-known-approx"), expressionCase.text);
    EvolutionState state = Evolve.initialize(database, 0);
    long startTime = System.nanoTime();
    state.run(EvolutionState.C_STARTED_FRESH);
    Evolve.cleanup(state);
    System.out.println(String.format("Finished %s (%g s)", nameAndFile[1], elapsed(startTime)));

    final MyGPIndividual bestInd = ((SimpleGPStatistics) state.statistics).getBestSoFar()[0];
    final String bestMessage = String.format("%s\n%s",
        bestInd.fitness.fitnessToStringForHumans(), bestInd.stringRootedTreeForHumans());

    SummaryFile.writeToSummary(String.format("\nBest fitness of run: %s\n", bestMessage), expressionCase);

    bestOfRuns.put(expressionCase, MyGPIndividual.getErrorBest(bestOfRuns.get(expressionCase), bestInd));
  }

  public static double elapsed(long startTime) {
    return (System.nanoTime() - startTime) / 1.0E9;
  }

  private static String[] getNameAndFile(Case expressionCase, boolean best) {
    String[] nameAndFile = new String[2];

    switch (expressionCase) {
      case FRICTION_FACTOR:
        nameAndFile[0] = best ? "params/best_friction_factor.params" : "params/friction_factor.params";
        nameAndFile[1] = "Friction factor";
        break;
      case DRAG_COEFFICIENT:
        nameAndFile[0] = best ? "params/best_drag_coef.params" : "params/drag_coef.params";
        nameAndFile[1] = "Drag Coefficient";
        break;
      case NUSSELT_NUMBER:
      default:
        nameAndFile[0] = best ? "params/best_nusselt_number.params" : "params/nusselt_number.params";
        nameAndFile[1] = "Nusselt Number";
        break;
    }
    return nameAndFile;
  }

  private static void executeExpression(Case exprCase) {
    String[] nameAndFile = getNameAndFile(exprCase, false);
    ParameterDatabase database = Evolve.loadParameterDatabase(new String[]{"-file", nameAndFile[0]});
    database.set(new Parameter("gp.tc.0.init.use-known-approx"), exprCase.text);
    EvolutionState state = Evolve.initialize(database, 0);
    ArrayList<LoopCallable> loopSteps = LoopCallable.populateLoops(database, state, exprCase);
    System.out.println(nameAndFile[1] + ", number of loops to run: " + LoopCallable.totalChainedLoops(loopSteps));
    long startTime = System.nanoTime();
    LoopCallable.initiateLoops(loopSteps);
    System.out.println(String.format("Finished %s (%g s)", nameAndFile[1], elapsed(startTime)));
  }

  private static void createDirIfNotExist() {

    String results = "Results";
    File theDir = new File(results);

    // if the directory does not exist, create it
    if (!theDir.exists()) {
      System.out.println("Creating directory: " + results);

      try {
        theDir.mkdir();
      } catch (SecurityException se) {
        System.out.println("Couldn't create " + results + " directory. Please create it manually to continue");
      }
    }
  }
}
