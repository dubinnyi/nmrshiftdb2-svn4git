/*
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
 * Created on 19. November 2004
 */
package org.openscience.nmrshiftdb.spectrumapplet.util;

/**
 * Interface for spectrum viewer applet
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public interface SpectrumApplet {
    public static final String[] APPLET_PARAM = new String[] {
                                                    "spectrumTyp", "frequency", "spectrum", "predictions",
                                                    "solvent", "spectrumExperiment", "realisticLines",
                                                    "jModulation", "showCoupling", "showIntegral",
                                                    "hideNavigation", "autoIntensity", "lineWidth",
                                                    "showRoofEffect", "coloredPeaks13C", "showCorrelation"
                                                };

    // index of the first optional parameter in APPLET_PARAM
    public static final int OPTIONAL_INDEX    = 6;

    /**
     * Highlight peak in spectrum
     * @param  atomNo  number of the atom to highlight
     */
    public void highlightPeakInSpectrum(int atomNo);
    
    /**
     * Remove the peak highlighting in spectrum
     */
    public void removeHighlightingInSpectrum();
    
    /**
     * Handles interaction with structure viewer,
     * highlighted atoms in spectrum view will be highlighted in structure
     * @param atomNumbers atom numbers of peaks highlighted in spectrum
     */
    public void highlightAtomsInStructur(int[] atomNumbers);
    
    public void highlightRowsInTable(int[] atomNumbers);

    /**
     * Unselect all atoms in structure
     */
    public void removeHighlightingInStructur();
}
