package cl.ian.gp;

import ec.EvolutionState;
import ec.gp.koza.KozaFitness;
import ec.util.Parameter;

/**
 * Created by Ian on 20/01/2016.
 */
public class HitLevelKozaFitness extends KozaFitness {

    public static final String P_ACCEPTEDERROR = "accepted-error";
    public static final String P_REQUIREDMEETINGCONDITION = "required-meeting-condition";

    public static double hitLevel;
    public double meetsCondition;
    public static double requiredMeetingCondition;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);

        Parameter def = defaultBase();

        if (!state.parameters.exists(base.push(P_ACCEPTEDERROR), def.push(P_ACCEPTEDERROR)))
            state.output.fatal("Need to define accepted error.", base.push(P_ACCEPTEDERROR), def.push(P_ACCEPTEDERROR));
        hitLevel = state.parameters.getDouble(base.push(P_ACCEPTEDERROR), def.push(P_ACCEPTEDERROR), 0.0)/100;

        if (!state.parameters.exists(base.push(P_REQUIREDMEETINGCONDITION), def.push(P_REQUIREDMEETINGCONDITION)))
            state.output.fatal("Need to define percentage of samples that meet condition.",
                    base.push(P_REQUIREDMEETINGCONDITION), def.push(P_REQUIREDMEETINGCONDITION));
        requiredMeetingCondition = state.parameters.getDouble(base.push(P_REQUIREDMEETINGCONDITION),
                def.push(P_REQUIREDMEETINGCONDITION))/100;
    }

    @Override
    public String fitnessToStringForHumans()
    {
        return "" + standardizedFitness + " (Adjusted=" + adjustedFitness() + ", Hits=" + hits+")";
    }

    @Override
    public boolean isIdealFitness() {
        return meetsCondition >= requiredMeetingCondition;
    }
}
