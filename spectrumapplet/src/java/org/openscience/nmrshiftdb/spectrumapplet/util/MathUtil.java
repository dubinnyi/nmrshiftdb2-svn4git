/*
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 *  02111-1307  USA.
 *
 * Created on 24. November 2004
 */
package org.openscience.nmrshiftdb.spectrumapplet.util;

/**
 * Provides math functions
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public class MathUtil {

    /**
     * The lorentz equation used to calculated the peak curve
     * @param x the x positon
     * @param max the peak intensity
     * @param center the chemical shift of the peak
     * @return the y value for specified x position
     */
    public static double lorentz(double x, double max, double center) {
        return max / (1 + 18000 * Math.pow((x - center), 2));
    }


    /**
     * The lorentz equation used to calculated the peak curve
     * @param x the x positon
     * @param max the peak intensity
     * @param center the chemical shift of the peak
     * @param hw 'half-width': f(center - hw)=max/2 , 2*hw ==> line width of a peak
     * @return the y value for specified x position
     */
    public static double lorentz(double x, double max, double center, double hw) {
        double f = 1 / (hw * hw);
        return max / (1 + f * Math.pow((x - center), 2));
    }


    /**
     * Return the next smaler value:
     *     nextSmallerValue(1.123,1) = 1.1
     *     nextSmallerValue(1.123,2) = 1.12
     *     nextSmallerValue(1.123,0) = 1
     *     nextSmallerValue(11.123,-1) = 10
     *     nextSmallerValue(1.123,-1) = 0
     *     nextSmallerValue(-1.123,1) = 1.2
     *     nextSmallerValue(-1.123,0) = -2
     *     nextSmallerValue(-11.123,-1) = -20
     *     nextSmallerValue(-1.123,-1) = -10
     * @param z number of digit used, count from colon
     * @param x the value
     * @return the next smaller value
     */
    public static double nextSmallerValue(double x, double z) {
        double h = Math.pow(10, z);
        double r = ((int) (x * h) + ((x < 0) ? -1 : 0)) / h;
        return r;
    }


    /**
     * Return the next greater value:
     *     nextGreaterValue(1.123,1) = 1.2
     *     nextGreaterValue(1.123,2) = 1.13
     *     nextGreaterValue(1.123,0) = 2
     *     nextGreaterValue(11.123,-1) = 20
     *     nextGreaterValue(1.123,-1) = 10
     *     nextGreaterValue(-1.123,1) = -1.1
     *     nextGreaterValue(-1.123,0) = -1
     *     nextGreaterValue(-11.123,-1) = -10
     *     nextGreaterValue(-1.123,-1) = 0
     * @param z number of digit used, count from colon
     * @param x the value
     * @return the next greater value
     */
    public static double nextGreaterValue(double x, double z) {
        double h = Math.pow(10, z);
        double r = ((int) (x * h) + ((x < 0) ? 0 : 1)) / h;
        return r;
    }


    public static double round(double val, int digitsBehindColon) {
        double h = Math.pow(10, digitsBehindColon);
        return (double) Math.floor(val * h + sign(val) * 0.5) / h;
    }


    public static double getBinominalCoefficient(int n, int k) {
        // ( n )       n!
        // (   )  = ---------
        // ( k )    k!(n-k)!
        double b = 0;
        b = getFaculty(n) / (getFaculty(k) * getFaculty(n - k));
        return b;
    }


    public static double getFaculty(int n) {
        double f = 1;
        if ((n < 0) || (n > 170)) {
            f = Double.NaN;
        }
        else if (n > 0) {
            for (int i = 1; i <= n; i++) {
                f = f * i;
            }
        }
        return f;
    }


    public static int sign(double x) {
        int r = 0;
        if (x > 0) {
            r = 1;
        }
        else if (x < 0) {
            r = -1;
        }
        return r;
    }


    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        MathUtil m = new MathUtil();
        System.out.println("nextSmallerValue(1.123,1) =      1.1  ==> " + m.nextSmallerValue(1.123, 1));
        System.out.println("nextSmallerValue(1.123,2) =      1.12 ==> " + m.nextSmallerValue(1.123, 2));
        System.out.println("nextSmallerValue(1.123,0) =      1    ==> " + m.nextSmallerValue(1.123, 0));
        System.out.println("nextSmallerValue(11.123,-1) =   10    ==> " + m.nextSmallerValue(11.123, -1));
        System.out.println("nextSmallerValue(1.123,-1) =     0    ==> " + m.nextSmallerValue(1.123, -1));
        System.out.println("nextSmallerValue(-1.123,1) =    -1.2  ==> " + m.nextSmallerValue(-1.123, 1));
        System.out.println("nextSmallerValue(-1.123,0) =    -2    ==> " + m.nextSmallerValue(-1.123, 0));
        System.out.println("nextSmallerValue(-11.123,-1) = -20    ==> " + m.nextSmallerValue(-11.123, -1));
        System.out.println("nextSmallerValue(1.123,-1) =   -10    ==> " + m.nextSmallerValue(-1.123, -1));
        System.out.println("nextGreaterValue(1.123,1) =      1.2  ==> " + m.nextGreaterValue(1.123, 1));
        System.out.println("nextGreaterValue(1.123,2) =      1.13 ==> " + m.nextGreaterValue(1.123, 2));
        System.out.println("nextGreaterValue(1.123,0) =      2    ==> " + m.nextGreaterValue(1.123, 0));
        System.out.println("nextGreaterValue(11.123,-1) =   20    ==> " + m.nextGreaterValue(11.123, -1));
        System.out.println("nextGreaterValue(1.123,-1) =    10    ==> " + m.nextGreaterValue(1.123, -1));
        System.out.println("nextGreaterValue(-1.123,1) =    -1.1  ==> " + m.nextGreaterValue(-1.123, 1));
        System.out.println("nextGreaterValue(-1.123,0) =    -1    ==> " + m.nextGreaterValue(-1.123, 0));
        System.out.println("nextGreaterValue(-11.123,-1) = -10    ==> " + m.nextGreaterValue(-11.123, -1));
        System.out.println("nextGreaterValue(-1.123,-1) =    0    ==> " + m.nextGreaterValue(-1.123, -1));
        System.out.println("1000!= " + m.getFaculty(1000));
        System.out.println(" 180 over 80= " + getBinominalCoefficient(180, 80));
        System.out.println(" 6 over 2= " + getBinominalCoefficient(6, 2));
    }
}
