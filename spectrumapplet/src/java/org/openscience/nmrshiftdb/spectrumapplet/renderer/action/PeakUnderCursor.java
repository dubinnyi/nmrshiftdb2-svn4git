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
 * Created on 27. Januar 2005
 */
package org.openscience.nmrshiftdb.spectrumapplet.renderer.action;

import java.util.*;
import org.openscience.nmrshiftdb.spectrumapplet.data.nmr.*;
import org.openscience.nmrshiftdb.spectrumapplet.util.*;



/**
 * This class handles all information about the peak/peaks found at cursor position
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public class PeakUnderCursor {
    private ArrayList predictions;
    private ArrayList atomNumbers;
    private NmrPeak found;
    private String solvent;
    private StringBuffer info;
    private boolean firstInfo = true;

    /** Creates a new instance of PeakUnderCursor
     *
     */
    public PeakUnderCursor(NmrPeak firstFound, String theSolvent) {
        solvent     = theSolvent;
        found       = firstFound;
        atomNumbers = new ArrayList();
        predictions = new ArrayList();
        info        = new StringBuffer();
        info.append(found.getValueX()).append("ppm; Peak= ");
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


    public int getFirstAtomNumber() {
        String s = (String) atomNumbers.get(0);
        int r    = Integer.parseInt(s);
        return r;
    }


    public int[] getAllAtomNumbers() {
        int[] r  = new int[atomNumbers.size()];
        String s = null;
        for (int i = 0; i < atomNumbers.size(); i++) {
            s    = (String) atomNumbers.get(i);
            r[i] = Integer.parseInt(s);
        }
        return r;
    }


    public void addAtomNumber(NmrPeak p) {
        int n    = p.getAtomNumber();
        String s = String.valueOf(n);
        if (!atomNumbers.contains(s)) {
            atomNumbers.add(s);
            // update info string
            if (!firstInfo) {
                info.append(",");
            }
            else {
                firstInfo = false;
            }
            if (p.isSolventPeak()) {
                info.append(CommonUtils.getProperty("L(" + solvent + ")"));
            }
            else {
                if (n >= 0) {
                    info.append(n);
                }
                else {
                    info.append("?");
                }
                info.append(p.multiplicityToString());
            }
        }
    }


    public void addPredictions(Prediction[] pred) {
        for (int ii = 0; ii < pred.length; ii++) {
            predictions.add(pred[ii]);
        }
    }


    public double getIntensity() {
        return found.getIntensity();
    }


    public double getChemicalShift() {
        return found.getValueX();
    }


    public int getX() {
        return found.getX();
    }


    public int getY() {
        return found.getY();
    }


    public String toString() {
        return info.toString();
    }
}
