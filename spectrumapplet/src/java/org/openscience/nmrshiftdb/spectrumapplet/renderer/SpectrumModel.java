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
package org.openscience.nmrshiftdb.spectrumapplet.renderer;

import org.openscience.nmrshiftdb.spectrumapplet.util.*;

import java.awt.*;

/**
 * Class handles settings for <code>SpecRenderer</code>
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public class SpectrumModel {
    private String title                        = ""; // Main title
    private String xtitle                       = "[ppm]"; // Title of the x axis
    private String ytitle                       = ""; // Title of the y axis

    /** Paints the spectra with histogram lines */
    public final static int HISTO               = 0;
    /** Paints the spectra using  a Lorentz equation for peaks */
    public final static int REALISTIC           = 1;
    private int displaymode;

    // peak line width in Hz
    private double lineWidth                    = 1;
    private boolean jModulation;
    private boolean showShiftValues;
    private boolean showCoupling;
    private boolean showIntegral;
    private boolean showNavigationView;
    private boolean autoIntensity;
    private boolean roofEffectEnabled;
    private boolean coloredPeaks ;
    private boolean showCorrelation;
    private transient Color backGround ;
    private transient Color backGroundHighlight;
    private transient Color highlightColor;
    private transient String fontName;
    private transient int fontSize;
    private transient int yOffsetTop;
    private transient int yOffsetBottom;
    private transient int xOffsetLeft;
    private transient int xOffsetRight;

    public SpectrumModel() {
        loadProperties();
    }

    private void loadProperties() {
        fontName = CommonUtils.getProperty("fontName", "ARIAL");
        fontSize = (int) CommonUtils.getNumberProperty("fontSize", 12);
        double[] d = CommonUtils.getNumberArrayProperty("offsets");
        if (d == null) {
            xOffsetLeft   = 29;
            xOffsetRight  = 19;
            yOffsetTop    = 40;
            yOffsetBottom = 40;
        }
        else {
            xOffsetLeft   = (int) d[0];
            xOffsetRight  = (int) d[1];
            yOffsetTop    = (int) d[2];
            yOffsetBottom = (int) d[3];
            ;
        }
        d = CommonUtils.getNumberArrayProperty("backGround");
        if (d == null) {
            backGround = new Color(206, 206, 206);
        }
        else {
            backGround = new Color((int) d[0], (int) d[1], (int) d[2]);
        }
        d = CommonUtils.getNumberArrayProperty("backGroundHighlight");
        if (d == null) {
            backGroundHighlight = Color.white;
        }
        else {
            backGroundHighlight = new Color((int) d[0], (int) d[1], (int) d[2]);
        }
        d = CommonUtils.getNumberArrayProperty("highlightColor");
        if (d == null) {
            highlightColor = Color.red;
        }
        else {
            highlightColor = new Color((int) d[0], (int) d[1], (int) d[2]);
        }
        String h = "HISTO";
        String dm = CommonUtils.getProperty("displayMode", h);
        if (h.equals(dm.toUpperCase()) ){
            setDisplayMode(HISTO);
        }
        else {
            setDisplayMode(REALISTIC);
        }
        jModulation = CommonUtils.getBooleanProperty("jModulation");
        showShiftValues = CommonUtils.getBooleanProperty("showShiftValues");
        showCoupling = CommonUtils.getBooleanProperty("showCoupling");
        showIntegral = CommonUtils.getBooleanProperty("showIntegral");
        showNavigationView = CommonUtils.getBooleanProperty("showNavigationView");
        autoIntensity = CommonUtils.getBooleanProperty("autoIntensity");
        roofEffectEnabled = CommonUtils.getBooleanProperty("roofEffectEnabled");
        coloredPeaks = CommonUtils.getBooleanProperty("coloredPeaks");
        showCorrelation = CommonUtils.getBooleanProperty("showCorrelation");
    }


    /**
    * Clone the SpectrumModel
    * @return a clone of this model
    */
    public Object clone() {
        SpectrumModel r = new SpectrumModel();
        r.setShowCoupling(showCoupling);
        r.setShowIntegral(showIntegral);
        r.setAutoIntensity(autoIntensity);
        r.setBackGround(backGround);
        r.setColoredPeaks(coloredPeaks);
        r.setShowCorrelation(showCorrelation);
        r.setDisplayMode(displaymode);
        r.setHighlightColor(highlightColor);
        r.setIsJmodulated(jModulation);
        r.setLineWidth(lineWidth);
        r.setOffsets(new int[] { xOffsetLeft, xOffsetRight, yOffsetTop, yOffsetBottom });
        r.setRoofEffectEnabled(roofEffectEnabled);
        r.setShowNavigationView(showNavigationView);
        r.setShowShiftValues(showShiftValues);
        r.setTheFont(getTheFont());
        r.setTitle(title);
        r.setXTitle(xtitle);
        r.setYTitle(ytitle);
        return r;
    }


    /*
     * Return the peak line width in Hz
     * @return the peak line width
     */
    public double getLineWidth() {
        return lineWidth;
    }


    /*
     * Set the peak line width in Hz
     * @param lw the peak line width
     */
    public void setLineWidth(double lw) {
        lineWidth = lw;
    }


    /**
     * Return TRUE if couplings are displayed
     * @return TRUE if couplings are displayed
     */
    public boolean isShowCoupling() {
        return showCoupling;
    }


    /**
     * Set mode for coupling display, if TRUE couplings are displayed
     * @param isEnabled if TRUE couplings are displayed
     */
    public void setShowCoupling(boolean isEnabled) {
        if (isEnabled) {
            // if multipletts are displayed NO peak is colored !!
            setColoredPeaks(false);
        }
        showCoupling = isEnabled;
    }


    /**
     * Set spin-echo/jModulation mode info
     * @param spinE if spin-echo mode is used
     */
    public void setIsJmodulated(boolean spinE) {
        jModulation = spinE;
    }


    /**
     * Return spin-echo mode info
     * @return spin-echo mode info
     */
    public boolean isJmodulated() {
        return jModulation;
    }


    /**
     * If set TRUE the chemical shift values are displayed
     * @param isVisible if TRUE shift values are displayed
     */
    public void setShowShiftValues(boolean isVisible) {
        showShiftValues = isVisible;
    }


    /**
     * Set the main title
     * @param string the title
     */
    public void setTitle(String string) {
        if (string != null) {
            title = string;
        }
    }


    /**
     * Get the main title
     * @return the title
     */
    public String getTitle() {
        return title;
    }


    /**
     * Set the title of the x axis
     * @param string the title
     */
    public void setXTitle(String string) {
        if (string != null) {
            xtitle = string;
        }
    }


    /**
     * Get the title of the x axis
     * @return the title
     */
    public String getXTitle() {
        return xtitle;
    }


    /**
     * Set the title of the y axis
     * @param string the title
     */
    public void setYTitle(String string) {
        if (string != null) {
            ytitle = string;
        }
    }


    /**
     * Get the title of the y axis
     * @return the title
     */
    public String getYTitle() {
        return ytitle;
    }


    public boolean isColoredPeaks() {
        return coloredPeaks;
    }


    public void setColoredPeaks(boolean enabled) {
        coloredPeaks = enabled;
    }


    /**
     * Set the display mode
     * @param mode the display mode
     */
    public void setDisplayMode(int mode) {
        if (mode <= REALISTIC) {
            displaymode = mode;
            if (mode == REALISTIC) {
                setColoredPeaks(false);
            }
            else {
                setColoredPeaks(true);
            }
        }
    }


    /**
     * Get the display mode
     * @return the display mode
     */
    public int getDisplayMode() {
        return displaymode;
    }


    /**
     * Getter for property backGround.
     * @return Value of property backGround.
     */
    public Color getBackGround() {
        return backGround;
    }


    /**
     * Getter for property backGround.
     * @return Value of property backGround.
     */
    public Color getBackGroundHighlighted() {
        return backGroundHighlight;
    }


    /**
     * Setter for property backGround.
     * @param backGround New value of property backGround.
     */
    public void setBackGround(Color backGround) {
        this.backGround = backGround;
    }


    /**
     * Getter for property theFont.
     * @return Value of property theFont.
     */
    public Font getTheFont() {
        return new Font(fontName, Font.PLAIN, fontSize);
    }


    /**
     * Setter for property theFont.
     * @param aFont the font
     */
    public void setTheFont(Font aFont) {
        fontName = aFont.getFontName();
        fontSize = aFont.getSize();
    }


    /**
     * Getter for property highlightColor.
     * @return Value of property highlightColor.
     */
    public Color getHighlightColor() {
        return highlightColor;
    }


    /**
     * Setter for property highlightColor.
     * @param highlightColor New value of property highlightColor.
     */
    public void setHighlightColor(Color highlightColor) {
        this.highlightColor = highlightColor;
    }


    /**
     * Getter for property showIntegral.
     * @return Value of property showIntegral.
     */
    public boolean isShowIntegral() {
        return showIntegral;
    }


    /**
     * Setter for property showIntegral.
     * @param showIntegral New value of property showIntegral.
     */
    public void setShowIntegral(boolean showIntegral) {
        this.showIntegral = showIntegral;
    }


    /**
     * Getter for property xOffsetLeft.
     * @return Value of property xOffsetLeft.
     */
    public int getXoffsetLeft() {
        return xOffsetLeft;
    }


    /**
     * Getter for property xOffsetRight.
     * @return Value of property xOffsetRight.
     */
    public int getXoffsetRight() {
        return xOffsetRight;
    }


    /**
     * Getter for property yOffsetTop.
     * @return Value of property yOffsetTop.
     */
    public int getYoffsetTop() {
        return yOffsetTop;
    }


    /**
     * Getter for property yOffsetBottom.
     * @return Value of property yOffsetBottom.
     */
    public int getYoffsetBottom() {
        return yOffsetBottom;
    }


    /**
     * Set the offset in spectrum display
     * @param offsets array of offsets:  Offset Left   = offsets[0];
     *                   Offset Right  = offsets[1];
     *                   Offset Top    = offsets[2];
     *                   Offset Bottom = offsets[3];
     */
    public void setOffsets(int[] offsets) {
        xOffsetLeft   = offsets[0];
        xOffsetRight  = offsets[1];
        yOffsetTop    = offsets[2];
        yOffsetBottom = offsets[3];
    }


    /**
     * Get auto-intensity feature, if TRUE returned the y-scale is automatically
     * adjusted to the maxmimum intensity of the visible peaks
     * @return if TRUE the y-scale is automatically scaled
     *         if FALSE the global maximum intensties are used for y-scale
     */
    public boolean isAutoIntensity() {
        return autoIntensity;
    }


    /**
     * Set auto-intensity feature, if TRUE the y-scale is automatically
     * adjusted to the maxmimum intensity of the visible peaks
     * @param autoIntensity if TRUE the y-scale is automatically scaled
     *                      if FALSE the global maximum intensties are used for y-scale
     */
    public void setAutoIntensity(boolean autoIntensity) {
        this.autoIntensity = autoIntensity;
    }


    /**
     * Getter for property showNavigationView.
     * @return Value of property showNavigationView.
     */
    public boolean isShowNavigationView() {
        return showNavigationView;
    }


    /**
     * Setter for property showNavigationView.
     * @param showNavigationView New value of property showNavigationView.
     */
    public void setShowNavigationView(boolean showNavigationView) {
        this.showNavigationView = showNavigationView;
    }


    /**
     * Getter for property roofEffectEnabled.
     * @return Value of property roofEffectEnabled.
     */
    public boolean isRoofEffectEnabled() {
        return roofEffectEnabled;
    }


    /**
     * Setter for property roofEffectEnabled.
     * @param roofEffectEnabled New value of property roofEffectEnabled.
     */
    public void setRoofEffectEnabled(boolean roofEffectEnabled) {
        this.roofEffectEnabled = roofEffectEnabled;
    }


    /**
     * Getter for property showCorrelation.
     * @return Value of property showCorrelation.
     */
    public boolean isShowCorrelation() {
        return showCorrelation;
    }


    /**
     * Setter for property showCorrelation.
     * @param showCorrelation New value of property showCorrelation.
     */
    public void setShowCorrelation(boolean showCorrelation) {
        this.showCorrelation = showCorrelation;
    }
}
