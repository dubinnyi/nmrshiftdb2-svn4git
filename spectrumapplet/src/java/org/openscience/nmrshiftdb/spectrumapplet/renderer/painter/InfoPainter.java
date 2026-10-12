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
 * @author Dr. Guenter Schneider
 * @version 1.0
 */
public class InfoPainter extends AbstractPainter {
    private String info;

    /**
     * Creates a new instance
     * @param r the spectrum renderer
     */
    public InfoPainter(NmrSpecRenderer r) {
        super(r);
    }

    /**
     * Paint this component
     * @param g2 the graphics context
     */
    public void paintComponent(Graphics2D g2) {
        if (info != null) {
            g2.setColor(new Color(222, 222, 222));
            int w = g2.getFontMetrics().stringWidth(info);
            int h = g2.getFontMetrics().getHeight();
            int x = metrics.getSize().width / 2 - w / 2;
            int y = metrics.getYoffsetTop();
            y     = 5;
            g2.fillRect(x - 3, y, w + 6, h + 2);
            g2.setColor(Color.black);
            g2.drawString(info, x, y + h - 2);
        }
    }


    /**
     * Update the data used for painting
     * @param data the data used for painting
     */
    public void updateData(Object data) {
        info = (String) data;
    }
}
