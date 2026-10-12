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

/**
 * Interface for container holding spectra data
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public interface DataContainer {
    public static final String SPEC_TYPE = "type";
    // values for property SPEC_TYPE:
    public static String SPECTRUM_13C    = "13C";
    public static String SPECTRUM_1H     = "1H";
    public static String SPECTRUM_IR     = "IR";
    public static String IR_ABS    = "IR absorbance";
    public static String IR_TRANS     = "IR transmission";
    public static final String SOLVENT   = "solvent";
    public static final String FREQ      = "freq";
    public static final String UNKNOWN_FREQ = "unknownFreq";

    public double getNumberProperty(String property);


    public void setNumberProperty(String property, double value);


    public String getSpectrumProperty(String property);


    public void setSpectrumProperty(String property, String value);


    /**
    * Return the minimal x-value e.g. minimal chemical shift in nmr spectrum
    * @return the minimal x-value
    */
    public double getXmin();


    /**
     * Return the maximal x-value e.g. maximal chemical shift in nmr spectrum
     * @return the maximal x-value
     */
    public double getXmax();


    /**
    * Return the maximal intensity in this spectrum
    * @param signed if TRUE, the peak orientation is used
    * @return the maximal intensity
    */
    public double getMaxIntensity(boolean signed);


    /**
    * Return the minimal intensity in this spectrum
    * @param signed if TRUE, the peak orientation is used
    * @return the minimal intensity
    */
    public double getMinIntensity(boolean signed);
    
    public AbstractDataContainer clone(boolean option);
}
