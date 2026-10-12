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
 * Created on 5. Januar 2005
 */
package org.openscience.nmrshiftdb.spectrumapplet.renderer.painter;

import java.awt.*;

/**
 * Interface for Painter objects
 * @author Dr. Guenter Schneider
 * @version 1.0
 */
public interface Painter {

    /**
     * Paint this component
     * @param g2 the graphics context
     */
    public void paintComponent(Graphics2D g2);


    /**
     * Repaint this component
     */
    public void repaintComponent();


    /**
     * Clear this component
     */
    public void clearComponent();


    /**
     * Update the data used for painting
     * @param data the data used for painting
     */
    public void updateData(Object data);


    /**
     * Returns TRUE if somthing to paint
     * @return TRUE if somthing to paint
     */
    public boolean hasData();
}
