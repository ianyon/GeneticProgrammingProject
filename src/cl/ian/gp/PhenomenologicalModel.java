package cl.ian.gp;

import cl.ian.GeneralModelEvaluator;
import cl.ian.InputVariables;
import cl.ian.problemtype.DragCoefficientEvaluator;
import cl.ian.problemtype.FrictionFactorEvaluator;
import cl.ian.problemtype.NusseltNumberEvaluator;
import ec.EvolutionState;
import ec.Individual;
import ec.app.regression.RegressionData;
import ec.gp.GPIndividual;
import ec.gp.GPProblem;
import ec.simple.SimpleProblemForm;
import ec.util.Parameter;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class PhenomenologicalModel extends GPProblem implements SimpleProblemForm {
  private static final long serialVersionUID = 1;

  public static final String INPUT_FILE = "inputfile";
  public static final String OUTPUT_FILE = "outputfile";
  public static final String REGULARIZATION_FACTOR = "regularization-factor";
  public static final String PROBLEM_CASE = "problem-case";

  public enum Case {
    FRICTION_FACTOR("FrictionFactor"),
    DRAG_COEFFICIENT("DragCoefficient"),
    NUSSELT_NUMBER("NusseltNumber");

    public final String text;

    Case(final String text) {
      this.text = text;
    }

    @Override
    public String toString() {
      return text;
    }
  }

  public final InputVariables currentValue = new InputVariables();
  public final EvolutionStateBean evolutionStateBean = new EvolutionStateBean();

  public double inputs[][];
  public double outputs[];

  public static GeneralModelEvaluator model;

  // Regularization factor
  public double alpha;

  /**
   * Variables used in the evaluation of the individual
   **/
  public double reynolds;
  public double separation;
  public double normalizedVelocity;
  public double normalizedDensity;
  public double normalizedArea;
  public double fluidColumn;

  /******************************************************************************************************************/

  // don't bother cloning the inputs and outputs; they're read-only :-)
  // don't bother cloning the currentValue; it's transitory
  public void setup(final EvolutionState state, final Parameter base) {
    super.setup(state, base);

    // verify our input is the right class (or subclasses from it)
    if (!(input instanceof RegressionData))
      state.output.fatal("GPData class must subclass from " + RegressionData.class,
          base.push(P_DATA), null);

    InputStream inputFile = state.parameters.getResource(base.push(INPUT_FILE), null);
    InputStream outputFile = state.parameters.getResource(base.push(OUTPUT_FILE), null);

    String inputFilePath = state.parameters.getString(base.push(INPUT_FILE), null).replace("$", "");
    String outputFilePath = state.parameters.getString(base.push(OUTPUT_FILE), null).replace("$", "");

    if (inputFile == null || outputFile == null)
      state.output.fatal("Training data files doesn't exist", base.push(INPUT_FILE), base.push(OUTPUT_FILE));

    // Compute our inputs so they can be copied with clone later
    String[] inputValues;

    try {
      List<String> lines = Files.readAllLines(Paths.get(inputFilePath));
      inputs = new double[lines.size()][5];
      for (int i = 0; i < lines.size(); i++) {
        inputValues = lines.get(i).split(",");
        for (int j = 0; j < 5; j++) inputs[i][j] = Double.parseDouble(inputValues[j]);
      }

      lines = Files.readAllLines(Paths.get(outputFilePath));
      outputs = new double[lines.size()];
      for (int i = 0; i < lines.size(); i++) {
        outputs[i] = Double.parseDouble(lines.get(i));
      }
    } catch (IOException e) {
      state.output.fatal("Error reading the file: " + e.toString(), base.push(INPUT_FILE), base.push(OUTPUT_FILE));
    } catch (IndexOutOfBoundsException e) {
      state.output.fatal("Error in input file. There mis be 5 values by example", base.push(INPUT_FILE));
    }

    if (inputs.length != outputs.length) state.output.fatal("Input and output has different sizes");

    alpha = state.parameters.getDouble(base.push(REGULARIZATION_FACTOR), null, 0.0);

    String problemCase = state.parameters.getString(base.push(PROBLEM_CASE), null);

    if (problemCase.equalsIgnoreCase(Case.FRICTION_FACTOR.text)) {
      model = new GeneralModelEvaluator(new FrictionFactorEvaluator());
    } else if (problemCase.equalsIgnoreCase(Case.DRAG_COEFFICIENT.text)) {
      model = new GeneralModelEvaluator(new DragCoefficientEvaluator());
    } else if (problemCase.equalsIgnoreCase(Case.NUSSELT_NUMBER.text)) {
      model = new GeneralModelEvaluator(new NusseltNumberEvaluator());
    }
  }

  public void evaluate(final EvolutionState state, final Individual ind, final int subpop, final int threadnum) {
    if (ind.evaluated) return; // don't bother reevaluating

    updateControlVariables(state, threadnum);

    RegressionData input = (RegressionData) (this.input);

    // the fitness better be HitLevelKozaFitness!
    HitLevelKozaFitness f = ((HitLevelKozaFitness) ind.fitness);

    int hits = 0;
    double quadraticErrorSum = 0, errorSum = 0.0, error;

    for (int i = initLoop(); i < endLoop(); i++) {
      currentValue.set(inputs[i]);
      evolutionStateBean.set(state, threadnum, input, stack, this);
      input.x = model.compute(
          currentValue.current,
          currentValue.separation,
          currentValue.flow,
          currentValue.initTemperature,
          currentValue.cellDiameter,
          (GPIndividual) ind,
          evolutionStateBean);

      error = input.x - outputs[i];
      errorSum += error;
      quadraticErrorSum += Math.pow(error, 2);

      // Check if the error is within hitLevel percent of the desired error
      if (Math.abs(error) <= Math.abs(outputs[i]) * HitLevelKozaFitness.hitLevel) hits++;
    }

    // Calculate L1 distance: mean((outputs-input.x)^2)+regularizationExpression;
    final int testCount = getTestedElementsCount();
    final double quadraticErrorAvg = quadraticErrorSum / testCount;
    double MSEWithRegularization = quadraticErrorAvg + alpha * Math.sqrt(ind.size());

    f.errorAvg = Math.abs(errorSum / testCount);
    f.variance = quadraticErrorAvg - Math.pow(f.errorAvg, 2);

    if (Double.isNaN(MSEWithRegularization) || Double.isInfinite(MSEWithRegularization))
      MSEWithRegularization = Double.MAX_VALUE;

    // Limit fitness precision, to eliminate rounding error problem. 12 decimals default precision in GPlab
    MSEWithRegularization = new BigDecimal(MSEWithRegularization).setScale(12, RoundingMode.HALF_UP).doubleValue();

    f.setStandardizedFitness(state, MSEWithRegularization);
    f.meetsCondition = (double) hits / testCount;
    ind.evaluated = true;

  }

  protected int getTestedElementsCount() {
    return inputs.length;
  }

  protected int initLoop() {
    return 0;
  }

  protected int endLoop() {
    return inputs.length;
  }

  protected void updateControlVariables(EvolutionState state, int threadnum) {
  }

  public void setFrictionFactorModelVariables(double reynolds,
                                              double separation,
                                              double normalizedVelocity,
                                              double normalizedDensity) {
    this.reynolds = reynolds;
    this.separation = separation;
    this.normalizedVelocity = normalizedVelocity;
    this.normalizedDensity = normalizedDensity;
  }

  public void setDragCoefficientModelVariables(double reynolds,
                                               double normalizedArea,
                                               double normalizedDensity,
                                               double fluidColumn) {
    this.reynolds = reynolds;
    this.normalizedArea = normalizedArea;
    this.normalizedDensity = normalizedDensity;
    this.fluidColumn = fluidColumn;
  }

  public void setNusseltNumberModelVariables(double rem) {
    this.reynolds = rem;
  }
}

