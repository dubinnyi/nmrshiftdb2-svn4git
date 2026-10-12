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
 * Created on 24. Januar 2005
 */
package tests;

import org.openscience.nmrshiftdb.spectrumapplet.data.nmr.*;
import tests.org.openscience.nmrshiftdb.spectrumapplet.renderer.RendererTest;

/**
 * Test for 1H nmr spectra
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public class Test_1H_Spectra {

    /** Creates a new instance of Test_1H_Spectra
     *
     */
    public Test_1H_Spectra() {
    }

    public static void main(String[] args) {
        // new RendererTest("197.74d;0.0;5|143.55;0.0;0|27.36;0.0;4|18.26;0.0;2|133.32;0.0;1|197.74t;0.0;3|",
        //                  NmrData.SPECTRUM_13C);
        //Butadiene
        String butadiene = "S(1,1,1)=5.06;J(d,2)=1.8;J(d,3)=10.2;J(d,4)=-0.9;J(d,6)=1.3;J(d,5)=0.6|" +
                           "S(1,1,6)=5.06;J(d,5)=1.8;J(d,4)=10.2;J(d,3)=-0.9;J(d,1)=1.3;J(d,2)=0.6|" +
                           "S(1,1,2)=5.16;J(d,1)=1.8;J(d,3)=17.1;J(d,4)=-0.8;J(d,6)=0.6;J(d,5)=0.6|" +
                           "S(1,1,5)=5.16;J(d,6)=1.8;J(d,4)=17.1;J(d,3)=-0.8;J(d,1)=0.6;J(d,2)=0.6|" +
                           "S(1,1,3)=6.27;J(d,1)=10.2;J(d,2)=17.1;J(d,4)=10.4;J(d,6)=-0.9;J(d,5)=-0.8|" +
                           "S(1,1,4)=6.27;J(d,6)=10.2;J(d,5)=17.1;J(d,3)=10.4;J(d,1)=-0.9;J(d,2)=-0.8|";
        String butaPredi = "S(1,1,1)=4.96;J(d,2)=1.8;J(d,3)=10.2;J(d,4)=-0.9;J(d,6)=1.3;J(d,5)=0.6|" +
                           "S(1,1,6)=4.96;J(d,5)=1.8;J(d,4)=10.2;J(d,3)=-0.9;J(d,1)=1.3;J(d,2)=0.6|" +
                           "S(1,1,2)=5.11;J(d,1)=1.8;J(d,3)=17.1;J(d,4)=-0.8;J(d,6)=0.6;J(d,5)=0.6|" +
                           "S(1,1,5)=5.11;J(d,6)=1.8;J(d,4)=17.1;J(d,3)=-0.8;J(d,1)=0.6;J(d,2)=0.6|" +
                           "S(1,1,3)=6.37;J(d,1)=10.2;J(d,2)=17.1;J(d,4)=10.4;J(d,6)=-0.9;J(d,5)=-0.8|" +
                           "S(1,1,4)=6.37;J(d,6)=10.2;J(d,5)=17.1;J(d,3)=10.4;J(d,1)=-0.9;J(d,2)=-0.8|";
        RendererTest rt0 = new RendererTest(butadiene, "", butaPredi, NmrData.SPECTRUM_1H, "", "100", false, false);
        rt0.getFrame().setTitle("Butadiene");
        rt0.setSize(350, 550);
        rt0.getFrame().setLocation(10, 10);
        //Brom-ethan
        //RendererTest rt =new RendererTest("1.7;3.0:3;1;0-t:7.5|3.4;2.0:2;0;1-q:7.5||5.7;3.0:3;3;2-t:7.5|6.1;2.0:2;2;3-q:7.5",
        //                                  NmrData.SPECTRUM_1H, 50);
        RendererTest rt1 = new RendererTest("S(2,2,1)=1.7;J(d,2)=7.5|S(2,2,2)=1.9;J(d,1)=7.5|" +
                                            "S(2,2,3)=2.7;J(d,4)=7.5|S(2,2,4)=2.8;J(d,3)=7.5|" +
                                            "S(2,2,5)=3.7;J(d,6)=7.5|S(2,2,6)=3.75;J(d,5)=7.5|", "", "",
                                            NmrData.SPECTRUM_1H, "", "50", false, false);
        rt1.setParameter(7, "true");
        rt1.getFrame().setTitle("Test ROOF-EFFECT 1");
        rt1.getFrame().setLocation(410, 10);
        RendererTest rt2 = new RendererTest("S(2,2,1)=1.7;J(d,2)=7.5|S(2,2,2)=1.9;J(t,1)=4.5|" +
                                            "S(2,2,3)=2.7;J(d,4)=7.5|S(2,2,4)=2.8;J(t,3)=4.5|" +
                                            "S(2,2,5)=3.7;J(d,6)=7.5|S(2,2,6)=3.75;J(t,5)=4.5|", "", "",
                                            NmrData.SPECTRUM_1H, "", "50", false,false);
        rt2.setParameter(7, "true");
        rt2.getFrame().setTitle("Test ROOF-EFFECT 2");
        rt2.getFrame().setLocation(410, 310);
        //Zimts�ure 
        // new RendererTest("S(1,1,1)=6.47;J(d,2)=16|S(1,1,2)=7.82;J(d,1)=16|S(1,1,3)=5.47;J(d,4)=16|"+
        //                    "S(1,1,4)=5.55;J(d,3)=16|S(1,1,5)=3.47;J(d,6)=16|S(1,1,6)=3.6;J(d,5)=16", PeakList.SPECTRUM_1H,60);
    }
}
