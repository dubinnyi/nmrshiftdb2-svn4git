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
package org.openscience.nmrshiftdb.spectrumapplet.renderer.ir;

import org.openscience.nmrshiftdb.spectrumapplet.data.*;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.*;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.dialog.*;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.nmr.*;

import java.awt.*;
import java.awt.event.*;

/**
 * Used to display an IR spectrum in detail
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public class IrRawDataDetailSpectrum extends NmrRawDataDetailSpectrum {
    private double yZoomFactor;

    /**
     * Creates a DetailSpectrum
     * @param model the corresponding model
     * @param l list of peaks
     */
    public IrRawDataDetailSpectrum(SpectrumModel model, AbstractDataContainer l) {
        super(model, l,false,false);
        init();
    }

    /**
     * Init spectrum
     */
    private void init() {
        yZoomFactor = 1;
        help        = new HelpDialog(this);
        model.setXTitle("[1/cm]");
        titlePainter.updateData(new String[] { model.getTitle(), model.getXTitle(), model.getYTitle() });
        //infoPainter.updateData(null);
    }


    /**
     * Update a status information text at the bottom of the spectrum
     * @param g2 the Graphics context
     */
    protected void printStatusInfo(Graphics2D g2) {
        // no status displayed
    }


    // ******************************
    // *                            *
    // *    Mouse action stuff      *
    // *                            *
    // ******************************
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
        Object data = new double[] { x, xx, 1, 0 };
        distancePainter.updateData(data);
        updateView(true);
    }


    /**
     * Invoked when left mouse button pressed and mouse moved
     * @param mouseX mose position x
     * @param mouseY mose position y
     */
    public void leftDragged(int mouseX, int mouseY) {
        leftPressed(mouseX, mouseY);
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
}
