/*
 * StopTimer.java
 *
 * Created on 6 de junio de 2005, 04:34 PM
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
import java.util.*;

import gpalta.core.*;

/**
 * A Timer Task to schedule the Evolution to stop at a certain time
 *
 * @author DSP
 */
public class StopTimer extends TimerTask
{
    GPaltaGUI gui;

    /**
     * Creates a new instance of StopTimer
     */
    public StopTimer(GPaltaGUI gui)
    {
        this.gui = gui;
    }

    public void run()
    {
        gui.stopSaveQuit = true;
        Logger.log("Setting to save and quit at the end of this generation");
    }

}
