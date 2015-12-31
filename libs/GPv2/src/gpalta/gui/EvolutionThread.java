/*
 * EvolutionThread.java
 *
 * Created on 25 de mayo de 2005, 09:50 PM
 *
 * Copyright (C) 2005 Neven Boric <nboric@gmail.com>
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

package gpalta.gui;

import java.io.IOException;

import gpalta.core.*;

/**
 * Controls the evolution in a thread separated from the gui
 *
 * @author neven
 */
public class EvolutionThread extends Thread
{

    private GPaltaGUI gui;
    private int nGenerations;
    private Evolution e;
    private boolean exit;
    private boolean first;

    /**
     * Creates a new instance of EvolutionThread
     *
     * @param gui      The GPaltaGUI that controls this EvoThread
     * @param fromFile Whether the Evolution should continue from a previously saved
     *                 "evo.bin" file
     */
    public EvolutionThread(GPaltaGUI gui, boolean fromFile)
    {
        this.gui = gui;
        e = new Evolution(gui.config, !fromFile);
        exit = false;
        nGenerations = 0;
        first = true;
        if (fromFile)
        {
            first = false;
            try
            {
                e.read(gui.config.saveFileName);
            }
            catch (IOException ex)
            {
                Logger.log("Error reading evolution file:");
                Logger.log(ex.toString());
                Logger.log("Generating a new one...");
                e = new Evolution(gui.config, true);
                first = true;
            }
            catch (ClassNotFoundException ex)
            {
                //TODO: the evo file is probably old and has obsolete class data. Do something
            }
        }
        /* If we read from file, this will set the gui to the real stats
         * Else, it will set all zeros, and a random chosen tree as the best so far
         */
        gui.updateStats(e.evoStats);
    }

    /**
     * Tell the Evolution to advance n generatinos
     *
     * @param n The number of generations to advance
     */
    public synchronized void go(int n)
    {
        //Store how many generations to advance:
        nGenerations = n;
        //and then take out the thread from waiting state ("wake it up"):
        notifyAll();
    }

    public synchronized void exit()
    {
        exit = true;
        notifyAll();
    }

    /**
     * Save inmediatelly (not threaded)
     */
    public synchronized void save() throws IOException
    {
        e.save(e.config.saveFileName);
    }

    /**
     * Invoked in a threaded way by the start method
     */
    public synchronized void run()
    {
        //This should run forever
        while (!exit)
        {
            /* At the beginning, nGenerations = 0, so we don't enter here
             * and will end up on waiting state.
             * When the thread is "waked up" by notifyAll in go(), we will get
             * here again, but nGenerations will have some value other than 0
             * and the evolution will begin/continue
             */
            for (int i = 0; i < nGenerations; i++)
            {
                /* Check if the evolution has been canceled and we should
                 * stop instead of proceeding with the next generation
                 */
                if (gui.stopAtNextGen || gui.stopSaveQuit)
                {
                    break;
                }
                //Eval the initial population (gen =0)
                if (first)
                {
                    first = false;
                    gui.reportStatus("Evaluating initial population");
                    e.eval();
                    gui.updateStats(e.evoStats);
                }
                gui.reportStatus("Evolving to generation " + (e.evoStats.generation + 1));
                e.evolve();
                gui.reportStatus("Evaluating generation " + (e.evoStats.generation + 1));
                e.eval();
                if (gui.saveEnabled() && e.evoStats.generation % gui.getSaveInterval() == 0)
                {
                    try
                    {
                        save();
                    }
                    /* We will have to catch this exception here, as method run
                     * is not directly called from the gui
                     */
                    catch (IOException e)
                    {
                        Logger.log("An error ocurred while trying to save. Evolution will proceed");
                        Logger.log(e);
                    }
                }
                /* The thread will stop here until the next call returns, so there is
                 * no way the next e.evolve() would modify e.evoStats before the gui 
                 * has time to update 
                 */
                gui.updateStats(e.evoStats);
            }
            //We're done for this go() call
            nGenerations = 0;
            gui.notifyReady();

            //Enter waiting state:
            try
            {
                wait();
            }
            /* TODO: I don't know when this exception could be thrown and what
             * to do about it
             */
            catch (InterruptedException e)
            {
                Logger.log(e);
            }
        }
    }

}
