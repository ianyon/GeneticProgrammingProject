/*
 * Common.java
 *
 * Created on 11 de mayo de 2005, 12:23 AM
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

/**
 * Useful utilities for all clases
 *
 * @author neven
 */
public abstract class Common
{

    /**
     * A random number generator for use by other classes
     */
    public static java.util.Random globalRandom = new java.util.Random();

    /**
     * Efficient permutation algorithm, O(n)
     *
     * @param n The size of the permutation
     * @return A permutation of size n. That is, an int array consisting of all
     *         integer numbers between 0 and n (non-inclusive) in random order
     */
    public static int[] randPerm(int n)
    {
        int[] permutation = new int[n];
        for (int i = 0; i < n; i++)
        {
            permutation[i] = i;
        }

        /* At the end of each iteration:
        * Between 0 and i is the sequence of indexes already selected
        * Between i+1 and n-1 are the indexes not selected so far (in any order)
        * The loop goes until i<n-1 because when i=n-1 it does nothing
        * (always swaps with itself)
        */
        for (int i = 0; i < n - 1; i++)
        {
            //choose a random position between i and n-1
            int which = i + globalRandom.nextInt(n - i);
            //swap permutation[i] and permutation[which]
            int tmp = permutation[i];
            permutation[i] = permutation[which];
            permutation[which] = tmp;
        }

        return permutation;
    }

    /**
     * Calculate the sigmoid (logistic function) for an entire array (inplace, i.e. modifying its contents)
     *
     * @param x the array
     */
    public static void sigmoid(double[] x)
    {
        for (int i = 0; i < x.length; i++)
        {
            x[i] = 1 / (1 + Math.exp(-x[i]));
        }
    }

    /**
     * Read a matrix from file
     * @param fileName The file to read
     * @param separator Carachter that separates each value
     * @return a matrix with the file contents
     * @throws IOException If any errors occur when reading the file
     */
    public static double[][] readFromFile(String fileName, String separator) throws IOException
    {
        File dataFile = new File(fileName);

        BufferedReader in = new BufferedReader(new FileReader(dataFile));

        //count columns:
        int nCols = in.readLine().trim().split(separator).length;

        //count rows:
        int nRows;
        for (nRows = 1; in.readLine() != null; nRows++) ;

        double[][] data = new double[nRows][nCols];

        in = new BufferedReader(new FileReader(dataFile));
        for (int row = 0; row < nRows; row++)
        {
            String[] vars = in.readLine().trim().split(separator);
            for (int col = 0; col < nCols; col++)
            {
                data[row][col] = Double.parseDouble(vars[col]);
            }
        }

        return data;
    }

    /**
     * Transpose a matrix
     * @param m The matrix to transpose
     * @return The transposed matrix
     */
    public static double[][] transpose(double[][] m)
    {
        int nRows = m.length;
        int nCols = m[0].length;
        double[][] mT = new double[nCols][nRows];

        for (int row = 0; row < nRows; row++)
            for (int col = 0; col < nCols; col++)
                mT[col][row] = m[row][col];

        return mT;
    }

    /**
     * Sum of the values of a vector
     * @param x The vector
     * @return The sum of all the values
     */
    public static double sum(double[] x)
    {
        double sum=0;
        for (int i=0; i<x.length; i++)
        {
            sum += x[i];
        }
        return sum;
    }

    /**
     * Obtain an independent copy of the given matrix
     * @param m The matrix to copy
     * @return a new matrix, with the same values as the original
     */
    public static double[][] copy(double[][] m)
    {
        double[][] out = new double[m.length][];
        for (int i=0; i<m.length; i++)
        {
            out[i] = copy(m[i]);
        }
        return out;
    }

    /**
     * Obtain a copy of an array
     * @param x The array to copy
     * @return a new array, with the same values as the original
     */
    public static double[] copy(double[] x)
    {
        double[] out = new double[x.length];
        System.arraycopy(x, 0, out, 0, x.length);
        return out;
    }

