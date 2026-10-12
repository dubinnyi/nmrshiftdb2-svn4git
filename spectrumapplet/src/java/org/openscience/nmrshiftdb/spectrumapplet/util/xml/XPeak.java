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

/**
 * Wrapper class to create XML output and input
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public class XPeak {
    private int atomNumber;
    private double chemicalShift;
    private double integral;
    private double intensity;
    private XCoupling[] couplingList;

    /** Creates a new instance of Peak
     *
     */
    public XPeak(int atomNumber, double chemShift, double integral, double intens, XCoupling[] c) {
        couplingList    = c;
        intensity       = intens;
        this.integral   = integral;
        chemicalShift   = chemShift;
        this.atomNumber = atomNumber;
    }


    public XPeak(NmrPeak p) {
        Coupling[] c = p.getCouplings();
        couplingList = new XCoupling[c.length];
        for (int i = 0; i < c.length; i++) {
            couplingList[i] = new XCoupling(c[i]);
        }
        intensity     = p.getIntensity();
        integral      = p.getIntegral();
        chemicalShift = p.getValueX();
        atomNumber    = p.getAtomNumber();
    }

    public NmrPeak getPeak() {
        NmrPeak r = new NmrPeak(chemicalShift, intensity, atomNumber);
        r.setIntegral(integral);
        for (int i = 0; i < couplingList.length; i++) {
            r.addCoupling(couplingList[i].getCoupling());
        }
        return r;
    }
}
