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

import org.openscience.nmrshiftdb.spectrumapplet.data.*;
import org.openscience.nmrshiftdb.spectrumapplet.util.*;
import org.openscience.nmrshiftdb.spectrumapplet.util.jcamp.*;
import tests.org.openscience.nmrshiftdb.spectrumapplet.renderer.RendererTest;

/**
 * Test for XMLutil.java
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public class TestJCamp {
    public static void main(String[] args) {
        try {
            String p = "tests/testdata/jcamp/Ethanal.jdx";

            //p="tests/testdata/jcamp/4-methyl pentanal.jdx";
            p = "tests/testdata/jcamp/fixdec2.jdx";
            p = "tests/testdata/jcamp/ir1.jdx";
            String s = CommonUtils.getFileContent(p);

            //System.out.println(s);

            /*
            AbstractDataContainer rd =JCampUtil.parseJcampData(s);
            RendererTest rt1 = new RendererTest(rd);
            rt1.getFrame().setTitle(p);
            rt1.getFrame().setLocation(380, 10);
             */
            p = "tests/testdata/jcamp/ir1.jdx";
            p = "tests/testdata/jcamp/ir2.jdx";
            p = "tests/testdata/jcamp/coffeineIR.jdx";
            s = CommonUtils.getFileContent(p);
            AbstractDataContainer rd2 = JCampUtil.parseJcampData(s);
            RendererTest rt2 = new RendererTest(rd2);
            rt2.getFrame().setTitle(p + ",  IR Spectrum");
            rt2.getFrame().setLocation(380, 410);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
