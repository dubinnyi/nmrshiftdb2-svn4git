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
package org.openscience.nmrshiftdb.spectrumapplet.data.nmr;

import org.openscience.nmrshiftdb.spectrumapplet.util.*;

import java.util.*;

/**
 * Class capsulates all chemical shifts used for prediction
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public class Prediction {
    private double chemicalShift      = 123456789;
    private int atomNo                = -1;
    private double[] predictionShifts;
    private int[] shiftCount          = null;
    private int maxShiftCount         = 0;
    private String info;
    private String id;

    private Prediction(double chemicalShift, int atomNo, double[] predictionShifts, int[] shiftCount,
                       int maxShiftCount, String info, String id) {
        this.chemicalShift    = chemicalShift;
        this.atomNo           = atomNo;
        this.predictionShifts = predictionShifts;
        this.shiftCount       = shiftCount;
        this.maxShiftCount    = maxShiftCount;
        this.info             = info;
        this.id               = id;
    }

    /**
     * Return a clone of this prediction
     * @return a clone of this prediction
     */
    public Object clone() {
        return new Prediction(chemicalShift, atomNo, predictionShifts, shiftCount, maxShiftCount, info, id);
    }

    /** Creates a new instance of Prediction
     *
     */
    public Prediction() {
        id = CommonUtils.getNewGUID();
    }

    /**
     * Return prediction id
     * @return the unique ID
     */
    public String getID() {
        return id;
    }


    /**
     * Set the chemical shift (the predicted one)
     * @param shift the chemical shift
     */
    public void setChemicalShift(double shift) {
        chemicalShift = shift;
        info          = "Prediction Histogram for Peak: " + shift + "ppm";
        if (atomNo != -1) {
            info += "  AtomNo: " + atomNo;
        }
    }


    /**
     * Return info string about this prediction
     * @return the info
     */
    public String getInfo() {
        return info;
    }


    /**
     * Return the chemical shift (the predicted one)
     * @return the chemical shift
     */
    public double getChemicalShift() {
        return chemicalShift;
    }


    /**
     * Set the chemical shifts used for prediction, array will be sorted
     * @param base array of chemical shift
     */
    public void setShiftsUsedForPrediction(double[] base) {
        predictionShifts = base;
        Arrays.sort(predictionShifts);
        calcDistribution();
    }


    private void calcDistribution() {
        double[] d = new double[predictionShifts.length];
        int[] c    = new int[predictionShifts.length];
        int z      = 0;
        d[0]       = predictionShifts[0];
        c[0]       = 1;
        for (int i = 1; i < predictionShifts.length; i++) {
            if (d[z] == predictionShifts[i]) {
                c[z]++;
                if (c[z] > maxShiftCount) {
                    maxShiftCount = c[z];
                }
            }
            else {
                z++;
                d[z] = predictionShifts[i];
                c[z] = 1;
            }
        }
        predictionShifts = new double[z + 1];
        shiftCount       = new int[z + 1];
        System.arraycopy(d, 0, predictionShifts, 0, z + 1);
        System.arraycopy(c, 0, shiftCount, 0, z + 1);
    }


    /**
     * Return the maximum shift count
     * @return the maximum shift count
     */
    public int getMaximumCount() {
        return maxShiftCount;
    }


    /**
     * Return specified shift
     * @param pos position in array
     * @return the shift
     */
    public double getShiftAt(int pos) {
        return predictionShifts[pos];
    }


    /**
     * Return number of shift
     * @param pos the position in array
     * @return shift count
     */
    public int getShiftCountAt(int pos) {
        return shiftCount[pos];
    }


    /**
     * Return the maximum shift
     * @return the maximum shift
     */
    public double getMaximumShift() {
        return predictionShifts[predictionShifts.length - 1];
    }


    /**
     * Return the minimum shift
     * @return the minimum shift
     */
    public double getMinimumShift() {
        return predictionShifts[0];
    }


    /**
     * Return number of different shift values
     * @return number of different shift values
     */
    public int getShiftCount() {
        return predictionShifts.length;
    }


    /**
     * String representation for this prediction
     * @return string representation for this prediction
     */
    public String toString() {
        StringBuffer b = new StringBuffer();
        b.append("shift= ").append(chemicalShift).append(" => [ ");
        for (int i = 0; i < predictionShifts.length; i++) {
            b.append(predictionShifts[i]);
            if (shiftCount != null) {
                b.append("-").append(shiftCount[i]);
            }
            if (i < predictionShifts.length - 1) {
                b.append("  ");
            }
        }
        b.append(" ]").append(" maxCount=").append(maxShiftCount);
        return b.toString();
    }


    /**
     * Getter for property atomNo.
     * @return Value of property atomNo.
     */
    public int getAtomNo() {
        return atomNo;
    }


    /**
     * Setter for property atomNo.
     * @param atomNo New value of property atomNo.
     */
    public void setAtomNo(int atomNo) {
        this.atomNo = atomNo;
        info        = "Prediction Histogram for Peak: ";
        if (chemicalShift != 123456789) {
            info += chemicalShift + "ppm ";
        }
        info += " AtomNo: " + atomNo;
    }
}
