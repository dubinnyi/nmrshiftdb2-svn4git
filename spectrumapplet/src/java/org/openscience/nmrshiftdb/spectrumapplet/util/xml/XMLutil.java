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

import com.thoughtworks.xstream.*;

import org.openscience.nmrshiftdb.spectrumapplet.data.nmr.*;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.*;
import org.openscience.nmrshiftdb.spectrumapplet.util.*;

import java.io.*;

/**
 * Provides methods to create XML output and read XML input
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public abstract class XMLutil {
    private static XStream xstream;
    private static String tempDir;

    public static void writeToFile(NmrData p, SpectrumModel m, String path) {
        init();
        String dest = path;
        if (path == null) {
            dest = tempDir + "data.xml";
        }
        FileUtil.deleteRec(new File(dest));
        Spectrum sp = new Spectrum(p, m);
        String xml  = xstream.toXML(sp);
        FileUtil.writeToFile(dest, xml);
    }


    public static Spectrum readFromFile(String path) {
        init();
        Spectrum s = null;
        try {
            s = (Spectrum) xstream.fromXML(new FileReader(new File(path)));
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return s;
    }


    private static void init() {
        tempDir = System.getProperty("java.io.tmpdir");
        System.out.println("xxxx");
        xstream = new XStream();
        System.out.println("yyyy");
        xstream.alias("spectrum", Spectrum.class);
        xstream.alias("peak", XPeak.class);
        xstream.alias("coupling", XCoupling.class);
    }
}
