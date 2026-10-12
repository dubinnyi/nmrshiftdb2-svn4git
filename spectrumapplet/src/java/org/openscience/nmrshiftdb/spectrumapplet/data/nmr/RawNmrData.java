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
 * Created on 10. Februar 2005
 */
package org.openscience.nmrshiftdb.spectrumapplet.data.nmr;

import org.openscience.nmrshiftdb.spectrumapplet.data.*;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.*;

import java.util.*;

/**
 *
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public class RawNmrData extends XYData {
    private NmrPeak[] peaks;

    public void addPeaks(ArrayList peakList, double factorX) {
        peaks = null;
        if ((peakList != null) && (peakList.size() > 0)) {
            peaks = new NmrPeak[peakList.size()];
            for (int i = 0; i < peakList.size(); i++) {
                peaks[i] = (NmrPeak) peakList.get(i);
                peaks[i].setChemicalShift(peaks[i].getValueX() * factorX);
            }
            Arrays.sort(peaks);
        }
    }


    public AbstractDataContainer clone(boolean option) {
        RawNmrData r = new RawNmrData();
        cloneData(r);
        return r;
    }
}
