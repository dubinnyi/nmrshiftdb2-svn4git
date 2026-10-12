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
 * Created on 15. November 2004
 */
package org.openscience.nmrshiftdb.spectrumapplet.renderer.nmr;

import org.openscience.nmrshiftdb.spectrumapplet.data.*;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.*;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.action.*;

/**
 * Provides a spectrum with navigation functionality:
 *   - the zoomed range is highlighted
 *   - dragging with left mouse button translates zoom range
 *   - the minimum hight is limited to 100 points
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public class NmrRawDataNavigationSpectrum extends NmrNavigationSpectrum {

    /**
     * Creates a NavigationSpectrum
     * @param model the corresponding model
     * @param l list of peaks
     */
    public NmrRawDataNavigationSpectrum(SpectrumModel model, AbstractDataContainer l) {
        super(model, l);
        init(l);
    }

    /**
     * Init spectrum
     */
    protected void init(AbstractDataContainer data) {
        double min = data.getXmin();
        double max = data.getXmax();
        if (data.is13Cspectrum()) {
            if (min < 5) {
                min = min - 10;
            }
            else {
                min = 0;
            }
            if (max > 250) {
                max += 10;
            }
            else {
                max = 250;
            }
        }
        else {
            if (min < 0.5) {
                min = min - 1;
            }
            else {
                min = 0;
            }
            if (max > 15) {
                max += 1;
            }
            else {
                max = 15;
            }
        }
        metrics.calcBorderValues();
        metrics.setLimitsX(min, max);
        metrics.setInitialLimitsX(min, max);
        setMouseActionManager(new NavSpectrumMouseActions(this));
    }


    /**
      * Executes a recalculation of display data
      */
    public void doRecalculation() {
        if (forceRecalculation) {
            forceRecalculation = false;
            // take care to update update marker values
            updateMarker();
            setIgnoreZoomOps(false);
            calculatePoints();
        }
    }


    /**
    * Calculates all points displayed in spectrum
    */
    protected void calculatePoints() {
        Object[] data = ((XYData) getDataContainer()).getValuesBetween(metrics.getXmin(), metrics.getXmax(),
                                                                       metrics, 1);
        getCurvePainter().updateData(data);
    }
}
