package cl.ian.gp;

import cl.ian.IndividualErrorComparator;
import ec.EvolutionState;
import ec.Fitness;
import ec.Individual;
import ec.gp.koza.KozaFitness;
import ec.util.Parameter;

import java.util.Arrays;

/**
 * Created by Ian on 20/01/2016.
 */
public class HitLevelKozaFitness extends KozaFitness {

  public static final String P_ACCEPTEDERROR = "accepted-error";
  public static final String P_REQUIREDMEETINGCONDITION = "required-meeting-condition";

  public static double hitLevel;
  public double meetsCondition;
  public static double requiredMeetingCondition;
  public double errorAvg;
  public double variance;

  @Override
  public void setup(EvolutionState state, Parameter base) {
    super.setup(state, base);

    Parameter def = defaultBase();

    if (!state.parameters.exists(base.push(P_ACCEPTEDERROR), def.push(P_ACCEPTEDERROR)))
      state.output.fatal("Need to define accepted error.", base.push(P_ACCEPTEDERROR), def.push(P_ACCEPTEDERROR));
    hitLevel = state.parameters.getDouble(base.push(P_ACCEPTEDERROR), def.push(P_ACCEPTEDERROR), 0.0) / 100;

    if (!state.parameters.exists(base.push(P_REQUIREDMEETINGCONDITION), def.push(P_REQUIREDMEETINGCONDITION)))
      state.output.fatal("Need to define percentage of samples that meet condition.",
          base.push(P_REQUIREDMEETINGCONDITION), def.push(P_REQUIREDMEETINGCONDITION));
    requiredMeetingCondition = state.parameters.getDouble(base.push(P_REQUIREDMEETINGCONDITION),
        def.push(P_REQUIREDMEETINGCONDITION)) / 100;
  }

  @Override
  public String fitnessToStringForHumans() {
    return String.format("Avg=%g Var=%g%s", errorAvg, variance, hits > 0 ? String.format(" (Hits=%d)", hits) : "");
    //return "" + standardizedFitness + " (Adjusted=" + adjustedFitness() + ", Hits=" + hits+")";
  }

  @Override
  public boolean isIdealFitness() {
    return meetsCondition >= requiredMeetingCondition;
  }

  public boolean errorBetterThan(final Fitness _fitness) {
    return errorAvg < (((HitLevelKozaFitness) _fitness).errorAvg)
        //&& (((HitLevelKozaFitness) _fitness).variance) > (variance)
        ;
  }

  public static Individual[] findTopKHeap(Individual[] inds, int k) {
    IndividualErrorComparator comparator = new IndividualErrorComparator();
    // TODO: Use a min heap for better performance
    Arrays.sort(inds, comparator);

    Individual[] res = new Individual[k];
    for (int i = 0; i < k; i++) res[i] = (Individual) inds[i].clone();
    return res;
  }
}
