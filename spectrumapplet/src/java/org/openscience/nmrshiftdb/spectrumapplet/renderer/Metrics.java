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
 * Created on 8. November 2004
 */
package org.openscience.nmrshiftdb.spectrumapplet.renderer;

import org.openscience.nmrshiftdb.spectrumapplet.data.*;
import org.openscience.nmrshiftdb.spectrumapplet.data.nmr.*;
import org.openscience.nmrshiftdb.spectrumapplet.util.*;

import java.awt.*;

/**
 * Class containing all metric/geometric information of the displayed spectrum
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public class Metrics {
    // constants for x-axis calculation
    private static final double[] stepsX      = new double[] {
                                                    0.01, 0.02, 0.05, 0.1, 0.2, 0.5, 1, 2, 5, 10, 20, 50, 100,
                                                    200, 500, 1000
                                                };
    private static final double[] minorTicksX = new double[] {
                                                    0.001, 0.005, 0.01, 0.01, 0.05, 0.1, 0.1, 0.5, 1, 1, 5, 10,
                                                    10, 50, 100, 100
                                                };
    private static final double[] roundX      = new double[] {
                                                    1, 1, 1, 0, 0, 0, -1, -1, -1, -2, -2, -2, -3, -3, -3, -4
                                                };

    // constants for y-axis calculation
    private static final double[] stepsY      = new double[] {
                                                    0.02, 0.1, 0.2, 0.5, 1, 2, 5, 10, 20, 50, 100, 200, 500,
                                                    1000, 2000, 5000, 10000, 20000, 50000, 100000
                                                };
    private SpectrumRenderer renderer;
    private DataContainer dataContainer;
    private boolean inversXaxis;
    private Dimension theSize;
    private int xBorder;
    private int yBorder;
    private double xMin;
    private double xMax;
    private double xMinInitial;
    private double xMaxInitial;
    private double yMin;
    private double yMax;
    private int xPointCount;
    private int yPointCount;
    private double factorX;
    private double factorY;
    private double scaleX;
    private double scaleY;
    private double stepWidthX;

    /**
     * Creates a new instance of Metrics
     * @param invertedX if TRUE the spectrum x axis orientation is inverted
     */
    public Metrics(SpectrumRenderer r, boolean invertedX) {
        renderer      = r;
        dataContainer = r.getDataContainer();
        inversXaxis   = invertedX;
        xBorder       = getXoffsetLeft() + getXoffsetRight();
        yBorder       = getYoffsetTop() + renderer.getSpectrumModel().getYoffsetBottom();
    }

    /**
     * Return the size of paint area
     * @return the size of paint area
     */
    public Dimension getSize() {
        return theSize;
    }


    /**
     * Set the size of paint area
     * @param size the size of paint area
     */
    public void setSize(Dimension size) {
        theSize = size;
        calcFactors();
    }


    /**
    * Calc factors used e.g. for transformation operations
    */
    private void calcFactors() {
        if (theSize != null) {
            xPointCount = theSize.width - xBorder;
            yPointCount = theSize.height - yBorder;
            factorX     = theSize.width / (xMax - xMin);
            factorY     = theSize.height / (yMax - yMin);
            scaleX      = ((double) (theSize.width - xBorder)) / theSize.width;
            scaleY      = ((double) (theSize.height - yBorder)) / theSize.height;
            stepWidthX  = (xMax - xMin) / xPointCount; // x-step width
        }
    }


    /**
     * Return TRUE if x-axis is inverted (default for NMR spectra)
     * @return TRUE if x-axis is inverted
     */
    public boolean isInversXaxis() {
        return inversXaxis;
    }


    /**
     * Calc the border properties used to display the spectrum
     */
    public void calcBorderValues() {
        calcXborderValues();
        calcYborderValues();
    }


    /**
     * Calculate the border values on x-axis, (xmin and xmax)
     */
    public void calcXborderValues() {
        double xmax = dataContainer.getXmax();
        double xmin = dataContainer.getXmin();
        if (dataContainer instanceof NmrData) {
            //System.out.println("xmin=" + xmin + "   xmax=" + xmax);
            double dx = 1 + (int) Math.floor((xmax - xmin) / 10);
            if (((AbstractDataContainer) dataContainer).is1Hspectrum()) {
                dx   = 0.5;
                xmax = xmax + dx;
                xmin = xmin - dx;
            }
            else {
                xmax = Math.ceil(xmax + dx);
                xmin = Math.floor(xmin - dx);
            }
        }

        //System.out.println(" --- xmin=" + xmin + "   xmax=" + xmax);
        setLimitsX(xmin, xmax);
    }


    /**
     * Calculate the border values on y-axis, (ymin and ymax)
     */
    public void calcYborderValues() {
        boolean isJmodulated = renderer.getSpectrumModel().isJmodulated();
        double ymin          = dataContainer.getMinIntensity(isJmodulated);
        double ymax          = dataContainer.getMaxIntensity(isJmodulated);
        setLimitsY(ymin, ymax, isJmodulated);
    }


    public void setLimitsY(double yMin, double yMax, boolean isJmodulated) {
        double ymin = yMin;
        double ymax = yMax;
        if ((ymin == 0) && (ymax == 0)) {
            ymin = -1;
            ymax = 1;
        }
        else {
            if (((AbstractDataContainer) dataContainer).is13Cspectrum() ||
                    ((AbstractDataContainer) dataContainer).is1Hspectrum()) {
                // for NMR spectrum force display of zero line:
                double factor = 10;
                ymax = Math.max(ymax, 0);
                ymin = Math.min(ymin, 0);
                double h = Math.max(Math.abs(ymax) / factor, Math.abs(ymin) / factor);
                ymax += h;
                ymin -= h;
            }
            else if (((AbstractDataContainer) dataContainer).isIRspectrum()) {
                // for IR spectrum force display of zero line:
                double factor = 20;
                ymax = Math.max(ymax, 100);
                ymin = Math.min(ymin, 0);
                double h = Math.max(Math.abs(ymax) / factor, Math.abs(ymin) / factor);
                ymax += h;
                ymin -= h;
            }
        }
        setLimitsY(ymin, ymax);
    }


    /**
     * Calculate the border values for y-axis based on visible peaks
     * @param visiblePeaks only the visible peaks
     */
    public void calcYborderValues(NmrPeak[] visiblePeaks) {
        double ymin = Double.MAX_VALUE;
        double ymax = Double.MIN_VALUE;
        double y    = 0;
        if (!renderer.getSpectrumModel().isJmodulated()) {
            for (int i = 0; i < visiblePeaks.length; i++) {
                y = visiblePeaks[i].getIntensity();
                if (y > ymax) {
                    ymax = y;
                }
            }
        }
        else {
            for (int i = 0; i < visiblePeaks.length; i++) {
                y = visiblePeaks[i].getSignedIntensity();
                if (y > ymax) {
                    ymax = y;
                }
                if (y < ymin) {
                    ymin = y;
                }
            }
        }
        setLimitsY(ymin, ymax, renderer.getSpectrumModel().isJmodulated());
    }


    /**
     * Return the step width between two points in the displayed spectrum,
     * (delta(chemical shift) in ppm)
     * @return the step width between two points (in ppm)
     */
    public double getStepWidthX() {
        return stepWidthX;
    }


    /**
     * Set the x range displayed
     * @param minX minimum x value displayed
     * @param maxX maximum x value displayed
     */
    public void setLimitsX(double minX, double maxX) {
        // take care that xMin <= xMax !!!
        double x1 = minX;
        double x2 = maxX;
        if (x1 > x2) {
            x1 = maxX;
            x2 = minX;
        }

        // format: 000.00
        int precission = 2;
        if (dataContainer instanceof RawNmrData) {
            // format: 000.000
            precission = 3;
        }

        // format: 000.00
        xMax = MathUtil.nextGreaterValue(x2, precission);
        xMin = MathUtil.nextSmallerValue(x1, precission);
        // minimal range: 0.01
        if ((xMax - xMin) < Math.pow(10, -precission)) {
            xMax = xMin + Math.pow(10, -precission);
        }
        calcFactors();
    }


    /**
     * Retrun TRUE if the specified peak is visible in displayed spectrum
     * @param p the <CODE>Peak</CODE>
     * @return TRUE if the specified peak is visible
     */
    public boolean isPeakVisible(NmrPeak p) {
        boolean r = false;
        if ((p.getValueX() >= xMin) && (p.getValueX() <= xMax)) {
            r = true;
        }
        return r;
    }


    /**
     * Calculates the values used to display the x-axis scale and grid
     * @param fm the <CODE>FontMetrics</CODE>
     * @return array containing scale info :
     *          [0] first major tick
     *          [1] step width between major ticks
     *          [2] step width between minor ticks
     */
    public double[] searchXgrid(FontMetrics fm) {
        int z     = 0;
        int zz    = stepsX.length - 1;
        double dX = xMax - xMin;
        double h  = dX / stepsX[z];
        double w  = xPointCount / h; // grid-width on JComponent
        while ((z < zz) && (w < 2 + fm.stringWidth(String.valueOf(xMax + stepsX[z])))) {
            z++;
            h = dX / stepsX[z];
            w = xPointCount / h;
        }

        //calc first major tick
        h = MathUtil.nextSmallerValue(xMin, roundX[z]);
        while (h < xMin) {
            h += stepsX[z];
        }
        return new double[] { h, stepsX[z], minorTicksX[z] };
    }


    /**
     * Calculates the values used to display the y-axis scale and grid
     * @param fm the <CODE>FontMetrics</CODE>
     * @return array containing scale info :
     *         [0] step width between major ticks
     */
    public double[] searchYgrid(FontMetrics fm) {
        int z     = 0;
        int zz    = stepsY.length - 1;
        double dY = yMax - yMin;
        double h  = dY / stepsY[z];
        double w  = yPointCount / h; // grid-width on JComponent
        while ((z < zz) && (w < 2 + fm.getHeight())) {
            z++;
            h = dY / stepsY[z];
            w = yPointCount / h;
        }
        return new double[] { stepsY[z] };
    }


    /**
     * Set the initial x range displayed, used e.g. to reset zoom
     * @param minX minimum x value displayed
     * @param maxX maximum x value displayed
     */
    public void setInitialLimitsX(double minX, double maxX) {
        if (minX <= maxX) {
            xMaxInitial = maxX;
            xMinInitial = minX;
        }
        else {
            xMaxInitial = minX;
            xMinInitial = maxX;
        }
    }


    /**
     * Set the y range displayed
     * @param minY minimum y value displayed
     * @param maxY maximum y value displayed
     */
    public void setLimitsY(double minY, double maxY) {
        yMax = maxY;
        yMin = minY;
        calcFactors();
    }


    /**
     * Getter for property xOffsetLeft.
     * @return Value of property xOffset.
     */
    public int getXoffsetLeft() {
        return renderer.getSpectrumModel().getXoffsetLeft();
    }


    /**
     * Getter for property xOffsetRight.
     * @return Value of property xOffset.
     */
    public int getXoffsetRight() {
        return renderer.getSpectrumModel().getXoffsetRight();
    }


    /**
     * Getter for property yOffsetTop.
     * @return Value of property yOffset.
     */
    public int getYoffsetTop() {
        return renderer.getSpectrumModel().getYoffsetTop();
    }


    /**
     * Getter for property xMin.
     * @return Value of property xMin.
     */
    public double getXmin() {
        return xMin;
    }


    /**
     * Getter for property xMax.
     * @return Value of property xMax.
     */
    public double getXmax() {
        return xMax;
    }


    /**
     * Getter for property xMinInitial.
     * @return Value of property xMinInitial.
     */
    public double getXminInitial() {
        return xMinInitial;
    }


    /**
     * Getter for property xMaxInitial.
     * @return Value of property xMaxInitial.
     */
    public double getXmaxInitial() {
        return xMaxInitial;
    }


    /**
     * Getter for property yMin.
     * @return Value of property yMin.
     */
    public double getYmin() {
        return yMin;
    }


    /**
     * Getter for property yMax.
     * @return Value of property yMax.
     */
    public double getYmax() {
        return yMax;
    }


    /**
     * Return the number of points drawn in x-direction
     * @return Value of property xPointCount.
     */
    public int getXPointCount() {
        return xPointCount;
    }


    /**
     * Return the number of points drawn in y-direction
     * @return Value of property yPointCount.
     */
    public int getYPointCount() {
        return yPointCount;
    }


    /**
     * Transform x-value from the function coordinates to component coordinates
     * @param x the x value
     * @return the transformed value
     */
    public double translateX(double x) {
        double r = (((x * factorX) - (xMin * factorX)) * scaleX) + getXoffsetLeft();
        if (inversXaxis) {
            r = (((xMax * factorX) - (x * factorX)) * scaleX) + getXoffsetLeft();
        }
        return r;
    }


    /**
     * Transform x-value from the function coordinates to component coordinates
     * @param x the x value
     * @return the transformed value as int-value (rounded!)
     */
    public int translateX_int(double x) {
        return (int) MathUtil.round(translateX(x), 0);
    }


    /**
     * Transform x-value from component coordinates to function coordinates
     * @param x the x value
     * @return the transformed value
     */
    public double translateMouseX(int x) {
        double r = xMin + (x - getXoffsetLeft()) / (factorX * scaleX);
        if (inversXaxis) {
            r = xMax - (x - getXoffsetLeft()) / (factorX * scaleX);
        }
        return r;
    }


    /**
     * Transform y from the function coordinates to component coordinates
     * @param y the y value
     * @return the transformed value
     */
    public double translateY(double y) {
        double r = (((theSize.height + (yMin * factorY)) - (y * factorY)) * scaleY) + getYoffsetTop();
        return r;
    }


    /**
     * Transform y-value from the function coordinates to component coordinates
     * @param y the y value
     * @return the transformed value as int-value (rounded!)
     */
    public int translateY_int(double y) {
        return (int) MathUtil.round(translateY(y), 0);
    }
}
