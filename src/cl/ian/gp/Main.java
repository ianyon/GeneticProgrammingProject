package cl.ian.gp;

import cl.ian.MatlabUtils;
import gpalta.core.Config;
import gpalta.core.Evolution;
import gpalta.core.SingleOutput;
import gpalta.multitree.MultiOutput;
import org.ejml.data.DenseMatrix64F;
import org.ejml.equation.Equation;
import org.ejml.ops.CommonOps;
import org.ejml.ops.SpecializedOps;

public class Main {

    public static void main(String[] args) {
        //generate fitness cases

        int maxGen = 10;
        // examples=50;
        // x(1,:)=10*rand(1,examples);
        // x(2,:)=10*rand(1,examples);
        // y = x(1,:).*x(1,:).*x(2,:) + x(1,:).*x(2,:) + x(2,:);
        // disp ('Looking for x2*x1^2 + x2*x1 + x2')
        DenseMatrix64F t = MatlabUtils.range(-5 * Math.PI, 0.5, 5 * Math.PI);
        int examples = t.getNumElements();

        DenseMatrix64F X = MatlabUtils.cos(t);
        DenseMatrix64F Y = MatlabUtils.sin(t);
        DenseMatrix64F Z = new DenseMatrix64F(t.getNumRows(), t.getNumCols());
        CommonOps.scale(1 / Math.sqrt(2), t, Z);


        Equation eq = new Equation();
        eq.alias(X, "X", Y, "Y", Z, "Z");
        eq.process("x = [X;Y;Z]");
        DenseMatrix64F x = eq.lookupMatrix("x");
        DenseMatrix64F y = new DenseMatrix64F();

        //initialize evolution:
        long meanTime;
        long startTime = System.nanoTime();

        Config config = new Config("Config.txt");

        double[] zeros = new double[examples];
        double[] zeros2 = new double[examples];
        DenseMatrix64F[] rows = SpecializedOps.splitIntoVectors(x, false);
        double[][] data = new double[x.getNumRows()][x.getNumCols()];
        for (int i = 0; i < rows.length; i++) {
            data[i] = rows[i].getData();
        }

        Evolution evo = new Evolution(config, data, zeros, zeros2, true);
        evo.eval();

        //go
        for (int i = 0; i <= maxGen; i++) {
            //    if 1 - evo.evoStats.bestSoFar.readFitness < .001
            //        time = toc;
            //        disp (['Objective reached in generation ' num2str(i) ' (' num2str(time) ' seconds)'])
            //        break
            //    end
            evo.evolve();
            evo.eval();
        }

        //evolution done (objective or max generations reached)
        System.out.println("winner tree: " + evo.evoStats.bestSoFar.toString());
        /*MultiOutput singleOutput = (MultiOutput) evo.getRawOutput(evo.evoStats.bestSoFar);
        double[] outputX = singleOutput.getArray(0);
        double[][] output = new double[1][outputX.length];
        output[0] = outputX;
        DenseMatrix64F diff = new DenseMatrix64F();
        DenseMatrix64F EJMLOutput = new DenseMatrix64F(output);
        CommonOps.subtract(CommonOps.transpose(EJMLOutput , null), y, diff);
        DenseMatrix64F diffPower = new DenseMatrix64F();
        CommonOps.elementPower(diff, 2, diffPower);*/
        System.out.println("fitness: " + evo.evoStats.bestSoFar.readFitness() );
                //+ ", MSE:"+ Math.sqrt(CommonOps.elementSum(diffPower)));

        // evo.evoStats.bestSoFar(1).getTree(1)
        // out=evo.getRawOutput(evo.evoStats.bestSoFar)
        // out.getArray(0)
        meanTime = (System.nanoTime() - startTime) / 1000;
        System.out.println("Tiempo medio: " + meanTime + "us");
    }

