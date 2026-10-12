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
 * Created on 21. November 2004
 */
package org.openscience.nmrshiftdb.spectrumapplet.renderer.nmr;

import org.openscience.nmrshiftdb.spectrumapplet.data.*;
import org.openscience.nmrshiftdb.spectrumapplet.data.nmr.*;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.*;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.action.*;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.dialog.*;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.painter.*;
import org.openscience.nmrshiftdb.spectrumapplet.util.*;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * Provides basic renderer functions
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public abstract class AbstractRenderer extends JComponent implements NmrSpecRenderer {
    private boolean flipXaxis                 = true;
    protected boolean forceRecalculation      = true;
    protected SpectrumModel model;
    private AbstractDataContainer pList;
    protected NmrPeak[] visiblePeaks;
    protected FontMetrics fontMetrics;
    private MouseActionManager mouseActions;
    protected Metrics metrics;
    protected AbstractDialog options;
    protected AbstractDialog help;
    private SpectrumControler con;
    private String statusInfo;
    protected ZoomMarker marker;
    protected double yZoomFactor;

    // the Painters:
    protected Painter titlePainter;
    protected Painter xAxis;
    protected Painter xGrid;
    protected Painter yAxis;
    protected Painter yGrid;
    private Painter curvePainter;
    private Painter correlation;
    protected Painter integralPainter;
    protected Painter statusPainter;
    private Painter infoPainter;
    protected Painter peakHighlighter;
    protected DistancePainter distancePainter;
    private boolean peakpicker;

    /** Creates a new instance of AbstractRenderer */
    public AbstractRenderer() {
    }


    /**
     * Creates a GraphRenderer
     * @param model the corresponding model
     * @param l list of peaks
     * @param peakpicker Shall the applet be used as a peak selection tool (see peakpicker.html in demo)?
     */
    public AbstractRenderer(SpectrumModel model, AbstractDataContainer l, boolean peakpicker) {
        this.model = model;
        this.peakpicker=peakpicker;
        pList      = l;
        metrics    = new Metrics(this, flipXaxis);
        metrics.calcBorderValues();
        metrics.setInitialLimitsX(metrics.getXmin(), metrics.getXmax());
        init();
    }

    /**
     * Creates a GraphRenderer, only the specified range is displayed
     * @param xMin minimum x value displayed
     * @param xMax maximum x value displayed
     * @param model the corresponding model
     * @param l list of peaks
     *
    public AbstractRenderer(SpectrumModel model, AbstractDataContainer l, double xMin, double xMax) {
        this.model = model;
        pList      = l;
        metrics    = new Metrics(this, flipXaxis);
        metrics.calcBorderValues();
        metrics.setLimitsX(xMin, xMax);
        metrics.setInitialLimitsX(xMin, xMax);
        init();
    }
     */
    /**
     * Init renderer
     */
    private void init() {
        yZoomFactor = 1;
        marker      = new ZoomMarker(this);
        setMouseActionManager(new SpectrumMouseActions(this,peakpicker));
        setBackground(model.getBackGround());
        initializePainters();
    }


    public void setMouseActionManager(MouseActionManager m) {
        if (mouseActions != null) {
            this.removeMouseListener(mouseActions);
            this.removeMouseMotionListener(mouseActions);
        }
        mouseActions = m;
        this.addMouseListener(mouseActions);
        this.addMouseMotionListener(mouseActions);
    }


    protected void initializePainters() {
        titlePainter    = new TitlePainter(this);
        xAxis           = new XaxisPainter(this);
        xGrid           = new XgridPainter(this);
        yAxis           = new YaxisPainter(this);
        yGrid           = new YgridPainter(this);
        curvePainter    = new CurvePainter(this);
        integralPainter = new CurvePainter(this);
        ((CurvePainter) integralPainter).setColor(Color.red);
        correlation     = new CorrelationPainter(this);
        statusPainter   = new StatusPainter(this);
        infoPainter     = new InfoPainter(this);
        peakHighlighter = new PeakHighlighter(this);
        distancePainter = new DistancePainter(this);
    }


    /**
     * Return the InfoPainter for this spectrum
     * @return the InfoPainter
     */
    public InfoPainter getInfoPainter() {
        return (InfoPainter) infoPainter;
    }


    /**
     * Return the CurvePainter for this spectrum
     * @return the CurvePainter
     */
    public CurvePainter getCurvePainter() {
        return (CurvePainter) curvePainter;
    }


    /**
     * Return the CorrelationPainter for this spectrum
     * @return the CorrelationPainter
     */
    public CorrelationPainter getCorrelationPainter() {
        return (CorrelationPainter) correlation;
    }


    /**
     * Return the data container
     * @return the data container
     */
    public DataContainer getDataContainer() {
        return pList;
    }


    /**
     * Set a special option dialog object
     * @param optionsDialog the option dialog
     */
    public void setOptionDialog(AbstractDialog optionsDialog) {
        options = optionsDialog;
    }


    /**
     * Return the corresponding model
     * @return the corresponding model
     */
    public SpectrumModel getSpectrumModel() {
        return model;
    }


    /**
     * Return the object that handles zoom information and marker drawing
     * @return the ZoomMarker
     */
    public ZoomMarker getZoomMarker() {
        return marker;
    }


    /**
     * Return the <CODE>SpectrumControler</CODE> object that manages the
     * syncronisation of multiple spectrums displayed
     * @return the <CODE>SpectrumControler</CODE>
     */
    public SpectrumControler getControler() {
        return con;
    }


    /**
     * Set the <CODE>SpectrumControler</CODE> object that manages the
     * syncronisation of multiple spectrums displayed
     * @param c the <CODE>SpectrumControler</CODE>
     */
    public void setControler(SpectrumControler c) {
        con = c;
    }


    /**
     * Return the object that handles all metric information
     * @return the metirc info
     */
    public Metrics getMetrics() {
        return metrics;
    }


    //***************************************************
    //   
    //  Methods used by paintComponent() 
    //
    //***************************************************
    /**
     * Paint the component
     * @param g the graphics contexts
     */
    public void paintComponent(Graphics g) {
        if (getSize() != null) {
            //System.out.println("---->  paint called for: " + getSpectrumModel().getTitle());
            Graphics2D g2 = (Graphics2D) g.create();
            if ((!getSize().equals(metrics.getSize()))) {
                //System.out.println("Attention: should not be called becaucse of overwritten setSize() methods");
                updateMetricsSize(getSize());
                doRecalculation();
            }
            if (forceRecalculation) {
                doRecalculation();
            }
            draw(g2);
        }
        else {
            System.out.println("Size = null");
        }
    }


    /**
     * Repaint the spectrum or all spectrums
     * @param globalUpdate if TRUE the view update is managed by Connector (if exist)
     */
    protected void updateView(boolean globalUpdate) {
        if (globalUpdate && (getControler() != null)) {
            getControler().updateViews();
        }
        else {
            doRecalculation();
            repaint();
        }
    }


    /**
     * Draw the component
     * @param g2 the Grahics2D context
     */
    protected abstract void draw(Graphics2D g2);


    /**
     * Force a new calculation of all values used in paintComponent(Graphics)
     */
    public void forceRecalculation() {
        forceRecalculation = true;
    }


    /**
     * Update border values
     */
    public void updateBorderValues() {
        metrics.calcBorderValues();
    }


    /**
     * Executes a recalculation of display data
     */
    public void doRecalculation() {
        if (forceRecalculation) {
            forceRecalculation = false;
            calculatePoints();
        }
    }


    /**
     * Overwritten super.setSize() method to handle Metrics update
     * @param w the width
     * @param h the hight
     */
    public void setSize(int w, int h) {
        updateMetricsSize(new Dimension(w, h));
        super.setSize(w, h);
    }


    /**
     * Update metrics values during resize of the spectrum
     * @param d the spectrum size
     */
    public void updateMetricsSize(Dimension d) {
        if ((d != null) && (!d.equals(metrics.getSize()))) {
            metrics.setSize(d);
            forceRecalculation();
            doRecalculation();
        }
    }


    /**
     * Update zoom marker values after resize of the component
     */
    public void updateMarker() {
        double[] r = marker.getRange();
        marker.initializeRange(r[0], r[1]);
    }


    //***************************************************
    //   
    //  Methods used to calculate the spectrum
    //
    //***************************************************
    /**
     * Calculates all points displayed in spectrum
     */
    protected void calculatePoints() {
        int[] xVals   = null;
        int[] yVals   = null;
        int y0        = 0;

        // handle ONLY visible peaks, e.g. if zoomed in spectra !!
        visiblePeaks  = getVisiblePeaks(metrics.getXmin(), metrics.getXmax());
        int peaks     = visiblePeaks.length;
        Object[] data = null;
        if (peaks == 0) {
            if (model.isAutoIntensity()) {
                // adjust y-scale to visible peaks
                metrics.calcYborderValues();
            }
            y0       = metrics.translateY_int(0);
            // no peaks ==> draw the baseline
            xVals    = new int[2];
            yVals    = new int[2];
            xVals[0] = metrics.translateX_int(metrics.getXmin());
            yVals[0] = y0;
            xVals[1] = metrics.translateX_int(metrics.getXmax());
            yVals[1] = y0;
            data     = new Object[] { xVals, yVals };
        }
        else {
            if (model.getDisplayMode() == model.REALISTIC) {
                //calcRealisticData(visiblePeaks);
                int[][] dataPoints = calcRealisticData(getVisiblePeaks(metrics.getXmin() - 0.3,
                                                                       metrics.getXmax() + 0.3));
                xVals = dataPoints[0];
                yVals = dataPoints[1];
                data  = new Object[] { xVals, yVals };
                SpectrumUtil.updatePeakScreenCoordinates(visiblePeaks, metrics, model.isJmodulated());
            }
            else if (model.getDisplayMode() == model.HISTO) {
                SpectrumUtil.updatePeakScreenCoordinates(visiblePeaks, metrics, model.isJmodulated());
                y0 = metrics.translateY_int(0);
                int xMin = metrics.translateX_int(metrics.getXmin());
                int xMax = metrics.translateX_int(metrics.getXmax());
                
                ArrayList isoList = new ArrayList(peaks);
                IsochronePeaksList ip = null;
                // search isochrone peaks:
                for (int i = 0; i < peaks; i++) {
                    if (ip == null ){
                        ip = new IsochronePeaksList();
                        ip.addPeak(visiblePeaks[i]);
                    }
                    else {
                        if (visiblePeaks[i-1].getValueX() == visiblePeaks[i].getValueX() ){
                            ip.addPeak(visiblePeaks[i]);
                        }
                        else {
                            isoList.add(ip);
                            ip = new IsochronePeaksList();
                            ip.addPeak(visiblePeaks[i]);
                        }
                    }
                }
                // ad last isochrone peak list
                if (ip != null ){
                    isoList.add(ip);
                }
                boolean colored = (((NmrData) pList).is13Cspectrum() && model.isColoredPeaks() && !model.isShowCoupling());
                xVals = new int[3 + peaks * 3];
                double[] yyVals = new double[3 + peaks * 3];
                int[] colors = new int[1 + peaks];
                xVals[0] = xMin;
                yyVals[0] = 0;    
                xVals[1] = xMax;
                yyVals[1 ] = 0;
                xVals[2] = xMin;
                yyVals[2] = 0;
                colors[0] = NmrPeak.UNKNOWN_MULTI;
                int z = 1;
                double min = 0;
                double max = 0;
                double[] minmax = null;
                if (isoList.size() > 0){
                    for (int i = 0; i < isoList.size() ; i++) {
                        ip = (IsochronePeaksList)isoList.get(i);
                        Object[] d = ip.getLineData(colored, model.isJmodulated()); 
                        int[] xValues = (int[]) d[0];
                        double[] yValues = (double[]) d[1];
                        int[] col  = (int[]) d[2];
                        minmax = (double[]) d[3];
                        if (minmax[0] < min){
                            min = minmax[0];
                        }
                        if (minmax[1] > max){
                            max = minmax[1];
                        }
                        for (int j = 0; j < col.length ; j++) {
                            colors[z + j] = col[j];
                            xVals[(z + j) * 3] = xValues[j * 3];
                            yyVals[(z + j) * 3] = yValues[j * 3];    
                            xVals[(z + j) * 3 + 1] = xValues[j * 3 + 1];
                            yyVals[(z + j) * 3 + 1 ] = yValues[j * 3 + 1];
                            xVals[(z + j) * 3 + 2] = xValues[j * 3 + 2];
                            yyVals[(z + j) * 3 + 2] = yValues[j * 3 + 2];
                        }
                        z += col.length;
                    }
                    // adjust y-scale to visible histogram peaks
                    metrics.setLimitsY(min / yZoomFactor, max / yZoomFactor, model.isJmodulated());
                    yVals = new int[3 + peaks * 3];
                    for (int i = 0; i < yyVals.length; i++) {
                        yVals[i] = metrics.translateY_int(yyVals[i]);
                    }
                }
                data = new Object[] { xVals, yVals, colors };
                adjustYscale();
            }
        }
        curvePainter.updateData(data);
    }


    /**
     * Return visible peaks displayed in spectrum, may be overwritten by extended classes
     * @param minShift the minimum chemical shift
     * @param maxShift the maximum chemical shift
     * @return array of visible peaks
     */
    protected NmrPeak[] getVisiblePeaks(double minShift, double maxShift) {
        return ((NmrData) pList).getPeaksBetween(minShift, maxShift, model.isShowCoupling(), true);
    }


    /**
     * Return visible peaks displayed in spectrum
     * @return array of visible peaks
     */
    public NmrPeak[] getVisiblePeaks() {
        return visiblePeaks;
    }


    /**
     * Return the peak for specified atom number
     * @param atomNumber the atom number
     * @return the peak
     */
    public NmrPeak getPeakForAtomNumber(int atomNumber) {
        return ((NmrData) pList).getPeakForAtomnumber(atomNumber);
    }


    protected void adjustYscale() {
        if (model.isAutoIntensity()) {
            // adjust y-scale to visible peaks
            metrics.calcYborderValues(visiblePeaks);
        }
        SpectrumUtil.updatePeakScreenCoordinates(visiblePeaks, metrics, model.isJmodulated());
    }


    private int[][] calcRealisticData(NmrPeak[] p) {
        //long ti                = System.currentTimeMillis();
        SpectrumUtil.updatePeakScreenCoordinates(p, metrics, model.isJmodulated());
        double min                = Double.MAX_VALUE;
        double max                = Double.MIN_VALUE;
        NmrIntensityCalculator ic = new NmrIntensityCalculator(p, model.isJmodulated(),
                                                               model.getLineWidth() / pList.getNumberProperty(pList.FREQ));
        int start                 = metrics.getXoffsetLeft() + 1;
        int end                   = metrics.getSize().width - metrics.getXoffsetRight() - 1;
        int z                     = end - start;
        int[] xValues             = new int[z + 1];
        int[] yValues             = new int[z + 1];
        double[] yVals            = new double[z + 1];
        double y                  = 0;
        for (int i = 0; i < z + 1; i++) {
            xValues[i] = i + start;
            y          = ic.calcOverallIntensity(metrics.translateMouseX(xValues[i]));
            if (y < min) {
                min = y;
            }
            if (y > max) {
                max = y;
            }
            yVals[i] = y;
        }

        // force peak maxima are shown
        int x;
        for (int i = 0; i < p.length; i++) {
            x = p[i].getX();
            if ((x >= start) && (x <= end)) {
                y = ic.calcOverallIntensity(p[i].getValueX());
                if (y < min) {
                    min = y;
                }
                if (y > max) {
                    max = y;
                }
                if (model.isJmodulated()) {
                    if (y < 0) {
                        // peak down
                        if (yVals[x - start] > y) {
                            yVals[x - start] = y;
                        }
                    }
                    else {
                        //peak up
                        if (yVals[x - start] < y) {
                            yVals[x - start] = y;
                        }
                    }
                }
                else if (yVals[x - start] < y) {
                    yVals[x - start] = y;
                }
            }
        }

        // adjust y-scale to visible peaks
        metrics.setLimitsY(min / yZoomFactor, max / yZoomFactor, model.isJmodulated());
        for (int i = 0; i < z + 1; i++) {
            yValues[i] = metrics.translateY_int(yVals[i]);
        }
        return new int[][] { xValues, yValues };
    }


    /**
     * Checks if painted dialog buttons/checkboxes etc. are clicked by mouse
     * @param x x value of mouse position
     * @param y y value of mouse position
     */
    public void checkDialogActions(int x, int y) {
    }


    /**
     * Search the peak under mouse cursor and displays peak information
     * @param xCursor x value of mouse position
     */
    public void findPeakUnderCursor(int xCursor) {
    }


    /**
     * Highlight the specified atom, previous higlights are deleted
     * @param atomNo the atom number of the highlighted atom
     */
    public void highlightPeakForAtom(int atomNo) {
    }


    /**
     * Display a status information text at the bottom of the spectrum
     * @param info the status information text
     * @param c the color
     */
    public void printStatusInfo(String info, Color c) {
        statusInfo = info;
        Graphics2D g2 = (Graphics2D) getGraphics();
        printStatusInfo(g2, info, c);
        if (g2 != null) {
            g2.dispose();
        }
    }


    /**
     * Display a status information text at the bottom of the spectrum
     * @param g2 the Graphics context
     * @param info the status information text
     * @param c the color
     */
    private void printStatusInfo(Graphics2D g2, String info, Color c) {
        ((StatusPainter) statusPainter).setColor(c);
        statusPainter.updateData(info);
        statusPainter.paintComponent(g2);
    }


    /**
     * Update a status information text at the bottom of the spectrum
     * @param g2 the Graphics context
     */
    protected void printStatusInfo(Graphics2D g2) {
        if (statusInfo != null) {
            printStatusInfo(g2, statusInfo, Color.red);
        }
        else if (pList != null) {
            String info = "Freq: " + pList.getNumberProperty(pList.FREQ) + " MHz";
            String h = pList.getSpectrumProperty(pList.UNKNOWN_FREQ);
            if ((h != null) &&(h.equals("true"))){
                info = "Freq: UNKNOWN (use 100MHz)";
            }
            printStatusInfo(g2, info, Color.blue);
        }
    }


    /**
     * Start or stop zoom operations triggered by mouse operations
     * @param ignore if TRUE all zoom operations triggered by mouse operations are stopped
     */
    public void setIgnoreZoomOps(boolean ignore) {
        mouseActions.setIgnoreZoomOps(ignore);
    }


    /**
     * Reset zoom, display the initial range of the spectrum
     * extended classes should call: super.resetZoom();
     */
    public void resetZoom() {
    }


    /**
     * Zooms the displayed range of the spectrum
     * @param startX minimum x value displayed
     * @param endX maximum x value displayed
     */
    public void zoom(double startX, double endX) {
    }


    /**
     * Updates the displayed range of the spectrum
     * @param startX minimum x value displayed
     * @param endX maximum x value displayed
     * @return TRUE if data updated
     */
    public boolean updateZoomData(double startX, double endX) {
        return false;
    }


    /**
     * Notify the renderer that an external frame was closed
     * @param frameID the frame ID
     */
    public void setExtraFrameClosed(String frameID) {
    }


    /**
     *Invoked when a key has been typed.
     * @param e the KeyEvent
     */
    public void keyPressed(KeyEvent e) {
        // by default nothing happens
    }


    /**
     *Invoked when left mouse button pressed
     * @param mouseX mose position x
     * @param mouseY mose position y
     */
    public void leftPressed(int mouseX, int mouseY) {
        // by default nothing happens
    }


    /**
     *Invoked when left mouse button pressed and mouse moved
     * @param mouseX mose position x
     * @param mouseY mose position y
     */
    public void leftDragged(int mouseX, int mouseY) {
        // by default nothing happens
    }


    /**
     * Invoked when left mouse button released
     * @param mouseX mose position x
     * @param mouseY mose position y
     */
    public void leftReleased(int mouseX, int mouseY) {
        // by default nothing happens
    }
    
    /**
     * Does a peakpicking in the given range
     * @param mouseX mose position x
     * @param mouseY mose position y
     */    
    public void pickpeaks(double startX, double endX){
    	//by default nothing happens
    }
    
	/**
	 * Returns the peaks in case the applet was used as a peak selection tool (see peakpicer.html in demo)
	 * 
	 * @return The peaks
	 */
    public ArrayList getPickedPeaks(){
    	//nothing happens
    	return null;
    }
    
    /* (non-Javadoc)
     * @see org.openscience.nmrshiftdb.spectrumapplet.renderer.nmr.NmrSpecRenderer#isHelpOrOptionsVisible()
     */
    public boolean isHelpOrOptionsVisible(){
    	if(!help.isVisble() && !options.isVisble())
    		return false;
    	else
    		return true;
    }
}
