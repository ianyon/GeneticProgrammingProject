package cl.ian.gp.statistics;

import cl.ian.Case;
import cl.ian.gp.HitLevelKozaFitness;
import cl.ian.gp.KnownApproxRampedHalfHalfInit;
import cl.ian.gp.MyGPIndividual;
import cl.ian.gp.PhenomenologicalModel;
import ec.EvolutionState;
import ec.Fitness;
import ec.Individual;
import ec.Statistics;
import ec.gp.GPIndividual;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.steadystate.SteadyStateStatisticsForm;
import ec.util.Output;
import ec.util.Parameter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by Ian on 26/01/2016.
 */
public class SimpleGPStatistics extends Statistics implements SteadyStateStatisticsForm //, ec.eval.ProvidesBestSoFar
{
  private Individual[] tenBest;
  private double[][] validationInputs;
  private double[][] testInputs;
  private double[] validationOutputFriction;
  private double[] validationOutputDrag;
  private double[] validationOutputNusselt;
  private double[] testOutputFriction;
  private double[] testOutputDrag;
  private double[] testOutputNusselt;
  private MyGPIndividual best_of_test;

  public MyGPIndividual[] getBestSoFar() {
    return best_of_run;
  }

  /**
   * log file parameter
   */
  public static final String P_STATISTICS_FILE = "file";
  public static final String P_STATISTICS_FILE_SUFFIX = "suffix";

  /**
   * compress?
   */
  public static final String P_COMPRESS = "gzip";

  public static final String P_DO_FINAL = "do-final";
  public static final String P_DO_GENERATION = "do-generation";
  public static final String P_DO_MESSAGE = "do-message";
  public static final String P_ONLY_FINAL = "only-final";

  public static final String VALIDATION_FILE = "validation-file";
  public static final String TEST_FILE = "test-file";
  public static final String VALIDATION_OUTPUT = "validation-output";
  public static final String TEST_OUTPUT = "test-output";

  private String validationFile;
  private String testFile;
  private String validationOutput;
  private String testOutput;
  private boolean onlyFinal;
  protected boolean warned = false;

  /**
   * The Statistics' log
   */
  public int statisticslog = 0;  // stdout

  /**
   * The best individual we've found so far
   */
  public MyGPIndividual[] best_of_run = null;
  public MyGPIndividual best_of_validation = null;

  /**
   * Should we compress the file?
   */
  private boolean compress;
  private boolean doFinal;
  private boolean doGeneration;
  private boolean doMessage;

  @Override
  public void setup(final EvolutionState state, final Parameter base) {
    super.setup(state, base);

    compress = state.parameters.getBoolean(base.push(P_COMPRESS), null, false);

    String filenameBase = state.parameters.getString(base.push(P_STATISTICS_FILE), null);
    String executionParameters = state.parameters.getString(base.push(P_STATISTICS_FILE).push(P_STATISTICS_FILE_SUFFIX), null);

    // Remove the initial $ and append extension to the end
    final String finalFilename = filenameBase.replace(".stat", " " + executionParameters).substring(1) + ".stat";
    File statisticsFile = new File(finalFilename);

    doFinal = state.parameters.getBoolean(base.push(P_DO_FINAL), null, true);
    doGeneration = state.parameters.getBoolean(base.push(P_DO_GENERATION), null, true);
    doMessage = state.parameters.getBoolean(base.push(P_DO_MESSAGE), null, true);

    if (silentFile) {
      statisticslog = Output.NO_LOGS;
    } else {
      try {
        statisticslog = state.output.addLog(statisticsFile, !compress, compress);
      } catch (IOException i) {
        state.output.fatal("An IOException occurred while trying to create the log " + statisticsFile + ":\n" + i);
      }
    }

    onlyFinal = state.parameters.getBoolean(base.push(P_ONLY_FINAL), null, false);

    /*if (children.length != 0 && children[0] instanceof MyKozaShortStatistics) {
      ((MyKozaShortStatistics) children[0]).printHeader(state);
    }*/

    setupAlternateSets(state, base);
  }

