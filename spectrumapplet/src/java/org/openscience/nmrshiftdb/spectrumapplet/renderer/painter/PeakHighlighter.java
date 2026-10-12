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
import org.openscience.nmrshiftdb.spectrumapplet.renderer.*;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.nmr.*;

import java.awt.*;

import java.util.*;

/**
 * Paints highlighted peaks
 * @author Dr. Guenter Schneider
 * @version 1.0
 */
public class PeakHighlighter extends AbstractPainter {
    private NmrPeak[] peaks;
    private ArrayList parents;
    private boolean hasData;

    /**
     * Creates a new instance
     * @param r the spectrum renderer
     */
    public PeakHighlighter(NmrSpecRenderer r) {
        super(r);
    }

    /**
     * Paint this component
     * @param g2 the graphics context
     */
    public void paintComponent(Graphics2D g2) {
        g2.setStroke(new BasicStroke(3f));
        int y0    = metrics.translateY_int(0);
        int y     = metrics.translateY_int(metrics.getYmin());
        int x     = 0;
        NmrPeak p = null;
        if (parents != null) {
            g2.setColor(Color.MAGENTA);
            for (int i = 0; i < parents.size(); i++) {
                p = (NmrPeak) parents.get(i);
                x = p.getX();
                //g2.drawLine(x, y0, x, p.getY() );
                //g2.drawLine(x, y0, x, y+5);
                g2.drawLine(x, y - 5, x, y + 5);
            }
        }
        if (peaks != null) {
            g2.setColor(renderer.getSpectrumModel().getHighlightColor());
            for (int i = 0; i < peaks.length; i++) {
                p = peaks[i];
                if ((p.getValueX() <= metrics.getXmax()) && (p.getValueX() >= metrics.getXmin())) {
                    x = p.getX();
                    y = p.getY();
                    //y= metrics.translateY_int(p.getIntegral());
                    g2.drawLine(x, y0, x, y);
                }
            }
        }
        g2.setStroke(new BasicStroke());
    }


    /**
     * Returns TRUE if somthing to paint
     * @return TRUE if somthing to paint
     */
    public boolean hasData() {
        return hasData;
    }


    /**
     * Update the data used for painting
     *   expects:  Object[] with: [0] = Peak[]
     *                            [1] = PeakList
     *                            [2] = SpecRenderer
     * @param data the data used for painting
     */
    public void updateData(Object data) {
        if (data == null) {
            hasData = false;
            parents = null;
            peaks   = null;
        }
        else {
            hasData = true;
            Object[] d = (Object[]) data;
            peaks = (NmrPeak[]) d[0];
            // cut all value out of y-offsets
            int top    = renderer.getSpectrumModel().getYoffsetTop();
            int bottom = metrics.getSize().height - renderer.getSpectrumModel().getYoffsetBottom();
            for (int i = 0; i < peaks.length; i++) {
                if (peaks[i].getY() < top) {
                    peaks[i].setY(top);
                }
                else if (peaks[i].getY() > bottom) {
                    peaks[i].setY(bottom);
                }
            }
            NmrData pList     = (NmrData) d[1];
            NmrSpecRenderer r = (NmrSpecRenderer) d[2];
            int no            = 0;
            if (r.getSpectrumModel().isShowCoupling()) {
                Metrics metrics = r.getMetrics();

                // search parent peaks:
                parents = new ArrayList();
                NmrPeak p = null;
                for (int i = 0; i < peaks.length; i++) {
                    no = peaks[i].getAtomNumber();
                    p  = pList.getPeakForAtomnumber(no);
                    if (!parents.contains(p)) {
                        // update coordinates
                        p.setX(metrics.translateX_int(p.getValueX()));
                        parents.add(p);
                    }
                }
            }
        }
    }
}
