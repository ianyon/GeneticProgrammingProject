/*
 * Config.java
 *
 * Created on 10 de mayo de 2005, 11:46 PM
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

import java.io.*;
import java.util.*;

/**
 * Holds the GP parameters. See Config.txt for descriptions
 */
public class Config implements Serializable
{

    // --------------- Files -------------------
    public String saveFileName = "evo.bin";
    public String nodeConfigFileName = "NodeConfig.txt";
    public static String logFileName = "log.txt";

    // ----------- Basic GP options ------------
    public String population = "gpalta.core.SingleTreePopulation";
    public int populationSize = 500;
    public int nGenerations = 1000;

    public int maxDepth = 9;
    public int initialMinDepth = 3;
    public int initialMaxDepth = 6;

    /* Upper limits for the probability regions of the tree operations. This means:
    * probability of crossover = upLimitProbCrossOver - 0
    * probability of mutation = upLimitProbMutation - upLimitProbCrossOver
    * probability of reproduction = 1 - upLimitProbMutation
    */
    public double upLimitProbCrossOver = 0.85;
    public double upLimitProbMutation = 0.9;
    //The rest is for reproduction

    /* limits for constants: */
    public double constLowLimit = -100;
    public double constUpLimit = 100;

    public int maxCrossoverTries = 10;

    /* When selecting for crossover, use these probs */
    public double upLimitProbSelectTerminal = .1;
    public double upLimitProbSelectNonTerminal = 1;
    public double upLimitProbSelectRoot = 0;
    //The rest is for select any node

    /* For ramped half and half tree creation */
    public double probGrowBuild = .5;
    //The rest is for Full Build


    // -------------- Selection ---------------
    /* Tree selection methods: tournament, roulette, proportional, SUS */
    public String selectionMethod = "gpalta.ops.IndSelectorTournament";
    /* for tournament selection: */
    public int tournamentSize = 2;
    public double SUSPointerDistance = 0.1;
    /* Population ranking adjustment (unused in case of tournament selection method): Raw, LFR */
    public String rankingType = "gpalta.ops.RankingRaw";

    //---------------- Fitness -----------------------
    public String fitness = "gpalta.core.FitnessClassic";

    /* stop if fitness reaches this value: */
    public double stopFitness = 0.99;

    /* fitness will be: 
     * f' = (1-sizePenalization*treeDepth/maxDepth)*f
     */
    public double sizePenalization = 0;

    //For the clustering fitness:
    public int nClasses = 2;
    public double sigma = 0;

    //------------ General behavior ------------
    /* Use vectorial evaluation
     * On this mode, the system descends on the tree once and evaluates all fitness
     * cases on a loop. On the contrary, when using normal evaluation, the system
     * descends on the tree for each fitness case.
     * The number of operations is the same, but vectorial evaluation should be
     * faster when using lots of fitness cases.
     * Note that with vectorial evaluation, all nodes on the tree are always
     * evaluated. Instead, when using normal evaluation, sometimes the second kid
     * doesn't need to be evaluated because the result can be determined using
     * only the first kid (e.g on an "And" node if the first kid evals to zero)
     * This could eventually lead to a slowdown instead of speeding things up,
     * or cause problems when nodes have side effects (perform actions)
     */
    public boolean useVect = false;

    /* If true, trees that haven't changed from the past generation will remember
    * their fitness and won't be evaluated again.
    * WARNING: do not use if some values change between generations (e.g. cicling
    * fitness cases, random components in terminals, etc.)
    */
    public boolean rememberLastEval = false;

    /* These two for non interactive mode */
    public boolean nonInteractive = false;
    public int nDaysToRun = 1;

    /**
     * Reads config from a property file. The file must contain a value for all
     * the fields in the Config class
     *
     * @param fileName The name of the config file
     */
    public Config(String fileName)
    {
        try
        {
            FileInputStream in = new FileInputStream(fileName);
            Properties applicationProps = new Properties();
            applicationProps.load(in);
            in.close();

            java.lang.reflect.Field[] fields = Config.class.getFields();
            for (int i = 0; i < fields.length; i++)
            {
                Class type = fields[i].getType();
                if (applicationProps.getProperty(fields[i].getName()) == null)
                {
                    Logger.log("Property " + fields[i].getName() + " not found in " + fileName);
                    //Do not exit, just warn and continue:
                    continue;
                }

                if (type.getName().equals("double"))
                {
                    fields[i].setDouble(this, Double.parseDouble(applicationProps.getProperty(fields[i].getName())));
                }
                else if (type.getName().equals("int"))
                {
                    fields[i].setInt(this, Integer.parseInt(applicationProps.getProperty(fields[i].getName())));
                }
                else if (type.getName().equals("boolean"))
                {
                    fields[i].setBoolean(this, Boolean.parseBoolean(applicationProps.getProperty(fields[i].getName())));
                }
                else if (type.getName().equals("java.lang.String"))
                {
                    fields[i].set(this, applicationProps.getProperty(fields[i].getName()));
                }
            }

        }
        catch (IOException e)
        {
            Logger.log(e);
        }
        catch (IllegalAccessException e)
        {
            Logger.log(e);
        }
        catch (IllegalArgumentException e)
        {
            Logger.log(e);
        }
        catch (ExceptionInInitializerError e)
        {
            Logger.log(e.getMessage());
        }

    }
}
