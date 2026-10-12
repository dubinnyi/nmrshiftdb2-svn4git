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
 * Created on 5. Januar 2005
 */
package org.openscience.nmrshiftdb.spectrumapplet.renderer.painter;

import org.openscience.nmrshiftdb.spectrumapplet.renderer.*;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.nmr.*;

import java.awt.*;

/**
 * Abstract Painter implementation
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public abstract class AbstractPainter implements Painter {
    protected NmrSpecRenderer renderer;
    protected Metrics metrics;
    protected Color background;

    /** Creates a new instance of AbstractPainter
     */
    private AbstractPainter() {
    }


    /** Creates a new instance of AbstractPainter
     * @param r the spectrum renderer
     */
    public AbstractPainter(NmrSpecRenderer r) {
        renderer   = r;
        metrics    = renderer.getMetrics();
        background = r.getSpectrumModel().getBackGround();
    }

    /**
     * Paint this component
     * @param g2 the graphics context
     */
    public abstract void paintComponent(Graphics2D g2);


    /**
     * Repaint this component
     */
    public void repaintComponent() {
        paintComponent((Graphics2D) renderer.getGraphics());
    }


    /**
     * Clear this component
     */
    public void clearComponent() {
    }


    /**
     * Returns TRUE if somthing to paint
     * @return TRUE if somthing to paint
     */
    public boolean hasData() {
        return false;
    }


    /**
     * Update the data used for painting
     * @param data the data used for painting
     */
    public abstract void updateData(Object data);
}
