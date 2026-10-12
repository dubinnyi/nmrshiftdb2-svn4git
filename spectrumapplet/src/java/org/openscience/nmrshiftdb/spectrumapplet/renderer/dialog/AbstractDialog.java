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

import java.awt.*;

/**
 * Abstract class to display dialogs in a spectrum
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public abstract class AbstractDialog {
    private SpectrumRenderer renderer;
    private boolean isVisible;

    /**
     * Creates a new instance of AbstractDialog
     * @param r the spectrum renderer
     */
    public AbstractDialog(SpectrumRenderer r) {
        renderer  = r;
        isVisible = false;
    }

    /**
     * Paints the dialog
     ** @param g2 the <code>Graphics2D</code> context
     */
    public abstract void paintDialog(Graphics2D g2);


    /**
     * Paints the button that starts the dialog
     * @param g2 the <code>Graphics2D</code> context
     */
    public abstract void paintDialogButton(Graphics2D g2);


    /**
     * Check if the button that opens the dialog is clicked by mouse
     * @param mouseX x position of mouse
     * @param mouseY y position of mouse
     * @return TRUE if the mouse is clicked in the area of the open button
     */
    public abstract boolean openButtonClicked(int mouseX, int mouseY);


    /**
     * Check if the button that closes the dialog is clicked by mouse
     * @param mouseX x position of mouse
     * @param mouseY y position of mouse
     * @return TRUE if the mouse is clicked in the area of the close button
     */
    public abstract boolean closeButtonClicked(int mouseX, int mouseY);


    /**
     * Set visble info
     * @param enabled if TRUE the dialog is visible
     */
    public void setVisible(boolean enabled) {
        isVisible = enabled;
    }


    /**
     * Return visble info
     * @return visble info
     */
    public boolean isVisble() {
        return isVisible;
    }


    /**
     * Return the <code>Graphics2D</code> context
     * @return the <code>Graphics2D</code> context
     */
    public Graphics2D getGraphics2D() {
        return (Graphics2D) renderer.getGraphics();
    }


    /**
     * Return the renderer
     * @return the renderer
     */
    public SpectrumRenderer getRenderer() {
        return renderer;
    }


    /**
     * Returns the maximum horizontal offset (on the left or right side)
     * @return the maximum horizontal offset
     */
    public int getXoffset() {
        Metrics m = renderer.getMetrics();
        int o     = m.getXoffsetLeft();
        if (o > m.getXoffsetRight()) {
            o = m.getXoffsetRight();
        }
        return o;
    }


    /**
     * Paint a check box
     * @param g2 the <code>Graphics2D</code> context
     * @param r the rectangle used to specify postion and size
     * @param isChecked if TRUE the check box is checked
     */
    public void paintCheckBox(Graphics2D g2, Rectangle r, boolean isChecked) {
        g2.setColor(Color.white);
        g2.fillRect(r.x, r.y, r.width, r.height);
        g2.setColor(Color.black);
        g2.drawRect(r.x, r.y, r.width, r.height);
        if (isChecked) {
            g2.setStroke(new BasicStroke(1.3f));
            g2.drawLine(r.x, r.y, r.x + r.width, r.y + r.height);
            g2.drawLine(r.x, r.y + r.height, r.x + r.width, r.y);
            g2.setStroke(new BasicStroke());
        }
    }


    /**
     * Paint a check box
     * @param g2 the <code>Graphics2D</code> context
     * @param r the rectangle used to specify postion and size
     * @param isChecked if TRUE the check box is checked
     */
    public void paintDisabledCheckBox(Graphics2D g2, Rectangle r, boolean isChecked) {
        g2.setColor(Color.lightGray);
        g2.fillRect(r.x, r.y, r.width, r.height);
        g2.setColor(Color.darkGray);
        g2.drawRect(r.x, r.y, r.width, r.height);
        if (isChecked) {
            g2.setStroke(new BasicStroke(1.3f));
            g2.drawLine(r.x, r.y, r.x + r.width, r.y + r.height);
            g2.drawLine(r.x, r.y + r.height, r.x + r.width, r.y);
            g2.setStroke(new BasicStroke());
        }
    }


    /**
     * Draws a button
     * @param g2 the <code>Graphics2D</code> context
     * @param r the rectangle used to specify postion and size
     * @param text the button text
     */
    public void paintButton(Graphics2D g2, Rectangle r, String text) {
        paintButton(g2, r.x, r.y, r.width, r.height, text);
    }


    /**
     * Draws a button
     * @param x the left x position of the button
     * @param yTop the top y position of the button
     * @param width the button width
     * @param height the button height
     * @param g2 the <code>Graphics2D</code> context
     * @param text the button text
     */
    private void paintButton(Graphics2D g2, int x, int yTop, int width, int height, String text) {
        int y = yTop + height;
        g2.setColor(Color.white);
        g2.drawLine(x + 1, y - 1, x + 1, y - height);
        g2.drawLine(x, y - height + 1, x + width - 1, y - height + 1);
        g2.setColor(Color.lightGray);
        g2.drawLine(x, y, x, y - height);
        g2.drawLine(x, y - height, x + width, y - height);
        g2.setColor(Color.darkGray);
        g2.drawLine(x + 2, y - 1, x + width, y - 1);
        g2.drawLine(x + width - 1, y, x + width - 1, y - height + 2);
        g2.setColor(Color.black);
        g2.drawLine(x, y, x + width, y);
        g2.drawLine(x + width, y, x + width, y - height);
        g2.setColor(Color.lightGray);
        g2.fillRect(x + 2, y - height + 2, width - 3, height - 3);
        g2.setColor(Color.black);
        int z = width / 2 - g2.getFontMetrics().stringWidth(text) / 2 + 1;
        g2.drawString(text, x + z, y - 6);
    }


    /**
     * Return TRUE if a repaint of parent component/spectrum is necessary
     * @return TRUE if a repaint is necessary
     */
    public boolean repaintNecessary() {
        return false;
    }


    /**
     * Return boolean options
     * @return array of options
     */
    public boolean[] getOptions() {
        return null;
    }
}
