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
package org.openscience.nmrshiftdb.spectrumapplet.renderer.nmr;

import java.awt.Cursor;
import java.util.ArrayList;

import org.openscience.nmrshiftdb.spectrumapplet.data.nmr.NmrPeak;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.SpectrumRenderer;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.dialog.AbstractDialog;

/**
 * Renderer interface
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public interface NmrSpecRenderer extends SpectrumRenderer {

    /**
     * Set a special option dialog object
     * @param optionsDialog the option dialog
     */
    public void setOptionDialog(AbstractDialog optionsDialog);


    /**
     * Sets the cursor image to the specified cursor.
     * @param c One of the constants defined
     *                 by the <code>Cursor</code> class;
     *                 if this parameter is <code>null</code>
     *                 then this component will inherit
     *                 the cursor of its parent
     */
    public void setCursor(Cursor c);


    /**
     * Return the peak for specified atom number
     * @param atomNumber the atom number
     * @return the peak
     */
    public NmrPeak getPeakForAtomNumber(int atomNumber);


    /**
     * Zooms the displayed range of the spectrum
     * @param startX minimum x value displayed
     * @param endX maximum x value displayed
     */
    public void zoom(double startX, double endX);


    /**
     * Reset zoom, display the initial range of the spectrum
     */
    public void resetZoom();


    /**
     * Checks if painted dialog buttons/checkboxes etc. are clicked by mouse
     * @param x x value of mouse position
     * @param y y value of mouse position
     */
    public void checkDialogActions(int x, int y);


    /**
     * Search the peak under mouse cursor and displays peak information
     * @param xCursor x value of mouse position
     */
    public void findPeakUnderCursor(int xCursor);


    /**
     * Notify the renderer that an external frame was closed
     * @param frameID the frame ID
     */
    public void setExtraFrameClosed(String frameID);


    /**
     *Invoked when left mouse button pressed
     * @param mouseX mose position x
     * @param mouseY mose position y
     */
    public void leftPressed(int mouseX, int mouseY);


    /**
     * Invoked when left mouse button pressed and mouse moved
     * @param mouseX mose position x
     * @param mouseY mose position y
     */
    public void leftDragged(int mouseX, int mouseY);


    /**
     * Invoked when left mouse button released
     * @param mouseX mose position x
     * @param mouseY mose position y
     */
    public void leftReleased(int mouseX, int mouseY);
    
    /**
     * Does a peakpicking in the given range
     * @param mouseX mose position x
     * @param mouseY mose position y
     */
    public void pickpeaks(double startX, double endX);
    
	/**
	 * Returns the peaks in case the applet was used as a peak selection tool (see peakpicer.html in demo)
	 * 
	 * @return The peaks
	 */
    public ArrayList getPickedPeaks();
    
    /**
     * Return if Help or Options dialog are visible
     * 
     * @return true if help or (not xor!) options are visible, else false.
     */
    public boolean isHelpOrOptionsVisible();
}
