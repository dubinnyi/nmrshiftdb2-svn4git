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
import org.openscience.nmrshiftdb.spectrumapplet.util.*;

import java.util.*;

/**
 * Contains an ordered list of peaks in a NMR-spectrum,
 * handles peaks which can represent multiple (isochron) atoms
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public class NmrData extends AbstractDataContainer {
    
    private NmrPeak[] pList;
    private NmrPeak[] pListOff;
    private NmrPeak[] solventPeaks;
    private ArrayList solventMultipletts;
    private Prediction[] predictions;
    private Map mapAtomToPeak;
    private boolean roofEffectEnabled;

    /**
     * Return a clone of this NmrData,
     * only start parameters are cloned,
     * calculations must be done again if needed
     * @param includePredictions if TRUE the predictions are also cloned
     * @return are clone of the NmrData
     */
    public  AbstractDataContainer clone(boolean includePredictions) {
        NmrPeak[] p = new NmrPeak[pList.length];
        for (int i = 0; i < pList.length; i++) {
            p[i] = (NmrPeak) pList[i].clone();
        }
        NmrData r = new NmrData(p, getSpectrumProperty(SPEC_TYPE), getNumberProperty(FREQ), getSpectrumProperty(SOLVENT));
        if (includePredictions) {
            Prediction[] pd = new Prediction[predictions.length];
            for (int i = 0; i < predictions.length; i++) {
                pd[i] = (Prediction) predictions[i].clone();
            }
            r.setPredictions(pd);
        }
        r.setSpectrumProperties(this.cloneProperties());
        return r;
    }

    /**
     * Creates a new instance of NmrData
     * @param spectrumTyp the spectrum typ
     * @param specFrequency the spectrometer frequency in MHz
     * @param peaks array of NmrPeaks
     * @param solvent the solvent
     */
    public NmrData(NmrPeak[] peaks, String spectrumTyp, double specFrequency, String solvent) {
        this(peaks, spectrumTyp, solvent);
        setSpectrometerFrequency(specFrequency);
    }


    /**
     * Creates a new instance of PeakList
     * @param spectrumTyp the spectrum typ
     * @param peaks array of NmrPeaks
     * @param solvent the solvent
     */
    private NmrData(NmrPeak[] peaks, String spectrumTyp, String solvent) {
        setSpectrumProperty(SPEC_TYPE, spectrumTyp);
        int z            = peaks.length;
        pList            = new NmrPeak[z];
        System.arraycopy(peaks, 0, pList, 0, peaks.length);
        Arrays.sort(pList);
        solventMultipletts = new ArrayList();
        if (solvent != null && solvent.length() > 0) {
            setSpectrumProperty(SOLVENT, solvent.toUpperCase());
            solventPeaks = getSolventPeaks();
        }

        // fill atom-to-peak map
        mapAtomToPeak = new HashMap();
        int no = 0;
        for (int i = 0; i < pList.length; i++) {
            no = pList[i].getAtomNumber();
            mapAtomToPeak.put(new Integer(no), pList[i]);
        }
        normalizeIntensities();
    }

    private NmrPeak[] getSolventPeaks() {
        NmrPeak[] r           = null;
        String h              = "H(";
        if (is13Cspectrum()) {
            h             = "C(";
        }
        h += getSpectrumProperty(SOLVENT) + ")";
        String data = CommonUtils.getProperty(h);
        if(data==null)
          return null;
        r = ParserUtil.parseTheSpectrum(data, is13Cspectrum()); 
        if (r != null && r.length > 0) {
            for (int i = 0; i < r.length; i++) {
                r[i].setAtomNumber(SpectrumUtil.getInstance().getNextSolventAtomNumber());
                r[i].setSolventPeak(true);
            }
        }
        return r;
    }


    /**
     * Set option for roof effect calculation in a 1H spectrum,
     * if TRUE the multipletts are calculated using a simple roof effect algorithm
     * @param isEnabled if TRUE roof effect is used
     */
    public void setRoofEffectEnabled(boolean isEnabled) {
        if (roofEffectEnabled != isEnabled) {
            roofEffectEnabled = isEnabled;
            forceMultiplettRecalculation();
        }
    }


    private void forceMultiplettRecalculation() {
        if (pListOff != null) {
            pListOff = null;
        }
    }








    /**
     * Set the spectrometer frequency (in MHz)
     * @param specFrequency the spectrometer frequency
     */
    public void setSpectrometerFrequency(double specFrequency) {
        setNumberProperty(FREQ, specFrequency);
        Coupling[] c = null;
        for (int i = 0; i < pList.length; i++) {
            c = pList[i].getCouplings();
            if (c != null) {
                for (int ii = 0; ii < c.length; ii++) {
                    c[ii].setSpectrometerFrequency(specFrequency);
                }
            }
        }
        if (solventPeaks != null) {
            for (int i = 0; i < solventPeaks.length; i++) {
                c = solventPeaks[i].getCouplings();
                if (c != null) {
                    for (int ii = 0; ii < c.length; ii++) {
                        c[ii].setSpectrometerFrequency(specFrequency);
                    }
                }
            }
            calcSolventMultipletts();
        }
        forceMultiplettRecalculation();
    }


    private void calcSolventMultipletts() {
        solventMultipletts.clear();
        NmrPeak[] p = null;
        for (int i = 0; i < solventPeaks.length; i++) {
            p = solventPeaks[i].getMultiplett(this, false);
            if (p != null) {
                for (int ii = 0; ii < p.length; ii++) {
                    p[ii].setSolventPeak(true);
                    solventMultipletts.add(p[ii]);
                }
            }
        }
    }


    /**
     * Set predictions for this peak list
     * predictions will be mapped to peaks in method getReducedList() !!
     * @param p array of <CODE>Prediction</CODE>, unsorted
     */
    public void setPredictions(Prediction[] p) {
        predictions = p;
        mapPredictions();
    }


    /**
     * Map predictions to peaks, triggered by method setPredictions()
     *  - if <CODE>Prediction</CODE> object has a atomNo the atomNo is used for mapping
     *  - otherwise the chemical shift is used
     */
    private void mapPredictions() {
        if (predictions != null) {
            Prediction p = null;
            NmrPeak peak = null;
            for (int i = 0; i < predictions.length; i++) {
                p    = predictions[i];
                peak = null;
                if (p.getAtomNo() != -1) {
                    peak = getPeakForAtomnumber(p.getAtomNo());
                    peak.addPrediction(p);
                }
                else {
                    for (int j = 0; j < pList.length; j++) {
                        if (pList[j].getValueX()  == p.getChemicalShift()) {
                            peak = pList[j];
                            peak.addPrediction(p);
                        }
                    }
                }
                if (peak == null) {
                    System.out.println("No mathing peak found for prediction:   shift= " +
                                       p.getChemicalShift() + "ppm  atomNo= " + p.getAtomNo());
                }
            }

            // add atomNo to prediction (with no atomNo) for peaks with only one prediction:
            Prediction[] pp = null;
            for (int i = 0; i < pList.length; i++) {
                pp = pList[i].getPredictions();
                //System.out.println("Prediction for: " + pList[i]);
                if ((pp != null) && (pp.length == 1)) {
                    if ((pp[0].getAtomNo() == -1) && (pList[i].getAtomNumber() >= 0)) {
                        pp[0].setAtomNo(pList[i].getAtomNumber());
                    }

                    // System.out.println("atomNo set: " + pp[0].getInfo());
                }
            }
        }
    }


    /**
     * Return the peak for the specified atom number
     * @param atomNo the atom number
     * @return the peak for the specified atom number
     */
    public NmrPeak getPeakForAtomnumber(int atomNo) {
        return (NmrPeak) mapAtomToPeak.get(new Integer(atomNo));
    }


    /**
     * Return the peaks for the specified atom number,
     * used if multiple peaks represent one atom,
     * e.g. if couplings are displayed
     * @param atomNo the atom number
     * @return array of peaks for the specified atom number
     */
    public NmrPeak[] getMultiPeaksForAtomnumber(int atomNo) {
        // mapAtomToPeak is already filled for Off-Resonance display
        return (NmrPeak[]) mapAtomToPeak.get("Off_" + atomNo);
    }


    /**
     * Return the minimal x-value e.g. minimal chemical shift in nmr spectrum
     * @return the minimal x-value
     */
    public double getXmin() {
        return pList[0].getValueX();
    }


    /**
     * Return the maximal x-value e.g. maximal chemical shift in nmr spectrum
     * @return the maximal x-value
     */
    public double getXmax() {
        return pList[pList.length - 1].getValueX();
    }


    /**
     * Return the maximal intensity in this PeakList
     * @param signed if TRUE, the peak orientation is used
     * @return the maximal intensity
     */
    public double getMaxIntensity(boolean signed) {
        double z = 0;
        for (int i = 0; i < pList.length; i++) {
            if (signed) {
                z = Math.max(pList[i].getSignedIntensity(), z);
            }
            else {
                z = Math.max(pList[i].getIntensity(), z);
            }
        }
        return z;
    }


    /**
     * Return the minimal intensity in this PeakList
     * @param signed if TRUE, the peak orientation is used
     * @return the minimal intensity
     */
    public double getMinIntensity(boolean signed) {
        double z = 0;
        for (int i = 0; i < pList.length; i++) {
            if (signed) {
                z = Math.min(pList[i].getSignedIntensity(), z);
            }
            else {
                z = Math.min(pList[i].getIntensity(), z);
            }
        }
        return z;
    }


    /**
     * Return all peaks defined in this PeakList
     * @param withSolventPeaks if TRUE the solvent peaks are added
     * @return an array of peaks
     */
    public NmrPeak[] getAllPeaks(boolean withSolventPeaks) {
        NmrPeak[] r = pList;
        if (withSolventPeaks) {
            r = new NmrPeak[pList.length + solventPeaks.length];
            System.arraycopy(pList, 0, r, 0, pList.length);
            System.arraycopy(solventPeaks, 0, r, pList.length, solventPeaks.length);
        }
        return r;
    }


    /**
     * Return an array of peaks with a chemical shift >= minShift and <= maxShift
     * @return the peaks
     * @param offResonance if TRUE the multiplicty of a peak is used, multiple peak for a peak in spectrum are generated
     * @param solventPeaks if TRUE solvent peaks are used
     * @param minShift the smallest chemical shift allowed
     * @param maxShift the greatest chemical shift allowed
     */
    public NmrPeak[] getPeaksBetween(double minShift, double maxShift, boolean offResonance,
                                     boolean solventPeaks) {
        ArrayList p   = null;
        NmrPeak[] tmp = null;
        double shift  = 0;
        if (offResonance) {
            if (pListOff == null) {
                genOffResonancePeaks();
            }
            tmp = pListOff;
        }
        else {
            tmp = pList;
        }
        p = new ArrayList(tmp.length + solventMultipletts.size());
        // search 'normal' peaks
        for (int i = 0; i < tmp.length; i++) {
            shift = tmp[i].getValueX();
            if ((shift >= minShift) && (shift < maxShift)) {
                p.add(tmp[i]);
            }
        }
        if (solventPeaks) {
            // search solvent peaks
            NmrPeak pk = null;
            for (int i = 0; i < solventMultipletts.size(); i++) {
                pk    = (NmrPeak) solventMultipletts.get(i);
                shift = pk.getValueX();
                if ((shift >= minShift) && (shift < maxShift)) {
                    p.add(pk);
                }
            }
        }
        NmrPeak[] r = new NmrPeak[p.size()];
        r = (NmrPeak[]) p.toArray(r);
        Arrays.sort(r);
        return r;
    }


    /**
     * Calculates peak multipletts
     */
    private void genOffResonancePeaks() {
        ArrayList pp    = new ArrayList();
        NmrPeak[] multi = null;
        for (int i = 0; i < pList.length; i++) {
            multi = pList[i].getMultiplett(this, roofEffectEnabled);
            mapOffResonancePeaks(multi, pList[i].getAtomNumber());
            for (int ii = 0; ii < multi.length; ii++) {
                pp.add(multi[ii]);
            }
        }
        pListOff = new NmrPeak[pp.size()];
        pp.toArray(pListOff);
        Arrays.sort(pListOff);
    }


    private void mapOffResonancePeaks(NmrPeak[] p, int atomNo) {
        // manage multiatom peaks
        mapAtomToPeak.put("Off_" + atomNo, p);
    }


    /**
     * Normalize the intensities
     */
    public void normalizeIntensities() {
        double max    = getMaxIntensity(false);
        double f      = 1;
        double intens = 0;
        if (max > 0.0001) {
            if (is13Cspectrum()) {
                f = 80 / max;
            }
            else {
                f = 1; // do nothing
            }
            for (int i = 0; i < pList.length; i++) {
                intens = pList[i].getIntensity();
                if (intens > 0.0001) {
                    pList[i].setIntensity(intens * f);
                }
            }
        }
    }


    /**
     * Provide the string representation of the PeakList
     * @return the string representation
     */
    public String toString() {
        StringBuffer s = new StringBuffer();
        String h       = "";
        for (int i = 0; i < pList.length; i++) {
            s.append(h).append(pList[i].toString());
            h = " / ";
        }
        return s.toString();
    }
}
