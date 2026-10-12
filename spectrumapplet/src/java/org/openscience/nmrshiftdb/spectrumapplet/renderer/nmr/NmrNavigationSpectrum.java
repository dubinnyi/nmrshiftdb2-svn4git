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
 * Created on 15. November 2004
 */
package org.openscience.nmrshiftdb.spectrumapplet.renderer.nmr;

import org.openscience.nmrshiftdb.spectrumapplet.data.*;
import org.openscience.nmrshiftdb.spectrumapplet.data.nmr.*;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.*;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.action.*;

import java.awt.*;

/**
 * Provides a spectrum with navigation functionality:
 *   - the zoomed range is highlighted
 *   - dragging with left mouse button translates zoom range
 *   - the minimum hight is limited to 100 points
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public class NmrNavigationSpectrum extends AbstractRenderer {

	public SpectrumNavigation specNav=null;

    /**
     * Creates a NavigationSpectrum
     * @param model the corresponding model
     * @param l list of peaks
     */
    public NmrNavigationSpectrum(SpectrumModel model, AbstractDataContainer l, SpectrumNavigation sn) {
        super(model, l,false);
        init(l,false);
        this.specNav=sn;
    }

    /**
     * Creates a NavigationSpectrum
     * @param model the corresponding model
     * @param l list of peaks
     */
    public NmrNavigationSpectrum(SpectrumModel model, AbstractDataContainer l) {
        super(model, l,false);
        init(l,false);
        this.specNav=null;
    }
    
    
    /**
     * Init spectrum
     * 
     * @param data list of peaks
     * @param peakpicker Shall the applet be used as a peak selection tool (see peakpicker.html in demo)?
     */
    protected void init(AbstractDataContainer data, boolean peakpicker) {
        double min = data.getXmin();
        double max = data.getXmax();
        if (data.is13Cspectrum()) {
            if (min < 5) {
                min = min - 10;
            }
            else {
                min = 0;
            }
            if (max > 250) {
                max += 10;
            }
            else {
                max = 250;
            }
        }
        else {
            if (min < 0.5) {
                min = min - 1;
            }
            else {
                min = 0;
            }
            if (max > 15) {
                max += 1;
            }
            else {
                max = 15;
            }
        }
        metrics.calcBorderValues();
        metrics.setLimitsX(min, max);
        metrics.setInitialLimitsX(min, max);
        setMouseActionManager(new NavSpectrumMouseActions(this));
    }


    /**
     * Return visible peaks displayed in spectrum, may be overwritten by extended classes
     * @param minShift the minimum chemical shift
     * @param maxShift the maximum chemical shift
     * @return array of visible peaks
     */
    protected NmrPeak[] getVisiblePeaks(double minShift, double maxShift) {
        // overwrite super().getVisiblePeaks():  NO solvent peaks displayed in navigation view !!
        return ((NmrData) getDataContainer()).getPeaksBetween(minShift, maxShift, model.isShowCoupling(),
                                                              false);
    }


    /**
      * Executes a recalculation of display data
      */
    public void doRecalculation() {
        if (forceRecalculation) {
            forceRecalculation = false;
            // take care to update update marker values
            updateMarker();
            setIgnoreZoomOps(false);
            calculatePoints();
        }
    }


    /**
     * Draw the component
     * @param g2 the Grahics2D context
     */
    protected void draw(Graphics2D g2) {
        g2.setBackground(model.getBackGround());
        g2.setFont(model.getTheFont());
        g2.clearRect(0, 0, metrics.getSize().width, metrics.getSize().height);
        // draw zoom range marker if available
        marker.paintMarker(g2, false);
        if (isVisible()) {
            // drawing ONLY necessary if component is visible  !!
            highlightZoomedArea(g2);
            // turn antialiasing on
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            fontMetrics = g2.getFontMetrics();
            g2.setColor(Color.black);
            // Draw x title
            //drawXtitle(g2);
            // draw axis
            yGrid.paintComponent(g2);
            yAxis.paintComponent(g2);
            xGrid.paintComponent(g2);
            xAxis.paintComponent(g2);
            // draw spectrum
            getCurvePainter().paintComponent(g2);
        }
    }


    private void highlightZoomedArea(Graphics2D g2) {
        int[] zoomRange = marker.getGraphicsRange();
        g2.setColor(model.getBackGroundHighlighted());
        int y = metrics.translateY_int(metrics.getYmin());
        g2.fillRect(zoomRange[0], metrics.getYoffsetTop(), zoomRange[1] - zoomRange[0],
                    y - metrics.getYoffsetTop());
    }


    /**
     * Zooms the displayed range of the spectrum
     * @param startX minimum x value displayed
     * @param endX maximum x value displayed
     */
    public void zoom(double startX, double endX) {
        if (getControler() != null) {
            getControler().zoom(this, startX, endX);
        }
        else {
            updateZoomData(startX, endX);
        }
        updateView(true);
    }


    /**
     * Updates the displayed range of the spectrum
     * @param startX minimum x value displayed
     * @param endX maximum x value displayed
     * @return TRUE if data updated
     */
    public boolean updateZoomData(double startX, double endX) {
        boolean r = false;
        if (Math.abs(startX - endX) > 0.001) {
            r = true;
            marker.initializeRange(startX, endX);
        }
        else {
            marker.restoreBackup();
        }
        return r;
    }
}
