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

import org.openscience.nmrshiftdb.spectrumapplet.renderer.nmr.*;

import java.awt.*;

import java.util.*;

/**
 * Paints correlation lines; paint only line sections in spectrum area
 * @author Dr. Guenter Schneider
 * @version 1.0
 */
public class CorrelationPainter extends AbstractPainter {
    private ArrayList points;

    /** Creates a new instance
     * @param r the spectrum renderer
     */
    public CorrelationPainter(NmrSpecRenderer r) {
        super(r);
    }

    /**
     * Paint this component
     * @param g2 the graphics context
     */
    public void paintComponent(Graphics2D g2) {
        if ((points != null) && (points.size() > 0)) {
            g2.setColor(Color.blue);
            BasicStroke dash = new BasicStroke(1.3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 15.0f,
                                               new float[] { 3 }, 0.0f);
            g2.setStroke(dash);
            Point[] p = null;
            for (int i = 0; i < points.size(); i++) {
                p = (Point[]) points.get(i);
                g2.drawLine(p[0].x, p[0].y, p[1].x, p[1].y);
            }
            g2.setStroke(new BasicStroke());
        }
    }


    /**
     * Update the data used for painting
     * @param data the data used for painting
     */
    public void updateData(Object data) {
        if (data == null) {
            points = null;
        }
        else {
            points = (ArrayList) data;
        }
    }
}
