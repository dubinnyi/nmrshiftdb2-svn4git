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
package org.openscience.nmrshiftdb.spectrumapplet.renderer.ir;

import org.openscience.nmrshiftdb.spectrumapplet.data.*;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.*;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.action.*;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.nmr.*;

/**
 * Provides an IR spectrum with navigation functionality:
 *   - the zoomed range is highlighted
 *   - dragging with left mouse button translates zoom range
 *   - the minimum hight is limited to 100 points
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public class IrRawDataNavigationSpectrum extends NmrRawDataNavigationSpectrum {

    /**
     * Creates a NavigationSpectrum
     * @param model the corresponding model
     * @param l list of peaks
     */
    public IrRawDataNavigationSpectrum(SpectrumModel model, AbstractDataContainer l) {
        super(model, l);
        init(l);
    }

    /**
     * Init spectrum
     */
    protected void init(AbstractDataContainer data) {
        double min = data.getXmin();
        double max = data.getXmax();
        metrics.calcBorderValues();
        metrics.setLimitsX(min, max);
        metrics.setInitialLimitsX(min, max);
        setMouseActionManager(new NavSpectrumMouseActions(this));
    }
}
