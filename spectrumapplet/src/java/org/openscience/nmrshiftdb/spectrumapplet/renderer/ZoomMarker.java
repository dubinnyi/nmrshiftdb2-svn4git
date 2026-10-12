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
 * Created on 21. November 2004
 */
package org.openscience.nmrshiftdb.spectrumapplet.renderer;

import org.openscience.nmrshiftdb.spectrumapplet.renderer.nmr.*;
import org.openscience.nmrshiftdb.spectrumapplet.util.*;

import java.awt.*;

/**
 * Handles the zoom parameters of a spectrum and
 * displays/deletes the marker if necessary
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public class ZoomMarker {
    private NmrSpecRenderer renderer;

    // unvalidated values:
    private int startValue;
    private int endValue;
    private double startValueD;
    private double endValueD;

    // backups
    private double startValueB;
    private double endValueB;

    // validated values:
    // spectrum values
    private double minX;
    private double maxX;

    // x-Values in screen coordinates
    private int xMin;
    private int xMax;

    // previous x-Values in screen coordinates
    private int xMinPrev;
    private int xMaxPrev;
    private boolean hasPrevious     = false;
    private boolean isMouseAction   = false;
    private boolean isNonMouseRange = false;

    /**
     * Creates a new instance of ZoomMarker
     * @param r the spectrum renderer
     */
    public ZoomMarker(NmrSpecRenderer r) {
        renderer = r;
    }

    public void backUpValues() {
        startValueB = startValueD;
        endValueB   = endValueD;
    }


    public void restoreBackup() {
        initializeRange(startValueB, endValueB);
    }


    //**********************************************
    //  methods using Graphics2D coordinate system
    //**********************************************
    /**
     * Set min/max x-values (using Graphics2D coordinate system)
     * @param min minimum x-value
     * @param max maximum x-value
     */
    public void initializeGraphicsRange(int min, int max) {
        startValue      = min;
        endValue        = max;
        isMouseAction   = true;
        isNonMouseRange = false;
    }


    /**
     * Set start point of a mouse action, e.g zooming
     * @param mouseX mouse position
     */
    public void initGraphicsRange(int mouseX) {
        //initilization of the marker
        isMouseAction   = true;
        isNonMouseRange = false;
        startValue      = limitGraphics2dX(mouseX);
        endValue        = startValue;
    }


    /**
     * Check if specified range is the actual zoom range set for this marker
     * @param start min x-value
     * @param end max x-value
     * @return TRUE if ranges are identical
     */
    public boolean isActualZoomRange(double start, double end) {
        boolean toReturn = false;
        validateTheRanges();
        int x1 = renderer.getMetrics().translateX_int(start);
        int x2 = renderer.getMetrics().translateX_int(end);
        if (x1 > x2) {
            int h = x1;
            x1 = x2;
            x2 = h;
        }
        if ((Math.abs(x1 - xMin) <= 1) && (Math.abs(x2 - xMax) <= 1)) {
            toReturn = true;
        }
        return toReturn;
    }


    /**
     * Limits the allowed x-value to the spectrum range
     * @param xValue the x-value to test
     * @return the limited value
     */
    public int limitGraphics2dX(int xValue) {
        int toReturn = xValue;
        int right    = renderer.getMetrics().translateX_int(renderer.getMetrics().getXmin());
        int left     = renderer.getMetrics().translateX_int(renderer.getMetrics().getXmax());

        // set limits to spectrum range
        if (xValue <= left) {
            toReturn = left + 1;
        }
        else if (xValue >= right) {
            toReturn = right - 1;
        }

        //System.out.println("x:"+xValue+"  left:"+left+ "  right:"+right+"   toReturn:"+toReturn);
        return toReturn;
    }


    /**
     * Set actual mouse position during expanding the zoom marker
     * @param mouseX mouse position
     */
    public void setDraggedLimit(int mouseX) {
        isMouseAction   = true;
        isNonMouseRange = false;
        endValue        = limitGraphics2dX(mouseX);
    }


    /**
     * Tanslates the marker in horizontal direction until border limit is reached,
     * range size is NOT changed
     * @param deltaX horizontal translation (in points)
     */
    public void translate(int deltaX) {
        int d   = xMax - xMin;
        int min;
        int max;
        if (deltaX < 0) {
            // translate left
            min = limitGraphics2dX(xMin + deltaX);
            max = min + d;
        }
        else {
            // translate right
            max = limitGraphics2dX(xMax + deltaX);
            min = max - d;
        }
        initializeGraphicsRange(min, max);
    }


    /**
     * uses Graphics2D coordinates
     */
    private void validateGraphicsRange(int min, int max) {
        if (hasPrevious) {
            xMinPrev = xMin;
            xMaxPrev = xMax;
        }
        hasPrevious = true;
        if (min > max) {
            xMin = max;
            xMax = min;
        }
        else {
            xMin = min;
            xMax = max;
        }
        double x1 = renderer.getMetrics().translateMouseX(xMin);
        double x2 = renderer.getMetrics().translateMouseX(xMax);
        if (x1 > x2) {
            minX = x2;
            maxX = x1;
        }
        else {
            minX = x1;
            maxX = x2;
        }
    }


    //**************************************************
    //  methods using spectrum values coordinate system
    //**************************************************
    /**
     * Set min/max x-values (using chemical shift values)
     * @param min minimum x-value
     * @param max maximum x-value
     */
    public void initializeRange(double min, double max) {
        startValueD     = min;
        endValueD       = max;
        isNonMouseRange = true;
        isMouseAction   = false;
    }


    /**
     * uses spectrum values,, limits managed
     */
    private void validateRange(double min, double max) {
        if (hasPrevious) {
            xMinPrev = xMin;
            xMaxPrev = xMax;
        }
        hasPrevious = true;
        if (min > max) {
            minX = max;
            maxX = min;
        }
        else {
            minX = min;
            maxX = max;
        }

        // set limits to spectrum range
        if (minX < renderer.getMetrics().getXmin()) {
            minX = renderer.getMetrics().getXmin();
        }
        if (maxX > renderer.getMetrics().getXmax()) {
            maxX = renderer.getMetrics().getXmax();
        }
        int x1 = renderer.getMetrics().translateX_int(minX);
        int x2 = renderer.getMetrics().translateX_int(maxX);
        if (x1 > x2) {
            xMin = x2;
            xMax = x1;
        }
        else {
            xMin = x1;
            xMax = x2;
        }
    }


    /**
     * Return the actual zoom range of this marker (using chemical shift values)
     * @return array : double[] { minX, maxX }
     */
    public double[] getRange() {
        validateTheRanges();
        return new double[] { minX, maxX };
    }


    /**
     * Return the actual zoom range of this marker (using Graphics2D coordinate system)
     * @return array : int[] { minX, maxX }
     */
    public int[] getGraphicsRange() {
        validateTheRanges();
        return new int[] { xMin, xMax };
    }


    /**
     * Paint this marker
     * @param deletePreviousMarker if TRUE the last drawn version of the marker will be deleted first
     */
    public void paintMarker(boolean deletePreviousMarker) {
        Graphics2D g2 = (Graphics2D) renderer.getGraphics();
        paintMarker(g2, deletePreviousMarker);
        g2.dispose();
    }


    private void validateTheRanges() {
        if (isMouseAction) {
            validateGraphicsRange(startValue, endValue);
            isMouseAction = false;
        }
        if (isNonMouseRange) {
            validateRange(startValueD, endValueD);
            isNonMouseRange = false;
        }
    }


    /**
     * Paint this marker
     * @param g2 the Graphics context
     * @param deletePreviousMarker if TRUE the last drawn version of the marker will be deleted first
     */
    public void paintMarker(Graphics2D g2, boolean deletePreviousMarker) {
        if (g2 != null) {
            validateTheRanges();
            int y = renderer.getMetrics().getYoffsetTop() - PaintUtil.MARKER_OFFSET;
            if (hasPrevious && deletePreviousMarker) {
                // delete previous marker   
                PaintUtil.deleteMarker(g2, xMinPrev, xMaxPrev, y, renderer.getSpectrumModel().getBackGround());
            }

            // paint marker
            PaintUtil.drawMarker(g2, xMin, xMax, y);
        }
    }


    /**
     * Delete this marker
     */
    public void deleteMarker() {
        Graphics2D g2 = (Graphics2D) renderer.getGraphics();
        deleteMarker(g2);
        g2.dispose();
    }


    /**
     * Delete this marker
     * @param g2 the Graphics context
     */
    public void deleteMarker(Graphics2D g2) {
        int y = renderer.getMetrics().getYoffsetTop() - PaintUtil.MARKER_OFFSET;
        PaintUtil.deleteMarker(g2, xMin, xMax, y, renderer.getSpectrumModel().getBackGround());
    }
}
