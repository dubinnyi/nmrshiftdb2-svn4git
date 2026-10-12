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
 * Created on 9. November 2004
 */
package org.openscience.nmrshiftdb.spectrumapplet.util;

import org.openscience.nmrshiftdb.spectrumapplet.data.*;
import org.openscience.nmrshiftdb.spectrumapplet.data.nmr.*;

import java.util.*;

/**
 * Utility class to parse spectrum data information/input
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public class ParserUtil {
    private static double jC = CommonUtils.getNumberProperty("j(CH)", 16);

    public static NmrPeak[] parseSpectrum(String spectrum, String specType)
                                   throws NumberFormatException {
        NmrPeak[] r = null;
        if ((spectrum != null) && (spectrum.length() > 0)) {
            if (specType.equals(DataContainer.SPECTRUM_13C)) {
                if (spectrum.indexOf('=') > -1) {
                    r = parseTheSpectrum(spectrum, true);
                }
                else {
                    // supports "old" NMRSHIFTDB format
                    r = parseSpectrum13C(spectrum);
                }
            }
            else {
                r = parseTheSpectrum(spectrum, false);
            }
        }
        return r;
    }


    /**
     * Return an array of peaks for the spezified spectrum
     * @param spectrum the spectrum info,
     *                 format: "shift; intensity; atomnumber| shift;...|",
     *                 ATTENTION: by default the atom count starts with 0 !!
     *                 the multiplicity may be specified by adding a letter at
     *                 the end of a peak value (allowed: 's','d','t','q', not case sensitve)
     *                 example: "12.4;1t;4|15.0;2;6Q|23,5S;6;7|"
     * @throws NumberFormatException thrown if illegal number format
     * @return array of peaks
     */
    public static NmrPeak[] parseSpectrum13C(String spectrum)
                                      throws NumberFormatException {
        NmrPeak[] r        = null;
        Coupling coupl     = null;
        String s           = replaceIllegal(spectrum);
        StringTokenizer t  = new StringTokenizer(s, "|");
        String p           = null;
        StringTokenizer tp = null;
        ArrayList l        = new ArrayList();
        String m           = "";
        while (t.hasMoreTokens()) {
            p  = t.nextToken().trim();
            tp = new StringTokenizer(p, ";");
            if (tp.hasMoreTokens()) {
                //chemical shift exist
                m = "";
                NmrPeak pk = new NmrPeak();
                p = tp.nextToken().trim();
                if (Character.isLetter(p.charAt(p.length() - 1))) {
                    m = String.valueOf(p.charAt(p.length() - 1));
                    p = p.substring(0, p.length() - 1);
                }
                pk.setChemicalShift(Double.parseDouble(p));
                if (tp.hasMoreTokens()) {
                    //intensity exist
                    p = tp.nextToken().trim();
                    if (Character.isLetter(p.charAt(p.length() - 1))) {
                        m = String.valueOf(p.charAt(p.length() - 1));
                        p = p.substring(0, p.length() - 1);
                    }
                    pk.setIntensity(Double.parseDouble(p));
                    if (tp.hasMoreTokens()) {
                        //atomnumber exist
                        p = tp.nextToken().trim();
                        if (Character.isLetter(p.charAt(p.length() - 1))) {
                            m = String.valueOf(p.charAt(p.length() - 1));
                            p = p.substring(0, p.length() - 1);
                        }
                        pk.setAtomNumber(Integer.parseInt(p) + 1);
                    }
                }
                if (m.length() > 0) {
                    coupl = new Coupling(jC, getMultiplicityValue(m));
                    pk.addCoupling(coupl);
                }
                if (pk.getIntensity() == 0) {
                    // for 13C: try to estimate intensity
                    pk.setIntensity(pk.INTENSITIES[pk.getCouplings()[0].getMultiplicity()]);
                }
                l.add(pk);
            }
        }
        if (l.size() > 0) {
            r = new NmrPeak[l.size()];
            l.toArray(r);
        }
        return r;
    }


    public static int getMultiplicityValue(String m) {
        int r    = NmrPeak.UNKNOWN_MULTI;
        String s = m.toLowerCase();
        for (int i = 0; i < NmrPeak.MULTIPLICITY.length; i++) {
            if (s.equals(NmrPeak.MULTIPLICITY[i])) {
                r = i;
                break;
            }
        }
        return r;
    }


    /**
     * Return an array of predictions for the specified spectrum
     * @param predict the prediction info,
     *                 format:
     *                     a) "SHIFT|shift; shift;shift; ..| SHIFT|shift;shift;|...|",
     *                                SHIFT: the predicted shift
     *                                shift: the shift used for prediction
     *                     b) "AtomNo:shift; shift;shift; ..| AtomNo:shift;shift;|...|",
     *                                AtomNo: the atom predicted
     *                                shift:  the shift used for prediction
     * @throws NumberFormatException thrown if illegal number format
     * @return array of predictions
     */
    public static Prediction[] parsePrediction(String predict) {
        Prediction[] r = null;
        if ((predict != null) && (predict.length() > 0)) {
            String s           = replaceIllegal(predict);
            StringTokenizer t  = new StringTokenizer(s, "|");
            String p           = null;
            StringTokenizer tp = null;
            ArrayList l        = new ArrayList();
            double[] base      = null;
            int z              = 0;
            if (s.indexOf(':') > -1) {
                while (t.hasMoreTokens()) {
                    // atom number
                    p = t.nextToken().trim();
                    z = p.indexOf(':');
                    Prediction pre = new Prediction();
                    pre.setAtomNo(Integer.parseInt(p.substring(0, z)));
                    // the prediction base
                    p  = p.substring(z + 1);
                    tp = new StringTokenizer(p, ";");
                    if (tp.hasMoreTokens()) {
                        base = new double[tp.countTokens()];
                        z    = 0;
                        while (tp.hasMoreTokens()) {
                            p       = tp.nextToken().trim();
                            base[z] = MathUtil.round(Double.parseDouble(p), 2);
                            z++;
                        }
                        pre.setShiftsUsedForPrediction(base);
                        l.add(pre);
                    }
                }
            }
            else {
                // 'old' NMRSHIFTDB format, version a)
                while (t.hasMoreTokens()) {
                    // chemical shift
                    p = t.nextToken().trim();
                    Prediction pre = new Prediction();
                    pre.setChemicalShift(MathUtil.round(Double.parseDouble(p), 2));
                    // the prediction base
                    p  = t.nextToken().trim();
                    tp = new StringTokenizer(p, ";");
                    if (tp.hasMoreTokens()) {
                        base = new double[tp.countTokens()];
                        z    = 0;
                        while (tp.hasMoreTokens()) {
                            p       = tp.nextToken().trim();
                            base[z] = MathUtil.round(Double.parseDouble(p), 2);
                            z++;
                        }
                        pre.setShiftsUsedForPrediction(base);
                        l.add(pre);
                    }
                }
            }
            if (l.size() > 0) {
                r = new Prediction[l.size()];
                l.toArray(r);
            }
        }
        return r;
    }


    public static String replaceIllegal(String str) {
        String s = str.replace('\n', ' ');
        s = s.replace('\t', ' ');
        return s;
    }


    /**
     * Return an array of peaks for the specified spectrum
     * @param spectrum the spectrum info, atom number count starts with 1 !!!!
     *                 format:
     *                 "shift; intensity:integral;atomNo;atomNo-multiplicity:coupling;..| shift;...|",
     *                 ATTENTION: by default the atom count starts with 0 !! (in NMRSHIFTDB)
     *                 the multiplicity is specified by adding:
     *                       's','d','t','q','5','6','7','8' (not case sensitve)
     *                 example: "S(3.0;3;1)=1.7;J(t;2;H)=7.5|S(2.0;2;2)=3.4;J(q;1)=7.5"
     * @throws NumberFormatException thrown if illegal number format
     * @return array of peaks
     */
    public static NmrPeak[] parseTheSpectrum(String spectrum, boolean is13Cspectrum)
                                      throws NumberFormatException {
        NmrPeak[] r             = null;
        double hh               = 0;
        int z                   = 0;
        String s                = replaceIllegal(spectrum);
        StringTokenizer t       = new StringTokenizer(s, "|");
        String p                = null;
        StringTokenizer tp      = null;
        StringTokenizer details = null;
        ArrayList l             = new ArrayList();
        Coupling c              = null;
        NmrPeak pk              = null;
        while (t.hasMoreTokens()) {
            p  = t.nextToken().trim();
            tp = new StringTokenizer(p, ";");
            if (tp.hasMoreTokens()) {
                //chemical shift defined, e.g.: S(3.0,3,1)=1.7
                pk = new NmrPeak();
                p  = tp.nextToken().trim();
                z  = p.indexOf('=');
                pk.setChemicalShift(Double.parseDouble(p.substring(z + 1)));
                z = p.indexOf('(');
                if (z > -1) {
                    //details exist
                    p       = p.substring(z + 1, p.indexOf(')'));
                    details = new StringTokenizer(p, ",");
                    if (details.hasMoreTokens()) {
                        // intensity AND integral values exist:
                        p = details.nextToken().trim();
                        pk.setIntensity(Double.parseDouble(p));
                        p  = details.nextToken().trim();
                        hh = Double.parseDouble(p);
                        if (hh < 0.00001) {
                            // if integral is 0
                            hh = 1;
                        }
                        pk.setIntegral(hh);
                    }
                    if (details.hasMoreTokens()) {
                        // atomNo exist:
                        p = details.nextToken().trim();
                        pk.setAtomNumber(Integer.parseInt(p));
                        if (details.hasMoreTokens()) {
                            // special case for 13C data: multiplicity definition
                            p = details.nextToken().trim();
                            c = new Coupling(jC, getMultiplicityValue(p));
                            pk.addCoupling(c);
                        }
                    }
                }
                while (tp.hasMoreTokens()) {
                    // coupling data exist, e.g.: J(t-D,2)=7.5
                    p = tp.nextToken().trim();
                    z = p.indexOf('=');
                    c = new Coupling();
                    c.setCouplingConstantInHz(Double.parseDouble(p.substring(z + 1)));
                    z = p.indexOf('(');
                    if (z > -1) {
                        //details exist
                        p       = p.substring(z + 1, p.indexOf(')'));
                        details = new StringTokenizer(p, ",");
                        if (details.hasMoreTokens()) {
                            // multiplicity value exist: J(t-D,2)=7.5
                            p = details.nextToken().trim();
                            z = p.indexOf('-');
                            if (z > -1) {
                                // optional element info exist, by default Hydrogen ('H') is used
                                c.setMultiplicity(getMultiplicityValue(p.substring(0, z)));
                                c.setElement(p.substring(z + 1));
                            }
                            else {
                                c.setMultiplicity(getMultiplicityValue(p));
                            }
                        }
                        if (details.hasMoreTokens()) {
                            // atomNo exist:
                            p = details.nextToken().trim();
                            c.setPartner(Integer.parseInt(p));
                        }
                    }
                    pk.addCoupling(c);
                    //System.out.println("Coupling: " + c);
                }

                // define intensity if necessary
                if (pk.getIntensity() == 0) {
                    if (is13Cspectrum) {
                        // for 13C: try to estimate intensity
                        pk.setIntensity(pk.INTENSITIES[pk.getCouplings()[0].getMultiplicity()]);
                    }
                    else {
                        // for 1H: set intensity equal to integral
                        pk.setIntensity(pk.getIntegral());
                    }
                }
                l.add(pk);
                //System.out.println("Peak: " + pk);
            }
        }
        if (l.size() > 0) {
            r = new NmrPeak[l.size()];
            l.toArray(r);
        }
        return r;
    }


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //Brom-ethan
        String s = "S(3.0,3,1)=1.7;J(t,2)=7.5|S(2.0,2,2)=3.4;J(q-D,1)=7.5";
        parseTheSpectrum(s, false);
        String h = "C(CDCL3)";
        h = "C(CD3COCD3)";
        String data = CommonUtils.getProperty(h);
        System.out.println(h + " ==> " + data);
        parseTheSpectrum(data, true);
    }
}
