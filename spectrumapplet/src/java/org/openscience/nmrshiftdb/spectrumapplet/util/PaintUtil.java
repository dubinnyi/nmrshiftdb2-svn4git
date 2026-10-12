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
 * Created on 9. November 2004
 */
package org.openscience.nmrshiftdb.spectrumapplet.util;

import org.openscience.nmrshiftdb.spectrumapplet.renderer.*;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.nmr.*;

import java.awt.*;

/**
 * Utility to display graphic objects in spectrum
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public class PaintUtil {
    public static final int MARKER_OFFSET = 2;

    /**
     * Draw a vertical line in spectrum
     * @param r the renderer
     * @param x the x postion
     * @param c the color
     */
    public static void drawVerticalLine(NmrSpecRenderer r, int x, Color c) {
        Graphics2D g2 = (Graphics2D) r.getGraphics();
        drawVerticalLine(g2, x, r.getMetrics().getYoffsetTop(),
                         r.getMetrics().getSize().height - r.getMetrics().getYoffsetTop(), c);
        g2.dispose();
    }


    /**
     * Draw a vertical line in spectrum
     * @param g2 the graphics context
     * @param m the renderer metrics
     * @param x the x postion
     * @param c the color
     */
    public static void drawVerticalLine(Graphics2D g2, Metrics m, int x, Color c) {
        drawVerticalLine(g2, x, m.getYoffsetTop(), m.getSize().height - m.getYoffsetTop(), c);
    }


    /**
     * Draw a vertical line in spectrum
     * @param g2 the graphics context
     * @param y0 start value y
     * @param y1 end value y
     * @param x the x postion
     * @param c the color
     */
    public static void drawVerticalLine(Graphics2D g2, int x, int y0, int y1, Color c) {
        g2.setColor(c);
        g2.drawLine(x, y0, x, y1);
    }


    /**
     * Draw a marker specifying a range in spectrum, e.g. used for zooming
     * @param r the renderer
     * @param xStart the start value for the marker
     * @param xEnd the end value for the marker, if the value is not in
     *             spectrum display range this value is set to xMin or xMax
     */
    public static void drawMarker(NmrSpecRenderer r, int xStart, int xEnd) {
        Graphics2D g2 = (Graphics2D) r.getGraphics();
        drawMarker(g2, xStart, xEnd, r.getMetrics().getYoffsetTop() - MARKER_OFFSET);
        g2.dispose();
    }


    /**
     * Draw a marker specifying a range in spectrum, e.g. used for zooming
     * @param startX minimum value of the range to mark
     * @param endX maximum value of the range to mark
     * @param g the graphics context
     * @param yPos the bottom y position of the marker
     */
    public static void drawMarker(Graphics2D g, int startX, int endX, int yPos) {
        g.setColor(Color.black);
        Polygon p = new Polygon();
        p.addPoint(startX, yPos - 9);
        p.addPoint(startX, yPos - 7);
        p.addPoint(endX, yPos - 7);
        p.addPoint(endX, yPos - 9);
        g.draw(p);
        Polygon p1 = new Polygon();
        p1.addPoint(startX, yPos);
        p1.addPoint(startX - 4, yPos - 7);
        p1.addPoint(startX + 4, yPos - 7);
        Polygon p2 = new Polygon();
        p2.addPoint(endX, yPos);
        p2.addPoint(endX - 4, yPos - 7);
        p2.addPoint(endX + 4, yPos - 7);
        g.setColor(Color.red);
        g.draw(p1);
        g.fill(p1);
        g.draw(p2);
        g.fill(p2);
    }


    /**
     * Delete a marker specifying a range in spectrum, e.g. used for zooming
     * @param r the renderer
     * @param xStart the start value for the marker
     * @param xEnd the end value for the marker, if the value is not in
     *             spectrum display range this value is set to xMin or xMax
     */
    public static void deleteMarker(NmrSpecRenderer r, int xStart, int xEnd) {
        deleteMarker(r, xStart, xEnd, r.getMetrics().getYoffsetTop() - MARKER_OFFSET);
    }


    /**
     * Delete a marker specifying a range in spectrum, e.g. used for zooming
     * @param r the renderer
     * @param startX the start value for the marker
     * @param endX the end value for the marker
     * @param yPos the bottom y position of the marker
     */
    private static void deleteMarker(NmrSpecRenderer r, int startX, int endX, int yPos) {
        Graphics2D g = (Graphics2D) r.getGraphics();
        deleteMarker(g, startX, endX, yPos, r.getSpectrumModel().getBackGround());
        g.dispose();
    }


    /**
     * Delete a marker specifying a range in spectrum, e.g. used for zooming
     * @param g the graphics context
     * @param startX the start value for the marker
     * @param endX the end value for the marker
     * @param yPos the bottom y position of the marker
     */
    public static void deleteMarker(Graphics2D g, int startX, int endX, int yPos, Color background) {
        Polygon p1 = new Polygon();
        p1.addPoint(startX, yPos);
        p1.addPoint(startX - 4, yPos - 7);
        p1.addPoint(startX + 4, yPos - 7);
        Polygon p2 = new Polygon();
        p2.addPoint(endX, yPos);
        p2.addPoint(endX - 4, yPos - 7);
        p2.addPoint(endX + 4, yPos - 7);
        Polygon p = new Polygon();
        p.addPoint(startX, yPos - 9);
        p.addPoint(startX, yPos - 7);
        p.addPoint(endX, yPos - 7);
        p.addPoint(endX, yPos - 9);
        g.setColor(background);
        if (startX <= endX) {
            g.fillRect(startX - 4, yPos - 9, endX - startX + 9, 10);
        }
        else {
            g.fillRect(endX - 4, yPos - 9, startX - endX + 9, 10);
        }
    }
}
