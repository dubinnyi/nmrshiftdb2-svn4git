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
 * Created on 21. November 2004
 */
package org.openscience.nmrshiftdb.spectrumapplet.renderer.action;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * Interface for all classes that handle mouse input and zooming operations in a renderer
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public interface MouseActionManager extends MouseListener, MouseMotionListener {

    /**
     * Start or stop zoom operations triggered by mouse operations
     * @param ignore if TRUE all zoom operations triggered by mouse operations are stopped
     */
    public void setIgnoreZoomOps(boolean ignore);


    /**
     * Invoked when a mouse button has been pressed on a component.
     * @param e the MouseEvent
     */
    public void mousePressed(MouseEvent e);


    /**
     * Invoked when a mouse button is pressed on a component and then
     * dragged.
     * @param e the MouseEvent
     */
    public void mouseDragged(MouseEvent e);


    /**
     * Invoked when the mouse button has been clicked (pressed
     * and released) on a component.
     * @param e the MouseEvent
     */
    public void mouseClicked(MouseEvent e);


    /**
     * Invoked when a mouse button has been released on a component.
     * @param e the MouseEvent
     */
    public void mouseReleased(MouseEvent e);


    /**
     * Invoked when the mouse cursor has been moved onto a component
     * but no buttons have been pushed.
     * @param e the MouseEvent
     */
    public void mouseMoved(MouseEvent e);


    /**
     * Invoked when the mouse enters a component.
     * @param e the MouseEvent
     */
    public void mouseEntered(MouseEvent e);
}
