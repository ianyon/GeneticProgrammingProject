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

  // Variables to check if we need to reevaluate
  double reynolds, separation, normalizedVelocity, normalizedDensity, a1, normalizedArea, fluidColumn, a3, cachedFrictionFactor, cachedDragCoefficient, cachedNusseltNumber;
  int doubleColumn;

  public abstract double evaluateFrictionFactor(double reynolds, double separation, double normalizedVelocity,
                                                double normalizedDensity);

  /**
   * Evaluates the individual of the friction factor passing the necessary arguments if this object is of instance
   * FrictionFactorEvaluator, otherwise evaluates its phenomenological model passing the
   * necessary arguments, the remaining arguments are ignored
   */
  public final double computeFrictionFactor(double reynolds, double separation, double normalizedVelocity,
                                            double normalizedDensity) {
    if (reynolds == this.reynolds &&
        separation == this.separation &&
        normalizedVelocity == this.normalizedVelocity &&
        normalizedDensity == this.normalizedDensity)
      return cachedFrictionFactor;

    cachedFrictionFactor = evaluateFrictionFactor(reynolds, separation, normalizedVelocity, normalizedDensity);
    return cachedFrictionFactor;
  }

  public abstract double evaluateDragCoefficient(double a1, double reynolds, double normalizedArea,
                                                 double normalizedDensity, double fluidColumn);

  /**
   * Evaluates the individual of the drag coefficient passing the necessary arguments if this object is of instance
   * DragCoefficientEvaluator, otherwise evaluates its phenomenological model passing the
   * necessary arguments, the remaining arguments are ignored
   */
  public final double computeDragCoefficient(double a1, double reynolds, double normalizedArea,
                                             double normalizedDensity, double fluidColumn) {
    if (a1 == this.a1 &&
        reynolds == this.reynolds &&
        normalizedArea == this.normalizedArea &&
        normalizedDensity == this.normalizedDensity &&
        fluidColumn == this.fluidColumn)
      return cachedDragCoefficient;

    cachedDragCoefficient = evaluateDragCoefficient(a1, reynolds, normalizedArea, normalizedDensity, fluidColumn);
    return cachedDragCoefficient;
  }

  public abstract double evaluateNusseltNumber(int doubleColumn, double reynolds, double a3);


  /**
   * Evaluates the individual of the nusselt number passing the necessary arguments if this object is of instance
   * NusseltNumberEvaluator, otherwise evaluates its phenomenological model passing the
   * necessary arguments, the remaining arguments are ignored
   */
  public final double computeNusseltNumber(int doubleColumn, double reynolds, double a3) {
    if (doubleColumn == this.doubleColumn &&
        reynolds == this.reynolds &&
        a3 == this.a3)
      return cachedNusseltNumber;

    cachedNusseltNumber = evaluateNusseltNumber(doubleColumn, reynolds, a3);
    return cachedNusseltNumber;
  }

  /**
   * La presión atmosferica en pascales es 101325, aunque no importa porque la ecuación está en función del delta
   *
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
