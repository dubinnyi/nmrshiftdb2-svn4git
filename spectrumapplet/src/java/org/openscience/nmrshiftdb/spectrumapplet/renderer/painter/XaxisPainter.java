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

import java.text.*;

/**
 * Paints the x-axis with labels
 * @author Dr. Guenter Schneider
 * @version 1.0
 */
public class XaxisPainter extends AbstractPainter {

    /** Creates a new instance
         * @param r the spectrum renderer
         */
    public XaxisPainter(NmrSpecRenderer r) {
        super(r);
    }

    /**
     * Paint this component
     * @param g2 the graphics context
     */
    public void paintComponent(Graphics2D g2) {
        int x                = 0;
        int bottom           = metrics.translateY_int(metrics.getYmin());
        double[] tickData    = metrics.searchXgrid(g2.getFontMetrics());

        // draw major ticks and labels
        DecimalFormat format = new DecimalFormat("0.00");
        if ((tickData[1] > 0.05) && (tickData[1] < 1)) {
            format = new DecimalFormat("0.0");
        }
        else if (tickData[1] >= 1) {
            format = new DecimalFormat("0");
        }
        g2.setStroke(new BasicStroke());
        g2.setColor(Color.black);
        String label = null;
        for (double i = tickData[0]; i <= metrics.getXmax(); i += tickData[1]) {
            x = metrics.translateX_int(i);
            g2.drawLine(x, bottom, x, bottom + 4);
            label = format.format(i);
            g2.drawString(label, x - g2.getFontMetrics().stringWidth(label) / 2,
                          bottom + 6 + g2.getFontMetrics().getHeight());
        }

        // draw minor ticks
        for (double i = tickData[0]; i < metrics.getXmax(); i += tickData[2]) {
            x = metrics.translateX_int(i);
            g2.drawLine(x, bottom, x, bottom + 2);
        }
        if ((tickData[0] - tickData[2]) > metrics.getXmin()) {
            for (double i = tickData[0] - tickData[2]; i >= metrics.getXmin(); i -= tickData[2]) {
                x = metrics.translateX_int(i);
                g2.drawLine(x, bottom, x, bottom + 2);
            }
        }
        g2.drawLine(metrics.translateX_int(metrics.getXmin()), bottom,
                    metrics.translateX_int(metrics.getXmax()), bottom);
    }


    /**
     * Update the data used for painting
     * @param data the data used for painting
     */
    public void updateData(Object data) {
    }
}
