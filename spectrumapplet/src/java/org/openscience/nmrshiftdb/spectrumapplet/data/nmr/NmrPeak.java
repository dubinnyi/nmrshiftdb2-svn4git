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
 * Created on 2. November 2004 
 */
package org.openscience.nmrshiftdb.spectrumapplet.data.nmr;

import org.openscience.nmrshiftdb.spectrumapplet.data.*;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.*;
import org.openscience.nmrshiftdb.spectrumapplet.util.*;

import java.awt.*;

import java.util.*;

/**
 * Class handling all information of a peak in spectrum
 * a peak can represent multiple atoms
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public class NmrPeak extends SpectrumPeak {
    // Constant for atom numbers
    public static final int SOLVENT           = 99999;
    public static final int NUMBER_UNKNOWN    = -99999;

    // Constants for multiplicity
    public static final int UNKNOWN_MULTI     = 0;
    public static final int SINGULETT         = 1;
    public static final int DUPLETT           = 2;
    public static final int TRIPLETT          = 3;
    public static final int QUARTETT          = 4;
    public static final int QUINTETT          = 5;
    public static final int SEXTETT           = 6;
    public static final int SEPTETT           = 7;
    public static final int OCTETT            = 8;
    public static final int NONETT            = 9;
    public static final int MIXED_MULTI       = 10;
    public static final Color[] PEAK_COLOR    = new Color[] {
                                                    Color.BLACK, Color.RED, Color.BLUE, Color.GREEN,
                                                    Color.BLACK, Color.MAGENTA, Color.ORANGE, Color.CYAN,
                                                    Color.YELLOW, Color.PINK
                                                };
    public static final String[] MULTIPLICITY = new String[] {
                                                    "?", "s", "d", "t", "q", "5", "6", "7", "8", "9", "mix"
                                                };

    // Constants for the standard signal intensity, multiplicity used as index 
    public static final int[] INTENSITIES     = new int[] { 80, 15, 80, 80, 80, 80, 80, 80, 80, 80, 80 };
    private ArrayList predictions;
    private double integral;
    private int atomNumber;
    private int multiplettPosition            = -1;

    // corresponding multiplicities
    private ArrayList couplings;
    private int sign                          = 0;
    private boolean solventPeak;

    /**
     * Creates a new instance of Peak
     * @param number the corresponding atom number
     * @param chemShift the chemical shift in ppm
     * @param intens the intensity
     */
    public NmrPeak(double chemShift, double intens, int number) {
        this(chemShift, intens);
        atomNumber = number;
    }


    /**
     * Creates a new instance of Peak
     * @param chemShift the chemical shift in ppm
     * @param intens the intensity
     */
    public NmrPeak(double chemShift, double intens) {
        this(chemShift);
        setIntensity ( intens);
    }


    /**
     * Creates a new instance of Peak
     * @param chemShift the chemical shift in ppm
     */
    public NmrPeak(double chemShift) {
        this();
        setChemicalShift(chemShift);
    }


    /**
     * Creates a new instance of Peak
     * default intensity is 1, atomnumber is unknown
     */
    public NmrPeak() {
        atomNumber  = SpectrumUtil.getInstance().getNextUnknownAtomNumber();
        predictions = new ArrayList();
        couplings   = new ArrayList();
        integral    = 1;
    }

    /**
     * Return the string representation of the peak couplings multiplicities
     * @return the string representation of the couplings multiplicities
     */
    public String multiplicityToString() {
        String s     = "(";
        Coupling[] c = getCouplings();
        for (int i = 0; i < c.length; i++) {
            s += MULTIPLICITY[c[i].getMultiplicity()];
        }
        s += ")";
        return s;
    }


    /**
     * Return the multiplett based on the peak couplings
     * @param pl the peaklist of the spectrum
     * @return array of peaks
     */
    public NmrPeak[] getMultiplett(NmrData pl, boolean roofEffectEnabled) {
        NmrPeak[] r         = null;
        Coupling[] c     = getCouplings();
        ArrayList childs = new ArrayList();
        childs.add((NmrPeak) this.clone());
        for (int i = 0; i < c.length; i++) {
            childs = generateChilds(childs, c[i], pl, roofEffectEnabled);
        }
        r = new NmrPeak[childs.size()];
        childs.toArray(r);
        Arrays.sort(r);
        for (int i = 0; i < r.length; i++) {
            r[i].setMultiplettPosition(i);
        }
        return r;
    }


    private ArrayList generateChilds(ArrayList childs, Coupling c, NmrData pl, boolean roofEffectEnabled) {
        ArrayList r = new ArrayList();
        if (c.getMultiplicity() == UNKNOWN_MULTI || c.getMultiplicity() == MIXED_MULTI) {
            r = childs;
        }
        else {
            r = new ArrayList(childs.size() * c.getMultiplicity());
            NmrPeak[] m = null;
            for (int i = 0; i < childs.size(); i++) {
                m = calcMultiplett((NmrPeak) childs.get(i), c, pl, roofEffectEnabled);
                for (int ii = 0; ii < m.length; ii++) {
                    r.add(m[ii]);
                }
            }
        }
        return r;
    }


    private NmrPeak[] calcMultiplett(NmrPeak p, Coupling c, NmrData pl, boolean roofEffectEnabled) {
        double startShift = p.getValueX() -
                            (((double) c.getMultiplicity()) / 2 - 0.5) * c.getCouplingConstantInPPM();
        double f          = 0;
        NmrPeak[] r          = new NmrPeak[c.getMultiplicity()];
        for (int i = 0; i < c.getMultiplicity(); i++) {
            r[i] = (NmrPeak) p.clone();
            f    = c.getMultiplettFactor(i);
            r[i].setIntegral(p.getIntegral() * f);
            r[i].setIntensity(p.getIntensity() * f);
            r[i].setChemicalShift(startShift + i * c.getCouplingConstantInPPM());
        }
        if (roofEffectEnabled) {
            calcRoofEffect(r, c, pl);
        }
        return r;
    }


    private void calcRoofEffect(NmrPeak[] p, Coupling c, NmrData pl) {
        NmrPeak partner = pl.getPeakForAtomnumber(c.getPartner());
        if (partner != null) {
            double delta = c.getSpectrometerFrequency() * (getValueX() - partner.getValueX()) / c.getCouplingConstantInHz();
            if (Math.abs(delta) <= CommonUtils.getNumberProperty("roofEffectLimit", 10)) {
                double h      = 0;
                double deltaJ = (1 / Math.pow((Math.abs(delta) + 1), 1)) * c.getCouplingConstantInPPM() / 2;
                int m         = p.length / 2;
                int s         = MathUtil.sign(delta);
                double f      = calcFactor(delta);
                if (m * 2 == p.length) {
                    // calculation of center shift
                    for (int i = 0; i < m; i++) {
                        h = p[i].getIntensity();
                        p[i].setIntensity(h + s * h * f);
                        h = p[i].getIntegral();
                        p[i].setIntegral(h + s * h * f);
                        h = p[i].getValueX() + s * deltaJ;
                        p[i].setChemicalShift(h);
                    }
                    for (int i = m; i < p.length; i++) {
                        h = p[i].getIntensity();
                        p[i].setIntensity(h - s * h * f);
                        h = p[i].getIntegral();
                        p[i].setIntegral(h - s * h * f);
                        h = p[i].getValueX() + s * deltaJ;
                        p[i].setChemicalShift(h);
                    }
                }
                else {
                    // for triplett,quintett,septett,... no center shift calculation
                    for (int i = 0; i < m; i++) {
                        h = p[i].getIntensity();
                        p[i].setIntensity(h + s * h * f);
                        h = p[i].getIntegral();
                        p[i].setIntegral(h + s * h * f);
                    }
                    for (int i = m + 1; i < p.length; i++) {
                        h = p[i].getIntensity();
                        p[i].setIntensity(h - s * h * f);
                        h = p[i].getIntegral();
                        p[i].setIntegral(h - s * h * f);
                    }
                }
            }
        }
    }


    private double calcFactor(double delta) {
        double h = (delta * delta + 1) / Math.pow((delta * delta + 1), 1.6);
        return h;
    }


    /**
     * Add a <CODE>Coupling</CODE> to this peak
     * @param c a coupling
     */
    public void addCoupling(Coupling c) {
        couplings.add(c);
    }


    /**
     * Return the couplings defined for this peak;
     * if no coupling input available the multiplicity is set to UNKNOWN
     * @return an array of couplings
     */
    public Coupling[] getCouplings() {
        Coupling[] r = new Coupling[] { new Coupling() };
        if (couplings.size() > 0) {
            r = new Coupling[couplings.size()];
            for (int i = 0; i < couplings.size(); i++) {
                r[i] = (Coupling) couplings.get(i);
            }
        }
        return r;
    }





    /**
     * Set the chemical shift
     * @param shift the chemical shift in ppm
     */
    public void setChemicalShift(double shift) {
        // format: 000.000
        setValueX( MathUtil.round(shift, 3));
    }


    /**
     * Add prediction info object for this peak, multi predictions are possible
     * @param predict the <CODE>Prediction</CODE>
     */
    public void addPrediction(Prediction predict) {
        predictions.add(predict);
    }


    /**
     * Return an array of <CODE>Prediction</CODE>
     * @return an array of <CODE>Prediction</CODE>, null if no prediction info availlable
     */
    public Prediction[] getPredictions() {
        Prediction[] r = null;
        if (predictions.size() > 0) {
            r = new Prediction[predictions.size()];
            predictions.toArray(r);
        }
        return r;
    }


    /**
     * Return array of atom numbers
     * @return an array of atom numbers
     */
    public void clearAtomNumber() {
        atomNumber = SpectrumUtil.getInstance().getNextUnknownAtomNumber();
    }


    /**
     * Return atom number,
     * - if number < -99999 this indicates an peak with unkown atomnumber
     * - if number > 99999 this indicates an solvent peak
     * @return the atom number
     */
    public int getAtomNumber() {
        return atomNumber;
    }


    /**
     * Add a new atom number to this peak, a peak can represent multiple atoms
     * @param no the atom number
     */
    public void setAtomNumber(int no) {
        atomNumber = no;
    }


    /**
     * Getter for property integral.
     * @return Value of property integral.
     */
    public double getIntegral() {
        return integral;
    }


    /**
     * Setter for property integral.
     * @param integral New value of property integral.
     */
    public void setIntegral(double integral) {
        this.integral = integral;
    }




    /**
     * Return the peak intensity, the peak orientation in a j-modulation experiment is used
     * @return the peak intensity with orientation info
     */
    public double getSignedIntensity() {
        return getIntensity() * getOrientation();
    }


    /**
     * Set the peak intensity
     * @param intens the peak intensity
     */
    public void setIntensity(double intens) {
        // format: 000.00000
        super.setIntensity( MathUtil.round(intens, 5));
    }


    /**
     * Return the peak orientation in a j-modulation experiment ('spin-echo')
     * (for multi couplings e.g. in a 1H spectrum the greatest coupling is used)
     * @return the peak orientation
     */
    public int getOrientation() {
        if (sign == 0) {
            Coupling[] c = getCouplings();
            if (c.length == 1) {
                sign = c[0].getOrientation();
            }
            else {
                double j = 0;
                for (int i = 0; i < c.length; i++) {
                    if (j < Math.abs(c[i].getCouplingConstantInHz())) {
                        j    = Math.abs(c[i].getCouplingConstantInHz());
                        sign = c[i].getOrientation();
                    }
                }
            }
        }
        return sign;
    }





    /**
     * Clone the NmrPeak
     * @return a clone of this peak
     */
    public Object clone() {
        NmrPeak r = new NmrPeak(getValueX(), getIntensity(), atomNumber);
        r.setIntegral(getIntegral());
        for (int i = 0; i < predictions.size(); i++) {
            r.addPrediction((Prediction) predictions.get(i));
        }
        for (int i = 0; i < couplings.size(); i++) {
            r.addCoupling((Coupling) couplings.get(i));
        }
        r.setX(getX());
        r.setY(getY());
        r.setInfo(getInfo());
        r.setMultiplettPosition(multiplettPosition);
        return r;
    }


    /**
     * Return string representation of this peak
     * @return string representation of this peak
     */
    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append(getValueX()).append("ppm, (");
        Coupling[] c = getCouplings();
        for (int i = 0; i < c.length; i++) {
            s.append(MULTIPLICITY[c[i].getMultiplicity()]);
        }
        s.append("), ");
        s.append(getIntensity());
        if (atomNumber > SOLVENT) {
            s.append(",  Solvent");
        }
        else {
            s.append(",  Atom: ");
            if (atomNumber < NUMBER_UNKNOWN) {
                s.append("?");
            }
            else {
                s.append(atomNumber);
            }
        }
        return s.toString();
    }



    /**
     * Getter for property solventPeak.
     * @return Value of property solventPeak.
     */
    public boolean isSolventPeak() {
        return solventPeak;
    }


    /**
     * Setter for property solventPeak.
     * @param isSolventPeak New value of property solventPeak.
     */
    public void setSolventPeak(boolean isSolventPeak) {
        solventPeak = isSolventPeak;
    }


    /**
     * Get the position of this peak in a multiplett, if -1: no multiplett part
     * @return the position of this peak in a multiplett
     */
    public int getMultiplettPosition() {
        return multiplettPosition;
    }


    /**
     * Set the position of this peak in a multiplett, if -1: no multiplett part
     * @param multiplettPosition the position of this peak in a multiplett
     */
    public void setMultiplettPosition(int multiplettPosition) {
        this.multiplettPosition = multiplettPosition;
    }
}
