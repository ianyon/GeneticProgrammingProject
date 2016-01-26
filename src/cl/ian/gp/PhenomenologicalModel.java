package cl.ian.gp;

import cl.ian.QModeloFfriction;
import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPIndividual;
import ec.gp.GPProblem;
import ec.simple.SimpleProblemForm;
import ec.util.Parameter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class PhenomenologicalModel extends GPProblem implements SimpleProblemForm {
    private static final long serialVersionUID = 1;

    public static final String P_NUMBER_OF_SLICES = "number-of-slices";
    public static final String INPUT_FILE = "inputfile";
    public static final String OUTPUT_FILE = "outputfile";

    public double[] currentValue = new double[5];
    public int slicesCount;
    private int slice;
    private int[] sliceLimits;
    public File file;

    public double inputs[][];
    public double outputs[];

    public static final QModeloFfriction model = new QModeloFfriction();

    /**
     * Variables used in the evaluation of the individual
     **/
    public double rem;
    public double separation;
    public double normalizedMeanVelocity;
    public double normalizedFluidDensity;
    private static int lastGeneration;


    /******************************************************************************************************************/

    // don't bother cloning the inputs and outputs; they're read-only :-)
    // don't bother cloning the currentValue; it's transitory
    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state, base);

        // verify our input is the right class (or subclasses from it)
        if (!(input instanceof PhenomenologicalData))
            state.output.fatal("GPData class must subclass from " + PhenomenologicalData.class,
                    base.push(P_DATA), null);

        slicesCount = state.parameters.getInt(base.push(P_NUMBER_OF_SLICES), null, 1);
        if (slicesCount < 1)
            state.output.fatal("Training Set Size must be an integer greater than 0", base.push(P_NUMBER_OF_SLICES));

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

        sliceLimits = new int[slicesCount + 1];
        for (int i = 0; i <= slicesCount; i++)
            sliceLimits[i] = (int) (i * Math.floor(inputs.length / slicesCount));

        // Used to mark the advance in time
        lastGeneration = -1;
    }

    public void evaluate(final EvolutionState state, final Individual ind, final int subpop, final int threadnum) {
        if (ind.evaluated) return; // don't bother reevaluating

        // We advanced to a new generation so reset the random slice
        if (state.generation != lastGeneration) {
            slice = state.random[threadnum].nextInt(slicesCount);
            lastGeneration = state.generation;
        }

        PhenomenologicalData input = (PhenomenologicalData) (this.input);

        /* Replace Xi with X(:,i)
        * outstr=ind.str;
            for i=params.numvars:-1:1
                outstr=strrep(outstr,strcat('X',num2str(i)),strcat('X(:,',num2str(i),')'));
            end*/

        // the fitness better be HitLevelKozaFitness!
        HitLevelKozaFitness f = ((HitLevelKozaFitness) ind.fitness);

        int hits = 0;
        double quadraticErrorSum = 0.0;
        double error;
        final double PROBABLY_ZERO = 1.11E-15;
        final double BIG_NUMBER = 1.0e15;  // the same as lilgp uses

        for (int i = sliceLimits[slice]; i < sliceLimits[slice + 1]; i++) {
            currentValue = inputs[i];
            input.x = model.compute(currentValue[0], currentValue[1], currentValue[2], currentValue[3], currentValue[4],
                    (GPIndividual) ind, new EvolutionStateBean(state, threadnum, input, stack, this));

            error = outputs[i] - input.x;

            // math errors can creep in when evaluating two equivalent by differently-ordered functions
            // like x * (x*x*x + x*x)  vs. x*x*x*x + x*x*x
            /*if (error < PROBABLY_ZERO)  // slightly off
                error = 0.0;*/

            // Check if the error is within hitLevel percent of the desired error
            if (Math.abs(error) <= Math.abs(outputs[i]) * HitLevelKozaFitness.hitLevel) hits++;

            quadraticErrorSum += error * error;
        }

        // Regularization factor
        final double alpha = 0.1;
        final double regularizationExpression = alpha * Math.sqrt(ind.size());
        // Calculate L1 distance: mean((outputs-input.x)^2)+regularizationExpression;
        int testCount = sliceLimits[slice + 1] - sliceLimits[slice];
        double MSEWithRegularization = quadraticErrorSum / testCount + regularizationExpression;

        //ind.error=0;
        if (Double.isNaN(MSEWithRegularization))
            MSEWithRegularization = 100000000;

        // Limit fitness precision, to eliminate rounding error problem. 12 decimals default precision in GPlab
        MSEWithRegularization = (double) Math.round(MSEWithRegularization * 1000000000000d) / 1000000000000d;

        f.setStandardizedFitness(state, MSEWithRegularization);
        f.meetsCondition = (double) hits / testCount;
        ind.evaluated = true;

    }
}

