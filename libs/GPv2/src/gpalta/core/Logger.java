/*
 * Logger.java
 *
 * Created on 7 de junio de 2005, 01:56 PM
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

package gpalta.core;

import java.io.*;

/**
 * @author DSP
 */

//TODO: investigate about Java Logging
public abstract class Logger
{

    private static PrintWriter writer;
    private static boolean initialized = false;

    public static void init()
    {
        //TODO: this exception should be caught here?
        try
        {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(Config.logFileName)));
        }
        catch (IOException e)
        {
            e.printStackTrace(writer);
        }
    }

    public static void log(String s)
    {
        if (!initialized)
        {
            init();
            initialized = true;
        }
        writer.println(s);
        System.out.println(s);
        //TODO: should we alway flush?
        writer.flush();
    }

    public static void log(Exception e)
    {
        if (!initialized)
        {
            init();
            initialized = true;
        }
        e.printStackTrace(writer);
        e.printStackTrace();
        //TODO: should we alway flush?
        writer.flush();
    }

}
