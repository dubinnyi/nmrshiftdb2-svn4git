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
 * Test for 13C nmr spectra
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public class Test_13C_Spectra {

    /** Creates a new instance of Test_1H_Spectra
     *
     */
    public Test_13C_Spectra() {
    }

    /**
    * the main method
    * @param args args
    */
    public static void main(String[] args) {

        /*
        RendererTest rt0 = new RendererTest("136.55;0s;1|122.88;0d;2|17.64s;0\n;3|19.27;0\tt;4|12.31q;0;5|27.17q;0;6|27.51t;0;7|8.71;0;8q|",
                                            "", "",NmrData.SPECTRUM_13C, "CDCL3","100",true);
        rt0.getFrame().setTitle("OLD input format");
        rt0.getFrame().setLocation(10, 10);

        RendererTest rt1 = new RendererTest("120.93;0d;2|108.21d;0;3|108.21;0d;4|120.93d;0;5|44.78t;0;6|16.59q;0;7||",
                                            "",NmrData.SPECTRUM_13C, "CDCL3","100",true);
        rt1.getFrame().setTitle("OLD input format 2");
        rt1.getFrame().setLocation(10, 410);

        RendererTest rt1a = new RendererTest("S(60,1,2)=116.5;J(q-F)=283|S(20,1,1)=164.4;J(q-F)=44", "",
                                             "S(60,1,2)=136.5;J(q-F)=283|S(20,1,1)=164.4;J(q-F)=44",
                                             NmrData.SPECTRUM_13C, "CDCL3","100",true);
        rt1a.getFrame().setTitle("CF3COOD in CDCl3, with experimental");
        rt1a.setSize(350, 550);
        rt1a.getFrame().setLocation(310, 10);

        rt1a.getFrame().repaint();


        String pred       = "30.8;0.0;8q|30.8;0.0;9q|30.8;0.0;10q|31.3;0.0;12q|" +
                            "31.3;0.0;13q|31.3;0.0;14q|34.6;0.0;7s|35.3;0.0;11s|" +
                            "66.0;0.0;15q|124.3;0.0;0d|129.0;0.0;4s|130.7;0.0;2d|" +
                            "142.8;0.0;5s|146.2;0.0;1s|161.3;0.0;3s|190.8;0.0;6d|";
        String exp        = "30.7;0.0;8q|30.7;0.0;9q|30.7;0.0;10q|32.3;0.0;12q|" +
                            "32.3;0.0;13q|32.3;0.0;14q|34.1;0.0;7s|36.3;0.0;11s|" +
                            "61.0;0.0;15q|124.0;0.0;0d|127.0;0.0;4s|130.0;0.0;2d|" +
                            "143.8;0.0;5s|148.2;0.0;1s|164.3;0.0;3s|195.8;0.0;6d|";
        RendererTest rt1a = new RendererTest(pred, "", exp, NmrData.SPECTRUM_13C, "", "100", true);
        rt1a.setParameter(0, "true");
        rt1a.getFrame().setTitle("13C spectrum, with experimental");
        rt1a.setSize(350, 550);
        rt1a.getFrame().setLocation(310, 10);
        rt1a.getFrame().repaint();
        */
        RendererTest rt2 = new RendererTest("197.74t;0.0;8|197.74d;0.0;9|197.74s;0.0;5|143.55;0.0;0|107.74q;0.0;10|27.36s;0.0;4|18.26d;0.0;2|133.32t;0.0;1|197.74q;0.0;3|",
                                            "143.55|142.6999969482422;143.60000610351562;144.3699951171875;|197.74|197.0;" +
                                            "197.89999389648438;198.32000732421875;|27.36|26.600000381469727;26.899999618530273;" +
                                            "28.600000381469727;|18.26|18.0;18.200000762939453;18.600000381469727;|133.32|" +
                                            "133.10000610351562;133.3000030517578;133.57000732421875;|197.74|197.0;" +
                                            "197.89999389648438;198.32000732421875;198.33000732421875;198.34000732421875;|", "",
                                            NmrData.SPECTRUM_13C, "", "100", false,false);
        rt2.getFrame().setTitle("With Prediction");
        rt2.getFrame().setLocation(310, 410);
   /*     
        RendererTest rt3 = new RendererTest("S(60,1,2)=116.5;J(q-F)=283|S(20,1,1)=164.4;J(q-F)=44", "", "",
                                            NmrData.SPECTRUM_13C, "CDCL3", "100", false,false);
        rt3.getFrame().setTitle("CF3COOD in CDCl3");
        rt3.getFrame().setLocation(610, 10);
        RendererTest rt4 = new RendererTest("S(60,1)=116.5;J(q-F)=283|S(20,1)=164.4;J(q-F)=44", "", "",
                                            NmrData.SPECTRUM_13C, "CDCL3", "100", true,false);
        rt4.getFrame().setTitle("CF3COOD in CDCl3, no atom numbers !!");
        rt4.getFrame().setLocation(610, 410);
        RendererTest rt5 = new RendererTest("197.74;0.0;5|143.55;0.0;0|27.36;0.0;4|18.26;0.0;2|133.32;0.0;1|197.74;0.0;3|",
                                            "", "", NmrData.SPECTRUM_13C, "CF3COOD", "100", true,false);
        rt5.getFrame().setTitle("Isochrone peak, in CF3COOD");
        rt5.getFrame().setLocation(610, 410);
        new RendererTest("197.74d;0.0;5|143.55;0.0;0|27.36;0.0;4|18.26;0.0;2|133.32;0.0;1|197.74t;0.0;3|",
                         "", "", NmrData.SPECTRUM_13C, "CD3COCD3", "100", false,false);
        new RendererTest("S(0.0,0,6,d)=197.74|S(0.0,0,1)=143.55|S(0.0,0,5)=27.36|" +
                         "S(0.0,0,3)=18.26|S(0.0,0,2)=133.32|S(0.0,0,4,t)=197.74|", "", "",
                         NmrData.SPECTRUM_13C, "CDCL3", "100", false,false);
    */
    }
        
}
