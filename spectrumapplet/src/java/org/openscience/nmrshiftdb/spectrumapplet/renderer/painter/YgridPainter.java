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

/**
 * Paints horizontal grid lines
 * @author Dr. Guenter Schneider
 * @version 1.0
 */
public class YgridPainter extends AbstractPainter {

    /** Creates a new instance
     * @param r the spectrum renderer
     */
    public YgridPainter(NmrSpecRenderer r) {
        super(r);
    }

    /**
     * Paint this component
     * @param g2 the graphics context
     */
    public void paintComponent(Graphics2D g2) {
        int right = metrics.translateX_int(metrics.getXmin());
        int left  = metrics.translateX_int(metrics.getXmax());
        if (!metrics.isInversXaxis()) {
            right = metrics.translateX_int(metrics.getXmax());
            left  = metrics.translateX_int(metrics.getXmin());
        }
        double[] tickData = metrics.searchYgrid(g2.getFontMetrics());
        g2.setColor(Color.gray);
        BasicStroke dash = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f,
                                           new float[] { 3 }, 0.0f);
        g2.setStroke(dash);
        // Draw the horziontal lines
        int y = 0;
        for (double i = 0; i < metrics.getYmax(); i += tickData[0]) {
            y = metrics.translateY_int(i);
            g2.drawLine(left, y, right, y);
        }
        if (-tickData[0] > metrics.getYmin()) {
            for (double i = -tickData[0]; i > metrics.getYmin(); i -= tickData[0]) {
                y = metrics.translateY_int(i);
                g2.drawLine(left, y, right, y);
            }
        }
        g2.setStroke(new BasicStroke());
    }


    /**
     * Update the data used for painting
     * @param data the data used for painting
     */
    public void updateData(Object data) {
    }
}
