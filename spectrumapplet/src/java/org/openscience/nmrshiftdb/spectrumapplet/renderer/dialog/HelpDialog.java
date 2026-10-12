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

import java.awt.*;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.nmr.*;



/**
 * Draws a dialog for help information
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public class HelpDialog extends AbstractDialog {
    private Rectangle helpButton;
    private Rectangle helpCloseButton;

    /**
     * Creates a new instance of HelpDialog
     * @param r the spectrum renderer
     */
    public HelpDialog(NmrSpecRenderer r) {
        super(r);
    }

    /**
     * Paints the dialog
     * @param g2 the <code>Graphics2D</code> context
     */
    public void paintDialog(Graphics2D g2) {
        setVisible(true);
        //g2.setFont(new Font("Arial", Font.PLAIN, 12));
        int w  = 180;
        int fh = g2.getFontMetrics().getHeight();
        int x  = getRenderer().getMetrics().getSize().width - getXoffset() - w;
        int y  = getRenderer().getMetrics().getYoffsetTop() - 5;
        y      = getRenderer().getMetrics().getYoffsetTop() - 24 - fh;
        int h  = 150;
        g2.setColor(getRenderer().getSpectrumModel().getBackGround());
        g2.fillRect(x - 5, y, w + 7, h + 5);
        g2.setColor(new Color(222, 222, 222));
        g2.fillRect(x + 1, y + 1, w - 1, h - 1);
        g2.setColor(Color.black);
        g2.drawRect(x, y, w, h);
        y = y + 7 + fh;
        x += 5;
        g2.drawString("Zoom:", x, y);
        x += 5;
        y += 5 + fh;
        g2.drawString("-   Right-click on spectrum", x, y);
        y += 1 + fh;
        g2.drawString("    ( press button: start interval,", x, y);
        y += fh;
        g2.drawString("      leave button: end interval )", x, y);
        y += 5 + fh;
        g2.drawString("-   Double-click on spectrum:", x, y);
        y += 1 + fh;
        g2.drawString("      ==> Reset  zoom", x, y);
        String s = "Ok";
        w = g2.getFontMetrics().stringWidth(s) + 20;
        x += 60;
        y += 7;
        helpCloseButton = new Rectangle(x, y, w, fh + 4 + 4);
        paintButton(g2, helpCloseButton, s);
    }


    /**
     * Paints the button that starts the dialog
     * @param g2 the <code>Graphics2D</code> context
     */
    public void paintDialogButton(Graphics2D g2) {
        g2.setColor(Color.black);
        String s   = "Help";
        int h      = g2.getFontMetrics().getHeight() - 1;
        int w      = g2.getFontMetrics().stringWidth(s) + 10;
        int xPos   = getRenderer().getMetrics().getSize().width - getXoffset() - w;
        int yPos   = getRenderer().getMetrics().getYoffsetTop() - 23 - h;
        helpButton = new Rectangle(xPos, yPos, w, h + 4 + 4);
        paintButton(g2, helpButton, s);
    }


    /**
     * Check if the button that opens the dialog is clicked by mouse
     * @param mouseX x position of mouse
     * @param mouseY y position of mouse
     * @return TRUE if the mouse is clicked in the area of the open button
     */
    public boolean openButtonClicked(int mouseX, int mouseY) {
        return helpButton.contains(mouseX, mouseY);
    }


    /**
     * Check if the button that closes the dialog is clicked by mouse
     * @param mouseX x position of mouse
     * @param mouseY y position of mouse
     * @return TRUE if the mouse is clicked in the area of the close button
     */
    public boolean closeButtonClicked(int mouseX, int mouseY) {
        return helpCloseButton.contains(mouseX, mouseY);
    }
}
