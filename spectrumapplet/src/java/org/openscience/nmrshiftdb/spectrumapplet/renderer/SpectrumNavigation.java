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
 * Created on 16. November 2004
 */
package org.openscience.nmrshiftdb.spectrumapplet.renderer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Panel;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import org.openscience.nmrshiftdb.spectrumapplet.data.AbstractDataContainer;
import org.openscience.nmrshiftdb.spectrumapplet.data.DataContainer;
import org.openscience.nmrshiftdb.spectrumapplet.data.ir.RawIRdata;
import org.openscience.nmrshiftdb.spectrumapplet.data.nmr.NmrData;
import org.openscience.nmrshiftdb.spectrumapplet.data.nmr.NmrPeak;
import org.openscience.nmrshiftdb.spectrumapplet.data.nmr.Prediction;
import org.openscience.nmrshiftdb.spectrumapplet.data.nmr.RawNmrData;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.dialog.OptionDialogExt;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.ir.IrRawDataDetailSpectrum;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.ir.IrRawDataNavigationSpectrum;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.nmr.AbstractRenderer;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.nmr.NmrDetailSpectrum;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.nmr.NmrNavigationSpectrum;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.nmr.NmrRawDataDetailSpectrum;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.nmr.NmrRawDataNavigationSpectrum;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.nmr.NmrSpecRenderer;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.painter.CorrelationControler;
import org.openscience.nmrshiftdb.spectrumapplet.util.CommonUtils;
import org.openscience.nmrshiftdb.spectrumapplet.util.ParserUtil;
import org.openscience.nmrshiftdb.spectrumapplet.util.SpectrumApplet;

