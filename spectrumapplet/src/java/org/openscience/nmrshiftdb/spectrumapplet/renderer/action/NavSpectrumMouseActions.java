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

import java.awt.Container;
import java.awt.Cursor;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.event.MouseInputAdapter;

import org.openscience.nmrshiftdb.spectrumapplet.renderer.SpectrumNavigation;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.nmr.NmrNavigationSpectrum;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.nmr.NmrSpecRenderer;

/**
 * Provides zooming functionality in the navigation spectrum
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public class NavSpectrumMouseActions extends MouseInputAdapter implements MouseActionManager {
    private NmrSpecRenderer renderer;
    private int startX;

    //private int endX;
    private boolean isRightPressed;
    private boolean isLeftPressed;
    private boolean ignoreZoomOps;

    /**
     * Creates a new instance of NavSpectrumMouseActions
     * @param r the spectrum renderer
     */
    public NavSpectrumMouseActions(NmrSpecRenderer r) {
        renderer = r;
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
        if (!ignoreZoomOps) {
            if (!isLeftPressed && (e.getButton() == e.BUTTON3)) {
                isRightPressed = true;
                renderer.getZoomMarker().backUpValues();
                renderer.getZoomMarker().initGraphicsRange(e.getX());
                renderer.getZoomMarker().paintMarker(true);
                ((JComponent) renderer).setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
            }
            else if (!isRightPressed && (e.getButton() == e.BUTTON1)) {
                isLeftPressed = true;
                startX        = e.getX();
                ((JComponent) renderer).setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
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
            int delta = e.getX() - startX;
            startX = e.getX();
            renderer.getZoomMarker().translate(delta);
            renderer.getZoomMarker().paintMarker(true);
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
            	SpectrumNavigation sn=((NmrNavigationSpectrum)renderer).specNav;
   	    		if (!sn.getJexf().isShowing()) {
   					sn.getJexf().show(sn);
   				}
            }
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
        else if ((isLeftPressed) && (e.getButton() == e.BUTTON1)) {
            isLeftPressed = false;
            renderer.setCursor(Cursor.getDefaultCursor());
            //renderer.getZoomMarker().setDraggedLimit(e.getX());
            //renderer.getZoomMarker().paintMarker(true);
            double[] r = renderer.getZoomMarker().getRange();
            renderer.zoom(r[0], r[1]);
        }
    }


    /**
     * Invoked when the mouse cursor has been moved onto a component
     * but no buttons have been pushed.
     * @param e the MouseEvent
     */
    public void mouseMoved(MouseEvent e) {
    }


    /**
     * Invoked when the mouse enters a component.
     * @param e the MouseEvent
     */
    public void mouseEntered(MouseEvent e) {
        // used to update panel after focus lost, perhaps there is a better way
        //renderer.repaint();
    }
}
