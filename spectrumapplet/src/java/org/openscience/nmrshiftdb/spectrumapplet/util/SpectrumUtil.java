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
package org.openscience.nmrshiftdb.spectrumapplet.util;

import org.openscience.nmrshiftdb.spectrumapplet.data.nmr.*;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.*;

import java.util.*;

/**
 *
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public class SpectrumUtil {
    private int unknownNumber            = NmrPeak.NUMBER_UNKNOWN;
    private int solventNumber            = NmrPeak.SOLVENT;
    private static SpectrumUtil instance;

    private SpectrumUtil() {
    }

    public static SpectrumUtil getInstance() {
        if (instance == null) {
            instance = new SpectrumUtil();
        }
        return instance;
    }


    public int getNextUnknownAtomNumber() {
        unknownNumber--;
        return unknownNumber;
    }


    public int getNextSolventAtomNumber() {
        solventNumber++;
        return solventNumber;
    }


    /**
     * Search the peaks next to cursor position using a sensitivity range;
     * if more than one peak is found the peaks with smallest delta are used
     * @param xCursor the cursor x-position
     * @param sense the maximum distance of a peak to cursor poasition (sensitivity range)
     * @return array of Peaks or null if no peak was found
     */
    public static NmrPeak[] searchPeaksNextToCursor(NmrPeak[] visiblePeaks, int xCursor, int sense) {
        NmrPeak[] toReturn = null;
        ArrayList l        = new ArrayList(visiblePeaks.length);

        // search peaks within sense range
        for (int i = 0; i < visiblePeaks.length; i++) {
            if (Math.abs(xCursor - visiblePeaks[i].getX()) < sense) {
                l.add(visiblePeaks[i]);
            }
        }
        if (l.size() == 0) {
            toReturn = null;
        }
        else {
            if (l.size() > 1) {
                //search peaks with smallest delta
                int delta    = 999999;
                int px       = 0;
                NmrPeak p    = null;
                NmrPeak pp   = null;
                ArrayList ll = new ArrayList(l.size());
                for (int i = 0; i < l.size(); i++) {
                    pp = (NmrPeak) l.get(i);
                    ll.add(pp);
                    px = pp.getX();
                    if (Math.abs(xCursor - px) < delta) {
                        delta = Math.abs(xCursor - px);
                        p     = pp;
                    }
                }
                l.clear();
                l.add(p);
                // search isochrone peaks
                double sh = p.getValueX();
                for (int i = 0; i < ll.size(); i++) {
                    pp = (NmrPeak) ll.get(i);
                    if ((!p.equals(pp)) && (sh == pp.getValueX())) {
                        l.add(pp);
                    }
                }
            }
            toReturn = new NmrPeak[l.size()];
            l.toArray(toReturn);
        }
        return toReturn;
    }


    public static void updatePeakScreenCoordinates(NmrPeak[] p, Metrics metrics, boolean isSpinEcho) {
        for (int i = 0; i < p.length; i++) {
            p[i].setX(metrics.translateX_int(p[i].getValueX()));
            if (isSpinEcho) {
                p[i].setY(metrics.translateY_int(p[i].getSignedIntensity()));
            }
            else {
                p[i].setY(metrics.translateY_int(p[i].getIntensity()));
            }
        }
    }


    public static Object[] calcIntegralData(Metrics metrics, NmrPeak[] visiblePeaks) {
        int right = metrics.translateX_int(metrics.getXmin());
        int left  = metrics.translateX_int(metrics.getXmax());
        if (!metrics.isInversXaxis()) {
            right = metrics.translateX_int(metrics.getXmax());
            left  = metrics.translateX_int(metrics.getXmin());
        }
        int z      = 0;
        double sum = 0;
        for (int i = 0; i < visiblePeaks.length; i++) {
            // ignore solvent peaks for integral calculation
            if (!(visiblePeaks[i].getAtomNumber() > NmrPeak.SOLVENT)) {
                sum += visiblePeaks[i].getIntegral();
                z++;
            }
        }
        double offset = metrics.getYmax() / 15;
        double f      = (metrics.getYmax() - metrics.getYmax() / 15) / sum;
        int[] x       = new int[(z + 1) * 2];
        int[] y       = new int[(z + 1) * 2];
        x[0]          = left;
        y[0]          = metrics.translateY_int(offset);
        z             = 1;
        sum           = 0;
        if (metrics.isInversXaxis()) {
            for (int i = visiblePeaks.length - 1; i >= 0; i--) {
                // ignore solvent peaks for integral calculation
                if (!(visiblePeaks[i].getAtomNumber() > NmrPeak.SOLVENT)) {
                    x[z]     = visiblePeaks[i].getX();
                    y[z]     = y[z - 1];
                    x[z + 1] = x[z];
                    sum += visiblePeaks[i].getIntegral();
                    y[z + 1] = metrics.translateY_int(offset + sum * f);
                    z        = z + 2;
                }
            }
            x[z] = right;
            y[z] = y[z - 1];
        }
        else {
            // must be implemented if necessary 
            ;
        }
        Object[] data = new Object[] { x, y };
        return data;
    }
}
