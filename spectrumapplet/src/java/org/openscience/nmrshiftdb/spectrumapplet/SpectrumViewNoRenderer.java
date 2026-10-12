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
 
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.JApplet;

import org.openscience.nmrshiftdb.spectrumapplet.renderer.SpectrumControler;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.SpectrumNavigation;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.nmr.NmrSpecRenderer;
import org.openscience.nmrshiftdb.spectrumapplet.util.SpectrumApplet;

/**
 * Applet displaying a NMR spectrum,
 * NO interaction with Marvin structure applet !!!
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public class SpectrumViewNoRenderer extends JApplet implements SpectrumApplet {
    private SpectrumControler spectrumPanel = null;

    /**
     *Gets the appletInfo
     *
     * @return    The appletInfo value
     */
    public String getAppletInfo() {
        return ("Spectrum viewer applet by G.Schneider");
    }


    /**
     *Initialises the applet
     */
    public void init() {
        String specType = getParameter(APPLET_PARAM[0]);
        String f        = getParameter(APPLET_PARAM[1]);
        String predSpec = getParameter(APPLET_PARAM[2]);
        String pred     = getParameter(APPLET_PARAM[3]);
        String solvent  = getParameter(APPLET_PARAM[4]);
        String expSpec  = getParameter(APPLET_PARAM[5]);
        String peakPicker= getParameter("peakpicker");
        spectrumPanel   = new SpectrumNavigation(specType, f, solvent, predSpec, pred, expSpec, this, false, peakPicker!=null && peakPicker.equals("true"), Color.LIGHT_GRAY);
        readParameter();
        //activateKeyListener();
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add((Component) spectrumPanel);
    }


    private void readParameter() {
        String s = null;
        for (int i = OPTIONAL_INDEX; i < APPLET_PARAM.length; i++) {
            s = getParameter(APPLET_PARAM[i]);
            if ((s != null) && (s.length() > 0)) {
                try {
                    spectrumPanel.setAppletParameter(i - OPTIONAL_INDEX, s.trim());
                }
                catch (Exception e) {
                    System.out.println("Exception during:  setParam(" + APPLET_PARAM[i] + ")= " + s);
                    e.printStackTrace();
                }
            }
        }
        NmrSpecRenderer[] r = spectrumPanel.getDetailSpectrums();
        for (int i = 0; i < r.length; i++) {
            if (r[i] != null) {
                r[i].forceRecalculation();
            }
        }
    }


    /**
     *Starts the applet
     */
    public void start() {
        repaint();
    }


    /**
     * Highlight peak in spectrum
     * Better: hightlightPeakInSpectrum(int atomNo), 
     * @param  paintAtom  number of the atom to highlight
     */
    public void paintLine(int paintAtom) {
        highlightPeakInSpectrum(paintAtom);
    }

    /**
     * Highlight peak in spectrum
     * @param  atomNo  number of the atom to highlight
     */
    public void highlightPeakInSpectrum(int atomNo) {
        // atom count starts with zero in NMRSHIFTDB, chemical count starts with 1 !!
        spectrumPanel.highlightPeakForAtom(atomNo + 1);
    }
    
    /**
     * Remove the peak highlighting in spectrum
     */
    public void removeHighlightingInSpectrum() {
        spectrumPanel.highlightPeakForAtom(Integer.MIN_VALUE);
    }

    /**
     * Handles interaction with structure viewer,
     * highlighted atoms in spectrum view will be highlighted in structure
     * @param atomNumbers atom numbers of peaks highlighted in spectrum
     */
    public void highlightAtomsInStructur(int[] atomNumbers) {
    }
    
    public void highlightRowsInTable(int[] atomNumbers){
    }


    /**
     * Unselect all atoms in structure
     */
    public void removeHighlightingInStructur() {
    }
    
	/**
	 * Returns the peaks in case the applet was used as a peak selection tool (see peakpicer.html in demo)
	 * 
	 * @return The peaks
	 */
	public String getPickedPeaks(){
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<spectrumPanel.getPickedPeaks().size();i++){
			sb.append((Double)spectrumPanel.getPickedPeaks().get(i)+"|");
		}
		return sb.toString();
	}
}
