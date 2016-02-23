package cl.ian.gp.statistics;

import ec.EvolutionState;
import ec.gp.koza.KozaShortStatistics;
import ec.util.Output;
import ec.util.Parameter;

import java.io.File;
import java.io.IOException;

/**
 * Created by Ian on 26/01/2016.
 */
public class MyKozaShortStatistics extends KozaShortStatistics {

  public static final String P_STATISTICS_FILE_SUFFIX = "suffix";

  @Override
  public void setup(EvolutionState state, Parameter base) {
    super.setup(state, base);

    state.output.removeLog(statisticslog);

    String filenameBase = state.parameters.getString(base.push(P_STATISTICS_FILE), null).substring(1);
    String executionParameters = state.parameters.getString(base.push(P_STATISTICS_FILE).push(P_STATISTICS_FILE_SUFFIX), null);

    // Remove the initial $ and append extension to the end
    final String finalFilename = filenameBase.replace(".stat", " " + executionParameters) + ".stat";

    if (silentFile) {
      statisticslog = Output.NO_LOGS;
    } else {
      try {
        File statisticsFile = new File(finalFilename);
        statisticslog = state.output.addLog(statisticsFile,
            !state.parameters.getBoolean(base.push(P_COMPRESS), null, false),
            state.parameters.getBoolean(base.push(P_COMPRESS), null, false));
      } catch (IOException i) {
        state.output.fatal("An IOException occurred while trying to create the log " + finalFilename + ":\n" + i);
      }
    }
  }

  public void printHeader(EvolutionState state) {
    state.output.println("Gen" +
            (doTime ? " BreedTime EvalTime" : "") +
            (doDepth ? " [AvgDepth]" : "") +
            (doSize ? " [AvgSize] AvgSizeInd AvgSizeSoFar BestSize BestSizeSoFar" : "") +
            " [AvgFit BestFitGen BestFitSoFar]"
        , statisticslog);
  }
}
