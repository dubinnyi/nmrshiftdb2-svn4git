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
 * Created on 25. November 2004
 */
package org.openscience.nmrshiftdb.spectrumapplet.renderer.nmr;

import org.openscience.nmrshiftdb.spectrumapplet.data.nmr.*;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.*;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.painter.*;
import org.openscience.nmrshiftdb.spectrumapplet.util.*;

import java.awt.*;
import java.awt.event.*;

/**
 * Displays a prediction distribution histogram
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public class PredictionHistogram extends AbstractRenderer {
    private int yOffsetTop                   = 8;
    private int yOffsetBottom                = 40;
    private int xOffsetLeft                  = 25;
    private int xOffsetRight                 = 5;
    private Font theFont                     = new Font("ARIAL", Font.PLAIN, 12);
    private Color theBack                    = Color.lightGray;
    private Prediction predict;
    private boolean isXaxisInverted;
    private PredictCurvePainter curvePainter;

    /**
     * Creates a new instance of Histogram
     * @param p the prediction
     * @param _isXaxisInverted if TRUE x-axis is inverted
     */
    public PredictionHistogram(Prediction p, boolean _isXaxisInverted) {
        predict         = p;
        isXaxisInverted = _isXaxisInverted;
        model           = new SpectrumModel();
        metrics         = new Metrics(this, isXaxisInverted);
        double d        = (p.getMaximumShift() - p.getMinimumShift()) / 20;
        metrics.setLimitsX(p.getMinimumShift() - d, p.getMaximumShift() + d);
        metrics.setLimitsY(0, p.getMaximumCount() + 1);
        initializePainters();
        curvePainter = new PredictCurvePainter(this);
        titlePainter.updateData(new String[] { null, "[ppm]", null });
    }

    protected void draw(Graphics2D g2) {
        g2.setBackground(theBack);
        g2.setFont(theFont);
        g2.clearRect(0, 0, metrics.getSize().width, metrics.getSize().height);
        // turn antialiasing on
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        fontMetrics = g2.getFontMetrics();
        g2.setColor(Color.black);
        // Draw x title
        titlePainter.paintComponent(g2);
        // draw axis
        yGrid.paintComponent(g2);
        yAxis.paintComponent(g2);
        xGrid.paintComponent(g2);
        xAxis.paintComponent(g2);
        // draw spectrum
        curvePainter.paintComponent(g2);
        printStatusInfo(g2);
    }


    /**
     * Calculates all points displayed in spectrum
     */
    protected void calculatePoints() {
        int[] xValues = null;
        int[] yValues = null;
        int peaks     = predict.getShiftCount();
        int y0        = metrics.translateY_int(0);
        if (peaks != 0) {
            // peaks found ==> draw 
            xValues            = new int[peaks + 2];
            yValues            = new int[peaks + 2];
            xValues[0]         = metrics.translateX_int(metrics.getXmin());
            yValues[0]         = y0;
            for (int i = 0; i < peaks; i++) {
                xValues[1 + i] = metrics.translateX_int(predict.getShiftAt(i));
                yValues[1 + i] = metrics.translateY_int(predict.getShiftCountAt(i));
            }
            xValues[1 + peaks] = metrics.translateX_int(metrics.getXmax());
            yValues[1 + peaks] = y0;
        }
        int[] center = new int[2];
        center[0] = metrics.translateX_int(predict.getChemicalShift());
        center[1] = metrics.translateY_int(predict.getMaximumCount() + 1);
        reduceDataVolume(xValues, yValues);
        Object[] data = new Object[] { xValues, yValues, center };
        curvePainter.updateData(data);
    }


    private void reduceDataVolume(int[] xValues, int[] yValues) {
        // search number of really visible points
        int h      = xValues.length - 1;
        int[] xVal = new int[h + 1];
        int[] yVal = new int[h + 1];
        int z      = 0;
        for (int i = 0; i < h; i++) {
            xVal[z] = xValues[i];
            yVal[z] = yValues[i];
            while ((i < h) && (xVal[z] == xValues[i + 1])) {
                if (yVal[z] > yValues[i + 1]) {
                    // Graphics coordinates !! 
                    yVal[z] = yValues[i + 1];
                }
                i++;
            }
            z++;
        }
        xVal[z] = xValues[h];
        yVal[z] = yValues[h];
        xValues = new int[z + 1];
        yValues = new int[z + 1];
        System.arraycopy(xVal, 0, xValues, 0, z + 1);
        System.arraycopy(yVal, 0, yValues, 0, z + 1);
    }


    public static void main(String[] args) {
        String t          = "17.75|12.699999809265137;12.709999809265137;12.800000190734863;12.8000" +
                            "00190734863;12.899999618530273;13.0;15.0;15.600000381469727;15.899999618530273;17.100000381" +
                            "469727;17.200000762939453;17.299999237060547;17.299999237060547;17.399999618530273;17.5;" +
                            "17.5;17.5;17.5;17.5;17.5;17.600000381469727;17.700000762939453;17.799999237060547;17.799999237060" +
                            "547;17.799999237060547;17.799999237060547;17.899999618530273;17.899999618530273;17.8999996185" +
                            "30273;17.899999618530273;17.899999618530273;17.899999618530273;18.0;18.0;18.0;18.0;18.0;18.0;" +
                            "18.0;18.0;18.0;18.100000381469727;18.100000381469727;18.100000381469727;18.200000762939453;18.200" +
                            "000762939453;18.200000762939453;18.200000762939453;18.200000762939453;18.200000762939453;18.2000" +
                            "00762939453;18.299999237060547;18.299999237060547;18.299999237060547;18.299999237060547;18.32999" +
                            "9923706055;18.329999923706055;18.399999618530273;18.399999618530273;18.399999618530273;18.399999" +
                            "618530273;18.43000030517578;18.5;18.5;18.5;18.5;18.5;18.59000015258789;18.600000381469727;18.600" +
                            "000381469727;18.6299991607666;18.700000762939453;18.700000762939453;18.700000762939453;18.79999" +
                            "9237060547;18.799999237060547;18.799999237060547;19.0;19.100000381469727;19.229999542236328;" +
                            "17.51;17.51;17.5;17.52;17.5;17.6100000381469727;17.7100000762939453;17.7199999237060547;17.7399999237060" +
                            "547;17.7499999237060547;17.7399999237060547;17.8899999618530273;17.8899999618530273;17.88999996185" +
                            "30273;17.8799999618530273;17.8799999618530273;17.8799999618530273;18.01;18.01;18.02;18.02;18.03;18.03;" +
                            "18.04;18.04;18.05;18.1100000381469727;18.1100000381469727;18.1200000381469727;18.200000762939453;18.200" +
                            "000762939453;18.2100000762939453;18.2100000762939453;18.2200000762939453;18.22100000762939453;18.2000" +
                            "00762939453;18.298999237060547;18.297999237060547;18.299999237060547;18.299999237060547;18.32999" +
                            "9923706055;18.329999923706055;18.399999618530273;18.399999618530273;18.399999618530273;18.399999" +
                            "618530273;18.43000030517578;18.5;18.5;18.5;18.5;18.5;18.59000015258789;18.600000381469727;18.600" +
                            "000381469727;18.6299991607666;18.700000762939453;18.700000762939453;18.700000762939453;18.79999" +
                            "9237060547;18.799999237060547;18.799999237060547;19.0;19.100000381469727;19.229999542236328;" +
                            "19.299999237060547;19.299999237060547;19.399999618530273;20.0;20.0;||";
        Prediction p      = ParserUtil.parsePrediction(t)[0];
        PredictionFrame f = new PredictionFrame("abc", null, p, new Dimension(550, 380), 0, p.getInfo());
        f.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });
    }
}