    /**
     * N Distance between two vectors
     * @param x1 one vector
     * @param x2 the other
     * @param n
     * @return  (sum |x1[i] - x2[i]|<sup>n</sup>) <sup>1/n</sup>
     */
    public static double dist(double[] x1, double[] x2, int n)
    {
        double d = 0;
        for (int i=0; i<x1.length; i++)
            d += Math.pow(Math.abs(x1[i] - x2[i]), n);
        return Math.pow(d, (double)1/n);
    }

    /**
     * Euclidean distance between two vectors
     * @param x1 one vector
     * @param x2 the other vector
     * @return the euclidean distance between them
     */
    public static double dist2(double[] x1, double[] x2)
    {
        double d = 0;
        double p;
        for (int i=0; i<x1.length; i++)
        {
            p = x1[i] - x2[i];
            d += p*p;
        }
        return Math.sqrt(d);
    }
    
    /**
     * Euclidean distance between N vectors
     * @param x Matrix of N samples and P variables
     * @param shape It indicates if it is necessary to transpose the matrix before compute the range (true, false)
     * @return Row vector with the N*(N-1)/2 pairs of samples in x
     */
    public static double[] pdist(double[][] x, boolean ind)
    {
        if (ind)
        {
            int nRows = x.length;
            int nCols = x[0].length;
            int n = 0;
            double[] md = new double[nCols*(nCols-1)/2];
            for (int i = 0; i < (nCols - 1); i++)
            {
                for  (int j = i+1; j < nCols; j++)
                {
                    double d2 = 0;
                    for (int k = 0; k < nRows; k++)
                    {
                    d2 += Math.pow(x[k][i] - x[k][j], 2);
                    }
                    md[n] = Math.sqrt(d2);
                    n += 1;
                }
            }
            return md;
        }
        else
        {
            int nRows = x.length;
            int nCols = x[0].length;
            int n = 0;
            double[] md = new double[nRows*(nRows-1)/2];
            for (int i = 0; i < (nRows - 1); i++)
            {
                for (int j = i+1; j < nRows; j++)
                {
                    double d2 = 0;
                    for (int k = 0; k < nCols; k++)
                    {
                        d2 += Math.pow(x[i][k] - x[j][k], 2);
                    }
                    md[n] = Math.sqrt(d2);
                    n += 1;
                }
            }
            return md;   
        }
    }

    /**
     * Reformat a distance row vector (of the pdist function) to symmetric square matrix
     * @param x Row vector creted by the pdist function
     * @return Symmetric square matrix
     */
    public static double[][] squareform(double[] x)
    {
        int N;
        int k = 0;
        N = (int)(Math.ceil(Math.sqrt(2*x.length)));
        double[][] mtx = new double[N][N];
        
        for (int i = 0; i < N; i++)
        {
            for (int j = i; j < N; j++)
            {
                if (j == i)
                {
                    mtx[i][j] = 0;
                }
                else
                {
                    mtx[i][j] = x[k];
                    mtx[j][i] = x[k];
                    k += 1;
                }
            }
        }
        return mtx;
    }
    
    /**
     * Dot product between two vectors
     * @param x1 one vector
     * @param x2 the other vector
     * @return the euclidean distance between them
     */
    public static double dotProduct(double[] x1, double[] x2)
    {
        double out = 0;
        for (int i=0; i<x1.length; i++)
        {
            out += x1[i]*x2[i];
        }
        return out;
    }

    /**
     * Variance of a vector
     * @param x The vector
     * @return The sum of all the values
     */
    public static double variance(double[] x)
    {
        double[] x2 = new double[x.length];
        for (int i=0; i<x.length; i++)
        {
            x2[i] = x[i]*x[i];
        }
        return sum(x2)/x.length - Math.pow(sum(x)/x.length,2);
    }
    
}
