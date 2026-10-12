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
 * Created on 17. November 2004
 */
package org.openscience.nmrshiftdb.spectrumapplet.renderer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import org.openscience.nmrshiftdb.spectrumapplet.data.DataContainer;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.painter.CurvePainter;

/**
 * Renderer interface
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public interface SpectrumRenderer {

    /**
     * Set the <CODE>SpectrumControler</CODE> object that manages the
     * syncronisation of multiple spectrums displayed
     * @param c the <CODE>SpectrumControler</CODE>
     */
    public void setControler(SpectrumControler c);


    /**
     * Return the <CODE>SpectrumControler</CODE> object that manages the
     * syncronisation of multiple spectrums displayed
     * @return the <CODE>SpectrumControler</CODE>
     */
    public SpectrumControler getControler();


    /**
     * Returns a graphics context for this component. This method will
     * return <code>null</code> if this component is currently not
     * displayable.
     * @return a graphics context for this component, or <code>null</code>
     *             if it has none
     */
    public Graphics getGraphics();


    /**
     * Return the object that handles all metric information
     * @return the metirc info
     */
    public Metrics getMetrics();


    public void repaint();


    /**
     * Update border values
     */
    public void updateBorderValues();


    /**
     * Executes a recalculation of display data
     */
    public void doRecalculation();


    /**
     * Force a new calculation of all values used in paintComponent(Graphics)
     */
    public void forceRecalculation();


    /**
     * Return the corresponding model
     * @return the corresponding model
     */
    public SpectrumModel getSpectrumModel();


    /**
     * Return the CurvePainter for this spectrum
     * @return the CurvePainter
     */
    public CurvePainter getCurvePainter();


    /**
     * Return the data container
     * @return the data container
     */
    public DataContainer getDataContainer();


    /**
     * Return the object that handles zoom information and marker drawing
     * @return the ZoomMarker
     */
    public ZoomMarker getZoomMarker();


    /**
    * Updates the displayed range of the spectrum
    * @param startX minimum x value displayed
    * @param endX maximum x value displayed
    * @return TRUE if data updated
    */
    public boolean updateZoomData(double startX, double endX);


    /**
     * Start or stop zoom operations triggered by mouse operations
     * @param ignore if TRUE all zoom operations triggered by mouse operations are stopped
     */
    public void setIgnoreZoomOps(boolean ignore);


    /**
     * Highlight the specified atom, previous higlights are deleted
     * @param atomNo the atom number of the highlighted atom
     */
    public void highlightPeakForAtom(int atomNo);


    /**
     * Display a status information text at the bottom of the spectrum
     * @param info the status information text
     * @param c the color
     */
    public void printStatusInfo(String info, Color c);


    /**
     * Invoked when a key has been typed.
     * @param e the KeyEvent
     */
    public void keyPressed(KeyEvent e);
    
	/**
	 * Returns the peaks in case the applet was used as a peak selection tool (see peakpicer.html in demo)
	 * 
	 * @return The peaks
	 */
    public ArrayList getPickedPeaks();
}
