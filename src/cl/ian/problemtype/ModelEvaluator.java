package cl.ian.problemtype;

import cl.ian.gp.EvolutionStateBean;
import cl.ian.gp.PhenomenologicalModel;
import ec.gp.GPIndividual;

/**
 * Created by Ian on 08/02/2016.
 * Abstract class that evaluates the value of the friction factor, drag coefficient and nusselt number depending on
 * the instance class.
 */
public abstract class ModelEvaluator implements Cloneable{
  protected EvolutionStateBean stateBean;
  protected GPIndividual individual;
  protected PhenomenologicalModel model;

  // Variables to check if we need to reevaluate
  protected double reynolds;
  protected double separation;
  protected double normalizedVelocity;
  protected double normalizedDensity;
  protected double a1;
  protected double normalizedArea;
  protected double fluidColumn;
  protected double a3;
  protected double cachedFrictionFactor;
  protected double cachedDragCoefficient;
  protected double cachedNusseltNumber;
  protected int doubleColumn;

  public ModelEvaluator() {
  }

  @Override
  public Object clone() {
    ModelEvaluator myobj;
    try {
      myobj = (ModelEvaluator) (super.clone());
    } catch (CloneNotSupportedException e) {
      throw new InternalError();
    } // never happens
    return myobj;
  }

  protected abstract double evaluateFrictionFactor(double reynolds, double separation, double normalizedVelocity,
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

    this.reynolds = reynolds;
    this.separation = separation;
    this.normalizedVelocity = normalizedVelocity;
    this.normalizedDensity = normalizedDensity;
    cachedFrictionFactor = evaluateFrictionFactor(reynolds, separation, normalizedVelocity, normalizedDensity);
    return cachedFrictionFactor;
  }

  protected abstract double evaluateDragCoefficient(double a1, double reynolds, double normalizedArea,
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

    this.a1 = a1;
    this.reynolds = reynolds;
    this.normalizedArea = normalizedArea;
    this.normalizedDensity = normalizedDensity;
    this.fluidColumn = fluidColumn;
    cachedDragCoefficient = evaluateDragCoefficient(a1, reynolds, normalizedArea, normalizedDensity, fluidColumn);
    return cachedDragCoefficient;
  }

  protected abstract double evaluateNusseltNumber(int doubleColumn, double reynolds, double a3);


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

    this.doubleColumn = doubleColumn;
    this.reynolds = reynolds;
    this.a3 = a3;
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

  public void setStateIndividual(EvolutionStateBean stateBean, GPIndividual individual, PhenomenologicalModel model) {
    this.stateBean = stateBean;
    this.individual = individual;
    this.model = model;
  }

  /**
   * Receives the pressure, velocity and temperature and returns the corresponding value according to the instance of
   * the object. FrictionFactorEvaluator outputs pf. DragCoefficientEvaluator outputs vf.
   * NusseltNumberEvaluator outputs tc.
   */
  public abstract double returnValue(double pf, double vf, double tc);
}
