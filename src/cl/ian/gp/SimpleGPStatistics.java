package cl.ian.gp;

import ec.EvolutionState;
import ec.Individual;
import ec.Statistics;
import ec.gp.GPIndividual;
import ec.simple.SimpleProblemForm;
import ec.simple.SimpleStatistics;
import ec.steadystate.SteadyStateStatisticsForm;
import ec.util.Log;
import ec.util.Output;
import ec.util.Parameter;

import java.io.File;
import java.io.IOException;

/**
 * Created by Ian on 26/01/2016.
 */
public class SimpleGPStatistics extends Statistics implements SteadyStateStatisticsForm //, ec.eval.ProvidesBestSoFar
{
  public Individual[] getBestSoFar() {
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
  public boolean onlyFinal;

  /**
   * The Statistics' log
   */
  public int statisticslog = 0;  // stdout

  /**
   * The best individual we've found so far
   */
  public Individual[] best_of_run = null;

  /**
   * Should we compress the file?
   */
  public boolean compress;
  public boolean doFinal;
  public boolean doGeneration;
  public boolean doMessage;
  public boolean doDescription;
  public boolean doPerGenerationDescription;

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
    doDescription = state.parameters.getBoolean(base.push(P_DO_DESCRIPTION), null, true);
    doPerGenerationDescription = state.parameters.getBoolean(base.push(P_DO_PER_GENERATION_DESCRIPTION), null, false);

    if (silentFile) {
      statisticslog = Output.NO_LOGS;
    } else if (statisticsFile != null) {
      try {
        statisticslog = state.output.addLog(statisticsFile, !compress, compress);
      } catch (IOException i) {
        state.output.fatal("An IOException occurred while trying to create the log " + statisticsFile + ":\n" + i);
      }
    } else
      state.output.warning("No statistics file specified, printing to stdout at end.", base.push(P_STATISTICS_FILE));

    onlyFinal = state.parameters.getBoolean(base.push(P_ONLY_FINAL), null, false);

    if (children.length != 0 && children[0] instanceof MyKozaShortStatistics) {
      ((MyKozaShortStatistics) children[0]).printHeader(state);
    }

  }

  public void postInitializationStatistics(final EvolutionState state) {
    super.postInitializationStatistics(state);

    // set up our best_of_run array -- can't do this in setup, because
    // we don't know if the number of subpopulations has been determined yet
    best_of_run = new Individual[state.population.subpops.length];
  }

  /**
   * Allows MultiObjectiveStatistics etc. to call super.super.finalStatistics(...) without
   * calling super.finalStatistics(...)
   */
  protected void bypassFinalStatistics(EvolutionState state, int result) {
    super.finalStatistics(state, result);
  }

  @Override
  public void postEvaluationStatistics(final EvolutionState state) {
    // Call the other childrens first. This is part of the grandparent but we cannot call it without calling our parent
    // overrides the changes of this class
    for (Statistics aChildren : children) aChildren.postEvaluationStatistics(state);

    // for now we just print the best fitness per subpopulation.
    Individual[] best_i = new Individual[state.population.subpops.length];  // quiets compiler complaints
    best_i[0] = state.population.subpops[0].individuals[0];
    for (int y = 1; y < state.population.subpops[0].individuals.length; y++) {
      if (state.population.subpops[0].individuals[y] == null) {
        if (!warned) {
          state.output.warnOnce("Null individuals found in subpopulation");
          warned = true;  // we do this rather than relying on warnOnce because it is much faster in a tight loop
        }
      } else if (best_i[0] == null || state.population.subpops[0].individuals[y].fitness.betterThan(best_i[0].fitness))
        best_i[0] = state.population.subpops[0].individuals[y];
      if (best_i[0] == null) {
        if (!warned) {
          state.output.warnOnce("Null individuals found in subpopulation");
          warned = true;  // we do this rather than relying on warnOnce because it is much faster in a tight loop
        }
      }
    }

    // now test to see if it's the new best_of_run
    if (best_of_run[0] == null || best_i[0].fitness.betterThan(best_of_run[0].fitness))
      best_of_run[0] = (Individual) (best_i[0].clone());

    if (onlyFinal) return;

    // print the best-of-generation individual
    if (doGeneration) state.output.println("\nBest Individual Generation " + state.generation + ":", statisticslog);
    if (doGeneration) best_i[0].printIndividualForHumans(state, statisticslog);
    if (doGeneration)
      state.output.message("Depth: " + ((GPIndividual) best_i[0]).trees[0].child.depth() + " Size: " + best_i[0].size());
    if (doMessage && !silentPrint) state.output.message("Best fitness: " +
        (best_i[0].evaluated ? " " : " (not evaluated): ") +
        best_i[0].fitness.fitnessToStringForHumans());

  }

  @Override
  public void finalStatistics(final EvolutionState state, final int result) {//TODO: avoid overwriting logs
    // for now we just print the best fitness
    if (doFinal) state.output.print("\nBest Individual of Run:", statisticslog);
    if (doFinal) best_of_run[0].printIndividualForHumans(state, statisticslog);
    if (doFinal) state.output.println(
        "Depth: " + ((GPIndividual) best_of_run[0]).trees[0].child.depth() +
            " Size: " + best_of_run[0].size(), statisticslog);
    if (doMessage && !silentPrint)
      state.output.message("Best fitness of run: " + best_of_run[0].fitness.fitnessToStringForHumans());
  }
}
