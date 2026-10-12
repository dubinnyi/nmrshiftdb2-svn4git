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
 * Created on 25. November 2004
 */
package org.openscience.nmrshiftdb.spectrumapplet.renderer.nmr;

import org.openscience.nmrshiftdb.spectrumapplet.data.nmr.*;

import java.awt.*;
import java.awt.event.*;

/**
 * A frame displaying the prediction distribution
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public class PredictionFrame extends Frame {
    private String frameId;
    private final NmrSpecRenderer renderer;

    /**
     * Creates a new instance of PredictionFrame
     * @param id the frame id
     * @param r the calling SpecRenderer
     * @param p the prediction
     * @param size the frame size
     * @param offset the frame offset from screen center
     * @param title the frame title
     */
    public PredictionFrame(String id, NmrSpecRenderer r, Prediction p, Dimension size, int offset,
                           String title) {
        frameId  = id;
        renderer = r;
        setTitle(title);
        setSize(size.width, size.height);
        center(offset);
        addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    if (renderer != null) {
                        renderer.setExtraFrameClosed(frameId);
                    }
                    dispose();
                }
            });

        PredictionHistogram histo = new PredictionHistogram(p, true);
        add(histo);
        setVisible(true);
    }

    private void center(int offset) {
        Dimension s = Toolkit.getDefaultToolkit().getScreenSize();
        int x       = (s.width - getSize().width) / 2;
        int y       = (s.height - getSize().height) / 2;
        setLocation(x + offset, y + offset);
    }
}
