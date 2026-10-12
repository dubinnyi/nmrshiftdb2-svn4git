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
public class XCoupling {
    private double value;
    private int multiplicity;
    private int atomNumber        = -1;
    private String partnerElement = "H";

    /** Creates a new instance of Coupling
     *
     */
    public XCoupling(double j, int atomNumber, String element, int multi) {
        multiplicity    = multi;
        this.atomNumber = atomNumber;
        value           = j;
        if (element != null && element.length() > 0) {
            partnerElement = element;
        }
    }


    public XCoupling(Coupling c) {
        multiplicity   = c.getMultiplicity();
        atomNumber     = c.getPartner();
        value          = c.getCouplingConstantInHz();
        partnerElement = c.getElement();
    }

    public Coupling getCoupling() {
        Coupling c = new Coupling(value, multiplicity);
        if (atomNumber > -1) {
            c.setPartner(atomNumber);
        }
        c.setElement(partnerElement);
        return c;
    }
}
