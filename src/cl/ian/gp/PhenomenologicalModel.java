package cl.ian.gp;

import cl.ian.QModeloFfriction;
import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPIndividual;
import ec.gp.GPProblem;
import ec.gp.koza.KozaFitness;
import ec.simple.SimpleProblemForm;
import ec.util.Parameter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

public class PhenomenologicalModel extends GPProblem implements SimpleProblemForm {
    private static final long serialVersionUID = 1;

    public static final String P_SIZE = "size";
    public static final String INPUT_FILE = "inputfile";
    public static final String OUTPUT_FILE = "outputfile";
    public static final String P_USE_FUNCTION = "use-function";

    public double[] currentValue = new double[5];
    public int trainingSetSize;
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

    /******************************************************************************************************************/

    // don't bother cloning the inputs and outputs; they're read-only :-)
    // don't bother cloning the currentValue; it's transitory
    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state, base);

        // verify our input is the right class (or subclasses from it)
        if (!(input instanceof PhenomenologicalData))
            state.output.fatal("GPData class must subclass from " + PhenomenologicalData.class,
                    base.push(P_DATA), null);

        trainingSetSize = state.parameters.getInt(base.push(P_SIZE), null, 1);
        if (trainingSetSize < 1)
            state.output.fatal("Training Set Size must be an integer greater than 0", base.push(P_SIZE));

        InputStream inputFile = state.parameters.getResource(base.push(INPUT_FILE), null);
        InputStream outputFile = state.parameters.getResource(base.push(OUTPUT_FILE), null);

        if (inputFile == null || outputFile == null)
            state.output.fatal("Training data files doesn't exist", base.push(INPUT_FILE), base.push(OUTPUT_FILE));

        // Compute our inputs so they can be copied with clone later
        inputs = new double[trainingSetSize][5];
        outputs = new double[trainingSetSize];
        String[] inputValues;

        try {
            List<String> lines = Files.readAllLines(Paths.get(base.push(INPUT_FILE).toString()));
            for (int i = 0; i < lines.size(); i++) {
                inputValues = lines.get(i).split(",");
                for (int j = 0; j < 5; j++) inputs[i][j] = Double.parseDouble(inputValues[j]);
            }

            lines = Files.readAllLines(Paths.get(base.push(OUTPUT_FILE).toString()));
            for (int i = 0; i < lines.size(); i++) {
                outputs[i] = Double.parseDouble(lines.get(i));
            }
        } catch (IOException e) {
            state.output.fatal("Error reading the file", base.push(INPUT_FILE), base.push(OUTPUT_FILE));
        } catch (IndexOutOfBoundsException e) {
            state.output.fatal("Error in input file. There mis be 5 values by example", base.push(INPUT_FILE));
        }

        if (inputs.length != outputs.length) state.output.fatal("Input and output has different sizes");
    }

    public void evaluate(final EvolutionState state, final Individual ind,
                         final int subpopulation, final int threadnum) {
        if (ind.evaluated)  // don't bother reevaluating
            return;

        PhenomenologicalData input = (PhenomenologicalData) (this.input);

        /* Replace Xi with X(:,i)
        * outstr=ind.str;
            for i=params.numvars:-1:1
                outstr=strrep(outstr,strcat('X',num2str(i)),strcat('X(:,',num2str(i),')'));
            end

        Not needed we have the size method
        * %Calculo cantidad de nodos
         if isempty(ind.nodes)
             ind.nodes=nodes(ind.tree);
         end */

        int hits = 0;
        double sum = 0.0;
        double result;
        final double HIT_LEVEL = 0.01;
        final double PROBABLY_ZERO = 1.11E-15;
        final double BIG_NUMBER = 1.0e15;  // the same as lilgp uses

        for (int i = 0; i < trainingSetSize; i++) {
            currentValue = inputs[i];
            input.x = model.compute(currentValue[0], currentValue[1], currentValue[2], currentValue[3], currentValue[4],
                    (GPIndividual) ind, new EvolutionStateBean(state, threadnum, input, stack, this));

            result = outputs[i] - input.x;
            result *= result;

            if (!(result < BIG_NUMBER))   // *NOT* (input.x >= BIG_NUMBER)
                result = BIG_NUMBER;
                // math errors can creep in when evaluating two equivalent by differently-ordered functions
                // like x * (x*x*x + x*x)  vs. x*x*x*x + x*x*x
            else if (result < PROBABLY_ZERO)  // slightly off
                result = 0.0;

            if (result <= HIT_LEVEL) hits++;  // whatever!

            sum += result;
        }

        final double Fsize = 0.1 * Math.sqrt(ind.size());
        // Calculate L1 distance: mean((outputs-input.x)^2)+Fsize;
        double L1Distance = sum / trainingSetSize + Fsize;

        //ind.result=0;
        if (Double.isNaN(L1Distance))
            L1Distance = 100000000;

        // Limit fitness precision, to eliminate rounding error problem. 12 decimals default precision in GPlab
        L1Distance = (double) Math.round(L1Distance * 1000000000000d) / 1000000000000d;

        // the fitness better be KozaFitness!
        KozaFitness f = ((KozaFitness) ind.fitness);
        f.setStandardizedFitness(state, L1Distance);
        f.hits = hits;
        ind.evaluated = true;

    }
}