  private void setupAlternateSets(EvolutionState state, final Parameter base) {
    InputStream validationFileStream = state.parameters.getResource(base.push(VALIDATION_FILE), null);
    InputStream testFileStream = state.parameters.getResource(base.push(TEST_FILE), null);
    InputStream validationOutputStream = state.parameters.getResource(base.push(VALIDATION_OUTPUT), null);
    InputStream testOutputStream = state.parameters.getResource(base.push(TEST_OUTPUT), null);

    String validationFilePath = state.parameters.getString(base.push(VALIDATION_FILE), null).replace("$", "");
    String testFilePath = state.parameters.getString(base.push(TEST_FILE), null).replace("$", "");
    String validationOutputPath = state.parameters.getString(base.push(VALIDATION_OUTPUT), null).replace("$", "");
    String testOutputPath = state.parameters.getString(base.push(TEST_OUTPUT), null).replace("$", "");

    if (validationFileStream == null || testFileStream == null || validationOutputStream == null || testOutputStream == null)
      state.output.fatal("Validation or test data files doesn't exist");

    try {
      validationInputs = readInputData(validationFilePath);
      testInputs = readInputData(testFilePath);
      validationOutputFriction = readOutputDataComplete(validationOutputPath, Case.FRICTION_FACTOR);
      validationOutputDrag = readOutputDataComplete(validationOutputPath, Case.DRAG_COEFFICIENT);
      validationOutputNusselt = readOutputDataComplete(validationOutputPath, Case.NUSSELT_NUMBER);
      testOutputFriction = readOutputDataComplete(testOutputPath, Case.FRICTION_FACTOR);
      testOutputDrag = readOutputDataComplete(testOutputPath, Case.DRAG_COEFFICIENT);
      testOutputNusselt = readOutputDataComplete(testOutputPath, Case.NUSSELT_NUMBER);
    } catch (IOException e) {
      state.output.fatal("Error reading the file: " + e.toString());
    } catch (IndexOutOfBoundsException e) {
      state.output.fatal("Error in input file.");
    }
  }

  public static double[] readOutputDataComplete(String filePath, Case exprCase) throws IOException {
    List<String> lines = Files.readAllLines(Paths.get(filePath));
    double[] outputs = new double[lines.size()];
    int index = exprCase == Case.NUSSELT_NUMBER ? 1 : exprCase == Case.FRICTION_FACTOR ? 2 : exprCase == Case.DRAG_COEFFICIENT ? 3 : -1;
    String[] inputValues;
    for (int i = 0; i < lines.size(); i++) {
      inputValues = lines.get(i).split(",");
      outputs[i] = Double.parseDouble(inputValues[index]);
    }
    return outputs;
  }

  public static double[][] readInputData(String filePath) throws IOException {
    String[] inputValues;
    List<String> lines = Files.readAllLines(Paths.get(filePath));
    double[][] inputs = new double[lines.size()][5];
    for (int i = 0; i < lines.size(); i++) {
      inputValues = lines.get(i).split(",");
      for (int j = 0; j < 5; j++) inputs[i][j] = Double.parseDouble(inputValues[j]);
    }
    return inputs;
  }

  @Override
  public void preEvaluationStatistics(EvolutionState state) {
    super.preEvaluationStatistics(state);

    double meanDepth = 0, meanSize = 0, quadraticSizeSum = 0, quadraticDepthSum = 0, varianceDepth, varianceSize;
    for (Individual individual : state.population.subpops[0].individuals) {
      final int nodesCount = ((MyGPIndividual) individual).trees[0].child.numNodes(GPNode.NODESEARCH_ALL);
      meanSize += nodesCount;
      quadraticSizeSum += Math.pow(nodesCount, 2);

      final int depth = ((MyGPIndividual) individual).trees[0].child.depth();
      meanDepth += depth;
      quadraticDepthSum += Math.pow(depth, 2);
    }

    final int individualsCount = state.population.subpops[0].individuals.length;
    meanDepth /= individualsCount;
    meanSize /= individualsCount;
    varianceDepth = (quadraticDepthSum / individualsCount) - Math.pow(meanDepth, 2);
    varianceSize = (quadraticSizeSum / individualsCount) - Math.pow(meanSize, 2);
    state.output.message(String.format("New Gen stats: AvgDepth=%s (Var=%.2f) AvgSize=%s (Var=%.2f)",
        meanDepth, varianceDepth, meanSize, varianceSize));
  }

  public void postInitializationStatistics(final EvolutionState state) {
    super.postInitializationStatistics(state);

    // set up our best_of_run array -- can't do this in setup, because
    // we don't know if the number of subpopulations has been determined yet
    best_of_run = new MyGPIndividual[state.population.subpops.length];

    KnownApproxRampedHalfHalfInit init = (KnownApproxRampedHalfHalfInit) ((GPIndividual)
        state.population.subpops[0].individuals[0]).trees[0].constraints((GPInitializer) state.initializer).init;
    state.output.message(String.format("Initializer [%d]: full=%d grow=%d known=%d",
        state.population.subpops[0].individuals.length, init.fullCount, init.growCount, init.knownApproxCount));
  }

