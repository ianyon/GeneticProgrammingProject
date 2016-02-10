package cl.ian.problemtype;


import cl.ian.Interpolation;
import cl.ian.ModelUtils;

/**
 * Created by ian on 6/15/15.
 * This class evaluates the evolved individual of the drag coefficient and computes the phenomenological model for the
 * other two expressions
 */
public class DragCoefficientEvaluator extends ModelEvaluator {

    @Override
    public double evaluateFrictionFactor(double reynolds, double separation, double normalizedVelocity, double normalizedDensity) {
        return Interpolation.phenomenologicalFrictionFactor(reynolds,separation);
    }

    @Override
    public double evaluateDragCoefficient(double a1, double reynolds, double normalizedArea, double normalizedDensity, double fluidColumn) {
        stateBean.phenomenologicalModel.setDragCoefficientModelVariables(
                reynolds, normalizedArea, normalizedDensity, fluidColumn);
        individual.trees[0].child.eval(stateBean.state, stateBean.threadNumber, stateBean.input, stateBean.stack,
                individual, stateBean.phenomenologicalModel);

        return stateBean.input.x;
    }

    @Override
    public double evaluateNusseltNumber(int doubleColumn, double reynolds, double a3) {
        return Interpolation.q_cznusselt(doubleColumn, reynolds) * ModelUtils.q_nusselt2(reynolds, a3);
    }

    @Override
    public double returnValue(double pf, double vf, double tc) {
        // This happens if the result is NaN, Infinite or not real
        if (Double.isNaN(vf) || Double.isInfinite(vf)) return 1e100;

        return vf;
    }

}