    static public void geneticProgrammingFrictionFactor(int individualCount, int generations){
        Config config = new Config("Config.txt");
        config.saveFileName = "evo.bin";
        config.nodeConfigFileName = "NodeConfig.txt";
        config.logFileName = "log.txt";


        // ----------- Basic GP options ------------
        config.population = "gpalta.core.SingleTreePopulation";
        config.populationSize = individualCount;
        config.nGenerations = generations;

        //Inicialización de población
        // TODO review: Opción no disponible: config.depthnodes = '1'; //1: niveles, 2: nodos
        config.initialMinDepth = 3;   // Default
        config.initialMaxDepth = 6; //nivel maximo de individuos iniciales
        config.probGrowBuild = 0.5; //initpoptype='q_rampedinit'; // fullinit, growinit, rampedinit

        //Tamaño de arboles
        // TODO review: Opción no disponible: config.fixedlevel = 1; //binario
        config.maxDepth = 17;//realmaxlevel = 17; //maximo nivel global
        // TODO review: Opción no disponible: config.dynamiclevel = '0'; //0: fixed, 1: dynamic 2: heavy dynamic
        // TODO review: Opción no disponible: config.veryheavy = 0;//binario
        // TODO review: Opción no disponible: config.inicdynlevel = 6;

        // Cantidad de individuos para pasar a la siguiente generación
        // TODO review: Opción no disponible: config.gengap = floor(nIndividuals);

        config.upLimitProbCrossOver = 0.8;
        config.upLimitProbMutation = 1.0;
        //The rest is for reproduction

        // Operator adaptability
        // TODO review: Opción no disponible: config.adaptinterval = 1;
        // TODO review: Opción no disponible: config.percentchange = .25;
        // TODO review: Opción no disponible: config.minprob=0.01/3;

        /* limits for constants: */
        config.constLowLimit = -100;
        config.constUpLimit = 100;

        config.maxCrossoverTries = 10;

        /* When selecting for crossover, use these probs */
        config.upLimitProbSelectTerminal = .1;
        config.upLimitProbSelectNonTerminal = 1;
        config.upLimitProbSelectRoot = 0;
        //The rest is for select any node

        // -------------- Selection ---------------
        /* Tree selection methods: tournament, roulette, proportional, SUS */
        //config.sampling = 'lexictour'; //roulette, sus, tournament, lexictour, doubletour,, 'tournament'
        config.selectionMethod = "gpalta.ops.IndSelectorTournament";
        /* for tournament selection: */
        config.tournamentSize = 2;
        config.SUSPointerDistance = 0.1; // This shouldn't be used because we are not using SUS
        /* Population ranking adjustment (unused in case of tournament selection method): Raw, LFR */
        config.rankingType = "gpalta.ops.RankingRaw";

        //hijos esperados por individuo Solo util para sampling como roulette
        //config.expected = 'rank85'; //absolute, rank85, rank89

        //Sobrevivencia
        // TODO review: Opción no disponible: config.elitism = 'keepbest';// replace, keepbest, halfelitism, totalelitism
        // TODO review: Opción no disponible: config.survival = 'fixedpopsize';//fixedpopsize,resources,rivotfixe

        //---------------- Fitness -----------------------
        config.fitness = "gpalta.core.FitnessClassic";
        // TODO: Review config.lowerisbetter = 1;//binario
        // TODO: Hacer. config.calcfitness = 'q_fitness_tree_ffriction';//'regfitness'

        /* stop if fitness reaches this value: */
        config.stopFitness = 0.95;
        config.sizePenalization = 0;

        //For the clustering fitness:
        config.nClasses = 2;
        config.sigma = 0;

        //------------ General behavior ------------
        config.useVect = true;
        config.rememberLastEval = true; // config.adaptwindowsize = 150;

       /* These two for non interactive mode */
        config.nonInteractive = false;
        config.nDaysToRun = 1;

        // Load data carga datos de entrada y salida para entrenamiento
        //config.datafilex='../InputData/doe500train_sort.txt';
        //config.datafiley='../InputData/salidas_P500train_sort.txt';


        int maxGen = 10;
        // examples=50;
        // x(1,:)=10*rand(1,examples);
        // x(2,:)=10*rand(1,examples);
        // y = x(1,:).*x(1,:).*x(2,:) + x(1,:).*x(2,:) + x(2,:);
        // disp ('Looking for x2*x1^2 + x2*x1 + x2')
        DenseMatrix64F t = MatlabUtils.range(-5 * Math.PI, 0.5, 5 * Math.PI);
        int examples = t.getNumElements();

        DenseMatrix64F X = MatlabUtils.cos(t);
        DenseMatrix64F Y = MatlabUtils.sin(t);
        DenseMatrix64F Z = new DenseMatrix64F(t.getNumRows(), t.getNumCols());
        CommonOps.scale(1 / Math.sqrt(2), t, Z);


        Equation eq = new Equation();
        eq.alias(X, "X", Y, "Y", Z, "Z");
        eq.process("x = [X;Y;Z]");
        DenseMatrix64F x = eq.lookupMatrix("x");
        double[] zeros = new double[3];
        double[] zeros2 = new double[3];
        DenseMatrix64F[] rows = SpecializedOps.splitIntoVectors(x, false);
        double[][] data = new double[x.getNumRows()][x.getNumCols()];
        for (int i = 0; i < rows.length; i++) {
            data[i] = rows[i].getData();
        }

        Evolution evo = new Evolution(config, data, zeros, zeros2, true);

    }

    public void newMain(){

    }
}
