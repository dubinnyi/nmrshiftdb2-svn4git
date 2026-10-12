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
 * Provides an extended option dialog for <code>DetailSpectrum</code> used
 * in <code>SpectrumNavigation</code>
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public class OptionDialogExt extends OptionDialog {
    private int width;
    private SpectrumModel model;
    private Rectangle dialog;
    private Rectangle lineTyp;
    private Rectangle modulation;
    private Rectangle resonance;
    private Rectangle integral;
    private Rectangle optionCloseButton;
    private Rectangle optionButton;
    private Rectangle navigation;
    private Rectangle autoIntensity;
    private Rectangle effect;
    private boolean spinEchoChanged;
    private boolean autoIntensChanged;

    /** Creates a new instance of extended OptionDialog
     * @param r the spectrum renderer
     */
    public OptionDialogExt(SpectrumRenderer r, boolean is13Cspectrum) {
        super(r, is13Cspectrum);
        model             = r.getSpectrumModel();
        spinEchoChanged   = false;
        autoIntensChanged = false;
    }

    /**
     * Paints the dialog
     * @param g2 the <code>Graphics2D</code> context
     */
    public void paintDialog(Graphics2D g2) {
        setVisible(true);
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        int fh = g2.getFontMetrics().getHeight();
        width = 320;
        int wh = width / 2;
        int h  = 120;
        int x  = getXoffset();
        int y  = getRenderer().getMetrics().getYoffsetTop() - 5;
        y      = getRenderer().getMetrics().getYoffsetTop() - 24 - fh;
        g2.setColor(model.getBackGround());
        g2.fillRect(x - 5, y, width + 14, h + 5);
        g2.setColor(new Color(222, 222, 222));
        g2.fillRect(x + 1, y + 1, width - 1, h - 1);
        dialog = new Rectangle(x + 1, y + 1, width - 1, h - 1);
        g2.setColor(Color.black);
        g2.drawRect(x, y, width, h);
        y = y + 8 + fh;
        x += 30;
        g2.drawString("Show Realistic Lines", x, y);
        lineTyp = new Rectangle(x - 20, y - 10, 10, 10);
        paintCheckBox(g2, lineTyp, (model.getDisplayMode() != model.HISTO));
        g2.drawString("Show Navigation View", x + wh, y);
        navigation = new Rectangle(x - 20 + wh, y - 10, 10, 10);
        paintCheckBox(g2, navigation, model.isShowNavigationView());
        y += 4 + fh;
        //g2.drawString("J-Modulation ('Spin-Echo')", x, y);
        if (is13Ctype) {
            g2.drawString("J-Modulation ", x, y);
        }
        else {
            g2.drawString("Peak phase", x, y);
        }
        modulation = new Rectangle(x - 20, y - 10, 10, 10);
        paintCheckBox(g2, modulation, model.isJmodulated());
        g2.drawString("Auto Intensity", x + wh, y);
        autoIntensity = new Rectangle(x - 20 + wh, y - 10, 10, 10);
        if (model.getDisplayMode() == model.HISTO) {
            paintCheckBox(g2, autoIntensity, (model.isAutoIntensity()));
        }
        else {
            model.setAutoIntensity(true);
            paintDisabledCheckBox(g2, autoIntensity, (model.isAutoIntensity()));
        }
        y += 4 + fh;
        if (is13Ctype) {
            g2.drawString("Off-Resonance", x, y);
        }
        else {
            g2.drawString("Show Couplings", x, y);
        }
        resonance = new Rectangle(x - 20, y - 10, 10, 10);
        paintCheckBox(g2, resonance, model.isShowCoupling());
        effect = new Rectangle(x - 20 + wh, y - 10, 10, 10);
        if (!is13Ctype) {
            g2.drawString("Enable 'roof effect'", x + wh, y);
            paintCheckBox(g2, effect, model.isRoofEffectEnabled());
        }
        else {
            g2.drawString("Show colored peaks", x + wh, y);
            updateColoredOption(g2);
        }
        y += 4 + fh;
        g2.drawString("Show Integral", x, y);
        integral = new Rectangle(x - 20, y - 10, 10, 10);
        paintCheckBox(g2, integral, model.isShowIntegral());
        x = wh;
        String s = "Ok";
        wh = g2.getFontMetrics().stringWidth(s) + 20;
        //x += 45;
        y += 10;
        optionCloseButton = new Rectangle(x, y, wh, fh + 4 + 4);
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
                if (mouseX < width / 2) {
                    boolean isAutoIntensity = model.isAutoIntensity();
                    boolean b               = ((NmrDetailSpectrum) getRenderer()).toggleDisplayMode();
                    if (b) {
                        paintCheckBox(g2, lineTyp, true);
                        if (!isAutoIntensity) {
                            autoIntensChanged = !autoIntensChanged;
                        }
                        paintDisabledCheckBox(g2, autoIntensity, (model.isAutoIntensity()));
                    }
                    else {
                        paintCheckBox(g2, lineTyp, false);
                        paintCheckBox(g2, autoIntensity, (model.isAutoIntensity()));
                    }
                    if (is13Ctype) {
                        updateColoredOption(g2);
                    }
                    spinEchoChanged = true;
                }
                else {
                    model.setShowNavigationView(!model.isShowNavigationView());
                    paintCheckBox(g2, navigation, model.isShowNavigationView());
                }
            }
            else if ((modulation.y <= mouseY) && (mouseY <= (modulation.y + modulation.height))) {
                if (mouseX < width / 2) {
                    model.setIsJmodulated(!model.isJmodulated());
                    paintCheckBox(g2, modulation, model.isJmodulated());
                    spinEchoChanged = true;
                }
                else {
                    if (model.getDisplayMode() == model.HISTO) {
                        model.setAutoIntensity(!model.isAutoIntensity());
                        paintCheckBox(g2, autoIntensity, model.isAutoIntensity());
                        autoIntensChanged = !autoIntensChanged;
                    }
                }
            }
            else if ((resonance.y <= mouseY) && (mouseY <= (resonance.y + resonance.height))) {
                if (mouseX < width / 2) {
                    model.setShowCoupling(!model.isShowCoupling());
                    paintCheckBox(g2, resonance, model.isShowCoupling());
                    if (is13Ctype) {
                        updateColoredOption(g2);
                    }
                }
                else {
                    if (!is13Ctype) {
                        model.setRoofEffectEnabled(!model.isRoofEffectEnabled());
                        paintCheckBox(g2, effect, model.isRoofEffectEnabled());
                    }
                    else {
                        if (!model.isShowCoupling() || (model.getDisplayMode() != model.REALISTIC)) {
                            model.setColoredPeaks(!model.isColoredPeaks());
                            updateColoredOption(g2);
                        }
                    }
                }
            }
            else if ((integral.y <= mouseY) && (mouseY <= (integral.y + integral.height))) {
                model.setShowIntegral(!model.isShowIntegral());
                paintCheckBox(g2, integral, model.isShowIntegral());
            }
            g2.dispose();
        }
    }


    private void updateColoredOption(Graphics2D g2) {
        if (model.isShowCoupling() || (model.getDisplayMode() == model.REALISTIC)) {
            paintDisabledCheckBox(g2, effect, model.isColoredPeaks());
        }
        else {
            paintCheckBox(g2, effect, model.isColoredPeaks());
        }
    }


    /**
     * Return TRUE if a repaint of parent component/spectrum is necessary
     * @return TRUE if a repaint is necessary
     */
    public boolean repaintNecessary() {
        boolean r = (spinEchoChanged || autoIntensChanged);
        spinEchoChanged   = false;
        autoIntensChanged = false;
        return r;
    }


    /**
     * Return boolean options
     * @return array of options
     */
    public boolean[] getOptions() {
        return new boolean[] { model.isShowNavigationView() };
    }
}
