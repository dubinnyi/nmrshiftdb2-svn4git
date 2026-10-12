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
import java.awt.geom.*;

/**
 * Paints titles
 * @author Dr. Guenter Schneider
 * @version 1.0
 */
public class TitlePainter extends AbstractPainter {
    private String[] titles;

    /** Creates a new instance
     * @param r the spectrum renderer
     */
    public TitlePainter(NmrSpecRenderer r) {
        super(r);
    }

    /**
     * Paint this component
     * @param g2 the graphics context
     */
    public void paintComponent(Graphics2D g2) {
        // Draw the global title:
        if (titles[0] != null) {
            g2.drawString(titles[0],
                          metrics.getSize().width / 2 - g2.getFontMetrics().stringWidth(titles[0]) / 2, 15);
        }

        // Draw x title:
        if (titles[1] != null) {
            g2.drawString(titles[1],
                          metrics.getSize().width - metrics.getXoffsetRight() -
                          g2.getFontMetrics().stringWidth(titles[1]), metrics.getSize().height - 5);
        }

        // Draw the title on y-axis:
        if (titles[2] != null) {
            AffineTransform org = g2.getTransform();
            AffineTransform at  = new AffineTransform();
            at.translate(15, metrics.getSize().height / 2 + g2.getFontMetrics().stringWidth(titles[2]) / 2);
            at.rotate(-Math.PI / 2.0);
            g2.setTransform(at);
            g2.drawString(titles[2], 0, 0);
            g2.setTransform(org);
        }
    }


    /**
     * Update the data used for painting
     *   expects:  String[] with: [0] = global title
     *                            [1] = x-axis title
     *                            [2] = y-axis title
     * @param data the data used for painting
     */
    public void updateData(Object data) {
        titles = (String[]) data;
    }
}
