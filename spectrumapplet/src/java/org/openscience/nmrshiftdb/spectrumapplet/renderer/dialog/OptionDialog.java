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
package org.openscience.nmrshiftdb.spectrumapplet.renderer.dialog;

import org.openscience.nmrshiftdb.spectrumapplet.renderer.*;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.nmr.*;

import java.awt.*;

/**
 * Provides the standard option dialog for <code>DetailSpectrum</code>
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public class OptionDialog extends AbstractDialog {
    private SpectrumModel model;
    private Rectangle dialog;
    private Rectangle lineTyp;
    private Rectangle modulation;
    private Rectangle resonance;
    private Rectangle integral;
    private Rectangle optionCloseButton;
    private Rectangle optionButton;
    private boolean spinEchoChanged;
    protected boolean is13Ctype;

    /**
     * Creates a new instance of OptionDialog
     * @param r the spectrum renderer
     */
    public OptionDialog(SpectrumRenderer r, boolean is13Cspectrum) {
        super(r);
        model           = r.getSpectrumModel();
        spinEchoChanged = false;
        is13Ctype       = is13Cspectrum;
    }

    /**
     * Paints the dialog
     * @param g2 the <code>Graphics2D</code> context
     */
    public void paintDialog(Graphics2D g2) {
        setVisible(true);
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        int fh = g2.getFontMetrics().getHeight();
        int w  = 180;
        int h  = 120;
        int x  = getXoffset();
        int y  = getRenderer().getMetrics().getYoffsetTop() - 5;
        y      = getRenderer().getMetrics().getYoffsetTop() - 24 - fh;
        g2.setColor(model.getBackGround());
        g2.fillRect(x - 5, y, w + 14, h + 5);
        g2.setColor(new Color(222, 222, 222));
        g2.fillRect(x + 1, y + 1, w - 1, h - 1);
        dialog = new Rectangle(x + 1, y + 1, w - 1, h - 1);
        g2.setColor(Color.black);
        g2.drawRect(x, y, w, h);
        y = y + 8 + fh;
        x += 30;
        g2.drawString("Show Realistic Lines", x, y);
        lineTyp = new Rectangle(x - 20, y - 10, 10, 10);
        paintCheckBox(g2, lineTyp, (model.getDisplayMode() != model.HISTO));
        y += 4 + fh;
        //g2.drawString("J-Modulation ('Spin-Echo')", x, y);
        g2.drawString("J-Modulation ", x, y);
        modulation = new Rectangle(x - 20, y - 10, 10, 10);
        paintCheckBox(g2, modulation, model.isJmodulated());
        y += 4 + fh;
        if (is13Ctype) {
            g2.drawString("Off-Resonance", x, y);
        }
        else {
            g2.drawString("Show Couplings", x, y);
        }
        resonance = new Rectangle(x - 20, y - 10, 10, 10);
        paintCheckBox(g2, resonance, model.isShowCoupling());
        y += 4 + fh;
        g2.drawString("Show Integral", x, y);
        integral = new Rectangle(x - 20, y - 10, 10, 10);
        paintCheckBox(g2, integral, model.isShowIntegral());
        String s = "Ok";
        w = g2.getFontMetrics().stringWidth(s) + 20;
        x += 45;
        y += 10;
        optionCloseButton = new Rectangle(x, y, w, fh + 4 + 4);
        paintButton(g2, optionCloseButton, s);
    }


    /**
     * Paints the button that starts the dialog
     * @param g2 the <code>Graphics2D</code> context
     */
    public void paintDialogButton(Graphics2D g2) {
        g2.setColor(Color.black);
        //paintButton(g2,xpad,ypad-15,30,15,"Options");
        String s     = "Options";
        int h        = g2.getFontMetrics().getHeight() - 1;
        int w        = g2.getFontMetrics().stringWidth(s) + 10;
        int xPos     = getXoffset();
        int yPos     = getRenderer().getMetrics().getYoffsetTop() - 23 - h;
        optionButton = new Rectangle(xPos, yPos, w, h + 4 + 4);
        paintButton(g2, optionButton, s);
    }


    /**
     * Check if the button that opens the dialog is clicked by mouse
     * @param mouseX x position of mouse
     * @param mouseY y position of mouse
     * @return TRUE if the mouse is clicked in the area of the open button
     */
    public boolean openButtonClicked(int mouseX, int mouseY) {
        return optionButton.contains(mouseX, mouseY);
    }


    /**
     * Check if the button that closes the dialog is clicked by mouse
     * @param mouseX x position of mouse
     * @param mouseY y position of mouse
     * @return TRUE if the mouse is clicked in the area of the close button
     */
    public boolean closeButtonClicked(int mouseX, int mouseY) {
        return optionCloseButton.contains(mouseX, mouseY);
    }


    /**
     * Check if one of the options is clicked by mouse,
     * changes the corresponding check box
     * @param mouseX x position of the mouse
     * @param mouseY y position of the mouse
     */
    public void optionsSelected(int mouseX, int mouseY) {
        if (dialog.contains(mouseX, mouseY)) {
            Graphics2D g2 = getGraphics2D();
            if ((lineTyp.y <= mouseY) && (mouseY <= (lineTyp.y + lineTyp.height))) {
                boolean b = ((NmrDetailSpectrum) getRenderer()).toggleDisplayMode();
                paintCheckBox(g2, lineTyp, b);
                spinEchoChanged = true;
            }
            else if ((modulation.y <= mouseY) && (mouseY <= (modulation.y + modulation.height))) {
                model.setIsJmodulated(!model.isJmodulated());
                paintCheckBox(g2, modulation, model.isJmodulated());
                spinEchoChanged = true;
            }
            else if ((resonance.y <= mouseY) && (mouseY <= (resonance.y + resonance.height))) {
                model.setShowCoupling(!model.isShowCoupling());
                paintCheckBox(g2, resonance, model.isShowCoupling());
            }
            else if ((integral.y <= mouseY) && (mouseY <= (integral.y + integral.height))) {
                model.setShowIntegral(!model.isShowIntegral());
                paintCheckBox(g2, integral, model.isShowIntegral());
            }
            g2.dispose();
        }
    }


    /**
     * Return TRUE if a repaint of parent component/spectrum is necessary
     * @return TRUE if a repaint is necessary
     */
    public boolean repaintNecessary() {
        boolean r = spinEchoChanged;
        spinEchoChanged = false;
        return r;
    }
}
