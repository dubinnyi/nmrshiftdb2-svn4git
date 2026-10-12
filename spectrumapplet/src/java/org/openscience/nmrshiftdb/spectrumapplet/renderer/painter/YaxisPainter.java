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
 * Paints the y-axis with labels
 * @author Dr. Guenter Schneider
 * @version 1.0
 */
public class YaxisPainter extends AbstractPainter {

    /** Creates a new instance
         * @param r the spectrum renderer
         */
    public YaxisPainter(NmrSpecRenderer r) {
        super(r);
    }

    /**
     * Paint this component
     * @param g2 the graphics context
     */
    public void paintComponent(Graphics2D g2) {
        int left = metrics.translateX_int(metrics.getXmax());
        if (!metrics.isInversXaxis()) {
            left = metrics.translateX_int(metrics.getXmin());
        }
        double[] tickData    = metrics.searchYgrid(g2.getFontMetrics());
        DecimalFormat format = new DecimalFormat("0");
        if (tickData[0] < 1) {
            format = new DecimalFormat("0.0");
        }
        if (tickData[0] < 0.1) {
            format = new DecimalFormat("0.00");
        }
        g2.setColor(Color.black);
        String label            = null;

        // Draw the vertical labels
        FontMetrics fontMetrics = g2.getFontMetrics();
        int dy                  = fontMetrics.getHeight() / 2 - 3;
        for (double i = 0; i < metrics.getYmax(); i += tickData[0]) {
            label = format.format(i);
            g2.drawString(label, left - 4 - fontMetrics.stringWidth(label), metrics.translateY_int(i) + dy);
        }
        if (-tickData[0] > metrics.getYmin()) {
            for (double i = -tickData[0]; i > metrics.getYmin(); i -= tickData[0]) {
                label = format.format(Math.abs(i));
                g2.drawString(label, left - 4 - fontMetrics.stringWidth(label), metrics.translateY_int(i) +
                              dy);
            }
        }
        g2.drawLine(left, metrics.translateY_int(metrics.getYmin()), left,
                    metrics.translateY_int(metrics.getYmax()));
    }


    /**
     * Update the data used for painting
     * @param data the data used for painting
     */
    public void updateData(Object data) {
    }
}
