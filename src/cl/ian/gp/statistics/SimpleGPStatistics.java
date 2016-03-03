package cl.ian.gp.statistics;

import cl.ian.gp.KnownApproxRampedHalfHalfInit;
import cl.ian.gp.MyGPIndividual;
import ec.EvolutionState;
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

/**
 * Created by Ian on 26/01/2016.
 */
public class SimpleGPStatistics extends Statistics implements SteadyStateStatisticsForm //, ec.eval.ProvidesBestSoFar
{
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
  public static final String P_DO_DESCRIPTION = "do-description";
  public static final String P_DO_PER_GENERATION_DESCRIPTION = "do-per-generation-description";

  protected boolean warned = false;

  public static final String P_ONLY_FINAL = "only-final";
  private boolean onlyFinal;

  /**
   * The Statistics' log
   */
  public int statisticslog = 0;  // stdout

  /**
   * The best individual we've found so far
   */
  public MyGPIndividual[] best_of_run = null;

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

    final String bestMessage = String.format("\nBest fitness of run: %s\n", best_of_run[0].fitnessAndTree());

    if (doMessage && !silentPrint) state.output.message(bestMessage);
  }
}
