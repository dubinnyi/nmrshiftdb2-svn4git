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
 * Created on 11. Februar 2005
 */
package org.openscience.nmrshiftdb.spectrumapplet.data;

import java.util.*;

/**
 * Abstract implementaion of DataContainer
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public abstract class AbstractDataContainer implements DataContainer {
    private Map properties = new HashMap();

    public void setSpectrumProperties(Map theProperties) {
        properties = theProperties;
    }


    public double getNumberProperty(String property) {
        double r = Double.NaN;
        Double d = (Double) properties.get(property);
        if (d != null) {
            r = d.doubleValue();
        }
        return r;
    }


    public void setNumberProperty(String property, double value) {
        properties.put(property, new Double(value));
    }


    public String getSpectrumProperty(String property) {
        return (String) properties.get(property);
    }


    public void setSpectrumProperty(String property, String value) {
        properties.put(property, value);
    }


    /**
     * Return the minimal x-value e.g. minimal chemical shift in nmr spectrum
     * @return the minimal x-value
     */
    public abstract double getXmin();


    /**
     * Return the maximal x-value e.g. maximal chemical shift in nmr spectrum
     * @return the maximal x-value
     */
    public abstract double getXmax();


    /**
    * Return the maximal intensity in this spectrum
    * @param signed if TRUE, the peak orientation is used
    * @return the maximal intensity
    */
    public abstract double getMaxIntensity(boolean signed);


    /**
    * Return the minimal intensity in this spectrum
    * @param signed if TRUE, the peak orientation is used
    * @return the minimal intensity
    */
    public abstract double getMinIntensity(boolean signed);


    public abstract AbstractDataContainer clone(boolean option);




    public Map cloneProperties() {
        return new HashMap(properties);
    }


    /**
     * Return TRUE if spectrum  is a 13C spectrum
     * @return TRUE if 13C spectrum
     */
    public boolean is13Cspectrum() {
        return isSpecType(SPECTRUM_13C);
    }


    public boolean is1Hspectrum() {
        return isSpecType(SPECTRUM_1H);
    }


    public boolean isIRspectrum() {
        return isSpecType(SPECTRUM_IR);
    }


    private boolean isSpecType(String type) {
        boolean r = false;
        String p  = getSpectrumProperty(SPEC_TYPE);
        if ((p != null) && (p.equals(type))) {
            r = true;
        }
        return r;
    }
}
