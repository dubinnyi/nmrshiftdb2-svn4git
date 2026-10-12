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

import org.openscience.nmrshiftdb.spectrumapplet.data.nmr.*;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.nmr.*;

import java.awt.*;

/**
 * Paints the curve/spectra
 * @author Dr. Guenter Schneider
 * @version 1.0
 */
public class CurvePainter extends AbstractPainter {
    private int[] xValues;
    private int[] yValues;
    private int[] colors;
    private Color c;

    /**
     * Creates a new instance of TitlePainter
     * @param r the spectrum renderer
     */
    public CurvePainter(NmrSpecRenderer r) {
        super(r);
        c      = Color.black;
        colors = null;
    }

    /**
     * Set the curve color
     */
    public void setColor(Color color) {
        c = color;
    }


    /**
     * Paint this component
     * @param g2 the graphics context
     */
    public void paintComponent(Graphics2D g2) {
        BasicStroke bs = new BasicStroke(1.2f);
        g2.setStroke(bs);
        if (colors == null) {
            // draw curve/spectra
            g2.setColor(c);
            g2.drawPolyline(xValues, yValues, xValues.length);
        }
        else {
            //colored NMR histogram
            /*
            for (int i = 0; i < colors.length ; i++) {
                g2.setColor(NmrPeak.PEAK_COLOR[colors[i]]);
                g2.drawLine(xValues[i*3], yValues[i*3], 
                            xValues[i*3 + 1], yValues[i*3 + 1]);
                g2.drawLine(xValues[i*3 + 1], yValues[i*3 + 1], 
                            xValues[i*3 + 2], yValues[i*3 + 2]);
            }
            */
            g2.setColor(NmrPeak.PEAK_COLOR[colors[0]]);
            g2.drawLine(xValues[0], yValues[0], xValues[1], yValues[1]);
            for (int i = 1; i < colors.length; i++) {
                g2.setColor(NmrPeak.PEAK_COLOR[colors[i]]);
                g2.drawLine(xValues[i*3], yValues[i*3], xValues[i*3 + 1], yValues[i*3 + 1]);
                g2.drawLine(xValues[i*3 + 1], yValues[i*3 + 1], xValues[i*3 + 2], yValues[i*3 + 2]);
            }
             
        }
        g2.setStroke(new BasicStroke());
    }


    /**
     * Update the data used for painting
     *   expects:  Object[] with: [0] = xValues[]
     *                            [1] = yValues[]
     * @param data the data used for painting
     */
    public void updateData(Object data) {
        Object[] d = (Object[]) data;
        if (d.length == 2) {
            xValues = (int[]) d[0];
            yValues = (int[]) d[1];
            colors  = null;
        }
        else {
            // colored NMR histogram
            xValues = (int[]) d[0];
            yValues = (int[]) d[1];
            colors  = (int[]) d[2];
        }

        // cut all value out of y-offsets
        int top    = renderer.getSpectrumModel().getYoffsetTop();
        int bottom = metrics.getSize().height - renderer.getSpectrumModel().getYoffsetBottom();
        for (int i = 0; i < yValues.length; i++) {
            if (yValues[i] < top) {
                yValues[i] = top;
            }
            else if (yValues[i] > bottom) {
                yValues[i] = bottom;
            }
        }
    }


    public int getYvalue(int index) {
        int r = 0;
        int z = index - metrics.getXoffsetLeft() - 1;
        if (z < yValues.length) {
            r = yValues[z];
        }
        return r;
    }
}
