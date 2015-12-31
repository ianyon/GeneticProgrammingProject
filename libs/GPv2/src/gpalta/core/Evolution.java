/*
 * Evolution.java
 *
 * Created on 18 de mayo de 2005, 07:31 PM
 *
 * Copyright (C) 2005, 2006 Neven Boric <nboric@gmail.com>
 *
 * This file is part of GPalta.
 *
 * GPalta is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * GPalta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GPalta; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package gpalta.core;

import gpalta.ops.*;

import java.io.*;
import java.lang.reflect.InvocationTargetException;


/**
 * This is the highest level class of GPalta and stores the whole state of the system. Users should
 * create an instance of Evolution and use it to perform all aspects of a GP run.
 *
 * @author neven
 */
public class Evolution
{

    private TreeBuilder treeBuilder;
    public Population population;
    private TreeOperator treeOp;
    private IndSelector indSelector;
    private Ranking theRanking;
    public Fitness fitness;
    private ProblemData problemData;
    public int generation;
    public Config config;
    private NodeFactory nodeFactory;
    public EvolutionStats evoStats;
    private TempVectorFactory tempVectorFactory;

    private void initCommon(Config config, ProblemData initializedProblemData, boolean initPop)
    {
        nodeFactory = new NodeFactory(config, initializedProblemData);

        treeOp = new TreeOperator(config, nodeFactory);

        treeBuilder = new TreeBuilder(config, nodeFactory);

        try
        {
            //selection:
            Class cl = Class.forName(config.selectionMethod);
            java.lang.reflect.Constructor[] co = cl.getConstructors();
            if (co.length != 1)
            {
                //How do we know which constructor to use?
            }
            else
            {
                Class[] params = co[0].getParameterTypes();
                if (params.length == 0)
                {
                    indSelector = (IndSelector)co[0].newInstance();
                }
                else if (params.length == 1 && params[0].getName().equals(Config.class.getName()))
                {
                    indSelector = (IndSelector)co[0].newInstance(config);
                }
                else if (params.length == 2 && params[0].getName().equals(Config.class.getName()) && params[1].getName().equals(Ranking.class.getName()))
                {
                    cl = Class.forName(config.rankingType);
                    java.lang.reflect.Constructor[] co2 = cl.getConstructors();
                    if (co2.length == 1 && co2[0].getParameterTypes().length==0)
                    {
                        theRanking = (Ranking)co2[0].newInstance();
                        indSelector = (IndSelector)co[0].newInstance(config, theRanking);
                    }
                }
            }

            //fitness:
            cl = Class.forName(config.fitness);
            co = cl.getConstructors();
            if (co[0].getParameterTypes().length == 0)
            {
                fitness = (Fitness)co[0].newInstance();
            }

            evoStats = new EvolutionStats();

            //population:
            if (initPop)
            {
                cl = Class.forName(config.population);
                co = cl.getConstructors();
                if (co[0].getParameterTypes().length == 0)
                {
                    population = (Population)co[0].newInstance();
                }
                population.init(config, initializedProblemData, treeBuilder);
                evoStats.bestSoFar = population.get(0);
            }
        }
        catch (IllegalAccessException e)
        {
            Logger.log(e);
        }
        catch (InvocationTargetException e)
        {
            Logger.log(e);
        }
        catch (InstantiationException e)
        {
            Logger.log(e);
        }
        catch (ClassNotFoundException e)
        {
            Logger.log(e);
        }

        if (config.useVect)
        {
            tempVectorFactory = new TempVectorFactory(problemData.nSamples);
        }

        generation = 0;

    }

    /**
     * Creates a new Evolution, loading data from file
     *
     * @param config  The evolution configuration
     * @param initPop If true, the population is randomly initialized. Else, nothing is done
     *                (population will be later read from a file)
     */
    public Evolution(Config config, boolean initPop)
    {
        this.config = config;

        problemData = new ProblemData("data.txt");

        initCommon(config, problemData, initPop);

        fitness.init(config, problemData, "class.txt");

    }

    /**
     * Creates a new instance of Evolution, using the given data, desiredOutputs and weights
     *
     * @param config         The evolution configuration
     * @param data           The current problem's data, where every row correponds to all the
     *                       samples for a variable.
     * @param desiredOutputs The desired outputs
     * @param weights        The weight (importance) of each sample
     * @param initPop        If true, the population is randomly initialized. Else, nothing is done
     *                       (population will be later read from a file)
     */
    public Evolution(Config config, double[][] data, double[] desiredOutputs, double[] weights, boolean initPop)
    {
        this.config = config;

        problemData = new ProblemData(data);

        initCommon(config, problemData, initPop);

        SingleOutput des = new SingleOutput(problemData.nSamples);
        des.store(desiredOutputs);
        fitness.init(config, problemData, des, weights);

    }