  @Override
  public void postEvaluationStatistics(final EvolutionState state) {
    super.postEvaluationStatistics(state);

    // for now we just print the best fitness per subpopulation.
    MyGPIndividual[] best_i = new MyGPIndividual[1];
    best_i[0] = (MyGPIndividual) state.population.subpops[0].individuals[0];
    for (int y = 1; y < state.population.subpops[0].individuals.length; y++) {
      if (state.population.subpops[0].individuals[y] == null) {
        if (!warned) {
          state.output.warnOnce("Null individuals found in subpopulation");
          warned = true;  // we do this rather than relying on warnOnce because it is much faster in a tight loop
        }
      } else
        best_i[0] = MyGPIndividual.getBest(best_i[0], state.population.subpops[0].individuals[y]);
      if (best_i[0] == null) {
        if (!warned) {
          state.output.warnOnce("Null individuals found in subpopulation");
          warned = true;  // we do this rather than relying on warnOnce because it is much faster in a tight loop
        }
      }
    }

    // now test to see if it's the new best_of_run
    best_of_run[0] = MyGPIndividual.getBest(best_of_run[0], best_i[0]);

    if (onlyFinal) return;

    // print the best-of-generation individual
    if (doGeneration) state.output.println("\nBest Individual Generation " + state.generation + ":", statisticslog);
    if (doGeneration) best_i[0].printIndividualForHumans(state, statisticslog);

    if (!best_i[0].evaluated) state.output.warning(" (not evaluated)");
    if (doMessage && !silentPrint)
      state.output.message(String.format("Best individual fitness: %s %s",
          best_i[0].fitness.fitnessToStringForHumans(), best_i[0].depthAndSize()));
  }

  @Override
  public void finalStatistics(final EvolutionState state, final int result) {
    // for now we just print the best fitness
    if (doFinal) state.output.print("\nBest Individual of Run:", statisticslog);
    if (doFinal) best_of_run[0].printIndividualForHumans(state, statisticslog);
    if (doFinal) state.output.println(best_of_run[0].depthAndSize(), statisticslog);

    String bestMessage = String.format("\nBest fitness of run: %s\n", best_of_run[0].fitnessAndTree());

    if (doMessage && !silentPrint) state.output.message(bestMessage);

    /** Validate individual **/
    // Validation individual
    tenBest = HitLevelKozaFitness.findTopKHeap(state.population.subpops[0].individuals, 10);

    final PhenomenologicalModel problem = (PhenomenologicalModel) state.evaluator.p_problem;
    double[] validationOutput;
    if (problem.problemCase.equals(Case.FRICTION_FACTOR))
      validationOutput = validationOutputFriction;
    else if (problem.problemCase.equals(Case.DRAG_COEFFICIENT))
      validationOutput = validationOutputDrag;
    else
      validationOutput = validationOutputNusselt;

    for (Individual ind : tenBest) {
      ind.evaluated = false;
      problem.evaluate(state, ind, validationInputs, validationOutput);
    }

    for (Individual ind : tenBest) {
      best_of_validation = MyGPIndividual.getBest(best_of_validation, ind);
    }


    if (doFinal) state.output.print("\nBest of validation:", statisticslog);
    if (doFinal) best_of_validation.printIndividualForHumans(state, statisticslog);
    if (doFinal) state.output.println(best_of_validation.depthAndSize(), statisticslog);

    bestMessage = String.format("\nBest of validation: %s\n", best_of_validation.fitnessAndTree());

    if (doMessage && !silentPrint) state.output.message(bestMessage);

    /** Test individual **/
    double[] testOutput;
    if (problem.problemCase.equals(Case.FRICTION_FACTOR))
      testOutput = testOutputFriction;
    else if (problem.problemCase.equals(Case.DRAG_COEFFICIENT))
      testOutput = testOutputDrag;
    else
      testOutput = testOutputNusselt;

    best_of_test = (MyGPIndividual)best_of_validation.clone();
    best_of_test.evaluated = false;
    problem.evaluate(state, best_of_test, testInputs, testOutput);

    if (doFinal) state.output.print("\nBest of test:", statisticslog);
    if (doFinal) best_of_test.printIndividualForHumans(state, statisticslog);
    if (doFinal) state.output.println(best_of_test.depthAndSize(), statisticslog);

    bestMessage = String.format("\nBest of test: %s\n", best_of_test.fitnessAndTree());

    if (doMessage && !silentPrint) state.output.message(bestMessage);
  }
}
