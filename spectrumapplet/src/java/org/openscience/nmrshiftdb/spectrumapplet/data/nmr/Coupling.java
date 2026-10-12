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
 * Created on 29. November 2004
 */
package org.openscience.nmrshiftdb.spectrumapplet.data.nmr;

import org.openscience.nmrshiftdb.spectrumapplet.util.*;

/**
 * Specifies a coupling
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public class Coupling {
    // relative intensities for multpletts with I=1
    private static double[][] FACTORS      = new double[][] {
                                                 {  },
                                                 { 1 },
                                                 {  },
                                                 { (double) 1 / 3, (double) 1 / 3, (double) 1 / 3 },
                                                 {  },
                                                 {
                                                     (double) 1 / 9, (double) 2 / 9, (double) 3 / 9,
                                                     (double) 2 / 9, (double) 1 / 9
                                                 },
                                                 {  },
                                                 {
                                                     (double) 1 / 27, (double) 3 / 27, (double) 6 / 27,
                                                     (double) 7 / 27, (double) 6 / 27, (double) 3 / 27,
                                                     (double) 1 / 27
                                                 }
                                             };

    // the peak orientation in j-modulation display
    private static final int[] ORIENTATION = new int[] { 1, 1, -1, 1, -1, 1, -1, 1, -1, 1, 1 };

    // elements with spin quantum number 0.5
    private static final String ELEMENT_05 = "H,C,15N,F,Si,P";

    // elements with spin quantum number 1
    private static final String ELEMENT_1  = "D,Li,14N";
    private double j;
    private int multi;
    private int partner                    = -1;
    private String partnerElement          = "H";
    private double spinQuantumNumber       = 0.5;
    private double frequency               = 100;

    /**
     * Creates a new instance of Coupling
     *
     */
    public Coupling() {
        j     = 0;
        multi = NmrPeak.UNKNOWN_MULTI;
    }


    /**
     * Creates a new instance of Coupling
     * @param j the coupling constant
     * @param m the multiplicity
     */
    public Coupling(double j, int m) {
        this.j = j;
        multi  = m;
    }

    /**
     * Set the coupling atom for this coupling
     * @param element the coupling atom
     */
    public void setElement(String element) {
        partnerElement = element;
        if (ELEMENT_05.indexOf(partnerElement) > -1) {
            spinQuantumNumber = 0.5;
        }
        else if (ELEMENT_1.indexOf(partnerElement) > -1) {
            spinQuantumNumber = 1;
        }
    }


    /**
     * Returns the relative intensity of a multiplett caused by this coupling
     * used during Peak.getMultiplett()
     * @param position the peak position of the peak in multiplett
     * @return the relative intensity
     */
    public double getMultiplettFactor(int position) {
        double r = 0;
        int n    = multi - 1;
        int sum  = (int) Math.pow(2, n);
        r        = MathUtil.getBinominalCoefficient(n, position) / sum;
        if (spinQuantumNumber == 1) {
            n = multi;
            r = FACTORS[multi][position];
        }
        return r;
    }


    /**
     * Returns the multiplett orientation caused by this coupling
     * @return the orientation
     */
    public int getOrientation() {
        int r = ORIENTATION[multi];
        if (spinQuantumNumber == 1) {
            r = ORIENTATION[NmrPeak.SINGULETT];
        }
        return r;
    }


    /**
     * Returns the coupling atom for this coupling
     * @return the coupling atom
     */
    public String getElement() {
        return partnerElement;
    }


    /**
     * Get the spectrometer frequency (in MHz)
     * @return the spectrometer frequency (in MHz)
     */
    public double getSpectrometerFrequency() {
        return frequency;
    }


    /**
     * Set the spectrometer frequency (in MHz)
     * @param f the spectrometer frequency (in MHz)
     */
    public void setSpectrometerFrequency(double f) {
        frequency = f;
    }


    /**
     * Get the multiplicity
     * @return the multiplicity
     */
    public int getMultiplicity() {
        return multi;
    }


    /**
     * Set the multiplicity
     * @param m the multiplicity
     */
    public void setMultiplicity(int m) {
        multi = m;
    }


    /**
     * Return coupling constant (in ppm)
     * @return the coupling constant
     */
    public double getCouplingConstantInPPM() {
        return j / frequency;
    }


    /**
     * Return coupling constant (in Hz)
     * @return the coupling constant
     */
    public double getCouplingConstantInHz() {
        return j;
    }


    /**
     * Set coupling constant (in Hz)
     * @param jHz the coupling constant
     */
    public void setCouplingConstantInHz(double jHz) {
        j = jHz;
    }


    /**
     * Return string representation
     * @return string representation
     */
    public String toString() {
        String s = "J(";
        if (partner > -1) {
            s += partner;
        }
        if (!partnerElement.equals("H")) {
            s += "-" + partnerElement;
        }
        s += ")=" + j + "Hz,(" + NmrPeak.MULTIPLICITY[multi] + ")";
        return s;
    }


    /**
     * Get atom number of coupling partner
     * @return atom number of coupling partner
     */
    public int getPartner() {
        return partner;
    }


    /**
     * Set atom number of coupling partner
     * @param partner atom number of coupling partner
     */
    public void setPartner(int partner) {
        this.partner = partner;
    }
}
