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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultListModel;

import org.openscience.nmrshiftdb.spectrumapplet.data.AbstractDataContainer;
import org.openscience.nmrshiftdb.spectrumapplet.data.nmr.NmrData;
import org.openscience.nmrshiftdb.spectrumapplet.data.nmr.NmrPeak;
import org.openscience.nmrshiftdb.spectrumapplet.data.nmr.Prediction;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.SpectrumModel;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.action.PeakUnderCursor;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.dialog.HelpDialog;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.dialog.OptionDialog;
import org.openscience.nmrshiftdb.spectrumapplet.util.SpectrumUtil;

/**
 * Used to display a nmr spectrum in detail
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public class NmrDetailSpectrum extends AbstractRenderer {
    private PeakUnderCursor peakUnderCursor;
    private NmrData pList;
    private Map openPredictionFrames;
    boolean isgraphic = false;
    ArrayList pickedPeaks=new ArrayList();
    DefaultListModel listmodel;

    /**
     * Creates a DetailSpectrum
     * @param model the corresponding model
     * @param l list of peaks
     * @param isgraphic flag used to paint graphic (e.g. for images)
     * @param peakpicker Shall the applet be used as a peak selection tool (see peakpicker.html in demo)?
     */
    public NmrDetailSpectrum(SpectrumModel model, AbstractDataContainer l, boolean isgraphic, boolean peakpicker) {
        super(model, l,peakpicker);
        init(l);
        this.isgraphic = isgraphic;
    }

    /**
     * Creates a DetailSpectrum, only the specified range is displayed
     * @param xMin minimum x value displayed
     * @param xMax maximum x value displayed
     * @param model the corresponding model
     * @param l list of peaks
     *
    public DetailSpectrum(SpectrumModel model, AbstractDataContainer l, double xMin, double xMax) {
        super(model, l, xMin, xMax);
        init(l);
    }
     */
    /**
     * Init spectrum
     */
    private void init(AbstractDataContainer data) {
        pList                = (NmrData) data;
        options              = new OptionDialog(this, pList.is13Cspectrum());
        openPredictionFrames = new HashMap();
        help                 = new HelpDialog(this);
        pList.setRoofEffectEnabled(model.isRoofEffectEnabled());
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
            options.setVisible(false);
            help.setVisible(false);
            pList.setRoofEffectEnabled(model.isRoofEffectEnabled());
            calculatePoints();
            if (model.isShowCorrelation() && getControler() != null) {
                getControler().calcCorrelationLines();
            }
            if (model.isShowIntegral()) {
                // calculate integral data
                integralPainter.updateData(SpectrumUtil.calcIntegralData(metrics, visiblePeaks));
            }
        }
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
            if (getControler() != null) {
                getControler().paintCorrelationLines(this, g2);
            }

            // draw spectrum
            getCurvePainter().paintComponent(g2);
            if (model.isShowIntegral()) {
                // paint integral
                integralPainter.paintComponent(g2);
            }
            printStatusInfo(g2);
            // use PLAIN font type for dialogs and buttons
            Font f = g2.getFont();
            g2.setFont(new Font(f.getName(), f.PLAIN, f.getSize()));
            if (!isgraphic) {
              drawButtons(g2);
            }
            if (help.isVisble()) {
                help.paintDialog(g2);
            }
            if (options.isVisble()) {
                options.paintDialog(g2);
            }
            peakHighlighter.paintComponent(g2);
            distancePainter.paintComponent(g2);
            // if (peakUnderCursor != null) {
            //     infoPainter.updateData(peakUnderCursor.getInfo());
            // }
            getInfoPainter().paintComponent(g2);
        }
    }


    private void drawButtons(Graphics2D g2) {
        options.paintDialogButton(g2);
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
        else if (options.isVisble()) {
            if (options.closeButtonClicked(x, y)) {
                setIgnoreZoomOps(false);
                options.setVisible(false);
                forceRecalculation();
                if (options.repaintNecessary()) {
                    resetZoom();
                }
                else {
                    updateView(true);
                }
                if (getControler() != null) {
                    getControler().setOptions(options.getOptions());
                }
            }
            else {
                ((OptionDialog) options).optionsSelected(x, y);
            }
        }
        else {
            // check if a button is clicked by mouse
            if (options.openButtonClicked(x, y)) {
                setIgnoreZoomOps(true);
                Graphics2D g2 = (Graphics2D) getGraphics();
                getInfoPainter().updateData(null);
                getInfoPainter().paintComponent(g2);
                options.paintDialog(g2);
                g2.dispose();
            }
            else if (help.openButtonClicked(x, y)) {
                setIgnoreZoomOps(true);
                Graphics2D g2 = (Graphics2D) getGraphics();
                getInfoPainter().updateData(null);
                getInfoPainter().paintComponent(g2);
                help.paintDialog(g2);
                g2.dispose();
            }
        }
    }


    public boolean toggleDisplayMode() {
        boolean isRealistic = true;
        if (model.getDisplayMode() == model.HISTO) {
            model.setDisplayMode(model.REALISTIC);
            model.setAutoIntensity(true);
        }
        else {
            isRealistic = false;
            model.setDisplayMode(model.HISTO);
        }
        return isRealistic;
    }


    /**
     * Invoked when a key has been typed.
     * @param e the KeyEvent
     */
    public void keyPressed(KeyEvent e) {
        char c = e.getKeyChar();
        if (peakUnderCursor != null) {
            // handle predictions for peak/peaks 
            Prediction[] pred = peakUnderCursor.getPredictions();
            if (pred != null) {
                //System.out.println("Found Prediction: " + pred.length);
                int off = 0;
                for (int i = 0; i < pred.length; i++) {
                    String id = pred[i].getID();
                    if (!openPredictionFrames.containsKey(id)) {
                        PredictionFrame f = new PredictionFrame(id, this, pred[i], new Dimension(500, 350),
                                                                off, pred[i].getInfo());
                        off += 30;
                        openPredictionFrames.put(id, f);
                    }
                }
            }
        }

        // special keyboard actions:
        else if (c == 'l') {
            // toggle correlation lines display
            if (getControler() != null) {
                getControler().toggleCorrelation();
            }
        }
        else {
            if (c == '+' || c == '-') {
                // increase/decrease the spectrum frequency
                double f = pList.getNumberProperty(pList.FREQ);
                if (c == '+') {
                    f += 10;
                }
                else if (c == '-') {
                    f -= 10;
                    if (f < 10) {
                        f = 10;
                    }
                }
                pList.setSpectrometerFrequency(f);
            }
            else if (c == 'd') {
                // toggle display mode between REALISTIC and HISTO
                toggleDisplayMode();
            }
            else if (c == 'i') {
                // toggle integral display 
                model.setShowIntegral(!model.isShowIntegral());
            }
            else if (c == 'j') {
                // toggle j-modulation display 
                model.setIsJmodulated(!model.isJmodulated());
                resetZoom();
            }
            else if (c == 'c') {
                // toggle coupling display 
                model.setShowCoupling(!model.isShowCoupling());
            }
            else if (c == 'n') {
                // toggle navigation spectrum display 
                if (getControler() != null) {
                    model.setShowNavigationView(!model.isShowNavigationView());
                    getControler().setOptions(new boolean[] { model.isShowNavigationView() });
                }
            }
            else if (c == 'r') {
                // toggle roof-effect display 
                model.setRoofEffectEnabled(!model.isRoofEffectEnabled());
            }
            else if (c == 'f') {
                // toggle colored peaks display 
                model.setColoredPeaks(!model.isColoredPeaks());
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
            resetPeakHighlighter();
            forceRecalculation();
            updateView(true);
        }

        //else if (c=='#' ){
        //    System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
        //}
    }


    /**
     * Notify the renderer that an external frame was closed
     * @param frameID the frame ID
     */
    public void setExtraFrameClosed(String frameID) {
        openPredictionFrames.remove(frameID);
    }


    /**
     * Invoked when left mouse button pressed
     * @param mouseX mose position x
     * @param mouseY mose position y
     */
    public void leftPressed(int mouseX, int mouseY) {
        int x     = marker.limitGraphics2dX(mouseX);
        double xx = metrics.translateMouseX(x);
        if (peakUnderCursor != null) {
            xx              = peakUnderCursor.getChemicalShift();
            x               = peakUnderCursor.getX();
            peakUnderCursor = null;
            resetPeakHighlighter();
        }
        Object data = new double[] { x, xx, pList.getNumberProperty(pList.FREQ), metrics.translateY_int(0) };
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
     * Search the peak under mouse cursor and displays peak information
     * @param xCursor x value of mouse position
     */
    public void findPeakUnderCursor(int xCursor) {
        int sense                  = 2;
        boolean changed            = false;
        boolean predictionHasFocus = false;
        if (visiblePeaks != null) {
            if (peakUnderCursor != null) {
                // check if mouse leaves peak
                if (Math.abs(xCursor - peakUnderCursor.getX()) >= sense) {
                    peakUnderCursor = null;
                    changed         = true;
                    getInfoPainter().updateData(null);
                    resetPeakHighlighter();
                }
            }
            else if ((metrics.getSize() != null) && !options.isVisble() && !help.isVisble()) {
                getInfoPainter().updateData(null);
                NmrPeak[] peaksFound = SpectrumUtil.searchPeaksNextToCursor(visiblePeaks, xCursor, sense);
                if (peaksFound != null) {
                    changed            = true;
                    predictionHasFocus = createPeakUnderCursor(peaksFound);
                    getInfoPainter().updateData(peakUnderCursor.toString());
                }
            }
            if (changed) {
                if (peakUnderCursor != null) {
                    int no = peakUnderCursor.getFirstAtomNumber();
                    if (getControler() != null) {
                        getControler().updateAllPeakHighlighter(no);
                        getControler().highlightAtoms(peakUnderCursor.getAllAtomNumbers(), !predictionHasFocus);
                    }
                    else {
                        updatePeakHighlighter(no);
                    }
                }
                updateView(true);
            }
        }
    }


    private boolean createPeakUnderCursor(NmrPeak[] peaksFound) {
        // create a dummy peak manging highlighting and prediction stuff
        peakUnderCursor = new PeakUnderCursor(peaksFound[0], pList.getSpectrumProperty(pList.SOLVENT));
        Prediction[] pred = null;
        for (int i = 0; i < peaksFound.length; i++) {
            peakUnderCursor.addAtomNumber(peaksFound[i]);
            pred = peaksFound[i].getPredictions();
            if (pred != null) {
                peakUnderCursor.addPredictions(pred);
            }
        }
        pred = peakUnderCursor.getPredictions();
        boolean predictionHasFocus = false;
        if ((pred != null)) {
            for (int i = 0; i < pred.length; i++) {
                if (openPredictionFrames.containsKey(pred[i].getID())) {
                    predictionHasFocus = true;
                    PredictionFrame f = (PredictionFrame) openPredictionFrames.get(pred[i].getID());
                    f.setExtendedState(f.NORMAL);
                    f.requestFocus();
                }
            }
        }
        return predictionHasFocus;
    }


    /**
     * Highlight the specified atom, previous higlights are deleted
     * @param atomNo the atom number of the highlighted atom
     */
    public void highlightPeakForAtom(int atomNo) {
        updatePeakHighlighter(atomNo);
        //delete old highlights and paint new one
        updateView(true);
    }


    /**
     * Update the data for the peak highlighter;
     * if the specified atom number == Integer.MIN_VALUE the highlighter is reset
     * @param atomNo the atom number of highlighted peak
     */
    public void updatePeakHighlighter(int atomNo) {
        peakHighlighter.updateData(null);
        if (atomNo != Integer.MIN_VALUE) {
            if (!model.isShowCoupling()) {
                NmrPeak p = getPeakForAtomNumber(atomNo);
                if (p != null) {
                    peakHighlighter.updateData(new Object[] { new NmrPeak[] { p }, pList, this });
                }
            }
            else {
                NmrPeak[] p = pList.getMultiPeaksForAtomnumber(atomNo);
                if (p != null) {
                    // highlight only visible parts of a multiplett
                    peakHighlighter.updateData(new Object[] { p, pList, this });
                }
            }
        }
    }


    private void resetPeakHighlighter() {
        if (getControler() != null) {
            getControler().updateAllPeakHighlighter(Integer.MIN_VALUE);
        }
        else {
            updatePeakHighlighter(Integer.MIN_VALUE);
        }
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
    
    /**
     * Does a peakpicking in the given range
     * @param mouseX mose position x
     * @param mouseY mose position y
     */    
    public void pickpeaks(double startX, double endX){
    	double sum=0;
    	int numberpeaks=0;
    	for(int i=0;i<pList.getAllPeaks(false).length;i++){
    		if(pList.getAllPeaks(false)[i].getValueX()>=startX && pList.getAllPeaks(false)[i].getValueX()<=endX){
    			sum+=pList.getAllPeaks(false)[i].getValueX();
    			numberpeaks++;
    		}
    	}
    	//if no peaks were found, we do not add anything
    	if(numberpeaks>0){
	    	double newpeak=((int)(sum/numberpeaks*100))/100d;
	    	pickedPeaks.add(new Double(newpeak));
	    	if(listmodel!=null && !listmodel.contains(new String(newpeak+"")))
	    		listmodel.addElement(new String(newpeak+""));
    	}
    }
    
	/**
	 * Returns the peaks in case the applet was used as a peak selection tool (see peakpicer.html in demo)
	 * 
	 * @return The peaks
	 */
    public ArrayList getPickedPeaks(){
    	return pickedPeaks;
    }
    
	/**
	 * Sets a defaultListModel for a JList in the main window to show the selected peaks
	 * 
	 * @param listmodel The listmodel
	 */
    public void setListModel(DefaultListModel listmodel){
    	this.listmodel=listmodel;
    }
}
