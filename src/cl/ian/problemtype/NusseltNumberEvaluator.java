package cl.ian.problemtype;


import cl.ian.Interpolation;
import cl.ian.ModelUtils;

/**
 * Created by ian on 6/15/15.
 * This class evaluates the evolved individual of the Nusselt number and computes the phenomenological model for the
 * other two expressions
 */
public class NusseltNumberEvaluator extends ModelEvaluator {

    @Override
    public double evaluateFrictionFactor(double reynolds, double separation, double normalizedVelocity, double normalizedDensity) {
        return Interpolation.phenomenologicalFrictionFactor(reynolds,separation);
    }

    @Override
    public double evaluateDragCoefficient(double a1, double reynolds, double normalizedArea, double normalizedDensity, double fluidColumn) {
        return a1 * ModelUtils.q_cdr3(reynolds);
    }

    @Override
    public double evaluateNusseltNumber(int doubleColumn, double reynolds, double a3) {
        stateBean.phenomenologicalModel.setNusseltNumberModelVariables(reynolds);
        individual.trees[0].child.eval(stateBean.state, stateBean.threadNumber, stateBean.input, stateBean.stack,
                individual, stateBean.phenomenologicalModel);
        return stateBean.input.x;
    }

    @Override
    public double returnValue(double pf, double vf, double tc) {
        // This happens if the result is NaN, Infinite or not real
        if (Double.isNaN(tc) || Double.isInfinite(tc)) return 100000;

        return tc;
    }

    @Override
    public double atmosphericPressure() {
        return 101325;
    }
}