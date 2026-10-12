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
package org.openscience.nmrshiftdb.spectrumapplet.util.xml;

import org.openscience.nmrshiftdb.spectrumapplet.data.nmr.*;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.*;

/**
 * Wrapper class to create XML output and input
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public class Spectrum {
    private String specType;
    private double frequency;
    private String solvent;
    private SpectrumModel options;
    private XPeak[] peakList;

    /** Creates a new instance of Spectrum
     *
     */
    public Spectrum(String specType, double freq, String solvent, XPeak[] p, SpectrumModel m) {
        peakList      = p;
        this.specType = specType;
        frequency     = freq;
        this.solvent  = solvent;
        options       = m;
    }


    public Spectrum(NmrData p, SpectrumModel m) {
        NmrPeak[] pp = p.getAllPeaks(false);
        peakList = new XPeak[pp.length];
        for (int i = 0; i < pp.length; i++) {
            peakList[i] = new XPeak(pp[i]);
        }
        specType  = p.getSpectrumProperty(p.SPEC_TYPE);
        frequency = p.getNumberProperty(p.FREQ);
        solvent   = p.getSpectrumProperty(p.SOLVENT);
        options   = m;
    }

    public NmrData getPeakList() {
        NmrPeak[] p = new NmrPeak[peakList.length];
        for (int i = 0; i < peakList.length; i++) {
            p[i] = peakList[i].getPeak();
        }
        NmrData r = new NmrData(p, specType, frequency, solvent);
        return r;
    }


    public SpectrumModel getModel() {
        return options;
    }
}
