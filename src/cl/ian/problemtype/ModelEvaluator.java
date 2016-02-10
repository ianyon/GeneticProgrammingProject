package cl.ian.problemtype;

import cl.ian.gp.EvolutionStateBean;
import ec.gp.GPIndividual;

/**
 * Created by Ian on 08/02/2016.
 * Abstract class that evaluates the value of the friction factor, drag coefficient and nusselt number depending on
 * the instance class.
 */
public abstract class ModelEvaluator {
    EvolutionStateBean stateBean;
    GPIndividual individual;

    /**
     * Evaluates the individual of the friction factor passing the necessary arguments if this object is of instance
     * FrictionFactorEvaluator, otherwise evaluates its phenomenological model passing the
     * necessary arguments, the remaining arguments are ignored
     */
    public abstract double evaluateFrictionFactor(double reynolds, double separation, double normalizedVelocity,
                                                  double normalizedDensity);

    /**
     * Evaluates the individual of the drag coefficient passing the necessary arguments if this object is of instance
     * DragCoefficientEvaluator, otherwise evaluates its phenomenological model passing the
     * necessary arguments, the remaining arguments are ignored
     */
    public abstract double evaluateDragCoefficient(double a1, double reynolds, double normalizedArea,
                                                   double normalizedDensity, double fluidColumn);

    /**
     * Evaluates the individual of the nusselt number passing the necessary arguments if this object is of instance
     * NusseltNumberEvaluator, otherwise evaluates its phenomenological model passing the
     * necessary arguments, the remaining arguments are ignored
     */
    public abstract double evaluateNusseltNumber(int doubleColumn, double reynolds, double a3);

    /**
     * La presión atmosferica en pascales es 101325, aunque no importa porque la ecuación está en función del delta
     * @return atmosferic pressure value
     */
    public double atmosphericPressure() {
        return 0;
    }

    public void setStateIndividual(EvolutionStateBean stateBean, GPIndividual individual) {
        this.stateBean = stateBean;
        this.individual = individual;
    }

    /**
     * Receives the pressure, velocity and temperature and returns the corresponding value according to the instance of
     * the object. FrictionFactorEvaluator outputs pf. DragCoefficientEvaluator outputs vf.
     * NusseltNumberEvaluator outputs tc.
     */
    public abstract double returnValue(double pf, double vf, double tc);
}
