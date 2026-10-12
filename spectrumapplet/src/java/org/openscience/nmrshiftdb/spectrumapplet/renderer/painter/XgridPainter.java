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
 * Paints vertical grid lines
 * @author Dr. Guenter Schneider
 * @version 1.0
 */
public class XgridPainter extends AbstractPainter {

    /** Creates a new instance
         * @param r the spectrum renderer
         */
    public XgridPainter(NmrSpecRenderer r) {
        super(r);
    }

    /**
     * Paint this component
     * @param g2 the graphics context
     */
    public void paintComponent(Graphics2D g2) {
        int top           = metrics.translateY_int(metrics.getYmax());
        int bottom        = metrics.translateY_int(metrics.getYmin());
        double[] tickData = metrics.searchXgrid(g2.getFontMetrics());

        // Draw the vertical lines
        g2.setColor(Color.gray);
        BasicStroke dash = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f,
                                           new float[] { 3 }, 0.0f);
        g2.setStroke(dash);
        int x = 0;
        for (double i = tickData[0]; i < metrics.getXmax(); i += tickData[1]) {
            x = metrics.translateX_int(i);
            g2.drawLine(x, top, x, bottom);
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
