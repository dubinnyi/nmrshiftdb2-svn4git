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
 * Created on 19. November 2004
 */
package org.openscience.nmrshiftdb.spectrumapplet;

import java.applet.*;

import netscape.javascript.JSObject;

import org.openscience.nmrshiftdb.spectrumapplet.data.nmr.*;
import org.openscience.nmrshiftdb.spectrumapplet.util.*;

/**
 * Applet displaying a NMR spectrum,
 * interaction with JChemPaint structure applet implemented 
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public class SpectrumView extends SpectrumViewNoRenderer {
    private StructureApplet structureApplet;
    int[] oldnumbers=null;
    
    /**
     * Gets the appletInfo
     * @return    The appletInfo value
     */
    public String getAppletInfo() {
        return ("Spectrum viewer applet by G.Schneider (with Marvin interaction)");
    }


    /**
     * Handles interaction with structure viewer,
     * highlighted atoms in spectrum view will be highlighted in structure
     * @param atomNumbers atom numbers of peaks highlighted in spectrum
     */
    public void highlightAtomsInStructur(int[] atomNumbers) {
        getStructureApplet();
        if (structureApplet != null) {
            structureApplet.highlightAtoms(atomNumbers);
        }
    }
   
    
    public void highlightRowsInTable(int[] atomNumbers){
        if(getParameter("highlightTable")==null || getParameter("highlightTable").equals("false"))
          return;
        if(atomNumbers==null)
        	return;
        JSObject win = JSObject.getWindow(this);
        for(int i=0;i<atomNumbers.length;i++){
        	JSObject tr = (JSObject) win.eval("document.getElementById(\"tableid"+(atomNumbers[i]-1)+"\").style");
        	if(tr!=null){
        		tr.setMember("backgroundColor","#FF6600");
        	}
        }
        if(oldnumbers!=null){
        	for(int i=0;i<oldnumbers.length;i++){
        		JSObject tr = (JSObject) win.eval("document.getElementById(\"tableid"+(oldnumbers[i]-1)+"\").style");
        		if((oldnumbers[i]+1)%2==0)
        			tr.setMember("backgroundColor","white");
        		else
        			tr.setMember("backgroundColor","#D3D3D3");
        	}
        }
        oldnumbers=atomNumbers;
    }

    private void getStructureApplet() {
        if (structureApplet == null) {
            String s = getParameter("structureRenderer");
            if ((s != null) && (s.length() > 0)) {
                Applet applet = getAppletContext().getApplet(s);
                try {
                    structureApplet = new JChemPaintWrapper(applet);
                }
                catch (ClassCastException e) {
                    System.out.println("\nATTENTION:\n   - take care that applets use the same archive param (import the same jar files);");
                    System.out.println("   -  otherwise the applets use different classloaders and casting is NOT possible !!\n");
                    e.printStackTrace();
                }
            }
        }
    }
}
