package cl.ian.gp;

import cl.ian.Case;
import cl.ian.GeneralModelEvaluator;
import cl.ian.InputVariables;
import cl.ian.problemtype.DragCoefficientEvaluator;
import cl.ian.problemtype.FrictionFactorEvaluator;
import cl.ian.problemtype.ModelEvaluator;
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
  public static final String VALIDATION_FILE = "validation-file";
  public static final String TEST_FILE = "test-file";
  public static final String VALIDATION_OUTPUT = "validation-output";
  public static final String TEST_OUTPUT = "test-output";
  public static final String REGULARIZATION_FACTOR = "regularization-factor";
  public static final String PROBLEM_CASE = "problem-case";

  protected double inputs[][];
  private double[] outputs;
  private double[][] validationInputs;
  private double[][] testInputs;
  private double[] validationOutputFriction;
  private double[] validationOutputDrag;
  private double[] validationOutputNusselt;
  private double[] testOutputFriction;
  private double[] testOutputDrag;
  private double[] testOutputNusselt;

  private GeneralModelEvaluator model;

  // Regularization factor
  private double alpha;

  /**
   * Variables used in the evaluation of the individual
   **/
  public double reynolds;
  public double separation;
  public double normalizedVelocity;
  public double normalizedDensity;
  public double normalizedArea;
  public double fluidColumn;
  public Case problemCase;
  // 1.0e15 is for lil-gp
  public final static double BIG_NUMBER = 1.0e15;
  // The individuals with error similar to this number are bad solutions
  public final static double REALLY_BIG_NUMBER = BIG_NUMBER * 1.0e5;
  public final static double PROBABLY_ZERO = 1.11E-15;

  /******************************************************************************************************************/
  @Override
  public Object clone() {
    PhenomenologicalModel prob = (PhenomenologicalModel) (super.clone());
    prob.model = (GeneralModelEvaluator) model.clone();
    return prob;
  }

  // don't bother cloning the inputs and outputs; they're read-only :-)
  // don't bother cloning the currValue; it's transitory
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

    problemCase = Case.chooseCase(state.parameters.getString(base.push(PROBLEM_CASE), null));

    ModelEvaluator evaluator;
    switch (problemCase) {
      case FRICTION_FACTOR:
        evaluator = new FrictionFactorEvaluator();
        break;
      case DRAG_COEFFICIENT:
        evaluator = new DragCoefficientEvaluator();
        break;
      case NUSSELT_NUMBER:
      default:
        evaluator = new NusseltNumberEvaluator();
    }
    model = new GeneralModelEvaluator(evaluator);


    setupAlternateSets(state, base);
  }


  private void setupAlternateSets(EvolutionState state, final Parameter base) {
    InputStream validationFileStream = state.parameters.getResource(base.push(VALIDATION_FILE), null);
    InputStream testFileStream = state.parameters.getResource(base.push(TEST_FILE), null);
    InputStream validationOutputStream = state.parameters.getResource(base.push(VALIDATION_OUTPUT), null);
    InputStream testOutputStream = state.parameters.getResource(base.push(TEST_OUTPUT), null);

    String validationFilePath = state.parameters.getString(base.push(VALIDATION_FILE), null).replace("$", "");
    String testFilePath = state.parameters.getString(base.push(TEST_FILE), null).replace("$", "");
    String validationOutputPath = state.parameters.getString(base.push(VALIDATION_OUTPUT), null).replace("$", "");
    String testOutputPath = state.parameters.getString(base.push(TEST_OUTPUT), null).replace("$", "");

    if (validationFileStream == null || testFileStream == null || validationOutputStream == null || testOutputStream == null)
      state.output.fatal("Validation or test data files doesn't exist");

    try {
      validationInputs = readInputData(validationFilePath);
      testInputs = readInputData(testFilePath);
      validationOutputFriction = readOutputDataComplete(validationOutputPath, Case.FRICTION_FACTOR);
      validationOutputDrag = readOutputDataComplete(validationOutputPath, Case.DRAG_COEFFICIENT);
      validationOutputNusselt = readOutputDataComplete(validationOutputPath, Case.NUSSELT_NUMBER);
      testOutputFriction = readOutputDataComplete(testOutputPath, Case.FRICTION_FACTOR);
      testOutputDrag = readOutputDataComplete(testOutputPath, Case.DRAG_COEFFICIENT);
      testOutputNusselt = readOutputDataComplete(testOutputPath, Case.NUSSELT_NUMBER);
    } catch (IOException e) {
      state.output.fatal("Error reading the file: " + e.toString());
    } catch (IndexOutOfBoundsException e) {
      state.output.fatal("Error in input file.");
    }
  }


  public static double[] readOutputDataComplete(String filePath, Case exprCase) throws IOException {
    List<String> lines = Files.readAllLines(Paths.get(filePath));
    double[] outputs = new double[lines.size()];

    int index;
    switch (exprCase) {
      case NUSSELT_NUMBER:
        index = 1;
        break;
      case FRICTION_FACTOR:
        index = 2;
        break;
      case DRAG_COEFFICIENT:
        index = 3;
        break;
      default:
        index = -1;
    }

    String[] inputValues;
    for (int i = 0; i < lines.size(); i++) {
      inputValues = lines.get(i).split(",");
      outputs[i] = Double.parseDouble(inputValues[index]);
    }
    return outputs;
  }


  public static double[][] readInputData(String filePath) throws IOException {
    String[] inputValues;
    List<String> lines = Files.readAllLines(Paths.get(filePath));
    double[][] inputs = new double[lines.size()][5];
    for (int i = 0; i < lines.size(); i++) {
      inputValues = lines.get(i).split(",");
      for (int j = 0; j < 5; j++) inputs[i][j] = Double.parseDouble(inputValues[j]);
    }
    return inputs;
  }

  public void evaluate(final EvolutionState state, final Individual ind, final int subpop, final int threadnum) {
    evaluate(state, ind, subpop, threadnum, inputs, outputs, false);
  }

  public void evaluate(final EvolutionState state, final Individual ind, final int subpop,
                       final int threadnum, double[][] inputs, double[] outputs, boolean saveError) {
    if (ind.evaluated) return; // don't bother reevaluating

    ((MyGPIndividual) ind).setEvaluationErrorSize(getTestedElementsCount());

    updateControlVariables(state, threadnum);

    RegressionData input = (RegressionData) (this.input);

    HitLevelKozaFitness f = ((HitLevelKozaFitness) ind.fitness);

    int hits = 0;
    double errorSum = 0.0;
    BigDecimal quadraticErrorSum = new BigDecimal("0.0");
    double error;
    final InputVariables currValue = new InputVariables();
    final EvolutionStateBean evolutionStateBean = new EvolutionStateBean();
    double maxError = -0.0;

    for (int i = initLoop(); i < endLoop(); i++) {
      currValue.set(inputs[i]);
      evolutionStateBean.set(state, threadnum, input, stack);
      double result = model.compute(
          currValue.current, currValue.separation, currValue.flow, currValue.initTemperature, currValue.cellDiameter,
          (GPIndividual) ind, evolutionStateBean, this);

      error = Math.abs(outputs[i] - result);
      maxError = Double.isNaN(error) ? maxError : Math.max(error, maxError);

      // Solutions that bad are similarly bad between them
      if (error > BIG_NUMBER)
        error = BIG_NUMBER;

      else if (Double.isNaN(error) || Double.isInfinite(error))
        error = REALLY_BIG_NUMBER;
        // very slight math errors can creep in when evaluating two equivalent by differently-ordered functions, like
        // x * (x*x*x + x*x)  vs. x*x*x*x + x*x
      else if (error < PROBABLY_ZERO)  // slightly off
        error = 0.0;

      errorSum += error;
      quadraticErrorSum = quadraticErrorSum.add(BigDecimal.valueOf(error * error));

      // Check if the error is within hitLevel percent of the desired error
      if (error <= Math.abs(outputs[i]) * HitLevelKozaFitness.hitLevel)
        hits++;

      ((MyGPIndividual)ind).setEvaluationError(i - initLoop(), error);
    }

    // Calculate L1 distance: mean((outputs-input.x)^2)+regularizationExpression;
    final double testCount = getTestedElementsCount();
    final double quadraticErrorAvg = quadraticErrorSum.divide(BigDecimal.valueOf(testCount)).doubleValue();
    double regularizedMSE = quadraticErrorAvg + alpha * Math.sqrt(ind.size());

    f.errorAvg = Math.abs(errorSum / testCount);
    f.variance = quadraticErrorAvg - f.errorAvg * f.errorAvg;

    if (Double.isNaN(regularizedMSE) || Double.isInfinite(regularizedMSE))
      regularizedMSE = Double.MAX_VALUE;

    // Limit fitness precision, to eliminate rounding error problem. 12 decimals default precision in GPlab
    regularizedMSE = BigDecimal.valueOf(regularizedMSE).setScale(12, RoundingMode.HALF_UP).doubleValue();

    f.setStandardizedFitness(state, regularizedMSE);
    f.meetsCondition = (double) hits / getTestedElementsCount();
    ind.evaluated = true;
  }

  /**
   * Evaluate an individual with an specific dataset
   *
   * @param state
   * @param ind
   * @param inputs
   * @param outputs
   * @param saveError
   */
  public void evaluate(final EvolutionState state, final Individual ind, double[][] inputs, double[] outputs, boolean saveError) {
    evaluate(state, ind, 0, 0, inputs, outputs, saveError);
  }

  public MyGPIndividual evaluateValidation(final EvolutionState state, Individual[] tenBest) {

    double[] validationOutput;
    switch (this.problemCase) {
      case FRICTION_FACTOR:
        validationOutput = validationOutputFriction;
        break;
      case DRAG_COEFFICIENT:
        validationOutput = validationOutputDrag;
        break;
      case NUSSELT_NUMBER:
      default:
        validationOutput = validationOutputNusselt;
    }

    return getBestOf(state, tenBest, validationOutput);
  }


  public MyGPIndividual evaluateTest(EvolutionState state, MyGPIndividual bestOfValidation) {
    MyGPIndividual bestOfTest = (MyGPIndividual) bestOfValidation.clone();
    bestOfTest.evaluated = false;

    double[] testOutput;
    switch (this.problemCase) {
      case FRICTION_FACTOR:
        testOutput = testOutputFriction;
        break;
      case DRAG_COEFFICIENT:
        testOutput = testOutputDrag;
        break;
      case NUSSELT_NUMBER:
      default:
        testOutput = testOutputNusselt;
    }

    evaluate(state, bestOfTest, testInputs, testOutput, true);
    return bestOfTest;
  }

  public MyGPIndividual getBestOf(final EvolutionState state, Individual[] tenBest, double[] validationOutput) {
    MyGPIndividual bestOfValidation = null;
    for (Individual ind : tenBest) {
      ind.evaluated = false;
      evaluate(state, ind, validationInputs, validationOutput, true);
      bestOfValidation = MyGPIndividual.getBest(bestOfValidation, ind);
    }
    return bestOfValidation;
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

  /**
   * This is only used in subclasses of PhenomenologicalModel like the verticalslicing one. Here is empty
   *
   * @param state
   * @param threadnum
   */
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