    /**
     * Evaluate the current population.
     */
    public synchronized void eval()
    {
        population.eval(fitness, tempVectorFactory, problemData);
        Individual bestThisGen = population.get(0);
        evoStats.avgFit = 0;
        evoStats.avgNodes = 0;

        int nValid = 0;

        for (int i = 0; i < config.populationSize; i++)
        {
            Individual ind = population.get(i);
            if (!Double.isNaN(ind.readFitness()))
            {
                evoStats.avgFit += ind.readFitness();
                nValid++;
            }
            evoStats.avgNodes += ind.getSize();
            if (ind.readFitness() > bestThisGen.readFitness())
                bestThisGen = ind;
        }

        evoStats.bestTreeChanged = false;
        if (bestThisGen.readFitness() > evoStats.bestSoFar.readFitness()
            || (bestThisGen.readFitness() == evoStats.bestSoFar.readFitness() && bestThisGen.getSize() < evoStats.bestSoFar.getSize()))
        {
            evoStats.bestSoFar = bestThisGen.deepClone();
            evoStats.bestTreeChanged = true;
            evoStats.bestGen = generation;
        }

        evoStats.avgFit /= nValid;
        evoStats.avgNodes /= config.populationSize;
        evoStats.generation = generation;
        evoStats.bestFitThisGen = bestThisGen.readFitness();
    }

    /**
     * Evaluate a single Individual and get its "raw" output for every sample. Raw means that the
     * result is obtained only from the Individual, and not modified by the Fitness. The individual
     * will be evaluated on the problem data currently stored.
     *
     * @return The "raw" output of the Individual for every sample
     */
    public synchronized Output getRawOutput(Individual ind)
    {
        return population.getRawOutput(ind, tempVectorFactory, problemData);
    }

    /**
     * Evaluate a single Individual and get its "raw" output for every sample, using the supplied
     * data matrix, instead of the one used in evolution. Raw means that the result is obtained only
     * from the Individual, and not modified by the Fitness
     *
     * @param ind  The individual to evaluate
     * @param data A matrix with all the samples in which to evaluate the individual
     * @return The "raw" output of the Individual for every sample in the given data matrix
     */
    public synchronized Output getRawOutput(Individual ind, double[][] data)
    {
        ProblemData tmpProblemData = new ProblemData(data);
        TempVectorFactory tmpOutFact = new TempVectorFactory(data[0].length);
        return population.getRawOutput(ind, tmpOutFact, tmpProblemData);
    }

    /**
     * Evaluate a single Individual and get its output after being further processed by the Fitness
     *
     * @return The "processed" output of the Individual for every sample
     */
    public synchronized Output getProcessedOutput(Individual ind)
    {
        return population.getProcessedOutput(ind, fitness, tempVectorFactory, problemData);
    }

    /**
     * Evaluate a single Individual and get its "raw" output for every sample, using the supplied
     * data matrix, instead of the one used in evolution. Raw means that the result is obtained only
     * from the Individual, and not modified by the Fitness
     *
     * @param ind  The individual to evaluate
     * @param data A matrix with all the samples in which to evaluate the individual
     * @return The "raw" output of the Individual for every sample in the given data matrix
     */
    public synchronized Output getProcessedOutput(Individual ind, double[][] data)
    {
        ProblemData tmpProblemData = new ProblemData(data);
        TempVectorFactory tmpOutFact = new TempVectorFactory(data[0].length);
        return population.getProcessedOutput(ind, fitness, tmpOutFact, tmpProblemData);
    }

    /**
     * Evolve one generation. Assumes the current population is already evaluated and doesn't
     * evaluate the evolved one
     */
    public synchronized void evolve()
    {
        population.doSelection(indSelector);
        population.evolve(treeOp);
        generation++;
    }


    /**
     * Save Evolution to file. TODO: We should do something to check that the saved info is correct.
     * Maybe more things should be saved. For instance, if the file was saved with a grater maxDepth
     * than it is now, there would be nodes with larger depth than current maxDepth, and an attempt
     * to mutate those nodes would result in an error.
     *
     * @param fileName The file to write to
     * @throws IOException if a problem is encountered while writing (controlling classes should do
     *                     something about it)
     */
    public synchronized void save(String fileName) throws IOException
    {
        Logger.log("******************************************");
        Logger.log("Saving in file " + fileName);
        Logger.log("Best tree so far:");
        Logger.log("" + evoStats.bestSoFar);
        Logger.log("with fitness: " + evoStats.bestSoFar.readFitness());


        FileOutputStream fos = new FileOutputStream(fileName);
        ObjectOutputStream out = new ObjectOutputStream(fos);

        out.writeInt(generation);
        out.writeObject(evoStats.bestSoFar);
        out.writeObject(population);

        out.close();

        Logger.log("Done");
        Logger.log("******************************************");

    }

    /**
     * Read Evolution from file. TODO: We should do something to check that the saved info is
     * correct. Maybe more things should be saved. For instance, if the file was saved with a grater
     * maxDepth than it is now, there would be nodes with larger depth than current maxDepth, and an
     * attempt to mutate those nodes would result in an error.
     *
     * @param fileName The file to be read
     * @throws IOException            if a problem is encountered while reading (controlling classes
     *                                should do something about it)
     * @throws ClassNotFoundException if class read doesn't match existing classes (probably old
     *                                data in file)
     */
    public synchronized void read(String fileName) throws IOException, ClassNotFoundException
    {
        FileInputStream fis = new FileInputStream(fileName);
        ObjectInputStream in = new ObjectInputStream(fis);

        generation = in.readInt();
        evoStats.generation = generation;
        evoStats.bestSoFar = (Tree) in.readObject();
        population = (Population) in.readObject();

        in.close();

    }

}
