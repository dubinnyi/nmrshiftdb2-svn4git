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
 * Created on 2. November 2004
 */
package org.openscience.nmrshiftdb.spectrumapplet.renderer.nmr;

import org.openscience.nmrshiftdb.spectrumapplet.data.*;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.*;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.dialog.*;

import java.awt.*;
import java.awt.event.*;

/**
 * Used to display a nmr spectrum in detail
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public class NmrRawDataDetailSpectrum extends AbstractRenderer {
    private double yZoomFactor;
    boolean isgraphic = false;
    
    /**
     * Creates a DetailSpectrum
     * @param model the corresponding model
     * @param l list of peaks
     * @param isgraphic flag used to paint graphic (e.g. for images)
     * @param peakpicker Shall the applet be used as a peak selection tool (see peakpicker.html in demo)?
     */
    public NmrRawDataDetailSpectrum(SpectrumModel model, AbstractDataContainer l, boolean isgraphic, boolean peakpicker) {
        super(model, l, peakpicker);
        init();
        this.isgraphic = isgraphic;
    }

    /**
     * Init spectrum
     */
    private void init() {
        yZoomFactor = 1;
        help        = new HelpDialog(this);
        titlePainter.updateData(new String[] { model.getTitle(), model.getXTitle(), model.getYTitle() });
    }


    /**
     * Executes a recalculation of display data
     */
    public void doRecalculation() {
        if (forceRecalculation) {
            forceRecalculation = false;
            metrics.calcYborderValues();
            // take care to update update marker values
            updateMarker();
            setIgnoreZoomOps(false);
            help.setVisible(false);
            calculatePoints();
        }
    }


    /**
     * Calculates all points displayed in spectrum
     */
    protected void calculatePoints() {
        Object[] data = ((XYData) getDataContainer()).getValuesBetween(metrics.getXmin(), metrics.getXmax(),
                                                                       metrics, yZoomFactor);
        getCurvePainter().updateData(data);
    }


    /**
     * Draw the component
     * @param g2 the Grahics2D context
     */
    protected void draw(Graphics2D g2) {
        if (g2 != null) {
            g2.setBackground(model.getBackGround());
            g2.setFont(model.getTheFont());
            // g2.clearRect(0, 0, metrics.getSize().width, metrics.getSize().height);
            // turn antialiasing on
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            fontMetrics = g2.getFontMetrics();
            g2.setColor(Color.black);
            titlePainter.paintComponent(g2);
            // draw axis
            yGrid.paintComponent(g2);
            yAxis.paintComponent(g2);
            xGrid.paintComponent(g2);
            xAxis.paintComponent(g2);
            // draw spectrum
            getCurvePainter().paintComponent(g2);
            printStatusInfo(g2);
            // use PLAIN font type for dialogs and buttons
            Font f = g2.getFont();
            g2.setFont(new Font(f.getName(), f.PLAIN, f.getSize()));
            if(!isgraphic) {
              drawButtons(g2);
            }
            if (help.isVisble()) {
                help.paintDialog(g2);
            }
            distancePainter.paintComponent(g2);
            // if (peakUnderCursor != null) {
            //     infoPainter.updateData(peakUnderCursor.getInfo());
            // }
            getInfoPainter().paintComponent(g2);
        }
    }


    private void drawButtons(Graphics2D g2) {
        help.paintDialogButton(g2);
    }


    // ******************************
    // *                            *
    // *    Mouse action stuff      *
    // *                            *
    // ******************************
    /**
     * Checks if painted dialog buttons/checkboxes etc. are clicked by mouse
     * @param x x value of mouse position
     * @param y y value of mouse position
     */
    public void checkDialogActions(int x, int y) {
        if (help.isVisble()) {
            if (help.closeButtonClicked(x, y)) {
                setIgnoreZoomOps(false);
                help.setVisible(false);
                updateView(false);
            }
        }
        else {
            // check if a button is clicked by mouse
            if (help.openButtonClicked(x, y)) {
                setIgnoreZoomOps(true);
                Graphics2D g2 = (Graphics2D) getGraphics();
                getInfoPainter().updateData(null);
                getInfoPainter().paintComponent(g2);
                help.paintDialog(g2);
                g2.dispose();
            }
        }
    }


    /**
     * Invoked when a key has been typed.
     * @param e the KeyEvent
     */
    public void keyPressed(KeyEvent e) {
        char c = e.getKeyChar();
        if (c == 'n') {
            // toggle navigation spectrum display 
            if (getControler() != null) {
                model.setShowNavigationView(!model.isShowNavigationView());
                getControler().setOptions(new boolean[] { model.isShowNavigationView() });
            }
        }
        else if (c == '/') {
            // decrease yZoomFactor 
            yZoomFactor = yZoomFactor / 2;
        }
        else if (c == '*') {
            // increase yZoomFactor 
            yZoomFactor = yZoomFactor * 2;
        }

        // update view:
        forceRecalculation();
        updateView(true);
        //else if (c=='#' ){
        //    System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
        //}
    }


    /**
     * Invoked when left mouse button pressed
     * @param mouseX mose position x
     * @param mouseY mose position y
     */
    public void leftPressed(int mouseX, int mouseY) {
        int x       = marker.limitGraphics2dX(mouseX);
        double xx   = metrics.translateMouseX(x);
        Object data = new double[] {
                          x, xx, getDataContainer().getNumberProperty(getDataContainer().FREQ),
                          metrics.translateY_int(0)
                      };
        distancePainter.updateData(data);
        updateView(true);
    }


    /**
     * Invoked when left mouse button pressed and mouse moved
     * @param mouseX mose position x
     * @param mouseY mose position y
     */
    public void leftDragged(int mouseX, int mouseY) {
        int x = marker.limitGraphics2dX(mouseX);
        if ((x - metrics.getXoffsetLeft() - 1) < 0) {
            x = 0;
        }
        distancePainter.updateData(new Integer(x));
        updateView(false);
    }


    /**
     * Invoked when left mouse button released
     * @param mouseX mose position x
     * @param mouseY mose position y
     */
    public void leftReleased(int mouseX, int mouseY) {
        distancePainter.updateData(null);
        getInfoPainter().updateData(null);
        updateView(false);
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
            forceRecalculation = updateZoomData(startX, endX);
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
        yZoomFactor = 1;
        if (Math.abs(startX - endX) > 0.001) {
            r = true;
            metrics.setLimitsX(startX, endX);
            marker.initializeRange(startX, endX);
        }
        return r;
    }


    /**
     * Reset zoom, display the initial range of the spectrum
     */
    public void resetZoom() {
        zoom(metrics.getXminInitial(), metrics.getXmaxInitial());
    }
}