/**
 * Provides a splitted spectrum display:
 *  -  on the top there is a navigation view of the spectrum, complete range (-10 to 250 ppm)
 *  -  in the middle there is an optional detailed spectrum view for experimental spectrum
 *     providing different display options
 *  -  on the bottom there is the detailed spectrum view for predicted spectrum
 *     providing different display options
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public class SpectrumNavigation extends JPanel implements SpectrumControler {
    private SpectrumApplet applet;
    private SpectrumRenderer nav;
    private SpectrumRenderer sprExperimental;
    private SpectrumRenderer sprPrediction;
    private Dimension size;
    private boolean sizeDefined           = false;
    private CorrelationControler corrCalc;
	JExternalFrame jexf = null;
	private boolean peakpicker=false;
	private ScrollPane sp;
	DefaultListModel listModel;
	JList peaklist;

    
    /**
    * Creates a new instance of SpectrumNavigation
    * @param specType the spectrum type, if not specified default 13Cspectrum is used
    * @param specFrequency the spectrometer frequency
    * @param solvent the solvent
    * @param predictSpec the data for predicted spectrum
    * @param predictionHistogram the data used for prediction
    * @param experimentalSpec the data for experimental spectrum
    * @param model the spectrum model
    * @param a the applet displaying the spectra
    */
    public SpectrumNavigation(String specType, String specFrequency, String solvent, String predictSpec,
                              String predictionHistogram, String experimentalSpec, SpectrumModel model,
                              SpectrumApplet a, boolean isgraphic, boolean peakpicker, Color bgColor) {
        boolean unknownFreq = false;
        this.peakpicker=peakpicker;
        if (specType == null || specType.length() == 0 || !specType.trim().equals(NmrData.SPECTRUM_1H)) {
            specType = DataContainer.SPECTRUM_13C;
        }
        double freq = 100;
        if (specFrequency != null && specFrequency.length() != 0) {
            specFrequency = specFrequency.replace(',','.').trim();
            try {
                freq = Double.parseDouble(specFrequency);
            }
            catch (Exception e){
                freq = 100;
                unknownFreq = true;
                //e.printStackTrace();
            }
        }else{
        	unknownFreq = true;
        }
        NmrPeak[] p        = ParserUtil.parseSpectrum(predictSpec, specType);
        Prediction[] pred  = ParserUtil.parsePrediction(predictionHistogram);
        NmrData pPredicted = new NmrData(p, specType, freq, solvent);
        pPredicted.setSpectrumProperty(pPredicted.UNKNOWN_FREQ, String.valueOf(unknownFreq));
        if ((pred != null)) {
            pPredicted.setPredictions(pred);
        }
        NmrData pExp = null;
        p = ParserUtil.parseSpectrum(experimentalSpec, specType);
        if (p != null) {
            pExp = new NmrData(p, specType, freq, solvent);
            pExp.setSpectrumProperty(pExp.UNKNOWN_FREQ, String.valueOf(unknownFreq));
        }
        init(pPredicted, pExp, model, a, isgraphic, peakpicker, bgColor);
    }


    /**
    * Creates a new instance of SpectrumNavigation
    * @param specType list of peaks
    * @param specFrequency the spectrometer frequency
    * @param solvent the solvent
    * @param predictSpec the data for predicted spectrum
    * @param predictionHistogram the data used for prediction
    * @param experimentalSpec the data for experimental spectrum
    * @param a the applet displaying the spectra
    */
    public SpectrumNavigation(String specType, String specFrequency, String solvent, String predictSpec,
                              String predictionHistogram, String experimentalSpec, SpectrumApplet a, boolean isgraphic, boolean peakpicker, Color bgColor) {
        this(specType, specFrequency, solvent, predictSpec, predictionHistogram, experimentalSpec, null, a, isgraphic,peakpicker, bgColor);
    }


    /**
    * Creates a new instance of SpectrumNavigation
    * @param p list of peaks
    * @param m the spectrum model
    * @param a the applet displaying the spectra
    * @param isgraphic flag used to paint graphic (e.g. for images)
    */
    public SpectrumNavigation(AbstractDataContainer p, SpectrumModel m, SpectrumApplet a, boolean isgraphic, Color bgColor) {
        init(p, null, m, a, isgraphic,false, bgColor);
    }

    private SpectrumRenderer initRenderer(AbstractDataContainer dc, SpectrumModel m,
                                          boolean isNavigationSpec, boolean setExtendedOptionDialog, boolean isgraphic, boolean peakpicker, Color bgColor) {
        SpectrumRenderer r = null;
        boolean is13C      = dc.is13Cspectrum();
        if (isNavigationSpec) {
            m.setIsJmodulated(is13C); // depends on spectype
            //m.setTitle("Navigation");
            m.setDisplayMode(m.HISTO);
            m.setColoredPeaks(false);
            m.setBackGround(bgColor);
            m.setOffsets(new int[] { 25, 5, 15, 25 });
        }
        if (dc instanceof NmrData) {
            // set default linewidth
            if (is13C) {
                m.setLineWidth(CommonUtils.getNumberProperty("lw(13C)", 1));
            }
            else {
                m.setLineWidth(CommonUtils.getNumberProperty("lw(1H)", 0.5));
            }
            r = new NmrDetailSpectrum(m, dc, isgraphic,peakpicker);
            if (isNavigationSpec) {
                r = new NmrNavigationSpectrum(m, dc,this);
            }
            if (setExtendedOptionDialog) {
                ((NmrDetailSpectrum) r).setOptionDialog(new OptionDialogExt(r, is13C));
            }
        }
        else if (dc instanceof RawNmrData) {
            if (isNavigationSpec) {
                NmrPeak[] p = (NmrPeak[]) ((RawNmrData) dc).getPeaks();
                if (p != null) {
                    NmrData d = new NmrData(p, ((RawNmrData) dc).getSpectrumProperty(dc.SPEC_TYPE),
                                            ((RawNmrData) dc).getNumberProperty(dc.FREQ), null);
                    r = new NmrNavigationSpectrum(m, d);
                }
                else {
                    r = new NmrRawDataNavigationSpectrum(m, dc);
                }
            }
            else {
                r = new NmrRawDataDetailSpectrum(m, dc, isgraphic,peakpicker);
            }
        }
        else if (dc instanceof RawIRdata) {
            if (isNavigationSpec) {
                r = new IrRawDataNavigationSpectrum(m, dc);
            }
            else {
                r = new IrRawDataDetailSpectrum(m, dc);
            }
        }
        r.setControler(this);
        return r;
    }


    /**
     * Creates a new instance of SpectrumNavigation
     * @param pExperimental list of peaks for experimental spectrum
     * @param pPrediction list of peaks for predicted spectrum
     * @param model the spectrum model
     * @param a the applet displaying the spectra
     * @param isgraphic flag used to paint graphic (e.g. for images)
     */
    private void init(AbstractDataContainer pPrediction, AbstractDataContainer pExperimental,
                      SpectrumModel model, SpectrumApplet a, boolean isgraphic, boolean peakpicker, Color bgColor) {
        applet = a;
        SpectrumModel mPred = model;
        if (mPred == null) {
            mPred = new SpectrumModel();
            mPred.setIsJmodulated(false);
            mPred.setDisplayMode(mPred.REALISTIC);
            //mPred.setTitle("Prediction");
        }
        SpectrumModel mExp = (SpectrumModel) mPred.clone();
        mExp.setTitle("Experimental");
        AbstractDataContainer pNavigation = null;
        if (pPrediction instanceof NmrData) {
            pNavigation = pPrediction.clone(false);
        }
        else {
            // for raw data spec use the same DataContainer
            pNavigation = pPrediction;
        }

        //initialize predicted spectrum display:
        sprPrediction = initRenderer(pPrediction, mPred, false, true, isgraphic, peakpicker, bgColor);
        //initialize experimental spectrum display:
        if (pExperimental != null) {
            // use experimental data for navigation view:
            if (pExperimental instanceof NmrData) {
                pNavigation = pExperimental.clone(false);
            }
            else {
                // for raw data spec use the same DataContainer
                pNavigation = pExperimental;
            }
            sprExperimental = initRenderer(pExperimental, mExp, false, true, isgraphic, peakpicker, bgColor);
        }

        //initialize navigation spectrum display:
        SpectrumModel mNav = new SpectrumModel();
        nav = initRenderer(pNavigation, mNav, true, false, isgraphic,peakpicker, bgColor);
        ((Component) nav).setVisible(mPred.isShowNavigationView());
        if ((sprExperimental != null) && (sprExperimental instanceof NmrDetailSpectrum) &&
                (sprPrediction instanceof NmrDetailSpectrum)) {
            corrCalc = new CorrelationControler((NmrDetailSpectrum) sprPrediction,
                                                (NmrDetailSpectrum) sprExperimental);
        }
        initGraphic(peakpicker);
    }


    private void initGraphic(boolean peakpicker) {
    	setLayout(new FlowLayout());
    	Panel outerpanel=new Panel();
        add(outerpanel);
    	outerpanel.setLayout(new BorderLayout());
        //setMinimumSize(new Dimension(150, 200));
    	outerpanel.add((Component) nav, BorderLayout.NORTH);
        activateKeyListener(sprPrediction);
        if(peakpicker){
            listModel = new DefaultListModel();
            if(sprPrediction instanceof NmrDetailSpectrum)
                ((NmrDetailSpectrum)sprPrediction).setListModel(listModel);
            peaklist=new JList(listModel);
            sp=new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
            sp.add(peaklist);
            JPanel panel=new JPanel();
            panel.setLayout(new BorderLayout());
            panel.add(sp,BorderLayout.CENTER);
            JLabel label=new JLabel("Selected Peaks:");
            panel.add(label,BorderLayout.NORTH);
            JButton button=new JButton("Delete Peak");
            panel.add(button,BorderLayout.SOUTH);
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    sprPrediction.getPickedPeaks().remove(peaklist.getSelectedIndex());
                    listModel.remove(peaklist.getSelectedIndex());
                }
            });
            outerpanel.add(panel,BorderLayout.EAST);
        }
        if (sprExperimental != null) {
        	outerpanel.add((Component) sprExperimental, BorderLayout.CENTER);
        	outerpanel.add((Component) sprPrediction, BorderLayout.SOUTH);
            nav.getZoomMarker().initializeRange(sprExperimental.getMetrics().getXminInitial(),
                                                sprExperimental.getMetrics().getXmaxInitial());
        }
        else {
        	outerpanel.add((Component) sprPrediction, BorderLayout.CENTER);
            nav.getZoomMarker().initializeRange(sprPrediction.getMetrics().getXminInitial(),
                                                sprPrediction.getMetrics().getXmaxInitial());
        }
    }


    public void activateKeyListener(final SpectrumRenderer graph) {
        if (applet != null) {
            ((JApplet) applet).addKeyListener(new KeyAdapter() {
                    public void keyTyped(KeyEvent e) {
                        graph.keyPressed(e);
                    }
                });
        }
    }


    /**
     * Calculates the correlation lines if experimental and predicted spectrum are shown
     */
    public void calcCorrelationLines() {
        if (corrCalc != null) {
            corrCalc.calcCorrelationLines();
        }
    }


    /**
     * Paint correlation lines between experimental and predicted spectrum
     * @param sender the renderer calling this method
     * @param g2 the grahics context of sender
     */
    public void paintCorrelationLines(NmrSpecRenderer sender, Graphics2D g2) {
        if (corrCalc != null) {
            corrCalc.paintCorrelationLines(sender, g2);
        }
    }


    /**
     * Toggle the state for correlation lines display
     */
    public void toggleCorrelation() {
        if (corrCalc != null) {
            corrCalc.toggleCorrelation();
            updateViews();
        }
    }


    /**
     * Return all detail spectrums displayed (predicted and if defined experimental)
     * @return array of DetailSpectrum
     */
    public NmrSpecRenderer[] getDetailSpectrums() {
        return new NmrSpecRenderer[] { (NmrSpecRenderer) sprPrediction, (NmrSpecRenderer) sprExperimental };
    }


    /**
     * Set parameter read in applet
     * @param paramNo parameter number,
     *                see SpectrumApplet.APPLET_PARAM: paramNo = index - OPTIONAL_INDEX;
     * @param value the parameter value
     */
    public void setAppletParameter(int paramNo, String value) {
        setAppletParameter(sprPrediction, paramNo, value);
        if (sprExperimental != null) {
            setAppletParameter((NmrDetailSpectrum) sprExperimental, paramNo, value);
        }
    }


    private void setAppletParameter(SpectrumRenderer r, int paramNo, String value) {
        boolean b       = new Boolean(value).booleanValue();
        SpectrumModel m = r.getSpectrumModel();
        switch (paramNo) {
            case 0:
                // "realisticLines"
                if (b) {
                    m.setDisplayMode(m.REALISTIC);
                }
                else {
                    m.setDisplayMode(m.HISTO);
                }
                break;
            case 1:
                // "jModulation"
                m.setIsJmodulated(b);
                r.updateBorderValues();
                break;
            case 2:
                // "show Couplings"
                m.setShowCoupling(b);
                break;
            case 3:
                // "showIntegral"
                m.setShowIntegral(b);
                break;
            case 4:
                // "hideNavigation"
                ((Component) nav).setVisible(!b);
                m.setShowNavigationView(!b);
                break;
            case 5:
                // "autoIntensity"
                m.setAutoIntensity(b);
                break;
            case 6:
                // "line width"
                m.setLineWidth(Double.parseDouble(value));
                break;
            case 7:
                //"showRoofEffect"
                m.setRoofEffectEnabled(b);
            case 8:
                //"coloredPeaks for 13C"
                m.setColoredPeaks(b);
            case 9:
                //"show correlation for experimental-predicted spectrum"
                m.setShowCorrelation(b);
        }
    }


    /**
     * Paints this component, the size of the spectrums is calculated and set
     * @param g the graphis context
     */
    public void paintComponent(Graphics g) {
        Dimension d = getSize();
        if ((d != null) && (!d.equals(size))) {
            size = d;
            calcSpectrumSizes();
        }
        if (sizeDefined) {
            super.paintComponent(g);
        }
    }


    private void calcSpectrumSizes() {
        sizeDefined = true;
        int min = 150;
        int h   = size.height;
        int w   = size.width;
        if(peakpicker)
        	w-=100;
        if (h != 0) {
            if (h < 530) {
                min = h / 7 * 2;
            }
            if (min < 100) {
                min = 100;
            }
        }
        Dimension dd = new Dimension(w, min);
        Dimension peaklistd=new Dimension(100,min);
        if (!((Component) nav).isVisible()) {
            min = 0;
            //dd  = new Dimension(0, 0);
        }
        setSize(nav, dd,peaklistd);
        if (sprExperimental != null) {
            int z        = ((h - min) / 2);
            Dimension d1 = new Dimension(w, z);
            setSize(sprExperimental, d1,peaklistd);
            z = (h - min) - z;
            Dimension d2 = new Dimension(w, z);
            setSize(sprPrediction, d2,peaklistd);
        }
        else {
            Dimension d2 = new Dimension(w, h - min);
            setSize(sprPrediction, d2,peaklistd);
        }

        // => revalidate to update size info for other specs
        revalidate();
    }


    private void setSize(SpectrumRenderer r, Dimension d, Dimension d2) {
        ((AbstractRenderer) r).setSize(d);
        ((AbstractRenderer) r).setMinimumSize(d);
        ((AbstractRenderer) r).setMaximumSize(d);
        ((AbstractRenderer) r).setPreferredSize(d);
        if(sp!=null){
	        sp.setSize(d2);
	        sp.setMinimumSize(d2);
	        sp.setMaximumSize(d2);
	        sp.setPreferredSize(d2);
        }
    }


    /**
     * Called by a renderer if a zooming action is called,
     * the display of all renderers will be updated
     * @param renderer the renderer calling the zoom transaction
     * @param xStart minimum x value displayed
     * @param xEnd maximum x value displayed
     */
    public void zoom(NmrSpecRenderer renderer, double xStart, double xEnd) {
        double[] r = new double[] { xStart, xEnd };
        nav.updateZoomData(xStart, xEnd);
        if (sprPrediction.updateZoomData(xStart, xEnd)) {
            sprPrediction.forceRecalculation();
        }
        if (sprExperimental != null) {
            if (sprExperimental.updateZoomData(xStart, xEnd)) {
                sprExperimental.forceRecalculation();
            }
        }
    }


    /**
     * Recalc and update spectrum views
     */
    public void updateViews() {
        nav.doRecalculation();
        sprPrediction.doRecalculation();
        if (sprExperimental != null) {
            sprExperimental.doRecalculation();
        }

        // repaint all specs
        nav.repaint();
        sprPrediction.repaint();
        if (sprExperimental != null) {
            sprExperimental.repaint();
        }
    }


    /**
     * Update the data for the peak highlighters;
     * if the specified atom number == Integer.MIN_VALUE the highlighters are reset
     * @param atomNo the atom number of highlighted peak
     */
    public void updateAllPeakHighlighter(int atomNo) {
        ((NmrDetailSpectrum) sprPrediction).updatePeakHighlighter(atomNo);
        if (sprExperimental != null) {
            ((NmrDetailSpectrum) sprExperimental).updatePeakHighlighter(atomNo);
        }
    }


    /**
     * Called by a renderer if a zooming action is finished,
     * the display of the other renderers will be updated
     * @param renderer the renderer that finished a zoom transaction
     */
    public void zoomingFinished(NmrSpecRenderer renderer) {
    }


    /**
     * Called by a renderer if a zooming action is started,
     * the display of the other renderers will be updated
     * @param renderer the renderer that finished a zoom transaction
     * @param startX the minimal x-value displayed in the renderer
     */
    public void zoomingStarted(NmrSpecRenderer renderer, double startX) {
    }


    /**
     * Returns the actual range of x-values displayed
     * @return the actual range of x-values displayed:
     *   [0]= minimal x-value
     *   [1]= maximal x-value
     */
    public double[] getActualZoomRange() {
        return sprPrediction.getZoomMarker().getRange();
    }


    /**
     * Highlights specified atoms (in structure)
     * @param atomNumbers the atoms to highlight
     * @param requestFocus if TRUE the applet shall request the focus
     */
    public void highlightAtoms(int[] atomNumbers, boolean requestFocus) {
        if (applet != null) {
            applet.highlightAtomsInStructur(atomNumbers);
            if (requestFocus) {
                // if at least one of the predictions is NOT already displayed:                                 
                // request focus ==> react on keyboard events to open prediction distribution display
                ((JApplet) applet).requestFocus();
            }
            applet.highlightRowsInTable(atomNumbers);
        }
        else {
            if (requestFocus) {
                this.getFocusCycleRootAncestor().requestFocus();
            }
        }
    }


    /**
     * Highlight the specified atom, previous higlights are deleted
     * @param atomNo the atom number of the highlighted atom
     */
    public void highlightPeakForAtom(int atomNo) {
        ((NmrDetailSpectrum) sprPrediction).highlightPeakForAtom(atomNo);
        if (sprExperimental != null) {
            ((NmrDetailSpectrum) sprExperimental).highlightPeakForAtom(atomNo);
        }
    }


    /**
     * Display a status information text at the bottom of the spectrum
     * @param info the status information text
     */
    public void printStatusInfo(String info) {
        sprPrediction.printStatusInfo(info, Color.red);
        if (sprExperimental != null) {
            sprExperimental.printStatusInfo(info, Color.red);
        }
    }


    /**
     * Called by a renderer if zooming actions are blocked or allowed again,
     * e.g. if one renderer shows a dialog
     * @param sender the renderer that block/allow a zooming
     * @param zoomAllowed if TRUE zooming actions are allowed
     */
    public void ignoreZoom(NmrSpecRenderer sender, boolean zoomAllowed) {
        if (sender instanceof NmrNavigationSpectrum) {
            sprPrediction.setIgnoreZoomOps(zoomAllowed);
            if (sprExperimental != null) {
                sprExperimental.setIgnoreZoomOps(zoomAllowed);
            }
        }
        else {
            nav.setIgnoreZoomOps(zoomAllowed);
            if (sender == sprPrediction && sprExperimental != null) {
                // called by predicted spectrum:
                sprExperimental.setIgnoreZoomOps(zoomAllowed);
            }
            else {
                // called by experimental spectrum:
                sprPrediction.setIgnoreZoomOps(zoomAllowed);
            }
        }
    }


    /**
     * Set options
     * @param options array of boolean values
     */
    public void setOptions(boolean[] options) {
        //  options[0] = isNavigation view visible
        sprPrediction.getSpectrumModel().setShowNavigationView(options[0]);
        if (sprExperimental != null) {
            sprExperimental.getSpectrumModel().setShowNavigationView(options[0]);
        }
        if (((Component) nav).isVisible() != options[0]) {
            ((Component) nav).setVisible(options[0]);
            calcSpectrumSizes();
            //repaint();
            updateViews();
        }
    }
    
	/**
	 * @return Returns the jexf.
	 */
	public JExternalFrame getJexf() {
		if (jexf == null)
			jexf = new JExternalFrame();
		return jexf;
	}
	
	/**
	 * Returns the peaks in case the applet was used as a peak selection tool (see peakpicer.html in demo)
	 * 
	 * @return The peaks
	 */
	public ArrayList getPickedPeaks(){
		return sprPrediction.getPickedPeaks();
	}
}
