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
 * Created on 30. November 2004
 */
package org.openscience.nmrshiftdb.spectrumapplet.renderer.nmr;

import org.openscience.nmrshiftdb.spectrumapplet.data.nmr.*;
import org.openscience.nmrshiftdb.spectrumapplet.util.*;

/**
 * Class to calc the spectrum values using realistic peak display;
 * the peaks are simulated using the Lorentz equation
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public class NmrIntensityCalculator {
    private double hw;
    private double[] center;
    private double[] max;
    private int z;

    /** Creates a new instance of IntensityCalculator
     * @param p array of peaks used for calculation
     * @param isSpinEcho if TRUE J-modulation is used
     * @param lineWidth the peak line width used
     */
    public NmrIntensityCalculator(NmrPeak[] p, boolean isSpinEcho, double lineWidth) {
        z      = p.length;
        hw     = lineWidth / 2;
        center = new double[z];
        max    = new double[p.length];
        for (int i = 0; i < z; i++) {
            center[i] = p[i].getValueX();
            if (isSpinEcho) {
                max[i] = p[i].getSignedIntensity();
            }
            else {
                max[i] = p[i].getIntensity();
            }
        }
    }

    /**
     * Return the curve value for the specified chemical shift
     * @param x the chemical shift
     * @return the curve value
     */
    public double calcOverallIntensity(double x) {
        double r = 0;
        for (int i = 0; i < z; i++) {
            r += MathUtil.lorentz(x, max[i], center[i], hw);
        }
        r = MathUtil.round(r, 6);
        return r;
    }
}
