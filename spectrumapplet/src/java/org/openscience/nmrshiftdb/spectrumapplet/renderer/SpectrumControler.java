/*
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
 * Created on 16. November 2004
 */
package org.openscience.nmrshiftdb.spectrumapplet.renderer;

import org.openscience.nmrshiftdb.spectrumapplet.renderer.nmr.*;

import java.awt.*;
import java.util.ArrayList;

/**
 * Connects the mouse action between two spectrums displayed
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public interface SpectrumControler {

    /**
     * Recalc and update spectrum views
     */
    public void updateViews();


    /**
     * Called by a renderer if a zooming action is started,
     * the display of the other renderers will be updated
     * @param renderer the renderer that finished a zoom transaction
     * @param startX the minimal x-value displayed in the renderer
     */
    public void zoomingStarted(NmrSpecRenderer renderer, double startX);


    /**
     * Called by a renderer if a zooming action is called,
     * the display of all renderers will be updated
     * @param renderer the renderer calling the zoom transaction
     * @param xStart minimum x value displayed
     * @param xEnd maximum x value displayed
     */
    public void zoom(NmrSpecRenderer renderer, double xStart, double xEnd);


    /**
     * Called by a renderer if zooming actions are blocked or allowed again,
     * e.g. if one renderer shows a dialog
     * @param sender the renderer that block/allow a zooming
     * @param zoomAllowed if TRUE zooming actions are allowed
     */
    public void ignoreZoom(NmrSpecRenderer sender, boolean zoomAllowed);


    /**
     * Returns the actual range of x-values displayed
     * @return the actual range of x-values displayed:
     *   [0]= minimal x-value
     *   [1]= maximal x-value
     */
    public double[] getActualZoomRange();


    /**
     * Highlight the specified atom, previous higlights are deleted
     * @param atomNo the atom number of the highlighted atom
     */
    public void highlightPeakForAtom(int atomNo);


    /**
     * Update the data for the peak highlighters;
     * if the specified atom number == Integer.MIN_VALUE the highlighters are reset
     * @param atomNo the atom number of highlighted peak
     */
    public void updateAllPeakHighlighter(int atomNo);


    /**
     * Highlights specified atoms (in structure)
     * @param atomNumbers the atoms to highlight
     * @param requestFocus if TRUE the applet shall request the focus
     */
    public void highlightAtoms(int[] atomNumbers, boolean requestFocus);


    /**
     * Set parameter read in applet
     * @param paramNo parameter number,
     *                see SpectrumApplet.APPLET_PARAM: paramNo = index - OPTIONAL_INDEX;
     * @param value the parameter value
     */
    public void setAppletParameter(int paramNo, String value);


    /**
     * Return all detail spectrums displayed (predicted and if defined experimental)
     * @return array of DetailSpectrum
     */
    public NmrSpecRenderer[] getDetailSpectrums();


    /**
     * Display a status information text at the bottom of the spectrum
     * @param info the status information text
     */
    public void printStatusInfo(String info);


    /**
     * Set options
     * @param options array of boolean values
     */
    public void setOptions(boolean[] options);


    /**
     * Calculates the correlation lines if experimental and predicted spectrum are shown
     */
    public void calcCorrelationLines();


    /**
     * Paint correlation lines between experimental and predicted spectrum
     * @param sender the renderer calling this method
     * @param g2 the grahics context of sender
     */
    public void paintCorrelationLines(NmrSpecRenderer sender, Graphics2D g2);


    /**
     * Toggle the state for correlation lines display
     */
    public void toggleCorrelation();
    
	/**
	 * Returns the peaks in case the applet was used as a peak selection tool (see peakpicer.html in demo)
	 * 
	 * @return The peaks
	 */
    public ArrayList getPickedPeaks();
}
