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
import java.text.*;
import org.openscience.nmrshiftdb.spectrumapplet.data.ir.*;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.*;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.nmr.*;




/**
 * Paints the curve/spectra
 * @author Dr. Guenter Schneider
 * @version 1.0
 */
public class DistancePainter extends AbstractPainter {
    private boolean isEnabled;
    private int distanceStart;
    private double distanceStartValue;
    private double specFrequency;
    private int distanceEnd;
    private Color c                   = Color.red;
    private CurvePainter curvePainter;
    private InfoPainter infoPainter;
    private int arrowLimit;

    /** Creates a new instance
         * @param r the spectrum renderer
         */
    public DistancePainter(NmrSpecRenderer r) {
        super(r);
        infoPainter  = ((AbstractRenderer) renderer).getInfoPainter();
        curvePainter = ((AbstractRenderer) renderer).getCurvePainter();
    }


    /**
     * Paint this component
     * @param g2 the graphics context
     */
    public void paintComponent(Graphics2D g2) {
        if (isEnabled) {
            g2.setColor(c);
            g2.setStroke(new BasicStroke(2f));
            if (renderer.getSpectrumModel().getDisplayMode() == SpectrumModel.REALISTIC) {
                paintArrow(g2, distanceStart);
                if (distanceStart != distanceEnd) {
                    paintArrow(g2, distanceEnd);
                }
            }
            else {
                int y = metrics.translateY_int(0) - 3;
                g2.drawLine(distanceStart, metrics.getYoffsetTop(), distanceStart, y);
                g2.drawLine(distanceEnd, metrics.getYoffsetTop(), distanceEnd, y);
            }
            g2.setStroke(new BasicStroke());
        }
    }


    private void paintArrow(Graphics2D g2, int x) {
        int y0 = curvePainter.getYvalue(x);
        if (y0 > 0) {
            int direction = -1;
            if (y0 <= (arrowLimit+2)) {
                direction = 1;
            }
            y0 = y0 - direction * 5;
            int y1 = y0 - direction * 15;
            if (y1 < metrics.getYoffsetTop()) {
                y1 = metrics.getYoffsetTop();
            }
            g2.drawLine(x, y1, x, y0);
            g2.drawLine(x, y0, x - 3, y0 - direction * 3);
            g2.drawLine(x, y0, x + 3, y0 - direction * 3);
        }
    }


    /**
     * Update the data used for painting
     * @param data the data used for painting
     */
    public void updateData(Object data) {
        isEnabled = true;
        if (data == null) {
            isEnabled = false;
        }
        else if (data instanceof double[]) {
            // init:
            double[] d = (double[]) data;
            distanceStart      = (int) d[0];
            distanceEnd        = distanceStart;
            distanceStartValue = d[1];
            specFrequency      = d[2];
            // d[3]:  defines y-limit for arrow direction
            arrowLimit =(int)d[3];
        }
        else if (data instanceof Integer) {
            // move:
            distanceEnd = ((Integer) data).intValue();
        }
        if (isEnabled) {
            String info          = "Distance= ";
            DecimalFormat format = new DecimalFormat("0.00");
            if (distanceStart != distanceEnd) {
                double d = Math.abs(distanceStartValue - metrics.translateMouseX(distanceEnd));
                info += format.format(d * specFrequency) + " Hz";
            }
            else {
                String unit = " ppm";
                if (renderer.getDataContainer() instanceof RawIRdata ){
                    format = new DecimalFormat("0.0");
                    unit = "  1/cm";
                }
                info = "Position= " + format.format(distanceStartValue) + unit;
            }
            infoPainter.updateData(info);
        }
    }
}
