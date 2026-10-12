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
 * Created on 21. November 2004
 */
package org.openscience.nmrshiftdb.spectrumapplet.renderer.action;

import org.openscience.nmrshiftdb.spectrumapplet.renderer.nmr.*;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;

/**
 * Provides zooming functionality in the detailed spectrum
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public class SpectrumMouseActions extends MouseInputAdapter implements MouseActionManager {
    private NmrSpecRenderer renderer;
    private boolean isLeftPressed;
    private boolean isRightPressed;
    private boolean ignoreZoomOps;
    private int oldX;
    private boolean peakpicker;

    /**
     * Creates a new instance of SpectrumMouseActions
     * @param r the spectrum renderer
     * @param peakpicker Shall the applet be used as a peak selection tool (see peakpicker.html in demo)?
     */
    public SpectrumMouseActions(NmrSpecRenderer r, boolean peakpicker) {
        renderer = r;
        this.peakpicker=peakpicker;
    }

    /**
     * Start or stop zoom operations triggered by mouse operations
     * @param ignore if TRUE all zoom operations triggered by mouse operations are stopped
     */
    public void setIgnoreZoomOps(boolean ignore) {
        if (ignore != ignoreZoomOps) {
            ignoreZoomOps = ignore;
            if (renderer.getControler() != null) {
                renderer.getControler().ignoreZoom(renderer, ignore);
            }
        }
    }


    /**
     * Invoked when a mouse button has been pressed on a component.
     * @param e the MouseEvent
     */
    public void mousePressed(MouseEvent e) {
        synchronized (this) {
            if (!ignoreZoomOps) {
                if (e.getButton() == e.BUTTON3) {
                    isRightPressed = true;
                    renderer.getZoomMarker().initGraphicsRange(e.getX());
                    renderer.getZoomMarker().paintMarker(true);
                    ((JComponent) renderer).setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
                }
                else if (e.getButton() == e.BUTTON1) {
                    isLeftPressed = true;
                    renderer.getZoomMarker().initGraphicsRange(e.getX());
                    renderer.leftPressed(e.getX(), e.getY());
                }
            }
        }
    }


    /**
     * Invoked when a mouse button is pressed on a component and then
     * dragged.
     * @param e the MouseEvent
     */
    public void mouseDragged(MouseEvent e) {
        if (isRightPressed) {
            renderer.getZoomMarker().setDraggedLimit(e.getX());
            renderer.getZoomMarker().paintMarker(true);
        }
        else if (isLeftPressed) {
            renderer.leftDragged(e.getX(), e.getY());
        }
    }


    /**
     * Invoked when a mouse button has been released on a component.
     * @param e the MouseEvent
     */
    public void mouseReleased(MouseEvent e) {
        if ((isRightPressed) && (e.getButton() == e.BUTTON3)) {
            isRightPressed = false;
            renderer.setCursor(Cursor.getDefaultCursor());
            renderer.getZoomMarker().setDraggedLimit(e.getX());
            renderer.getZoomMarker().paintMarker(true);
            double[] r = renderer.getZoomMarker().getRange();
            renderer.zoom(r[0], r[1]);
            
        }
        else if (isLeftPressed) {
        	if(!peakpicker){
	            isLeftPressed = false;
	            renderer.leftReleased(0, 0);
	            //the !renderer.isHelpOrOptionsVisible() avoids peak picking if a dialog is closed
        	}else if(!renderer.isHelpOrOptionsVisible()){
                renderer.getZoomMarker().setDraggedLimit(e.getX());
                double[] r = renderer.getZoomMarker().getRange();
                //if borders match (eg if clicking without draggin, no peak can be in the area anyway)
                if(r[0]!=r[1])
                	renderer.pickpeaks(r[0],r[1]);
        	}
        }
    }


    /**
     * Invoked when the mouse button has been clicked (pressed
     * and released) on a component.
     * @param e the MouseEvent
     */
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == e.BUTTON1) {
            // check for double-click with left mouse button
            if (!ignoreZoomOps && (e.getClickCount() >= 2)) {
           		renderer.resetZoom();
            }
            else if (e.getClickCount() == 1) {
                renderer.checkDialogActions(e.getX(), e.getY());
            }
        }
    }


    /**
     * Invoked when the mouse cursor has been moved onto a component
     * but no buttons have been pushed.
     * @param e the MouseEvent
     */
    public void mouseMoved(MouseEvent e) {
        int x = e.getX();
        if (oldX != x) {
            // only horizontal movement
            oldX = x;
            renderer.findPeakUnderCursor(x);
        }
    }
}
