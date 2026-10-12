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
 * Created on 11. Februar 2005
 */
package org.openscience.nmrshiftdb.spectrumapplet.renderer.painter;

import org.openscience.nmrshiftdb.spectrumapplet.data.nmr.*;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.*;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.nmr.*;

import java.awt.*;

import java.util.*;

/**
 *
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public class CorrelationControler {
    private NmrDetailSpectrum sprExperimental;
    private NmrDetailSpectrum sprPrediction;

    /** Creates a new instance of CorrelationCalculater
     *
     */
    public CorrelationControler(NmrDetailSpectrum pred, NmrDetailSpectrum exp) {
        sprPrediction   = pred;
        sprExperimental = exp;
    }

    /**
    * Calculates the correlation lines if experimental and predicted spectrum are shown
    */
    public void calcCorrelationLines() {
        sprPrediction.getCorrelationPainter().updateData(null);
        sprExperimental.getCorrelationPainter().updateData(null);
        NmrPeak[] p     = null;
        ArrayList pairs = searchPairs();
        if (pairs != null) {
            ArrayList correlPrediction = new ArrayList(pairs.size());
            ArrayList correlExperiment = new ArrayList(pairs.size());
            int h                      = ((Component) sprExperimental).getSize().height;
            for (int i = 0; i < pairs.size(); i++) {
                p = (NmrPeak[]) pairs.get(i);
                if (p == null) {
                    System.out.println("NULL peak pair ************");
                }
                Point[] coord = searchCorrelationLinePoints(p[0], p[1]);

                // if (coord[0].x==coord[1].x ){
                Point[] c     = new Point[2];
                c[0]          = new Point(coord[0]);
                c[1]          = new Point(c[0].x, h);
                correlExperiment.add(c);
                Point[] c2 = new Point[2];
                c2[0] = new Point(c[0].x, 0);
                c2[1] = new Point(coord[1]);
                correlPrediction.add(c2);
            }
            sprExperimental.getCorrelationPainter().updateData(correlExperiment);
            sprPrediction.getCorrelationPainter().updateData(correlPrediction);
        }
    }


    private ArrayList searchPairs() {
        ArrayList r       = null;
        NmrPeak[] visExp  = sprExperimental.getVisiblePeaks();
        NmrPeak[] visPred = sprPrediction.getVisiblePeaks();
        if ((visExp != null) && (visExp.length > 0) && (visPred != null) && (visPred.length > 0)) {
            r = new ArrayList();
            int no = 0;
            for (int i = 0; i < visExp.length; i++) {
                no = visExp[i].getAtomNumber();
                if (sprPrediction.getSpectrumModel().isShowCoupling() &&
                        sprExperimental.getSpectrumModel().isShowCoupling()) {
                    NmrPeak[] pairM = null;
                    for (int j = 0; j < visPred.length; j++) {
                        if ((no == visPred[j].getAtomNumber()) &&
                                (visExp[i].getMultiplettPosition() == visPred[j].getMultiplettPosition())) {
                            pairM = new NmrPeak[] { visExp[i], visPred[j] };
                            r.add(pairM);
                            break;
                        }
                    }
                }
                else {
                    for (int j = 0; j < visPred.length; j++) {
                        if (no == visPred[j].getAtomNumber()) {
                            NmrPeak[] pair = new NmrPeak[] { visExp[i], visPred[j] };
                            r.add(pair);
                        }
                    }
                }
            }
        }
        return r;
    }


    private Point[] searchCorrelationLinePoints(NmrPeak pExp, NmrPeak pPred) {
        Point[] p = new Point[2];

        // first implementation:
        p[0] = new Point(pExp.getX(), pExp.getY());
        p[1] = new Point(pPred.getX(), pPred.getY());
        // handle experimental spectrum:
        int y0 = sprExperimental.getMetrics().translateY_int(0);
        if (sprExperimental.getSpectrumModel().getDisplayMode() == SpectrumModel.REALISTIC) {
            int z = sprExperimental.getCurvePainter().getYvalue(p[0].x);
            if (z > 0) {
                p[0].y = z;
            }
        }
        if (p[0].y < y0) {
            p[0].y = y0;
        }

        // handle prediction spectrum:
        y0 = sprPrediction.getMetrics().translateY_int(0);
        if (sprPrediction.getSpectrumModel().getDisplayMode() == SpectrumModel.REALISTIC) {
            int z = sprPrediction.getCurvePainter().getYvalue(p[1].x);
            if (z > 0) {
                p[1].y = z;
            }
        }
        if (p[1].y > y0) {
            p[1].y = y0;
        }
        return p;
    }


    /**
    * Paint correlation lines between experimental and predicted spectrum
    * @param sender the renderer calling this method
    * @param g2 the grahics context of sender
    */
    public void paintCorrelationLines(NmrSpecRenderer sender, Graphics2D g2) {
        if (sprExperimental.getSpectrumModel().isShowCorrelation()) {
            if (sender == sprExperimental) {
                sprExperimental.getCorrelationPainter().paintComponent(g2);
                Graphics2D g = (Graphics2D) sprPrediction.getGraphics();
                sprPrediction.getCorrelationPainter().paintComponent(g);
                g.dispose();
            }
            else {
                sprPrediction.getCorrelationPainter().paintComponent(g2);
                Graphics2D g = (Graphics2D) sprExperimental.getGraphics();
                sprExperimental.getCorrelationPainter().paintComponent(g);
                g.dispose();
            }
        }
    }


    /**
     * Toggle the state for correlation lines display
     */
    public void toggleCorrelation() {
        boolean b = !sprExperimental.getSpectrumModel().isShowCorrelation();
        sprExperimental.getSpectrumModel().setShowCorrelation(b);
        sprExperimental.getCorrelationPainter().updateData(null);
        sprExperimental.forceRecalculation();
        sprPrediction.getSpectrumModel().setShowCorrelation(b);
        sprPrediction.getCorrelationPainter().updateData(null);
        sprPrediction.forceRecalculation();
    }
}
