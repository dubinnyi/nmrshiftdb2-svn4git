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
 * Created on 28. Feb 2005
 */
package org.openscience.nmrshiftdb.spectrumapplet.data;

import org.openscience.nmrshiftdb.spectrumapplet.data.*;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.*;
import org.openscience.nmrshiftdb.spectrumapplet.util.*;

import java.awt.*;

import java.util.*;

/**
 * Class handling information of a peak in spectrum
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public class SpectrumPeak implements Comparable, Cloneable{
    // spectrum coordinate values, e.g. chemical shift in ppm and intensity
    private double valueX = 0;
    private double valueY = 0;

    // Graphics2D coordinates:
    private int x;
    private int y;
    private String info;

    /**
     * Creates a new instance of SpectrumPeak
     * @param x the xValue
     * @param y the yValue
     */
    public SpectrumPeak(double x, double y) {
        this(x);
        valueY = y;
    }


    /**
     * Creates a new instance of SpectrumPeak
     * @param x the xValue
     */
    private SpectrumPeak(double x) {
        valueX = x;
    }


    /**
     * Creates a new instance of SpectrumPeak
     */
    public SpectrumPeak() {
    }

    public Object clone() {
        SpectrumPeak r = new SpectrumPeak(valueX,valueY);
        r.setInfo(info);
        r.setX(x);
        r.setY(y);
        return r;
    }
 
    
    /**
    * Return the x-value
    * @return the x-value
    */
    public double getValueX() {
        return valueX;
    }


    /**
     * Set the x-value
     * @param x the x-value
     */
    public void setValueX(double x) {
        valueX = x;
    }


    /**
     * Return the peak intensity
     * @return the peak intensity
     */
    public double getIntensity() {
        return valueY;
    }


    /**
     * Set the y-value
     * @param y the y-value
     */
    public void setIntensity(double y) {
        valueY = y;
    }


    /**
     * Getter for property x.
     * @return Value of property x.
     */
    public int getX() {
        return x;
    }


    /**
     * Setter for property x.
     * @param x New value of property x.
     */
    public void setX(int x) {
        this.x = x;
    }


    /**
     * Getter for property y.
     * @return Value of property y.
     */
    public int getY() {
        return y;
    }


    /**
     * Setter for property y.
     * @param y New value of property y.
     */
    public void setY(int y) {
        this.y = y;
    }


    /**
     * Getter for property info.
     * @return Value of property info.
     */
    public String getInfo() {
        return info;
    }


    /**
     * Setter for property info.
     * @param info New value of property info.
     */
    public void setInfo(String info) {
        this.info = info;
    }
    
    /**
     * Compares two <code>Peak</code> objects by their chemical shift values.
     *
     * @param   aPeak   the <code>NmrPeak</code> to be compared.
     * @return  the value <code>0</code> if this <code>NmrPeak</code> has
     *          the same chemical shift as the argument <code>NmrPeak</code>;
     *          a value less than <code>0</code> if this <code>NmrPeak</code>
     *          has a chemical shift less than the argument <code>NmrPeak</code>;
     *          and a value greater than <code>0</code> if this <code>NmrPeak</code>
     *          has a chemical shift greater than the argument <code>NmrPeak</code>
     *          (signed comparison).
     */
    public int compareTo(SpectrumPeak aPeak) {
        double sh = aPeak.getValueX();
        return (getValueX() < sh ? -1 : (getValueX() == sh ? 0 : 1));
    }


    /**
     * Compares this <code>NmrPeak</code> object to another object.
     * If the object is an <code>NmrPeak</code>, this function behaves
     * like <code>compareTo(NmrPeak)</code>.  Otherwise, it throws a
     * <code>ClassCastException</code> (as <code>NmrPeak</code>
     * objects are only comparable to other <code>NmrPeak</code>
     * objects).
     *
     * @return the value <code>0</code> if the argument is a
     *         <code>NmrPeak</code> with the same chemical shift as this
     *         <code>NmrPeak</code>; a value less than <code>0</code>
     *         if the argument is a <code>NmrPeak</code> with a chemical
     *         shift greater than this <code>NmrPeak</code>; and a value
     *         greater than <code>0</code> if the argument is a
     *         <code>NmrPeak</code> with a chemical shift less than this
     *         <code>NmrPeak</code>.
     * @param o the <code>Object</code> to be compared.
     */
    public int compareTo(Object o) {
        return compareTo((SpectrumPeak) o);
    }
}
