/**
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
 * Created on 28. Januar 2005
 */
package tests;

import org.openscience.nmrshiftdb.spectrumapplet.data.nmr.*;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.*;
import org.openscience.nmrshiftdb.spectrumapplet.util.*;
import org.openscience.nmrshiftdb.spectrumapplet.util.xml.*;
import tests.org.openscience.nmrshiftdb.spectrumapplet.renderer.RendererTest;

/**
 * Test for XMLutil.java
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public class TestXMLutil {
    public static void main(String[] args) {
        String s         = "S(1,1,1)=5.06;J(d,2)=1.8;J(d,3)=10.2;J(d,4)=-0.9;J(d,6)=1.3;J(d,5)=0.6|" +
                           "S(1,1,6)=5.06;J(d,5)=1.8;J(d,4)=10.2;J(d,3)=-0.9;J(d,1)=1.3;J(d,2)=0.6|" +
                           "S(1,1,2)=5.16;J(d,1)=1.8;J(d,3)=17.1;J(d,4)=-0.8;J(d,6)=0.6;J(d,5)=0.6|" +
                           "S(1,1,5)=5.16;J(d,6)=1.8;J(d,4)=17.1;J(d,3)=-0.8;J(d,1)=0.6;J(d,2)=0.6|" +
                           "S(1,1,3)=6.27;J(d,1)=10.2;J(d,2)=17.1;J(d,4)=10.4;J(d,6)=-0.9;J(d,5)=-0.8|" +
                           "S(1,1,4)=6.27;J(d,6)=10.2;J(d,5)=17.1;J(d,3)=10.4;J(d,1)=-0.9;J(d,2)=-0.8|";
        String type      = "1H";
        double freq      = 100;
        RendererTest rt0 = new RendererTest(s, "", "", NmrData.SPECTRUM_1H, "", "100", true,false);
        rt0.getFrame().setTitle("Butadiene (original)");
        rt0.getFrame().setLocation(10, 10);
        setParam(rt0);
        NmrPeak[] p = ParserUtil.parseSpectrum(s, type);
        NmrData pl  = new NmrData(p, type, freq, "CDCL3");
        String dest = System.getProperty("java.io.tmpdir") + "Butadiene.xml";
        System.out.println("Destination: " + dest);
        XMLutil.writeToFile(pl, new SpectrumModel(), dest);
        NmrData pl2      = XMLutil.readFromFile(dest).getPeakList();
        RendererTest rt1 = new RendererTest(pl2);
        rt1.getFrame().setTitle("Butadiene , load from: " + dest);
        rt1.getFrame().setLocation(380, 10);
        setParam(rt1);
        /*
        init();
        XCoupling[] c1 = new XCoupling[] { new XCoupling(2.5, 1, "F", 4), new XCoupling(13.5, 2, "H", 2) };
        XCoupling[] c2 = new XCoupling[] { new XCoupling(4.7, 2, "H", 4), new XCoupling(7.5, 1, "H", 3) };
        XPeak p1       = new XPeak(1, 15.6, 1, 60, c1);
        XPeak p2       = new XPeak(2, 55.7, 1, 40, null);
        XSpectrum sp   = new XSpectrum("13C", 100, "CDCL3", new XPeak[] { p1, p2 });
        String xml     = xstream.toXML(sp);
        System.out.println("Spectrum:\n\n" + xml);
        XSpectrum sp2 = (XSpectrum) xstream.fromXML(xml);
        String xml2   = xstream.toXML(sp2);
        System.out.println("\nIdentical: " + xml.equals(xml2));
         */
    }


    private static void setParam(RendererTest rt) {
        rt.setParameter(0, "true");
        rt.setParameter(2, "true");
        rt.setParameter(7, "true");
        rt.updateView();
    }
}
