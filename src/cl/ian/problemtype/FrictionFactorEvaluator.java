package cl.ian.problemtype;


import cl.ian.Interpolation;
import cl.ian.ModelUtils;

/**
 * Created by ian on 6/15/15.
 * This class evaluates the evolved individual of the friction factor and computes the phenomenological model for the
 * other two expressions
 */
public class FrictionFactorEvaluator extends ModelEvaluator {

    @Override
    protected double evaluateFrictionFactor(double reynolds,
                                         double separation,
                                         double normalizedVelocity,
                                         double normalizedDensity) {
        model.setFrictionFactorModelVariables(reynolds,separation,
                normalizedVelocity, normalizedDensity);
        individual.trees[0].child.eval(stateBean.state, stateBean.threadNumber, stateBean.input, stateBean.stack,
                individual, model);

        return stateBean.input.x;
    }

    @Override
    protected double evaluateDragCoefficient(double a1, double reynolds, double normalizedArea, double normalizedDensity,
                                          double fluidColumn) {
        return a1 * ModelUtils.q_cdr3(reynolds);
    }

    @Override
    protected double evaluateNusseltNumber(int doubleColumn, double reynolds, double a3) {
        return Interpolation.q_cznusselt(doubleColumn, reynolds) * ModelUtils.q_nusselt2(reynolds, a3);
   }

    @Override
    public double returnValue(double pf, double vf, double tc) {
        // This happens if the result is NaN, Infinite or not real
        if (Double.isNaN(pf) || Double.isInfinite(pf)) return 0;

        return pf;
    }
}